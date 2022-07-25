package com.sofar.wan.android.feature.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.sofar.base.tab.TabFragment
import com.sofar.wan.android.feature.category.CategoryFragment
import com.sofar.wan.android.feature.navi.NaviFragment
import com.sofar.wan.android.feature.profile.ProfileFragment
import com.sofar.wan.android.feature.project.ProjectFragment
import com.sofar.wan.android.feature.tutorial.TutorialFragment

class NaviTabFragment : TabFragment() {

  private val list = listOf("导航", "体系", "教程")

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mTabLayout.tabGravity = TabLayout.GRAVITY_CENTER
    mTabLayout.tabMode = TabLayout.MODE_SCROLLABLE
    mTabLayout.isTabIndicatorFullWidth = false
    mTabLayout.tabIndicatorAnimationMode = TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC
  }

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    tab.text = list[position]
  }

  override fun createFragments(): MutableList<Fragment> {
    val list = mutableListOf<Fragment>()
    list.add(NaviFragment())
    list.add(CategoryFragment())
    list.add(TutorialFragment())
    return list
  }

}