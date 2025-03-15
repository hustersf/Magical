package com.sofar.snapu.feature.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sofar.base.app.BaseUIActivity
import com.sofar.base.util.setOnSingleClickListener
import com.sofar.snapu.MainActivity
import com.sofar.snapu.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class LoginActivity : BaseUIActivity() {

  private lateinit var loginBtn: Button
  private lateinit var signupBtn: Button
  private lateinit var forgotPwdTv: TextView
  private var disposables: CompositeDisposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    initData()
    initObserve()
  }

  override fun onCreateView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?
  ): View {
    val view: View = layoutInflater.inflate(R.layout.login_activity, viewGroup, false)
    return view
  }

  private fun initView() {
    loginBtn = findViewById(R.id.login_btn)
    signupBtn = findViewById(R.id.sign_up_btn)
    forgotPwdTv = findViewById(R.id.forgot_pwd_tv)
    loginBtn.setOnSingleClickListener {
      showLoading()
      disposables.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          hideLoading()
          MainActivity.launch(this)
        })
    }
    signupBtn.setOnSingleClickListener {
      SignupActivity.launch(this)
    }
    forgotPwdTv.setOnSingleClickListener {
      ForgetPasswordActivity.launch(this)
    }
  }

  private fun initData() {
    setTitle(ContextCompat.getString(this, R.string.app_name))
  }

  private fun initObserve() {
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
  }

  companion object {
    fun launch(context: Context) {
      val intent = Intent(context, LoginActivity::class.java)
      context.startActivity(intent)
    }
  }

}