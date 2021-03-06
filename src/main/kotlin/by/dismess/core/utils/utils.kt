package by.dismess.core.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.math.BigInteger
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*

val md = MessageDigest.getInstance("MD5")

fun hashMD5(input: String): BigInteger {
    return BigInteger(1, md.digest(input.substring(0, 8).toByteArray(Charsets.UTF_8)))
}

fun generateUserID(login: String): UniqID = hashMD5(login)

fun randomUniqID() = BigInteger(128, Random())

typealias UniqID = BigInteger

fun groupID(vararg idList: UniqID): UniqID {
    var sum = BigInteger.ZERO
    for (id in idList) {
        sum += id
    }
    return sum
}

val Int.uniqID: BigInteger
    get() = UniqID.valueOf(this.toLong())

fun twoBytesToInt(number: ByteArray): Int = (number[0].toInt() and 0xff shl 8) or
    (number[1].toInt() and 0xff)

fun fourBytesToInt(number: ByteArray): Int {
    var value = 0
    if (number.size != 4) {
        return value
    }
    for (i in 0..3) {
        value = value or (number[i].toInt() and 0xff shl 8 * (3 - i))
    }
    return value
}

fun intToBytes(number: Int, size: Int = 1): ByteArray =
    ByteBuffer.allocate(4).putInt(number).array().sliceArray((4 - size)..3)

val gsonBuilder: GsonBuilder = GsonBuilder()
    .registerTypeAdapter(InetSocketAddress::class.java, InetSocketAddressConverter())
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
