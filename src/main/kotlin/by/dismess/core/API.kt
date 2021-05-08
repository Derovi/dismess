package by.dismess.core

import by.dismess.core.chating.Message
import by.dismess.core.model.UserID

interface API {
    suspend fun registration()
    suspend fun sendMessage(userId: UserID, message: Message)
}
