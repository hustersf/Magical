package com.sofar.login.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.sofar.login.activity.QQSSOActivity

/**
 * 🎯 现代化组件化解耦契约
 * 输入参数：[QQAuth] 外部（Activity）提前 new 好的、属于业务层的空数据实体
 * 返回参数：[QQAuth?] 契约帮你在底层填满 Token 数据后的同一个实体对象
 */
class QQAuthContract : ActivityResultContract<QQAuth, QQAuth?>() {

  // 仅作为单次跳转的临时中转引用，parseResult 消费后立刻置空，绝对不产生内存泄漏
  private var pendingAuthObj: QQAuth? = null

  override fun createIntent(context: Context, input: QQAuth): Intent {
    // 1. 在这里完美接住外部传进来的业务层实体对象
    this.pendingAuthObj = input
    // 2. 利用官方入参里自动送给你的安全 [context]，直接构建 Intent，不需要任何成员变量
    return Intent(context, QQSSOActivity::class.java)
  }

  override fun parseResult(resultCode: Int, intent: Intent?): QQAuth? {
    val authObj = pendingAuthObj
    pendingAuthObj = null // 极其重要：结果一返回，立刻斩断强引用释放内存，防漏气

    if (resultCode != Activity.RESULT_OK || intent == null || authObj == null) {
      return null
    }

    val tokenStr = intent.getStringExtra(QQSSOActivity.KEY_TOKEN)
    val openIdStr = intent.getStringExtra(QQSSOActivity.KEY_OPEN_ID)
    val expiresTime = intent.getLongExtra(QQSSOActivity.KEY_EXPIRES_IN, 0)

    return if (!tokenStr.isNullOrEmpty() && !openIdStr.isNullOrEmpty()) {
      // 🎯 核心解耦点：契约本身是泛型工具人，它不 new 任何业务类。
      // 它只管在业务层送进来的空碗里，把 Token 倒进去，然后完璧归赵丢还给上层！
      authObj.apply {
        this.token = tokenStr
        this.openId = openIdStr
        this.expiresIn = expiresTime
      }
    } else {
      null
    }
  }
}

