package io.nekohasekai.sagernet.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionTestStatusFormatterTest {
    @Test
    fun showsDelay() {
        assertEquals(
            "300ms",
            ConnectionTestStatusFormatter.formatLatency(300)
        )
    }
}
