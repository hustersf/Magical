package com.sofar.apollo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sofar.apollo.home.HomeFragment
import com.sofar.apollo.mock.MockManager
import com.sofar.apollo.word.WordDataManager
import com.sofar.core.ui.util.setupEdgeToEdge

class MainActivity : AppCompatActivity() {

  companion object {
    private const val MAX_BACK_PRESS_INTERVAL = 2500
  }

  private var lastBackPressed: Long = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupEdgeToEdge()
    setContentView(R.layout.activity_main)

    supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, HomeFragment())
      .commitAllowingStateLoss()

    WordDataManager.get().words = MockManager.get().convertData(this)
  }

  override fun onBackPressed() {
    val current = System.currentTimeMillis()
    if (current - lastBackPressed < MAX_BACK_PRESS_INTERVAL) {
      finish()
    } else {
      lastBackPressed = current
      Toast.makeText(
        this,
        getString(com.sofar.core.legacy.R.string.back_press_again),
        Toast.LENGTH_SHORT
      ).show()
    }
  }
}
