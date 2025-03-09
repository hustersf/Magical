package com.sofar.mlkit.barcode

import com.google.mlkit.vision.barcode.common.Barcode

interface BarcodeResult {

  fun success(results: List<Barcode>)

  fun error(e: Exception)
}