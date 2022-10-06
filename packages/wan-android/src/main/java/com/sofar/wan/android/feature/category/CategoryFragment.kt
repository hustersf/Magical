package com.sofar.wan.android.feature.category

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.sofar.base.rx.RxBus
import com.sofar.wan.android.R
import com.sofar.wan.android.feature.article.ArticleAdapter
import com.sofar.wan.android.feature.article.ArticleDiffCalculator
import com.sofar.wan.android.feature.article.ArticleUtil
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.model.Kind
import com.sofar.wan.android.paging.PageFragment
import com.sofar.wan.android.paging.PageList
import com.sofar.wan.android.utility.CommonUtil
import com.sofar.widget.recycler.adapter.CellAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryFragment : PageFragment<Article>() {

  private lateinit var tabRecyclerView: RecyclerView
  private lateinit var tabAdapter: CategoryTabAdapter
  private lateinit var subFlexBox: FlexboxLayout
  private lateinit var categoryTitle: TextView

  private lateinit var pageList: CategoryPageList

  private val viewModel: CategoryViewModel by lazy {
    ViewModelProvider(this).get(CategoryViewModel::class.java)
  }
  private val categoryList = mutableListOf<Kind>()
  private var tabIndex = -1
  private var subIndex = -1

  private val disposables = CompositeDisposable()

  override fun getLayoutResId(): Int {
    return R.layout.category_fragment
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    categoryTitle = view.findViewById(R.id.category_title)
    tabRecyclerView = view.findViewById(R.id.category_recycler)
    initTabRecycler()

    subFlexBox = view.findViewById(R.id.category_sub_flexbox)
    recyclerView.addItemDecoration(ArticleUtil.createItemDecoration())
    fetchCategoryData()
  }

  override fun onCreateAdapter(): CellAdapter<Article> {
    return ArticleAdapter()
  }

  override fun onCreatePageList(): PageList<*, Article> {
    pageList = CategoryPageList()
    return pageList
  }

  override fun onCreateDiffCallback(): DiffUtil.ItemCallback<Article> {
    return ArticleDiffCalculator.getArticleDiffItemCallback()
  }

  private fun initTabRecycler() {
    tabAdapter = CategoryTabAdapter()
    tabRecyclerView.adapter = tabAdapter
    tabRecyclerView.layoutManager = LinearLayoutManager(context)

    disposables.add(RxBus.get().toObservable(CategoryEvent.TabClickEvent::class.java)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(Consumer {
        categoryList.forEachIndexed { index, kind ->
          if (TextUtils.equals(kind.name, it.kind.name)) {
            selectTab(index)
          }
        }
      }))
  }

  private fun fetchCategoryData() {
    lifecycleScope.launch {
      viewModel.categoryDataFlow.collectLatest {
        categoryList.clear()
        it.forEach { kind ->
          if (!kind.children.isNullOrEmpty()) {
            categoryList.add(kind)
          }
        }
        initTabAdapter()
      }
    }
  }

  private fun initTabAdapter() {
    tabAdapter.items = categoryList
    tabAdapter.notifyDataSetChanged()
    selectTab(0)
  }

  private fun createCategoryView(
    index: Int,
    kind: Kind,
    layoutParams: ViewGroup.MarginLayoutParams,
  ): TextView {
    var space = CommonUtil.dip2px(6f)
    val textView = TextView(context).apply {
      text = kind.name
      setTextColor(CommonUtil.colors(R.color.category_tab_colors))
      setBackgroundResource(R.drawable.category_tab_button)
      setOnClickListener {
        onSubTagClick(index, kind)
      }
      setPadding(space, space, space, space)
    }
    return textView
  }

  private fun onSubTagClick(index: Int, kind: Kind) {
    selectSubFlexBox(index)
  }

  private fun selectTab(index: Int) {
    if (tabIndex == index) {
      return
    }
    tabIndex = index
    tabAdapter.selectPosition(index)

    initSubFlexBox()
  }

  private fun initSubFlexBox() {
    if (tabIndex == -1) {
      return
    }
    subFlexBox.removeAllViews()
    var subList = categoryList[tabIndex].children
    subList.forEachIndexed { index, kind ->
      var layoutParams = ViewGroup.MarginLayoutParams(
        ViewGroup.MarginLayoutParams.WRAP_CONTENT,
        ViewGroup.MarginLayoutParams.WRAP_CONTENT)
      subFlexBox.addView(createCategoryView(index, kind, layoutParams), layoutParams)
    }
    subIndex = -1
    selectSubFlexBox(0)
  }

  private fun selectSubFlexBox(index: Int) {
    if (subIndex == index) {
      return
    }

    //更新view选中状态
    if (subIndex != -1) {
      var lastChild = subFlexBox.getChildAt(subIndex)
      lastChild.isSelected = false
    }
    var curChild = subFlexBox.getChildAt(index)
    curChild.isSelected = true
    subIndex = index

    var subList = categoryList[tabIndex].children
    var cid = subList[index].id
    pageList.updateCid(cid)
    refresh()

    categoryTitle.text = categoryList[tabIndex].name + "/" + subList[index].name
  }

  override fun autoLoad(): Boolean {
    return false
  }

  override fun onDestroyView() {
    super.onDestroyView()
    disposables.clear()
  }

}