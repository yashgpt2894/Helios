package com.helios.core.data.repository

import com.helios.core.domain.model.Brand

/**
 * Brand repository: resolves a brand from a deep link, a snapshot payload or settings.
 *
 * Registry ids and accent pairs match `src/services/brand.ts` exactly, because a shared
 * snapshot carries the brand id: a registry that differs resolves another person's link
 * to the wrong brand. `accent` is the dark-theme accent and `accentLight` the
 * light-theme accent, which is the same field meaning the PWA uses.
 */
object BrandRepository {

    val helios = Brand(
        id = "helios",
        name = "helios\u00B0",
        legalName = "helios\u00B0 energy",
        accent = "#F0C674",
        accentLight = "#B8862E",
        mark = "helios",
        tagline = "Precision energy intelligence for your solar array."
    )

    private val registry: Map<String, Brand> = listOf(
        helios,
        Brand(
            id = "voltcraft",
            name = "Voltcraft",
            legalName = "Voltcraft Solar, Inc.",
            accent = "#A78BFA",
            accentLight = "#6D28D9",
            mark = "text",
            textMark = "V",
            supportEmail = "support@voltcraft.example",
            tagline = "Your solar, refined."
        ),
        Brand(
            id = "sunworks",
            name = "SunWorks",
            legalName = "SunWorks Energy Co.",
            accent = "#38BDF8",
            accentLight = "#0369A1",
            mark = "text",
            textMark = "S",
            supportEmail = "help@sunworks.example",
            tagline = "Powering your home, smarter."
        ),
        Brand(
            id = "meridian",
            name = "Meridian",
            legalName = "Meridian Renewables",
            accent = "#FB923C",
            accentLight = "#C2410C",
            mark = "text",
            textMark = "M",
            supportEmail = "care@meridian.example",
            tagline = "Solar, perfectly tuned."
        )
    ).associateBy { it.id }

    private var currentBrand: Brand = helios

    /** Registry order, for the settings brand row. */
    fun all(): List<Brand> = listOf("helios", "voltcraft", "sunworks", "meridian").mapNotNull { registry[it] }

    fun resolve(from: String?): Brand {
        val brand = from?.lowercase()?.let { registry[it] } ?: helios
        currentBrand = brand
        return brand
    }

    fun current(): Brand = currentBrand
}
