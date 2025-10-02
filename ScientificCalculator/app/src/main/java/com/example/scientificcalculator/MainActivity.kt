package com.example.scicalc

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvExpression: TextView
    private lateinit var tvResult: TextView

    private var expr = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvExpression = findViewById(R.id.tvExpression)
        tvResult = findViewById(R.id.tvResult)

        // number buttons
        val idsNumbers = listOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9)
        for (id in idsNumbers) {
            findViewById<Button>(id).setOnClickListener {
                val text = (it as Button).text.toString()
                appendToExpr(text)
            }
        }

        // operators and functions
        mapOf(
            R.id.btnDot to ".",
            R.id.btnPlus to "+",
            R.id.btnMinus to "-",
            R.id.btnMultiply to "*",
            R.id.btnDivide to "/",
            R.id.btnPow to "^",
            R.id.btnSqrt to "sqrt(",
            R.id.btnLn to "ln(",
            R.id.btnLog to "log(",
            R.id.btnSin to "sin(",
            R.id.btnCos to "cos(",
            R.id.btnTan to "tan(",
            R.id.btnParen to "(",
            R.id.btnFactorial to "!",
            R.id.btnPercent to "%",
        ).forEach { (id, token) ->
            findViewById<Button>(id).setOnClickListener {
                appendToExpr(token)
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            expr.clear()
            updateExpr()
            tvResult.text = "0"
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            if (expr.isNotEmpty()) {
                expr.deleteCharAt(expr.length - 1)
                updateExpr()
            }
        }

        findViewById<Button>(R.id.btnPlusMinus).setOnClickListener {
            // naive: prepend unary minus
            expr.insert(0, "u-")
            updateExpr()
        }

        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            evaluateExpression()
        }
    }

    private fun appendToExpr(s: String) {
        expr.append(s)
        updateExpr()
    }

    private fun updateExpr() {
        // visual: replace "u-" with "(-)" for display simplicity
        tvExpression.text = expr.toString().replace("u-","-")
    }

    private fun evaluateExpression() {
        val expression = expr.toString()
        if (expression.isBlank()) return
        try {
            val result = ExpressionEvaluator.evaluate(expression)
            // show rounded if integer-like
            val rText = if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
            tvResult.text = rText
        } catch (e: Exception) {
            tvResult.text = "Error"
        }
    }
}
