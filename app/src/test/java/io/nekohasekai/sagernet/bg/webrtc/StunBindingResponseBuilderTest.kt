package io.nekohasekai.sagernet.bg.webrtc

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.InetAddress
import java.net.InetSocketAddress

class StunBindingResponseBuilderTest {
    @Test
    fun buildsIpv4XorMappedAddress() {
        val request = stunRequest()
        val response = StunBindingResponseBuilder.buildSuccessResponse(
            request,
            InetSocketAddress(InetAddress.getByName("203.0.113.9"), 54321)
        )

        assertEquals(0x01, response[0].toInt() and 0xFF)
        assertEquals(0x01, response[1].toInt() and 0xFF)
        assertArrayEquals(request.copyOfRange(4, 20), response.copyOfRange(4, 20))
        assertEquals(0x20, response[21].toInt() and 0xFF)
        assertEquals(0x01, response[25].toInt() and 0xFF)
        assertEquals(203 xor 0x21, response[28].toInt() and 0xFF)
        assertEquals(9 xor 0x42, response[31].toInt() and 0xFF)
    }

    private fun stunRequest() = byteArrayOf(
        0x00, 0x01, 0x00, 0x00,
        0x21, 0x12, 0xA4.toByte(), 0x42,
        0x01, 0x02, 0x03, 0x04,
        0x05, 0x06, 0x07, 0x08,
        0x09, 0x0A, 0x0B, 0x0C,
    )
}
