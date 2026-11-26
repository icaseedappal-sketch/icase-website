package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.JsonHelper
import com.example.data.ProductColorOption
import com.example.data.ProductEntity
import com.example.data.ProductRatingSummary
import com.example.data.ReviewEntity
import com.example.data.ReviewRatingHelper
import com.example.ui.AppViewModel
import com.example.ui.Screen
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.ResponsiveSpec
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGold
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseLightBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductDetailScreen(
    product: ProductEntity,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateToRepair: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val spec = LocalResponsiveSpec.current
    val allProducts by viewModel.allProducts.collectAsState()
    val liveProduct = remember(allProducts, product) {
        allProducts.firstOrNull { it.id == product.id } ?: product
    }

    val storePhone by viewModel.storePhone.collectAsState()
    val storeEmail by viewModel.storeEmail.collectAsState()
    val publicProductReviews by viewModel.publicProductReviews.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    val productReviews = remember(publicProductReviews, liveProduct.id, liveProduct.name) {
        ReviewRatingHelper.matchingReviews(publicProductReviews, liveProduct.id, liveProduct.name)
    }
    val ratingSummary = remember(publicProductReviews, liveProduct.id, liveProduct.name) {
        ReviewRatingHelper.summarizeProductRating(publicProductReviews, liveProduct.id, liveProduct.name)
    }
    var showWriteReviewDialog by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(1) }
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val isFavorite = wishlistIds.contains(liveProduct.id)

    val colorOptions = remember(liveProduct.colorOptionsJson) {
        JsonHelper.parseColorOptions(liveProduct.colorOptionsJson)
    }
    var selectedColorIndex by remember(liveProduct.id) { mutableIntStateOf(0) }
    val safeColorIndex = if (colorOptions.isNotEmpty()) {
        selectedColorIndex.coerceIn(0, colorOptions.lastIndex)
    } else {
        0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("product_detail_screen")
    ) {
        // Detail Header with safe drawing top/horizontal insets
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = iCaseNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Product Details",
                        color = iCaseNavy,
                        fontSize = spec.sp(18),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        val added = viewModel.toggleWishlist(liveProduct.id)
                        scope.launch {
                            val msg = if (added) "Added ${liveProduct.name} to Wishlist ❤️" else "Removed from Wishlist"
                            snackbarHostState.showSnackbar(msg)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("detail_wishlist_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = iCaseRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (spec.isExpanded) {
            // Two-Pane Canonical Layout on Expanded Tablets/Desktops
            ResponsiveCenteredBox(
                maxWidth = spec.browseMaxWidth,
                modifier = Modifier
                    .weight(1f)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spec.horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Box(modifier = Modifier.weight(0.95f)) {
                        ProductHeroImageCard(
                            liveProduct = liveProduct,
                            spec = spec,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1.05f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        ProductDetailContentBody(
                            liveProduct = liveProduct,
                            colorOptions = colorOptions,
                            safeColorIndex = safeColorIndex,
                            onSelectColor = { selectedColorIndex = it },
                            quantity = quantity,
                            onDecreaseQty = { if (quantity > 1) quantity-- },
                            onIncreaseQty = { if (quantity < liveProduct.stockCount) quantity++ },
                            onAddToCart = {
                                viewModel.addToCart(liveProduct, quantity)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Added $quantity x ${liveProduct.name} to cart")
                                }
                            },
                            onBuyNow = {
                                viewModel.addToCart(liveProduct, quantity)
                                viewModel.navigateTo(Screen.Cart)
                            },
                            onNavigateToRepair = onNavigateToRepair,
                            storePhone = storePhone,
                            storeEmail = storeEmail,
                            onCallSupport = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$storePhone"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            ratingSummary = ratingSummary,
                            productReviews = productReviews,
                            onWriteReviewClick = { showWriteReviewDialog = true },
                            spec = spec
                        )
                    }
                }
            }
        } else {
            // Single-Column Adaptive Layout for Compact & Medium screens
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                        )
                    )
            ) {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spec.horizontalPadding)
                    ) {
                        ProductHeroImageCard(
                            liveProduct = liveProduct,
                            spec = spec,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(spec.detailHeroHeight)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProductDetailContentBody(
                            liveProduct = liveProduct,
                            colorOptions = colorOptions,
                            safeColorIndex = safeColorIndex,
                            onSelectColor = { selectedColorIndex = it },
                            quantity = quantity,
                            onDecreaseQty = { if (quantity > 1) quantity-- },
                            onIncreaseQty = { if (quantity < liveProduct.stockCount) quantity++ },
                            onAddToCart = {
                                viewModel.addToCart(liveProduct, quantity)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Added $quantity x ${liveProduct.name} to cart")
                                }
                            },
                            onBuyNow = {
                                viewModel.addToCart(liveProduct, quantity)
                                viewModel.navigateTo(Screen.Cart)
                            },
                            onNavigateToRepair = onNavigateToRepair,
                            storePhone = storePhone,
                            storeEmail = storeEmail,
                            onCallSupport = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$storePhone"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            ratingSummary = ratingSummary,
                            productReviews = productReviews,
                            onWriteReviewClick = { showWriteReviewDialog = true },
                            spec = spec
                        )
                    }
                }
            }
        }
    }

    if (showWriteReviewDialog) {
        WriteProductReviewDialog(
            productName = liveProduct.name,
            defaultCustomerName = customerProfile.name,
            onDismiss = { showWriteReviewDialog = false },
            onSubmit = { refId, customerName, rating, comment, onError ->
                viewModel.submitProductReview(
                    productId = liveProduct.id,
                    productName = liveProduct.name,
                    referenceId = refId,
                    customerName = customerName,
                    rating = rating,
                    comment = comment
                ) { res ->
                    res.onSuccess {
                        showWriteReviewDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Review submitted! Visible once approved by Admin.")
                        }
                    }.onFailure { err ->
                        onError(err.message ?: "Could not submit review.")
                    }
                }
            }
        )
    }
}

