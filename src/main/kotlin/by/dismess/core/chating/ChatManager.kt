package by.dismess.core.chating

import by.dismess.core.utils.UniqID
import by.dismess.core.utils.groupID

interface ChatManager {
    suspend fun synchronize()

    val chats: List<Chat>
    suspend fun loadChunk(chatID: UniqID, authorID: UniqID, index: UniqID): MessageChunk =
            loadChunk(groupID(chatID, authorID, index))

    suspend fun loadChunk(chunkID: UniqID): MessageChunk
}
