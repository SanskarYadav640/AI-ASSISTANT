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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.PrivaViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SeaBlue
import com.example.ui.theme.SeaBlueGlow
import com.example.ui.theme.StarkGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun TodayBriefingScreen(
    viewModel: PrivaViewModel,
    modifier: Modifier = Modifier,
    onNavigateToAlerts: (Int) -> Unit = {},
    onNavigateToRadar: (Int) -> Unit = {},
    onOpenCarHud: () -> Unit = {}
) {
    val missedCalls by viewModel.missedCalls.collectAsStateWithLifecycle()
    val urgentMessages by viewModel.urgentMessages.collectAsStateWithLifecycle()
    val emailDigests by viewModel.emailDigests.collectAsStateWithLifecycle()
    val traffic by viewModel.commuteTraffic.collectAsStateWithLifecycle()
    val sectors by viewModel.sectorInflation.collectAsStateWithLifecycle()
    val isGeneratingBriefing by viewModel.isGeneratingAiBriefing.collectAsStateWithLifecycle()
    val liveBriefingText by viewModel.liveAiBriefingText.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val askHistory by viewModel.askPrivaHistory.collectAsStateWithLifecycle()
    val isAskingPriva by viewModel.isAskingPriva.collectAsStateWithLifecycle()
    val isMasterSyncing by viewModel.isMasterSyncing.collectAsStateWithLifecycle()

    val isVoiceListening by viewModel.voiceCommandManager.isListening.collectAsStateWithLifecycle()
    val voiceSpokenText by viewModel.voiceCommandManager.spokenText.collectAsStateWithLifecycle()
    val voiceCommandStatus by viewModel.voiceCommandManager.commandStatus.collectAsStateWithLifecycle()

    val mapSearchResults by viewModel.mapSearchResults.collectAsStateWithLifecycle()
    val isSearchingMap by viewModel.isSearchingMap.collectAsStateWithLifecycle()

    val missingEmails = emailDigests.filter { it.isMissingImportant && !it.isAddressed }
    val topRisingSector = sectors.firstOrNull()

    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val phoneUnlockLogs by viewModel.phoneUnlockLogs.collectAsStateWithLifecycle()
    val todayWakeUp by viewModel.todayWakeUpLog.collectAsStateWithLifecycle()
    val smartWatchHealth by viewModel.smartWatchHealth.collectAsStateWithLifecycle()
    val isSyncingCalendar by viewModel.isSyncingCalendar.collectAsStateWithLifecycle()
    val dailyPlanItems by viewModel.dailyPlanItems.collectAsStateWithLifecycle()
    val todayWaterMl by viewModel.todayWaterMl.collectAsStateWithLifecycle()
    val isRefreshingTraffic by viewModel.isRefreshingTraffic.collectAsStateWithLifecycle()
    val autoRunVoice by viewModel.autoRunVoice.collectAsStateWithLifecycle()

    var selectedJarvisSection by remember { mutableIntStateOf(0) }
    var userQuestion by remember { mutableStateOf("") }
    var showCommuteDialog by remember { mutableStateOf(false) }
    var showAddCalendarDialog by remember { mutableStateOf(false) }
    var detectedLocationText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsStateWithLifecycle()

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.syncCalendar { count ->
                Toast.makeText(context, "Google Calendar: Synced $count events", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Calendar permission denied. Using synced workspace schedule.", Toast.LENGTH_SHORT).show()
        }
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.voiceCommandManager.startListening { command ->
                viewModel.handleVoiceCommand(command)
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice commands", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectDetailedLocation { result ->
                Toast.makeText(context, "GPS Location: ${result.fullAddress}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Live Clock Ticker
    var currentClockEpoch by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var displayTimezoneMode by remember { mutableStateOf("IST") }

    LaunchedEffect(Unit) {
        while (true) {
            currentClockEpoch = System.currentTimeMillis()
            delay(1000L)
        }
    }

    LaunchedEffect(displayTimezoneMode) {
        viewModel.voiceSpeaker.preferredTimeZone = if (displayTimezoneMode == "IST") {
            java.util.TimeZone.getTimeZone("Asia/Kolkata")
        } else {
            java.util.TimeZone.getDefault()
        }
    }

    val istTimeFormat = remember {
        SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
    }
    val istDateFormat = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
    }
    val localTimeFormat = remember { SimpleDateFormat("hh:mm:ss a (z)", Locale.getDefault()) }
    val localDateFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }

    val currentDate = Date(currentClockEpoch)
    val cal = remember(currentClockEpoch) {
        Calendar.getInstance(if (displayTimezoneMode == "IST") TimeZone.getTimeZone("Asia/Kolkata") else TimeZone.getDefault()).apply {
            time = currentDate
        }
    }
    val currentHour = cal.get(Calendar.HOUR_OF_DAY)

    // Accurate Day vs Night Recognition
    val isNightProtocol = currentHour >= 21 || currentHour < 5
    val timeSalutation = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }
    val butlerGreeting = "$timeSalutation, Sir."

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive Live Clock & Day/Night Recognition Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PitchBlack),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Brush.horizontalGradient(listOf(SeaBlue, SeaBlueGlow)), RoundedCornerShape(20.dp))
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SeaBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isNightProtocol) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                                contentDescription = if (isNightProtocol) "Night Protocol" else "Day Protocol",
                                tint = if (isNightProtocol) SeaBlue else StarkGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = butlerGreeting,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = PureWhite,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isNightProtocol) "🌙 NIGHT PROTOCOL ACTIVE (STANDBY)" else "☀️ DAY PROTOCOL ACTIVE (KALYAN-SHILPHATA)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightProtocol) SeaBlue else SuccessGreen
                            )
                        }
                    }

                    // Timezone Mode Switcher
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (displayTimezoneMode == "IST") SeaBlue else Color.Transparent)
                                .clickable { displayTimezoneMode = "IST" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🇮🇳 IST",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (displayTimezoneMode == "IST") PitchBlack else PureWhite
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (displayTimezoneMode == "LOCAL") SeaBlue else Color.Transparent)
                                .clickable { displayTimezoneMode = "LOCAL" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LOCAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (displayTimezoneMode == "LOCAL") PitchBlack else PureWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Digital Clock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = if (displayTimezoneMode == "IST") istTimeFormat.format(currentDate) else localTimeFormat.format(currentDate),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (displayTimezoneMode == "IST") istDateFormat.format(currentDate) else localDateFormat.format(currentDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SeaBlue
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isNightProtocol) Color(0xFF0F1A2E) else Color(0xFF0E2A1D))
                            .border(1.dp, if (isNightProtocol) SeaBlue.copy(alpha = 0.5f) else SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (displayTimezoneMode == "IST") "IST • GMT+5:30" else "DEVICE TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightProtocol) SeaBlue else SuccessGreen
                        )
                    }
                }
            }
        }

        // 2. MASTER SYNCHRONIZE CARD: One-Tap Sync Everything
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PitchBlack),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Brush.horizontalGradient(listOf(SeaBlue, SeaBlueGlow)), RoundedCornerShape(18.dp))
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
                                .clip(CircleShape)
                                .background(SeaBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = SeaBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MASTER SYNCHRONIZATION",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Arihant Aarohi Route • Calendar • News • Market • Health",
                                style = MaterialTheme.typography.bodySmall,
                                color = SeaBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.syncEverything {
                            Toast.makeText(context, "All Systems Synchronized with Arihant Aarohi!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeaBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isMasterSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PitchBlack, strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SYNCHRONIZING ALL SYSTEMS...", color = PitchBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = PitchBlack, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SYNCHRONIZE EVERYTHING AT ONCE", color = PitchBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. INTERACTIVE VOICE COMMAND CARD ("Hey Jarvis!", "Hello Jarvis!")
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PitchBlack),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, if (isVoiceListening) SuccessGreen else SeaBlue.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
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
                                .clip(CircleShape)
                                .background(if (isVoiceListening) SuccessGreen.copy(alpha = 0.2f) else SeaBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isVoiceListening) Icons.Default.Mic else Icons.Default.RecordVoiceOver,
                                contentDescription = "Voice",
                                tint = if (isVoiceListening) SuccessGreen else SeaBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VOICE COMMAND (HEY JARVIS!)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = voiceCommandStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isVoiceListening) SuccessGreen else SeaBlue
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (isVoiceListening) {
                                viewModel.voiceCommandManager.stopListening()
                            } else {
                                if (viewModel.voiceCommandManager.hasRecordPermission()) {
                                    viewModel.voiceCommandManager.startListening { command ->
                                        viewModel.handleVoiceCommand(command)
                                    }
                                } else {
                                    recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceListening) AlertRed else SeaBlue)
                    ) {
                        Icon(
                            imageVector = if (isVoiceListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = PitchBlack,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = voiceSpokenText.isNotBlank()) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Heard: \"$voiceSpokenText\"",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SeaBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Voice Quick Test Buttons: Dynamic according to time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        1 to "$timeSalutation, Sir.",
                        3 to "Arihant Aarohi Route",
                        4 to "Master Synchronize"
                    ).forEach { (idx, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, SeaBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.testButlerVoicePhrase(idx) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Section Switcher Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedJarvisSection,
            containerColor = DarkSurface,
            contentColor = SeaBlue,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedJarvisSection]),
                    color = SeaBlue
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
        ) {
            listOf(
                "1. Protocol & Routine",
                "2. Commute & Map Radar",
                "3. Butler AI & Health",
                "4. Market & News"
            ).forEachIndexed { index, title ->
                Tab(
                    selected = selectedJarvisSection == index,
                    onClick = { selectedJarvisSection = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedJarvisSection == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // Section Content
        when (selectedJarvisSection) {
            0 -> TodayProtocolSection(
                planItems = dailyPlanItems,
                onTogglePlanItem = { id, done -> viewModel.togglePlanItem(id, done) },
                onMealReminder = { item -> viewModel.triggerMealReminder(item) },
                currentWaterMl = todayWaterMl,
                goalWaterMl = 3000,
                onAddWater = { ml -> viewModel.addWater(ml) },
                onResetWater = { viewModel.resetWater() },
                onTriggerWaterReminder = { viewModel.triggerHydrationReminder() },
                currentEpoch = currentClockEpoch,
                todayWakeUp = todayWakeUp,
                unlockLogs = phoneUnlockLogs,
                onTestUnlock = { viewModel.simulateUnlockWakeUp() },
                onClearUnlockLogs = { viewModel.clearUnlockLogs() }
            )
            1 -> TodayInterceptorCommuteSection(
                onTestMissedCall = { viewModel.simulateInterceptMissedCall() },
                onTestUrgentMessage = { viewModel.simulateInterceptUrgentMessage() },
                unresolvedCalls = missedCalls.filter { !it.isResolved },
                unreadMessages = urgentMessages.filter { !it.isRead },
                onNavigateToAlerts = onNavigateToAlerts,
                traffic = traffic,
                detectedLocationText = detectedLocationText,
                isDetectingLocation = isDetectingLocation,
                onDetectLocation = {
                    if (viewModel.locationController.hasLocationPermission()) {
                        viewModel.detectDetailedLocation { result ->
                            detectedLocationText = result.fullAddress
                            Toast.makeText(context, "Location: ${result.fullAddress}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                onSelectDestinationPreset = { dest ->
                    val home = traffic?.homeAddress ?: "Arihant Aarohi, Kalyan-Shilphata Road"
                    viewModel.updateCommuteRoute(home, dest)
                    viewModel.refreshTrafficWithApi()
                },
                onOpenCommuteDialog = { showCommuteDialog = true },
                onSwapRoute = {
                    val current = traffic
                    if (current != null) {
                        viewModel.updateCommuteRoute(current.officeAddress, current.homeAddress)
                        viewModel.refreshTrafficWithApi()
                    }
                },
                onOpenCarHud = onOpenCarHud,
                onRefreshLiveTraffic = {
                    viewModel.refreshTrafficWithApi { updated ->
                        Toast.makeText(context, "Traffic updated: ${updated.currentMinutes} min", Toast.LENGTH_SHORT).show()
                    }
                },
                isRefreshingTraffic = isRefreshingTraffic,
                onSpeakTraffic = { text -> viewModel.speakBriefing(text) }
            )
            2 -> TodayButlerHealthSection(
                isSpeaking = isSpeaking,
                liveBriefingText = liveBriefingText,
                onStopSpeaking = { viewModel.stopSpeaking() },
                onSpeakBriefing = { text -> viewModel.speakBriefing(text) },
                userQuestion = userQuestion,
                onUserQuestionChange = { userQuestion = it },
                isAskingPriva = isAskingPriva,
                onAskQuestion = { q ->
                    viewModel.askPriva(q)
                    userQuestion = ""
                },
                qaHistory = askHistory,
                calendarEvents = calendarEvents,
                isSyncingCalendar = isSyncingCalendar,
                onSyncCalendar = {
                    if (viewModel.calendarHelper.hasCalendarPermission()) {
                        viewModel.syncCalendar { count ->
                            Toast.makeText(context, "Google Calendar: $count events synced", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    }
                },
                onOpenCalendar = { viewModel.openGoogleCalendar() },
                onAddEventClick = { showAddCalendarDialog = true },
                smartWatchHealth = smartWatchHealth,
                onLaunchTitanApp = { viewModel.launchTitanSmartApp() },
                onLaunchHealthConnect = { viewModel.launchHealthConnect() },
                onRefreshHealth = {
                    viewModel.refreshSmartWatchMetrics()
                    Toast.makeText(context, "Titan Watch & Health Connect data refreshed", Toast.LENGTH_SHORT).show()
                }
            )
            3 -> TodayRadarMarketSection(
                missingEmails = missingEmails,
                emailDigests = emailDigests,
                topRisingSector = topRisingSector,
                onMarkEmailAddressed = { id, addressed -> viewModel.markEmailAddressed(id, addressed) },
                onOpenGmail = { viewModel.launchGmail() },
                onComposeEmail = { viewModel.launchGmail(toAddress = "work@seawoods.in", subject = "Update regarding Seawoods Grand Central schedule") },
                onNavigateToRadar = onNavigateToRadar
            )
        }
    }

    // Modal Dialog: Map Address Selector & Direct Route Setup
    if (showCommuteDialog) {
        var startPointText by remember(traffic) { mutableStateOf(traffic?.homeAddress ?: "Arihant Aarohi, Kalyan-Shilphata Road") }
        var destinationText by remember(traffic) { mutableStateOf(traffic?.officeAddress ?: "Seawoods Grand Central Mall, Navi Mumbai") }
        var routeText by remember(traffic) { mutableStateOf(traffic?.currentRouteName ?: "Kalyan-Shilphata Rd & Thane-Belapur / Palm Beach") }
        var mapSearchInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCommuteDialog = false },
            containerColor = PitchBlack,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.EditLocation, contentDescription = null, tint = SeaBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Map Location & Route Selector", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Home Location is Arihant Aarohi. Search any location on OpenStreetMap or select presets below to add directly into the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SeaBlue
                    )

                    // Quick Set Arihant Aarohi as Home Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                startPointText = "Arihant Aarohi, Kalyan-Shilphata Road"
                                Toast.makeText(context, "Home set to Arihant Aarohi", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SeaBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = PitchBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Arihant Aarohi (Home)", color = PitchBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // Map Search Input Box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = mapSearchInput,
                            onValueChange = { mapSearchInput = it },
                            placeholder = { Text("Search address / landmark on map...", fontSize = 11.sp, color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SeaBlue,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (mapSearchInput.isNotBlank()) {
                                    viewModel.searchAddressOnMap(mapSearchInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SeaBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isSearchingMap) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PitchBlack, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, tint = PitchBlack, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Map Search Results List
                    if (mapSearchResults.isNotEmpty()) {
                        Text(text = "SEARCH RESULTS (TAP TO SET):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SeaBlue)
                        mapSearchResults.forEach { res ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = res.name, fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 12.sp)
                                    Text(text = res.address, fontSize = 10.sp, color = TextMuted, maxLines = 2)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.selectMapAddressAsHome(res)
                                                startPointText = res.address
                                                showCommuteDialog = false
                                                Toast.makeText(context, "Added as Home: ${res.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Add as Home", fontSize = 10.sp, color = SeaBlue, maxLines = 1)
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.selectMapAddressAsDestination(res)
                                                destinationText = res.address
                                                showCommuteDialog = false
                                                Toast.makeText(context, "Added to App: ${res.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SeaBlue),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Add to App", fontSize = 10.sp, color = PitchBlack, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = startPointText,
                        onValueChange = { startPointText = it },
                        label = { Text("Start Point (Home / Origin)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = SeaBlue)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SeaBlue,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = destinationText,
                        onValueChange = { destinationText = it },
                        label = { Text("Destination (Where You Need To Go)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = SeaBlueGlow)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SeaBlueGlow,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = routeText,
                        onValueChange = { routeText = it },
                        label = { Text("Route / Preferred Highway") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SeaBlue,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Open In External Google Maps Button
                    OutlinedButton(
                        onClick = {
                            try {
                                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(destinationText))).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(destinationText)))
                                context.startActivity(browserIntent)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify & Open in Google Maps", color = SeaBlue, fontSize = 11.sp)
                    }

                    // Popular Destination Presets for Arihant Aarohi Corridor
                    Text(
                        text = "EXECUTIVE DESTINATION PRESETS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeaBlue
                    )

                    listOf(
                        "Seawoods Grand Central Mall, Navi Mumbai" to "Kalyan-Shilphata Rd & Thane-Belapur / Palm Beach",
                        "Bandra-Kurla Complex (BKC), Mumbai" to "Eastern Express Hwy & SCLR",
                        "Navi Mumbai International Airport (NMIA)" to "Kalyan-Shilphata & Sion-Panvel Hwy",
                        "Cyber City, DLF Phase 2, Gurugram" to "NH-48 Delhi-Gurgaon Expressway"
                    ).forEach { (destName, prefRoute) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .clickable {
                                    destinationText = destName
                                    routeText = prefRoute
                                }
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = destName, fontSize = 11.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                                Text(text = "via $prefRoute", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCommuteRoute(
                            home = startPointText,
                            office = destinationText,
                            customRouteName = routeText.takeIf { it.isNotBlank() }
                        )
                        viewModel.refreshTrafficWithApi { updated ->
                            Toast.makeText(context, "Live Traffic: ${updated.currentMinutes} min (${updated.trafficCondition})", Toast.LENGTH_SHORT).show()
                        }
                        showCommuteDialog = false
                        Toast.makeText(context, "Route saved: $startPointText ➔ $destinationText", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeaBlue)
                ) {
                    Text("Save & Calculate Route", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCommuteDialog = false }) {
                    Text("Cancel", color = PureWhite)
                }
            }
        )
    }

    // Modal Dialog: Add Calendar Event
    if (showAddCalendarDialog) {
        AddCalendarEventDialog(
            onDismiss = { showAddCalendarDialog = false },
            onConfirm = { title, location, startEpoch, endEpoch, desc ->
                viewModel.addNewCalendarEvent(title, location, startEpoch, endEpoch, desc)
                showAddCalendarDialog = false
                Toast.makeText(context, "Calendar event created & synced", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
