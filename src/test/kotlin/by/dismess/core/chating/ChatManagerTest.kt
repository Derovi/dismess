package by.dismess.core.chating

import by.dismess.core.common.VirtualNetwork
import by.dismess.core.common.VirtualUser
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ChatManagerTest {
    @Test
    fun testSimple() = runBlocking {
        val virtualNetwork = VirtualNetwork()
        val firstUser = VirtualUser(virtualNetwork)
        val secondUser = VirtualUser(virtualNetwork)
        firstUser.dht.connectTo(secondUser.id, secondUser.address)
        firstUser.chatManager.startChat(secondUser.dataManager.getId())
        println(firstUser.chatManager.chats.size)
        println(secondUser.chatManager.chats.size)
    }
}
