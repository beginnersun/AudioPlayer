package com.example.audioplayer.sqlite

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Voice::class],version = 1)
abstract class AppDataBase : RoomDatabase() {

    abstract fun voiceDao():VoiceDao?

}