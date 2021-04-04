package by.dismess.core.outer

import by.dismess.core.model.Message

interface EventListener {
    fun onMessageReceived(message: Message)
}
