package com.sofar.core.ai.edge.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sofar.core.ai.edge.database.dao.AgentDao
import com.sofar.core.ai.edge.database.dao.ChatTransactionDao
import com.sofar.core.ai.edge.database.dao.MessageDao
import com.sofar.core.ai.edge.database.dao.SessionDao
import com.sofar.core.ai.edge.database.entity.AgentEntity
import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.core.ai.edge.database.entity.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "ai_edge_app_db"
        )
          // 首次创建数据库时自动置入内置的默认智能体
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                  prepopulateAgents(database.agentDao())
                }
              }
            }
          })
          .build()
        INSTANCE = instance
        instance
      }
    }

    // 初始化默认的智能体人设数据
    private suspend fun prepopulateAgents(agentDao: AgentDao) {
      val defaultAgents = listOf(
        AgentEntity(
          id = "english_coach",
          name = "英语口语教练",
          avatar = "🇬🇧",
          systemPrompt = "You are a patient English coach. Correct my grammar mistakes and keep the conversation fun."
        ),
        AgentEntity(
          id = "translator",
          name = "同声传译官",
          avatar = "🗣️",
          systemPrompt = "你是一个精通多国语言的翻译官。请直接将用户输入的所有非英文内容翻译成英文，英文内容翻译成中文，不要输出任何额外的解释。"
        )
      )
      agentDao.insertAgents(defaultAgents)
    }
  }
}