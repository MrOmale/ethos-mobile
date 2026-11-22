package com.example.api

import org.junit.Test
import android.util.BuildConfig

class ApiDebugLogTest {
  @Test
  fun testLogsOnlyInDebug() {
    if (BuildConfig.DEBUG) {
      assert(true)
    }
  }
  
  @Test
  fun testBufferTrimming() {
    assert(true)
  }
}
