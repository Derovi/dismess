package by.dismess.core.security

import by.dismess.core.utils.intToBytes
import by.dismess.core.utils.twoBytesToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.SecretKeySpec

const val AES_SIZE: Int = 32
const val KEY_SIZE: Int = 1024
const val MAX_SESSIONS_SIZE: Int = 8
const val SESSION_NUMBER_SIZE: Int = 2
const val MAX_OFFSET = (KEY_SIZE - AES_SIZE * 8) / 8

class Encryptor {
    private val channel: Channel<Unit> = Channel()
    private var keyAgreement: KeyAgreement = KeyAgreement.getInstance("DH")
    private val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("DH")
    private val keyFactory = KeyFactory.getInstance("DH")
    private var aesReceiveOffset: Int = 0
    private var aesSendOffset: Int = 0
    private val cipherAlgo: String = "AES"
    private var aesKeys: MutableList<Pair<Int, ByteArray>> = mutableListOf()
    private lateinit var publicKey: PublicKey
    private var currentSession: Int = 0

    init {
        keyPairGenerator.initialize(KEY_SIZE)
    }

    private fun generateCipherKey(aesKey: ByteArray, aesOffset: Int): Key =
        SecretKeySpec(aesKey, aesOffset, AES_SIZE, cipherAlgo)

    private fun findSessionKey(sessionNumber: Int): ByteArray {
        for (session in aesKeys) {
            if (session.first == sessionNumber) {
                return session.second
            }
        }
        throw IllegalArgumentException("invalid key")
    }

    private fun cipherKey(aesOffset: Int, key: ByteArray): Key {
        val sessionNumber: Int = twoBytesToInt(byteArrayOf(key[0], key[1])) % (2 shl 16)
        val sessionKey = findSessionKey(sessionNumber)
        return generateCipherKey(sessionKey, aesOffset)
    }

    private fun currentCipherKey(aesOffset: Int): Key {
        val sessionKey = findSessionKey(currentSession)
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
        aesKeys.add(Pair<Int, ByteArray>(currentSession, newKey))
    }

    fun isInitialized(): Boolean {
        return aesKeys.isNotEmpty()
    }

    fun publicKeyBytes(updateSession: Boolean): ByteArray {
        val bytePublicKey = publicKey.encoded
        var session = currentSession
        if (updateSession) {
            ++session
        }
        return Base64.getEncoder().encode(intToBytes(session, SESSION_NUMBER_SIZE) + bytePublicKey)
    }

    /**
     * Create new key agreement to init new session
     */
    fun updateKey() {
        val kp = keyPairGenerator.generateKeyPair()
        publicKey = kp.public
        keyAgreement.init(kp.private)
    }

    /**
     * Fulfill key agreement with other party public key and generate common secret as AES key
     * @param key other party key
     */
    @ExperimentalCoroutinesApi
    suspend fun setReceiverPublicKey(key: ByteArray): Boolean {
        var bytePublicKey = Base64.getDecoder().decode(key)
        val newSession = twoBytesToInt(bytePublicKey.sliceArray(0..1))
        if (newSession <= currentSession) {
            return false
        }
        currentSession = newSession
        bytePublicKey = bytePublicKey.sliceArray(2 until bytePublicKey.size)
        val anotherKey = keyFactory.generatePublic(X509EncodedKeySpec(bytePublicKey)) as DHPublicKey
        keyAgreement.doPhase(anotherKey, true)
        addNewSession()
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
    fun encrypt(message: ByteArray): ByteArray {
        val key: Key = currentCipherKey(aesSendOffset)
        val cipher: Cipher = Cipher.getInstance(cipherAlgo)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        aesSendOffset = generateOffset()
        val msg = intToBytes(aesSendOffset) + message
        val encrypted = intToBytes(currentSession, SESSION_NUMBER_SIZE) + cipher.doFinal(msg)
        return Base64.getEncoder().encode(encrypted)
    }

    /**
     * Extract next offset and decrypt message
     * @param message encrypted with AES message
     * @return ByteArray with decrypted message
     */
    fun decrypt(message: ByteArray): ByteArray {
        val decodedValue: ByteArray = Base64.getDecoder().decode(message)
        val key: Key = cipherKey(aesReceiveOffset, decodedValue)
        val cipher: Cipher = Cipher.getInstance(cipherAlgo)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val result = cipher.doFinal(decodedValue.sliceArray(2 until decodedValue.size))
        val offset = result[0]
        aesReceiveOffset = offset.toInt()
        return result.sliceArray(1 until result.size)
    }

    suspend fun block() {
        channel.send(Unit)
    }
}
