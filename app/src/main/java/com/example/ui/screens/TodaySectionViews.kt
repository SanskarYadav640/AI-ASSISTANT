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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.CommuteTrafficEntity
import com.example.data.local.entity.DailyPlanItemEntity
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.IndiaMarketEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.PhoneUnlockLogEntity
import com.example.data.local.entity.SectorInflationEntity
import com.example.data.local.entity.SmartWatchHealthEntity
import com.example.data.local.entity.UrgentMessageEntity
import com.example.ui.AskPrivaMessage
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArcReactorBlue
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SeaBlue
import com.example.ui.theme.StarkGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber

/**
 * Section 0: Day Protocol (Maharashtra Diet, Hydration, Wake-Up, Year-End Countdown)
 * Planned strictly by J.A.R.V.I.S.
 */
@Composable
fun TodayProtocolSection(
    planItems: List<DailyPlanItemEntity>,
    onTogglePlanItem: (Long, Boolean) -> Unit,
    onMealReminder: (DailyPlanItemEntity) -> Unit,
    currentWaterMl: Int,
    goalWaterMl: Int,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit,
    onTriggerWaterReminder: () -> Unit,
    currentEpoch: Long,
    todayWakeUp: PhoneUnlockLogEntity?,
    unlockLogs: List<PhoneUnlockLogEntity>,
    onTestUnlock: () -> Unit,
    onClearUnlockLogs: () -> Unit
) {
    // 1. J.A.R.V.I.S. Daily Master Protocol (Authentic Maharashtra Menus & Routine)
    JarvisDayPlanCard(
        planItems = planItems,
        onToggleItem = onTogglePlanItem,
        onMealReminder = onMealReminder
    )

    // 2. J.A.R.V.I.S. Water Hydration Protocol Tracker
    WaterHydrationTrackerCard(
        currentMl = currentWaterMl,
        goalMl = goalWaterMl,
        onAddWater = onAddWater,
        onResetWater = onResetWater,
        onTriggerReminder = onTriggerWaterReminder
    )

    // 3. Year-End Countdown Card
    YearEndCountdownCard(currentEpoch = currentEpoch)

    // 4. Auto Wake-Up Detector & Phone Unlock History
    WakeUpTimeCard(
        todayWakeUp = todayWakeUp,
        unlockLogs = unlockLogs,
        onTestUnlock = onTestUnlock,
        onClearLogs = onClearUnlockLogs
    )
}

/**
 * Section 1: Interceptor & Commute (Missed calls & Urgent SMS interceptor + Seawoods Grand Central commute)
 */
