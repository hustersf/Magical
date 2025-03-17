package com.sofar.mlkit.barcode

interface BarcodeResult {

  fun success(result: String)

  fun error(e: Exception)
}