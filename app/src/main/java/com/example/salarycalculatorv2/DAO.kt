package com.example.salarycalculatorv2

import androidx.room.*

@Dao
interface DAO {
    @Insert
    suspend fun Save(info: DBEntity)

    @Query( "select * from dbentity")
    suspend fun Read(): List<DBEntity>

    @Query("DELETE from dbentity")
    suspend fun DeleteAll()

    @Delete
    suspend fun DeleteElement(info: DBEntity)

    @Update
    suspend fun Update(info: DBEntity)
}