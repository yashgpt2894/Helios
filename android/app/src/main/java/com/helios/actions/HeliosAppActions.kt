package com.helios.actions

/**
 * HeliosAppActions — constants and docs for Google Assistant App Actions.
 *
 * Two shortcuts are defined in res/xml/shortcuts.xml:
 *  - check_production  → "Check solar production"  → helios://dashboard
 *  - check_battery     → "Check battery level"     → helios://battery
 *
 * Both bind to the built-in intent actions.intent.GET_THING with
 * distinct thing.name values ("solar production" / "battery level").
 *
 * After uploading these shortcuts to Google Play Console (Deep links → App Actions)
 * the following voice commands become active:
 *   "Hey Google, check solar production on Helios"
 *   "Hey Google, check battery level on Helios"
 */
object HeliosAppActions {

    const val SHORTCUT_PRODUCTION = "check_production"
    const val SHORTCUT_BATTERY = "check_battery"

    const val DEEP_LINK_DASHBOARD = "helios://dashboard"
    const val DEEP_LINK_BATTERY = "helios://battery"
}
