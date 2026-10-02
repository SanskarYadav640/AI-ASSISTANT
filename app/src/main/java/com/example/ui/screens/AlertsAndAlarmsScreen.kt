package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.UrgentMessageEntity
import com.example.ui.PrivaViewModel
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
import java.util.Date
import java.util.Locale

@Composable
fun AlertsAndAlarmsScreen(
    viewModel: PrivaViewModel,
    initialSubTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val missedCalls by viewModel.missedCalls.collectAsStateWithLifecycle()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val urgentMessages by viewModel.urgentMessages.collectAsStateWithLifecycle()
    val emailDigests by viewModel.emailDigests.collectAsStateWithLifecycle()
    val deviceVolume by viewModel.deviceAlarmVolumePercent.collectAsStateWithLifecycle()

    var selectedSubTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab.coerceIn(0, 3)) }
    val tabs = listOf("Alarms & Audio", "Missed Calls", "Urgent Msgs", "Email Radar")

    var showAddAlarmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Sub Tab selector
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = DarkSurface,
            contentColor = ElectricCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = ElectricCyan
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedSubTab) {
            0 -> AlarmsAndAudioTab(
                alarms = alarms,
                deviceVolume = deviceVolume,
                onVolumeChange = { viewModel.setDeviceAlarmVolume(it) },
                onAlarmToggle = { id, enabled -> viewModel.toggleAlarm(id, enabled) },
                onApplyVolume = { alarm -> viewModel.triggerAlarmAuditAndVolumeSync(alarm) },
                onOpenSystemClock = { viewModel.audioController.openAlarmSettings(it) },
                onShowAddAlarm = { showAddAlarmDialog = true }
            )
            1 -> MissedCallsTab(
                calls = missedCalls,
                viewModel = viewModel,
                onResolve = { id, resolved -> viewModel.resolveMissedCall(id, resolved) },
                onNotify = { call -> viewModel.triggerMissedCallAlert(call) }
            )
            2 -> UrgentMessagesTab(
                messages = urgentMessages,
                viewModel = viewModel,
                onMarkRead = { id, isRead -> viewModel.markMessageRead(id, isRead) },
                onNotify = { msg -> viewModel.triggerUrgentMessageAlert(msg) }
            )
            3 -> EmailRadarTab(
                emails = emailDigests,
                viewModel = viewModel,
                onMarkAddressed = { id, addressed -> viewModel.markEmailAddressed(id, addressed) },
                onNotify = { email -> viewModel.triggerMissingEmailAlert(email) }
            )
        }
    }

    if (showAddAlarmDialog) {
        AddAlarmModalDialog(
            onDismiss = { showAddAlarmDialog = false },
            onConfirm = { label, hour, minute, days, volume ->
                viewModel.addNewAlarm(label, hour, minute, days, volume)
                showAddAlarmDialog = false
            }
        )
    }
}

