package com.helios.core.data

import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.repository.ShareRepository
import com.helios.core.domain.model.SnapshotPayload
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Byte-level parity with the PWA v1 snapshot.
 *
 * The expectation is the literal string `JSON.stringify` would produce for the same
 * input, base64url encoded the way `src/services/share.ts` encodes it. `lifeKwh` and
 * `soc` are integral, `fc` entries are rounded, and `br` is omitted for the helios
 * brand: a `Double` holding 18420.0 would print "18420.0" and break the link.
 */
class SnapshotParityTest {

    private val timestamp = 1_760_000_000_000L

    private fun telemetry() = FixtureData.daytimeTelemetry(timestamp).copy(gridExportW = 760.0)

    private fun decode(encoded: String): String =
        String(Base64.getUrlDecoder().decode(encoded), Charsets.UTF_8)

    @Test
    fun `payload is byte identical to the PWA encoding for the same reading`() {
        val payload = ShareRepository.buildSnapshot(
            telemetry = telemetry(),
            locationLabel = "San Francisco, CA",
            forecastDays = listOf(37.8, 34.2),
            brandId = "helios"
        )
        val expected =
            """{"v":1,"ts":1760000000000,"loc":"San Francisco, CA","ac":4.23,"todayKwh":18.4,"lifeKwh":18421,"soc":62,"selfUse":82,"fc":[38,34]}"""

        assertEquals(expected, decode(ShareRepository.encodeSnapshot(payload)))
        assertNull("helios omits the brand field", payload.br)
        assertEquals(18421, payload.lifeKwh)
        assertEquals(62, payload.soc)
        assertEquals(82, payload.selfUse)
    }

    @Test
    fun `a non-helios brand is carried and no forecast means no fc key`() {
        val payload = ShareRepository.buildSnapshot(
            telemetry = telemetry(),
            locationLabel = "Lisbon, Portugal",
            forecastDays = null,
            brandId = "voltcraft"
        )
        val json = decode(ShareRepository.encodeSnapshot(payload))

        assertEquals("voltcraft", payload.br)
        assertTrue("keys stay in PWA order", json.endsWith(""""selfUse":82,"br":"voltcraft"}"""))
        assertTrue("fc is omitted when there is no forecast", !json.contains("\"fc\""))
    }

    @Test
    fun `round trip preserves every field`() {
        val payload = SnapshotPayload(
            v = 1,
            ts = timestamp,
            loc = "San Francisco, CA",
            ac = 4.23,
            todayKwh = 18.4,
            lifeKwh = 18421,
            soc = 62,
            selfUse = 82,
            fc = listOf(38, 34, 22),
            br = "sunworks"
        )

        val decoded = ShareRepository.decodeSnapshot(ShareRepository.encodeSnapshot(payload))

        assertNotNull(decoded)
        assertEquals(payload, decoded)
    }

    @Test
    fun `decoding rejects a version the app cannot read`() {
        val versionTwo = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"v":2,"ts":1}""".toByteArray(Charsets.UTF_8))

        assertNull(ShareRepository.decodeSnapshot(versionTwo))
        assertNull(ShareRepository.decodeSnapshot("not base64 at all"))
    }

    @Test
    fun `unknown keys from a newer PWA are ignored rather than fatal`() {
        val forwardCompatibleJson =
            """{"v":1,"ts":5,"loc":"x","ac":1.0,"todayKwh":1.0,"lifeKwh":1,"soc":50,"selfUse":50,"futureKey":"ignore me"}"""
        val forwardCompatible = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(forwardCompatibleJson.toByteArray(Charsets.UTF_8))

        val decoded = ShareRepository.decodeSnapshot(forwardCompatible)

        assertNotNull(decoded)
        assertEquals(50, decoded!!.soc)
    }
}
