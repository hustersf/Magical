package com.sofar.core.ui.swip

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.customview.widget.ViewDragHelper
import com.sofar.core.ui.util.dp2pxInt
import com.sofar.core.ui.util.hasNavigationBar
import com.sofar.core.ui.util.navigationBarHeight
import kotlin.math.abs

/**
 * 滑动返回布局,支持上下左右四个方向
 */
class SwipeLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  companion object {
    private const val TAG = "SwipeLayout"
  }

  // 定义滑动方向的注解
  @IntDef(SwipeDirection.LEFT, SwipeDirection.RIGHT, SwipeDirection.UP, SwipeDirection.DOWN)
  @Retention(AnnotationRetention.SOURCE)
  annotation class SwipeDirection {
    companion object {
      const val LEFT = 0
      const val RIGHT = 1
      const val UP = 2
      const val DOWN = 3
    }
  }

  private var listener: OnSwipedListener? = null
  private var dragHelper: ViewDragHelper? = null

  var isEnable = true // 是否支持滑动
  var isFromEdge = false // 是否只支持从边缘滑动

  @SwipeDirection
  var direction: Int = SwipeDirection.RIGHT // 滑动方向

  @FloatRange(from = 0.0, to = 1.0)
  var swipeThreshold = 0.5f // 触发滑动完成的阈值

  private var canFinishSwipe = false

  private var shadowDrawable: Drawable? = null // 边缘绘制的阴影
  private var shadowWidth = 0
  private var viewLeft = 0
  private var viewTop = 0
  private var navigationHeight = 0 // 底部导航栏高度

  init {
    dragHelper = ViewDragHelper.create(this, ViewDragCallback()).apply {
      setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL)
    }
    shadowWidth = context.dp2pxInt(10f)
    if (context.hasNavigationBar) {
      navigationHeight = context.navigationBarHeight
    }
  }

  override fun computeScroll() {
    if (dragHelper?.continueSettling(true) == true) {
      invalidate()
    }
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    return if (isEnable) {
      dragHelper?.shouldInterceptTouchEvent(ev) == true
    } else {
      super.onInterceptTouchEvent(ev)
    }
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    return if (isEnable) {
      dragHelper?.processTouchEvent(event)
      true
    } else {
      super.onTouchEvent(event)
    }
  }

  override fun dispatchDraw(canvas: Canvas) {
    super.dispatchDraw(canvas)
    shadowDrawable?.let { shadow ->
      when (direction) {
        SwipeDirection.LEFT -> {
          shadow.setBounds(width + viewLeft, 0, width + shadowWidth + viewLeft, height)
        }

        SwipeDirection.RIGHT -> {
          shadow.setBounds(viewLeft - shadowWidth, 0, viewLeft, height)
        }

        SwipeDirection.UP -> {
          shadow.setBounds(
            0,
            height + viewTop - navigationHeight,
            width,
            height + viewTop + shadowWidth - navigationHeight
          )
        }

        SwipeDirection.DOWN -> {
          shadow.setBounds(0, viewTop - shadowWidth, width, viewTop)
        }
      }
      shadow.draw(canvas)
    }
  }

  private inner class ViewDragCallback : ViewDragHelper.Callback() {

    /**
     * true表示可以拖动
     */
    override fun tryCaptureView(child: View, pointerId: Int): Boolean {
      return !isFromEdge
    }

    override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
      super.onEdgeDragStarted(edgeFlags, pointerId)
      Log.d(TAG, "onEdgeDragStarted")
      dragHelper?.captureChildView(this@SwipeLayout.getChildAt(0), pointerId)
    }

    override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
      super.onEdgeTouched(edgeFlags, pointerId)
      Log.d(TAG, "edgeFlags:$edgeFlags")
    }

    override fun onViewDragStateChanged(state: Int) {
      super.onViewDragStateChanged(state)
      Log.d(TAG, "onViewDragStateChanged:$state")
      if (canFinishSwipe && state == ViewDragHelper.STATE_IDLE) {
        canFinishSwipe = false
        listener?.onSwipeFinish()
      }
    }

    /**
     * child 位置发生变化时回调
     */
    override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
      super.onViewPositionChanged(changedView, left, top, dx, dy)
      viewLeft = left
      viewTop = top
      var progress = 0f
      if (direction == SwipeDirection.LEFT || direction == SwipeDirection.RIGHT) {
        progress = abs(1.0f * left / width)
      } else if (direction == SwipeDirection.UP || direction == SwipeDirection.DOWN) {
        progress = abs(1.0f * top / height)
      }
      listener?.onSwipeProgress(progress)
      invalidate()
    }

    /**
     * 手指离开当前捕获的View的时候，会回调这个方法
     */
    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
      super.onViewReleased(releasedChild, xvel, yvel)
      Log.d(TAG, "onViewReleased")
      var finalLeft = 0
      var finalTop = 0
      when (direction) {
        SwipeDirection.LEFT -> {
          canFinishSwipe = releasedChild.right < width * swipeThreshold
          finalLeft = -width
        }

        SwipeDirection.RIGHT -> {
          canFinishSwipe = releasedChild.left > width * swipeThreshold
          finalLeft = width
        }

        SwipeDirection.UP -> {
          canFinishSwipe = releasedChild.bottom < height * swipeThreshold
          finalTop = -height
        }

        SwipeDirection.DOWN -> {
          canFinishSwipe = releasedChild.top > height * swipeThreshold
          finalTop = height
        }
      }
      if (!canFinishSwipe) {
        finalLeft = 0
        finalTop = 0
      }
      dragHelper?.settleCapturedViewAt(finalLeft, finalTop)
      invalidate()
    }

    /**
     * 返回一个大于0的数，则横向可滑动
     */
    override fun getViewHorizontalDragRange(child: View): Int {
      return 1
    }

    /**
     * 返回一个大于0的数，则纵向可滑动
     */
    override fun getViewVerticalDragRange(child: View): Int {
      return 1
    }

    /**
     * 控制child横向移动的边界
     */
    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
      var x = 0
      if (direction == SwipeDirection.LEFT) {
        x = when {
          left > 0 -> 0
          -left > width -> -width
          else -> left
        }
      } else if (direction == SwipeDirection.RIGHT) {
        x = when {
          left < 0 -> 0
          left > width -> width
          else -> left
        }
      }
      return x
    }

    /**
     * 控制child纵向移动的边界
     */
    override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
      var y = 0
      if (direction == SwipeDirection.UP) {
        y = when {
          top > 0 -> 0
          -top > height -> -height
          else -> top
        }
      } else if (direction == SwipeDirection.DOWN) {
        y = when {
          top < 0 -> 0
          top > height -> height
          else -> top
        }
      }
      return y
    }
  }

  /**
   * 设置边缘阴影
   */
  fun setEdgeShadow(shadow: Drawable?) {
    shadowDrawable = shadow
  }

  fun setOnSwipedListener(listener: OnSwipedListener?) {
    this.listener = listener
  }

  interface OnSwipedListener {
    fun onSwipeFinish()
    fun onSwipeProgress(progress: Float)
  }
}
