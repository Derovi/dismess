package by.dismess.core.dht

import by.dismess.core.model.UserID
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.KlaxonException
import java.math.BigInteger
import java.net.InetSocketAddress

@Target(AnnotationTarget.FIELD)
annotation class KlaxonBorder

class Bucket(
    @KlaxonBorder
    val border: BucketBorder
) {
    val idToIP = mutableMapOf<UserID, InetSocketAddress>()
    var lastPingTime = System.currentTimeMillis()
    fun printBucketData() {
        println("Borders: " + border.left + " " + border.right)
        for (user in idToIP) {
            println("User: " + user.key + " " + user.value)
        }
    }
}

val Converter = object : Converter {
    override fun canConvert(cls: Class<*>): Boolean = cls == BucketBorder::class.java

    override fun toJson(value: Any): String {
        return """{ {"left" : ${(value as BucketBorder).left}}, {"right" : ${(value as BucketBorder).right}} }"""
    }

    override fun fromJson(jv: JsonValue): Any? {
        println(jv.propertyClass)
        val data = jv.inside
        println((data as BucketBorder).left)
        return if (jv.string != null) {
            BucketBorder(BigInteger(jv.objString("left")), BigInteger(jv.objString("right")))
        } else {
            throw KlaxonException("Couldn't parse BigInteger: ${jv.string}")
        }
    }
}
