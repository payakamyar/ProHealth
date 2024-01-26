package com.projekt.prohealth.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.projekt.prohealth.db.RunDao
import com.projekt.prohealth.db.RunDatabase
import com.projekt.prohealth.utility.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext context:Context
    ):RunDatabase = Room.databaseBuilder(
        context,
        RunDatabase::class.java,
        Constants.RUN_DB_NAME
    ).addMigrations(Migration(1,2){
        it.execSQL ("CREATE TABLE new_run (\n" +
                "        caloriesBurned INTEGER NOT NULL,\n" +
                "        img BLOB,\n" +
                "        avgSpeedInKMH REAL NOT NULL,\n" +
                "        distanceInMeter REAL NOT NULL,\n" +
                "        time INTEGER NOT NULL,\n" +
                "        id INTEGER PRIMARY KEY,\n" +
                "        timestamp INTEGER NOT NULL)")
        it.execSQL ("DROP TABLE running_tbl")
        it.execSQL ("ALTER TABLE new_run RENAME TO running_tbl")
    })
        .build()


    @Singleton
    @Provides
    fun provideRunDao(db: RunDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context):SharedPreferences =
        context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideFirebaseAuth():FirebaseAuth =
        Firebase.auth

    @Singleton
    @Provides
    fun provideFirebaseFirestore():FirebaseFirestore = Firebase.firestore

}