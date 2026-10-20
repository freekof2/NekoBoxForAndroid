package io.nekohasekai.sagernet.fmt

import io.nekohasekai.sagernet.bg.webrtc.StunTcpRelayService
import moe.matsuri.nb4a.SingBoxOptions.Outbound_SocksOptions
import moe.matsuri.nb4a.SingBoxOptions.RouteOptions
import moe.matsuri.nb4a.SingBoxOptions.Rule_DefaultOptions
import moe.matsuri.nb4a.SingBoxOptions.SingBoxOption

object WebRtcStunConfig {
    fun appendRelayOutboundAndRules(outbounds: MutableList<SingBoxOption>, route: RouteOptions) {
        outbounds.add(Outbound_SocksOptions().apply {
            tag = StunTcpRelayService.WEBRTC_STUN_RELAY_TAG
            type = "socks"
            server = LOCALHOST
            server_port = StunTcpRelayService.WEBRTC_STUN_RELAY_PORT
            version = "5"
        })
        route.rules.add(Rule_DefaultOptions().apply {
            inbound = listOf("tun-in")
            network = listOf("udp")
            action = "sniff"
        })
        route.rules.add(Rule_DefaultOptions().apply {
            inbound = listOf("tun-in")
            network = listOf("udp")
            protocol = listOf("stun")
            outbound = StunTcpRelayService.WEBRTC_STUN_RELAY_TAG
        })
    }
}
