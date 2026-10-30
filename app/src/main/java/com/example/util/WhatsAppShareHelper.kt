package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ServiceEntry
import com.example.data.model.Worker
import java.io.File
import java.text.NumberFormat
import java.util.Locale

object WhatsAppShareHelper {

    private fun formatAmount(amount: Double): String {
        return "₹" + String.format(Locale.US, "%,.0f", amount)
    }

    /**
     * Share daily salary statement and work slip to worker's WhatsApp with automated PDF attachment.
     */
    fun shareWorkerSalarySlip(
        context: Context,
        worker: Worker?,
        workerName: String,
        date: String,
        entries: List<ServiceEntry>
    ) {
        try {
            // 1. Generate the PDF file
            val pdfFile = PdfInvoiceGenerator.generateWorkerDailySlip(context, workerName, date, entries)
            val pdfUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            // 2. Build the message
            val totalEarnings = entries.sumOf { it.amount }
            val totalCash = entries.filter { it.paymentMethod.equals("CASH", true) }.sumOf { it.amount }
            val totalGPay = entries.filter { it.paymentMethod.equals("GPAY", true) }.sumOf { it.amount }

            val messageBuilder = StringBuilder()
            messageBuilder.append("📄 *DAILY SALARY & WORK STATEMENT*\n")
            messageBuilder.append("👤 *Worker:* ${workerName.uppercase()}\n")
            messageBuilder.append("📅 *Date:* $date\n")
            messageBuilder.append("━━━━━━━━━━━━━━━━━━\n")
            messageBuilder.append("*Jobs Completed (${entries.size}):*\n")

            entries.forEach { entry ->
                val amt = formatAmount(entry.amount)
                val mode = if (entry.paymentMethod.equals("CASH", true)) "Cash" else "GPay"
                val service = if (entry.serviceName.isNotBlank() && entry.serviceName != "Service Work") " - ${entry.serviceName}" else ""
                messageBuilder.append("${entry.jobNumber}. $amt ($mode)$service\n")
            }

            messageBuilder.append("━━━━━━━━━━━━━━━━━━\n")
            messageBuilder.append("💰 *TOTAL EARNINGS: ${formatAmount(totalEarnings)}*\n")
            messageBuilder.append("💵 *Cash Collected:* ${formatAmount(totalCash)}\n")
            messageBuilder.append("📱 *GPay Collected:* ${formatAmount(totalGPay)}\n\n")
            messageBuilder.append("📎 *Attached: Automated PDF Slip for clarification & records.*\n")
            messageBuilder.append("Please check and confirm with accounts.")

            val shareMessage = messageBuilder.toString()

            // 3. Create Send Intent
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                putExtra(Intent.EXTRA_TEXT, shareMessage)
                putExtra(Intent.EXTRA_SUBJECT, "Salary Slip - $workerName ($date)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Check if WhatsApp is installed to target it directly
            val whatsappIntent = Intent(sendIntent).apply {
                setPackage("com.whatsapp")
            }

            val pm = context.packageManager
            if (whatsappIntent.resolveActivity(pm) != null) {
                context.startActivity(whatsappIntent)
            } else {
                // Check WhatsApp Business
                val whatsappBusinessIntent = Intent(sendIntent).apply {
                    setPackage("com.whatsapp.w4b")
                }
                if (whatsappBusinessIntent.resolveActivity(pm) != null) {
                    context.startActivity(whatsappBusinessIntent)
                } else {
                    // Fallback to system share chooser
                    val chooser = Intent.createChooser(sendIntent, "Share Salary Slip to WhatsApp / Other Apps")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing to WhatsApp: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Share automated client invoice PDF via WhatsApp or email.
     */
    fun shareClientInvoice(
        context: Context,
        entry: ServiceEntry
    ) {
        try {
            val pdfFile = PdfInvoiceGenerator.generateJobInvoice(context, entry)
            val pdfUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val invNum = if (entry.invoiceNumber.isNotBlank()) entry.invoiceNumber else "INV-${entry.id}"
            val message = """
                🧾 *SERVICE INVOICE - $invNum*
                👤 *Client:* ${entry.clientName}
                📅 *Date:* ${entry.date}
                🔧 *Service:* ${entry.serviceName}
                👷 *Technician:* ${entry.workerName}
                💳 *Payment Mode:* ${entry.paymentMethod}
                💰 *Amount:* ${formatAmount(entry.amount)}
                ✅ *Status: Paid in Full*
                
                Attached is your official service invoice. Thank you!
            """.trimIndent()

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, "Invoice $invNum - ${entry.clientName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Share Invoice via WhatsApp / Others")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing invoice: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
