package com.fatemeh.nudge.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// status: 0 = در انتظار، 1 = انجام‌شده، 2 = انجام‌نشده، 3 = نامشخص
@Entity data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueAt: Long? = null,
    val urgent: Boolean = false,
    val status: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val repeatDays: String = "daily",
    val streak: Int = 0,
    val lastDoneDay: Long = 0
)

@Entity data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val progress: Int = 0,
    val deadline: Long? = null
)

// حافظه‌ی بلندمدت AI: گفت‌وگوها
@Entity data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromUser: Boolean,
    val text: String,
    val at: Long = System.currentTimeMillis()
)

@Dao interface AppDao {
    @Query("SELECT * FROM Task ORDER BY status, dueAt") fun tasks(): Flow<List<Task>>
    @Insert suspend fun add(t: Task)
    @Update suspend fun update(t: Task)
    @Delete suspend fun delete(t: Task)

    @Query("SELECT * FROM Habit") fun habits(): Flow<List<Habit>>
    @Insert suspend fun add(h: Habit)
    @Update suspend fun update(h: Habit)
    @Delete suspend fun delete(h: Habit)

    @Query("SELECT * FROM Goal") fun goals(): Flow<List<Goal>>
    @Insert suspend fun add(g: Goal)
    @Update suspend fun update(g: Goal)
    @Delete suspend fun delete(g: Goal)

    @Query("SELECT * FROM ChatMessage ORDER BY at") fun chat(): Flow<List<ChatMessage>>
    @Insert suspend fun add(m: ChatMessage)
}

@Database(entities = [Task::class, Habit::class, Goal::class, ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): AppDao
    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(c: Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(c.applicationContext, AppDb::class.java, "nudge.db").build().also { inst = it }
        }
    }
}
