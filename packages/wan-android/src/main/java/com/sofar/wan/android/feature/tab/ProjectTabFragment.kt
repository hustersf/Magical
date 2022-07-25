package com.sofar.wan.android.feature.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.sofar.base.tab.TabFragment
import com.sofar.wan.android.feature.project.ProjectFragment

class ProjectTabFragment : TabFragment() {

  private val title = listOf("首页", "广场", "问答")

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mTabLayout.tabGravity = TabLayout.GRAVITY_CENTER
    mTabLayout.tabMode = TabLayout.MODE_SCROLLABLE
    mTabLayout.isTabIndicatorFullWidth = false
    mTabLayout.tabIndicatorAnimationMode = TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC
  }

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    tab.text = title[position]
  }

  override fun createFragments(): MutableList<Fragment> {
    val list = mutableListOf<Fragment>()
    title.forEach {
      list.add(ProjectFragment())
    }
    return list
  }

}