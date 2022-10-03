package com.sofar.wan.android

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.sofar.base.tab.TabFragment
import com.sofar.wan.android.feature.tab.HomeTabFragment
import com.sofar.wan.android.feature.profile.ProfileFragment
import com.sofar.wan.android.feature.tab.NaviTabFragment
import com.sofar.wan.android.feature.tab.ProjectTabFragment
import com.sofar.wan.android.feature.wxarticle.WxArticleTabFragment

class MainFragment : TabFragment() {

  private val list = listOf("首页", "项目", "导航", "公众号", "我")

  override fun getLayoutResId(): Int {
    return return R.layout.main_fragment
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mTabLayout.setSelectedTabIndicator(null)
    mTabLayout.tabGravity = TabLayout.GRAVITY_FILL
    mTabLayout.tabMode = TabLayout.MODE_FIXED
  }

  override fun createFragments(): MutableList<Fragment> {
    val list = mutableListOf<Fragment>()
    list.add(HomeTabFragment())
    list.add(ProjectTabFragment())
    list.add(NaviTabFragment())
    list.add(WxArticleTabFragment())
    list.add(ProfileFragment())
    return list
  }

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    tab.text = list[position]
  }

  override fun enableSmoothScroll(): Boolean {
    return false
  }

}