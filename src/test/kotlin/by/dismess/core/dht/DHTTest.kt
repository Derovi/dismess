package by.dismess.core.dht

import by.dismess.core.common.VirtualNetwork
import by.dismess.core.common.VirtualUser
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.koin.test.KoinTest
import kotlin.random.Random

class DHTTest : KoinTest {

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    @Test
    fun findTest() {
        val network = VirtualNetwork()

        val firstUser = VirtualUser(network)
        val usersList = mutableListOf(firstUser)

        for (i in 1..1000) {
            usersList.add(VirtualUser(network))
            val randomUser = usersList[Random.nextInt(i)]
            runBlocking { usersList[i].dht.connectTo(randomUser.id, randomUser.address) }
        }

        for (i in 1..1000) {
            val user = Random.nextInt(usersList.size)
            var target = Random.nextInt(usersList.size)
            while (user == target) {
                target = Random.nextInt(usersList.size)
            }
            val targetID = usersList[target].id
            val targetAddress = usersList[target].address
            val findResult = runBlocking { usersList[user].dht.find(targetID) }
            Assert.assertEquals(findResult, targetAddress)
        }
    }

    @Test
    fun findSimpleTest() {
        val network = VirtualNetwork()

        val alice = VirtualUser(network)
        val bob = VirtualUser(network)
        runBlocking { bob.dht.connectTo(alice.id, alice.address) }

        Assert.assertEquals(runBlocking { alice.dht.find(bob.id) }, bob.address)
        Assert.assertEquals(runBlocking { bob.dht.find(alice.id) }, alice.address)
    }

    @Test
    fun storeSimpleTest() {
        val network = VirtualNetwork()

        val alice = VirtualUser(network)
        val bob = VirtualUser(network)
        runBlocking { bob.dht.connectTo(alice.id, alice.address) }

        val message = "Very interesting text"
        Assert.assertTrue(runBlocking { alice.dht.store("Secrete message", message.toByteArray()) })

        val aliceResponse = String(runBlocking { alice.dht.retrieve("Secrete message") } ?: ByteArray(0))
        val bobResponse = String(runBlocking { bob.dht.retrieve("Secrete message") } ?: ByteArray(0))
        Assert.assertEquals(message, aliceResponse)
        Assert.assertEquals(message, bobResponse)

        val emptyResponse = String(runBlocking { alice.dht.retrieve("Wrong key") } ?: ByteArray(0))
        Assert.assertEquals(String(), emptyResponse)
    }

    @Test
    fun storeTest() {
        val network = VirtualNetwork()
        val firstUser = VirtualUser(network)
        val usersList = mutableListOf(firstUser)

        for (i in 1..1000) {
            usersList.add(VirtualUser(network))
            val randomUser = usersList[Random.nextInt(i)]
            runBlocking { usersList[i].dht.connectTo(randomUser.id, randomUser.address) }
        }

        val data = mutableMapOf<String, String>()
        for (i in 1..1000) {
            val key = getRandomString(Random.nextInt(50, 100))
            val message = getRandomString(Random.nextInt(100))
            data[key] = message
            Assert.assertTrue(runBlocking { firstUser.dht.store(key, message.toByteArray()) })
        }

        for (request in data) {
            val response = runBlocking { firstUser.dht.retrieve(request.key) }
            val message = String(response ?: ByteArray(0))
            Assert.assertEquals(request.value, message)
        }

        for (i in 1..100) {
            val wrongKey = getRandomString(Random.nextInt(50, 100))
            val response = runBlocking { firstUser.dht.retrieve(wrongKey) }
            val message = String(response ?: ByteArray(0))
            Assert.assertEquals(message, String())
        }
    }
}
