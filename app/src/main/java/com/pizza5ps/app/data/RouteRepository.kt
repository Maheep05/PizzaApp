package com.pizza5ps.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

/**
 * A road route, resolved to a list of points along real streets.
 *
 * [isApproximate] is true when the route came from the offline fallback rather than
 * the routing service. The UI surfaces that distinction rather than hiding it —
 * quietly presenting a synthesised line as a real road route would be misleading.
 */
data class DeliveryRoute(
    val points: List<GeoPoint>,
    val distanceMetres: Double,
    val durationSeconds: Double,
    val isApproximate: Boolean
) {
    /** Cumulative distance to each point, so progress can be measured in metres
     *  travelled rather than in points visited (points are unevenly spaced). */
    private val cumulative: List<Double> = buildList {
        var running = 0.0
        add(0.0)
        for (index in 1 until points.size) {
            running += haversineMetres(points[index - 1], points[index])
            add(running)
        }
    }

    private val totalLength: Double get() = cumulative.lastOrNull() ?: 0.0

    val distanceLabel: String
        get() = if (distanceMetres >= 1000) {
            String.format("%.1f km", distanceMetres / 1000)
        } else {
            "${distanceMetres.roundToInt()} m"
        }

    val durationLabel: String
        get() = "${(durationSeconds / 60).roundToInt().coerceAtLeast(1)} min"

    /** Position along the route at [fraction] (0..1) of the distance travelled. */
    fun pointAt(fraction: Double): GeoPoint {
        if (points.isEmpty()) return DeliveryGeo.store.point
        if (points.size == 1 || totalLength == 0.0) return points.first()

        val target = (fraction.coerceIn(0.0, 1.0)) * totalLength
        val index = cumulative.indexOfFirst { it >= target }.coerceAtLeast(1)
        val segmentStart = cumulative[index - 1]
        val segmentLength = cumulative[index] - segmentStart
        val within = if (segmentLength == 0.0) 0.0 else (target - segmentStart) / segmentLength

        val from = points[index - 1]
        val to = points[index]
        return GeoPoint(
            latitude = from.latitude + (to.latitude - from.latitude) * within,
            longitude = from.longitude + (to.longitude - from.longitude) * within
        )
    }

    /** The stretch already covered — drawn in brand red over the full grey route. */
    fun travelledPath(fraction: Double): List<GeoPoint> {
        if (points.size < 2) return points
        val target = fraction.coerceIn(0.0, 1.0) * totalLength
        val covered = points.filterIndexed { index, _ -> cumulative[index] <= target }
        return (covered + pointAt(fraction)).distinct()
    }
}

/**
 * Resolves the shortest driving route between two points.
 *
 * Routing uses the public OSRM demo service, which is open-source, needs no API key
 * and returns the genuine shortest path over the OpenStreetMap road graph. It does
 * require a network, so a failure falls back to a synthesised path — the app must
 * still demo in a classroom with no wifi.
 */
object RouteRepository {

    private const val OSRM_BASE = "https://router.project-osrm.org/route/v1/driving"
    private const val TIMEOUT_MS = 6_000

    suspend fun shortestRoute(from: GeoPoint, to: GeoPoint): DeliveryRoute =
        withContext(Dispatchers.IO) {
            fetchFromOsrm(from, to) ?: approximateRoute(from, to)
        }

    private fun fetchFromOsrm(from: GeoPoint, to: GeoPoint): DeliveryRoute? {
        // OSRM takes coordinates as lon,lat — the reverse of the usual convention.
        val url = "$OSRM_BASE/" +
            "${from.longitude},${from.latitude};${to.longitude},${to.latitude}" +
            "?overview=full&geometries=geojson"

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("User-Agent", "Pizza5Ps-Android-Demo")
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            if (json.optString("code") != "Ok") return null

            val route = json.getJSONArray("routes").optJSONObject(0) ?: return null
            val coordinates = route.getJSONObject("geometry").getJSONArray("coordinates")

            val points = (0 until coordinates.length()).map { index ->
                val pair = coordinates.getJSONArray(index)
                GeoPoint(latitude = pair.getDouble(1), longitude = pair.getDouble(0))
            }
            if (points.size < 2) return null

            DeliveryRoute(
                points = points,
                distanceMetres = route.getDouble("distance"),
                durationSeconds = route.getDouble("duration"),
                isApproximate = false
            )
        } catch (error: Exception) {
            // Offline, DNS failure, rate limit, malformed payload — all fall back.
            null
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Offline stand-in: interpolated points nudged sideways along the way so the
     * line bends like a street instead of cutting straight across the map. Marked
     * approximate so the UI can say so.
     */
    private fun approximateRoute(from: GeoPoint, to: GeoPoint): DeliveryRoute {
        val steps = 24
        val straightLine = haversineMetres(from, to)
        val points = (0..steps).map { step ->
            val t = step.toDouble() / steps
            // A single smooth sine bow, largest at the midpoint.
            val bow = kotlin.math.sin(t * Math.PI) * 0.0016
            GeoPoint(
                latitude = from.latitude + (to.latitude - from.latitude) * t + bow * 0.6,
                longitude = from.longitude + (to.longitude - from.longitude) * t - bow
            )
        }
        // Road distance runs longer than the straight line; ~35% is a fair city guess.
        val estimatedDistance = straightLine * 1.35
        return DeliveryRoute(
            points = points,
            distanceMetres = estimatedDistance,
            // ~18 km/h average through Bengaluru traffic.
            durationSeconds = estimatedDistance / 5.0,
            isApproximate = true
        )
    }
}
