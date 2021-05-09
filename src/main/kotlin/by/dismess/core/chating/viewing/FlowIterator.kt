package by.dismess.core.chating.viewing

import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.elements.Chat
import by.dismess.core.chating.elements.Message
import by.dismess.core.chating.elements.id.MessageID

class FlowIterator(
        val chatManager: ChatManager,
        val chat: Chat,
        val currentID: MessageID
) : MessageIterator {
    override val value: Message
        get() = TODO("Not implemented yet")

    override suspend fun next(): Boolean {
        val currentChunk = chatManager.loadChunk(currentID.chunkID)
        if (currentID.index != currentChunk.messages.lastIndex) {
            ++currentID.index
            return true
        }

    }

    override suspend fun previous(): Boolean {

    }
}