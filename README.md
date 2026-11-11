# SunCommerz Associate

An Android application for store associates managing **Buy Online, Pick Up In Store (BOPIS)** order fulfillment. The app guides associates through picking customer orders, resolving unavailable items, handling substitutions, finding nearby-store inventory, and reporting inventory issues.

> **Project status:** The AI Pickup implementation supports Firebase ADK, LiteRT-LM, and Gemini Nano backends. Availability depends on Firebase configuration, Android device support, and the selected local model.

## Features

- **Pickup order workflow** — review orders and work through their items.
- **Guided item picking** — record picked items and handle exceptions.
- **Substitutions** — discover suitable substitute products and manage substitution decisions.
- **Nearby-store fulfillment** — find nearby stores with sufficient inventory when the current store cannot fulfill an item.
- **Inventory issue reporting** — report unavailable and low-stock items to store management.
- **Associate dashboard and notifications** — view work and receive relevant updates.
- **AI-assisted pickup proposals** — an agent evaluates fulfillment options using application tools. Associates review and approve proposals; AI does not silently commit order changes.

## AI Pickup

AI Pickup uses a common `PickupAgent` interface with three backend implementations:

| Backend | Technology | Role |
|---|---|---|
| Firebase | Google ADK for Kotlin + Firebase AI Logic | Cloud model execution and fallback |
| LiteRT-LM | Google ADK for Kotlin + LiteRT-LM | Locally downloaded on-device model |
| Gemini Nano | SDK `agent-mlkit` + host Android AppFunctions | AICore-managed on-device model |

Firebase and LiteRT-LM share the SDK ADK agent factory and BOPIS tool set. Gemini Nano uses the SDK's `agent-mlkit` backend provider, which encapsulates ML Kit Agent/Prompt clients, readiness checks and AgentRunner lifecycle. The host retains AppFunctions declarations, manifest registration, KSP processing and business authorization. The implementations share BOPIS instructions and equivalent application capabilities, but do not have identical underlying APIs or session lifecycles.

The associate selects a preferred backend in **AI Model Preferences**. If a preferred on-device backend is unavailable, fails, or returns an invalid pickup action, the app falls back to Firebase for that invocation. The preference itself is not silently changed.

### Safety and fulfillment integrity

The agent can read application data through tools for stock, store inventory, nearby stores, and substitute candidates. It returns one structured pickup action:

- `PICKED`
- `SUBSTITUTE(productId)`
- `RESERVE_NEARBY_STORE(storeId)`
- `UNAVAILABLE`

The model response is only a proposal. The app independently checks current inventory and eligible fulfillment options before showing the proposal, then revalidates stock/options when the associate approves it. Existing domain use cases perform the actual order updates. Stale or invalid proposals are rejected rather than trusted.

See [AI Pickup architecture](docs/AI_PICKUP_ARCHITECTURE.md) for the request flow, tool inventory, backend lifecycle details, and validation boundaries.

## On-device model support

The current LiteRT-LM catalogue contains two downloadable models validated by the project owner for this app's BOPIS tool-calling flow with LiteRT-LM **0.13.1**:

- **Gemma 4 E2B IT**
- **Qwen2.5 1.5B Instruct**

Both entries include pinned artifact revisions, expected file sizes, and SHA-256 checksums. Device suitability is still model- and device-dependent; Gemma has explicit app admission guardrails, while Qwen device requirements need evaluation on each target device.

Gemini Nano is provisioned by Android AICore. The app checks ML Kit GenAI availability and does not download or manage Gemini Nano weights. A supported Android version alone does not guarantee that Gemini Nano is available on a particular device.

See [AI model preferences and on-device AI](docs/AI_PICKUP_LOCAL_MODEL.md) for model metadata, download/integrity behaviour, device checks, runtime compatibility, and testing guidance.

## Reusable agent SDK

