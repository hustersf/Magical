package com.sofar.core.ai.edge.database.di

import com.sofar.core.ai.edge.database.AppDatabase
import com.sofar.core.ai.edge.database.dao.AgentDao
import com.sofar.core.ai.edge.database.dao.ChatTransactionDao
import com.sofar.core.ai.edge.database.dao.MessageDao
import com.sofar.core.ai.edge.database.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {
  @Provides
  fun providesAgentDao(
    database: AppDatabase,
  ): AgentDao = database.agentDao()

  @Provides
  fun providesSessionDao(
    database: AppDatabase,
  ): SessionDao = database.sessionDao()

  @Provides
  fun providesMessageDao(
    database: AppDatabase,
  ): MessageDao = database.messageDao()

  @Provides
  fun providesChatTransactionDao(
    database: AppDatabase,
  ): ChatTransactionDao = database.chatTransactionDao()
}