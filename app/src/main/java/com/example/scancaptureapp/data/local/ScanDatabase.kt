package com.example.scancaptureapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.scancaptureapp.data.local.dao.ScanHistoryDao
import com.example.scancaptureapp.data.local.entity.ScanHistoryEntity

@Database(
    entities = [ScanHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
}
