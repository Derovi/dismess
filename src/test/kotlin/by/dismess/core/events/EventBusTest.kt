package by.dismess.core.events

import org.junit.Assert
import org.junit.Test

class EventBusTest {

    @Test
    fun test() {
        val eventBus = EventBus()

        class FirstEvent(val data: String) : Event
        class SecondEvent(val data: Int) : Event
        class ThirdEvent(val data: Boolean) : Event

        var firstA: String? = null
        var firstB: String? = null
        var second = -1
        var third = false
        eventBus.registerHandler<FirstEvent> {
            firstA = it.data
        }
        eventBus.registerHandler<FirstEvent> {
            firstB = it.data
        }
        eventBus.registerHandler<SecondEvent> {
            second = it.data
        }
        eventBus.registerHandler<ThirdEvent> {
            third = it.data
        }
        eventBus.callEvent(FirstEvent("44"))
        eventBus.callEvent(SecondEvent(31))
        // assertions
        Assert.assertEquals(firstA, "44")
        Assert.assertEquals(firstB, "44")
        Assert.assertEquals(second, 31)
        Assert.assertEquals(third, false)
    }
}
