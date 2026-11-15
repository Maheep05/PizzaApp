# Pizza 5 P's

*Perfect Pizza. Perfect Moments.*

An Android ordering app for a single pizzeria, built with Kotlin and Jetpack Compose
(Material 3). Customers can sign in, build a pizza, check its nutrition, pay at checkout
and follow the delivery on a live map. It was built as a college project. The goal was a
polished, commercial-grade interface without needing a backend.

| | |
| :--- | :--- |
| **Language** | Kotlin (JVM 17) |
| **UI** | Jetpack Compose, Material 3, Navigation Compose |
| **State** | `ViewModel` + `StateFlow` + coroutines |
| **Map** | osmdroid (OpenStreetMap tiles) + OSRM routing |
| **AI (optional)** | Anthropic Java SDK. The user supplies their own key |
| **Android** | minSdk 26 (Android 8.0), target/compile SDK 34 |

---

## Contents

- [Features](#features)
- [Getting started](#getting-started)
- [Demo walkthrough](#demo-walkthrough)
- [Project structure](#project-structure)
- [How it works](#how-it-works)
- [Design system](#design-system)
- [Testing](#testing)
- [Release build](#release-build)
- [Limitations](#limitations)
- [Third-party licences](#third-party-licences)

---

## Features

**Ordering**
- **Sign in / sign up.** A ready-made demo account means no setup is needed (see
  [Getting started](#getting-started)).
- **Menu of nine pizzas** with search and Veg / Non-Veg / Popular / Cheesy filters.
- **Three-step customiser** for size, crust and extra toppings. The price updates live
  with an itemised breakdown. Extra toppings are free.
- **Cart and coupons.** `PIZZA20` gives 20% off and `PIZZA40` gives 40% off.
- **Checkout** with a saved-address picker and a choice of payment method.

**Nutrition**
- **Calculated, not guessed.** Calories, protein, carbs, fat, saturated fat, fibre,
  sugar and sodium are worked out from each pizza's recipe: the ingredient weights
  multiplied by standard published food-composition values. The figures update as you
  change the size, crust or toppings.
- **Nutrition assistant.** A chat that answers questions about the pizza on screen. It
  works offline out of the box. If you add your own Anthropic API key in Profile, it
  switches to Claude for more natural answers. Either way, the numbers always come from
  the calculator and never from the model.

**Delivery**
- **Live tracking map.** Real OpenStreetMap tiles, with the actual shortest road route
  from the outlet (Indiranagar, Bengaluru) to the drop-off, fetched from OSRM. The rider
  moves along the route as the order progresses.
- **Four-stage tracker:** Confirmed → Preparing → Out for Delivery → Delivered. It
  advances by itself every 5 seconds and ends with confetti. Prev / Next / Auto controls
  let you set the pace during a presentation.
- **Notifications** at every stage, with a progress bar, so you can follow the order
  with the app closed.

**Look and feel**
- **Every pizza is drawn in code, not photographed.** The pizza is drawn on a Canvas,
  with the crust, char, sauce, cheese and slices, and toppings are layered on top.
  Changing the size resizes the pizza, adding a topping drops it on, and removing one
  flips the pizza to throw it off. The app bundles no image assets.
- **Light and dark themes** (System / Light / Dark, switched in Profile).
- **Bundled fonts:** Playfair Display for headings and Inter for UI text, so nothing is
  downloaded at runtime.
- **Consistent motion:** spring animations from a shared token set, with haptic
  feedback on tap.

---

## Getting started

### Requirements

- **Android Studio** (any recent version; JDK 17 is bundled)
- **Android SDK 34**
- An emulator or a phone running **Android 8.0 (API 26) or later**

### Run in Android Studio

1. **File → Open…** and select this folder.
2. Wait for Gradle sync to finish. The first sync downloads the dependencies.
3. Choose an emulator or a connected device and press **Run ▶**.

### Run from the command line

```bash
# Build and install on the running emulator or connected device
./gradlew installDebug

# Launch it
adb shell monkey -p com.pizza5ps.app -c android.intent.category.LAUNCHER 1
```

If Gradle can't find the SDK, point it there first:

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"   # macOS default location
```

### Demo account

| Email | Password |
| :--- | :--- |
| `maheep.student@college.edu` | `pizza123` |

The sign-in screen has a **"Use maheep.student@college.edu"** button that fills these in.

### Network

The map tiles and road routing need an internet connection. Without one, the app still
works: the route falls back to a bundled approximation, labelled **"Offline estimate"**.

---

## Demo walkthrough

A suggested route through the app for a presentation:

1. **Splash.** The logo and tagline animate in.
2. **Sign in** with the demo account.
3. **Home.** The offer carousel rotates, filter chips sit below it, and pizza cards
   cascade in.
4. **Product page.** Open **Margherita** and switch to **Large** to watch the pizza
   grow. Add **Extra Mozzarella** and **Black Olives** and watch them drop on, then
   remove one. Scroll to the **Nutrition** card and see the figures change.
5. **Nutrition assistant.** Tap *Ask the nutrition assistant* and try *"How much
   protein is in a slice?"* or *"How can I make this lighter?"*
6. **Cart.** `PIZZA20` is already applied and the bill is itemised.
7. **Checkout.** Pick an address and a payment method, then **Place order**.
8. **Tracking.** The map shows the real route and the rider driving it, with
   notifications at each stage and confetti on delivery. Use **Prev / Next / Auto** to
   control the pace.
9. **Profile.** Switch between System, Light and Dark themes, or add an API key for the
   Claude-powered assistant.

---

## Project structure

```text
app/src/
├── main/
│   ├── AndroidManifest.xml          # INTERNET, ACCESS_NETWORK_STATE, POST_NOTIFICATIONS
│   ├── java/com/pizza5ps/app/
│   │   ├── MainActivity.kt          # Edge-to-edge setup, theme host, notification bridge
│   │   ├── navigation/
│   │   │   └── AppNavigation.kt     # Navigation graph
│   │   ├── model/                   # Pizza, CartItem, Order, CustomizationOptions
│   │   ├── data/
│   │   │   ├── PizzaDataSource.kt   # The nine-pizza menu
│   │   │   ├── AccountRepository.kt # In-memory demo accounts
│   │   │   ├── Nutrition.kt         # Ingredient table + NutritionCalculator
│   │   │   ├── NutritionAssistant.kt# Offline assistant + Claude assistant
│   │   │   ├── ApiKeyStore.kt       # Encrypted storage for the user's API key
│   │   │   ├── DeliveryGeo.kt       # Outlet and drop-off coordinates, distances
│   │   │   └── RouteRepository.kt   # OSRM road route + offline fallback
│   │   ├── notifications/
│   │   │   └── OrderNotifier.kt     # Notification channel and order-stage alerts
│   │   ├── viewmodel/
│   │   │   └── PizzaViewModel.kt    # App state: cart, pricing, coupons, order progress
│   │   └── ui/
│   │       ├── screens/             # Splash, Auth, Home, Search, PizzaDetail, Cart,
│   │       │                        # Checkout, OrderTracking, Profile
│   │       ├── components/          # PizzaImagery, DeliveryMap, NutritionChat,
│   │       │                        # AddressPicker, Effects, AnimatedValue, …
│   │       └── theme/               # Color, Theme, Type, Dimens, Motion
│   └── res/
│       ├── drawable/                # Logo, map pins, rider, veg/non-veg marks
│       ├── font/                    # Inter + Playfair Display (variable fonts)
│       └── values/                  # Strings, colours, themes
└── test/java/com/pizza5ps/app/
    ├── NutritionTest.kt
    └── PizzaOrderTest.kt
```

---

## How it works

### State

`PizzaViewModel` holds all app state as `StateFlow`s: the menu, the current build, the
cart, the applied coupon and the order stage. Screens collect these flows and send user
actions back to the ViewModel. Composables never change state directly.

### Pricing

The price is the base price, plus the size (Medium +₹100, Large +₹200), plus the crust
(Thin +₹30, Cheese Burst +₹70). Toppings are free. The cart then applies the coupon
discount, a ₹30 delivery fee (free on orders of ₹500 or more) and 5% GST. Everything is
recalculated on every change and shown as line items.

### Nutrition

`NutritionCalculator` (in [`data/Nutrition.kt`](app/src/main/java/com/pizza5ps/app/data/Nutrition.kt))
holds a per-100 g composition table for each ingredient and a recipe of gram weights for
each pizza at the 10" reference size. For any build, it:

1. Scales the toppings by **area**, not diameter. A 12" pizza carries 2.25× the
   toppings of an 8".
2. Adjusts the dough for the crust (thin crust uses 70% of the dough; cheese burst adds
   stuffed-rim mozzarella).
3. Adds the extra toppings and divides the total into per-slice figures (4, 6 or 8
   slices).

These are good estimates for a menu, not lab measurements, and the UI says so.

### Nutrition assistant

| Mode | When it's used | How it answers |
| :--- | :--- | :--- |
| **Offline** (default) | Always available, no key needed | Matches the question to figures the calculator has already worked out |
| **Claude** | After the user adds their own API key in Profile | Claude writes the reply, using only a fact sheet of calculated figures passed to it |

No API key ships in the APK, because any key bundled in an APK can be extracted by
whoever has the file. The user's key is stored with `EncryptedSharedPreferences` (backed
by the Android keystore) and is only ever sent to Anthropic.

### Delivery map

`RouteRepository` asks the public OSRM server for the driving route between the outlet
and the chosen address. If the request fails, it falls back to a stored approximation.
`DeliveryMap` draws the route and the moving rider marker on an osmdroid `MapView`
inside Compose.

---

## Design system

| Concern | Where it lives | Notes |
| :--- | :--- | :--- |
| Colour | `theme/Theme.kt` (`PizzaColors`) | Screens use semantic roles (`surface`, `textMuted`, `brand`), never raw colours. That is what makes light and dark themes possible without touching any screen. |
| Type | `theme/Type.kt` | Playfair Display for display text, Inter for everything else. |
| Spacing | `theme/Dimens.kt` | 4 dp grid (`Space`), plus `Radius` and `Elevation` tokens. |
| Motion | `theme/Motion.kt` | A small shared set of springs for physical interactions, short tweens for colour changes. |
| Elevation | `Modifier.warmShadow` | Brown-tinted shadows in light mode, black in dark mode. |

---

## Testing

```bash
./gradlew testDebugUnitTest
```

There are 13 JVM unit tests in two files:

- **`NutritionTest`** (8) checks that calories fall in a realistic range, that toppings
  scale with area, that per-slice × slices equals the whole pizza, that thin crust is
  lighter and cheese burst heavier, and that every menu pizza has a recipe. It also
  checks that both assistants work only from the calculated figures.
- **`PizzaOrderTest`** (5) covers the menu, price calculation, free toppings, each
  pizza's default toppings and coupon discounts.

The HTML report is written to `app/build/reports/tests/testDebugUnitTest/index.html`.

---

## Release build

```bash
./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

Release signing reads its credentials from `keystore.properties` in the project root:

```properties
storeFile=pizza5ps-release.jks
storePassword=…
keyAlias=…
keyPassword=…
```

If that file is missing, the release build is produced **unsigned**, so the project still
builds on any machine. **Never commit `keystore.properties` or the `.jks` file to a public
repository.** If you lose them, you can never publish an update to the same Play Store
listing.

R8 minification is turned off on purpose. osmdroid uses reflection and would need extra
keep rules.

---

## Limitations

This is a front-end demo, not a production service:

- **No backend.** Accounts, the cart and orders live in memory and are lost when the app
  closes. Demo passwords are stored in plain text.
- **Payments are simulated.** Nothing is charged.
- **Order progress is simulated** on a timer. No real kitchen or rider is involved.
- **Nutrition values are estimates** from typical recipes and published food data.
- **Map routing uses the public OSRM demo server**, which is rate-limited and not meant
  for production traffic.

---

## Third-party licences

| Component | Licence |
| :--- | :--- |
| Inter, Playfair Display fonts | SIL Open Font License |
| OpenStreetMap map data and tiles | ODbL |
| OSRM routing | BSD-2-Clause |
| osmdroid | Apache 2.0 |
| Anthropic Java SDK | MIT |
| AndroidX / Jetpack Compose | Apache 2.0 |

All pizza artwork is drawn in the app. The font, map, routing and osmdroid notices are
in [`THIRD_PARTY_LICENSES.txt`](THIRD_PARTY_LICENSES.txt).
