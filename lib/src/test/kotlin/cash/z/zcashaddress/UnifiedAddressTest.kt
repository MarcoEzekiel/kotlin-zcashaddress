/**
 */
package cash.z.zcashaddress

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@kotlin.ExperimentalStdlibApi
class UnifiedAddressTest {

        @Test
        fun testUnifiedAddress() {

                for (example in TestVectors.unifiedTestVectors()) {

                        var decoded = UnifiedAddress.decodeUnified(example.uaddr, "u")
                        var encoded = UnifiedAddress.encodeUnified(decoded, "u")

                        if (example.p2pkhBytesHex != null) {
                                assertEquals(example.p2pkhBytesHex, decoded.p2pkh?.toHexString())
                        }
                        if (example.p2shBytesHex != null) {
                                assertEquals(example.p2shBytesHex, decoded.p2sh?.toHexString())
                        }
                        if (example.saplingBytesHex != null) {
                                assertEquals(
                                                example.saplingBytesHex,
                                                decoded.sapling?.toHexString()
                                )
                        }
                        if (example.orchardBytesHex != null) {
                                assertEquals(
                                                example.orchardBytesHex,
                                                decoded.orchard?.toHexString()
                                )
                        }

                        assertEquals(example.uaddr, encoded)
                }
        }
}
