package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.InitialData
import com.example.data.RepairBookingEntity
import com.example.ui.AppViewModel
import com.example.ui.components.GpsDetectLocationButton
import com.example.ui.components.SearchableDeviceSelector
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RepairPickupScreen(
    viewModel: AppViewModel,
    onTrackRepair: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    val customerProfile by viewModel.customerProfile.collectAsState()
    val storePhone by viewModel.storePhone.collectAsState()
    val storeEmail by viewModel.storeEmail.collectAsState()

    var brandName by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }
    val selectedComplaints = remember { mutableStateListOf<String>() }
    var problemDescription by remember { mutableStateOf("") }
    var photoUriString by remember { mutableStateOf<String?>(null) }

    var pickupAddress by remember(customerProfile) { mutableStateOf(customerProfile.address) }
    var customerName by remember(customerProfile) { mutableStateOf(customerProfile.name) }
    var phoneNumber by remember(customerProfile) { mutableStateOf(customerProfile.phone) }
    var whatsappNumber by remember(customerProfile) { mutableStateOf(customerProfile.whatsapp) }

    var preferredDate by remember { mutableStateOf("Today") }
    var preferredTimeSlot by remember { mutableStateOf("10:00 AM - 12:00 PM") }

    var validationError by remember { mutableStateOf<String?>(null) }
    var bookedRepair by remember { mutableStateOf<RepairBookingEntity?>(null) }

    // Photo picker launcher (Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUriString = uri.toString()
        }
    }

    if (bookedRepair != null) {
        RepairSuccessDialog(
            repair = bookedRepair!!,
            onDismiss = { bookedRepair = null },
            onTrackRepair = {
                val repId = bookedRepair!!.id
                bookedRepair = null
                onTrackRepair(repId)
            },
            onSendWhatsApp = {
                viewModel.sendWhatsAppRepairNotification(bookedRepair!!)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("repair_pickup_screen"),
        contentPadding = PaddingValues(bottom = if (spec.useNavRail) 24.dp else 100.dp)
    ) {
        // Top Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = iCaseNavy,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .padding(horizontal = spec.horizontalPadding, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iCaseRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repair & Pickup Service",
                            fontSize = spec.sp(18),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "ONE Combined Service • Free Doorstep Pickup",
                            fontSize = spec.sp(11),
                            color = Color(0xFFBAC7D5)
                        )
                    }
                }
            }
        }

        // Section 1: Searchable Device Selector
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text(
                            text = "1. Device Information",
                            fontSize = spec.sp(15),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SearchableDeviceSelector(
                            selectedBrand = brandName,
                            selectedModel = modelName,
                            onDeviceSelected = { brand, model ->
                                brandName = brand
                                modelName = model
                                validationError = null
                            }
                        )
                    }
                }
            }
        }

        // Section 2: Complaint Selection
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text(
                            text = "2. Select Issue / Complaint(s)",
                            fontSize = spec.sp(15),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Text(
                            text = "Select all issues that apply to your device",
                            fontSize = spec.sp(12),
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Multi-select complaint chips with responsive FlowRow
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            InitialData.complaintOptions.forEach { complaint ->
                                val isSelected = selectedComplaints.contains(complaint)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedComplaints.remove(complaint)
                                        else selectedComplaints.add(complaint)
                                    },
                                    label = {
                                        Text(
                                            text = complaint,
                                            fontSize = spec.sp(12),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = iCaseBlue,
                                        selectedLabelColor = Color.White,
                                        containerColor = BackgroundLight,
                                        labelColor = Color(0xFF334155)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.testTag("complaint_chip_${complaint.lowercase().replace(" ", "_")}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = problemDescription,
                            onValueChange = { problemDescription = it },
                            label = { Text("Detailed Problem Description") },
                            placeholder = { Text("Describe what happened (e.g. cracked glass, touch not working, phone gets hot while charging)...") },
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("repair_description_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Optional Photo Upload
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = iCaseBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (photoUriString != null) "Photo Attached (Change Photo)" else "Upload Optional Photo of Damage",
                                color = iCaseBlue,
                                fontSize = spec.sp(12),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (photoUriString != null) {
                            Text(
                                text = "✓ Diagnostic photo attached for technician review.",
                                color = iCaseGreen,
                                fontSize = spec.sp(11),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Pickup Address
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text(
                            text = "3. Doorstep Pickup Address",
                            fontSize = spec.sp(15),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Text(
                            text = "Our verified agent will collect your device from this location",
                            fontSize = spec.sp(12),
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        GpsDetectLocationButton(
                            onAddressDetected = { detected ->
                                pickupAddress = detected
                                validationError = null
                            },
                            buttonLabel = "Use GPS / Detect Current Location",
                            testTag = "use_current_location_button"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = pickupAddress,
                            onValueChange = { pickupAddress = it },
                            label = { Text("Or Manually Enter Address / Landmark") },
                            placeholder = { Text("House / Flat No., Street, Landmark, City, PIN") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("repair_address_input")
                        )
                    }
                }
            }
        }

        // Section 4: Customer Contact Details
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text(
                            text = "4. Customer Contact Details",
                            fontSize = spec.sp(15),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer Name") },
                            placeholder = { Text("e.g. Rahul Sharma") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("repair_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            if (maxWidth < 420.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it },
                                        label = { Text("Phone Number") },
                                        placeholder = { Text("e.g. +91 98460 12345") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("repair_phone_input")
                                    )
                                    OutlinedTextField(
                                        value = whatsappNumber,
                                        onValueChange = { whatsappNumber = it },
                                        label = { Text("WhatsApp Number") },
                                        placeholder = { Text("e.g. +91 98460 12345") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("repair_whatsapp_input")
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it },
                                        label = { Text("Phone Number") },
                                        placeholder = { Text("e.g. +91 98460 12345") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("repair_phone_input")
                                    )
                                    OutlinedTextField(
                                        value = whatsappNumber,
                                        onValueChange = { whatsappNumber = it },
                                        label = { Text("WhatsApp Number") },
                                        placeholder = { Text("e.g. +91 98460 12345") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("repair_whatsapp_input")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Preferred Pickup Schedule
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Text(
                            text = "5. Preferred Pickup Date & Time",
                            fontSize = spec.sp(15),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Pickup Date:", fontSize = spec.sp(12), color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Today", "Tomorrow", "In 2 Days").forEach { dateOption ->
                                val isSel = preferredDate == dateOption
                                FilterChip(
                                    selected = isSel,
                                    onClick = { preferredDate = dateOption },
                                    label = { Text(dateOption, fontSize = spec.sp(12), maxLines = 1) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = iCaseBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Time Window:", fontSize = spec.sp(12), color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "10:00 AM - 12:00 PM",
                                "02:00 PM - 04:00 PM",
                                "05:00 PM - 07:00 PM"
                            ).forEach { slot ->
                                val isSel = preferredTimeSlot == slot
                                FilterChip(
                                    selected = isSel,
                                    onClick = { preferredTimeSlot = slot },
                                    label = { Text(slot, fontSize = spec.sp(12), maxLines = 1) },
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
        }

        // Strict Compliance Notice: No repair prices & No 1-Hour delivery for repairs
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spec.horizontalPadding, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = iCaseBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Repair diagnosis is free. Exact quote is provided after technician inspection of your device.",
                            fontSize = spec.sp(11),
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }

        // Validation error display & Confirm Button
        item {
            ResponsiveCenteredBox(maxWidth = spec.formMaxWidth) {
                Column(modifier = Modifier.padding(spec.horizontalPadding)) {
                    AnimatedVisibility(visible = validationError != null) {
                        Text(
                            text = validationError ?: "",
                            color = iCaseRed,
                            fontSize = spec.sp(13),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (brandName.isBlank()) {
                                validationError = "Please select your device brand."
                                return@Button
                            }
                            if (modelName.isBlank()) {
                                validationError = "Please select your device model."
                                return@Button
                            }
                            if (selectedComplaints.isEmpty()) {
                                validationError = "Please select at least one complaint / issue."
                                return@Button
                            }
                            if (pickupAddress.isBlank()) {
                                validationError = "Please provide or detect your pickup address."
                                return@Button
                            }
                            if (customerName.isBlank() || phoneNumber.isBlank()) {
                                validationError = "Please enter customer name and phone number."
                                return@Button
                            }

                            validationError = null
                            // Save profile
                            viewModel.updateCustomerProfile(customerName, phoneNumber, whatsappNumber, pickupAddress)

                            // Book repair
                            viewModel.bookRepair(
                                brandName = brandName,
                                modelName = modelName,
                                complaints = selectedComplaints.toList(),
                                description = problemDescription,
                                photoUri = photoUriString,
                                pickupAddress = pickupAddress,
                                customerName = customerName,
                                phoneNumber = phoneNumber,
                                whatsappNumber = whatsappNumber,
                                preferredDate = preferredDate,
                                preferredTimeSlot = preferredTimeSlot,
                                onSuccess = { booking ->
                                    bookedRepair = booking
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("confirm_repair_booking_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm Repair & Pickup Booking",
                            fontSize = spec.sp(15),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Customer Support banner in Repair
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = iCaseBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Questions about doorstep repair? Call $storePhone or email $storeEmail",
                            fontSize = spec.sp(12),
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.components.BrandFooterBanner()
        }
    }
}

@Composable
fun RepairSuccessDialog(
    repair: RepairBookingEntity,
    onDismiss: () -> Unit,
    onTrackRepair: () -> Unit,
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
                        .background(iCaseBlue),
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
                        text = "Booking Confirmed!",
                        fontSize = spec.sp(18),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Text(
                        text = "Booking ID: ${repair.id}",
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
                    text = "Your repair pickup has been scheduled. Our technician will arrive during your preferred time slot.",
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
                            Text("Device:", fontSize = spec.sp(12), color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${repair.brandName} ${repair.modelName}",
                                fontSize = spec.sp(12),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pickup Slot:", fontSize = spec.sp(12), color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${repair.preferredDate} (${repair.preferredTimeSlot})",
                                fontSize = spec.sp(12),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pickup Address:", fontSize = spec.sp(12), color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = repair.pickupAddress,
                                fontSize = spec.sp(11),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSendWhatsApp,
                    shape = RoundedCornerShape(8.dp),
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
                onClick = onTrackRepair,
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Track Repair Status")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Done")
            }
        }
    )
}
