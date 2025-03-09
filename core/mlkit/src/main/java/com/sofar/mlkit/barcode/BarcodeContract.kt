package com.sofar.mlkit.barcode

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class BarcodeContract : ActivityResultContract<Unit, String?>() {

  companion object {
    const val KEY_SCAN_RESULT = "scan_result"
  }

  override fun createIntent(context: Context, input: Unit): Intent {
    return Intent(context, BarcodeScanActivity::class.java)
  }

  override fun parseResult(resultCode: Int, intent: Intent?): String? {
    return if (resultCode == Activity.RESULT_OK) {
      intent?.getStringExtra(KEY_SCAN_RESULT)
    } else {
      null
    }
  }
}