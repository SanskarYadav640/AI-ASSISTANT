package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.CommuteTrafficEntity
import com.example.data.local.entity.DailyPlanItemEntity
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.NewsItemEntity
import com.example.data.local.entity.NotificationLogEntity
import com.example.data.local.entity.PhoneUnlockLogEntity
import com.example.data.local.entity.PrivaSettingsEntity
import com.example.data.local.entity.SectorInflationEntity
import com.example.data.local.entity.SmartWatchHealthEntity
import com.example.data.local.entity.UrgentMessageEntity
import com.example.data.local.entity.SavedDestinationEntity
import com.example.data.local.entity.CarConnectionLogEntity
import com.example.data.repository.PrivaRepository
import com.example.network.LocalNewsTrafficService
import com.example.network.PrivaAiService
import com.example.service.CarConnectionDetector
import com.example.service.DeviceAudioController
import com.example.service.DeviceCalendarHelper
import com.example.service.DeviceCallLogHelper
import com.example.service.DeviceLocationController
import com.example.service.DeviceSmsHelper
import com.example.service.JarvisNotificationManagerService
import com.example.service.PrivaNotificationManager
import com.example.service.PrivaVoiceSpeaker
import com.example.service.SmartWatchAndHealthHelper
import com.example.service.YouTubeMusicController
import com.example.service.YouTubeMusicPlaylist
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AskPrivaMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

class PrivaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = PrivaRepository(database.privaDao())
    val audioController = DeviceAudioController(application)
    val notificationManager = PrivaNotificationManager(application)
    val aiService = PrivaAiService()
    val voiceSpeaker = PrivaVoiceSpeaker(application)
    val locationController = DeviceLocationController(application)
    val callLogHelper = DeviceCallLogHelper(application)
    val smsHelper = DeviceSmsHelper(application)
    val calendarHelper = DeviceCalendarHelper(application)
    val smartWatchHelper = SmartWatchAndHealthHelper(application)
    val jarvisNotificationService = JarvisNotificationManagerService(application)
    val localNewsTrafficService = LocalNewsTrafficService(aiService)
    val voiceCommandManager = com.example.service.JarvisVoiceCommandManager(application)

    private val _isMasterSyncing = MutableStateFlow(false)
    val isMasterSyncing: StateFlow<Boolean> = _isMasterSyncing.asStateFlow()

    private val _mapSearchResults = MutableStateFlow<List<com.example.network.MapSearchResult>>(emptyList())
    val mapSearchResults: StateFlow<List<com.example.network.MapSearchResult>> = _mapSearchResults.asStateFlow()

    private val _isSearchingMap = MutableStateFlow(false)
    val isSearchingMap: StateFlow<Boolean> = _isSearchingMap.asStateFlow()

    private val _isRefreshingTraffic = MutableStateFlow(false)
    val isRefreshingTraffic: StateFlow<Boolean> = _isRefreshingTraffic.asStateFlow()

    private val _isRefreshingNews = MutableStateFlow(false)
    val isRefreshingNews: StateFlow<Boolean> = _isRefreshingNews.asStateFlow()

    private val _autoRunVoice = MutableStateFlow(true)
    val autoRunVoice: StateFlow<Boolean> = _autoRunVoice.asStateFlow()

    val youtubeMusicController = YouTubeMusicController(application)
    val carConnectionDetector = CarConnectionDetector(
        context = application,
        voiceSpeaker = voiceSpeaker,
        youtubeMusicController = youtubeMusicController,
        scope = viewModelScope,
        onLogCarSession = { carName, connType, greeting, playlist ->
            viewModelScope.launch {
                repository.logCarConnection(carName, connType, greeting, musicPlaylist = playlist)
            }
        }
    )

    val savedDestinations: StateFlow<List<SavedDestinationEntity>> = repository.savedDestinations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val carConnectionLogs: StateFlow<List<CarConnectionLogEntity>> = repository.carConnectionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isCarConnected: StateFlow<Boolean> = carConnectionDetector.isCarConnected
    val connectedCarName: StateFlow<String> = carConnectionDetector.connectedCarName
    val carConnectionType: StateFlow<String> = carConnectionDetector.connectionType
    val carSpokenGreeting: StateFlow<String> = carConnectionDetector.lastSpokenGreeting
    val ytmCurrentPlaylist: StateFlow<YouTubeMusicPlaylist> = youtubeMusicController.currentPlaylist
    val ytmIsPlaying: StateFlow<Boolean> = youtubeMusicController.isPlaying
    val ytmAutoPlayOnConnect: StateFlow<Boolean> = youtubeMusicController.autoPlayOnCarConnect

    val missedCalls: StateFlow<List<MissedCallEntity>> = repository.missedCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alarms: StateFlow<List<AlarmEntity>> = repository.alarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val urgentMessages: StateFlow<List<UrgentMessageEntity>> = repository.urgentMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emailDigests: StateFlow<List<EmailDigestEntity>> = repository.emailDigests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commuteTraffic: StateFlow<CommuteTrafficEntity?> = repository.commuteTraffic
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val marketItems: StateFlow<List<com.example.data.local.entity.IndiaMarketEntity>> = repository.marketItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newsItems: StateFlow<List<NewsItemEntity>> = repository.newsItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sectorInflation: StateFlow<List<SectorInflationEntity>> = repository.sectorInflation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationLogs: StateFlow<List<NotificationLogEntity>> = repository.notificationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<PrivaSettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val calendarEvents: StateFlow<List<CalendarEventEntity>> = repository.calendarEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val phoneUnlockLogs: StateFlow<List<PhoneUnlockLogEntity>> = repository.phoneUnlockLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smartWatchHealth: StateFlow<SmartWatchHealthEntity?> = repository.smartWatchHealth
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyPlanItems: StateFlow<List<DailyPlanItemEntity>> = repository.dailyPlanItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val todayDateString: String
        get() = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    val todayWakeUpLog: StateFlow<PhoneUnlockLogEntity?> = repository.observeTodayWakeUpLog(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayWaterMl: StateFlow<Int> = repository.observeTodayWater(todayDateString)
        .map { it ?: 750 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 750)

    private val _isSyncingCalendar = MutableStateFlow(false)
    val isSyncingCalendar: StateFlow<Boolean> = _isSyncingCalendar.asStateFlow()

    private val _isSyncingSms = MutableStateFlow(false)
    val isSyncingSms: StateFlow<Boolean> = _isSyncingSms.asStateFlow()

    private val _deviceAlarmVolumePercent = MutableStateFlow(audioController.getAlarmVolumePercent())
    val deviceAlarmVolumePercent: StateFlow<Int> = _deviceAlarmVolumePercent.asStateFlow()

    private val _isGeneratingAiBriefing = MutableStateFlow(false)
    val isGeneratingAiBriefing: StateFlow<Boolean> = _isGeneratingAiBriefing.asStateFlow()

    private val _liveAiBriefingText = MutableStateFlow<String?>(null)
    val liveAiBriefingText: StateFlow<String?> = _liveAiBriefingText.asStateFlow()

    // Interactive Ask Priva Chat & Q&A
    private val _askPrivaHistory = MutableStateFlow<List<AskPrivaMessage>>(emptyList())
    val askPrivaHistory: StateFlow<List<AskPrivaMessage>> = _askPrivaHistory.asStateFlow()

    private val _isAskingPriva = MutableStateFlow(false)
    val isAskingPriva: StateFlow<Boolean> = _isAskingPriva.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = voiceSpeaker.isSpeaking

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            refreshAlarmVolume()
            jarvisNotificationService.registerReceivers()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceSpeaker.shutdown()
        smartWatchHelper.unregister()
        jarvisNotificationService.unregisterReceivers()
        voiceCommandManager.destroy()
    }

    fun refreshAlarmVolume() {
        _deviceAlarmVolumePercent.value = audioController.getAlarmVolumePercent()
    }

    fun setDeviceAlarmVolume(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        audioController.setAlarmVolumePercent(clamped)
        _deviceAlarmVolumePercent.value = clamped
    }

    fun resolveMissedCall(id: Long, resolved: Boolean = true) {
        viewModelScope.launch {
            repository.resolveMissedCall(id, resolved)
        }
    }

    fun markMessageRead(id: Long, isRead: Boolean = true) {
        viewModelScope.launch {
            repository.markMessageRead(id, isRead)
        }
    }

    fun markEmailAddressed(id: Long, addressed: Boolean = true) {
        viewModelScope.launch {
            repository.markEmailAddressed(id, addressed)
        }
    }

    fun toggleAlarm(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAlarm(id, isEnabled)
        }
    }

    fun updateAlarmVolume(id: Long, volume: Int) {
        viewModelScope.launch {
            repository.updateAlarmVolume(id, volume)
        }
    }

    fun addNewAlarm(label: String, hour: Int, minute: Int, days: String, volume: Int) {
        viewModelScope.launch {
            val recommendation = if (hour in 6..8) {
                "Morning commute alarm: volume set to $volume% with traffic monitoring enabled."
            } else {
                "Standard alarm set to $volume%."
            }
            val alarm = AlarmEntity(
                label = label,
                timeHour = hour,
                timeMinute = minute,
                isEnabled = true,
                targetVolumePercent = volume,
                aiRecommendation = recommendation,
                daysOfWeek = days
            )
            val dao = database.privaDao()
            dao.insertAlarm(alarm)
        }
    }

    fun speakBriefing(text: String) {
        voiceSpeaker.speak(text)
    }

    fun stopSpeaking() {
        voiceSpeaker.stop()
    }

    fun sendTestNotificationTalk() {
        viewModelScope.launch {
            val unreadCalls = missedCalls.value.filter { !it.isResolved }.size
            val unreadMsgs = urgentMessages.value.filter { !it.isRead }.size
            val traffic = commuteTraffic.value
            val delayText = if (traffic != null && traffic.delayMinutes > 0) "+${traffic.delayMinutes}m delay" else "Clear"

            val title = "Hey, checking in with you"
            val shortText = "$unreadCalls calls pending, $unreadMsgs urgent msgs, traffic is $delayText."
            val expandedText = """
                Priva AI Check-in:
                • Missed calls needing callback: $unreadCalls
                • Urgent unread messages: $unreadMsgs
                • Commute from Home to Office: ${traffic?.currentMinutes ?: 28} mins ($delayText)
                • Costliest Sector Radar: Tech Hardware (+9.4%)
                • Device Alarm Volume: ${_deviceAlarmVolumePercent.value}%
                
                All your data remains private and local on this device.
            """.trimIndent()

            notificationManager.sendPeriodicBriefing(title, shortText, expandedText)
            repository.logNotification(title, shortText, "Check-in")
        }
    }

    fun triggerMissedCallAlert(call: MissedCallEntity) {
        viewModelScope.launch {
            notificationManager.sendMissedCallAlert(
                call.callerName,
                call.reasonSummary,
                call.phoneNumber
            )
            repository.logNotification(
                "Missed Call: ${call.callerName}",
                call.reasonSummary,
                "Missed Call"
            )
        }
    }

    fun triggerUrgentMessageAlert(message: UrgentMessageEntity) {
        viewModelScope.launch {
            notificationManager.sendUrgentMessageAlert(
                message.sender,
                message.summary,
                message.actionRequired
            )
            repository.logNotification(
                "Urgent from ${message.sender}",
                message.summary,
                "Urgent Message"
            )
        }
    }

    fun triggerAlarmAuditAndVolumeSync(alarm: AlarmEntity) {
        viewModelScope.launch {
            val target = alarm.targetVolumePercent
            setDeviceAlarmVolume(target)

            notificationManager.sendAlarmAuditAlert(
                alarm.label,
                target,
                alarm.aiRecommendation
            )
            repository.logNotification(
                "Alarm Volume Adjusted: ${alarm.label}",
                "Set to $target% (${alarm.aiRecommendation})",
                "Alarm"
            )
        }
    }

    fun triggerTrafficAlert() {
        viewModelScope.launch {
            val traffic = commuteTraffic.value ?: return@launch
            notificationManager.sendTrafficWarning(
                traffic.currentRouteName,
                traffic.delayMinutes,
                traffic.incidentAlert
            )
            repository.logNotification(
                "Traffic Warning (+${traffic.delayMinutes}m)",
                traffic.incidentAlert,
                "Commute"
            )
        }
    }

    fun triggerSectorInflationAlert(sector: SectorInflationEntity) {
        viewModelScope.launch {
            notificationManager.sendSectorInflationAlert(
                sector.sectorName,
                sector.projectedChangePercent,
                sector.savingsAdvice
            )
            repository.logNotification(
                "Sector Alert: ${sector.sectorName}",
                "+${sector.projectedChangePercent}% forecast. ${sector.savingsAdvice}",
                "Economic"
            )
        }
    }

    fun triggerMissingEmailAlert(email: EmailDigestEntity) {
        viewModelScope.launch {
            notificationManager.sendMissingEmailAlert(
                email.sender,
                email.missingDetail.ifBlank { email.aiSummary }
            )
            repository.logNotification(
                "Email Missing Item: ${email.sender}",
                email.missingDetail,
                "Email"
            )
        }
    }

    fun generateLiveExecutiveBriefing() {
        viewModelScope.launch {
            _isGeneratingAiBriefing.value = true
            val traffic = commuteTraffic.value
            val calls = missedCalls.value.filter { !it.isResolved }
            val msgs = urgentMessages.value.filter { !it.isRead }
            val missingEmails = emailDigests.value.filter { it.isMissingImportant && !it.isAddressed }
            val topSector = sectorInflation.value.firstOrNull()
            val events = calendarEvents.value
            val wakeUp = todayWakeUpLog.value
            val water = todayWaterMl.value
            val plans = dailyPlanItems.value

            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
            val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            val isNight = hour >= 21 || hour < 5
            val salutation = when (hour) {
                in 5..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                in 17..20 -> "Good evening"
                else -> "Good night"
            }
            val periodName = when (hour) {
                in 5..11 -> "morning"
                in 12..16 -> "afternoon"
                in 17..20 -> "evening"
                else -> "night"
            }

            val prompt = """
                You are J.A.R.V.I.S., Tony Stark's iconic, highly capable, polished and witty AI assistant (Crisp British accent, lively, composed, confident, addressing the user as 'Sir').
                Current period in India: $periodName ($salutation).
                Synthesize a lively, reassuring $periodName audio briefing for Sir based on these facts:
                1. Master ${if (isNight) "Night" else "Day"} Protocol: Status is active. Current Water Hydration: $water / 3000 ml. ${if (isNight) "Rest and sleep recovery schedule standing by." else "Today's Maharashtra Menu includes Kanda Poha breakfast, Jowar Bhakri & Varan Bhaat Thali for lunch, and Solkadhi."}
                2. Commute: ${traffic?.currentMinutes ?: 32} min from ${traffic?.homeAddress ?: "Arihant Aarohi"} to ${traffic?.officeAddress ?: "Seawoods Grand Central Mall, Navi Mumbai"} (${traffic?.distanceKm ?: 14.8} km, via ${traffic?.currentRouteName ?: "Sion-Panvel Hwy / Palm Beach Road"}).
                3. Interceptor Status: ${calls.size} intercepted missed calls, ${msgs.size} intercepted critical messages.
                4. Schedule: ${events.firstOrNull()?.eventTitle ?: "Work Shift at Seawoods Grand Central Mall"}. Wake-up: ${wakeUp?.wakeUpNote ?: "Auto-logged on unlock"}.
                5. Indian Markets: Gold 24K @ ₹76,850/10g, Nifty 50 @ 25,790 pts.
                Tone: Iconic JARVIS—crisp British, razor-sharp, energetic, respectful ('Sir'), slightly witty. Keep under 110 words.
            """.trimIndent()

            val result = aiService.generateBriefingResponse(prompt)
            val briefing = if (result.isSuccess) {
                result.getOrThrow()
            } else {
                """
                $salutation, Sir. Jarvis online and all systems nominal.
                • ${if (isNight) "Night Protocol: Standing by for overnight interceptor security and restful recovery. Hydration logged at ${water}ml." else "Day Protocol: Schedule is mapped out. Today's Maharashtra menu features Kanda Poha for breakfast and a wholesome Jowar Bhakri Thali with Solkadhi for lunch. Hydration is logged at ${water}ml."}
                • Transit Vector: Journey between ${traffic?.homeAddress ?: "Arihant Aarohi"} and ${traffic?.officeAddress ?: "Seawoods Grand Central Mall, Navi Mumbai"} is ${traffic?.currentMinutes ?: 32} minutes via ${traffic?.currentRouteName ?: "Palm Beach Road corridor"}.
                • Security Interceptor: Active. You have ${calls.size} intercepted missed calls and ${msgs.size} priority messages standing by. Gold holds steady at ₹76,850/10g.
                """.trimIndent()
            }

            _liveAiBriefingText.value = briefing
            _isGeneratingAiBriefing.value = false

            // Spoken briefing audio in JARVIS persona
            voiceSpeaker.speak(briefing)

            notificationManager.sendPeriodicBriefing(
                "J.A.R.V.I.S. Protocol Active, Sir",
                "Seawoods Commute ${traffic?.currentMinutes ?: 32}m • Water ${water}ml • Maharashtra Menu Ready",
                briefing
            )
            repository.logNotification("Executive Briefing", "Delivered daily audio summary to Sir", "Briefing")
        }
    }

    fun askPriva(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isAskingPriva.value = true

            val traffic = commuteTraffic.value
            val calls = missedCalls.value.filter { !it.isResolved }
            val msgs = urgentMessages.value.filter { !it.isRead }
            val missingEmails = emailDigests.value.filter { it.isMissingImportant && !it.isAddressed }
            val markets = marketItems.value
            val events = calendarEvents.value
            val wakeUp = todayWakeUpLog.value
            val health = smartWatchHealth.value
            val water = todayWaterMl.value
            val plans = dailyPlanItems.value
            val currentVol = _deviceAlarmVolumePercent.value

            val goldItem = markets.find { it.symbol == "GOLD24K" }
            val silverItem = markets.find { it.symbol == "SILVER999" }
            val niftyItem = markets.find { it.symbol == "NIFTY50" }

            val contextSummary = """
                J.A.R.V.I.S. System Context:
                - Master Plan: Hydration at ${water}ml / 3000ml. Maharashtra meal plan active (Breakfast: Kanda Poha, Lunch: Jowar Bhakri, Varan Bhaat, Solkadhi, Dinner: Moong Dal Khichdi).
                - Commute: Origin to ${traffic?.officeAddress ?: "Seawoods Grand Central Mall, Navi Mumbai"} is ${traffic?.distanceKm ?: 14.8} km, taking ${traffic?.currentMinutes ?: 32}m via ${traffic?.currentRouteName ?: "Palm Beach Road"}.
                - Calendar: ${events.size} scheduled events. Account: SanskarYadav640@gmail.com.
                - Wake-Up: ${wakeUp?.wakeUpNote ?: "Auto-set on phone unlock after 6 AM"}.
                - Wearable (Titan Smart World / iQOO z9s): ${health?.steps ?: 7420} steps, ${health?.heartRateBpm ?: 72} BPM, sleep score ${health?.sleepScore ?: 88}/100.
                - Intercepted Missed Calls (${calls.size}): ${calls.joinToString { "${it.callerName} (${it.reasonSummary})" }}.
                - Intercepted Messages (${msgs.size}): ${msgs.joinToString { "${it.sender}: ${it.summary}" }}.
                - Financial Radar: Gold 24K: ₹${goldItem?.priceInr ?: 76850}/10g, NIFTY 50: ${niftyItem?.priceInr ?: 25790.6} pts.
            """.trimIndent()

            val prompt = """
                You are J.A.R.V.I.S., Tony Stark's iconic, highly capable, polished British AI assistant (energetic, composed, sharp wit, polite, addressing the user as 'Sir').
                Answer Sir's question directly, clearly, and concisely (1-3 sentences) using their context.
                $contextSummary
                Sir's Question: "$query"
            """.trimIndent()

            val result = aiService.generateBriefingResponse(prompt)
            val answer = if (result.isSuccess) {
                result.getOrThrow()
            } else {
                when {
                    query.contains("water", ignoreCase = true) || query.contains("drink", ignoreCase = true) || query.contains("hydrate", ignoreCase = true) ->
                        "Sir, your hydration level is at ${water}ml towards your 3,000ml target. I suggest a 250ml glass right about now."
                    query.contains("food", ignoreCase = true) || query.contains("eat", ignoreCase = true) || query.contains("lunch", ignoreCase = true) || query.contains("dinner", ignoreCase = true) || query.contains("menu", ignoreCase = true) ->
                        "Sir, today's Maharashtra nutritional protocol includes hot Jowar Bhakri with Varan Bhaat and Solkadhi for lunch, followed by soothing Moong Dal Khichdi for dinner."
                    query.contains("destination", ignoreCase = true) || query.contains("seawoods", ignoreCase = true) || query.contains("work", ignoreCase = true) || query.contains("commute", ignoreCase = true) ->
                        "Sir, your workplace destination is locked on Seawoods Grand Central Mall, Navi Mumbai. Estimated travel time is ${traffic?.currentMinutes ?: 32} minutes via Palm Beach Road."
                    query.contains("plan", ignoreCase = true) || query.contains("day", ignoreCase = true) || query.contains("protocol", ignoreCase = true) ->
                        "Sir, the entire day is calibrated under J.A.R.V.I.S. protocol: awakening logged, hydration checkpoints set, Seawoods commute monitored, and Maharashtra meals scheduled."
                    query.contains("calendar", ignoreCase = true) || query.contains("schedule", ignoreCase = true) ->
                        "Sir, your Google Calendar for SanskarYadav640@gmail.com shows ${events.size} items today, beginning with ${events.firstOrNull()?.eventTitle ?: "Work Shift at Seawoods Grand Central Mall"}."
                    query.contains("year", ignoreCase = true) || query.contains("countdown", ignoreCase = true) -> {
                        val nowCal = java.util.Calendar.getInstance()
                        val yearEndCal = java.util.Calendar.getInstance().apply {
                            set(nowCal.get(java.util.Calendar.YEAR), java.util.Calendar.DECEMBER, 31, 23, 59, 59)
                        }
                        val diffDays = (yearEndCal.timeInMillis - nowCal.timeInMillis) / (24 * 3600 * 1000L)
                        "Sir, the countdown to year-end stands at $diffDays days. Q4 sprint targets remain on track."
                    }
                    query.contains("suit", ignoreCase = true) || query.contains("armor", ignoreCase = true) || query.contains("iron man", ignoreCase = true) ->
                        "Mark 85 armor diagnostics are green, Sir. Arc reactor at 100% capacity."
                    query.contains("call", ignoreCase = true) || query.contains("intercept", ignoreCase = true) ->
                        "Sir, the Interceptor has logged ${calls.size} missed calls and ${msgs.size} critical incoming transmissions for your immediate review."
                    else ->
                        "Sir, all systems operational under J.A.R.V.I.S. protocol. Seawoods commute is ${traffic?.currentMinutes ?: 32}m, hydration is ${water}ml, and Maharashtra meal schedule is on track."
                }
            }

            val newMsg = AskPrivaMessage(question = query, answer = answer)
            _askPrivaHistory.value = _askPrivaHistory.value + newMsg
            _isAskingPriva.value = false

            // Speak answer in character
            voiceSpeaker.speak(answer)
        }
    }

    fun testButlerVoicePhrase(phraseIndex: Int) {
        voiceSpeaker.testPhrase(phraseIndex)
    }

    fun syncDeviceSmsMessages(onResult: (Int) -> Unit) {
        viewModelScope.launch {
            _isSyncingSms.value = true
            try {
                val messages = smsHelper.getDeviceSmsMessages()
                if (messages.isNotEmpty()) {
                    repository.syncDeviceSmsMessages(messages)
                }
                onResult(messages.size)
            } finally {
                _isSyncingSms.value = false
            }
        }
    }

    fun clearSampleMessages() {
        viewModelScope.launch {
            repository.clearSampleMessages()
        }
    }

    fun refreshMarketPrices() {
        viewModelScope.launch {
            repository.refreshMarketPrices()
        }
    }

    fun addEmailDigest(
        sender: String,
        subject: String,
        snippet: String,
        category: String,
        summary: String,
        missingDetail: String
    ) {
        viewModelScope.launch {
            repository.addEmailDigest(sender, subject, snippet, category, summary, missingDetail)
        }
    }

    fun deleteEmailDigest(id: Long) {
        viewModelScope.launch {
            repository.deleteEmailDigest(id)
        }
    }

    fun getQuickRepliesForMessage(msg: UrgentMessageEntity): List<String> {
        return when {
            msg.rawSnippet.contains("failover", ignoreCase = true) || msg.rawSnippet.contains("database", ignoreCase = true) ->
                listOf("I reviewed and authorize the failover config.", "Looking at metrics now, will approve in 10 mins.", "Please hold off until I reach my workstation.")
            msg.rawSnippet.contains("water", ignoreCase = true) ->
                listOf("Thank you, noted for tomorrow morning.", "Will make sure water is stored in advance.", "Does this affect hot water as well?")
            msg.rawSnippet.contains("pharmacy", ignoreCase = true) || msg.rawSnippet.contains("prescription", ignoreCase = true) ->
                listOf("I will pick it up today after 4 PM.", "Please extend the hold for 24 hours.", "Can this be scheduled for home delivery?")
            else ->
                listOf("Received, reviewing immediately.", "Thanks for the update, will follow up soon.", "Got it, action item in progress.")
        }
    }

    fun simulateNewUrgentMessage(sender: String, messageText: String, channel: String = "SMS") {
        viewModelScope.launch {
            val summaryText = aiService.summarizeUrgentMessage(sender, messageText)
            val lines = summaryText.split("\n")
            val summary = lines.find { it.startsWith("Summary:") }?.removePrefix("Summary:")?.trim()
                ?: summaryText.take(80)
            val action = lines.find { it.startsWith("Action:") }?.removePrefix("Action:")?.trim()
                ?: "Requires prompt attention"

            repository.addUrgentMessage(
                sender = sender,
                channel = channel,
                snippet = messageText,
                summary = summary,
                urgency = "Critical",
                action = action
            )

            notificationManager.sendUrgentMessageAlert(sender, summary, action)
            repository.logNotification("Urgent from $sender", summary, "Urgent Message")
        }
    }

    fun simulateNewMissedCall(callerName: String, phone: String, reason: String) {
        viewModelScope.launch {
            repository.addMissedCall(callerName, phone, reason, "Urgent")
            notificationManager.sendMissedCallAlert(callerName, reason, phone)
            repository.logNotification("Missed Call: $callerName", reason, "Missed Call")
        }
    }

    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

    private val _isSyncingCalls = MutableStateFlow(false)
    val isSyncingCalls: StateFlow<Boolean> = _isSyncingCalls.asStateFlow()

    fun updateCommuteRoute(
        home: String,
        office: String,
        customRouteName: String? = null,
        customDistanceKm: Double? = null,
        customNormalMinutes: Int? = null,
        customDelayMinutes: Int? = null,
        customCondition: String? = null,
        customIncidentAlert: String? = null
    ) {
        viewModelScope.launch {
            repository.updateCommuteLocations(
                home = home,
                office = office,
                customRouteName = customRouteName,
                customDistanceKm = customDistanceKm,
                customNormalMinutes = customNormalMinutes,
                customDelayMinutes = customDelayMinutes,
                customCondition = customCondition,
                customIncidentAlert = customIncidentAlert
            )
        }
    }

    fun detectCurrentLocation(onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isDetectingLocation.value = true
            try {
                val detected = locationController.detectDetailedLocation()
                onResult(detected.fullAddress)
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }

    fun detectDetailedLocation(onResult: (com.example.service.DetectedLocation) -> Unit) {
        viewModelScope.launch {
            _isDetectingLocation.value = true
            try {
                val detected = locationController.detectDetailedLocation()
                onResult(detected)
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }

    fun deleteMissedCall(id: Long) {
        viewModelScope.launch {
            repository.deleteMissedCall(id)
        }
    }

    fun clearAllMissedCalls() {
        viewModelScope.launch {
            repository.clearAllMissedCalls()
        }
    }

    fun clearSampleMissedCalls() {
        viewModelScope.launch {
            repository.clearSampleMissedCalls()
        }
    }

    fun syncDeviceMissedCalls(clearSamples: Boolean = true, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            _isSyncingCalls.value = true
            try {
                val calls = callLogHelper.getDeviceMissedCalls()
                if (calls.isNotEmpty()) {
                    repository.syncDeviceCalls(calls, clearSamples = clearSamples)
                }
                onResult(calls.size)
            } finally {
                _isSyncingCalls.value = false
            }
        }
    }

    fun addNewMissedCall(callerName: String, phone: String, reason: String, urgency: String) {
        viewModelScope.launch {
            repository.addMissedCall(callerName, phone, reason, urgency)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearNotificationLogs()
        }
    }

    // Google Calendar Integration
    fun syncCalendar(onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            _isSyncingCalendar.value = true
            try {
                val events = calendarHelper.getCalendarEvents()
                if (events.isNotEmpty()) {
                    repository.syncCalendarEvents(events)
                }
                onResult(events.size)
            } finally {
                _isSyncingCalendar.value = false
            }
        }
    }

    fun addNewCalendarEvent(
        title: String,
        location: String,
        startEpoch: Long,
        endEpoch: Long,
        description: String,
        isPriority: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addCalendarEvent(
                title = title,
                location = location,
                startEpoch = startEpoch,
                endEpoch = endEpoch,
                description = description,
                accountEmail = "SanskarYadav640@gmail.com",
                isPriority = isPriority
            )
            // Also attempt to push to Android Google Calendar
            calendarHelper.insertCalendarEvent(title, location, startEpoch, endEpoch, description)
        }
    }

    fun deleteCalendarEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteCalendarEvent(id)
        }
    }

    fun openGoogleCalendar(epochTime: Long = System.currentTimeMillis()) {
        calendarHelper.launchGoogleCalendar(epochTime)
    }

    fun openAddCalendarEvent(
        title: String = "Work at Seawoods Grand Central Mall",
        location: String = "Seawoods Grand Central Mall, Navi Mumbai",
        startEpoch: Long = System.currentTimeMillis() + 3600000L
    ) {
        calendarHelper.launchAddCalendarEvent(title, location, startEpoch)
    }

    // Phone Unlock Logs (Auto-set wake-up time after 6:00 AM)
    fun recordPhoneUnlock(isSimulation: Boolean = false, onResult: (PhoneUnlockLogEntity) -> Unit = {}) {
        viewModelScope.launch {
            val entity = repository.recordPhoneUnlock(getApplication(), isSimulation)
            onResult(entity)
        }
    }

    fun simulateUnlockWakeUp(onResult: (PhoneUnlockLogEntity) -> Unit = {}) {
        recordPhoneUnlock(isSimulation = true, onResult = onResult)
    }

    fun clearUnlockLogs() {
        viewModelScope.launch {
            repository.clearUnlockLogs()
        }
    }

    // Titan Smart Watch & Health Connect Integration
    fun launchTitanSmartApp() {
        smartWatchHelper.launchTitanSmartWatchApp()
    }

    fun launchHealthConnect() {
        smartWatchHelper.launchHealthConnect()
    }

    fun refreshSmartWatchMetrics() {
        viewModelScope.launch {
            val updated = smartWatchHelper.getInitialSmartWatchHealth()
            repository.updateSmartWatchHealth(updated)
        }
    }

    // Gmail Integration
    fun launchGmail(toAddress: String? = null, subject: String? = null) {
        smartWatchHelper.launchGmail(toAddress, subject)
    }

    // Water Hydration Tracker & Reminders
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.logWaterIntake(amountMl, todayDateString)
            val current = todayWaterMl.value + amountMl
            if (current >= 3000) {
                jarvisNotificationService.triggerHydrationReminder(current, 3000)
            }
        }
    }

    fun resetWater() {
        viewModelScope.launch {
            repository.resetTodayWater(todayDateString)
        }
    }

    fun triggerHydrationReminder() {
        jarvisNotificationService.triggerHydrationReminder(todayWaterMl.value, 3000)
        if (_autoRunVoice.value) {
            voiceSpeaker.speak("Sir, hydration protocol checkpoint. Please consume water to maintain peak performance.")
        }
    }

    // JARVIS Daily Master Plan (Hydration & Maharashtra Meals)
    fun togglePlanItem(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleDailyPlanItem(id, completed)
        }
    }

    fun triggerMealReminder(item: DailyPlanItemEntity) {
        jarvisNotificationService.triggerMealReminder(item)
        if (_autoRunVoice.value) {
            voiceSpeaker.speak("Sir, your Maharashtra meal protocol reminder: ${item.title}. ${item.description}")
        }
    }

    // Interceptor Service: Missed Calls & Urgent Messages
    fun simulateInterceptMissedCall(
        callerName: String = "Dr. Sneha Kulkarni",
        phone: String = "+91 98201 44521",
        urgency: String = "Urgent",
        reason: String = "Callback requested regarding Navi Mumbai medical lab reports."
    ) {
        viewModelScope.launch {
            jarvisNotificationService.interceptMissedCall(
                callerName = callerName,
                phoneNumber = phone,
                urgency = urgency,
                reason = reason
            )
        }
    }

    fun simulateInterceptUrgentMessage(
        sender: String = "HDFC-BANK",
        messageText: String = "ALERT: High priority security notification for account. Immediate verification required.",
        urgency: String = "Critical",
        actionReq: String = "Verify transaction activity immediately."
    ) {
        viewModelScope.launch {
            jarvisNotificationService.interceptUrgentMessage(
                sender = sender,
                content = messageText,
                urgency = urgency,
                actionRequired = actionReq
            )
        }
    }

    // Car & Android Auto Controls
    fun connectToCar(
        carName: String = "Audi MMI Pro (Wireless Android Auto)",
        type: String = "Android Auto (Projection)"
    ) {
        carConnectionDetector.simulateConnectToCar(carName, type)
    }

    fun disconnectFromCar() {
        carConnectionDetector.simulateDisconnectFromCar()
    }

    fun speakCarGreeting() {
        carConnectionDetector.speakGreetingAgain()
    }

    fun selectSavedDestination(
        destination: SavedDestinationEntity,
        context: Context,
        onLaunched: () -> Unit = {}
    ) {
        // Voice greeting / route briefing
        val message = "Destination confirmed: ${destination.title}, Sir. Estimated commute is ${destination.estimatedMinutes} minutes across ${destination.distanceKm} kilometers. Have a safe drive."
        voiceSpeaker.speak(message)

        // Update active commute route
        val currentOrigin = commuteTraffic.value?.homeAddress ?: "Current Location"
        updateCommuteRoute(currentOrigin, destination.address)

        // Launch Turn-by-Turn navigation
        try {
            val uri = Uri.parse("google.navigation:q=${Uri.encode(destination.address)}")
            val navIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (navIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(navIntent)
            } else {
                val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(destination.address)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(genericIntent)
            }
        } catch (e: Exception) {
            Log.e("PrivaViewModel", "Could not launch Maps: ${e.message}")
        }
        onLaunched()
    }

    fun addSavedDestination(
        title: String,
        address: String,
        category: String,
        estimatedMinutes: Int,
        distanceKm: Double
    ) {
        viewModelScope.launch {
            repository.addSavedDestination(title, address, category, estimatedMinutes, distanceKm)
        }
    }

    fun deleteSavedDestination(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedDestination(id)
        }
    }

    fun playYouTubeMusicPlaylist(playlist: YouTubeMusicPlaylist, context: Context) {
        youtubeMusicController.selectPlaylist(playlist, andPlay = true)
    }

    fun toggleYouTubeMusicPlayPause(context: Context) {
        youtubeMusicController.togglePlayPause(context)
    }

    fun nextYouTubeMusicTrack() {
        youtubeMusicController.nextTrack()
    }

    fun prevYouTubeMusicTrack() {
        youtubeMusicController.prevTrack()
    }

    fun toggleAutoPlayOnCarConnect() {
        youtubeMusicController.toggleAutoPlayOnConnect()
    }

    fun clearCarConnectionLogs() {
        viewModelScope.launch {
            repository.clearCarLogs()
        }
    }

    // Real-Time Traffic API & Auto-Run Voice
    fun refreshTrafficWithApi(onComplete: (CommuteTrafficEntity) -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshingTraffic.value = true
            try {
                val currentHome = commuteTraffic.value?.homeAddress ?: "Palm Beach Road, Vashi, Navi Mumbai"
                val currentOffice = commuteTraffic.value?.officeAddress ?: "Seawoods Grand Central Mall, Navi Mumbai"
                val updated = repository.refreshRealtimeTrafficWithApi(currentHome, currentOffice, localNewsTrafficService)

                if (_autoRunVoice.value) {
                    val speech = "Sir, live traffic update between ${updated.homeAddress} and ${updated.officeAddress}. Current condition is ${updated.trafficCondition}. Estimated travel time is ${updated.currentMinutes} minutes across ${updated.distanceKm} kilometers. ${updated.incidentAlert}"
                    voiceSpeaker.speak(speech)
                }
                onComplete(updated)
            } catch (e: Exception) {
                Log.e("PrivaViewModel", "Failed to refresh traffic: ${e.message}")
            } finally {
                _isRefreshingTraffic.value = false
            }
        }
    }

    // Local News API & AI Summarization
    fun refreshLocalNewsWithApi(onComplete: (List<NewsItemEntity>) -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshingNews.value = true
            try {
                val updatedNews = repository.refreshLocalNewsWithApi(localNewsTrafficService)
                if (_autoRunVoice.value && updatedNews.isNotEmpty()) {
                    val topStory = updatedNews.first()
                    val speech = "Sir, your local news summary is refreshed. Top story: ${topStory.title}. ${topStory.aiTakeaway}"
                    voiceSpeaker.speak(speech)
                }
                onComplete(updatedNews)
            } catch (e: Exception) {
                Log.e("PrivaViewModel", "Failed to refresh local news: ${e.message}")
            } finally {
                _isRefreshingNews.value = false
            }
        }
    }

    fun toggleAutoRunVoice() {
        _autoRunVoice.value = !_autoRunVoice.value
        val state = if (_autoRunVoice.value) "Voice briefings auto-run enabled, Sir." else "Voice briefings auto-run paused."
        voiceSpeaker.speak(state)
    }

    /**
     * Master Synchronize Button: Syncs Calendar, Traffic, News, Market, and Health in one coordinated action.
     */
    fun syncEverything(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isMasterSyncing.value = true
            try {
                // 1. Sync Calendar
                try {
                    syncCalendar()
                } catch (ignored: Exception) {}

                // 2. Sync Traffic for Arihant Aarohi
                val current = commuteTraffic.value
                val home = current?.homeAddress ?: "Arihant Aarohi, Kalyan-Shilphata Road"
                val work = current?.officeAddress ?: "Seawoods Grand Central Mall, Navi Mumbai"
                val updatedTraffic = localNewsTrafficService.fetchRealtimeTraffic(home, work)
                updateCommuteRoute(
                    home = home,
                    office = work,
                    customRouteName = updatedTraffic.currentRouteName,
                    customCondition = updatedTraffic.trafficCondition,
                    customIncidentAlert = updatedTraffic.incidentAlert
                )

                // 3. Sync News API
                try {
                    repository.refreshLocalNewsWithApi(localNewsTrafficService)
                } catch (ignored: Exception) {}

                // 4. Sync Market Prices
                try {
                    repository.refreshMarketPrices()
                } catch (ignored: Exception) {}

                // 5. Sync Smartwatch & Health
                refreshSmartWatchMetrics()

                voiceSpeaker.speakInCharacter("Master synchronization complete, Sir. All systems, traffic from Arihant Aarohi, market telemetry, and executive schedule are synchronized.")
                onComplete()
            } catch (e: Exception) {
                Log.e("PrivaViewModel", "Master sync error: ${e.message}")
            } finally {
                _isMasterSyncing.value = false
            }
        }
    }

    fun searchAddressOnMap(query: String) {
        viewModelScope.launch {
            _isSearchingMap.value = true
            try {
                _mapSearchResults.value = localNewsTrafficService.searchAddressOnMap(query)
            } catch (e: Exception) {
                _mapSearchResults.value = emptyList()
            } finally {
                _isSearchingMap.value = false
            }
        }
    }

    fun selectMapAddressAsHome(result: com.example.network.MapSearchResult) {
        viewModelScope.launch {
            val currentWork = commuteTraffic.value?.officeAddress ?: "Seawoods Grand Central Mall, Navi Mumbai"
            updateCommuteRoute(result.address, currentWork)
            refreshTrafficWithApi()
            voiceSpeaker.speakInCharacter("Home address updated to ${result.name}, Sir. Recalculating transit telemetry.")
        }
    }

    fun selectMapAddressAsDestination(result: com.example.network.MapSearchResult) {
        viewModelScope.launch {
            val currentHome = commuteTraffic.value?.homeAddress ?: "Arihant Aarohi, Kalyan-Shilphata Road"
            updateCommuteRoute(currentHome, result.address)
            refreshTrafficWithApi()
            voiceSpeaker.speakInCharacter("Destination updated to ${result.name}, Sir. Live route telemetry synchronized.")
        }
    }

    fun handleVoiceCommand(command: String) {
        val lower = command.lowercase().trim()
        when {
            lower.contains("sync") || lower.contains("synchronize") -> {
                syncEverything()
            }
            lower.contains("traffic") || lower.contains("seawoods") || lower.contains("commute") -> {
                refreshTrafficWithApi { traffic ->
                    voiceSpeaker.speakInCharacter("Commute from Arihant Aarohi to ${traffic.officeAddress} is ${traffic.currentMinutes} minutes, ${traffic.trafficCondition}. ${traffic.incidentAlert}")
                }
            }
            lower.contains("gold") || lower.contains("silver") || lower.contains("market") -> {
                val gold = marketItems.value.find { it.symbol == "GOLD24K" }
                val nifty = marketItems.value.find { it.symbol == "NIFTY50" }
                voiceSpeaker.speakInCharacter("24 Karat Gold is trading at Rupees ${gold?.priceInr?.toInt() ?: 76850} per 10 grams, and Nifty 50 is at ${nifty?.priceInr?.toInt() ?: 25790} points, Sir.")
            }
            lower.contains("news") || lower.contains("headline") -> {
                val topStory = newsItems.value.firstOrNull()
                if (topStory != null) {
                    voiceSpeaker.speakInCharacter("Top headline, Sir: ${topStory.title}. Takeaway: ${topStory.aiTakeaway}")
                } else {
                    refreshLocalNewsWithApi { list ->
                        val s = list.firstOrNull()
                        if (s != null) voiceSpeaker.speakInCharacter("Top news: ${s.title}")
                    }
                }
            }
            else -> {
                askPriva(command)
            }
        }
    }
}

