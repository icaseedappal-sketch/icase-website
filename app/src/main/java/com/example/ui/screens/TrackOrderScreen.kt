package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.OrderEntity
import com.example.data.OrderItemJson
import com.example.data.RepairBookingEntity
import com.example.data.ReviewEligibilityHelper
import com.example.data.TrackingResult
import com.example.ui.AppViewModel
import com.example.ui.components.VerifiedReviewDialog
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackOrderScreen(
    viewModel: AppViewModel,
    initialId: String? = null,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    val trackingQuery by viewModel.trackingQuery.collectAsState()
    var searchId by remember(trackingQuery, initialId) {
        mutableStateOf(initialId ?: trackingQuery)
    }
    val trackingResult by viewModel.trackingResult.collectAsState()
    val isSearching by viewModel.isSearchingTracking.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    var reviewRefToLaunch by remember { mutableStateOf<String?>(null) }
    var reviewSubmittedBanner by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialId) {
        if (!initialId.isNullOrBlank()) {
            searchId = initialId
            viewModel.searchTracking(initialId)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("track_order_screen"),
        contentPadding = PaddingValues(bottom = if (spec.useNavRail) 24.dp else 100.dp)
    ) {
        // Pure White Top Bar with Back Arrow and "Track Order"
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = iCaseNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Track Order",
                        fontSize = spec.sp(18),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Search Input Card
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spec.horizontalPadding),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text(
                            text = "Enter Reference Number",
                            fontSize = spec.sp(14),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchId,
                                onValueChange = {
                                    searchId = it
                                    if (it.isBlank()) {
                                        viewModel.clearTracking()
                                    }
                                },
                                placeholder = { Text("e.g. ORD-10294 or REP-5819", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        if (searchId.isNotBlank()) {
                                            viewModel.searchTracking(searchId.trim())
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("track_search_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (searchId.isNotBlank()) {
                                        viewModel.searchTracking(searchId.trim())
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .heightIn(min = 52.dp)
                                    .testTag("track_search_button")
                            ) {
                                if (isSearching) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tracking Result Content
        when (val res = trackingResult) {
            is TrackingResult.NotFound -> {
                item {
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spec.horizontalPadding),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = iCaseRed,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Order Not Found",
                                    fontSize = spec.sp(18),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "We could not find an active order matching \"$searchId\". Please check your ID and retry.",
                                    fontSize = spec.sp(13),
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            is TrackingResult.ProductOrder -> {
                if (reviewSubmittedBanner != null) {
                    item {
                        ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spec.horizontalPadding, vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8FDF2)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = reviewSubmittedBanner!!,
                                    color = Color(0xFF065F46),
                                    fontSize = spec.sp(12.5f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Screen9ProductOrderCard(
                            order = res.order,
                            items = res.items,
                            onWriteReview = { reviewRefToLaunch = res.order.id }
                        )
                    }
                }
            }
            is TrackingResult.Repair -> {
                if (reviewSubmittedBanner != null) {
                    item {
                        ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spec.horizontalPadding, vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2FF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = reviewSubmittedBanner!!,
                                    color = iCaseNavy,
                                    fontSize = spec.sp(12.5f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Screen9RepairBookingCard(
                            repair = res.repair,
                            complaints = res.complaints,
                            onWriteReview = { reviewRefToLaunch = res.repair.id }
                        )
                    }
                }
            }
            null -> {
                item {
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spec.horizontalPadding),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = iCaseNavy,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Track Your Order or Repair",
                                    fontSize = spec.sp(17),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Enter your Order ID or Repair Booking ID above and tap search to view live tracking details.",
                                    fontSize = spec.sp(14),
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (reviewRefToLaunch != null) {
        VerifiedReviewDialog(
            viewModel = viewModel,
            initialReferenceId = reviewRefToLaunch,
            defaultCustomerName = customerProfile.name,
            onDismiss = { reviewRefToLaunch = null },
            onSubmitted = { msg ->
                reviewRefToLaunch = null
                reviewSubmittedBanner = msg
            }
        )
    }
}

/**
 * Real Product Order Timeline matching Screen 9 styling
 */
@Composable
fun Screen9ProductOrderCard(
    order: OrderEntity,
    items: List<OrderItemJson>,
    onWriteReview: (() -> Unit)? = null
) {
    val spec = LocalResponsiveSpec.current
    val steps = listOf("Order Placed", "Packed", "Out for Delivery", "Delivered")
    val currentStepIndex = when (order.status) {
        "Delivered" -> 3
        "Out for Delivery" -> 2
        "Processing" -> 1
        else -> 0
    }

    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateHeaderFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val orderDate = Date(order.createdAt)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spec.horizontalPadding)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(spec.cardPadding)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${order.id}",
                            fontSize = spec.sp(18),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dateHeaderFormat.format(orderDate),
                            fontSize = spec.sp(13),
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (order.status == "Delivered") Color(0xFFE8FDF2) else Color(0xFFFFF1F2))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = order.status,
                            fontSize = spec.sp(12),
                            fontWeight = FontWeight.Bold,
                            color = if (order.status == "Delivered") Color(0xFF10B981) else iCaseRed,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Timeline
                steps.forEachIndexed { index, stepName ->
                    val isCompleted = index <= currentStepIndex
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompleted) Color(0xFF10B981) else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (index < steps.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height(36.dp)
                                        .background(if (index < currentStepIndex) Color(0xFF10B981) else Color(0xFFE2E8F0))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (index < steps.size - 1) 22.dp else 4.dp)
                        ) {
                            Text(
                                text = stepName,
                                fontSize = spec.sp(14),
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) iCaseNavy else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isCompleted) dateFormat.format(orderDate) else "Pending",
                                fontSize = spec.sp(12),
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                Text(
                    text = "Items: ${items.joinToString(", ") { "${it.quantity}x ${it.productName}" }}",
                    fontSize = spec.sp(13),
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total Paid: ₹${"%,.0f".format(order.totalAmount)} (${if (order.deliveryType == "ONE_HOUR") "⚡ 1-Hour" else "Standard"})",
                    fontSize = spec.sp(13),
                    fontWeight = FontWeight.Bold,
                    color = iCaseRed
                )
            }
        }

        if (order.status == "Delivered") {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your order has been delivered!",
                        fontSize = spec.sp(15),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Thank you for shopping with iCASE ❤️",
                        fontSize = spec.sp(13),
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    if (onWriteReview != null && ReviewEligibilityHelper.isOrderCompleted(order.status)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onWriteReview,
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("track_write_order_review_button")
                        ) {
                            Icon(imageVector = Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Write Verified Product Review", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Real Repair Booking Timeline matching Screen 9 styling
 */
@Composable
fun Screen9RepairBookingCard(
    repair: RepairBookingEntity,
    complaints: List<String>,
    onWriteReview: (() -> Unit)? = null
) {
    val spec = LocalResponsiveSpec.current
    val steps = listOf(
        "Booking Received",
        "Pickup Scheduled",
        "Device Received",
        "Repairing",
        "Repair Completed",
        "Out for Delivery",
        "Delivered"
    )
    val currentIndex = steps.indexOf(repair.status).let { if (it >= 0) it else 0 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spec.horizontalPadding)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${repair.id}",
                            fontSize = spec.sp(18),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${repair.brandName} ${repair.modelName}",
                            fontSize = spec.sp(13),
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (repair.status == "Delivered") Color(0xFFE8FDF2) else Color(0xFFFFF1F2))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = repair.status,
                            fontSize = spec.sp(12),
                            fontWeight = FontWeight.Bold,
                            color = if (repair.status == "Delivered") Color(0xFF10B981) else iCaseRed,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                steps.forEachIndexed { index, stepName ->
                    val isCompleted = index <= currentIndex
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompleted) Color(0xFF10B981) else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                }
                            }

                            if (index < steps.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height(28.dp)
                                        .background(if (index < currentIndex) Color(0xFF10B981) else Color(0xFFE2E8F0))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (index < steps.size - 1) 16.dp else 4.dp)
                        ) {
                            Text(
                                text = stepName,
                                fontSize = spec.sp(13),
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) iCaseNavy else Color.Gray
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                Text(
                    text = "Reported Issues: ${complaints.joinToString(", ")}",
                    fontSize = spec.sp(12),
                    fontWeight = FontWeight.SemiBold,
                    color = iCaseRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pickup Address: ${repair.pickupAddress}",
                    fontSize = spec.sp(12),
                    color = Color(0xFF475569)
                )

                if (onWriteReview != null && ReviewEligibilityHelper.isRepairCompleted(repair.status)) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = onWriteReview,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("track_write_repair_feedback_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = iCaseBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Private Service Feedback",
                            fontWeight = FontWeight.Bold,
                            color = iCaseBlue
                        )
                    }
                }
            }
        }

        if (repair.status == "Delivered") {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your device has been repaired and delivered!",
                        fontSize = spec.sp(15),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Thank you for trusting iCASE Edappal ❤️",
                        fontSize = spec.sp(13),
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
