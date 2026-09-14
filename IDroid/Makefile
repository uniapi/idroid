# \uFDFD
#      \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0627\u0644\u062D\u0643\u064A\u0645
#                  \u06F1\u06F4\u06F4\u06F7 \u0631\u062C\u0628 \u06F1\u06F7
#               \u0661\u0664\u0664\u0647 \u0634\u0639\u0628\u0627\u0646  \u0661

LEVEL ?= 14
PLATFORM := $(ANDROID_HOME)/platforms/android-$(LEVEL)
LOG ?= "*:D"

KOTLIN_LIB ?= /usr/share/kotlin/kotlinc/lib
JAVA_LIB ?= /usr/share/java

APP ?= salamun
ACTIVITY ?= SalamActivity
override ACTIVITY := $(basename $(ACTIVITY))
APPKEY = $(APP)key
APKDIR ?= apk
RESDIR ?= ""
PKGDIR ?= localhost/idroid/$(APP)
PKGROOTDIR := $(shell echo "$(PKGDIR)" | sed 's/\/.*//')
PKGPATH := $(shell echo "$(PKGDIR)" | sed 's/\/\+/./g')

MANIFEST := AndroidManifest.xml
SRC := *.kt
LIB := \
	$(PLATFORM)/android.jar \
	$(KOTLIN_LIB)/kotlin-reflect.jar
ILIB ?= "" # $(KOTLIN_LIB)/kotlin-stdlib.jar : pass manually only once after `make clean` to optimize the build

INCLUDE := -I $(shell echo $(LIB) | sed 's/[[:blank:]]/ -I /g')
CLASSPATH := $(shell echo $(LIB) | sed 's/[[:blank:]]/:/g')

export KEYSTORE ?= ./salamun.jks
export IPASS ?= "\u0627\u0633\u0645 \u0627\u0644\u0644\u0651\u0670\u0647"
export KEYDNAME ?= 'CN=localhost, O="IDroid"'

help:
	@echo "Commands:\n" \
		"    res\tCompile resources into R.java\n" \
		"    build\tBuild Activities\n" \
		"    dex\tConvert into the DEX (Dalvik Executable) format\n" \
		"    lib\tCreate a shared library\n" \
		"    pack\tPack into the APK (Android Package Kit) format\n" \
		"    align\tAlign the APK for better performance\n" \
		"    key\tGenerate a key to sign an apk\n" \
		"    sign\tSign the apk\n" \
		"    install\tInstall the apk\n" \
		"    run\tRun the apk\n" \
		"    log\tLogcat the apk\n" \
		"    stop\tStop the apk\n" \
		"    uninstall\tUninstall the apk\n" \
		"    clean\tRemove all the generated files and folders\n" \
		"\n" \
		"    app\tbuild dex pack |align| |key| sign\n" \
		"    sync\tinstall run\n" \
		"    cycle\tapp sync\n" \
		"VARIABLES:\n" \
		"    LEVEL APP ACTIVITY PKGDIR APKDIR KEYSTORE IPASS KEYDNAME LOG\n"

app: res build dex pack sign

sync: install run

cycle: app sync

res: $(MANIFEST) $(LIB)
	@if [ -d "$(RESDIR)" ]; then \
		aapt package -f -m -S $(RESDIR) -M $(MANIFEST) -J . -I $(PLATFORM)/android.jar; \
	fi

build: $(SRC)
	@sed -i 's/<uses-sdk android:minSdkVersion="[^"]*"/<uses-sdk android:minSdkVersion="$(LEVEL)"/g' $(MANIFEST)
	@sed -i 's/<activity android:name="[^"]*"/<activity android:name="\.$(ACTIVITY)"/g' $(MANIFEST)
	@if [ -n "$(RESDIR)" ] && [ -f "$(PKGDIR)/R.java" ]; then \
		kotlinc -classpath $(CLASSPATH) -d . $(PKGDIR)/R.java $(SRC); \
	else \
		kotlinc -classpath $(CLASSPATH) -d . $(SRC); \
	fi

dex:
	@if [ ! -d $(APKDIR) ]; then mkdir $(APKDIR); fi
	dx --dex --multi-dex --output $(APKDIR) $(PKGDIR)/*.class
	@if [ -n $(ILIB) ]; then dx --dex --output $(APKDIR)/classes2.dex $(ILIB); fi

lib:
	mkdir -p $(APKDIR)/lib/arm64-v8a $(APKDIR)/lib/armeabi-v7a

pack:
	@if [ -d $(RESDIR) ]; then \
		aapt package -f -M $(MANIFEST) -S $(RESDIR) $(INCLUDE) -F $(APP).apk $(APKDIR); \
	else \
		aapt package -f -M $(MANIFEST) $(INCLUDE) -F $(APP).apk $(APKDIR); \
	fi

align:
	zipalign -f -p -v 4 $(APP).apk $(APP).apk

key:
	keytool -genkeypair -alias $(APPKEY) -validity 10000 -keyalg RSA -keysize 2048 \
			-keystore $(KEYSTORE) -storepass $(IPASS) -keypass $(IPASS) -dname $(KEYDNAME); \

sign:
	@if [ ! -f $(KEYSTORE) ]; then $(MAKE) key --no-print-directory; fi
	apksigner sign --ks $(KEYSTORE) --ks-key-alias $(APPKEY) \
		--ks-pass pass:$(IPASS) --key-pass pass:$(IPASS) \
		--out $(APP).signed.apk $(APP).apk

install:
	adb install -r --no-incremental $(APP).signed.apk

run:
	adb shell am start -n "$(PKGPATH)/.$(ACTIVITY)"

log:
	adb logcat $(LOG) | grep --color=always $(PKGPATH)

stop:
	adb shell am force-stop $(PKGPATH)

uninstall:
	adb uninstall $(PKGPATH)

clean:
	rm -rf META-INF $(PKGROOTDIR) $(APKDIR) $(APP)*.apk*
