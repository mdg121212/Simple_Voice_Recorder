package com.mattg.simplevoicerecorder.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Recording::class], version = 3, exportSchema = false)
abstract class RecordingDatabase: RoomDatabase() {

    abstract fun recordingDao(): RecordingDao

    companion object {
        @Volatile private var INSTANCE: RecordingDatabase ?= null

        fun getInstance(context: Context): RecordingDatabase {
            synchronized(this){
                var instance = INSTANCE

                if(instance == null) instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecordingDatabase::class.java,
                    "recordings.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }

    }
}