package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.GoogleAuthHelper
import com.example.ui.AppViewModel
import com.example.ui.Screen
import com.example.ui.components.BrandFooterBanner
import com.example.ui.components.CustomerSupportCard
import com.example.ui.components.ProfileDetailsDialog
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: AppViewModel,
    @Suppress("UNUSED_PARAMETER") onTrackOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val profile by viewModel.customerProfile.collectAsState()
    val myOrders by viewModel.myOrders.collectAsState()
    val myRepairs by viewModel.myRepairs.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()

    val storeName by viewModel.storeName.collectAsState()
    val storePhone by viewModel.storePhone.collectAsState()
    val storeWhatsapp by viewModel.storeWhatsapp.collectAsState()
    val storeEmail by viewModel.storeEmail.collectAsState()
    val storeAddress by viewModel.storeAddress.collectAsState()
    val storeHours by viewModel.storeHours.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSupportCard by remember { mutableStateOf(false) }
    var authStatusMessage by remember { mutableStateOf<String?>(null) }

    val isProfileBlank = !profile.isLoggedIn &&
        profile.name.isBlank() &&
        profile.phone.isBlank() &&
        !profile.isGoogleSignedIn

    BackHandler(enabled = showSupportCard) {
        showSupportCard = false
    }

    fun triggerGoogleSignIn() {
        authStatusMessage = null
        GoogleAuthHelper.onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            scope = scope,
            onAuthSuccess = { user ->
                viewModel.onGoogleUserAuthenticated(user)
                authStatusMessage = null
            },
            onAuthError = { err ->
                authStatusMessage = err
            }
        )
    }

    if (showEditProfileDialog) {
        ProfileDetailsDialog(
            title = "Edit Profile",
            initialProfile = profile,
            initialName = profile.name,
            initialPhone = profile.phone,
            initialWhatsapp = profile.whatsapp,
            initialAddress = profile.address,
            showAddressField = false,
            requireAddress = false,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, phone, whatsapp, address ->
                viewModel.updateCustomerProfile(name, phone, whatsapp, address)
                authStatusMessage = null
                showEditProfileDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About $storeName", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "$storeName is your trusted mobile accessories & express repair center located at $storeAddress.",
                        fontSize = spec.sp(14),
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "• Phone: $storePhone", fontSize = spec.sp(13), color = iCaseNavy, fontWeight = FontWeight.Medium)
                    Text(text = "• Email: $storeEmail", fontSize = spec.sp(13), color = iCaseNavy, fontWeight = FontWeight.Medium)
                    Text(text = "• Hours: $storeHours", fontSize = spec.sp(13), color = iCaseNavy, fontWeight = FontWeight.Medium)
                    Text(text = "• Express 1-Hour local home delivery", fontSize = spec.sp(13), color = Color.Gray)
                    Text(text = "• Free doorstep repair pickup & return", fontSize = spec.sp(13), color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Got it")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(bottom = if (spec.useNavRail) 24.dp else 100.dp)
    ) {
        // Fresh Unauthenticated / Authenticated Hero Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0B1728),
                                Color(0xFF132238)
                            )
                        )
                    )
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(vertical = if (spec.isCompactSmall) 18.dp else 24.dp, horizontal = spec.horizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val avatarSize = if (spec.isCompactSmall) 64.dp else 76.dp
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = iCaseNavy,
                            modifier = Modifier.size(if (spec.isCompactSmall) 36.dp else 44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = when {
                            profile.name.isNotBlank() -> profile.name
                            profile.email.isNotBlank() -> profile.email.substringBefore("@")
                            profile.phone.isNotBlank() -> profile.phone
                            else -> "Welcome to iCASE"
                        },
                        fontSize = spec.sp(20),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    val subtitleText = when {
                        profile.phone.isNotBlank() && profile.email.isNotBlank() -> "${profile.phone} • ${profile.email}"
                        profile.email.isNotBlank() -> profile.email
                        profile.phone.isNotBlank() -> profile.phone
                        else -> "Sign in with Google to get started"
                    }
                    Text(
                        text = subtitleText,
                        fontSize = spec.sp(13),
                        color = Color(0xFFBAC7D5),
                        textAlign = TextAlign.Center
                    )

                    if (profile.isGoogleSignedIn) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(iCaseGreen.copy(alpha = 0.22f))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Google Signed In",
                                fontSize = spec.sp(11),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF86EFAC)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .clickable { showEditProfileDialog = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("profile_edit_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = iCaseNavy,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Profile",
                                fontSize = spec.sp(13),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy
                            )
                        }

                        if (!profile.isGoogleSignedIn) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(iCaseRed)
                                    .clickable { triggerGoogleSignIn() }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("profile_google_signin_button"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = spec.sp(14),
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google Sign-In",
                                    fontSize = spec.sp(13),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (authStatusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authStatusMessage!!,
                            fontSize = spec.sp(12),
                            color = Color(0xFFFCA5A5),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Dedicated Navigation Menu Cards
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        ProfileMenuItemRow(
                            icon = Icons.Default.ShoppingBag,
                            title = "My Orders",
                            badge = if (myOrders.isNotEmpty()) "${myOrders.size}" else null,
                            modifier = Modifier.testTag("profile_my_orders_row"),
                            onClick = { viewModel.navigateTo(Screen.MyOrders) }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileMenuItemRow(
                            icon = Icons.Default.Build,
                            title = "My Repairs",
                            badge = if (myRepairs.isNotEmpty()) "${myRepairs.size}" else null,
                            modifier = Modifier.testTag("profile_my_repairs_row"),
                            onClick = { viewModel.navigateTo(Screen.MyRepairs) }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileMenuItemRow(
                            icon = Icons.Default.LocationOn,
                            title = "My Addresses",
                            badge = if (profile.address.isNotBlank()) "1" else null,
                            modifier = Modifier.testTag("profile_my_addresses_row"),
                            onClick = { viewModel.navigateTo(Screen.MyAddresses) }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileMenuItemRow(
                            icon = Icons.Default.Favorite,
                            title = "Wishlist",
                            badge = if (wishlistIds.isNotEmpty()) "${wishlistIds.size}" else null,
                            modifier = Modifier.testTag("profile_wishlist_row"),
                            onClick = { viewModel.navigateTo(Screen.Wishlist) }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileMenuItemRow(
                            icon = Icons.Default.HeadsetMic,
                            title = "Support",
                            modifier = Modifier.testTag("profile_support_row"),
                            onClick = { showSupportCard = !showSupportCard }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileMenuItemRow(
                            icon = Icons.Default.Info,
                            title = "About iCASE",
                            modifier = Modifier.testTag("profile_about_row"),
                            onClick = { showAboutDialog = true }
                        )

                        if (!isProfileBlank) {
                            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                            ProfileMenuItemRow(
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                title = "Logout",
                                isDestructive = true,
                                modifier = Modifier.testTag("profile_logout_row"),
                                onClick = {
                                    GoogleAuthHelper.signOut(credentialManager, scope) {
                                        viewModel.clearCustomerProfile()
                                        viewModel.navigateToHome()
                                    }
                                    showSupportCard = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showSupportCard) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Box(modifier = Modifier.padding(horizontal = spec.horizontalPadding)) {
                        CustomerSupportCard(
                            storePhone = storePhone,
                            storeWhatsapp = storeWhatsapp,
                            storeEmail = storeEmail,
                            storeHours = storeHours,
                            storeAddress = storeAddress
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            BrandFooterBanner()
        }
    }
}

@Composable
fun ProfileMenuItemRow(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val spec = LocalResponsiveSpec.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) iCaseRed else iCaseNavy,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = spec.sp(15),
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) iCaseRed else iCaseNavy,
            modifier = Modifier.weight(1f)
        )

        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(iCaseRed)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = spec.sp(11),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = if (isDestructive) iCaseRed.copy(alpha = 0.6f) else Color(0xFF94A3B8),
            modifier = Modifier.size(16.dp)
        )
    }
}
