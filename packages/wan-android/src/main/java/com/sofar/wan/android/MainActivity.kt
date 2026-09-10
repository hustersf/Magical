package com.sofar.wan.android

import android.os.Bundle
import com.sofar.core.ui.activity.BaseUIActivity
import com.sofar.core.legacy.R as legacyR

class MainActivity : BaseUIActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(legacyR.layout.activity_container)

    supportFragmentManager.beginTransaction()
      .replace(legacyR.id.fragment_container, MainFragment())
      .commitAllowingStateLoss()
  }

}