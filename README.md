# 🍕 Pizza 5 P's — Android Single-Brand Pizza Ordering App

> **Tagline:** *Perfect Pizza. Perfect Moments.*
> **Target:** Android College Project / Commercial-grade UI Showcase
> **Tech Stack:** 100% Kotlin + Jetpack Compose (Material 3) + StateFlow & Coroutines

---

## 📱 App Highlights

- **Every pizza is drawn, not photographed.** One circular dough base is rendered on a
  Canvas — baked crust, oven char, sauce, molten cheese, slice cuts — and *everything
  else is a topping*. Each pizza's signature toppings identify it on the menu; the
  customer's extras land on that same base. No image assets are bundled at all.
- **Interactive customiser.** Choosing a size physically resizes the pizza; adding a
  topping drops it onto the pizza; removing one flips the pizza to throw it off. The
  hero stays pinned and collapses as you scroll, so every choice is visibly applied
  and all three steps sit within easy reach.
- **Extra toppings are free.** Every topping is complimentary, shown as "Free" on the
  chip and in the breakdown.
- **Live delivery map.** Real OpenStreetMap tiles via **osmdroid** with the genuine
  shortest road route from the outlet to a randomly assigned drop-off, resolved by the
  **OSRM** routing service. The rider advances along the route as the order progresses.
- **Order notifications.** Every stage change posts a heads-up notification with a
  step progress bar, so the order can be followed with the app closed.
- **Light and dark appearance.** A full semantic colour system with a warm cream light
  theme and a wood-fired-oven dark theme. Switchable in Profile (System/Light/Dark).
- **Bundled type system.** Playfair Display for display text, Inter for UI — both
  variable fonts, bundled, so there is no network fetch and no fallback flash.
- **One motion language.** Every press, reveal and transition draws its spring from a
  shared token set (`Motion`), with haptic feedback on tap.
- **Dynamic pricing engine.** Live recalculation across size and crust, with an
  itemised breakdown and a rolling-digit total.
- **Interactive cart & coupons.** `PIZZA20` (20% off) and `PIZZA40` (40% off).
- **4-stage delivery tracking (demo mode):** Confirmed → Preparing → Out for Delivery
  → Delivered, auto-advancing every 5s with a confetti payoff, plus manual
  `[Prev] / [Next]` controls and an auto-play pause for classroom presentation.

---

## 🚀 How to Run

### Android Studio
1. **File → Open…** and select this directory.
2. Let Gradle sync (it will fetch osmdroid on first sync).
3. Pick an emulator or device (**minSdk 26**) and press **Run**.

### Command line
```bash
./gradlew installDebug      # build and install on the connected device
./gradlew testDebugUnitTest # run unit tests
```

### Network
The map needs a network for tiles and routing. Without one the app still works: the
route falls back to a bundled approximation and is labelled **"Offline estimate"**
rather than being passed off as a real road route.

---

## 📂 Project Architecture

```text
app/src/main/
├── AndroidManifest.xml            # INTERNET, ACCESS_NETWORK_STATE, POST_NOTIFICATIONS
├── java/com/pizza5ps/app/
│   ├── MainActivity.kt            # edge-to-edge, theme host, notification bridge
│   ├── navigation/AppNavigation.kt
│   ├── model/                     # Pizza, CartItem, Order, CustomizationOptions
│   ├── data/
│   │   ├── PizzaDataSource.kt     # the eight-pizza menu
│   │   ├── DeliveryGeo.kt         # real Indiranagar coordinates + haversine
│   │   └── RouteRepository.kt     # OSRM shortest route + offline fallback
│   ├── notifications/
│   │   └── OrderNotifier.kt       # channel, permission check, stage notifications
│   ├── viewmodel/PizzaViewModel.kt
│   └── ui/
│       ├── theme/
│       │   ├── Color.kt           # raw palette (light, dark, ingredients)
│       │   ├── Theme.kt           # PizzaColors semantic roles + ThemeMode
│       │   ├── Type.kt            # Playfair + Inter variable font scale
│       │   ├── Dimens.kt          # Space / Radius / Elevation / Shapes
│       │   └── Motion.kt          # springs, haptics, pressScale, shimmer, stagger
│       ├── components/
│       │   ├── PizzaImagery.kt    # drawn dough base, size glyph, topping sprites
│       │   ├── DeliveryMap.kt     # osmdroid MapView interop and route overlays
│       │   ├── DeliveryMapCard.kt # map card, route facts, full-screen map
│       │   ├── Atoms.kt           # SectionHeader, TagPill, buttons, SoftCard
│       │   ├── Effects.kt         # confetti, pulse rings, shimmer, favourite burst
│       │   ├── AnimatedValue.kt   # rolling price and count
│       │   ├── PizzaCard.kt, CategoryChip.kt, BottomNavBar.kt, TopBars.kt
│       │   ├── DynamicPriceBar.kt, FloatingCartBar.kt, OrderTimelineStepper.kt
│       └── screens/               # Splash, Home, Search, PizzaDetail, Cart,
│                                  # Checkout, OrderTracking, Profile
└── res/
    ├── drawable/                  # map pins, rider, logo, notification icon
    ├── font/                      # Inter + Playfair Display (variable)
    └── values/                    # strings, colors, themes
```

---

## 🎨 Design System

| Concern | Where it lives | Notes |
| :--- | :--- | :--- |
| Colour | `theme/Theme.kt` (`PizzaColors`) | Screens use **semantic roles** (`surface`, `textMuted`, `brand`), never raw hues — which is what makes two appearances possible without touching a screen. |
| Type | `theme/Type.kt` | Two families, one job each. Large sizes get negative tracking, small labels positive. |
| Spacing | `theme/Dimens.kt` | 4dp base grid via `Space`; radii scale with container size. |
| Motion | `theme/Motion.kt` | Four springs total. Physical interactions use springs, colour crossfades use short eased tweens. |
| Elevation | `Modifier.warmShadow` | Shadows are tinted roasted-brown in light mode (black shadows turn cream muddy) and plain black in dark. |

---

## 🎯 The Demo Journey

1. **Splash** — emblem springs in, wordmark and tagline follow.
2. **Home** — auto-rotating offer carousel, icon filter chips, cards cascading in.
3. **Product page** — tap **Margherita**. Switch to **Large** and watch the pizza grow
   (the size cards show real sliced dough discs at true relative scale). Add **Extra
   Mozzarella** and **Black Olives** — free — and watch them drop onto the pizza.
   Remove one and watch the pizza flip to throw it off.
4. **Cart** — `PIZZA20` applied, itemised bill, drawn pizza thumbnails.
5. **Checkout** — pick an address and UPI, then **PLACE ORDER**.
6. **Tracking** — a real map of Indiranagar with the shortest road route to a randomly
   chosen drop-off, the rider driving it, notifications at every stage, and confetti on
   delivery. Use **Prev / Next** or the **Auto** toggle to control the pace.
7. **Profile** — switch between **System / Light / Dark**.

---

## 📄 Third-party assets

Fonts (SIL OFL), map tiles (OpenStreetMap, ODbL), routing (OSRM) and the map view
(osmdroid, Apache 2.0). The pizza artwork is drawn in-app, so no image assets are
bundled. Full notices in
[`THIRD_PARTY_LICENSES.txt`](THIRD_PARTY_LICENSES.txt).
