# GroTrack — Grocery & Waste Tracker

GroTrack is an offline-first Android mini-project for households to keep grocery stock organized, notice expiry dates, plan purchases, and record food consumption and waste. The app keeps each purchase as its own batch so separate expiry dates stay visible.

## Features

- Home dashboard with product, near-expiry, expired-stock, low-stock, and recent-waste summaries.
- Inventory of products and purchase batches, with category chips, search, and expiry filters.
- Separate batches for repeat purchases, including purchase and expiry dates and optional price.
- Shopping list with manual items and suggestions based on each product's minimum stock.
- Purchase recording that creates an inventory batch only after the purchase is confirmed.
- Consume and waste actions that validate quantities and update stock together with a history record.
- Waste history filtered by product and inclusive date range.
- Insights for selected periods: waste-record count, quantity grouped by unit, and frequently wasted products.
- Daily WorkManager expiry checks, Android notifications, Android 13+ runtime permission handling, and an immediate test action.
- Local Room database; core features work without a network connection.

## Technology

- Java 11 application code and Android XML layouts.
- Android Gradle Plugin 9.2.1, compile SDK 36.1, target SDK 36, minimum SDK 28.
- Room 2.7.2 with the Java `annotationProcessor`.
- RecyclerView 1.4.0, Material Components 1.14.0, and WorkManager 2.12.0.
- AndroidX SplashScreen compatibility library for the native system splash screen on supported Android versions.

## Database structure

| Table | Purpose |
| --- | --- |
| `products` | Product name, category, unit, minimum stock, and archived state. |
| `grocery_batches` | One row per purchase, with purchased/remaining quantity, dates, and optional price. |
| `shopping_list_items` | Planned items, quantity/unit, optional product link, and purchased state. |
| `stock_history` | Purchase, consumption, and waste events with product/unit snapshots, batch link, quantity, reason, and timestamp. |

Products with batches are archived when removed from the active inventory, preserving purchase and transaction history. Stock adjustments and their history rows are written in SQLite transactions. Room version 2 includes a migration from version 1 to preserve existing Phase 1 data.

## Open and run in Android Studio

1. Open this project folder in Android Studio.
2. Let Gradle sync the existing Kotlin-DSL project and resolve the AndroidX dependencies.
3. Choose an API 28 or newer emulator/device and run the `app` configuration.
4. The first launch opens the dashboard. Use **My Inventory** to add products and batches. All saved data remains local on the device.

If Gradle cannot start its local daemon in a restricted environment, try Sync Project with Gradle Files and Build > Make Project inside Android Studio. The command line build is `gradlew.bat :app:assembleDebug`; the local test command is `gradlew.bat :app:testDebugUnitTest`.

## Manual testing walkthrough

### Inventory and batches

1. Add a product with a minimum stock of `2`, a quantity of `1`, and an expiry date today.
2. Add another batch for that product with a later date. Confirm both batches remain separate.
3. Search for the product and try category/status filters. Test a past date, today, three days away, and four days away.
4. Edit the product and confirm its unit cannot be changed after stock exists. Try zero/negative purchase quantities and negative prices.

### Shopping, consumption, and waste

1. From the dashboard, open Shopping List and add a manual item. Confirm inventory does not increase before purchase is recorded.
2. Use **Suggest low stock** and verify suggestions use the stock shortfall and do not duplicate an active suggestion.
3. Record a purchase with a quantity and expiry date. Verify a new batch appears in inventory.
4. Consume part of a batch and record waste with a reason. Try zero, negative, and over-stock quantities; they should be rejected.
5. Open Waste History, filter by product and date, and confirm the end date is included.
6. Restart the app and verify inventory and history remain saved.

### Dashboard, insights, and notifications

1. Verify dashboard product/expiry/low-stock counts against the batches above.
2. Record waste in two different units and confirm dashboard and Insights keep quantities grouped by unit.
3. Select 7, 30, and 90 day periods in Insights. Compare record counts and product ranking with Waste History.
4. On an API 33+ emulator, tap **Enable expiry alerts** and allow notifications. Tap **Test alert** and inspect the notification shade.
5. Add a batch expiring today, within three days, or already expired with remaining stock. Test again; the notification should report the current saved data. A batch four days away or with zero remaining stock should not be alerted.
6. Deny notification permission and confirm the inventory and insights screens continue to work. The app skips notification posting when permission is unavailable.

WorkManager periodic work is system-scheduled and may not run at an exact wall-clock time. Use **Test alert** to run the same Room-backed expiry check immediately during a demonstration.

## Viva explanation

1. A **Product** describes a grocery type (for example, milk, measured in litres) and its minimum desired stock.
2. Every shopping purchase creates a **GroceryBatch**. This is why two milk purchases with different expiry dates do not overwrite one another.
3. The inventory screen reads the product/batch relationship from Room. Expiry labels are derived from the saved date and today's local date instead of being stored as stale text.
4. When food is consumed or wasted, the app subtracts the amount from the selected batch and inserts a **StockHistory** row in one database transaction. If either part fails, SQLite rolls back the update.
5. A shopping-list suggestion only creates a planned **ShoppingListItem**. Stock changes after the user records an actual purchase, which then creates a batch.
6. Dashboard and Insights values are calculated from Room rows. Quantities are grouped by unit so kilograms and pieces are never added together.
7. WorkManager periodically runs an expiry worker even when the screen is closed. Notifications are optional; the data and other features remain available if permission is denied.

## Build/test status

Run the commands above in an environment where Gradle can open its local loopback socket and access the configured dependency cache. A previous command-line attempt in this environment stopped before compilation with `Unable to establish loopback connection`; that failure does not establish whether source compilation succeeds.
