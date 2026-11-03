# SunCommerz Associate

An Android application for store associates managing **Buy Online, Pick Up In Store (BOPIS)** order fulfillment. The app guides associates through picking customer orders, resolving unavailable items, handling substitutions, and coordinating inventory issues.

> **Project status:** Setup requires Firebase configuration. AI features depend on the configured model provider and device/runtime support.

## Features

- **Pickup order workflow** — review orders and work through their items.
- **Guided item picking** — record picked items and handle exceptions.
- **Substitutions** — discover suitable substitute products and manage substitution decisions.
- **Nearby-store fulfillment** — look for inventory at nearby stores when the current store cannot fulfill an item.
- **Inventory issue reporting** — surface out-of-stock issues to store management.
- **Associate dashboard and notifications** — view work and receive relevant updates.
- **AI-assisted pickup proposals** — an AI agent can reason over order items and read inventory/store data through tools. Associates review and approve proposed order changes; the AI should not silently commit the pickup.

## AI pickup agent

The AI pickup workflow uses Google's Agent Development Kit (ADK) for Kotlin and Firebase AI Logic. The agent can call application tools to look up product stock, store inventory, nearby stores, available substitute candidates, store products, and prior substitution history.

Tool results are grounded in the app's repositories and domain use cases. Treat model output as a proposal: verify it and use the in-app associate approval flow before applying order changes.

### Model/provider notes

The current main branch uses the Firebase-backed model provider. An on-device Gemini Nano path is being explored separately. Local model availability, device compatibility, tool support, and runtime behavior are platform-dependent. Do not assume every Android device can run Gemini Nano or that a device's consumer AI features expose an app-accessible model API.

AI behavior may evolve as provider and agent implementations change. Review the AI implementation in the app source under the AI package.

## Technology

- Kotlin
- Jetpack Compose and Material 3
- AndroidX Navigation
- Hilt dependency injection
- Clean Architecture-inspired presentation, domain, and data layers
- Google ADK for Kotlin
- Firebase AI Logic
- Kotlin Serialization
- Gradle Version Catalog

## Architecture

- **Presentation/UI:** Compose screens and ViewModels manage UI state and interactions.
- **Domain:** business entities, repository contracts, and use cases represent application rules.
- **Data:** repository implementations, data sources, DTOs, and mappers handle data access.
- **AI:** agent orchestration and model providers connect model output to domain tools. Keep tool operations constrained and use domain use cases for business mutations.

This separation helps keep business rules independent of UI and model-provider details.

## Requirements

- Android Studio with a compatible Android SDK and Gradle/JDK setup.
- Android device or emulator meeting the configured minimum SDK (API 26).
- A Firebase project configured for the application.
- Firebase configuration values used by the Gradle build.

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

## Use case

Retail customers place orders online for pickup at a store. Associates prepare orders by picking requested items. If an item is unavailable, the workflow can consider a substitute or another store, while inventory issues can be reported for follow-up. The app supports store operations while keeping associates in control of fulfillment decisions.

## Licensing and attribution

Original project code authored by Sundeep K. Mallick is licensed under the [Apache License, Version 2.0](LICENSE).

See [NOTICE](NOTICE) for project attribution and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the current inventory of third-party SDKs and libraries. Third-party components, Google/Firebase services, and AI models retain their own licenses and terms; this project's license does not relicense them.