@Composable
private fun AddAlarmModalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, String, Int) -> Unit
) {
    var label by remember { mutableStateOf("Work Standup Alarm") }
    var hourText by remember { mutableStateOf("08") }
    var minuteText by remember { mutableStateOf("15") }
    var daysText by remember { mutableStateOf("Mon, Tue, Wed, Thu, Fri") }
    var volume by remember { mutableIntStateOf(80) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Add Smart Alarm", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alarm Label") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it.take(2) },
                        label = { Text("Hour (0-23)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it.take(2) },
                        label = { Text("Minute (0-59)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("Repeat Days") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Target Volume: $volume%",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = volume.toFloat(),
                    onValueChange = { volume = it.toInt() },
                    valueRange = 10f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 8
                    val m = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    onConfirm(label, h, m, daysText, volume)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Save Alarm", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
private fun AlarmsAndAudioTab(
    alarms: List<AlarmEntity>,
    deviceVolume: Int,
    onVolumeChange: (Int) -> Unit,
    onAlarmToggle: (Long, Boolean) -> Unit,
    onApplyVolume: (AlarmEntity) -> Unit,
    onOpenSystemClock: (android.content.Context) -> Unit,
    onShowAddAlarm: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Device Alarm Volume Controller Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Device Alarm Stream Volume",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "AudioManager.STREAM_ALARM",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "$deviceVolume%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Slider(
                        value = deviceVolume.toFloat(),
                        onValueChange = { onVolumeChange(it.toInt()) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("alarm_volume_slider")
                    )

                    // Quick volume presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to "Mute", 40 to "Gentle", 70 to "Normal", 85 to "Commute 85%", 100 to "Max").forEach { (vol, label) ->
                            FilterChip(
                                selected = (deviceVolume == vol),
                                onClick = { onVolumeChange(vol) },
                                label = { Text(text = label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan,
                                    selectedLabelColor = Color(0xFF003548),
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenSystemClock(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Launch,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "System Clock", color = ElectricCyan, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onShowAddAlarm,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF003548),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Add Alarm", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "SCHEDULED ALARMS & AI AUDIT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        items(alarms, key = { it.id }) { alarm ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (alarm.isEnabled) ElectricCyan.copy(alpha = 0.2f) else DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (alarm.isEnabled) ElectricCyan else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                val timeFormatted = String.format(Locale.US, "%02d:%02d", alarm.timeHour, alarm.timeMinute)
                                Text(
                                    text = "$timeFormatted • ${alarm.label}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (alarm.isEnabled) Color.White else Color.Gray
                                )
                                Text(
                                    text = "${alarm.daysOfWeek} • Target: ${alarm.targetVolumePercent}% vol",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = alarm.isEnabled,
                            onCheckedChange = { onAlarmToggle(alarm.id, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectricCyan,
                                checkedTrackColor = ElectricCyan.copy(alpha = 0.4f)
                            )
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
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Volume Audit: ${alarm.aiRecommendation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onApplyVolume(alarm) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sync Volume (${alarm.targetVolumePercent}%) & Audit",
                                fontSize = 11.sp,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissedCallsTab(
    calls: List<MissedCallEntity>,
    viewModel: PrivaViewModel,
    onResolve: (Long, Boolean) -> Unit,
    onNotify: (MissedCallEntity) -> Unit
) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()) }
    val isSyncing by viewModel.isSyncingCalls.collectAsStateWithLifecycle()

    var filterOption by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var syncFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val callPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_CALL_LOG] == true
        if (granted) {
            viewModel.syncDeviceMissedCalls(clearSamples = true) { count ->
                syncFeedbackMessage = if (count > 0) {
                    "Synced $count missed calls from phone Call Log"
                } else {
                    "Call Log accessed. 0 missed calls found on device."
                }
                Toast.makeText(context, syncFeedbackMessage, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Call Log permission required to sync phone missed calls", Toast.LENGTH_SHORT).show()
        }
    }

    val hasSampleCalls = calls.any {
        it.reasonSummary.contains("metabolic panel") ||
                it.reasonSummary.contains("fraud security") ||
                it.reasonSummary.contains("board meeting")
    }

    val filteredCalls = when (filterOption) {
        "Unresolved" -> calls.filter { !it.isResolved }
        "Resolved" -> calls.filter { it.isResolved }
        else -> calls
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MISSED CALLS RADAR",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Call", fontSize = 11.sp, color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                if (viewModel.callLogHelper.hasCallLogPermission()) {
                                    viewModel.syncDeviceMissedCalls(clearSamples = true) { count ->
                                        syncFeedbackMessage = if (count > 0) {
                                            "Synced $count missed calls from phone"
                                        } else {
                                            "Call Log accessed: 0 missed calls on device."
                                        }
                                        Toast.makeText(context, syncFeedbackMessage, Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    callPermissionsLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_CALL_LOG,
                                            Manifest.permission.READ_CONTACTS
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = ElectricTeal)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Syncing...", fontSize = 11.sp, color = ElectricTeal)
                            } else {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Phone", fontSize = 11.sp, color = ElectricTeal)
                            }
                        }

                        if (calls.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearDialog = true },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Options", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Sample data notice banner if sample data is present
                if (hasSampleCalls) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF161F33))
                            .border(1.dp, NeonIndigo.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Preview Sample Calls Displayed",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                Text(
                                    text = "Tap 'Sync Phone' to load real calls from your device, or clear sample calls.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.clearSampleMissedCalls() },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Clear Sample", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val unresolvedCount = calls.count { !it.isResolved }
                    val resolvedCount = calls.count { it.isResolved }

                    listOf(
                        "All" to "All (${calls.size})",
                        "Unresolved" to "Unresolved ($unresolvedCount)",
                        "Resolved" to "Resolved ($resolvedCount)"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = filterOption == key,
                            onClick = { filterOption = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (key == "Unresolved" && unresolvedCount > 0) AlertRed else ElectricCyan,
                                selectedLabelColor = if (key == "Unresolved" && unresolvedCount > 0) Color.White else Color(0xFF003548),
                                containerColor = DarkSurfaceVariant,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        if (filteredCalls.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (filterOption == "Unresolved") "No pending unresolved calls." else "No missed calls found in vault.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Log a Missed Call Manually", color = ElectricCyan, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        items(filteredCalls, key = { it.id }) { call ->
            val isSample = call.reasonSummary.contains("metabolic panel") ||
                    call.reasonSummary.contains("fraud security") ||
                    call.reasonSummary.contains("board meeting")
            val isPhoneLog = call.reasonSummary.contains("Call Log") || call.reasonSummary.contains("Incoming call") || call.reasonSummary.contains("Unanswered call")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (!call.isResolved && call.urgency == "Urgent") AlertRed.copy(alpha = 0.5f) else DarkCardBorder,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (call.urgency == "Urgent") AlertRed.copy(alpha = 0.2f) else DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.PhoneMissed,
                                    contentDescription = null,
                                    tint = if (call.urgency == "Urgent") AlertRed else Color.LightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = call.callerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (isPhoneLog) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ElectricTeal.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("PHONE LOG", fontSize = 8.sp, color = ElectricTeal, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (isSample) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(NeonIndigo.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("SAMPLE", fontSize = 8.sp, color = NeonIndigo, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = "${call.phoneNumber} • ${sdf.format(Date(call.timestamp))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (call.isResolved) SuccessGreen.copy(alpha = 0.2f) else AlertRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (call.isResolved) "RESOLVED" else call.urgency.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (call.isResolved) SuccessGreen else AlertRed
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { viewModel.deleteMissedCall(call.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Call",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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
                            text = "AI Context Note: ${call.reasonSummary}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${call.phoneNumber}")
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = Color(0xFF00382E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Call Back", color = Color(0xFF00382E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onResolve(call.id, !call.isResolved) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (call.isResolved) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = if (call.isResolved) SuccessGreen else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (call.isResolved) "Re-open" else "Mark Done",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = { onNotify(call) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Test Notification",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var reason by remember { mutableStateOf("") }
        var urgency by remember { mutableStateOf("Urgent") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSurface,
            title = { Text("Log Missed Call", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Caller Name or Contact") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason Summary / Context") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Urgent", "Medium", "Normal").forEach { u ->
                            FilterChip(
                                selected = urgency == u,
                                onClick = { urgency = u },
                                label = { Text(u, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (u == "Urgent") AlertRed else ElectricCyan,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addNewMissedCall(
                                callerName = name,
                                phone = phone.ifBlank { "Unknown" },
                                reason = reason.ifBlank { "Important callback requested" },
                                urgency = urgency
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Save", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = DarkSurface,
            title = { Text("Manage Missed Calls", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "You can remove the initial preview sample calls or wipe the entire missed calls log.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.clearSampleMissedCalls()
                            showClearDialog = false
                            Toast.makeText(context, "Sample calls cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Remove Preview Sample Calls Only", color = ElectricCyan, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllMissedCalls()
                        showClearDialog = false
                        Toast.makeText(context, "All missed calls cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Clear Everything", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun UrgentMessagesTab(
    messages: List<UrgentMessageEntity>,
    viewModel: PrivaViewModel,
    onMarkRead: (Long, Boolean) -> Unit,
    onNotify: (UrgentMessageEntity) -> Unit
) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()) }
    val isSyncingSms by viewModel.isSyncingSms.collectAsStateWithLifecycle()
    var isSmsConnected by remember { mutableStateOf<Boolean>(viewModel.smsHelper.hasSmsPermission()) }

    var showSimulateDialog by remember { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.READ_SMS] == true
        isSmsConnected = granted
        if (granted) {
            viewModel.syncDeviceSmsMessages { count ->
                Toast.makeText(context, "Connected to Device SMS: Synced $count messages", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "SMS permission required to read device messages", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // SMS Connection Status Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isSmsConnected) SuccessGreen.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isSmsConnected) SuccessGreen else WarningAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSmsConnected) "DEVICE SMS CONNECTED" else "MESSAGES NOT CONNECTED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSmsConnected) SuccessGreen else WarningAmber,
                                letterSpacing = 1.sp
                            )
                        }

                        if (isSyncingSms) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = ElectricCyan, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Syncing...", fontSize = 10.sp, color = ElectricCyan)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSmsConnected)
                            "Scans local Android Telephony SMS inbox on-device with zero cloud transmission."
                        else
                            "Grant SMS permission to connect your real device messages for on-device AI summarization.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isSmsConnected) {
                                    viewModel.syncDeviceSmsMessages { count ->
                                        Toast.makeText(context, "Synced $count device SMS messages", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    smsPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_SMS,
                                            Manifest.permission.READ_CONTACTS
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSmsConnected) ElectricTeal else ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSmsConnected) "Sync Phone SMS" else "Connect SMS",
                                color = Color(0xFF003548),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { showSimulateDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Simulate SMS", color = ElectricCyan, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = {
                                viewModel.clearSampleMessages()
                                Toast.makeText(context, "Cleared preview samples; device SMS preserved", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Clear Samples", tint = AlertRed)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "PRIVATE URGENT MESSAGE SUMMARIES (${messages.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        if (messages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No urgent messages recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap 'Connect SMS' to import phone messages or 'Simulate SMS' to test.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(messages, key = { it.id }) { msg ->
            var expanded by remember { mutableStateOf(false) }
            var showQuickReplies by remember { mutableStateOf(false) }
            val quickReplies = remember(msg) { viewModel.getQuickRepliesForMessage(msg) }
            val isRealSms = msg.channel.contains("SMS", ignoreCase = true)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (!msg.isRead && msg.urgencyLevel == "Critical") AlertRed.copy(alpha = 0.5f) else DarkCardBorder,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Message,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = msg.sender,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isRealSms) Color(0xFF0F2B1C) else Color(0xFF1B2335))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = if (isRealSms) "PHONE SMS" else "SAMPLE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRealSms) SuccessGreen else ElectricCyan
                                        )
                                    }
                                }
                                Text(
                                    text = "${msg.channel} • ${sdf.format(Date(msg.timestamp))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (msg.urgencyLevel == "Critical") AlertRed.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = msg.urgencyLevel.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (msg.urgencyLevel == "Critical") AlertRed else WarningAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "AI 1-Line TL;DR: ${msg.summary}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚡ Action: ${msg.actionRequired}",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningAmber,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Raw snippet expandable
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expanded = !expanded }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (expanded) "Hide local raw message" else "Inspect raw message",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = "\"${msg.rawSnippet}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            modifier = Modifier
                                .background(Color(0xFF070B13), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        )
                    }

                    // Quick AI replies expandable
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showQuickReplies = !showQuickReplies }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showQuickReplies) "Hide AI Quick Replies" else "Generate Private Quick Replies",
                            fontSize = 11.sp,
                            color = ElectricTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = showQuickReplies) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            quickReplies.forEach { reply ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0D1D28))
                                        .border(1.dp, ElectricTeal.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, reply)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Send Quick Reply"))
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = reply, fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1f))
                                        Icon(imageVector = Icons.AutoMirrored.Filled.Reply, contentDescription = "Send", tint = ElectricTeal, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onMarkRead(msg.id, !msg.isRead) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (msg.isRead) "Mark Unread" else "Mark Read",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { onNotify(msg) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Alert Notification", fontSize = 11.sp, color = ElectricCyan)
                        }
                    }
                }
            }
        }
    }

    if (showSimulateDialog) {
        var simSender by remember { mutableStateOf("HDFC Bank Alert") }
        var simText by remember { mutableStateOf("Dear Customer, OTP for INR 24,500 at Flipkart is 894120. Valid for 10 mins. Do not share.") }

        AlertDialog(
            onDismissRequest = { showSimulateDialog = false },
            containerColor = DarkSurface,
            title = { Text("Simulate Priority SMS", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = simSender,
                        onValueChange = { simSender = it },
                        label = { Text("Sender Name / Bank") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = simText,
                        onValueChange = { simText = it },
                        label = { Text("Message Body") },
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
                        viewModel.simulateNewUrgentMessage(simSender, simText, channel = "SMS")
                        showSimulateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Ingest & Summarize", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSimulateDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun EmailRadarTab(
    emails: List<EmailDigestEntity>,
    viewModel: PrivaViewModel,
    onMarkAddressed: (Long, Boolean) -> Unit,
    onNotify: (EmailDigestEntity) -> Unit
) {
    var showAddEmailDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("All") }

    val displayedEmails = remember(emails, filterType) {
        when (filterType) {
            "Missing Action" -> emails.filter { it.isMissingImportant && !it.isAddressed }
            "Addressed" -> emails.filter { it.isAddressed }
            else -> emails
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonIndigo.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("EMAIL RADAR AUDIT", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Private On-Device Action Item Tracking", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        IconButton(
                            onClick = {
                                val missingCount = emails.count { it.isMissingImportant && !it.isAddressed }
                                viewModel.voiceSpeaker.speakInCharacter("Sir, your Email Radar shows $missingCount high-priority action items requiring your verification, including quarterly TDS Form 16 filing and partner agreement exhibits.")
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak Briefing", tint = ElectricCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddEmailDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Email", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                // Add rich default emails if missing
                                viewModel.addEmailDigest(
                                    sender = "finance-audit@company.in",
                                    subject = "URGENT: Form 16 & TDS Quarterly Filing verification required",
                                    snippet = "Please verify your tax deduction declarations and sign the attached declaration.",
                                    category = "Important Missing",
                                    summary = "Quarterly TDS self-declaration form is missing your signature; statutory deadline tomorrow 5:00 PM IST.",
                                    missingDetail = "Signed tax deduction self-declaration document is missing."
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restore Sample", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "All" to "All (${emails.size})",
                    "Missing Action" to "Action Items (${emails.count { it.isMissingImportant && !it.isAddressed }})",
                    "Addressed" to "Resolved (${emails.count { it.isAddressed }})"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = filterType == key,
                        onClick = { filterType = key },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = Color(0xFF003548),
                            containerColor = DarkSurfaceVariant,
                            labelColor = Color.White
                        )
                    )
                }
            }
        }

        if (displayedEmails.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No emails matching selected filter.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { showAddEmailDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) {
                            Text("Add Email Item", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(displayedEmails, key = { it.id }) { email ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (email.isMissingImportant && !email.isAddressed) AlertRed.copy(alpha = 0.5f) else DarkCardBorder,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mail,
                                    contentDescription = null,
                                    tint = NeonIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = email.sender,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = email.subject,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        if (email.isMissingImportant) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AlertRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "MISSING ITEM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                            }
                        }
                    }

                    if (email.isMissingImportant && email.missingDetail.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF331417))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "❗ You are missing: ${email.missingDetail}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "AI Summary: ${email.aiSummary}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onMarkAddressed(email.id, !email.isAddressed) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (email.isAddressed) "Resolved" else "Mark Addressed",
                                fontSize = 11.sp,
                                color = if (email.isAddressed) SuccessGreen else Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onNotify(email) },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Send Alert", fontSize = 11.sp, color = ElectricCyan)
                            }

                            IconButton(
                                onClick = { viewModel.deleteEmailDigest(email.id) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AlertRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEmailDialog) {
        var sender by remember { mutableStateOf("statutory-filings@gov.in") }
        var subject by remember { mutableStateOf("Notice: Annual Financial Reporting Verification") }
        var snippet by remember { mutableStateOf("Immediate submission of authenticated signature requested.") }
        var missingDetail by remember { mutableStateOf("Missing authenticated corporate signature.") }

        AlertDialog(
            onDismissRequest = { showAddEmailDialog = false },
            containerColor = DarkSurface,
            title = { Text("Track New Priority Email", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sender,
                        onValueChange = { sender = it },
                        label = { Text("Sender") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = missingDetail,
                        onValueChange = { missingDetail = it },
                        label = { Text("Missing Detail / Action Item") },
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
                        viewModel.addEmailDigest(
                            sender = sender,
                            subject = subject,
                            snippet = snippet,
                            category = "Important Missing",
                            summary = "$subject from $sender",
                            missingDetail = missingDetail
                        )
                        showAddEmailDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Add to Radar", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddEmailDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}
