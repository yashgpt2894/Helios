# Google Play Data Safety Form

This document maps helios° data practices to Google Play's Data Safety section format. Use these values when filling out the Data Safety form in Google Play Console.

---

## Section 1: Data Collection and Security

### Does the developer collect or share any of the required user data types?

**Yes.** (Because Coarse Location is sent to Open-Meteo for weather forecasts.)

### Is all of the data collected by your app encrypted in transit?

**Yes.** All network requests use HTTPS. The app communicates with the inverter over LAN (Modbus TCP, unencrypted by protocol limitation — this is local network traffic only) and with Open-Meteo over HTTPS.

### Do users have a way to request that their data is deleted?

**Yes.** All data is device-local. Uninstalling the app deletes all data. Users can also clear connection configurations, location data, and telemetry cache from within the app's Settings screen.

---

## Section 2: Data Types

For each data type, indicate whether it is Collected, Shared, and its purpose.

### Location

| Data Type | Collected? | Shared? | Purpose | Optional? |
|-----------|-----------|---------|---------|-----------|
| Approximate Location | Yes | No | App functionality (weather-based solar forecast) | Yes — users can enter coordinates manually instead |

Data is sent to Open-Meteo's public API over HTTPS. No user identification is included. No API key.

### Personal Identifiers

| Data Type | Collected? | Shared? | Purpose | Optional? |
|-----------|-----------|---------|---------|-----------|
| Device or Other IDs | Yes | No | App functionality (local data partitioning per device) | No |

Device ID is used only for local storage partitioning. Never transmitted off-device.

### App Info and Performance

| Data Type | Collected? | Shared? | Purpose | Optional? |
|-----------|-----------|---------|---------|-----------|
| Crash Logs | Yes (if opted in) | No | Analytics (crash reporting via platform default mechanisms) | Yes — user must opt in to share crash reports |

### All Other Data Types

The following data types are **NOT collected** by helios°:

- **Personal Info**: Name, email address, phone number, physical address, date of birth — not collected.
- **Financial Info**: No payment info. No purchases.
- **Health and Fitness**: No health data.
- **Messages**: No SMS, email, or chat.
- **Photos and Videos**: No photos, videos, or media access.
- **Audio**: No audio recording or voice.
- **Files and Docs**: No file access other than internal app storage.
- **Calendar**: No calendar access.
- **Contacts**: No contacts access.
- **App Activity**: Page views, taps, in-app search history — not collected.
- **Web History**: No in-app browser. No browsing history.

---

## Section 3: Data Sharing

### Does your app share data with third parties?

**No.** The only third-party interaction is with Open-Meteo for weather forecasts, and the app sends only approximate coordinates with no user identification. This is not data "sharing" in the Play Data Safety sense because:
- No user account or identifier is sent.
- Open-Meteo has no way to correlate requests to individual users.
- Open-Meteo does not retain or profile user request data.

---

## Section 4: Data Security Practices

### Data Encryption
- **In transit**: Yes (HTTPS for all external requests; local Modbus TCP traffic is on the LAN only).
- **At rest**: Yes (device-local storage; Android Keystore for any sensitive connection parameters).

### Data Retention
- All app data is stored locally on the device.
- No data is retained on external servers.
- Deleting the app removes all locally stored data.
- Users can manually reset all data from within Settings.

### Commitment to Play Families Policy
helios° does not target children. The target audience is adults (18+) who own residential solar installations. The app contains no content that would be inappropriate for children, but it is not designed for or marketed to children.

---

## Google Play Console Checklist

When filling out the Data Safety form in Play Console:

1. **Overview**: Answer "Yes" to data collection, "Yes" to encryption in transit, "Yes" to data deletion mechanism.

2. **Data Types**: Select these as "Collected":
   - Location: Approximate location — App functionality — Optional — Not shared
   - Device or other IDs: Device or other IDs — App functionality — Not optional — Not shared
   - App info and performance: Crash logs — Analytics — Optional — Not shared

3. **All other data types**: Mark as "Not Collected".

4. **Data sharing**: Answer "No" — no data is shared with third parties.

5. **Review and submit**: The Data Safety section should show a green checkmark for all declarations.

6. **Privacy policy URL**: Required. Must be provided in the App Content > Privacy Policy section.

---

## Notes

- If you add analytics (Firebase, etc.) in the future, you must update this form before the next release.
- If you add account sign-in (Google Sign-In, email), the personal identifiers declaration must also be updated.
- Crash reporting via Google Play's native mechanism (Android Vitals) does not require additional declaration — it is covered by the "Crash Logs" entry above.
