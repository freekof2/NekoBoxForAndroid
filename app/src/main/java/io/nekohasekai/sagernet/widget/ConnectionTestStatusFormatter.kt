package io.nekohasekai.sagernet.widget

object ConnectionTestStatusFormatter {
    fun formatLatency(delayMillis: Int): String = "${delayMillis}ms"
}
