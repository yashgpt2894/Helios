package com.helios.core.designsystem.color

import androidx.compose.ui.graphics.Color

/**
 * P3 color ramps from shared-spec/design-tokens.json.
 * Light and dark variants per hue. Dynamic color support with brand-accent fallback.
 */
object HeliosColor {

    // --- Solar ---
    object Solar {
        val Light50 = Color(0xFFFFF9E6)
        val Light100 = Color(0xFFFFEFC2)
        val Light200 = Color(0xFFFFE094)
        val Light300 = Color(0xFFF0C674)
        val Light400 = Color(0xFFD4A843)
        val Light500 = Color(0xFFB88A2E)
        val Light600 = Color(0xFF9A6E1F)
        val Light700 = Color(0xFF7C5518)
        val Light800 = Color(0xFF5E3F12)
        val Light900 = Color(0xFF422C0C)
        val Light950 = Color(0xFF2A1B06)

        val Dark50 = Color(0xFF2A1B06)
        val Dark100 = Color(0xFF422C0C)
        val Dark200 = Color(0xFF5E3F12)
        val Dark300 = Color(0xFF7C5518)
        val Dark400 = Color(0xFF9A6E1F)
        val Dark500 = Color(0xFFB88A2E)
        val Dark600 = Color(0xFFD4A843)
        val Dark700 = Color(0xFFF0C674)
        val Dark800 = Color(0xFFFFE094)
        val Dark900 = Color(0xFFFFEFC2)
        val Dark950 = Color(0xFFFFF9E6)

        fun ramp(step: Int, isDark: Boolean): Color = if (isDark) when (step) {
            50 -> Dark50; 100 -> Dark100; 200 -> Dark200; 300 -> Dark300; 400 -> Dark400
            500 -> Dark500; 600 -> Dark600; 700 -> Dark700; 800 -> Dark800; 900 -> Dark900
            950 -> Dark950; else -> Dark500
        } else when (step) {
            50 -> Light50; 100 -> Light100; 200 -> Light200; 300 -> Light300; 400 -> Light400
            500 -> Light500; 600 -> Light600; 700 -> Light700; 800 -> Light800; 900 -> Light900
            950 -> Light950; else -> Light500
        }
    }

    // --- Flow ---
    object Flow {
        val Light50 = Color(0xFFE8F5E9)
        val Light100 = Color(0xFFC8E6C9)
        val Light200 = Color(0xFFA5D6A7)
        val Light300 = Color(0xFF7FB069)
        val Light400 = Color(0xFF5E9A4E)
        val Light500 = Color(0xFF4A7F3C)
        val Light600 = Color(0xFF3A6630)
        val Light700 = Color(0xFF2B4D24)
        val Light800 = Color(0xFF1E3819)
        val Light900 = Color(0xFF132510)
        val Light950 = Color(0xFF0A1508)

        val Dark50 = Color(0xFF0A1508)
        val Dark100 = Color(0xFF132510)
        val Dark200 = Color(0xFF1E3819)
        val Dark300 = Color(0xFF2B4D24)
        val Dark400 = Color(0xFF3A6630)
        val Dark500 = Color(0xFF4A7F3C)
        val Dark600 = Color(0xFF5E9A4E)
        val Dark700 = Color(0xFF7FB069)
        val Dark800 = Color(0xFFA5D6A7)
        val Dark900 = Color(0xFFC8E6C9)
        val Dark950 = Color(0xFFE8F5E9)

        fun ramp(step: Int, isDark: Boolean): Color = if (isDark) when (step) {
            50 -> Dark50; 100 -> Dark100; 200 -> Dark200; 300 -> Dark300; 400 -> Dark400
            500 -> Dark500; 600 -> Dark600; 700 -> Dark700; 800 -> Dark800; 900 -> Dark900
            950 -> Dark950; else -> Dark500
        } else when (step) {
            50 -> Light50; 100 -> Light100; 200 -> Light200; 300 -> Light300; 400 -> Light400
            500 -> Light500; 600 -> Light600; 700 -> Light700; 800 -> Light800; 900 -> Light900
            950 -> Light950; else -> Light500
        }
    }

    // --- Grid Import ---
    object GridImport {
        val Light50 = Color(0xFFFFF3E0)
        val Light100 = Color(0xFFFFE0B2)
        val Light200 = Color(0xFFFFCC80)
        val Light300 = Color(0xFFFFB74D)
        val Light400 = Color(0xFFFFA726)
        val Light500 = Color(0xFFF57C00)
        val Light600 = Color(0xFFE65100)
        val Light700 = Color(0xFFBF360C)
        val Light800 = Color(0xFF8D2808)
        val Light900 = Color(0xFF5C1A05)
        val Light950 = Color(0xFF380F03)

