package cloud.bjx.mm.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import cloud.bjx.mm.android.repo.ServiceFactory
import cloud.bjx.mm.android.utils.LogUtil
import cloud.bjx.mm.android.utils.Outcome
import cloud.bjx.mm.android.utils.checkId

class LoginViewModel : ViewModel() {

    fun login(userId: String) = liveData {
        try {
            if (userId.checkId()) {
                val bean = ServiceFactory.apiService.fetchToken(userId)
                if (bean.isSuccess()) {
                    LogUtil.d("login success: $bean")
                    emit(Outcome.Success(bean))
                } else {
                    emit(Outcome.Failure(bean.message))
                }
            } else {
                emit(Outcome.Failure("userId invalid"))
            }
        } catch (e: Exception) {
            LogUtil.e("login error", e)
            emit(Outcome.Failure("Login error"))
        }
    }

}