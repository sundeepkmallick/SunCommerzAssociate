# SunMobileAiAgentSdk integration

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

The app consumes Core, ADK, Firebase, LiteRT-LM and agent-mlkit. Gemini Nano inference, ML Kit Agent/Prompt clients, readiness checks and AgentRunner lifecycle are encapsulated by agent-mlkit. The host retains Android AppFunctions declarations, service implementation, manifest registration, KSP/compiler processing and business authorization.

## Select the SDK source

The app keeps the same Maven coordinates in its version catalog. Select the source for the entire SDK group with `-PsunAiSdkSource`:

| Mode | Gradle command | Behavior |
|---|---|---|
| Composite build | `-PsunAiSdkSource=includeBuild` | Uses all matching SDK projects from a checkout; no publication needed |
| Local Maven | `-PsunAiSdkSource=mavenLocal` | Resolves all SDK artifacts from `~/.m2/repository` |
| Maven Central | `-PsunAiSdkSource=mavenCentral` | Uses the released version from Maven Central (default) |

### Composite build (recommended during SDK development)

Keep the repositories as sibling directories:

```text
development/
  SunCommerzAssociate/
  SunMobileAiAgentSdk/
```

Check out the SDK development branch in the sibling directory, then run from the app:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest \\
  -PsunAiSdkSource=includeBuild \\
  -PsunAiSdkDir=../SunMobileAiAgentSdk
```

Gradle's composite-build substitution matches each SDK project's group and project name to external module coordinates. Since all SDK projects share the same group, this covers every module in the SDK build without a hand-maintained substitution list.

### Local Maven publication

From the SDK checkout, publish all SDK modules:

```bash
./gradlew publishSdkToMavenLocal
```

Then from the app checkout:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest \\
  -PsunAiSdkSource=mavenLocal \\
  -PsunAiSdkVersion=0.1.0-rc.2
```

Local mode restricts Maven Local to the SDK group and resolves every SDK module at the local version. The default local version matches the current SDK development branch; override it with `sunAiSdkVersion` if you publish another version.

### Maven Central

When the SDK release is published, use the default source or specify it explicitly:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest \\
  -PsunAiSdkSource=mavenCentral
```

The app's dependency declarations do not need to change when switching sources.

## Pickup integration and ownership

Firebase AI Logic and LiteRT-LM use the SDK ADK runner/provider, ToolRegistry, JSON-schema validation and ToolExecutor policy. BOPIS-specific tools remain in the host app. The host owns model download/catalogue/checksum/device admission; the SDK LiteRT-LM model factory receives only an already installed and verified file.

GeminiNanoPickupAgent creates a core AgentBuilder with GeminiNanoAgentBackendProvider and GeminiNanoConfiguration. It checks SDK readiness before execution and verifies that getProductStockInStore was invoked. The SDK deliberately does not accept dynamic ToolRegistry tools for this backend: Android AppFunctions dispatch outside the SDK AgentPolicy, so each host AppFunction must independently validate caller identity, arguments, authorization and current business state. The app keeps androidx.appfunctions:appfunctions and its compiler; it no longer declares ML Kit GenAI Agent or Prompt dependencies directly.

A successful compile/unit-test run does not validate Firebase credentials, device AICore/Gemini Nano availability, local-model performance or AppFunctions behavior. Test each backend on supported devices before enabling for associates.
