package com.sofar.wan.android.feature.project

import androidx.lifecycle.ViewModel
import com.sofar.wan.android.feature.article.ArticleConst
import com.sofar.wan.android.model.Kind
import kotlinx.coroutines.flow.flow

class ProjectTabViewModel : ViewModel() {
  private val repository = ProjectTabRepository()

  val projectDataFlow = flow {
    val data = mutableListOf<Kind>()
    data.add(createNewProjectBean())
    data.addAll(repository.getProjectKinds().data ?: emptyList())
    emit(data)
  }

  private fun createNewProjectBean() = Kind(
    id = ArticleConst.NEW_PROJECT_CID, name = "最新项目"
  )
}