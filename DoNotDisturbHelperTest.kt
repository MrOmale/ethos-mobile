package com.example.services

import android.app.NotificationManager
import org.junit.Test
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import com.example.services.DoNotDisturbHelper

class DoNotDisturbHelperTest {
  @Mock
  private lateinit var notificationManager: NotificationManager

  private lateinit var helper: DoNotDisturbHelper

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    helper = DoNotDisturbHelper(notificationManager)
  }

  @Test
  fun testAllowedState() {
    assert(helper.mayPostReminder(NotificationManager.INTERRUPTION_FILTER_ALL))
  }

  @Test
  fun testBlockedState() {
    assert(!helper.mayPostReminder(NotificationManager.INTERRUPTION_FILTER_NONE))
  }

  @Test
  fun testUnknownState() {
    val result = helper.mayPostReminder(999)
    assert(result == false)
  }
}
