package com.sofar.snapu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sofar.base.app.BaseUIActivity
import com.sofar.base.tab.FragmentAdapter
import com.sofar.mlkit.core.MLKit
import com.sofar.snapu.feature.daq.product.ProductListFragment
import com.sofar.snapu.feature.daq.shelf.ShelfListFragment
import com.sofar.snapu.feature.mine.MineFragment
import com.sofar.snapu.feature.record.RecordListFragment

class MainActivity : BaseUIActivity() {

  private lateinit var viewPager2: ViewPager2
  private lateinit var adapter: FragmentAdapter
  private lateinit var bottomNavigationView: BottomNavigationView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    initData()
    initObserve()
  }

  override fun onCreateView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?
  ): View {
    val view: View = layoutInflater.inflate(R.layout.main_activity, viewGroup, false)
    return view
  }

  private fun initView() {
    viewPager2 = findViewById(R.id.view_pager2)
    viewPager2.isUserInputEnabled = false
    bottomNavigationView = findViewById(R.id.bottom_nav)
    bottomNavigationView.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.nav_product -> selectItem(0)
        R.id.nav_shelf -> selectItem(1)
        R.id.nav_record -> selectItem(2)
        R.id.nav_mine -> selectItem(3)
      }
      true
    }
  }

  private fun initData() {
    setTitle(ContextCompat.getString(this, R.string.app_name))
    MLKit.get().preloadCameraX(this)

    adapter = FragmentAdapter(this)
    viewPager2.adapter = adapter
    adapter.setFragments(buildFragments())
    adapter.notifyDataSetChanged()
    selectItem(0)
  }

  private fun selectItem(index: Int) {
    viewPager2.setCurrentItem(index, false)
  }

  private fun buildFragments(): MutableList<Fragment> {
    val list = mutableListOf<Fragment>()
    list.add(ProductListFragment())
    list.add(ShelfListFragment())
    list.add(RecordListFragment())
    list.add(MineFragment())
    return list
  }

  private fun initObserve() {
  }

  override fun windowInsetsType(): Int {
    return WindowInsetsCompat.Type.statusBars()
  }

  companion object {
    fun launch(context: Context) {
      val intent = Intent(context, MainActivity::class.java)
      context.startActivity(intent)
    }
  }
}