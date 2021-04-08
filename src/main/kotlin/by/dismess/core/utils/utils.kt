package by.dismess.core.utils

import by.dismess.core.model.UserID
import java.math.BigInteger
import java.security.MessageDigest

fun hashMD5(input: String): BigInteger {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray(Charsets.UTF_8)))
}

fun generateUserID(login: String): UserID {
    return UserID(hashMD5(login))
}
