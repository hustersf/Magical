package com.sofar.aurora;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.sofar.aurora.feature.home.HomeFragment;
import com.sofar.aurora.feature.home.HomeTabBar;
import com.sofar.aurora.feature.mine.MineFragment;
import com.sofar.base.BaseActivity;
import com.sofar.utility.statusbar.StatusBarUtil;

public class MainActivity extends BaseActivity {

  @NonNull
  HomeTabBar mTabBar;
  @NonNull
  ViewPager2 mViewPager2;

  List<Fragment> mFragments = new ArrayList<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    StatusBarUtil.setLightMode(this);

    mTabBar = findViewById(R.id.home_tab_bar);
    mViewPager2 = findViewById(R.id.view_pager);

    initTabs();
  }

  private void initTabs() {
    mTabBar.addHomeTab(1, "发现", R.drawable.home_tab_icon_music);
    mTabBar.addHomeTab(2, "我的", R.drawable.home_tab_icon_mine);
    mTabBar.setTabSelected(0);

    HomeFragment homeFragment = new HomeFragment();
    MineFragment mineFragment = new MineFragment();
    mFragments.add(homeFragment);
    mFragments.add(mineFragment);
    MainPagerAdapter adapter = new MainPagerAdapter(this);
    mViewPager2.setAdapter(adapter);
    adapter.setList(mFragments);
    adapter.notifyDataSetChanged();
    mViewPager2.setCurrentItem(0);
    mViewPager2.setUserInputEnabled(false);

    mTabBar.setOnTabBarClickListener((index, view) -> {
      mViewPager2.setCurrentItem(index);
    });
  }

}
