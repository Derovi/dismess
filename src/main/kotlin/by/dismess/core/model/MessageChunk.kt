package by.dismess.core.model

data class MessageChunk(val messages: List<Message>) {
    companion object {
        /**
         * (bytes)
         * Messages can be added until current size less than frontier
         * Chosen in order not to overflow the size of one UDP-datagram
         */
        const val BYTE_SIZE_FRONTIER = 64500
    }

    var byteSize = 0
        private set
    init {
        for (message in messages) {
            byteSize += message.byteSize
        }
    }

    val full: Boolean
        get() = byteSize < BYTE_SIZE_FRONTIER

    fun addMessage(message: Message) {
        (messages as MutableList<Message>).add(message)
        byteSize += message.byteSize
    }
}
