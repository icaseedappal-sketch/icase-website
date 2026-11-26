package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.components.BrandFooterBanner
import com.example.ui.components.CameraLensProtectionCard
import com.example.ui.components.GuardSampleVideoCard
import com.example.ui.components.InstallationOptionCard
import com.example.ui.components.PouchesFinderCard
import com.example.ui.components.TemperedGlassFinderCard
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.ResponsiveCenteredBox
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import kotlinx.coroutines.launch

@Composable
fun ScreenGuardsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialSection: String = ""
) {
    val spec = LocalResponsiveSpec.current
    val screenGuardConfig by viewModel.screenGuardConfig.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val totalCartCount = cartItems.sumOf { it.quantity }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var installationOption by remember { mutableStateOf("Shop Installation") }
    var selectedGlassTriple by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var selectedGlassDevice by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(initialSection) {
        val targetIndex = when (initialSection.uppercase()) {
            "TEMPERED_GLASS" -> 0
            "CAMERA_LENS" -> 1
            "POUCHES" -> 2
            else -> -1
        }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    fun notifyAdded(label: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val res = snackbarHostState.showSnackbar(
                message = "Added $label to Cart",
                actionLabel = "View Cart",
                duration = SnackbarDuration.Short
            )
            if (res == SnackbarResult.ActionPerformed) {
                onCartClick()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("screen_guards_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("guards_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = iCaseNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Tempered Glass & Lens",
                            fontSize = spec.sp(18.5f),
                            fontWeight = FontWeight.Bold,
                            color = iCaseNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("guards_cart_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (totalCartCount > 0) {
                                    Badge(
                                        containerColor = iCaseRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("$totalCartCount", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = iCaseNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = if (spec.useNavRail) 24.dp else 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Tempered Glass Section
                item {
                    ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                        TemperedGlassFinderCard(
                            config = screenGuardConfig,
                            isHighlighted = initialSection.equals("TEMPERED_GLASS", ignoreCase = true),
                            installationOption = installationOption,
                            onAddToCart = { brand, model, glassType ->
                                viewModel.addTemperedGlassToCart(brand, model, glassType, installationOption)
                                notifyAdded("$model ($glassType)")
                            },
                            onSelectionChanged = { brand, model, glassType ->
                                selectedGlassTriple = if (model.isNotBlank() && glassType.isNotBlank()) {
                                    Triple(brand, model, glassType)
                                } else {
                                    null
                                }
                                selectedGlassDevice = if (model.isNotBlank()) brand to model else null
                            },
                            onWhatsAppUvEnquiry = {
                                viewModel.openWhatsAppEnquiry(
                                    "Hi iCASE Edappal, I would like UV tempered glass options for a curved-display Samsung S series phone."
                                )
                            },
                            onWhatsAppOrder = { msg ->
                                viewModel.openWhatsAppEnquiry(msg)
                            },
                            modifier = Modifier.padding(horizontal = spec.horizontalPadding)
                        )
                    }
                }

                // 2. Camera Lens Protection + Quick Combo Section
                item {
                    ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                        CameraLensProtectionCard(
                            config = screenGuardConfig,
                            isHighlighted = initialSection.equals("CAMERA_LENS", ignoreCase = true),
                            installationOption = installationOption,
                            selectedGlassSelection = selectedGlassTriple,
                            selectedGlassDevice = selectedGlassDevice,
                            onAddToCart = { brand, model ->
                                viewModel.addCameraLensToCart(brand, model, installationOption)
                                notifyAdded("$model Camera Lens")
                            },
                            onAddComboToCart = { gBrand, gModel, gType, cBrand, cModel ->
                                viewModel.addComboToCart(
                                    glassBrand = gBrand,
                                    glassModel = gModel,
                                    glassType = gType,
                                    cameraBrand = cBrand,
                                    cameraModel = cModel,
                                    installationOption = installationOption
                                )
                                notifyAdded("$gModel + Lens Combo")
                            },
                            onWhatsAppOrder = { msg ->
                                viewModel.openWhatsAppEnquiry(msg)
                            },
                            modifier = Modifier.padding(horizontal = spec.horizontalPadding)
                        )
                    }
                }

                // 3. Separate Pouches Section (iPhone, Samsung S Series & WhatsApp for Other Android)
                item {
                    ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                        PouchesFinderCard(
                            config = screenGuardConfig,
                            onAddPouchToCart = { brand, model, pouch, selectedColor ->
                                viewModel.addPouchToCart(brand, model, pouch, selectedColor)
                                val label = if (selectedColor.isNotBlank()) {
                                    "${pouch.name} • $selectedColor ($model)"
                                } else {
                                    "${pouch.name} ($model)"
                                }
                                notifyAdded(label)
                            },
                            onWhatsAppStockPhotos = {
                                viewModel.openWhatsAppEnquiry(
                                    "Hi iCASE Edappal, please share today's available pouch stock photos for my Android smartphone."
                                )
                            },
                            modifier = Modifier.padding(horizontal = spec.horizontalPadding)
                        )
                    }
                }

                // 4. Installation Preference Toggle
                item {
                    ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                        InstallationOptionCard(
                            selectedOption = installationOption,
                            onSelectOption = { installationOption = it },
                            modifier = Modifier.padding(horizontal = spec.horizontalPadding)
                        )
                    }
                }

                // 5. Bottom Video Banner
                item {
                    ResponsiveCenteredBox(maxWidth = spec.browseMaxWidth) {
                        GuardSampleVideoCard(
                            modifier = Modifier.padding(horizontal = spec.horizontalPadding)
                        )
                    }
                }

                item {
                    BrandFooterBanner()
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                    )
                )
                .padding(bottom = if (spec.useNavRail) 16.dp else 80.dp, start = 16.dp, end = 16.dp)
        )
    }
}
