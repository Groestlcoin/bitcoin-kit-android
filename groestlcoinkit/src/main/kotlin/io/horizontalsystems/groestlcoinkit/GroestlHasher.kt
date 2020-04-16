package io.horizontalsystems.groestlcoinkit

import fr.cryptohash.Groestl512
import io.horizontalsystems.bitcoincore.core.IHasher
import java.util.*


class GroestlHasher : IHasher {
    private val algorithms = listOf(
            Groestl512(),
            Groestl512()
    )

    companion object
    {
        var native_library_loaded = false

        init {
            native_library_loaded = try {
                System.loadLibrary("groestl")
                true
            } catch (e: UnsatisfiedLinkError) {
                false
            } catch (e: Exception) {
                false
            }
        }
    }

    external fun groestldNative(input: ByteArray, offset: Int, length: Int): ByteArray

    fun groestldKotlin(data: ByteArray): ByteArray {
        var hash = data

        algorithms.forEach {
            hash = it.digest(hash)
        }

        return Arrays.copyOfRange(hash, 0, 32)
    }

    override fun hash(data: ByteArray): ByteArray {
        return if (native_library_loaded) groestldNative(data, 0, data.size) else groestldKotlin(data)
    }

}

fun groestlhash(data: ByteArray): ByteArray {
    return GroestlHasher().hash(data)
}