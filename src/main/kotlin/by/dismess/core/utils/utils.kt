package by.dismess.core.utils

import by.dismess.core.model.UserID
import com.google.gson.*
import java.lang.reflect.Type
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.net.InetSocketAddress
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
        outputStream.writeBytes(id.toByteArray())
    }
    return UniqID(1, md.digest(outputStream.toByteArray()))
}

val Int.uniqID: BigInteger
    get() = UniqID.valueOf(this.toLong())

fun twoBytesToInt(number: ByteArray): Int = (number[0].toInt() and 0xff shl 8) or
    (number[1].toInt() and 0xff)

fun intToBytes(number: Int, size: Int = 1): ByteArray =
    ByteBuffer.allocate(4).putInt(number).array().sliceArray((4 - size)..3)

val gsonBuilder: GsonBuilder = GsonBuilder()
    .registerTypeAdapter(InetSocketAddress::class.java, InetSocketAddressConverter())
    .registerTypeAdapter(UserID::class.java, UserIDConverter())
val gson: Gson = gsonBuilder.create()

class InetSocketAddressConverter : JsonSerializer<InetSocketAddress>, JsonDeserializer<InetSocketAddress> {
    override fun serialize(src: InetSocketAddress?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            throw Exception("Couldn't serialize: null object!")
        }
        val address = src.address.toString().substring(1)
        val port = src.port
        return JsonPrimitive("/$address:$port")
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): InetSocketAddress {
        if (json == null) {
            throw Exception("Couldn't deserialize: null object!")
        }
        val data = json.asString.substring(1).split(":")
        return InetSocketAddress(data[0], data[1].toInt())
    }
}

class UserIDConverter : JsonSerializer<UserID>, JsonDeserializer<UserID> {
    override fun serialize(src: UserID?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            throw Exception("Couldn't serialize: null object!")
        }
        val id = src.rawID
        return JsonPrimitive(id)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): UserID {
        if (json == null) {
            throw Exception("Couldn't deserialize: null object!")
        }
        val userID = UserID(json.asString)
        return userID
    }
}
