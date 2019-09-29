package io.horizontalsystems.groestlcoinkit

import android.content.Context
import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.BitcoinCoreBuilder
import io.horizontalsystems.bitcoincore.blocks.*
import io.horizontalsystems.bitcoincore.blocks.validators.BlockValidatorChain
import io.horizontalsystems.bitcoincore.blocks.validators.IBlockValidator
import io.horizontalsystems.bitcoincore.blocks.validators.ProofOfWorkValidator
import io.horizontalsystems.bitcoincore.core.*
import io.horizontalsystems.bitcoincore.extensions.toHexString
import io.horizontalsystems.bitcoincore.managers.*
import io.horizontalsystems.bitcoincore.models.BitcoinPaymentData
import io.horizontalsystems.bitcoincore.models.BlockInfo
import io.horizontalsystems.bitcoincore.models.PublicKey
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.network.Network
import io.horizontalsystems.bitcoincore.network.messages.*
import io.horizontalsystems.bitcoincore.network.peer.*
import io.horizontalsystems.bitcoincore.serializers.BlockHeaderParser
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoincore.transactions.*
import io.horizontalsystems.bitcoincore.transactions.builder.InputSigner
import io.horizontalsystems.bitcoincore.transactions.builder.TransactionBuilder
import io.horizontalsystems.bitcoincore.transactions.scripts.ScriptBuilder
import io.horizontalsystems.bitcoincore.transactions.scripts.ScriptType
import io.horizontalsystems.bitcoincore.utils.*
import io.horizontalsystems.groestlcoinkit.network.messages.GroestlcoinNetworkMessageSerializer
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.Single
import java.util.concurrent.Executor

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
        bitcoinCore.addMessageParser(io.horizontalsystems.groestlcoinkit.network.messages.TransactionMessageParser())

        bitcoinCore.removeMessageSerializer(TransactionMessageSerializer())
        bitcoinCore.addMessageSerializer(io.horizontalsystems.groestlcoinkit.network.messages.TransactionMessageSerializer())
        return bitcoinCore
    }
}
