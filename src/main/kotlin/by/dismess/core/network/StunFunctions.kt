package by.dismess.core.network

import de.javawi.jstun.attribute.ChangeRequest
import de.javawi.jstun.attribute.MappedAddress
import de.javawi.jstun.attribute.MessageAttributeInterface
import de.javawi.jstun.header.MessageHeader
import de.javawi.jstun.header.MessageHeaderInterface
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress

fun retrievePublicSocketAddress(port: Int): InetSocketAddress {
    val sendMessageHeader = MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingRequest)
    val changeRequest = ChangeRequest()  // JSTUN requires empty request to be attached
    sendMessageHeader.addMessageAttribute(changeRequest)
    val data: ByteArray = sendMessageHeader.bytes

    val socket = DatagramSocket(port)
    socket.reuseAddress = true
    val packet = DatagramPacket(data, data.size, InetAddress.getByName("stun.l.google.com"), 19302)
    socket.send(packet)
    val receivedPacket = DatagramPacket(ByteArray(32), 32)
    socket.receive(receivedPacket)

    val receiveMessageHeader = MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingResponse)
    receiveMessageHeader.parseAttributes(receivedPacket.data)
    val mappedAddress: MappedAddress = receiveMessageHeader
        .getMessageAttribute(MessageAttributeInterface.MessageAttributeType.MappedAddress) as MappedAddress
    return InetSocketAddress(mappedAddress.address.inetAddress, mappedAddress.port)
}
