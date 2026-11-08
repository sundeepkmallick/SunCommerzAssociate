# AI model preferences and on-device AI

This document describes model selection and local-model handling for the AI Pickup feature in the current application. Model management is separate from the BOPIS fulfillment workflow: installing or changing a model does not alter the associate proposal-and-approval process.

## Backend preference

The first-install default is **Firebase ADK**. In the app bar, open **AI Model Preferences** to choose a preferred backend:

- **Firebase ADK** — cloud model execution through Firebase AI Logic and Google ADK for Kotlin.
- **Gemini Nano** — on-device model access through the ML Kit GenAI Agent API. Android AICore manages model provisioning; the app does not download Gemini Nano weights.
- **LiteRT-LM** — local model execution through Google ADK's LiteRT-LM adapter. The app downloads and manages the selected `.litertlm` artifact.

If Gemini Nano or LiteRT-LM is selected but cannot initialize, execute, or return a supported pickup action, `HybridPickupAgentManager` falls back to Firebase for that invocation. The saved preference remains unchanged. If Firebase itself fails, the request fails rather than silently changing the associate's preference.

Firebase and LiteRT-LM use the shared `PickupAdkAgentFactory`. Gemini Nano uses its separate ML Kit Agent API/AppFunctions implementation. They share BOPIS instructions and application capabilities, but their APIs, model lifecycle, and session behaviour differ. In particular, the current Gemini Nano implementation is single-turn and does not provide the same ADK session handling.

## Current LiteRT-LM model catalogue

The current `LocalModelCatalog` contains two downloadable model definitions. The project owner has reported validating both models' BOPIS tool-calling behaviour with the app's LiteRT-LM **0.13.1** integration.

| Model | Approx. download size | Artifact revision | App BOPIS validation |
|---|---:|---|---|
| Gemma 4 E2B IT | 2,583 MB | `6b78abd019e61a1ca4cbe3b212d2c9ce8ff38a94` | Validated with LiteRT-LM 0.13.1 |
| Qwen2.5 1.5B Instruct | 1,598 MB | `19edb84` | Validated with LiteRT-LM 0.13.1 |

The catalogue records pinned download URLs, exact expected byte counts, and SHA-256 checksums for both entries. The checksum and expected size protect against an unexpected or incomplete artifact; they do not establish model quality, device performance, or compatibility with a different runtime/ADK version.

The catalogue is intentionally app-specific. Its `pickupAgentValidated` flag means the model has passed the BOPIS tool-calling scenarios recorded for this app; it is not a general model certification and should not be copied into a future generic agent SDK as a universal compatibility flag.

## Gemma 4 E2B IT artifact

The app downloads the CPU-compatible `.litertlm` artifact from the LiteRT Community Hugging Face repository using a pinned revision:

- Repository: `litert-community/gemma-4-E2B-it-litert-lm`
- File: `gemma-4-E2B-it.litertlm`
- Pinned revision: `6b78abd019e61a1ca4cbe3b212d2c9ce8ff38a94`
- Expected size: `2,588,147,712` bytes (approximately 2.59 GB)
- Expected SHA-256: `181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c`

The catalogue records LiteRT-LM 0.13.1 as both the minimum and highest tested runtime for this exact app integration. Recheck the upstream revision and checksum before changing the artifact.

## Qwen2.5 1.5B Instruct artifact

The app downloads the LiteRT-LM conversion from the LiteRT Community Hugging Face repository:

- Repository: `litert-community/Qwen2.5-1.5B-Instruct`
- File: `Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm`
- Pinned revision: `19edb84`
- Expected size: `1,597,931,520` bytes
- Expected SHA-256: `faa60663b333290c1496c499828b21d3e3254a788cacd8cce917ce0f761a2dc9`

The catalogue records LiteRT-LM 0.13.1 as the minimum version verified for this app integration. It does not currently record Qwen-specific minimum Android API, ABI, total RAM, available RAM, or runtime-cache requirements. The app still rejects devices classified as low-RAM, and device suitability should be checked on each target device rather than inferred from the model's successful tool-calling test on another device.

Review the exact upstream model card and license before redistributing the converted artifact.

## Download and integrity handling

