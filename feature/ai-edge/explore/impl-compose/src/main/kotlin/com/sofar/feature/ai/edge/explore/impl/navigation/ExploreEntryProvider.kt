package com.sofar.feature.ai.edge.explore.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.feature.ai.edge.explore.api.navigation.ExploreNavKey
import com.sofar.feature.ai.edge.explore.impl.ExploreScreen

fun EntryProviderScope<NavKey>.exploreEntry(navigator: Navigator) {
  entry<ExploreNavKey> {
    ExploreScreen()
  }
}