The app integrates [SunMobileAiAgentSdk](https://github.com/sundeepkmallick/SunMobileAiAgentSdk) using six published `0.1.0-rc.2` Maven artifacts (the app currently depends on five of them). Maven Central is the default source. For local development, select Maven Local with `-PsunAiSdkSource=mavenLocal` or a sibling SDK checkout with `-PsunAiSdkSource=includeBuild -PsunAiSdkDir=../SunMobileAiAgentSdk`. See [SDK integration and source selection](docs/SUNMOBILE_AI_AGENT_SDK_INTEGRATION.md) for exact commands, persistent local configuration and Android Studio setup.

## Technology

- Kotlin
- Jetpack Compose and Material 3
- AndroidX Navigation
- Hilt dependency injection
- Clean Architecture-inspired presentation, domain, and data layers
- Google ADK for Kotlin
- Firebase AI Logic
- LiteRT-LM
- SunMobileAiAgentSdk `agent-mlkit` backend (encapsulates ML Kit GenAI Agent and Prompt APIs)
- Android AppFunctions
- Kotlin Serialization
- Gradle Version Catalog

## Architecture

- **Presentation/UI:** Compose screens and ViewModels manage UI state and interactions.
- **Domain:** business entities, repository contracts, and use cases represent application rules.
- **Data:** repository implementations, data sources, DTOs, and mappers handle data access.
- **AI:** backend adapters orchestrate model execution and expose BOPIS tools. Domain use cases remain the source of truth for fulfillment validation and mutations.

The current AI package is application-specific. It is not yet a standalone, general-purpose Android agent SDK. BOPIS instructions, tool implementations, response formats, and approval logic belong to this application.

## Requirements

- Android Studio with a compatible Android SDK and Gradle/JDK setup.
- Android device or emulator meeting the configured minimum SDK (API 26).
- A Firebase project configured for the application.
- Firebase configuration values used by the Gradle build.
- For LiteRT-LM: a compatible physical device, sufficient memory/storage, and an installed supported model artifact.
- For Gemini Nano: a supported device and available ML Kit GenAI feature managed by AICore.

The project currently configures compileSdk and targetSdk as 37. Use the SDK versions declared by the project rather than assuming an older Android SDK is sufficient.

## Getting started

1. Clone the repository:
   ```bash
   git clone https://github.com/sundeepkmallick/SunCommerzAssociate.git
   cd SunCommerzAssociate
   ```
2. Open the project in Android Studio and allow Gradle sync, or use the Gradle wrapper.
3. Configure Firebase:
   - Create or select a Firebase project and register the Android application with package ID `com.suncommerz.associate`.
   - Follow Firebase's Android setup guidance for the Google Services Gradle plugin and add `google-services.json` as required by your setup.
   - Provide `FIREBASE_API_KEY`, `FIREBASE_APP_ID`, and `FIREBASE_PROJECT_ID`. The build reads Gradle properties first, then `local.properties`.
   - Keep local credentials/configuration out of commits. Never put private service-account keys or server credentials in the Android app.
4. Build and run:
   ```bash
   ./gradlew :app:assembleDebug
   ```
   On Windows, use `gradlew.bat :app:assembleDebug`.

> Firebase setup and access to the configured project are necessary for the cloud AI path. Values embedded in Android applications should be treated as public client configuration and protected with appropriate Firebase restrictions and App Check—not as secrets.

## Media and diagrams

- [Screenshots](docs/screenshots)
- [Screen recordings](docs/screenrecordings)
- [Application flow diagram](docs/SunCommerz_flow_diagram_improvised_final.png)
- [Screen flow](docs/SunCommerz_screen_flow.png)

## Licensing and attribution

Original project code authored by Sundeep K. Mallick is licensed under the [Apache License, Version 2.0](LICENSE).

See [NOTICE](NOTICE) for project attribution and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the current inventory of third-party SDKs and libraries. Third-party components, Google/Firebase services, and AI models retain their own licenses and terms; this project's license does not relicense them.
