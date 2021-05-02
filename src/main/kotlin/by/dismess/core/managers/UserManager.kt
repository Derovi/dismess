package by.dismess.core.managers

import by.dismess.core.model.User
import by.dismess.core.model.UserID
import by.dismess.core.network.NetworkMessage

interface UserManager {
    suspend fun isOnline(userId: UserID): Boolean

    /**
     * Send one-directional message
     */
    suspend fun sendNetworkMessage(
        userID: UserID,
        message: NetworkMessage,
        userStatusChanged: ((UserStatus) -> Unit)? = null
    ): Boolean

    suspend fun retrieveUser(userId: UserID): User
    suspend fun retrieveUserNoAvatar(userId: UserID): User // retrieve user without avatar
}
