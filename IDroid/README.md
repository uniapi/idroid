### Android Backup
* The 24-byte header usually contains: `ANDROID BACKUP\n5\n1\nnone\n`
* The backup data is compressed using the **DEFLATE** algorithm
* <application> node of AndroidManifest.xml should not contain: `android:allowBackup="false"`
1. Create backup:
	* Full backup:
		`adb backup -f backup.ab -apk -shared -all -system`
	* All app (without system apps) backup:
		`adb backup -f backup.ab -apk -all -nosystem`
	* Specific app backup:
		`adb backup -f backup.ab -apk com.example.app`
	* Specific app data-only backup:
		`adb backup -f backup.ab -noapk com.example.app`
2. Skip the header and extract the decompressed data:
	`dd if=backup.ab bs=24 skip=1 | openssl zlib -d | tar -xvf -`
3. Construct backup:
	* Write the header and the compressed data being packed:
		`echo -e "ANDROID BACKUP\n5\n1\nnone" > backup.ab; tar -cvf - apps | openssl zlib -e | dd >> backup.ab`
	* Truncate the existing backup and concat the compressed data being packaged:
		`truncate -s 24 backup.ab; tar -cvf - apps | openssl zlib -e | dd >> backup.ab`
4. Restore backup:
	`adb restore backup.ab`

### App Install
- Streamed install:
	`adb install --no-incremental <package_name>.signed`
- Streamed reinstall keeping the app data:
	`adb install -r --no-incremental <package_name>.signed`

### App Uninstall
- Uninstall:
	`adb uninstall <package_path>`
- Uninstall keeping the app data and cache:
	`adb uninstall -k <package_path>`

Android ADB Logcat uses seven core log priority levels ordered from lowest to highest priority.
When you filter logs by a specific level, Android displays all messages at that severity and higher.
### 📊 Logcat Levels Breakdown

| Character | Level | Description | Code Equivalent (android.util.Log) |
|---|---|---|---|
| V | Verbose | Lowest priority. Detailed debugging logs, usually stripped from production builds. | Log.v() |
| D | Debug | Useful diagnostic messages during development. | Log.d() |
| I | Info | Standard runtime context messages (e.g., successful network requests or state changes). | Log.i() |
| W | Warning | Non-critical errors or unexpected behaviors that don't crash the app. | Log.w() |
| E | Error | Critical system issues, fatal exceptions, or severe failures. | Log.e() |
| F | Fatal / Assert | Severe conditions where the process cannot recover and will terminate. | Log.wtf() (What a Terrible Failure) |
| S | Silent | Highest priority. Used strictly as a filter option to suppress all logs. | N/A (Filtering only) |

### Logs
- Clear the default buffers (main, system and crash):
	`adb logcat -c`
- Clear all log buffers:
	`adb logcat -b all -c`
- Show only Warning, Error, and Fatal logs (suppresses Verbose, Debug, Info)
	`adb logcat *:W`
- Show only Error and Fatal logs
	`adb logcat *:E`
- Show logs for a specific tag (e.g., "MyApp") at Debug level and above, and silence everything else
	`adb logcat MyApp:D *:S`

### Package Manager
- List Package Manager Commands and Options:
	`adb shell pm help`
- List All Installed Apps:
	`adb shell pm list packages`
- List Only Third-Party (installed by user) Apps:
	`adb shell pm list packages -3`
- List Only System Apps:
	`adb shell pm list packages -s`
- Show Associated File Paths of Apps:
	`adb shell pm list packages -f`
- Clear App Data:
	`adb shell pm clear <package_name>`
- Enable App or Component:
	`adb shell pm enable [--user <USER_ID>] <package_name>`
- Disable App or Component for the Primary (Current) User:
	`adb shell pm disable-user --user 0 <package_name>`
- Remove Restrictive System App for the Current User (not removed from the system partition):
	`adb shell pm uninstall --user 0 <package_name>`
