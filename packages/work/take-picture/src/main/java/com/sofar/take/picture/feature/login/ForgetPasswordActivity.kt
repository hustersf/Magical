package com.sofar.take.picture.feature.login

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
import com.sofar.take.picture.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class ForgetPasswordActivity : BaseUIActivity() {

  private lateinit var confirmBtn: Button
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
    val view: View = layoutInflater.inflate(R.layout.forget_password_activity, viewGroup, false)
    return view
  }

  private fun initView() {
    confirmBtn = findViewById(R.id.confirm_btn)
    confirmBtn.setOnSingleClickListener {
      showLoading()
      disposables.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          hideLoading()
          finish()
        })
    }
  }

  private fun initData() {
    setTitle(ContextCompat.getString(this, R.string.login_password_forgot))
  }

  private fun initObserve() {
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
  }

  companion object {
    fun launch(context: Context) {
      val intent = Intent(context, ForgetPasswordActivity::class.java)
      context.startActivity(intent)
    }
  }

}