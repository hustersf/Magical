package com.sofar.wan.android.feature.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.sofar.base.tab.TabFragment
import com.sofar.wan.android.feature.wxarticle.WxArticleFragment

class WxArticleTabFragment : TabFragment() {

  private val title = listOf("公众号1", "公众号2", "公众号3",
    "公众号4", "公众号5", "公众号6",
    "公众号7", "公众号8", "公众号9",
    "公众号1", "公众号2", "公众号3")

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mTabLayout.tabMode = TabLayout.MODE_AUTO
  }

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    tab.text = title[position]
  }

  override fun createFragments(): MutableList<Fragment> {
    val list = mutableListOf<Fragment>()
    title.forEach {
      list.add(WxArticleFragment())
    }
    return list
  }

}