package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.example.data.local.entity.UrgentMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class DeviceSmsHelper(private val context: Context) {

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getDeviceSmsMessages(): List<UrgentMessageEntity> = withContext(Dispatchers.IO) {
        if (!hasSmsPermission()) {
            return@withContext emptyList()
        }

        val result = mutableListOf<UrgentMessageEntity>()
        var cursor: Cursor? = null
        try {
            val inboxUri = Telephony.Sms.Inbox.CONTENT_URI ?: Uri.parse("content://sms/inbox")
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.READ
            )

            // Order by date DESC without LIMIT in SQL clause for compatibility
            cursor = context.contentResolver.query(
                inboxUri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
                val readIdx = it.getColumnIndex(Telephony.Sms.READ)

                var count = 0
                while (it.moveToNext() && count < 30) {
                    val rawAddress = if (addressIdx >= 0) it.getString(addressIdx) else null
                    val body = if (bodyIdx >= 0) it.getString(bodyIdx) else ""
                    val timestamp = if (dateIdx >= 0) it.getLong(dateIdx) else System.currentTimeMillis()
                    val isRead = if (readIdx >= 0) it.getInt(readIdx) == 1 else false

                    val senderAddress = rawAddress?.trim() ?: "Unknown Sender"
                    var senderName = senderAddress
                    if (senderAddress.length >= 4 && !senderAddress.all { ch -> ch.isLetter() }) {
                        val contact = lookupContactName(senderAddress)
                        if (!contact.isNullOrBlank()) {
                            senderName = contact
                        }
                    }

                    // Determine urgency level and action item based on content
                    val lowerBody = body.lowercase(Locale.getDefault())
                    val isCritical = lowerBody.contains("otp") ||
                            lowerBody.contains("debited") ||
                            lowerBody.contains("credited") ||
                            lowerBody.contains("urgent") ||
                            lowerBody.contains("asap") ||
                            lowerBody.contains("alert") ||
                            lowerBody.contains("verify") ||
                            lowerBody.contains("action required")

                    val urgency = when {
                        isCritical -> "Critical"
                        lowerBody.contains("due") || lowerBody.contains("bill") || lowerBody.contains("delivery") -> "High"
                        else -> "Normal"
                    }

                    val action = when {
                        lowerBody.contains("otp") -> "One-Time Password received; do not share."
                        lowerBody.contains("debited") || lowerBody.contains("bank") -> "Verify banking transaction details."
                        lowerBody.contains("delivery") || lowerBody.contains("out for delivery") -> "Expected courier delivery today."
                        lowerBody.contains("due") || lowerBody.contains("invoice") -> "Review invoice / bill due date."
                        else -> "Review message from $senderName"
                    }

                    val summary = if (body.length > 90) {
                        body.take(87) + "..."
                    } else {
                        body
                    }

                    result.add(
                        UrgentMessageEntity(
                            sender = senderName,
                            channel = "Device SMS",
                            rawSnippet = body,
                            summary = summary,
                            urgencyLevel = urgency,
                            actionRequired = action,
                            timestamp = timestamp,
                            isRead = isRead
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            // Handled gracefully
        } finally {
            try { cursor?.close() } catch (_: Exception) {}
        }

        result
    }

    private fun lookupContactName(phoneNumber: String): String? {
        if (!hasContactsPermission()) return null
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
