package com.sofar.feature.ai.edge.chat.impl.di

import com.sofar.feature.ai.edge.chat.api.ChatNavigator
import com.sofar.feature.ai.edge.chat.impl.navigation.ChatNavigatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatsExportModule {

  @Binds
  @Singleton
  abstract fun bindChatNavigator(
    impl: ChatNavigatorImpl
  ): ChatNavigator
}