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

The preferences screen is backed by `LocalModelCatalog`. Download eligibility is intentionally permissive for experimental testing and is separate from runtime support, integrity metadata, and BOPIS suitability. Any catalogue entry with a real artifact URL and a usable size estimate can be downloaded, even when runtime compatibility or pickup-agent behaviour is not yet verified.

The preferences screen lists ten candidates. Associates can select any entry to inspect its approximate size, runtime notes, device guidance, license guidance, and availability. Models with artifact URLs are offered as experimental downloads. Entries without an artifact URL remain informational. Downloading does not make a model selectable as the AI Pickup backend: the separate pickup-agent validation flag still controls activation. Gemma 4 E2B IT remains the only model validated for this app's BOPIS flow.

Google's LiteRT-LM documentation lists models beyond Gemma, including Gemma 3n, FunctionGemma, Phi-4 Mini, Qwen2.5 and Qwen3. That is runtime/model-family support, not a guarantee that every artifact works with this app's pinned runtime or that every model reliably invokes our ADK tools. Check the [official supported-model list](https://developers.google.com/edge/litert-lm/overview) and the individual model card before adding an entry.

For experimental testing, a pinned revision, exact byte count, checksum, runtime minimum, and device requirements may be incomplete; those gaps must be shown to the associate. Never invent an artifact URL or checksum. Do not mark a model pickup-agent validated until its BOPIS scenarios pass.

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
- The app uses the catalogue size estimate plus 512 MiB as a conservative free-storage check. Because estimates may be rounded, Android may still report insufficient space during transfer.
- The model is downloaded to a temporary file. Exact byte size is checked only when `expectedSizeBytes` is recorded. SHA-256 is checked when a valid checksum is available. If no checksum is published, the model is installed as **unverified** and the UI says so; this is experimental and should not be treated as trusted for production.
- A replacement is staged only after verification. The currently installed model is retained if the replacement download or integrity check fails. After successful activation, the previous model file is deleted.
- The LiteRT-LM engine is released before a model file is replaced or removed.
- Only one local model is retained after a successful replacement. The app may temporarily need space for both the existing and replacement model during download.
- Removing the installed model switches a LiteRT-LM preference back to Firebase ADK.

The Gemma model is stored in the app-specific external files directory:
`/sdcard/Android/data/com.suncommerz.associate/files/gemma-4-E2B-it.litertlm`

## LiteRT-LM runtime admission

The current Gemma runtime performs conservative device checks before initialization: Android 8+, arm64, not classified as low-RAM, at least 8 GiB total RAM, at least 3 GiB currently available, and 512 MiB free runtime-cache space. These are application guardrails, not official universal hardware guarantees. The ADK factory is model-agnostic; model-specific admission requirements belong in catalogue metadata and validation, not in pickup-agent instructions or tool logic.

## Runtime-version compatibility gate

The app generates `BuildConfig.LITERT_LM_VERSION` from the `litertLm` version-catalog entry in `gradle/libs.versions.toml`. This is the same version used by the `google-ai-edge-litertlm-android` dependency, so the Preferences screen does not rely on a separately maintained version string.

Each catalogue entry can declare:
- `minimumLiteRtLmVersion`: the minimum version documented or tested for that exact artifact. This is informational during experimental download.
- `maximumTestedLiteRtLmVersion`: the highest runtime version tested with this app integration. This is informational during experimental download.
- `sizeBytes`: estimated or known transfer size, used for storage planning and progress fallback.
- `expectedSizeBytes`: nullable exact byte count; only enforce exact file-size matching when present.
- Optional SHA-256. When absent, installation is labelled unverified.
- Independent ADK and BOPIS validation flags.

The experimental download gate is enforced in the Preferences UI, ViewModel, and download manager. A model needs a usable artifact URL and positive size estimate. A checksum is verified when available; an absent checksum does not block experimentation but is explicitly reported. Runtime compatibility is shown as guidance rather than a download blocker. Selecting a catalogue entry does not make it an approved AI Pickup backend.

Gemma 4 E2B IT has been exercised with the app's 0.13.1 integration. Qwen3 0.6B can be downloaded experimentally with other runtime versions, but the recorded 0.18.0 guidance should be considered when testing. A successful download does not prove that the runtime, ADK adapter, device, or BOPIS tools work together.

## Model compatibility research without downloading weights

Most initial compatibility research can be performed without downloading multi-gigabyte model files:
1. Inspect the publisher's model card and immutable repository revision for a real `.litertlm` artifact, file name, file size, license and documented target.
2. Inspect Google's LiteRT-LM supported-model list, release notes, and the artifact's model configuration/metadata. LiteRT-LM model-inspection tooling can report model metadata and requirements when supported.
3. Check the exact LiteRT-LM runtime API and Android artifact used by this project.
4. Check the exact Google ADK LiteRT-LM adapter version and its declared runtime dependencies.
5. Run a small import/load/inference test on a controlled Android test device or emulator with the exact artifact/runtime pair. This step requires the model file, but can be performed by CI/device-lab automation rather than manually on every associate's phone.
6. Separately run ADK tool-calling tests and the app's BOPIS acceptance scenarios. A successful model load does not establish tool-call correctness.

Record tested combinations as a matrix of artifact revision × LiteRT-LM version × Android ABI/device × ADK adapter version. Do not infer compatibility for every version in a family from one model card or one benchmark.

## Build and validation

- The app currently pins LiteRT-LM 0.13.1 for its ADK integration. Do not upgrade this dependency without checking the ADK adapter compatibility and rebuilding.
- The model is not bundled in the APK.
- Gemini Nano Agent API and Android AppFunctions support remain preview/alpha and require supported physical devices.
- Build, unit tests, emulator tests, and physical-device download/inference tests must pass before production rollout.


## Expanded model catalogue (November 2026)

The preferences screen lists ten candidates. Associates can select an entry to inspect its estimated bundle size, model type, runtime note, device note, license guidance, and experimental download availability. Entries with real artifact URLs can be downloaded for testing; entries without URLs remain informational.

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

Catalogue states are deliberately separate: listed means documented by Google; experimental download means a usable artifact URL and size estimate are present; checksum verified means a recorded SHA-256 matched; exact-size verified means a recorded expected byte count matched; BOPIS validated means the model passed the app's stock lookup, substitution/nearby-store, structured response, and tool-call scenarios. Gemma 4 E2B IT is currently the only model validated for this app's BOPIS flow. FunctionGemma is a base/function-specialised model rather than a standard chat model and requires separate evaluation.

The current app pins LiteRT-LM 0.13.1 with the ADK adapter. Qwen3's documented Android CPU/GPU candidate is demonstrated with LiteRT-LM 0.18.0, but this does not prove compatibility with the app's ADK adapter. Do not upgrade the runtime independently of checking the adapter, Kotlin/Gradle compatibility, and a complete Android build.
