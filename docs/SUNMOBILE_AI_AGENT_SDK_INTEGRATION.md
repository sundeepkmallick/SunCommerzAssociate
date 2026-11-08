# SunMobileAiAgentSdk integration

## What changed

The app includes the SDK as a Git submodule at `SunMobileAiAgentSdk`, pinned to the SDK branch commit recorded by the submodule gitlink. Gradle includes that checkout as a composite build; no Maven publication or credentials are required.

The Firebase and LiteRT-LM pickup paths now compose host-owned read-only BOPIS tools through:
- `:agent-core` — tool registry, schema validation, capability contracts and policy executor;
- `:agent-adk` — Google ADK runner/session/event integration;
- `:agent-firebase` — Firebase AI Logic model factory;
- `:agent-litertlm` — LiteRT-LM model factory.

Gemini Nano remains on the existing host ML Kit Agent API + AppFunctions implementation. AppFunctions are declared by the host app and must enforce their own authorization; the SDK's dynamic tool policy does not intercept framework-dispatched AppFunctions.

## Local checkout

Clone with submodules:

```bash
git clone --recurse-submodules https://github.com/sundeepkmallick/SunCommerzAssociate.git
cd SunCommerzAssociate
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

If already cloned:

```bash
git submodule update --init --recursive
```

The app's existing Firebase configuration remains required for real execution. CI creates a non-production placeholder file only to compile; it cannot run Firebase requests.

## Request scoping and safety

For each item, the ViewModel passes the current order product, current store, requested quantity and maximum nearby radius into the SDK tool composition. The tool registry is created per item. Tool implementations:
- only query the current product;
- only query the current store, or nearby stores returned by the host's availability-filtered nearby-store use case;
- do not accept a model-selected associate, order, tenant or arbitrary store scope;
- validate requested quantity and radius against host values;
- are read-only; model output is still a proposal, not an order mutation.

The existing ViewModel re-reads live inventory and fulfillment options before showing/approving proposals. Associate approval and the authoritative order update use cases remain unchanged. The host still owns fallback selection and Gemini Nano AppFunctions.

## LiteRT-LM lifecycle

The app's download manager remains responsible for artifact provenance, file size, SHA-256, installation and the BOPIS-validated catalogue flag. The SDK factory accepts only an already-installed file. SDK agent creation/execution/close is serialized by the host's LiteRT-LM agent mutex, so a native engine is closed before a later request can load the model again. This is intentionally conservative; model caching/performance tuning should follow device tests.

## CI and validation limits

The workflow discovers the SDK Manager under `ANDROID_HOME/cmdline-tools` instead of relying on a globally exported `sdkmanager` command. It installs the API 37.0 preview package (`platforms;android-37.0`), which is the SDK Manager package name for this API level.

The Android workflow checks out the SDK submodule recursively and compiles/tests the app with a placeholder Firebase configuration. A green CI run proves compile/unit-test success only. It does not validate Firebase credentials, device AICore/Gemini Nano availability, local-model performance, or AppFunctions behavior.

Before enabling for associates, test Firebase and LiteRT-LM on the app's supported devices, test Gemini Nano on eligible API 36+ devices, and run the regression cases in the SDK migration guide. Do not remove the legacy pickup classes until those checks and rollout telemetry are satisfactory.
