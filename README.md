# 👾 IDroid [Android Research, Lessons \& Hacks] 🧪
# `$_`

## Setup \& Build
Make sure you have `Android SDK` and `Kotlin lib` before setting variables!
```shell
export ANROID_HOME=/usr/lib/android-sdk

export KOTLIN_LIB=/usr/share/kotlin/kotlinc/lib
```

Make sure you have all these binaries before building an app!
```shell
aapt      - Android Asset Packaging Tool
kotlinc   - Kotlin Compiler
dx        - Dalvik Exchange (or Executable)
zipalign  (would be nice but don't have)
keytool   - Key and Certificate Management Tool
apksigner - Signing Android APK Tool 
adb       - Android Debug Bridge
```

- Use `IDroid` as a template project
- Get help: `make` or `make help`
- Build, install and run the app: `make cycle`
