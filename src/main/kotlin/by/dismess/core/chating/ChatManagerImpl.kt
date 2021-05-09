package by.dismess.core.chating

import by.dismess.core.chating.elements.Chat
import by.dismess.core.chating.elements.Chunk
import by.dismess.core.chating.elements.Flow
import by.dismess.core.chating.elements.Message
import by.dismess.core.chating.elements.stored.ChunkStored
import by.dismess.core.chating.elements.stored.FlowStored
import by.dismess.core.dht.DHT
import by.dismess.core.events.EventBus
import by.dismess.core.events.MessageEvent
import by.dismess.core.klaxon
import by.dismess.core.managers.UserManager
import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import by.dismess.core.utils.UniqID

class ChatManagerImpl(
        val userManager: UserManager,
        val networkService: NetworkService,
        val storageService: StorageService,
        val eventBUS: EventBus,
        val dht: DHT
) : ChatManager {
    init {
        networkService.registerPost("Chats/Send") {
            it.data ?: return@registerPost
            val message = klaxon.parse<Message>(it.data!!) ?: return@registerPost
            val chat = chats[message.chatID] ?: return@registerPost
            chat.otherFlow.addMessage(message)
            chat.otherFlow.accept() // TODO optimize
            eventBUS.callEvent(MessageEvent(message))
        }
    }

    override suspend fun synchronize() {
        for (chat in chats.values) {
            chat.synchronize()
        }
    }

    override val chats: Map<UniqID, Chat>
        get() = TODO("Not yet implemented")

    override suspend fun sendDirectMessage(userID: UniqID, message: Message): Boolean =
        userManager.sendPost(UserID(userID), "Chats/Send", message)

    override suspend fun loadChunk(chunkID: UniqID): Chunk? {
        var result = storageService.load<Chunk>("chunks/$chunkID")
        if (result == null) {
            result = klaxon.parse<Chunk>(String(dht.retrieve("chunks/$chunkID"))) ?: return null
            if (result) {
                storageService.save("chunks/$chunkID", result)
            }
        }
        return result
    }

    override suspend fun loadFlow(flowID: UniqID): Flow? {
        var result = storageService.load<Flow>("flows/$flowID")
        if (result == null) {
            result = klaxon.parse<Flow>(String(dht.retrieve("flows/$flowID")))
        }
        return result
    }

    override suspend fun acceptChunk(chunk: ChunkStored) {
        TODO("Not yet implemented")
    }

    override suspend fun persistChunk(chunk: ChunkStored): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun acceptFlow(flow: FlowStored) {
        TODO("Not yet implemented")
    }

    override suspend fun persistFlow(flow: FlowStored): Boolean {
        TODO("Not yet implemented")
    }
}
