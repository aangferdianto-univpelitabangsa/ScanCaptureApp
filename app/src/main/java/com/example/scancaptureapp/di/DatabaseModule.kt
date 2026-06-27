package com.example.scancaptureapp.di

import android.content.Context
import androidx.room.Room
import com.example.scancaptureapp.data.local.ScanDatabase
import com.example.scancaptureapp.data.local.dao.ScanHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScanDatabase =
        Room.databaseBuilder(
            context,
            ScanDatabase::class.java,
            "scan_capture_db"
        ).build()

    @Provides
    fun provideScanHistoryDao(database: ScanDatabase): ScanHistoryDao =
        database.scanHistoryDao()
}
