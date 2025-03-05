package com.sofar.wan.android.feature.navi

import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sofar.base.BaseFragment
import com.sofar.base.rx.RxBus
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.model.Tag
import com.sofar.wan.android.utility.CommonUtil
import com.sofar.widget.recycler.LinearMarginItemDecoration
import com.sofar.widget.recycler.adapter.multitype.MultiTypeAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NaviFragment : BaseFragment() {

  private val viewModel: NaviViewModel by lazy {
    ViewModelProvider(this).get(NaviViewModel::class.java)
  }

  private lateinit var tabRecyclerView: RecyclerView
  private lateinit var tagRecyclerView: RecyclerView

  private lateinit var tabAdapter: NaviTabAdapter
  private lateinit var tagAdapter: MultiTypeAdapter

  private val disposables = CompositeDisposable()
  private val touchSlop: Int by lazy { ViewConfiguration.get(requireContext()).scaledTouchSlop }
  private val scrollListener = object : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      super.onScrolled(recyclerView, dx, dy)
      onTagRecyclerViewScroll()

    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.navi_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    tabRecyclerView = view.findViewById(R.id.tab_recycler)
    tagRecyclerView = view.findViewById(R.id.tag_recycler)

    initTabs()
    initTags()
    initScrollEvent()
    refresh()
  }

  private fun refresh() {
    lifecycleScope.launch {
      viewModel.naviDataFlow.collectLatest {
        val titles = mutableListOf<Tag>()
        val tags = mutableListOf<Any>()
        it.forEach {
          val tag = Tag().apply { name = it.name }
          titles.add(tag)
          tags.add(tag)
          tags.addAll(it.articles)
        }

        tabAdapter.items = titles
        tabAdapter.selectPosition(0)
        tabAdapter.notifyDataSetChanged()

        tagAdapter.items = tags
        tagAdapter.notifyDataSetChanged()
      }
    }
  }

  private fun initTabs() {
    tabAdapter = NaviTabAdapter()
    tabRecyclerView.adapter = tabAdapter
    tabRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

    var sideSpace = CommonUtil.dip2px(15f)
    var betweenSpace = CommonUtil.dip2px(10f)
    var itemDecoration =
      LinearMarginItemDecoration(RecyclerView.HORIZONTAL, sideSpace, betweenSpace)
    tabRecyclerView.addItemDecoration(itemDecoration)
    var vSpace = CommonUtil.dip2px(10f)
    tabRecyclerView.setPadding(0, vSpace, 0, vSpace)
  }

  private fun initTags() {
    tagAdapter = MultiTypeAdapter()
    tagAdapter.register(Tag::class.java) { NaviTitleCell() }
    tagAdapter.register(Article::class.java) { NaviTagCell() }
    tagRecyclerView.adapter = tagAdapter

    val layoutManager = GridLayoutManager(context, 3).apply {
      spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
          var item = tagAdapter.getItem(position)
          if (item is Tag) {
            return spanCount
          }
          return 1
        }
      }
    }
    tagRecyclerView.layoutManager = layoutManager
    var vSpace = CommonUtil.dip2px(10f)
    var hSpace = CommonUtil.dimen(R.dimen.padding_left)
    tagRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
      override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
      ) {
        var pos = parent.getChildAdapterPosition(view)
        var item = tagAdapter.getItem(pos)
        if (item is Tag) {
          outRect.left = CommonUtil.dimen(R.dimen.padding_left)
          return
        }

        var bottomTag = tagAdapter.getItem(pos + 1) is Tag
            || tagAdapter.getItem(pos + 2) is Tag
            || tagAdapter.getItem(pos + 3) is Tag
        outRect.bottom = if (bottomTag) 0 else vSpace
        outRect.left = hSpace
      }
    })
    tagRecyclerView.setPadding(0, 0, hSpace, 0)
  }

  private fun initScrollEvent() {
    disposables.add(RxBus.get().toObservable(NaviEvent.TabClickEvent::class.java)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(Consumer {
        scrollToTab(it.tag.name)
        scrollToTag(it.tag)
      }))
    tagRecyclerView.addOnScrollListener(scrollListener)
  }

  private fun scrollToTag(tag: Tag) {
    var pos = -1
    tagAdapter.items.forEachIndexed { index, any ->
      if (any is Tag && TextUtils.equals(tag.name, any.name)) {
        pos = index
      }
    }
    if (pos != -1) {
      var layoutManager = tagRecyclerView.layoutManager as GridLayoutManager
      layoutManager.scrollToPositionWithOffset(pos, 0)
    }
  }

  private fun onTagRecyclerViewScroll() {
    var layoutManager = tagRecyclerView.layoutManager as GridLayoutManager
    val firstPos = layoutManager.findFirstVisibleItemPosition()
    val item = tagAdapter.getItem(firstPos)
    var name = ""
    if (item is Tag) {
      name = item.name
    } else if (item is Article) {
      name = item.chapterName
    }
    scrollToTab(name)
  }

  private fun scrollToTab(name: String) {
    var pos = tabAdapter.selectTag(name)
    var layoutManager = tabRecyclerView.layoutManager as LinearLayoutManager
    //指定的child滚动到中间
    val child = tabRecyclerView.getChildAt(0)
    val offset = (tabRecyclerView.width - child.width) / 2
    layoutManager.scrollToPositionWithOffset(pos, offset)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    disposables.clear()
    tagRecyclerView.removeOnScrollListener(scrollListener)
  }
}