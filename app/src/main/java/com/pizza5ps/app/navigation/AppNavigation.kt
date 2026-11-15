package com.pizza5ps.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pizza5ps.app.ui.screens.AuthScreen
import com.pizza5ps.app.ui.screens.CartScreen
import com.pizza5ps.app.ui.screens.CheckoutScreen
import com.pizza5ps.app.ui.screens.HomeScreen
import com.pizza5ps.app.ui.screens.OrderTrackingScreen
import com.pizza5ps.app.ui.screens.PizzaDetailScreen
import com.pizza5ps.app.ui.screens.ProfileScreen
import com.pizza5ps.app.ui.screens.SearchScreen
import com.pizza5ps.app.ui.screens.SplashScreen
import com.pizza5ps.app.viewmodel.PizzaViewModel
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.pizza5ps.app.ui.theme.Motion
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val HOME = "home"
    const val SEARCH = "search"
    const val DETAIL = "detail"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val TRACKING = "tracking"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: PizzaViewModel = viewModel()
) {
    val account by viewModel.account.collectAsState()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // The sign-in gate is enforced continuously, not just once at the splash.
    //
    // Navigation state is saved and restored across process death, but the ViewModel
    // holding the account is not — so a restored back stack could drop someone
    // straight onto the cart or a product page with nobody signed in. Watching the
    // account instead of checking it once closes that hole, and it also handles
    // signing out from anywhere.
    LaunchedEffect(account, currentRoute) {
        val needsGate = currentRoute != null &&
            currentRoute != Routes.SPLASH &&
            currentRoute != Routes.AUTH
        if (account == null && needsGate) {
            navController.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Lateral moves between peer screens slide horizontally, which matches the
    // direction of travel through the app. The distances are deliberately short —
    // a full-width slide on every navigation quickly feels sluggish.
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(Motion.DurationMedium, easing = Motion.EaseEmphasised),
                initialOffsetX = { it / 6 }
            ) + fadeIn(tween(Motion.DurationMedium))
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(Motion.DurationMedium, easing = Motion.EaseStandard),
                targetOffsetX = { -it / 8 }
            ) + fadeOut(tween(Motion.DurationFast))
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(Motion.DurationMedium, easing = Motion.EaseEmphasised),
                initialOffsetX = { -it / 8 }
            ) + fadeIn(tween(Motion.DurationMedium))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(Motion.DurationMedium, easing = Motion.EaseStandard),
                targetOffsetX = { it / 6 }
            ) + fadeOut(tween(Motion.DurationFast))
        }
    ) {
        // 1. Splash Screen. Dissolves rather than slides: there is no previous
        //    screen for it to have travelled from.
        composable(
            route = Routes.SPLASH,
            exitTransition = { fadeOut(tween(Motion.DurationSlow)) }
        ) {
            SplashScreen(
                onSplashComplete = {
                    // Signing in is a gate, not a step: an already-signed-in customer
                    // goes straight to the menu.
                    val destination = if (viewModel.account.value == null) {
                        Routes.AUTH
                    } else {
                        Routes.HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Sign in / sign up gate
        composable(Routes.AUTH) {
            AuthScreen(
                viewModel = viewModel,
                onAuthenticated = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // 3. Home Screen
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDetails = { pizza ->
                    navController.navigate(Routes.DETAIL)
                },
                onNavigateToSearch = {
                    navController.navigate(Routes.SEARCH)
                },
                onNavigateToCart = {
                    navController.navigate(Routes.CART)
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        // 3. Search Screen
        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = viewModel,
                onNavigateToDetails = { pizza ->
                    navController.navigate(Routes.DETAIL)
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToCart = {
                    navController.navigate(Routes.CART)
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        // 4. Pizza Details & Customization Screen
        composable(
            route = Routes.DETAIL,
            // Rises from the bottom like a sheet, because that is what it is —
            // a detail view pulled up over the menu rather than a peer screen.
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(Motion.DurationSlow, easing = Motion.EaseEmphasised)
                ) + fadeIn(tween(Motion.DurationMedium))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(Motion.DurationMedium, easing = Motion.EaseStandard)
                ) + fadeOut(tween(Motion.DurationMedium))
            }
        ) {
            PizzaDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onAddedToCart = {
                    navController.navigate(Routes.CART)
                }
            )
        }

        // 5. Cart Screen
        composable(Routes.CART) {
            CartScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToCheckout = {
                    navController.navigate(Routes.CHECKOUT)
                },
                onBrowseMenuClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // 6. Checkout Screen
        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onOrderPlaced = {
                    navController.navigate(Routes.TRACKING) {
                        popUpTo(Routes.CHECKOUT) { inclusive = true }
                    }
                }
            )
        }

        // 7. Order Tracking Screen
        composable(Routes.TRACKING) {
            OrderTrackingScreen(
                viewModel = viewModel,
                onBackToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // 8. Profile Screen
        composable(Routes.PROFILE) {
            ProfileScreen(
                viewModel = viewModel,
                onSignedOut = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToSearch = {
                    navController.navigate(Routes.SEARCH)
                },
                onNavigateToCart = {
                    navController.navigate(Routes.CART)
                },
                onNavigateToTracking = {
                    navController.navigate(Routes.TRACKING)
                }
            )
        }
    }
}
