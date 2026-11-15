package com.pizza5ps.app.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** A plain lat/lon pair, kept free of any map-library types so the data layer
 *  and the ViewModel never depend on osmdroid. */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class DeliveryStop(
    val label: String,
    val address: String,
    val point: GeoPoint
)

/**
 * Real coordinates in Bengaluru.
 *
 * There is exactly one outlet and it is in Indiranagar — the brand is a single
 * pizzeria, so every order in the app is routed from that one kitchen. Only the
 * delivery end of the route ever changes.
 *
 * Using genuine streets means the routing service returns genuine roads, so the map
 * shows a real path across the city rather than a decorative squiggle.
 */
object DeliveryGeo {

    /** The one and only outlet. Every route starts here and this never changes. */
    val store = DeliveryStop(
        label = "Pizza 5 P's — Indiranagar",
        address = "100 Feet Road, HAL 2nd Stage, Indiranagar, Bengaluru - 560038",
        point = GeoPoint(12.9719, 77.6412)
    )

    /**
     * Saved delivery addresses. The first is the default the app opens with.
     */
    val destinations: List<DeliveryStop> = listOf(
        DeliveryStop(
            label = "Jain University",
            address = "Jain (Deemed-to-be University), Jayanagar 9th Block, " +
                "Bengaluru - 560069",
            point = GeoPoint(12.9250, 77.5938)
        ),
        DeliveryStop(
            label = "Home",
            address = "123, 4th Cross, Indiranagar, Bengaluru - 560038",
            point = GeoPoint(12.9784, 77.6408)
        ),
        DeliveryStop(
            label = "CMH Road",
            address = "48, CMH Road, Indiranagar, Bengaluru - 560038",
            point = GeoPoint(12.9766, 77.6370)
        ),
        DeliveryStop(
            label = "Domlur",
            address = "9, Amarjyoti Layout, Domlur, Bengaluru - 560071",
            point = GeoPoint(12.9609, 77.6387)
        ),
        DeliveryStop(
            label = "Koramangala",
            address = "22, 5th Block, Koramangala, Bengaluru - 560095",
            point = GeoPoint(12.9352, 77.6245)
        ),
        DeliveryStop(
            label = "Ulsoor",
            address = "7, Someshwara Temple Road, Ulsoor, Bengaluru - 560008",
            point = GeoPoint(12.9812, 77.6255)
        )
    )

    /** Where the app delivers to until the customer picks something else. */
    val defaultDestination: DeliveryStop = destinations.first()

    fun destinationForAddress(address: String): DeliveryStop =
        destinations.firstOrNull { it.address == address || address.contains(it.label) }
            ?: defaultDestination
}

/** Great-circle distance in metres. */
fun haversineMetres(from: GeoPoint, to: GeoPoint): Double {
    val earthRadius = 6_371_000.0
    val dLat = Math.toRadians(to.latitude - from.latitude)
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
        sin(dLon / 2).pow(2)
    return 2 * earthRadius * atan2(sqrt(a), sqrt(1 - a))
}
