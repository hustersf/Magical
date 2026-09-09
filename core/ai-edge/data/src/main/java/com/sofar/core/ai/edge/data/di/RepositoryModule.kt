package com.sofar.core.ai.edge.data.di

import android.content.Context
import com.sofar.core.ai.edge.data.datasource.LiteRtLmDataSource
import com.sofar.core.ai.edge.data.repository.AgentRepository
import com.sofar.core.ai.edge.data.repository.ChatRepository
import com.sofar.core.ai.edge.data.repository.DownloadRepository
import com.sofar.core.ai.edge.data.repository.ModelRepository
import com.sofar.core.ai.edge.database.dao.AgentDao
import com.sofar.core.ai.edge.database.dao.MessageDao
import com.sofar.core.ai.edge.database.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext context: Context,
    sessionDao: SessionDao,// Hilt 会全自动去 DaosModule 帮把这个 Dao 找过来
    messageDao: MessageDao,
    agentDao: AgentDao,
    dataSource: LiteRtLmDataSource
  ): ChatRepository {
    // 在这里进行显式的、可见的 new 操作，一目了然
    return ChatRepository(context, sessionDao, messageDao, agentDao, dataSource)
  }

  @Provides
  @Singleton
  fun providesAgentRepository(agentDao: AgentDao): AgentRepository {
    return AgentRepository(agentDao)
  }

  @Provides
  @Singleton
  fun provideModelRepository(
    @ApplicationContext context: Context
  ): ModelRepository {
    return ModelRepository(context)
  }

  @Provides
  @Singleton
  fun provideDownloadRepository(
    @ApplicationContext context: Context
  ): DownloadRepository {
    return DownloadRepository(context)
  }
}
