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
        val description: FlowStored
) : Storable {
    var globalCount = description.chunkCount
    var localCount = description.chunkCount

    val chunks = List<Chunk?>(description.chunkCount) { null }

    val id
        get() = description.id

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

    override suspend fun store() {
        if (chunks.size == localCount) {
            return
        }
        for (idx in localCount until chunks.size) {
            chunks[idx]?.store()
        }
        localCount = chunks.size
        chatManager.storeFlow(FlowStored(id, localCount))
    }

    override suspend fun publish(): Boolean {
        if (chunks.size == globalCount) {
            return true
        }
        store()
        for (idx in globalCount until chunks.size) {
            if (chunks[idx]!!.publish()) {
                return false
            }
        }
        globalCount = chunks.size
        return chatManager.publishFlow(FlowStored(id, globalCount))
    }
}
