package by.dismess.core.utils

import by.dismess.core.model.UserID
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest

fun hashMD5(input: String): BigInteger {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray(Charsets.UTF_8)))
}

fun generateUserID(login: String): UserID {
    return UserID(hashMD5(login))
}

fun byteToInt(number: Byte): Int {
    return number.toInt()
}

fun twoBytesToInt(number: ByteArray): Int {
    return (number[0].toInt() and 0xff shl 8) or
        (number[1].toInt() and 0xff)
}

fun intToBytes(number: Int, size: Int = 1): ByteArray {
    return ByteBuffer.allocate(4).putInt(number).array().sliceArray((3 - size + 1)..3)
}
