package com.example.scancaptureapp.di

import com.example.scancaptureapp.data.image.ImagePreprocessor
import com.example.scancaptureapp.data.ocr.TextPostProcessor
import com.example.scancaptureapp.domain.repository.ImageProcessor
import com.example.scancaptureapp.domain.repository.TextFormatter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OcrModule {

    @Binds
    @Singleton
    abstract fun bindImageProcessor(impl: ImagePreprocessor): ImageProcessor

    @Binds
    @Singleton
    abstract fun bindTextFormatter(impl: TextPostProcessor): TextFormatter
}
