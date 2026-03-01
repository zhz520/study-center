package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val relationMap = mapOf(
    "我,父" to "爸爸",
    "我,母" to "妈妈",
    "我,兄" to "哥哥",
    "我,弟" to "弟弟",
    "我,姐" to "姐姐",
    "我,妹" to "妹妹",
    "我,夫" to "老公",
    "我,妻" to "老婆",
    "我,子" to "儿子",
    "我,女" to "女儿",

    "爸爸,父" to "爷爷",
    "爸爸,母" to "奶奶",
    "爸爸,兄" to "伯父",
    "爸爸,弟" to "叔叔",
    "爸爸,姐" to "姑妈",
    "爸爸,妹" to "姑姑",
    "爸爸,妻" to "妈妈",

    "妈妈,父" to "外公",
    "妈妈,母" to "外婆",
    "妈妈,兄" to "舅舅",
    "妈妈,弟" to "舅舅",
    "妈妈,姐" to "姨妈",
    "妈妈,妹" to "小姨",
    "妈妈,夫" to "爸爸",

    "爷爷,子" to "爸爸/伯父/叔叔",
    "爷爷,女" to "姑妈/姑姑",
    "奶奶,子" to "爸爸/伯父/叔叔",
    "奶奶,女" to "姑妈/姑姑",

    "外公,子" to "舅舅",
    "外公,女" to "妈妈/姨妈",
    "外婆,子" to "舅舅",
    "外婆,女" to "妈妈/姨妈",

    "哥哥,妻" to "嫂子",
    "哥哥,子" to "侄子",
    "哥哥,女" to "侄女",

    "弟弟,妻" to "弟妹",
    "弟弟,子" to "侄子",
    "弟弟,女" to "侄女",

    "姐姐,夫" to "姐夫",
    "姐姐,子" to "外甥",
    "姐姐,女" to "外甥女",

    "妹妹,夫" to "妹夫",
    "妹妹,子" to "外甥",
    "妹妹,女" to "外甥女",

    "伯父,妻" to "伯母",
    "伯父,子" to "堂哥/堂弟",
    "伯父,女" to "堂姐/堂妹",

    "叔叔,妻" to "婶婶",
    "叔叔,子" to "堂哥/堂弟",
    "叔叔,女" to "堂姐/堂妹",
    
    "姑妈,子" to "表哥/表弟",
    "姑妈,女" to "表姐/表妹",
    "姑姑,夫" to "姑父",
    "姑父,子" to "表哥/表弟",
    "姑父,女" to "表姐/表妹",

    "舅舅,妻" to "舅妈",
    "舅舅,子" to "表哥/表弟",
    "舅舅,女" to "表姐/表妹",
    "舅妈,子" to "表哥/表弟",
    "舅妈,女" to "表姐/表妹",

    "姨妈,夫" to "姨父",
    "小姨,夫" to "姨父",
    "姨父,子" to "表哥/表弟",
    "姨父,女" to "表姐/表妹",
    "姨妈,子" to "表哥/表弟",
    "姨妈,女" to "表姐/表妹",

    "老公,父" to "公公",
    "老公,母" to "婆婆",
    "老公,兄" to "大伯子",
    "老公,弟" to "小叔子",
    "老公,姐" to "大姑子",
    "老公,妹" to "小姑子",

    "老婆,父" to "岳父",
    "老婆,母" to "岳母",
    "老婆,兄" to "大舅哥",
    "老婆,弟" to "小舅子",
    "老婆,姐" to "大姨子",
    "老婆,妹" to "小姨子",

    "儿子,妻" to "儿媳",
    "儿子,子" to "孙子",
    "儿子,女" to "孙女",

    "女儿,夫" to "女婿",
    "女儿,子" to "外孙",
    "女儿,女" to "外孙女",
    
    "孙子,妻" to "孙媳",
    "孙子,子" to "曾孙",
    "孙子,女" to "曾孙女",
    
    "外孙,妻" to "外孙媳",
    "外孙,子" to "外曾孙",
    "外孙,女" to "外曾孙女"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KinshipCalculatorScreen(onBack: () -> Unit) {
    var formula by remember { mutableStateOf(listOf("我")) }
    var currentResult by remember { mutableStateOf("我") }

    // Calc Engine
    LaunchedEffect(formula) {
        var result = "我"
        for (i in 1 until formula.size) {
            val op = formula[i]
            val key = "$result,$op"
            result = relationMap[key] ?: "未知亲戚"
            if (result == "未知亲戚") break
        }
        currentResult = result
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("亲戚称呼计算", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formula.joinToString("的"),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "= $currentResult",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentResult == "未知亲戚") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Keyboard Area
            Column(
                modifier = Modifier.weight(0.6f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KinButton("父", Modifier.weight(1f)) { formula = formula + "父" }
                    KinButton("母", Modifier.weight(1f)) { formula = formula + "母" }
                    KinButton("夫", Modifier.weight(1f)) { formula = formula + "夫" }
                    KinButton("妻", Modifier.weight(1f)) { formula = formula + "妻" }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KinButton("兄", Modifier.weight(1f)) { formula = formula + "兄" }
                    KinButton("弟", Modifier.weight(1f)) { formula = formula + "弟" }
                    KinButton("姐", Modifier.weight(1f)) { formula = formula + "姐" }
                    KinButton("妹", Modifier.weight(1f)) { formula = formula + "妹" }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KinButton("子", Modifier.weight(1f)) { formula = formula + "子" }
                    KinButton("女", Modifier.weight(1f)) { formula = formula + "女" }
                    
                    // Backspace Action
                    Button(
                        onClick = {
                            if (formula.size > 1) {
                                formula = formula.dropLast(1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).aspectRatio(1.5f)
                    ) {
                        Icon(Icons.Filled.Backspace, contentDescription = "Backspace")
                    }
                    
                    // Clear Action
                    Button(
                        onClick = { formula = listOf("我") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).aspectRatio(1.5f)
                    ) {
                        Text("C", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun KinButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.aspectRatio(1.5f) // Make buttons somewhat square/rectangular
    ) {
        Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}