        val Dark50 = Color(0xFF380F03)
        val Dark100 = Color(0xFF5C1A05)
        val Dark200 = Color(0xFF8D2808)
        val Dark300 = Color(0xFFBF360C)
        val Dark400 = Color(0xFFE65100)
        val Dark500 = Color(0xFFF57C00)
        val Dark600 = Color(0xFFFFA726)
        val Dark700 = Color(0xFFFFB74D)
        val Dark800 = Color(0xFFFFCC80)
        val Dark900 = Color(0xFFFFE0B2)
        val Dark950 = Color(0xFFFFF3E0)

        fun ramp(step: Int, isDark: Boolean): Color = if (isDark) when (step) {
            50 -> Dark50; 100 -> Dark100; 200 -> Dark200; 300 -> Dark300; 400 -> Dark400
            500 -> Dark500; 600 -> Dark600; 700 -> Dark700; 800 -> Dark800; 900 -> Dark900
            950 -> Dark950; else -> Dark500
        } else when (step) {
            50 -> Light50; 100 -> Light100; 200 -> Light200; 300 -> Light300; 400 -> Light400
            500 -> Light500; 600 -> Light600; 700 -> Light700; 800 -> Light800; 900 -> Light900
            950 -> Light950; else -> Light500
        }
    }

    // --- Grid Export ---
    object GridExport {
        val Light50 = Color(0xFFE3F2FD)
        val Light100 = Color(0xFFBBDEFB)
        val Light200 = Color(0xFF90CAF9)
        val Light300 = Color(0xFF5D8AA8)
        val Light400 = Color(0xFF4A7A9E)
        val Light500 = Color(0xFF3D6584)
        val Light600 = Color(0xFF2F4F6A)
        val Light700 = Color(0xFF223B50)
        val Light800 = Color(0xFF172A38)
        val Light900 = Color(0xFF0D1A22)
        val Light950 = Color(0xFF060E13)

        val Dark50 = Color(0xFF060E13)
        val Dark100 = Color(0xFF0D1A22)
        val Dark200 = Color(0xFF172A38)
        val Dark300 = Color(0xFF223B50)
        val Dark400 = Color(0xFF2F4F6A)
        val Dark500 = Color(0xFF3D6584)
        val Dark600 = Color(0xFF4A7A9E)
        val Dark700 = Color(0xFF5D8AA8)
        val Dark800 = Color(0xFF90CAF9)
        val Dark900 = Color(0xFFBBDEFB)
        val Dark950 = Color(0xFFE3F2FD)

        fun ramp(step: Int, isDark: Boolean): Color = if (isDark) when (step) {
            50 -> Dark50; 100 -> Dark100; 200 -> Dark200; 300 -> Dark300; 400 -> Dark400
            500 -> Dark500; 600 -> Dark600; 700 -> Dark700; 800 -> Dark800; 900 -> Dark900
            950 -> Dark950; else -> Dark500
        } else when (step) {
            50 -> Light50; 100 -> Light100; 200 -> Light200; 300 -> Light300; 400 -> Light400
            500 -> Light500; 600 -> Light600; 700 -> Light700; 800 -> Light800; 900 -> Light900
            950 -> Light950; else -> Light500
        }
    }

    // --- Battery ---
    object Battery {
        val Light50 = Color(0xFFF5F0E6)
        val Light100 = Color(0xFFE6DCC8)
        val Light200 = Color(0xFFD4C5A2)
        val Light300 = Color(0xFFC5A572)
        val Light400 = Color(0xFFA68A52)
        val Light500 = Color(0xFF856B32)
        val Light600 = Color(0xFF6B5426)
        val Light700 = Color(0xFF523F1C)
        val Light800 = Color(0xFF3A2D14)
        val Light900 = Color(0xFF251D0D)
        val Light950 = Color(0xFF151006)

        val Dark50 = Color(0xFF151006)
        val Dark100 = Color(0xFF251D0D)
        val Dark200 = Color(0xFF3A2D14)
        val Dark300 = Color(0xFF523F1C)
        val Dark400 = Color(0xFF6B5426)
        val Dark500 = Color(0xFF856B32)
        val Dark600 = Color(0xFFA68A52)
        val Dark700 = Color(0xFFC5A572)
        val Dark800 = Color(0xFFD4C5A2)
        val Dark900 = Color(0xFFE6DCC8)
        val Dark950 = Color(0xFFF5F0E6)

