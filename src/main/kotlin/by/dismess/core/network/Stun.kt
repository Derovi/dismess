package by.dismess.core.network

import de.javawi.jstun.attribute.ChangeRequest
import de.javawi.jstun.attribute.MappedAddress
import de.javawi.jstun.attribute.MessageAttributeInterface
import de.javawi.jstun.header.MessageHeader
import de.javawi.jstun.header.MessageHeaderInterface
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

private val stunServers = listOf(
    InetSocketAddress("stun.l.google.com", 19302),
    InetSocketAddress("stun1.l.google.com", 19302),
    InetSocketAddress("stun2.l.google.com", 19302),
    InetSocketAddress("stun3.l.google.com", 19302),
    InetSocketAddress("stun.stunprotocol.org", 3478))

private const val socketTimeoutMillis = 10000
private const val datagramLength = 68

fun retrievePublicSocketAddress(port: Int): InetSocketAddress? {
    val sendMessageHeader = MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingRequest)
    val changeRequest = ChangeRequest() // JSTUN requires empty request to be attached
    sendMessageHeader.addMessageAttribute(changeRequest)
    val data: ByteArray = sendMessageHeader.bytes

    val socket = DatagramSocket(port)
    socket.reuseAddress = true
    socket.soTimeout = socketTimeoutMillis

    lateinit var packet: DatagramPacket
    var receivedPacket: DatagramPacket? = null
    for (stunAddress in stunServers) {
        try {
            packet = DatagramPacket(data, data.size, stunAddress)
            socket.send(packet)
            receivedPacket = DatagramPacket(ByteArray(datagramLength), datagramLength)
            socket.receive(receivedPacket)
        } catch (e: IOException) {
            receivedPacket = null
        }
        if (receivedPacket != null) {
            break
        }
    }
    // No stun server available
    if (receivedPacket == null) {
        return null
    }
    val receiveMessageHeader = MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingResponse)
    receiveMessageHeader.parseAttributes(receivedPacket.data)
    val mappedAddress: MappedAddress = receiveMessageHeader
        .getMessageAttribute(MessageAttributeInterface.MessageAttributeType.MappedAddress) as MappedAddress
    socket.close()
    return InetSocketAddress(mappedAddress.address.inetAddress, mappedAddress.port)
}
