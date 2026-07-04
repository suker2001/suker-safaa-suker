package com.example.data.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.model.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder

object WhatsAppService {

    private val client = OkHttpClient()

    /**
     * Sends a WhatsApp message automatically using configured cloud APIs.
     * Returns true if successful, false otherwise.
     */
    suspend fun sendWhatsAppMessage(
        phone: String,
        message: String,
        settings: AppSettings
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!settings.isWhatsAppEnabled) {
            return@withContext Result.failure(Exception("إرسال رسائل واتساب معطل من الإعدادات."))
        }

        val formattedPhone = formatPhoneNumber(phone)

        when (settings.whatsAppApiType) {
            "ULTRAMSG" -> {
                if (settings.whatsAppInstanceId.isBlank() || settings.whatsAppToken.isBlank()) {
                    return@withContext Result.failure(Exception("إعدادات UltraMsg غير مكتملة (يرجى إدخال المعرف والرمز)."))
                }
                sendUltraMsg(formattedPhone, message, settings.whatsAppInstanceId, settings.whatsAppToken)
            }
            "TWILIO" -> {
                if (settings.whatsAppSid.isBlank() || settings.whatsAppToken.isBlank()) {
                    return@withContext Result.failure(Exception("إعدادات Twilio غير مكتملة (يرجى إدخال SID والرمز)."))
                }
                sendTwilioMsg(formattedPhone, message, settings.whatsAppSid, settings.whatsAppToken)
            }
            "MANUAL" -> {
                Result.failure(Exception("تم تكوين الإرسال اليدوي. يرجى استخدام الخيار اليدوي لفتح تطبيق واتساب."))
            }
            else -> {
                Result.failure(Exception("طريقة الإرسال المحددة غير مدعومة."))
            }
        }
    }

    private fun sendUltraMsg(
        phone: String,
        message: String,
        instanceId: String,
        token: String
    ): Result<String> {
        val url = "https://api.ultramsg.com/$instanceId/messages/chat"
        val formBody = FormBody.Builder()
            .add("token", token)
            .add("to", phone)
            .add("body", message)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success("تم الإرسال بنجاح عبر UltraMsg: $body")
                } else {
                    Result.failure(IOException("فشل UltraMsg: رمز الاستجابة ${response.code} | $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sendTwilioMsg(
        phone: String,
        message: String,
        accountSid: String,
        authToken: String
    ): Result<String> {
        val url = "https://api.twilio.com/2010-04-01/Accounts/$accountSid/Messages.json"
        
        // Twilio requires phone format like "whatsapp:+1234567890"
        val toValue = "whatsapp:$phone"
        // In real Twilio sandbox/production, you must configure your sender phone.
        // We'll use a standard Twilio parameter or configuration from app setting.
        // For simplicity, we assume the user can enter their twilio number or use a standard test.
        val fromValue = "whatsapp:+14155238886" // standard sandbox number

        val formBody = FormBody.Builder()
            .add("To", toValue)
            .add("From", fromValue)
            .add("Body", message)
            .build()

        val credentials = okhttp3.Credentials.basic(accountSid, authToken)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", credentials)
            .post(formBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success("تم الإرسال بنجاح عبر Twilio: $body")
                } else {
                    Result.failure(IOException("فشل Twilio: رمز الاستجابة ${response.code} | $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Formats a phone number to be compatible with WhatsApp (E.164 format, no leading zeros or spaces).
     */
    fun formatPhoneNumber(phone: String): String {
        var clean = phone.replace(Regex("[^0-9+]"), "")
        if (clean.startsWith("00")) {
            clean = "+" + clean.substring(2)
        } else if (clean.startsWith("0") && !clean.startsWith("+")) {
            // Assume default local country code if missing, but just clean leading zero
            clean = clean.substring(1)
        }
        // Ensure international code is present. If user starts with no +, we keep the numbers as is
        return clean
    }

    /**
     * Opens the native WhatsApp application with a pre-filled message and target number.
     * This serves as the manual fallback so that sending never breaks.
     */
    fun sendWhatsAppManualIntent(context: Context, phone: String, message: String): Boolean {
        return try {
            val formattedPhone = formatPhoneNumber(phone)
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=" + URLEncoder.encode(message, "UTF-8")
            intent.data = Uri.parse(url)
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                // Try without package constraint (covers WhatsApp Business or browser fallback)
                val formattedPhone = formatPhoneNumber(phone)
                val intent = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=" + URLEncoder.encode(message, "UTF-8")
                intent.data = Uri.parse(url)
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                Log.e("WhatsAppService", "Failed to open WhatsApp intent", e2)
                false
            }
        }
    }
}
