package by.dismess.core.managers

import by.dismess.core.model.Invite

interface App {
    suspend fun isRegistered(): Boolean
    suspend fun register(login: String, invite: Invite)
}
