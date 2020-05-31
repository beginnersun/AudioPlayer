package com.example.audioplayer.sqlite

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VoiceDao {

    @get:Query("SELECT * from voice")
    val all:MutableList<Voice>

    @Query("SELECT * from voice WHERE path = :srcPath LIMIT 1")
    fun findBySrcPath(srcPath:String):Voice

    @Query("SELECT * from voice WHERE userCode = :userName")
    fun findByUserName(userName:String):MutableList<Voice>

    @Insert
    fun insert(voice:Voice)

    @Insert
    fun insertAll(vararg voices:Voice)

    @Delete
    fun delete(voice: Voice)

    @Delete
    fun deleteAll(voices: Array<out Voice>)

}