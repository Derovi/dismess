package by.dismess.core.dht

import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService
) : DHT {

    private val K : Int = 8

    private var table : LinkedList<Bucket> = LinkedList()

    override fun store(key: String, data: ByteArray) {

        TODO("Not yet implemented")
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not yet implemented")
    }
}
