package com.helios.core.domain.model

data class Brand(
    val id: String,
    val name: String,
    val legalName: String? = null,
    val accent: String,
    val accentLight: String,
    val mark: String, // "helios" | "text"
    val textMark: String? = null,
    val supportEmail: String? = null,
    val supportUrl: String? = null,
    val tagline: String? = null
)
