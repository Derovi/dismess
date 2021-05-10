package by.dismess.core.chating

import by.dismess.core.chating.elements.Chat
import by.dismess.core.chating.elements.Message
import by.dismess.core.chating.elements.id.ChunkID
import by.dismess.core.chating.elements.id.FlowID
import by.dismess.core.chating.elements.stored.ChunkStored
import by.dismess.core.chating.elements.stored.FlowStored
import by.dismess.core.utils.UniqID

interface ChatManager {
    suspend fun synchronize()

    val chats: Map<UniqID, Chat>

    suspend fun sendDirectMessage(userID: UniqID, message: Message): Boolean

    suspend fun loadChunk(chunkID: ChunkID) = loadChunk(chunkID.uniqID)
    suspend fun loadChunk(chunkID: UniqID): ChunkStored?

    suspend fun loadFlow(flowID: FlowID) = loadFlow(flowID.uniqID)
    suspend fun loadFlow(flowID: UniqID): FlowStored?

    suspend fun acceptChunk(chunk: ChunkStored)
    suspend fun persistChunk(chunk: ChunkStored): Boolean

    suspend fun acceptFlow(flow: FlowStored)
    suspend fun persistFlow(flow: FlowStored): Boolean
}
