package com.sofar.core.ai.edge.data.di

import com.sofar.core.ai.edge.data.datasource.LiteRtLmDataSource
import com.sofar.core.ai.edge.data.repository.AgentRepository
import com.sofar.core.ai.edge.data.repository.ChatRepository
import com.sofar.core.ai.edge.database.dao.AgentDao
import com.sofar.core.ai.edge.database.dao.MessageDao
import com.sofar.core.ai.edge.database.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 全局 Repository 统一批发管理中心
 */
@Module
@InstallIn(SingletonComponent::class)
internal object RepositoryModule {

  @Provides
  @Singleton // 在这里统一控制全局单例，免去在类上面写注解的麻烦
  fun providesChatRepository(
    sessionDao: SessionDao,// Hilt 会全自动去 DaosModule 里面帮你把这个 Dao 找过来
    messageDao: MessageDao,
    dataSource: LiteRtLmDataSource
  ): ChatRepository {
    // 在这里进行显式的、可见的 new 操作，一目了然
    return ChatRepository(sessionDao, messageDao, dataSource)
  }

  @Provides
  @Singleton
  fun providesAgentRepository(agentDao: AgentDao): AgentRepository {
    return AgentRepository(agentDao)
  }
}