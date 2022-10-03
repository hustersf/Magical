package com.sofar.base.tab;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sofar.base.BaseFragment;
import com.sofar.base.R;

/**
 * TabLayout+ViewPager2
 */
public abstract class TabFragment extends BaseFragment {
  private static final String TAG = "TabFragment";

  protected TabLayout mTabLayout;
  protected ViewPager2 mViewPager2;
  protected FragmentAdapter mAdapter;
  protected TabLayoutMediator mMediator;

  protected int getLayoutResId() {
    return R.layout.base_tab_fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(getLayoutResId(), container, false);
    mTabLayout = rootView.findViewById(R.id.tab_layout);
    mViewPager2 = rootView.findViewById(R.id.view_pager);
    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    List<Fragment> fragments = createFragments();
    mAdapter = new FragmentAdapter(this);
    mViewPager2.setAdapter(mAdapter);
    mAdapter.setFragments(fragments);

    boolean smoothScroll = enableSmoothScroll();
    mViewPager2.setUserInputEnabled(smoothScroll);
    mMediator = new TabLayoutMediator(mTabLayout, mViewPager2, true, smoothScroll,
      (tab, position) -> onConfigureTab(tab, position));
    mMediator.attach();
  }

  /**
   * 是否支持左右滑动
   */
  protected boolean enableSmoothScroll() {
    return true;
  }

  protected abstract void onConfigureTab(@NonNull TabLayout.Tab tab, int position);

  @NonNull
  protected abstract List<Fragment> createFragments();

  public void updateTabs() {
    List<Fragment> fragments = createFragments();
    mAdapter.setFragments(fragments);
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mMediator.detach();
  }
}
