package com.vij.calculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.vij.calculator.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var expression = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setupButtonClickListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun setupButtonClickListeners() {
        binding.layoutMain.children.filterIsInstance<Button>().forEach { button ->
            button.setOnClickListener { handleButtonClick(button.text.toString()) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleButtonClick(buttonText: String) {
        when {
            buttonText.matches(Regex("[0-9]")) || buttonText == "." -> handleNumberInput(buttonText)
            buttonText.matches(Regex("[-+*/]")) -> handleOperatorInput(buttonText)
            buttonText == "=" -> calculateResult()
            buttonText == "C" -> clearCalculator()
        }
    }

    private fun handleNumberInput(number: String) {
        expression += number
        binding.tvResult.text = expression
    }

    private fun handleOperatorInput(operator: String) {
        if (expression.isNotEmpty() && !expression.last().toString().matches(Regex("[-+*/]"))) {
            expression += operator
            binding.tvResult.text = expression
        }
    }

    private fun calculateResult() {
        if (expression.isNotEmpty()) {
            try {
                val result = evaluateExpression(expression)
                binding.tvFormula.text = expression
                binding.tvResult.text = result
                expression = result // Set result as new expression for continued calculations
            } catch (e: Exception) {
                binding.tvResult.text = "Error"
                expression = ""
            }
        }
    }

    private fun clearCalculator() {
        expression = ""
        binding.tvResult.text = "0"
        binding.tvFormula.text = ""
    }

    private fun evaluateExpression(expression: String): String {
        return try {
            val postfix = infixToPostfix(expression)
            val result = evaluatePostfix(postfix)
            result.toString()
        } catch (e: Exception) {
            "Error"
        }
    }

    // Convert infix notation to postfix notation using the Shunting-Yard Algorithm
    private fun infixToPostfix(expression: String): List<String> {
        val output = mutableListOf<String>()
        val operators = Stack<Char>()
        val precedence = mapOf('+' to 1, '-' to 1, '*' to 2, '/' to 2)

        var numberBuffer = ""

        for (char in expression) {
            when {
                char.isDigit() || char == '.' -> numberBuffer += char
                char in precedence -> {
                    if (numberBuffer.isNotEmpty()) {
                        output.add(numberBuffer)
                        numberBuffer = ""
                    }
                    while (operators.isNotEmpty() && precedence[char]!! <= precedence[operators.peek()]!!) {
                        output.add(operators.pop().toString())
                    }
                    operators.push(char)
                }
            }
        }
        if (numberBuffer.isNotEmpty()) output.add(numberBuffer)
        while (operators.isNotEmpty()) output.add(operators.pop().toString())

        return output
    }

    // Evaluate the postfix expression
    private fun evaluatePostfix(postfix: List<String>): Double {
        val stack = Stack<Double>()

        for (token in postfix) {
            when {
                token.matches(Regex("-?\\d+(\\.\\d+)?")) -> stack.push(token.toDouble())
                else -> {
                    val b = stack.pop()
                    val a = stack.pop()
                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> if (b != 0.0) a / b else throw ArithmeticException("Divide by zero")
                        else -> throw IllegalArgumentException("Unknown operator")
                    }
                    stack.push(result)
                }
            }
        }
        return stack.pop()
    }
}
