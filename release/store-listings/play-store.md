# Google Play Store Listing

## App Name
helios° — solar intelligence

## Short Description
Precision energy intelligence for residential solar arrays. Live telemetry, AI insights, and 7-day forecast — private by design.

## Full Description

helios° brings professional-grade solar monitoring to your Android device. See your energy flow in real time — from panels to home to battery to grid — with an animated energy diagram that responds to every watt. The live dashboard shows exactly what your system is producing right now, what your home is consuming, and where the rest is going.

Go deeper with per-string telemetry, AI-powered insights that learn your array's patterns, and a 7-day production forecast that helps you plan energy use around the weather. The battery screen maps your state of charge, charge rate, temperature, and backup readiness in a stunning ring visualization that glows when you're charging and pulses when you're discharging.

Privacy is built in, not bolted on. All your data stays on your device. No cloud accounts, no third-party analytics, no tracking. Share screenshots with a link — encrypted and ephemeral. helios° is for solar owners who want insight without surveillance.

## Category
**Primary**: Tools  
**Secondary**: Productivity

Note: Google Play does not have a standalone "Utilities" category. "Tools" is the closest match. "Productivity" is the secondary.

## Tags
solar, energy, monitoring, battery, inverter, photovoltaic, home automation, utility

## What's New
Initial release. Full feature parity with Helios PWA: Dashboard, Production, Insights, Battery, Settings. Native Android with Material 3 theming, Room persistence, Compose Canvas visualization, and WorkManager background fetch.

## Graphics Requirements
- **App icon**: 512x512 px PNG, adaptive icon (foreground + background + monochrome layers)
- **Feature graphic**: 1024x500 px PNG (used at top of store listing). Design: Dark carbon background with aurora gradient. HeliosMark centered. "helios°" in bone text, "solar intelligence" below in warm gray.
- **Screenshots**: Phone (1080x2400), Foldable (1768x2208), Tablet (1600x2560). Minimum 4 screenshots, maximum 8 per device type.

## Content Rating
Complete the IARC questionnaire:
- **Violence**: None
- **Sexual Content**: None
- **Language**: None
- **Controlled Substances**: None (alcohol, tobacco, drugs)
- **User Generated Content**: No
- **Digital Purchases**: No
- **Personal Information Collection**: No (the app processes data locally; no account required; no data collection)

Expected rating: **Everyone**

## Target Audience
Ages 18 and older (primary users are homeowners with solar installations).

## App Access
**No login credentials needed.** The app works entirely offline and communicates directly with the user's local inverter — no account registration or sign-in.

## Privacy Policy URL
Include a link to your privacy policy page. The privacy policy must disclose:
- No personal data is collected or transmitted.
- All telemetry data stays on-device.
- Weather forecast data is fetched from Open-Meteo (no API key, no user identification).
- Snapshot sharing uses URL fragments (data never hits a server).

## Supported Languages
English

## Platform Requirements
- Minimum Android API level: 26 (Android 8.0 Oreo)
- Target API level: 35 (Android 15)
- Compile SDK: 35
- Supported architectures: arm64-v8a, armeabi-v7a, x86_64, x86 (via AAB)

## Testing Instructions
No special test account required. Install the app, enter the inverter connection details (protocol, host, port, unit ID), and the dashboard populates from local telemetry. A demo/simulation mode is accessible from the settings screen for testing without hardware.
