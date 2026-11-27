package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RoutineEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        StudySessionEntity::class,
        GoalEntity::class,
        AiMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LifeTrackDatabase : RoomDatabase() {
    abstract fun dao(): LifeTrackDao

    companion object {
        @Volatile
        private var INSTANCE: LifeTrackDatabase? = null

        fun getInstance(context: Context): LifeTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LifeTrackDatabase::class.java,
                    "lifetrack_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
