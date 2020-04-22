package io.horizontalsystems.groestlcoinkit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.horizontalsystems.bitcoincore.AbstractKit
import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.blocks.validators.*
import io.horizontalsystems.bitcoincore.core.Bip
import io.horizontalsystems.bitcoincore.managers.*
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.network.Network
import io.horizontalsystems.bitcoincore.storage.CoreDatabase
import io.horizontalsystems.bitcoincore.storage.Storage
import io.horizontalsystems.bitcoincore.utils.Base58AddressConverter
import io.horizontalsystems.bitcoincore.utils.PaymentAddressParser
import io.horizontalsystems.bitcoincore.utils.SegwitAddressConverter
import io.horizontalsystems.groestlcoinkit.managers.ChainzApi
import io.horizontalsystems.groestlcoinkit.utils.GroestlcoinBase58AddressConverter
import io.horizontalsystems.groestlcoinkit.validators.DarkGravityWaveValidator
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.Single

class GroestlcoinKit : AbstractKit {
    enum class NetworkType {
        MainNet,
        TestNet,
    }

    interface Listener : BitcoinCore.Listener

    override var bitcoinCore: BitcoinCore
    override var network: Network

    var listener: Listener? = null
        set(value) {
            field = value
            bitcoinCore.listener = value
        }

    constructor(
            context: Context,
            words: List<String>,
            walletId: String,
            networkType: NetworkType = NetworkType.MainNet,
            peerSize: Int = 10,
            syncMode: BitcoinCore.SyncMode = BitcoinCore.SyncMode.Api(),
            confirmationsThreshold: Int = 6,
            bip: Bip = Bip.BIP44
    ) : this(context, Mnemonic().toSeed(words), walletId, networkType, peerSize, syncMode, confirmationsThreshold, bip)

    constructor(
            context: Context,
            seed: ByteArray,
            walletId: String,
            networkType: NetworkType = NetworkType.MainNet,
            peerSize: Int = 10,
            syncMode: BitcoinCore.SyncMode = BitcoinCore.SyncMode.Api(),
            confirmationsThreshold: Int = 6,
            bip: Bip = Bip.BIP44
    ) {
        val database = CoreDatabase.getInstance(context, getDatabaseName(networkType, walletId))
        val storage = Storage(database)
        var initialSyncUrl = ""

        network = when (networkType) {
            NetworkType.MainNet -> {
                initialSyncUrl = "https://chainz.cryptoid.info/grs"
                MainNetGroestlcoin()
            }
            NetworkType.TestNet -> {
                initialSyncUrl = "https://chainz.cryptoid.info/grs-test"
                TestNetGroestlcoin()
            }
        }

        val paymentAddressParser = PaymentAddressParser("groestlcoin", removeScheme = true)
        val initialSyncApi = ChainzApi(initialSyncUrl)

        bitcoinCore = GroestlcoinCoreBuilder()
                .setContext(context)
                .setSeed(seed)
                .setNetwork(network)
                .setBip(bip)
                .setPaymentAddressParser(paymentAddressParser)
                .setPeerSize(peerSize)
                .setSyncMode(syncMode)
                .setConfirmationThreshold(confirmationsThreshold)
                .setStorage(storage)
                .setInitialSyncApi(initialSyncApi)
                .build()

        //  extending bitcoinCore

        val blockHelper = BlockValidatorHelper(storage)

        val blockValidatorSet = BlockValidatorSet()
        blockValidatorSet.addBlockValidator(ProofOfWorkValidator())
        val blockValidatorChain = BlockValidatorChain()

        if (network is MainNetGroestlcoin) {
            blockValidatorChain.add(DarkGravityWaveValidator(blockHelper, heightInterval, targetTimespan, maxTargetBits, 99999))
        } else {
            blockValidatorChain.add(DarkGravityWaveValidator(blockHelper, heightInterval, targetTimespan, maxTargetBits, 99999))
        }

        blockValidatorSet.addBlockValidator(blockValidatorChain)

        val bech32AddressConverter = SegwitAddressConverter(network.addressSegwitHrp)
        val base58AddressConverter = GroestlcoinBase58AddressConverter(network.addressVersion, network.addressScriptVersion)

        bitcoinCore.removeAllAddressConverters()
        bitcoinCore.prependAddressConverter(bech32AddressConverter)
        bitcoinCore.prependAddressConverter(base58AddressConverter)


        when (bip) {
            Bip.BIP44 -> {
                bitcoinCore.addRestoreKeyConverter(Bip44RestoreKeyConverter(base58AddressConverter))
                bitcoinCore.addRestoreKeyConverter(Bip49RestoreKeyConverter(base58AddressConverter))
                bitcoinCore.addRestoreKeyConverter(Bip84RestoreKeyConverter(bech32AddressConverter))
            }
            Bip.BIP49 -> {
                bitcoinCore.addRestoreKeyConverter(Bip49RestoreKeyConverter(base58AddressConverter))
            }
            Bip.BIP84 -> {
                bitcoinCore.addRestoreKeyConverter(Bip84RestoreKeyConverter(bech32AddressConverter))
            }
        }
    }

    fun transactions(fromHash: String? = null, limit: Int? = null): Single<List<TransactionInfo>> {
        return bitcoinCore.transactions(fromHash, limit)
    }

    companion object {
        const val maxTargetBits: Long = 0x1e0fffff

        const val targetSpacing = 60              // 1.0 min. for mining 1 Block
        const val targetTimespan = 1440L          // 1 hour for 60 blocks
        const val heightInterval = targetTimespan / targetSpacing

        private fun getDatabaseName(networkType: NetworkType, walletId: String): String = "Groestlcoin-${networkType.name}-$walletId"

        fun clear(context: Context, networkType: NetworkType, walletId: String) {
            SQLiteDatabase.deleteDatabase(context.getDatabasePath(getDatabaseName(networkType, walletId)))
        }
    }

}
