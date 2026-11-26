package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.PouchImageHelper
import com.example.ui.components.PouchThumbImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import com.example.data.InitialData
import com.example.data.JsonHelper
import com.example.data.OrderEntity
import com.example.data.ProductColorOption
import com.example.data.ProductEntity
import com.example.data.RepairBookingEntity
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseLightBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import com.example.ui.theme.iCaseRedLight

@Composable
fun AdminOrdersTab(
    orders: List<OrderEntity>,
    onUpdateStatus: (OrderEntity) -> Unit,
    onDeleteOrder: (OrderEntity) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    val statusFilters = listOf("All", "Order Received", "Processing", "Out for Delivery", "Delivered")

    val filtered = remember(orders, searchQuery, selectedStatusFilter) {
        orders.filter { order ->
            val matchesStatus = selectedStatusFilter == "All" || order.status == selectedStatusFilter
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                order.id.contains(q, ignoreCase = true) ||
                order.customerName.contains(q, ignoreCase = true) ||
                order.customerPhone.contains(q, ignoreCase = true) ||
                order.userId.contains(q, ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = spec.horizontalPadding, vertical = 10.dp)
        ) {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Order ID, customer, phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(statusFilters) { status ->
                            FilterChip(
                                selected = selectedStatusFilter == status,
                                onClick = { selectedStatusFilter = status },
                                label = { Text(status, fontSize = spec.sp(11)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = iCaseNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(spec.horizontalPadding), contentAlignment = Alignment.Center) {
                Text(
                    text = if (orders.isEmpty()) "No customer orders recorded yet." else "No orders match the current filter.",
                    color = Color.Gray,
                    fontSize = spec.sp(14)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = spec.horizontalPadding, vertical = 12.dp)
            ) {
                items(filtered, key = { it.id }) { order ->
                    val items = JsonHelper.parseOrderItems(order.itemsJson)
                    ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(spec.cardPadding)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = order.id,
                                            fontSize = spec.sp(16),
                                            fontWeight = FontWeight.Black,
                                            color = iCaseNavy,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(order.customerName, fontSize = spec.sp(13), fontWeight = FontWeight.Bold)
                                        if (order.userId.isNotBlank()) {
                                            Text(
                                                text = "Account: ${order.userId.take(18)}",
                                                fontSize = spec.sp(10),
                                                color = iCaseBlue,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (order.status) {
                                                    "Delivered" -> iCaseGreen
                                                    "Out for Delivery" -> iCaseRed
                                                    else -> iCaseBlue
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = order.status,
                                            fontSize = spec.sp(11),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Phone: ${order.customerPhone} • WhatsApp: ${order.whatsappNumber}", fontSize = spec.sp(12), color = Color.Gray)
                                Text("Delivery: ${order.deliveryAddress}", fontSize = spec.sp(12), color = Color(0xFF475569))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Items: " + items.joinToString(", ") { "${it.quantity}x ${it.productName}" }, fontSize = spec.sp(12), color = Color(0xFF1E293B))
                                Text(
                                    text = "Subtotal: ₹${"%.0f".format(order.subtotal)} • Delivery: ₹${"%.0f".format(order.deliveryFee)}",
                                    fontSize = spec.sp(11),
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val stackActions = maxWidth < 380.dp
                                    if (stackActions) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "Total: ₹${"%.0f".format(order.totalAmount)} (${if (order.deliveryType == "ONE_HOUR") "⚡ 1-Hour" else "Standard"})",
                                                fontSize = spec.sp(13),
                                                fontWeight = FontWeight.Bold,
                                                color = iCaseNavy
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { onDeleteOrder(order) },
                                                    modifier = Modifier.testTag("admin_delete_order_${order.id}")
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Order", tint = iCaseRed)
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Button(
                                                    onClick = { onUpdateStatus(order) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = iCaseBlue),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .heightIn(min = 44.dp)
                                                        .testTag("admin_update_order_${order.id}")
                                                ) {
                                                    Text("Update Status", fontSize = spec.sp(12), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                                }
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Total: ₹${"%.0f".format(order.totalAmount)} (${if (order.deliveryType == "ONE_HOUR") "⚡ 1-Hour" else "Standard"})",
                                                fontSize = spec.sp(13),
                                                fontWeight = FontWeight.Bold,
                                                color = iCaseNavy,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { onDeleteOrder(order) },
                                                    modifier = Modifier.testTag("admin_delete_order_${order.id}")
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Order", tint = iCaseRed)
                                                }
                                                Button(
                                                    onClick = { onUpdateStatus(order) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = iCaseBlue),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .heightIn(min = 44.dp)
                                                        .testTag("admin_update_order_${order.id}")
                                                ) {
                                                    Text("Update Status", fontSize = spec.sp(12), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }
}

@Composable
fun AdminRepairsTab(
    repairs: List<RepairBookingEntity>,
    onUpdateStatus: (RepairBookingEntity) -> Unit,
    onReschedule: (RepairBookingEntity) -> Unit,
    onDeleteRepair: (RepairBookingEntity) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    val statusFilters = listOf("All", "Booking Received", "Pickup Scheduled", "Device Received", "Repairing", "Repair Completed", "Out for Delivery", "Delivered")

    val filtered = remember(repairs, searchQuery, selectedStatusFilter) {
        repairs.filter { repair ->
            val matchesStatus = selectedStatusFilter == "All" || repair.status == selectedStatusFilter
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                repair.id.contains(q, ignoreCase = true) ||
                repair.customerName.contains(q, ignoreCase = true) ||
                repair.phoneNumber.contains(q, ignoreCase = true) ||
                repair.brandName.contains(q, ignoreCase = true) ||
                repair.modelName.contains(q, ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = spec.horizontalPadding, vertical = 10.dp)
        ) {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Repair ID, device, customer...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(statusFilters) { status ->
                            FilterChip(
                                selected = selectedStatusFilter == status,
                                onClick = { selectedStatusFilter = status },
                                label = { Text(status, fontSize = spec.sp(11)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = iCaseNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(spec.horizontalPadding), contentAlignment = Alignment.Center) {
                Text(
                    text = if (repairs.isEmpty()) "No repair bookings scheduled yet." else "No repairs match the current filter.",
                    color = Color.Gray,
                    fontSize = spec.sp(14)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = spec.horizontalPadding, vertical = 12.dp)
            ) {
                items(filtered, key = { it.id }) { repair ->
                    val complaints = JsonHelper.parseComplaints(repair.complaintsJson)
                    ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(spec.cardPadding)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = repair.id,
                                            fontSize = spec.sp(16),
                                            fontWeight = FontWeight.Black,
                                            color = iCaseNavy,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text("${repair.brandName} ${repair.modelName}", fontSize = spec.sp(14), fontWeight = FontWeight.Bold, color = iCaseRed)
                                        if (repair.userId.isNotBlank()) {
                                            Text(
                                                text = "Account: ${repair.userId.take(18)}",
                                                fontSize = spec.sp(10),
                                                color = iCaseBlue,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (repair.status) {
                                                    "Delivered" -> iCaseGreen
                                                    "Repair Completed" -> Color(0xFF0F766E)
                                                    "Repairing" -> iCaseRed
                                                    else -> iCaseNavy
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = repair.status,
                                            fontSize = spec.sp(11),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Customer: ${repair.customerName} (${repair.phoneNumber})", fontSize = spec.sp(12), fontWeight = FontWeight.Medium)
                                Text("Pickup: ${repair.preferredDate} - ${repair.preferredTimeSlot}", fontSize = spec.sp(12), color = iCaseBlue, fontWeight = FontWeight.Bold)
                                Text("Address: ${repair.pickupAddress}", fontSize = spec.sp(12), color = Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Complaints: ${complaints.joinToString(", ")}", fontSize = spec.sp(12), color = iCaseRed, fontWeight = FontWeight.SemiBold)
                                if (repair.description.isNotBlank()) {
                                    Text("Notes: ${repair.description}", fontSize = spec.sp(11), color = Color(0xFF475569))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    if (maxWidth < 360.dp) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = { onReschedule(repair) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 44.dp)
                                                ) {
                                                    Text("Reschedule", fontSize = spec.sp(12), maxLines = 1, softWrap = false)
                                                }
                                                IconButton(
                                                    onClick = { onDeleteRepair(repair) },
                                                    modifier = Modifier.testTag("admin_delete_repair_${repair.id}")
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Repair", tint = iCaseRed)
                                                }
                                            }
                                            Button(
                                                onClick = { onUpdateStatus(repair) },
                                                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 44.dp)
                                                    .testTag("admin_update_repair_${repair.id}")
                                            ) {
                                                Text("Change Status", fontSize = spec.sp(12), fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { onReschedule(repair) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 44.dp)
                                            ) {
                                                Text("Reschedule", fontSize = spec.sp(12), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }

                                            Button(
                                                onClick = { onUpdateStatus(repair) },
                                                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 44.dp)
                                                    .testTag("admin_update_repair_${repair.id}")
                                            ) {
                                                Text("Change Status", fontSize = spec.sp(12), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }

                                            IconButton(
                                                onClick = { onDeleteRepair(repair) },
                                                modifier = Modifier.testTag("admin_delete_repair_${repair.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Repair", tint = iCaseRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminProductsTab(
    products: List<ProductEntity>,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit,
    onOpenExportCode: () -> Unit = {}
) {
    val spec = LocalResponsiveSpec.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filtered = remember(products, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchesCat = selectedCategory == "All" || prod.category == selectedCategory
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                prod.name.contains(q, ignoreCase = true) ||
                prod.category.contains(q, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = spec.horizontalPadding, vertical = 10.dp)
        ) {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search catalog products...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(InitialData.categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = spec.sp(11)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = iCaseBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onOpenExportCode,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 42.dp)
                            .testTag("admin_products_export_code_button")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = iCaseNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export Default Code for GitHub (No-DB Live Site)",
                            fontSize = spec.sp(12),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = spec.horizontalPadding, end = spec.horizontalPadding, top = 12.dp, bottom = 96.dp)
        ) {
            items(filtered, key = { it.id }) { product ->
                ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PouchThumbImage(
                                imageUrl = product.imageUrl,
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(if (spec.isCompactSmall) 48.dp else 56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                val prodColors = remember(product.colorOptionsJson) {
                                    JsonHelper.parseColorOptions(product.colorOptionsJson)
                                }
                                Text(
                                    text = product.name,
                                    fontSize = spec.sp(13),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${product.category} • ₹${"%.0f".format(product.price)} • Qty: ${product.stockCount}",
                                    fontSize = spec.sp(11),
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (product.inStock) "In Stock" else "Out of Stock",
                                        fontSize = spec.sp(10),
                                        color = if (product.inStock) iCaseGreen else iCaseRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (product.isOneHourEligible) {
                                        Text("⚡ 1-Hour", fontSize = spec.sp(10), color = iCaseRed, fontWeight = FontWeight.Bold)
                                    }
                                    if (product.isPopular) {
                                        Text("HOME ★ #${product.popularityRank}", fontSize = spec.sp(10), color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (prodColors.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        prodColors.take(6).forEach { opt ->
                                            val swatchColor = remember(opt.hex) {
                                                try {
                                                    Color(android.graphics.Color.parseColor(opt.hex))
                                                } catch (_: Exception) {
                                                    Color(0xFF1E293B)
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(swatchColor)
                                                    .border(0.75.dp, Color(0xFF94A3B8), CircleShape)
                                            )
                                        }
                                        Text(
                                            text = "${prodColors.size} color(s)",
                                            fontSize = spec.sp(10),
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onEdit(product) },
                                modifier = Modifier.testTag("admin_edit_product_${product.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Product", tint = iCaseBlue)
                            }

                            IconButton(
                                onClick = { onDelete(product) },
                                modifier = Modifier.testTag("admin_delete_product_${product.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Product", tint = iCaseRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateOrderStatusDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onUpdate: (newStatus: String, notes: String) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    val statuses = listOf("Order Received", "Processing", "Out for Delivery", "Delivered")
    var selectedStatus by remember { mutableStateOf(order.status) }
    var notes by remember { mutableStateOf(order.statusNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Order Status (${order.id})", fontWeight = FontWeight.Bold, fontSize = spec.sp(16)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Select new pipeline status:", fontSize = spec.sp(13), color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                statuses.forEach { st ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedStatus = st }
                            .background(if (selectedStatus == st) iCaseLightBlue else Color.Transparent)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = st,
                            fontSize = spec.sp(14),
                            fontWeight = if (selectedStatus == st) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedStatus == st) iCaseBlue else Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Status Notes (visible to customer)") },
                    placeholder = { Text("e.g. Rider assigned, parcel in transit") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdate(selectedStatus, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseBlue),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Confirm Update")
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
fun UpdateRepairStatusDialog(
    repair: RepairBookingEntity,
    onDismiss: () -> Unit,
    onUpdate: (newStatus: String, notes: String) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    val statuses = listOf(
        "Booking Received",
        "Pickup Scheduled",
        "Device Received",
        "Repairing",
        "Repair Completed",
        "Out for Delivery",
        "Delivered"
    )
    var selectedStatus by remember { mutableStateOf(repair.status) }
    var notes by remember { mutableStateOf(repair.adminNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Repair Status (${repair.id})", fontWeight = FontWeight.Bold, fontSize = spec.sp(16)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Device: ${repair.brandName} ${repair.modelName}", fontSize = spec.sp(13), fontWeight = FontWeight.Bold, color = iCaseNavy)
                Spacer(modifier = Modifier.height(8.dp))

                statuses.forEach { st ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 42.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedStatus = st }
                            .background(if (selectedStatus == st) iCaseRedLight else Color.Transparent)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = st,
                            fontSize = spec.sp(13),
                            fontWeight = if (selectedStatus == st) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedStatus == st) iCaseRed else Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Technician Diagnostic Notes") },
                    placeholder = { Text("e.g. Screen replaced, passing QA tests") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdate(selectedStatus, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Update Repair")
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
fun RescheduleRepairDialog(
    repair: RepairBookingEntity,
    onDismiss: () -> Unit,
    onSave: (newDate: String, newSlot: String, notes: String) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var newDate by remember { mutableStateOf(repair.preferredDate) }
    var newSlot by remember { mutableStateOf(repair.preferredTimeSlot) }
    var notes by remember { mutableStateOf(repair.adminNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reschedule Pickup (${repair.id})", fontWeight = FontWeight.Bold, fontSize = spec.sp(16)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = newDate,
                    onValueChange = { newDate = it },
                    label = { Text("Rescheduled Date (e.g., Tomorrow, 12 Nov)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newSlot,
                    onValueChange = { newSlot = it },
                    label = { Text("Time Window (e.g., 03:00 PM - 05:00 PM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Reason / Reschedule Note") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(newDate, newSlot, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseBlue),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Save Reschedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val categoryPresets = InitialData.categories.filter { it != "All" }
    val defaultProductImageUrl = "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500"
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Cases") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toInt()?.toString() ?: "499") }
    var originalPriceStr by remember { mutableStateOf(product?.originalPrice?.toInt()?.toString() ?: "799") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: defaultProductImageUrl) }
    var videoUrl by remember { mutableStateOf(product?.videoUrl ?: "") }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var isProcessingVideo by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var videoStatus by remember { mutableStateOf<String?>(null) }
    var showVideoPreviewDialog by remember { mutableStateOf(false) }
    var inStock by remember { mutableStateOf(product?.inStock ?: true) }
    var stockCountStr by remember { mutableStateOf(product?.stockCount?.toString() ?: "25") }
    var isOneHourEligible by remember { mutableStateOf(product?.isOneHourEligible ?: true) }
    var isPopular by remember { mutableStateOf(product?.isPopular ?: false) }
    var popularityRankStr by remember { mutableStateOf(product?.popularityRank?.toString() ?: "10") }

    var colorOptions by remember(product?.id) {
        mutableStateOf(JsonHelper.parseColorOptions(product?.colorOptionsJson ?: "[]"))
    }
    var editingColorIndex by remember { mutableStateOf<Int?>(null) }
    var colorNameInput by remember { mutableStateOf("") }
    var colorHexInput by remember { mutableStateOf("#1B2A4A") }

    fun applyCompressedPhoto(uri: Uri) {
        scope.launch {
            isProcessingPhoto = true
            photoError = null
            val dataUri = withContext(Dispatchers.IO) {
                PouchImageHelper.compressImageUriToDataUrl(context, uri)
            }
            if (dataUri != null) {
                imageUrl = dataUri
            } else {
                photoError = "Could not process selected image."
            }
            isProcessingPhoto = false
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) applyCompressedPhoto(uri)
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isProcessingVideo = true
                videoStatus = null
                val (resolvedUrl, msg) = withContext(Dispatchers.IO) {
                    PouchImageHelper.saveShortVideoUriToUrl(context, uri)
                }
                if (resolvedUrl != null) {
                    videoUrl = resolvedUrl
                    videoStatus = msg ?: "Short video ready."
                } else {
                    videoStatus = msg ?: "Could not load video."
                }
                isProcessingVideo = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) applyCompressedPhoto(uri)
    }

    val maxDialogBodyHeight = (spec.availableHeight * 0.62f).coerceIn(240.dp, 540.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (product != null) "Edit Product" else "Add New Product", fontWeight = FontWeight.Bold, fontSize = spec.sp(16))
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxDialogBodyHeight)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Category:", fontSize = spec.sp(12), fontWeight = FontWeight.Bold, color = iCaseNavy)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categoryPresets) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = spec.sp(11)) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Selling Price (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = originalPriceStr,
                            onValueChange = { originalPriceStr = it },
                            label = { Text("Original MRP (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val mrp = originalPriceStr.toDoubleOrNull() ?: priceStr.toDoubleOrNull() ?: 0.0
                        listOf(10, 20, 30, 50).forEach { pct ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    if (mrp > 0.0) {
                                        val discounted = (mrp * (1.0 - pct / 100.0)).toInt().coerceAtLeast(1)
                                        priceStr = discounted.toString()
                                    }
                                },
                                label = { Text("-$pct% Offer", fontSize = spec.sp(10.5f)) }
                            )
                        }
                        FilterChip(
                            selected = false,
                            onClick = {
                                val p = priceStr.toDoubleOrNull() ?: mrp
                                if (p > 0.0) originalPriceStr = p.toInt().toString()
                            },
                            label = { Text("No Offer", fontSize = spec.sp(10.5f)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                            .testTag("admin_product_image_upload_box")
                    ) {
                        Text(
                            text = "Product Image Upload / URL",
                            fontSize = spec.sp(12.5f),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isProcessingPhoto) {
                                    CircularProgressIndicator(color = iCaseNavy, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                } else if (imageUrl.isNotBlank()) {
                                    PouchThumbImage(
                                        imageUrl = imageUrl,
                                        contentDescription = "Product Preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(26.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .testTag("admin_product_gallery_button")
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = iCaseNavy, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gallery", fontSize = spec.sp(11), fontWeight = FontWeight.Bold, color = iCaseNavy)
                                }
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val uri = PouchImageHelper.createCameraTempUri(context)
                                            pendingCameraUri = uri
                                            cameraLauncher.launch(uri)
                                        } catch (_: Exception) {
                                            photoError = "Camera unavailable. Use Gallery or Image URL."
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .testTag("admin_product_camera_button")
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = iCaseNavy, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Camera", fontSize = spec.sp(11), fontWeight = FontWeight.Bold, color = iCaseNavy)
                                }
                                if (imageUrl != defaultProductImageUrl) {
                                    TextButton(
                                        onClick = { imageUrl = defaultProductImageUrl },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.heightIn(min = 40.dp)
                                    ) {
                                        Text("Reset", fontSize = spec.sp(11), fontWeight = FontWeight.Bold, color = iCaseRed)
                                    }
                                }
                            }
                        }
                        if (photoError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(photoError!!, color = iCaseRed, fontSize = spec.sp(11), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = if (imageUrl.startsWith("data:image", ignoreCase = true)) "" else imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Or Paste Image URL") },
                            placeholder = { Text("https://...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Optional Product Video Block (URL / YouTube / Short Clip)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                            .testTag("admin_product_video_upload_box")
                    ) {
                        Text(
                            text = "Optional Product Video (MP4 URL, YouTube, or Short Clip)",
                            fontSize = spec.sp(12),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.heightIn(min = 38.dp)
                            ) {
                                if (isProcessingVideo) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Videocam, contentDescription = null, tint = iCaseNavy, modifier = Modifier.size(15.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload Video", fontSize = spec.sp(11), fontWeight = FontWeight.Bold, color = iCaseNavy)
                            }
                            if (videoUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { showVideoPreviewDialog = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.heightIn(min = 38.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = iCaseBlue, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Preview Video", fontSize = spec.sp(11), fontWeight = FontWeight.Bold, color = iCaseBlue)
                                }
                                TextButton(
                                    onClick = {
                                        videoUrl = ""
                                        videoStatus = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.heightIn(min = 38.dp)
                                ) {
                                    Text("Remove", fontSize = spec.sp(11), fontWeight = FontWeight.Bold, color = iCaseRed)
                                }
                            }
                        }
                        if (videoStatus != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(videoStatus!!, fontSize = spec.sp(11), color = Color(0xFF475569))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = if (videoUrl.startsWith("data:video", ignoreCase = true)) "" else videoUrl,
                            onValueChange = { videoUrl = it },
                            label = { Text("Video URL or YouTube Link (Optional)") },
                            placeholder = { Text("https://youtu.be/... or https://.../demo.mp4") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = stockCountStr,
                            onValueChange = { stockCountStr = it },
                            label = { Text("Stock Qty") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = popularityRankStr,
                            onValueChange = { popularityRankStr = it },
                            label = { Text("Pop. Rank") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Product Color Variants Manager
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                            .testTag("admin_color_manager")
                    ) {
                        Text(
                            text = "Color Variants (${colorOptions.size})",
                            fontSize = spec.sp(13),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Text(
                            text = "Add, edit, or remove color options shown on Product Details.",
                            fontSize = spec.sp(11),
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (colorOptions.isNotEmpty()) {
                            colorOptions.forEachIndexed { idx, opt ->
                                val previewSwatch = remember(opt.hex) {
                                    try {
                                        Color(android.graphics.Color.parseColor(opt.hex))
                                    } catch (_: Exception) {
                                        Color(0xFF1E293B)
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (editingColorIndex == idx) iCaseLightBlue else Color.White)
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(previewSwatch)
                                            .border(1.dp, Color(0xFF94A3B8), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(opt.name, fontSize = spec.sp(12), fontWeight = FontWeight.SemiBold, color = iCaseNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(opt.hex, fontSize = spec.sp(10), color = Color(0xFF64748B))
                                    }
                                    IconButton(
                                        onClick = {
                                            editingColorIndex = idx
                                            colorNameInput = opt.name
                                            colorHexInput = opt.hex
                                        },
                                        modifier = Modifier.testTag("admin_edit_color_$idx")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Color", tint = iCaseBlue, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            colorOptions = colorOptions.filterIndexed { i, _ -> i != idx }
                                            if (editingColorIndex == idx) {
                                                editingColorIndex = null
                                                colorNameInput = ""
                                                colorHexInput = "#1B2A4A"
                                            } else if ((editingColorIndex ?: -1) > idx) {
                                                editingColorIndex = editingColorIndex!! - 1
                                            }
                                        },
                                        modifier = Modifier.testTag("admin_delete_color_$idx")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove Color", tint = iCaseRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text("Quick Presets:", fontSize = spec.sp(11), fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(JsonHelper.presetColors) { preset ->
                                val presetCol = remember(preset.hex) {
                                    try {
                                        Color(android.graphics.Color.parseColor(preset.hex))
                                    } catch (_: Exception) {
                                        Color(0xFF1E293B)
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            colorNameInput = preset.name
                                            colorHexInput = preset.hex
                                        }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(presetCol)
                                            .border(1.dp, Color(0xFF94A3B8), CircleShape)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = colorNameInput,
                                onValueChange = { colorNameInput = it },
                                label = { Text("Color Name") },
                                placeholder = { Text("Midnight Blue") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("admin_color_name_input")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = colorHexInput,
                                onValueChange = { colorHexInput = it },
                                label = { Text("Hex") },
                                placeholder = { Text("#1B2A4A") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_color_hex_input")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val liveHexColor = remember(colorHexInput) {
                                val norm = JsonHelper.normalizeHexColor(colorHexInput)
                                try {
                                    Color(android.graphics.Color.parseColor(norm))
                                } catch (_: Exception) {
                                    Color(0xFF1E293B)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(liveHexColor)
                                    .border(1.dp, Color(0xFF64748B), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val cleanName = colorNameInput.trim()
                                    if (cleanName.isNotBlank()) {
                                        val entry = ProductColorOption(
                                            name = cleanName,
                                            hex = JsonHelper.normalizeHexColor(colorHexInput)
                                        )
                                        val idx = editingColorIndex
                                        colorOptions = if (idx != null && idx in colorOptions.indices) {
                                            colorOptions.mapIndexed { i, existing -> if (i == idx) entry else existing }
                                        } else {
                                            colorOptions + entry
                                        }
                                        editingColorIndex = null
                                        colorNameInput = ""
                                        colorHexInput = "#1B2A4A"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("admin_save_color_variant_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (editingColorIndex != null) "Update Color" else "Add Color",
                                    fontSize = spec.sp(12),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (editingColorIndex != null) {
                                TextButton(
                                    onClick = {
                                        editingColorIndex = null
                                        colorNameInput = ""
                                        colorHexInput = "#1B2A4A"
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("Cancel Edit", fontSize = spec.sp(12))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Product In Stock", fontSize = spec.sp(13), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(
                            checked = inStock,
                            onCheckedChange = { inStock = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iCaseGreen)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isOneHourEligible, onCheckedChange = { isOneHourEligible = it })
                        Text("Eligible for Express 1-Hour Delivery", fontSize = spec.sp(12), modifier = Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPopular,
                            onCheckedChange = { isPopular = it },
                            modifier = Modifier.testTag("admin_product_show_on_home_checkbox")
                        )
                        Text("Show on Home Page (Featured Carousel)", fontSize = spec.sp(12), modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = ProductEntity(
                        id = product?.id ?: ("prod_" + System.currentTimeMillis()),
                        name = name.trim().ifBlank { "Untitled Product" },
                        category = category.trim().ifBlank { "Accessories" },
                        description = description.trim(),
                        price = priceStr.toDoubleOrNull() ?: 499.0,
                        originalPrice = originalPriceStr.toDoubleOrNull() ?: 799.0,
                        imageUrl = imageUrl.trim().ifBlank { "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500" },
                        videoUrl = videoUrl.trim(),
                        inStock = inStock,
                        stockCount = stockCountStr.toIntOrNull() ?: 20,
                        isOneHourEligible = isOneHourEligible,
                        isPopular = isPopular,
                        popularityRank = popularityRankStr.toIntOrNull() ?: (product?.popularityRank ?: 10),
                        colorOptionsJson = JsonHelper.serializeColorOptions(colorOptions)
                    )
                    onSave(p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseBlue),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancel")
            }
        }
    )

    if (showVideoPreviewDialog && videoUrl.isNotBlank()) {
        com.example.ui.components.MediaVideoPlayerDialog(
            title = name.ifBlank { "Product Video Preview" },
            videoUrl = videoUrl,
            onDismiss = { showVideoPreviewDialog = false }
        )
    }
}

@Composable
fun ExportDefaultCodeDialog(
    exportCode: String,
    onDismiss: () -> Unit,
    onCopied: () -> Unit
) {
    val spec = LocalResponsiveSpec.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export Default Code for GitHub",
                fontWeight = FontWeight.Bold,
                fontSize = spec.sp(16),
                color = iCaseNavy
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = (spec.availableHeight * 0.58f).coerceIn(240.dp, 460.dp))
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = iCaseLightBlue,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "1. Copy this code or Share it to your PC/repo.\n2. Replace INITIAL_PRODUCTS, DEFAULT_SETTINGS & BAKED_SCREEN_GUARD_CONFIG in companion-website/src/data/constants.js and public/js/data.js.\n3. Commit & push to GitHub — your live site will show all updated images, prices, offers & items without a database!",
                        fontSize = spec.sp(11.5f),
                        color = iCaseNavy,
                        lineHeight = spec.sp(16),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 240.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = exportCode,
                        fontSize = spec.sp(10.5f),
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        try {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "iCASE default-data-export.js")
                                putExtra(Intent.EXTRA_TEXT, exportCode)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Export Code"))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportCode))
                        onCopied()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .testTag("export_copy_code_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Code")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 44.dp)) {
                Text("Close")
            }
        }
    )
}
