package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.GoogleAuthHelper
import com.example.ui.AppViewModel
import com.example.ui.Screen
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppNavigationRail
import com.example.ui.components.AppTopBar
import com.example.ui.components.ConcealedAdminPasswordDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartAndCheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyAddressesScreen
import com.example.ui.screens.MyOrdersScreen
import com.example.ui.screens.MyRepairsScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RepairPickupScreen
import com.example.ui.screens.ScreenGuardsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.TrackOrderScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ProvideResponsiveLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.statusBars())

        setContent {
            ProvideResponsiveLayout {
                MyApplicationTheme {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp(viewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val spec = LocalResponsiveSpec.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val totalCartCount = cartItems.sumOf { it.quantity }
    val showAdminDialog by viewModel.showAdminPasswordDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var firebaseUser by remember {
        mutableStateOf(
            try {
                Firebase.auth.currentUser
            } catch (_: Exception) {
                null
            }
        )
    }
    var isCheckingInitialSession by remember { mutableStateOf(firebaseUser == null && !customerProfile.isLoggedIn) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            firebaseUser = auth.currentUser
        }
        try {
            Firebase.auth.addAuthStateListener(listener)
        } catch (_: Exception) {
        }
        onDispose {
            try {
                Firebase.auth.removeAuthStateListener(listener)
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        val credentialManager = CredentialManager.create(context)
        GoogleAuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            scope = scope,
            onAuthSuccess = { user ->
                firebaseUser = user
                viewModel.onFirebaseSessionRestored(user)
                isCheckingInitialSession = false
            },
            onUnauthenticated = {
                isCheckingInitialSession = false
            }
        )
    }

    val isAuthenticated = firebaseUser != null && customerProfile.isLoggedIn && customerProfile.isGoogleSignedIn
    if (!isAuthenticated) {
        AuthScreen(
            isCheckingInitialSession = isCheckingInitialSession,
            onAuthSuccess = { user ->
                firebaseUser = user
                viewModel.onGoogleUserAuthenticated(user)
                viewModel.navigateToHome()
            }
        )
        return
    }

    // Intercept back key when on sub-screen
    BackHandler(enabled = currentScreen !is Screen.Home) {
        if (currentScreen is Screen.AdminDashboard) {
            viewModel.adminLogout()
        } else {
            viewModel.navigateBack()
        }
    }

    val isDedicatedSubPage = currentScreen is Screen.AdminDashboard ||
        currentScreen is Screen.ProductDetail ||
        currentScreen is Screen.Shop ||
        currentScreen is Screen.ScreenGuards ||
        currentScreen is Screen.MyOrders ||
        currentScreen is Screen.MyRepairs ||
        currentScreen is Screen.MyAddresses ||
        currentScreen is Screen.Wishlist

    val hideBottomNav = currentScreen is Screen.AdminDashboard ||
        currentScreen is Screen.ProductDetail ||
        currentScreen is Screen.MyOrders ||
        currentScreen is Screen.MyRepairs ||
        currentScreen is Screen.MyAddresses ||
        currentScreen is Screen.Wishlist

    Row(modifier = Modifier.fillMaxSize()) {
        if (!hideBottomNav && spec.useNavRail) {
            AppNavigationRail(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }

        Scaffold(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // Customer TopBar with 5-tap logo handler (only visible on primary Customer screens)
                if (!isDedicatedSubPage) {
                    AppTopBar(
                        viewModel = viewModel,
                        onCartClicked = { viewModel.navigateTo(Screen.Cart) },
                        onMenuClicked = { viewModel.navigateTo(Screen.Shop) }
                    )
                }
            },
            bottomBar = {
                // Customer Bottom Navigation on compact screens
                if (!hideBottomNav && !spec.useNavRail) {
                    AppBottomNav(
                        currentScreen = currentScreen,
                        cartCount = totalCartCount,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onProductClick = { product -> viewModel.navigateTo(Screen.ProductDetail(product)) },
                        onNavigateToRepair = { viewModel.navigateTo(Screen.RepairPickup) },
                        snackbarHostState = snackbarHostState
                    )
                }
                is Screen.Shop -> {
                    ShopScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() },
                        onCartClick = { viewModel.navigateTo(Screen.Cart) },
                        onProductClick = { product -> viewModel.navigateTo(Screen.ProductDetail(product)) },
                        onOpenScreenGuards = { viewModel.navigateTo(Screen.ScreenGuards()) },
                        snackbarHostState = snackbarHostState
                    )
                }
                is Screen.ScreenGuards -> {
                    ScreenGuardsScreen(
                        viewModel = viewModel,
                        initialSection = screen.initialSection,
                        initialGlassType = screen.initialGlassType,
                        onBack = { viewModel.navigateBack() },
                        onCartClick = { viewModel.navigateTo(Screen.Cart) }
                    )
                }
                is Screen.ProductDetail -> {
                    ProductDetailScreen(
                        product = screen.product,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() },
                        onNavigateToRepair = { viewModel.navigateTo(Screen.RepairPickup) },
                        snackbarHostState = snackbarHostState
                    )
                }
                is Screen.Cart -> {
                    CartAndCheckoutScreen(
                        viewModel = viewModel,
                        onTrackOrder = { orderId ->
                            viewModel.navigateTo(Screen.TrackOrder)
                            viewModel.searchTracking(orderId)
                        },
                        onContinueShopping = { viewModel.navigateToHome() }
                    )
                }
                is Screen.RepairPickup -> {
                    RepairPickupScreen(
                        viewModel = viewModel,
                        onTrackRepair = { repairId ->
                            viewModel.navigateTo(Screen.TrackOrder)
                            viewModel.searchTracking(repairId)
                        }
                    )
                }
                is Screen.TrackOrder -> {
                    TrackOrderScreen(viewModel = viewModel)
                }
                is Screen.Profile -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        onTrackOrder = { id ->
                            viewModel.navigateTo(Screen.TrackOrder)
                            viewModel.searchTracking(id)
                        }
                    )
                }
                is Screen.MyOrders -> {
                    MyOrdersScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() },
                        onTrackOrder = { id ->
                            viewModel.navigateTo(Screen.TrackOrder)
                            viewModel.searchTracking(id)
                        },
                        onExploreShop = { viewModel.navigateTo(Screen.Shop) }
                    )
                }
                is Screen.MyRepairs -> {
                    MyRepairsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() },
                        onTrackRepair = { id ->
                            viewModel.navigateTo(Screen.TrackOrder)
                            viewModel.searchTracking(id)
                        },
                        onBookRepair = { viewModel.navigateTo(Screen.RepairPickup) }
                    )
                }
                is Screen.MyAddresses -> {
                    MyAddressesScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() },
                        snackbarHostState = snackbarHostState
                    )
                }
                is Screen.Wishlist -> {
                    WishlistScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() },
                        onProductClick = { product -> viewModel.navigateTo(Screen.ProductDetail(product)) },
                        onExploreShop = { viewModel.navigateTo(Screen.Shop) },
                        snackbarHostState = snackbarHostState
                    )
                }
                is Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onLogout = { viewModel.adminLogout() }
                    )
                }
            }
        }
    }
    }

    // Concealed Admin Password Modal (triggered only after 5 logo taps)
    if (showAdminDialog) {
        ConcealedAdminPasswordDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.dismissAdminPasswordDialog() }
        )
    }
}
