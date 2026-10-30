package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.ServiceEntry
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object PdfInvoiceGenerator {

    private fun formatAmount(amount: Double): String {
        return "₹" + String.format(Locale.US, "%,.0f", amount)
    }

    /**
     * Generates an individual worker's daily salary & service slip PDF for WhatsApp clarification.
     */
    fun generateWorkerDailySlip(
        context: Context,
        workerName: String,
        date: String,
        entries: List<ServiceEntry>
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Top Banner (Deep Slate / Navy)
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRect(0f, 0f, 595f, 110f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("DAILY SALARY & SERVICE LEDGER", 36f, 48f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawText("Automated Daily Work Slip & Payment Statement", 36f, 72f, paint)
        canvas.drawText("Generated on: $date", 36f, 92f, paint)

        // Worker Info Card
        paint.color = Color.rgb(241, 245, 249)
        val infoRect = RectF(36f, 130f, 559f, 205f)
        canvas.drawRoundRect(infoRect, 10f, 10f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("WORKER / TECHNICIAN:", 52f, 156f, paint)

        paint.textSize = 18f
        paint.color = Color.rgb(30, 64, 175)
        canvas.drawText(workerName.uppercase(), 52f, 185f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Date: $date", 380f, 156f, paint)
        canvas.drawText("Total Jobs: ${entries.size}", 380f, 176f, paint)
        val firstTime = entries.firstOrNull()?.time ?: "9:00 am"
        canvas.drawText("Shift Started: $firstTime", 380f, 196f, paint)

        // Table Header
        var yPos = 235f
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawRoundRect(RectF(36f, yPos, 559f, yPos + 32f), 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("#", 50f, yPos + 20f, paint)
        canvas.drawText("SERVICE DESCRIPTION", 90f, yPos + 20f, paint)
        canvas.drawText("CLIENT / FOLDER", 280f, yPos + 20f, paint)
        canvas.drawText("MODE", 430f, yPos + 20f, paint)
        canvas.drawText("AMOUNT", 495f, yPos + 20f, paint)

        yPos += 36f

        var totalEarnings = 0.0
        var totalCash = 0.0
        var totalGPay = 0.0

        paint.textSize = 11f
        paint.isFakeBoldText = false

        entries.forEachIndexed { idx, entry ->
            totalEarnings += entry.amount
            if (entry.paymentMethod.equals("CASH", ignoreCase = true)) {
                totalCash += entry.amount
            } else {
                totalGPay += entry.amount
            }

            // Alternating row background
            if (idx % 2 == 0) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(36f, yPos, 559f, yPos + 26f, paint)
            }

            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText("${entry.jobNumber}", 50f, yPos + 18f, paint)
            val desc = if (entry.serviceName.length > 28) entry.serviceName.take(26) + "..." else entry.serviceName
            canvas.drawText(desc, 90f, yPos + 18f, paint)

            val client = if (entry.clientName.length > 18) entry.clientName.take(16) + "..." else entry.clientName
            canvas.drawText(client, 280f, yPos + 18f, paint)

            // Mode Badge
            if (entry.paymentMethod.equals("CASH", ignoreCase = true)) {
                paint.color = Color.rgb(5, 150, 105)
                canvas.drawText("CASH", 430f, yPos + 18f, paint)
            } else {
                paint.color = Color.rgb(37, 99, 235)
                canvas.drawText("GPAY", 430f, yPos + 18f, paint)
            }

            paint.color = Color.rgb(15, 23, 42)
            paint.isFakeBoldText = true
            val amtText = formatAmount(entry.amount)
            canvas.drawText(amtText, 495f, yPos + 18f, paint)
            paint.isFakeBoldText = false

            yPos += 28f
        }

        // Summary Card at bottom
        yPos += 15f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(RectF(36f, yPos, 559f, yPos + 105f), 10f, 10f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("SUMMARY & RECONCILIATION", 52f, yPos + 28f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Total Cash Collected:", 52f, yPos + 55f, paint)
        paint.color = Color.rgb(5, 150, 105)
        paint.isFakeBoldText = true
        canvas.drawText(formatAmount(totalCash), 200f, yPos + 55f, paint)

        paint.isFakeBoldText = false
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Total GPay Transferred:", 52f, yPos + 80f, paint)
        paint.color = Color.rgb(37, 99, 235)
        paint.isFakeBoldText = true
        canvas.drawText(formatAmount(totalGPay), 200f, yPos + 80f, paint)

        // Net Total Box
        paint.color = Color.rgb(30, 64, 175)
        canvas.drawRoundRect(RectF(350f, yPos + 15f, 545f, yPos + 90f), 8f, 8f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("TOTAL EARNINGS", 370f, yPos + 40f, paint)

        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText(formatAmount(totalEarnings), 370f, yPos + 72f, paint)

        // Footer Note
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("Generated via Salary Daily App. Please verify all entries and notify accounts for any discrepancy.", 36f, 800f, paint)

        pdfDoc.finishPage(page)

        // Save to cache directory
        val fileName = "SalarySlip_${workerName.replace(" ", "_")}_${date.replace(".", "_")}.pdf"
        val outputDir = File(context.cacheDir, "salary_slips").apply { mkdirs() }
        val outputFile = File(outputDir, fileName)
        FileOutputStream(outputFile).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()
        return outputFile
    }

    /**
     * Generates a formal Client Job Invoice PDF.
     */
    fun generateJobInvoice(
        context: Context,
        entry: ServiceEntry
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Top Header
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("TAX INVOICE", 36f, 50f, paint)

        val invNum = if (entry.invoiceNumber.isNotBlank()) entry.invoiceNumber else "INV-${System.currentTimeMillis() % 100000}"
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("Invoice #: $invNum", 36f, 75f, paint)
        canvas.drawText("Date: ${entry.date}", 420f, 75f, paint)

        // Client & Technician details
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(RectF(36f, 120f, 559f, 210f), 8f, 8f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("BILLED TO:", 52f, 145f, paint)
        paint.textSize = 16f
        paint.color = Color.rgb(30, 64, 175)
        canvas.drawText(entry.clientName, 52f, 172f, paint)
        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Client Folder: ${entry.clientName}", 52f, 192f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("TECHNICIAN / SERVICE:", 340f, 145f, paint)
        paint.textSize = 15f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(entry.workerName, 340f, 172f, paint)
        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Job Ref: #${entry.jobNumber} (${entry.time})", 340f, 192f, paint)

        // Item Table
        var yPos = 240f
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawRoundRect(RectF(36f, yPos, 559f, yPos + 30f), 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("#", 50f, yPos + 20f, paint)
        canvas.drawText("DESCRIPTION OF SERVICE", 90f, yPos + 20f, paint)
        canvas.drawText("PAYMENT MODE", 360f, yPos + 20f, paint)
        canvas.drawText("TOTAL", 480f, yPos + 20f, paint)

        yPos += 35f
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRect(36f, yPos, 559f, yPos + 40f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("1", 50f, yPos + 24f, paint)
        canvas.drawText(entry.serviceName, 90f, yPos + 24f, paint)

        paint.isFakeBoldText = true
        if (entry.paymentMethod.equals("CASH", ignoreCase = true)) {
            paint.color = Color.rgb(5, 150, 105)
            canvas.drawText("CASH", 360f, yPos + 24f, paint)
        } else {
            paint.color = Color.rgb(37, 99, 235)
            canvas.drawText("GPAY", 360f, yPos + 24f, paint)
        }

        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(formatAmount(entry.amount), 480f, yPos + 24f, paint)

        // Total Row
        yPos += 60f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(RectF(340f, yPos, 559f, yPos + 50f), 8f, 8f, paint)

        paint.color = Color.WHITE
        paint.textSize = 13f
        paint.isFakeBoldText = false
        canvas.drawText("AMOUNT PAID:", 360f, yPos + 30f, paint)

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(formatAmount(entry.amount), 465f, yPos + 32f, paint)

        // Status Stamp
        paint.color = Color.rgb(16, 185, 129)
        val stampRect = RectF(50f, yPos, 190f, yPos + 50f)
        canvas.drawRoundRect(stampRect, 6f, 6f, paint)
        paint.color = Color.WHITE
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("✓ PAID IN FULL", 65f, yPos + 32f, paint)

        // Notes
        if (entry.notes.isNotBlank()) {
            yPos += 75f
            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("Notes: ${entry.notes}", 36f, yPos, paint)
        }

        // Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("Thank you for your business. For any queries, contact support.", 36f, 800f, paint)

        pdfDoc.finishPage(page)

        val cleanClient = entry.clientName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val fileName = "Invoice_${cleanClient}_${invNum}.pdf"
        val outputDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val outputFile = File(outputDir, fileName)
        FileOutputStream(outputFile).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()
        return outputFile
    }
}
