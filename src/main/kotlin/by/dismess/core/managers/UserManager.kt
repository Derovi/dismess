package by.dismess.core.managers

import by.dismess.core.model.User
import by.dismess.core.chating.attachments.ImageAttachment
import by.dismess.core.utils.UniqID

interface UserManager {
    suspend fun sendPost(target: UniqID, tag: String, data: Any, timeout: Long = 1000): Boolean
    suspend fun sendPost(target: UniqID, tag: String, timeout: Long = 1000): Boolean
    suspend fun sendPost(target: UniqID, tag: String, data: String, timeout: Long = 1000): Boolean

    suspend fun sendGet(target: UniqID, tag: String, data: Any, timeout: Long = 1000): String?
    suspend fun sendGet(target: UniqID, tag: String, timeout: Long = 1000): String?
    suspend fun sendGet(target: UniqID, tag: String, data: String, timeout: Long = 1000): String?

    suspend fun isOnline(userId: UniqID): Boolean
    suspend fun retrieveUser(userId: UniqID): User?
    suspend fun retrieveAvatar(userId: UniqID): ImageAttachment? // retrieve user without avatar
}
