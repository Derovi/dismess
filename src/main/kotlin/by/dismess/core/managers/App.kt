package by.dismess.core.managers

interface App {
    suspend fun isRegistered(): Boolean
    suspend fun register(login: String, invite: Invite)
}
