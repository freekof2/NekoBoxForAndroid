package io.nekohasekai.sagernet.bg.webrtc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StunPacketInspectorTest {
    @Test
    fun acceptsValidBindingRequest() {
        assertTrue(StunPacketInspector.isBindingRequest(stunRequest()))
    }

    @Test
    fun rejectsInvalidMagicCookie() {
        val packet = stunRequest()
        packet[7] = 0x43

        assertFalse(StunPacketInspector.isBindingRequest(packet))
    }

    private fun stunRequest() = byteArrayOf(
        0x00, 0x01, 0x00, 0x00,
        0x21, 0x12, 0xA4.toByte(), 0x42,
        0x01, 0x02, 0x03, 0x04,
        0x05, 0x06, 0x07, 0x08,
        0x09, 0x0A, 0x0B, 0x0C,
    )
}
