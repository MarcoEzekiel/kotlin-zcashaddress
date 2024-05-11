/**
 */
package cash.z.zcashaddress

import kotlin.text.toHexString
import kotlin.system.exitProcess
import kotlin.math.exp
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import cash.z.zcashaddress.UnifiedAddress
import cash.z.zcashaddress.ZcashAddress

@kotlin.ExperimentalStdlibApi
class ZCashAddressTest {

   @Test
   fun testZCashAddressTex() {
       for (texVector in TestVectors.tAddrData()) {
           
            var decoded = ZcashAddress.decodeAddress(texVector.texAddr, mainnet())
            when (decoded) {
                is ZcashAddress.Tex -> 
                    assertEquals(decoded.data.toHexString(), texVector.taddrBytesHex)
                else -> 
                    fail("Not a TEX address")
            }  
       }
   }

    @Test
    fun testZCashAddressUnifiedDecodeAddress() {
        for (example in TestVectors.unifiedTestVectors()) {
           
            var decoded = ZcashAddress.decodeAddress(example.uaddr, mainnet())

            when (decoded) {
                is ZcashAddress.Unified -> {
                    if (example.p2pkhBytesHex != null){
                        assertEquals(example.p2pkhBytesHex, decoded.data.p2pkh?.toHexString())
                    } 
                    if (example.p2shBytesHex != null){
                        assertEquals(example.p2shBytesHex, decoded.data.p2sh?.toHexString())
                    } 
                    if (example.saplingBytesHex != null){
                        assertEquals(example.saplingBytesHex, decoded.data.sapling?.toHexString())
                    } 
                    if (example.orchardBytesHex != null){
                        assertEquals(example.orchardBytesHex, decoded.data.orchard?.toHexString())
                    }  
                    if(example.unknownBytes != null && example.unknownTypecode != -1){
                       
                        decoded.data.unknown.forEach { entry ->
                            assertEquals(example.unknownTypecode, entry.key.toInt())
                            assertEquals(example.unknownBytes, entry.value.toHexString())
                        }
                    }
                   
                }
               
                else -> 
                    fail("Not a TEX address")
            }  
        
        }
    }
}
