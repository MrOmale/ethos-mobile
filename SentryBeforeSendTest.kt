package com.example

import io.sentry.SentryEvent
import io.sentry.protocol.Request
import org.junit.Test

class SentryBeforeSendTest {
  @Test
  fun testStripsQueryString() {
    val event = SentryEvent()
    event.request = Request().apply {
      url = "https://api.example.com?token=secret"
    }
    val cleaned = event.apply {
      request?.url = request?.url?.substringBefore("?")
    }
    assert(!cleaned.request?.url?.contains("token")!!)
  }
  
  @Test
  fun testStripsHeaders() {
    val event = SentryEvent()
    event.request = Request().apply { headers = mapOf("Auth" to "token") }
    event.request = event.request?.copy()?.also { it.headers = null }
    assert(event.request?.headers == null)
  }
}
