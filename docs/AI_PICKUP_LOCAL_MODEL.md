# AI model preferences and on-device AI

AI model management is separate from the AI Pickup workflow. AI Pickup keeps its existing proposal and associate-approval flow; model installation never blocks or prompts during pickup.

## Backend preference

The first-install default is **Firebase ADK**. In the app bar, open **AI Model Preferences** to select a preferred backend:

- **Firebase ADK** — cloud backend and application fallback.
- **Gemini Nano** — Android/AICore-managed on-device model. The app checks ML Kit GenAI feature status and does not download or manage Gemini Nano weights.
- **Gemma 4 E2B IT (LiteRT-LM)** — optional model downloaded and stored by this app. It can be selected only after the downloaded artifact has passed size and SHA-256 verification.

If the selected local backend is unavailable or fails to initialize/produce a valid pickup action, the dispatcher uses Firebase ADK for that invocation. The preference remains unchanged so the associate can decide whether to switch it.

All backends share the pickup instructions and read-only inventory tools. Order mutations remain exclusively behind the existing associate approval action.

## Gemma 4 E2B IT download

The app downloads the CPU-compatible .litertlm artifact from the LiteRT Community Hugging Face repository using a pinned revision:

- Repository: litert-community/gemma-4-E2B-it-litert-lm
- File: gemma-4-E2B-it.litertlm
- Pinned revision: 6b78abd019e61a1ca4cbe3b212d2c9ce8ff38a94
- Expected size: 2,588,147,712 bytes (approximately 2.59 GB)
- Expected SHA-256: 181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c

The artifact and checksum are documented by the Arm Gemma 4 download guide and the Hugging Face model file metadata. Confirm the upstream revision/checksum again before changing the pinned artifact.

### Download behavior

- The associate explicitly starts the download from AI Model Preferences.
- Wi-Fi/unmetered network is the default. Mobile data can be explicitly enabled before starting.
- Android DownloadManager owns the transfer, so it continues outside the app and survives app process death/reboot. The platform retries and resumes interrupted transfers when the host supports it.
- The settings screen reports transfer progress and status when reopened.
- Cancel removes the in-progress download and partial file. A failed download can be retried.
- The app requires at least the artifact size plus 512 MiB free in app-specific external storage.
- The file is first downloaded as a temporary file. The app checks exact byte size and SHA-256, then renames it to the final model filename. A failed integrity check discards the file.
- Removing the model switches a LiteRT-LM preference back to Firebase ADK.

The final model path is the app-specific external files directory:
 /sdcard/Android/data/com.suncommerz.associate/files/gemma-4-E2B-it.litertlm

## LiteRT-LM runtime admission

The existing runtime performs conservative device checks before initialization: Android 8+, arm64, not classified as low-RAM, at least 8 GiB total RAM, at least 3 GiB currently available, and 512 MiB free runtime-cache space. These are application guardrails, not official universal hardware guarantees. A model initialization or inference failure falls back to Firebase ADK.

## Build and validation

- Use JDK 21 or newer for the LiteRT-LM Android runtime dependencies.
- The model is not bundled in the APK.
- Gemini Nano Agent API and Android AppFunctions support remain preview/alpha and require supported physical devices.
- Build, unit tests, emulator tests, and physical-device download/inference tests must pass before production rollout.
