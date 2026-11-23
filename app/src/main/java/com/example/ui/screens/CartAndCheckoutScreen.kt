package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.ui.components.PouchThumbImage
import com.example.data.CartItem
import com.example.data.GoogleAuthHelper
import com.example.data.OrderEntity
import com.example.ui.AppViewModel
import com.example.ui.components.ProfileDetailsDialog
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseLightBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartAndCheckoutScreen(
    viewModel: AppViewModel,
    onTrackOrder: (String) -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val spec = LocalResponsiveSpec.current
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val cartItems by viewModel.cartItems.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    val isOneHourGloballyEnabled by viewModel.isOneHourDeliveryGloballyEnabled.collectAsState()
    val standardDeliveryFee by viewModel.standardDeliveryFee.collectAsState()
    val expressDeliveryFee by viewModel.expressDeliveryFee.collectAsState()
    val freeDeliveryThreshold by viewModel.freeDeliveryThreshold.collectAsState()
    val isStoreOnline by viewModel.isStoreOnline.collectAsState()
    val isPlacingOrder by viewModel.isPlacingOrder.collectAsState()

    var isCheckoutStep by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showEditAddressDialog by remember { mutableStateOf(false) }

    var customerName by remember(customerProfile) { mutableStateOf(customerProfile.name) }
    var customerPhone by remember(customerProfile) { mutableStateOf(customerProfile.phone) }
    var whatsappNumber by remember(customerProfile) { mutableStateOf(customerProfile.whatsapp) }
    var deliveryAddress by remember(customerProfile) { mutableStateOf(customerProfile.address) }

    val allItemsEligibleForOneHour = cartItems.isNotEmpty() && cartItems.all { it.product.isOneHourEligible }
    val canChooseOneHour = allItemsEligibleForOneHour && isOneHourGloballyEnabled

    var selectedDeliveryType by remember { mutableStateOf("STANDARD") }
    LaunchedEffect(canChooseOneHour) {
        if (!canChooseOneHour && selectedDeliveryType == "ONE_HOUR") {
            selectedDeliveryType = "STANDARD"
        }
    }

    val subtotal = cartItems.sumOf { it.product.price * it.quantity }
    val isFreeStandardQualified = freeDeliveryThreshold > 0.0 && subtotal >= freeDeliveryThreshold
    val deliveryFee = when {
        cartItems.isEmpty() -> 0.0
        selectedDeliveryType == "ONE_HOUR" && canChooseOneHour -> expressDeliveryFee
        isFreeStandardQualified -> 0.0
        else -> standardDeliveryFee
    }
    val total = if (cartItems.isEmpty()) 0.0 else subtotal + deliveryFee

    var selectedPaymentMethod by remember { mutableStateOf("UPI") }
    var completedOrder by remember { mutableStateOf<OrderEntity?>(null) }

    BackHandler(enabled = isCheckoutStep) {
        isCheckoutStep = false
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Shopping Cart", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = { Text("Are you sure you want to remove all items from your cart?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCart()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Clear Cart")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditAddressDialog) {
        ProfileDetailsDialog(
            title = if (customerName.isNotBlank() && deliveryAddress.isNotBlank()) {
                "Edit Delivery Details"
            } else {
                "Add Delivery Details"
            },
            initialProfile = customerProfile,
            initialName = customerName,
            initialPhone = customerPhone,
            initialWhatsapp = whatsappNumber,
            initialAddress = deliveryAddress,
            showAddressField = true,
            requireAddress = true,
            onDismiss = { showEditAddressDialog = false },
            onSave = { name, phone, whatsapp, address ->
                customerName = name
                customerPhone = phone
                whatsappNumber = whatsapp
                deliveryAddress = address
                viewModel.updateCustomerProfile(name, phone, whatsapp, address)
                showEditAddressDialog = false
            }
        )
    }

    if (completedOrder != null) {
        OrderSuccessDialog(
            order = completedOrder!!,
            onDismiss = { completedOrder = null },
            onTrackOrder = {
                val orderId = completedOrder!!.id
                completedOrder = null
                onTrackOrder(orderId)
            },
            onSendWhatsApp = {
                viewModel.sendWhatsAppOrderNotification(completedOrder!!)
            }
        )
    }

    if (cartItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF1F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = iCaseRed,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Cart is Empty",
                        fontSize = spec.sp(20),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Explore mobile covers, screen guards, chargers and audio accessories.",
                        fontSize = spec.sp(13.5f),
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onContinueShopping,
                        colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Explore Shop →", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    if (!isCheckoutStep) {
        // ==========================================
        // SCREEN 7: MY CART
        // ==========================================
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .testTag("cart_screen"),
            contentPadding = PaddingValues(bottom = if (spec.useNavRail) 24.dp else 96.dp)
        ) {
            item {
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
                                onClick = onContinueShopping,
                                modifier = Modifier.size(48.dp)
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
                                text = "My Cart",
                                fontSize = spec.sp(18),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy
                            )
                        }

                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Cart",
                                tint = iCaseNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            items(cartItems, key = { it.product.id }) { item ->
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Screen7CartItemRow(
                        item = item,
                        onIncrease = { viewModel.updateCartQuantity(item.product.id, item.quantity + 1) },
                        onDecrease = { viewModel.updateCartQuantity(item.product.id, item.quantity - 1) },
                        onRemove = { viewModel.removeFromCart(item.product.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Dynamic Bill Breakdown Card
            item {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(spec.cardPadding)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", color = Color(0xFF64748B), fontSize = spec.sp(14))
                                Text("₹${"%,.0f".format(subtotal)}", fontWeight = FontWeight.Bold, fontSize = spec.sp(14), color = iCaseNavy)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (selectedDeliveryType == "ONE_HOUR" && canChooseOneHour) "Express 1-Hour Charge" else "Delivery Charge",
                                    color = Color(0xFF64748B),
                                    fontSize = spec.sp(14),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (deliveryFee == 0.0) "FREE" else "₹${"%.0f".format(deliveryFee)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = spec.sp(14),
                                    color = if (deliveryFee == 0.0) iCaseGreen else iCaseNavy
                                )
                            }

                            if (freeDeliveryThreshold > 0.0 && subtotal < freeDeliveryThreshold) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Add ₹${"%,.0f".format(freeDeliveryThreshold - subtotal)} more for FREE standard delivery!",
                                    fontSize = spec.sp(12),
                                    fontWeight = FontWeight.SemiBold,
                                    color = iCaseBlue
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = spec.sp(16), color = iCaseNavy)
                                Text("₹${"%,.0f".format(total)}", fontWeight = FontWeight.Black, fontSize = spec.sp(20), color = iCaseRed)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Box(modifier = Modifier.padding(horizontal = spec.horizontalPadding)) {
                        Button(
                            onClick = { isCheckoutStep = true },
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("proceed_to_checkout_button")
                        ) {
                            Text(
                                text = "Proceed to Checkout →",
                                fontSize = spec.sp(16),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    } else {
        // ==========================================
        // SCREEN 8: CHECKOUT
        // ==========================================
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .testTag("checkout_screen"),
            contentPadding = PaddingValues(bottom = if (spec.useNavRail) 24.dp else 96.dp)
        ) {
            item {
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isCheckoutStep = false },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Cart",
                                tint = iCaseNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Checkout",
                            fontSize = spec.sp(18),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                    }
                }
            }

            if (!isStoreOnline) {
                item {
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spec.horizontalPadding),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = iCaseRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Store orders are temporarily paused by Admin. Please check back shortly or contact Customer Support.",
                                        fontSize = spec.sp(13),
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!customerProfile.isGoogleSignedIn) {
                item {
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spec.horizontalPadding),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = iCaseLightBlue)
                            ) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = "Sync Orders to Your Google Account",
                                            fontSize = spec.sp(13),
                                            fontWeight = FontWeight.Bold,
                                            color = iCaseNavy
                                        )
                                        Text(
                                            text = "Keep your personal order history isolated & backed up.",
                                            fontSize = spec.sp(11.5f),
                                            color = Color(0xFF475569)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            GoogleAuthHelper.onGoogleSignInClicked(
                                                context = context,
                                                credentialManager = credentialManager,
                                                scope = scope,
                                                onAuthSuccess = { user -> viewModel.onGoogleUserAuthenticated(user) },
                                                onAuthError = {}
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier.heightIn(min = 42.dp)
                                    ) {
                                        Text("Google Sign-In", fontSize = spec.sp(11.5f), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            // Card 1: Delivery Address
            item {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(spec.cardPadding)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = iCaseRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Delivery Address",
                                        fontSize = spec.sp(15),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy
                                    )
                                }

                                Text(
                                    text = if (customerName.isNotBlank() && deliveryAddress.isNotBlank()) "Edit" else "+ Add",
                                    fontSize = spec.sp(13),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseBlue,
                                    modifier = Modifier
                                        .clickable { showEditAddressDialog = true }
                                        .padding(6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (customerName.isNotBlank() && deliveryAddress.isNotBlank()) {
                                Text(
                                    text = customerName,
                                    fontSize = spec.sp(14),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = deliveryAddress,
                                    fontSize = spec.sp(13),
                                    color = Color(0xFF64748B),
                                    lineHeight = spec.sp(18)
                                )
                                if (customerPhone.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = customerPhone,
                                        fontSize = spec.sp(12.5f),
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .clickable { showEditAddressDialog = true }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "No address entered yet",
                                            fontSize = spec.sp(13),
                                            fontWeight = FontWeight.Bold,
                                            color = iCaseNavy
                                        )
                                        Text(
                                            text = "Tap here to enter delivery address",
                                            fontSize = spec.sp(12),
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = "Add →",
                                        fontSize = spec.sp(13),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            // Card 2: Delivery Option
            item {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(spec.cardPadding)) {
                            Text(
                                text = "Delivery Option",
                                fontSize = spec.sp(15),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedDeliveryType == "STANDARD") Color(0xFFFFF1F2) else Color.Transparent)
                                    .clickable { selectedDeliveryType = "STANDARD" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDeliveryType == "STANDARD",
                                    onClick = { selectedDeliveryType = "STANDARD" },
                                    colors = RadioButtonDefaults.colors(selectedColor = iCaseRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Home Delivery",
                                        fontSize = spec.sp(14),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy
                                    )
                                    Text(
                                        text = if (isFreeStandardQualified) "1-2 Days • Free Shipping Unlocked" else "(1-2 Days)",
                                        fontSize = spec.sp(12),
                                        color = if (isFreeStandardQualified) iCaseGreen else Color.Gray
                                    )
                                }
                                Text(
                                    text = if (isFreeStandardQualified) "FREE" else "₹${"%.0f".format(standardDeliveryFee)}",
                                    fontSize = spec.sp(13),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFreeStandardQualified) iCaseGreen else iCaseNavy,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = iCaseRed,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            if (canChooseOneHour) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selectedDeliveryType == "ONE_HOUR") Color(0xFFFFF1F2) else Color.Transparent)
                                        .clickable { selectedDeliveryType = "ONE_HOUR" }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedDeliveryType == "ONE_HOUR",
                                        onClick = { selectedDeliveryType = "ONE_HOUR" },
                                        colors = RadioButtonDefaults.colors(selectedColor = iCaseRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Express 1-Hour Delivery ⚡",
                                            fontSize = spec.sp(14),
                                            fontWeight = FontWeight.Bold,
                                            color = iCaseRed
                                        )
                                        Text(
                                            text = "Direct doorstep delivery in Edappal",
                                            fontSize = spec.sp(12),
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = "₹${"%.0f".format(expressDeliveryFee)}",
                                        fontSize = spec.sp(13),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            // Card 3: Payment Method
            item {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(spec.cardPadding)) {
                            Text(
                                text = "Payment Method",
                                fontSize = spec.sp(15),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPaymentMethod = "UPI" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "UPI",
                                    onClick = { selectedPaymentMethod = "UPI" },
                                    colors = RadioButtonDefaults.colors(selectedColor = iCaseRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "UPI (Google Pay, PhonePe)",
                                    fontSize = spec.sp(14),
                                    fontWeight = FontWeight.Medium,
                                    color = iCaseNavy,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE2E8F0))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("UPI", fontSize = spec.sp(11), fontWeight = FontWeight.Black, color = iCaseNavy)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPaymentMethod = "COD" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "COD",
                                    onClick = { selectedPaymentMethod = "COD" },
                                    colors = RadioButtonDefaults.colors(selectedColor = iCaseRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cash on Delivery",
                                    fontSize = spec.sp(14),
                                    fontWeight = FontWeight.Medium,
                                    color = iCaseNavy,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPaymentMethod = "CARD" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "CARD",
                                    onClick = { selectedPaymentMethod = "CARD" },
                                    colors = RadioButtonDefaults.colors(selectedColor = iCaseRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Card Payment",
                                    fontSize = spec.sp(14),
                                    fontWeight = FontWeight.Medium,
                                    color = iCaseNavy,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = iCaseBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            // Card 4: Checkout Summary
            item {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spec.horizontalPadding),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(spec.cardPadding)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Items Subtotal", fontSize = spec.sp(13), color = Color.Gray)
                                Text("₹${"%,.0f".format(subtotal)}", fontSize = spec.sp(13), fontWeight = FontWeight.Bold, color = iCaseNavy)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Charge", fontSize = spec.sp(13), color = Color.Gray)
                                Text(
                                    text = if (deliveryFee == 0.0) "FREE" else "₹${"%.0f".format(deliveryFee)}",
                                    fontSize = spec.sp(13),
                                    fontWeight = FontWeight.Bold,
                                    color = if (deliveryFee == 0.0) iCaseGreen else iCaseNavy
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Payable", fontSize = spec.sp(15), fontWeight = FontWeight.Bold, color = iCaseNavy)
                                Text("₹${"%,.0f".format(total)}", fontSize = spec.sp(18), fontWeight = FontWeight.Black, color = iCaseRed)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Box(modifier = Modifier.padding(horizontal = spec.horizontalPadding)) {
                        Button(
                            onClick = {
                                val phoneDigits = customerPhone.filter { it.isDigit() }
                                if (
                                    customerName.isBlank() ||
                                    phoneDigits.length < 10 ||
                                    deliveryAddress.isBlank()
                                ) {
                                    showEditAddressDialog = true
                                    return@Button
                                }

                                viewModel.updateCustomerProfile(
                                    customerName.trim(),
                                    customerPhone.trim(),
                                    whatsappNumber.trim(),
                                    deliveryAddress.trim()
                                )

                                viewModel.placeOrder(
                                    customerName = customerName.trim(),
                                    customerPhone = customerPhone.trim(),
                                    whatsappNumber = whatsappNumber.trim(),
                                    deliveryAddress = deliveryAddress.trim(),
                                    deliveryType = selectedDeliveryType,
                                    deliveryFee = deliveryFee,
                                    onSuccess = { order ->
                                        completedOrder = order
                                    }
                                )
                            },
                            enabled = isStoreOnline && !isPlacingOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("place_order_button")
                        ) {
                            Text(
                                text = if (isStoreOnline) "Place Order • ₹${"%,.0f".format(total)} →" else "Store Currently Paused",
                                fontSize = spec.sp(16),
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
    }
}

@Composable
fun Screen7CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    val spec = LocalResponsiveSpec.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spec.horizontalPadding, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val useStackedActions = maxWidth < 380.dp

            if (useStackedActions) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PouchThumbImage(
                            imageUrl = item.product.imageUrl,
                            contentDescription = item.product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                fontSize = spec.sp(14.5f),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${"%,.0f".format(item.product.price)}",
                                fontSize = spec.sp(15.5f),
                                color = iCaseRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CartQuantityStepper(
                            quantity = item.quantity,
                            onDecrease = onDecrease,
                            onIncrease = onIncrease
                        )
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove",
                                tint = iCaseRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PouchThumbImage(
                        imageUrl = item.product.imageUrl,
                        contentDescription = item.product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            fontSize = spec.sp(15),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${"%,.0f".format(item.product.price)}",
                            fontSize = spec.sp(16),
                            color = iCaseRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    CartQuantityStepper(
                        quantity = item.quantity,
                        onDecrease = onDecrease,
                        onIncrease = onIncrease
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove",
                            tint = iCaseRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartQuantityStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val spec = LocalResponsiveSpec.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(onClick = onDecrease, modifier = Modifier.size(34.dp)) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(15.dp))
        }
        Text(
            text = "$quantity",
            fontSize = spec.sp(14),
            fontWeight = FontWeight.Bold,
            color = iCaseNavy,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        IconButton(onClick = onIncrease, modifier = Modifier.size(34.dp)) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
fun OrderSuccessDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onTrackOrder: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    val spec = LocalResponsiveSpec.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iCaseGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order Placed!",
                        fontSize = spec.sp(18),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Text(
                        text = "ID: ${order.id}",
                        fontSize = spec.sp(13),
                        fontWeight = FontWeight.Bold,
                        color = iCaseRed
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
                    text = "Your order has been recorded in the iCASE system. Our logistics team is preparing it for shipment.",
                    fontSize = spec.sp(13),
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BackgroundLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Type:", fontSize = spec.sp(12), color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (order.deliveryType == "ONE_HOUR") "⚡ 1-Hour Express" else "Home Delivery",
                                fontSize = spec.sp(12),
                                fontWeight = FontWeight.Bold,
                                color = if (order.deliveryType == "ONE_HOUR") iCaseRed else iCaseBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Paid:", fontSize = spec.sp(12), color = Color.Gray)
                            Text("₹${"%,.0f".format(order.totalAmount)}", fontSize = spec.sp(12), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Initial Status:", fontSize = spec.sp(12), color = Color.Gray)
                            Text(order.status, fontSize = spec.sp(12), fontWeight = FontWeight.Bold, color = iCaseNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSendWhatsApp,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send WhatsApp Confirmation",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = spec.sp(13)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onTrackOrder,
                colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track Order Now")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Close")
            }
        }
    )
}
