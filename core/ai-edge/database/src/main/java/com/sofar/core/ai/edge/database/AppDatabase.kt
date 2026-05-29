package com.sofar.core.ai.edge.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sofar.core.ai.edge.database.dao.AgentDao
import com.sofar.core.ai.edge.database.dao.ChatTransactionDao
import com.sofar.core.ai.edge.database.dao.MessageDao
import com.sofar.core.ai.edge.database.dao.SessionDao
import com.sofar.core.ai.edge.database.entity.AgentEntity
import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.core.ai.edge.database.entity.SessionEntity

@Database(
  entities = [AgentEntity::class, SessionEntity::class, MessageEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  // 垂直拆分后的 DAO 统一出口
  abstract fun agentDao(): AgentDao
  abstract fun sessionDao(): SessionDao
  abstract fun messageDao(): MessageDao
  abstract fun chatTransactionDao(): ChatTransactionDao
}