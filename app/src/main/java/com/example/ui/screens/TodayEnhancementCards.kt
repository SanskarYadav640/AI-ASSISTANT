package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.PhoneUnlockLogEntity
import com.example.data.local.entity.SmartWatchHealthEntity
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 1. Year Ends In Countdown Card
 * Displays real-time ticking countdown to December 31, 23:59:59 (Days, Hours, Minutes, Seconds)
 * with % of the year completed and Q4 strategic milestone reflection.
 */
@Composable
fun YearEndCountdownCard(
    currentEpoch: Long,
    modifier: Modifier = Modifier
) {
    val cal = remember(currentEpoch) {
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
            timeInMillis = currentEpoch
        }
    }
    val currentYear = cal.get(Calendar.YEAR)

    val yearEndCal = remember(currentYear) {
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
            set(currentYear, Calendar.DECEMBER, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }
    val yearStartCal = remember(currentYear) {
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
            set(currentYear, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val remainingMillis = (yearEndCal.timeInMillis - currentEpoch).coerceAtLeast(0L)
    val daysLeft = remainingMillis / (24 * 3600 * 1000L)
    val hoursLeft = (remainingMillis % (24 * 3600 * 1000L)) / (3600 * 1000L)
    val minutesLeft = (remainingMillis % (3600 * 1000L)) / (60 * 1000L)
    val secondsLeft = (remainingMillis % (60 * 1000L)) / 1000L

    val totalDuration = (yearEndCal.timeInMillis - yearStartCal.timeInMillis).toFloat()
    val progressFloat = (((currentEpoch - yearStartCal.timeInMillis).toFloat() / totalDuration)).coerceIn(0f, 1f)
    val progressPercent = (progressFloat * 100f)
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(NeonIndigo, ElectricCyan)),
                RoundedCornerShape(20.dp)
            )
            .testTag("year_end_countdown_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonIndigo, ElectricCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = "Year End Countdown",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "YEAR $currentYear ENDS IN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Q4 Strategic Sprint • Live Countdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricTeal
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF131D38))
                        .border(1.dp, NeonIndigo.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DAY $dayOfYear / 365",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Digital Countdown Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountdownBlock(label = "DAYS", value = String.format(Locale.US, "%02d", daysLeft), modifier = Modifier.weight(1f))
                CountdownBlock(label = "HOURS", value = String.format(Locale.US, "%02d", hoursLeft), modifier = Modifier.weight(1f))
                CountdownBlock(label = "MINUTES", value = String.format(Locale.US, "%02d", minutesLeft), modifier = Modifier.weight(1f))
                CountdownBlock(label = "SECONDS", value = String.format(Locale.US, "%02d", secondsLeft), isAccent = true, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar of Year Completed
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Year $currentYear Progress",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%% Completed", progressPercent),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElectricCyan,
                    trackColor = DarkSurfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .padding(10.dp)
            ) {
                Text(
                    text = "🎯 Butler Note: $daysLeft days remaining to complete annual objectives and review Q4 roadmap milestones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun CountdownBlock(
    label: String,
    value: String,
    isAccent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isAccent) Color(0xFF0F2633) else DarkSurfaceVariant)
            .border(1.dp, if (isAccent) ElectricCyan.copy(alpha = 0.5f) else DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isAccent) ElectricCyan else Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * 2. Auto-Set Wake Up Time Card
 * Monitors phone unlock logs and sets wake-up time upon first unlock after 6:00 AM.
 */