@Composable
fun TodayInterceptorCommuteSection(
    onTestMissedCall: () -> Unit,
    onTestUrgentMessage: () -> Unit,
    unresolvedCalls: List<MissedCallEntity>,
    unreadMessages: List<UrgentMessageEntity>,
    onNavigateToAlerts: (Int) -> Unit,
    traffic: CommuteTrafficEntity?,
    detectedLocationText: String?,
    isDetectingLocation: Boolean,
    onDetectLocation: () -> Unit,
    onSelectDestinationPreset: (String) -> Unit,
    onOpenCommuteDialog: () -> Unit,
    onSwapRoute: () -> Unit,
    onOpenCarHud: () -> Unit = {},
    onRefreshLiveTraffic: () -> Unit = {},
    isRefreshingTraffic: Boolean = false,
    onSpeakTraffic: (String) -> Unit = {}
) {
    // 0. Android Auto & Car Mode Hub Card
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(ElectricCyan, SuccessGreen)),
                RoundedCornerShape(20.dp)
            )
            .clickable { onOpenCarHud() }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(ElectricCyan, ArcReactorBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Car Mode",
                            tint = Color(0xFF002B1D),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ANDROID AUTO & CAR MODE",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF00301D))
                                    .border(1.dp, SuccessGreen, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "READY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                        Text(
                            text = "Auto-detect car • British Butler greeting • YouTube Music",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open HUD",
                    tint = ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Greeting snippet banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .padding(10.dp)
            ) {
                Text(
                    text = "🎙️ \"Hello Sir! Good evening. Please tell me what is your destination?\"",
                    fontSize = 12.sp,
                    color = StarkGold,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎵 Auto-streams YouTube Music Playlists",
                    fontSize = 11.sp,
                    color = Color(0xFFFF4D6D)
                )

                Button(
                    onClick = onOpenCarHud,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Launch Car HUD", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }

    // 1. Active Notification Manager Interceptor Card
    JarvisInterceptorCard(
        onTestMissedCall = onTestMissedCall,
        onTestUrgentMessage = onTestUrgentMessage
    )

    // 2. Urgent Attention Radar Grid (Missed Calls & Messages)
    Text(
        text = "URGENT ATTENTION INTERCEPTOR RADAR",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Missed Calls Metric Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .weight(1f)
                .border(
                    1.dp,
                    if (unresolvedCalls.isNotEmpty()) AlertRed.copy(alpha = 0.5f) else DarkCardBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onNavigateToAlerts(1) }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (unresolvedCalls.isNotEmpty()) AlertRed.copy(alpha = 0.2f) else DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PhoneMissed,
                            contentDescription = null,
                            tint = if (unresolvedCalls.isNotEmpty()) AlertRed else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${unresolvedCalls.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (unresolvedCalls.isNotEmpty()) AlertRed else Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Missed Calls",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = if (unresolvedCalls.isNotEmpty()) "${unresolvedCalls.first().callerName}" else "All intercepted calls cleared",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        // Urgent Messages Metric Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .weight(1f)
                .border(
                    1.dp,
                    if (unreadMessages.isNotEmpty()) WarningAmber.copy(alpha = 0.5f) else DarkCardBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onNavigateToAlerts(2) }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (unreadMessages.isNotEmpty()) WarningAmber.copy(alpha = 0.2f) else DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Message,
                            contentDescription = null,
                            tint = if (unreadMessages.isNotEmpty()) WarningAmber else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${unreadMessages.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (unreadMessages.isNotEmpty()) WarningAmber else Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Urgent Messages",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = if (unreadMessages.isNotEmpty()) "${unreadMessages.first().sender}" else "No pending SMS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }

    // 3. Real-Time Commute & Office Traffic Navigator Card
    traffic?.let { commute ->
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
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
                                .background(Color(0xFF2B1D0E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Commute & Journey Radar",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = commute.currentRouteName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (commute.delayMinutes > 0) Color(0xFF3B1519) else Color(0xFF0F291E))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (commute.delayMinutes > 0) "+${commute.delayMinutes}m DELAY" else "NOMINAL FLOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (commute.delayMinutes > 0) AlertRed else SuccessGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explicit Origin & Destination Route Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Origin: Where you are
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "START POINT (WHERE YOU ARE)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElectricTeal)
                                    Text(text = commute.homeAddress, style = MaterialTheme.typography.bodySmall, color = Color.White, maxLines = 1)
                                }
                            }

                            IconButton(
                                onClick = onSwapRoute,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Reverse Route", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Transit connector
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.width(2.dp).height(12.dp).background(Color.Gray))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "via ${commute.currentRouteName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Destination: Where to go
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.NearMe, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "DESTINATION (WHERE TO GO)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                                Text(text = commute.officeAddress, style = MaterialTheme.typography.bodySmall, color = Color.White, maxLines = 1)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats: Duration, Normal Duration, Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${commute.currentMinutes}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (commute.delayMinutes > 5) WarningAmber else Color.White
                            )
                            Text(
                                text = " min total",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Text(
                            text = "Standard: ${commute.normalMinutes}m • Distance: ${commute.distanceKm} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "CONDITION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Text(text = commute.trafficCondition, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (commute.delayMinutes > 0) WarningAmber else SuccessGreen)
                        }
                    }
                }

                // Quick Destination Preset Chips
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "QUICK DESTINATIONS (\"WHERE TO GO\"):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "🏢 Seawoods Mall" to "Seawoods Grand Central Mall, Navi Mumbai",
                        "🏠 Arihant Aarohi" to "Arihant Aarohi, Kalyan-Shilphata Road",
                        "🏙️ BKC Mumbai" to "Bandra Kurla Complex (BKC), Mumbai",
                        "✈️ NMIA Airport" to "Navi Mumbai International Airport (NMIA)"
                    ).forEach { (label, dest) ->
                        val isSelected = commute.officeAddress.contains(dest, ignoreCase = true) || commute.officeAddress.contains("Seawoods", ignoreCase = true) && label.contains("Seawoods")
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectDestinationPreset(dest) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricCyan,
                                containerColor = DarkSurfaceVariant,
                                labelColor = Color.LightGray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feature: Select Address on Maps & Add Directly to App
                Button(
                    onClick = onOpenCommuteDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = SeaBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.AddLocation, contentDescription = null, tint = PitchBlack, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SELECT ADDRESS ON MAPS & ADD TO APP", color = PitchBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Incident Alert & Live Traffic API Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (commute.delayMinutes > 5) Color(0xFF2A1014) else Color(0xFF0F251E))
                        .border(1.dp, if (commute.delayMinutes > 5) AlertRed.copy(alpha = 0.5f) else SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INCIDENT & ROUTE TELEMETRY (LIVE OSRM API):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (commute.delayMinutes > 5) AlertRed else SuccessGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = commute.incidentAlert,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                onSpeakTraffic("Sir, traffic report: ${commute.trafficCondition}. Duration is ${commute.currentMinutes} minutes across ${commute.distanceKm} kilometers. ${commute.incidentAlert}")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Read Traffic",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action row with Live OSRM Traffic API Refresh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRefreshLiveTraffic,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        if (isRefreshingTraffic) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFF003548))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Updating API...", color = Color(0xFF003548), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live Traffic API", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onDetectLocation,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GPS Origin", color = ElectricTeal, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenCommuteDialog,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.EditLocation, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Map Selector", color = SeaBlue, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

/**
 * Section 2: Butler AI Voice, Interactive Query & Health Agenda
 */
@Composable
fun TodayButlerHealthSection(
    isSpeaking: Boolean,
    liveBriefingText: String?,
    onStopSpeaking: () -> Unit,
    onSpeakBriefing: (String) -> Unit,
    userQuestion: String,
    onUserQuestionChange: (String) -> Unit,
    isAskingPriva: Boolean,
    onAskQuestion: (String) -> Unit,
    qaHistory: List<AskPrivaMessage>,
    calendarEvents: List<CalendarEventEntity>,
    isSyncingCalendar: Boolean,
    onSyncCalendar: () -> Unit,
    onOpenCalendar: () -> Unit,
    onAddEventClick: () -> Unit,
    smartWatchHealth: SmartWatchHealthEntity?,
    onLaunchTitanApp: () -> Unit,
    onLaunchHealthConnect: () -> Unit,
    onRefreshHealth: () -> Unit
) {
    // 1. Live Audio Briefing Card if speaking or available
    AnimatedVisibility(visible = isSpeaking || liveBriefingText != null) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isSpeaking) Color(0xFF0D2235) else DarkSurfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isSpeaking) ElectricCyan else NeonIndigo.copy(alpha = 0.5f),
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSpeaking) ElectricCyan else NeonIndigo.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = if (isSpeaking) Color(0xFF003548) else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isSpeaking) "J.A.R.V.I.S. is Speaking, Sir..." else "Executive Audio Briefing",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "British Butler Persona (RP)",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElectricTeal
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                onStopSpeaking()
                            } else {
                                liveBriefingText?.let { onSpeakBriefing(it) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Play/Stop Voice",
                            tint = if (isSpeaking) AlertRed else ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                liveBriefingText?.let { briefing ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = briefing,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }

    // 2. Interactive "Ask J.A.R.V.I.S." Query Box
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ask J.A.R.V.I.S. (Executive AI Assistant)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Speaks aloud in character",
                    fontSize = 10.sp,
                    color = ElectricTeal
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pre-set quick questions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "What time is it?" to "What is the exact time?",
                    "Gold rate?" to "What are the live Gold and Silver prices in India?",
                    "Seawoods Mall?" to "How long to reach Seawoods Grand Central Mall in Navi Mumbai?",
                    "Iron Man suit" to "Shall I prepare the Iron Man suit, Sir?"
                ).forEach { (label, query) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { onAskQuestion(query) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Text Input Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = userQuestion,
                    onValueChange = onUserQuestionChange,
                    placeholder = { Text("Ask J.A.R.V.I.S. anything...", fontSize = 12.sp, color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (userQuestion.isNotBlank()) {
                            onAskQuestion(userQuestion)
                        }
                    }),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (userQuestion.isNotBlank()) {
                            onAskQuestion(userQuestion)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (userQuestion.isNotBlank()) ElectricCyan else DarkSurfaceVariant)
                ) {
                    if (isAskingPriva) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF003548), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send to J.A.R.V.I.S.",
                            tint = if (userQuestion.isNotBlank()) Color(0xFF003548) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Q&A History
            if (qaHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                qaHistory.take(2).forEach { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Sir: ${msg.question}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "J.A.R.V.I.S.: ${msg.answer}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // 3. Google Calendar Agenda Card
    GoogleCalendarAgendaCard(
        events = calendarEvents,
        isSyncing = isSyncingCalendar,
        onSyncCalendar = onSyncCalendar,
        onOpenCalendar = onOpenCalendar,
        onAddEventClick = onAddEventClick
    )

    // 4. Titan Smart Watch & Health Connect Card
    SmartWatchHealthCard(
        health = smartWatchHealth,
        onLaunchTitanApp = onLaunchTitanApp,
        onLaunchHealthConnect = onLaunchHealthConnect,
        onRefresh = onRefreshHealth
    )
}

/**
 * Section 3: Radar, Indian Markets & Corporate Email Audit
 */
@Composable
fun TodayRadarMarketSection(
    missingEmails: List<EmailDigestEntity>,
    emailDigests: List<EmailDigestEntity>,
    topRisingSector: SectorInflationEntity?,
    onMarkEmailAddressed: (Long, Boolean) -> Unit,
    onOpenGmail: () -> Unit,
    onComposeEmail: () -> Unit,
    onNavigateToRadar: (Int) -> Unit
) {
    // 1. Dual Overview Cards: Missing Action Items & Indian Market Spotlight
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Missing Emails Metric Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .weight(1f)
                .border(
                    1.dp,
                    if (missingEmails.isNotEmpty()) NeonIndigo.copy(alpha = 0.5f) else DarkCardBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onNavigateToRadar(4) }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailUnread,
                            contentDescription = null,
                            tint = NeonIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${missingEmails.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeonIndigo
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Email Radar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = if (missingEmails.isNotEmpty()) "${missingEmails.size} action items" else "Inbox clear",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        // Indian Market Spotlight Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .weight(1f)
                .border(1.dp, ElectricTeal.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onNavigateToRadar(1) }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "₹76.8k",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ElectricTeal
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gold & Stocks (₹)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Nifty 25,790 pts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }

    // 2. Gmail Hub Card (Connected to SanskarYadav640@gmail.com)
    GmailHubCard(
        onOpenGmail = onOpenGmail,
        onCompose = onComposeEmail
    )

    // 3. Dedicated Email Radar & Missing Action Items
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Email Radar: Active Action Items",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${emailDigests.size} monitored corporate & statutory emails",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { onNavigateToRadar(4) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View Radar", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            emailDigests.take(3).forEach { email ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (email.isMissingImportant) AlertRed.copy(alpha = 0.4f) else DarkCardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = email.sender,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (email.isMissingImportant) AlertRed.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (email.isMissingImportant) "ACTION REQUIRED" else "CONFIRMED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (email.isMissingImportant) AlertRed else SuccessGreen
                                )
                            }
                        }

                        Text(
                            text = email.subject,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )

                        if (email.isMissingImportant && email.missingDetail.isNotBlank()) {
                            Text(
                                text = "❗ ${email.missingDetail}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WarningAmber
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onMarkEmailAddressed(email.id, !email.isAddressed) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (email.isAddressed) "Resolved" else "Mark Addressed", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // 4. Sector Inflation Spotlight Card
    topRisingSector?.let { sector ->
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                .clickable { onNavigateToRadar(5) }
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
                                .background(NeonIndigo.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = NeonIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sector Cost Radar",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Inflation projection (6 months)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AlertRed.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+${sector.projectedChangePercent}% SURGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Costliest Sector: ${sector.sectorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sector.savingsAdvice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
