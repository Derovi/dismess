package by.dismess.core.services

import by.dismess.core.outer.NetworkInterface
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import java.net.InetAddress
import java.net.InetSocketAddress

class NetworkServiceTest : KoinTest {

    private val networkService by inject<NetworkService>()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(
            module {
                single { NetworkService(get()) }
                single<NetworkInterface> {
                    object : NetworkInterface {
                        private lateinit var receiver: (sender: InetAddress, data: ByteArray) -> Unit

                        override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
                            receiver(address.address, data)
                        }

                        override suspend fun setMessageReceiver(receiver: (sender: InetAddress, data: ByteArray) -> Unit) {
                            this.receiver = receiver
                        }
                    }
                }
            }
        )
    }

    @Test
    fun test() {
        networkService.registerHandler("test") { Assert.fail() }
        var visited = false
        networkService.registerHandler("TEST") {
            visited = true
            Assert.assertEquals(it.data, "Hello, world!")
            Assert.assertEquals(it.tag, "TEST")
            Assert.assertEquals(it.senderAddress, InetAddress.getByName("123.123.254.4"))
        }
        networkService.sendMessage(
            InetSocketAddress("123.123.254.4", 4567), "TEST", "Hello, world!"
        )
        Assert.assertTrue(visited)
    }
}
