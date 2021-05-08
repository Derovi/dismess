package by.dismess.core.chating

import by.dismess.core.chating.elements.Chat
import by.dismess.core.chating.elements.Chunk
import by.dismess.core.dht.DHT
import by.dismess.core.klaxon
import by.dismess.core.services.StorageService
import by.dismess.core.utils.UniqID

class ChatManagerImpl(
        val storageService: StorageService,
        val dht: DHT
) : ChatManager {
    override suspend fun synchronize() {
        for (chat in chats) {
            chat.synchronize()
        }
    }

    override val chats: List<Chat>
        get() = TODO("Not yet implemented")

    override suspend fun loadChunk(chunkID: UniqID): Chunk {
        var result = storageService.load<Chunk>("chunks/$chunkID")
        if (result == null) {
            result = klaxon.parse<Chunk>(String(dht.retrieve("chunks/$chunkID")))
            if (result!!.full) {
                storageService.save("chunks/$chunkID", result)
            }
        }
        return result
    }
}
