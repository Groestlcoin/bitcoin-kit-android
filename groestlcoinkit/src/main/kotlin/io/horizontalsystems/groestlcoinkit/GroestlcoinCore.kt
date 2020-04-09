package io.horizontalsystems.groestlcoinkit

import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.BitcoinCoreBuilder
import io.horizontalsystems.bitcoincore.DustCalculator
import io.horizontalsystems.bitcoincore.core.PluginManager
import io.horizontalsystems.bitcoincore.core.TransactionDataSorterFactory
import io.horizontalsystems.bitcoincore.managers.PublicKeyManager
import io.horizontalsystems.bitcoincore.managers.RestoreKeyConverterChain
import io.horizontalsystems.bitcoincore.managers.UnspentOutputSelectorChain
import io.horizontalsystems.bitcoincore.network.Network
import io.horizontalsystems.bitcoincore.network.messages.*
import io.horizontalsystems.bitcoincore.serializers.BlockHeaderParser
import io.horizontalsystems.bitcoincore.transactions.*
import io.horizontalsystems.bitcoincore.transactions.builder.*
import io.horizontalsystems.groestlcoinkit.network.messages.GroestlcoinNetworkMessageParser
import io.horizontalsystems.groestlcoinkit.network.messages.GroestlcoinNetworkMessageSerializer
import io.horizontalsystems.groestlcoinkit.network.messages.GroestlcoinTransactionMessageParser
import io.horizontalsystems.groestlcoinkit.transactions.builder.GroestlcoinInputSigner
import io.horizontalsystems.groestlcoinkit.transactions.builder.GroestlcoinTransactionBuilder
import io.horizontalsystems.groestlcoinkit.utils.GroestlcoinBase58AddressConverter

class GroestlcoinCoreBuilder : BitcoinCoreBuilder {

    constructor() : super()

    private var network: Network? = null

    override fun setNetwork(network: Network): BitcoinCoreBuilder {
        this.network = network
        super.setNetwork(network)
        return this
    }

    override fun build(): BitcoinCore {
        val network = checkNotNull(this.network)
        val bitcoinCore : BitcoinCore = super.build()
        val storage = this.storage!!
        bitcoinCore.networkMessageSerializer = GroestlcoinNetworkMessageSerializer(network.magic)
        bitcoinCore.networkMessageParser = GroestlcoinNetworkMessageParser(network.magic)

        val restoreKeyConverterChain = RestoreKeyConverterChain()

        val pluginManager = PluginManager()
        restoreKeyConverterChain.add(pluginManager)

        val publicKeyManager = PublicKeyManager.create(storage, hdWallet, restoreKeyConverterChain)
        val transactionDataSorterFactory = TransactionDataSorterFactory()
        val unspentOutputSelector = UnspentOutputSelectorChain()
        val transactionSizeCalculator = TransactionSizeCalculator()
        val inputSigner = GroestlcoinInputSigner(hdWallet, network)
        val outputSetter = OutputSetter(transactionDataSorterFactory)
        val dustCalculator = DustCalculator(network.dustRelayTxFee, transactionSizeCalculator)
        val inputSetter = InputSetter(unspentOutputSelector, publicKeyManager, addressConverter, bip.scriptType, transactionSizeCalculator, pluginManager, dustCalculator, transactionDataSorterFactory)
        val signer = TransactionSigner(inputSigner)
        val lockTimeSetter = LockTimeSetter(storage)
        val recipientSetter = RecipientSetter(addressConverter, pluginManager)

        val transactionBuilder = GroestlcoinTransactionBuilder(recipientSetter, outputSetter, inputSetter, signer, lockTimeSetter)
        bitcoinCore.replaceTransactionBuilder(transactionBuilder)

        bitcoinCore.addMessageParser(AddrMessageParser())
                .addMessageParser(MerkleBlockMessageParser(BlockHeaderParser(GroestlHasher())))
                .addMessageParser(InvMessageParser())
                .addMessageParser(GetDataMessageParser())
                .addMessageParser(PingMessageParser())
                .addMessageParser(PongMessageParser())
                .addMessageParser(GroestlcoinTransactionMessageParser())
                .addMessageParser(VerAckMessageParser())
                .addMessageParser(VersionMessageParser())
                .addMessageParser(RejectMessageParser())

        bitcoinCore.addMessageSerializer(FilterLoadMessageSerializer())
                .addMessageSerializer(GetBlocksMessageSerializer())
                .addMessageSerializer(InvMessageSerializer())
                .addMessageSerializer(GetDataMessageSerializer())
                .addMessageSerializer(MempoolMessageSerializer())
                .addMessageSerializer(PingMessageSerializer())
                .addMessageSerializer(PongMessageSerializer())
                .addMessageSerializer(io.horizontalsystems.groestlcoinkit.network.messages.GroestlcoinTransactionMessageSerializer())
                .addMessageSerializer(VerAckMessageSerializer())
                .addMessageSerializer(VersionMessageSerializer())

        bitcoinCore.peerGroup.setNetworkMessageSerializer(bitcoinCore.networkMessageSerializer)
        bitcoinCore.peerGroup.setNetworkMessageParser(bitcoinCore.networkMessageParser)

        bitcoinCore.prependAddressConverter(GroestlcoinBase58AddressConverter(network.addressVersion, network.addressScriptVersion))

        return bitcoinCore
    }
}
