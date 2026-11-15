package com.pizza5ps.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.pizza5ps.app.navigation.AppNavigation
import com.pizza5ps.app.notifications.OrderNotifier
import com.pizza5ps.app.ui.theme.Pizza5PsTheme
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.viewmodel.PizzaViewModel

class MainActivity : ComponentActivity() {

    // Hoisted to the activity so the appearance choice can be read above the
    // navigation graph, and so navigation and the theme share one instance.
    private val viewModel: PizzaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        OrderNotifier.ensureChannel(this)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            Pizza5PsTheme(themeMode = themeMode) {
                NotificationPermissionGate()
                OrderNotificationBridge(viewModel = viewModel)

                Box(modifier = Modifier.fillMaxSize()) {
                    AmbientWarmth()
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * Asks for notification access once, on Android 13+ where it is runtime-gated.
 *
 * Requested at launch rather than at checkout so the permission dialog never lands
 * on top of the order flow — being interrupted mid-purchase is the worst possible
 * moment to be asked for anything.
 */
@Composable
private fun NotificationPermissionGate() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Declining is fine: in-app tracking works either way. */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !OrderNotifier.hasPermission(context)
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/**
 * Posts a notification whenever the active order changes stage.
 *
 * Lives here, watching the ViewModel, rather than inside it: posting a notification
 * needs a Context, and keeping Android framework handles out of the ViewModel is what
 * keeps it testable and leak-free.
 */
@Composable
private fun OrderNotificationBridge(viewModel: PizzaViewModel) {
    val context = LocalContext.current
    val activeOrder by viewModel.activeOrder.collectAsState()

    LaunchedEffect(activeOrder?.id, activeOrder?.status) {
        val order = activeOrder
        if (order == null) {
            OrderNotifier.clear(context)
        } else {
            OrderNotifier.notifyStatus(context, order, order.status)
        }
    }
}

/**
 * A single app-wide ground layer: the theme's ground colour with two very soft
 * off-screen glows, one brand-tinted at the top-left and one honey at the
 * bottom-right. It is almost subliminal, but it stops large empty areas from reading
 * as flat dead paper and gives every screen the same ambient light direction.
 */
@Composable
private fun AmbientWarmth() {
    val colors = PizzaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.ground)
            .background(
                Brush.radialGradient(
                    colors = listOf(colors.brandSoft, colors.ground.copy(alpha = 0f)),
                    center = Offset(-40f, -80f),
                    radius = 1100f
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colors.accentSoft.copy(alpha = if (colors.isDark) 0.35f else 0.18f),
                        colors.ground.copy(alpha = 0f)
                    ),
                    center = Offset(1200f, 2600f),
                    radius = 1300f
                )
            )
    )
}
