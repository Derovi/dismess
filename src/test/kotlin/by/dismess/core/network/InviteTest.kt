package by.dismess.core.network

import org.junit.Assert
import org.junit.Test
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*

class InviteTest {
    @Test
    fun testRetrieve() {
        val address = retrievePublicSocketAddress(8080)
        Assert.assertNotNull(address)
        val decipheredInvite = address!!.address.toString().drop(1) + ":" + address.port.toString()
        println(decipheredInvite)
        Assert.assertTrue(validateDecipheredInvite(decipheredInvite))
    }

    @Test
    fun testReal() {
        val initialAddress = InetAddress.getByName("31.214.29.41")
        val initialPort = 12345
        val initialSocketAddress = InetSocketAddress(initialAddress, initialPort)
        val invite: String = convertAddressToInvite(initialSocketAddress)
        val decipheredSocketAddress: InetSocketAddress? = convertInviteToAddress(invite)
        Assert.assertEquals(initialSocketAddress, decipheredSocketAddress)
    }

    @Test
    fun testJunk() {
        val initialEncodedBytes = "192.168.86868:01".encodeToByteArray()  // Non-valid ip
        val invite = Base64.getEncoder().encode(initialEncodedBytes).decodeToString()
        val finalAddress: InetSocketAddress? = convertInviteToAddress(invite)
        Assert.assertNull(finalAddress)
    }
}