package com.sofar.wan.android.feature.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.sofar.base.tab.TabFragment
import com.sofar.wan.android.feature.answer.AnswerFragment
import com.sofar.wan.android.feature.home.HomeFragment
import com.sofar.wan.android.feature.square.SquareFragment

class HomeTabFragment : TabFragment() {

  private val list = listOf("首页", "广场", "问答")

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
    list.add(HomeFragment())
    list.add(SquareFragment())
    list.add(AnswerFragment())
    return list
  }

}