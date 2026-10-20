package io.nekohasekai.sagernet.bg.webrtc

import io.nekohasekai.sagernet.bg.proto.ConnectionTestIpParser
import java.net.InetAddress

object WebRtcMappedIpSelector {
    fun selectAddress(manualIp: String, connectionTestResponseProvider: () -> String): InetAddress? {
        val trimmedManualIp = manualIp.trim()
        if (trimmedManualIp.isNotEmpty()) return parseNumericIp(trimmedManualIp)
        val testIp = ConnectionTestIpParser.parse(connectionTestResponseProvider())
        return parseNumericIp(testIp)
    }

    private fun parseNumericIp(ip: String): InetAddress? {
        val maybeIpv4 = ip.count { it == '.' } == 3 && ip.all { it == '.' || it in '0'..'9' }
        val maybeIpv6 = ':' in ip
        if (!maybeIpv4 && !maybeIpv6) return null
        return runCatching { InetAddress.getByName(ip) }.getOrNull()
    }
}
