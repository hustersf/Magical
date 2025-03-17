package com.sofar.snapu.feature.daq

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts

class CaptureVideoExtra(private val duration: Int) : ActivityResultContracts.CaptureVideo() {

  override fun createIntent(context: Context, input: Uri): Intent {
    val intent = super.createIntent(context, input)
    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, duration)
    return intent
  }
}