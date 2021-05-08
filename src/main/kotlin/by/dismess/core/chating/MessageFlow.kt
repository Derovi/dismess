package by.dismess.core.chating

import by.dismess.core.utils.UniqID

class MessageFlow() {
    var chunkCount: Int = 0
    val chunks = listOf<MessageChunk?>()
    fun getChunk(idx: Int): MessageChunk {
        if (chunks[idx] == null) {

        }
        return chunks[idx]
    }
}
