package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.CommuteTrafficEntity
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.NewsItemEntity
import com.example.data.local.entity.NotificationLogEntity
import com.example.data.local.entity.PrivaSettingsEntity
import com.example.data.local.entity.SectorInflationEntity
import com.example.data.local.entity.UrgentMessageEntity
import com.example.data.repository.PrivaRepository
import com.example.network.PrivaAiService
import com.example.service.DeviceAudioController
import com.example.service.PrivaNotificationManager
import com.example.service.PrivaVoiceSpeaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val newsItems: StateFlow<List<NewsItemEntity>> = repository.newsItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sectorInflation: StateFlow<List<SectorInflationEntity>> = repository.sectorInflation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationLogs: StateFlow<List<NotificationLogEntity>> = repository.notificationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<PrivaSettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceSpeaker.shutdown()
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

            val prompt = """
                You are Priva, an elite private on-device executive AI assistant.
                Synthesize a concise, reassuring 3-bullet personalized morning audio briefing for the user based strictly on these facts:
                1. Commute: ${traffic?.currentMinutes} min to office (${traffic?.trafficCondition}, ${traffic?.incidentAlert}).
                2. Urgent missed calls: ${calls.size} (${calls.firstOrNull()?.callerName}: ${calls.firstOrNull()?.reasonSummary}).
                3. Critical messages: ${msgs.size} (${msgs.firstOrNull()?.summary}).
                4. Unresolved missing emails: ${missingEmails.size} (${missingEmails.firstOrNull()?.missingDetail}).
                5. Costliest sector in next 6 months: ${topSector?.sectorName} (+${topSector?.projectedChangePercent}%).
                Tone: Intelligent, calm, protective, strictly professional. Keep it under 100 words.
            """.trimIndent()

            val result = aiService.generateBriefingResponse(prompt)
            val briefing = if (result.isSuccess) {
                result.getOrThrow()
            } else {
                """
                • Commute Alert: ${traffic?.currentMinutes ?: 44} mins to office via ${traffic?.currentRouteName ?: "Expressway"}. Incident near Exit 14 adds +16m; consider leaving 15 min early.
                • High Priority: Callback needed for ${calls.firstOrNull()?.callerName ?: "Dr. Patel"} regarding blood panel results; authorization needed on database failover from ${msgs.firstOrNull()?.sender ?: "Sarah Vance"}.
                • Radar Watch: ${topSector?.sectorName ?: "Tech Hardware"} is projected +9.4% costlier over the next 6 months. Review cloud & hardware renewals now.
                """.trimIndent()
            }

            _liveAiBriefingText.value = briefing
            _isGeneratingAiBriefing.value = false

            // Spoken briefing audio
            voiceSpeaker.speak(briefing)

            notificationManager.sendPeriodicBriefing(
                "Your Executive Briefing is Ready",
                "Traffic +16m delay • 2 critical callbacks • Tech inflation radar",
                briefing
            )
            repository.logNotification("Executive Briefing", "Delivered daily audio summary", "Briefing")
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
            val sectors = sectorInflation.value
            val currentVol = _deviceAlarmVolumePercent.value

            val contextSummary = """
                Local Database Context:
                - Commute: Home to Office is ${traffic?.currentMinutes ?: 44}m (normal ${traffic?.normalMinutes ?: 28}m), route: ${traffic?.currentRouteName}, alert: ${traffic?.incidentAlert}.
                - Unresolved Missed Calls (${calls.size}): ${calls.joinToString { "${it.callerName} (${it.reasonSummary})" }}.
                - Unread Urgent Messages (${msgs.size}): ${msgs.joinToString { "${it.sender}: ${it.summary} [Action: ${it.actionRequired}]" }}.
                - Missing Important Emails: ${missingEmails.joinToString { "${it.sender}: ${it.missingDetail}" }}.
                - Alarm volume: $currentVol%.
                - 6-Month Costliest Sectors: ${sectors.take(3).joinToString { "${it.sectorName} (+${it.projectedChangePercent}%)" }}.
            """.trimIndent()

            val prompt = """
                You are Priva, an intelligent private on-device assistant.
                Answer the user's question directly, clearly, and concisely (1-3 sentences) using their local context if applicable.
                $contextSummary
                User Question: "$query"
            """.trimIndent()

            val result = aiService.generateBriefingResponse(prompt)
            val answer = if (result.isSuccess) {
                result.getOrThrow()
            } else {
                when {
                    query.contains("traffic", ignoreCase = true) || query.contains("commute", ignoreCase = true) || query.contains("office", ignoreCase = true) ->
                        "Commute takes ${traffic?.currentMinutes ?: 44} mins via ${traffic?.currentRouteName ?: "I-280 North"} due to a +${traffic?.delayMinutes ?: 16}m delay (${traffic?.incidentAlert ?: "accident near Exit 14"})."
                    query.contains("call", ignoreCase = true) ->
                        "You have ${calls.size} urgent missed calls. Most critical is ${calls.firstOrNull()?.callerName ?: "Dr. Patel"} regarding ${calls.firstOrNull()?.reasonSummary ?: "blood test results"}."
                    query.contains("alarm", ignoreCase = true) || query.contains("volume", ignoreCase = true) ->
                        "Your current alarm stream volume is $currentVol%. We recommend 85% to ensure you don't sleep through the morning commute delay."
                    query.contains("costlier", ignoreCase = true) || query.contains("sector", ignoreCase = true) || query.contains("inflation", ignoreCase = true) ->
                        "Tech Hardware & Cloud (+9.4%) and Auto Insurance (+8.1%) are projected to be the costliest sectors over the next 6 months."
                    query.contains("email", ignoreCase = true) || query.contains("mail", ignoreCase = true) || query.contains("missing", ignoreCase = true) ->
                        "You are missing signed Exhibit B (Security Addendum) for the partner MSA, and an unpaid cloud server invoice needs payment verification."
                    else ->
                        "Priva local status: ${calls.size} missed calls pending, ${msgs.size} urgent messages, and your morning commute has a +${traffic?.delayMinutes ?: 16}m delay. All private on your device."
                }
            }

            val newMsg = AskPrivaMessage(question = query, answer = answer)
            _askPrivaHistory.value = _askPrivaHistory.value + newMsg
            _isAskingPriva.value = false

            // Speak answer
            voiceSpeaker.speak(answer)
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

    fun updateCommuteRoute(home: String, office: String) {
        viewModelScope.launch {
            val current = commuteTraffic.value ?: return@launch
            val updated = current.copy(
                homeAddress = home,
                officeAddress = office,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateCommute(updated)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearNotificationLogs()
        }
    }
}
