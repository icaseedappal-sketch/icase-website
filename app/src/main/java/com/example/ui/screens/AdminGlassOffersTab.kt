package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CameraLensModelConfig
import com.example.data.GlassTypeAdminConfig
import com.example.data.JsonHelper
import com.example.data.ModelAdminConfig
import com.example.data.PouchAdminItem
import com.example.data.PouchColorOption
import com.example.data.ScreenGuardAdminConfig
import com.example.data.ScreenGuardCatalog
import com.example.data.ScreenGuardOffer
import com.example.data.ScreenGuardOfferType
import com.example.ui.components.PouchImageHelper
import com.example.ui.components.PouchThumbImage
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseLightBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private sealed class GlassAdminDeleteTarget {
    data class GlassType(val name: String) : GlassAdminDeleteTarget()
    data class TemperedModel(val brand: String, val model: String) : GlassAdminDeleteTarget()
    data class CameraLensModel(val brand: String, val model: String) : GlassAdminDeleteTarget()
    data class PouchItem(val id: String, val name: String) : GlassAdminDeleteTarget()
    data class PouchModel(val brand: String, val model: String) : GlassAdminDeleteTarget()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminGlassOffersTab(
    config: ScreenGuardAdminConfig,
    onSaveConfig: (ScreenGuardAdminConfig, String) -> Unit,
    onOpenExportCode: () -> Unit = {}
) {
    val spec = LocalResponsiveSpec.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingImageTarget by remember { mutableStateOf("") } // "GLASS" or "CAMERA"
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessingSamplePhoto by remember { mutableStateOf(false) }

    var glassUrlInput by remember(config.temperedGlassImageUrl) {
        mutableStateOf(
            if (config.temperedGlassImageUrl.startsWith("data:image", ignoreCase = true)) "" else config.temperedGlassImageUrl
        )
    }
    var cameraUrlInput by remember(config.cameraLensImageUrl) {
        mutableStateOf(
            if (config.cameraLensImageUrl.startsWith("data:image", ignoreCase = true)) "" else config.cameraLensImageUrl
        )
    }

    fun applySampleCardUri(uri: Uri, target: String) {
        scope.launch {
            isProcessingSamplePhoto = true
            val dataUri = withContext(Dispatchers.IO) {
                PouchImageHelper.compressImageUriToDataUrl(context, uri)
            }
            if (dataUri != null) {
                if (target == "CAMERA") {
                    onSaveConfig(
                        config.copy(cameraLensImageUrl = dataUri),
                        "Camera Lens card image updated"
                    )
                } else {
                    onSaveConfig(
                        config.copy(temperedGlassImageUrl = dataUri),
                        "Tempered Glass card image updated"
                    )
                }
            }
            isProcessingSamplePhoto = false
        }
    }

    val sampleGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) applySampleCardUri(uri, pendingImageTarget)
    }

    val sampleCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) applySampleCardUri(uri, pendingImageTarget)
    }

    var editingGlassType by remember { mutableStateOf<String?>(null) }
    var showAddGlassTypeDialog by remember { mutableStateOf(false) }

    var showTemperedModelPicker by remember { mutableStateOf(false) }
    var showAddTemperedModelDialog by remember { mutableStateOf(false) }
    var editingTemperedModel by remember { mutableStateOf<Pair<String, String>?>(null) }

    var editingCameraDefault by remember { mutableStateOf(false) }
    var showCameraModelPicker by remember { mutableStateOf(false) }
    var showAddCameraModelDialog by remember { mutableStateOf(false) }
    var editingCameraModel by remember { mutableStateOf<Pair<String, String>?>(null) }
    var editingComboOffer by remember { mutableStateOf(false) }

    var showAddPouchDialog by remember { mutableStateOf(false) }
    var editingPouchItem by remember { mutableStateOf<PouchAdminItem?>(null) }
    var showPouchModelPicker by remember { mutableStateOf(false) }
    var showAddPouchModelDialog by remember { mutableStateOf(false) }
    var editingPouchModel by remember { mutableStateOf<Pair<String, String>?>(null) }

    var pendingDeleteTarget by remember { mutableStateOf<GlassAdminDeleteTarget?>(null) }

    val allKnownGlassTypes = remember(config.glassTypeConfigs) {
        ScreenGuardCatalog.allGlassTypes(config)
    }
    val activeOrPausedGlassTypes = allKnownGlassTypes.filter { glassType ->
        val cfg = config.glassTypeConfigs[glassType]
        cfg == null || !cfg.deleted
    }
    val deletedGlassTypes = allKnownGlassTypes.filter { glassType ->
        config.glassTypeConfigs[glassType]?.deleted == true
    }

    val customizedModels = remember(config.modelConfigs) {
        config.modelConfigs.entries.mapNotNull { (key, mCfg) ->
            val parts = key.split("::")
            if (parts.size == 2) Triple(parts[0], parts[1], mCfg) else null
        }
    }

    val customizedCameraModels = remember(config.cameraLensModelConfigs) {
        config.cameraLensModelConfigs.entries.mapNotNull { (key, cCfg) ->
            val parts = key.split("::")
            if (parts.size == 2) Triple(parts[0], parts[1], cCfg) else null
        }
    }

    val activeOrPausedPouches = remember(config.pouchItems) {
        config.pouchItems.filter { !it.deleted }
    }
    val deletedPouches = remember(config.pouchItems) {
        config.pouchItems.filter { it.deleted }
    }
    val customizedPouchModels = remember(config.pouchModelConfigs) {
        config.pouchModelConfigs.entries.mapNotNull { (key, mCfg) ->
            val parts = key.split("::")
            if (parts.size == 2) Triple(parts[0], parts[1], mCfg) else null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_glass_offers_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(start = spec.horizontalPadding, end = spec.horizontalPadding, top = 14.dp, bottom = 96.dp)
    ) {
        // 0. Home Sample Card & Item Images + Export Default Code
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(spec.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sample Card & Item Images",
                                    fontSize = spec.sp(15),
                                    fontWeight = FontWeight.Bold,
                                    color = iCaseNavy
                                )
                                Text(
                                    text = "Update photos shown on Home cards, Screen Guard page & Cart.",
                                    fontSize = spec.sp(11.5f),
                                    color = Color(0xFF64748B)
                                )
                            }
                            if (isProcessingSamplePhoto) {
                                CircularProgressIndicator(color = iCaseNavy, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }

                        // Tempered Glass Sample Image Row
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(10.dp)
                        ) {
                            Text("Tempered Glass Image", fontSize = spec.sp(12.5f), fontWeight = FontWeight.Bold, color = iCaseNavy)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PouchThumbImage(
                                    imageUrl = ScreenGuardCatalog.resolveTemperedGlassImageUrl(config),
                                    contentDescription = "Tempered Glass Sample",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                FlowRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            pendingImageTarget = "GLASS"
                                            sampleGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Upload", fontSize = spec.sp(11), fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                pendingImageTarget = "GLASS"
                                                val uri = PouchImageHelper.createCameraTempUri(context)
                                                pendingCameraUri = uri
                                                sampleCameraLauncher.launch(uri)
                                            } catch (_: Exception) {}
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Camera", fontSize = spec.sp(11), fontWeight = FontWeight.Bold)
                                    }
                                    TextButton(
                                        onClick = {
                                            glassUrlInput = ScreenGuardCatalog.TEMPERED_GLASS_IMAGE_URL
                                            onSaveConfig(
                                                config.copy(temperedGlassImageUrl = ScreenGuardCatalog.TEMPERED_GLASS_IMAGE_URL),
                                                "Reset Tempered Glass image to default"
                                            )
                                        },
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Text("Reset", fontSize = spec.sp(11), color = iCaseRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = glassUrlInput,
                                    onValueChange = { glassUrlInput = it },
                                    label = { Text("Image URL") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val clean = glassUrlInput.trim().ifBlank { ScreenGuardCatalog.TEMPERED_GLASS_IMAGE_URL }
                                        onSaveConfig(config.copy(temperedGlassImageUrl = clean), "Tempered Glass image saved")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                    modifier = Modifier.heightIn(min = 46.dp)
                                ) {
                                    Text("Save", fontSize = spec.sp(11.5f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Camera Lens Sample Image Row
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(10.dp)
                        ) {
                            Text("Camera Lens Protector Image", fontSize = spec.sp(12.5f), fontWeight = FontWeight.Bold, color = iCaseNavy)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PouchThumbImage(
                                    imageUrl = ScreenGuardCatalog.resolveCameraLensImageUrl(config),
                                    contentDescription = "Camera Lens Sample",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                FlowRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            pendingImageTarget = "CAMERA"
                                            sampleGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Upload", fontSize = spec.sp(11), fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                pendingImageTarget = "CAMERA"
                                                val uri = PouchImageHelper.createCameraTempUri(context)
                                                pendingCameraUri = uri
                                                sampleCameraLauncher.launch(uri)
                                            } catch (_: Exception) {}
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Camera", fontSize = spec.sp(11), fontWeight = FontWeight.Bold)
                                    }
                                    TextButton(
                                        onClick = {
                                            cameraUrlInput = ScreenGuardCatalog.CAMERA_LENS_IMAGE_URL
                                            onSaveConfig(
                                                config.copy(cameraLensImageUrl = ScreenGuardCatalog.CAMERA_LENS_IMAGE_URL),
                                                "Reset Camera Lens image to default"
                                            )
                                        },
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Text("Reset", fontSize = spec.sp(11), color = iCaseRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = cameraUrlInput,
                                    onValueChange = { cameraUrlInput = it },
                                    label = { Text("Image URL") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val clean = cameraUrlInput.trim().ifBlank { ScreenGuardCatalog.CAMERA_LENS_IMAGE_URL }
                                        onSaveConfig(config.copy(cameraLensImageUrl = clean), "Camera Lens image saved")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                    modifier = Modifier.heightIn(min = 46.dp)
                                ) {
                                    Text("Save", fontSize = spec.sp(11.5f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenExportCode,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp)
                                .testTag("admin_glass_export_code_button")
                        ) {
                            Text(
                                text = "Export Default Code for GitHub (No-DB Live Site)",
                                fontSize = spec.sp(12),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy
                            )
                        }
                    }
                }
            }
        }

        // 1. Tempered Glass Type Rates, Helper Notes & Offers
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = iCaseBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Tempered Glass Types & Rates",
                                        fontSize = spec.sp(15),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy
                                    )
                                    Text(
                                        text = "Add types, edit rates/helper notes/offers, pause or permanently delete.",
                                        fontSize = spec.sp(11.5f),
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showAddGlassTypeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 42.dp)
                                    .testTag("admin_add_glass_type_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Type", fontSize = spec.sp(11.5f), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (activeOrPausedGlassTypes.isEmpty()) {
                            Text(
                                text = "All tempered glass types are deleted.",
                                fontSize = spec.sp(12),
                                color = iCaseRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            activeOrPausedGlassTypes.forEach { glassType ->
                                val gCfg = config.glassTypeConfigs[glassType]
                                    ?: GlassTypeAdminConfig(
                                        basePrice = ScreenGuardAdminConfig.defaultPricesByGlassType[glassType] ?: 199.0
                                    )
                                val pricing = ScreenGuardCatalog.resolveTemperedGlassPrice("", "", glassType, config)
                                val helper = ScreenGuardCatalog.glassTypeHelperNote(glassType, config)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (gCfg.enabled) Color(0xFFF8FAFC) else Color(0xFFFEF2F2))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = glassType,
                                                fontSize = spec.sp(13.5f),
                                                fontWeight = FontWeight.Bold,
                                                color = iCaseNavy
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "₹${pricing.finalPrice.roundToInt()}",
                                                    fontSize = spec.sp(13),
                                                    fontWeight = FontWeight.Black,
                                                    color = iCaseBlue
                                                )
                                                if (pricing.basePrice > pricing.finalPrice) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "₹${pricing.basePrice.roundToInt()}",
                                                        fontSize = spec.sp(11),
                                                        color = Color(0xFF94A3B8),
                                                        textDecoration = TextDecoration.LineThrough
                                                    )
                                                }
                                                if (pricing.offerBadge.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = pricing.offerBadge,
                                                        fontSize = spec.sp(10),
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF15803D)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = helper,
                                                fontSize = spec.sp(10.5f),
                                                color = Color(0xFF475569)
                                            )
                                            Text(
                                                text = if (gCfg.enabled) "Active for customers" else "Paused (hidden)",
                                                fontSize = spec.sp(10.5f),
                                                color = if (gCfg.enabled) iCaseGreen else iCaseRed,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Switch(
                                                checked = gCfg.enabled,
                                                onCheckedChange = { enabled ->
                                                    val nextMap = config.glassTypeConfigs.toMutableMap()
                                                    nextMap[glassType] = gCfg.copy(enabled = enabled)
                                                    onSaveConfig(
                                                        config.copy(glassTypeConfigs = nextMap),
                                                        "$glassType ${if (enabled) "enabled" else "paused"}"
                                                    )
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = iCaseGreen
                                                )
                                            )
                                            IconButton(
                                                onClick = { editingGlassType = glassType },
                                                modifier = Modifier.testTag("admin_edit_glass_${glassType.lowercase().replace(" ", "_")}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit $glassType rate & offer", tint = iCaseBlue)
                                            }
                                            IconButton(
                                                onClick = { pendingDeleteTarget = GlassAdminDeleteTarget.GlassType(glassType) },
                                                modifier = Modifier.testTag("admin_delete_glass_${glassType.lowercase().replace(" ", "_")}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete $glassType", tint = iCaseRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (deletedGlassTypes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Deleted Glass Types (${deletedGlassTypes.size}) — hidden from catalog",
                                fontSize = spec.sp(11),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                deletedGlassTypes.forEach { deletedName ->
                                    OutlinedButton(
                                        onClick = {
                                            val current = config.glassTypeConfigs[deletedName]
                                                ?: GlassTypeAdminConfig(
                                                    basePrice = ScreenGuardAdminConfig.defaultPricesByGlassType[deletedName] ?: 199.0
                                                )
                                            val nextMap = config.glassTypeConfigs.toMutableMap()
                                            nextMap[deletedName] = current.copy(deleted = false, enabled = true)
                                            onSaveConfig(
                                                config.copy(glassTypeConfigs = nextMap),
                                                "Restored $deletedName"
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.heightIn(min = 40.dp)
                                    ) {
                                        Text("Restore $deletedName", fontSize = spec.sp(11), color = iCaseNavy)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Tempered Glass Models CRUD (Add, Rename, Rates, Offers, Pause, Delete)
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Tempered Glass Models (Add, Rename & Rates)",
                                fontSize = spec.sp(15),
                                fontWeight = FontWeight.Bold,
                                color = iCaseNavy
                            )
                            Text(
                                text = "Add new phone models, rename presets, adjust per-model rates/offers, pause or permanently delete.",
                                fontSize = spec.sp(11.5f),
                                color = Color(0xFF64748B)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddTemperedModelDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("admin_add_glass_model_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Model", fontSize = spec.sp(12), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showTemperedModelPicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("admin_pick_glass_model_button")
                            ) {
                                Text("Browse & Edit", fontSize = spec.sp(12), fontWeight = FontWeight.Bold)
                            }
                        }

                        val customModelCount = config.customModelsByBrand.values.sumOf { it.size }
                        if (customModelCount > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Added / Custom Models: $customModelCount",
                                fontSize = spec.sp(11.5f),
                                fontWeight = FontWeight.SemiBold,
                                color = iCaseBlue
                            )
                        }

                        if (customizedModels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            customizedModels.forEach { (brand, model, mCfg) ->
                                val key = ScreenGuardAdminConfig.modelKey(brand, model)
                                val statusText = when {
                                    mCfg.deleted -> "Permanently Deleted"
                                    !mCfg.enabled -> "Paused"
                                    else -> "Active"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (mCfg.deleted || !mCfg.enabled) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("$brand • $model", fontSize = spec.sp(12.5f), fontWeight = FontWeight.Bold, color = iCaseNavy)
                                        val offerNote = if (mCfg.offer.active) " • Offer: ${mCfg.offer.resolvedBadgeText()}" else ""
                                        val rateNote = if (mCfg.customRates.isNotEmpty()) " • ${mCfg.customRates.size} custom rate(s)" else ""
                                        Text(
                                            text = "$statusText$offerNote$rateNote",
                                            fontSize = spec.sp(11),
                                            color = if (mCfg.deleted || !mCfg.enabled) iCaseRed else iCaseGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row {
                                        if (mCfg.deleted) {
                                            OutlinedButton(
                                                onClick = {
                                                    val nextModels = config.modelConfigs.toMutableMap()
                                                    nextModels[key] = mCfg.copy(deleted = false, enabled = true)
                                                    onSaveConfig(config.copy(modelConfigs = nextModels), "Restored $model")
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.heightIn(min = 38.dp)
                                            ) {
                                                Text("Restore", fontSize = spec.sp(11), color = iCaseNavy)
                                            }
                                        } else {
                                            IconButton(onClick = { editingTemperedModel = brand to model }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit $model", tint = iCaseBlue)
                                            }
                                            IconButton(onClick = {
                                                val nextModels = config.modelConfigs.toMutableMap()
                                                nextModels.remove(key)
                                                onSaveConfig(
                                                    config.copy(modelConfigs = nextModels),
                                                    "Reset $model to defaults"
                                                )
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Reset $model", tint = Color(0xFF64748B))
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

        // 3. Camera Lens Protection + Tempered Glass Combo Offer
        item {
            ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(spec.cardPadding)) {
                        val camDefault = config.cameraLensDefault
                        val camPricing = ScreenGuardCatalog.resolveCameraLensPrice("", "", config)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = iCaseRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Camera Lens Protection & Combo",
                                        fontSize = spec.sp(15),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy
                                    )
                                    Text(
                                        text = if (camDefault.enabled && !camDefault.deleted) "Active for iPhone & Samsung S Series" else "Paused globally",
                                        fontSize = spec.sp(11.5f),
                                        color = if (camDefault.enabled && !camDefault.deleted) iCaseGreen else iCaseRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Switch(
                                checked = camDefault.enabled && !camDefault.deleted,
                                onCheckedChange = { enabled ->
                                    onSaveConfig(
                                        config.copy(cameraLensDefault = camDefault.copy(enabled = enabled, deleted = false)),
                                        "Camera Lens Protection ${if (enabled) "enabled" else "paused"}"
                                    )
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iCaseGreen)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Default Camera Lens Rate", fontSize = spec.sp(12.5f), fontWeight = FontWeight.Bold, color = iCaseNavy)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("₹${camPricing.finalPrice.roundToInt()}", fontSize = spec.sp(14), fontWeight = FontWeight.Black, color = iCaseBlue)
                                    if (camPricing.basePrice > camPricing.finalPrice) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "₹${camPricing.basePrice.roundToInt()}",
                                            fontSize = spec.sp(11),
                                            color = Color(0xFF94A3B8),
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    }
                                    if (camPricing.offerBadge.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(camPricing.offerBadge, fontSize = spec.sp(10.5f), fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = { editingCameraDefault = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .testTag("admin_edit_camera_default_button")
                            ) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = iCaseBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rate & Offer", fontSize = spec.sp(12), color = iCaseBlue, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Combo Discount Config Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (config.comboEnabled) Color(0xFFECFDF5) else Color(0xFFF8FAFC))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tempered Glass + Lens Combo",
                                        fontSize = spec.sp(12.5f),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy
                                    )
                                    val comboSummary = if (config.comboOffer.active && config.comboOffer.type != ScreenGuardOfferType.NONE) {
                                        config.comboOffer.resolvedBadgeText()
                                    } else {
                                        "No extra discount"
                                    }
                                    Text(
                                        text = if (config.comboEnabled) "Active • $comboSummary" else "Combo hidden",
                                        fontSize = spec.sp(11),
                                        color = if (config.comboEnabled) Color(0xFF047857) else iCaseRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = config.comboEnabled,
                                        onCheckedChange = { enabled ->
                                            onSaveConfig(
                                                config.copy(comboEnabled = enabled),
                                                "Glass + Camera Combo ${if (enabled) "enabled" else "paused"}"
                                            )
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iCaseGreen)
                                    )
                                    OutlinedButton(
                                        onClick = { editingComboOffer = true },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .heightIn(min = 40.dp)
                                            .testTag("admin_edit_combo_offer_button")
                                    ) {
                                        Text("Edit Deal", fontSize = spec.sp(11.5f), color = iCaseNavy, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddCameraModelDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("admin_add_camera_model_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Model", fontSize = spec.sp(12), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showCameraModelPicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("admin_pick_camera_model_button")
                            ) {
                                Text("Manage Models", fontSize = spec.sp(12), fontWeight = FontWeight.Bold)
                            }
                        }

                        if (customizedCameraModels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            customizedCameraModels.forEach { (brand, model, cCfg) ->
                                val key = ScreenGuardAdminConfig.modelKey(brand, model)
                                val statusText = when {
                                    cCfg.deleted -> "Permanently Deleted"
                                    !cCfg.enabled -> "Paused"
                                    else -> "Active"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (cCfg.deleted || !cCfg.enabled) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("$brand • $model", fontSize = spec.sp(12.5f), fontWeight = FontWeight.Bold, color = iCaseNavy)
                                        val priceNote = cCfg.customPrice?.let { " • ₹${it.roundToInt()}" } ?: ""
                                        val offerNote = if (cCfg.offer.active) " • ${cCfg.offer.resolvedBadgeText()}" else ""
                                        Text(
                                            text = "$statusText$priceNote$offerNote",
                                            fontSize = spec.sp(11),
                                            color = if (cCfg.deleted || !cCfg.enabled) iCaseRed else iCaseGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row {
                                        if (cCfg.deleted) {
                                            OutlinedButton(
                                                onClick = {
                                                    val nextCam = config.cameraLensModelConfigs.toMutableMap()
                                                    nextCam[key] = cCfg.copy(deleted = false, enabled = true)
                                                    onSaveConfig(config.copy(cameraLensModelConfigs = nextCam), "Restored $model Camera Lens")
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.heightIn(min = 38.dp)
                                            ) {
                                                Text("Restore", fontSize = spec.sp(11), color = iCaseNavy)
                                            }
                                        } else {
                                            IconButton(onClick = { editingCameraModel = brand to model }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit $model", tint = iCaseBlue)
                                            }
                                            IconButton(onClick = {
                                                val nextCam = config.cameraLensModelConfigs.toMutableMap()
                                                nextCam.remove(key)
                                                onSaveConfig(
                                                    config.copy(cameraLensModelConfigs = nextCam),
                                                    "Reset Camera Lens $model"
                                                )
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Reset $model", tint = Color(0xFF64748B))
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

        // 4. Pouches Management Card (iPhone & Samsung S Series)
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = iCaseNavy, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Pouches (iPhone & Samsung S Series)",
                                        fontSize = spec.sp(15),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy
                                    )
                                    Text(
                                        text = "Add/edit pouch styles, rates & offers or manage supported phone models.",
                                        fontSize = spec.sp(11.5f),
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showAddPouchDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 42.dp)
                                    .testTag("admin_add_pouch_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Pouch", fontSize = spec.sp(11.5f), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (activeOrPausedPouches.isEmpty()) {
                            Text(
                                text = "No active or paused pouches. Tap Add Pouch to create one.",
                                fontSize = spec.sp(12),
                                color = iCaseRed
                            )
                        } else {
                            activeOrPausedPouches.forEach { pouch ->
                                val pricing = ScreenGuardCatalog.resolvePouchPrice(pouch)
                                val thumbUrl = ScreenGuardCatalog.resolvePouchImageUrl(pouch)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (pouch.enabled) Color(0xFFF8FAFC) else Color(0xFFFEF2F2))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    PouchThumbImage(
                                        imageUrl = thumbUrl,
                                        contentDescription = pouch.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                pouch.name,
                                                fontSize = spec.sp(13),
                                                fontWeight = FontWeight.Bold,
                                                color = iCaseNavy,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (pouch.showOnHomePage) {
                                                Surface(
                                                    color = Color(0xFFFEF3C7),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "HOME",
                                                        fontSize = spec.sp(9.5f),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFB45309),
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "₹${pricing.finalPrice.roundToInt()} • ${pouch.compatibleBrands.joinToString(", ")}",
                                            fontSize = spec.sp(11),
                                            color = iCaseBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (pouch.colorOptions.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                pouch.colorOptions.take(6).forEach { col ->
                                                    val dotColor = remember(col.hex) {
                                                        try {
                                                            Color(android.graphics.Color.parseColor(col.hex))
                                                        } catch (_: Exception) {
                                                            Color(0xFF1E293B)
                                                        }
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .clip(CircleShape)
                                                            .background(dotColor)
                                                            .border(1.dp, Color(0xFF94A3B8), CircleShape)
                                                    )
                                                }
                                                Text(
                                                    text = "${pouch.colorOptions.size} colour${if (pouch.colorOptions.size == 1) "" else "s"}",
                                                    fontSize = spec.sp(10),
                                                    color = Color(0xFF475569),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        if (pricing.offerBadge.isNotBlank()) {
                                            Text(pricing.offerBadge, fontSize = spec.sp(10), color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = pouch.enabled,
                                            onCheckedChange = { enabled ->
                                                val nextList = config.pouchItems.map {
                                                    if (it.id == pouch.id) it.copy(enabled = enabled) else it
                                                }
                                                onSaveConfig(
                                                    config.copy(pouchItems = nextList),
                                                    "${pouch.name} ${if (enabled) "enabled" else "paused"}"
                                                )
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iCaseGreen)
                                        )
                                        IconButton(onClick = { editingPouchItem = pouch }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit ${pouch.name}", tint = iCaseBlue)
                                        }
                                        IconButton(onClick = {
                                            pendingDeleteTarget = GlassAdminDeleteTarget.PouchItem(pouch.id, pouch.name)
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete ${pouch.name}", tint = iCaseRed)
                                        }
                                    }
                                }
                            }
                        }

                        if (deletedPouches.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Deleted Pouches (${deletedPouches.size})",
                                fontSize = spec.sp(11),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                deletedPouches.forEach { pouch ->
                                    OutlinedButton(
                                        onClick = {
                                            val nextList = config.pouchItems.map {
                                                if (it.id == pouch.id) it.copy(deleted = false, enabled = true) else it
                                            }
                                            onSaveConfig(config.copy(pouchItems = nextList), "Restored ${pouch.name}")
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Text("Restore ${pouch.name}", fontSize = spec.sp(11), color = iCaseNavy)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddPouchModelDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("admin_add_pouch_model_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = iCaseNavy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Model", fontSize = spec.sp(11.5f), color = iCaseNavy, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showPouchModelPicker = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("admin_pick_pouch_model_button")
                            ) {
                                Text("Pouch Models", fontSize = spec.sp(11.5f), color = iCaseNavy, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (customizedPouchModels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            customizedPouchModels.forEach { (brand, model, mCfg) ->
                                val key = ScreenGuardAdminConfig.modelKey(brand, model)
                                val statusText = when {
                                    mCfg.deleted -> "Permanently Deleted"
                                    !mCfg.enabled -> "Paused"
                                    else -> "Active"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (mCfg.deleted || !mCfg.enabled) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("$brand • $model", fontSize = spec.sp(12), fontWeight = FontWeight.Bold, color = iCaseNavy)
                                        Text(
                                            text = statusText,
                                            fontSize = spec.sp(10.5f),
                                            color = if (mCfg.deleted || !mCfg.enabled) iCaseRed else iCaseGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row {
                                        if (mCfg.deleted) {
                                            OutlinedButton(
                                                onClick = {
                                                    val nextMap = config.pouchModelConfigs.toMutableMap()
                                                    nextMap[key] = mCfg.copy(deleted = false, enabled = true)
                                                    onSaveConfig(config.copy(pouchModelConfigs = nextMap), "Restored $model in Pouches")
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.heightIn(min = 36.dp)
                                            ) {
                                                Text("Restore", fontSize = spec.sp(10.5f), color = iCaseNavy)
                                            }
                                        } else {
                                            IconButton(onClick = { editingPouchModel = brand to model }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit $model", tint = iCaseBlue)
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

    // Add New Glass Type Dialog
    if (showAddGlassTypeDialog) {
        AddGlassTypeDialog(
            existingNames = allKnownGlassTypes,
            onDismiss = { showAddGlassTypeDialog = false },
            onAdd = { name, price, helperNote, offer ->
                val nextMap = config.glassTypeConfigs.toMutableMap()
                nextMap[name] = GlassTypeAdminConfig(
                    basePrice = price,
                    enabled = true,
                    deleted = false,
                    offer = offer,
                    helperNote = helperNote
                )
                onSaveConfig(config.copy(glassTypeConfigs = nextMap), "Added glass type $name")
                showAddGlassTypeDialog = false
            }
        )
    }

    // Edit Glass Type Rate, Helper Note & Offer Dialog
    editingGlassType?.let { gType ->
        val currentCfg = config.glassTypeConfigs[gType]
            ?: GlassTypeAdminConfig(basePrice = ScreenGuardAdminConfig.defaultPricesByGlassType[gType] ?: 199.0)
        RateAndOfferEditDialog(
            title = "Edit $gType",
            initialPrice = currentCfg.basePrice,
            initialOffer = currentCfg.offer,
            initialHelperNote = currentCfg.helperNote.ifBlank { ScreenGuardCatalog.defaultGlassTypeHelperNote(gType) },
            showHelperNoteField = true,
            onDismiss = { editingGlassType = null },
            onSave = { newPrice, newOffer, newNote ->
                val nextMap = config.glassTypeConfigs.toMutableMap()
                nextMap[gType] = currentCfg.copy(
                    basePrice = newPrice,
                    offer = newOffer,
                    helperNote = newNote
                )
                onSaveConfig(config.copy(glassTypeConfigs = nextMap), "Updated $gType pricing & details")
                editingGlassType = null
            }
        )
    }

    // Edit Default Camera Lens Rate & Offer Dialog
    if (editingCameraDefault) {
        RateAndOfferEditDialog(
            title = "Default Camera Lens Protection",
            initialPrice = config.cameraLensDefault.basePrice,
            initialOffer = config.cameraLensDefault.offer,
            initialHelperNote = "",
            showHelperNoteField = false,
            onDismiss = { editingCameraDefault = false },
            onSave = { newPrice, newOffer, _ ->
                onSaveConfig(
                    config.copy(cameraLensDefault = config.cameraLensDefault.copy(basePrice = newPrice, offer = newOffer)),
                    "Updated default Camera Lens pricing & offer"
                )
                editingCameraDefault = false
            }
        )
    }

    // Edit Combo Offer Dialog
    if (editingComboOffer) {
        ComboOfferEditDialog(
            initialOffer = config.comboOffer,
            onDismiss = { editingComboOffer = false },
            onSave = { newOffer ->
                onSaveConfig(config.copy(comboOffer = newOffer), "Updated Glass + Camera Combo offer")
                editingComboOffer = false
            }
        )
    }

    // Add Tempered Glass Model Dialog
    if (showAddTemperedModelDialog) {
        AddCustomModelDialog(
            title = "Add Tempered Glass Model",
            brands = ScreenGuardCatalog.brands,
            onDismiss = { showAddTemperedModelDialog = false },
            onAdd = { brand, modelName ->
                val updated = addModelToCustomMap(config.customModelsByBrand, brand, modelName)
                val key = ScreenGuardAdminConfig.modelKey(brand, modelName)
                val nextModelCfgs = config.modelConfigs.toMutableMap()
                if (nextModelCfgs[key]?.deleted == true) {
                    nextModelCfgs[key] = nextModelCfgs[key]!!.copy(deleted = false, enabled = true)
                }
                onSaveConfig(
                    config.copy(customModelsByBrand = updated, modelConfigs = nextModelCfgs),
                    "Added $brand $modelName"
                )
                showAddTemperedModelDialog = false
            }
        )
    }

    // Tempered Model Picker (includes presets + admin-added models)
    if (showTemperedModelPicker) {
        val mergedTemperedMap = remember(config) {
            ScreenGuardCatalog.brands.associateWith { ScreenGuardCatalog.modelsForBrand(it, config) }
        }
        AdminCatalogModelPickerDialog(
            title = "Select Model to Manage",
            brands = ScreenGuardCatalog.brands,
            modelsByBrand = mergedTemperedMap,
            onDismiss = { showTemperedModelPicker = false },
            onSelect = { brand, model ->
                showTemperedModelPicker = false
                editingTemperedModel = brand to model
            }
        )
    }

    // Edit Tempered Glass Model Dialog (with Rename + Custom Rates + Offer + Delete)
    editingTemperedModel?.let { (brand, model) ->
        val key = ScreenGuardAdminConfig.modelKey(brand, model)
        val current = config.modelConfigs[key] ?: ModelAdminConfig()
        val deviceGlassTypes = remember(brand, model, config.glassTypeConfigs) {
            ScreenGuardCatalog.glassTypesForDevice(brand, model, config)
        }
        AdminTemperedModelEditDialog(
            brand = brand,
            model = model,
            applicableTypes = deviceGlassTypes.ifEmpty { ScreenGuardCatalog.allGlassTypes(config) },
            config = config,
            initial = current,
            onDismiss = { editingTemperedModel = null },
            onDeleteModel = {
                editingTemperedModel = null
                pendingDeleteTarget = GlassAdminDeleteTarget.TemperedModel(brand, model)
            },
            onSave = { newName, updated ->
                val nextMap = config.modelConfigs.toMutableMap()
                var nextCustom = config.customModelsByBrand
                if (newName != model) {
                    val existingCustomList = nextCustom[brand].orEmpty()
                    if (existingCustomList.contains(model)) {
                        val replaced = existingCustomList.map { if (it == model) newName else it }.distinct()
                        nextCustom = nextCustom.toMutableMap().apply { put(brand, replaced) }
                        nextMap.remove(key)
                    } else {
                        nextMap[key] = current.copy(deleted = true, enabled = false)
                        nextCustom = addModelToCustomMap(nextCustom, brand, newName)
                    }
                    nextMap[ScreenGuardAdminConfig.modelKey(brand, newName)] = updated
                } else {
                    nextMap[key] = updated
                }
                onSaveConfig(
                    config.copy(modelConfigs = nextMap, customModelsByBrand = nextCustom),
                    "Saved settings for $newName"
                )
                editingTemperedModel = null
            }
        )
    }

    // Add Camera Lens Model Dialog
    if (showAddCameraModelDialog) {
        AddCustomModelDialog(
            title = "Add Camera Lens Model",
            brands = ScreenGuardCatalog.cameraLensBrands,
            onDismiss = { showAddCameraModelDialog = false },
            onAdd = { brand, modelName ->
                val updated = addModelToCustomMap(config.customCameraLensModelsByBrand, brand, modelName)
                val key = ScreenGuardAdminConfig.modelKey(brand, modelName)
                val nextCamCfgs = config.cameraLensModelConfigs.toMutableMap()
                if (nextCamCfgs[key]?.deleted == true) {
                    nextCamCfgs[key] = nextCamCfgs[key]!!.copy(deleted = false, enabled = true)
                }
                onSaveConfig(
                    config.copy(customCameraLensModelsByBrand = updated, cameraLensModelConfigs = nextCamCfgs),
                    "Added Camera Lens model $modelName"
                )
                showAddCameraModelDialog = false
            }
        )
    }

    // Camera Lens Model Picker
    if (showCameraModelPicker) {
        val mergedCamMap = remember(config) {
            ScreenGuardCatalog.cameraLensBrands.associateWith { ScreenGuardCatalog.cameraLensModelsForBrand(it, config) }
        }
        AdminCatalogModelPickerDialog(
            title = "Select Camera Lens Model",
            brands = ScreenGuardCatalog.cameraLensBrands,
            modelsByBrand = mergedCamMap,
            onDismiss = { showCameraModelPicker = false },
            onSelect = { brand, model ->
                showCameraModelPicker = false
                editingCameraModel = brand to model
            }
        )
    }

    // Edit Camera Lens Model Dialog
    editingCameraModel?.let { (brand, model) ->
        val key = ScreenGuardAdminConfig.modelKey(brand, model)
        val current = config.cameraLensModelConfigs[key] ?: CameraLensModelConfig()
        AdminCameraModelEditDialog(
            brand = brand,
            model = model,
            defaultPrice = config.cameraLensDefault.basePrice,
            initial = current,
            onDismiss = { editingCameraModel = null },
            onDeleteModel = {
                editingCameraModel = null
                pendingDeleteTarget = GlassAdminDeleteTarget.CameraLensModel(brand, model)
            },
            onSave = { newName, updated ->
                val nextCam = config.cameraLensModelConfigs.toMutableMap()
                var nextCustom = config.customCameraLensModelsByBrand
                if (newName != model) {
                    val existingCustomList = nextCustom[brand].orEmpty()
                    if (existingCustomList.contains(model)) {
                        val replaced = existingCustomList.map { if (it == model) newName else it }.distinct()
                        nextCustom = nextCustom.toMutableMap().apply { put(brand, replaced) }
                        nextCam.remove(key)
                    } else {
                        nextCam[key] = current.copy(deleted = true, enabled = false)
                        nextCustom = addModelToCustomMap(nextCustom, brand, newName)
                    }
                    nextCam[ScreenGuardAdminConfig.modelKey(brand, newName)] = updated
                } else {
                    nextCam[key] = updated
                }
                onSaveConfig(
                    config.copy(cameraLensModelConfigs = nextCam, customCameraLensModelsByBrand = nextCustom),
                    "Saved Camera Lens settings for $newName"
                )
                editingCameraModel = null
            }
        )
    }

    // Add / Edit Pouch Dialog
    if (showAddPouchDialog || editingPouchItem != null) {
        AdminPouchEditDialog(
            initial = editingPouchItem,
            onDismiss = {
                showAddPouchDialog = false
                editingPouchItem = null
            },
            onSave = { savedPouch ->
                val exists = config.pouchItems.any { it.id == savedPouch.id }
                val nextList = if (exists) {
                    config.pouchItems.map { if (it.id == savedPouch.id) savedPouch else it }
                } else {
                    config.pouchItems + savedPouch
                }
                onSaveConfig(
                    config.copy(pouchItems = nextList),
                    if (exists) "Updated ${savedPouch.name}" else "Added ${savedPouch.name}"
                )
                showAddPouchDialog = false
                editingPouchItem = null
            }
        )
    }

    // Add Pouch Model Dialog
    if (showAddPouchModelDialog) {
        AddCustomModelDialog(
            title = "Add Pouch Phone Model",
            brands = ScreenGuardCatalog.pouchBrands,
            onDismiss = { showAddPouchModelDialog = false },
            onAdd = { brand, modelName ->
                val updated = addModelToCustomMap(config.customPouchModelsByBrand, brand, modelName)
                val key = ScreenGuardAdminConfig.modelKey(brand, modelName)
                val nextPouchCfgs = config.pouchModelConfigs.toMutableMap()
                if (nextPouchCfgs[key]?.deleted == true) {
                    nextPouchCfgs[key] = nextPouchCfgs[key]!!.copy(deleted = false, enabled = true)
                }
                onSaveConfig(
                    config.copy(customPouchModelsByBrand = updated, pouchModelConfigs = nextPouchCfgs),
                    "Added $modelName to Pouches"
                )
                showAddPouchModelDialog = false
            }
        )
    }

    // Pouch Model Picker
    if (showPouchModelPicker) {
        val mergedPouchMap = remember(config) {
            ScreenGuardCatalog.pouchBrands.associateWith { ScreenGuardCatalog.pouchModelsForBrand(it, config) }
        }
        AdminCatalogModelPickerDialog(
            title = "Manage Pouch Phone Model",
            brands = ScreenGuardCatalog.pouchBrands,
            modelsByBrand = mergedPouchMap,
            onDismiss = { showPouchModelPicker = false },
            onSelect = { brand, model ->
                showPouchModelPicker = false
                editingPouchModel = brand to model
            }
        )
    }

    // Edit Pouch Model Dialog
    editingPouchModel?.let { (brand, model) ->
        val key = ScreenGuardAdminConfig.modelKey(brand, model)
        val current = config.pouchModelConfigs[key] ?: ModelAdminConfig()
        AdminPouchModelEditDialog(
            brand = brand,
            model = model,
            initial = current,
            onDismiss = { editingPouchModel = null },
            onDeleteModel = {
                editingPouchModel = null
                pendingDeleteTarget = GlassAdminDeleteTarget.PouchModel(brand, model)
            },
            onSave = { newName, updated ->
                val nextMap = config.pouchModelConfigs.toMutableMap()
                var nextCustom = config.customPouchModelsByBrand
                if (newName != model) {
                    val existingCustomList = nextCustom[brand].orEmpty()
                    if (existingCustomList.contains(model)) {
                        val replaced = existingCustomList.map { if (it == model) newName else it }.distinct()
                        nextCustom = nextCustom.toMutableMap().apply { put(brand, replaced) }
                        nextMap.remove(key)
                    } else {
                        nextMap[key] = current.copy(deleted = true, enabled = false)
                        nextCustom = addModelToCustomMap(nextCustom, brand, newName)
                    }
                    nextMap[ScreenGuardAdminConfig.modelKey(brand, newName)] = updated
                } else {
                    nextMap[key] = updated
                }
                onSaveConfig(
                    config.copy(pouchModelConfigs = nextMap, customPouchModelsByBrand = nextCustom),
                    "Updated Pouch model $newName"
                )
                editingPouchModel = null
            }
        )
    }

    // Confirmation Dialog for Permanent Deletion
    pendingDeleteTarget?.let { target ->
        val label = when (target) {
            is GlassAdminDeleteTarget.GlassType -> target.name
            is GlassAdminDeleteTarget.TemperedModel -> "${target.brand} ${target.model}"
            is GlassAdminDeleteTarget.CameraLensModel -> "Camera Lens for ${target.model}"
            is GlassAdminDeleteTarget.PouchItem -> target.name
            is GlassAdminDeleteTarget.PouchModel -> "Pouch Model ${target.model}"
        }
        AlertDialog(
            onDismissRequest = { pendingDeleteTarget = null },
            title = { Text("Delete Permanently?", fontWeight = FontWeight.Bold, color = iCaseNavy) },
            text = {
                Text("Remove \"$label\" from customer selectors and active carts? This persists across app restarts and web sync.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (target) {
                            is GlassAdminDeleteTarget.GlassType -> {
                                val existing = config.glassTypeConfigs[target.name]
                                    ?: GlassTypeAdminConfig(
                                        basePrice = ScreenGuardAdminConfig.defaultPricesByGlassType[target.name] ?: 199.0
                                    )
                                val nextMap = config.glassTypeConfigs.toMutableMap()
                                nextMap[target.name] = existing.copy(deleted = true, enabled = false)
                                onSaveConfig(config.copy(glassTypeConfigs = nextMap), "Deleted ${target.name}")
                            }
                            is GlassAdminDeleteTarget.TemperedModel -> {
                                val key = ScreenGuardAdminConfig.modelKey(target.brand, target.model)
                                val existing = config.modelConfigs[key] ?: ModelAdminConfig()
                                val nextMap = config.modelConfigs.toMutableMap()
                                nextMap[key] = existing.copy(deleted = true, enabled = false)
                                onSaveConfig(config.copy(modelConfigs = nextMap), "Deleted ${target.model}")
                            }
                            is GlassAdminDeleteTarget.CameraLensModel -> {
                                val key = ScreenGuardAdminConfig.modelKey(target.brand, target.model)
                                val existing = config.cameraLensModelConfigs[key] ?: CameraLensModelConfig()
                                val nextCam = config.cameraLensModelConfigs.toMutableMap()
                                nextCam[key] = existing.copy(deleted = true, enabled = false)
                                onSaveConfig(config.copy(cameraLensModelConfigs = nextCam), "Deleted Camera Lens for ${target.model}")
                            }
                            is GlassAdminDeleteTarget.PouchItem -> {
                                val nextList = config.pouchItems.map {
                                    if (it.id == target.id) it.copy(deleted = true, enabled = false) else it
                                }
                                onSaveConfig(config.copy(pouchItems = nextList), "Deleted ${target.name}")
                            }
                            is GlassAdminDeleteTarget.PouchModel -> {
                                val key = ScreenGuardAdminConfig.modelKey(target.brand, target.model)
                                val existing = config.pouchModelConfigs[key] ?: ModelAdminConfig()
                                val nextMap = config.pouchModelConfigs.toMutableMap()
                                nextMap[key] = existing.copy(deleted = true, enabled = false)
                                onSaveConfig(config.copy(pouchModelConfigs = nextMap), "Deleted Pouch model ${target.model}")
                            }
                        }
                        pendingDeleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = iCaseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingDeleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun addModelToCustomMap(
    source: Map<String, List<String>>,
    brand: String,
    modelName: String
): Map<String, List<String>> {
    val clean = modelName.trim()
    if (clean.isEmpty()) return source
    val next = source.toMutableMap()
    val current = next[brand].orEmpty()
    if (!current.contains(clean)) {
        next[brand] = current + clean
    }
    return next
}

@Composable
private fun AddGlassTypeDialog(
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onAdd: (name: String, price: Double, helperNote: String, offer: ScreenGuardOffer) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("299") }
    var helperNote by remember { mutableStateOf("") }
    var offerActive by remember { mutableStateOf(false) }
    var offerType by remember { mutableStateOf(ScreenGuardOfferType.NONE) }
    var offerValueStr by remember { mutableStateOf("") }
    var badgeText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tempered Glass Type", fontWeight = FontWeight.Bold, fontSize = spec.sp(16), color = iCaseNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMsg = null
                    },
                    label = { Text("Glass Type Name") },
                    placeholder = { Text("e.g. Anti-Blue Ray") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Base Price (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = helperNote,
                    onValueChange = { helperNote = it },
                    label = { Text("1-Line Helper Note") },
                    placeholder = { Text("e.g. Filters harsh blue light for eye comfort.") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OfferEditorSection(
                    offerActive = offerActive,
                    onOfferActiveChange = { offerActive = it },
                    offerType = offerType,
                    onOfferTypeChange = {
                        offerType = it
                        if (it != ScreenGuardOfferType.NONE) offerActive = true
                    },
                    offerValueStr = offerValueStr,
                    onOfferValueChange = { offerValueStr = it },
                    badgeText = badgeText,
                    onBadgeTextChange = { badgeText = it }
                )
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg!!, color = iCaseRed, fontSize = spec.sp(12), fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isBlank()) {
                        errorMsg = "Enter a glass type name."
                        return@Button
                    }
                    if (existingNames.any { it.equals(cleanName, ignoreCase = true) }) {
                        errorMsg = "This glass type already exists."
                        return@Button
                    }
                    val price = (priceStr.toDoubleOrNull() ?: 299.0).coerceAtLeast(1.0)
                    val offer = ScreenGuardOffer(
                        active = offerActive && offerType != ScreenGuardOfferType.NONE,
                        type = if (offerActive) offerType else ScreenGuardOfferType.NONE,
                        value = (offerValueStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                        badgeText = badgeText.trim()
                    )
                    onAdd(cleanName, price, helperNote.trim(), offer)
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy)
            ) {
                Text("Add Type")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCustomModelDialog(
    title: String,
    brands: List<String>,
    onDismiss: () -> Unit,
    onAdd: (brand: String, model: String) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var selectedBrand by remember { mutableStateOf(brands.firstOrNull().orEmpty()) }
    var modelInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = spec.sp(16), color = iCaseNavy) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Select Brand", fontSize = spec.sp(12), fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    brands.forEach { b ->
                        FilterChip(
                            selected = selectedBrand == b,
                            onClick = { selectedBrand = b },
                            label = { Text(b, fontSize = spec.sp(11)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = iCaseNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = modelInput,
                    onValueChange = {
                        modelInput = it
                        errorMsg = null
                    },
                    label = { Text("Exact Phone Model Name") },
                    placeholder = { Text("e.g. iPhone 17 Pro, Galaxy S26...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg!!, color = iCaseRed, fontSize = spec.sp(12), fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = modelInput.trim()
                    if (clean.isBlank()) {
                        errorMsg = "Enter a model name."
                        return@Button
                    }
                    onAdd(selectedBrand, clean)
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy)
            ) {
                Text("Add Model")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RateAndOfferEditDialog(
    title: String,
    initialPrice: Double,
    initialOffer: ScreenGuardOffer,
    initialHelperNote: String,
    showHelperNoteField: Boolean,
    onDismiss: () -> Unit,
    onSave: (Double, ScreenGuardOffer, String) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var priceStr by remember { mutableStateOf(initialPrice.roundToInt().toString()) }
    var helperNote by remember { mutableStateOf(initialHelperNote) }
    var offerActive by remember { mutableStateOf(initialOffer.active) }
    var offerType by remember { mutableStateOf(initialOffer.type) }
    var offerValueStr by remember {
        mutableStateOf(if (initialOffer.value > 0.0) initialOffer.value.roundToInt().toString() else "")
    }
    var badgeText by remember { mutableStateOf(initialOffer.badgeText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = spec.sp(16), color = iCaseNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Base Price (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_guard_base_price_input")
                )
                if (showHelperNoteField) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = helperNote,
                        onValueChange = { helperNote = it },
                        label = { Text("1-Line Customer Helper Note") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                OfferEditorSection(
                    offerActive = offerActive,
                    onOfferActiveChange = { offerActive = it },
                    offerType = offerType,
                    onOfferTypeChange = {
                        offerType = it
                        if (it != ScreenGuardOfferType.NONE) offerActive = true
                    },
                    offerValueStr = offerValueStr,
                    onOfferValueChange = { offerValueStr = it },
                    badgeText = badgeText,
                    onBadgeTextChange = { badgeText = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val base = (priceStr.toDoubleOrNull() ?: initialPrice).coerceAtLeast(1.0)
                    val valNum = (offerValueStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                    val cleanOffer = ScreenGuardOffer(
                        active = offerActive && offerType != ScreenGuardOfferType.NONE,
                        type = if (offerActive) offerType else ScreenGuardOfferType.NONE,
                        value = valNum,
                        badgeText = badgeText.trim()
                    )
                    onSave(base, cleanOffer, helperNote.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("admin_guard_save_button")
            ) {
                Text("Save")
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
private fun ComboOfferEditDialog(
    initialOffer: ScreenGuardOffer,
    onDismiss: () -> Unit,
    onSave: (ScreenGuardOffer) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var offerActive by remember { mutableStateOf(initialOffer.active) }
    var offerType by remember {
        mutableStateOf(
            if (initialOffer.type == ScreenGuardOfferType.NONE) ScreenGuardOfferType.FLAT_OFF else initialOffer.type
        )
    }
    var offerValueStr by remember {
        mutableStateOf(if (initialOffer.value > 0.0) initialOffer.value.roundToInt().toString() else "50")
    }
    var badgeText by remember { mutableStateOf(initialOffer.badgeText.ifBlank { "COMBO SAVE" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tempered Glass + Lens Combo Offer", fontWeight = FontWeight.Bold, fontSize = spec.sp(15.5f), color = iCaseNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OfferEditorSection(
                    offerActive = offerActive,
                    onOfferActiveChange = { offerActive = it },
                    offerType = offerType,
                    onOfferTypeChange = {
                        offerType = it
                        if (it != ScreenGuardOfferType.NONE) offerActive = true
                    },
                    offerValueStr = offerValueStr,
                    onOfferValueChange = { offerValueStr = it },
                    badgeText = badgeText,
                    onBadgeTextChange = { badgeText = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valNum = (offerValueStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                    onSave(
                        ScreenGuardOffer(
                            active = offerActive && offerType != ScreenGuardOfferType.NONE,
                            type = if (offerActive) offerType else ScreenGuardOfferType.NONE,
                            value = valNum,
                            badgeText = badgeText.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy)
            ) {
                Text("Save Combo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminPouchEditDialog(
    initial: PouchAdminItem?,
    onDismiss: () -> Unit,
    onSave: (PouchAdminItem) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var priceStr by remember { mutableStateOf(initial?.basePrice?.roundToInt()?.toString() ?: "349") }
    var imageUrl by remember { mutableStateOf(initial?.imageUrl.orEmpty()) }
    var colorOptions by remember { mutableStateOf(initial?.colorOptions.orEmpty()) }
    var customColorName by remember { mutableStateOf("") }
    var customColorHex by remember { mutableStateOf("#1E293B") }
    var expandedColorPhotoName by remember { mutableStateOf<String?>(null) }

    var forIphone by remember { mutableStateOf(initial == null || initial.compatibleBrands.any { it.equals("iPhone", true) }) }
    var forSamsung by remember { mutableStateOf(initial == null || initial.compatibleBrands.any { it.equals("Samsung S Series", true) }) }
    var showOnHomePage by remember { mutableStateOf(initial?.showOnHomePage ?: false) }
    var offerActive by remember { mutableStateOf(initial?.offer?.active ?: false) }
    var offerType by remember { mutableStateOf(initial?.offer?.type ?: ScreenGuardOfferType.NONE) }
    var offerValueStr by remember {
        mutableStateOf(if ((initial?.offer?.value ?: 0.0) > 0.0) initial!!.offer.value.roundToInt().toString() else "")
    }
    var badgeText by remember { mutableStateOf(initial?.offer?.badgeText.orEmpty()) }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Target key: "MAIN" or "COLOR:<name>"
    var pendingPickTarget by remember { mutableStateOf("MAIN") }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun applyCompressedUri(uri: Uri, target: String) {
        scope.launch {
            isProcessingPhoto = true
            val dataUrl = withContext(Dispatchers.IO) {
                PouchImageHelper.compressImageUriToDataUrl(context, uri)
            }
            isProcessingPhoto = false
            if (dataUrl == null) {
                errorMsg = "Could not read image file. Please try another photo."
            } else {
                errorMsg = null
                if (target == "MAIN") {
                    imageUrl = dataUrl
                } else if (target.startsWith("COLOR:")) {
                    val colorName = target.removePrefix("COLOR:")
                    colorOptions = colorOptions.map { opt ->
                        if (opt.name.equals(colorName, ignoreCase = true)) opt.copy(imageUrl = dataUrl) else opt
                    }
                }
            }
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            applyCompressedUri(uri, pendingPickTarget)
        }
    }

    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            applyCompressedUri(uri, pendingPickTarget)
        }
    }

    fun launchGalleryFor(target: String) {
        pendingPickTarget = target
        galleryPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun launchCameraFor(target: String) {
        try {
            pendingPickTarget = target
            val uri = PouchImageHelper.createCameraTempUri(context)
            pendingCameraUri = uri
            cameraCaptureLauncher.launch(uri)
        } catch (_: Exception) {
            errorMsg = "Camera unavailable on this device. Use Gallery or Image URL."
        }
    }

    val maxBodyHeight = (spec.availableHeight * 0.65f).coerceIn(260.dp, 560.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Add New Pouch" else "Edit Pouch",
                fontWeight = FontWeight.Bold,
                fontSize = spec.sp(16),
                color = iCaseNavy
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxBodyHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Pouch Image Upload Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                        .testTag("admin_pouch_image_upload_box")
                ) {
                    Text(
                        text = "Pouch Image Upload",
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
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProcessingPhoto && pendingPickTarget == "MAIN") {
                                CircularProgressIndicator(color = iCaseNavy, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else if (imageUrl.isNotBlank()) {
                                PouchThumbImage(
                                    imageUrl = imageUrl,
                                    contentDescription = "Pouch Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (imageUrl.isNotBlank()) "Custom pouch photo ready" else "Using default cover image",
                                fontSize = spec.sp(11.5f),
                                fontWeight = FontWeight.SemiBold,
                                color = if (imageUrl.isNotBlank()) iCaseGreen else Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { launchGalleryFor("MAIN") },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .testTag("admin_pouch_gallery_button")
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = iCaseNavy, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (imageUrl.isNotBlank()) "Change" else "Gallery",
                                        fontSize = spec.sp(11),
                                        fontWeight = FontWeight.Bold,
                                        color = iCaseNavy
                                    )
                                }
                                OutlinedButton(
                                    onClick = { launchCameraFor("MAIN") },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .testTag("admin_pouch_camera_button")
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = iCaseNavy, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Camera", fontSize = spec.sp(11), fontWeight = FontWeight.Bold, color = iCaseNavy)
                                }
                                if (imageUrl.isNotBlank()) {
                                    TextButton(
                                        onClick = { imageUrl = "" },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .heightIn(min = 40.dp)
                                            .testTag("admin_pouch_remove_image_button")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = iCaseRed, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Remove", fontSize = spec.sp(11), fontWeight = FontWeight.Bold, color = iCaseRed)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = if (imageUrl.startsWith("data:image", ignoreCase = true)) "" else imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Or Paste Optional Image URL") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pouch_image_url_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMsg = null
                    },
                    label = { Text("Pouch Name") },
                    placeholder = { Text("e.g. MagSafe Leather Case") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pouch_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Compatible Brands", fontSize = spec.sp(12), fontWeight = FontWeight.SemiBold, color = iCaseNavy)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = forIphone, onCheckedChange = { forIphone = it })
                    Text("iPhone", fontSize = spec.sp(12.5f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Checkbox(checked = forSamsung, onCheckedChange = { forSamsung = it })
                    Text("Samsung S Series", fontSize = spec.sp(12.5f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show on Home Page",
                            fontSize = spec.sp(12.5f),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy
                        )
                        Text(
                            text = "Feature this pouch in Featured Pouches on Home tab",
                            fontSize = spec.sp(10.5f),
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = showOnHomePage,
                        onCheckedChange = { showOnHomePage = it },
                        modifier = Modifier.testTag("admin_pouch_show_on_home_switch"),
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iCaseGreen)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 2. Colour Selection Manager ("Available Colours")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                        .testTag("admin_pouch_color_manager")
                ) {
                    Text(
                        text = "Available Colours (${colorOptions.size})",
                        fontSize = spec.sp(12.5f),
                        fontWeight = FontWeight.Bold,
                        color = iCaseNavy
                    )
                    Text(
                        text = "Tap preset colours or add custom names & per-colour images.",
                        fontSize = spec.sp(11),
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ScreenGuardCatalog.presetPouchColors.forEach { preset ->
                            val isSelected = colorOptions.any { it.name.equals(preset.name, ignoreCase = true) }
                            val swatchColor = remember(preset.hex) {
                                try {
                                    Color(android.graphics.Color.parseColor(preset.hex))
                                } catch (_: Exception) {
                                    Color(0xFF1E293B)
                                }
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    colorOptions = if (isSelected) {
                                        colorOptions.filterNot { it.name.equals(preset.name, ignoreCase = true) }
                                    } else {
                                        colorOptions + preset.copy()
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                            .border(1.dp, Color(0xFF94A3B8), CircleShape)
                                    )
                                },
                                trailingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                label = { Text(preset.name, fontSize = spec.sp(11), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = iCaseLightBlue,
                                    selectedLabelColor = iCaseNavy
                                ),
                                modifier = Modifier.testTag("admin_pouch_preset_color_${preset.name.lowercase().replace(Regex("[^a-z0-9]"), "_")}")
                            )
                        }
                    }

                    if (colorOptions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        colorOptions.forEachIndexed { idx, opt ->
                            val dotColor = remember(opt.hex) {
                                try {
                                    Color(android.graphics.Color.parseColor(opt.hex))
                                } catch (_: Exception) {
                                    Color(0xFF1E293B)
                                }
                            }
                            val isPhotoExpanded = expandedColorPhotoName.equals(opt.name, ignoreCase = true)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                            .border(1.dp, Color(0xFF94A3B8), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(opt.name, fontSize = spec.sp(12), fontWeight = FontWeight.Bold, color = iCaseNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            text = if (opt.imageUrl.isNotBlank()) "${opt.hex} • Per-colour image set" else opt.hex,
                                            fontSize = spec.sp(10),
                                            color = if (opt.imageUrl.isNotBlank()) iCaseGreen else Color(0xFF64748B)
                                        )
                                    }
                                    if (opt.imageUrl.isNotBlank()) {
                                        PouchThumbImage(
                                            imageUrl = opt.imageUrl,
                                            contentDescription = opt.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            expandedColorPhotoName = if (isPhotoExpanded) null else opt.name
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "Per-colour image for ${opt.name}",
                                            tint = iCaseBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            colorOptions = colorOptions.filterIndexed { i, _ -> i != idx }
                                            if (isPhotoExpanded) expandedColorPhotoName = null
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("admin_pouch_remove_color_$idx")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove ${opt.name}", tint = iCaseRed, modifier = Modifier.size(16.dp))
                                    }
                                }

                                if (isPhotoExpanded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { launchGalleryFor("COLOR:${opt.name}") },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.heightIn(min = 36.dp)
                                        ) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Gallery", fontSize = spec.sp(10.5f))
                                        }
                                        OutlinedButton(
                                            onClick = { launchCameraFor("COLOR:${opt.name}") },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.heightIn(min = 36.dp)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Camera", fontSize = spec.sp(10.5f))
                                        }
                                        if (opt.imageUrl.isNotBlank()) {
                                            TextButton(
                                                onClick = {
                                                    colorOptions = colorOptions.mapIndexed { i, c ->
                                                        if (i == idx) c.copy(imageUrl = "") else c
                                                    }
                                                },
                                                modifier = Modifier.heightIn(min = 36.dp)
                                            ) {
                                                Text("Clear Photo", fontSize = spec.sp(10.5f), color = iCaseRed)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = if (opt.imageUrl.startsWith("data:image", ignoreCase = true)) "" else opt.imageUrl,
                                        onValueChange = { urlVal ->
                                            colorOptions = colorOptions.mapIndexed { i, c ->
                                                if (i == idx) c.copy(imageUrl = urlVal) else c
                                            }
                                        },
                                        label = { Text("Per-Colour Image URL (optional)") },
                                        placeholder = { Text("https://...") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add Custom Colour", fontSize = spec.sp(11.5f), fontWeight = FontWeight.SemiBold, color = iCaseNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customColorName,
                            onValueChange = { customColorName = it },
                            label = { Text("Colour Name") },
                            placeholder = { Text("Desert Titanium") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("admin_pouch_custom_color_name")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = customColorHex,
                            onValueChange = { customColorHex = it },
                            label = { Text("Hex") },
                            placeholder = { Text("#C4B8A5") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_pouch_custom_color_hex")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val previewHexCol = remember(customColorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(JsonHelper.normalizeHexColor(customColorHex)))
                            } catch (_: Exception) {
                                Color(0xFF1E293B)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(previewHexCol)
                                .border(1.dp, Color(0xFF64748B), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val clean = customColorName.trim()
                            if (clean.isNotEmpty()) {
                                val entry = PouchColorOption(
                                    name = clean,
                                    hex = JsonHelper.normalizeHexColor(customColorHex)
                                )
                                val existingIdx = colorOptions.indexOfFirst { it.name.equals(clean, ignoreCase = true) }
                                colorOptions = if (existingIdx >= 0) {
                                    colorOptions.mapIndexed { i, old -> if (i == existingIdx) old.copy(hex = entry.hex) else old }
                                } else {
                                    colorOptions + entry
                                }
                                customColorName = ""
                                customColorHex = "#1E293B"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("admin_pouch_add_custom_color_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Custom Colour", fontSize = spec.sp(11.5f), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OfferEditorSection(
                    offerActive = offerActive,
                    onOfferActiveChange = { offerActive = it },
                    offerType = offerType,
                    onOfferTypeChange = {
                        offerType = it
                        if (it != ScreenGuardOfferType.NONE) offerActive = true
                    },
                    offerValueStr = offerValueStr,
                    onOfferValueChange = { offerValueStr = it },
                    badgeText = badgeText,
                    onBadgeTextChange = { badgeText = it }
                )
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg!!, color = iCaseRed, fontSize = spec.sp(12), fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isBlank()) {
                        errorMsg = "Enter a pouch name."
                        return@Button
                    }
                    val brands = mutableListOf<String>()
                    if (forIphone) brands.add("iPhone")
                    if (forSamsung) brands.add("Samsung S Series")
                    if (brands.isEmpty()) {
                        errorMsg = "Select at least one compatible brand."
                        return@Button
                    }
                    val price = (priceStr.toDoubleOrNull() ?: 299.0).coerceAtLeast(1.0)
                    val offer = ScreenGuardOffer(
                        active = offerActive && offerType != ScreenGuardOfferType.NONE,
                        type = if (offerActive) offerType else ScreenGuardOfferType.NONE,
                        value = (offerValueStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                        badgeText = badgeText.trim()
                    )
                    val generatedId = initial?.id ?: ("pouch_${System.currentTimeMillis()}_${cleanName.lowercase().replace(Regex("[^a-z0-9]"), "_").take(16)}")
                    onSave(
                        PouchAdminItem(
                            id = generatedId,
                            name = cleanName,
                            description = description.trim(),
                            basePrice = price,
                            compatibleBrands = brands,
                            enabled = initial?.enabled ?: true,
                            deleted = false,
                            offer = offer,
                            imageUrl = imageUrl.trim(),
                            colorOptions = colorOptions.map {
                                it.copy(
                                    name = it.name.trim(),
                                    hex = JsonHelper.normalizeHexColor(it.hex),
                                    imageUrl = it.imageUrl.trim()
                                )
                            },
                            showOnHomePage = showOnHomePage
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("admin_save_pouch_button")
            ) {
                Text("Save Pouch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfferEditorSection(
    offerActive: Boolean,
    onOfferActiveChange: (Boolean) -> Unit,
    offerType: ScreenGuardOfferType,
    onOfferTypeChange: (ScreenGuardOfferType) -> Unit,
    offerValueStr: String,
    onOfferValueChange: (String) -> Unit,
    badgeText: String,
    onBadgeTextChange: (String) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Enable Offer / Discount", fontSize = spec.sp(13), fontWeight = FontWeight.Bold, color = iCaseNavy)
        Switch(
            checked = offerActive,
            onCheckedChange = onOfferActiveChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iCaseGreen)
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            ScreenGuardOfferType.NONE to "None",
            ScreenGuardOfferType.FLAT_OFF to "Flat ₹ Off",
            ScreenGuardOfferType.PERCENT_OFF to "% Off",
            ScreenGuardOfferType.SPECIAL_PRICE to "Offer Price ₹"
        ).forEach { (type, label) ->
            FilterChip(
                selected = offerType == type,
                onClick = { onOfferTypeChange(type) },
                label = { Text(label, fontSize = spec.sp(11)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = iCaseRed,
                    selectedLabelColor = Color.White
                )
            )
        }
    }

    if (offerType != ScreenGuardOfferType.NONE) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = offerValueStr,
            onValueChange = onOfferValueChange,
            label = {
                Text(
                    when (offerType) {
                        ScreenGuardOfferType.FLAT_OFF -> "Discount Amount (₹)"
                        ScreenGuardOfferType.PERCENT_OFF -> "Discount Percentage (%)"
                        ScreenGuardOfferType.SPECIAL_PRICE -> "Final Offer Price (₹)"
                        ScreenGuardOfferType.NONE -> "Value"
                    }
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = badgeText,
            onValueChange = onBadgeTextChange,
            label = { Text("Promo Badge Label (optional)") },
            placeholder = { Text("e.g. WEEKEND DEAL, COMBO SAVE") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminCatalogModelPickerDialog(
    title: String,
    brands: List<String>,
    modelsByBrand: Map<String, List<String>>,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var selectedBrand by remember { mutableStateOf(brands.firstOrNull().orEmpty()) }
    var search by remember { mutableStateOf("") }
    val models = remember(selectedBrand, search, modelsByBrand) {
        val list = modelsByBrand[selectedBrand].orEmpty()
        val q = search.trim().lowercase()
        if (q.isBlank()) list else list.filter { it.lowercase().contains(q) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = spec.sp(16), color = iCaseNavy) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    brands.forEach { b ->
                        FilterChip(
                            selected = selectedBrand == b,
                            onClick = { selectedBrand = b },
                            label = { Text(b, fontSize = spec.sp(11)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = iCaseNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search model...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(models, key = { it }) { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp)
                                .clickable { onSelect(selectedBrand, model) }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(model, fontSize = spec.sp(13), fontWeight = FontWeight.Medium, color = iCaseNavy)
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun AdminTemperedModelEditDialog(
    brand: String,
    model: String,
    applicableTypes: List<String>,
    config: ScreenGuardAdminConfig,
    initial: ModelAdminConfig,
    onDismiss: () -> Unit,
    onDeleteModel: () -> Unit,
    onSave: (String, ModelAdminConfig) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var modelNameInput by remember { mutableStateOf(model) }
    var enabled by remember { mutableStateOf(initial.enabled && !initial.deleted) }
    var customRateInputs by remember {
        mutableStateOf(
            applicableTypes.associateWith { type ->
                initial.customRates[type]?.roundToInt()?.toString().orEmpty()
            }
        )
    }
    var offerActive by remember { mutableStateOf(initial.offer.active) }
    var offerType by remember { mutableStateOf(initial.offer.type) }
    var offerValueStr by remember {
        mutableStateOf(if (initial.offer.value > 0.0) initial.offer.value.roundToInt().toString() else "")
    }
    var badgeText by remember { mutableStateOf(initial.offer.badgeText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$brand • $model", fontWeight = FontWeight.Bold, fontSize = spec.sp(15.5f), color = iCaseNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = modelNameInput,
                    onValueChange = { modelNameInput = it },
                    label = { Text("Model Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Model Active in Picker", fontSize = spec.sp(13), fontWeight = FontWeight.Bold, color = iCaseNavy)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Custom Rates per Glass Type (blank = default)", fontSize = spec.sp(11.5f), fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                Spacer(modifier = Modifier.height(6.dp))
                applicableTypes.forEach { gType ->
                    val defaultTypePrice = config.glassTypeConfigs[gType]?.basePrice?.roundToInt()
                        ?: ScreenGuardAdminConfig.defaultPricesByGlassType[gType]?.roundToInt()
                        ?: 199
                    OutlinedTextField(
                        value = customRateInputs[gType].orEmpty(),
                        onValueChange = { v ->
                            customRateInputs = customRateInputs.toMutableMap().apply { put(gType, v) }
                        },
                        label = { Text("$gType (Default ₹$defaultTypePrice)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OfferEditorSection(
                    offerActive = offerActive,
                    onOfferActiveChange = { offerActive = it },
                    offerType = offerType,
                    onOfferTypeChange = {
                        offerType = it
                        if (it != ScreenGuardOfferType.NONE) offerActive = true
                    },
                    offerValueStr = offerValueStr,
                    onOfferValueChange = { offerValueStr = it },
                    badgeText = badgeText,
                    onBadgeTextChange = { badgeText = it }
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDeleteModel,
                    border = BorderStroke(1.dp, iCaseRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = iCaseRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Model Permanently", color = iCaseRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = modelNameInput.trim().ifBlank { model }
                    val parsedRates = linkedMapOf<String, Double>()
                    customRateInputs.forEach { (gType, str) ->
                        val v = str.trim().toDoubleOrNull()
                        if (v != null && v > 0.0) {
                            parsedRates[gType] = v
                        }
                    }
                    val offer = ScreenGuardOffer(
                        active = offerActive && offerType != ScreenGuardOfferType.NONE,
                        type = if (offerActive) offerType else ScreenGuardOfferType.NONE,
                        value = (offerValueStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                        badgeText = badgeText.trim()
                    )
                    onSave(
                        cleanName,
                        ModelAdminConfig(
                            enabled = enabled,
                            deleted = false,
                            customRates = parsedRates,
                            offer = offer
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy)
            ) {
                Text("Save Model")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AdminCameraModelEditDialog(
    brand: String,
    model: String,
    defaultPrice: Double,
    initial: CameraLensModelConfig,
    onDismiss: () -> Unit,
    onDeleteModel: () -> Unit,
    onSave: (String, CameraLensModelConfig) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var modelNameInput by remember { mutableStateOf(model) }
    var enabled by remember { mutableStateOf(initial.enabled && !initial.deleted) }
    var customPriceStr by remember { mutableStateOf(initial.customPrice?.roundToInt()?.toString().orEmpty()) }
    var offerActive by remember { mutableStateOf(initial.offer.active) }
    var offerType by remember { mutableStateOf(initial.offer.type) }
    var offerValueStr by remember {
        mutableStateOf(if (initial.offer.value > 0.0) initial.offer.value.roundToInt().toString() else "")
    }
    var badgeText by remember { mutableStateOf(initial.offer.badgeText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Camera Lens • $model", fontWeight = FontWeight.Bold, fontSize = spec.sp(15.5f), color = iCaseNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Category: $brand", fontSize = spec.sp(11.5f), color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = modelNameInput,
                    onValueChange = { modelNameInput = it },
                    label = { Text("Model Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Model Active in Camera Lens", fontSize = spec.sp(13), fontWeight = FontWeight.Bold, color = iCaseNavy)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customPriceStr,
                    onValueChange = { customPriceStr = it },
                    label = { Text("Custom Rate (₹, Default ₹${defaultPrice.roundToInt()})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OfferEditorSection(
                    offerActive = offerActive,
                    onOfferActiveChange = { offerActive = it },
                    offerType = offerType,
                    onOfferTypeChange = {
                        offerType = it
                        if (it != ScreenGuardOfferType.NONE) offerActive = true
                    },
                    offerValueStr = offerValueStr,
                    onOfferValueChange = { offerValueStr = it },
                    badgeText = badgeText,
                    onBadgeTextChange = { badgeText = it }
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDeleteModel,
                    border = BorderStroke(1.dp, iCaseRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = iCaseRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Camera Lens Model", color = iCaseRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = modelNameInput.trim().ifBlank { model }
                    val custom = customPriceStr.trim().toDoubleOrNull()?.takeIf { it > 0.0 }
                    val offer = ScreenGuardOffer(
                        active = offerActive && offerType != ScreenGuardOfferType.NONE,
                        type = if (offerActive) offerType else ScreenGuardOfferType.NONE,
                        value = (offerValueStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                        badgeText = badgeText.trim()
                    )
                    onSave(
                        cleanName,
                        CameraLensModelConfig(
                            enabled = enabled,
                            deleted = false,
                            customPrice = custom,
                            offer = offer
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AdminPouchModelEditDialog(
    brand: String,
    model: String,
    initial: ModelAdminConfig,
    onDismiss: () -> Unit,
    onDeleteModel: () -> Unit,
    onSave: (String, ModelAdminConfig) -> Unit
) {
    val spec = LocalResponsiveSpec.current
    var modelNameInput by remember { mutableStateOf(model) }
    var enabled by remember { mutableStateOf(initial.enabled && !initial.deleted) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pouch Model • $model", fontWeight = FontWeight.Bold, fontSize = spec.sp(15.5f), color = iCaseNavy) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Brand Group: $brand", fontSize = spec.sp(11.5f), color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = modelNameInput,
                    onValueChange = { modelNameInput = it },
                    label = { Text("Model Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Model Active in Pouches", fontSize = spec.sp(13), fontWeight = FontWeight.Bold, color = iCaseNavy)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDeleteModel,
                    border = BorderStroke(1.dp, iCaseRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = iCaseRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Pouch Model", color = iCaseRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = modelNameInput.trim().ifBlank { model }
                    onSave(cleanName, initial.copy(enabled = enabled, deleted = false))
                },
                colors = ButtonDefaults.buttonColors(containerColor = iCaseNavy)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
