package by.dismess.core.chating.elements

import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.LoadMode
import by.dismess.core.chating.elements.id.ChunkID
import by.dismess.core.chating.elements.id.MessageID
import by.dismess.core.chating.elements.stored.ChunkStored
import by.dismess.core.chating.elements.stored.FlowStored
import kotlinx.coroutines.runBlocking
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
    var stored: FlowStored,
    val loadMode: LoadMode
) : Element {

    val chunks = List<Chunk?>(stored.chunkCount) { null }
    val lastMessage: MessageID?
        get() = runBlocking {
            if (chunks.isEmpty()) null else
                MessageID(chunkAt(chunks.lastIndex)!!.id, chunkAt(chunks.lastIndex)!!.messages.lastIndex)
        }

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
                chatManager.loadChunk(ChunkID(id, idx), loadMode) ?: return null,
                loadMode
            )
        }
        return chunks[idx]
    }

    suspend fun addMessage(message: Message) {
        chunks as MutableList<Chunk?> // gives access to change list
        if (chunks.isEmpty() || chunkAt(chunks.lastIndex)!!.complete) {
            chunks.add(Chunk(chatManager, ChunkStored(ChunkID(id, chunks.size), false, mutableListOf(message)), loadMode))
        } else {
            chunks.last()!!.addMessage(message)
        }
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
        return chatManager.persistFlow(newStored, loadMode)
    }
}
