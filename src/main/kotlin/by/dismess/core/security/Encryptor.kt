package by.dismess.core.security

import by.dismess.core.utils.intToBytes
import by.dismess.core.utils.twoBytesToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.SecretKeySpec

const val AES_SIZE: Int = 32
const val KEY_SIZE: Int = 1024
const val MAX_SESSIONS_SIZE: Int = 8
const val SESSION_NUMBER_SIZE: Int = 2
const val MAX_OFFSET = (KEY_SIZE - AES_SIZE * 8) / 8
const val CIPHER_ALGO = "AES"

class Encryptor {
    private val channel: Channel<Unit> = Channel()
    private val mutex = Mutex()
    private var keyAgreement: KeyAgreement = KeyAgreement.getInstance("DH")
    private val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("DH")
    private val keyFactory = KeyFactory.getInstance("DH")
    private var aesReceiveOffset = AtomicInteger(0)
    private var aesSendOffset = AtomicInteger(0)
    private var aesKeys: MutableList<Pair<Int, ByteArray>> = mutableListOf()
    private lateinit var publicKey: PublicKey
    private var currentSession = AtomicInteger(0)

    init {
        keyPairGenerator.initialize(KEY_SIZE)
    }

    private fun generateCipherKey(aesKey: ByteArray, aesOffset: Int): Key =
        SecretKeySpec(aesKey, aesOffset, AES_SIZE, CIPHER_ALGO)

    private suspend fun findSessionKey(sessionNumber: Int): ByteArray {
        mutex.withLock {
            for (session in aesKeys) {
                if (session.first == sessionNumber) {
                    return@findSessionKey session.second
                }
            }
        }
        throw IllegalArgumentException("invalid key")
    }

    private suspend fun cipherKey(aesOffset: Int, key: ByteArray): Key {
        val sessionNumber: Int = twoBytesToInt(byteArrayOf(key[0], key[1])) % (2 shl 16)
        val sessionKey = findSessionKey(sessionNumber)
        return generateCipherKey(sessionKey, aesOffset)
    }

    private suspend fun currentCipherKey(aesOffset: Int): Key {
        val sessionKey = findSessionKey(currentSession.get())
        return generateCipherKey(sessionKey, aesOffset)
    }

    /**
     * Generate random number in range from 0 to max offset position
     * @return new offset
     */
    private fun generateOffset(): Int = (0 until MAX_OFFSET).random()

    /**
     * Create new session and drop old sessions
     */
    private fun addNewSession() {
        val newKey = keyAgreement.generateSecret()
        if (aesKeys.size >= MAX_SESSIONS_SIZE) {
            aesKeys.removeAt(0)
        }
        aesKeys.add(Pair<Int, ByteArray>(currentSession.get(), newKey))
    }

    suspend fun isInitialized(): Boolean =
        mutex.withLock { aesKeys.isNotEmpty() }

    suspend fun publicKeyBytes(updateSession: Boolean): ByteArray {
        val bytePublicKey = mutex.withLock { publicKey.encoded }
        var session = currentSession.get()
        if (updateSession) {
            ++session
        }
        return Base64.getEncoder().encode(intToBytes(session, SESSION_NUMBER_SIZE) + bytePublicKey)
    }

    /**
     * Create new key agreement to init new session
     */
    suspend fun updateKey() {
        val kp = keyPairGenerator.generateKeyPair()
        mutex.withLock {
            publicKey = kp.public
            keyAgreement.init(kp.private)
        }
    }

    /**
     * Fulfill key agreement with other party public key and generate common secret as AES key
     * @param key other party key
     */
    @ExperimentalCoroutinesApi
    suspend fun setReceiverPublicKey(key: ByteArray): Boolean {
        var bytePublicKey = Base64.getDecoder().decode(key)
        val newSession = twoBytesToInt(bytePublicKey.sliceArray(0..1))
        if (newSession <= currentSession.get()) {
            return false
        }
        currentSession.set(newSession)
        bytePublicKey = bytePublicKey.sliceArray(2 until bytePublicKey.size)
        val anotherKey = keyFactory.generatePublic(X509EncodedKeySpec(bytePublicKey)) as DHPublicKey
        mutex.withLock {
            keyAgreement.doPhase(anotherKey, true)
            addNewSession()
        }
        if (!channel.isEmpty) {
            channel.receive()
        }
        return true
    }

    /**
     * Generate offset for next message and join it with encrypted message
     * @param message data to encrypt with AES cipher
     * @return next offset + message
     */
    suspend fun encrypt(message: ByteArray): ByteArray {
        val key: Key = currentCipherKey(aesSendOffset.get())
        val cipher: Cipher = Cipher.getInstance(CIPHER_ALGO)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        aesSendOffset.set(generateOffset())
        val msg = intToBytes(aesSendOffset.get()) + message
        val encrypted = intToBytes(currentSession.get(), SESSION_NUMBER_SIZE) + cipher.doFinal(msg)
        return Base64.getEncoder().encode(encrypted)
    }

    /**
     * Extract next offset and decrypt message
     * @param message encrypted with AES message
     * @return ByteArray with decrypted message
     */
    suspend fun decrypt(message: ByteArray): ByteArray {
        val decodedValue: ByteArray = Base64.getDecoder().decode(message)
        val key: Key = cipherKey(aesReceiveOffset.get(), decodedValue)
        val cipher: Cipher = Cipher.getInstance(CIPHER_ALGO)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val result = cipher.doFinal(decodedValue.sliceArray(2 until decodedValue.size))
        val offset = result[0]
        aesReceiveOffset.set(offset.toInt())
        return result.sliceArray(1 until result.size)
    }

    suspend fun block() {
        channel.send(Unit)
    }
}
