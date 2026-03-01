package cn.zhzgo.study

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cn.zhzgo.study.ui.AppNavigation
import cn.zhzgo.study.ui.theme.MyApplicationTheme
import com.tencent.tauth.Tencent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Tencent.onActivityResultData(requestCode, resultCode, data, null)
    }
}
