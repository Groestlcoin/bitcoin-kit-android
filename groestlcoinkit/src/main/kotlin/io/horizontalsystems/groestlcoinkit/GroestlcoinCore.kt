package io.horizontalsystems.groestlcoinkit

import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.BitcoinCoreBuilder
import io.horizontalsystems.bitcoincore.network.Network
import io.horizontalsystems.bitcoincore.network.messages.*
import io.horizontalsystems.bitcoincore.utils.GroestlcoinBase58AddressConverter
import io.horizontalsystems.groestlcoinkit.network.messages.GroestlcoinNetworkMessageSerializer

class GroestlcoinCoreBuilder : BitcoinCoreBuilder {

    constructor() : super()

    private var network: Network? = null

    override fun setNetwork(network: Network): BitcoinCoreBuilder {
        this.network = network
        return this
    }

    override fun build(): BitcoinCore {
        val bitcoinCore : BitcoinCore = super.build()
        val network = checkNotNull(this.network)
        bitcoinCore.networkMessageSerializer = GroestlcoinNetworkMessageSerializer(network.magic)
        bitcoinCore.removeMessageParser(TransactionMessageParser())
        bitcoinCore.addMessageParser(io.horizontalsystems.groestlcoinkit.network.messages.GroestlcoinTransactionMessageParser())

        bitcoinCore.removeMessageSerializer(TransactionMessageSerializer())
        bitcoinCore.addMessageSerializer(io.horizontalsystems.groestlcoinkit.network.messages.GroestlcoinTransactionMessageSerializer())

        bitcoinCore.prependAddressConverter(GroestlcoinBase58AddressConverter(network.addressVersion, network.addressScriptVersion))

        return bitcoinCore
    }
}