@Composable
fun WakeUpTimeCard(
    todayWakeUp: PhoneUnlockLogEntity?,
    unlockLogs: List<PhoneUnlockLogEntity>,
    onTestUnlock: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogsHistory by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
            .testTag("wake_up_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2E2211)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Wake Up Detection",
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Auto Wake-Up Detector",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Triggers on phone unlock after 6:00 AM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (todayWakeUp != null) Color(0xFF0F2B1D) else Color(0xFF261D12))
                        .border(
                            1.dp,
                            if (todayWakeUp != null) SuccessGreen.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (todayWakeUp != null) "LOGGED" else "MONITORING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (todayWakeUp != null) SuccessGreen else WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wake-up Time Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S WAKE-UP TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (todayWakeUp != null) {
                            val timeStr = String.format(
                                Locale.US,
                                "%02d:%02d %s",
                                if (todayWakeUp.hour % 12 == 0) 12 else todayWakeUp.hour % 12,
                                todayWakeUp.minute,
                                if (todayWakeUp.hour >= 12) "PM" else "AM"
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = timeStr,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(via Phone Unlock)",
                                    fontSize = 11.sp,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Awaiting unlock after 6 AM",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.LightGray
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF14243B))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Test unlock & View Logs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestUnlock,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF00382E), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simulate Unlock (>6 AM)", color = Color(0xFF00382E), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { showLogsHistory = !showLogsHistory },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (showLogsHistory) "Hide History" else "Unlock Logs (${unlockLogs.size})", color = ElectricCyan, fontSize = 11.sp)
                }
            }

            // Expandable Unlock Logs History
            AnimatedVisibility(visible = showLogsHistory) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT UNLOCK LOGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Clear All",
                            fontSize = 10.sp,
                            color = AlertRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onClearLogs() }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (unlockLogs.isEmpty()) {
                        Text("No phone unlock logs recorded yet.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        unlockLogs.take(5).forEach { log ->
                            val timeStr = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).format(Date(log.timestamp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = log.wakeUpNote.ifBlank { "Phone Unlocked" }, fontSize = 11.sp, color = if (log.isWakeUpLog) SuccessGreen else Color.White, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${log.unlockMethod} • $timeStr", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (log.isWakeUpLog) {
                                        Text(text = "WAKE-UP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Google Calendar & Agenda Card
 * Connected to SanskarYadav640@gmail.com with Seawoods Grand Central work schedule.
 */
@Composable
fun GoogleCalendarAgendaCard(
    events: List<CalendarEventEntity>,
    isSyncing: Boolean,
    onSyncCalendar: () -> Unit,
    onOpenCalendar: () -> Unit,
    onAddEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
            .testTag("calendar_agenda_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF14243B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Google Calendar",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google Calendar Agenda",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "SanskarYadav640@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricTeal
                        )
                    }
                }

                IconButton(onClick = onSyncCalendar) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ElectricCyan, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync Calendar", tint = ElectricCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calendar Events List
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No upcoming events found on calendar.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                events.take(3).forEach { event ->
                    val startFmt = SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    }.format(Date(event.startTimeEpoch))
                    val endFmt = SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    }.format(Date(event.endTimeEpoch))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, if (event.isPriority) ElectricCyan.copy(alpha = 0.4f) else DarkCardBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.eventTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$startFmt - $endFmt",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }
                            Text(
                                text = "📍 ${event.location}",
                                fontSize = 11.sp,
                                color = ElectricTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (event.description.isNotBlank()) {
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddEventClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Event", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenCalendar,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Calendar", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * 4. Titan Smart Watch & Health Connect (iQOO z9s) Card
 * Displays live steps, heart rate, sleep metrics, and one-tap connection to Titan Smart World & Health Connect.
 */
@Composable
fun SmartWatchHealthCard(
    health: SmartWatchHealthEntity?,
    onLaunchTitanApp: () -> Unit,
    onLaunchHealthConnect: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHealth = health ?: SmartWatchHealthEntity()
    val steps = currentHealth.steps
    val goal = currentHealth.dailyStepGoal
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
            .testTag("smartwatch_health_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F2624)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = "Titan Smart Watch",
                            tint = ElectricTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Titan Smart Watch & Health Connect",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "iQOO z9s • Bluetooth Linked",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricTeal
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F2B1D))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔋 ${currentHealth.watchBattery}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Counter Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$steps / $goal steps",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${(progress * 100).toInt()}% Goal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ElectricCyan,
                        trackColor = DarkSurface,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Metric Health Grid: Heart Rate, Sleep, Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Heart Rate
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = AlertRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${currentHealth.heartRateBpm}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(text = "Heart Rate (BPM)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Sleep Score
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bedtime, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${currentHealth.sleepHours}h", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(text = "Score: ${currentHealth.sleepScore}/100", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Active Burn
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${currentHealth.caloriesKcal} kcal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricTeal)
                        Text(text = "${currentHealth.distanceKm} km walked", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLaunchTitanApp,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Titan Smart World", color = Color(0xFF00382E), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onLaunchHealthConnect,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Health Connect (iQOO)", color = ElectricCyan, fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * 5. Gmail Hub Card
 * Quick launch, VIP unread monitoring, and work email composition for SanskarYadav640@gmail.com
 */
@Composable
fun GmailHubCard(
    onOpenGmail: () -> Unit,
    onCompose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
            .testTag("gmail_hub_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF331619)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = "Gmail",
                            tint = AlertRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Gmail Connected",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "SanskarYadav640@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F2B1D))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("SYNCED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Priva monitors incoming VIP emails for missing tax forms, invoices, and Seawoods Grand Central corporate access passes.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenGmail,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Gmail", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCompose,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compose Email", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Dialog to add a new event to Google Calendar
 */
@Composable
fun AddCalendarEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, location: String, startEpoch: Long, endEpoch: Long, desc: String) -> Unit
) {
    var title by remember { mutableStateOf("Work Shift: Seawoods Grand Central Mall") }
    var location by remember { mutableStateOf("Seawoods Grand Central Mall, Sector 40, Navi Mumbai") }
    var description by remember { mutableStateOf("Scheduled duty & workspace session") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Calendar Event", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Event will be saved to your local database and synced with your device Google Calendar for SanskarYadav640@gmail.com.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    onConfirm(title, location, now + 3600000L, now + (4 * 3600000L), description)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Save Event", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

/**
 * 6. Water Hydration Tracker Card (Planned by JARVIS)
 * Displays daily water intake (e.g. 1,750 / 3,000 ml) with quick log buttons and instant alert trigger.
 */
@Composable
fun WaterHydrationTrackerCard(
    currentMl: Int,
    goalMl: Int = 3000,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit,
    onTriggerReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (currentMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f)
    val remainingMl = (goalMl - currentMl).coerceAtLeast(0)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(ElectricCyan, ElectricTeal)),
                RoundedCornerShape(20.dp)
            )
            .testTag("water_hydration_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ElectricCyan, ElectricTeal))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💧", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "JARVIS Hydration Protocol",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Daily Target: $goalMl ml • Coastal Navi Mumbai",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricTeal
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F2633))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% MET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Hydration Counter Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentMl",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = " / $goalMl ml",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = if (remainingMl > 0) "$remainingMl ml needed to reach full cellular hydration" else "Daily hydration target achieved, Sir! Outstanding.",
                        fontSize = 11.sp,
                        color = if (remainingMl > 0) Color.LightGray else SuccessGreen
                    )
                }

                IconButton(
                    onClick = onTriggerReminder,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Trigger Water Alert",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ElectricCyan,
                trackColor = DarkSurfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Add Water Buttons (+250ml Glass, +500ml Bottle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAddWater(250) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+250 ml (Glass)", color = Color(0xFF003040), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = { onAddWater(500) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+500 ml (Bottle)", color = Color(0xFF00382E), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onResetWater,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * 7. JARVIS Daily Master Protocol (Maharashtra Meals & Schedule)
 * Full hourly protocol planned by JARVIS with authentic healthy Maharashtra menus.
 */
@Composable
fun JarvisDayPlanCard(
    planItems: List<com.example.data.local.entity.DailyPlanItemEntity>,
    onToggleItem: (Long, Boolean) -> Unit,
    onMealReminder: (com.example.data.local.entity.DailyPlanItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .testTag("jarvis_day_plan_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF261D0F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Planned by J.A.R.V.I.S.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Daily Master Protocol • Maharashtra Diet",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningAmber
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF261D0F))
                        .border(1.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AUTOPILOT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Sir, your entire schedule, hydration checkpoints, and Maharashtra cuisine nutritional intake are orchestrated under J.A.R.V.I.S. protocol:",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            planItems.forEach { item ->
                val isMeal = item.category.contains("Maharashtra", ignoreCase = true)
                val isHydration = item.category.contains("Hydration", ignoreCase = true)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (item.isCompleted) Color(0xFF0F1A17) else DarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (item.isCompleted) SuccessGreen.copy(alpha = 0.4f)
                            else if (isMeal) WarningAmber.copy(alpha = 0.3f)
                            else if (isHydration) ElectricCyan.copy(alpha = 0.3f)
                            else DarkCardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isMeal) Color(0xFF332000) else if (isHydration) Color(0xFF003040) else Color(0xFF1E2838))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.timeSlot,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMeal) WarningAmber else if (isHydration) ElectricCyan else Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.category.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isMeal || isHydration) {
                                    IconButton(
                                        onClick = { onMealReminder(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "Alert Me",
                                            tint = if (isMeal) WarningAmber else ElectricCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (item.isCompleted) SuccessGreen else Color.Transparent)
                                        .border(1.dp, if (item.isCompleted) SuccessGreen else Color.Gray, CircleShape)
                                        .clickable { onToggleItem(item.id, !item.isCompleted) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.isCompleted) {
                                        Text("✓", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isCompleted) SuccessGreen else Color.White
                        )

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )

                        if (item.details.isNotBlank()) {
                            Text(
                                text = "💡 ${item.details}",
                                fontSize = 10.sp,
                                color = if (isMeal) WarningAmber else ElectricTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 8. JARVIS Notification Interceptor Service Card
 * Intercepts missed calls and incoming urgent communications, triggering rich local alerts.
 */
@Composable
fun JarvisInterceptorCard(
    onTestMissedCall: () -> Unit,
    onTestUrgentMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(AlertRed.copy(alpha = 0.8f), NeonIndigo)),
                RoundedCornerShape(20.dp)
            )
            .testTag("jarvis_interceptor_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AlertRed, NeonIndigo))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚨", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "JARVIS Active Interceptor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Missed Calls & Critical SMS Guard",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlertRed
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2E1019))
                        .border(1.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "INTERCEPTING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlertRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "The notification manager service intercepts incoming phone call disconnections and high-priority messages, instantly triggering heads-up local alerts with 'Call Back' and 'Reply SMS' actions.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Test Simulation Triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestMissedCall,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Trigger Intercept Call", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }

                Button(
                    onClick = onTestUrgentMessage,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Trigger Intercept SMS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }
    }
}

