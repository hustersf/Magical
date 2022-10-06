package com.sofar.wan.android.feature.course

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.sofar.widget.recycler.LinearMarginItemDecoration
import com.sofar.widget.recycler.adapter.CellAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CourseFragment : PageFragment<Article>() {

  private lateinit var courseRecyclerView: RecyclerView
  private lateinit var courseAdapter: CourseAdapter

  private lateinit var title: TextView
  private lateinit var author: TextView
  private lateinit var subTitle: TextView

  private val viewModel: CourseViewModel by lazy {
    ViewModelProvider(this).get(CourseViewModel::class.java)
  }

  private lateinit var pageList: CoursePageList

  private var courseIndex = -1
  private val disposables = CompositeDisposable()

  override fun getLayoutResId(): Int {
    return R.layout.course_fragment
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    title = view.findViewById(R.id.course_title)
    author = view.findViewById(R.id.course_author)
    subTitle = view.findViewById(R.id.course_sub_title)
    courseRecyclerView = view.findViewById(R.id.course_recycler)
    initCourseRecycler()

    recyclerView.addItemDecoration(ArticleUtil.createItemDecoration())
    fetchCourseData()
  }

  override fun onCreateAdapter(): CellAdapter<Article> {
    return ArticleAdapter()
  }

  override fun onCreatePageList(): PageList<*, Article> {
    pageList = CoursePageList()
    return pageList
  }

  override fun onCreateDiffCallback(): DiffUtil.ItemCallback<Article> {
    return ArticleDiffCalculator.getArticleDiffItemCallback()
  }

  private fun initCourseRecycler() {
    courseAdapter = CourseAdapter()
    courseRecyclerView.adapter = courseAdapter

    var space = CommonUtil.dip2px(10f)
    var itemDecoration = LinearMarginItemDecoration(RecyclerView.VERTICAL, space, space)
    courseRecyclerView.addItemDecoration(itemDecoration)
    courseRecyclerView.layoutManager = LinearLayoutManager(context)

    courseRecyclerView.itemAnimator?.changeDuration = 0

    disposables.add(RxBus.get().toObservable(CourseEvent.CoverClickEvent::class.java)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(Consumer {
        selectCourse(it.index, it.kind)
      }))
  }

  private fun selectCourse(index: Int, kind: Kind) {
    if (courseIndex == index) {
      return
    }

    courseAdapter.selectPosition(index)
    title.text = kind.name
    author.text = kind.author
    subTitle.text = kind.desc

    pageList.updateCid(kind.id)
    refresh()
  }

  private fun fetchCourseData() {
    lifecycleScope.launch {
      viewModel.courseDataFlow.collectLatest {
        courseAdapter.items = it
        courseAdapter.notifyDataSetChanged()
        if (it.isNotEmpty()) {
          selectCourse(0, it[0])
        }
      }
    }
  }

  override fun autoLoad(): Boolean {
    return false
  }

  override fun onDestroyView() {
    super.onDestroyView()
    disposables.clear()
  }
}