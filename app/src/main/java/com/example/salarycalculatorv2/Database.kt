package com.example.salarycalculatorv2

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [(DBEntity::class)], version = 1)
abstract class Database: RoomDatabase() {
    abstract fun DB_DAO () : DAO
}