package by.dismess.core.chating.elements

import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.LoadMode
import by.dismess.core.chating.elements.stored.ChunkStored

/**
 * Chunks can be complete and incomplete
 * Complete chunks are all chunks in the flow except the last one
 * Complete chunks are immutable
 * Incomplete chunk is the last one, it is mutable
 */
class Chunk(
    val chatManager: ChatManager,
    stored: ChunkStored,
    val loadMode: LoadMode
) : Element {
    var storedSize = stored.messages.size

    companion object {
        /**
         * (bytes)
         * Messages can be added until current size less than frontier
         * Chosen in order not to overflow the size of one UDP-datagram
         */
        const val BYTE_SIZE_FRONTIER = 64500
    }

    val id = stored.id
    val messages = stored.messages

    var byteSize = 0
        private set
    init {
        for (message in messages) {
            byteSize += message.byteSize
        }
    }

    val complete: Boolean
        get() = byteSize > BYTE_SIZE_FRONTIER

    fun addMessage(message: Message) {
        (messages as MutableList<Message>).add(message)
        byteSize += message.byteSize
    }

    override suspend fun persist(): Boolean {
        if (!chatManager.persistChunk(ChunkStored(id, complete, messages), loadMode)) {
            return false
        }
        storedSize = messages.size
        return true
    }
}
