package by.dismess.core.dht

import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkInterface
import by.dismess.core.services.StorageService
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList

class DHTImpl(
    val networkInterface: NetworkInterface,
    val storageInterface: StorageService,
    val ownerID : UserID
) : DHT {

    private val K : Int = 8

    private var table : LinkedList<Bucket> = LinkedList()

    override fun store(key: String, data: ByteArray) {
        val idFormKey : BigInteger = TODO("Some hash of key")
        for (bucket in table) {
            if (bucket.leftBorder <= idFormKey && idFormKey <= bucket.rightBorder) {
                if (bucket.list.size < K) {
                    bucket.list.add(Pair(idFormKey, data))
                    bucket.list.sortBy { it.first }
                } else {

                }
                break
            }
        }

        TODO("Not yet implemented")
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not yet implemented")
    }
}
