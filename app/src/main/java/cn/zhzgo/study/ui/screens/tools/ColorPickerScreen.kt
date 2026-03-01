package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerScreen(onBack: () -> Unit) {
    var hexInput by remember { mutableStateOf("FF0000") }
    var rInput by remember { mutableStateOf("255") }
    var gInput by remember { mutableStateOf("0") }
    var bInput by remember { mutableStateOf("0") }
    
    val colorFromHex = try {
        Color(android.graphics.Color.parseColor("#$hexInput"))
    } catch (e: Exception) {
        Color.Transparent
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("颜色工具", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorFromHex),
                contentAlignment = Alignment.Center
            ) {
                if (colorFromHex == Color.Transparent) {
                    Text("无效颜色代码", color = MaterialTheme.colorScheme.error)
                }
            }
            
            Text("Hex HEX颜色值", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = hexInput,
                onValueChange = { 
                    hexInput = it.take(6).uppercase() 
                    try {
                        val parsed = android.graphics.Color.parseColor("#$hexInput")
                        rInput = android.graphics.Color.red(parsed).toString()
                        gInput = android.graphics.Color.green(parsed).toString()
                        bInput = android.graphics.Color.blue(parsed).toString()
                    } catch (e: Exception) {}
                },
                prefix = { Text("#") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text("RGB 数值", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = rInput,
                    onValueChange = { updateFromRgb(it, gInput, bInput) { r, g, b -> 
                        rInput = r; hexInput = rgbToHex(r, g, b) 
                    }},
                    label = { Text("R") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = gInput,
                    onValueChange = { updateFromRgb(rInput, it, bInput) { r, g, b -> 
                        gInput = g; hexInput = rgbToHex(r, g, b) 
                    }},
                    label = { Text("G") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = bInput,
                    onValueChange = { updateFromRgb(rInput, gInput, it) { r, g, b -> 
                        bInput = b; hexInput = rgbToHex(r, g, b) 
                    }},
                    label = { Text("B") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

private fun updateFromRgb(rStr: String, gStr: String, bStr: String, update: (String, String, String) -> Unit) {
    if (rStr.length > 3 || gStr.length > 3 || bStr.length > 3) return
    val r = rStr.toIntOrNull()?.coerceIn(0, 255) ?: 0
    val g = gStr.toIntOrNull()?.coerceIn(0, 255) ?: 0
    val b = bStr.toIntOrNull()?.coerceIn(0, 255) ?: 0
    update(if(rStr.isEmpty()) "" else r.toString(), if(gStr.isEmpty()) "" else g.toString(), if(bStr.isEmpty()) "" else b.toString())
}

private fun rgbToHex(rStr: String, gStr: String, bStr: String): String {
    val r = rStr.toIntOrNull() ?: 0
    val g = gStr.toIntOrNull() ?: 0
    val b = bStr.toIntOrNull() ?: 0
    return String.format("%02X%02X%02X", r, g, b)
}
