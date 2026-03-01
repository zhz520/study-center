package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.zhzgo.study.ui.screens.tools.UnitType.*

enum class UnitType(val displayName: String) {
    LENGTH("长度"), WEIGHT("重量")
}

data class UnitItem(val name: String, val factor: Double)

val lengthUnits = listOf(
    UnitItem("米 (m)", 1.0),
    UnitItem("千米 (km)", 1000.0),
    UnitItem("厘米 (cm)", 0.01),
    UnitItem("毫米 (mm)", 0.001),
    UnitItem("英尺 (ft)", 0.3048),
    UnitItem("英寸 (in)", 0.0254),
    UnitItem("英里 (mi)", 1609.34)
)

val weightUnits = listOf(
    UnitItem("千克 (kg)", 1.0),
    UnitItem("克 (g)", 0.001),
    UnitItem("吨 (t)", 1000.0),
    UnitItem("磅 (lb)", 0.453592),
    UnitItem("盎司 (oz)", 0.0283495)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(onBack: () -> Unit) {
    var selectedType by remember { mutableStateOf(LENGTH) }
    
    var inputValue by remember { mutableStateOf("1") }
    var outputValue by remember { mutableStateOf("100") }
    
    var inputUnitIndex by remember { mutableStateOf(0) }
    var outputUnitIndex by remember { mutableStateOf(2) }

    val currentUnits = if (selectedType == LENGTH) lengthUnits else weightUnits

    // Reset indices if switching type
    LaunchedEffect(selectedType) {
        inputUnitIndex = 0
        outputUnitIndex = if (currentUnits.size > 1) 1 else 0
        updateOutput(inputValue, currentUnits[inputUnitIndex], currentUnits[outputUnitIndex]) { outputValue = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单位换算", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Type Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                UnitType.values().forEach { type ->
                    val isSelected = selectedType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input Section
            UnitBox(
                label = "输入",
                value = inputValue,
                onValueChange = { 
                    inputValue = it
                    updateOutput(inputValue, currentUnits[inputUnitIndex], currentUnits[outputUnitIndex]) { out -> outputValue = out }
                },
                units = currentUnits,
                selectedIndex = inputUnitIndex,
                onUnitSelected = { 
                    inputUnitIndex = it 
                    updateOutput(inputValue, currentUnits[inputUnitIndex], currentUnits[outputUnitIndex]) { out -> outputValue = out }
                }
            )

            // Swap Button
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {
                        val tempIdx = inputUnitIndex
                        inputUnitIndex = outputUnitIndex
                        outputUnitIndex = tempIdx
                        inputValue = outputValue 
                        // Note: after swap, output automatically matches because of the logic, 
                        // but let's re-run conversion to be precise
                        updateOutput(inputValue, currentUnits[inputUnitIndex], currentUnits[outputUnitIndex]) { out -> outputValue = out }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(Icons.Filled.SwapVert, contentDescription = "Swap", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // Output Section
            UnitBox(
                label = "结果",
                value = outputValue,
                onValueChange = {},
                readOnly = true,
                units = currentUnits,
                selectedIndex = outputUnitIndex,
                onUnitSelected = { 
                    outputUnitIndex = it 
                    updateOutput(inputValue, currentUnits[inputUnitIndex], currentUnits[outputUnitIndex]) { out -> outputValue = out }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    units: List<UnitItem>,
    selectedIndex: Int,
    onUnitSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 24.sp, 
                    fontWeight = FontWeight.Bold,
                    color = if (readOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )

            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(units[selectedIndex].name, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    units.forEachIndexed { index, unit ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    unit.name, 
                                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                                    color = if (index == selectedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                onUnitSelected(index)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun updateOutput(input: String, fromUnit: UnitItem, toUnit: UnitItem, onResult: (String) -> Unit) {
    val inputVal = input.toDoubleOrNull()
    if (inputVal == null) {
        onResult("")
        return
    }
    
    // Convert to base unit (kg or m)
    val baseVal = inputVal * fromUnit.factor
    // Convert to target unit
    val result = baseVal / toUnit.factor
    
    // Format to avoid long decimals
    val formatted = if (result % 1.0 == 0.0) {
        String.format("%.0f", result)
    } else {
        String.format("%.5f", result).trimEnd('0').trimEnd('.')
    }
    onResult(formatted)
}
