package com.sofar.wan.android.feature.project

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.sofar.base.tab.TabFragment
import com.sofar.wan.android.feature.article.ArticleConst
import com.sofar.wan.android.model.Kind
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProjectTabFragment : TabFragment() {

  private val viewModel = ProjectTabViewModel()
  private val projectTabs = mutableListOf<Kind>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mTabLayout.tabGravity = TabLayout.GRAVITY_CENTER
    mTabLayout.tabMode = TabLayout.MODE_SCROLLABLE
    mTabLayout.isTabIndicatorFullWidth = false
    mTabLayout.tabIndicatorAnimationMode = TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC
    refresh()
  }

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    tab.text = projectTabs[position].name
  }

  override fun createFragments(): MutableList<Fragment> {
    val list = mutableListOf<Fragment>()
    projectTabs.forEachIndexed { index, kind ->
      val fragment = ProjectFragment()
      fragment.arguments = Bundle().apply {
        this.putInt(ArticleConst.KEY_INDEX, index)
        this.putInt(ArticleConst.KEY_ID, kind.id)
      }
      list.add(fragment)
    }
    return list
  }

  private fun refresh() {
    lifecycleScope.launch {
      viewModel.projectDataFlow.collect {
        projectTabs.clear()
        projectTabs.addAll(it)
        ProjectDataManager.updateProjectTab(it)
        updateTabs()
      }
    }
  }

}