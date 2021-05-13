import by.dismess.core.dht.DHT
import by.dismess.core.managers.App
import by.dismess.core.managers.DataManager
import by.dismess.core.model.Invite
import by.dismess.core.model.UserID
import by.dismess.core.network.retrievePublicSocketAddress
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AppImpl(
    val dht: DHT,
    val dataManager: DataManager
) : App {
    override suspend fun register(login: String, invite: Invite): Unit = coroutineScope {
        saveInitialData(login)
        GlobalScope.launch { dht.connectTo(UserID(invite.userId), invite.address) }
    }

    override suspend fun isRegistered(): Boolean = dataManager.getLogin() != null

    override suspend fun saveInitialData(login: String) {
        GlobalScope.launch { dataManager.saveLogin(login) }
        GlobalScope.launch { dataManager.setOwnIP(retrievePublicSocketAddress(8080)!!) }
    }
}
