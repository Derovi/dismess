package by.dismess.core.network

import by.dismess.core.model.Invite
import by.dismess.core.utils.UniqID
import org.junit.Assert
import org.junit.Test
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.Base64

class InviteTest {
    @Test
    fun testRetrieve() {
        val address = retrievePublicSocketAddress(8080)
        Assert.assertNotNull(address)
    }

    @Test
    fun testReal() {
        val initialAddress = InetAddress.getByName("31.214.29.41")
        val initialPort = 12345
        val initialId = UniqID("0")
        val initialSocketAddress = InetSocketAddress(initialAddress, initialPort)
        val invite = Invite(initialId, initialSocketAddress)
        val inviteString = invite.toInviteString()
        val inviteFromInviteString = Invite.create(inviteString)
        Assert.assertEquals(initialSocketAddress, inviteFromInviteString!!.address)
        Assert.assertEquals(initialId, inviteFromInviteString.userId)
    }

    @Test
    fun testJunk() {
        val initialEncodedBytes = "192.168.86868:01:111".encodeToByteArray() // Non-valid ip + userId
        val inviteData = Base64.getEncoder().encode(initialEncodedBytes).decodeToString()
        val invite = Invite.create(inviteData)
        Assert.assertNull(invite)
    }
}