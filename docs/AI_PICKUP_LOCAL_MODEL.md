# On-device AI Pickup model setup

The AI Pickup dispatcher tries these backends in order:

1. Gemini Nano through ML Kit Agent + AppFunctions, when AICore reports it is already available.
2. ADK + LiteRT-LM with Gemma 4 E2B IT, when the device passes runtime and memory checks and the model file is installed.
3. Firebase ADK as the final fallback.

## Provisioning the LiteRT-LM model

The Gemma 4 E2B IT LiteRT-LM model is approximately 2.6 GB. The app does not download it automatically because doing so could consume mobile data and storage without the associate's consent.

Download `gemma-4-E2B-it.litertlm` from the official LiteRT community repository:
`litert-community/gemma-4-E2B-it-litert-lm`

Place the file in the app-specific external files directory:
`/sdcard/Android/data/com.suncommerz.associate/files/gemma-4-E2B-it.litertlm`

For development, with the app installed and a device connected over ADB:

```bash
adb push gemma-4-E2B-it.litertlm /sdcard/Android/data/com.suncommerz.associate/files/
```

The dispatcher attempts LiteRT-LM only when the file exists and is at least 2 GB, the device is arm64, Android 8+, not classified as low-RAM, has at least 8 GB total RAM and 3 GB currently available, and has at least 512 MB free cache storage. These are conservative app-side admission checks, not official universal hardware guarantees. If any check fails, it proceeds to Firebase.

## Build toolchain

The current ADK Kotlin LiteRT-LM Android integration uses the LiteRT-LM 0.16.x runtime, whose Android/JVM artifacts require JDK 21 or newer to compile against. Use JDK 21 for Gradle builds.

The model file is app-scoped and is not bundled in the APK. A production download/install UI should explicitly ask the associate for consent and preferably require unmetered Wi-Fi before downloading the 2.6 GB weights.
