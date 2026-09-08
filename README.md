# SunCommerzAssociate

An Android application designed for store associates to manage the Buy Online, Pick Up In Store (BOPIS) fulfillment process efficiently.

## Overview

The app streamlines the picking process for customer orders, handling complex scenarios such as product substitutions and inventory issues, ensuring a seamless experience from order placement to customer pickup.

## Key Features

- **Order Management**: Browse and manage customer orders slated for pickup.
- **Smart Picking**: Guided picking process for order items with support for substitutions.
- **Inventory Reporting**: Quickly notify managers about depleted or low-stock items on shelves.
- **Substitution Handling**: Manage product substitutions when the original item is unavailable, including specifying substitution reasons.
- **Associate Notifications**: Real-time alerts for critical tasks and updates.
- **Dashboard**: A centralized view of pending tasks and priorities for store associates.

## Technical Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System**: [Material 3](https://m3.material.io/)
- **Dependency Injection**: [Hilt](https://developer.android.com/hilt)
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- **Architecture**: Clean Architecture (Data -> Domain -> UI)

## Architecture

The project follows **Clean Architecture** principles to ensure scalability, testability, and maintainability:

- **UI Layer**: Feature-based modules (Dashboard, Orders, Order Items) using state-hoisting and ViewModels for state management.
- **Domain Layer**: Contains business logic, use cases, and pure domain models (e.g., `Order`, `Product`, `OrderItem`). This layer is independent of any framework.
- **Data Layer**: Implements repositories and handles data retrieval from local/remote sources, including DTOs and mappers to convert data to domain models.

## Media

- [Screenshots](docs/screenshots)
- [Screen Recordings](docs/screenrecordings)

## Flow & Design

### Flow Diagram
<img src="docs/SunCommerz_flow_diagram_improvised_final.png" alt="Application Flow Diagram" width="300"/>

### Screen Flow
<img src="docs/SunCommerz_screen_flow.png" alt="Application Screen Flow" width="500"/>

## Requirements & Use Case

Retail stores support a variety of sales fulfilment models, one of the most common scenarios being Buy Online, Pick Up In Store (BOPIS). In this process, customers place orders online, which creates sales orders for the store. Store associates pick the ordered items and prepare them for collection.

**Key Challenges Addressed:**
- **Product Unavailability**: Support for similar product substitutions.
- **Cross-Store Availability**: Handling items available at nearby locations.
- **System Synchronization**: Real-time updates to notify customers when orders are ready.
- **Inventory Maintenance**: Associate-driven reporting of empty shelves.