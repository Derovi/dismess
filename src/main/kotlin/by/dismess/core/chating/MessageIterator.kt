package by.dismess.core.chating

import by.dismess.core.chating.elements.Chat
import by.dismess.core.chating.elements.Message
import by.dismess.core.chating.elements.id.MessageID

class MessageIterator(
        val chatManager: ChatManager,
        val chat: Chat,
        val currentID: MessageID
) {
    val value: Message
        get() = TODO("Not implemented yet")

    /**
     * True if cas next
     */
    suspend fun next(): Boolean {
        val currentChunk = chatManager.loadChunk(currentID.chunkID)

    }

    /**
     * True if has previous
     */
    suspend fun previous(): Boolean {

    }
}