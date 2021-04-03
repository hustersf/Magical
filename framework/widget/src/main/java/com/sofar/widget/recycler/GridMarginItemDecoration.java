package com.sofar.widget.recycler;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 处理 {@link GridLayoutManager} 的分割，控制分割的大小.
 */
public class GridMarginItemDecoration extends RecyclerView.ItemDecoration {

  private final int mSpanCount;
  private final int mItemSpace;
  private final int mLeftSpace;
  private final int mRightSpace;
  private final int mOrientation;

  public GridMarginItemDecoration(int orientation, int spanCount, int itemSpace) {
    this(orientation, spanCount, itemSpace, 0, 0);
  }

  public GridMarginItemDecoration(int orientation, int spanCount, int itemSpace, int leftSpace,
    int rightSpace) {
    this.mOrientation = orientation;
    this.mSpanCount = spanCount;
    this.mItemSpace = itemSpace;
    this.mLeftSpace = leftSpace;
    this.mRightSpace = rightSpace;
  }


  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
    RecyclerView.State state) {
    int position = parent.getChildAdapterPosition(view);
    int itemCount = parent.getAdapter().getItemCount();
    if (mOrientation == RecyclerView.VERTICAL) {
      int column = position % mSpanCount;
      outRect.top = 0;
      outRect.bottom = mItemSpace;
      if (column == 0) {
        outRect.left = mLeftSpace;
        outRect.right = mItemSpace / 2;
      } else if (column == mSpanCount - 1) {
        outRect.left = mItemSpace / 2;
        outRect.right = mRightSpace;
      } else {
        outRect.left = mItemSpace / 2;
        outRect.right = mItemSpace / 2;
      }
    } else {
      int row = position % mSpanCount;
      int column = position / mSpanCount;
      outRect.bottom = mItemSpace;
      if (row == mSpanCount - 1) {
        //实际上不生效，bottom的值虽然是0，但视觉效果还是有mItemSpace的间距
        outRect.bottom = 0;
      }

      outRect.left = column == 0 ? mLeftSpace : mItemSpace / 2;
      outRect.right = column == itemCount / mSpanCount - 1 ? mRightSpace : mItemSpace / 2;
    }
  }
}