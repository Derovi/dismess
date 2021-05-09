package by.dismess.core.chating.elements

import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.elements.id.ChunkID
import by.dismess.core.chating.elements.stored.ChunkStored
import by.dismess.core.chating.elements.stored.FlowStored

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

    val id
        get() = stored.id

    suspend fun chunkAt(idx: Int): Chunk? {
        chunks as MutableList<Chunk?> // gives access to change list
        if (idx !in chunks.indices) {
            return null
        }
        if (chunks[idx] == null) {
            chunks[idx] = chatManager.loadChunk(ChunkID(id, idx))
        }
        return chunks[idx]
    }

    suspend fun addMessage(message: Message) {
        chunks as MutableList<Chunk?> // gives access to change list
        if (chunks.isEmpty() || chunkAt(chunks.lastIndex)!!.complete) {
            chunks.add(Chunk(ChunkStored(listOf(message))))
        }
    }

    override suspend fun accept() {
        if (chunks.size == stored.chunkCount) {
            return
        }
        for (idx in stored.chunkCount until chunks.size) {
            chunks[idx]?.accept()
        }
        val newStored = FlowStored(id, chunks.size)
        chatManager.acceptFlow(newStored)
    }

    override suspend fun persist(): Boolean {
        if (chunks.size == stored.chunkCount) {
            return true
        }
        for (idx in stored.chunkCount until chunks.size) {
            if (!chunks[idx]!!.persist()) {
                return false
            }
        }
        val newStored = FlowStored(id, chunks.size)
        return chatManager.persistFlow(newStored)
    }
}
