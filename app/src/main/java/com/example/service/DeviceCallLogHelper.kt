package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.local.entity.MissedCallEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DeviceCallLogHelper(private val context: Context) {

    fun hasCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getDeviceMissedCalls(): List<MissedCallEntity> = withContext(Dispatchers.IO) {
        if (!hasCallLogPermission()) {
            return@withContext emptyList()
        }

        val result = mutableListOf<MissedCallEntity>()
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.TYPE
            )

            // CRITICAL FIX: Do NOT append "LIMIT 20" directly to sortOrder.
            // On Android 10+ (API 29+), SQLite query builder throws IllegalArgumentException for LIMIT in sortOrder.
            val selection = "${CallLog.Calls.TYPE} = ?"
            val selectionArgs = arrayOf(CallLog.Calls.MISSED_TYPE.toString())
            val sortOrder = "${CallLog.Calls.DATE} DESC"

            cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)

                var count = 0
                while (it.moveToNext() && count < 30) {
                    val rawName = if (nameIndex >= 0) it.getString(nameIndex) else null
                    val rawNumber = if (numberIndex >= 0) it.getString(numberIndex) else null
                    val timestamp = if (dateIndex >= 0) it.getLong(dateIndex) else System.currentTimeMillis()

                    val cleanNumber = when {
                        rawNumber.isNullOrBlank() -> "Private / Unknown"
                        rawNumber == "-1" -> "Private Number"
                        rawNumber == "-2" -> "Unavailable"
                        rawNumber == "-3" -> "Network Unknown"
                        else -> rawNumber.trim()
                    }

                    // Contact name resolution: if cached name is missing, try Contacts Provider
                    var displayName = rawName?.trim()
                    if (displayName.isNullOrBlank() && cleanNumber.length >= 4 && cleanNumber != "Private / Unknown") {
                        displayName = lookupContactName(cleanNumber)
                    }
                    if (displayName.isNullOrBlank()) {
                        displayName = cleanNumber
                    }

                    // AI context reasoning
                    val timeDescription = formatCallTimeContext(timestamp)
                    val urgencyLevel = when {
                        cleanNumber.contains("800") || cleanNumber.contains("888") -> "Medium"
                        displayName != cleanNumber -> "Urgent" // Known contact in address book
                        else -> "Normal"
                    }

                    val reason = if (displayName != cleanNumber) {
                        "Incoming call from contact '$displayName'. $timeDescription."
                    } else {
                        "Unanswered call from $cleanNumber. $timeDescription."
                    }

                    result.add(
                        MissedCallEntity(
                            callerName = displayName,
                            phoneNumber = cleanNumber,
                            timestamp = timestamp,
                            urgency = urgencyLevel,
                            reasonSummary = reason,
                            isResolved = false,
                            notified = false
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            // Handled safely
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
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun formatCallTimeContext(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffMinutes = ((now - timestamp) / 60000).toInt()
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        val timePeriod = when (hour) {
            in 0..6 -> "Late night"
            in 7..11 -> "Morning"
            in 12..17 -> "Afternoon"
            else -> "Evening"
        }

        return when {
            diffMinutes <= 15 -> "Received just now ($diffMinutes mins ago, $timePeriod)"
            diffMinutes < 60 -> "Received $diffMinutes mins ago ($timePeriod)"
            diffMinutes < 1440 -> "Received ${diffMinutes / 60} hours ago ($timePeriod)"
            else -> {
                val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                "Logged on ${fmt.format(Date(timestamp))}"
            }
        }
    }
}
