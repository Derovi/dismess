package by.dismess.core

import org.koin.core.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal lateinit var App: KoinApplication

private var apiModule = module {
    single<API> { APIImplementation(App.koin.get()) }
}

private var innerModule = module {
}

fun startCore(outerModule: Module) {
    App = koinApplication {
        modules(innerModule, outerModule)
    }
    loadKoinModules(apiModule)
}

fun stopCore() {
    unloadKoinModules(apiModule)
    App.close()
}
