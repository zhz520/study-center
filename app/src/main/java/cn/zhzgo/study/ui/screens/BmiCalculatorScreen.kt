package cn.zhzgo.study.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiCalculatorScreen(onBack: () -> Unit) {
    var heightStr by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("") }
    var bmiResult by remember { mutableStateOf<Double?>(null) }

    val calculateBmi = {
        val heightCm = heightStr.toDoubleOrNull()
        val weightKg = weightStr.toDoubleOrNull()
        if (heightCm != null && weightKg != null && heightCm > 0) {
            val heightM = heightCm / 100
            bmiResult = weightKg / (heightM * heightM)
        } else {
            bmiResult = null
        }
    }

    @Composable
    fun getBmiStatus(bmi: Double): Pair<String, androidx.compose.ui.graphics.Color> {
        return when {
            bmi < 18.5 -> "偏瘦" to MaterialTheme.colorScheme.tertiary
            bmi < 24.0 -> "正常" to MaterialTheme.colorScheme.primary
            bmi < 28.0 -> "偏胖" to MaterialTheme.colorScheme.error
            else -> "肥胖" to MaterialTheme.colorScheme.error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BMI 计算", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "计算您的身体质量指数",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            OutlinedTextField(
                value = heightStr,
                onValueChange = { heightStr = it },
                label = { Text("身高 (cm)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it },
                label = { Text("体重 (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Button(
                onClick = calculateBmi,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text("计算 BMI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            bmiResult?.let { bmi ->
                val (status, color) = getBmiStatus(bmi)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("您的 BMI 指数为", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format("%.1f", bmi),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            color = color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "身体状态: $status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
