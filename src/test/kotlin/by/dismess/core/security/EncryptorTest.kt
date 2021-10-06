package by.dismess.core.security

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.koin.test.KoinTest

class EncryptorTest : KoinTest {
    @ExperimentalCoroutinesApi
    private suspend fun updateEncryptors(lhs: Encryptor, rhs: Encryptor) {
        lhs.updateKey()
        rhs.updateKey()
        lhs.setReceiverPublicKey(rhs.publicKeyBytes(true))
        rhs.setReceiverPublicKey(lhs.publicKeyBytes(false))
    }

    private suspend fun checkMessage(message: ByteArray, firstEncryptor: Encryptor, secondEncryptor: Encryptor) {
        val encrypted = firstEncryptor.encrypt(message)
        val decrypted = secondEncryptor.decrypt(encrypted)
        Assert.assertArrayEquals(message, decrypted)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testSimpleEncryptor() {
        runBlocking {
            val firstEncryptor = Encryptor()
            val secondEncryptor = Encryptor()
            updateEncryptors(firstEncryptor, secondEncryptor)
            var message = "Simple message".toByteArray()
            checkMessage(message, firstEncryptor, secondEncryptor)
            message = "Русский текст".toByteArray()
            checkMessage(message, firstEncryptor, secondEncryptor)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testSessions() {
        runBlocking {
            val firstEncryptor = Encryptor()
            val secondEncryptor = Encryptor()
            updateEncryptors(firstEncryptor, secondEncryptor)
            val message = "Simple message".toByteArray()
            checkMessage(message, firstEncryptor, secondEncryptor)
            updateEncryptors(firstEncryptor, secondEncryptor)
            checkMessage(message, firstEncryptor, secondEncryptor)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testDifferentSessions() {
        runBlocking {
            val firstEncryptor = Encryptor()
            val secondEncryptor = Encryptor()
            updateEncryptors(firstEncryptor, secondEncryptor)
            val message = "Simple message".toByteArray()
            checkMessage(message, firstEncryptor, secondEncryptor)
            updateEncryptors(firstEncryptor, secondEncryptor)
            secondEncryptor.updateKey()
            secondEncryptor.updateKey()
            checkMessage(message, firstEncryptor, secondEncryptor)
            secondEncryptor.encrypt(message)
        }
    }
}
