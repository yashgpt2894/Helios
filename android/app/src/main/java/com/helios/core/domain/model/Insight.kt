package com.helios.core.domain.model

enum class InsightSeverity {
    positive,
    neutral,
    attention,
    critical
}

enum class InsightCategory {
    production,
    consumption,
    battery,
    savings,
    maintenance,
    forecast
}

data class Insight(
    val id: String,
    val category: InsightCategory,
    val severity: InsightSeverity,
    val title: String,
    val body: String,
    val metric: String? = null,
    val delta: String? = null,
    val actionLabel: String? = null
)
