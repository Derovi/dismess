package by.dismess.core.chating.viewing

import by.dismess.core.chating.elements.Message
import java.util.Date

class ChatIterator(val flows: MutableList<MessageIterator>) : MessageIterator {
    data class MessageInfo(val message: Message, val isEnd: Boolean)

    private val nextLayer: MutableList<MessageInfo> = mutableListOf()
    private val previousLayer: MutableList<MessageInfo> = mutableListOf()
    private var lastActionIsNext = true
    private var valueFlowInd = 0
    override lateinit var value: Message
        private set

    companion object {
        private suspend fun nextMessageInfo(flow: MessageIterator): MessageInfo {
            val hasNext = flow.next()
            return MessageInfo(flow.value, !hasNext)
        }

        private fun flowLess(dateLhs: Date, dateRhs: Date, indLhs: Int, indRhs: Int): Boolean {
            return dateLhs < dateRhs || (dateLhs == dateRhs && indLhs < indRhs)
        }

        private fun flowGreater(dateLhs: Date, dateRhs: Date, indLhs: Int, indRhs: Int): Boolean {
            return dateLhs > dateRhs || (dateLhs == dateRhs && indLhs > indRhs)
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
        if (!lastActionIsNext) {
            lastActionIsNext = true
            if (flows[valueFlowInd].value != value) {
                flows[valueFlowInd].next()
            }
            flows[valueFlowInd].next()
            for (i in flows.indices) {
                if (flowLess(flows[i].value.date, value.date, i, valueFlowInd)) {
                    flows[i].next()
                }
            }
        }
        var nextInd: Int? = null
        for (i in nextLayer.indices) {
            if (nextLayer[i].isEnd) {
                continue
            }
            if (nextInd == null ||
                flowLess(nextLayer[i].message.date, nextLayer[nextInd].message.date, i, nextInd)
            ) {
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
        if (lastActionIsNext) {
            lastActionIsNext = false
            if (flows[valueFlowInd].value != value) {
                flows[valueFlowInd].previous()
            }
            flows[valueFlowInd].previous()
            for (i in flows.indices) {
                if (flowGreater(flows[i].value.date, value.date, i, valueFlowInd)) {
                    flows[i].previous()
                }
            }
        }
        var prevInd: Int? = null
        for (i in previousLayer.indices) {
            if (previousLayer[i].isEnd) {
                continue
            }
            if (prevInd == null ||
                flowGreater(previousLayer[i].message.date, previousLayer[prevInd].message.date, i, prevInd)
            ) {
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
