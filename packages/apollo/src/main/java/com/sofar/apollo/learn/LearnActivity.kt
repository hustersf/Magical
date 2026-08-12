package com.sofar.apollo.learn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sofar.core.legacy.R
import com.sofar.core.ui.util.setupEdgeToEdge

class LearnActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupEdgeToEdge()
    setContentView(R.layout.activity_container)

    supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, LearnFragment())
      .commitAllowingStateLoss()
  }

  companion object {
    @JvmStatic
    fun launch(activity: Activity) {
      val intent = Intent(activity, LearnActivity::class.java)
      activity.startActivity(intent)
    }
  }
}
