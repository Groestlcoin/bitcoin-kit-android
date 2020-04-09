package io.horizontalsystems.groestlcoinkit.transactions.builder

import io.horizontalsystems.bitcoincore.core.IPluginData
import io.horizontalsystems.bitcoincore.core.IRecipientSetter
import io.horizontalsystems.bitcoincore.models.*
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.storage.InputToSign
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoincore.transactions.builder.*
import io.horizontalsystems.bitcoincore.transactions.scripts.OpCodes
import io.horizontalsystems.bitcoincore.transactions.scripts.ScriptType
import io.horizontalsystems.groestlcoinkit.storage.GroestlcoinFullTransaction

class GroestlcoinTransactionBuilder(
        private val recipientSetter: IRecipientSetter,
        private val outputSetter: OutputSetter,
        private val inputSetter: InputSetter,
        private val signer: TransactionSigner,
        private val lockTimeSetter: LockTimeSetter) : TransactionBuilder(recipientSetter, outputSetter, inputSetter, signer, lockTimeSetter) {

    override fun buildTransaction(toAddress: String, value: Long, feeRate: Int, senderPay: Boolean, sortType: TransactionDataSortType, pluginData: Map<Byte, IPluginData>): FullTransaction {
        val mutableTransaction = GroestlcoinMutableTransaction()

        recipientSetter.setRecipient(mutableTransaction, toAddress, value, pluginData, false)
        inputSetter.setInputs(mutableTransaction, feeRate, senderPay, sortType)
        lockTimeSetter.setLockTime(mutableTransaction)

        outputSetter.setOutputs(mutableTransaction, sortType)
        signer.sign(mutableTransaction)

        return mutableTransaction.build()
    }

    override fun buildTransaction(unspentOutput: UnspentOutput, toAddress: String, feeRate: Int, sortType: TransactionDataSortType): FullTransaction {
        val mutableTransaction = GroestlcoinMutableTransaction(false)

        recipientSetter.setRecipient(mutableTransaction, toAddress, unspentOutput.output.value, mapOf(), false)
        inputSetter.setInputs(mutableTransaction, unspentOutput, feeRate)
        lockTimeSetter.setLockTime(mutableTransaction)

        outputSetter.setOutputs(mutableTransaction, sortType)
        signer.sign(mutableTransaction)

        return mutableTransaction.build()
    }
}
