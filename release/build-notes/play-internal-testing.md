# Play Internal Testing Upload Instructions

## Prerequisites

1. **Google Play Developer account** (one-time $25 registration fee, paid).
2. **Android Studio** (Ladybug or later recommended) with the Helios project open.
3. **Google Play Console access** with Release management permissions.
4. The Helios Android app must have an **app record** created in Play Console with a registered package name (e.g., `com.yourcompany.helios`).
5. App content declarations completed (Content Rating questionnaire, Data Safety form, app category).

---

## Step 1: Review Build Configuration

1. Open the Helios Android project in Android Studio.
2. Open `android/app/build.gradle.kts` (or `build.gradle`).
3. Verify:
   ```kotlin
   android {
       namespace = "com.yourcompany.helios"
       compileSdk = 35

       defaultConfig {
           applicationId = "com.yourcompany.helios"
           minSdk = 26
           targetSdk = 35
           versionCode = 1
           versionName = "1.0.0"
       }

       signingConfigs {
           create("release") {
               // Configured via keystore properties
           }
       }
   }
   ```
4. Confirm `applicationId` exactly matches the Play Console app record.
5. Confirm `versionCode` is an integer (not a string). Each upload must have a higher versionCode than the previous.
6. Confirm `versionName` follows semantic versioning (`"1.0.0"`).

---

## Step 2: Configure Release Signing

The app must be signed with an upload key (not debug key). Google Play manages the app signing key.

### First-time signing setup:

1. Generate an upload keystore if you don't have one:
   ```bash
   keytool -genkey -v -keystore ~/helios-upload-keystore.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias helios-upload
   ```
2. Store the keystore password and key password securely. You cannot update your app without them.

3. Create `android/keystore.properties` (add to `.gitignore`):
   ```
   storeFile=/Users/username/helios-upload-keystore.jks
   storePassword=your-store-password
   keyAlias=helios-upload
   keyPassword=your-key-password
   ```

4. In `android/app/build.gradle.kts`, reference these properties:
   ```kotlin
   val keystorePropertiesFile = rootProject.file("keystore.properties")
   val keystoreProperties = Properties()
   if (keystorePropertiesFile.exists()) {
       keystoreProperties.load(FileInputStream(keystorePropertiesFile))
   }

   signingConfigs {
       create("release") {
           storeFile = file(keystoreProperties["storeFile"] as String)
           storePassword = keystoreProperties["storePassword"] as String
           keyAlias = keystoreProperties["keyAlias"] as String
           keyPassword = keystoreProperties["keyPassword"] as String
       }
   }

   buildTypes {
       release {
           signingConfig = signingConfigs.getByName("release")
           isMinifyEnabled = true
           isShrinkResources = true
           proguardFiles(
               getDefaultProguardFile("proguard-android-optimize.txt"),
               "proguard-rules.pro"
           )
       }
   }
   ```

---

## Step 3: Build the Signed AAB

Google Play requires Android App Bundle (`.aab`) format, not APK.

### Via Android Studio:

1. From the menu: **Build > Generate Signed App Bundle / APK**.
2. Select **Android App Bundle**.
3. Select your upload keystore file, enter key alias and passwords.
4. Select the **release** build variant.
5. Check **Export encrypted key for enrolling Google Play App Signing** (recommended for first upload).
6. Click **Finish**.
7. Wait for the build to complete. Android Studio opens the output directory.

### Via command line (CI-friendly):

```bash
cd android
./gradlew bundleRelease \
  -Pandroid.injected.signing.store.file=/path/to/keystore.jks \
  -Pandroid.injected.signing.store.password=$STORE_PASS \
  -Pandroid.injected.signing.key.alias=helios-upload \
  -Pandroid.injected.signing.key.password=$KEY_PASS
```

The signed AAB is at: `android/app/build/outputs/bundle/release/app-release.aab`

---

## Step 4: Upload to Play Console Internal Testing Track

