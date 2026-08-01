package com.sofar.login.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sofar.login.Account
import com.sofar.login.R
import com.sofar.login.auth.QQAuth
import com.sofar.login.auth.QQAuthContract
import com.sofar.login.model.User

class LoginActivity : AppCompatActivity() {

  companion object {
    // 💡 转换为 Kotlin 标准的伴生对象常量，供外部页面（如 SplashActivity）提取结果
    const val KEY_USER = "user"
  }

  // 🎯 100% 符合官方原生规范：在类头部一行代码静态注册自定义 QQ 授权契约，永不崩溃！
  // 💡 编译器会在底层全自动帮你管理门牌号 Key，免去精神折磨
  private val qqAuthLauncher = registerForActivityResult(QQAuthContract()) { qqAuth ->
    // 📸 接收时机：当契约完璧归赵、帮你把填满 Token 数据的 QQAuth 对象带回来时触发
    if (qqAuth != null) {
      Account.loginWithQQ(qqAuth)
        .subscribe({ user ->
          loginFinish(user) // 顺利换取后台 User 成功，上岸销毁
        }, { throwable ->
          Toast.makeText(this, throwable.toString(), Toast.LENGTH_SHORT).show()
        })
    } else {
      Toast.makeText(this, "QQ授权失败或取消", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.login_activity)

    val qqLogin = findViewById<ImageView>(R.id.qq_login)

    // 🚀 触发时机：用户点击 QQ 登录图标
    qqLogin.setOnClickListener {
      val qqAuth = QQAuth(this)
      // 顺畅流入官方 Launcher 唤醒透明授权页
      qqAuthLauncher.launch(qqAuth)
    }
  }

  private fun loginFinish(user: User) {
    val intent = Intent().apply {
      putExtra(KEY_USER, user)
    }
    setResult(RESULT_OK, intent)
    finish()
  }
}
