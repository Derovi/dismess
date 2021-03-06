package by.dismess.core.chating.elements

import by.dismess.core.chating.Attachment
import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.LoadMode
import by.dismess.core.chating.MessageStatus
import by.dismess.core.chating.elements.id.FlowID
import by.dismess.core.chating.elements.stored.FlowStored
import by.dismess.core.chating.viewing.ChatIterator
import by.dismess.core.chating.viewing.FlowIterator
import by.dismess.core.chating.viewing.MessageIterator
import by.dismess.core.security.Encryptor
import by.dismess.core.utils.UniqID
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Represents dialog
 * Use @param lastMessage to look through chat
 * @see MessageIterator
 * All messages are stored in Flows. (One Flow for each chat member)
 * @see Flow
 */
class Chat(
    val id: UniqID,
    val ownID: UniqID,
    val otherID: UniqID,
    val chatManager: ChatManager
) {

    private val encryptor = Encryptor()
    private val wasOffline = AtomicBoolean(false)
    lateinit var ownFlow: Flow
    lateinit var otherFlow: Flow

    init {
        chatManager.encryptors[id] = encryptor
    }

    private suspend fun exchangeKey() {
        coroutineScope {
            val keyMessage = KeyMessage(
                encryptor.publicKeyBytes(true),
                id,
                ownID,
                true
            )
            launch { chatManager.sendKey(otherID, keyMessage) }
        }
    }

    private suspend fun tryInitEncryptor() {
        if (encryptor.isInitialized()) {
            return
        }
        exchangeKey()
        encryptor.block()
    }

    /**
     * Synchronize incomming messages from DHT
     */
    suspend fun synchronize() {
        val ownFlowID = FlowID(id, ownID)
        val otherFlowID = FlowID(id, otherID)
        ownFlow = Flow(
            chatManager,
            chatManager.loadFlow(ownFlowID, LoadMode.OWN) ?: FlowStored(ownFlowID, 0),
            LoadMode.OWN
        )
        otherFlow = Flow(
            chatManager,
            chatManager.loadFlow(FlowID(id, otherID), LoadMode.OTHER) ?: FlowStored(otherFlowID, 0),
            LoadMode.OTHER
        )
    }

    /**
     * @return MessageStatus - can be SENT, DELIVERED or ERROR
     * DELIVERED if receiver online for sender and received message
     * SENT if receiver offline for sender and message successfully stored in DHT
     * ERROR if both receiver and DHT offline for sender
     *
     * READ status can be received by event
     */
    suspend fun sendMessage(message: Message): MessageStatus {
        var status = MessageStatus.ERROR
        runBlocking {
            //        tryInitEncryptor()
            launch { ownFlow.addMessage(message) }
            val persistSuccessful = async { ownFlow.persist() } // TODO optimize
            val directSuccessful = async { chatManager.sendDirectMessage(otherID, message) }
            if (directSuccessful.await()) {
                status = MessageStatus.DELIVERED
                if (wasOffline.getAndSet(false)) {
                    exchangeKey()
                }
            } else if (persistSuccessful.await()) {
                status = MessageStatus.SENT
                wasOffline.set(true)
            }
        }
        return status
    }

    suspend fun sendMessage(text: String, attachments: MutableList<Attachment> = mutableListOf()) {
        sendMessage(
            Message(
                Date(), id, ownID, text, attachments
            )
        )
    }

    val lastMessage: MessageIterator
        get() = runBlocking {
            ChatIterator.create(
                FlowIterator.create(chatManager, ownFlow, ownFlow.lastMessage!!),
                FlowIterator.create(chatManager, otherFlow, otherFlow.lastMessage!!)
            )
        }
}
