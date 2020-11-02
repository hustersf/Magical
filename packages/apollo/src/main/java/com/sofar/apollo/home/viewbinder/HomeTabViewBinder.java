package com.sofar.apollo.home.viewbinder;

import android.view.View;

import com.sofar.apollo.R;
import com.sofar.apollo.home.HomeTabBar;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.utility.ToastUtil;

public class HomeTabViewBinder extends ViewBinder<HomeContext> {

  HomeTabBar tabBar;

  @Override
  protected void onCreate() {
    super.onCreate();
    tabBar = view.findViewById(R.id.home_tab);
  }

  @Override
  protected void onBind(HomeContext data) {
    super.onBind(data);
    tabBar.setOnTabClickListener(new HomeTabBar.OnTabClickListener() {
      @Override
      public void OnTabClick(int index, View view) {
        ToastUtil.startShort(context, "点击：" + index);
      }
    });
  }

}
