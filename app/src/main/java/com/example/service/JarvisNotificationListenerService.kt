package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.data.local.AppDatabase
import com.example.data.local.entity.InterceptedNotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * J.A.R.V.I.S. Notification Interceptor Service
 *
 * Implements Android's official NotificationListenerService.
 * Capabilities:
 * 1. Intercepts all incoming notifications in real-time.
 * 2. Categorizes them based on priority:
 *    - URGENT: High priority (OTPs, banking transactions, emergency/critical messages, server outages).
 *    - REGULAR: Normal priority (direct chats, work emails, calendar, package transit tracking).
 *    - UNNECESSARY: Low priority marketing, discount codes, food delivery promos, spam clutter.
 * 3. Removes / cancels unnecessary notifications directly from the system notification bar (cancelNotification).
 * 4. Logs all decisions with AI reasoning into Room database audit trail.
 */
class JarvisNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "JarvisNotifListener"

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _lastInterceptedNotification = MutableStateFlow<InterceptedNotificationEntity?>(null)
        val lastInterceptedNotification: StateFlow<InterceptedNotificationEntity?> = _lastInterceptedNotification.asStateFlow()

        var onNotificationInterceptedListener: ((InterceptedNotificationEntity) -> Unit)? = null

        /**
         * Checks if user has granted Notification Listener permission in Android settings
         */
        fun isNotificationAccessGranted(context: Context): Boolean {
            val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
            if (enabledListeners.contains(context.packageName)) {
                return true
            }
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return flat != null && flat.contains(context.packageName)
        }

        /**
         * Intent to open Android's Notification Access Settings screen
         */
        fun getNotificationAccessSettingsIntent(): Intent {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            } else {
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }
        }

        /**
         * Analyzes notification content and determines priority, category, and removal action
         */
        fun classifyNotification(
            packageName: String,
            title: String,
            content: String,
            subText: String = ""
        ): ClassificationResult {
            val combined = "$title $content $subText".lowercase()
            val pkg = packageName.lowercase()

            // 1. Check for UNNECESSARY (Promotional clutter, marketing spam, food coupons)
            val isPromoOrSpam = hasUnnecessaryKeywords(combined, pkg)
            if (isPromoOrSpam) {
                val reason = when {
                    combined.contains("off") || combined.contains("sale") || combined.contains("discount") || combined.contains("coupon") || combined.contains("cashback") ->
                        "Auto-removed: promotional discount offer detected"
                    pkg.contains("zomato") || pkg.contains("swiggy") || pkg.contains("dominos") || combined.contains("craving") || combined.contains("biryani") || combined.contains("lunch") ->
                        "Auto-removed: food delivery marketing prompt dismissed"
                    pkg.contains("myntra") || pkg.contains("flipkart") || pkg.contains("meesho") || pkg.contains("amazon") || combined.contains("price drop") || combined.contains("cart") ->
                        "Auto-removed: e-commerce shopping deal dismissed"
                    combined.contains("spin") || combined.contains("coins") || combined.contains("claim") || combined.contains("bonus") ->
                        "Auto-removed: gaming promotional alert dismissed"
                    else -> "Auto-removed: marketing push notification dismissed"
                }

                return ClassificationResult(
                    category = "Promotional Clutter",
                    priority = "UNNECESSARY",
                    actionTaken = "REMOVED_FROM_SHADE",
                    shouldCancel = true,
                    reasoning = reason
                )
            }

            // 2. Check for URGENT (OTPs, banking alerts, emergency messages, server incidents)
            val isUrgent = hasUrgentKeywords(combined, pkg)
            if (isUrgent) {
                val category = when {
                    combined.contains("otp") || combined.contains("code is") || combined.contains("verification") || combined.contains("one time password") ->
                        "Financial & OTP"
                    combined.contains("debited") || combined.contains("credited") || combined.contains("bank") || combined.contains("fraud") || combined.contains("atm") || combined.contains("inr") || combined.contains("rs.") ->
                        "Financial Alert"
                    combined.contains("emergency") || combined.contains("hospital") || combined.contains("doctor") || combined.contains("ambulance") || combined.contains("asap") ->
                        "Emergency Protocol"
                    combined.contains("failover") || combined.contains("incident") || combined.contains("outage") || combined.contains("server down") || combined.contains("p0") || combined.contains("p1") ->
                        "Production Alert"
                    else -> "Urgent Directive"
                }

                return ClassificationResult(
                    category = category,
                    priority = "URGENT",
                    actionTaken = "PRIORITIZED",
                    shouldCancel = false,
                    reasoning = "Kept & prioritized: critical action required ($category)"
                )
            }

            // 3. Regular Updates (Personal messages, work emails, transport/delivery status)
            val regularCategory = when {
                pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("signal") || pkg.contains("messaging") ->
                    "Personal Chat"
                pkg.contains("gmail") || pkg.contains("outlook") || pkg.contains("slack") || pkg.contains("teams") ->
                    "Work Communication"
                combined.contains("arriving") || combined.contains("out for delivery") || combined.contains("driver") || combined.contains("tracking") ->
                    "Transit & Delivery"
                else -> "Routine Update"
            }

            return ClassificationResult(
                category = regularCategory,
                priority = "REGULAR",
                actionTaken = "SUMMARIZED",
                shouldCancel = false,
                reasoning = "Kept in shade: regular $regularCategory update"
            )
        }

        private fun hasUnnecessaryKeywords(combined: String, pkg: String): Boolean {
            val promoKeywords = listOf(
                "50% off", "40% off", "30% off", "20% off", "60% off", "70% off", "80% off",
                "% off", "flat ₹", "flat 50", "flat 100", "sale is live", "mega sale",
                "clearance sale", "discount coupon", "use code", "coupon code", "cashback offer",
                "cashback up to", "buy 1 get 1", "bogo", "limited time offer", "hurry, offer ends",
                "deal of the day", "don't miss out", "special offer for you", "exclusive deal",
                "craving a", "craving biryani", "your dinner is missing", "free delivery on order",
                "order now and save", "spin and win", "scratch & win", "claim your coins",
                "free bonus coins", "coins credited", "trending reels", "trending video",
                "people you may know", "suggested for you", "checkout your cart", "price drop alert",
                "items in your wishlist", "flash sale starts"
            )

            if (promoKeywords.any { combined.contains(it) }) return true

            // Known marketing spam triggers from delivery apps
            if (pkg.contains("zomato") || pkg.contains("swiggy") || pkg.contains("zepto") || pkg.contains("blinkit") || pkg.contains("instamart")) {
                if (combined.contains("hungry") || combined.contains("lunch") || combined.contains("dinner") || combined.contains("save") || combined.contains("order")) {
                    return true
                }
            }

            // Casual e-commerce marketing notifications
            if (pkg.contains("myntra") || pkg.contains("meesho") || pkg.contains("ajio") || pkg.contains("nykaa")) {
                if (combined.contains("sale") || combined.contains("offer") || combined.contains("look") || combined.contains("new collection")) {
                    return true
                }
            }

            return false
        }

        private fun hasUrgentKeywords(combined: String, pkg: String): Boolean {
            val urgentKeywords = listOf(
                "otp", "verification code", "one time password", "security alert",
                "debited from a/c", "credited to a/c", "bank account", "unauthorized transaction",
                "fraud detected", "card blocked", "emergency", "hospital", "doctor called",
                "ambulance", "asap", "call me back immediately", "server down", "production outage",
                "failover active", "incident #", "severity 1", "sev-1", "p0 incident",
                "flight delayed", "flight cancelled", "meeting started", "starts in 5 minutes"
            )

            if (urgentKeywords.any { combined.contains(it) }) return true

            // Financial apps are usually high priority unless explicit marketing
            if (pkg.contains("hdfc") || pkg.contains("sbi") || pkg.contains("icici") || pkg.contains("axis") || pkg.contains("paytm") || pkg.contains("phonepe") || pkg.contains("gpay")) {
                if (combined.contains("transfer") || combined.contains("paid") || combined.contains("received") || combined.contains("alert") || combined.contains("otp") || combined.contains("inr") || combined.contains("rs")) {
                    return true
                }
            }

            return false
        }

        fun extractFriendlyAppName(packageName: String): String {
            val lower = packageName.lowercase()
            return when {
                lower.contains("whatsapp") -> "WhatsApp"
                lower.contains("gm") || lower.contains("gmail") -> "Gmail"
                lower.contains("zomato") -> "Zomato"
                lower.contains("swiggy") -> "Swiggy"
                lower.contains("hdfc") -> "HDFC Bank"
                lower.contains("sbi") -> "State Bank of India"
                lower.contains("icici") -> "ICICI Bank"
                lower.contains("slack") -> "Slack"
                lower.contains("amazon") -> "Amazon"
                lower.contains("flipkart") -> "Flipkart"
                lower.contains("myntra") -> "Myntra"
                lower.contains("telegram") -> "Telegram"
                lower.contains("calendar") -> "Calendar"
                lower.contains("uber") || lower.contains("ola") -> "Ride Service"
                lower.contains("messaging") || lower.contains("mms") || lower.contains("sms") -> "Messages (SMS)"
                else -> packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isServiceConnected.value = true
        Log.i(TAG, "JarvisNotificationListenerService connected and monitoring system notifications")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isServiceConnected.value = false
        Log.i(TAG, "JarvisNotificationListenerService disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkgName = sbn.packageName ?: return

        // Skip intercepting our own app's notifications to prevent infinite loops
        if (pkgName == applicationContext.packageName) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""

        if (title.isBlank() && text.isBlank()) return

        val appName = extractFriendlyAppName(pkgName)
        val classification = classifyNotification(pkgName, title, text, subText)

        Log.i(TAG, "Intercepted notification from $appName: \"$title\" -> Priority: ${classification.priority}")

        // If classified as UNNECESSARY, REMOVE IT FROM NOTIFICATION SHADE!
        if (classification.shouldCancel) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cancelNotification(sbn.key)
                } else {
                    cancelNotification(sbn.packageName, sbn.tag, sbn.id)
                }
                Log.i(TAG, "Successfully removed unnecessary notification from shade: ${sbn.key}")
            } catch (e: Exception) {
                Log.w(TAG, "Could not cancel notification: ${e.message}")
            }
        }

        val entity = InterceptedNotificationEntity(
            notificationKey = sbn.key ?: "key_${System.currentTimeMillis()}",
            packageName = pkgName,
            appName = appName,
            title = title,
            content = text,
            subText = subText,
            category = classification.category,
            priority = classification.priority,
            actionTaken = classification.actionTaken,
            isRemoved = classification.shouldCancel,
            timestamp = System.currentTimeMillis(),
            aiReasoning = classification.reasoning
        )

        _lastInterceptedNotification.value = entity
        onNotificationInterceptedListener?.invoke(entity)

        // Persist to Room database
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                db.privaDao().insertInterceptedNotification(entity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist intercepted notification: ${e.message}")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Can be used to track user dismissals or external cancellations
    }

    data class ClassificationResult(
        val category: String,
        val priority: String, // "URGENT", "REGULAR", "UNNECESSARY"
        val actionTaken: String, // "PRIORITIZED", "SUMMARIZED", "REMOVED_FROM_SHADE"
        val shouldCancel: Boolean,
        val reasoning: String
    )
}
