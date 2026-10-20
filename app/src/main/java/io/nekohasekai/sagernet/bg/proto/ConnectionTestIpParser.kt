package io.nekohasekai.sagernet.bg.proto

object ConnectionTestIpParser {

    private val ipFieldPattern = Regex("\"ip\"\\s*:\\s*\"([^\"]+)\"")

    fun parse(response: String): String {
        val body = response.trim()
        if (body.isEmpty()) return body
        return ipFieldPattern.find(body)?.groupValues?.get(1) ?: body
    }
}
