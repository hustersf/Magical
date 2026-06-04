package com.sofar.core.ai.edge.data.di

import com.sofar.core.ai.edge.data.datasource.DefaultLiteRtLmDataSource
import com.sofar.core.ai.edge.data.datasource.LiteRtLmDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

  @Binds
  @Singleton
  abstract fun bindLiteRtLmDataSource(
    impl: DefaultLiteRtLmDataSource
  ): LiteRtLmDataSource
}