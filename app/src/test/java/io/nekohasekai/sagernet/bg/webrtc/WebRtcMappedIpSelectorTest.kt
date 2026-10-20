package io.nekohasekai.sagernet.bg.webrtc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebRtcMappedIpSelectorTest {
    @Test
    fun usesConnectionTestIpWhenManualIpIsEmpty() {
        val address = WebRtcMappedIpSelector.selectAddress("  ") {
            "{\"ip\":\"203.0.113.7\"}"
        }

        assertEquals("203.0.113.7", address?.hostAddress)
    }

    @Test
    fun usesManualIpv4WhenProvided() {
        val address = WebRtcMappedIpSelector.selectAddress("198.51.100.8") {
            error("connection test IP should not be requested")
        }

        assertEquals("198.51.100.8", address?.hostAddress)
    }

    @Test
    fun usesManualIpv6WhenProvided() {
        val address = WebRtcMappedIpSelector.selectAddress("2001:db8::1") {
            error("connection test IP should not be requested")
        }

        assertEquals("2001:db8:0:0:0:0:0:1", address?.hostAddress)
    }

    @Test
    fun rejectsManualHostnames() {
        val address = WebRtcMappedIpSelector.selectAddress("example.com") {
            error("connection test IP should not be requested")
        }

        assertNull(address)
    }
}
