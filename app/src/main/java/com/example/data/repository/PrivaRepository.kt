package com.example.data.repository

import com.example.data.local.dao.PrivaDao
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.CommuteTrafficEntity
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.NewsItemEntity
import com.example.data.local.entity.NotificationLogEntity
import com.example.data.local.entity.PrivaSettingsEntity
import com.example.data.local.entity.SectorInflationEntity
import com.example.data.local.entity.UrgentMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PrivaRepository(private val dao: PrivaDao) {

    val missedCalls: Flow<List<MissedCallEntity>> = dao.getAllMissedCalls()
    val alarms: Flow<List<AlarmEntity>> = dao.getAllAlarms()
    val urgentMessages: Flow<List<UrgentMessageEntity>> = dao.getAllUrgentMessages()
    val emailDigests: Flow<List<EmailDigestEntity>> = dao.getAllEmailDigests()
    val commuteTraffic: Flow<CommuteTrafficEntity?> = dao.getCommuteTraffic()
    val newsItems: Flow<List<NewsItemEntity>> = dao.getAllNews()
    val sectorInflation: Flow<List<SectorInflationEntity>> = dao.getAllSectors()
    val notificationLogs: Flow<List<NotificationLogEntity>> = dao.getAllLogs()
    val settings: Flow<PrivaSettingsEntity?> = dao.getSettings()

    suspend fun initializeDefaultDataIfEmpty() {
        val count = dao.getMissedCallsCount()
        if (count > 0) return

        val now = System.currentTimeMillis()

        // 1. Initial Missed Calls
        val defaultCalls = listOf(
            MissedCallEntity(
                callerName = "Dr. Elena Patel",
                phoneNumber = "+1 (555) 349-8821",
                timestamp = now - (42 * 60 * 1000), // 42 min ago
                urgency = "Urgent",
                reasonSummary = "Callback needed regarding urgent metabolic panel blood test results and medication adjustment.",
                isResolved = false
            ),
            MissedCallEntity(
                callerName = "Apex Capital Fraud Alert",
                phoneNumber = "+1 (800) 555-0199",
                timestamp = now - (2 * 3600 * 1000), // 2 hrs ago
                urgency = "Urgent",
                reasonSummary = "Automated fraud security check for pending $640 online transaction.",
                isResolved = false
            ),
            MissedCallEntity(
                callerName = "Marcus Johnson (Client)",
                phoneNumber = "+1 (555) 782-9904",
                timestamp = now - (5 * 3600 * 1000), // 5 hrs ago
                urgency = "Medium",
                reasonSummary = "Inquiry on Q3 roadmap deliverable milestones prior to board meeting.",
                isResolved = false
            )
        )
        dao.insertMissedCalls(defaultCalls)

        // 2. Initial Alarms
        val defaultAlarms = listOf(
            AlarmEntity(
                label = "Morning Work Commute",
                timeHour = 7,
                timeMinute = 0,
                isEnabled = true,
                targetVolumePercent = 85,
                aiRecommendation = "Volume boost to 85% recommended: 16 min traffic delay and heavy rain detected along I-280 North.",
                daysOfWeek = "Mon, Tue, Wed, Thu, Fri"
            ),
            AlarmEntity(
                label = "Gym & Morning Routine",
                timeHour = 6,
                timeMinute = 15,
                isEnabled = false,
                targetVolumePercent = 60,
                aiRecommendation = "Optimal volume 60% for gradual wakefulness.",
                daysOfWeek = "Mon, Wed, Fri"
            ),
            AlarmEntity(
                label = "Important Medication Reminder",
                timeHour = 21,
                timeMinute = 30,
                isEnabled = true,
                targetVolumePercent = 75,
                aiRecommendation = "Scheduled evening priority alert.",
                daysOfWeek = "Everyday"
            )
        )
        dao.insertAlarms(defaultAlarms)

        // 3. Initial Urgent Messages
        val defaultMessages = listOf(
            UrgentMessageEntity(
                sender = "Sarah Vance (Engineering Lead)",
                channel = "Slack",
                rawSnippet = "Hey, we noticed an unexpected spike in database query latency on cluster 3. Can you please review and authorize the failover config before 2:00 PM?",
                summary = "Database cluster 3 latency spike detected. Failover config requires your review and approval.",
                urgencyLevel = "Critical",
                actionRequired = "Review and authorize failover before 2:00 PM",
                timestamp = now - (15 * 60 * 1000),
                isRead = false
            ),
            UrgentMessageEntity(
                sender = "Highland Property Management",
                channel = "SMS",
                rawSnippet = "Urgent Notice: Municipal water main valve inspection scheduled tomorrow between 9 AM - 1 PM. Water service will be paused.",
                summary = "Water service shutoff tomorrow 9:00 AM - 1:00 PM for building valve maintenance.",
                urgencyLevel = "High",
                actionRequired = "Store water before 9:00 AM tomorrow",
                timestamp = now - (90 * 60 * 1000),
                isRead = false
            ),
            UrgentMessageEntity(
                sender = "CVS Caremark Pharmacy",
                channel = "SMS",
                rawSnippet = "Rx #994821 is prepared and waiting at 4th St location. Will be returned to stock if not retrieved in 48 hours.",
                summary = "Prescription ready for pickup at 4th St location. Must pick up within 48h.",
                urgencyLevel = "Normal",
                actionRequired = "Pick up prescription before Friday evening",
                timestamp = now - (3 * 3600 * 1000),
                isRead = true
            )
        )
        dao.insertUrgentMessages(defaultMessages)

        // 4. Initial Email Digests (with important missing item highlights)
        val defaultEmails = listOf(
            EmailDigestEntity(
                sender = "billing@cloudservices.com",
                subject = "URGENT: Outstanding Invoice #INV-8820 requires payment verification",
                snippet = "We attempted to process your auto-charge for cloud servers but the transaction was declined. Update card to prevent service disruption.",
                category = "Urgent Action",
                aiSummary = "Cloud server auto-charge failed. Action needed to prevent cluster disruption.",
                isMissingImportant = true,
                missingDetail = "Updated corporate payment method has NOT been provided; service pause in 24h.",
                receivedTime = now - (35 * 60 * 1000),
                isAddressed = false
            ),
            EmailDigestEntity(
                sender = "legal@partnergroup.io",
                subject = "Countersigned Master Services Agreement - Missing Exhibit B",
                snippet = "Attached is the executed agreement. Notice that Exhibit B (Security Addendum) was missing from your initial signed packet.",
                category = "Important Missing",
                aiSummary = "Executed MSA received, but partner noted Exhibit B security addendum is missing.",
                isMissingImportant = true,
                missingDetail = "You are missing signed Exhibit B (Security Addendum) to finalize the contract.",
                receivedTime = now - (3 * 3600 * 1000),
                isAddressed = false
            ),
            EmailDigestEntity(
                sender = "Delta Airlines Reservations",
                subject = "Flight DL 1492 Departure Gate & Schedule Revision",
                snippet = "Your flight to Seattle tomorrow has been rescheduled 20 minutes earlier due to airspace congestion. Gate changed to B18.",
                category = "Travel Alert",
                aiSummary = "Departure moved 20 min earlier to 8:15 AM; gate moved to B18.",
                isMissingImportant = false,
                missingDetail = "",
                receivedTime = now - (6 * 3600 * 1000),
                isAddressed = true
            )
        )
        dao.insertEmailDigests(defaultEmails)

        // 5. Initial Commute & Traffic
        val defaultTraffic = CommuteTrafficEntity(
            id = 1,
            homeAddress = "742 Evergreen Crest, West Hills",
            officeAddress = "100 Silicon Way, Tech Campus",
            currentRouteName = "I-280 North via Expressway",
            normalMinutes = 28,
            currentMinutes = 44,
            delayMinutes = 16,
            trafficCondition = "Heavy Delay",
            incidentAlert = "Multi-vehicle collision blocking right 2 lanes near Exit 14. Recommend taking Skyline Blvd detour.",
            lastUpdated = now
        )
        dao.insertOrUpdateTraffic(defaultTraffic)

        // 6. News Items (Local City & Local AI)
        val defaultNews = listOf(
            NewsItemEntity(
                title = "Local Edge AI: City Tech Hub deploys private on-device intelligence for municipal sensors",
                category = "Local AI",
                source = "Metro Tech Tribune",
                summary = "The regional research center in partnership with university engineers deployed an edge AI model running purely locally with zero cloud streaming.",
                aiTakeaway = "Direct alignment with Priva's on-device privacy architecture; local tech ecosystem shifting toward local models.",
                timestamp = now - (2 * 3600 * 1000)
            ),
            NewsItemEntity(
                title = "Local AI Developer Summit announced at Innovation Center this Saturday",
                category = "Local AI",
                source = "Silicon Valley Newsbeat",
                summary = "Workshops spotlight open weights LLMs, privacy-first mobile assistants, and local NPU hardware acceleration.",
                aiTakeaway = "Key topics include quantized mobile inference and secure biometric encryption.",
                timestamp = now - (5 * 3600 * 1000)
            ),
            NewsItemEntity(
                title = "West Hills Light Rail Express Extension approved for 2027 operations",
                category = "Local City",
                source = "Bay Regional Dispatch",
                summary = "New rapid transit line will connect residential West Hills directly to the Silicon Way tech corridor in under 20 minutes.",
                aiTakeaway = "Will reduce future office commute times by ~35% once operational.",
                timestamp = now - (8 * 3600 * 1000)
            ),
            NewsItemEntity(
                title = "County Water District announces seasonal infrastructure maintenance schedule",
                category = "Local City",
                source = "City Gazette",
                summary = "Brief intermittent pressure drops expected across sector 4 during overnight hours next Tuesday.",
                aiTakeaway = "No expected impact on daytime commute or office hours.",
                timestamp = now - (12 * 3600 * 1000)
            )
        )
        dao.insertNews(defaultNews)

        // 7. Sector Inflation & Cost Forecasts (Which sector gonna be costlier in next 6 months)
        val defaultSectors = listOf(
            SectorInflationEntity(
                sectorName = "Tech Hardware & Cloud Compute",
                projectedChangePercent = 9.4,
                isCostlier = true,
                timeHorizon = "Next 6 Months (Q4 2026 - Q1 2027)",
                primaryDrivers = "TSMC 2nm/3nm wafer allocation surcharges, high-bandwidth memory (HBM) shortages, and rising data center power tariffs.",
                savingsAdvice = "Lock in annual cloud contracts now; purchase required laptop/storage hardware before seasonal price adjustments."
            ),
            SectorInflationEntity(
                sectorName = "Auto Insurance & Vehicle Repairs",
                projectedChangePercent = 8.1,
                isCostlier = true,
                timeHorizon = "Next 6 Months (Q4 2026 - Q1 2027)",
                primaryDrivers = "Advanced driver assistance sensors (ADAS) calibration expenses and state insurance commissioner rate hike approvals.",
                savingsAdvice = "Request telematics safe-driver re-rate, increase comprehensive deductible slightly, and bundle home/renters."
            ),
            SectorInflationEntity(
                sectorName = "Urban Housing & Apartment Rents",
                projectedChangePercent = 6.5,
                isCostlier = true,
                timeHorizon = "Next 6 Months (Q4 2026 - Q1 2027)",
                primaryDrivers = "Persistent low metro inventory, high mortgage rates dampening new home buyers, and delayed multi-family completions.",
                savingsAdvice = "Negotiate lease extension 60-90 days prior to expiration or lock in a 15-to-18-month term to skip summer peaks."
            ),
            SectorInflationEntity(
                sectorName = "Winter Electricity & Grid Utilities",
                projectedChangePercent = 5.8,
                isCostlier = true,
                timeHorizon = "Next 6 Months (Q4 2026 - Q1 2027)",
                primaryDrivers = "Peak winter grid transition fees, regional natural gas distribution updates, and transmission grid upgrade levies.",
                savingsAdvice = "Utilize smart thermostat scheduling during off-peak hours and inspect door weatherstripping."
            ),
            SectorInflationEntity(
                sectorName = "Healthcare & Prescription Copays",
                projectedChangePercent = 4.3,
                isCostlier = true,
                timeHorizon = "Next 6 Months (Q4 2026 - Q1 2027)",
                primaryDrivers = "Annual insurance benefit renewals, specialty biologic tier reclassifications, and hospital labor costs.",
                savingsAdvice = "Check generic bio-equivalent alternatives on GoodRx or manufacturer copay savings cards."
            ),
            SectorInflationEntity(
                sectorName = "Food & Packaged Groceries",
                projectedChangePercent = 3.6,
                isCostlier = true,
                timeHorizon = "Next 6 Months (Q4 2026 - Q1 2027)",
                primaryDrivers = "Selective agricultural input costs (cocoa, dairy) while staple grains remain stabilized.",
                savingsAdvice = "Purchase staple pantry items in bulk when featured on weekly promotional rotations."
            )
        )
        dao.insertSectors(defaultSectors)

        // 8. Initial Notification Logs
        val defaultLogs = listOf(
            NotificationLogEntity(
                title = "Commute Alert: Heavy Traffic",
                message = "I-280 North accident +16m delay. Recommend adjusting alarm volume to 85%.",
                category = "Commute",
                timestamp = now - (30 * 60 * 1000)
            ),
            NotificationLogEntity(
                title = "Missed Call from Dr. Elena Patel",
                message = "Callback requested regarding blood test results. Marked as Urgent.",
                category = "Missed Call",
                timestamp = now - (42 * 60 * 1000)
            ),
            NotificationLogEntity(
                title = "Urgent Email Radar",
                message = "Partner MSA missing Exhibit B Security Addendum. Action required.",
                category = "Email",
                timestamp = now - (3 * 3600 * 1000)
            )
        )
        for (log in defaultLogs) {
            dao.insertLog(log)
        }

        // 9. Initial Settings
        val defaultSettings = PrivaSettingsEntity(
            id = 1,
            notificationFrequencyHours = 2,
            isUrgentOnly = false,
            enableAlarmVolumeAutoAdjust = true,
            enableMissedCallAlerts = true,
            enableTrafficBriefing = true,
            enableEmailAudit = true,
            privateOnDeviceOnly = true,
            homeLocation = "742 Evergreen Crest, West Hills",
            officeLocation = "100 Silicon Way, Tech Campus",
            lastBriefingTime = now
        )
        dao.insertOrUpdateSettings(defaultSettings)
    }

    suspend fun resolveMissedCall(id: Long, resolved: Boolean) {
        dao.setMissedCallResolved(id, resolved)
    }

    suspend fun updateAlarmVolume(id: Long, volume: Int) {
        dao.updateAlarmVolume(id, volume)
    }

    suspend fun toggleAlarm(id: Long, isEnabled: Boolean) {
        dao.toggleAlarm(id, isEnabled)
    }

    suspend fun markMessageRead(id: Long, isRead: Boolean) {
        dao.markMessageRead(id, isRead)
    }

    suspend fun markEmailAddressed(id: Long, isAddressed: Boolean) {
        dao.markEmailAddressed(id, isAddressed)
    }

    suspend fun addMissedCall(callerName: String, phone: String, reason: String, urgency: String) {
        val call = MissedCallEntity(
            callerName = callerName,
            phoneNumber = phone,
            timestamp = System.currentTimeMillis(),
            urgency = urgency,
            reasonSummary = reason,
            isResolved = false
        )
        dao.insertMissedCall(call)
    }

    suspend fun addUrgentMessage(sender: String, channel: String, snippet: String, summary: String, urgency: String, action: String) {
        val msg = UrgentMessageEntity(
            sender = sender,
            channel = channel,
            rawSnippet = snippet,
            summary = summary,
            urgencyLevel = urgency,
            actionRequired = action,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        dao.insertUrgentMessage(msg)
    }

    suspend fun logNotification(title: String, message: String, category: String) {
        val log = NotificationLogEntity(
            title = title,
            message = message,
            category = category,
            timestamp = System.currentTimeMillis()
        )
        dao.insertLog(log)
    }

    suspend fun clearNotificationLogs() {
        dao.clearLogs()
    }

    suspend fun updateSettings(settings: PrivaSettingsEntity) {
        dao.insertOrUpdateSettings(settings)
    }

    suspend fun updateCommute(traffic: CommuteTrafficEntity) {
        dao.insertOrUpdateTraffic(traffic)
    }
}
