package io.nekohasekai.sagernet.bg.webrtc

object StunPacketInspector {
    private const val HEADER_LENGTH = 20

    fun isBindingRequest(payload: ByteArray): Boolean {
        if (!isStunPacket(payload)) return false
        return payload[0] == 0.toByte() && payload[1] == 1.toByte()
    }

    fun isStunPacket(payload: ByteArray): Boolean {
        if (payload.size < HEADER_LENGTH) return false
        if ((payload[0].toInt() and 0xC0) != 0) return false
        if (payload[4] != 0x21.toByte() || payload[5] != 0x12.toByte() ||
            payload[6] != 0xA4.toByte() || payload[7] != 0x42.toByte()
        ) return false
        val messageLength = ((payload[2].toInt() and 0xFF) shl 8) or (payload[3].toInt() and 0xFF)
        return messageLength % 4 == 0 && HEADER_LENGTH + messageLength <= payload.size
    }
}
