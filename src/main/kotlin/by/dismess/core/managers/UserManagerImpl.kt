package by.dismess.core.managers

import by.dismess.core.model.User
import by.dismess.core.model.UserID

class UserManagerImpl : UserManager {
    override suspend fun isOnline(userId: UserID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveUser(userId: UserID): User {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveUserNoAvatar(userId: UserID): User {
        TODO("Not yet implemented")
    }
}
