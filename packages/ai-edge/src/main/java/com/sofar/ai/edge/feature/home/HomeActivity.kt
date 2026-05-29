package com.sofar.ai.edge.feature.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sofar.ai.edge.R
import com.sofar.ai.edge.feature.home.viewmodel.HomeViewModel
import com.sofar.core.ui.BaseUIActivity
import com.sofar.core.ui.FragmentAdapter
import com.sofar.feature.ai.edge.agent.impl.AgentHomeFragment
import com.sofar.feature.ai.edge.chat.impl.home.ChatHomeFragment
import com.sofar.feature.ai.edge.meeting.impl.MeetingHomeFragment
import com.sofar.feature.ai.edge.models.impl.ModelsHomeFragment
import com.sofar.feature.ai.edge.vision.impl.VisionHomeFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : BaseUIActivity() {
  private lateinit var viewPager2: ViewPager2
  private lateinit var adapter: FragmentAdapter
  private lateinit var bottomNavigationView: BottomNavigationView

  private val viewModel: HomeViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)
    splashScreen.setKeepOnScreenCondition { true }

    lifecycleScope.launch {
      delay(2000)
      splashScreen.setKeepOnScreenCondition { false }
    }

    initView()
    initData()
  }

  private fun initView() {
    viewPager2 = findViewById(R.id.view_pager2)
    viewPager2.isUserInputEnabled = false
    bottomNavigationView = findViewById(R.id.bottom_nav)
    bottomNavigationView.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.nav_chat -> selectItem(0)
        R.id.nav_agent -> selectItem(1)
        R.id.nav_vision -> selectItem(2)
        R.id.nav_meeting -> selectItem(3)
        R.id.nav_model -> selectItem(4)
      }
      true
    }
  }

  private fun initData() {
    adapter = FragmentAdapter(this)
    viewPager2.adapter = adapter
    adapter.setFragments(buildFragments())
    adapter.notifyDataSetChanged()
    selectItem(0)
  }

  private fun buildFragments(): MutableList<Fragment> {
    val list = mutableListOf<Fragment>()
    list.add(ChatHomeFragment())
    list.add(AgentHomeFragment())
    list.add(VisionHomeFragment())
    list.add(MeetingHomeFragment())
    list.add(ModelsHomeFragment())
    return list
  }

  private fun selectItem(index: Int) {
    viewPager2.setCurrentItem(index, false)
  }

  override fun windowInsetsType(): Int {
    return WindowInsetsCompat.Type.statusBars()
  }
}