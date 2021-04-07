package by.dismess.core.managers

import by.dismess.core.model.User
import by.dismess.core.model.UserID

interface UserManager {
    suspend fun isOnline(userId: UserID): Boolean
    suspend fun retrieveUser(userId: UserID): User
    suspend fun retrieveUserNoAvatar(userId: UserID): User // retrieve user without avatar
}
