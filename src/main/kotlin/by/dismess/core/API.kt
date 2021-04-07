package by.dismess.core

import by.dismess.core.model.Message
import by.dismess.core.model.User
import by.dismess.core.model.UserID
import by.dismess.core.model.attachments.ImageAttachment

interface API {
    suspend fun registration()
    suspend fun sendMessage(userId: UserID, message: Message)
    suspend fun retrieveAvatar(userId: UserID): ImageAttachment
    suspend fun retrieveDisplayName(userId: UserID): String
    suspend fun retrieveUser(userId: UserID): User
}
