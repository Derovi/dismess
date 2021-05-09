package by.dismess.core.chating.elements

import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.MessageStatus
import by.dismess.core.chating.elements.id.FlowID
import by.dismess.core.utils.UniqID
import java.lang.Exception

/**
 * Represents dialog
 * Use @param lastMessage to look through chat
 * @see MessageIterator
 * All messages are stored in Flows. (One Flow for each chat member)
 * @see Flow
 */
class Chat(val ownID: UniqID,
           val chatManager: ChatManager,
           val id: UniqID,
           val otherID: UniqID) {

    lateinit var ownFlow: Flow
    lateinit var otherFlow: Flow

    /**
     * Synchronize incomming messages from DHT
     */
    suspend fun synchronize() {
        ownFlow = chatManager.loadFlow(FlowID(id, ownID)) ?: throw Exception("Can't load own flow")
        otherFlow = chatManager.loadFlow(FlowID(id, otherID)) ?: throw Exception("Can't load other flow")
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
        
    }
}