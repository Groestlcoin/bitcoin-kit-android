package io.horizontalsystems.groestlcoinkit.transactions.builder

import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.transactions.builder.MutableTransaction
import io.horizontalsystems.groestlcoinkit.storage.GroestlcoinFullTransaction

open class GroestlcoinMutableTransaction(isOutgoing: Boolean = true): MutableTransaction(isOutgoing) {

    override fun build(): FullTransaction {
        return GroestlcoinFullTransaction(transaction, inputsToSign.map { it.input }, outputs)
    }

}
