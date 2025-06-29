package com.sofar.snapu.feature.daq.model

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofar.snapu.SofarApp
import com.sofar.snapu.feature.daq.TaskUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductListViewModel : ViewModel() {
  private var context = SofarApp.getAppContext()
  private val _dataState = MutableLiveData<DataState<List<Product>>>()
  val dataState: LiveData<DataState<List<Product>>> = _dataState
  private val products = mutableListOf<Product>()
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

  fun fetchData() {
    viewModelScope.launch {
      _dataState.value = DataState.Loading
      val result = withContext(Dispatchers.IO) {
        readFile()
      }
      products.clear()
      products.addAll(result)
      _dataState.value = DataState.Success(products)
    }
  }

  private fun readFile(): List<Product> {
    val rootFile = TaskUtil.productTextRootFile(context)
    val list = mutableListOf<Product>()
    if (rootFile.exists() && rootFile.isDirectory) {
      rootFile.listFiles()?.let {
        for (file in it) {
          val item = TaskUtil.readProduct(file)
          list.add(item)
        }
      }
    }
    return list.sortedByDescending { it.taskId }
  }

  fun filterList(mode: Int) {
    val newList = mutableListOf<Product>()
    for (item in products) {
      if (mode == ListMode.LIST_UPLOAD_PENDING) {
        if (!item.uploaded) {
          newList.add(item)
        }
      } else if (mode == ListMode.LIST_UPLOADED) {
        if (item.uploaded) {
          newList.add(item)
        }
      } else {
        newList.add(item)
      }
    }
    _dataState.value = DataState.Success(newList)
  }

  fun filterListByDay(time: Long) {
    val d1 = dateFormat.format(Date(time))
    val newList = mutableListOf<Product>()
    for (item in products) {
      val d = dateFormat.format(Date(item.taskId))
      if (TextUtils.equals(d1, d)) {
        newList.add(item)
      }
    }
    _dataState.value = DataState.Success(newList)
  }

  fun deleteProduct(item: Product) {
    products.remove(item)
  }
}
