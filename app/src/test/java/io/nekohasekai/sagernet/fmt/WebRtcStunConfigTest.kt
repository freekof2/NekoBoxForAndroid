package io.nekohasekai.sagernet.fmt

import io.nekohasekai.sagernet.bg.webrtc.StunTcpRelayService
import moe.matsuri.nb4a.SingBoxOptions.Outbound
import moe.matsuri.nb4a.SingBoxOptions.RouteOptions
import moe.matsuri.nb4a.SingBoxOptions.SingBoxOption
import org.junit.Assert.assertEquals
import org.junit.Test

class WebRtcStunConfigTest {
    @Test
    fun appendsRelayOutboundAfterProxyOutbound() {
        val outbounds = mutableListOf<SingBoxOption>(Outbound().apply { tag = TAG_PROXY })
        val route = RouteOptions().apply { rules = mutableListOf() }

        WebRtcStunConfig.appendRelayOutboundAndRules(outbounds, route)

        assertEquals(TAG_PROXY, outbounds.first().tag)
        assertEquals(StunTcpRelayService.WEBRTC_STUN_RELAY_TAG, outbounds.last().tag)
    }
}
