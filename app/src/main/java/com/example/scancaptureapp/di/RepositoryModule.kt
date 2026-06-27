package com.example.scancaptureapp.di

import com.example.scancaptureapp.data.repository.AuthRepositoryImpl
import com.example.scancaptureapp.data.repository.OcrRepositoryImpl
import com.example.scancaptureapp.data.repository.ScanHistoryRepositoryImpl
import com.example.scancaptureapp.domain.repository.AuthRepository
import com.example.scancaptureapp.domain.repository.OcrRepository
import com.example.scancaptureapp.domain.repository.ScanHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindScanHistoryRepository(impl: ScanHistoryRepositoryImpl): ScanHistoryRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(impl: OcrRepositoryImpl): OcrRepository
}
