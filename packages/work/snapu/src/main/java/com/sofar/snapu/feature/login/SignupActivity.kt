package com.sofar.snapu.feature.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import com.sofar.base.app.BaseUIActivity
import com.sofar.base.util.setOnSingleClickListener
import com.sofar.snapu.MainActivity
import com.sofar.snapu.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class SignupActivity : BaseUIActivity() {

  private lateinit var signupBtn: Button
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
    val view: View = layoutInflater.inflate(R.layout.signup_activity, viewGroup, false)
    return view
  }

  private fun initView() {
    signupBtn = findViewById(R.id.sign_up_btn)
    signupBtn.setOnSingleClickListener {
      showLoading()
      disposables.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          hideLoading()
          MainActivity.launch(this)
        })
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
      val intent = Intent(context, SignupActivity::class.java)
      context.startActivity(intent)
    }
  }

}