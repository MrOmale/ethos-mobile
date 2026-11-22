package com.example.security

import org.junit.Test
import org.junit.Before
import java.time.Instant

class AppLifecycleObserverTest {
  private lateinit var observer: AppLifecycleObserver
  private val sessionTimeoutMs = 5 * 60 * 1000  // 5 minutes

  @Before
  fun setup() {
    observer = AppLifecycleObserver(timeoutMs = sessionTimeoutMs)
  }

  @Test
  fun testBackgroundingRecordsTimestamp() {
    // FIX: Test that backgrounding records timestamp
    val before = System.currentTimeMillis()
    observer.onAppBackgrounded()
    val after = System.currentTimeMillis()

    val recorded = observer.getBackgroundTime()
    assert(recorded >= before && recorded <= after)
  }

  @Test
  fun testForegroundingPastTimeoutLocksSession() {
    // FIX: Test that foregrounding past timeout locks session
    observer.onAppBackgrounded()

    // Simulate time passing
    Thread.sleep(sessionTimeoutMs + 1000)

    observer.onAppForegrounded()
    assert(observer.isSessionLocked())
  }

  @Test
  fun testForegroundingWithinTimeoutDoesNotLock() {
    // FIX: Test session not locked if comeback is fast
    observer.onAppBackgrounded()
    Thread.sleep(100)  // Short time
    observer.onAppForegrounded()
    assert(!observer.isSessionLocked())
  }
}
