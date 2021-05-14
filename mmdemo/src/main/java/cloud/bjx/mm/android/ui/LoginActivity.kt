package cloud.bjx.mm.android.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cloud.bjx.mm.android.R
import cloud.bjx.mm.android.UserSettings
import cloud.bjx.mm.android.databinding.ActivityLoginBinding
import cloud.bjx.mm.android.utils.Outcome
import cloud.bjx.mm.android.utils.ProgressHUD
import cloud.bjx.mm.android.utils.ToastUtil
import cloud.bjx.mm.android.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    var userId: String = ""
    private val mViewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityLoginBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.activity = this
    }

    fun actionLogin() {
        if (TextUtils.isEmpty(userId)) {
            ToastUtil.showShortToast("请输入用户ID")
            return
        }

        ProgressHUD.show(this)
        mViewModel.login(userId).observe(this, {
            ProgressHUD.dismiss()
            when (it) {
                is Outcome.Failure -> ToastUtil.showShortToast(it.message)
                is Outcome.Success -> {
                    UserSettings.setAppKey(it.value.data.appKey)
                    UserSettings.setUserId(userId)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        })
    }

}
