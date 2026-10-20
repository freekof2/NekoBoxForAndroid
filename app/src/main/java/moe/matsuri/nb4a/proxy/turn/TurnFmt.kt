package moe.matsuri.nb4a.proxy.turn

import io.nekohasekai.sagernet.ktx.blankAsNull
import io.nekohasekai.sagernet.ktx.linkBuilder
import io.nekohasekai.sagernet.ktx.toLink
import io.nekohasekai.sagernet.ktx.urlSafe
import moe.matsuri.nb4a.SingBoxOptions
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun buildSingBoxOutboundTurnBean(bean: TurnBean): SingBoxOptions.Outbound_TURNOptions {
    return SingBoxOptions.Outbound_TURNOptions().apply {
        type = "turn"
        server = bean.serverAddress
        server_port = bean.serverPort
        username = bean.username
        password = bean.password
        network = listOf("tcp", "udp")
        // Use TCP control for plain TURN. It supports both UDP and TCP relay allocations
        // and is required by TURN servers that do not accept TCP allocations over UDP control.
        control_transport = if (bean.tls == true) "tls" else "tcp"
        if (bean.tls == true) {
            tls = SingBoxOptions.OutboundTLSOptions().apply {
                enabled = true
                server_name = bean.sni.blankAsNull()
                if (bean.allowInsecure) insecure = true
            }
        }
    }
}

fun TurnBean.toUri(): String {
    val builder = linkBuilder()
        .host(serverAddress)
        .port(serverPort)
        .username(username)
        .password(password)
    if (!name.isNullOrBlank()) builder.encodedFragment(name.urlSafe())
    if (tls) builder.addQueryParameter("tls", "1")
    if (allowInsecure) builder.addQueryParameter("insecure", "1")
    if (!sni.isNullOrBlank()) builder.addQueryParameter("sni", sni)
    return builder.toLink("turn")
}

fun parseTurn(url: String): TurnBean {
    val link = url.replaceFirst("turn://", "https://").toHttpUrlOrNull()
        ?: error("invalid turn link $url")
    return TurnBean().apply {
        serverAddress = link.host
        serverPort = link.port
        username = link.username
        password = link.password
        name = link.fragment ?: ""
        tls = link.queryParameter("tls") == "1" || link.queryParameter("tls") == "true"
        allowInsecure = link.queryParameter("insecure") == "1" || link.queryParameter("insecure") == "true"
        sni = link.queryParameter("sni") ?: ""
    }
}
