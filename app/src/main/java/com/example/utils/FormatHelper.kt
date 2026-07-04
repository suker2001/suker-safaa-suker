package com.example.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatHelper {
    private val currencyFormat = DecimalFormat("#,##0")
    private val arabicLocale = Locale("ar", "IQ")
    private val dateFormat = SimpleDateFormat("dd / MM / yyyy", arabicLocale)
    private val timeFormat = SimpleDateFormat("hh:mm a", arabicLocale)

    fun formatCurrency(amount: Double): String {
        // Ensure standard digits are used for the number part as per user example "250,000 د.ع"
        val symbols = currencyFormat.decimalFormatSymbols
        symbols.zeroDigit = '0'
        currencyFormat.decimalFormatSymbols = symbols
        return "${currencyFormat.format(amount)} د.ع"
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
    
    fun formatDateTime(timestamp: Long): String {
        return "${formatDate(timestamp)} - ${formatTime(timestamp)}"
    }

    /**
     * Normalizes Arabic-Indic digits (٠١٢٣٤٥٦٧٨٩) to standard digits (0123456789)
     */
    fun normalizeDigits(input: String): String {
        var output = input
        val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        for (i in 0..9) {
            output = output.replace(arabicIndicDigits[i], i.toString()[0])
        }
        return output
    }
}
