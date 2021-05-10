package by.dismess.core.security

import by.dismess.core.outer.NetworkInterface
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException
import java.net.InetSocketAddress

/**
 *                           DISMESS PROTOCOL
 *
 *                       Protocol for message
 *                +------+-----------+-------------+------------+....+----+
 *                |   0  |  SESSION  | NEXT OFFSET |     MESSAGE     |NULL|
 *                +------+-----------+-------------+------------+....+----+
# of bytes:	         1        2              1           variable       1
 * SESSION is number of current session, goes unencrypted
 *
 *                       Protocol for key
 *                +------+-----------+-----------+--------------+....+----+
 *                |   1  |   ORDER   |  SESSION  |        KEY        |NULL|
 *                +------+-----------+-----------+--------------+....+----+
# of bytes:	         1        1              2           variable       1
 * ORDER is equal to 0 if receiver should send his public key back, 1 if shouldn't
 * KEY is sent without encryption.
 **/

class SecureNetworkInterface(
    private val networkInterface: NetworkInterface
) : NetworkInterface {
    private val sessionManager: SessionManager = SessionManager()

    private suspend fun tryUpdateKey(address: InetSocketAddress): ByteArray? = sessionManager.tryUpdateKey(address)

    private suspend fun processData(
        sender: InetSocketAddress,
        data: ByteArray,
        receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit
    ) {
        val response: TypedData = sessionManager.processData(sender, data)
        when (response.type) {
            DataType.MESSAGE -> {
                receiver(sender, response.data)
            }
            DataType.SEND_BACK_KEY -> {
                GlobalScope.launch {
                    networkInterface.sendRawMessage(sender, response.data)
                }
            }
            DataType.KEY -> {
                GlobalScope.launch {
                    sessionManager.release(sender)
                }
            }
        }
    }

    override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
        val updatedKey = tryUpdateKey(address)
        if (updatedKey != null) {
            networkInterface.sendRawMessage(address, updatedKey)
            sessionManager.block(address)
        }
        try {
            val encryptedMessage = sessionManager.encrypt(address, data)
            networkInterface.sendRawMessage(address, encryptedMessage)
        } catch (_: IllegalArgumentException) {
        }
    }

    override fun setMessageReceiver(receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit) {
        networkInterface.setMessageReceiver { sender: InetSocketAddress, data: ByteArray ->
            try {
                runBlocking { processData(sender, data, receiver) }
            } catch (_: IllegalArgumentException) {
            }
        }
    }

    fun setSessionLifetime(time: Int) {
        sessionManager.keyLifetimeMS = time
    }
}
