package by.dismess.core.security

import by.dismess.core.utils.byteToInt
import by.dismess.core.utils.intToBytes
import by.dismess.core.utils.twoBytesToInt
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.SecretKeySpec

const val AES_SIZE: Int = 32
const val KEY_SIZE: Int = 1024
const val MAX_SESSIONS_SIZE: Int = 8
const val SESSION_NUMBER_SIZE: Int = 2

class Encryptor {
    private var keyAgreement: KeyAgreement = KeyAgreement.getInstance("DH")
    private val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("DH")
    private var aesReceiveOffset: Int = 0
    private var aesSendOffset: Int = 0
    private val cipherAlgo: String = "AES"
    private var aesKeys: List<Pair<Int, ByteArray>> = mutableListOf()
    private lateinit var publicKey: PublicKey
    private var currentSession: Int = 0

    init {
        keyPairGenerator.initialize(KEY_SIZE)
//        updateKey()
    }

    private fun generateCipherKey(aesKey: ByteArray, aesOffset: Int): Key {
        return SecretKeySpec(aesKey, aesOffset, AES_SIZE, cipherAlgo)
    }

    private fun findSession(sessionNumber: Int): ByteArray? {
        for (session in aesKeys) {
            if (session.first == sessionNumber) {
                return session.second
            }
        }
        return null
    }

    private fun cipherKey(aesOffset: Int, key: ByteArray): Key? {
        val sessionNumber: Int = twoBytesToInt(key.sliceArray(1..3))
        val sessionKey = findSession(sessionNumber) ?: return null
        return generateCipherKey(sessionKey, aesOffset)
    }

    private fun currentCipherKey(aesOffset: Int): Key? {
        val sessionKey = findSession(currentSession) ?: return null
        return generateCipherKey(sessionKey, aesOffset)
    }

    /**
     * Generate random number in range from 0 to max offset position
     * @return new offset
     */
    private fun generateOffset(): Int {
        val rightBorder: Int = (KEY_SIZE - AES_SIZE * 8) / 8
        return (0..rightBorder).random()
    }

    /**
     * Create new session and drop old sessions
     */
    private fun addNewSession() {
        val newKey = keyAgreement.generateSecret()
        if (aesKeys.size >= MAX_SESSIONS_SIZE) {
            aesKeys = aesKeys.drop(1)
        }
        aesKeys = aesKeys + Pair<Int, ByteArray>(currentSession, newKey)
    }

    fun publicKeyBytes(updateSession: Boolean): ByteArray {
        val bytePublicKey = publicKey.encoded
        var session = currentSession
        if (updateSession) {
            session += 1
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
    fun setReceiverPublicKey(key: ByteArray) {
        var bytePublicKey = Base64.getDecoder().decode(key)
        currentSession = twoBytesToInt(bytePublicKey.sliceArray(0..1))
        bytePublicKey = bytePublicKey.sliceArray(2 until bytePublicKey.size)
        val factory = KeyFactory.getInstance("DH")
        val anotherKey = factory.generatePublic(X509EncodedKeySpec(bytePublicKey)) as DHPublicKey
        keyAgreement.doPhase(anotherKey, true)
        addNewSession()
    }

    /**
     * Generate offset for next message and join it with encrypted message
     * @param message data to encrypt with AES cipher
     * @return next offset + message
     */
    fun encrypt(message: ByteArray): ByteArray {
        val key: Key = currentCipherKey(aesSendOffset) ?: return byteArrayOf()
        val cipher: Cipher = Cipher.getInstance(cipherAlgo)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        aesSendOffset = generateOffset()
        val msg = intToBytes(aesSendOffset) + intToBytes(currentSession, SESSION_NUMBER_SIZE) + message
        val encrypted = cipher.doFinal(msg)
        return Base64.getEncoder().encode(encrypted)
    }

    /**
     * Extract next offset and decrypt message
     * @param message encrypted with AES message
     * @return ByteArray with decrypted message
     */
    fun decrypt(message: ByteArray): ByteArray {
        val key: Key = cipherKey(aesReceiveOffset, message) ?: return byteArrayOf()
        val cipher: Cipher = Cipher.getInstance(cipherAlgo)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedValue: ByteArray = Base64.getDecoder().decode(message)
        val result = cipher.doFinal(decodedValue)
        val offset = result[0]
        aesReceiveOffset = byteToInt(offset)
        return result.sliceArray(3 until result.size)
    }
}
