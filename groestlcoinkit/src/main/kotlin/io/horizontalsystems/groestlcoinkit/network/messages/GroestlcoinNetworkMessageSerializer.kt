package io.horizontalsystems.groestlcoinkit.network.messages

import io.horizontalsystems.bitcoincore.network.messages.NetworkMessageSerializer
import io.horizontalsystems.groestlcoinkit.GroestlHasher
import java.util.*

class GroestlcoinNetworkMessageSerializer : NetworkMessageSerializer {

    constructor(magic: Long) : super (magic)

    override fun getCheckSum(payload: ByteArray): ByteArray {
        val hash = GroestlHasher().hash(payload)
        return Arrays.copyOfRange(hash, 0, 4)
    }
}