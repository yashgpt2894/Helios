# TestFlight Upload Instructions

## Prerequisites

1. **Apple Developer Program membership** (paid, active).
2. **Xcode 16+** installed on macOS.
3. **App Store Connect access** with Admin or App Manager role.
4. The Helios iOS project builds and archives without errors.
5. An **App Store Connect app record** already created for helios° (bundle ID registered, app metadata partially filled).

---

## Step 1: Update Build Configuration

1. Open `ios/Helios.xcodeproj` (or `.xcworkspace` if using CocoaPods/SPM) in Xcode.
2. Select the **Helios** target, then the **General** tab.
3. Verify:
   - **Display Name**: "helios°"
   - **Bundle Identifier**: matches the App Store Connect app record (e.g., `com.yourcompany.helios`)
   - **Version**: semantic version (e.g., `1.0.0`)
   - **Build**: incrementing integer (e.g., `1`)
4. Select the **Signing & Capabilities** tab.
5. Enable **Automatically manage signing**. Select your team.
6. Confirm the provisioning profile resolves without errors.
7. Close the project settings.

---

## Step 2: Archive the App

1. In Xcode, select **Any iOS Device (arm64)** as the build destination (not a simulator).
2. From the menu bar: **Product > Archive**.
3. Wait for the build and archive to complete. This may take several minutes.
4. The Organizer window opens automatically, showing the new archive.

---

## Step 3: Validate the Archive

1. In the Organizer, select the new archive.
2. Click **Validate App**.
3. Select your distribution method: **TestFlight & App Store**.
4. Click **Next**.
5. Select your signing certificate. Xcode should auto-select the correct distribution certificate.
6. Click **Validate**.
7. Wait for validation to complete. Fix any errors:
   - Missing icons: Add all required icon sizes to `Assets.xcassets > AppIcon`.
   - Missing privacy manifest: Apple requires `PrivacyInfo.xcprivacy` from Spring 2024. Ensure it's in the target.
   - Invalid entitlements: Remove any entitlements not matching the App ID.
   - Missing push notification entitlement: Only needed if using push; Helios does not.
8. Validation passes: green checkmark. Continue.

---

## Step 4: Distribute to TestFlight

1. In the Organizer, with the validated archive selected, click **Distribute App**.
2. Select **TestFlight & App Store**.
3. Click **Next**.
4. Review the signing and entitlements. Click **Next**.
5. Ensure **Upload your app's symbols to receive symbolicated reports from Apple** is checked.
6. Ensure **Include bitcode for iOS content** is unchecked (deprecated since Xcode 14).
7. Click **Next**.
8. Select **Automatically manage signing**. Click **Next**.
9. Review the summary and click **Upload**.
10. Wait for the upload to complete. This typically takes 5-15 minutes depending on binary size and network.

---

## Step 5: Configure TestFlight Internal Testing

1. Open [App Store Connect](https://appstoreconnect.apple.com).
2. Navigate to **My Apps > helios° > TestFlight**.
3. Under **iOS**, you should see the uploaded build with status "Processing". Wait for it to complete (usually 10-30 minutes). You'll receive an email when processing is done.
4. Once processed, the build status changes to "Ready to Test".
5. Click on the build version number.
6. Scroll to **Internal Testing**.
7. Click the **+** button next to "Internal Testers" to add testers.

### Setting Up Internal Testers

1. In App Store Connect, navigate to **Users & Access**.
2. Click the **+** button to add a new user.
3. Enter their **Apple ID email address**.
4. Assign role: **Developer** (minimum) or **App Manager**.
5. Under **Apps**, select helios°.
6. Click **Invite**. The user receives an email invitation.
7. Once they accept, they appear in the TestFlight > Internal Testing tester list.

Alternate: Add existing team members directly from the TestFlight > Internal Testing section by clicking the **+** button and selecting from the team list.

### Adding External Testers (Beta Review Required)

External testing requires App Review for the first build only. Subsequent builds auto-approve unless significant changes are made:
1. Create an **External Testing Group**.
2. Add testers by email (up to 10,000).
3. Provide beta testing information: feedback email, contact info, and a brief description of what to test.
4. Submit for Beta App Review. Turnaround is typically 24 hours.

---

## Step 6: Distribute to Testers

1. In the build's Internal Testing section, confirm all desired testers are selected.
2. The build automatically becomes available to internal testers once processing completes.
3. Testers receive an email from TestFlight with an invitation.
4. They install TestFlight from the App Store if not already installed.
5. They tap "Install" in TestFlight to install helios°.
6. Subsequent builds are delivered as updates through TestFlight.

---

## Step 7: UDID Registration (For Ad Hoc / Development Builds)

If you need to distribute builds directly (not via TestFlight) — for example, to test on a specific device whose UDID is not in your provisioning profile:

1. Ask the tester for their device UDID. They can find it by:
   - Connecting the device to a Mac and viewing it in Finder (click device name, then click serial number to toggle to UDID).
   - Using a free UDID lookup service (e.g., udid.io) — tester opens in Safari and follows instructions.
2. In [Apple Developer](https://developer.apple.com), navigate to **Certificates, Identifiers & Profiles > Devices**.
3. Click the **+** button to add a new device.
4. Enter the device name and UDID. Click **Continue**, then **Register**.
5. Navigate to **Profiles** and find the Development or Ad Hoc provisioning profile for helios°.
6. Click the profile, then **Edit**. The new device should be available in the device list. Select it and **Save**.
7. Download the updated provisioning profile and double-click to install.
8. Re-archive and select the updated profile, or export an Ad Hoc build with the updated profile.
9. Note: Maximum 100 devices per product family per membership year for Ad Hoc. TestFlight has no device limit.

---

## Troubleshooting

| Issue | Resolution |
|-------|------------|
| "No signing certificate found" | Generate a distribution certificate in Xcode > Settings > Accounts > Manage Certificates > + > Apple Distribution. |
| "Missing privacy manifest" | Add `PrivacyInfo.xcprivacy` to the target. See Apple's documentation for required privacy manifest keys. |
| "ITMS-90683: Missing purpose string" | Add `NSLocationWhenInUseUsageDescription` to Info.plist if location is used for weather. |
| "Invalid binary — missing required architecture" | Ensure archive was built with "Any iOS Device (arm64)" target, not a simulator. |
| Build stuck on "Processing" for > 1 hour | Contact Apple Developer Support. Rare but can happen with first builds. |
| Tester doesn't see the build | Confirm they accepted the TestFlight invitation. Ask them to check the TestFlight app > helios° > Previous Builds section. |

---

## Post-Upload Checklist

- [ ] Build is uploaded and "Processing" completes.
- [ ] Internal testers added and build is available.
- [ ] At least one tester has installed and launched the app successfully.
- [ ] Crash reports appearing in Xcode Organizer > Crashes (if any).
- [ ] Feedback from testers received via TestFlight app or separate channel.
- [ ] Build number incremented for the next upload.
