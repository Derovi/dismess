package by.dismess.core.chating.viewing

import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.elements.Chunk
import by.dismess.core.chating.elements.Flow
import by.dismess.core.chating.elements.Message
import by.dismess.core.chating.elements.id.ChunkID
import by.dismess.core.chating.elements.id.MessageID

class FlowIterator private constructor(
        val chatManager: ChatManager,
        val flow: Flow,
        var messageID: MessageID
) : MessageIterator {
    lateinit var currentChunk: Chunk
        private set

    companion object {
        /**
         * Motivation:
         * Kotlin do not support suspending constructors
         */
        suspend fun create(chatManager: ChatManager,
                           flow: Flow,
                           messageID: MessageID): FlowIterator =
                FlowIterator(chatManager, flow, messageID).also {
                    it.currentChunk = flow.chunkAt(messageID.chunkID.index)
                            ?: throw ExceptionInInitializerError("Can't load initial chunk")
                }
    }

    override val value: Message
        get() = currentChunk.messages[messageID.index]

    override suspend fun next(): Boolean {
        if (messageID.index < currentChunk.messages.lastIndex) {
            messageID = MessageID(messageID.chunkID, messageID.index + 1)
            return true
        }
        if (messageID.chunkID.index < flow.chunks.lastIndex) {
            val newChunkID = ChunkID(messageID.chunkID.flowID, messageID.chunkID.index + 1)
            currentChunk = flow.chunkAt(newChunkID.index) ?: return false
            messageID = MessageID(newChunkID, 0)
        }
        return false
    }

    override suspend fun previous(): Boolean {
        if (messageID.index > 0) {
            messageID = MessageID(messageID.chunkID, messageID.index - 1)
            return true
        }
        if (messageID.chunkID.index > 0) {
            val newChunkID = ChunkID(messageID.chunkID.flowID, messageID.chunkID.index - 1)
            currentChunk = flow.chunkAt(newChunkID.index) ?: return false
            messageID = MessageID(newChunkID, currentChunk.messages.lastIndex)
        }
        return false
    }
}
