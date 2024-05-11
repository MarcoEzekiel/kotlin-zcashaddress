/*
* Parsing and serialization for Zcash addresses, including:
* base58-encoded transparent p2pkh and p2sh addresses
* bech32-encoded Sapling addresses
* bech32m-encoded ZIP 320 TEX addresses
* Unified Addresses
 */

package cash.z.zcashaddress

import fr.acinq.bitcoin.Base58
import fr.acinq.bitcoin.Bech32
import fr.acinq.bitcoin.Base58Check
import java.beans.beancontext.BeanContext
import cash.z.zcashaddress.UnifiedAddress
import org.checkerframework.checker.units.qual.s

/**
 * A parsed Zcash address. Fields of this structure are mutually exclusive; only one field may be non-nil.
 */
data class Network(
        val p2pkhLead: ByteArray,
        val p2shLead: ByteArray,
        val texHRP: String,
        val saplingHRP: String,
        val unifiedHRP: String,
        val unifiedR1HRP: String
)

/**
 * The Zcash mainnet network constants.
 */
fun mainnet(): Network {
    return Network(
            byteArrayOf(0x1c.toByte(), 0xb8.toByte()),
            byteArrayOf(0x1c.toByte(), 0xbd.toByte()),
            "tex",
            "zs",
            "u",
            "ur"
    )
}

/**
 * The Zcash testnet network constants.
 */
fun testnet(): Network {
    return Network(
            byteArrayOf(0x1c.toByte(), 0x25.toByte()),
            byteArrayOf(0x1c.toByte(), 0xba.toByte()),
            "textest",
            "ztestsapling",
            "utest",
            "urtest"
    )
}
/**
 * The Zcash regtest network constants.
 */
fun regtest(): Network {
    return Network(
            byteArrayOf(0x1c.toByte(), 0x25.toByte()),
            byteArrayOf(0x1c.toByte(), 0xba.toByte()),
            "texregtest",
            "zregtestsapling",
            "uregtest",
            "urregtest"
    )
}


sealed class ZcashAddress {
    class P2pkh(val data: ByteArray): ZcashAddress()
    class P2sh(val data: ByteArray): ZcashAddress()
    class Sapling(val data: ByteArray): ZcashAddress()
    class Unified(val data: UnifiedAddress): ZcashAddress()
    class Tex(val data: ByteArray): ZcashAddress()
   
    companion object {
  
        /**
         * ZcashAddressDecodeAddress is the master function for decoding all zcash address types.
         * It returns a ZcashAddress; only one of the fields of the returned struct will be non-nil.
         */
        fun decodeAddress(address: String, network: Network): ZcashAddress? {
            try {
                // Try base58 decoding. If the prefix matches a the transparent address prefix bytes, this
                // is a Zcash transparent address.
                // Ignore the error from attempting to CheckDecode, return in final error if all other decodes fail
                val (version,decoded) = Base58Check.decode(address)
                
                if ( version ==  network.p2pkhLead[0] && decoded[0] == network.p2pkhLead[1] && decoded.size == 21) {
                    var p2pkh = decoded.copyOfRange(1, decoded.size)
                    return  P2pkh(p2pkh)
                } else if (version == network.p2shLead[0]  && decoded[0] == network.p2shLead[1] && decoded.size == 21) {
                    var p2sh = decoded.copyOfRange(1, decoded.size)
                    return P2sh(p2sh)
                }
            } catch (e: IllegalArgumentException) {
                
            }
        
            try {
                // Bech32m decoding currently for tex addresses
                // Ignore the error from attempting to decode, return in final error if all other decodes fail
                val (humanReadablePrefix, bech32mDecodedAddress, bech32Version) = Bech32.decode(address)
          
                if ( bech32Version == Bech32.Encoding.Bech32m) {
                    if (humanReadablePrefix == network.texHRP ) {  
                        var tex = Bech32.five2eight(bech32mDecodedAddress, 0)    
                        return Tex(tex)
                    } else if (humanReadablePrefix == network.unifiedR1HRP) {
                        // Attempt unified R1 decoding
                        throw UnsupportedOperationException("Unified address revision 1 decoding not yet supported")
                    }
                } else if (bech32Version == Bech32.Encoding.Bech32 && humanReadablePrefix == network.saplingHRP) {
                    // This might be Sapling? Check for the "z" HRP
                    Bech32.five2eight(bech32mDecodedAddress,0)
                    throw UnsupportedOperationException("Z-prefixed address type not yet supported")
                }
            } catch (e: IllegalArgumentException) {
               println("did not convert")
               println(e)
            }

            try {
                var unified = UnifiedAddress.decodeUnified(address, "u")
                return Unified(unified)
            } catch(e: Exception) {

            }
            
            return null
        }

        /**
        * Convenience function for decoding transparent p2pkh addresses.
        * This will decode the 20-byte payload of:
        * A base58-encoded p2pkh address
        * The p2pkh receiver of a Unified Address
        * If `allowTex == true`, a ZIP 320 TEX address
        *
        * or will return `nil` if the address does not contain a transparent public key hash.
        */
        fun decodeP2pkh(address: String, network: Network, allowTex: Boolean): ByteArray? {
            val decoded = decodeAddress(address, network)
            
            return when (decoded) {
                is P2pkh -> decoded.data
                is Tex -> if (allowTex) { decoded.data } else { null }
                is Unified -> decoded.data.p2pkh
                else -> null
            }
        }
        /** 
        * Convenience function for decoding transparent p2sh addresses.
        *
        * This will decode the 20-byte payload of:
        * A base58-encoded p2sh address
        * The p2sh receiver of a Unified Address
        *
        * or will return `nil` if the address does not contain a transparent script hash.
        */
        fun decodeP2sh(address: String, network: Network): ByteArray? {
            val decoded = decodeAddress(address, network)
            return when (decoded) {
                is P2sh -> decoded.data
                is Unified -> decoded.data.p2sh
                else -> null
            }
        }
        /**
        * Convenience function for decoding Sapling addresses.
        *
        * This will decode the 43-byte payload of:
        * A bech32-encoded Sapling address
        * The Sapling receiver of a Unified Address
        *
        * or will return `nil` if the address does not contain a Sapling address.
        */
        fun decodeSapling(address: String, network: Network): ByteArray? {
            val decoded = decodeAddress(address, network)
            return when (decoded) {
               is Unified -> decoded.data.sapling
               else -> null
            }
        }
        /** 
        * Convenience function for decoding Orchard addresses.
        *
        * This will decode the 43-byte payload of:
        * The Orchard receiver of a Unified Address
        *
        * or will return `nil` if the address is not a Unified address or does not contain
        * an Orchard receiver.
        */
        fun decodeOrchard(address: String, network: Network): ByteArray? {
            val decoded = decodeAddress(address, network)
            return when (decoded) {
               is Unified -> decoded.data.orchard
               else -> null
            }
        }
    }
}

