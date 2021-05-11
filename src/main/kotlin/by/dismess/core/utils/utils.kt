package by.dismess.core.utils

import by.dismess.core.model.UserID
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest

val md = MessageDigest.getInstance("MD5")

fun hashMD5(input: String): BigInteger {
    return BigInteger(1, md.digest(input.toByteArray(Charsets.UTF_8)))
}

fun generateUserID(login: String): UserID = UserID(hashMD5(login))

typealias UniqID = BigInteger

fun groupID(vararg idList: UniqID): UniqID {
    val outputStream = ByteArrayOutputStream()
    // TODO fix
    for (id in idList) {
        outputStream.write(id.toByteArray())
    }
    return UniqID(1, md.digest(outputStream.toByteArray()))
}

val Int.uniqID: BigInteger
    get() = UniqID.valueOf(this.toLong())

fun twoBytesToInt(number: ByteArray): Int = (number[0].toInt() and 0xff shl 8) or
    (number[1].toInt() and 0xff)

fun intToBytes(number: Int, size: Int = 1): ByteArray =
    ByteBuffer.allocate(4).putInt(number).array().sliceArray((4 - size)..3)
