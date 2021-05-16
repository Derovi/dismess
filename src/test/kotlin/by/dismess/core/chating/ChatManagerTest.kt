package by.dismess.core.chating

import by.dismess.core.chating.viewing.FlowIterator
import by.dismess.core.chating.viewing.MessageIterator
import by.dismess.core.common.VirtualNetwork
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

fun testMessageHistory(messageIterator: MessageIterator, messages: List<String>): Boolean {
    println("vvvvv")
    var idx = messages.lastIndex
    do {
        println(messageIterator.value!!.text + "*"+messages[idx] + "|" + (messageIterator as FlowIterator).messageID!!.index)
        if (messageIterator.value!!.text != messages[idx--]) {
            return false
        }
    } while (runBlocking { messageIterator.previous() })
    runBlocking {
        messageIterator.previous()
        messageIterator.previous()
    }
    println("^^^^^")
    return true
}

class ChatManagerTest {
    @Test
    fun testSimple() {
        val network = VirtualNetwork()
        network.configuration

        val firstUser = network.createUser()
        val secondUser = network.createUser()
        runBlocking {
            firstUser.dht.connectTo(secondUser.id, secondUser.address)
            secondUser.dht.connectTo(firstUser.id, firstUser.address)
            repeat(10) {
                val usr = network.createUser()
                usr.dht.connectTo(firstUser.id, firstUser.address)
                firstUser.dht.connectTo(usr.id, usr.address)
                usr.dht.connectTo(secondUser.id, secondUser.address)
                secondUser.dht.connectTo(usr.id, usr.address)
            }
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
        val messages = mutableListOf("Kek lol!", "AA uu!", "op op")
        for (message in messages) {
            runBlocking {
                chat1.sendMessage(message)
            }
        }
        runBlocking {

            chat2!!.sendMessage("first")
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

        network.makeOffline(secondUser)

        runBlocking {
            messages.add("uu")
            chat1.sendMessage("uu")
        }

        runBlocking {
            delay(500)
        }

        network.makeOnline(secondUser)

        runBlocking {
            chat1.synchronize()
            chat2.synchronize()
            chat2.sendMessage("second")
        }



        println(chat2.otherFlow.lastMessage)

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

        Assert.assertTrue(
            testMessageHistory(
                runBlocking { FlowIterator.create(chat1.otherFlow.chatManager, chat1.otherFlow, chat1.otherFlow.lastMessage) }, listOf("first", "second")
            )
        )
        Assert.assertTrue(
            testMessageHistory(
                runBlocking { FlowIterator.create(chat2.ownFlow.chatManager, chat2.ownFlow, chat2.ownFlow.lastMessage) }, listOf("first", "second")
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
