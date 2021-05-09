class AppImpl(
        dht: DHT,
        dataManager: DataManager
) : App {
    override suspend fun register(login: String, invite: Invite) = runBlocking {
        GlobalScope.launch { dataManager.saveLogin(login) }
        GlobalScopy.launch { dht.remember(invite.users) }
    }

    override suspend fun isRegistered(): Boolean = dataManager.getLogin() != null
}
