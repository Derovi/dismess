package by.dismess.core.utils

import by.dismess.core.model.UserID
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest

val md = MessageDigest.getInstance("MD5")

fun hashMD5(input: String): BigInteger {
    return BigInteger(1, md.digest(input.toByteArray(Charsets.UTF_8)))
}

fun generateUserID(login: String): UserID {
    return UserID(hashMD5(login))
}

typealias UniqID = BigInteger

fun groupID(vararg idList: UniqID): UniqID {
    val outputStream = ByteArrayOutputStream()
    for (id in idList) {
        outputStream.writeBytes(id.toByteArray())
    }
    return UniqID(1, md.digest(outputStream.toByteArray()))
}

val Int.uniqID: BigInteger
    get() = UniqID.valueOf(this.toLong())
