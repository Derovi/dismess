package by.dismess.core.managers

import by.dismess.core.model.User
import by.dismess.core.model.UserID

interface UserManager {
    suspend fun isOnline(userId: UserID): Boolean

    suspend fun sendPost(target: UserID, tag: String, data: Any, timeout: Long = 1000): Boolean
    suspend fun sendPost(target: UserID, tag: String, timeout: Long = 1000): Boolean
    suspend fun sendPost(target: UserID, tag: String, data: String, timeout: Long = 1000): Boolean

    suspend fun sendGet(target: UserID, tag: String, data: Any, timeout: Long = 1000): String?
    suspend fun sendGet(target: UserID, tag: String, timeout: Long = 1000): String?
    suspend fun sendGet(target: UserID, tag: String, data: String, timeout: Long = 1000): String?

    suspend fun retrieveUser(userId: UserID): User
    suspend fun retrieveUserNoAvatar(userId: UserID): User // retrieve user without avatar
}
