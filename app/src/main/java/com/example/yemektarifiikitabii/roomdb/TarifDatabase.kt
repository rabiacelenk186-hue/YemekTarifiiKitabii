package com.example.yemektarifiikitabii.roomdb
import androidx.room.Database
import androidx.room.RoomDatabase



    @Database(entities = [Tarif::class], version = 1)
    abstract class TarifDatabase : RoomDatabase() {
        abstract fun tarifDao() : TarifDAO
    }
