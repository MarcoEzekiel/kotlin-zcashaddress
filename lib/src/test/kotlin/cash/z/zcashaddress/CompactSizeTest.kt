/*
 * This source file was generated by the Gradle 'init' task
 */
package cash.z.zcashaddress

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFails
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import com.google.common.base.Utf8
import org.checkerframework.checker.units.qual.radians
@OptIn(ExperimentalStdlibApi::class)
class CompactSizeTest {
   
    @Test
    fun testZCompactSize() {
        var allowU64 = false
        for (n in listOf(0UL, 1UL, 252UL, 253UL, 254UL, 255UL, 256UL, 0xFFFEUL, 0xFFFFUL, 0x010000UL, 0x010001UL, 0x02000000UL)) {
            
            val encoding = CompactSize.writeCompactSize(n, allowU64)
           
            val (decoded, remaining) = CompactSize.parseCompactSize(encoding, allowU64)
    
            assertEquals(n, decoded)
            assertTrue(remaining.size == 0)
        }
        
        assertFailsWith(
            exceptionClass = IllegalArgumentException::class,
            block = { assertParseFails(byteArrayOf(0xFE.toByte(), 0x01, 0x00, 0x00, 0x02), false) }
        )
        assertFailsWith(
            exceptionClass = IllegalArgumentException::class,
            block = { assertParseFails(byteArrayOf(0xFF.toByte(), 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00), false) }
        )
        assertFailsWith(
            exceptionClass = IllegalArgumentException::class,
            block = { assertParseFails(byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()), false) }
        )
        allowU64 = true
        for (n in listOf(0xFFFFFFFEUL, 0xFFFFFFFFUL, 0x0100000000UL, 0xFFFFFFFFFFFFFFFFUL)) {
            val encoding = CompactSize.writeCompactSize(n, allowU64)
            val (decoded, remaining) = CompactSize.parseCompactSize(encoding, allowU64)
         
            assertEquals(n, decoded)
            assertTrue(remaining.size == 0)
        } 
    }

    fun assertParseFails(encoding: ByteArray, allowU64: Boolean) {
        val (result, _,) = CompactSize.parseCompactSize(encoding, allowU64)
        if (result != 0UL) {
            throw IllegalStateException("parseCompactSize($encoding) failed to return an error")
        }
    }
}