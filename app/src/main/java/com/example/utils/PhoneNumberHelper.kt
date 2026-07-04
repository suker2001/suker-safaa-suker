package com.example.utils

object PhoneNumberHelper {
    fun formatPhoneNumber(phone: String, defaultCountryCode: String = "+966"): String {
        var formatted = phone.trim().replace(" ", "").replace("-", "")
        if (formatted.startsWith("0")) {
            formatted = formatted.substring(1)
        }
        if (!formatted.startsWith("+")) {
            formatted = "$defaultCountryCode$formatted"
        }
        return formatted
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        val formatted = formatPhoneNumber(phone)
        // Basic validation: starts with + and has 9-14 digits
        val regex = Regex("^\\+[1-9]\\d{8,14}$")
        return regex.matches(formatted)
    }
}
