package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.data.GoogleAuthHelper
import com.example.data.JsonHelper
import com.example.data.ProductEntity
import com.example.data.ReviewEligibilityHelper
import com.example.ui.AppViewModel
import com.example.ui.components.GpsDetectLocationButton
import com.example.ui.components.VerifiedReviewDialog
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun DedicatedPageTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    backTestTag: String
) {
    val spec = LocalResponsiveSpec.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag(backTestTag)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = iCaseNavy
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = spec.sp(18),
                    fontWeight = FontWeight.Bold,
                    color = iCaseNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = spec.sp(12),
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DedicatedAccountRequiredCard(
    viewModel: AppViewModel,
    message: String
) {
    val spec = LocalResponsiveSpec.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var authError by remember { mutableStateOf<String?>(null) }

    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spec.horizontalPadding),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    fontSize = spec.sp(14),
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )
                if (authError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = authError!!,
                        fontSize = spec.sp(12),
                        color = iCaseRed,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        authError = null
                        GoogleAuthHelper.onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            scope = scope,
                            onAuthSuccess = { user -> viewModel.onGoogleUserAuthenticated(user) },
                            onAuthError = { err -> authError = err }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("dedicated_google_signin_button")
                ) {
                    Text("Sign in with Google", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MyOrdersScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onExploreShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val spec = LocalResponsiveSpec.current
    val currentUserId by viewModel.currentUserId.collectAsState()
    val myOrders by viewModel.myOrders.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    var reviewOrderRef by remember { mutableStateOf<String?>(null) }
    var feedbackBanner by remember { mutableStateOf<String?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .testTag("my_orders_screen")
    ) {
        DedicatedPageTopBar(
            title = "My Orders",
            subtitle = if (currentUserId.isNotBlank()) "${myOrders.size} order(s) synced" else "Sign in required",
            onBack = onBack,
            backTestTag = "my_orders_back_button"
        )

        if (currentUserId.isBlank()) {
            DedicatedAccountRequiredCard(
                viewModel = viewModel,
                message = "Sign in with Google to view your personal order history synced across mobile and web."
            )
        } else if (myOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF1F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = iCaseRed,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Orders Yet",
                            fontSize = spec.sp(19),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Orders placed from your account on Android or the iCASE website appear here in real time.",
                            fontSize = spec.sp(13),
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onExploreShop,
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Explore Shop →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = spec.horizontalPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myOrders, key = { it.id }) { order ->
                    val items = JsonHelper.parseOrderItems(order.itemsJson)
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrackOrder(order.id) }
                                .testTag("my_order_card_${order.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(spec.cardPadding)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${order.id}",
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy,
                                        fontSize = spec.sp(16),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (order.status == "Delivered") Color(0xFFE8FDF2) else Color(0xFFFFF1F2))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = order.status,
                                            fontSize = spec.sp(11),
                                            fontWeight = FontWeight.Bold,
                                            color = if (order.status == "Delivered") iCaseGreen else iCaseRed,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = items.joinToString(", ") { "${it.quantity}x ${it.productName}" },
                                    fontSize = spec.sp(13),
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Placed: ${dateFormat.format(Date(order.createdAt))}",
                                    fontSize = spec.sp(11),
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "₹${"%,.0f".format(order.totalAmount)}",
                                        fontSize = spec.sp(16),
                                        fontWeight = FontWeight.Black,
                                        color = iCaseRed
                                    )
                                    Text(
                                        text = "Track Order →",
                                        fontSize = spec.sp(13),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseBlue
                                    )
                                }
                                if (ReviewEligibilityHelper.isOrderCompleted(order.status)) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { reviewOrderRef = order.id },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 44.dp)
                                            .testTag("my_order_review_${order.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RateReview,
                                            contentDescription = null,
                                            tint = iCaseRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Write Verified Review",
                                            fontSize = spec.sp(12.5f),
                                            fontWeight = FontWeight.Bold,
                                            color = iCaseRed
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

    if (feedbackBanner != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { feedbackBanner = null },
            title = { Text("Review Submitted", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = { Text(feedbackBanner!!) },
            confirmButton = {
                Button(
                    onClick = { feedbackBanner = null },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy)
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (reviewOrderRef != null) {
        VerifiedReviewDialog(
            viewModel = viewModel,
            initialReferenceId = reviewOrderRef,
            defaultCustomerName = customerProfile.name,
            onDismiss = { reviewOrderRef = null },
            onSubmitted = { msg ->
                reviewOrderRef = null
                feedbackBanner = msg
            }
        )
    }
}

@Composable
fun MyRepairsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onTrackRepair: (String) -> Unit,
    onBookRepair: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val spec = LocalResponsiveSpec.current
    val currentUserId by viewModel.currentUserId.collectAsState()
    val myRepairs by viewModel.myRepairs.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    var reviewRepairRef by remember { mutableStateOf<String?>(null) }
    var feedbackBanner by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .testTag("my_repairs_screen")
    ) {
        DedicatedPageTopBar(
            title = "My Repairs",
            subtitle = if (currentUserId.isNotBlank()) "${myRepairs.size} booking(s) synced" else "Sign in required",
            onBack = onBack,
            backTestTag = "my_repairs_back_button"
        )

        if (currentUserId.isBlank()) {
            DedicatedAccountRequiredCard(
                viewModel = viewModel,
                message = "Sign in with Google to track your doorstep repair & pickup bookings."
            )
        } else if (myRepairs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEAF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = iCaseBlue,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Repair Bookings Yet",
                            fontSize = spec.sp(19),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Need a display, battery, or charging repair? Book doorstep pickup in seconds.",
                            fontSize = spec.sp(13),
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onBookRepair,
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Book Repair Pickup →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = spec.horizontalPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myRepairs, key = { it.id }) { repair ->
                    val complaints = JsonHelper.parseComplaints(repair.complaintsJson)
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrackRepair(repair.id) }
                                .testTag("my_repair_card_${repair.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(spec.cardPadding)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${repair.id}",
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy,
                                        fontSize = spec.sp(16),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (repair.status == "Delivered") Color(0xFFE8FDF2) else Color(0xFFE0F2FE))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = repair.status,
                                            fontSize = spec.sp(11),
                                            fontWeight = FontWeight.Bold,
                                            color = if (repair.status == "Delivered") iCaseGreen else iCaseBlue,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${repair.brandName} ${repair.modelName}",
                                    fontSize = spec.sp(14),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Issues: ${complaints.joinToString(", ")}",
                                    fontSize = spec.sp(12),
                                    color = Color(0xFF475569)
                                )
                                if (repair.description.isNotBlank()) {
                                    Text(
                                        text = repair.description,
                                        fontSize = spec.sp(12),
                                        color = Color(0xFF64748B),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${repair.preferredDate} • ${repair.preferredTimeSlot}",
                                        fontSize = spec.sp(12),
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Track Repair →",
                                        fontSize = spec.sp(13),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseBlue
                                    )
                                }
                                if (ReviewEligibilityHelper.isRepairCompleted(repair.status)) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { reviewRepairRef = repair.id },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 44.dp)
                                            .testTag("my_repair_review_${repair.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RateReview,
                                            contentDescription = null,
                                            tint = iCaseBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Share Private Service Feedback",
                                            fontSize = spec.sp(12.5f),
                                            fontWeight = FontWeight.Bold,
                                            color = iCaseBlue
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

    if (feedbackBanner != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { feedbackBanner = null },
            title = { Text("Feedback Sent", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = { Text(feedbackBanner!!) },
            confirmButton = {
                Button(
                    onClick = { feedbackBanner = null },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy)
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (reviewRepairRef != null) {
        VerifiedReviewDialog(
            viewModel = viewModel,
            initialReferenceId = reviewRepairRef,
            defaultCustomerName = customerProfile.name,
            onDismiss = { reviewRepairRef = null },
            onSubmitted = { msg ->
                reviewRepairRef = null
                feedbackBanner = msg
            }
        )
    }
}

@Composable
fun MyAddressesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val spec = LocalResponsiveSpec.current
    val profile by viewModel.customerProfile.collectAsState()
    val scope = rememberCoroutineScope()
    var addressText by remember(profile.address) { mutableStateOf(profile.address) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .testTag("my_addresses_screen")
    ) {
        DedicatedPageTopBar(
            title = "My Addresses",
            subtitle = "Primary delivery & repair pickup address",
            onBack = onBack,
            backTestTag = "my_addresses_back_button"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = spec.horizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (profile.address.isNotBlank()) {
                item {
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(spec.cardPadding),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8FDF2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = iCaseGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Saved Address",
                                            fontSize = spec.sp(14),
                                            fontWeight = FontWeight.Bold,
                                            color = iCaseNavy
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = iCaseGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = profile.address,
                                        fontSize = spec.sp(13),
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(spec.cardPadding)) {
                            Text(
                                text = if (profile.address.isBlank()) "Add Delivery / Pickup Address" else "Update Address",
                                fontSize = spec.sp(16),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Detect your exact live location via GPS or enter your complete address manually.",
                                fontSize = spec.sp(12),
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            GpsDetectLocationButton(
                                onAddressDetected = { detected ->
                                    addressText = detected
                                    validationError = null
                                },
                                buttonLabel = "Use GPS / Detect Current Location",
                                testTag = "address_gps_detect_button"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = addressText,
                                onValueChange = {
                                    addressText = it
                                    validationError = null
                                },
                                label = { Text("Full Address & Landmark") },
                                placeholder = { Text("House / Flat No., Street, Landmark, City, PIN") },
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("my_address_input")
                            )

                            if (validationError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = validationError!!,
                                    fontSize = spec.sp(12),
                                    color = iCaseRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val clean = addressText.trim()
                                    if (clean.isBlank()) {
                                        validationError = "Please detect your location or enter an address."
                                        return@Button
                                    }
                                    viewModel.updateCustomerAddress(clean)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Address saved & synced")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("save_address_button")
                            ) {
                                Text("Save Address", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onExploreShop: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val spec = LocalResponsiveSpec.current
    val allProducts by viewModel.allProducts.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val scope = rememberCoroutineScope()

    val savedProducts = remember(allProducts, wishlistIds) {
        allProducts.filter { wishlistIds.contains(it.id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .testTag("wishlist_screen")
    ) {
        DedicatedPageTopBar(
            title = "Wishlist",
            subtitle = "${savedProducts.size} saved item(s)",
            onBack = onBack,
            backTestTag = "wishlist_back_button"
        )

        if (savedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF1F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = iCaseRed,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Your Wishlist is Empty",
                            fontSize = spec.sp(19),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the heart icon on any accessory to save it here and sync across your devices.",
                            fontSize = spec.sp(13),
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onExploreShop,
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Explore Shop →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = spec.horizontalPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedProducts, key = { it.id }) { product ->
                    ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductClick(product) }
                                .testTag("wishlist_item_${product.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spec.cardPadding)
                            ) {
                                val thumbSize = if (maxWidth < 330.dp) 64.dp else 76.dp
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(thumbSize)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF1F5F9))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.category,
                                            fontSize = spec.sp(11),
                                            fontWeight = FontWeight.SemiBold,
                                            color = iCaseBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = product.name,
                                            fontSize = spec.sp(14),
                                            fontWeight = FontWeight.Bold,
                                            color = iCaseNavy,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "₹${"%,.0f".format(product.price)}",
                                                fontSize = spec.sp(15),
                                                fontWeight = FontWeight.Black,
                                                color = iCaseRed
                                            )
                                            if (product.originalPrice > product.price) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "₹${"%,.0f".format(product.originalPrice)}",
                                                    fontSize = spec.sp(12),
                                                    color = Color.Gray,
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleWishlist(product.id)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Removed ${product.name} from Wishlist")
                                                }
                                            },
                                            modifier = Modifier.testTag("wishlist_remove_${product.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Remove from Wishlist",
                                                tint = iCaseRed
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.addToCart(product)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Added ${product.name} to cart")
                                                }
                                            },
                                            enabled = product.inStock,
                                            colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.heightIn(min = 38.dp)
                                        ) {
                                            Text(
                                                text = if (product.inStock) "Add" else "Sold Out",
                                                fontSize = spec.sp(11),
                                                fontWeight = FontWeight.Bold,
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
            }
        }
    }
}
