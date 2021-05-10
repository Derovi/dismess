package by.dismess.core.chating.viewing

import by.dismess.core.chating.elements.Message

class ChatIterator(val flows: MutableList<MessageIterator>) : MessageIterator {
    data class MessageInfo(val message: Message, val isEnd: Boolean)

    private val nextLayer: MutableList<MessageInfo> = mutableListOf()
    private val previousLayer: MutableList<MessageInfo> = mutableListOf()
    private var lastNext = true
    private var valueFlowInd = 0
    override lateinit var value: Message
        private set

    companion object {
        private suspend fun nextMessageInfo(flow: MessageIterator): MessageInfo {
            val hasNext = flow.next()
            return MessageInfo(flow.value, !hasNext)
        }

        /**
         * Motivation:
         * Kotlin do not support suspending constructors
         */
        suspend fun create(vararg flows: MessageIterator): ChatIterator {
            return create(flows.toMutableList())
        }

        suspend fun create(flows: MutableList<MessageIterator>): ChatIterator {
            val chatIterator = ChatIterator(flows)
            chatIterator.value = chatIterator.flows[0].value
            var curInd = 0
            for (i in chatIterator.flows.indices) {
                val message = chatIterator.flows[i].value
                if (chatIterator.value.date > message.date) {
                    chatIterator.value = message
                    curInd = i
                }
                val hasPrev = chatIterator.flows[i].previous()
                chatIterator.previousLayer.add(MessageInfo(chatIterator.flows[i].value, !hasPrev))
                if (hasPrev) {
                    chatIterator.nextLayer.add(nextMessageInfo(chatIterator.flows[i]))
                } else {
                    chatIterator.nextLayer.add(MessageInfo(chatIterator.flows[i].value, false))
                }
            }
            chatIterator.valueFlowInd = curInd
            val hasPrev = chatIterator.flows[curInd].previous()
            chatIterator.previousLayer[curInd] = MessageInfo(chatIterator.flows[curInd].value, !hasPrev)
            if (hasPrev) {
                chatIterator.flows[curInd].next()
            }
            chatIterator.nextLayer[curInd] = nextMessageInfo(chatIterator.flows[curInd])
            return chatIterator
        }
    }

    override suspend fun next(): Boolean {
        if (!lastNext) {
            lastNext = true
            flows[valueFlowInd].next()
            for (flow in flows) {
                if (flow.value.date <= value.date) {
                    flow.next()
                }
            }
        }
        var nextInd: Int? = null
        for (i in nextLayer.indices) {
            if (nextLayer[i].isEnd) {
                continue
            }
            if (nextInd == null || nextLayer[i].message.date < nextLayer[nextInd].message.date) {
                nextInd = i
            }
        }
        if (nextInd == null) {
            return false
        }
        previousLayer[valueFlowInd] = MessageInfo(value, false)
        valueFlowInd = nextInd
        value = nextLayer[nextInd].message
        val hasNext = flows[nextInd].next()
        nextLayer[nextInd] = MessageInfo(flows[nextInd].value, !hasNext)
        return true
    }

    override suspend fun previous(): Boolean {
        if (lastNext) {
            lastNext = false
            flows[valueFlowInd].previous()
            for (flow in flows) {
                if (flow.value.date >= value.date) {
                    flow.previous()
                }
            }
        }
        var prevInd: Int? = null
        for (i in previousLayer.indices) {
            if (previousLayer[i].isEnd) {
                continue
            }
            if (prevInd == null || previousLayer[i].message.date > previousLayer[prevInd].message.date) {
                prevInd = i
            }
        }
        if (prevInd == null) {
            return false
        }
        nextLayer[valueFlowInd] = MessageInfo(value, false)
        valueFlowInd = prevInd
        value = previousLayer[prevInd].message
        val hasPrev = flows[prevInd].previous()
        previousLayer[prevInd] = MessageInfo(flows[prevInd].value, !hasPrev)
        return true
    }
}
