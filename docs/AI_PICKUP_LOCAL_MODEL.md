# AI model preferences and on-device AI

AI model management is separate from the AI Pickup workflow. AI Pickup keeps its existing proposal and associate-approval flow; model installation never blocks or prompts during pickup.

## Backend preference

The first-install default is **Firebase ADK**. In the app bar, open **AI Model Preferences** to select a preferred backend:

- **Firebase ADK** — cloud backend and application fallback.
- **Gemini Nano** — Android/AICore-managed on-device model. The app checks ML Kit GenAI feature status and does not download or manage Gemini Nano weights.
- **LiteRT-LM** — optional local model runtime. The selected artifact is downloaded and verified by this app.

If the selected local backend is unavailable or fails to initialize/produce a valid pickup action, the dispatcher uses Firebase ADK for that invocation. The preference remains unchanged so the associate can decide whether to switch it.

All backends share the pickup instructions and equivalent inventory capabilities. Firebase and LiteRT-LM use the shared ADK agent factory; Gemini Nano uses its separate ML Kit Agent API/AppFunctions integration. Order mutations remain exclusively behind the existing associate approval action.

## Local model catalogue

The preferences screen is backed by `LocalModelCatalog`. Runtime support and BOPIS suitability are tracked separately: a model must have a compatible LiteRT-LM artifact, a pinned URL and checksum, and pass the app's pickup tool-calling scenarios before it is downloadable.

The preferences screen lists ten candidates. Associates can select any entry to inspect its approximate size, runtime notes, device guidance, license guidance, and availability. Only models with a pinned artifact, integrity metadata, compatible app/runtime integration, and successful BOPIS tool-calling validation can be downloaded. Gemma 4 E2B IT is currently the only enabled model; the complete catalogue is documented below.

Google's LiteRT-LM documentation lists models beyond Gemma, including Gemma 3n, FunctionGemma, Phi-4 Mini, Qwen2.5 and Qwen3. That is runtime/model-family support, not a guarantee that every artifact works with this app's pinned runtime or that every model reliably invokes our ADK tools. Check the [official supported-model list](https://developers.google.com/edge/litert-lm/overview) and the individual model card before adding an entry.

Do not add an artifact as downloadable until its exact revision, filename, size, SHA-256, minimum runtime version, device requirements and pickup-agent validation state are known.

## Gemma 4 E2B IT artifact

The app downloads the CPU-compatible .litertlm artifact from the LiteRT Community Hugging Face repository using a pinned revision:

- Repository: litert-community/gemma-4-E2B-it-litert-lm
- File: gemma-4-E2B-it.litertlm
- Pinned revision: 6b78abd019e61a1ca4cbe3b212d2c9ce8ff38a94
- Expected size: 2,588,147,712 bytes (approximately 2.59 GB)
- Expected SHA-256: 181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c

Confirm the upstream revision/checksum again before changing the pinned artifact.

## Download, replacement and removal

- The associate explicitly starts the download from AI Model Preferences.
- Wi-Fi/unmetered network is the default. Mobile data can be explicitly enabled before starting.
- Android DownloadManager owns the transfer, so it continues outside the app and survives app process death/reboot.
- The settings screen reports transfer progress and status when reopened. A failed download can be retried; an in-progress download can be cancelled.
- The app requires the artifact size plus 512 MiB free in app-specific external storage.
- The model is downloaded to a temporary file. Exact byte size and SHA-256 are checked before activation.
- A replacement is staged only after verification. The currently installed model is retained if the replacement download or integrity check fails. After successful activation, the previous model file is deleted.
- The LiteRT-LM engine is released before a model file is replaced or removed.
- Only one local model is retained after a successful replacement. The app may temporarily need space for both the existing and replacement model during download.
- Removing the installed model switches a LiteRT-LM preference back to Firebase ADK.

The Gemma model is stored in the app-specific external files directory:
`/sdcard/Android/data/com.suncommerz.associate/files/gemma-4-E2B-it.litertlm`

## LiteRT-LM runtime admission

The current Gemma runtime performs conservative device checks before initialization: Android 8+, arm64, not classified as low-RAM, at least 8 GiB total RAM, at least 3 GiB currently available, and 512 MiB free runtime-cache space. These are application guardrails, not official universal hardware guarantees. The ADK factory is model-agnostic; model-specific admission requirements belong in catalogue metadata and validation, not in pickup-agent instructions or tool logic.

## Build and validation

- The app currently pins LiteRT-LM 0.13.1 for its ADK integration. Do not upgrade this dependency without checking the ADK adapter compatibility and rebuilding.
- The model is not bundled in the APK.
- Gemini Nano Agent API and Android AppFunctions support remain preview/alpha and require supported physical devices.
- Build, unit tests, emulator tests, and physical-device download/inference tests must pass before production rollout.


## Expanded model catalogue (November 2026)

The preferences screen now lists the ten chat/base candidates from Google's LiteRT-LM supported-model table. Associates can select any entry to inspect its approximate bundle size, model type, runtime note, device note, license guidance, and availability. Selection is informational; it does not enable installation.

| Model | Approx. bundle size | Type | Availability |
|---|---:|---|---|
| Gemma 4 E2B IT | 2,583 MB | Chat | Downloadable; exact artifact pinned and BOPIS validated |
| Gemma 4 E4B IT | 3,654 MB | Chat | Listed; artifact and BOPIS validation pending |
| Gemma 3n E2B | 2,965 MB | Chat | Listed; artifact and BOPIS validation pending |
| Gemma 3n E4B | 4,235 MB | Chat | Listed; artifact and BOPIS validation pending |
| Gemma 3 1B | 1,005 MB | Chat | Listed; artifact and BOPIS validation pending |
| FunctionGemma | 289 MB | Base/function model | Experimental; separate tool-calling evaluation required |
| Phi-4 Mini | 3,906 MB | Chat | Listed; artifact and BOPIS validation pending |
| Qwen2.5 1.5B | 1,598 MB | Chat | Listed; artifact and BOPIS validation pending |
| Qwen3 0.6B | 586 MB | Chat | Google Android guidance demonstrates a CPU/GPU artifact with LiteRT-LM 0.18.0; app ADK compatibility pending |
| Qwen2.5 0.5B | 521 MB | Chat | Listed; artifact and BOPIS validation pending |

Sizes are approximate published bundle sizes, not peak memory requirements. Google's benchmark results are tied to particular phones and execution backends; they are reference points, not universal minimum hardware specifications. Exact artifact ABI, Android API, available RAM, runtime cache, and device performance must be measured before enabling a model for download.

Catalogue states are deliberately separate: listed means documented by Google; downloadable requires an exact artifact URL/revision, filename, byte size, SHA-256, license review, compatible app/runtime integration, and successful integrity verification; BOPIS validated means the model has passed the app's stock lookup, substitution/nearby-store, structured response, and tool-call scenarios. Only Gemma 4 E2B IT currently meets the download and BOPIS validation gates. FunctionGemma is a base/function-specialised model rather than a standard chat model and requires separate evaluation.

The current app pins LiteRT-LM 0.13.1 with the ADK adapter. Qwen3's documented Android CPU/GPU candidate is demonstrated with LiteRT-LM 0.18.0, but this does not prove compatibility with the app's ADK adapter. Do not upgrade the runtime independently of checking the adapter, Kotlin/Gradle compatibility, and a complete Android build.
