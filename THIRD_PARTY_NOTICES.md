# Third-Party Notices

This document identifies the direct libraries and SDKs declared in the project's
Gradle version catalog and app module. It is a maintenance inventory, not a
complete software bill of materials (SBOM) or a legal determination of the license
for every resolved artifact.

**Important:** dependency versions and transitive dependencies can change. Before
publishing or distributing an APK/AAB, verify the license and notice files for the
exact resolved artifacts (including transitive dependencies) and include any
required notices. The project license does not relicense these components.

## Application and runtime dependencies

| Component | Project / publisher | Purpose in this app | License information |
|---|---|---|---|
| AndroidX Core KTX, Activity Compose, Lifecycle, Navigation and ConstraintLayout Compose | [AndroidX](https://developer.android.com/jetpack/androidx) | Android platform, lifecycle, navigation and layout APIs | Apache-2.0; verify the exact artifact metadata |
| Jetpack Compose UI, Material 3, Material Icons, tooling and test artifacts | [AndroidX / Jetpack Compose](https://developer.android.com/jetpack/compose) | UI toolkit, components, icons, previews and UI tests | Apache-2.0; verify each resolved artifact |
| AndroidX AppFunctions and AppFunctions compiler | [AndroidX](https://developer.android.com/jetpack/androidx) | Exposes app functions to the ML Kit GenAI Agent integration | Verify the exact artifact metadata and preview/alpha terms |
| Hilt and AndroidX Hilt | [Dagger / Google](https://dagger.dev/hilt/) and [AndroidX](https://developer.android.com/jetpack/androidx) | Dependency injection and lifecycle integration | Apache-2.0; verify each resolved artifact |
| Google Agent Development Kit (ADK) for Kotlin (core, Firebase, LiteRT-LM and processor artifacts) | [Google ADK](https://google.github.io/adk-docs/) | Agent orchestration, Firebase and LiteRT-LM model integration, and code generation | Check the license distributed with the exact ADK artifact/version |
| LiteRT-LM Android runtime | [Google AI Edge](https://ai.google.dev/edge/litert) | Local model inference on Android | Verify the license and notices for the exact runtime artifact/version |
| ML Kit GenAI Agent and Prompt APIs | [Google ML Kit](https://developers.google.com/ml-kit) | Gemini Nano agent execution and prompt/content APIs | Verify the license and preview/alpha terms for the exact artifacts |
| Firebase AI Logic, Firebase BOM and Firebase App Check Debug | [Firebase](https://firebase.google.com/) | Firebase AI integration, dependency alignment and App Check | Firebase Android SDK components are published under Apache-2.0; review each artifact's license and applicable Firebase service terms |
| Kotlinx Serialization JSON | [Kotlin](https://github.com/Kotlin/kotlinx.serialization) | JSON serialization | Apache-2.0; verify the exact artifact metadata |

## Model and service terms

The local model weights are separate from the LiteRT-LM runtime and from this repository's source-code license.

- **Gemma 4 E2B IT:** the LiteRT-LM conversion repository may identify the conversion package as Apache-2.0, but the underlying Gemma weights remain subject to Google's Gemma terms and prohibited-use policy. Review the exact model card and terms before use or redistribution.
- **Qwen2.5 1.5B Instruct:** review the exact upstream model card and license for the converted LiteRT-LM artifact before use or redistribution.
- **Gemini Nano and Firebase-hosted models/services:** availability, model access, acceptable use, and redistribution are governed by the applicable Google, Firebase, and model/service terms.

The repository license does not grant rights to model weights, hosted AI services, or third-party APIs.

## Build and test tooling

The project also declares Android Gradle Plugin, Kotlin compiler/Compose/serialization plugins,
KSP, Google Services Gradle plugin, Gradle, JUnit 4, AndroidX Test and Espresso.
These are build-time or test dependencies rather than necessarily shipped app code.
Review their respective license files and notices when redistributing build tooling,
test artifacts, or a complete development environment.

- [Android Gradle Plugin](https://developer.android.com/build)
- [Kotlin](https://kotlinlang.org/)
- [KSP](https://github.com/google/ksp)
- [Google Services Gradle plugin](https://developers.google.com/android/guides/google-services-plugin)
- [JUnit 4](https://junit.org/junit4/)
- [AndroidX Test](https://developer.android.com/training/testing)

## Attribution and license texts

Third-party names and trademarks are used only to identify the relevant projects.
Copyright and license notices supplied with each dependency must be preserved as
required by that dependency's license. Where a dependency ships a NOTICE or
additional attribution file, retain the applicable contents when redistributing it.

This inventory is based on the dependencies declared in `gradle/libs.versions.toml`
and `app/build.gradle.kts` on the date this file was prepared. It does not claim
that every listed dependency is bundled into every application build.
