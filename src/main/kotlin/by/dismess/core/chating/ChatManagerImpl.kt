package by.dismess.core.chating

import by.dismess.core.chating.elements.Chat
import by.dismess.core.chating.elements.Chunk
import by.dismess.core.chating.elements.KeyMessage
import by.dismess.core.chating.elements.Message
import by.dismess.core.chating.elements.stored.ChatListStored
import by.dismess.core.chating.elements.stored.ChunkStored
import by.dismess.core.chating.elements.stored.FlowStored
import by.dismess.core.dht.DHT
import by.dismess.core.events.EventBus
import by.dismess.core.events.MessageEvent
import by.dismess.core.managers.DataManager
import by.dismess.core.managers.UserManager
import by.dismess.core.security.Encryptor
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import by.dismess.core.utils.UniqID
import by.dismess.core.utils.gson
import by.dismess.core.utils.randomUniqID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.ConcurrentHashMap

@ExperimentalCoroutinesApi
class ChatManagerImpl(
    val dataManager: DataManager,
    val userManager: UserManager,
    val networkService: NetworkService,
    val storageService: StorageService,
    val eventBUS: EventBus,
    val dht: DHT
) : ChatManager {
    class StartChatMessage(val chatID: UniqID, val senderID: UniqID)

    override val encryptors: ConcurrentHashMap<UniqID, Encryptor> = ConcurrentHashMap()

    override suspend fun startChat(userID: UniqID): Chat? {
        val chatID = randomUniqID()
        val ownID = dataManager.getId()
        if (!userManager.sendPost(userID, "Chats/Start", StartChatMessage(chatID, ownID))) {
            return null
        }
        val chat = Chat(chatID, ownID, userID, this)
        chat.synchronize()
        (chats as MutableMap<UniqID, Chat>)[chatID] = chat
        return chat
    }

    override val chats: Map<UniqID, Chat> = mutableMapOf()

    override suspend fun sendDirectMessage(userID: UniqID, message: Message): Boolean =
        userManager.sendPost(userID, "Chats/Send", message)

    override suspend fun sendKey(userID: UniqID, key: KeyMessage): Boolean =
        userManager.sendPost(userID, "Chats/Key", key)

    override suspend fun loadChunk(chunkID: UniqID, chatID: UniqID, loadMode: LoadMode): ChunkStored? {
        var result = storageService.load<ChunkStored>("chunks/$chunkID")
        if (loadMode == LoadMode.OTHER && (result == null || !result.complete)) {
            result = gson.fromJson(String(dht.retrieve("chunks/$chunkID") ?: return null), ChunkStored::class.java) ?: return null
        }
        return result
    }

    override suspend fun loadFlow(flowID: UniqID, loadMode: LoadMode): FlowStored? {
        return if (loadMode == LoadMode.OWN) {
            storageService.load("flows/$flowID")
        } else {
            val result = gson.fromJson(String(dht.retrieve("flows/$flowID") ?: return null), FlowStored::class.java)
            println(result)
            result
        }
    }

    override suspend fun persistChunk(chunk: ChunkStored, loadMode: LoadMode): Boolean {
        return if (loadMode == LoadMode.OTHER) {
            storageService.save("chunks/${chunk.id.uniqID}", chunk)
            true
        } else {
            storageService.save("chunks/${chunk.id.uniqID}", chunk)
            dht.store("chunks/${chunk.id.uniqID}", gson.toJson(chunk).toByteArray())
        }
    }

    override suspend fun persistFlow(flow: FlowStored, loadMode: LoadMode): Boolean {
        return if (loadMode == LoadMode.OTHER) {
            storageService.save("flows/${flow.id.uniqID}", flow)
            true
        } else {
            storageService.save("flows/${flow.id.uniqID}", flow)
            dht.store("flows/${flow.id.uniqID}", gson.toJson(flow).toByteArray())
        }
    }

    private fun registerHandlers() {
        networkService.registerPost("Chats/Send") {
            it.data ?: return@registerPost
            val message = gson.fromJson(it.data!!, Message::class.java) ?: return@registerPost
            println("got" + message.text)
            val chat = chats[message.chatID] ?: return@registerPost
            chat.otherFlow.addMessage(message)
            chat.otherFlow.persist() // TODO optimize
            eventBUS.callEvent(MessageEvent(message))
        }
        networkService.registerPost("Chats/Key") {
            it.data ?: return@registerPost
            val key = gson.fromJson(it.data!!, KeyMessage::class.java) ?: return@registerPost
            val encryptor = encryptors[key.chatID] ?: return@registerPost
            encryptor.updateKey()
            val updated = encryptor.setReceiverPublicKey(key.key)
            if (key.sendBack) {
                val backKey = KeyMessage(
                    encryptor.publicKeyBytes(!updated),
                    key.chatID,
                    key.senderID,
                    !updated
                )
                sendKey(key.senderID, backKey)
            }
        }
        networkService.registerPost("Chats/Start") {
            it.data ?: return@registerPost
            val message = gson.fromJson(it.data!!, StartChatMessage::class.java) ?: return@registerPost
            val chat = Chat(message.chatID, dataManager.getId(), message.senderID, this)
            chat.synchronize()
            (chats as MutableMap<UniqID, Chat>)[message.chatID] = chat
        }
    }

    override suspend fun load() {
        registerHandlers()
        loadChats()
    }

    private suspend fun loadChats() {
        chats as MutableMap<UniqID, Chat> // access to chat list mutation
        val ownID = dataManager.getId()
        val chatListStored = storageService.load<ChatListStored>("Chats/List") ?: ChatListStored()
        for (chatStored in chatListStored.chatsID) {
            var otherID: UniqID? = null
            for (memberID in chatStored.membersID) {
                if (memberID != ownID) {
                    otherID = memberID
                    break
                }
            }
            if (otherID == null) {
                continue
            }
            val chat = Chat(chatStored.id, ownID, otherID, this)
            chat.synchronize()
            chats[chatStored.id] = chat
        }
    }
}
