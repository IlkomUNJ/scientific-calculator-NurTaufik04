package com.example.scicalc

import kotlin.math.*

// Simple expression evaluator using Shunting-yard + RPN evaluation
object ExpressionEvaluator {

    private val operators = setOf("+","-","*","/","^")
    private val functions = setOf("sin","cos","tan","asin","acos","atan","log","ln","sqrt","exp","abs")

    private fun isNumber(token: String) = token.toDoubleOrNull() != null

    private fun precedence(op: String) = when(op) {
        "+", "-" -> 2
        "*", "/" -> 3
        "^" -> 4
        else -> 0
    }

    private fun isLeftAssociative(op: String) = op != "^"

    // tokenize expression into numbers, operators, parentheses, functions
    fun tokenize(expr: String): List<String> {
        val s = expr.replace("÷","/").replace("×","*").replace("−","-").replace("π", "pi")
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < s.length && (s[i].isDigit() || s[i]=='.')) {
                        sb.append(s[i]); i++
                    }
                    tokens += sb.toString()
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < s.length && s[i].isLetter()) {
                        sb.append(s[i]); i++
                    }
                    tokens += sb.toString()
                }
                c == '(' || c == ')' || c == ',' -> {
                    tokens += c.toString(); i++
                }
                else -> {
                    // handle two-char operators? here only single-char ones used
                    tokens += c.toString(); i++
                }
            }
        }
        return handleUnary(tokens)
    }

    // convert leading unary minus to "u-" token
    private fun handleUnary(tokens: List<String>): List<String> {
        val res = mutableListOf<String>()
        for ((idx, tok) in tokens.withIndex()) {
            if (tok == "-" ) {
                if (idx == 0 || (res.isNotEmpty() && (res.last() in operators || res.last() == "(" || res.last() == ","))) {
                    res += "u-" // unary minus
                } else {
                    res += tok
                }
            } else {
                res += tok
            }
        }
        return res
    }

    // Shunting-yard to RPN
    fun toRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            when {
                isNumber(token) || token == "pi" || token == "e" -> output += token
                functions.contains(token) -> stack.addFirst(token)
                token == "," -> {
                    while (stack.isNotEmpty() && stack.first() != "(") {
                        output += stack.removeFirst()
                    }
                }
                token in operators || token == "u-" || token == "!" || token == "%" -> {
                    // treat ! and % as postfix unary with highest precedence
                    if (token == "!" || token == "%") {
                        while (stack.isNotEmpty() && ((stack.first() == "!" || stack.first() == "%") || (precedence(stack.first()) >= precedence(token)))) {
                            output += stack.removeFirst()
                        }
                        stack.addFirst(token)
                    } else {
                        while (stack.isNotEmpty() && ((stack.first() in operators && (
                                    (isLeftAssociative(token) && precedence(token) <= precedence(stack.first())) ||
                                            (!isLeftAssociative(token) && precedence(token) < precedence(stack.first()))
                                    )) || stack.first() == "!" || stack.first() == "%")) {
                            output += stack.removeFirst()
                        }
                        stack.addFirst(token)
                    }
                }
                token == "(" -> stack.addFirst(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.first() != "(") {
                        output += stack.removeFirst()
                    }
                    if (stack.isNotEmpty() && stack.first() == "(") stack.removeFirst()
                    if (stack.isNotEmpty() && functions.contains(stack.first())) {
                        output += stack.removeFirst()
                    }
                }
                else -> {
                    // unknown token
                    output += token
                }
            }
            i++
        }
        while (stack.isNotEmpty()) output += stack.removeFirst()
        return output
    }

    // Evaluate RPN
    fun evaluateRPN(rpn: List<String>): Double {
        val st = ArrayDeque<Double>()
        for (tok in rpn) {
            when {
                isNumber(tok) -> st.addFirst(tok.toDouble())
                tok == "pi" -> st.addFirst(Math.PI)
                tok == "e" -> st.addFirst(Math.E)
                tok == "u-" -> {
                    val v = st.removeFirst()
                    st.addFirst(-v)
                }
                tok == "+" -> {
                    val b = st.removeFirst(); val a = st.removeFirst(); st.addFirst(a + b)
                }
                tok == "-" -> {
                    val b = st.removeFirst(); val a = st.removeFirst(); st.addFirst(a - b)
                }
                tok == "*" -> {
                    val b = st.removeFirst(); val a = st.removeFirst(); st.addFirst(a * b)
                }
                tok == "/" -> {
                    val b = st.removeFirst(); val a = st.removeFirst(); st.addFirst(a / b)
                }
                tok == "^" -> {
                    val b = st.removeFirst(); val a = st.removeFirst(); st.addFirst(a.pow(b))
                }
                tok == "!" -> {
                    val a = st.removeFirst()
                    st.addFirst(factorial(a))
                }
                tok == "%" -> {
                    val a = st.removeFirst()
                    st.addFirst(a / 100.0)
                }
                functions.contains(tok) -> {
                    val a = st.removeFirst()
                    val res = when(tok) {
                        "sin" -> sin(a)
                        "cos" -> cos(a)
                        "tan" -> tan(a)
                        "asin" -> asin(a)
                        "acos" -> acos(a)
                        "atan" -> atan(a)
                        "log" -> log10(a)
                        "ln" -> ln(a)
                        "sqrt" -> sqrt(a)
                        "exp" -> exp(a)
                        "abs" -> abs(a)
                        else -> Double.NaN
                    }
                    st.addFirst(res)
                }
                else -> throw IllegalArgumentException("Unknown token $tok")
            }
        }
        return if (st.isNotEmpty()) st.first() else 0.0
    }

    private fun factorial(x: Double): Double {
        val n = x.toInt()
        if (n < 0) return Double.NaN
        // factorial for integers; for non-integers we could use gamma, but keep integer only
        var res = 1.0
        for (i in 2..n) res *= i
        return res
    }

    // top-level evaluate string
    fun evaluate(expr: String): Double {
        val tokens = tokenize(expr)
        val rpn = toRPN(tokens)
        return evaluateRPN(rpn)
    }
}
