package com.sofar.mlkit.core

/**
 * Describing a frame info.
 */
data class FrameMetadata(
  val width: Int,
  val height: Int,
  val rotation: Int,
) {

  class Builder {
    private var width = 0
    private var height = 0
    private var rotation = 0

    fun setWidth(width: Int) = apply { this.width = width }
    fun setHeight(height: Int) = apply { this.height = height }
    fun setRotation(rotation: Int) = apply { this.rotation = rotation }

    fun build() = FrameMetadata(width, height, rotation)
  }
}