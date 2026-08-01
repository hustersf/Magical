package com.sofar.core.ui.flowlayout

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import com.sofar.core.ui.util.dp2px
import com.sofar.core.ui.util.dp2pxInt

/**
 * tag标签布局样例
 * 如果想改变样式，可参考 [refreshView], [updateDrawable]
 */
class FlowTagList @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FlowLayout(context, attrs, defStyleAttr) {

  private var mTags: List<String>? = null
  private var mOnTagClickListener: OnTagClickListener? = null

  // 使用属性的 Setter 代替 setColor(int color)
  var color: Int = 0xFF3DDAE7.toInt()
    set(value) {
      field = value
      // 如果需要改变颜色后立即刷新界面，可以解开下行注释
      // refreshView()
    }

  private val mStrokeWidth: Int = context.dp2pxInt(1.0f)

  /**
   * 设置tag数据源
   */
  fun setTags(tags: List<String>?) {
    mTags = tags
    refreshView()
  }

  private fun refreshView() {
    removeAllViews()
    val tags = mTags ?: return

    tags.forEachIndexed { i, tagText ->
      val tv = TextView(context)
      val lp = MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
      val margin = context.dp2pxInt(5f)
      lp.setMargins(margin, margin, margin, margin)

      tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
      val paddingLeft = context.dp2pxInt(10f)
      val paddingTop = context.dp2pxInt(5f)
      tv.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop)

      updateDrawable(tv)
      updateTextColor(tv)

      tv.text = tagText
      addView(tv, lp)
      initEvent(tv, i)
    }
  }

  private fun updateDrawable(tv: TextView) {
    val radius = context.dp2px(1000f)
    val normalDrawable = GradientDrawable().apply {
      setColor(Color.WHITE)
      setStroke(mStrokeWidth, color)
      cornerRadius = radius
    }

    val pressedDrawable = GradientDrawable().apply {
      setColor(color)
      cornerRadius = radius
    }

    val stateListDrawable = StateListDrawable().apply {
      addState(intArrayOf(-android.R.attr.state_pressed), normalDrawable)
      addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
    }
    tv.background = stateListDrawable
  }

  private fun updateTextColor(tv: TextView) {
    val colors = intArrayOf(color, Color.WHITE)
    val states = arrayOf(
      intArrayOf(-android.R.attr.state_pressed),
      intArrayOf(android.R.attr.state_pressed)
    )
    val colorStateList = ColorStateList(states, colors)
    tv.setTextColor(colorStateList)
  }

  private fun initEvent(tv: TextView, position: Int) {
    tv.setOnClickListener {
      mOnTagClickListener?.onTagClick(tv.text.toString(), position)
    }
  }

  /**
   * tag点击监听
   */
  fun setOnTagClickListener(onTagClickListener: OnTagClickListener?) {
    mOnTagClickListener = onTagClickListener
  }

  interface OnTagClickListener {
    fun onTagClick(text: String, position: Int)
  }
}
