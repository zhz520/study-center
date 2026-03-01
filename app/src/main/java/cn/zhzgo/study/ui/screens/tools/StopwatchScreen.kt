package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(onBack: () -> Unit) {
    var timeMillis by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf(listOf<Long>()) }
    
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                delay(10L)
                timeMillis += 10L
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("秒表计时", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Timer Display
            val minutes = (timeMillis / 60000).toString().padStart(2, '0')
            val seconds = ((timeMillis % 60000) / 1000).toString().padStart(2, '0')
            val milliseconds = ((timeMillis % 1000) / 10).toString().padStart(2, '0')

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$minutes:$seconds",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = ".$milliseconds",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Button (Reset/Lap)
                Button(
                    onClick = {
                        if (isRunning) {
                            laps = laps + timeMillis
                        } else {
                            timeMillis = 0L
                            laps = emptyList()
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        if (isRunning) Icons.Filled.Flag else Icons.Filled.Stop,
                        contentDescription = if (isRunning) "Lap" else "Reset",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Primary Play/Pause Button
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isRunning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Lap List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(laps.reversed()) { index, lapTime ->
                    val actualIndex = laps.size - index
                    val lapMinutes = (lapTime / 60000).toString().padStart(2, '0')
                    val lapSeconds = ((lapTime % 60000) / 1000).toString().padStart(2, '0')
                    val lapMillis = ((lapTime % 1000) / 10).toString().padStart(2, '0')
                    
                    val previousLapTime = if (actualIndex > 1) laps[actualIndex - 2] else 0L
                    val diff = lapTime - previousLapTime
                    val diffMinutes = (diff / 60000).toString().padStart(2, '0')
                    val diffSeconds = ((diff % 60000) / 1000).toString().padStart(2, '0')
                    val diffMillis = ((diff % 1000) / 10).toString().padStart(2, '0')

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "计次 $actualIndex",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "+$diffMinutes:$diffSeconds.$diffMillis",
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "$lapMinutes:$lapSeconds.$lapMillis",
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp
                            )
                        }
                    }
                    if (index < laps.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}
