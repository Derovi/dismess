package by.dismess.core.chating

import by.dismess.core.chating.viewing.FlowIterator
import by.dismess.core.chating.viewing.MessageIterator
import by.dismess.core.common.VirtualNetwork
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

fun testMessageHistory(messageIterator: MessageIterator, messages: List<String>): Boolean {
    var idx = messages.lastIndex
    do {
        if (messageIterator.value!!.text != messages[idx--]) {
            return false
        }
        println(messageIterator.value!!.text)
    } while (runBlocking { messageIterator.previous() })
    runBlocking {
        messageIterator.previous()
        messageIterator.previous()
    }
    return true
}

class ChatManagerTest {
    @Test
    fun testSimple() {
        val virtualNetwork = VirtualNetwork()
        virtualNetwork.configuration

        val firstUser = virtualNetwork.createUser()
        val secondUser = virtualNetwork.createUser()
        runBlocking {
            firstUser.dht.connectTo(secondUser.id, secondUser.address)
            secondUser.dht.connectTo(firstUser.id, firstUser.address)
        }
        val chat1 = runBlocking { firstUser.chatManager.startChat(secondUser.dataManager.getId()) }
        Assert.assertNotNull(chat1)
        runBlocking {
            println(secondUser.dataManager.getId())
            println(secondUser.chatManager.chats.size)
        }
        runBlocking {
            delay(50)
        }
        Assert.assertEquals(firstUser.chatManager.chats.size, 1)
        Assert.assertEquals(secondUser.chatManager.chats.size, 1)
        println(chat1!!.id)
        println(secondUser.chatManager.chats.entries.first().value.id)
        val chat2 = secondUser.chatManager.chats[chat1!!.id]
        Assert.assertNotNull(chat2)
        val messages = listOf("Kek lol!", "AA uu!", "op op")
        for (message in messages) {
            runBlocking {
                chat1.sendMessage(message)
            }
        }
        println(chat2!!.otherFlow.chunks.size)
        println(chat2.otherFlow.lastMessage == null)
        Assert.assertTrue(
            testMessageHistory(
                runBlocking { FlowIterator.create(chat1.ownFlow.chatManager, chat1.ownFlow, chat1.ownFlow.lastMessage) }, messages
            )
        )
        Assert.assertTrue(
            testMessageHistory(
                runBlocking { FlowIterator.create(chat2.ownFlow.chatManager, chat2.otherFlow, chat2.otherFlow.lastMessage) }, messages
            )
        )
        val iter1: MessageIterator
        println(chat1.ownFlow.chunks.size)
//        runBlocking {
//            iter1 = FlowIterator.create(
//                secondUser.chatManager,
//                chat2!!.otherFlow,
//                chat2.otherFlow.lastMessage!!
//            )
//        }
    }
}
