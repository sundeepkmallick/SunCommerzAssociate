# SunMobileAiAgentSdk integration

This guide explains how the app selects the source for the six SunMobile AI Agent SDK artifacts. The published version is `0.1.0-rc.2`.

## SDK modules and coordinates

All SDK artifacts use group `io.github.sundeepkmallick` and share one version.

| Module | Maven artifact |
|---|---|
| Core | `agent-core` |
| ADK | `agent-adk` |
| Firebase | `agent-firebase` |
| LiteRT-LM | `agent-litertlm` |
| ML Kit GenAI | `agent-mlkit` |
| Gemini Nano compatibility adapter | `agent-gemini-nano` |

The app currently declares Core, ADK, Firebase, LiteRT-LM and `agent-mlkit`. All SDK dependencies use the version in `gradle/libs.versions.toml` (`sunMobileAiAgentSdk`).

## Select the SDK source

The app's `settings.gradle.kts` reads `sunAiSdkSource`. There are three supported values:

| Source | Gradle property | What Gradle resolves |
|---|---|---|
| Maven Central | `mavenCentral` | Published artifacts from Maven Central. This is the default when the property is omitted. |
| Maven Local | `mavenLocal` | Artifacts previously published to the current user's local Maven repository (`~/.m2/repository`). |
| Composite build | `includeBuild` | SDK Gradle projects from a local checkout; no Maven publication is required. |

The app's Maven coordinates and dependency declarations stay the same in all three modes. Only the source changes.

### 1. Maven Central (normal use; default)

The six `0.1.0-rc.2` components have been published. From the app repository:

```bash
./gradlew --refresh-dependencies :app:assembleDebug :app:testDebugUnitTest
```

You can make the source explicit:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest -PsunAiSdkSource=mavenCentral
```

When `sunAiSdkSource` is omitted, the build uses this mode. `--refresh-dependencies` is useful after a new SDK version is published or when checking that artifacts resolve remotely rather than from a previous Gradle cache.

### 2. Maven Local (test a locally published SDK build)

First, from the **SDK repository** (not the app repository), build and publish the SDK artifacts to your local Maven repository:

```bash
cd ../SunMobileAiAgentSdk
./gradlew publishSdkToMavenLocal
```

Then, from the **app repository**, select Maven Local:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest \
  -PsunAiSdkSource=mavenLocal \
  -PsunAiSdkVersion=0.1.0-rc.2
```

`sunAiSdkVersion` is optional and defaults to `0.1.0-rc.2`. Set it to the exact version you published locally if it differs. In this mode the build adds `mavenLocal()` restricted to the SDK's group and applies the selected version to all SDK dependencies. Maven Central remains available as the repository fallback, so make sure the intended version is actually present in Maven Local when testing unpublished changes.

### 3. Composite build (active SDK development)

Use this when changing SDK source and app together. Keep the repositories as sibling directories, or provide an absolute path to the SDK checkout:

```text
development/
  SunCommerzAssociate/
  SunMobileAiAgentSdk/
```

From the **app repository**:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest \
  -PsunAiSdkSource=includeBuild \
  -PsunAiSdkDir=../SunMobileAiAgentSdk
```

`sunAiSdkDir` is optional; it defaults to `../SunMobileAiAgentSdk`. Composite substitution maps the SDK's module projects to their Maven coordinates, so the app uses the checked-out source instead of downloaded SDK binaries. The selected SDK checkout and branch determine the code being compiled.

## Make a source selection persistent (optional)

For repeated local builds, put the properties in the app repository's **untracked/local** `gradle.properties` file (or in your user-level Gradle properties file). Example for a sibling SDK checkout:

```properties
sunAiSdkSource=includeBuild
sunAiSdkDir=../SunMobileAiAgentSdk
```

For Maven Local instead:

```properties
sunAiSdkSource=mavenLocal
sunAiSdkVersion=0.1.0-rc.2
```

For Maven Central, either remove the source properties or set:

```properties
sunAiSdkSource=mavenCentral
```

Do not commit machine-specific paths or local source preferences. To override a persistent setting for one invocation, pass the corresponding `-Pname=value` argument on the command line.

### Android Studio

Open **Settings/Preferences → Build, Execution, Deployment → Build Tools → Gradle** and add the same arguments to **Gradle options** (for example, `-PsunAiSdkSource=includeBuild -PsunAiSdkDir=../SunMobileAiAgentSdk`). Alternatively, use the local `gradle.properties` method above. Ensure Android Studio is using the intended Gradle JDK and reload/sync the project after changing the source.

## Useful commands

Run from the app repository:

```bash
# Maven Central (default)
./gradlew --refresh-dependencies :app:assembleDebug

# Maven Local
./gradlew :app:assembleDebug -PsunAiSdkSource=mavenLocal -PsunAiSdkVersion=0.1.0-rc.2

# Local SDK checkout
./gradlew :app:assembleDebug -PsunAiSdkSource=includeBuild -PsunAiSdkDir=../SunMobileAiAgentSdk

# Unit tests
./gradlew :app:testDebugUnitTest
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Pickup integration and ownership

Firebase AI Logic and LiteRT-LM use the SDK ADK runner/provider, ToolRegistry, JSON-schema validation and ToolExecutor policy. BOPIS-specific tools remain in the host app. The host owns model download/catalogue/checksum/device admission; the SDK LiteRT-LM model factory receives only an already installed and verified file.

GeminiNanoPickupAgent creates a core AgentBuilder with GeminiNanoAgentBackendProvider and GeminiNanoConfiguration. It checks SDK readiness before execution and verifies that getProductStockInStore was invoked. The SDK deliberately does not accept dynamic ToolRegistry tools for this backend: Android AppFunctions dispatch outside the SDK AgentPolicy, so each host AppFunction must independently validate caller identity, arguments, authorization and current business state. The app keeps androidx.appfunctions:appfunctions and its compiler; it does not declare ML Kit GenAI Agent or Prompt dependencies directly.

A successful compile/unit-test run does not validate Firebase credentials, device AICore/Gemini Nano availability, local-model performance or AppFunctions behavior. Test each backend on supported devices before enabling for associates.
