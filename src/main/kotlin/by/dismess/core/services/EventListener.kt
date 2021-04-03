package by.dismess.core.services

import by.dismess.core.model.Message

interface EventListener {
    fun onMessageReceived(message: Message)
}