1. Open [Google Play Console](https://play.google.com/console).
2. Select the helios° app.
3. In the left sidebar: **Testing > Internal Testing**.
4. Click **Create new release** (or **Edit release** if one exists).

### Release Details

5. Under **App bundles**, click **Upload** and select the `app-release.aab` file.
6. Wait for the upload to complete. Play Console validates the AAB:
   - Checks versionCode is higher than previous.
   - Checks signing certificate matches or is registered for Play App Signing.
   - Checks required declarations (Data Safety, Content Rating) are completed.
7. Under **Release details**, enter:
   - **Release name**: e.g., "1.0.0-beta1"
   - **Release notes**: Describe what testers should focus on. Example:
     ```
     Initial internal test release. Full feature set: Dashboard, Production,
     Insights, Battery, Settings. Test with Modbus TCP inverters.
     Known limitation: Foldable UI polish in progress.
     ```
8. Click **Save** (not "Review release" yet — configure testers first).

---

## Step 5: Configure Testers

### Internal Testing Track

Internal testing supports up to 100 testers by email. Testers don't need a special account — just a Google account.

1. In the Internal Testing page, click the **Testers** tab.
2. Under **Choose a testing method**:
   - **Email list**: Upload a CSV of email addresses, or type them one per line.
   - **Google Groups**: If you have a Google Group, enter the group email. Anyone in the group can test.
3. Click **Save changes**.

### Optional: Manage Tester Feedback

- Testers can send feedback directly from the Play Store app. Feedback goes to the email associated with the Play Console account.
- Set up a feedback channel (Slack, email alias) and include instructions in the release notes.

---

## Step 6: Start the Rollout

1. Go back to **Testing > Internal Testing**.
2. Click the release you created.
3. Click **Review release**.
4. Review all sections for warnings or errors:
   - **Data Safety**: Must be complete and green.
   - **Content Rating**: Must be complete.
   - **Target API**: Must target API 34+ (Google's current requirement).
   - **App Bundle**: Must be valid.
5. If all checks pass, click **Start rollout to Internal Testing**.
6. Confirm the rollout.

---

## Step 7: Tester Experience

1. Testers receive an email invitation with a link.
2. They open the link on their Android device (must be signed into the matching Google account).
3. They tap **Accept invitation**.
4. They are redirected to the Play Store listing (hidden from public, visible only to testers).
5. They tap **Install**.
6. The app installs like any Play Store app.
7. Updates: When you upload a new release to Internal Testing, testers get the update automatically through the Play Store (like a normal app update).
8. It may take a few hours for the listing to become available after first accepting the invitation.

### Testers Can Also Use the Web Link

The Play Store web link for internal testing is:
`https://play.google.com/apps/test/com.yourcompany.helios/`

Replace `com.yourcompany.helios` with the actual package name. Testers must be signed in with their invited Google account and have accepted the testing invitation.

---

## Troubleshooting

| Issue | Resolution |
|-------|------------|
| "Version code X has already been used" | Increment `versionCode` in `build.gradle.kts`. Must be strictly higher than any previous upload. |
| "Upload certificate differs from previous" | If this is the first upload, contact Play support to reset your app signing key. If not first, you used the wrong keystore. Find the correct one. |
| "Your app targets API 33 but must target API 34+" | Update `targetSdk` to 35 and `compileSdk` to 35. |
| "Data Safety form incomplete" | Complete the Data Safety section in Policy > App content > Data Safety. |
| "Content rating not set" | Complete the Content Rating questionnaire in Policy > App content > Content Rating. |
| "You need a privacy policy" | Add a privacy policy URL in Policy > App content > Privacy Policy. |
| "App bundle signed with debug key" | You must sign with a release key. Debug keystores are rejected. |
| Build fails with "minSdk 26 cannot be higher than targetSdk 33" | `minSdk` must be less than or equal to `targetSdk`. |
| ProGuard/R8 stripping removes required classes | Add `-keep` rules to `proguard-rules.pro` for any reflection-based libraries (Room, Gson, etc.). |

---

## Post-Upload Checklist

- [ ] AAB uploaded and accepted (no validation errors).
- [ ] Internal Testing release created with release notes.
- [ ] Testers added (email list or Google Group).
- [ ] Rollout started and status shows "Available to internal testers".
- [ ] At least one tester has successfully installed the app.
- [ ] Crash reports appear in Play Console > Quality > Android Vitals > Crashes.
- [ ] Tester feedback mechanism is working.
- [ ] `versionCode` incremented for the next upload.
