package by.dismess.core.chating.elements

import by.dismess.core.chating.elements.stored.ChunkStored

/**
 * Chunks can be complete and incomplete
 * Complete chunks are all chunks in the flow except the last one
 * Complete chunks are immutable
 * Incomplete chunk is the last one, it is mutable
 */
class Chunk(description: ChunkStored) : Storable {
    companion object {
        /**
         * (bytes)
         * Messages can be added until current size less than frontier
         * Chosen in order not to overflow the size of one UDP-datagram
         */
        const val BYTE_SIZE_FRONTIER = 64500
    }

    val messages = description.messages

    var byteSize = 0
        private set
    init {
        for (message in messages) {
            byteSize += message.byteSize
        }
    }

    val complete: Boolean
        get() = byteSize < BYTE_SIZE_FRONTIER

    fun addMessage(message: Message) {
        (messages as MutableList<Message>).add(message)
        byteSize += message.byteSize
    }

    override fun store() {
        TODO("Not yet implemented")
    }

    override fun publish(): Boolean {
        TODO("Not yet implemented")
    }
}
