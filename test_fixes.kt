// Issues #805, #806, #807, #808 - Test & WebSocket Fixes

// ============================================================================
// Issue #805: WebSocket URL built safely (not string replace)
// ============================================================================

import okhttp3.HttpUrl

class ApiClient(private val baseUrl: String) {
  init {
    // FIX: Validate base URL is https
    require(baseUrl.startsWith("https://")) { "Base URL must be https://" }
  }

  fun getWebSocketUrl(): String {
    // FIX: Use HttpUrl parsing instead of replaceFirst
    val parsed = HttpUrl.parse(baseUrl)
      ?: throw IllegalArgumentException("Invalid base URL: $baseUrl")

    return parsed.newBuilder()
      .scheme("wss")
      .build()
      .toString()
  }
}

// ============================================================================
// Issue #806: OfflineCacheTest - inject clock instead of Thread.sleep
// ============================================================================

interface Clock {
  fun currentTimeMillis(): Long
  fun advance(ms: Long)
}

class SystemClock : Clock {
  override fun currentTimeMillis() = System.currentTimeMillis()
  override fun advance(ms: Long) {
    Thread.sleep(ms)
  }
}

class TestClock : Clock {
  private var time: Long = 0
  override fun currentTimeMillis() = time
  override fun advance(ms: Long) {
    time += ms
  }
}

class OfflineCache(private val clock: Clock = SystemClock()) {
  private val entries = mutableMapOf<String, CacheEntry>()

  fun put(key: String, value: String, ttlMs: Long) {
    entries[key] = CacheEntry(value, clock.currentTimeMillis() + ttlMs)
  }

  fun get(key: String): String? {
    val entry = entries[key] ?: return null
    if (clock.currentTimeMillis() > entry.expiresAt) {
      entries.remove(key)
      return null
    }
    return entry.value
  }

  private data class CacheEntry(val value: String, val expiresAt: Long)
}

// Test usage:
class OfflineCacheTest {
  fun testExpiry() {
    val clock = TestClock()
    val cache = OfflineCache(clock)

    // FIX: No Thread.sleep needed
    cache.put("key", "value", 150)
    assert(cache.get("key") == "value")

    clock.advance(151)
    assert(cache.get("key") == null)  // Expired
  }
}

// ============================================================================
// Issue #807: DeepLinkRateLimiterTest - inject clock, remove 2.1s sleep
// ============================================================================

class DeepLinkRateLimiter(private val clock: Clock = SystemClock()) {
  private var lastAccessTime: Long = 0
  private val rateLimitMs = 2000

  fun allowAccess(): Boolean {
    val now = clock.currentTimeMillis()
    if (now - lastAccessTime >= rateLimitMs) {
      lastAccessTime = now
      return true
    }
    return false
  }
}

// Test usage:
class DeepLinkRateLimiterTest {
  fun testRateLimit() {
    val clock = TestClock()
    val limiter = DeepLinkRateLimiter(clock)

    // FIX: No 2.1s Thread.sleep
    assert(limiter.allowAccess())  // First access allowed
    assert(!limiter.allowAccess()) // Second access blocked

    clock.advance(2001)
    assert(limiter.allowAccess())  // Now allowed again
  }
}

// ============================================================================
// Issue #808: VaultWebSocketOptimizationTest - no 100ms sleeps
// ============================================================================

class WebSocketEventDispatcher(private val clock: Clock = SystemClock()) {
  private val listeners = mutableListOf<(String) -> Unit>()

  fun on(listener: (String) -> Unit) {
    listeners.add(listener)
  }

  fun emit(event: String) {
    listeners.forEach { it(event) }
  }
}

// Test usage:
class VaultWebSocketOptimizationTest {
  fun testEventDelivery() {
    val clock = TestClock()
    val dispatcher = WebSocketEventDispatcher(clock)
    val events = mutableListOf<String>()

    // FIX: No 100ms Thread.sleep needed
    dispatcher.on { events.add(it) }
    dispatcher.emit("event1")

    assert(events.contains("event1"))

    dispatcher.emit("event2")
    assert(events.size == 2)
  }
}
