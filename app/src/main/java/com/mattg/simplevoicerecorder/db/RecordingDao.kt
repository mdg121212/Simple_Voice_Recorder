package com.mattg.simplevoicerecorder.db


import androidx.paging.PagingSource
import androidx.room.*


@Dao
interface RecordingDao {

    @Query("SELECT * FROM recordings ORDER BY id")
    fun getAllPaged(): PagingSource<Int, Recording>

    @Query("SELECT * FROM recordings")
    suspend fun getAll(): List<Recording>

    @Query("SELECT * FROM recordings WHERE name like '%' || :query  || '%'")
    suspend fun getRecipesByTitleSearch(query: String): List<Recording>

    @Update()
    suspend fun updateRecording(recording: Recording)

    @Insert
    suspend fun addRecording(recording : Recording)

    @Delete
    suspend fun delete(recording: Recording)

}
