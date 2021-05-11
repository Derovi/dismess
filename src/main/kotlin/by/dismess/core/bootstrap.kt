package by.dismess.core

import AppImpl
import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.ChatManagerImpl
import by.dismess.core.dht.DHT
import by.dismess.core.dht.DHTImpl
import by.dismess.core.events.EventBus
import by.dismess.core.managers.App
import by.dismess.core.managers.DataManager
import by.dismess.core.managers.UserManager
import by.dismess.core.managers.impl.DataManagerImpl
import by.dismess.core.managers.impl.UserManagerImpl
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import com.beust.klaxon.Klaxon
import org.koin.core.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * Holds dependencies that shouldn't be visible for users
 */
internal lateinit var App: KoinApplication
val klaxon = Klaxon()

private var managersModule = module {
    single<UserManager> { UserManagerImpl(get(), get(), get()) }
    single<DataManager> { DataManagerImpl(get()) }
}

private var apiModule = module {
    // describes dependencies that should be visible for users
    single<App> { AppImpl(get(), get()) }
    single<ChatManager> { ChatManagerImpl(get(), get(), get(), get(), get()) }
    single { EventBus() }
}

private var servicesModule = module {
    // describes dependencies inside Core (NOT VISIBLE for users)
    single { NetworkService(get()) }
    single { StorageService(get()) }
}

private var dhtModule = module {
    single<DHT> { DHTImpl(get(), get(), get()) }
}

fun getModulesList(): List<Module> {
    return listOf(servicesModule, dhtModule, managersModule)
}

fun startCore(outerModule: Module) {
    App = koinApplication {
        modules(servicesModule, dhtModule, outerModule)
    }
    loadKoinModules(listOf(apiModule, dhtModule, outerModule))
}

fun stopCore() {
    unloadKoinModules(listOf(apiModule, dhtModule))
    App.close()
}
