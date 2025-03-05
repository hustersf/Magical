package com.sofar.wan.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sofar.base.R

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, MainFragment())
      .commitAllowingStateLoss()
  }

}