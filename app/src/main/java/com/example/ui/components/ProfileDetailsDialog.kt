package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CustomerProfile
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed

@Composable
fun ProfileDetailsDialog(
    title: String,
    initialProfile: CustomerProfile,
    initialName: String = initialProfile.name,
    initialPhone: String = initialProfile.phone,
    initialWhatsapp: String = initialProfile.whatsapp,
    initialAddress: String = initialProfile.address,
    showAddressField: Boolean = false,
    requireAddress: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        phone: String,
        whatsapp: String,
        address: String
    ) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var whatsapp by remember { mutableStateOf(initialWhatsapp) }
    var address by remember { mutableStateOf(initialAddress) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = iCaseNavy,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = iCaseNavy,
                    fontSize = spec.sp(18)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Update your contact and delivery information synced with your Google account.",
                    fontSize = spec.sp(13),
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        errorMessage = null
                    },
                    label = { Text("Mobile Number (e.g. 9846012345)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_phone_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp Number (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showAddressField) {
                    Spacer(modifier = Modifier.height(10.dp))
                    GpsDetectLocationButton(
                        onAddressDetected = { detected ->
                            address = detected
                            errorMessage = null
                        },
                        buttonLabel = "Use GPS / Detect Current Location",
                        testTag = "dialog_gps_detect_button"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            errorMessage = null
                        },
                        label = { Text("Delivery Address") },
                        placeholder = { Text("House / Flat No., Street, Landmark, City, PIN") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_address_input")
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
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
                    val cleanName = name.trim()
                    if (cleanName.isBlank()) {
                        errorMessage = "Please enter your full name."
                        return@Button
                    }
                    val digits = phone.filter { it.isDigit() }
                    if (digits.length < 10) {
                        errorMessage = "Please enter a valid 10-digit mobile number."
                        return@Button
                    }
                    if (requireAddress && address.trim().isBlank()) {
                        errorMessage = "Please enter your delivery address."
                        return@Button
                    }
                    val cleanPhone = phone.trim()
                    val finalWhatsapp = whatsapp.trim().ifBlank { cleanPhone }
                    onSave(cleanName, cleanPhone, finalWhatsapp, address.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("save_profile_details_button")
            ) {
                Text("Save Details", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
