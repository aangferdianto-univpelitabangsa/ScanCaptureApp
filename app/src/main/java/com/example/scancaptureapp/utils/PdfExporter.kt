package com.example.scancaptureapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates A4 PDFs from OCR text and saves them to Documents/ScanCapture/
 * using MediaStore (API 29+) or legacy public storage (API 28 and below).
 */
object PdfExporter {

    private const val TAG = "PdfExporter"

    /** A4 size in points (72 DPI). */
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    private const val MARGIN_LEFT = 48f
    private const val MARGIN_RIGHT = 48f
    private const val MARGIN_TOP = 56f
    private const val MARGIN_BOTTOM = 56f

    private const val TITLE_TEXT_SIZE = 20f
    private const val DATE_TEXT_SIZE = 11f
    private const val BODY_TEXT_SIZE = 12f

    /** MediaStore relative path: Documents/ScanCapture/ */
    private const val RELATIVE_PATH = "Documents/ScanCapture"

    data class PdfExportResult(
        val displayName: String,
        val uri: Uri?,
        val legacyFile: File? = null
    )

    /**
     * Generates a PDF from [text] and saves it to device storage.
     *
     * @param title Bold header shown at the top of the first page.
     */
    fun generatePdf(
        context: Context,
        text: String,
        title: String
    ): Result<PdfExportResult> = runCatching {
        require(text.isNotBlank()) { "Text is empty" }

        val displayName = buildFileName()
        val createdDate = formatCreatedDate()
        val pdfBytes = buildPdfBytes(title, createdDate, text.trim())

        if (pdfBytes.isEmpty()) {
            throw IOException("Generated PDF is empty")
        }

        savePdfToStorage(context, displayName, pdfBytes)
    }.onFailure { e ->
        Log.e(TAG, "generatePdf failed", e)
    }

    /** @deprecated Use [generatePdf] */
    fun exportTextToPdf(context: Context, title: String, content: String): Result<File> =
        generatePdf(context, content, title).map { result ->
            result.legacyFile
                ?: throw IOException("PDF saved but legacy file path unavailable")
        }

    private fun buildFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "ScanCapture_$timestamp.pdf"
    }

    private fun formatCreatedDate(): String {
        return SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())
    }

    private fun buildPdfBytes(title: String, createdDate: String, content: String): ByteArray {
        val document = PdfDocument()
        try {
            val contentWidth = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = TITLE_TEXT_SIZE
                isFakeBoldText = true
                color = Color.BLACK
            }
            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = DATE_TEXT_SIZE
                color = Color.DKGRAY
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = BODY_TEXT_SIZE
                color = Color.BLACK
            }

            val bodyLines = wrapTextToLines(content, bodyPaint, contentWidth)
            val lineHeight = bodyPaint.fontSpacing * 1.25f
            val maxY = PAGE_HEIGHT - MARGIN_BOTTOM

            var pageNumber = 1
            var page = startPage(document, pageNumber)
            var canvas = page.canvas
            var y = MARGIN_TOP

            if (pageNumber == 1) {
                canvas.drawText(title, MARGIN_LEFT, y, titlePaint)
                y += titlePaint.fontSpacing * 1.6f
                canvas.drawText(createdDate, MARGIN_LEFT, y, datePaint)
                y += datePaint.fontSpacing * 2.2f
            }

            for (line in bodyLines) {
                if (y + lineHeight > maxY) {
                    document.finishPage(page)
                    pageNumber++
                    page = startPage(document, pageNumber)
                    canvas = page.canvas
                    y = MARGIN_TOP
                }
                if (line.isNotEmpty()) {
                    canvas.drawText(line, MARGIN_LEFT, y, bodyPaint)
                }
                y += lineHeight
            }

            document.finishPage(page)

            return ByteArrayOutputStream().use { output ->
                document.writeTo(output)
                output.toByteArray()
            }
        } finally {
            document.close()
        }
    }

    private fun startPage(document: PdfDocument, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        return document.startPage(pageInfo)
    }

    /**
     * Word-wraps text to fit [maxWidth] using [Paint.breakText].
     */
    private fun wrapTextToLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.replace("\r\n", "\n").split("\n")

        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                lines.add("")
                continue
            }
            var remaining = paragraph.trim()
            while (remaining.isNotEmpty()) {
                val count = paint.breakText(remaining, true, maxWidth, null)
                if (count <= 0) {
                    lines.add(remaining.take(1))
                    remaining = remaining.drop(1)
                    continue
                }
                lines.add(remaining.substring(0, count).trimEnd())
                remaining = remaining.substring(count).trimStart()
            }
        }
        return lines
    }

    private fun savePdfToStorage(
        context: Context,
        displayName: String,
        pdfBytes: ByteArray
    ): PdfExportResult {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, displayName, pdfBytes)
        } else {
            saveViaLegacyStorage(context, displayName, pdfBytes)
        }
    }

    private fun saveViaMediaStore(
        context: Context,
        displayName: String,
        pdfBytes: ByteArray
    ): PdfExportResult {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val pendingValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, RELATIVE_PATH)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = resolver.insert(collection, pendingValues)
            ?: throw IOException("Could not create PDF in MediaStore")

        try {
            resolver.openOutputStream(uri, "w")?.use { output ->
                output.write(pdfBytes)
                output.flush()
            } ?: throw IOException("Could not open output stream for PDF")

            val publishValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            resolver.update(uri, publishValues, null, null)

            Log.d(TAG, "PDF saved via MediaStore: $displayName -> $uri")
            return PdfExportResult(displayName = displayName, uri = uri)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    @Suppress("DEPRECATION")
    private fun saveViaLegacyStorage(
        context: Context,
        displayName: String,
        pdfBytes: ByteArray
    ): PdfExportResult {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("WRITE_EXTERNAL_STORAGE permission required")
        }

        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "ScanCapture"
        )
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Could not create directory: ${directory.absolutePath}")
        }

        val file = File(directory, displayName)
        FileOutputStream(file).use { output ->
            output.write(pdfBytes)
            output.flush()
        }

        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf("application/pdf"),
            null
        )

        Log.d(TAG, "PDF saved via legacy storage: ${file.absolutePath}")
        return PdfExportResult(
            displayName = displayName,
            uri = Uri.fromFile(file),
            legacyFile = file
        )
    }
}
