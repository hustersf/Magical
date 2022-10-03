package com.sofar.wan.android.feature.project

import com.sofar.wan.android.model.Kind

object ProjectDataManager {

  private val projects = mutableListOf<Kind>()

  fun updateProjectTab(list: List<Kind>) {
    projects.clear()
    projects.addAll(list)
  }

  fun getProjectIndex(index: Int): Kind {
    return projects[index]
  }
}