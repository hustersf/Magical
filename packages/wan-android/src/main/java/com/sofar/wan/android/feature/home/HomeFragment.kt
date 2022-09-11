package com.sofar.wan.android.feature.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sofar.base.BaseFragment
import com.sofar.wan.android.R
import com.sofar.wan.android.network.api.ApiProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.home_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    test()
  }

  private fun test() {
    GlobalScope.launch {
      try {
        var res = ApiProvider.get().getBanner()
        Log.d("sufan333", "res code=${res.errorCode}")
      } catch (e: Exception) {
        Log.d("sufan333", e.toString())
      }

    }
  }

}