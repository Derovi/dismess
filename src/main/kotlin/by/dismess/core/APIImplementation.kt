package by.dismess.core

import by.dismess.core.model.Message
import by.dismess.core.model.User
import by.dismess.core.model.UserID
import by.dismess.core.model.attachments.ImageAttachment

class APIImplementation : API {
    override suspend fun registration() {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(userId: UserID, message: Message) {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveAvatar(userId: UserID): ImageAttachment {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveDisplayName(userId: UserID): String {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveUser(userId: UserID): User {
        TODO("Not yet implemented")
    }
}
