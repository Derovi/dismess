package by.dismess.core.chating

import by.dismess.core.chating.elements.Chat
import by.dismess.core.chating.elements.KeyMessage
import by.dismess.core.chating.elements.Message
import by.dismess.core.chating.elements.id.ChunkID
import by.dismess.core.chating.elements.id.FlowID
import by.dismess.core.chating.elements.stored.ChunkStored
import by.dismess.core.chating.elements.stored.FlowStored
import by.dismess.core.security.Encryptor
import by.dismess.core.utils.UniqID
import java.util.concurrent.ConcurrentHashMap

interface ChatManager {
    suspend fun synchronize()

    val chats: Map<UniqID, Chat>
    val encryptors: ConcurrentHashMap<UniqID, Encryptor>

    suspend fun load()
    suspend fun startChat(userID: UniqID, message: Message): Chat?

    suspend fun sendDirectMessage(userID: UniqID, message: Message): Boolean
    suspend fun sendKey(userID: UniqID, key: KeyMessage): Boolean

    suspend fun loadChunk(chunkID: ChunkID, loadMode: LoadMode) =
        loadChunk(chunkID.uniqID, chunkID.flowID.chatID, loadMode)
    suspend fun loadChunk(chunkID: UniqID, chatID: UniqID, loadMode: LoadMode): ChunkStored?

    suspend fun loadFlow(flowID: FlowID, loadMode: LoadMode) = loadFlow(flowID.uniqID, loadMode)
    suspend fun loadFlow(flowID: UniqID, loadMode: LoadMode): FlowStored?

    suspend fun persistChunk(chunk: ChunkStored, loadMode: LoadMode): Boolean
    suspend fun persistFlow(flow: FlowStored, loadMode: LoadMode): Boolean
}
