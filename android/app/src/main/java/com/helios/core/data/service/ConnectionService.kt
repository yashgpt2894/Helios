package com.helios.core.data.service

import com.helios.core.domain.model.ConnectionConfig
import kotlinx.coroutines.flow.Flow

/** The four link states the app can prove. */
enum class LinkState { DISCONNECTED, TESTING, CONNECTED, SIMULATED }

/** What the Connection sheet and the status pill show. */
data class ConnectionSnapshot(
    val config: ConnectionConfig,
    val state: LinkState,
    val lastCheckedAt: Long,
    val lastGoodAt: Long?,
    /** Attempt counter for the bounded retry schedule (1 s / 2 s / 4 s, then 30 s, 60 s). */
    val attempt: Int,
    val identifiedAs: String? = null,
    val firmware: String? = null
)

/**
 * Connection lifecycle (SVC-19). The real implementation persists [ConnectionConfig] in
 * Room and owns the retry schedule; this pass defines the contract and the failure
 * classification that the sheet renders.
 */
interface ConnectionService {

    val link: Flow<Loadable<ConnectionSnapshot>>

    /** Test a candidate without saving it. Used by onboarding step 4 (SCR-04). */
    suspend fun test(candidate: ConnectionConfig): Loadable<ConnectionSnapshot>

    /** Save and connect. */
    suspend fun connect(candidate: ConnectionConfig): Loadable<ConnectionSnapshot>

    suspend fun disconnect()

    /** The retry schedule as data, so the sheet can state it in words. */
    fun retrySchedule(): List<Long>
}
