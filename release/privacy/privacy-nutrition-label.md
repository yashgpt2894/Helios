# Privacy Nutrition Label (iOS App Store)

## Data Used to Track You

**helios° does not track you.** No data from this app is used to track you across apps and websites owned by other companies.

---

## Data Linked to You

The following data may be collected and linked to your identity:

### Identifiers
- **Device ID**: Used solely for local data partitioning (e.g., per-device settings and connection profiles). Never transmitted off-device.

### Location
- **Coarse Location**: Used to fetch weather-based solar production forecasts from Open-Meteo. The app sends approximate latitude and longitude to Open-Meteo's public API. No API key, no user identification, no logging of requests by the app. Users can also enter coordinates manually. Location is not shared with any other service.

---

## Data Not Linked to You

The following data is collected but is not linked to your identity:

### Diagnostics
- **Crash Data**: If the user opts in to share crash reports with Apple, crash logs may be collected. These do not contain user-identifiable content. The app itself does not collect or transmit crash data independently.

---

## Data Not Collected

The following data types are NOT collected by helios°:

- **Contact Info**: Name, email address, phone number, physical address — not collected.
- **Health & Fitness**: No health data collected.
- **Financial Info**: No payment information collected. No in-app purchases.
- **Sensitive Info**: No biometric, religious, political, or other sensitive data collected.
- **Contacts**: No access to user contacts.
- **User Content**: Emails, SMS, photos, videos, audio — not collected. Snapshot sharing uses ephemeral URL fragments that are never stored or transmitted to a server.
- **Browsing History**: No in-app browser. No browsing history collected.
- **Search History**: No search functionality. No search history collected.
- **Purchases**: No purchases, no purchase history.
- **Usage Data**: Product interaction data, advertising data, and other usage data are not collected.
- **Other Data Types**: No other data types are collected.

---

## Third-Party Services

### Open-Meteo (Weather API)
- **Purpose**: Fetches solar production forecasts based on user location.
- **Data sent**: Approximate latitude and longitude.
- **Data received**: Weather forecast (temperature, cloud cover, irradiance, weather code).
- **API key**: None required. Open-Meteo is a free, open-source weather API with no user tracking.
- **Privacy policy**: https://open-meteo.com/en/features#privacy

No other third-party services, analytics, or advertising SDKs are included in the app.

---

## Privacy Policy URL

A full privacy policy must be hosted at a publicly accessible URL and linked from App Store Connect. The policy should mirror the disclosures above and include:
- Data controller identity
- Types of data processed
- Purpose of processing (weather forecast only)
- Data retention (all data is device-local; no server-side retention)
- User rights (data export and deletion are trivial since all data is local)
- Contact information for privacy inquiries

---

## App Store Connect Checklist

When filling out App Privacy in App Store Connect:

1. **Data Used to Track You**: Uncheck all boxes. Publish "No, the developer does not track you."

2. **Data Linked to You**: Check only:
   - Identifiers > Device ID
   - Location > Coarse Location

3. **Data Not Linked to You**: Check only:
   - Diagnostics > Crash Data (if crash reporting opt-in is enabled)

4. **Data Not Collected**: Check all remaining categories.

5. For each checked item under "Data Linked to You", provide the purpose:
   - **Device ID**: App Functionality
   - **Coarse Location**: App Functionality (weather forecast)

6. **Privacy Policy**: Provide the URL to the full privacy policy.
