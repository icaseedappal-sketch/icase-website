package com.example.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.PouchAdminItem
import com.example.data.ReviewEntity
import com.example.data.ReviewVerificationResult
import com.example.data.ScreenGuardAdminConfig
import com.example.data.ScreenGuardCatalog
import com.example.ui.AppViewModel
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGold
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseLightBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemperedGlassFinderCard(
    config: ScreenGuardAdminConfig,
    onAddToCart: (brand: String, model: String, glassType: String) -> Unit,
    modifier: Modifier = Modifier,
    onSelectionChanged: (brand: String, model: String, glassType: String) -> Unit = { _, _, _ -> },
    onWhatsAppUvEnquiry: () -> Unit = {}
) {
    val spec = LocalResponsiveSpec.current

    var selectedBrand by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("") }
    var manualModelInput by remember { mutableStateOf("") }
    var selectedGlassType by remember { mutableStateOf("Normal 5D") }
    var showModelDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    val activeBrands = remember(config) {
        ScreenGuardCatalog.activeBrands(config)
    }
    val isManualMode = selectedModel == ScreenGuardCatalog.OTHER_MODEL_OPTION
    val availableGlassTypes = remember(selectedBrand, selectedModel, config) {
        ScreenGuardCatalog.glassTypesForDevice(selectedBrand, selectedModel, config)
    }

    LaunchedEffect(config, selectedBrand, selectedModel) {
        if (selectedModel.isNotBlank() && !isManualMode) {
            if (!ScreenGuardCatalog.isTemperedGlassModelAvailable(selectedBrand, selectedModel, config)) {
                selectedModel = ""
            }
        }
    }

    LaunchedEffect(availableGlassTypes) {
        if (availableGlassTypes.isNotEmpty() && !availableGlassTypes.contains(selectedGlassType)) {
            selectedGlassType = availableGlassTypes.first()
        }
    }

    LaunchedEffect(selectedBrand, selectedModel, manualModelInput, selectedGlassType, availableGlassTypes) {
        val effBrand = if (isManualMode) "CUSTOM" else selectedBrand
        val effModel = if (isManualMode) manualModelInput.trim() else selectedModel
        if (effModel.isNotBlank() && availableGlassTypes.contains(selectedGlassType)) {
            onSelectionChanged(effBrand, effModel, selectedGlassType)
        } else {
            onSelectionChanged("", "", "")
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tempered_glass_finder_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(spec.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iCaseLightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Tempered Glass",
                        tint = iCaseBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tempered Glass",
                        fontSize = spec.sp(16f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Text(
                        text = "Select your brand & model for exact tempered glass",
                        fontSize = spec.sp(12),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Filter by Brand",
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
                activeBrands.forEach { brand ->
                    val isSelected = selectedBrand.equals(brand, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                selectedBrand = brand
                                if (selectedModel != ScreenGuardCatalog.OTHER_MODEL_OPTION) {
                                    selectedModel = ""
                                }
                            }
                            searchQuery = ""
                            validationMessage = null
                            showModelDialog = true
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
                        modifier = Modifier.testTag("glass_brand_chip_${brand.lowercase().replace("/", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Phone Model (Searchable Dropdown)",
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
                        showModelDialog = true
                    }
                    .testTag("glass_model_dropdown_trigger"),
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
                            if (selectedModel.isNotBlank()) {
                                Text(
                                    text = if (isManualMode) "Manual Entry" else selectedBrand,
                                    fontSize = spec.sp(11),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseBlue
                                )
                                Text(
                                    text = selectedModel,
                                    fontSize = spec.sp(14),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = if (selectedBrand.isNotBlank()) {
                                        "Tap to pick $selectedBrand model or Other..."
                                    } else {
                                        "Search & select Brand / Model..."
                                    },
                                    fontSize = spec.sp(13.5f),
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Open model selector",
                        tint = iCaseNavy
                    )
                }
            }

            if (isManualMode) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = manualModelInput,
                    onValueChange = {
                        manualModelInput = it
                        validationMessage = null
                    },
                    label = { Text("Enter Your Exact Device Model") },
                    placeholder = { Text("e.g. Motorola Edge 50 Pro, Google Pixel 9") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("glass_manual_model_input")
                )
            }

            if (availableGlassTypes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Select Tempered Glass Type",
                    fontSize = spec.sp(12),
                    fontWeight = FontWeight.Bold,
                    color = iCaseNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableGlassTypes.forEach { glassType ->
                        val active = selectedGlassType == glassType
                        val chipPricing = ScreenGuardCatalog.resolveTemperedGlassPrice(
                            brand = if (isManualMode) "CUSTOM" else selectedBrand,
                            model = if (isManualMode) manualModelInput else selectedModel,
                            glassType = glassType,
                            config = config
                        )
                        FilterChip(
                            selected = active,
                            onClick = { selectedGlassType = glassType },
                            label = {
                                Text(
                                    text = "$glassType • ₹${chipPricing.finalPrice.roundToInt()}",
                                    fontSize = spec.sp(12),
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (active) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = iCaseRed,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = Color(0xFFF8FAFC),
                                labelColor = iCaseNavy
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.testTag(
                                "glass_type_chip_${glassType.lowercase().replace(" ", "_")}"
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("glass_type_helper_note")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = iCaseBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ScreenGuardCatalog.glassTypeHelperNote(selectedGlassType, config),
                        fontSize = spec.sp(11.5f),
                        color = Color(0xFF334155),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else if (selectedModel.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Tempered glass options for this device are currently paused by the store.",
                        fontSize = spec.sp(12),
                        color = iCaseRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (selectedBrand.equals("Samsung", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFF6FF))
                        .clickable { onWhatsAppUvEnquiry() }
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                        .testTag("samsung_curved_uv_whatsapp_note")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Contact on WhatsApp for UV glass",
                        tint = iCaseBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Curved display S series? Contact on WhatsApp for UV glass options.",
                        fontSize = spec.sp(11.5f),
                        fontWeight = FontWeight.SemiBold,
                        color = iCaseNavy,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (validationMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = validationMessage!!,
                    fontSize = spec.sp(12),
                    fontWeight = FontWeight.SemiBold,
                    color = iCaseRed
                )
            }

            if (selectedModel.isNotBlank() && availableGlassTypes.isNotEmpty()) {
                val effectiveModelForPrice = if (isManualMode) {
                    manualModelInput.trim().ifBlank { ScreenGuardCatalog.OTHER_MODEL_OPTION }
                } else {
                    selectedModel
                }
                val resolvedPricing = ScreenGuardCatalog.resolveTemperedGlassPrice(
                    brand = if (isManualMode) "CUSTOM" else selectedBrand,
                    model = effectiveModelForPrice,
                    glassType = selectedGlassType,
                    config = config
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$selectedGlassType Tempered Glass",
                            fontSize = spec.sp(12.5f),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (resolvedPricing.offerBadge.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = resolvedPricing.offerBadge,
                                    fontSize = spec.sp(10),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (resolvedPricing.basePrice > resolvedPricing.finalPrice) {
                            Text(
                                text = "₹${resolvedPricing.basePrice.roundToInt()}",
                                fontSize = spec.sp(12),
                                color = Color(0xFF94A3B8),
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "₹${resolvedPricing.finalPrice.roundToInt()}",
                            fontSize = spec.sp(16),
                            fontWeight = FontWeight.Black,
                            color = iCaseNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val effectiveModel = if (isManualMode) manualModelInput.trim() else selectedModel
                        if (effectiveModel.isBlank()) {
                            validationMessage = "Please enter your exact device model name first."
                            return@Button
                        }
                        validationMessage = null
                        onAddToCart(
                            if (isManualMode) "CUSTOM" else selectedBrand,
                            effectiveModel,
                            selectedGlassType
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("glass_add_to_cart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Cart • ₹${resolvedPricing.finalPrice.roundToInt()}",
                        fontSize = spec.sp(13.5f),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showModelDialog) {
        var modalBrandFilter by remember { mutableStateOf(selectedBrand) }
        val groupedModels = remember(modalBrandFilter, searchQuery, config) {
            ScreenGuardCatalog.searchGroupedModels(modalBrandFilter, searchQuery, config)
        }
        val listMaxHeight = (spec.availableHeight * 0.42f).coerceIn(180.dp, 340.dp)

        AlertDialog(
            onDismissRequest = { showModelDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (modalBrandFilter.isBlank()) "Select Phone Model" else "$modalBrandFilter Models",
                        fontSize = spec.sp(17),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showModelDialog = false },
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
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = modalBrandFilter.isBlank(),
                            onClick = { modalBrandFilter = "" },
                            label = { Text("All Brands", fontSize = spec.sp(11)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = iCaseRed,
                                selectedLabelColor = Color.White
                            )
                        )
                        activeBrands.forEach { brand ->
                            FilterChip(
                                selected = modalBrandFilter.equals(brand, ignoreCase = true),
                                onClick = { modalBrandFilter = brand },
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
                        placeholder = { Text("Search iPhone 15, Note 13, S24, Nord 4...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search model",
                                tint = Color(0xFF64748B)
                            )
                        },
                        trailingIcon = if (searchQuery.isNotBlank()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("glass_model_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = listMaxHeight)
                    ) {
                        if (groupedModels.isEmpty()) {
                            item {
                                Text(
                                    text = "No preset model matches \"$searchQuery\". Use Other Model below to type it manually.",
                                    fontSize = spec.sp(12.5f),
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        } else {
                            groupedModels.forEach { (brandName, models) ->
                                item(key = "header_$brandName") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF1F5F9))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = brandName,
                                            fontSize = spec.sp(11.5f),
                                            fontWeight = FontWeight.Black,
                                            color = iCaseBlue
                                        )
                                    }
                                }
                                items(models, key = { "${brandName}_$it" }) { modelName ->
                                    val isCurrent = selectedBrand == brandName && selectedModel == modelName
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .background(if (isCurrent) iCaseLightBlue else Color.Transparent)
                                            .clickable {
                                                selectedBrand = brandName
                                                selectedModel = modelName
                                                manualModelInput = ""
                                                validationMessage = null
                                                showModelDialog = false
                                            }
                                            .padding(horizontal = 10.dp, vertical = 10.dp)
                                            .testTag("glass_option_${modelName.lowercase().replace(" ", "_")}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = modelName,
                                            fontSize = spec.sp(13.5f),
                                            fontWeight = FontWeight.SemiBold,
                                            color = iCaseNavy,
                                            modifier = Modifier.weight(1f)
                                        )
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

                        item(key = "other_model_manual") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isManualMode) Color(0xFFFFECEE) else Color(0xFFF8FAFC))
                                    .clickable {
                                        selectedBrand = ""
                                        selectedModel = ScreenGuardCatalog.OTHER_MODEL_OPTION
                                        if (manualModelInput.isBlank() && searchQuery.isNotBlank()) {
                                            manualModelInput = searchQuery.trim()
                                        }
                                        validationMessage = null
                                        showModelDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                                    .testTag("glass_other_model_option"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ScreenGuardCatalog.OTHER_MODEL_OPTION,
                                        fontSize = spec.sp(13.5f),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseRed
                                    )
                                    Text(
                                        text = "Can't find your device? Type exact model & add to cart",
                                        fontSize = spec.sp(11),
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = "Manual model",
                                    tint = iCaseRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showModelDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, color = iCaseNavy)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraLensProtectionCard(
    config: ScreenGuardAdminConfig,
    onAddToCart: (brand: String, model: String) -> Unit,
    modifier: Modifier = Modifier,
    selectedGlassSelection: Triple<String, String, String>? = null,
    onAddComboToCart: (gBrand: String, gModel: String, gType: String, cBrand: String, cModel: String) -> Unit = { _, _, _, _, _ -> }
) {
    val spec = LocalResponsiveSpec.current

    var selectedBrand by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("") }
    var showModelDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val sectionAvailable = ScreenGuardCatalog.isCameraLensSectionAvailable(config)
    val activeCameraBrands = remember(config) {
        ScreenGuardCatalog.activeCameraLensBrands(config)
    }

    LaunchedEffect(config, selectedBrand, selectedModel) {
        if (selectedModel.isNotBlank() &&
            !ScreenGuardCatalog.isCameraLensModelAvailable(selectedBrand, selectedModel, config)
        ) {
            selectedModel = ""
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("camera_lens_protection_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(spec.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFECEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Lens Protection",
                        tint = iCaseRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Camera Lens Protection",
                        fontSize = spec.sp(16f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Text(
                        text = "Available exclusively for iPhone & Samsung S Series models",
                        fontSize = spec.sp(12),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!sectionAvailable || activeCameraBrands.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Camera Lens Protection is currently unavailable.",
                        fontSize = spec.sp(12),
                        color = iCaseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activeCameraBrands.forEach { brand ->
                        val active = selectedBrand.equals(brand, ignoreCase = true)
                        FilterChip(
                            selected = active,
                            onClick = {
                                if (!active) {
                                    selectedBrand = brand
                                    selectedModel = ""
                                }
                                searchQuery = ""
                                showModelDialog = true
                            },
                            label = {
                                Text(
                                    text = brand,
                                    fontSize = spec.sp(12),
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (active) {
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
                            modifier = Modifier.testTag(
                                "camera_lens_brand_${brand.lowercase().replace(" ", "_")}"
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            searchQuery = ""
                            showModelDialog = true
                        }
                        .testTag("camera_lens_dropdown_trigger"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selectedModel.isNotBlank()) iCaseRed else Color(0xFFCBD5E1)
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
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (selectedModel.isNotBlank()) iCaseRed else Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (selectedModel.isNotBlank()) {
                                    Text(
                                        text = selectedBrand,
                                        fontSize = spec.sp(11),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseRed
                                    )
                                    Text(
                                        text = selectedModel,
                                        fontSize = spec.sp(14),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "Select iPhone or Samsung S Series model...",
                                        fontSize = spec.sp(13.5f),
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Open camera lens selector",
                            tint = iCaseNavy
                        )
                    }
                }

                if (selectedModel.isNotBlank()) {
                    val camPricing = ScreenGuardCatalog.resolveCameraLensPrice(selectedBrand, selectedModel, config)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "9H Sapphire Tempered Camera Lens Ring Protection for $selectedModel",
                                fontSize = spec.sp(12),
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                            if (camPricing.offerBadge.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = camPricing.offerBadge,
                                        fontSize = spec.sp(10),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (camPricing.basePrice > camPricing.finalPrice) {
                                Text(
                                    text = "₹${camPricing.basePrice.roundToInt()}",
                                    fontSize = spec.sp(12),
                                    color = Color(0xFF94A3B8),
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "₹${camPricing.finalPrice.roundToInt()}",
                                fontSize = spec.sp(16),
                                fontWeight = FontWeight.Black,
                                color = iCaseNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onAddToCart(selectedBrand, selectedModel) },
                        colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("camera_lens_add_to_cart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add to Cart • ₹${camPricing.finalPrice.roundToInt()}",
                            fontSize = spec.sp(13.5f),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (selectedGlassSelection != null && config.comboEnabled) {
                        val (gBrand, gModel, gType) = selectedGlassSelection
                        val comboPricing = ScreenGuardCatalog.resolveComboPrice(
                            glassBrand = gBrand,
                            glassModel = gModel,
                            glassType = gType,
                            cameraBrand = selectedBrand,
                            cameraModel = selectedModel,
                            config = config
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(12.dp)
                        ) {
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
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = iCaseGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Combo Deal (${comboPricing.offerBadge.ifBlank { "BUNDLE" }})",
                                        fontSize = spec.sp(12),
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF065F46),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (comboPricing.basePrice > comboPricing.finalPrice) {
                                        Text(
                                            text = "₹${comboPricing.basePrice.roundToInt()}",
                                            fontSize = spec.sp(11.5f),
                                            color = Color(0xFF64748B),
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "₹${comboPricing.finalPrice.roundToInt()}",
                                        fontSize = spec.sp(14.5f),
                                        fontWeight = FontWeight.Black,
                                        color = iCaseNavy
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$gModel ($gType) + $selectedModel Camera Lens",
                                fontSize = spec.sp(11.5f),
                                color = Color(0xFF047857)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onAddComboToCart(gBrand, gModel, gType, selectedBrand, selectedModel)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("camera_lens_combo_add_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add Tempered Glass + Camera Lens Combo • ₹${comboPricing.finalPrice.roundToInt()}",
                                    fontSize = spec.sp(12.5f),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showModelDialog) {
        var modalBrandFilter by remember { mutableStateOf(selectedBrand) }
        val groupedModels = remember(modalBrandFilter, searchQuery, config) {
            ScreenGuardCatalog.searchCameraLensModels(modalBrandFilter, searchQuery, config)
        }
        val listMaxHeight = (spec.availableHeight * 0.42f).coerceIn(180.dp, 340.dp)

        AlertDialog(
            onDismissRequest = { showModelDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Camera Lens Models",
                        fontSize = spec.sp(17),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showModelDialog = false },
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
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = modalBrandFilter.isBlank(),
                            onClick = { modalBrandFilter = "" },
                            label = { Text("All Eligible", fontSize = spec.sp(11)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = iCaseRed,
                                selectedLabelColor = Color.White
                            )
                        )
                        activeCameraBrands.forEach { brand ->
                            FilterChip(
                                selected = modalBrandFilter.equals(brand, ignoreCase = true),
                                onClick = { modalBrandFilter = brand },
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
                        placeholder = { Text("Search iPhone 15 Pro, S24 Ultra, S25...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search camera lens model",
                                tint = Color(0xFF64748B)
                            )
                        },
                        trailingIcon = if (searchQuery.isNotBlank()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("camera_lens_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = listMaxHeight)
                    ) {
                        if (groupedModels.isEmpty()) {
                            item {
                                Text(
                                    text = "Camera Lens Protection is available only for active iPhone and Samsung S Series models.",
                                    fontSize = spec.sp(12.5f),
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        } else {
                            groupedModels.forEach { (brandName, models) ->
                                item(key = "cam_header_$brandName") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF1F5F9))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = brandName,
                                            fontSize = spec.sp(11.5f),
                                            fontWeight = FontWeight.Black,
                                            color = iCaseBlue
                                        )
                                    }
                                }
                                items(models, key = { "cam_${brandName}_$it" }) { modelName ->
                                    val isCurrent = selectedBrand == brandName && selectedModel == modelName
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .background(if (isCurrent) iCaseLightBlue else Color.Transparent)
                                            .clickable {
                                                selectedBrand = brandName
                                                selectedModel = modelName
                                                showModelDialog = false
                                            }
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = modelName,
                                            fontSize = spec.sp(13.5f),
                                            fontWeight = FontWeight.SemiBold,
                                            color = iCaseNavy,
                                            modifier = Modifier.weight(1f)
                                        )
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
                }
            },
                    confirmButton = {
                TextButton(
                    onClick = { showModelDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, color = iCaseNavy)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PouchesFinderCard(
    config: ScreenGuardAdminConfig,
    onAddPouchToCart: (brand: String, model: String, pouch: PouchAdminItem, selectedColor: String) -> Unit,
    onWhatsAppStockPhotos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    var selectedTab by remember { mutableStateOf("iPhone") }
    var selectedModel by remember { mutableStateOf("") }
    var showModelDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    val selectedColorByPouchId = remember { mutableStateMapOf<String, String>() }

    val pouchBrandOptions = remember {
        ScreenGuardCatalog.pouchBrands + ScreenGuardCatalog.OTHER_ANDROID_POUCHES_OPTION
    }
    val isOtherAndroid = selectedTab == ScreenGuardCatalog.OTHER_ANDROID_POUCHES_OPTION
    val activePouches = remember(selectedTab, config) {
        if (isOtherAndroid) emptyList() else ScreenGuardCatalog.activePouchesForBrand(selectedTab, config)
    }

    LaunchedEffect(config, selectedTab, selectedModel) {
        if (!isOtherAndroid && selectedModel.isNotBlank()) {
            if (!ScreenGuardCatalog.isPouchModelAvailable(selectedTab, selectedModel, config)) {
                selectedModel = ""
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pouches_finder_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(spec.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iCaseLightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Pouches",
                        tint = iCaseNavy,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pouches",
                        fontSize = spec.sp(16f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Text(
                        text = "iPhone & Samsung S Series ready in cart • Live photo check for Android",
                        fontSize = spec.sp(12),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pouchBrandOptions.forEach { tab ->
                    val active = selectedTab == tab
                    FilterChip(
                        selected = active,
                        onClick = {
                            if (selectedTab != tab) {
                                selectedTab = tab
                                selectedModel = ""
                                validationMessage = null
                            }
                        },
                        label = {
                            Text(
                                text = tab,
                                fontSize = spec.sp(12),
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = if (active) {
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
                        modifier = Modifier.testTag(
                            "pouch_brand_tab_${tab.lowercase().replace(" ", "_")}"
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isOtherAndroid) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Android Pouch Stock Varies Daily",
                        fontSize = spec.sp(13.5f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Designs for Redmi/Xiaomi, Oppo/Realme, Vivo/iQOO, OnePlus, Nothing & non-S Samsung rotate frequently. Message us on WhatsApp to receive live stock photos for your exact model.",
                        fontSize = spec.sp(12),
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onWhatsAppStockPhotos,
                        colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("pouch_other_android_whatsapp_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enquire on WhatsApp for Stock Photos",
                            fontSize = spec.sp(13),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            searchQuery = ""
                            showModelDialog = true
                        }
                        .testTag("pouch_dropdown_trigger"),
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
                                tint = if (selectedModel.isNotBlank()) iCaseBlue else Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (selectedModel.isNotBlank()) {
                                    Text(
                                        text = selectedTab,
                                        fontSize = spec.sp(11),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseBlue
                                    )
                                    Text(
                                        text = selectedModel,
                                        fontSize = spec.sp(14),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "Select $selectedTab model for pouch...",
                                        fontSize = spec.sp(13.5f),
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select pouch phone model",
                            tint = iCaseNavy
                        )
                    }
                }

                if (validationMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationMessage!!,
                        fontSize = spec.sp(12),
                        fontWeight = FontWeight.SemiBold,
                        color = iCaseRed
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activePouches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF2F2))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "No pouches currently active for $selectedTab.",
                            fontSize = spec.sp(12),
                            color = iCaseRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        activePouches.forEach { pouch ->
                            val pricing = ScreenGuardCatalog.resolvePouchPrice(pouch)
                            val activeColorOption = ScreenGuardCatalog.resolvePouchSelectedColor(
                                pouch = pouch,
                                selectedColor = selectedColorByPouchId[pouch.id].orEmpty()
                            )
                            val activeColorName = activeColorOption?.name.orEmpty()
                            val activeImageUrl = ScreenGuardCatalog.resolvePouchImageUrl(pouch, activeColorName)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    PouchThumbImage(
                                        imageUrl = activeImageUrl,
                                        contentDescription = pouch.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(82.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                                            .testTag("pouch_card_image_${pouch.id}")
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = pouch.name,
                                                fontSize = spec.sp(13.5f),
                                                fontWeight = FontWeight.Bold,
                                                color = iCaseNavy,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(horizontalAlignment = Alignment.End) {
                                                if (pricing.basePrice > pricing.finalPrice) {
                                                    Text(
                                                        text = "₹${pricing.basePrice.roundToInt()}",
                                                        fontSize = spec.sp(11.5f),
                                                        color = Color(0xFF94A3B8),
                                                        textDecoration = TextDecoration.LineThrough
                                                    )
                                                }
                                                Text(
                                                    text = "₹${pricing.finalPrice.roundToInt()}",
                                                    fontSize = spec.sp(15.5f),
                                                    fontWeight = FontWeight.Black,
                                                    color = iCaseNavy
                                                )
                                            }
                                        }
                                        if (pouch.description.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = pouch.description,
                                                fontSize = spec.sp(11.5f),
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        if (pricing.offerBadge.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFDCFCE7))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = pricing.offerBadge,
                                                    fontSize = spec.sp(10),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (pouch.colorOptions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Colour: $activeColorName",
                                        fontSize = spec.sp(11.5f),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        pouch.colorOptions.forEach { colorOpt ->
                                            val isSelectedColor = activeColorName.equals(colorOpt.name, ignoreCase = true)
                                            val dotColor = remember(colorOpt.hex) {
                                                try {
                                                    Color(android.graphics.Color.parseColor(colorOpt.hex))
                                                } catch (_: Exception) {
                                                    Color(0xFF1E293B)
                                                }
                                            }
                                            Surface(
                                                modifier = Modifier
                                                    .heightIn(min = 38.dp)
                                                    .clip(RoundedCornerShape(18.dp))
                                                    .clickable {
                                                        selectedColorByPouchId[pouch.id] = colorOpt.name
                                                    }
                                                    .testTag("pouch_color_${pouch.id}_${colorOpt.name.lowercase().replace(Regex("[^a-z0-9]"), "_")}"),
                                                shape = RoundedCornerShape(18.dp),
                                                color = if (isSelectedColor) iCaseLightBlue else Color.White,
                                                border = BorderStroke(
                                                    width = if (isSelectedColor) 1.5.dp else 1.dp,
                                                    color = if (isSelectedColor) iCaseNavy else Color(0xFFCBD5E1)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(dotColor)
                                                            .border(1.dp, Color(0xFF94A3B8), CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = colorOpt.name,
                                                        fontSize = spec.sp(11.5f),
                                                        fontWeight = if (isSelectedColor) FontWeight.Bold else FontWeight.Medium,
                                                        color = iCaseNavy,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (selectedModel.isBlank()) {
                                            validationMessage = "Please select your $selectedTab model above first."
                                            searchQuery = ""
                                            showModelDialog = true
                                        } else {
                                            validationMessage = null
                                            onAddPouchToCart(selectedTab, selectedModel, pouch, activeColorName)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("pouch_add_to_cart_${pouch.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            selectedModel.isBlank() -> "Select Model & Add • ₹${pricing.finalPrice.roundToInt()}"
                                            activeColorName.isNotBlank() -> "Add $activeColorName to Cart • ₹${pricing.finalPrice.roundToInt()}"
                                            else -> "Add to Cart • ₹${pricing.finalPrice.roundToInt()}"
                                        },
                                        fontSize = spec.sp(13),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showModelDialog && !isOtherAndroid) {
        val matchingModels = remember(selectedTab, searchQuery, config) {
            ScreenGuardCatalog.searchPouchModels(selectedTab, searchQuery, config)
        }
        val listMaxHeight = (spec.availableHeight * 0.42f).coerceIn(180.dp, 340.dp)

        AlertDialog(
            onDismissRequest = { showModelDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$selectedTab Pouch Models",
                        fontSize = spec.sp(17),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showModelDialog = false },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search $selectedTab model...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pouch_model_search_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = listMaxHeight)
                    ) {
                        if (matchingModels.isEmpty()) {
                            item {
                                Text(
                                    text = "No matching $selectedTab model found.",
                                    fontSize = spec.sp(12.5f),
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        } else {
                            items(matchingModels, key = { "pouch_${selectedTab}_$it" }) { modelName ->
                                val isCurrent = selectedModel == modelName
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .background(if (isCurrent) iCaseLightBlue else Color.Transparent)
                                        .clickable {
                                            selectedModel = modelName
                                            validationMessage = null
                                            showModelDialog = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = modelName,
                                        fontSize = spec.sp(13.5f),
                                        fontWeight = FontWeight.SemiBold,
                                        color = iCaseNavy,
                                        modifier = Modifier.weight(1f)
                                    )
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
                    onClick = { showModelDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, color = iCaseNavy)
                }
            }
        )
    }
}

@Composable
fun InstallationOptionCard(
    selectedOption: String,
    onSelectOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    val options = listOf(
        "Shop Installation" to "Expert dust-free installation at iCASE Edappal",
        "Delivery / Pickup" to "Sealed kit ready for home delivery or pickup"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("installation_option_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(spec.cardPadding)) {
            Text(
                text = "Installation Preference",
                fontSize = spec.sp(15),
                fontWeight = FontWeight.Bold,
                color = iCaseNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose how you prefer your Tempered Glass or Camera Lens protection fulfilled",
                fontSize = spec.sp(12),
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { (title, subtitle) ->
                    val active = selectedOption == title
                    val icon = if (title == "Shop Installation") Icons.Default.Storefront else Icons.Default.LocalShipping
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectOption(title) }
                            .testTag(if (title == "Shop Installation") "install_opt_shop" else "install_opt_delivery"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (active) iCaseLightBlue else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            width = if (active) 1.5.dp else 1.dp,
                            color = if (active) iCaseNavy else BorderLight
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (active) iCaseNavy else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = spec.sp(12.5f),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                fontSize = spec.sp(11),
                                color = Color(0xFF475569),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuardSampleVideoCard(
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    var showVideoModal by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showVideoModal = true }
            .testTag("guard_video_preview_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = iCaseNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(iCaseNavy, Color(0xFF172D4B))
                    )
                )
                .padding(spec.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(iCaseRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Watch Tempered Glass and Camera Lens Demo",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SAMPLE DEMO VIDEO",
                    fontSize = spec.sp(10),
                    fontWeight = FontWeight.Black,
                    color = iCaseGold
                )
                Text(
                    text = "See Tempered Glass & Camera Lens in Use",
                    fontSize = spec.sp(14.5f),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to watch clarity, matte finish & camera lens protection demo",
                    fontSize = spec.sp(11.5f),
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }

    if (showVideoModal) {
        SampleVideoPreviewModal(onDismiss = { showVideoModal = false })
    }
}

@Composable
fun SampleVideoPreviewModal(
    onDismiss: () -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tempered Glass & Camera Lens Demo",
                    fontSize = spec.sp(16),
                    fontWeight = FontWeight.Bold,
                    color = iCaseNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("video_modal_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close video preview",
                        tint = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (!hasError) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    videoViewRef = this
                                    setVideoURI(Uri.parse(ScreenGuardCatalog.SAMPLE_GUARD_VIDEO_URL))
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        isBuffering = false
                                        if (isPlaying) start()
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        isBuffering = false
                                        hasError = true
                                        true
                                    }
                                }
                            },
                            update = { vv ->
                                if (isPlaying && !vv.isPlaying && !isBuffering && !hasError) {
                                    vv.start()
                                } else if (!isPlaying && vv.isPlaying) {
                                    vv.pause()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (isBuffering && !hasError) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    }

                    if (hasError) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = iCaseGold,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Demo preview unavailable offline. Connect to internet or update sample video clip.",
                                fontSize = spec.sp(12),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val vv = videoViewRef
                            if (vv != null && !hasError) {
                                if (isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                }
                            }
                        },
                        enabled = !hasError,
                        colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("video_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Video" else "Play Video",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "Pause" else "Play",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Done", color = iCaseNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun VerifiedPublicReviewsSection(
    reviews: List<ReviewEntity>,
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = LocalResponsiveSpec.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Defensive strict privacy guard: ONLY approved PRODUCT reviews may ever render publicly
    val publicApprovedReviews = remember(reviews) {
        reviews.filter { it.reviewType == "PRODUCT" && it.isApproved }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("verified_public_reviews_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(spec.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Reviews",
                            tint = iCaseGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verified Customer Reviews",
                            fontSize = spec.sp(15.5f),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                    }
                    Text(
                        text = "Only verified completed buyers can submit feedback",
                        fontSize = spec.sp(11.5f),
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onWriteReviewClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, iCaseRed),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("write_verified_review_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = iCaseRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Write Review",
                        fontSize = spec.sp(12),
                        fontWeight = FontWeight.Bold,
                        color = iCaseRed,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (publicApprovedReviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "No approved product reviews published yet. Completed orders (ORD-...) can submit a verified review above or via Track Order.",
                        fontSize = spec.sp(12),
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    publicApprovedReviews.take(5).forEach { review ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp)
                                .testTag("public_review_card_${review.id}")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = review.customerName,
                                        fontSize = spec.sp(13),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        repeat(5) { idx ->
                                            Icon(
                                                imageVector = if (idx < review.rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                                contentDescription = null,
                                                tint = if (idx < review.rating) iCaseGold else Color(0xFFCBD5E1),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Purchase",
                                        tint = iCaseGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Verified Purchase • ${review.targetTitle}",
                                        fontSize = spec.sp(11),
                                        fontWeight = FontWeight.SemiBold,
                                        color = iCaseGreen,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = review.comment,
                                    fontSize = spec.sp(12.5f),
                                    color = Color(0xFF334155)
                                )

                                if (review.adminReply.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(iCaseLightBlue)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "iCASE Reply: ${review.adminReply}",
                                            fontSize = spec.sp(11.5f),
                                            color = iCaseNavy,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateFormat.format(Date(review.createdAt)),
                                    fontSize = spec.sp(10.5f),
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerifiedReviewDialog(
    viewModel: AppViewModel,
    initialReferenceId: String? = null,
    defaultCustomerName: String = "",
    onDismiss: () -> Unit,
    onSubmitted: (String) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    val scope = rememberCoroutineScope()

    var referenceId by remember(initialReferenceId) { mutableStateOf(initialReferenceId.orEmpty()) }
    var isVerifying by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<ReviewVerificationResult?>(null) }

    var customerName by remember(defaultCustomerName) { mutableStateOf(defaultCustomerName) }
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    fun verifyNow(id: String) {
        val clean = id.trim()
        if (clean.isBlank()) {
            formError = "Enter an Order ID (ORD-...) or Repair Service ID (REP-...)."
            return
        }
        isVerifying = true
        formError = null
        scope.launch {
            val res = viewModel.verifyReferenceForReview(clean)
            verificationResult = res
            when (res) {
                is ReviewVerificationResult.EligibleProduct -> {
                    if (customerName.isBlank()) customerName = res.customerName
                }
                is ReviewVerificationResult.EligibleService -> {
                    if (customerName.isBlank()) customerName = res.customerName
                }
                else -> {}
            }
            isVerifying = false
        }
    }

    LaunchedEffect(initialReferenceId) {
        if (!initialReferenceId.isNullOrBlank()) {
            verifyNow(initialReferenceId)
        }
    }

    val isEligible = verificationResult is ReviewVerificationResult.EligibleProduct ||
        verificationResult is ReviewVerificationResult.EligibleService

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Verified Customer Review",
                fontSize = spec.sp(17),
                fontWeight = FontWeight.Bold,
                color = iCaseNavy
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Only completed orders or repairs can submit verified feedback.",
                    fontSize = spec.sp(12),
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = referenceId,
                        onValueChange = {
                            referenceId = it
                            verificationResult = null
                            formError = null
                        },
                        label = { Text("Order / Service ID") },
                        placeholder = { Text("e.g. ORD-10294 or REP-5819") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("review_ref_id_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { verifyNow(referenceId) },
                        enabled = !isVerifying,
                        colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 52.dp)
                            .testTag("verify_review_id_button")
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Verify", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                when (val res = verificationResult) {
                    is ReviewVerificationResult.NotFound -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No order or repair found matching \"${referenceId.trim()}\".",
                            fontSize = spec.sp(12),
                            color = iCaseRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    is ReviewVerificationResult.NotCompleted -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Order/Service status is \"${res.currentStatus}\". Reviews open once completed/delivered.",
                            fontSize = spec.sp(12),
                            color = iCaseRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    is ReviewVerificationResult.EligibleProduct -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8FDF2))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Verified Purchase: ${res.targetTitle}\nProduct reviews are published after Admin approval.",
                                fontSize = spec.sp(11.5f),
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    is ReviewVerificationResult.EligibleService -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(iCaseLightBlue)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Verified Repair: ${res.targetTitle}\nService feedback is private for iCASE Admin review only.",
                                fontSize = spec.sp(11.5f),
                                color = iCaseNavy,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    null -> {}
                }

                if (isEligible) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {
                            customerName = it
                            formError = null
                        },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_customer_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your Rating",
                        fontSize = spec.sp(12),
                        fontWeight = FontWeight.SemiBold,
                        color = iCaseNavy
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (star in 1..5) {
                            IconButton(
                                onClick = { rating = star },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("review_star_$star")
                            ) {
                                Icon(
                                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Rate $star stars",
                                    tint = if (star <= rating) iCaseGold else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = {
                            comment = it
                            formError = null
                        },
                        label = { Text("Share your feedback") },
                        placeholder = { Text("How was the quality / service?") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_comment_input")
                    )
                }

                if (formError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formError!!,
                        fontSize = spec.sp(12),
                        color = iCaseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            if (isEligible) {
                Button(
                    onClick = {
                        if (comment.trim().isBlank()) {
                            formError = "Please enter your review comment."
                            return@Button
                        }
                        isSubmitting = true
                        viewModel.submitVerifiedReview(
                            referenceId = referenceId.trim(),
                            customerName = customerName.trim(),
                            rating = rating,
                            comment = comment.trim()
                        ) { res ->
                            isSubmitting = false
                            res.onSuccess { saved ->
                                val msg = if (saved.reviewType == "SERVICE") {
                                    "Thank you! Private service feedback sent to iCASE Admin."
                                } else {
                                    "Thank you! Review submitted for Admin approval."
                                }
                                onSubmitted(msg)
                            }.onFailure { err ->
                                formError = err.message ?: "Unable to submit review."
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("submit_verified_review_button")
                ) {
                    Text(if (isSubmitting) "Submitting..." else "Submit Review", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
        }
    )
}
