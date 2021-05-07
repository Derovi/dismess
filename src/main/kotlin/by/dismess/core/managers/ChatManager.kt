package by.dismess.core.managers

import by.dismess.core.model.Chat
import by.dismess.core.model.Message
import by.dismess.core.utils.UniqID

interface ChatManager {
    fun synchronize()

    fun sendMessage(user: UniqID, message: Message)

    val chats: List<Chat>
}
