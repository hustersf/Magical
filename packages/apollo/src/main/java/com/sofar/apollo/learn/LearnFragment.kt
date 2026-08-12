package com.sofar.apollo.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sofar.apollo.R
import com.sofar.apollo.learn.viewbinder.LearnContext
import com.sofar.apollo.learn.viewbinder.LearnCoreViewBinder
import com.sofar.apollo.learn.viewbinder.LearnViewBinder
import com.sofar.base.viewbinder.ViewBinder

class LearnFragment : Fragment() {

  private lateinit var viewBinder: ViewBinder<LearnContext>

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.learn_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewBinder = ViewBinder<LearnContext>()
    viewBinder.addViewBinder(LearnViewBinder())
    viewBinder.addViewBinder(LearnCoreViewBinder())
    viewBinder.create(view)
    val learnContext = LearnContext()
    viewBinder.bind(learnContext)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    if (::viewBinder.isInitialized) {
      viewBinder.destroy()
    }
  }
}
