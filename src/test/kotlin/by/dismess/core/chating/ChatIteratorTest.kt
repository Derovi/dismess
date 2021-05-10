package by.dismess.core.chating

import by.dismess.core.chating.elements.Message
import by.dismess.core.chating.viewing.ChatIterator
import by.dismess.core.chating.viewing.MessageIterator
import by.dismess.core.utils.generateUserID
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.koin.test.KoinTest
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.Date
import kotlin.random.Random

class ChatIteratorTest : KoinTest {

    class MockMessageIterator : MessageIterator {
        private lateinit var messages: MutableList<Message>
        private var index = 0
        override lateinit var value: Message
            private set

        fun setMessages(messages: MutableList<Message>) {
            this.messages = messages
            value = messages[0]
        }

        override suspend fun next(): Boolean {
            if (index + 1 == messages.size) {
                return false
            }
            value = messages[++index]
            return true
        }

        override suspend fun previous(): Boolean {
            if (index == 0) {
                return false
            }
            value = messages[--index]
            return true
        }
    }

    private fun messageList(dates: List<Long>): MutableList<Message> {
        val result = mutableListOf<Message>()
        for (date in dates) {
            val message = Message(
                Date(date),
                generateUserID(date.toString()).rawID,
                generateUserID(date.toString()).rawID,
                date.toString()
            )
            result.add(message)
        }
        return result
    }

    private suspend fun getChatValues(chatIterator: ChatIterator): Pair<MutableList<Long>, MutableList<String>> {
        val dates = mutableListOf<Long>()
        val texts = mutableListOf<String>()
        dates.add(chatIterator.value.date.time)
        texts.add(chatIterator.value.text)
        while (chatIterator.next()) {
            dates.add(chatIterator.value.date.time)
            texts.add(chatIterator.value.text)
        }
        return Pair(dates, texts)
    }

    private fun initMessageIterator(dates: List<Long>): MessageIterator {
        val messages = messageList(dates)
        val msgIterator = MockMessageIterator()
        msgIterator.setMessages(messages)
        return msgIterator
    }

    @Test
    fun testSimple() {
        runBlocking {
            val chatIterator = ChatIterator.create(initMessageIterator(mutableListOf(1, 2, 3, 4)))
            val (dates, texts) = getChatValues(chatIterator)
            Assert.assertArrayEquals(longArrayOf(1, 2, 3, 4), dates.toLongArray())
            Assert.assertArrayEquals(arrayOf("1", "2", "3", "4"), texts.toTypedArray())
        }
    }

    @Test
    fun testSimpleTwoUsers() {
        runBlocking {
            val firstMsg = initMessageIterator(mutableListOf(1, 3, 5, 7))
            val secondMsg = initMessageIterator(mutableListOf(2, 4, 6, 8))
            val chatIterator = ChatIterator.create(firstMsg, secondMsg)
            val (dates, texts) = getChatValues(chatIterator)
            Assert.assertArrayEquals(longArrayOf(1, 2, 3, 4, 5, 6, 7, 8), dates.toLongArray())
            Assert.assertArrayEquals(arrayOf("1", "2", "3", "4", "5", "6", "7", "8"), texts.toTypedArray())
        }
    }

    @Test
    fun testSimplePrevious() {
        runBlocking {
            val firstMsg = initMessageIterator(mutableListOf(1, 3, 5, 7))
            val secondMsg = initMessageIterator(mutableListOf(2, 4, 6, 8))
            val chatIterator = ChatIterator.create(firstMsg, secondMsg)
            val (dates, texts) = getChatValues(chatIterator)
            Assert.assertArrayEquals(longArrayOf(1, 2, 3, 4, 5, 6, 7, 8), dates.toLongArray())
            Assert.assertArrayEquals(arrayOf("1", "2", "3", "4", "5", "6", "7", "8"), texts.toTypedArray())
            dates.clear()
            dates.add(chatIterator.value.date.time)
            while (chatIterator.previous()) {
                dates.add(chatIterator.value.date.time)
            }
            Assert.assertArrayEquals(longArrayOf(1, 2, 3, 4, 5, 6, 7, 8).reversedArray(), dates.toLongArray())
            dates.clear()
            dates.add(chatIterator.value.date.time)
            while (chatIterator.next()) {
                dates.add(chatIterator.value.date.time)
            }
            Assert.assertArrayEquals(longArrayOf(1, 2, 3, 4, 5, 6, 7, 8), dates.toLongArray())
            dates.clear()
            dates.add(chatIterator.value.date.time)
            chatIterator.previous()
            dates.add(chatIterator.value.date.time)
            chatIterator.previous()
            dates.add(chatIterator.value.date.time)
            chatIterator.previous()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            chatIterator.previous()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            chatIterator.previous()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            Assert.assertArrayEquals(longArrayOf(8, 7, 6, 5, 6, 7, 7, 8, 7, 8), dates.toLongArray())
        }
    }

    @Test
    fun testNextPrev() {
        runBlocking {
            val firstMsg = initMessageIterator(mutableListOf(1, 3, 5, 7))
            val secondMsg = initMessageIterator(mutableListOf(2, 4, 6, 8))
            val chatIterator = ChatIterator.create(firstMsg, secondMsg)
            val dates = mutableListOf<Long>()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            chatIterator.previous()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            chatIterator.next()
            dates.add(chatIterator.value.date.time)
            Assert.assertArrayEquals(longArrayOf(1, 2, 3, 4, 3, 4, 5, 6, 7), dates.toLongArray())
        }
    }

    @Test
    fun testStress() {
        runBlocking {
            val numberOfFlows = 15
            val msgIterators = mutableListOf<MessageIterator>()
            val dates = mutableListOf<Long>()
            for (i in 0 until numberOfFlows) {
                val msgDates = MutableList(Random.nextInt(1, 100)) { Random.nextLong(0, 10000) }
                msgDates.sort()
                dates.addAll(dates.size, msgDates)
                msgIterators.add(initMessageIterator(msgDates))
            }
            dates.sort()
            val chatIterator = ChatIterator.create(msgIterators)
            var index = 0
            repeat(10000) {
                val isNext = Random.nextBoolean()
                if (isNext) {
                    index = min(index + 1, dates.lastIndex)
                    chatIterator.next()
                    Assert.assertEquals(dates[index], chatIterator.value.date.time)
                } else {
                    index = max(index - 1, 0)
                    chatIterator.previous()
                    Assert.assertEquals(dates[index], chatIterator.value.date.time)
                }
            }
        }
    }
}
