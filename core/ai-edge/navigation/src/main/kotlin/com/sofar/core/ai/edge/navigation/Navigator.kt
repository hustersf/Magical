package com.sofar.core.ai.edge.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 *
 * @param state - The navigation state that will be updated in response to navigation events.
 */
class Navigator(val state: NavigationState) {

  /**
   * Navigate to a navigation key
   *
   * @param key - the navigation key to navigate to.
   */
  fun navigate(key: NavKey) {
    when (key) {
      state.currentTopLevelKey -> clearSubStack()
      in state.topLevelKeys -> goToTopLevel(key)
      else -> goToKey(key)
    }
  }

  /**
   * Go back to the previous navigation key.
   */
  fun goBack(count: Int = 1) {
    repeat(count) {
      val currentKey = state.currentKey
      when (currentKey) {
        state.startKey -> {
          // You cannot go back from the start route
        }

        state.currentTopLevelKey -> {
          // We're at the base of the current sub stack, go back to the previous top level
          // stack.
          val topLevelStack = state.topLevelStack
          if (topLevelStack.size > 1) {
            topLevelStack.removeAt(topLevelStack.size - 1)
          }
        }

        else -> {
          val currentSubStack = state.currentSubStack
          if (currentSubStack.size > 1) {
            currentSubStack.removeAt(currentSubStack.size - 1)
          }
        }
      }
    }
  }

  /**
   * Go to a non top level key.
   */
  private fun goToKey(key: NavKey) {
    state.currentSubStack.apply {
      // Remove it if it's already in the stack so it's added at the end.
      remove(key)
      add(key)
    }
  }

  /**
   * Go to a top level stack.
   */
  private fun goToTopLevel(key: NavKey) {
    state.topLevelStack.apply {
      if (key == state.startKey) {
        // This is the start key. Clear the stack so it's added as the only key.
        clear()
      } else {
        // Remove it if it's already in the stack so it's added at the end.
        remove(key)
      }
      add(key)
    }
  }

  /**
   * Clearing all but the root key in the current sub stack.
   */
  private fun clearSubStack() {
    state.currentSubStack.run {
      if (size > 1) {
        val sub = subList(1, size)
        sub.clear()
      }
    }
  }
}
