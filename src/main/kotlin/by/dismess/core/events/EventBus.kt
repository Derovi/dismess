package by.dismess.core.events

import kotlin.reflect.KClass

class EventBus {
    val handlers = mutableMapOf<KClass<*>, MutableList<EventHandler>>()
    inline fun <reified T : Event> callEvent(event: T) {
        for (handler in handlers.getOrDefault(event::class, mutableListOf())) {
            handler(event)
        }
    }
    inline fun <reified T : Event> registerHandler(noinline handler: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        handlers.getOrPut(T::class) { mutableListOf() }.add(handler as EventHandler)
    }
}

typealias EventHandler = (Event) -> Unit
