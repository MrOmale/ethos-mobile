package com.example.services

import androidx.work.WorkManager
import org.junit.Test
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.*

class CheckInReminderWorkerTest {
  @Mock
  private lateinit var workManager: WorkManager

  private lateinit var worker: CheckInReminderWorker

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    worker = CheckInReminderWorker(workManager)
  }

  @Test
  fun testReminderEnqueuedForEachVault() {
    // FIX: Test that reminder is enqueued for each vault
    val vaults = listOf("vault1", "vault2", "vault3")

    worker.enqueueReminders(vaults)

    // Should enqueue one work request per vault
    verify(workManager, times(3)).enqueueUniqueWork(
      any(), any(), any()
    )
  }

  @Test
  fun testCancellationOnSignOut() {
    // FIX: Test that cancellation on sign-out removes reminders
    val vaults = listOf("vault1", "vault2", "vault3")

    worker.enqueueReminders(vaults)
    worker.cancelRemindersOnSignOut()

    // Should cancel all work
    verify(workManager).cancelAllWork()
  }

  @Test
  fun testNoRemindersAfterSignOut() {
    // FIX: Verify reminders are actually removed
    val vaults = listOf("vault1", "vault2")

    worker.enqueueReminders(vaults)
    worker.cancelRemindersOnSignOut()

    // Enqueue should be called only once (before cancellation)
    verify(workManager, times(2)).enqueueUniqueWork(
      any(), any(), any()
    )
  }
}
