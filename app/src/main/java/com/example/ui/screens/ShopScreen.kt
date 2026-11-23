package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.data.ReviewRatingHelper
import com.example.ui.AppViewModel
import com.example.ui.components.BrandFooterBanner
import com.example.ui.components.CustomerSupportCard
import com.example.ui.components.ProductCard
import com.example.ui.components.VerifiedPublicReviewsSection
import com.example.ui.components.VerifiedReviewDialog
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseGold
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import kotlinx.coroutines.launch

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

@Composable
fun ShopScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onOpenScreenGuards: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    val allProducts by viewModel.allProducts.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val storePhone by viewModel.storePhone.collectAsState()
    val storeWhatsapp by viewModel.storeWhatsapp.collectAsState()
    val storeEmail by viewModel.storeEmail.collectAsState()
    val storeHours by viewModel.storeHours.collectAsState()
    val storeAddress by viewModel.storeAddress.collectAsState()
    val publicProductReviews by viewModel.publicProductReviews.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    val totalCartCount = cartItems.sumOf { it.quantity }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val categoryList = listOf(
        CategoryItem("Cases", Icons.Default.PhoneAndroid, Color(0xFF1E40AF), Color(0xFFDBEAFE)),
        CategoryItem("Screen Protectors", Icons.Default.Shield, Color(0xFF047857), Color(0xFFD1FAE5)),
        CategoryItem("Fast Chargers", Icons.Default.ElectricalServices, Color(0xFFB45309), Color(0xFFFEF3C7)),
        CategoryItem("Audio", Icons.Default.Headphones, Color(0xFF6D28D9), Color(0xFFEDE9FE)),
        CategoryItem("Cables", Icons.Default.Cable, Color(0xFF0369A1), Color(0xFFE0F2FE)),
        CategoryItem("Accessories", Icons.Default.BatteryChargingFull, Color(0xFFBE123C), Color(0xFFFFE4E6))
    )

    val topBrands = listOf("Apple", "SAMSUNG", "Xiaomi", "oppo", "vivo", "realme")

    val displayedProducts = allProducts.filter { prod ->
        val matchesCategory = if (selectedCategory == "All") true else prod.category.equals(selectedCategory, ignoreCase = true)
        val matchesBrand = if (selectedBrand == null) true else prod.name.contains(selectedBrand!!, ignoreCase = true) || prod.description.contains(selectedBrand!!, ignoreCase = true)
        matchesCategory && matchesBrand
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("shop_categories_screen")
    ) {
        // Top Header with safe drawing insets
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
                            .testTag("categories_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = iCaseNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = if (selectedCategory == "All") "Shop Accessories" else selectedCategory,
                        fontSize = spec.sp(18.5f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("categories_cart_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (totalCartCount > 0) {
                                Badge(
                                    containerColor = iCaseRed,
                                    contentColor = Color.White
                                ) {
                                    Text("$totalCartCount", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = iCaseNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (spec.useNavRail) 24.dp else 96.dp)
        ) {
            // Adaptive Categories Grid (2 cols on phones, 3 cols on tablets)
            item {
                ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                    Column(modifier = Modifier.padding(spec.horizontalPadding)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Explore Categories",
                                fontSize = spec.sp(17),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedCategory != "All" || selectedBrand != null) {
                                Text(
                                    text = "Reset Filter",
                                    fontSize = spec.sp(13),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseRed,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier
                                        .clickable {
                                            selectedCategory = "All"
                                            selectedBrand = null
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }

                        categoryList.chunked(spec.categoryColumns).forEach { rowCats ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(if (spec.isCompactSmall) 8.dp else 12.dp)
                            ) {
                                rowCats.forEach { cat ->
                                    val isSelected = selectedCategory == cat.name
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 92.dp)
                                            .clickable {
                                                selectedCategory = if (selectedCategory == cat.name) "All" else cat.name
                                            }
                                            .testTag("category_card_${cat.name.lowercase().replace(" ", "_")}"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) iCaseNavy else Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else cat.bgColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = cat.icon,
                                                    contentDescription = cat.name,
                                                    tint = if (isSelected) Color.White else cat.iconColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = cat.name,
                                                fontSize = spec.sp(12.5f),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else iCaseNavy,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                repeat(spec.categoryColumns - rowCats.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Dedicated Tempered Glass & Camera Lens Protection Banner
            item {
                ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding, vertical = 4.dp)
                            .clickable { onOpenScreenGuards() }
                            .testTag("shop_open_guards_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spec.cardPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFECEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Tempered Glass and Camera Lens Protection",
                                    tint = iCaseRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tempered Glass & Camera Lens",
                                    fontSize = spec.sp(15),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy
                                )
                                Text(
                                    text = "Find exact Screen Guards & Camera Lens Protection →",
                                    fontSize = spec.sp(12),
                                    color = iCaseRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Top Brands Section
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
                                    .clickable { selectedBrand = null }
                                    .padding(vertical = 4.dp)
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = spec.horizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(topBrands) { brand ->
                                val isSelected = selectedBrand == brand
                                Card(
                                    modifier = Modifier
                                        .clickable {
                                            selectedBrand = if (selectedBrand == brand) null else brand
                                        }
                                        .testTag("brand_chip_$brand"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) iCaseRed else Color.White
                                    ),
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
                                            color = if (isSelected) Color.White else iCaseNavy,
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

            // Best Quality Best Price Banner
            item {
                ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding, vertical = 14.dp),
                        shape = RoundedCornerShape(16.dp),
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
                                    fontSize = spec.sp(20),
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
                                    text = "100% Genuine Mobile Accessories in Edappal",
                                    fontSize = spec.sp(12),
                                    color = Color(0xFFBAC7D5),
                                    lineHeight = spec.sp(17)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Crown",
                                    tint = iCaseGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Product List Header
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
                            text = if (selectedBrand != null) "$selectedBrand Products" else if (selectedCategory == "All") "All Accessories" else selectedCategory,
                            fontSize = spec.sp(18),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${displayedProducts.size} items",
                            fontSize = spec.sp(13),
                            color = Color.Gray
                        )
                    }
                }
            }

            // Adaptive Multi-Column Products Grid
            items(displayedProducts.chunked(spec.gridColumns)) { row ->
                ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(if (spec.isCompactSmall) 8.dp else 12.dp)
                    ) {
                        row.forEach { prod ->
                            Box(modifier = Modifier.weight(1f)) {
                                val ratingSummary = remember(publicProductReviews, prod.id, prod.name) {
                                    ReviewRatingHelper.summarizeProductRating(
                                        publicProductReviews,
                                        prod.id,
                                        prod.name
                                    )
                                }
                                ProductCard(
                                    product = prod,
                                    ratingSummary = ratingSummary,
                                    onClick = { onProductClick(prod) },
                                    onAddToCart = {
                                        viewModel.addToCart(prod)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Added ${prod.name} to cart")
                                        }
                                    }
                                )
                            }
                        }
                        repeat(spec.gridColumns - row.size) {
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
                            .padding(horizontal = spec.horizontalPadding, vertical = 8.dp)
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
}
