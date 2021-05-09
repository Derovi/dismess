import by.dismess.core.dht.DHT
import by.dismess.core.managers.App
import by.dismess.core.managers.DataManager
import by.dismess.core.model.Invite
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AppImpl(
        val dht: DHT,
        val dataManager: DataManager
) : App {
    override suspend fun register(login: String, invite: Invite): Unit = coroutineScope {
        GlobalScope.launch { dataManager.saveLogin(login) }
        GlobalScope.launch { dht.remember(invite.users) }
    }

    override suspend fun isRegistered(): Boolean = dataManager.getLogin() != null
}
