package by.dismess.core.common

import by.dismess.core.dht.DHTImpl
import by.dismess.core.dht.DHTTest
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import kotlinx.coroutines.runBlocking

class VirtualUser(
    network: VirtualNetwork
) {
    val dataManager = DHTTest.MockDataManager()
    val address = runBlocking { dataManager.getOwnIP()!! }
    val id = runBlocking { dataManager.getId() }
    val networkInterface = VirtualNetworkInterface(network, address)
    val networkService = NetworkService(networkInterface)
    val storageInterface = InMemoryStorageInterface()
    val storageService = StorageService(storageInterface)
    val DHT = DHTImpl(networkService, storageService, dataManager)
}
