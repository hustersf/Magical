package com.sofar.widget.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 保证子View的中线始终与RecyclerView本身的中线对齐
 * <p>
 * 与PagerSnapHelper的区别在于，不算decoration and margin，保证视觉效果上子View始终在RecyclerView的中间
 */
public class CenterPagerSnapHelper extends PagerSnapHelper {

  @Nullable
  private OrientationHelper mVerticalHelper;
  @Nullable
  private OrientationHelper mHorizontalHelper;

  @Nullable
  @Override
  public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
    @NonNull View targetView) {
    int[] out = new int[2];
    if (layoutManager.canScrollHorizontally()) {
      out[0] = distanceToCenter(layoutManager, targetView,
        getHorizontalHelper(layoutManager));
    } else {
      out[0] = 0;
    }

    if (layoutManager.canScrollVertically()) {
      out[1] = distanceToCenter(layoutManager, targetView,
        getVerticalHelper(layoutManager));
    } else {
      out[1] = 0;
    }
    return out;
  }

  private int distanceToCenter(@NonNull RecyclerView.LayoutManager layoutManager,
    @NonNull View targetView, OrientationHelper helper) {
    final int childCenter = targetView.getLeft() + (targetView.getMeasuredWidth() / 2);
    final int containerCenter = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
    return childCenter - containerCenter;
  }

  @NonNull
  private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
    if (mVerticalHelper == null) {
      mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
    }
    return mVerticalHelper;
  }

  @NonNull
  private OrientationHelper getHorizontalHelper(
    @NonNull RecyclerView.LayoutManager layoutManager) {
    if (mHorizontalHelper == null) {
      mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
    }
    return mHorizontalHelper;
  }
}
