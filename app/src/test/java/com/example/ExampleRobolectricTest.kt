package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JARVIS", appName)
  }

  @Test
  fun `verify context availability`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertNotNull(context)
  }

  @Test
  fun `verify default calendar events and account`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val calendarHelper = com.example.service.DeviceCalendarHelper(context)
    val events = calendarHelper.getCalendarEvents()
    assertNotNull(events)
    assertEquals(true, events.isNotEmpty())
    assertEquals("SanskarYadav640@gmail.com", events[0].accountEmail)
    assertEquals(true, events.any { it.location.contains("Seawoods Grand Central", ignoreCase = true) })
  }

  @Test
  fun `verify smartwatch and health helper initialization`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val helper = com.example.service.SmartWatchAndHealthHelper(context)
    val health = helper.getInitialSmartWatchHealth()
    assertNotNull(health)
    assertEquals("Titan Smart World Watch", health.watchModel)
    assertEquals("iQOO z9s (Health Connect Synced)", health.devicePhone)
    assertEquals(10000, health.dailyStepGoal)
    helper.unregister()
  }

  @Test
  fun `verify simulated phone unlock handles wake up after 6 am`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val unlockLog = com.example.service.PhoneUnlockReceiver.handleUnlockEvent(context, isSimulation = true)
    assertNotNull(unlockLog)
    assertEquals(true, unlockLog.isWakeUpLog)
    assertEquals(true, unlockLog.wakeUpNote.contains("wake up", ignoreCase = true))
  }

  @Test
  fun `verify car greeting format and time salutation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val speaker = com.example.service.PrivaVoiceSpeaker(context)
    val youtube = com.example.service.YouTubeMusicController(context)
    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
    val detector = com.example.service.CarConnectionDetector(context, speaker, youtube, scope) { _, _, _, _ -> }
    val greeting = detector.getCarGreeting()
    assertNotNull(greeting)
    assertEquals(true, greeting.startsWith("Hello Sir!"))
    assertEquals(true, greeting.endsWith("Please tell me what is your destination?"))
    assertEquals(true, greeting.contains("Good morning") || greeting.contains("Good afternoon") || greeting.contains("Good evening") || greeting.contains("Good night"))
    speaker.shutdown()
  }

  @Test
  fun `verify voice speaker parameters and name sanitization`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val speaker = com.example.service.PrivaVoiceSpeaker(context)
    assertEquals(0.98f, speaker.speechPitch, 0.02f)
    assertEquals(1.0f, speaker.speechRate, 0.02f)
    val sanitized = speaker.sanitizeForSpeech("J.A.R.V.I.S. online, gold is ₹76000 and distance is 24 km")
    assertEquals(true, sanitized.contains("Jarvis"))
    assertEquals(false, sanitized.contains("J.A.R.V.I.S."))
    assertEquals(true, sanitized.contains("Rupees"))
    assertEquals(true, sanitized.contains("kilometers"))
    speaker.shutdown()
  }

  @Test
  fun `verify notification interceptor prioritizes urgent and removes promotional spam`() {
    val promo = com.example.service.JarvisNotificationListenerService.classifyNotification(
      packageName = "com.application.zomato",
      title = "Craving Biryani?",
      content = "Get 60% OFF up to ₹120 with coupon JUMBO"
    )
    assertEquals("UNNECESSARY", promo.priority)
    assertEquals(true, promo.shouldCancel)
    assertEquals("REMOVED_FROM_SHADE", promo.actionTaken)

    val urgent = com.example.service.JarvisNotificationListenerService.classifyNotification(
      packageName = "com.google.android.apps.messaging",
      title = "HDFC Bank Alert",
      content = "Your OTP is 482910 for transaction of INR 4,500. Do not share with anyone."
    )
    assertEquals("URGENT", urgent.priority)
    assertEquals(false, urgent.shouldCancel)
    assertEquals("PRIORITIZED", urgent.actionTaken)
  }

  @Test
  fun `verify location fallback defaults to Arihant Aarohi`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val controller = com.example.service.DeviceLocationController(context)
    val location = controller.detectDetailedLocation()
    assertNotNull(location)
    assertEquals(true, location.fullAddress.contains("Arihant Aarohi", ignoreCase = true))
  }
}
