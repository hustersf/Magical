package com.sofar.wan.android.feature.wxarticle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.sofar.base.tab.TabFragment
import com.sofar.wan.android.feature.article.ArticleConst
import com.sofar.wan.android.model.Kind
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WxArticleTabFragment : TabFragment() {

  private val viewModel: WxArticleTabViewModel by lazy {
    ViewModelProvider(this).get(WxArticleTabViewModel::class.java)
  }
  private val wxArticles = mutableListOf<Kind>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mTabLayout.tabMode = TabLayout.MODE_AUTO
    refresh()
  }

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    tab.text = wxArticles[position].name
  }

  override fun createFragments(): MutableList<Fragment> {
    val list = mutableListOf<Fragment>()
    wxArticles.forEachIndexed { index, wxArticle ->
      val fragment = WxArticleFragment()
      fragment.arguments = Bundle().apply {
        this.putInt(ArticleConst.KEY_INDEX, index)
        this.putInt(ArticleConst.KEY_ID, wxArticle.id)
      }
      list.add(fragment)
    }
    return list
  }

  private fun refresh() {
    lifecycleScope.launch {
      viewModel.wxAuthorDataFlow.collect {
        wxArticles.clear()
        wxArticles.addAll(it)
        WxArticleDataManager.updateWxArticleTab(it)
        updateTabs()
      }
    }
  }

}