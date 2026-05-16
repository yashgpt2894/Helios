package com.helios.core.domain.model

/**
 * Inverter operating status matched verbatim to PWA InverterStatus.
 */
enum class InverterStatus {
    PRODUCING,
    STANDBY,
    CURTAILED,
    NIGHT,
    FAULT
}
