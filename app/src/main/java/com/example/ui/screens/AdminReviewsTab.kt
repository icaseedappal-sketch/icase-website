package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ReviewEntity
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGold
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseLightBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminReviewsTab(
    reviews: List<ReviewEntity>,
    onApproveToggle: (ReviewEntity, Boolean) -> Unit,
    onEditReview: (
        review: ReviewEntity,
        targetTitle: String,
        customerName: String,
        rating: Int,
        comment: String,
        isApproved: Boolean
    ) -> Unit,
    onReply: (ReviewEntity, String) -> Unit,
    onDelete: (ReviewEntity) -> Unit,
    onAddManual: (
        referenceId: String,
        reviewType: String,
        targetTitle: String,
        customerName: String,
        rating: Int,
        comment: String,
        isApproved: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    var filterMode by remember { mutableStateOf("ALL") } // ALL, PRODUCT, SERVICE
    var reviewToEdit by remember { mutableStateOf<ReviewEntity?>(null) }
    var reviewToReply by remember { mutableStateOf<ReviewEntity?>(null) }
    var reviewToDelete by remember { mutableStateOf<ReviewEntity?>(null) }
    var showAddManualDialog by remember { mutableStateOf(false) }

    val filteredReviews = remember(reviews, filterMode) {
        when (filterMode) {
            "PRODUCT" -> reviews.filter { it.reviewType == "PRODUCT" }
            "SERVICE" -> reviews.filter { it.reviewType == "SERVICE" }
            else -> reviews
        }
    }

    val approvedProductCount = reviews.count { it.reviewType == "PRODUCT" && it.isApproved }
    val pendingProductCount = reviews.count { it.reviewType == "PRODUCT" && !it.isApproved }
    val privateServiceCount = reviews.count { it.reviewType == "SERVICE" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_reviews_tab"),
        contentPadding = PaddingValues(horizontal = spec.horizontalPadding, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Verified Reviews & Private Service Feedback",
                                    fontSize = spec.sp(15.5f),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy
                                )
                                Text(
                                    text = "$approvedProductCount Approved Public • $pendingProductCount Pending • $privateServiceCount Private Service",
                                    fontSize = spec.sp(12),
                                    color = Color(0xFF64748B)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showAddManualDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .testTag("admin_add_manual_review_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Review", fontSize = spec.sp(12), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = filterMode == "ALL",
                                onClick = { filterMode = "ALL" },
                                label = { Text("All (${reviews.size})", fontSize = spec.sp(12)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = iCaseNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = filterMode == "PRODUCT",
                                onClick = { filterMode = "PRODUCT" },
                                label = { Text("Product (${reviews.count { it.reviewType == "PRODUCT" }})", fontSize = spec.sp(12)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = iCaseGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = filterMode == "SERVICE",
                                onClick = { filterMode = "SERVICE" },
                                label = { Text("Service Private ($privateServiceCount)", fontSize = spec.sp(12)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = iCaseBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        if (filteredReviews.isEmpty()) {
            item {
                ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No reviews in this filter yet.",
                                fontSize = spec.sp(14),
                                fontWeight = FontWeight.SemiBold,
                                color = iCaseNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Verified customer feedback from completed orders/repairs or manual entries will appear here.",
                                fontSize = spec.sp(12),
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredReviews, key = { it.id }) { review ->
                val isService = review.reviewType == "SERVICE"
                ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_review_card_${review.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isService) Color(0xFFF8FAFC) else Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isService) iCaseBlue.copy(alpha = 0.35f) else BorderLight
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when {
                                                    isService -> iCaseLightBlue
                                                    review.isApproved -> Color(0xFFE8FDF2)
                                                    else -> Color(0xFFFEF3C7)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isService) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Private Service Feedback",
                                                    tint = iCaseBlue,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = when {
                                                    isService -> "PRIVATE SERVICE FEEDBACK • HIDDEN FROM PUBLIC"
                                                    review.isApproved -> "PUBLIC PRODUCT REVIEW"
                                                    else -> "PENDING APPROVAL"
                                                },
                                                fontSize = spec.sp(10.5f),
                                                fontWeight = FontWeight.Black,
                                                color = when {
                                                    isService -> iCaseBlue
                                                    review.isApproved -> Color(0xFF065F46)
                                                    else -> Color(0xFF92400E)
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) { idx ->
                                        Icon(
                                            imageVector = if (idx < review.rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                            contentDescription = null,
                                            tint = if (idx < review.rating) iCaseGold else Color(0xFFCBD5E1),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${review.customerName} • Ref: ${review.referenceId}",
                                fontSize = spec.sp(14),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy
                            )
                            Text(
                                text = review.targetTitle,
                                fontSize = spec.sp(12),
                                fontWeight = FontWeight.SemiBold,
                                color = iCaseBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = review.comment,
                                fontSize = spec.sp(13),
                                color = Color(0xFF334155)
                            )

                            if (review.adminReply.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEAF2FF))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Admin Reply: ${review.adminReply}",
                                        fontSize = spec.sp(12),
                                        color = iCaseNavy,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${if (review.isVerified) "Verified • " else ""}${dateFormat.format(Date(review.createdAt))}",
                                fontSize = spec.sp(11),
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (!isService) {
                                    Button(
                                        onClick = { onApproveToggle(review, !review.isApproved) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (review.isApproved) Color(0xFFF1F5F9) else iCaseGreen,
                                            contentColor = if (review.isApproved) iCaseNavy else Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .heightIn(min = 42.dp)
                                            .testTag("admin_toggle_approve_${review.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (review.isApproved) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (review.isApproved) "Hide / Unpublish" else "Approve / Publish",
                                            fontSize = spec.sp(12),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { reviewToEdit = review },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .heightIn(min = 42.dp)
                                        .testTag("admin_edit_review_${review.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Review",
                                        tint = iCaseNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Edit",
                                        fontSize = spec.sp(12),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy
                                    )
                                }

                                OutlinedButton(
                                    onClick = { reviewToReply = review },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .heightIn(min = 42.dp)
                                        .testTag("admin_reply_review_${review.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Reply,
                                        contentDescription = "Reply",
                                        tint = iCaseBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (review.adminReply.isBlank()) "Reply" else "Edit Reply",
                                        fontSize = spec.sp(12),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseBlue
                                    )
                                }

                                OutlinedButton(
                                    onClick = { reviewToDelete = review },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, iCaseRed.copy(alpha = 0.6f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .heightIn(min = 42.dp)
                                        .testTag("admin_delete_review_${review.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = iCaseRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Delete",
                                        fontSize = spec.sp(12),
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

    // Reply Dialog
    if (reviewToReply != null) {
        val target = reviewToReply!!
        var replyText by remember(target.id) { mutableStateOf(target.adminReply) }
        AlertDialog(
            onDismissRequest = { reviewToReply = null },
            title = { Text("Reply to ${target.customerName}", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = target.comment,
                        fontSize = spec.sp(12),
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Admin Reply") },
                        placeholder = { Text("Thank you for choosing iCASE Edappal!") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_reply_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReply(target, replyText.trim())
                        reviewToReply = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy)
                ) {
                    Text("Save Reply")
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToReply = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (reviewToDelete != null) {
        val target = reviewToDelete!!
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = { Text("Delete Review?", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = { Text("Permanently remove this review by ${target.customerName} (${target.referenceId})?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(target)
                        reviewToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Manual Add Verified Review Dialog
    if (showAddManualDialog) {
        var reviewType by remember { mutableStateOf("PRODUCT") }
        var refId by remember { mutableStateOf("ORD-") }
        var targetTitle by remember { mutableStateOf("") }
        var customerName by remember { mutableStateOf("") }
        var rating by remember { mutableIntStateOf(5) }
        var comment by remember { mutableStateOf("") }
        var approvePublic by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddManualDialog = false },
            title = { Text("Add Verified Review", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = reviewType == "PRODUCT",
                            onClick = { reviewType = "PRODUCT" },
                            label = { Text("Product Purchase") }
                        )
                        FilterChip(
                            selected = reviewType == "SERVICE",
                            onClick = {
                                reviewType = "SERVICE"
                                approvePublic = false
                            },
                            label = { Text("Service (Private)") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = refId,
                        onValueChange = { refId = it; errorMsg = null },
                        label = { Text("Order / Service Reference ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = targetTitle,
                        onValueChange = { targetTitle = it; errorMsg = null },
                        label = { Text(if (reviewType == "PRODUCT") "Product Name" else "Device / Repair Service") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it; errorMsg = null },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rating:", fontSize = spec.sp(12), fontWeight = FontWeight.SemiBold, color = iCaseNavy)
                        Spacer(modifier = Modifier.width(4.dp))
                        for (s in 1..5) {
                            IconButton(
                                onClick = { rating = s },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (s <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Rate $s",
                                    tint = if (s <= rating) iCaseGold else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it; errorMsg = null },
                        label = { Text("Review Comment") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (reviewType == "PRODUCT") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = approvePublic,
                                onCheckedChange = { approvePublic = it },
                                colors = CheckboxDefaults.colors(checkedColor = iCaseGreen)
                            )
                            Text("Publish immediately to public reviews", fontSize = spec.sp(12))
                        }
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Service feedback is always saved strictly private in Admin Panel.",
                            fontSize = spec.sp(11.5f),
                            color = iCaseBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(errorMsg!!, color = iCaseRed, fontSize = spec.sp(12))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customerName.isBlank() || comment.isBlank() || targetTitle.isBlank()) {
                            errorMsg = "Please enter customer name, item/device title, and comment."
                            return@Button
                        }
                        onAddManual(
                            refId.trim().ifBlank { if (reviewType == "SERVICE") "REP-MANUAL" else "ORD-MANUAL" },
                            reviewType,
                            targetTitle.trim(),
                            customerName.trim(),
                            rating,
                            comment.trim(),
                            if (reviewType == "PRODUCT") approvePublic else false
                        )
                        showAddManualDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed)
                ) {
                    Text("Save Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddManualDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Review Dialog
    if (reviewToEdit != null) {
        val target = reviewToEdit!!
        val isService = target.reviewType == "SERVICE"
        var editTitle by remember(target.id) { mutableStateOf(target.targetTitle) }
        var editName by remember(target.id) { mutableStateOf(target.customerName) }
        var editRating by remember(target.id) { mutableIntStateOf(target.rating.coerceIn(1, 5)) }
        var editComment by remember(target.id) { mutableStateOf(target.comment) }
        var editApproved by remember(target.id) { mutableStateOf(!isService && target.isApproved) }
        var editErr by remember(target.id) { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { reviewToEdit = null },
            title = { Text("Edit Review", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it; editErr = null },
                        label = { Text(if (isService) "Device / Service" else "Product Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_edit_review_title_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it; editErr = null },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_edit_review_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Star Rating", fontSize = spec.sp(12), fontWeight = FontWeight.Bold, color = iCaseNavy)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { editRating = star },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= editRating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "$star star",
                                    tint = if (star <= editRating) iCaseGold else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = editComment,
                        onValueChange = { editComment = it; editErr = null },
                        label = { Text("Review Comment") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_edit_review_comment_input")
                    )
                    if (!isService) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = editApproved,
                                onCheckedChange = { editApproved = it },
                                colors = CheckboxDefaults.colors(checkedColor = iCaseGreen)
                            )
                            Text("Approved / Published on Product Page", fontSize = spec.sp(12))
                        }
                    }
                    if (editErr != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(editErr!!, color = iCaseRed, fontSize = spec.sp(12))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank() || editComment.isBlank() || editTitle.isBlank()) {
                            editErr = "Product/device name, customer name, and comment are required."
                            return@Button
                        }
                        onEditReview(
                            target,
                            editTitle.trim(),
                            editName.trim(),
                            editRating,
                            editComment.trim(),
                            if (isService) false else editApproved
                        )
                        reviewToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("admin_save_edited_review_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToEdit = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Cancel")
                }
            }
        )
    }
}
