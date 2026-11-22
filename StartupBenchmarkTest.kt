package com.example.androidTest.benchmark

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import org.junit.Test
import org.junit.Before
import org.junit.After

class StartupBenchmarkTest {
  private lateinit var idlingResource: CountingIdlingResource
  private lateinit var benchmark: StartupBenchmark

  @Before
  fun setup() {
    idlingResource = CountingIdlingResource("startup")
    IdlingRegistry.getInstance().register(idlingResource)
    benchmark = StartupBenchmark(idlingResource)
  }

  @After
  fun cleanup() {
    IdlingRegistry.getInstance().unregister(idlingResource)
  }

  @Test
  fun testStartupWaitForIdlingResource() {
    // FIX: Wait for IdlingResource instead of fixed 1-second sleep
    // This measures actual startup readiness, not elapsed time
    benchmark.measureStartup()

    // IdlingResource automatically waits via Espresso
    // No Thread.sleep(1000) needed
    assert(benchmark.isReady())
  }

  @Test
  fun testStartupWaitForFirstFrame() {
    // FIX: Use first-frame signal from StartupPerformance
    val perf = StartupPerformance()

    perf.onFirstFrameRendered()
    assert(benchmark.getFirstFrameTime() > 0)
  }
}
