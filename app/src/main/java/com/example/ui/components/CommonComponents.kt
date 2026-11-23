package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.data.ProductRatingSummary
import com.example.ui.AppViewModel
import com.example.ui.Screen
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGold
import com.example.ui.theme.iCaseLightBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import com.example.ui.theme.iCaseRedLight

/**
 * The exact iCASE Brand Logo matching all screens in the reference image:
 * - Curved red phone outline arch above the wordmark
 * - Bright red 'i', bold navy 'CASE'
 * - Tagline: "YOUR PHONE, OUR CARE"
 * - Tapping 5 times consecutively triggers concealed Admin Access
 */
@Composable
fun ICaseLogo(
    modifier: Modifier = Modifier,
    isDarkBackground: Boolean = false,
    showTagline: Boolean = true,
    scale: Float = 1f,
    onClick: (() -> Unit)? = null
) {
    val spec = LocalResponsiveSpec.current
    val effectiveScale = scale * if (spec.isCompactSmall) 0.9f else 1f

    Column(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
                } else Modifier
            )
            .padding(vertical = (2 * effectiveScale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.padding(horizontal = (4 * effectiveScale).dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .width((78 * effectiveScale).dp)
                    .height((36 * effectiveScale).dp)
            ) {
                val strokeWidth = 2.4f * density
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.12f, size.height * 0.90f)
                    cubicTo(
                        size.width * 0.16f, size.height * 0.08f,
                        size.width * 0.84f, size.height * 0.08f,
                        size.width * 0.88f, size.height * 0.90f
                    )
                }
                drawPath(
                    path = path,
                    color = iCaseRed,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = (8 * effectiveScale).dp)
            ) {
                Text(
                    text = "i",
                    fontSize = (26 * effectiveScale).sp,
                    fontWeight = FontWeight.Black,
                    color = iCaseRed,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "CASE",
                    fontSize = (24 * effectiveScale).sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDarkBackground) Color.White else iCaseNavy,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        if (showTagline) {
            Text(
                text = "YOUR PHONE, OUR CARE",
                fontSize = (8.5 * effectiveScale).sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkBackground) Color(0xFFBAC7D5) else Color(0xFF475569),
                letterSpacing = 1.2.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Top App Bar matching Screen 4:
 * Pure white surface, menu hamburger icon on left, centered iCASE logo, and cart on right.
 * Respects status bar and display cutout safe drawing insets.
 */
@Composable
fun AppTopBar(
    viewModel: AppViewModel,
    onCartClicked: () -> Unit,
    onMenuClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val totalCartCount = cartItems.sumOf { it.quantity }
    val spec = LocalResponsiveSpec.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
                .padding(horizontal = if (spec.isCompactSmall) 6.dp else 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onMenuClicked?.invoke() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = iCaseNavy,
                    modifier = Modifier.size(26.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .testTag("app_logo_tap"),
                contentAlignment = Alignment.Center
            ) {
                ICaseLogo(
                    onClick = { viewModel.onLogoClicked() }
                )
            }

            IconButton(
                onClick = onCartClicked,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_cart_button")
            ) {
                BadgedBox(
                    badge = {
                        if (totalCartCount > 0) {
                            Badge(
                                containerColor = iCaseRed,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "$totalCartCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Shopping Cart",
                        tint = iCaseNavy,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

private data class NavDestination(
    val screen: Screen,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

private val appNavDestinations = listOf(
    NavDestination(Screen.Home, "Home", Icons.Default.Home, Icons.Outlined.Home),
    NavDestination(Screen.Shop, "Shop", Icons.Default.ShoppingBag, Icons.Outlined.ShoppingBag),
    NavDestination(Screen.RepairPickup, "Repair", Icons.Default.Build, Icons.Outlined.Build),
    NavDestination(Screen.TrackOrder, "Pickup", Icons.Default.TwoWheeler, Icons.Outlined.TwoWheeler),
    NavDestination(Screen.Profile, "Account", Icons.Default.Person, Icons.Outlined.Person)
)

private fun isScreenSelected(navScreen: Screen, currentScreen: Screen): Boolean = when (navScreen) {
    is Screen.Home -> currentScreen is Screen.Home
    is Screen.Shop -> currentScreen is Screen.Shop
    is Screen.RepairPickup -> currentScreen is Screen.RepairPickup
    is Screen.TrackOrder -> currentScreen is Screen.TrackOrder
    is Screen.Profile -> currentScreen is Screen.Profile
    else -> false
}

/**
 * 5-tab Bottom Navigation for Compact screens (phones).
 */
@Composable
fun AppBottomNav(
    currentScreen: Screen,
    @Suppress("UNUSED_PARAMETER") cartCount: Int,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.White,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
        )
    ) {
        appNavDestinations.forEach { item ->
            val isSelected = isScreenSelected(item.screen, currentScreen)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(if (spec.isCompactSmall) 22.dp else 24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = spec.sp(11),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = iCaseRed,
                    selectedTextColor = iCaseRed,
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B),
                    indicatorColor = Color.Transparent
                ),
                modifier = Modifier.testTag("nav_${item.label.lowercase()}")
            )
        }
    }
}

/**
 * Canonical Material 3 NavigationRail for Medium/Expanded screens (tablets, foldables, ChromeOS, wide landscape).
 */
@Composable
fun AppNavigationRail(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current

    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = Color.White,
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Vertical + WindowInsetsSides.Start
        )
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        appNavDestinations.forEach { item ->
            val isSelected = isScreenSelected(item.screen, currentScreen)
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = spec.sp(11),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = iCaseRed,
                    selectedTextColor = iCaseRed,
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B),
                    indicatorColor = iCaseRedLight
                ),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .testTag("nav_${item.label.lowercase()}")
            )
        }
    }
}

@Composable
fun ConcealedAdminPasswordDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val passwordInput by viewModel.adminPasswordInput.collectAsState()
    val authError by viewModel.adminAuthError.collectAsState()
    val lockoutSeconds by viewModel.lockoutRemainingSeconds.collectAsState()
    var isPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iCaseNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Security",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "iCASE Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = iCaseNavy
                    )
                    Text(
                        text = "Authorized Personnel Only",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Please enter administrator credentials to access dashboard and management settings.",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { viewModel.adminPasswordInput.value = it },
                    label = { Text("Admin Password / PIN") },
                    placeholder = { Text("Enter admin password") },
                    singleLine = true,
                    enabled = lockoutSeconds == 0,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input")
                )

                AnimatedVisibility(visible = authError != null) {
                    Text(
                        text = authError ?: "",
                        color = iCaseRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (lockoutSeconds > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔒 Attempt Lockout active: $lockoutSeconds s",
                        color = iCaseRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.submitAdminPassword {} },
                enabled = lockoutSeconds == 0 && passwordInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("admin_login_button")
            ) {
                Text("Authenticate")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
    ratingSummary: ProductRatingSummary? = null
) {
    val spec = LocalResponsiveSpec.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spec.productImageHeight)
                    .background(Color(0xFFF1F5F9))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spec.productImageHeight)
                )

                // Top Badges Row (prevents overlap on narrow cards)
                if (product.isOneHourEligible || product.isPopular) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (product.isOneHourEligible) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(iCaseRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "1-Hour",
                                        fontSize = spec.sp(10),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (product.isPopular) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(iCaseNavy.copy(alpha = 0.9f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "POPULAR",
                                    fontSize = spec.sp(9.5f),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Out of Stock badge
                if (!product.inStock) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF475569).copy(alpha = 0.92f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OUT OF STOCK",
                            fontSize = spec.sp(9.5f),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(if (spec.isCompactSmall) 10.dp else 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = product.category,
                        fontSize = spec.sp(11.5f),
                        color = iCaseBlue,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (ratingSummary != null && ratingSummary.reviewCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                .testTag("product_rating_pill_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${"%.1f".format(ratingSummary.averageRating)} (${ratingSummary.reviewCount})",
                                fontSize = spec.sp(10),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = product.name,
                    fontSize = spec.sp(14.5f),
                    lineHeight = spec.sp(19),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val stackAction = maxWidth < 148.dp
                    if (stackAction) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "₹${"%.0f".format(product.price)}",
                                    fontSize = spec.sp(16),
                                    fontWeight = FontWeight.Black,
                                    color = iCaseRed,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                if (product.originalPrice > product.price) {
                                    Text(
                                        text = "₹${"%.0f".format(product.originalPrice)}",
                                        fontSize = spec.sp(12),
                                        color = Color.Gray,
                                        textDecoration = TextDecoration.LineThrough,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            Button(
                                onClick = onAddToCart,
                                enabled = product.inStock,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = iCaseRed,
                                    disabledContainerColor = Color(0xFFCBD5E1)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 38.dp)
                                    .testTag("add_to_cart_${product.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Add",
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (product.inStock) "Add" else "Sold Out",
                                    fontSize = spec.sp(12),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "₹${"%.0f".format(product.price)}",
                                    fontSize = spec.sp(17),
                                    fontWeight = FontWeight.Black,
                                    color = iCaseRed,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                if (product.originalPrice > product.price) {
                                    Text(
                                        text = "₹${"%.0f".format(product.originalPrice)}",
                                        fontSize = spec.sp(12),
                                        color = Color.Gray,
                                        textDecoration = TextDecoration.LineThrough,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = onAddToCart,
                                enabled = product.inStock,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = iCaseRed,
                                    disabledContainerColor = Color(0xFFCBD5E1)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 38.dp)
                                    .testTag("add_to_cart_${product.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Add",
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (product.inStock) "Add" else "Sold Out",
                                    fontSize = spec.sp(12.5f),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Customer Support Section with direct Phone, WhatsApp, and Gmail contact.
 */
@Composable
fun CustomerSupportCard(
    modifier: Modifier = Modifier,
    storePhone: String = "9961454059",
    storeWhatsapp: String = "9961454059",
    storeEmail: String = "icaseedappal@gmail.com",
    storeHours: String = "Open Daily 9 AM - 10 PM",
    storeAddress: String = "Edappal, Kerala"
) {
    val context = LocalContext.current
    val spec = LocalResponsiveSpec.current
    val cleanWhatsapp = storeWhatsapp.replace("+", "").replace(" ", "").replace("-", "").let {
        if (it.length == 10) "91$it" else it
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("customer_support_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(spec.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iCaseLightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = "Customer Support",
                        tint = iCaseBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Customer Support & Store",
                        fontSize = spec.sp(16.5f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Text(
                        text = "$storeAddress • $storeHours",
                        fontSize = spec.sp(12.5f),
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Have questions about products, 1-Hour delivery, or device repair bookings? Contact our support team directly:",
                fontSize = spec.sp(13.5f),
                lineHeight = spec.sp(19),
                color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Contact Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$storePhone"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iCaseBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Phone Support",
                        fontSize = spec.sp(11.5f),
                        color = Color.Gray
                    )
                    Text(
                        text = storePhone,
                        fontSize = spec.sp(15.5f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CALL NOW",
                    fontSize = spec.sp(11.5f),
                    fontWeight = FontWeight.Bold,
                    color = iCaseBlue,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // WhatsApp Contact Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F9EE))
                    .clickable {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://api.whatsapp.com/send?phone=$cleanWhatsapp&text=Hello%20iCASE%20Support!%20I%20have%20an%20enquiry.")
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WhatsApp Support",
                        fontSize = spec.sp(11.5f),
                        color = Color(0xFF1E7E34)
                    )
                    Text(
                        text = storeWhatsapp,
                        fontSize = spec.sp(15.5f),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14532D),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CHAT",
                    fontSize = spec.sp(11.5f),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A),
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gmail Contact Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEE2E2).copy(alpha = 0.6f))
                    .clickable {
                        try {
                            val intent = Intent(
                                Intent.ACTION_SENDTO,
                                Uri.parse("mailto:$storeEmail?subject=Customer%20Support%20Enquiry%20-%20iCASE")
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iCaseRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Official Gmail",
                        fontSize = spec.sp(11.5f),
                        color = Color(0xFF991B1B)
                    )
                    Text(
                        text = storeEmail,
                        fontSize = spec.sp(14.5f),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7F1D1D),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EMAIL",
                    fontSize = spec.sp(11.5f),
                    fontWeight = FontWeight.Bold,
                    color = iCaseRed,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * Bottom Brand Footer matching the reference image:
 * Deep navy curved banner with iCASE logo, "Trusted Service", "Fast Delivery", "Best Quality Products",
 * and "Shop Smart... Live Better..." script slogan.
 */
@Composable
fun BrandFooterBanner(
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = iCaseNavy,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spec.horizontalPadding, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ICaseLogo(isDarkBackground = true, scale = 1.05f)

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    FooterBadgeItem(
                        icon = Icons.Default.CheckCircle,
                        tint = Color.White,
                        label = "Trusted\nService",
                        modifier = Modifier.weight(1f)
                    )
                    FooterBadgeItem(
                        icon = Icons.Default.TwoWheeler,
                        tint = Color.White,
                        label = "Fast\nDelivery",
                        modifier = Modifier.weight(1f)
                    )
                    FooterBadgeItem(
                        icon = Icons.Default.Star,
                        tint = iCaseGold,
                        label = "Best Quality\nProducts",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.widthIn(max = 500.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = iCaseRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Shop Smart... Live Better...",
                        fontSize = spec.sp(17),
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterBadgeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = spec.sp(11),
            lineHeight = spec.sp(14),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
