package com.sofar.core.ai.edge.database.di

import android.content.Context
import androidx.room.Room
import com.sofar.core.ai.edge.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
  @Provides
  @Singleton
  fun providesAppDatabase(
    @ApplicationContext context: Context,
  ): AppDatabase = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "ai_edge_app_db",
  ).build()
}