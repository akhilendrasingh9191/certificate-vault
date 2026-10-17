package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class AttachmentInfo(
    val fileName: String,
    val mimeType: String,
    val localPath: String,
    val sizeBytes: Long
)

object AttachmentManager {

    fun saveAttachment(context: Context, sourceUri: Uri): AttachmentInfo? {
        return try {
            val contentResolver = context.contentResolver
            var displayName = "certificate_document"
            var fileSize = 0L

            contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) displayName = cursor.getString(nameIndex) ?: displayName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            val mimeType = contentResolver.getType(sourceUri) ?: run {
                val extension = MimeTypeMap.getFileExtensionFromUrl(displayName)
                if (extension.isNotBlank()) {
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "application/octet-stream"
                } else {
                    "application/octet-stream"
                }
            }

            val certsDir = File(context.filesDir, "saved_pdfs")
            if (!certsDir.exists()) certsDir.mkdirs()

            // Extension suffix
            val safeExtension = displayName.substringAfterLast('.', "")
            val extensionSuffix = if (safeExtension.isNotBlank()) ".$safeExtension" else ""
            val destFileName = "cert_${UUID.randomUUID()}$extensionSuffix"
            val destFile = File(certsDir, destFileName)

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (fileSize == 0L) {
                fileSize = destFile.length()
            }

            AttachmentInfo(
                fileName = displayName,
                mimeType = mimeType,
                localPath = destFile.absolutePath,
                sizeBytes = fileSize
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun openAttachment(context: Context, localPath: String, mimeType: String) {
        try {
            val file = File(localPath)
            if (!file.exists()) {
                Toast.makeText(context, "Attachment file not found", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open this document: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareAttachment(context: Context, localPath: String, mimeType: String, certTitle: String) {
        try {
            val file = File(localPath)
            if (!file.exists()) {
                Toast.makeText(context, "Attachment file not found", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType.ifBlank { "*/*" }
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Attached Document: $certTitle")
                putExtra(Intent.EXTRA_TEXT, "Certificate Document for $certTitle")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Certificate Document"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share attachment: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return ""
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
}
