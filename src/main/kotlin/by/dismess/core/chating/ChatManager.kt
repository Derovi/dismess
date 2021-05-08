package by.dismess.core.chating

import by.dismess.core.chating.elements.Chat
import by.dismess.core.chating.elements.Chunk
import by.dismess.core.chating.elements.id.ChunkID
import by.dismess.core.utils.UniqID

interface ChatManager {
    suspend fun synchronize()

    val chats: List<Chat>

    suspend fun loadChunk(chunkID: ChunkID) = loadChunk(chunkID.uniqID)

    suspend fun loadChunk(chunkID: UniqID): Chunk
}
