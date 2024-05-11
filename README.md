# Kotlin Zcash Address Parser

A Kotlin implementation of zcash address parsers

Parsing and serialization for Zcash addresses, including:

* base58-encoded transparent p2pkh and p2sh addresses
* bech32-encoded Sapling addresses
* bech32m-encoded ZIP 320 TEX addresses
* Unified Addresses

The main function in this library, decodeAddress() takes a string and a network. This function is part of a sealed class,

```Kotlin
sealed class ZcashAddress {
    class P2pkh(val data: ByteArray): ZcashAddress()
    class P2sh(val data: ByteArray): ZcashAddress()
    class Sapling(val data: ByteArray): ZcashAddress()
    class Unified(val data: UnifiedAddress): ZcashAddress()
    class Tex(val data: ByteArray): ZcashAddress()
}
```

returning an address of the appropriate type.

Note that only one of these classes will be non-nil.

The Network data class

```Kotlin
data class Network(
        val p2pkhLead: ByteArray,
        val p2shLead: ByteArray,
        val texHRP: String,
        val saplingHRP: String,
        val unifiedHRP: String,
        val unifiedR1HRP: String
)
```

provides the encoding prefixes for the Mainnet, Testnet, and Regtest networks.

Implementation patterns can be found in the ZCashAddressTest.kts found in this repository.

A set of convenience methods: `ZcashAddress.decodeP2pkh()`, `ZcashAddress.decodeP2sh()`, `ZcashAddress.decodeSapling()`,
and `ZcashAddress.decodeOrchard()` have been provided for direct decoding of specific address or receiver types.

Test Vectors in the file TestVectors.kt are a port of the vectors found at <https://github.com/zcash/zcash-test-vectors/blob/master/test-vectors/rust/unified_address.rs>

This repository Currently includes a copy of F4jumble.kt take from the repository
<https://github.com/MarcoEzekiel/kotlin-f4jumble>. This will be removed once i get this up on maven central.
