package io.nekohasekai.sagernet.bg.webrtc

import io.nekohasekai.sagernet.database.DataStore
import libcore.BoxInstance
import libcore.Libcore
import java.net.InetSocketAddress

class WebRtcMappedAddressProvider(
    private val box: BoxInstance,
    private val manualIpProvider: () -> String,
) {
    fun getMappedAddress(): InetSocketAddress? {
        val address = WebRtcMappedIpSelector.selectAddress(manualIpProvider()) {
            Libcore.urlTestIp(
                box,
                DataStore.connectionTestIPURL,
                3000
            )
        }
        return address?.let { InetSocketAddress(it, 9) }
    }
}
