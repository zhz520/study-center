package cn.zhzgo.study.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cn.zhzgo.study.data.UpdateData
import cn.zhzgo.study.utils.UpdateManager

@Composable
fun UpdateDialog(
    updateData: UpdateData,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val isForce = updateData.is_force == 1

    Dialog(
        onDismissRequest = { if (!isForce) onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = !isForce,
            dismissOnClickOutside = !isForce
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "发现新版本 V${updateData.version_name}",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = updateData.update_log ?: "修复了一些已知问题，优化用户体验",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isForce) {
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("稍后更新", color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "开始下载更新...", Toast.LENGTH_SHORT).show()
                            UpdateManager.downloadApk(context, updateData)
                            if (!isForce) {
                                onDismissRequest()
                            }
                        }
                    ) {
                        Text("立即更新")
                    }
                }
            }
        }
    }
}
