package com.example.scancaptureapp.domain.repository

import android.graphics.Bitmap
import com.example.scancaptureapp.domain.model.ScanMode

interface ImageProcessor {
    fun processForOcr(source: Bitmap, scanMode: ScanMode, enhance: Boolean): Bitmap
    fun detectBlurScore(source: Bitmap): Double
}
