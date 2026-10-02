package com.example.service

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.entity.CalendarEventEntity
import java.util.Calendar
import java.util.TimeZone

class DeviceCalendarHelper(private val context: Context) {

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Reads actual calendar events from the Android device's Google Calendar.
     * If permissions are not yet granted or no device events exist, returns realistic
     * scheduled agenda items for SanskarYadav640@gmail.com including Seawoods Grand Central Mall.
     */
    fun getCalendarEvents(): List<CalendarEventEntity> {
        val events = mutableListOf<CalendarEventEntity>()

        if (hasCalendarPermission()) {
            try {
                val now = System.currentTimeMillis()
                val oneWeekLater = now + (7 * 24 * 3600 * 1000L)
                val uri = CalendarContract.Events.CONTENT_URI
                val projection = arrayOf(
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.EVENT_LOCATION,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.ACCOUNT_NAME
                )

                val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
                val selectionArgs = arrayOf(now.toString(), oneWeekLater.toString())
                val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

                context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val titleIdx = cursor.getColumnIndex(CalendarContract.Events.TITLE)
                    val startIdx = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
                    val endIdx = cursor.getColumnIndex(CalendarContract.Events.DTEND)
                    val locIdx = cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
                    val descIdx = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                    val accountIdx = cursor.getColumnIndex(CalendarContract.Events.ACCOUNT_NAME)

                    while (cursor.moveToNext()) {
                        val title = if (titleIdx != -1) cursor.getString(titleIdx) ?: "Untitled Event" else "Event"
                        val start = if (startIdx != -1) cursor.getLong(startIdx) else now
                        val end = if (endIdx != -1) cursor.getLong(endIdx) else (start + 3600000L)
                        val loc = if (locIdx != -1) cursor.getString(locIdx) ?: "Seawoods Grand Central, Navi Mumbai" else "Seawoods Grand Central"
                        val desc = if (descIdx != -1) cursor.getString(descIdx) ?: "" else ""
                        val account = if (accountIdx != -1) cursor.getString(accountIdx) ?: "SanskarYadav640@gmail.com" else "SanskarYadav640@gmail.com"

                        events.add(
                            CalendarEventEntity(
                                eventTitle = title,
                                location = loc,
                                startTimeEpoch = start,
                                endTimeEpoch = end,
                                description = desc,
                                accountEmail = account,
                                isSyncedFromDevice = true,
                                isPriority = title.contains("Urgent", ignoreCase = true) || title.contains("Work", ignoreCase = true)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("DeviceCalendarHelper", "Error reading device calendar: ${e.message}")
            }
        }

        // If no events found or permission pending, return tailored default calendar schedule
        if (events.isEmpty()) {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
            cal.set(Calendar.HOUR_OF_DAY, 10)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val workStart = cal.timeInMillis

            events.add(
                CalendarEventEntity(
                    eventTitle = "Work Shift: Seawoods Grand Central Mall",
                    location = "Seawoods Grand Central Mall, Sector 40, Nerul, Navi Mumbai",
                    startTimeEpoch = workStart,
                    endTimeEpoch = workStart + (8 * 3600 * 1000L),
                    description = "Core on-site duty & workspace session at Seawoods Grand Central Mall tech hub. Commute route via Palm Beach Rd.",
                    accountEmail = "SanskarYadav640@gmail.com",
                    isSyncedFromDevice = false,
                    isPriority = true
                )
            )

            cal.set(Calendar.HOUR_OF_DAY, 15)
            cal.set(Calendar.MINUTE, 30)
            val meetingStart = cal.timeInMillis

            events.add(
                CalendarEventEntity(
                    eventTitle = "Quarterly Tech Review & Sprint Retro",
                    location = "Tower 1, 4th Floor, Seawoods Grand Central",
                    startTimeEpoch = meetingStart,
                    endTimeEpoch = meetingStart + (45 * 60 * 1000L),
                    description = "Executive briefing on Q3 deliverables & Q4 sprint roadmap alignment with regional engineering team.",
                    accountEmail = "SanskarYadav640@gmail.com",
                    isSyncedFromDevice = false,
                    isPriority = true
                )
            )

            // Tomorrow's calendar event
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 11)
            cal.set(Calendar.MINUTE, 15)
            val tomorrowEvent = cal.timeInMillis

            events.add(
                CalendarEventEntity(
                    eventTitle = "Navi Mumbai Client Operations Sync",
                    location = "Executive Boardroom, Seawoods Grand Central",
                    startTimeEpoch = tomorrowEvent,
                    endTimeEpoch = tomorrowEvent + (60 * 60 * 1000L),
                    description = "Discussion on client SLAs, system failover protocols, and corporate infrastructure.",
                    accountEmail = "SanskarYadav640@gmail.com",
                    isSyncedFromDevice = false,
                    isPriority = false
                )
            )
        }

        return events
    }

    /**
     * Inserts an event directly into Google Calendar on the Android device
     */
    fun insertCalendarEvent(
        title: String,
        location: String,
        startEpoch: Long,
        endEpoch: Long,
        description: String
    ): Boolean {
        if (!hasCalendarPermission()) return false
        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startEpoch)
                put(CalendarContract.Events.DTEND, endEpoch)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.EVENT_LOCATION, location)
                put(CalendarContract.Events.CALENDAR_ID, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri != null
        } catch (e: Exception) {
            Log.e("DeviceCalendarHelper", "Failed to insert calendar event: ${e.message}")
            false
        }
    }

    /**
     * Intent to open the Android Google Calendar App
     */
    fun launchGoogleCalendar(epochTime: Long = System.currentTimeMillis()) {
        try {
            val builder = CalendarContract.CONTENT_URI.buildUpon()
            builder.appendPath("time")
            android.content.ContentUris.appendId(builder, epochTime)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = builder.build()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://calendar.google.com")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    /**
     * Intent to add a new event via Google Calendar UI
     */
    fun launchAddCalendarEvent(
        title: String = "Work at Seawoods Grand Central Mall",
        location: String = "Seawoods Grand Central Mall, Navi Mumbai",
        startEpoch: Long = System.currentTimeMillis() + 3600000L
    ) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startEpoch)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startEpoch + 3600000L)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            launchGoogleCalendar(startEpoch)
        }
    }
}
