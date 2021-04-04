package by.dismess.core.dht

import java.math.BigInteger

infix fun BigInteger.inBorder(border: BucketBorder) = border.contains(this)
