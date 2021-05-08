package by.dismess.core.chating.elements

/**
 * Flow represents container for all
 * messages in chat that belong to a specific chat member.
 *
 * Messages in flow are divided into Chunks
 * @see Chunk
 */
class Flow {
    val chunks = listOf<Chunk?>()
    fun getChunk(idx: Int): Chunk {
        if (chunks[idx] == null) {

        }
        return chunks[idx]
    }
}
