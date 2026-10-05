package com.saksham.deskcompanion

import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

data class DeskEndpoint(
    val host: String,
    val port: Int
)

object DeskDiscovery {

    private const val DISCOVERY_PORT = 8766
    private const val MESSAGE = "DESK_DISCOVER"
    private const val TIMEOUT_MS = 1500

    fun discover(): DeskEndpoint? {

        DatagramSocket().use { socket ->

            socket.broadcast = true
            socket.soTimeout = TIMEOUT_MS

            val message =
                MESSAGE.toByteArray(Charsets.UTF_8)

            val packet = DatagramPacket(
                message,
                message.size,
                InetAddress.getByName("255.255.255.255"),
                DISCOVERY_PORT
            )

            socket.send(packet)

            val buffer = ByteArray(4096)

            val responsePacket = DatagramPacket(
                buffer,
                buffer.size
            )

            return try {

                socket.receive(responsePacket)

                val response =
                    String(
                        responsePacket.data,
                        0,
                        responsePacket.length,
                        Charsets.UTF_8
                    )

                val json =
                    JSONObject(response)

                if (
                    json.optString("type") !=
                    "DESK_COMPANION_DISCOVERY"
                ) {
                    null
                } else {

                    DeskEndpoint(
                        host =
                            responsePacket.address.hostAddress
                                ?: return null,

                        port =
                            json.optInt("port", 8765)
                    )
                }

            } catch (_: Exception) {
                null
            }
        }
    }
}
