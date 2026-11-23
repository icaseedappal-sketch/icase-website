package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.OrderEntity
import com.example.data.ProductEntity
import com.example.data.RepairBookingEntity
import com.example.ui.AppViewModel
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import com.example.ui.theme.iCaseRedLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AppViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onLogout() }
    val spec = LocalResponsiveSpec.current

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allOrders by viewModel.allOrders.collectAsState()
    val allRepairs by viewModel.allRepairs.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val popularProducts by viewModel.popularProducts.collectAsState()
    val adminAllReviews by viewModel.adminAllReviews.collectAsState()
    val ordersCount by viewModel.ordersCount.collectAsState()
    val pendingOrdersCount by viewModel.pendingOrdersCount.collectAsState()
    val repairsCount by viewModel.repairsCount.collectAsState()
    val pendingRepairsCount by viewModel.pendingRepairsCount.collectAsState()

    val isOneHourGloballyEnabled by viewModel.isOneHourDeliveryGloballyEnabled.collectAsState()
    val standardDeliveryFee by viewModel.standardDeliveryFee.collectAsState()
    val expressDeliveryFee by viewModel.expressDeliveryFee.collectAsState()
    val freeDeliveryThreshold by viewModel.freeDeliveryThreshold.collectAsState()

    val isStoreOnline by viewModel.isStoreOnline.collectAsState()
    val storeName by viewModel.storeName.collectAsState()
    val storePhone by viewModel.storePhone.collectAsState()
    val storeWhatsapp by viewModel.storeWhatsapp.collectAsState()
    val storeEmail by viewModel.storeEmail.collectAsState()
    val storeAddress by viewModel.storeAddress.collectAsState()
    val storeHours by viewModel.storeHours.collectAsState()
    val screenGuardConfig by viewModel.screenGuardConfig.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Orders, 2: Repairs, 3: Products, 4: Delivery & Settings, 5: Reviews, 6: Glass & Offers

    // Dialog States
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var orderToUpdate by remember { mutableStateOf<OrderEntity?>(null) }
    var orderToDelete by remember { mutableStateOf<OrderEntity?>(null) }
    var repairToUpdate by remember { mutableStateOf<RepairBookingEntity?>(null) }
    var repairToReschedule by remember { mutableStateOf<RepairBookingEntity?>(null) }
    var repairToDelete by remember { mutableStateOf<RepairBookingEntity?>(null) }

    fun notify(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("admin_dashboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
        ) {
            // Admin Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = iCaseNavy,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .padding(horizontal = spec.horizontalPadding, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(iCaseRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "iCASE Admin Portal",
                                    fontSize = spec.sp(17),
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(iCaseGreen)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("FULL CONTROL", fontSize = spec.sp(9), fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
                                }
                            }
                            Text(
                                text = "Real-Time Cloud Firestore Sync Enabled",
                                fontSize = spec.sp(11),
                                color = Color(0xFFBAC7D5),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("admin_logout_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exit", color = Color.White, fontSize = spec.sp(12), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                }
            }

            // Scrollable Admin Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = Color.White,
                contentColor = iCaseBlue,
                edgePadding = spec.horizontalPadding,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedAdminTab]),
                        color = iCaseRed
                    )
                }
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = { Text("Overview", fontSize = spec.sp(13), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_overview")
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = { Text("Orders (${allOrders.size})", fontSize = spec.sp(13), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_orders")
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = { Text("Repairs (${allRepairs.size})", fontSize = spec.sp(13), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_repairs")
                )
                Tab(
                    selected = selectedAdminTab == 3,
                    onClick = { selectedAdminTab = 3 },
                    text = { Text("Products (${allProducts.size})", fontSize = spec.sp(13), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_products")
                )
                Tab(
                    selected = selectedAdminTab == 4,
                    onClick = { selectedAdminTab = 4 },
                    text = { Text("Delivery & Settings", fontSize = spec.sp(13), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_settings")
                )
                Tab(
                    selected = selectedAdminTab == 5,
                    onClick = { selectedAdminTab = 5 },
                    text = { Text("Reviews (${adminAllReviews.size})", fontSize = spec.sp(13), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_reviews")
                )
                Tab(
                    selected = selectedAdminTab == 6,
                    onClick = { selectedAdminTab = 6 },
                    text = { Text("Glass & Offers", fontSize = spec.sp(13), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_glass_offers")
                )
            }

            // Tab Content
            when (selectedAdminTab) {
                0 -> AdminOverviewTab(
                    ordersCount = ordersCount,
                    pendingOrdersCount = pendingOrdersCount,
                    repairsCount = repairsCount,
                    pendingRepairsCount = pendingRepairsCount,
                    totalRevenue = allOrders.sumOf { it.totalAmount },
                    productsCount = allProducts.size,
                    inStockProductsCount = allProducts.count { it.inStock },
                    isOneHourEnabled = isOneHourGloballyEnabled,
                    isStoreOnline = isStoreOnline,
                    standardDeliveryFee = standardDeliveryFee,
                    expressDeliveryFee = expressDeliveryFee,
                    freeDeliveryThreshold = freeDeliveryThreshold,
                    onToggleOneHour = {
                        viewModel.toggleOneHourDelivery(it)
                        notify(if (it) "1-Hour Express Delivery enabled" else "1-Hour Express Delivery paused")
                    },
                    onToggleStoreOnline = {
                        viewModel.toggleStoreOnline(it)
                        notify(if (it) "Store is now Online" else "Store orders paused")
                    },
                    onNavigateToOrders = { selectedAdminTab = 1 },
                    onNavigateToRepairs = { selectedAdminTab = 2 },
                    onNavigateToProducts = { selectedAdminTab = 3 },
                    onNavigateToSettings = { selectedAdminTab = 4 }
                )
                1 -> AdminOrdersTab(
                    orders = allOrders,
                    onUpdateStatus = { orderToUpdate = it },
                    onDeleteOrder = { orderToDelete = it }
                )
                2 -> AdminRepairsTab(
                    repairs = allRepairs,
                    onUpdateStatus = { repairToUpdate = it },
                    onReschedule = { repairToReschedule = it },
                    onDeleteRepair = { repairToDelete = it }
                )
                3 -> AdminProductsTab(
                    products = allProducts,
                    onEdit = { productToEdit = it },
                    onDelete = { productToDelete = it }
                )
                4 -> AdminSettingsAndPopularTab(
                    popularProducts = popularProducts,
                    allProducts = allProducts,
                    isOneHourEnabled = isOneHourGloballyEnabled,
                    standardDeliveryFee = standardDeliveryFee,
                    expressDeliveryFee = expressDeliveryFee,
                    freeDeliveryThreshold = freeDeliveryThreshold,
                    isStoreOnline = isStoreOnline,
                    storeName = storeName,
                    storePhone = storePhone,
                    storeWhatsapp = storeWhatsapp,
                    storeEmail = storeEmail,
                    storeAddress = storeAddress,
                    storeHours = storeHours,
                    onSaveDeliverySettings = { std, exp, freeMin, oneHour ->
                        viewModel.updateDeliverySettings(std, exp, freeMin, oneHour)
                        notify("Delivery charges & express settings saved")
                    },
                    onSaveStoreSettings = { name, phone, wa, email, addr, hrs, online ->
                        viewModel.updateStoreSettings(name, phone, wa, email, addr, hrs, online)
                        notify("Store & customer support settings updated")
                    },
                    onUpdatePopular = { prod, isPop, rank ->
                        viewModel.setProductPopular(prod.id, isPop, rank)
                    },
                    onChangePin = { newPin ->
                        viewModel.updateAdminPin(newPin)
                        notify("Admin credentials updated")
                    }
                )
                5 -> AdminReviewsTab(
                    reviews = adminAllReviews,
                    onApproveToggle = { review, approved ->
                        viewModel.setProductReviewApproved(review.id, approved)
                        notify(if (approved) "Approved review for public view" else "Review hidden from public")
                    },
                    onEditReview = { review, title, customerName, rating, comment, isApproved ->
                        viewModel.updateReview(
                            reviewId = review.id,
                            targetTitle = title,
                            customerName = customerName,
                            rating = rating,
                            comment = comment,
                            isApproved = isApproved
                        )
                        notify("Review updated & synced to Cloud Firestore")
                    },
                    onReply = { review, reply ->
                        viewModel.replyToReview(review.id, reply)
                        notify("Reply saved for ${review.customerName}")
                    },
                    onDelete = { review ->
                        viewModel.deleteReview(review.id)
                        notify("Deleted review from ${review.customerName}")
                    },
                    onAddManual = { refId, reviewType, targetTitle, customerName, rating, comment, isApproved ->
                        viewModel.addManualVerifiedReview(
                            referenceId = refId,
                            reviewType = reviewType,
                            targetTitle = targetTitle,
                            customerName = customerName,
                            rating = rating,
                            comment = comment,
                            isApproved = isApproved
                        ) {
                            notify(
                                if (reviewType == "SERVICE") "Added private service feedback"
                                else "Added verified product review"
                            )
                        }
                    }
                )
                6 -> AdminGlassOffersTab(
                    config = screenGuardConfig,
                    onSaveConfig = { updatedCfg, msg ->
                        viewModel.saveScreenGuardConfig(updatedCfg)
                        notify(msg)
                    }
                )
            }
        }

        // Add Product FAB on Products Tab
        if (selectedAdminTab == 3) {
            FloatingActionButton(
                onClick = { showAddProductDialog = true },
                containerColor = iCaseBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(24.dp)
                    .testTag("admin_add_product_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                .padding(16.dp)
        )
    }

    // Product Add/Edit Dialog
    if (showAddProductDialog || productToEdit != null) {
        val initial = productToEdit
        AddEditProductDialog(
            product = initial,
            onDismiss = {
                showAddProductDialog = false
                productToEdit = null
            },
            onSave = { product ->
                if (initial != null) {
                    viewModel.updateProduct(product)
                    notify("Updated ${product.name}")
                } else {
                    viewModel.addProduct(product)
                    notify("Added ${product.name} to catalog")
                }
                showAddProductDialog = false
                productToEdit = null
            }
        )
    }

    // Product Delete Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = { Text("Remove \"${productToDelete!!.name}\" from the catalog and Cloud Firestore?") },
            confirmButton = {
                Button(
                    onClick = {
                        val name = productToDelete!!.name
                        viewModel.deleteProduct(productToDelete!!.id)
                        productToDelete = null
                        notify("Deleted $name")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Order Status Update Dialog
    if (orderToUpdate != null) {
        UpdateOrderStatusDialog(
            order = orderToUpdate!!,
            onDismiss = { orderToUpdate = null },
            onUpdate = { newStatus, notes ->
                val oid = orderToUpdate!!.id
                viewModel.updateOrderStatus(oid, newStatus, notes)
                orderToUpdate = null
                notify("Order $oid updated to $newStatus")
            }
        )
    }

    // Order Delete Confirmation Dialog
    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("Delete Order?", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = { Text("Permanently remove order ${orderToDelete!!.id} (${orderToDelete!!.customerName})?") },
            confirmButton = {
                Button(
                    onClick = {
                        val oid = orderToDelete!!.id
                        viewModel.deleteOrder(oid)
                        orderToDelete = null
                        notify("Deleted order $oid")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed)
                ) {
                    Text("Delete Order")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Repair Status Update Dialog
    if (repairToUpdate != null) {
        UpdateRepairStatusDialog(
            repair = repairToUpdate!!,
            onDismiss = { repairToUpdate = null },
            onUpdate = { newStatus, notes ->
                val rid = repairToUpdate!!.id
                viewModel.updateRepairStatus(rid, newStatus, notes)
                repairToUpdate = null
                notify("Repair $rid updated to $newStatus")
            }
        )
    }

    // Repair Reschedule Dialog
    if (repairToReschedule != null) {
        RescheduleRepairDialog(
            repair = repairToReschedule!!,
            onDismiss = { repairToReschedule = null },
            onSave = { newDate, newSlot, notes ->
                val rid = repairToReschedule!!.id
                viewModel.rescheduleRepair(rid, newDate, newSlot, notes)
                repairToReschedule = null
                notify("Rescheduled pickup for $rid")
            }
        )
    }

    // Repair Delete Confirmation Dialog
    if (repairToDelete != null) {
        AlertDialog(
            onDismissRequest = { repairToDelete = null },
            title = { Text("Delete Repair Booking?", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = { Text("Permanently remove repair booking ${repairToDelete!!.id}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val rid = repairToDelete!!.id
                        viewModel.deleteRepair(rid)
                        repairToDelete = null
                        notify("Deleted repair $rid")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { repairToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminOverviewTab(
    ordersCount: Int,
    pendingOrdersCount: Int,
    repairsCount: Int,
    pendingRepairsCount: Int,
    totalRevenue: Double,
    productsCount: Int,
    inStockProductsCount: Int,
    isOneHourEnabled: Boolean,
    isStoreOnline: Boolean,
    standardDeliveryFee: Double,
    expressDeliveryFee: Double,
    freeDeliveryThreshold: Double,
    onToggleOneHour: (Boolean) -> Unit,
    onToggleStoreOnline: (Boolean) -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToRepairs: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val spec = LocalResponsiveSpec.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = spec.horizontalPadding, vertical = 16.dp)
    ) {
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isStoreOnline) Color(0xFFE8FDF2) else Color(0xFFFEE2E2)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spec.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = if (isStoreOnline) iCaseGreen else iCaseRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Store Operational Status",
                                    fontSize = spec.sp(15),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isStoreOnline)
                                    "ONLINE: Accepting customer checkout orders."
                                else
                                    "PAUSED: Customer checkout is disabled.",
                                fontSize = spec.sp(12),
                                color = if (isStoreOnline) Color(0xFF166534) else Color(0xFF991B1B)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isStoreOnline,
                            onCheckedChange = onToggleStoreOnline,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = iCaseGreen
                            ),
                            modifier = Modifier.testTag("admin_toggle_store_online")
                        )
                    }
                }
            }
        }

        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOneHourEnabled) iCaseRedLight else Color(0xFFF1F5F9)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spec.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = if (isOneHourEnabled) iCaseRed else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1-Hour Express Delivery Switch",
                                    fontSize = spec.sp(15),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isOneHourEnabled)
                                    "ACTIVE: Eligible customers can select 1-Hour local delivery."
                                else
                                    "PAUSED: 1-Hour delivery is hidden in Checkout.",
                                fontSize = spec.sp(12),
                                color = if (isOneHourEnabled) Color(0xFF991B1B) else Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isOneHourEnabled,
                            onCheckedChange = onToggleOneHour,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = iCaseRed
                            ),
                            modifier = Modifier.testTag("admin_toggle_one_hour")
                        )
                    }
                }
            }
        }

        // Responsive KPI Stat Cards (2x2 on phones/medium, 4-column single row on Expanded >= 840dp)
        item {
            data class AdminStatItem(
                val label: String,
                val value: String,
                val valueColor: Color,
                val subtext: String,
                val subColor: Color,
                val onClick: () -> Unit
            )
            val statItems = listOf(
                AdminStatItem("Total Orders", "$ordersCount", iCaseNavy, "$pendingOrdersCount Pending", iCaseBlue, onNavigateToOrders),
                AdminStatItem("Repair Bookings", "$repairsCount", iCaseRed, "$pendingRepairsCount In Progress", iCaseRed, onNavigateToRepairs),
                AdminStatItem("Order Revenue", "₹${"%,.0f".format(totalRevenue)}", iCaseGreen, "Live Synced Total", Color.Gray, onNavigateToOrders),
                AdminStatItem("Catalog Products", "$productsCount", iCaseBlue, "$inStockProductsCount In Stock", iCaseGreen, onNavigateToProducts)
            )

            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    statItems.chunked(spec.adminStatColumns).forEach { rowStats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowStats.forEach { stat ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { stat.onClick() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                                        Text(
                                            text = stat.label,
                                            fontSize = spec.sp(12),
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stat.value,
                                            fontSize = spec.sp(24),
                                            fontWeight = FontWeight.Black,
                                            color = stat.valueColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stat.subtext,
                                            fontSize = spec.sp(12),
                                            color = stat.subColor,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            repeat(spec.adminStatColumns - rowStats.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Delivery Fee Snapshot Card
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Delivery Charges",
                                fontSize = spec.sp(15),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Fees & Settings →",
                                fontSize = spec.sp(12),
                                fontWeight = FontWeight.Bold,
                                color = iCaseBlue,
                                modifier = Modifier
                                    .clickable { onNavigateToSettings() }
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Standard Home Delivery:", fontSize = spec.sp(13), color = Color.Gray, modifier = Modifier.weight(1f))
                            Text("₹${"%.0f".format(standardDeliveryFee)}", fontSize = spec.sp(13), fontWeight = FontWeight.Bold, color = iCaseNavy)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Express 1-Hour Delivery:", fontSize = spec.sp(13), color = Color.Gray, modifier = Modifier.weight(1f))
                            Text("₹${"%.0f".format(expressDeliveryFee)}", fontSize = spec.sp(13), fontWeight = FontWeight.Bold, color = iCaseRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Free Delivery Threshold:", fontSize = spec.sp(13), color = Color.Gray, modifier = Modifier.weight(1f))
                            Text(
                                text = if (freeDeliveryThreshold > 0.0) "Orders ≥ ₹${"%.0f".format(freeDeliveryThreshold)}" else "Disabled",
                                fontSize = spec.sp(13),
                                fontWeight = FontWeight.Bold,
                                color = if (freeDeliveryThreshold > 0.0) iCaseGreen else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
fun AdminSettingsAndPopularTab(
    popularProducts: List<ProductEntity>,
    allProducts: List<ProductEntity>,
    isOneHourEnabled: Boolean,
    standardDeliveryFee: Double,
    expressDeliveryFee: Double,
    freeDeliveryThreshold: Double,
    isStoreOnline: Boolean,
    storeName: String,
    storePhone: String,
    storeWhatsapp: String,
    storeEmail: String,
    storeAddress: String,
    storeHours: String,
    onSaveDeliverySettings: (standardFee: Double, expressFee: Double, freeThreshold: Double, oneHourEnabled: Boolean) -> Unit,
    onSaveStoreSettings: (name: String, phone: String, whatsapp: String, email: String, address: String, hours: String, online: Boolean) -> Unit,
    onUpdatePopular: (ProductEntity, Boolean, Int) -> Unit,
    onChangePin: (String) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var stdFeeStr by remember(standardDeliveryFee) { mutableStateOf(standardDeliveryFee.toInt().toString()) }
    var expFeeStr by remember(expressDeliveryFee) { mutableStateOf(expressDeliveryFee.toInt().toString()) }
    var freeThresholdStr by remember(freeDeliveryThreshold) { mutableStateOf(freeDeliveryThreshold.toInt().toString()) }
    var oneHourToggle by remember(isOneHourEnabled) { mutableStateOf(isOneHourEnabled) }

    var editStoreOnline by remember(isStoreOnline) { mutableStateOf(isStoreOnline) }
    var editStoreName by remember(storeName) { mutableStateOf(storeName) }
    var editStorePhone by remember(storePhone) { mutableStateOf(storePhone) }
    var editStoreWhatsapp by remember(storeWhatsapp) { mutableStateOf(storeWhatsapp) }
    var editStoreEmail by remember(storeEmail) { mutableStateOf(storeEmail) }
    var editStoreAddress by remember(storeAddress) { mutableStateOf(storeAddress) }
    var editStoreHours by remember(storeHours) { mutableStateOf(storeHours) }

    var newPinInput by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = spec.horizontalPadding, vertical = 16.dp)
    ) {
        // 1. Delivery Charges & Express Delivery Control
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text("Delivery Charges & Express Control", fontSize = spec.sp(15), fontWeight = FontWeight.Bold, color = iCaseNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("All updates sync live to Customer Cart & Checkout.", fontSize = spec.sp(12), color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            if (maxWidth < 380.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = stdFeeStr,
                                        onValueChange = { stdFeeStr = it },
                                        label = { Text("Standard Fee (₹)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_standard_fee_input")
                                    )
                                    OutlinedTextField(
                                        value = expFeeStr,
                                        onValueChange = { expFeeStr = it },
                                        label = { Text("1-Hour Fee (₹)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_express_fee_input")
                                    )
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = stdFeeStr,
                                        onValueChange = { stdFeeStr = it },
                                        label = { Text("Standard Fee (₹)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("admin_standard_fee_input")
                                    )
                                    OutlinedTextField(
                                        value = expFeeStr,
                                        onValueChange = { expFeeStr = it },
                                        label = { Text("1-Hour Fee (₹)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("admin_express_fee_input")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = freeThresholdStr,
                            onValueChange = { freeThresholdStr = it },
                            label = { Text("Free Standard Delivery Min Order (₹, 0 = off)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_free_threshold_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (oneHourToggle) "Express 1-Hour Enabled" else "Express 1-Hour Paused",
                                fontSize = spec.sp(13),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = oneHourToggle,
                                onCheckedChange = { oneHourToggle = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iCaseRed)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onSaveDeliverySettings(
                                    stdFeeStr.toDoubleOrNull() ?: standardDeliveryFee,
                                    expFeeStr.toDoubleOrNull() ?: expressDeliveryFee,
                                    freeThresholdStr.toDoubleOrNull() ?: freeDeliveryThreshold,
                                    oneHourToggle
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("admin_save_delivery_button")
                        ) {
                            Text("Save Delivery Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Store & Customer Support Settings Editor
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text("Store & Customer Support Settings", fontSize = spec.sp(15), fontWeight = FontWeight.Bold, color = iCaseNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Modify app-wide store status, contact phone, WhatsApp, email, address & hours.", fontSize = spec.sp(12), color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (editStoreOnline) "Accepting Customer Orders" else "Store Orders Paused",
                                fontSize = spec.sp(13),
                                fontWeight = FontWeight.Bold,
                                color = if (editStoreOnline) iCaseGreen else iCaseRed,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = editStoreOnline,
                                onCheckedChange = { editStoreOnline = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iCaseGreen)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editStoreName,
                            onValueChange = { editStoreName = it },
                            label = { Text("Store Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            if (maxWidth < 380.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = editStorePhone,
                                        onValueChange = { editStorePhone = it },
                                        label = { Text("Support Phone") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editStoreWhatsapp,
                                        onValueChange = { editStoreWhatsapp = it },
                                        label = { Text("WhatsApp No.") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = editStorePhone,
                                        onValueChange = { editStorePhone = it },
                                        label = { Text("Support Phone") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = editStoreWhatsapp,
                                        onValueChange = { editStoreWhatsapp = it },
                                        label = { Text("WhatsApp No.") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editStoreEmail,
                            onValueChange = { editStoreEmail = it },
                            label = { Text("Official Gmail") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editStoreAddress,
                            onValueChange = { editStoreAddress = it },
                            label = { Text("Store Address") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editStoreHours,
                            onValueChange = { editStoreHours = it },
                            label = { Text("Working Hours") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onSaveStoreSettings(
                                    editStoreName.trim(),
                                    editStorePhone.trim(),
                                    editStoreWhatsapp.trim(),
                                    editStoreEmail.trim(),
                                    editStoreAddress.trim(),
                                    editStoreHours.trim(),
                                    editStoreOnline
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("admin_save_store_settings_button")
                        ) {
                            Text("Save Store & Support Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Manage Popular Showcase & Ranking
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text("Manage Popular Showcase (${popularProducts.size} active)", fontSize = spec.sp(15), fontWeight = FontWeight.Bold, color = iCaseNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Toggle homepage showcase visibility and priority rank.", fontSize = spec.sp(12), color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))

                        allProducts.forEach { prod ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clickable { onUpdatePopular(prod, !prod.isPopular, prod.popularityRank) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = prod.isPopular,
                                    onCheckedChange = { isChecked -> onUpdatePopular(prod, isChecked, prod.popularityRank) },
                                    colors = CheckboxDefaults.colors(checkedColor = iCaseBlue)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontSize = spec.sp(13), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                                    Text("₹${"%.0f".format(prod.price)} • Rank #${prod.popularityRank}", fontSize = spec.sp(11), color = Color.Gray)
                                }
                                if (prod.isPopular) {
                                    IconButton(
                                        onClick = {
                                            onUpdatePopular(prod, true, (prod.popularityRank - 1).coerceAtLeast(1))
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Rank", modifier = Modifier.size(16.dp))
                                    }
                                    Text("#${prod.popularityRank}", fontSize = spec.sp(12), fontWeight = FontWeight.Bold, color = iCaseNavy)
                                    IconButton(
                                        onClick = {
                                            onUpdatePopular(prod, true, prod.popularityRank + 1)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Rank", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Change Admin PIN / Password
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text("Change Admin Credentials", fontSize = spec.sp(15), fontWeight = FontWeight.Bold, color = iCaseNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Update the concealed password required after 5 logo taps.", fontSize = spec.sp(12), color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { newPinInput = it },
                            label = { Text("New Admin Password or PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (pinMessage != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(pinMessage!!, color = iCaseGreen, fontSize = spec.sp(12), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (newPinInput.isNotBlank()) {
                                    onChangePin(newPinInput.trim())
                                    pinMessage = "Admin credential successfully updated."
                                    newPinInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Save New Password")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}