@Composable
private fun ProductHeroImageCard(
    liveProduct: ProductEntity,
    spec: ResponsiveSpec,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            com.example.ui.components.PouchThumbImage(
                imageUrl = liveProduct.imageUrl,
                contentDescription = liveProduct.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // "Best Seller" red badge on bottom-left
            Box(
                modifier = Modifier
                    .padding(14.dp)
                    .align(Alignment.BottomStart)
                    .clip(CircleShape)
                    .background(iCaseRed)
                    .size(if (spec.isCompactSmall) 50.dp else 56.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Best",
                        fontSize = spec.sp(10.5f),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = spec.sp(12)
                    )
                    Text(
                        text = "Seller",
                        fontSize = spec.sp(10.5f),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = spec.sp(12)
                    )
                }
            }

            // Top Badges Row (prevents overlap on narrow devices)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (liveProduct.isOneHourEligible) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(iCaseRed)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "1-Hour Delivery",
                                fontSize = spec.sp(11),
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

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (liveProduct.inStock) iCaseGreen else Color.Gray)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (liveProduct.inStock) "In Stock" else "Out of Stock",
                        fontSize = spec.sp(11),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailContentBody(
    liveProduct: ProductEntity,
    colorOptions: List<ProductColorOption>,
    safeColorIndex: Int,
    onSelectColor: (Int) -> Unit,
    quantity: Int,
    onDecreaseQty: () -> Unit,
    onIncreaseQty: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    onNavigateToRepair: () -> Unit,
    storePhone: String,
    storeEmail: String,
    onCallSupport: () -> Unit,
    ratingSummary: ProductRatingSummary,
    productReviews: List<ReviewEntity>,
    onWriteReviewClick: () -> Unit,
    spec: ResponsiveSpec
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = liveProduct.name,
            fontSize = spec.sp(22),
            lineHeight = spec.sp(28),
            fontWeight = FontWeight.Bold,
            color = iCaseNavy
        )

        Spacer(modifier = Modifier.height(3.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Premium Quality • ${liveProduct.category}",
                fontSize = spec.sp(14),
                color = Color(0xFF64748B)
            )
            if (ratingSummary.reviewCount > 0) {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("product_detail_rating_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${"%.1f".format(ratingSummary.averageRating)} (${ratingSummary.reviewCount})",
                            fontSize = spec.sp(12),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pricing Row
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "₹${"%,.0f".format(liveProduct.price)}",
                fontSize = spec.sp(26),
                fontWeight = FontWeight.Black,
                color = iCaseRed
            )
            if (liveProduct.originalPrice > liveProduct.price) {
                Text(
                    text = "₹${"%,.0f".format(liveProduct.originalPrice)}",
                    fontSize = spec.sp(16),
                    color = Color.Gray,
                    textDecoration = TextDecoration.LineThrough,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                val discount = ((liveProduct.originalPrice - liveProduct.price) / liveProduct.originalPrice * 100).toInt()
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iCaseRed)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$discount% OFF",
                        fontSize = spec.sp(11),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Dynamic Color Selection Row (wraps cleanly on all screen widths)
        if (colorOptions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            val activeColor = colorOptions[safeColorIndex]
            Column(modifier = Modifier.testTag("product_color_selector")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Color: ",
                        fontSize = spec.sp(14),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Text(
                        text = activeColor.name,
                        fontSize = spec.sp(14),
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEachIndexed { index, option ->
                        val isSelected = safeColorIndex == index
                        val parsedColor = remember(option.hex) {
                            try {
                                Color(android.graphics.Color.parseColor(option.hex))
                            } catch (_: Exception) {
                                Color(0xFF1E293B)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) iCaseNavy else Color(0xFFCBD5E1),
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .clickable { onSelectColor(index) }
                                .testTag("product_color_option_$index")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Responsive Quantity Stepper + Add to Cart
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val stackControls = maxWidth < 300.dp
            if (stackControls) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuantityStepperPill(
                        quantity = quantity,
                        onDecrease = onDecreaseQty,
                        onIncrease = onIncreaseQty,
                        spec = spec
                    )
                    Button(
                        onClick = onAddToCart,
                        enabled = liveProduct.inStock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = iCaseRed,
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("detail_add_to_cart")
                    ) {
                        Text(
                            text = if (liveProduct.inStock) "Add to Cart" else "Out of Stock",
                            fontSize = spec.sp(15),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuantityStepperPill(
                        quantity = quantity,
                        onDecrease = onDecreaseQty,
                        onIncrease = onIncreaseQty,
                        spec = spec
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onAddToCart,
                        enabled = liveProduct.inStock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = iCaseRed,
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("detail_add_to_cart")
                    ) {
                        Text(
                            text = if (liveProduct.inStock) "Add to Cart" else "Out of Stock",
                            fontSize = spec.sp(15),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Full width Dark Navy Pill Button: Buy Now
        Button(
            onClick = onBuyNow,
            enabled = liveProduct.inStock,
            colors = ButtonDefaults.buttonColors(
                containerColor = iCaseNavy,
                disabledContainerColor = Color(0xFF94A3B8)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("detail_buy_now")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (liveProduct.inStock) "Buy Now →" else "Currently Unavailable",
                    fontSize = spec.sp(15),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Delivery Assurance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(spec.cardPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = iCaseBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "iCASE Guarantee & Express Delivery",
                        fontSize = spec.sp(13.5f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (liveProduct.isOneHourEligible)
                        "• Eligible for Express 1-Hour local home delivery.\n• 100% Genuine guaranteed with 6 months warranty.\n• Doorstep inspection before payment."
                    else
                        "• Standard Local Delivery (Arrives within 2-4 hours locally).\n• 100% Genuine guaranteed with 6 months warranty.\n• Doorstep inspection before payment.",
                    fontSize = spec.sp(12.5f),
                    color = Color(0xFF475569),
                    lineHeight = spec.sp(18)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Product Details",
            fontSize = spec.sp(17),
            fontWeight = FontWeight.Bold,
            color = iCaseNavy
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = liveProduct.description,
            fontSize = spec.sp(14),
            color = Color(0xFF334155),
            lineHeight = spec.sp(22)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cross-sell Repair
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToRepair() },
            colors = CardDefaults.cardColors(containerColor = iCaseLightBlue.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = iCaseBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Need Device Repair Instead?",
                        fontSize = spec.sp(15),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Text(
                        text = "Free doorstep pickup & certified technician service",
                        fontSize = spec.sp(12),
                        color = Color(0xFF475569)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Book Now →",
                    fontSize = spec.sp(13),
                    fontWeight = FontWeight.Bold,
                    color = iCaseBlue,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Customer Ratings & Product Reviews Section
        ProductReviewsCard(
            ratingSummary = ratingSummary,
            reviews = productReviews,
            onWriteReviewClick = onWriteReviewClick,
            spec = spec
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Customer Support Help note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
                .clickable { onCallSupport() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = iCaseBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Customer Support: Call $storePhone or email $storeEmail",
                fontSize = spec.sp(13),
                color = Color(0xFF334155),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductReviewsCard(
    ratingSummary: ProductRatingSummary,
    reviews: List<ReviewEntity>,
    onWriteReviewClick: () -> Unit,
    spec: ResponsiveSpec
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_detail_reviews_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    Text(
                        text = "Customer Ratings & Reviews",
                        fontSize = spec.sp(16),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    if (ratingSummary.reviewCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = iCaseGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${"%.1f".format(ratingSummary.averageRating)} / 5 • ${ratingSummary.reviewCount} review${if (ratingSummary.reviewCount == 1) "" else "s"}",
                                fontSize = spec.sp(13),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                        }
                    } else {
                        Text(
                            text = "No customer reviews published yet",
                            fontSize = spec.sp(12),
                            color = Color(0xFF64748B)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onWriteReviewClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .testTag("write_product_review_button")
                ) {
                    Text(
                        text = "Write Review ★",
                        fontSize = spec.sp(12.5f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                }
            }

            if (reviews.isNotEmpty()) {
                reviews.take(10).forEach { review ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = review.customerName,
                                    fontSize = spec.sp(13),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (review.isVerified) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Purchase",
                                        tint = iCaseGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { idx ->
                                    Icon(
                                        imageVector = if (idx < review.rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                        contentDescription = null,
                                        tint = if (idx < review.rating) iCaseGold else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = review.comment,
                            fontSize = spec.sp(13),
                            color = Color(0xFF334155),
                            lineHeight = spec.sp(18)
                        )
                        if (review.adminReply.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(iCaseLightBlue)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "iCASE Reply: ${review.adminReply}",
                                    fontSize = spec.sp(11.5f),
                                    color = iCaseNavy,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            text = "${if (review.isVerified) "Verified Purchase • " else ""}${dateFormat.format(Date(review.createdAt))}",
                            fontSize = spec.sp(10.5f),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WriteProductReviewDialog(
    productName: String,
    defaultCustomerName: String,
    onDismiss: () -> Unit,
    onSubmit: (
        referenceId: String,
        customerName: String,
        rating: Int,
        comment: String,
        onError: (String) -> Unit
    ) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var rating by remember { mutableIntStateOf(5) }
    var customerName by remember { mutableStateOf(defaultCustomerName) }
    var referenceId by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Rate & Review", fontWeight = FontWeight.Bold, color = iCaseNavy)
                Text(productName, fontSize = spec.sp(12.5f), color = iCaseBlue, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Your Star Rating", fontSize = spec.sp(12.5f), fontWeight = FontWeight.Bold, color = iCaseNavy)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { rating = star },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("product_review_star_$star")
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$star Star",
                                tint = if (star <= rating) iCaseGold else Color(0xFFCBD5E1),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_review_name_input")
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Write your experience *") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_review_comment_input")
                )
                OutlinedTextField(
                    value = referenceId,
                    onValueChange = { referenceId = it },
                    label = { Text("Order ID (Optional, e.g. ORD-12345)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_review_ref_input")
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        fontSize = spec.sp(12),
                        color = iCaseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (comment.isBlank()) {
                        errorMsg = "Please write a short review comment."
                        return@Button
                    }
                    errorMsg = null
                    onSubmit(referenceId, customerName, rating, comment) { msg ->
                        errorMsg = msg
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("submit_product_review_button")
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun QuantityStepperPill(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    spec: ResponsiveSpec
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(
            onClick = onDecrease,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
        }
        Text(
            text = "$quantity",
            fontSize = spec.sp(15),
            fontWeight = FontWeight.Bold,
            color = iCaseNavy,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        IconButton(
            onClick = onIncrease,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
        }
    }
}
