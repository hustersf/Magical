package com.sofar.apollo.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.sofar.apollo.R
import com.sofar.apollo.home.viewbinder.HomeAuthorViewBinder
import com.sofar.apollo.home.viewbinder.HomeContext
import com.sofar.apollo.home.viewbinder.HomeLearnViewBinder
import com.sofar.apollo.home.viewbinder.HomeReviewViewBinder
import com.sofar.apollo.home.viewbinder.HomeTabViewBinder
import com.sofar.apollo.home.viewbinder.HomeViewBinder
import com.sofar.base.viewbinder.ViewBinder
import com.sofar.core.ui.util.applyEdgeToEdgeInsetsPadding

class HomeFragment : Fragment() {

  private lateinit var viewBinder: ViewBinder<HomeContext>

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.home_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.applyEdgeToEdgeInsetsPadding()
    viewBinder = ViewBinder()
    viewBinder.addViewBinder(HomeViewBinder())
    viewBinder.addViewBinder(HomeTabViewBinder())
    viewBinder.addViewBinder(HomeLearnViewBinder())
    viewBinder.addViewBinder(HomeReviewViewBinder())
    viewBinder.addViewBinder(HomeAuthorViewBinder())
    val homeContext = HomeContext()
    viewBinder.create(view)
    viewBinder.bind(homeContext)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    if (::viewBinder.isInitialized) {
      viewBinder.destroy()
    }
  }
}