        fun ramp(step: Int, isDark: Boolean): Color = if (isDark) when (step) {
            50 -> Dark50; 100 -> Dark100; 200 -> Dark200; 300 -> Dark300; 400 -> Dark400
            500 -> Dark500; 600 -> Dark600; 700 -> Dark700; 800 -> Dark800; 900 -> Dark900
            950 -> Dark950; else -> Dark500
        } else when (step) {
            50 -> Light50; 100 -> Light100; 200 -> Light200; 300 -> Light300; 400 -> Light400
            500 -> Light500; 600 -> Light600; 700 -> Light700; 800 -> Light800; 900 -> Light900
            950 -> Light950; else -> Light500
        }
    }

    // --- Alert ---
    object Alert {
        val Light50 = Color(0xFFFBEAE4)
        val Light100 = Color(0xFFF5CDBF)
        val Light200 = Color(0xFFEDAB94)
        val Light300 = Color(0xFFD97757)
        val Light400 = Color(0xFFC45E3D)
        val Light500 = Color(0xFFA8492A)
        val Light600 = Color(0xFF8A381F)
        val Light700 = Color(0xFF6D2A16)
        val Light800 = Color(0xFF501E10)
        val Light900 = Color(0xFF36140B)
        val Light950 = Color(0xFF1F0A06)

        val Dark50 = Color(0xFF1F0A06)
        val Dark100 = Color(0xFF36140B)
        val Dark200 = Color(0xFF501E10)
        val Dark300 = Color(0xFF6D2A16)
        val Dark400 = Color(0xFF8A381F)
        val Dark500 = Color(0xFFA8492A)
        val Dark600 = Color(0xFFC45E3D)
        val Dark700 = Color(0xFFD97757)
        val Dark800 = Color(0xFFEDAB94)
        val Dark900 = Color(0xFFF5CDBF)
        val Dark950 = Color(0xFFFBEAE4)

        fun ramp(step: Int, isDark: Boolean): Color = if (isDark) when (step) {
            50 -> Dark50; 100 -> Dark100; 200 -> Dark200; 300 -> Dark300; 400 -> Dark400
            500 -> Dark500; 600 -> Dark600; 700 -> Dark700; 800 -> Dark800; 900 -> Dark900
            950 -> Dark950; else -> Dark500
        } else when (step) {
            50 -> Light50; 100 -> Light100; 200 -> Light200; 300 -> Light300; 400 -> Light400
            500 -> Light500; 600 -> Light600; 700 -> Light700; 800 -> Light800; 900 -> Light900
            950 -> Light950; else -> Light500
        }
    }

    // --- Neutral ---
    object Neutral {
        val Light50 = Color(0xFFF8F6F0)
        val Light100 = Color(0xFFF4F1EA)
        val Light200 = Color(0xFFEFECE5)
        val Light300 = Color(0xFFE8E4DB)
        val Light400 = Color(0xFFDCD6C8)
        val Light500 = Color(0xFFBDB6A6)
        val Light600 = Color(0xFFA59F90)
        val Light700 = Color(0xFF7A7568)
        val Light800 = Color(0xFF544F47)
        val Light900 = Color(0xFF2A2A2D)
        val Light950 = Color(0xFF0B0B0C)

        val Dark50 = Color(0xFF070708)
        val Dark100 = Color(0xFF0B0B0C)
        val Dark200 = Color(0xFF0F0F10)
        val Dark300 = Color(0xFF141416)
        val Dark400 = Color(0xFF1A1A1C)
        val Dark500 = Color(0xFF1C1C1E)
        val Dark600 = Color(0xFF2A2A2D)
        val Dark700 = Color(0xFF3A3A3E)
        val Dark800 = Color(0xFF544F47)
        val Dark900 = Color(0xFFA59F90)
        val Dark950 = Color(0xFFF4F1EA)

        fun ramp(step: Int, isDark: Boolean): Color = if (isDark) when (step) {
            50 -> Dark50; 100 -> Dark100; 200 -> Dark200; 300 -> Dark300; 400 -> Dark400
            500 -> Dark500; 600 -> Dark600; 700 -> Dark700; 800 -> Dark800; 900 -> Dark900
            950 -> Dark950; else -> Dark500
        } else when (step) {
            50 -> Light50; 100 -> Light100; 200 -> Light200; 300 -> Light300; 400 -> Light400
            500 -> Light500; 600 -> Light600; 700 -> Light700; 800 -> Light800; 900 -> Light900
            950 -> Light950; else -> Light500
        }
    }
}
