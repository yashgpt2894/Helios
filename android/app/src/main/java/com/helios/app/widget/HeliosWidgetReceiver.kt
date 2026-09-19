package com.helios.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * HeliosWidgetReceiver: delegates updates to Glance-based AppWidget providers.
 * All three widget sizes route through this single AppWidgetProvider registered in the manifest.
 */
class HeliosWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // updateAll is suspending, so keep the broadcast alive until every size has refreshed.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                SmallWidget().updateAll(context)
                MediumWidget().updateAll(context)
                LargeWidget().updateAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
