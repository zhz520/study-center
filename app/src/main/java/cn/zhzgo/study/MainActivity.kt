package cn.zhzgo.study

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import cn.zhzgo.study.data.UserPreferences
import cn.zhzgo.study.ui.AppNavigation
import cn.zhzgo.study.ui.theme.MyApplicationTheme
import com.tencent.tauth.Tencent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val userPrefs = remember { UserPreferences(context) }
            val appearance by userPrefs.appearance.collectAsState(initial = "system")
            val primaryColorHex by userPrefs.primaryColor.collectAsState(initial = "")

            val darkTheme = when (appearance) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val primaryColorOverride = remember(primaryColorHex) {
                if (primaryColorHex.isNotEmpty()) {
                    try {
                        Color(android.graphics.Color.parseColor(primaryColorHex))
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }

            MyApplicationTheme(
                darkTheme = darkTheme,
                dynamicColor = false,
                primaryColorOverride = primaryColorOverride
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val updateViewModel: cn.zhzgo.study.ui.viewmodels.UpdateViewModel = viewModel()
                    val updateState by updateViewModel.updateState.collectAsState()

                    LaunchedEffect(Unit) {
                        updateViewModel.checkForUpdates(context, isAutoCheck = true)
                    }

                    AppNavigation()

                    if (updateState?.hasUpdate == true && updateState?.data != null) {
                        cn.zhzgo.study.ui.components.UpdateDialog(
                            updateData = updateState!!.data!!,
                            onDismissRequest = { updateViewModel.dismissUpdate() }
                        )
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Tencent.onActivityResultData(requestCode, resultCode, data, null)
    }
}
