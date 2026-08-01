package com.sofar.core.ui.banner

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * 轮播图圆点指示器组件。
 *
 * 用于配合 ViewPager2 或 Banner 呈现当前的滚动页码位置。
 * 支持通过代码动态定制指示器的圆点数量、尺寸、间距、主色调、以及自定义图片样式。
 */
class BannerIndicator @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

  private var itemCount = 0
  private var lastPosition = -1 // 💡 引入记录上一次焦点的指针，用来做高频滑动时的两点局部刷新优化

  @ColorInt private var selectColor = 0xFFFFFFFF.toInt()
  @ColorInt private var unselectColor = 0x99FFFFFF.toInt()
  @ColorInt private var tintColor = 0xFFFFFFFF.toInt()

  private var selectedDrawable: Drawable? = null
  private var unSelectedDrawable: Drawable? = null

  init {
    selectedDrawable = createOvalDrawable(selectColor)
    unSelectedDrawable = createOvalDrawable(unselectColor)
  }

  /**
   * 内部工厂方法：动态绘制纯色小圆点
   */
  private fun createOvalDrawable(@ColorInt color: Int): GradientDrawable {
    return GradientDrawable().apply {
      shape = GradientDrawable.OVAL
      setColor(color)
    }
  }

  /**
   * 外部初始化能力：一键生成所有的指示器圆点项
   */
  fun initIndicatorItems(itemsNumber: Int, width: Int, height: Int, leftMargin: Int, rightMargin: Int) {
    itemCount = itemsNumber
    lastPosition = -1 // 计数重置
    removeAllViews()

    for (i in 0 until itemsNumber) {
      val imageView = ImageView(context).apply {
        layoutParams = LayoutParams(width, height).apply {
          this.leftMargin = leftMargin
          this.rightMargin = rightMargin
        }
        setImageDrawable(unSelectedDrawable)
        setColorFilter(tintColor)
      }
      addView(imageView)
    }
  }

  /**
   * 核心高频动作：切换高亮圆点
   * 🎯 工业级优化：改全量 `for` 循环刷新为精准的【双节点局部更新】，在大高频、大页数滚动中实现超轻量级的 UI 刷新性能。
   */
  fun setIndicator(position: Int) {
    if (position !in 0..<itemCount) return

    // 如果上一次的位置存在，且和新位置不同，先把上一个节点熄灭（设为非高亮样式）
    if (lastPosition in 0 until itemCount && lastPosition != position) {
      (getChildAt(lastPosition) as? ImageView)?.setImageDrawable(unSelectedDrawable)
    }

    // 将当前选中的新节点点亮（设为高亮样式）
    (getChildAt(position) as? ImageView)?.setImageDrawable(selectedDrawable)

    // 将焦点指针移向新位置，以便下一次复用
    lastPosition = position
  }

  /**
   * 动态设置色彩过滤器颜色
   */
  fun setTintColor(@ColorInt color: Int) {
    tintColor = color
  }

  /**
   * 动态设定高亮态的外部图片样式
   */
  fun setSelectedDrawable(@DrawableRes resId: Int) {
    selectedDrawable = ContextCompat.getDrawable(context, resId)
  }

  /**
   * 动态设定默认态的外部图片样式
   */
  fun setUnSelectedDrawable(@DrawableRes resId: Int) {
    unSelectedDrawable = ContextCompat.getDrawable(context, resId)
  }
}
