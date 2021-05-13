package by.dismess.core.chating.elements

import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.elements.id.ChunkID
import by.dismess.core.chating.elements.id.MessageID
import by.dismess.core.chating.elements.stored.ChunkStored
import by.dismess.core.chating.elements.stored.FlowStored
import java.lang.Integer.max

/**
 * Flow represents container for all
 * messages in chat that belong to a specific chat member.
 *
 * Messages in flow are divided into Chunks
 * @see Chunk
 */
class Flow(
    val chatManager: ChatManager,
    var stored: FlowStored
) : Element {

    val chunks = List<Chunk?>(stored.chunkCount) { null }
    lateinit var lastMessage: MessageID // TODO("Not yet implemented")
    val id
        get() = stored.id

    suspend fun chunkAt(idx: Int): Chunk? {
        chunks as MutableList<Chunk?> // gives access to change list
        if (idx !in chunks.indices) {
            return null
        }
        if (chunks[idx] == null) {
            chunks[idx] = Chunk(
                chatManager,
                chatManager.loadChunk(ChunkID(id, idx)) ?: return null
            )
        }
        return chunks[idx]
    }

    suspend fun addMessage(message: Message) {
        chunks as MutableList<Chunk?> // gives access to change list
        if (chunks.isEmpty() || chunkAt(chunks.lastIndex)!!.complete) {
            chunks.add(Chunk(chatManager, ChunkStored(ChunkID(id, chunks.size), listOf(message))))
        } else {
            chunks.last()!!.addMessage(message)
        }
    }

    override suspend fun accept() {
        for (idx in max(0, stored.chunkCount - 1) until chunks.size) {
            chunks[idx]?.accept()
        }
        if (stored.chunkCount == chunks.size) {
            return
        }
        val newStored = FlowStored(id, chunks.size)
        chatManager.acceptFlow(newStored)
    }

    override suspend fun persist(): Boolean {
        for (idx in max(0, stored.chunkCount - 1) until chunks.size) {
            if (!chunks[idx]!!.persist()) {
                return false
            }
        }
        if (stored.chunkCount == chunks.size) {
            return true
        }
        val newStored = FlowStored(id, chunks.size)
        return chatManager.persistFlow(newStored)
    }
}
