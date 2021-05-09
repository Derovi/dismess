package by.dismess.core.chating

import by.dismess.core.chating.elements.Chat
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

    override suspend fun loadChunk(chunkID: UniqID): ChunkStored? {
        var result = storageService.load<ChunkStored>("chunks/$chunkID")
        if (result == null) {
            result = klaxon.parse<ChunkStored>(String(dht.retrieve("chunks/$chunkID"))) ?: return null
        }
        return result
    }

    override suspend fun loadFlow(flowID: UniqID): FlowStored? {
        var result = storageService.load<FlowStored>("flows/$flowID")
        if (result == null) {
            result = klaxon.parse<FlowStored>(String(dht.retrieve("flows/$flowID")))
        }
        return result
    }

    override suspend fun acceptChunk(chunk: ChunkStored) {
        storageService.save("chunks/${chunk.id.uniqID}", chunk)
    }

    override suspend fun persistChunk(chunk: ChunkStored): Boolean =
        dht.store("chunks/${chunk.id.uniqID}", klaxon.toJsonString(chunk).toByteArray())

    override suspend fun acceptFlow(flow: FlowStored) {
        storageService.save("flows/${flow.id.uniqID}", flow)
    }

    override suspend fun persistFlow(flow: FlowStored): Boolean =
        dht.store("flows/${flow.id.uniqID}", klaxon.toJsonString(flow).toByteArray())
}
