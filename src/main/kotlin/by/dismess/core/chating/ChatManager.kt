package by.dismess.core.chating

import by.dismess.core.chating.elements.Chat
import by.dismess.core.chating.elements.Chunk
import by.dismess.core.chating.elements.id.ChunkID
import by.dismess.core.chating.elements.id.FlowID
import by.dismess.core.chating.elements.stored.ChunkStored
import by.dismess.core.chating.elements.stored.FlowStored
import by.dismess.core.utils.UniqID

interface ChatManager {
    suspend fun synchronize()

    val chats: Map<UniqID, Chat>

    suspend fun loadChunk(chunkID: ChunkID) = loadChunk(chunkID.uniqID)
    suspend fun loadChunk(chunkID: UniqID): Chunk?

    suspend fun loadFlow(flowID: FlowID) = loadFlow(flowID.uniqID)
    suspend fun loadFlow(flowID: UniqID): FlowStored?

    suspend fun storeChunk(chunk: ChunkStored)
    suspend fun publishChunk(chunk: ChunkStored): Boolean

    suspend fun storeFlow(flow: FlowStored)
    suspend fun publishFlow(flow: FlowStored): Boolean
}