- The associate explicitly starts a model download from AI Model Preferences.
- Wi-Fi/unmetered network is the default; mobile data can be explicitly enabled before starting.
- Android DownloadManager owns the transfer, allowing it to continue outside the app and survive app process death/reboot.
- The preferences screen reports transfer progress and status when reopened. A failed download can be retried; an in-progress download can be cancelled.
- The app uses the catalogue size plus 512 MiB as a conservative free-storage check. Android may still report insufficient space during transfer.
- Downloads are staged as temporary files. The app checks exact byte size when `expectedSizeBytes` is present and verifies SHA-256 when a valid checksum is recorded.
- A replacement is staged only after the available integrity checks complete. The currently installed model is retained if a replacement download or an available integrity check fails.
- The LiteRT-LM engine is released before a model file is replaced or removed. After successful activation, the previous model file is deleted.
- Only one local model is retained after a successful replacement. During a replacement download, storage may temporarily be needed for both the old and new model.
- Removing the installed model switches a LiteRT-LM preference back to Firebase ADK.

The model is stored in the app-specific external files directory. The exact filename is determined by the selected catalogue entry.

### What integrity checks mean

A matching size and SHA-256 establish that the downloaded file matches the catalogue's recorded artifact metadata. They do not prove that the model will load on every device, perform well, call tools correctly, or work with future versions of LiteRT-LM and ADK.

## LiteRT-LM runtime and device checks

The app pins LiteRT-LM **0.13.1** in the Gradle version catalogue. The same version-catalog entry supplies both the Android dependency and `BuildConfig.LITERT_LM_VERSION` shown by the app.

Before loading a local model, the runtime adapter applies catalogue-specific checks:

- Minimum Android API when recorded for the model.
- ARM64 ABI when required by the model metadata.
- Device is not classified as a low-RAM device.
- Minimum total RAM, currently available RAM, and runtime-cache free space when recorded for the selected model.
- The model's app-specific `pickupAgentValidated` flag must be true.

Gemma 4 E2B IT has these conservative app admission values:

- Android API 26 or later.
- `arm64-v8a`.
- At least 8 GiB total RAM.
- At least 3 GiB currently available RAM.
- At least 512 MiB free runtime-cache space.

These are application guardrails, not official universal Google hardware requirements. The Qwen entry does not currently specify equivalent per-model numeric thresholds; target-device testing remains necessary.

## Runtime compatibility and download eligibility

The catalogue stores minimum and, when known, highest-tested LiteRT-LM versions as guidance for each artifact. The Preferences UI can display whether the packaged runtime falls within the recorded guidance.

Experimental download eligibility is deliberately separate from runtime and BOPIS validation. In the current implementation, a model is downloadable when its catalogue entry is enabled, has a non-empty artifact URL, has a positive size estimate, and has either no checksum or a well-formed SHA-256 value. Runtime compatibility notes are informational for download; they are not a substitute for runtime admission and successful model initialization.

For the two current models, the project owner has reported successful BOPIS tool-calling with LiteRT-LM 0.13.1. Do not infer that a future LiteRT-LM or ADK upgrade is compatible without testing the exact dependency pair and model artifacts again.

## Gemini Nano availability

Gemini Nano is not part of the LiteRT-LM download catalogue. The app checks the ML Kit GenAI feature status and Android API level before running the Gemini Nano Agent API. AICore owns model provisioning; the app neither downloads nor replaces the Nano model.

A supported Android API level does not guarantee feature availability. Test on a physical device that reports the feature as available. The project owner has not reported a successful device test of this path on a supported Gemini Nano device yet.

## Model compatibility testing

Treat compatibility as a combination of the exact model artifact, runtime, ADK adapter, Android device, and app tool set. When changing any of them, validate separately:

1. Verify the publisher's model card, immutable artifact revision, file name, size, checksum, license, and target hardware.
2. Confirm the LiteRT-LM Android dependency and Google ADK LiteRT-LM adapter versions are compatible.
3. Download and verify the exact artifact.
4. Initialize and load the model on the intended physical device.
5. Run inference and confirm the response is usable.
6. Run ADK tool-calling tests, including confirmation that required stock lookup tools are invoked.
7. Run the BOPIS acceptance scenarios: sufficient current-store stock, substitute selection, nearby-store fulfillment, unavailable item, malformed/unsupported response, and stock changes before approval.
8. Confirm Firebase fallback behaviour when the preferred local backend fails.

Record the tested combination of artifact revision, LiteRT-LM version, ADK adapter version, Android API/ABI, and device. A successful model load alone does not establish tool-call correctness or safe fulfillment behaviour.

## Validation status and limitations

- The project owner reports BOPIS tool-calling validation for Gemma 4 E2B IT and Qwen2.5 1.5B Instruct with LiteRT-LM 0.13.1.
- The project owner has previously exercised the Firebase SDK path.
- Gemini Nano has not yet been device-tested on a supported physical device in the reported validation.
- No Gradle build or automated test result is asserted by this document. Check current CI/build results and repeat device acceptance tests after changing dependencies, artifacts, tool definitions, or approval logic.
