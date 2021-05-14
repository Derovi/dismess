package by.dismess.core.common

import by.dismess.core.chating.ChatManagerImpl
import by.dismess.core.dht.DHTImpl
import by.dismess.core.events.EventBus
import by.dismess.core.managers.impl.UserManagerImpl
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import kotlinx.coroutines.runBlocking

class VirtualUser(
    network: VirtualNetwork
) {
    val dataManager = MockDataManager()
    val address = runBlocking { dataManager.getOwnIP()!! }
    val id = runBlocking { dataManager.getId() }
    val networkInterface = VirtualNetworkInterface(network, address)
    val networkService = NetworkService(networkInterface)
    val storageInterface = InMemoryStorageInterface()
    val storageService = StorageService(storageInterface)
    val DHT = DHTImpl(networkService, storageService, dataManager)
    val eventBUS = EventBus()
    val userManager = UserManagerImpl(DHT, networkService, dataManager)
    val chatManager = ChatManagerImpl(
        dataManager,
        userManager,
        networkService,
        storageService,
        eventBUS,
        DHT
    )
}
