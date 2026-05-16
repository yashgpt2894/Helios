package com.helios.qs

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class HeliosQSTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName("com.helios.app", "com.helios.MainActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivityAndCollapse(intent)
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val snap = generateTelemetry()

        tile.label = "Helios"
        tile.subtitle = "%.2f kW | %.0f%%".format(snap.acPowerW / 1000, snap.batterySoc)
        tile.state = when {
            snap.acPowerW > 100 -> Tile.STATE_ACTIVE
            snap.acPowerW > 0 -> Tile.STATE_INACTIVE
            else -> Tile.STATE_UNAVAILABLE
        }
        tile.updateTile()
    }

    private fun generateTelemetry(): TelemetrySnapshot {
        val hour = System.currentTimeMillis() % (24 * 60 * 60 * 1000) / (60 * 60 * 1000.0)
        val factor = if (hour in 6.0..20.0) {
            Math.sin((hour - 6.0) / 14.0 * Math.PI)
        } else 0.0
        val acPowerW = factor * 9600.0 * 0.7
        val batterySoc = 40.0 + (factor * 50.0)
        return TelemetrySnapshot(acPowerW, batterySoc.coerceIn(0.0, 100.0))
    }

    private data class TelemetrySnapshot(
        val acPowerW: Double,
        val batterySoc: Double
    )
}
