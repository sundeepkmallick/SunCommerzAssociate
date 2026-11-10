# SunMobileAiAgentSdk integration

## SDK dependency

The app consumes the released SDK modules from Maven Central. The version is centralized in `gradle/libs.versions.toml` as `sunMobileAiAgentSdk = "0.1.0-rc.1"`.

| Module | Maven coordinate |
|---|---|
| Core | `io.github.sundeepkmallick:agent-core:0.1.0-rc.1` |
| ADK | `io.github.sundeepkmallick:agent-adk:0.1.0-rc.1` |
| Firebase | `io.github.sundeepkmallick:agent-firebase:0.1.0-rc.1` |
| LiteRT-LM | `io.github.sundeepkmallick:agent-litertlm:0.1.0-rc.1` |

Maven Central is already configured in the root dependency repositories. No SDK submodule checkout, composite build, repository credentials or local publication step is required. The published Gradle module metadata/POMs provide the SDK modules' transitive dependencies.

## Pickup integration

Firebase AI Logic and LiteRT-LM use the SDK ADK runner/provider, ToolRegistry, JSON-schema validation and ToolExecutor policy. BOPIS-specific tools remain in the host app.

The SDK tool composition scopes product/store/quantity/radius from the active order workflow, permits only read-only tools, and limits stock checks to the current store or nearby stores discovered by the host's stock-filtering use case. Existing host validation and associate approval remain the source of truth; no model output directly mutates an order.

Gemini Nano continues to use the existing host ML Kit Agent API + AppFunctions path. This is intentional: AppFunctions are declared by the host and their security checks are not intercepted by the SDK's dynamic ToolRegistry.

## LiteRT-LM lifecycle

The host owns model download/catalogue/checksum/device admission. The SDK model factory receives only an already installed and verified file. The host serializes execution and closes each SDK agent/model after the turn.

## Local build

Clone the application normally; no recursive submodule option is needed:

```bash
git clone https://github.com/sundeepkmallick/SunCommerzAssociate.git
cd SunCommerzAssociate
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

The app's Firebase configuration is required for real execution. CI creates a non-production placeholder file only to compile; it cannot run Firebase requests.

## CI and validation

Android CI checks out only the application repository, installs API 37.0, and compiles/tests against the fixed Maven Central SDK release. A green CI run proves compile/unit-test success only. It does not validate Firebase credentials, device AICore/Gemini Nano availability, local-model performance or AppFunctions behavior.

Before enabling for associates, test Firebase and LiteRT-LM on supported devices, test Gemini Nano on eligible API 36+ devices, and run the regression cases in the SDK migration guide. Keep legacy pickup classes until those checks and rollout telemetry are satisfactory.
