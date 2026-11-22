package com.example.services

import org.junit.Test
import org.junit.Before

class PasskeyRegistrationDiagnosticsTest {
  private lateinit var diagnostics: PasskeyRegistrationDiagnostics

  @Before
  fun setup() {
    diagnostics = PasskeyRegistrationDiagnostics()
  }

  @Test
  fun testEventListCap() {
    // FIX: Test that event list is capped (from #629)
    repeat(100) {
      diagnostics.recordAttestation(id = "key_$it", attestation = "data_$it")
    }
    // Should only keep last 50 (or configured cap)
    assert(diagnostics.getEvents().size <= 50)
  }

  @Test
  fun testRedactionOfStoredFields() {
    // FIX: Test sensitive fields are redacted
    diagnostics.recordAttestation(
      id = "key123",
      attestation = "sensitive_data_should_be_redacted"
    )

    val stored = diagnostics.getEvents().first()
    assert(!stored.toString().contains("sensitive_data"))
  }
}
