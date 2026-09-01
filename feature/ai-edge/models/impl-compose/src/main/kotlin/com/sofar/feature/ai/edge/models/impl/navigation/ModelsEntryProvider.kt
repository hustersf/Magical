package com.sofar.feature.ai.edge.models.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.feature.ai.edge.models.api.navigation.ModelsNavKey
import com.sofar.feature.ai.edge.models.impl.ModelsScreen

fun EntryProviderScope<NavKey>.modelsEntry(navigator: Navigator) {
  entry<ModelsNavKey> {
    ModelsScreen()
  }
}