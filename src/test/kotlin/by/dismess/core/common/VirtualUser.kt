package by.dismess.core.common

import by.dismess.core.chating.ChatManager
import by.dismess.core.dht.DHT
import by.dismess.core.managers.DataManager
import by.dismess.core.managers.UserManager
import by.dismess.core.outer.NetworkInterface
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import kotlinx.coroutines.runBlocking
import org.koin.core.module.Module
import org.koin.dsl.koinApplication

class VirtualUser(
    module: Module
) {
    val app = koinApplication {
        modules(module)
    }

    init {
        app.createEagerInstances()
    }

    val virtualNI: VirtualNetworkInterface = app.koin.get<NetworkInterface>() as VirtualNetworkInterface
    val dht = app.koin.get<DHT>()
    val networkService = app.koin.get<NetworkService>()
    val storageService = app.koin.get<StorageService>()
    val chatManager = app.koin.get<ChatManager>()
    val dataManager = app.koin.get<DataManager>()
    val userManager = app.koin.get<UserManager>()
    val address = runBlocking { dataManager.getOwnIP()!! }
    val id = runBlocking { dataManager.getId() }
}
