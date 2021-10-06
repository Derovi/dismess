package by.dismess.core.chating.viewing

import by.dismess.core.chating.elements.Message

interface MessageIterator {
    val value: Message?

    /**
     * True if has next
     */
    suspend fun next(): Boolean
    /**
     * True if has previous
     */
    suspend fun previous(): Boolean
}
