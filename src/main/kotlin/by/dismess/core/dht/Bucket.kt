package by.dismess.core.dht

class Bucket(
    val border: BucketBorder
) {
    val data = mutableListOf<Pair<String, ByteArray>>()
}
