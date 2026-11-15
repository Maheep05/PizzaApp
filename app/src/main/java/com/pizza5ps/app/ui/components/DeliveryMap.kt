package com.pizza5ps.app.ui.components

import android.content.Context
import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pizza5ps.app.R
import com.pizza5ps.app.data.DeliveryGeo
import com.pizza5ps.app.data.DeliveryRoute
import com.pizza5ps.app.data.DeliveryStop
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.bouncyClick
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import org.osmdroid.util.GeoPoint as OsmGeoPoint

/**
 * The live delivery map.
 *
 * Renders OpenStreetMap tiles through osmdroid — open source, no API key, no account
 * — and draws the road route resolved by [com.pizza5ps.app.data.RouteRepository].
 * The route is painted in two layers: the whole path in muted grey, and the stretch
 * already covered in brand red on top, so progress is legible at a glance without
 * reading any text.
 *
 * When [onTapToExpand] is supplied, a transparent overlay sits above the map and
 * takes the touches. That is deliberate: the inline map lives inside a vertically
 * scrolling screen, and osmdroid pans on a one-finger drag, so an exposed map would
 * swallow the scroll gesture every time a thumb landed on it. The overlay claims
 * taps for "expand" while vertical drags still reach the scrolling parent.
 */
@Composable
fun DeliveryMapView(
    route: DeliveryRoute?,
    riderFraction: Float,
    destination: DeliveryStop,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    onTapToExpand: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // osmdroid reads its configuration when a MapView is constructed, so this must
    // happen first. Caching into the app's own directory avoids storage permissions.
    val mapView = remember {
        configureOsmdroid(context)
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(interactive)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            zoomController.setVisibility(
                if (interactive) {
                    CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
                } else {
                    CustomZoomButtonsController.Visibility.NEVER
                }
            )
            controller.setZoom(14.0)
            controller.setCenter(
                OsmGeoPoint(DeliveryGeo.store.point.latitude, DeliveryGeo.store.point.longitude)
            )
            onResume()
        }
    }

    // Whether this map has already framed its route. Held here rather than on the
    // view so it survives recomposition without needing a view tag.
    val framed = remember { booleanArrayOf(false) }

    // The overlays are drawn by osmdroid, outside composition, so the theme's
    // colours have to be resolved here and handed down as plain ARGB ints.
    val routeColours = RouteColours(
        remaining = PizzaTheme.colors.textSecondary.copy(alpha = 0.45f).toArgb(),
        travelled = PizzaTheme.colors.brand.toArgb(),
        casing = PizzaTheme.colors.surface.toArgb()
    )

    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                map.renderRoute(route, riderFraction, destination, framed, routeColours)
            }
        )

        if (onTapToExpand != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .bouncyClick(pressedScale = 1f, onClick = onTapToExpand)
            )
        }
    }
}

private fun configureOsmdroid(context: Context) {
    Configuration.getInstance().apply {
        // OSM's tile servers reject requests without a meaningful User-Agent.
        userAgentValue = context.packageName
        osmdroidBasePath = File(context.cacheDir, "osmdroid").apply { mkdirs() }
        osmdroidTileCache = File(osmdroidBasePath, "tiles").apply { mkdirs() }
    }
}

/**
 * Rebuilds the overlays for the current progress. Overlays are cleared and re-added
 * rather than mutated, because the route itself arrives asynchronously and a handful
 * of overlays is far too cheap for the difference to matter.
 */
private fun MapView.renderRoute(
    route: DeliveryRoute?,
    riderFraction: Float,
    destination: DeliveryStop,
    framed: BooleanArray,
    colours: RouteColours
) {
    overlays.clear()

    if (route == null || route.points.size < 2) {
        invalidate()
        return
    }

    val fullPath = route.points.map { OsmGeoPoint(it.latitude, it.longitude) }

    // A casing beneath the route keeps it readable over dark map features.
    overlays.add(routeLine(fullPath, colours.casing, 17f))
    // The complete route, muted.
    overlays.add(routeLine(fullPath, colours.remaining, 10f))

    // The stretch already covered, in brand red on top.
    val travelled = route.travelledPath(riderFraction.toDouble())
    if (travelled.size >= 2) {
        overlays.add(
            routeLine(
                travelled.map { OsmGeoPoint(it.latitude, it.longitude) },
                colours.travelled,
                11f
            )
        )
    }

    overlays.add(pinMarker(fullPath.first(), R.drawable.ic_pin_store, DeliveryGeo.store.label))
    overlays.add(pinMarker(fullPath.last(), R.drawable.ic_pin_home, destination.address))

    // The rider stays upright rather than rotating to its heading — a rotated badge
    // would turn the scooter upside down on every westbound leg.
    val riderPoint = route.pointAt(riderFraction.toDouble())
    overlays.add(
        Marker(this).apply {
            position = OsmGeoPoint(riderPoint.latitude, riderPoint.longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_rider_marker)
            title = "Rider Vikram S."
            setInfoWindow(null)
        }
    )

    // Frame the whole route once. Zooming to a bounding box before the view has been
    // measured has no dimensions to work with and silently does nothing, so this
    // waits for layout when it has to — the route usually arrives after first layout,
    // in which case the box can be applied straight away.
    if (!framed[0]) {
        framed[0] = true
        val bounds = BoundingBox.fromGeoPoints(fullPath).increaseByScale(1.35f)
        if (width > 0 && height > 0) {
            zoomToBoundingBox(bounds, false, MAP_EDGE_PADDING_PX)
        } else {
            addOnFirstLayoutListener { _, _, _, _, _ ->
                zoomToBoundingBox(bounds, false, MAP_EDGE_PADDING_PX)
            }
        }
    }

    invalidate()
}

/** Route colours resolved from the theme before leaving composition. */
private data class RouteColours(
    val remaining: Int,
    val travelled: Int,
    val casing: Int
)

private const val MAP_EDGE_PADDING_PX = 56

private fun MapView.routeLine(
    points: List<OsmGeoPoint>,
    colour: Int,
    width: Float
): Polyline = Polyline(this).apply {
    setPoints(points)
    outlinePaint.apply {
        color = colour
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
}

private fun MapView.pinMarker(
    point: OsmGeoPoint,
    iconRes: Int,
    markerTitle: String
): Marker = Marker(this).apply {
    position = point
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    icon = ContextCompat.getDrawable(context, iconRes)
    title = markerTitle
    setInfoWindow(null)
}
