package by.dismess.core.model

import by.dismess.core.utils.UniqID
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*

class Invite(var userId: UniqID, var address: InetSocketAddress) {

    fun toInviteString(): String {
        val bytes =
            (address.address.toString().drop(1) + ":" + address.port.toString() + ":" + userId).encodeToByteArray()
        return Base64.getEncoder().encode(bytes).decodeToString()
    }

    companion object {
        fun create(inviteString: String): Invite? {
            return convertToInvite(inviteString)
        }

        private fun convertToInvite(invite: String): Invite? {
            val converted = Base64.getDecoder().decode(invite).decodeToString()
            if (!validateDecipheredInvite(converted)) {
                return null
            }
            val split = converted.split(':')
            val address = InetSocketAddress(InetAddress.getByName(split[0]), split[1].toInt())
            val userId = UniqID(split.last())
            return Invite(userId, address)
        }

        private val inviteRegex =
            Regex("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+:[0-9]+$")

        private fun validateDecipheredInvite(invite: String): Boolean {
            return inviteRegex.matches(invite)
        }
    }
}
