package com.sofar.core.ui.image

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import com.sofar.core.ui.R

class RoundImageView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

  init {
    if (attrs != null) {
      //  从 XML 中安全读取自定义属性
      val typedArray =
        context.obtainStyledAttributes(attrs, R.styleable.RoundImageView, defStyleAttr, 0)
      val isCircle = typedArray.getBoolean(R.styleable.RoundImageView_is_circle, false)
      val radiusPx = typedArray.getDimension(R.styleable.RoundImageView_radius, 0f)
      typedArray.recycle()

      // 将解析出来的属性施加到 Material 的 ShapeAppearanceModel 上
      when {
        isCircle -> setCircle()
        radiusPx > 0f -> setRadius(radiusPx)
      }
    }
  }

  /**
   * 在代码中动态设置为正圆形
   */
  fun setCircle() {
    this.shapeAppearanceModel = ShapeAppearanceModel.builder()
      .setAllCornerSizes(ShapeAppearanceModel.PILL)
      .build()
  }

  /**
   * 在代码中动态设置圆角半径（单位：像素）
   */
  fun setRadius(radiusPx: Float) {
    if (radiusPx >= 0f) {
      this.shapeAppearanceModel = ShapeAppearanceModel.builder()
        .setAllCornerSizes(radiusPx)
        .build()
    }
  }
}
