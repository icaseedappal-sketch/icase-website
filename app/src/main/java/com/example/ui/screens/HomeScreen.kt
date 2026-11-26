package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ProductEntity
import com.example.data.ReviewRatingHelper
import com.example.data.ScreenGuardCatalog
import com.example.ui.AppViewModel
import com.example.ui.Screen
import com.example.ui.components.BrandFooterBanner
import com.example.ui.components.CustomerSupportCard
import com.example.ui.components.MediaVideoPlayerDialog
import com.example.ui.components.PouchThumbImage
import com.example.ui.components.ProductCard
import com.example.ui.components.VerifiedPublicReviewsSection
import com.example.ui.components.VerifiedReviewDialog
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseGold
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import com.example.ui.theme.iCaseWhatsApp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconColor: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onProductClick: (ProductEntity) -> Unit,
    onNavigateToRepair: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val spec = LocalResponsiveSpec.current
    val allProducts by viewModel.allProducts.collectAsState()
    val popularProducts by viewModel.popularProducts.collectAsState()
    val isOneHourGloballyEnabled by viewModel.isOneHourDeliveryGloballyEnabled.collectAsState()
    val storePhone by viewModel.storePhone.collectAsState()
    val storeWhatsapp by viewModel.storeWhatsapp.collectAsState()
    val storeEmail by viewModel.storeEmail.collectAsState()
    val storeHours by viewModel.storeHours.collectAsState()
    val storeAddress by viewModel.storeAddress.collectAsState()
    val publicProductReviews by viewModel.publicProductReviews.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    val screenGuardConfig by viewModel.screenGuardConfig.collectAsState()
    val homeFeaturedPouches = remember(screenGuardConfig) {
        ScreenGuardCatalog.homeFeaturedPouches(screenGuardConfig)
    }
    var selectedHomePouchColors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var activeVideoPreview by remember { mutableStateOf<Pair<String, String>?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val filteredProducts = allProducts.filter { prod ->
        if (searchQuery.isBlank()) true else {
            prod.name.contains(searchQuery, ignoreCase = true) || prod.description.contains(searchQuery, ignoreCase = true)
        }
    }

    val quickActions = listOf(
        QuickActionItem(
            title = "Shop",
            subtitle = "Browse & Buy",
            icon = Icons.Default.ShoppingBag,
            iconBgColor = Color(0xFFFEF3C7),
            iconColor = Color(0xFFD97706),
            onClick = { viewModel.navigateTo(Screen.Shop) }
        ),
        QuickActionItem(
            title = "Repair",
            subtitle = "Get Expert Service",
            icon = Icons.Default.Build,
            iconBgColor = Color(0xFFE0E7FF),
            iconColor = Color(0xFF4338CA),
            onClick = { onNavigateToRepair() }
        ),
        QuickActionItem(
            title = "Tempered Glass",
            subtitle = "Screen Guards & Lens",
            icon = Icons.Default.Shield,
            iconBgColor = Color(0xFFFFECEE),
            iconColor = iCaseRed,
            onClick = { viewModel.navigateTo(Screen.ScreenGuards("TEMPERED_GLASS")) }
        ),
        QuickActionItem(
            title = "Track Order",
            subtitle = "Know Your Order",
            icon = Icons.Default.Search,
            iconBgColor = Color(0xFFDCFCE7),
            iconColor = Color(0xFF15803D),
            onClick = { viewModel.navigateTo(Screen.TrackOrder) }
        ),
        QuickActionItem(
            title = "Account",
            subtitle = "Profile & Sync",
            icon = Icons.Default.Person,
            iconBgColor = Color(0xFFF1F5F9),
            iconColor = Color(0xFF0F172A),
            onClick = { viewModel.navigateTo(Screen.Profile) }
        ),
        QuickActionItem(
            title = "WhatsApp",
            subtitle = "Order Now",
            icon = Icons.Default.Share,
            iconBgColor = Color(0xFFE8F9EE),
            iconColor = iCaseWhatsApp,
            onClick = {
                try {
                    val waNumber = storeWhatsapp.replace("+", "").replace(" ", "").replace("-", "").let {
                        if (it.length == 10) "91$it" else it
                    }
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://api.whatsapp.com/send?phone=$waNumber&text=Hello%20iCASE!%20I%20would%20like%20to%20order%20mobile%20accessories.")
                    )
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        )
    )

    val topBrands = listOf("Apple", "SAMSUNG", "Xiaomi", "oppo", "vivo", "realme")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = if (spec.useNavRail) 24.dp else 96.dp)
    ) {
        // Search Bar
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search for products...",
                                fontSize = spec.sp(14),
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = iCaseRed,
                            unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("home_search_input")
                    )
                }
            }
        }

        // Hero Banner (dynamically scales height cleanly with text & fontScale)
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (spec.isCompactSmall) 164.dp else 180.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(iCaseNavy, Color(0xFF162544), iCaseRed.copy(alpha = 0.9f))
                                )
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.banner_hero_1794309422038),
                            contentDescription = "iCASE Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            iCaseNavy.copy(alpha = 0.95f),
                                            iCaseNavy.copy(alpha = 0.80f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spec.cardPadding),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Mobile Accessories &",
                                fontSize = spec.sp(17),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Repair Service",
                                fontSize = spec.sp(21),
                                fontWeight = FontWeight.Black,
                                color = iCaseGold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Quality Products. Reliable Service.",
                                fontSize = spec.sp(12.5f),
                                color = Color(0xFFBAC7D5)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.navigateTo(Screen.Shop) },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseGold),
                                shape = RoundedCornerShape(22.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .testTag("explore_shop_button")
                            ) {
                                Text(
                                    text = "Explore Shop →",
                                    fontSize = spec.sp(12.5f),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        // Responsive Quick Action Grid (2 cols on ultra-small, 3 cols on phone, 6 cols on tablet/wide)
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 12.dp)
                ) {
                    quickActions.chunked(spec.quickActionColumns).forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { action ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = if (spec.isCompactSmall) 94.dp else 98.dp)
                                        .clickable { action.onClick() }
                                        .testTag("action_${action.title.lowercase().replace(" & ", "_").replace(" ", "_")}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(action.iconBgColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = action.icon,
                                                contentDescription = action.title,
                                                tint = action.iconColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Text(
                                            text = action.title,
                                            fontSize = spec.sp(11f),
                                            lineHeight = spec.sp(13),
                                            fontWeight = FontWeight.Bold,
                                            color = iCaseNavy,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = action.subtitle,
                                            fontSize = spec.sp(9.5f),
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            repeat(spec.quickActionColumns - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Screen & Camera Protection Sample Cards (Tempered Glass, Camera Lens Protector & Pouches) + Dynamic Tempered Glass Types
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                val startingGlassPrice = remember(screenGuardConfig) {
                    ScreenGuardCatalog.startingTemperedGlassPrice(screenGuardConfig).roundToInt()
                }
                val startingLensPrice = remember(screenGuardConfig) {
                    ScreenGuardCatalog.startingCameraLensPrice(screenGuardConfig).roundToInt()
                }
                val startingPouchPrice = remember(screenGuardConfig) {
                    ScreenGuardCatalog.startingPouchPrice(screenGuardConfig).roundToInt()
                }
                val camSectionActive = remember(screenGuardConfig) {
                    ScreenGuardCatalog.isCameraLensSectionAvailable(screenGuardConfig)
                }
                val glassImg = remember(screenGuardConfig.temperedGlassImageUrl) {
                    ScreenGuardCatalog.resolveTemperedGlassImageUrl(screenGuardConfig)
                }
                val camImg = remember(screenGuardConfig.cameraLensImageUrl) {
                    ScreenGuardCatalog.resolveCameraLensImageUrl(screenGuardConfig)
                }
                val pouchCardImg = remember(screenGuardConfig.pouchCardImageUrl) {
                    ScreenGuardCatalog.resolvePouchCardImageUrl(screenGuardConfig)
                }
                val activeGlassTypes = remember(screenGuardConfig) {
                    ScreenGuardCatalog.activeGlassTypes(screenGuardConfig)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 6.dp)
                        .testTag("home_guard_sample_cards_section")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛡️ Screen, Lens & Pouch Protection",
                                fontSize = spec.sp(17),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Tap a card or glass style to choose your phone model",
                                fontSize = spec.sp(11.5f),
                                color = Color(0xFF64748B)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explore All →",
                            fontSize = spec.sp(13),
                            fontWeight = FontWeight.Bold,
                            color = iCaseRed,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(Screen.ScreenGuards()) }
                                .padding(vertical = 4.dp)
                        )
                    }

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val stackCards = maxWidth < 340.dp
                        val threeColumns = maxWidth >= 560.dp
                        when {
                            threeColumns -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    HomeGuardSampleCard(
                                        title = "Tempered Glass",
                                        imageUrl = glassImg,
                                        priceLabel = "Starting ₹$startingGlassPrice",
                                        compatibilityText = "Available for iPhone & All Android Models",
                                        compatibilityBg = Color(0xFFE0F2FE),
                                        compatibilityFg = Color(0xFF0369A1),
                                        subtitle = "Privacy, Matte, Borderless, 360 & Normal 5D",
                                        ctaLabel = "Select Glass →",
                                        onClick = { viewModel.navigateTo(Screen.ScreenGuards("TEMPERED_GLASS")) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("home_sample_card_tempered_glass")
                                    )
                                    HomeGuardSampleCard(
                                        title = "Camera Lens Protector",
                                        imageUrl = camImg,
                                        priceLabel = if (camSectionActive) "Starting ₹$startingLensPrice" else "Paused",
                                        compatibilityText = "iPhone & Samsung S Series Only",
                                        compatibilityBg = Color(0xFFFEF3C7),
                                        compatibilityFg = Color(0xFF92400E),
                                        subtitle = "9H Sapphire Rings • Galaxy S20–S25 & iPhone",
                                        ctaLabel = "Select Lens →",
                                        onClick = { viewModel.navigateTo(Screen.ScreenGuards("CAMERA_LENS")) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("home_sample_card_camera_lens")
                                    )
                                    HomeGuardSampleCard(
                                        title = "Pouches & Back Cases",
                                        imageUrl = pouchCardImg,
                                        priceLabel = "Starting ₹$startingPouchPrice",
                                        compatibilityText = "iPhone, Samsung S Series & Other Android Models",
                                        compatibilityBg = Color(0xFFDCFCE7),
                                        compatibilityFg = Color(0xFF166534),
                                        subtitle = "MagSafe Clear, Matte Armor & Velvet Silicone Cases",
                                        ctaLabel = "Select Pouch →",
                                        onClick = { viewModel.navigateTo(Screen.ScreenGuards("POUCHES")) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("home_sample_card_pouches")
                                    )
                                }
                            }
                            stackCards -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    HomeGuardSampleCard(
                                        title = "Tempered Glass",
                                        imageUrl = glassImg,
                                        priceLabel = "Starting ₹$startingGlassPrice",
                                        compatibilityText = "Available for iPhone & All Android Models",
                                        compatibilityBg = Color(0xFFE0F2FE),
                                        compatibilityFg = Color(0xFF0369A1),
                                        subtitle = "Privacy, Matte, Borderless, 360 & Normal 5D",
                                        ctaLabel = "Select Glass →",
                                        onClick = { viewModel.navigateTo(Screen.ScreenGuards("TEMPERED_GLASS")) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("home_sample_card_tempered_glass")
                                    )
                                    HomeGuardSampleCard(
                                        title = "Camera Lens Protector",
                                        imageUrl = camImg,
                                        priceLabel = if (camSectionActive) "Starting ₹$startingLensPrice" else "Paused",
                                        compatibilityText = "iPhone & Samsung S Series Only",
                                        compatibilityBg = Color(0xFFFEF3C7),
                                        compatibilityFg = Color(0xFF92400E),
                                        subtitle = "9H Sapphire Rings • Supported: iPhone & Galaxy S20–S25 / Ultra",
                                        ctaLabel = "Select Lens →",
                                        onClick = { viewModel.navigateTo(Screen.ScreenGuards("CAMERA_LENS")) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("home_sample_card_camera_lens")
                                    )
                                    HomeGuardSampleCard(
                                        title = "Pouches & Back Cases",
                                        imageUrl = pouchCardImg,
                                        priceLabel = "Starting ₹$startingPouchPrice",
                                        compatibilityText = "iPhone, Samsung S Series & Other Android Models",
                                        compatibilityBg = Color(0xFFDCFCE7),
                                        compatibilityFg = Color(0xFF166534),
                                        subtitle = "MagSafe Clear, Matte Armor & Velvet Silicone Cases",
                                        ctaLabel = "Select Pouch →",
                                        onClick = { viewModel.navigateTo(Screen.ScreenGuards("POUCHES")) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("home_sample_card_pouches")
                                    )
                                }
                            }
                            else -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        HomeGuardSampleCard(
                                            title = "Tempered Glass",
                                            imageUrl = glassImg,
                                            priceLabel = "Starting ₹$startingGlassPrice",
                                            compatibilityText = "Available for iPhone & All Android Models",
                                            compatibilityBg = Color(0xFFE0F2FE),
                                            compatibilityFg = Color(0xFF0369A1),
                                            subtitle = "Privacy, Matte, Borderless, 360 & Normal 5D",
                                            ctaLabel = "Select Glass →",
                                            onClick = { viewModel.navigateTo(Screen.ScreenGuards("TEMPERED_GLASS")) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("home_sample_card_tempered_glass")
                                        )
                                        HomeGuardSampleCard(
                                            title = "Camera Lens Protector",
                                            imageUrl = camImg,
                                            priceLabel = if (camSectionActive) "Starting ₹$startingLensPrice" else "Paused",
                                            compatibilityText = "iPhone & Samsung S Series Only",
                                            compatibilityBg = Color(0xFFFEF3C7),
                                            compatibilityFg = Color(0xFF92400E),
                                            subtitle = "9H Sapphire Rings • Galaxy S20–S25 & iPhone",
                                            ctaLabel = "Select Lens →",
                                            onClick = { viewModel.navigateTo(Screen.ScreenGuards("CAMERA_LENS")) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("home_sample_card_camera_lens")
                                        )
                                    }
                                    HomeGuardSampleCard(
                                        title = "Pouches & Back Cases",
                                        imageUrl = pouchCardImg,
                                        priceLabel = "Starting ₹$startingPouchPrice",
                                        compatibilityText = "iPhone, Samsung S Series & Other Android Models",
                                        compatibilityBg = Color(0xFFDCFCE7),
                                        compatibilityFg = Color(0xFF166534),
                                        subtitle = "MagSafe Clear, Matte Armor & Velvet Silicone Cases",
                                        ctaLabel = "Select Pouch →",
                                        onClick = { viewModel.navigateTo(Screen.ScreenGuards("POUCHES")) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("home_sample_card_pouches")
                                    )
                                }
                            }
                        }
                    }

                    if (activeGlassTypes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tempered Glass Styles (Quick Select)",
                                    fontSize = spec.sp(14.5f),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy
                                )
                                Text(
                                    text = "Tap any style to open Tempered Glass with it pre-selected",
                                    fontSize = spec.sp(11),
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.testTag("home_glass_type_examples_row")
                        ) {
                            items(activeGlassTypes, key = { it }) { glassType ->
                                val pricing = ScreenGuardCatalog.resolveTemperedGlassPrice("", "", glassType, screenGuardConfig)
                                val note = ScreenGuardCatalog.glassTypeHelperNote(glassType, screenGuardConfig)
                                val typeImage = ScreenGuardCatalog.resolveGlassTypeImageUrl(glassType, screenGuardConfig)
                                val typeVideo = ScreenGuardCatalog.resolveGlassTypeVideoUrl(glassType, screenGuardConfig)
                                Card(
                                    modifier = Modifier
                                        .width(178.dp)
                                        .clickable {
                                            viewModel.navigateTo(
                                                Screen.ScreenGuards(
                                                    initialSection = "TEMPERED_GLASS",
                                                    initialGlassType = glassType
                                                )
                                            )
                                        }
                                        .testTag("home_glass_type_card_${glassType.lowercase().replace(Regex("[^a-z0-9]"), "_")}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PouchThumbImage(
                                                imageUrl = typeImage,
                                                contentDescription = glassType,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFF1F5F9))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = glassType,
                                                    fontSize = spec.sp(12.5f),
                                                    fontWeight = FontWeight.Bold,
                                                    color = iCaseNavy,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    lineHeight = spec.sp(15)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "₹${pricing.finalPrice.roundToInt()}",
                                                        fontSize = spec.sp(13),
                                                        fontWeight = FontWeight.Black,
                                                        color = iCaseRed
                                                    )
                                                    if (pricing.basePrice > pricing.finalPrice) {
                                                        Text(
                                                            text = "₹${pricing.basePrice.roundToInt()}",
                                                            fontSize = spec.sp(10),
                                                            color = Color(0xFF94A3B8),
                                                            textDecoration = TextDecoration.LineThrough
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = note,
                                            fontSize = spec.sp(10.5f),
                                            color = Color(0xFF475569),
                                            maxLines = 2,
                                            minLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = spec.sp(14)
                                        )
                                        if (pricing.offerBadge.isNotBlank() || typeVideo.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (pricing.offerBadge.isNotBlank()) {
                                                    Surface(
                                                        color = Color(0xFFDCFCE7),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = pricing.offerBadge,
                                                            fontSize = spec.sp(9.5f),
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF15803D),
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                            maxLines = 1
                                                        )
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.width(1.dp))
                                                }
                                                if (typeVideo.isNotBlank()) {
                                                    Surface(
                                                        color = Color(0xFF0F172A),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.clickable {
                                                            activeVideoPreview = glassType to typeVideo
                                                        }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = "Watch $glassType Video",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Text(
                                                                text = "Video",
                                                                fontSize = spec.sp(9.5f),
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Popular Products Carousel
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🔥 Popular Products",
                            fontSize = spec.sp(17),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View All →",
                            fontSize = spec.sp(13),
                            fontWeight = FontWeight.Bold,
                            color = iCaseRed,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(Screen.Shop) }
                                .padding(vertical = 4.dp)
                                .testTag("view_all_popular")
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = spec.horizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(popularProducts) { product ->
                            val ratingSummary = remember(publicProductReviews, product.id, product.name) {
                                ReviewRatingHelper.summarizeProductRating(
                                    publicProductReviews,
                                    product.id,
                                    product.name
                                )
                            }
                            Box(modifier = Modifier.width(spec.popularCardWidth)) {
                                ProductCard(
                                    product = product,
                                    ratingSummary = ratingSummary,
                                    onClick = { onProductClick(product) },
                                    onAddToCart = {
                                        viewModel.addToCart(product)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Added ${product.name} to cart")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Featured Pouches & Cases Section (Controlled live via Admin 'Show on Home Page' toggle)
        if (homeFeaturedPouches.isNotEmpty()) {
            item {
                ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .testTag("home_featured_pouches_section")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spec.horizontalPadding, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✨ Featured Pouches & Cases",
                                fontSize = spec.sp(17),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Select Model →",
                                fontSize = spec.sp(13),
                                fontWeight = FontWeight.Bold,
                                color = iCaseRed,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier
                                    .clickable { viewModel.navigateTo(Screen.ScreenGuards("POUCHES")) }
                                    .padding(vertical = 4.dp)
                                    .testTag("home_featured_pouches_view_all")
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = spec.horizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(homeFeaturedPouches, key = { it.id }) { pouch ->
                                val pricing = ScreenGuardCatalog.resolvePouchPrice(pouch)
                                val activeColor = ScreenGuardCatalog.resolvePouchSelectedColor(
                                    pouch,
                                    selectedHomePouchColors[pouch.id].orEmpty()
                                )
                                val resolvedImg = ScreenGuardCatalog.resolvePouchImageUrl(
                                    pouch,
                                    activeColor?.name.orEmpty()
                                )
                                Card(
                                    modifier = Modifier
                                        .width(spec.popularCardWidth.coerceAtLeast(188.dp))
                                        .clickable { viewModel.navigateTo(Screen.ScreenGuards("POUCHES")) }
                                        .testTag("home_featured_pouch_${pouch.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(124.dp)
                                                .background(Color(0xFFF1F5F9))
                                        ) {
                                            PouchThumbImage(
                                                imageUrl = resolvedImg,
                                                contentDescription = pouch.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            if (pricing.hasOffer && pricing.offerBadge.isNotBlank()) {
                                                Surface(
                                                    color = iCaseRed,
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(8.dp)
                                                ) {
                                                    Text(
                                                        text = pricing.offerBadge,
                                                        color = Color.White,
                                                        fontSize = spec.sp(10),
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                text = pouch.compatibleBrands.joinToString(" • "),
                                                fontSize = spec.sp(10.5f),
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1E40AF),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = pouch.name,
                                                fontSize = spec.sp(14),
                                                fontWeight = FontWeight.Bold,
                                                color = iCaseNavy,
                                                maxLines = 2,
                                                minLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "₹${pricing.finalPrice.roundToInt()}",
                                                    fontSize = spec.sp(16),
                                                    fontWeight = FontWeight.Black,
                                                    color = iCaseRed
                                                )
                                                if (pricing.hasOffer && pricing.basePrice > pricing.finalPrice) {
                                                    Text(
                                                        text = "₹${pricing.basePrice.roundToInt()}",
                                                        fontSize = spec.sp(11.5f),
                                                        color = Color.Gray,
                                                        textDecoration = TextDecoration.LineThrough
                                                    )
                                                }
                                            }
                                            if (pouch.colorOptions.isNotEmpty()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    pouch.colorOptions.take(5).forEach { col ->
                                                        val isColSelected = activeColor?.name.equals(col.name, ignoreCase = true)
                                                        val swatchColor = remember(col.hex) {
                                                            try {
                                                                Color(android.graphics.Color.parseColor(col.hex))
                                                            } catch (_: Exception) {
                                                                Color(0xFF1E293B)
                                                            }
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                                .background(swatchColor)
                                                                .border(
                                                                    width = if (isColSelected) 2.dp else 1.dp,
                                                                    color = if (isColSelected) iCaseNavy else Color(0xFF94A3B8),
                                                                    shape = CircleShape
                                                                )
                                                                .clickable {
                                                                    selectedHomePouchColors =
                                                                        selectedHomePouchColors + (pouch.id to col.name)
                                                                }
                                                        )
                                                    }
                                                }
                                            }
                                            Button(
                                                onClick = { viewModel.navigateTo(Screen.ScreenGuards("POUCHES")) },
                                                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 40.dp)
                                            ) {
                                                Text(
                                                    text = "Choose Model & Buy",
                                                    fontSize = spec.sp(12),
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Brands
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Top Brands",
                            fontSize = spec.sp(17),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "View All →",
                            fontSize = spec.sp(13),
                            fontWeight = FontWeight.Bold,
                            color = iCaseRed,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(Screen.Shop) }
                                .padding(vertical = 4.dp)
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = spec.horizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(topBrands) { brand ->
                            Card(
                                modifier = Modifier
                                    .clickable { viewModel.navigateTo(Screen.Shop) }
                                    .testTag("home_brand_$brand"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = brand,
                                        fontSize = spec.sp(13),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Best Quality Best Price Crown Banner
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = iCaseNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spec.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Best Quality",
                                fontSize = spec.sp(19),
                                fontWeight = FontWeight.Black,
                                color = iCaseGold
                            )
                            Text(
                                text = "Best Price",
                                fontSize = spec.sp(18),
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Genuine accessories & 6-month repair warranty in Edappal",
                                fontSize = spec.sp(11.5f),
                                color = Color(0xFFBAC7D5),
                                lineHeight = spec.sp(16)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Crown",
                                tint = iCaseGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // All Products Grid Header
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "All Accessories",
                        fontSize = spec.sp(18),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${filteredProducts.size} items",
                        fontSize = spec.sp(13),
                        color = Color.Gray
                    )
                }
            }
        }

        // Responsive Multi-Column Product Grid (2 on phones, 3 on Medium/foldables, 4 on Expanded tablets)
        items(filteredProducts.chunked(spec.gridColumns)) { rowProducts ->
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (spec.isCompactSmall) 8.dp else 12.dp)
                ) {
                    for (product in rowProducts) {
                        val ratingSummary = remember(publicProductReviews, product.id, product.name) {
                            ReviewRatingHelper.summarizeProductRating(
                                publicProductReviews,
                                product.id,
                                product.name
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(
                                product = product,
                                ratingSummary = ratingSummary,
                                onClick = { onProductClick(product) },
                                onAddToCart = {
                                    viewModel.addToCart(product)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Added ${product.name} to cart")
                                    }
                                }
                            )
                        }
                    }
                    repeat(spec.gridColumns - rowProducts.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Verified Customer Reviews (Approved Product Reviews Only)
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 10.dp)
                ) {
                    VerifiedPublicReviewsSection(
                        reviews = publicProductReviews,
                        onWriteReviewClick = { showReviewDialog = true }
                    )
                }
            }
        }

        // Customer Support Section
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 14.dp)
                ) {
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

        // Brand Footer Banner
        item {
            BrandFooterBanner()
        }
    }

    if (showReviewDialog) {
        VerifiedReviewDialog(
            viewModel = viewModel,
            defaultCustomerName = customerProfile.name,
            onDismiss = { showReviewDialog = false },
            onSubmitted = { msg ->
                showReviewDialog = false
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
    }

    activeVideoPreview?.let { (title, url) ->
        MediaVideoPlayerDialog(
            title = title,
            videoUrl = url,
            onDismiss = { activeVideoPreview = null }
        )
    }
}

@Composable
private fun HomeGuardSampleCard(
    title: String,
    imageUrl: String,
    priceLabel: String,
    compatibilityText: String,
    compatibilityBg: Color,
    compatibilityFg: Color,
    subtitle: String,
    ctaLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(Color(0xFFF1F5F9))
            ) {
                PouchThumbImage(
                    imageUrl = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = iCaseRed,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = priceLabel,
                        fontSize = spec.sp(10.5f),
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    fontSize = spec.sp(14.5f),
                    fontWeight = FontWeight.Bold,
                    color = iCaseNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    color = compatibilityBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = compatibilityText,
                        fontSize = spec.sp(10f),
                        fontWeight = FontWeight.Bold,
                        color = compatibilityFg,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = spec.sp(13),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = subtitle,
                    fontSize = spec.sp(11),
                    color = Color(0xFF475569),
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = spec.sp(15)
                )
                Surface(
                    color = iCaseNavy,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 38.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ctaLabel,
                            fontSize = spec.sp(11.5f),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
