# AI Pickup architecture and backend behaviour

This document describes the AI-assisted BOPIS implementation in the current `main` branch. It documents this application, not a separately published or reusable agent SDK.

## Overview

AI Pickup generates fulfillment proposals for order items. The model can use application tools to inspect inventory, substitutions, nearby stores, and relevant product/store data. The associate remains responsible for approving the proposal.

The implementation has three selectable backends:

| Backend | Integration | Model provisioning |
|---|---|---|
| Firebase | Google Agent Development Kit (ADK) for Kotlin with Firebase AI Logic | Cloud model provider |
| LiteRT-LM | Google ADK for Kotlin with the LiteRT-LM model adapter | App downloads and manages the selected local `.litertlm` artifact |
| Gemini Nano | ML Kit GenAI Agent API with Android AppFunctions | Android AICore manages model availability; the app does not download model weights |

Firebase and LiteRT-LM use the shared `PickupAdkAgentFactory` and BOPIS tool set. Gemini Nano uses a separate ML Kit Agent API integration and exposes app functions through `enableAppFunctions(true)`. The backends share the pickup instructions and are expected to perform equivalent BOPIS tasks, but their underlying APIs and lifecycle/session behaviour are not identical.

## Request flow

1. `AIPickUpViewModel` loads the order and processes its items one at a time.
2. For each item, it builds a prompt containing the order item, requested quantity, current store, nearby-store radius, and low-stock threshold.
3. `HybridPickupAgentManager` reads the associate's preferred backend.
4. The selected backend runs the agent. If a preferred on-device backend fails or returns an unsupported pickup action, the manager retries that invocation with Firebase ADK. Firebase failures are surfaced.
5. The model response is parsed as one of the supported pickup actions.
6. The ViewModel independently checks current inventory and calls the shared fulfillment-options use case. It does not trust the model to establish that a proposed action is valid.
7. The app displays proposals for associate review.
8. On approval, the ViewModel reads current order and inventory data again and revalidates each action before invoking the existing domain use cases.

The model is a decision-support component. It does not directly mutate order or inventory state.

## Agent tools

The ADK factory registers application tools in three groups:

### Inventory tools
- `getProductStockInStore(productId, storeId)` — reads current inventory for one product at one store.
- `getStoreInventory(storeId)` — reads the store's inventory.

### Store tools
- `findNearbyStores(productId, currentStoreId, requestedQuantity, radiusMeters)` — finds nearby stores that can fulfill the requested quantity.

### Substitution and product tools
- `getAvailableSubstitutes(productId, storeId, requestedQuantity)` — obtains substitute candidates and filters them through the application's availability use case.
- `observeStoreProductsUseCase(storeId)` — reads products at a store.
- `pastOrderWithSubstitutionHistory(productId)` — reads previous substitution history.

These tools are backed by the app's repositories and domain use cases. They are BOPIS application capabilities, not generic SDK tools.

## Pickup decision policy

The shared system instructions tell the agent to:

1. Check requested-product stock at the current store.
2. Return `PICKED` when current-store stock is sufficient.
3. Otherwise look for eligible in-store substitutes and return `SUBSTITUTE(productId)` when appropriate.
4. If no substitute is suitable, search nearby stores, verify stock there, and select the nearest store with sufficient quantity using `RESERVE_NEARBY_STORE(storeId)`.
5. Return `UNAVAILABLE` when no supported fulfillment option exists.

The accepted response forms are:

- `PICKED`
- `SUBSTITUTE(productId)`
- `RESERVE_NEARBY_STORE(storeId)`
- `UNAVAILABLE`

The manager rejects responses outside these forms. The ViewModel then reconciles the model's suggestion with live application data and the same fulfillment options used by the manual flow. In particular, it prefers an available current-store substitute before a nearby-store option.

## Deterministic validation and approval

Model output is not authoritative. The ViewModel checks current-store stock and calls `FindFulfillmentOptionsUseCase` before exposing a proposal. At approval time it checks the order item and inventory/options again.

