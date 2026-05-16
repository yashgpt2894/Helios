package com.helios.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

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
        SmallWidget().updateAll(context)
        MediumWidget().updateAll(context)
        LargeWidget().updateAll(context)
    }
}
