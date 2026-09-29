package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.example.data.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

sealed class PdfImportResult {
    data class Success(val book: Book) : PdfImportResult()
    data class Error(val message: String) : PdfImportResult()
}

object PdfBookManager {

    /**
     * Imports a PDF from a content Uri into local app storage.
     * Extracts page count, title, and renders cover thumbnail.
     */
    suspend fun importPdfFromUri(
        context: Context,
        uri: Uri,
        repository: CompanionRepository
    ): PdfImportResult = withContext(Dispatchers.IO) {
        try {
            // 1. Resolve Display Name
            var displayName = "Imported Book"
            var fileSize = 0L
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) {
                            displayName = cursor.getString(nameIndex) ?: displayName
                        }
                        if (sizeIndex != -1) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                // fallback to uri last path segment
                displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Book"
            }

            // Remove .pdf extension for clean book title
            val cleanTitle = if (displayName.endsWith(".pdf", ignoreCase = true)) {
                displayName.substringBeforeLast(".pdf")
            } else {
                displayName
            }

            // 2. Prepare internal books directory
            val booksDir = File(context.filesDir, "books")
            if (!booksDir.exists()) booksDir.mkdirs()

            val safeFileName = "book_${System.currentTimeMillis()}_${cleanTitle.replace("[^a-zA-Z0-9.-]".toRegex(), "_")}.pdf"
            val destinationFile = File(booksDir, safeFileName)

            // 3. Copy file from ContentResolver stream
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext PdfImportResult.Error("Could not open selected file. Inaccessible storage or permission denied.")
                outputStream = FileOutputStream(destinationFile)
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            } catch (e: Exception) {
                destinationFile.delete()
                return@withContext PdfImportResult.Error("Failed to import PDF: ${e.localizedMessage ?: "File read error"}")
            } finally {
                try { inputStream?.close() } catch (_: Exception) {}
                try { outputStream?.close() } catch (_: Exception) {}
            }

            // 4. Validate and extract PDF page count and generate cover
            val pfd = try {
                ParcelFileDescriptor.open(destinationFile, ParcelFileDescriptor.MODE_READ_ONLY)
            } catch (e: Exception) {
                destinationFile.delete()
                return@withContext PdfImportResult.Error("Corrupted or unreadable PDF file.")
            }

            val totalPages: Int
            var coverPath = ""
            try {
                val renderer = PdfRenderer(pfd)
                totalPages = renderer.pageCount
                if (totalPages <= 0) {
                    renderer.close()
                    pfd.close()
                    destinationFile.delete()
                    return@withContext PdfImportResult.Error("The selected PDF has no pages or is empty.")
                }

                // Render page 0 as cover thumbnail
                val coversDir = File(context.filesDir, "covers")
                if (!coversDir.exists()) coversDir.mkdirs()
                val coverFile = File(coversDir, "cover_${System.currentTimeMillis()}.png")

                val page = renderer.openPage(0)
                // Downscale cover thumbnail for performance
                val targetWidth = 360
                val targetHeight = (targetWidth * (page.height.toFloat() / page.width.toFloat())).toInt().coerceIn(300, 600)
                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                pfd.close()

                FileOutputStream(coverFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
                }
                coverPath = coverFile.absolutePath
            } catch (e: Exception) {
                try { pfd.close() } catch (_: Exception) {}
                destinationFile.delete()
                return@withContext PdfImportResult.Error("Could not parse PDF pages. File may be encrypted, password protected, or corrupted.")
            }

            // 5. Check duplicate book
            val existing = repository.findBookByTitleOrUri(cleanTitle, destinationFile.absolutePath)
            val bookToSave = if (existing != null) {
                existing.copy(
                    fileUri = destinationFile.absolutePath,
                    coverUri = if (coverPath.isNotEmpty()) coverPath else existing.coverUri,
                    totalPages = totalPages,
                    lastReadAt = System.currentTimeMillis()
                )
            } else {
                Book(
                    title = cleanTitle,
                    author = "Local Document",
                    fileUri = destinationFile.absolutePath,
                    coverUri = coverPath,
                    totalPages = totalPages,
                    currentPage = 1,
                    scrollPosition = 0f,
                    zoomLevel = 1.0f,
                    progress = 0f,
                    lastReadAt = System.currentTimeMillis(),
                    dateAdded = System.currentTimeMillis(),
                    status = "READING"
                )
            }

            val savedId = repository.insertBook(bookToSave)
            val finalBook = bookToSave.copy(id = if (existing != null) existing.id else savedId)
            PdfImportResult.Success(finalBook)
        } catch (e: Exception) {
            PdfImportResult.Error("Import error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Renders a specific page from a local PDF file into a Bitmap on-demand.
     * Pages and renderers are closed immediately to prevent memory leaks with large PDFs.
     */
    suspend fun renderPdfPage(
        pdfFile: File,
        pageIndex: Int,
        zoomScale: Float = 1.0f
    ): Bitmap? = withContext(Dispatchers.IO) {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null
        try {
            if (!pdfFile.exists()) return@withContext null
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

            page = renderer.openPage(pageIndex)
            val scale = zoomScale.coerceIn(1.0f, 3.0f)
            val width = (page.width * scale * 1.5f).toInt()
            val height = (page.height * scale * 1.5f).toInt()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try { page?.close() } catch (_: Exception) {}
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
        }
    }
}
