package by.dismess.core.chating

import by.dismess.core.common.VirtualNetwork
import kotlinx.coroutines.runBlocking
import org.junit.Assert
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
        val chat1 = firstUser.chatManager.startChat(secondUser.dataManager.getId())
        Assert.assertNotNull(chat1)
        Assert.assertEquals(firstUser.chatManager.chats.size, 1)
        Assert.assertEquals(secondUser.chatManager.chats.size, 1)
//        println(chat1!!.id)
//        println(secondUser.chatManager.chats.entries.first().value.id)
//        val chat2 = secondUser.chatManager.chats[chat1!!.id]
//        Assert.assertNotNull(chat2)
//        chat1.sendMessage("Kek lol!")
//        val iter1 = chat1.lastMessage
    }
}
