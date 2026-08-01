package com.sofar.apollo.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sofar.apollo.MainActivity
import com.sofar.apollo.SofarApp
import com.sofar.login.Account
import com.sofar.login.model.User
import com.sofar.login.ui.LoginActivity

class SplashActivity : AppCompatActivity() {

  private val loginLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    // 📸 接收时机：当登录页关闭返回时
    if (result.resultCode == Activity.RESULT_OK) {
      val data: Intent? = result.data
      val user = data?.getSerializableExtra(LoginActivity.KEY_USER) as? User

      if (user != null) {
        Account.saveUser(this, user)
        SofarApp.ME = user
        gotoMain()
        return@registerForActivityResult
      }
    }

    // 失败或取消处理
    finish()
    Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (SofarApp.isLogin()) {
      gotoMain()
    } else {
      gotoLogin()
    }
  }

  private fun gotoMain() {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    finish()
  }

  private fun gotoLogin() {
    val intent = Intent(this, LoginActivity::class.java)
    loginLauncher.launch(intent)
  }
}
