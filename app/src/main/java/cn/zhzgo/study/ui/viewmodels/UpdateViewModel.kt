package cn.zhzgo.study.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.UpdateResponse
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UpdateViewModel : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateResponse?>(null)
    val updateState: StateFlow<UpdateResponse?> = _updateState

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking
    
    fun checkForUpdates(context: android.content.Context, isAutoCheck: Boolean = false) {
        viewModelScope.launch {
            _isChecking.value = true
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                }

                val response = cn.zhzgo.study.network.RetrofitClient.create(context).checkUpdate(currentVersionCode)
                
                if (response.hasUpdate && response.updateType == "patch" && response.data != null) {
                    // It's a Tinker patch, handle it silently
                    cn.zhzgo.study.utils.TinkerManager.downloadAndApplyPatch(context, response.data)
                } else if (response.hasUpdate || !isAutoCheck) {
                    // It's an APK update, show dialog
                    // OR it's a manual check, emit state so UI can show "Already latest" toast
                    _updateState.value = response
                }
            } catch (e: Exception) {
                // Handle network error
                e.printStackTrace()
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun dismissUpdate() {
        _updateState.value = null
    }
}
