package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Certificate
import com.example.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private const val PAGE_WIDTH = 595 // Standard A4 width in pt
    private const val PAGE_HEIGHT = 842 // Standard A4 height in pt
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    fun generatePortfolioPdf(
        context: Context,
        profile: UserProfile?,
        certificates: List<Certificate>,
        portfolioTitle: String = "Official Credentials & Achievements Dossier"
    ): File? {
        val document = PdfDocument()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var y = MARGIN

        fun drawHeaderAndProfile() {
            // Navy top accent bar
            fillPaint.color = Color.parseColor("#1E3A8A")
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 18f, fillPaint)

            // Gold highlight stripe
            fillPaint.color = Color.parseColor("#D97706")
            canvas.drawRect(0f, 18f, PAGE_WIDTH.toFloat(), 22f, fillPaint)

            y = 48f

            // App Dossier Subtitle
            textPaint.apply {
                color = Color.parseColor("#D97706")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("CERTIFIED PORTFOLIO DOSSIER", MARGIN, y, textPaint)
            y += 18f

            // Full Name or Default
            val displayName = profile?.fullName?.ifBlank { "Professional Portfolio" } ?: "Professional Portfolio"
            textPaint.apply {
                color = Color.parseColor("#0F172A")
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(displayName, MARGIN, y, textPaint)
            y += 18f

            // Professional Headline
            val headline = profile?.titleOrProfession?.ifBlank { portfolioTitle } ?: portfolioTitle
            textPaint.apply {
                color = Color.parseColor("#475569")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText(headline, MARGIN, y, textPaint)
            y += 16f

            // Contact Information row
            val contactParts = mutableListOf<String>()
            if (!profile?.email.isNullOrBlank()) contactParts.add("Email: ${profile?.email}")
            if (!profile?.phone.isNullOrBlank()) contactParts.add("Phone: ${profile?.phone}")
            val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
            contactParts.add("Generated: $dateStr")

            textPaint.apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText(contactParts.joinToString("  •  "), MARGIN, y, textPaint)
            y += 20f

            // Executive Summary Box if present
            if (!profile?.summary.isNullOrBlank()) {
                fillPaint.color = Color.parseColor("#F1F5F9")
                strokePaint.color = Color.parseColor("#CBD5E1")
                strokePaint.strokeWidth = 1f
                val summaryRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 44f)
                canvas.drawRoundRect(summaryRect, 6f, 6f, fillPaint)
                canvas.drawRoundRect(summaryRect, 6f, 6f, strokePaint)

                textPaint.apply {
                    color = Color.parseColor("#334155")
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                }
                val summaryLines = wrapText(profile?.summary ?: "", textPaint, CONTENT_WIDTH - 20f)
                var summaryY = y + 16f
                for (line in summaryLines.take(2)) {
                    canvas.drawText(line, MARGIN + 10f, summaryY, textPaint)
                    summaryY += 13f
                }
                y += 54f
            }

            // Summary Stats KPI Row
            val totalCerts = certificates.size
            val uniqueOrgs = certificates.map { it.organization.trim() }.filter { it.isNotBlank() }.distinct().size
            val allSkills = certificates.flatMap { it.skills }.filter { it.isNotBlank() }.distinct().size

            val statBoxWidth = (CONTENT_WIDTH - 24f) / 3f
            val statBoxHeight = 36f

            fun drawStatBox(title: String, value: String, x: Float) {
                fillPaint.color = Color.parseColor("#F8FAFC")
                strokePaint.color = Color.parseColor("#E2E8F0")
                strokePaint.strokeWidth = 1f
                val rect = RectF(x, y, x + statBoxWidth, y + statBoxHeight)
                canvas.drawRoundRect(rect, 4f, 4f, fillPaint)
                canvas.drawRoundRect(rect, 4f, 4f, strokePaint)

                textPaint.apply {
                    color = Color.parseColor("#1E3A8A")
                    textSize = 14f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(value, x + 10f, y + 20f, textPaint)

                textPaint.apply {
                    color = Color.parseColor("#64748B")
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                canvas.drawText(title, x + 10f, y + 31f, textPaint)
            }

            drawStatBox("Total Credentials", "$totalCerts Recorded", MARGIN)
            drawStatBox("Issuing Bodies", "$uniqueOrgs Organizations", MARGIN + statBoxWidth + 12f)
            drawStatBox("Core Competencies", "$allSkills Skill Tags", MARGIN + (statBoxWidth * 2) + 24f)

            y += 50f

            // Section Header
            strokePaint.color = Color.parseColor("#E2E8F0")
            strokePaint.strokeWidth = 1f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, strokePaint)
            y += 18f

            textPaint.apply {
                color = Color.parseColor("#1E3A8A")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("VERIFIED CERTIFICATIONS & ACHIEVEMENTS", MARGIN, y, textPaint)
            y += 16f
        }

        fun drawFooter(currPage: Int) {
            strokePaint.color = Color.parseColor("#E2E8F0")
            strokePaint.strokeWidth = 1f
            canvas.drawLine(MARGIN, PAGE_HEIGHT - 35f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 35f, strokePaint)

            textPaint.apply {
                color = Color.parseColor("#94A3B8")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText("CertVault • Structured Professional Credentials Dossier", MARGIN, PAGE_HEIGHT - 20f, textPaint)

            val pageLabel = "Page $currPage"
            val labelWidth = textPaint.measureText(pageLabel)
            canvas.drawText(pageLabel, PAGE_WIDTH - MARGIN - labelWidth, PAGE_HEIGHT - 20f, textPaint)
        }

        // Draw initial page header
        drawHeaderAndProfile()

        // Draw certificate cards
        for ((index, cert) in certificates.withIndex()) {
            val estimatedCardHeight = 110f + (if (cert.skills.isNotEmpty()) 24f else 0f)

            // If card exceeds page height, create new page
            if (y + estimatedCardHeight > PAGE_HEIGHT - 50f) {
                drawFooter(pageNumber)
                document.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas

                // Accent banner on subsequent pages
                fillPaint.color = Color.parseColor("#1E3A8A")
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 12f, fillPaint)
                fillPaint.color = Color.parseColor("#D97706")
                canvas.drawRect(0f, 12f, PAGE_WIDTH.toFloat(), 15f, fillPaint)

                y = 35f

                textPaint.apply {
                    color = Color.parseColor("#1E3A8A")
                    textSize = 11f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("VERIFIED CERTIFICATIONS & ACHIEVEMENTS (CONTINUED)", MARGIN, y, textPaint)
                y += 20f
            }

            val cardTop = y
            val cardRect = RectF(MARGIN, cardTop, PAGE_WIDTH - MARGIN, cardTop + estimatedCardHeight)

            // Background card fill
            fillPaint.color = if (index % 2 == 0) Color.parseColor("#FFFFFF") else Color.parseColor("#F8FAFC")
            canvas.drawRoundRect(cardRect, 6f, 6f, fillPaint)

            // Left decorative category accent pill
            val categoryColor = when (cert.category.lowercase()) {
                "degree" -> "#059669"
                "award" -> "#D97706"
                "course" -> "#0284C7"
                "license" -> "#7C3AED"
                else -> "#2563EB"
            }
            fillPaint.color = Color.parseColor(categoryColor)
            val leftBar = RectF(MARGIN, cardTop, MARGIN + 4f, cardTop + estimatedCardHeight)
            canvas.drawRoundRect(leftBar, 4f, 4f, fillPaint)

            // Border
            strokePaint.color = Color.parseColor("#E2E8F0")
            strokePaint.strokeWidth = 1f
            canvas.drawRoundRect(cardRect, 6f, 6f, strokePaint)

            var textY = cardTop + 18f

            // Category Badge
            val categoryBadge = cert.category.uppercase()
            textPaint.apply {
                color = Color.parseColor(categoryColor)
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(categoryBadge, MARGIN + 14f, textY, textPaint)

            // Expiry/Validity Tag
            val validityText = if (cert.expiryDate.isNotBlank()) "Valid Until: ${cert.expiryDate}" else "No Expiration / Lifetime"
            textPaint.apply {
                color = Color.parseColor("#64748B")
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val validityWidth = textPaint.measureText(validityText)
            canvas.drawText(validityText, PAGE_WIDTH - MARGIN - 14f - validityWidth, textY, textPaint)

            textY += 16f

            // Certificate Title
            textPaint.apply {
                color = Color.parseColor("#0F172A")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(cert.title, MARGIN + 14f, textY, textPaint)
            textY += 15f

            // Issuing Organization & Date
            textPaint.apply {
                color = Color.parseColor("#334155")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val orgText = "Issued by: ${cert.organization}"
            canvas.drawText(orgText, MARGIN + 14f, textY, textPaint)

            val dateText = "Issue Date: ${cert.issueDate}"
            textPaint.apply {
                color = Color.parseColor("#64748B")
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val dateWidth = textPaint.measureText(dateText)
            canvas.drawText(dateText, PAGE_WIDTH - MARGIN - 14f - dateWidth, textY, textPaint)

            textY += 14f

            // Credential ID / Grade / Document if available
            val metaParts = mutableListOf<String>()
            if (cert.credentialId.isNotBlank()) metaParts.add("ID: ${cert.credentialId}")
            if (cert.gradeOrScore.isNotBlank()) metaParts.add(cert.gradeOrScore)
            if (cert.attachmentName.isNotBlank()) metaParts.add("Doc: ${cert.attachmentName}")
            if (metaParts.isNotEmpty()) {
                textPaint.apply {
                    color = Color.parseColor("#475569")
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                canvas.drawText(metaParts.joinToString("  •  "), MARGIN + 14f, textY, textPaint)
                textY += 13f
            }

            // Description
            if (cert.description.isNotBlank()) {
                textPaint.apply {
                    color = Color.parseColor("#475569")
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                val descLines = wrapText(cert.description, textPaint, CONTENT_WIDTH - 28f)
                for (line in descLines.take(2)) {
                    canvas.drawText(line, MARGIN + 14f, textY, textPaint)
                    textY += 12f
                }
            }

            // Skills tags
            if (cert.skills.isNotEmpty()) {
                textY += 4f
                var skillX = MARGIN + 14f
                val skillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 7.5f
                    color = Color.parseColor("#1E3A8A")
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val skillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#DBEAFE")
                }

                for (skill in cert.skills.take(5)) {
                    val skillWidth = skillTextPaint.measureText(skill) + 12f
                    if (skillX + skillWidth < PAGE_WIDTH - MARGIN - 14f) {
                        val pillRect = RectF(skillX, textY - 9f, skillX + skillWidth, textY + 5f)
                        canvas.drawRoundRect(pillRect, 8f, 8f, skillBgPaint)
                        canvas.drawText(skill, skillX + 6f, textY + 1f, skillTextPaint)
                        skillX += skillWidth + 6f
                    }
                }
            }

            y += estimatedCardHeight + 12f
        }

        // Draw footer on final page
        drawFooter(pageNumber)
        document.finishPage(page)

        // Save PDF to cache directory
        return try {
            val outputDir = File(context.cacheDir, "shared_pdfs")
            if (!outputDir.exists()) outputDir.mkdirs()
            val fileName = "CertVault_Portfolio_${System.currentTimeMillis()}.pdf"
            val file = File(outputDir, fileName)
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(potentialLine) <= maxWidth) {
                currentLine = StringBuilder(potentialLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Certified Credentials & Achievements Dossier")
                putExtra(Intent.EXTRA_TEXT, "Attached is my structured portfolio of verified certificates and achievements exported from CertVault.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Credentials Dossier"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to sharing if no dedicated PDF viewer is installed
            sharePdf(context, file)
        }
    }
}
