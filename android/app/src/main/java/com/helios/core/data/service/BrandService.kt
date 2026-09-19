package com.helios.core.data.service

import com.helios.core.domain.model.Brand
import kotlinx.coroutines.flow.Flow

/**
 * White-label brand resolution (SVC-10, SVC-11).
 *
 * The registry must match `src/services/brand.ts`: a snapshot payload or deep link
 * carries the brand id, so a registry that differs resolves a shared link to the wrong
 * brand. [BrandService.accentOverride] returns the accent pair only; the semantic hue
 * roles never change with a brand.
 */
interface BrandService {

    val brand: Flow<Loadable<Brand>>

    /** The registry, in the order the settings screen shows it. */
    val registry: List<Brand>

    fun resolve(id: String?): Brand

    suspend fun select(id: String): Loadable<Brand>

    /** Accent pair for the current brand: (accent, accentLight). */
    fun accentOverride(): Pair<String, String>
}
