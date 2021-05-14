package by.dismess.core.chating

import by.dismess.core.common.VirtualNetwork
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ChatManagerTest {
    @Test
    fun testSimple() = runBlocking {
        val virtualNetwork = VirtualNetwork()
        virtualNetwork.configuration
            .useVirtualDHT()

        val firstUser = virtualNetwork.createUser()
        val secondUser = virtualNetwork.createUser()
        firstUser.dht.connectTo(secondUser.id, secondUser.address)
        firstUser.chatManager.startChat(secondUser.dataManager.getId())
        println(firstUser.chatManager.chats.size)
        println(secondUser.chatManager.chats.size)
    }
}
