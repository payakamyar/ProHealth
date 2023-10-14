package com.projekt.prohealth.di

import android.content.Context
import androidx.room.Room
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
    ).build()


    @Singleton
    @Provides
    fun provideRunDao(db: RunDatabase) = db.getRunDao()

}