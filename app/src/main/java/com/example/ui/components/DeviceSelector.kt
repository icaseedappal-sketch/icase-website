package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DeviceCatalog
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchableDeviceSelector(
    selectedBrand: String,
    selectedModel: String,
    onDeviceSelected: (brand: String, model: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    var showModelPickerDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Select Brand",
            fontSize = spec.sp(12),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DeviceCatalog.majorBrands.forEach { brand ->
                val isSelected = selectedBrand.equals(brand, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val nextModel = if (isSelected) selectedModel else ""
                        onDeviceSelected(brand, nextModel)
                        searchQuery = ""
                        showModelPickerDialog = true
                    },
                    label = {
                        Text(
                            text = brand,
                            fontSize = spec.sp(12),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = iCaseNavy,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = BackgroundLight,
                        labelColor = iCaseNavy
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("device_brand_chip_${brand.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Device Model (Searchable Dropdown)",
            fontSize = spec.sp(12),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    searchQuery = ""
                    showModelPickerDialog = true
                }
                .testTag("repair_device_dropdown_trigger"),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(
                width = 1.dp,
                color = if (selectedModel.isNotBlank()) iCaseBlue else Color(0xFFCBD5E1)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = if (selectedModel.isNotBlank()) iCaseRed else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (selectedBrand.isNotBlank() && selectedModel.isNotBlank()) {
                            Text(
                                text = selectedBrand,
                                fontSize = spec.sp(11),
                                fontWeight = FontWeight.Bold,
                                color = iCaseBlue
                            )
                            Text(
                                text = selectedModel,
                                fontSize = spec.sp(14),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy
                            )
                        } else if (selectedBrand.isNotBlank()) {
                            Text(
                                text = "Tap to search & choose $selectedBrand model...",
                                fontSize = spec.sp(14),
                                color = Color(0xFF475569)
                            )
                        } else {
                            Text(
                                text = "Search & select Brand & Model...",
                                fontSize = spec.sp(14),
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Open Device Selector",
                    tint = iCaseNavy
                )
            }
        }
    }

    if (showModelPickerDialog) {
        var modalBrand by remember { mutableStateOf(selectedBrand) }
        val filteredOptions = remember(modalBrand, searchQuery) {
            DeviceCatalog.searchModels(modalBrand, searchQuery)
        }
        val listMaxHeight = (spec.availableHeight * 0.34f).coerceIn(160.dp, 300.dp)

        AlertDialog(
            onDismissRequest = { showModelPickerDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (modalBrand.isBlank()) "Select Your Device" else "Select $modalBrand Model",
                        fontSize = spec.sp(17),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showModelPickerDialog = false },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Brand filter row inside dialog (All + 8 major brands)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = modalBrand.isBlank(),
                            onClick = { modalBrand = "" },
                            label = { Text("All Brands", fontSize = spec.sp(11)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = iCaseRed,
                                selectedLabelColor = Color.White
                            )
                        )
                        DeviceCatalog.majorBrands.forEach { brand ->
                            FilterChip(
                                selected = modalBrand.equals(brand, ignoreCase = true),
                                onClick = { modalBrand = brand },
                                label = { Text(brand, fontSize = spec.sp(11)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = iCaseNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search model (e.g. iPhone 15, S24, Nord 4)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search device",
                                tint = Color(0xFF64748B)
                            )
                        },
                        trailingIcon = if (searchQuery.isNotBlank()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("device_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = listMaxHeight)
                    ) {
                        if (filteredOptions.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No matching preset model found.",
                                        fontSize = spec.sp(13),
                                        color = Color(0xFF64748B)
                                    )
                                    if (searchQuery.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                val chosenBrand = modalBrand.ifBlank { "Other" }
                                                onDeviceSelected(chosenBrand, searchQuery.trim())
                                                showModelPickerDialog = false
                                            },
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        ) {
                                            Text(
                                                text = "Use \"${searchQuery.trim()}\"",
                                                color = iCaseBlue,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredOptions, key = { "${it.brand}_${it.model}" }) { option ->
                                val isCurrent = selectedBrand.equals(option.brand, ignoreCase = true) &&
                                    selectedModel.equals(option.model, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCurrent) Color(0xFFEAF2FF) else Color.Transparent)
                                        .clickable {
                                            onDeviceSelected(option.brand, option.model)
                                            showModelPickerDialog = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 12.dp)
                                        .testTag("device_option_${option.brand.lowercase()}_${option.model.lowercase().replace(" ", "_")}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = option.model,
                                            fontSize = spec.sp(14),
                                            fontWeight = FontWeight.SemiBold,
                                            color = iCaseNavy
                                        )
                                        if (modalBrand.isBlank()) {
                                            Text(
                                                text = option.brand,
                                                fontSize = spec.sp(11),
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = iCaseBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showModelPickerDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Close", color = iCaseNavy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
