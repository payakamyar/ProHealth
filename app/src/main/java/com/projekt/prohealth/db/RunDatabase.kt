package com.projekt.prohealth.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.projekt.prohealth.entity.Run

@Database(entities = [Run::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RunDatabase: RoomDatabase() {

    abstract fun getRunDao(): RunDao

}