package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.FinancialTransaction
import com.example.data.model.Subscriber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun exportSubscriberStatementCsv(
        context: Context,
        subscriber: Subscriber,
        transactions: List<FinancialTransaction>
    ) {
        val fileName = "كشف_حساب_${subscriber.name.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)

        try {
            val fos = FileOutputStream(file)
            // Add UTF-8 BOM for Excel to recognize Arabic characters correctly
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            
            val writer = fos.writer(Charsets.UTF_8)
            

            // Header info
            writer.write("اسم المشترك,${subscriber.name}\n")
            writer.write("رقم الهاتف,${subscriber.phone}\n")
            writer.write("إجمالي الدين,${FormatHelper.formatCurrency(subscriber.totalDebt)}\n")
            writer.write("إجمالي المسدد,${FormatHelper.formatCurrency(subscriber.totalPaid)}\n")
            writer.write("المبلغ المتبقي,${FormatHelper.formatCurrency(subscriber.remainingDebt)}\n")
            writer.write("\n")
            
            // Table Headers
            writer.write("التاريخ والوقت,نوع العملية,المبلغ,الملاحظات,بواسطة\n")

            transactions.forEach { tx ->
                val dateStr = FormatHelper.formatDateTime(tx.timestamp)
                val typeStr = if (tx.type == "DEBT") "دين" else "تسديد"
                val notes = tx.notes.replace(",", " - ") // Avoid breaking CSV
                val formattedAmount = FormatHelper.formatCurrency(tx.amount)
                writer.write("$dateStr,$typeStr,$formattedAmount,$notes,${tx.createdBy}\n")
            }

            writer.flush()
            writer.close()
            fos.close()

            shareFile(context, file, "text/csv", "مشاركة كشف الحساب")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportAllDebtsCsv(
        context: Context,
        subscribers: List<Subscriber>
    ) {
        val fileName = "تقرير_الديون_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)

        try {
            val fos = FileOutputStream(file)
            // Add UTF-8 BOM for Excel to recognize Arabic characters correctly
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            
            val writer = fos.writer(Charsets.UTF_8)
            

            writer.write("تقرير الديون الإجمالي\n")
            writer.write("تاريخ التقرير,${FormatHelper.formatDate(System.currentTimeMillis())}\n")
            writer.write("\n")
            
            // Table Headers
            writer.write("الرمز,اسم المشترك,رقم الهاتف,إجمالي الدين,إجمالي المسدد,المبلغ المتبقي,تاريخ الاستحقاق\n")

            subscribers.forEach { sub ->
                val dueDateStr = sub.nextDueDate?.let { FormatHelper.formatDate(it) } ?: "غير محدد"
                val totalDebtFmt = FormatHelper.formatCurrency(sub.totalDebt)
                val totalPaidFmt = FormatHelper.formatCurrency(sub.totalPaid)
                val remainingDebtFmt = FormatHelper.formatCurrency(sub.remainingDebt)
                writer.write("${sub.uniqueCode},${sub.name},${sub.phone},$totalDebtFmt,$totalPaidFmt,$remainingDebtFmt,$dueDateStr\n")
            }

            writer.flush()
            writer.close()
            fos.close()

            shareFile(context, file, "text/csv", "مشاركة تقرير الديون")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, title))
    }
}