- Reinstall Pre-Installed System App (from the device's system partition):
	`adb shell pm install-existing <package_name>
- List All Settable Permissions:
	`adb shell pm list permissions -d -g`
	* `-d` only dangerous permissions (runtime)
	* `-g` by permission group
- Grant All Permissions:
	`adb shell pm grant -g <package_name>`
- Grant a Permission:
	`adb shell pm grant <package_name> <permission>`
- Revoke a Permission:
	`adb shell pm revoke <package_name <permission>`
- Reset All Runtime Permissions for All Apps:
	`adb shell pm reset-permissions`
_Note: the app must have the permission declared in the manifest for the commands to work._

### Permissions _Install-Time_ up to API Level 10
- 📍 Location & Connectivity
	```
	ACCESS_FINE_LOCATION: Precise location from GPS.
	ACCESS_COARSE_LOCATION: Approximate location from network/Wi-Fi.
	INTERNET: Full network access.
	ACCESS_WIFI_STATE: View Wi-Fi connections.
	CHANGE_WIFI_STATE: Change Wi-Fi connectivity.
	BLUETOOTH & BLUETOOTH_ADMIN: Use and manage Bluetooth connections.
	```
- 📞 Telephony & Messaging
	```
	READ_PHONE_STATE: Read phone status and ID.
	CALL_PHONE: Initiate a phone call without user interaction.
	SEND_SMS, RECEIVE_SMS, READ_SMS: Comprehensive SMS management.
	USE_SIP: Required for SIP-based (Session Initiation Protocol) video or voice calls (Added in API 9)
	```
- 👤 Personal Data
	```
	READ_CONTACTS & WRITE_CONTACTS: Access and modify user contacts.
	READ_CALENDAR & WRITE_CALENDAR: Access and modify the calendar.
	GET_ACCOUNTS: List of accounts in the Accounts Service.
	```
- 📷 Hardware & Media
	```
	CAMERA: Access the camera device.
	RECORD_AUDIO: Record audio from the microphone.
	VIBRATE: Control the vibrator.
	NFC: Use Near Field Communication (Added in API 9; expanded in API 10).
	```
- 📁 Storage & System
	```
	WRITE_EXTERNAL_STORAGE: Modify/delete SD card contents (Historically covered read access too in early APIs).
	WAKE_LOCK: Keep the processor from sleeping or screen from dimming.
	SET_WALLPAPER: Allow applications to set the wallpaper.
	RECEIVE_BOOT_COMPLETED: Start automatically when the system finishes booting.
	MOUNT_UNMOUNT_FILESYSTEMS: Manage file systems for removable storage.
	```
- 🛡️ Protection Levels in API 10
	Even though all were granted at install, they were internally categorized:
	```
	Normal: Low-risk (e.g., SET_WALLPAPER). Granted automatically.
	Dangerous: Potential privacy/security risk (e.g., READ_SMS). Displayed to the user during installation.
	Signature: Only granted if the app is signed with the same certificate as the OS or the app that defined it.
	```
### Permissions _Install-Time_ added in API Level 11
- 📡 Connectivity & Network
	```
	ACCESS_NETWORK_STATE: Technically existed before but saw refined enforcement in API 11 to allow applications to view the status of all networks.
	BIND_REMOTEVIEWS: Required for an app to bind to the RemoteViewsService (essential for the new interactive Home Screen Widgets introduced in Honeycomb).
	USE_SIP: Formally expanded in API 11 to allow applications to use SIP (Session Initiation Protocol) for internet telephony.
	```
- 🛠️ Hardware & Media
	```
	NFC: While added in API 9, API 11 introduced more robust support for applications to perform NFC (Near Field Communication) operations.
	SET_ORIENTATION: Allowed applications to change the orientation of the screen. (Note: This was later deprecated).
	SET_POINTER_SPEED: Allowed applications to change the mouse/trackpad pointer speed (relevant as Honeycomb introduced USB/Bluetooth mouse support). 
	```
- 🖥️ System & UI
	```
	HARDWARE_TEST: Allowed the app to control hardware for the purpose of hardware testing (usually for system-level apps).
	STATUS_BAR: Allowed an application to open, close, or disable the status bar and its icons.
	WRITE_SETTINGS: Allowed an application to read or write the system settings, which became more complex with Honeycomb's new UI
	```
- 📝 Important Context for API 11
	API 11 was a major shift because it introduced the Action Bar and Fragments.
	While these are API features rather than "permissions," many existing permissions (like INTERNET or READ_CONTACTS)
	became more common as developers built larger, more complex tablet applications for the first time.
### No Permissions were added for API Levels 12 and 13
In the original Android release cycle (2011), API Levels 12 and 13 were incremental updates to the tablet-focused Android 3.x Honeycomb platform.
Unlike modern Android versions, these releases introduced very few new permissions,
focusing instead on hardware connectivity like USB and resizing layouts.

API 12 was a minor update primarily known for introducing USB Host mode,
allowing tablets to act as hosts for peripherals like keyboards, mice, and cameras.
The primary security change was the introduction of the USB Host API,
which uses a different intent-based system rather than a standard manifest permission.
When an app wants to access a USB device, the system triggers a dialog asking the user for one-time permission to that specific device.

API 13 was another maintenance release that brought Honeycomb's features to smaller 7-inch tablets.
This release focused on **Screen Compatibility Mode** and new manifest attributes for layout management
(like _requiresSmallestWidthDp_), which did not require new security permissions.

### Permissions _Install-Time_ added in API Level 14
- Social APIs & Multimedia
	```
	READ_SOCIAL_STREAM: Allowed an app to read the user's social stream (like status updates from synced social accounts). Note: This was later deprecated/removed.
	WRITE_SOCIAL_STREAM: Allowed an app to edit social stream data.
	READ_PROFILE: Allowed an app to read the user's personal profile (the "Me" contact).
	WRITE_PROFILE: Allowed an app to update the user's personal profile.
	READ_USER_DICTIONARY: Access to the custom words the user has saved in their keyboard dictionary.
	WRITE_USER_DICTIONARY: Ability to add new words to the keyboard dictionary.
	ADD_VOICEMAIL: Allowed an app to add entries to the system's voicemail inbox.
	BIND_TEXT_SERVICE: Required for apps that wanted to provide spell-checking services.
	```
### Permissions _Install-Time_ added in API Level 15
- Call Logs
	```
	READ_CALL_LOG: Explicitly separated call log access from general contact access.
	WRITE_CALL_LOG: Explicitly separated the ability to modify call logs.
	```
- 💡 Pro-Tip for Legacy Migration
	In these API levels, the WRITE_EXTERNAL_STORAGE permission implicitly granted read access.
	If you are targeting these levels, you don't need READ_EXTERNAL_STORAGE yet (that wasn't enforced strictly until API 19).


### Permissions _Install-Time_ added in API Level 16, 17, 18
The Jelly Bean era (2012–2013) focused heavily on refined user privacy, secondary user profiles (for tablets),
and the introduction of Bluetooth Low Energy (BLE).
- API Level 16 (Android 4.1)
	```
	READ_EXTERNAL_STORAGE: Formally introduced to separate reading from the existing WRITE_EXTERNAL_STORAGE (though it wasn't strictly enforced for all apps until later versions).
	READ_CALL_LOG / WRITE_CALL_LOG: While added in the 15 revision, these were fully standardized here to decouple call history from general "Contacts" access.
	BIND_ACCESSIBILITY_SERVICE: Required to ensure only the system can bind to an AccessibilityService
	```
- API Level 17 (Android 4.2)
	```
	ACCESS_LOCATION_EXTRA_COMMANDS: Allowed apps to access additional location provider commands.
	INSTALL_SHORTCUT / UNINSTALL_SHORTCUT: Standardized how apps could add icons to the home screen (previously handled by unofficial intents in many launchers).
	READ_USER_DICTIONARY / WRITE_USER_DICTIONARY: Gained stricter enforcement as Jelly Bean improved the predictive keyboard.
	CHANGE_NETWORK_STATE: Allowed apps to change network connectivity (Wi-Fi vs. Data) more explicitly.
	```
- API Level 18 (Android 4.3)
	```
	BIND_NOTIFICATION_LISTENER_SERVICE: A major addition allowing apps (like smartwatches or automation tools) to read and interact with all system notifications.
	BLUETOOTH_ADMIN: Gained importance here as API 18 introduced official support for Bluetooth Low Energy (BLE).
	SEND_RESPOND_VIA_MESSAGE: Required for apps to handle the "Quick Response" feature for incoming calls via SMS.
	```
- Legacy Migration Tip
	During this era, Google introduced "App Ops" (a hidden feature in 4.3) which was the first experimental attempt
	at what would eventually become the API 23 Runtime Permissions.
	Users couldn't see it, but the foundation for revoking permissions was being laid here!

### Permissions _Install-Time_ added in API Level 19
In API Level 19 (Android 4.4 KitKat), several new permissions were introduced to support new hardware features
(like Infrared) and refined UI capabilities (like Home Screen shortcuts). 
A major change in this version was the loosening of requirements for app-specific storage,
allowing apps to write to their own external directories (via `getExternalFilesDir()`) without needing any storage permissions
- API Level 19 (Android 4.4)
	```
	INSTALL_SHORTCUT: Formally added to the standard manifest permissions to allow an application to install a shortcut in the Launcher.
	UNINSTALL_SHORTCUT: Allowed an application to remove a shortcut from the Launcher.
	TRANSMIT_IR: Required for apps to use the device's infrared (IR) transmitter, which became common in flagship devices at that time.
	BIND_NFC_SERVICE: Specifically for host-based NFC card emulation, ensuring only the system can bind to an app's HostApduService or OffHostApduService.
	BIND_PRINT_SERVICE: Required by any print service to ensure only the system can bind to it (supporting the new native Printing Framework).
	```
- **Key Feature**: The _maxSdkVersion_ Attribute
	API 19 introduced the _android:maxSdkVersion_ attribute for the _<uses-permission>_ tag.
	This is crucial for your migration: 
	* It allows you to "sunset" a permission once it is no longer required by the OS.
	* **Example**: Because API 19+ doesn't require `WRITE_EXTERNAL_STORAGE` for an app's private files,
		you can declare it with `android:maxSdkVersion="18"` so that newer devices don't see it as a requested permission.
- **New System Features** (Checkable via <uses-feature>)
	While not permissions, these were added in API 19 to help filter apps in the Play Store: 
	```
	FEATURE_CONSUMER_IR
	FEATURE_NFC_HOST_CARD_EMULATION
	FEATURE_SENSOR_STEP_COUNTER
	FEATURE_SENSOR_STEP_DETECTOR
	```

### Permissions _Install-Time_ added in API Level 20
API Level 20 was a unique release specifically for the launch of Android Wear (Android 4.4W).
Because it was a wearable-specific fork, it introduced permissions designed for body-worn sensors and the connection between watches and phones.
- ⌚ Wearable & Health
	```
	BODY_SENSORS: This is the most significant addition. It allows an app to access data from sensors that the user wears on their body,
		such as heart rate monitors.
	```
	* _Note:_ Even in this early API, this was considered a **"dangerous" permission**.
- 🔄 Data & Syncing
	```
	READ_SYNC_SETTINGS: While existing in older versions, its enforcement and use became more prominent in API 20
		to manage how wearable data synced with the companion phone app.
	WRITE_SYNC_SETTINGS: Allowed the wearable app to change whether data syncing was enabled for a specific account.
	```
- 🛠️ System & UI
	```
	WAKE_LOCK: While an old permission, API 20 introduced new ways to use it specifically for "Always-on" watch faces
		and background tasks on low-power wearable hardware.
	```
-📱 Connectivity
	```
	BLUETOOTH & BLUETOOTH_ADMIN: These were heavily utilized in API 20 for the constant tethered connection
		between the Android Wear device and the smartphone.
	```
- **Migration Tip:** Since you are building "incrementally up," keep in mind that **API 20 (4.4W)** and **API 21 (Lollipop 5.0)**
	were released very close together. Most developers skipped 20 unless they were specifically building
	for the first generation of smartwatches (like the Moto 360 or LG G Watch).

### Permissions _Install-Time_ added in API Level 21
Lollipop (API Level 21) was a massive overhaul. While it didn't change the way users granted permissions (still install-time),
it introduced powerful new system capabilities that required fresh security gates.
- 📺 Media & Capture
	```
	REAL_GET_TASKS: A restrictive version of the old GET_TASKS, used to see which apps are running.
	CAPTURE_VIDEO_OUTPUT: Allows an app to capture video output. (Reserved for system/signature apps).
	CAPTURE_SECURE_VIDEO_OUTPUT: For capturing secure video content (Reserved for system/signature apps).
	PROJECT_MEDIA: Technically an Intent-based permission (via MediaProjectionManager).
		This allowed apps to record the screen or audio without a permanent manifest permission,
		triggering a user-facing popup every time the session starts.
	```
- 🔋 Power & Scheduling
	```
	BIND_JOB_SERVICE: Required for using the new JobScheduler.
		This ensured only the system could trigger your background tasks to save battery.
	```
- 📶 Connectivity & Networking
	```
	BIND_TELECOM_CONNECTION_SERVICE: Allows an app to act as a calling account for the system (essential for VoIP apps).
	BIND_VPN_SERVICE: Required for apps that want to provide VPN services.
	```
- 👤 Identity & Auth
	```
	USE_FINGERPRINT: (Technically added in the transition to API 21/23).
		While finalized in 23, the early foundations for hardware authentication began appearing here.
	MANAGE_USERS: Allowed system-level apps to manage secondary user profiles (the "Multi-user" feature expanded in Lollipop).
	```
- 🛠️ System & Tools
	```
	PACKAGE_USAGE_STATS: Allows an app to collect statistics about how other apps are being used
		(requires a special "Usage Access" screen in Settings).
	BIND_DREAM_SERVICE: Required for "Daydreams" (interactive screen savers).
	BIND_TV_INPUT: Required for apps that provide TV input sources (for the newly launched Android TV).
	```
- 💡 Pro-Tip for your Migration
	Lollipop introduced the **ART** (_Android Runtime_), replacing the old Dalvik VM.
	This changed how permissions were verified internally during app execution, making it much more strict.
	If your app uses Native Code (JNI), you’ll need to ensure your permissions cover any hardware access performed in C/C++.

### Permissions _Install-Time_ added in API Level 22
API Level 22 (Lollipop 5.1) was a strategic "bridge" release. It didn't add many new permissions
because Google was already heads-down working on the massive API 23 runtime overhaul.
However, it introduced some very specific ones for **Multi-SIM** support and system-level management.
- 📞 Telephony (Multi-SIM Support)
	This release added official support for devices with multiple SIM cards, requiring new ways to handle phone state:
	```
	BIND_TELECOM_CONNECTION_SERVICE: While introduced in 21, it was heavily refined here to allow third-party apps
		to integrate more deeply into the system's "Phone" app UI.
	READ_PRECISE_PHONE_STATE: A more granular version of READ_PHONE_STATE. It allowed apps to see exactly what is happening
		with the radio (e.g., specific signal strength or data connection types) rather than just the general phone ID.
	```
- 📶 Connectivity
	```
	NET_ADMIN: Allowed a system-level app to manage network configurations (mostly used for advanced tethering or VPN setups).
	```
- 🛠️ System & Management
	```
	BIND_CHOOSER_TARGET_SERVICE: This was the foundation for "Direct Share." It allowed apps to provide deep-link targets
		to the system "Share" menu (e.g., sharing a photo directly to a specific contact in WhatsApp).
	SCORE_NETWORKS: Allowed a specialized app to "score" Wi-Fi networks to tell the system which ones are high quality
		or open (used by the Google Connectivity Services).
	```
- 🧱 The "Calm Before the Storm"
	API 22 is the **very last version** where the "Install-time" model was the only way to do things.
	* If your app's `targetSdkVersion` is **22**, the user still sees the "big list" at the Play Store and clicks "Accept."
	* If you move just **one level up to 23**, your app will likely crash immediately unless you've added the `checkSelfPermission()` logic.


### Activity Manager
- List Activity Manager Commands and Options:
	`adb shell am help`
- Start an Activity/App:
	`adb shell am start -n <package_name>/<activity_name>`
- Start a Service:
	`adb shell am startservice -n <package_name>/<service_name>`
- Send a Broadcast Intent:
	`adb shell am broadcast -a <action>`
- Force-Stop an App:
	`adb shell am force-stop <package_name>`
- Clear All Recent Apps (might not work on all devices on mine does not work):
	`adb shell am clear-recent-apps`
- Modify Device Screen Properties:
	* Change the screen resolution:
		`adb shell am display-size <width>x<height>`
	* Change the screen density:
		`adb shell am display-density <density>

### Diagnotic Output of Running System Services
- Find the package of the currently running app:
	`adb shell dumpsys activity | grep top-activity`


### User Input
- Simulate a __Screen Tap__ at specified coordinates:
	`adb shell input tap <x> <y>`
- Simulate a __Swap Gesture__
	`adb shell input swipe <x1> <y1> <x2> <y2> [duration_ms]`
- Type __Text__ into the focused field:
	`adb shell input text ""`
- Simulate a __Button Press__:
	`adb shell input keyevent <keycode_or_name>`

### Keycode/Name Values:
*  0 -> KEYCODE_UNKNOWN	: Unknown Key Code
*  1 -> KEYCODE_MENU
*  3 -> KEYCODE_HOME	:	Home Button
*  4 -> KEYCODE_BACK	:	Back Button
*  5 -> KEYCODE_CALL
*  6 -> KEYCODE_ENDCALL
*  7 -> KEYCODE_0
*  8 -> KEYCODE_1
*  9 -> KEYCODE_2

* 10 -> KEYCODE_3
* 11 -> KEYCODE_4
* 12 -> KEYCODE_5
* 13 -> KEYCODE_6
* 14 -> KEYCODE_7
* 15 -> KEYCODE_8
* 16 -> KEYCODE_9

* 19 -> KEYCODE_DPAD_UP
* 20 -> KEYCODE_DPAD_DOWN
* 21 -> KEYCODE_DPAD_LEFT
* 22 -> KEYCODE_DPAD_RIGHT
* 23 -> KEYCODE_DPAD_CENTER

* 24 -> KEYCODE_VOLUME_UP	:	Volume Up
* 25 -> KEYCODE_VOLUME_DOWN	:	Volume Down
* 26 -> KEYCODE_POWER	:	Power Button (toggles on/off status)

* 29 -> KEYCODE_A
* 54 -> KEYCODE_Z

* 61 -> KEYCODE_TAB
* 62 -> KEYCODE_SPACE

* 64 -> KEYCODE_EXPLORER	:	Opens the browser
* 65 -> KEYCODE_ENVELOPE	:	Launches a mail application
* 66 -> KEYCODE_ENTER	:	Enter Key
* 67 -> KEYCODE_DEL	:	Delete (Backspace) Key

* 84 -> KEYCODE_SEARCH

* 187 -> KEYCODE_APP_SWITCH	:	Brings up the application switcher dialog

* 207 -> KEYCODE_CONTACTS	:	Opens the contacts list
* 208 -> KEYCODE_CALENDAR	:	Lanuches a calendar application
* 209 -> KEYCODE_MUSIC	:	Launches an audio player application

* 219 -> KEYCODE_ASSIST	:	Launches the global assist activity
* 220 -> KEYCODE_BRIGHTNESS_DOWN	:	Decreases screen brightness
* 221 -> KEYCODE_BRIGHTNESS_UP	:	Increases screen brightness

* 223 -> KEYCODE_SLEEP	:	Puts the device to sleep
* 224 -> KEYCODE_WAKEUP	:	Wakes up the device

* 231 -> KEYCODE_VOICE_ASSIST	:	Launches the global voice assistant activity

## Commands and Sequences:

### Unlock without passcode:
- Wake Screen: `adb shell input keyevent 224`
- Swipe Right: `adb shell input keyevent 22`

### Unlock with passcode:
- Wake Screen: `adb shell input keyevent 224`
- Type PIN: `adb shell input text your_pin`
- Enter: `adb shell input keyevent 66`

### Stimulate the swipe `adb shell input swipe <x1> <y1> <x2> <y2> <duration>`:
1. <x1>, <y1> : Starting X and Y coordinates
2. <x2>, <y2> : Ending X and Y coordinates
3. <duration> : Optional duration in milliseconds (e.g., 300 for a smooth swipe)
_Example for a vertical swipe (swiping up near the center of the screen)_:
	`adb shell input swipe 500 1500 500 500 300`

### Input text with spaces
1. Replace spaces with `%s` (especially on older Android):
	`adb shell input text "IDroid%sText%sWith%sSpaces"
2. Wrap the entire command in single quotes and the text in double quotes (in some new Android):
	`adb shell 'input text "IDroid Text With Spaces"'`
3. Escape the space character:
	`adb shell input text "IDroid\\ Text\\ With\\ Spaces"`
4. Use a _Shell Script_:
	```bash
	text=$(printf '%s%%s' ${@})	# concatinating and replacing spaces with %s
	text=${text%%%s}			# removing the trailing %s
	adb shell input text "$text"
	```

### Input text unicode characters
1. Install the [APK](https://github.com/senzhk/ADBKeyBoard.git):
	`adb install ADBKeyboard.apk`
2. Enable the ADB Keyboard input method (editor) in the device settings:
	`adb shem ime enable com.android.adbkeyboard/.AdbIME`
3. Set ADB Keyboard as the default IME for the duration of the input session:
	`adb shell ime set com.android.adbkeyboard/.AdbIME`
4. Send the Unicode text directly with UTF-8 or encoded in Base64:
	`adb shell am broadcast -a ADB_INPUT_TEXT --es msg '﷽  '`
	`adb shell am broadcast -a ADB_INPUT_B64 --es msg '...'`
5. Switch back to the original keyboard one finished:
	- List available keyboards: `adb shell ime list -a`
	- Set back: `adb shell ime set com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME`
6. Alternative Methods: [scrcpy](https://github.com/Genymobile/scrcpy.git)