Approval rules include:

- `PICKED` is accepted only if current-store stock still covers the requested quantity.
- A substitute is accepted only if it remains in the current store's eligible substitute options.
- A nearby-store reservation is accepted only if that store remains in the eligible nearby-store options.
- `UNAVAILABLE` is accepted only if no current-store stock, substitute, or nearby-store option can fulfill the item.
- Stale proposals fail with an instruction to run AI Pickup again rather than recording an invalid fulfillment action.

The existing domain use cases perform the actual order updates. The ViewModel also mirrors the manual workflow's order-status transitions and reports applicable low/empty shelf inventory to store management.

## Backend selection and fallback

The initial preferred backend is Firebase ADK. The associate can change the preference in AI Model Preferences.

- If Firebase is preferred, Firebase is used directly.
- If Gemini Nano or LiteRT-LM is preferred, the manager attempts that backend first.
- If an on-device backend fails or its result fails pickup-action validation, the manager falls back to Firebase for that invocation.
- The stored preference is not silently changed by fallback.

The active backend reported by the manager describes the backend used for the latest successful invocation.

## Backend-specific lifecycle notes

### Firebase ADK

`FirebasePickupAgent` obtains its model through `FirebaseModelProvider`, builds an ADK runner using the shared pickup agent factory, and collects the final assistant response. A fresh in-memory session service is created for each request so a retry does not inherit conversation history from a previous item attempt.

### LiteRT-LM

`LiteRtLmPickupAgent` loads the selected installed artifact with the LiteRT-LM CPU backend and creates an ADK runner using the same pickup agent factory. The native model is cached while in use and released before its file is replaced or removed.

The adapter verifies that the agent invoked `getProductStockInStore` before accepting a response. Device admission checks are applied using catalogue metadata, along with a low-RAM-device rejection. The currently recorded Gemma guardrails are app-specific checks, not universal hardware guarantees.

### Gemini Nano

`GeminiNanoPickupAgent` checks Android API level and ML Kit GenAI feature status before use. It builds an ML Kit Agent with AppFunctions enabled and checks that `getProductStockInStore` was called. The model and AgentRunner are closed after the single-turn request.

Gemini Nano provisioning is controlled by AICore. This app does not download or manage Gemini Nano model weights. The current implementation uses a single-turn AgentRunner; do not assume it provides the same persistent session behaviour as the ADK paths.

## Relevant source files

- `ai/PickupAgent.kt` — common backend interface.
- `ai/PickupAgentInstructions.kt` — shared BOPIS instructions.
- `ai/PickupAdkAgentFactory.kt` — ADK agent and BOPIS tool registration.
- `ai/FirebasePickupAgent.kt` — Firebase ADK adapter.
- `ai/LiteRtLmPickupAgent.kt` — LiteRT-LM ADK adapter and runtime lifecycle.
- `ai/GeminiNanoPickupAgent.kt` — ML Kit Agent API adapter.
- `ai/HybridPickupAgentManager.kt` — preferred-backend selection, response validation, and Firebase fallback.
- `ai/LocalModelCatalog.kt` — local model metadata and app-specific validation flags.
- `ui/aipickup/AIPickUpViewModel.kt` — proposal reconciliation, approval-time validation, and order updates.

## Validation status

The project owner has reported exercising BOPIS tool-calling with both Gemma 4 E2B IT and Qwen2.5 1.5B Instruct using LiteRT-LM 0.13.1. The catalogue records both as validated for this app's BOPIS pickup-agent flow.

The project owner has also previously exercised the Firebase SDK path. Gemini Nano has not been device-tested on a supported physical device in the reported validation. Do not interpret source-level compatibility metadata as a substitute for testing on the target Android device.

No build or automated test result is asserted by this document. Check the current CI/build results and repeat device acceptance tests when changing dependencies, model artifacts, tool definitions, or approval logic.
