package io.nekohasekai.sagernet.bg.webrtc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.DatagramPacket
import java.net.DatagramSocket

class StunTcpRelayService(
    private val mappedAddressProvider: WebRtcMappedAddressProvider,
    private val listenPort: Int = WEBRTC_STUN_RELAY_PORT,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var acceptJob: Job? = null

    fun start() {
        if (serverSocket != null) return
        val server = ServerSocket().apply { bind(InetSocketAddress(LOCALHOST, listenPort)) }
        serverSocket = server
        acceptJob = scope.launch {
            while (isActive) {
                try {
                    val socket = server.accept()
                    launch { handleClient(socket) }
                } catch (_: SocketException) {
                    break
                }
            }
        }
    }

    suspend fun stop() {
        serverSocket?.close()
        serverSocket = null
        acceptJob?.cancelAndJoin()
        acceptJob = null
    }

    fun relayPacket(socks5UdpPacket: ByteArray): ByteArray? {
        val packet = Socks5UdpPacket.parse(socks5UdpPacket) ?: return null
        if (!StunPacketInspector.isBindingRequest(packet.payload)) return null
        val mappedAddress = mappedAddressProvider.getMappedAddress() ?: return null
        val response = StunBindingResponseBuilder.buildSuccessResponse(packet.payload, mappedAddress)
        return Socks5UdpPacket(packet.host, packet.port, response).encode()
    }

    private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
        socket.use { client ->
            val input = client.getInputStream()
            val output = client.getOutputStream()
            if (!handleGreeting(input, output)) return@use
            if (readCommand(input) != 0x03) {
                writeReply(output, 0x07, 0)
                return@use
            }
            DatagramSocket(InetSocketAddress(InetAddress.getByName(LOCALHOST), 0)).use { udp ->
                udp.soTimeout = 1000
                writeReply(output, 0x00, udp.localPort)
                val buffer = ByteArray(8192)
                val packet = DatagramPacket(buffer, buffer.size)
                while (isActive && !client.isClosed) {
                    try {
                        udp.receive(packet)
                    } catch (_: SocketTimeoutException) {
                        continue
                    }
                    val response = relayPacket(packet.data.copyOfRange(packet.offset, packet.offset + packet.length))
                    if (response != null) {
                        udp.send(DatagramPacket(response, response.size, packet.socketAddress))
                    }
                }
            }
        }
    }

    private fun handleGreeting(input: InputStream, output: OutputStream): Boolean {
        val header = input.readNBytesCompat(2)
        if (header.size != 2 || header[0] != 0x05.toByte()) return false
        val methods = input.readNBytesCompat(header[1].toInt() and 0xFF)
        val method = if (methods.contains(0x00.toByte())) 0x00 else 0xFF
        output.write(byteArrayOf(0x05, method.toByte()))
        return method == 0x00
    }

    private fun readCommand(input: InputStream): Int {
        val header = input.readNBytesCompat(4)
        if (header.size != 4 || header[0] != 0x05.toByte()) return -1
        val atyp = header[3].toInt() and 0xFF
        val addressLength = when (atyp) {
            0x01 -> 4
            0x03 -> input.read()
            0x04 -> 16
            else -> return -1
        }
        input.readNBytesCompat(addressLength + 2)
        return header[1].toInt() and 0xFF
    }

    private fun writeReply(output: OutputStream, reply: Int, port: Int) {
        output.write(byteArrayOf(0x05, reply.toByte(), 0x00, 0x01, 127, 0, 0, 1, ((port shr 8) and 0xFF).toByte(), (port and 0xFF).toByte()))
    }

    private fun InputStream.readNBytesCompat(length: Int): ByteArray {
        val buffer = ByteArray(length)
        var read = 0
        while (read < length) {
            val n = read(buffer, read, length - read)
            if (n < 0) break
            read += n
        }
        return if (read == length) buffer else buffer.copyOf(read)
    }

    companion object {
        const val LOCALHOST = "127.0.0.1"
        const val WEBRTC_STUN_RELAY_TAG = "webrtc-stun-relay"
        const val WEBRTC_STUN_RELAY_PORT = 10818
    }
}
