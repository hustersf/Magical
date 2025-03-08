package com.sofar.take.picture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sofar.base.app.BaseUIActivity

class MainActivity : BaseUIActivity() {

  override fun onCreateView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?
  ): View {
    val view: View = layoutInflater.inflate(R.layout.main_activity, viewGroup, false)
    return view
  }

  companion object {
    fun launch(context: Context) {
      val intent = Intent(context, MainActivity::class.java)
      context.startActivity(intent)
    }
  }
}