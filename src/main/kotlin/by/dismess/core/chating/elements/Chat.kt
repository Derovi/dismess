package by.dismess.core.chating.elements

import by.dismess.core.chating.ChatManager
import by.dismess.core.chating.MessageStatus
import by.dismess.core.utils.UniqID

class Chat(val chatManager: ChatManager,
           val id: UniqID,
           val interlocutorID: UniqID) {
    /**
     * Synchronize incomming messages from DHT
     */
    suspend fun synchronize() {
        TODO("Not implemented yet")
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
        TODO("Not implemented yet")
    }
}