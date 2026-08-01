package com.sofar.core.ui.flowlayout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

/**
 * 标签布局，一行显示不下，自动换到下一行
 */
open class FlowLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

  private val mAllViews = mutableListOf<List<View>>() // 存储所有的VIEW
  private val mLineHeights = mutableListOf<Int>() // 存储每一行的高度

  /**
   * 测量宽高，需要计算每一个子View的宽高，从而得出此从容器的宽高
   */
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val widthSize = MeasureSpec.getSize(widthMeasureSpec)
    val widthMode = MeasureSpec.getMode(widthMeasureSpec)
    val heightSize = MeasureSpec.getSize(heightMeasureSpec)
    val heightMode = MeasureSpec.getMode(heightMeasureSpec)

    var width = 0 // 布局宽度
    var height = 0 // 布局高度

    var lineWidth = 0 // 行宽
    var lineHeight = 0 // 行高

    // 过滤掉 GONE 的子 View，避免空白占用
    val visibleChildren = (0 until childCount)
      .map { getChildAt(it) }
      .filter { it.visibility != View.GONE }

    visibleChildren.forEachIndexed { index, child ->
      measureChild(child, widthMeasureSpec, heightMeasureSpec)

      val lp = child.layoutParams as? MarginLayoutParams ?: return@forEachIndexed
      val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
      val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

      // 换行判断
      if (lineWidth + childWidth + paddingLeft + paddingRight > widthSize) {
        width = max(lineWidth, childWidth)
        height += lineHeight

        lineWidth = childWidth
        lineHeight = childHeight
      } else {
        // 未换行
        lineWidth += childWidth
        lineHeight = max(lineHeight, childHeight)
      }

      // 最后一个元素的处理
      if (index == visibleChildren.lastIndex) {
        width = max(width, lineWidth)
        height += lineHeight
      }
    }

    setMeasuredDimension(
      if (widthMode == MeasureSpec.EXACTLY) widthSize else width + paddingLeft + paddingRight,
      if (heightMode == MeasureSpec.EXACTLY) heightSize else height + paddingTop + paddingBottom
    )
  }

  /**
   * 放置每一个子View的位置
   */
  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    mAllViews.clear()
    mLineHeights.clear()

    var lineViews = mutableListOf<View>()
    var lineWidth = 0
    var lineHeight = 0

    // 同样只处理非 GONE 的子 View
    val visibleChildren = (0 until childCount)
      .map { getChildAt(it) }
      .filter { it.visibility != View.GONE }

    visibleChildren.forEach { child ->
      val lp = child.layoutParams as? MarginLayoutParams ?: return@forEach
      val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
      val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

      // 换行
      if (lineWidth + childWidth + paddingLeft + paddingRight > measuredWidth) {
        mAllViews.add(lineViews)
        mLineHeights.add(lineHeight)

        lineViews = mutableListOf()
        lineWidth = 0
        lineHeight = childHeight
      }
      lineWidth += childWidth
      lineHeight = max(lineHeight, childHeight)

      lineViews.add(child)
    }

    if (visibleChildren.isNotEmpty()) {
      mAllViews.add(lineViews)
      mLineHeights.add(lineHeight)
    }

    // 开始设置 child 的位置
    var left = paddingLeft
    var top = paddingTop

    mAllViews.forEachIndexed { i, currentLineViews ->
      val currentLineHeight = mLineHeights[i]

      currentLineViews.forEach { child ->
        val lp = child.layoutParams as? MarginLayoutParams ?: return@forEach

        val cl = left + lp.leftMargin
        val ct = top + lp.topMargin
        val cr = cl + child.measuredWidth
        val cb = ct + child.measuredHeight
        child.layout(cl, ct, cr, cb)

        left += child.measuredWidth + lp.leftMargin + lp.rightMargin
      }

      left = paddingLeft
      top += currentLineHeight
    }
  }

  /**
   * 用于生成和此容器相匹配的布局参数
   */
  override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
    return MarginLayoutParams(context, attrs)
  }

  override fun generateDefaultLayoutParams(): LayoutParams {
    return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
  }

  override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
    return MarginLayoutParams(p)
  }

  override fun checkLayoutParams(p: LayoutParams?): Boolean {
    return p is MarginLayoutParams
  }
}
