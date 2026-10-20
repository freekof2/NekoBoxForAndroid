package io.nekohasekai.sagernet.bg.webrtc

import java.net.InetAddress

data class Socks5UdpPacket(val host: String, val port: Int, val payload: ByteArray) {
    fun encode(): ByteArray {
        require(port in 0..65535)
        val address = parseNumericIp(host)
        val addressBytes: ByteArray
        val atyp: Int
        if (address != null) {
            addressBytes = address.address
            atyp = if (addressBytes.size == 4) 0x01 else 0x04
        } else {
            addressBytes = host.toByteArray(Charsets.US_ASCII)
            require(addressBytes.isNotEmpty() && addressBytes.size <= 255)
            atyp = 0x03
        }
        val headerLength = if (atyp == 0x03) 5 + addressBytes.size + 2 else 4 + addressBytes.size + 2
        val encoded = ByteArray(headerLength + payload.size)
        encoded[3] = atyp.toByte()
        var offset = 4
        if (atyp == 0x03) encoded[offset++] = addressBytes.size.toByte()
        addressBytes.copyInto(encoded, offset)
        offset += addressBytes.size
        encoded[offset++] = ((port shr 8) and 0xFF).toByte()
        encoded[offset++] = (port and 0xFF).toByte()
        payload.copyInto(encoded, offset)
        return encoded
    }

    companion object {
        fun parse(buffer: ByteArray): Socks5UdpPacket? {
            if (buffer.size < 10 || buffer[0] != 0.toByte() || buffer[1] != 0.toByte() || buffer[2] != 0.toByte()) return null
            var offset = 4
            val host = when (buffer[3].toInt() and 0xFF) {
                0x01 -> {
                    if (buffer.size < offset + 4 + 2) return null
                    InetAddress.getByAddress(buffer.copyOfRange(offset, offset + 4)).hostAddress.also { offset += 4 }
                }
                0x03 -> {
                    if (buffer.size < offset + 1) return null
                    val length = buffer[offset++].toInt() and 0xFF
                    if (length == 0 || buffer.size < offset + length + 2) return null
                    String(buffer, offset, length, Charsets.US_ASCII).also { offset += length }
                }
                0x04 -> {
                    if (buffer.size < offset + 16 + 2) return null
                    InetAddress.getByAddress(buffer.copyOfRange(offset, offset + 16)).hostAddress.also { offset += 16 }
                }
                else -> return null
            }
            val port = ((buffer[offset].toInt() and 0xFF) shl 8) or (buffer[offset + 1].toInt() and 0xFF)
            offset += 2
            return Socks5UdpPacket(host, port, buffer.copyOfRange(offset, buffer.size))
        }

        private fun parseNumericIp(host: String): InetAddress? {
            val maybeIpv4 = host.count { it == '.' } == 3 && host.all { it == '.' || it in '0'..'9' }
            val maybeIpv6 = ':' in host
            if (!maybeIpv4 && !maybeIpv6) return null
            return runCatching { InetAddress.getByName(host) }.getOrNull()
        }
    }
}
