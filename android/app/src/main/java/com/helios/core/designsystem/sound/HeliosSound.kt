package com.helios.core.designsystem.sound

/**
 * Sound effect definitions (opt-in, default on).
 * Stub for M4 — actual audio file playback will be implemented with media APIs.
 *
 * All sounds: 32-bit float 48 kHz mono, < 250 ms, < 30 KB.
 */
object HeliosSound {
    /** Short click, 80 ms, low-pass filtered */
    const val TILE_TAP = "tile_tap"

    /** Soft thud, 120 ms, inverse envelope */
    const val SHEET_OPEN = "sheet_open"

    /** Bright ping, 200 ms, solar-frequency overtone */
    const val SUCCESS_CHIME = "success_chime"

    /** Subtle pop, 60 ms */
    const val SHARE_COPY = "share_copy"

    /** Soft sweep, 100 ms, pitch rises for light / falls for dark */
    const val THEME_TOGGLE = "theme_toggle"

    /** Glass clink, 150 ms */
    const val BRAND_SWITCH = "brand_switch"
}
