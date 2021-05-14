package by.dismess.core.common

import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.ChatManagerImpl
import by.dismess.core.common.dht.VirtualDHT
import by.dismess.core.common.dht.VirtualDHTCommon
import by.dismess.core.dht.DHTImpl
import by.dismess.core.events.EventBus
import by.dismess.core.managers.DataManager
import by.dismess.core.managers.UserManager
import by.dismess.core.managers.impl.UserManagerImpl
import by.dismess.core.outer.NetworkInterface
import by.dismess.core.outer.StorageInterface
import by.dismess.core.security.SecureNetworkInterface
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import java.net.InetSocketAddress

class VirtualNetwork {
    private val networkInterfaces = mutableMapOf<InetSocketAddress, VirtualNetworkInterface>()

    private var virtualDHTCommon: VirtualDHTCommon? = null

    val configuration = Configuration()
    inner class Configuration {
        var useSecureNI = false
        fun useSecureNI(value: Boolean = true) = this.also { useSecureNI = value }

        var useVirtualDHT = false
        fun useVirtualDHT(value: Boolean = true) = this.also {
            useVirtualDHT = value
            virtualDHTCommon = VirtualDHTCommon()
        }
    }

    fun createUser(): VirtualUser = VirtualUser(
        module {
            single { EventBus() }
            single {
                var result: NetworkInterface = VirtualNetworkInterface(
                    this@VirtualNetwork,
                    runBlocking { get<DataManager>().getOwnIP()!! }
                )
                if (configuration.useSecureNI) {
                    result = SecureNetworkInterface(result)
                }
                result
            }
            single {
                val dataManager = get<DataManager>()
                return@single if (configuration.useVirtualDHT) {
                    VirtualDHT(virtualDHTCommon!!)
                } else {
                    DHTImpl(get(), get(), get())
                }.also { runBlocking { it.initSelf(dataManager.getId(), dataManager.getOwnIP()!!) } }
            }
            single<StorageInterface> { InMemoryStorageInterface() }
            single { NetworkService(get()) }
            single { StorageService(get()) }
            single<DataManager> { MockDataManager() }
            single<UserManager> { UserManagerImpl(get(), get(), get()) }
            single<ChatManager> { ChatManagerImpl(get(), get(), get(), get(), get(), get()) }
        }
    )

    fun register(networkInterface: VirtualNetworkInterface) {
        networkInterfaces[networkInterface.ownAddress] = networkInterface
    }

    fun sendMessage(from: InetSocketAddress, to: InetSocketAddress, data: ByteArray) {
        networkInterfaces[to]?.receiver?.let { it(from, data) }
    }
}
