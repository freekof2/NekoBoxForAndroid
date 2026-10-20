package io.nekohasekai.sagernet.bg.proto

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionTestIpParserTest {

    @Test
    fun extractsIpFromJsonResponse() {
        val response = """{"ip":"203.0.113.7","country":"TEST"}"""

        assertEquals("203.0.113.7", ConnectionTestIpParser.parse(response))
    }

    @Test
    fun keepsPlainTextResponse() {
        assertEquals("2001:db8::1", ConnectionTestIpParser.parse(" 2001:db8::1\n"))
    }
}
