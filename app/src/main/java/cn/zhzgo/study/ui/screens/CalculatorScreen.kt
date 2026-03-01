package cn.zhzgo.study.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var currentOperation by remember { mutableStateOf<String?>(null) }
    var startNewNumber by remember { mutableStateOf(true) }

    val onNumClick = { num: String ->
        if (startNewNumber) {
            display = num
            startNewNumber = false
        } else {
            if (display == "0" && num != ".") {
                display = num
            } else {
                display += num
            }
        }
    }

    val onOpClick = { op: String ->
        if (operand1 == null) {
            operand1 = display.toDoubleOrNull()
        } else if (!startNewNumber) {
            // Evaluate previous operation
            val operand2 = display.toDoubleOrNull()
            if (operand2 != null && currentOperation != null) {
                val result = calculateResult(operand1!!, operand2, currentOperation!!)
                display = formatResult(result)
                operand1 = result
            }
        }
        currentOperation = op
        startNewNumber = true
    }

    val onEqualClick = {
        val operand2 = display.toDoubleOrNull()
        if (operand1 != null && operand2 != null && currentOperation != null) {
            val result = calculateResult(operand1!!, operand2, currentOperation!!)
            display = formatResult(result)
            operand1 = null
            currentOperation = null
            startNewNumber = true
        }
    }

    val onClearClick = {
        display = "0"
        operand1 = null
        currentOperation = null
        startNewNumber = true
    }

    val onDeleteClick = {
        if (!startNewNumber && display.length > 1) {
            display = display.dropLast(1)
        } else if (!startNewNumber) {
            display = "0"
            startNewNumber = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("计算器", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display Area
            Text(
                text = display,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (display.length > 8) 48.sp else 64.sp,
                    fontWeight = FontWeight.Light
                ),
                textAlign = TextAlign.End,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // Button Grid
            val buttons = listOf(
                listOf("C", "DEL", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=", "=") // Combine = visually later
            )

            buttons.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEachIndexed { colIndex, btn ->
                        if (rowIndex == 4 && colIndex == 3) return@forEachIndexed // Skip the extra "="

                        val modifier = if (rowIndex == 4 && colIndex == 2) {
                            Modifier.weight(2.1f) // Equal button spans ~2 columns accounting for spacing
                        } else {
                            Modifier.weight(1f)
                        }

                        val isOp = btn in listOf("÷", "×", "-", "+", "=")
                        val isAction = btn in listOf("C", "DEL", "%")
                        
                        // iOS Calculator inspired colors
                        val containerColor = when {
                            btn == "=" -> MaterialTheme.colorScheme.primary
                            isOp -> MaterialTheme.colorScheme.primaryContainer
                            isAction -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                        val contentColor = when {
                            btn == "=" -> MaterialTheme.colorScheme.onPrimary
                            isOp -> MaterialTheme.colorScheme.onPrimaryContainer
                            isAction -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = modifier
                                .aspectRatio(if (modifier == Modifier.weight(2.1f)) 2.1f else 1f)
                                .clip(if (modifier == Modifier.weight(2.1f)) RoundedCornerShape(percent = 50) else CircleShape)
                                .background(containerColor)
                                .clickable {
                                    when (btn) {
                                        "C" -> onClearClick()
                                        "DEL" -> onDeleteClick()
                                        "÷", "×", "-", "+" -> onOpClick(btn)
                                        "=" -> onEqualClick()
                                        else -> onNumClick(btn)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = btn,
                                fontSize = 32.sp,
                                fontWeight = if (isOp || isAction) FontWeight.Medium else FontWeight.Normal,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun calculateResult(op1: Double, op2: Double, operation: String): Double {
    return when (operation) {
        "+" -> op1 + op2
        "-" -> op1 - op2
        "×" -> op1 * op2
        "÷" -> if (op2 != 0.0) op1 / op2 else Double.NaN
        else -> op2
    }
}

private fun formatResult(result: Double): String {
    if (result.isNaN()) return "Error"
    val i = result.toLong()
    return if (result == i.toDouble()) i.toString() else String.format("%.6f", result).trimEnd('0').trimEnd('.')
}
