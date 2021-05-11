package by.dismess.core.network

import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.Base64

fun convertAddressToInvite(address: InetSocketAddress): String {
    val bytes = (address.address.toString().drop(1) + ":" + address.port.toString()).encodeToByteArray()
    return Base64.getEncoder().encode(bytes).decodeToString()
}

fun convertInviteToAddress(invite: String): InetSocketAddress? {
    val converted = Base64.getDecoder().decode(invite).decodeToString()
    if (!validateDecipheredInvite(converted)) {
        return null
    }
    val split = converted.split(':')
    return InetSocketAddress(InetAddress.getByName(split[0]), split.last().toInt())
}

private val inviteRegex =
    Regex("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+$")

fun validateDecipheredInvite(invite: String): Boolean {
    return inviteRegex.matches(invite)
}
