package io.nekohasekai.sagernet.bg.webrtc

import java.net.InetSocketAddress

object StunBindingResponseBuilder {
    fun buildSuccessResponse(request: ByteArray, mappedAddress: InetSocketAddress): ByteArray {
        require(StunPacketInspector.isBindingRequest(request)) { "Request must be a STUN binding request" }
        val addressBytes = mappedAddress.address.address
        val isIpv4 = addressBytes.size == 4
        val attrLength = if (isIpv4) 8 else 20
        val messageLength = 4 + attrLength
        val response = ByteArray(20 + messageLength)

        response[0] = 0x01
        response[1] = 0x01
        response[2] = ((messageLength shr 8) and 0xFF).toByte()
        response[3] = (messageLength and 0xFF).toByte()
        request.copyInto(response, destinationOffset = 4, startIndex = 4, endIndex = 20)

        response[20] = 0x00
        response[21] = 0x20
        response[22] = ((attrLength shr 8) and 0xFF).toByte()
        response[23] = (attrLength and 0xFF).toByte()
        response[25] = if (isIpv4) 0x01 else 0x02

        val xorPort = mappedAddress.port xor (((request[4].toInt() and 0xFF) shl 8) or (request[5].toInt() and 0xFF))
        response[26] = ((xorPort shr 8) and 0xFF).toByte()
        response[27] = (xorPort and 0xFF).toByte()

        addressBytes.forEachIndexed { index, byte ->
            response[28 + index] = (byte.toInt() xor (request[4 + (index % 16)].toInt() and 0xFF)).toByte()
        }
        return response
    }
}
