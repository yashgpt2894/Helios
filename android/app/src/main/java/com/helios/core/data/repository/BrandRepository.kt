package com.helios.core.data.repository

import com.helios.core.domain.model.Brand

/**
 * Brand repository: resolves brand from deep-link or defaults to helios.
 * 4-brand registry.
 */
object BrandRepository {

    private val registry = mapOf(
        "helios" to Brand(
            id = "helios",
            name = "Helios",
            legalName = "Helios Energy Inc.",
            accent = "#B88A2E",
            accentLight = "#F0C674",
            mark = "helios",
            tagline = "Power, illuminated."
        ),
        "solaris" to Brand(
            id = "solaris",
            name = "Solaris",
            accent = "#D97757",
            accentLight = "#EDAB94",
            mark = "text",
            textMark = "S"
        ),
        "volt" to Brand(
            id = "volt",
            name = "Volt",
            accent = "#5D8AA8",
            accentLight = "#90CAF9",
            mark = "text",
            textMark = "V"
        ),
        "aether" to Brand(
            id = "aether",
            name = "Aether",
            accent = "#4A7F3C",
            accentLight = "#7FB069",
            mark = "text",
            textMark = "A"
        )
    )

    private val _current = registry["helios"]!!
    private var currentBrand: Brand = _current

    fun resolve(from: String?): Brand {
        val brand = from?.let { registry[it] } ?: _current
        currentBrand = brand
        return currentBrand
    }

    fun current(): Brand = currentBrand
}
