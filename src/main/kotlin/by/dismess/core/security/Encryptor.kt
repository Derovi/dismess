package by.dismess.core.security

import java.nio.ByteBuffer
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

class Encryptor {
    private var keyAgreement: KeyAgreement = KeyAgreement.getInstance("DH")
    private val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("DH")
    private var aesReceiveOffset: Int = 0
    private var aesSendOffset: Int = 0
    private val cipherAlgo: String = "AES"
    private lateinit var aesKey: ByteArray
    private lateinit var publicKey: PublicKey

    init {
        keyPairGenerator.initialize(KEY_SIZE)
        updateKey()
    }

    private fun generateCipherKey(aesOffset: Int): Key {
        return SecretKeySpec(aesKey, aesOffset, AES_SIZE, cipherAlgo)
    }

    private fun byteToInt(number: Byte): Int {
        return number.toInt()
    }

    private fun intToBytes(number: Int): ByteArray {
        return ByteBuffer.allocate(4).putInt(number).array().sliceArray(3..3)
    }

    private fun generateOffset(): Int {
        val rightBorder: Int = (KEY_SIZE - AES_SIZE * 8) / 8
        return (0..rightBorder).random()
    }

    fun getPublicKey(): ByteArray {
        val bytePublicKey = publicKey.encoded
        return Base64.getEncoder().encode(bytePublicKey)
    }

    fun updateKey() {
        val kp = keyPairGenerator.generateKeyPair()
        publicKey = kp.public
        keyAgreement.init(kp.private)
    }

    fun setReceiverPublicKey(key: ByteArray) {
        val bytePublicKey = Base64.getDecoder().decode(key)
        val factory = KeyFactory.getInstance("DH")
        val anotherKey = factory.generatePublic(X509EncodedKeySpec(bytePublicKey)) as DHPublicKey
        keyAgreement.doPhase(anotherKey, true)
        aesKey = keyAgreement.generateSecret()
    }

    fun encrypt(message: ByteArray): ByteArray {
        println(aesSendOffset)
        val key: Key = generateCipherKey(aesSendOffset)
        val cipher: Cipher = Cipher.getInstance(cipherAlgo)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        aesSendOffset = generateOffset()
        val msg = intToBytes(aesSendOffset) + message
        val encrypted = cipher.doFinal(msg)
        return Base64.getEncoder().encode(encrypted)
    }

    fun decrypt(message: ByteArray): ByteArray {
        println(aesReceiveOffset)
        val key: Key = generateCipherKey(aesReceiveOffset)
        val cipher: Cipher = Cipher.getInstance(cipherAlgo)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedValue: ByteArray = Base64.getDecoder().decode(message)
        val result = cipher.doFinal(decodedValue)
        val offset = result[0]
        aesReceiveOffset = byteToInt(offset)
        return result.sliceArray(1 until result.size)
    }
}
