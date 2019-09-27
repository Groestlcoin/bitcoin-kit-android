package io.horizontalsystems.dashkit

import fr.cryptohash.*
import io.horizontalsystems.bitcoincore.core.IHasher
import java.util.*

class GroestlHasher : IHasher {
    private val algorithms = listOf(
            Groestl512(),
            Groestl512()
    )

    override fun hash(data: ByteArray): ByteArray {
        var hash = data

        algorithms.forEach {
            hash = it.digest(hash)
        }

        return Arrays.copyOfRange(hash, 0, 32)
    }
}
