package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.CameraLensModelConfig
import com.example.data.GlassTypeAdminConfig
import com.example.data.InitialData
import com.example.data.ModelAdminConfig
import com.example.data.PouchAdminItem
import com.example.data.PouchColorOption
import com.example.data.ReviewRatingHelper
import com.example.data.ReviewVerificationResult
import com.example.data.ScreenGuardAdminConfig
import com.example.data.ScreenGuardCatalog
import com.example.data.ScreenGuardOffer
import com.example.data.ScreenGuardOfferType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MietublAndReviewsTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var scope: CoroutineScope
    private lateinit var repo: AppRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("icase_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

        runBlocking {
            db.appDao().insertAllProducts(InitialData.products)
            db.appDao().insertAllSettings(InitialData.defaultSettings)
        }

        repo = AppRepository(
            appDao = db.appDao(),
            context = context,
            scope = scope,
            injectedFirestore = null,
            injectedAuth = null
        )
    }

    @After
    fun tearDown() {
        scope.cancel()
        db.close()
    }

    @Test
    fun screenGuardCatalog_hasSevenBrandsModelsAndCameraLensSubset() {
        val expectedBrands = listOf(
            "iPhone",
            "Redmi/Xiaomi",
            "Samsung",
            "Oppo/Realme",
            "Vivo/iQOO",
            "OnePlus",
            "Nothing"
        )
        assertEquals(expectedBrands, ScreenGuardCatalog.brands)
        assertEquals("Other Model (Type Manually)", ScreenGuardCatalog.OTHER_MODEL_OPTION)

        expectedBrands.forEach { brand ->
            assertTrue("Brand $brand should contain models", ScreenGuardCatalog.modelsForBrand(brand).isNotEmpty())
        }

        val groupedS24 = ScreenGuardCatalog.searchGroupedModels(null, "S24")
        assertTrue(groupedS24.containsKey("Samsung"))
        assertTrue(groupedS24["Samsung"].orEmpty().any { it.contains("S24 Ultra") })

        val filteredNothing = ScreenGuardCatalog.searchGroupedModels("Nothing", "Phone (2a)")
        assertEquals(listOf("Nothing"), filteredNothing.keys.toList())
        assertTrue(filteredNothing["Nothing"].orEmpty().any { it.contains("Phone (2a)") })

        // Curved S series must be excluded from Tempered Glass list, while flat S series stay available
        val samsungGlassModels = ScreenGuardCatalog.modelsForBrand("Samsung")
        assertFalse(samsungGlassModels.contains("Galaxy S20 FE / S20"))
        assertFalse(samsungGlassModels.contains("Galaxy S21 Ultra"))
        assertFalse(samsungGlassModels.contains("Galaxy S22 Ultra"))
        assertFalse(samsungGlassModels.contains("Galaxy S23 Ultra"))
        assertTrue(samsungGlassModels.contains("Galaxy S24 Ultra"))
        assertTrue(samsungGlassModels.contains("Galaxy S25 Ultra"))

        // Camera Lens Protection & Pouches must include ONLY iPhone & ALL Samsung S Series (S20-S25 + Ultra)
        assertEquals(listOf("iPhone", "Samsung S Series"), ScreenGuardCatalog.cameraLensBrands)
        assertEquals(listOf("iPhone", "Samsung S Series"), ScreenGuardCatalog.pouchBrands)
        val camSamsung = ScreenGuardCatalog.cameraLensModelsByBrand["Samsung S Series"].orEmpty()
        assertTrue(camSamsung.contains("Galaxy S20 FE / S20"))
        assertTrue(camSamsung.contains("Galaxy S21 Ultra"))
        assertTrue(camSamsung.contains("Galaxy S22 Ultra"))
        assertTrue(camSamsung.contains("Galaxy S23 Ultra"))
        assertTrue(camSamsung.contains("Galaxy S24 Ultra"))
        assertTrue(camSamsung.contains("Galaxy S25 Ultra"))
        assertTrue(camSamsung.contains("Galaxy S25 FE"))
        assertFalse(camSamsung.any { it.contains("A55") || it.contains("Fold") || it.contains("M55") })
        assertTrue(ScreenGuardCatalog.searchCameraLensModels(null, "Redmi").isEmpty())
        assertTrue(ScreenGuardCatalog.searchCameraLensModels(null, "OnePlus").isEmpty())
        assertEquals(camSamsung, ScreenGuardCatalog.pouchModelsByBrand["Samsung S Series"].orEmpty())
    }

    @Test
    fun temperedGlassTypes_matchDynamicIphoneSamsungFlatSAndStandardRules() {
        val expectedIphone = listOf(
            "Privacy",
            "Matte",
            "Borderless Tempered Glass",
            "360 Privacy",
            "Normal 5D"
        )
        assertEquals(expectedIphone, ScreenGuardCatalog.glassTypesForDevice("iPhone", "iPhone 16 Pro Max"))
        assertEquals(expectedIphone, ScreenGuardCatalog.glassTypesForDevice("iPhone", "iPhone SE (2020 / 2022)"))

        // Samsung Flat S Series -> ONLY Privacy & Matte
        val flatSamsungModels = listOf(
            "Galaxy S21", "Galaxy S21+", "Galaxy S21 FE",
            "Galaxy S22", "Galaxy S22+",
            "Galaxy S23", "Galaxy S23+", "Galaxy S23 FE",
            "Galaxy S24", "Galaxy S24+", "Galaxy S24 Ultra", "Galaxy S24 FE",
            "Galaxy S25", "Galaxy S25+", "Galaxy S25 Ultra", "Galaxy S25 FE"
        )
        flatSamsungModels.forEach { model ->
            assertTrue("$model should be flat S series", ScreenGuardCatalog.isSamsungFlatSSeries("Samsung", model))
            assertEquals(listOf("Privacy", "Matte"), ScreenGuardCatalog.glassTypesForDevice("Samsung", model))
        }

        // Samsung Curved S Series excluded from Tempered Glass picker
        val curvedSamsungModels = listOf(
            "Galaxy S20 FE / S20",
            "Galaxy S21 Ultra",
            "Galaxy S22 Ultra",
            "Galaxy S23 Ultra"
        )
        curvedSamsungModels.forEach { model ->
            assertFalse("$model must not be treated as flat S series", ScreenGuardCatalog.isSamsungFlatSSeries("Samsung", model))
            assertEquals(emptyList<String>(), ScreenGuardCatalog.glassTypesForDevice("Samsung", model))
        }

        // Non-S Samsung -> standard Normal 5D only
        val nonSSamsungModels = listOf(
            "Galaxy A55 5G",
            "Galaxy M55 5G",
            "Galaxy Z Fold 6 / Flip 6"
        )
        nonSSamsungModels.forEach { model ->
            assertFalse("$model must not be treated as flat S series", ScreenGuardCatalog.isSamsungFlatSSeries("Samsung", model))
            assertEquals(listOf("Normal 5D"), ScreenGuardCatalog.glassTypesForDevice("Samsung", model))
        }

        // All other brands & manual entry -> standard Normal 5D only
        assertEquals(listOf("Normal 5D"), ScreenGuardCatalog.glassTypesForDevice("Redmi/Xiaomi", "Xiaomi 15"))
        assertEquals(listOf("Normal 5D"), ScreenGuardCatalog.glassTypesForDevice("Oppo/Realme", "Oppo Find X8 Pro"))
        assertEquals(listOf("Normal 5D"), ScreenGuardCatalog.glassTypesForDevice("Vivo/iQOO", "Vivo X200 Pro"))
        assertEquals(listOf("Normal 5D"), ScreenGuardCatalog.glassTypesForDevice("OnePlus", "OnePlus 13"))
        assertEquals(listOf("Normal 5D"), ScreenGuardCatalog.glassTypesForDevice("Nothing", "Nothing Phone (2)"))
        assertEquals(listOf("Normal 5D"), ScreenGuardCatalog.glassTypesForDevice("", ScreenGuardCatalog.OTHER_MODEL_OPTION))
    }

    @Test
    fun verifiedReviewEligibility_requiresCompletedOrderOrRepair() = runBlocking {
        assertTrue(repo.verifyReferenceForReview("ORD-UNKNOWN") is ReviewVerificationResult.NotFound)

        repo.updateCustomerProfile("Nihal", "9876543210", "9876543210", "Edappal")
        repo.addToCart(InitialData.products.first(), 1)
        val order = repo.placeOrder("Nihal", "9876543210", "9876543210", "Edappal", "STANDARD", 50.0)
        val repair = repo.bookRepair(
            brandName = "Apple",
            modelName = "iPhone 15",
            complaints = listOf("Display"),
            description = "Glass cracked",
            photoUri = null,
            pickupAddress = "Edappal",
            customerName = "Nihal",
            phoneNumber = "9876543210",
            whatsappNumber = "9876543210",
            preferredDate = "Today",
            preferredTimeSlot = "10:00 AM - 12:00 PM"
        )

        // Pending items must be rejected as NotCompleted
        val pendingOrderRes = repo.verifyReferenceForReview(order.id)
        assertTrue(pendingOrderRes is ReviewVerificationResult.NotCompleted)
        val pendingRepairRes = repo.verifyReferenceForReview(repair.id)
        assertTrue(pendingRepairRes is ReviewVerificationResult.NotCompleted)

        // Mark completed -> both become eligible with right types
        repo.updateOrderStatus(order.id, "Delivered", "Delivered safely")
        repo.updateRepairStatus(repair.id, "Repair Completed", "Ready")

        val eligibleOrder = repo.verifyReferenceForReview(order.id)
        assertTrue(eligibleOrder is ReviewVerificationResult.EligibleProduct)
        assertEquals(order.id, (eligibleOrder as ReviewVerificationResult.EligibleProduct).referenceId)

        val eligibleRepair = repo.verifyReferenceForReview(repair.id)
        assertTrue(eligibleRepair is ReviewVerificationResult.EligibleService)
        assertEquals(repair.id, (eligibleRepair as ReviewVerificationResult.EligibleService).referenceId)
    }

    @Test
    fun reviewPrivacySplit_keepsServiceReviewsStrictlyPrivateAndModeratesProducts() = runBlocking {
        repo.updateCustomerProfile("Amina", "9111222333", "9111222333", "Edappal")
        repo.addToCart(InitialData.products.first(), 1)
        val order = repo.placeOrder("Amina", "9111222333", "9111222333", "Edappal", "STANDARD", 50.0)
        val repair = repo.bookRepair(
            brandName = "Samsung",
            modelName = "Galaxy S24 Ultra",
            complaints = listOf("Battery"),
            description = "Fast drain",
            photoUri = null,
            pickupAddress = "Edappal",
            customerName = "Amina",
            phoneNumber = "9111222333",
            whatsappNumber = "9111222333",
            preferredDate = "Today",
            preferredTimeSlot = "2:00 PM - 4:00 PM"
        )
        repo.updateOrderStatus(order.id, "Completed", "Handed over")
        repo.updateRepairStatus(repair.id, "Delivered", "Returned to customer")

        // Submit product review -> defaults unapproved (hidden from public until admin approves)
        val prodReviewRes = repo.submitVerifiedReview(order.id, "Amina", 5, "Awesome tempered glass finish!")
        assertTrue(prodReviewRes.isSuccess)
        val prodReview = prodReviewRes.getOrThrow()
        assertEquals("PRODUCT", prodReview.reviewType)
        assertFalse(prodReview.isApproved)
        assertTrue("Pending product review must not be public yet", repo.publicProductReviews.first().isEmpty())
        assertEquals(1, repo.adminAllReviews.first().size)

        // Submit service review -> strictly private, never approved/public
        val svcReviewRes = repo.submitVerifiedReview(repair.id, "Amina", 4, "Quick doorstep technician pickup.")
        assertTrue(svcReviewRes.isSuccess)
        val svcReview = svcReviewRes.getOrThrow()
        assertEquals("SERVICE", svcReview.reviewType)
        assertFalse(svcReview.isApproved)
        assertTrue(repo.publicProductReviews.first().isEmpty())
        assertEquals(2, repo.adminAllReviews.first().size)

        // Admin approves product review & attempts to approve service review (service must stay private)
        assertTrue(repo.verifyAdminPassword("2026"))
        repo.setProductReviewApproved(prodReview.id, true)
        repo.setProductReviewApproved(svcReview.id, true)

        val publicReviews = repo.publicProductReviews.first()
        assertEquals(1, publicReviews.size)
        assertEquals(prodReview.id, publicReviews.first().id)
        assertTrue(publicReviews.none { it.reviewType == "SERVICE" })

        // Admin reply & manual review (manual SERVICE review also forced private)
        repo.replyToReview(prodReview.id, "Thank you for shopping at iCASE Edappal!")
        assertEquals(
            "Thank you for shopping at iCASE Edappal!",
            repo.publicProductReviews.first().first().adminReply
        )

        val manualSvc = repo.addManualVerifiedReview(
            referenceId = "REP-9001",
            reviewType = "SERVICE",
            targetTitle = "iPhone 14 Screen Repair",
            customerName = "Rahul",
            rating = 5,
            comment = "Private technician feedback",
            isApproved = true // Must be forced false for SERVICE
        )
        assertFalse(manualSvc.isApproved)
        assertTrue(repo.publicProductReviews.first().none { it.id == manualSvc.id })

        // Admin hides product review & deletes manual review
        repo.setProductReviewApproved(prodReview.id, false)
        assertTrue(repo.publicProductReviews.first().isEmpty())
        repo.deleteReview(manualSvc.id)
        assertTrue(repo.adminAllReviews.first().none { it.id == manualSvc.id })

        // Admin logs out -> private/unapproved reviews wiped from local Room cache
        repo.adminLogout()
        val remainingAfterLogout = withTimeout(3000) {
            repo.adminAllReviews.first { it.isEmpty() }
        }
        assertTrue(remainingAfterLogout.isEmpty())
    }

    @Test
    fun screenGuardAdminConfig_calculatesOffersRoundTripsAndFiltersDeletedItems() {
        val baseConfig = ScreenGuardAdminConfig(
            glassTypeConfigs = ScreenGuardAdminConfig.defaultGlassTypeConfigs().toMutableMap().apply {
                this["Privacy"] = GlassTypeAdminConfig(
                    basePrice = 400.0,
                    enabled = true,
                    deleted = false,
                    offer = ScreenGuardOffer(
                        active = true,
                        type = ScreenGuardOfferType.PERCENT_OFF,
                        value = 25.0,
                        badgeText = "25% OFF"
                    )
                )
                this["360 Privacy"] = GlassTypeAdminConfig(
                    basePrice = 499.0,
                    enabled = false,
                    deleted = true
                )
            },
            modelConfigs = mapOf(
                ScreenGuardAdminConfig.modelKey("iPhone", "iPhone 16 Pro Max") to ModelAdminConfig(
                    enabled = true,
                    deleted = false,
                    customRates = mapOf("Matte" to 350.0),
                    offer = ScreenGuardOffer(
                        active = true,
                        type = ScreenGuardOfferType.FLAT_OFF,
                        value = 50.0,
                        badgeText = "₹50 OFF"
                    )
                ),
                ScreenGuardAdminConfig.modelKey("Nothing", "CMF Phone 1") to ModelAdminConfig(
                    enabled = false,
                    deleted = true
                )
            ),
            cameraLensDefault = GlassTypeAdminConfig(
                basePrice = 249.0,
                enabled = true,
                deleted = false,
                offer = ScreenGuardOffer(
                    active = true,
                    type = ScreenGuardOfferType.SPECIAL_PRICE,
                    value = 179.0,
                    badgeText = "LENS DEAL"
                )
            ),
            cameraLensModelConfigs = mapOf(
                ScreenGuardAdminConfig.modelKey("iPhone", "iPhone 11") to CameraLensModelConfig(
                    enabled = false,
                    deleted = true
                )
            )
        )

        val reparsed = ScreenGuardAdminConfig.fromJsonString(baseConfig.toJsonString())
        assertEquals(baseConfig, reparsed)

        // Deleted glass type removed from device choices
        val iphoneTypes = ScreenGuardCatalog.glassTypesForDevice("iPhone", "iPhone 16 Pro Max", reparsed)
        assertFalse(iphoneTypes.contains("360 Privacy"))
        assertTrue(iphoneTypes.contains("Privacy"))

        // Deleted model hidden from catalog & search
        assertFalse(ScreenGuardCatalog.isTemperedGlassModelAvailable("Nothing", "CMF Phone 1", reparsed))
        assertFalse(ScreenGuardCatalog.searchGroupedModels("Nothing", "", reparsed)["Nothing"].orEmpty().contains("CMF Phone 1"))

        // Model offer overrides glass type offer -> ₹350 - ₹50 = ₹300
        val mattePricing = ScreenGuardCatalog.resolveTemperedGlassPrice("iPhone", "iPhone 16 Pro Max", "Matte", reparsed)
        assertEquals(350.0, mattePricing.basePrice, 0.01)
        assertEquals(300.0, mattePricing.finalPrice, 0.01)
        assertEquals("₹50 OFF", mattePricing.offerBadge)

        // Non-overridden model inherits glass type offer -> ₹400 * 0.75 = ₹300
        val iphone15Privacy = ScreenGuardCatalog.resolveTemperedGlassPrice("iPhone", "iPhone 15", "Privacy", reparsed)
        assertEquals(400.0, iphone15Privacy.basePrice, 0.01)
        assertEquals(300.0, iphone15Privacy.finalPrice, 0.01)

        // Camera lens default offer -> Special ₹179 & deleted iPhone 11 hidden
        val camPricing = ScreenGuardCatalog.resolveCameraLensPrice("iPhone", "iPhone 16 Pro", reparsed)
        assertEquals(179.0, camPricing.finalPrice, 0.01)
        assertEquals("LENS DEAL", camPricing.offerBadge)
        assertFalse(ScreenGuardCatalog.isCameraLensModelAvailable("iPhone", "iPhone 11", reparsed))
    }

    @Test
    fun cartItemsRepriceLiveAndDropDeletedOptionsWhenAdminUpdatesConfigOrDeletesProduct() = runBlocking {
        // 1. Add Tempered Glass & Camera Lens items directly to in-app cart
        repo.addTemperedGlassToCart("iPhone", "iPhone 15", "Privacy")
        repo.addCameraLensToCart("Samsung S Series", "Galaxy S24 Ultra")
        val shopProduct = InitialData.products.first()
        repo.addToCart(shopProduct, 1)

        assertEquals(3, repo.cartItems.value.size)

        // 2. Admin updates Privacy rate + offer and updates Camera Lens rate
        val updatedConfig = ScreenGuardAdminConfig(
            glassTypeConfigs = ScreenGuardAdminConfig.defaultGlassTypeConfigs().toMutableMap().apply {
                this["Privacy"] = GlassTypeAdminConfig(
                    basePrice = 400.0,
                    enabled = true,
                    deleted = false,
                    offer = ScreenGuardOffer(
                        active = true,
                        type = ScreenGuardOfferType.FLAT_OFF,
                        value = 100.0,
                        badgeText = "FLASH ₹100 OFF"
                    )
                )
            },
            cameraLensDefault = GlassTypeAdminConfig(
                basePrice = 249.0,
                enabled = true,
                deleted = false,
                offer = ScreenGuardOffer(
                    active = true,
                    type = ScreenGuardOfferType.SPECIAL_PRICE,
                    value = 199.0,
                    badgeText = "SPECIAL"
                )
            )
        )
        repo.saveScreenGuardConfig(updatedConfig)

        val repricedPrivacy = repo.cartItems.value.first { it.product.id.startsWith("sg::") }
        assertEquals(300.0, repricedPrivacy.product.price, 0.01)
        assertEquals(400.0, repricedPrivacy.product.originalPrice, 0.01)

        val repricedCam = repo.cartItems.value.first { it.product.id.startsWith("cl::") }
        assertEquals(199.0, repricedCam.product.price, 0.01)

        // 3. Admin permanently deletes "Privacy" glass type & deletes shop product -> both drop from cart & settings persist deletion
        val configWithDelete = updatedConfig.copy(
            glassTypeConfigs = updatedConfig.glassTypeConfigs.toMutableMap().apply {
                this["Privacy"] = this["Privacy"]!!.copy(enabled = false, deleted = true)
            }
        )
        repo.saveScreenGuardConfig(configWithDelete)
        repo.deleteProduct(shopProduct.id)

        val remainingIds = repo.cartItems.value.map { it.product.id }
        assertEquals(listOf("cl::Samsung S Series::Galaxy S24 Ultra"), remainingIds)

        // Verify persistent deletion markers stored cleanly in Room settings
        val seededFlag = db.appDao().getSettingDirect(ScreenGuardCatalog.CATALOG_SEEDED_KEY)?.value
        val deletedJson = db.appDao().getSettingDirect(ScreenGuardCatalog.DELETED_PRODUCTS_KEY)?.value
        assertEquals("true", seededFlag)
        assertTrue(deletedJson.orEmpty().contains(shopProduct.id))
        assertTrue(db.appDao().getAllProductsDirect().none { it.id == shopProduct.id })
    }

    @Test
    fun comboAndPouchesAndCustomAdminEntriesWorkAndPreserveInstallationOption() = runBlocking {
        val customConfig = ScreenGuardAdminConfig(
            glassTypeConfigs = ScreenGuardAdminConfig.defaultGlassTypeConfigs().toMutableMap().apply {
                this["Privacy"] = this["Privacy"]!!.copy(helperNote = "Custom anti-spy helper note")
                this["Sapphire Anti-Blue"] = GlassTypeAdminConfig(
                    basePrice = 549.0,
                    helperNote = "Blocks harsh blue light & edges."
                )
            },
            customModelsByBrand = mapOf("Nothing" to listOf("Nothing Phone (3)")),
            customCameraLensModelsByBrand = mapOf("iPhone" to listOf("iPhone 17 Air")),
            comboEnabled = true,
            comboOffer = ScreenGuardOffer(
                active = true,
                type = ScreenGuardOfferType.FLAT_OFF,
                value = 100.0,
                badgeText = "COMBO ₹100 OFF"
            ),
            pouchItems = listOf(
                PouchAdminItem(
                    id = "pouch_custom_aramid",
                    name = "Aramid Carbon Case",
                    description = "Ultra-thin Kevlar weave",
                    basePrice = 799.0,
                    compatibleBrands = listOf("iPhone"),
                    offer = ScreenGuardOffer(
                        active = true,
                        type = ScreenGuardOfferType.SPECIAL_PRICE,
                        value = 599.0,
                        badgeText = "SPECIAL"
                    )
                ),
                PouchAdminItem(
                    id = "pouch_deleted_item",
                    name = "Old Deleted Case",
                    basePrice = 199.0,
                    enabled = false,
                    deleted = true
                )
            ),
            customPouchModelsByBrand = mapOf("Samsung S Series" to listOf("Galaxy S25 Edge"))
        )

        val reparsed = ScreenGuardAdminConfig.fromJsonString(customConfig.toJsonString())
        assertEquals(customConfig, reparsed)

        // Helper note override & custom glass type
        assertEquals("Custom anti-spy helper note", ScreenGuardCatalog.glassTypeHelperNote("Privacy", reparsed))
        assertTrue(ScreenGuardCatalog.glassTypesForDevice("iPhone", "iPhone 16", reparsed).contains("Sapphire Anti-Blue"))
        assertTrue(ScreenGuardCatalog.modelsForBrand("Nothing", reparsed).contains("Nothing Phone (3)"))
        assertTrue(ScreenGuardCatalog.cameraLensModelsForBrand("iPhone", reparsed).contains("iPhone 17 Air"))
        assertTrue(ScreenGuardCatalog.pouchModelsForBrand("Samsung S Series", reparsed).contains("Galaxy S25 Edge"))

        // Pouches brand filter & deleted filter
        val iphonePouches = ScreenGuardCatalog.activePouchesForBrand("iPhone", reparsed)
        assertEquals(listOf("pouch_custom_aramid"), iphonePouches.map { it.id })
        assertTrue(ScreenGuardCatalog.activePouchesForBrand("Samsung S Series", reparsed).isEmpty())

        // Repo save + add Combo & Pouch + installation option preservation across live reprice
        repo.saveScreenGuardConfig(reparsed)
        repo.addComboToCart("iPhone", "iPhone 16", "Privacy", "iPhone", "iPhone 16", "Shop Installation")
        repo.addPouchToCart("iPhone", "iPhone 16", iphonePouches.first())

        val comboItem = repo.cartItems.value.first { it.product.id.startsWith("combo::") }
        // Privacy 349 + Camera Lens 249 = 598 - 100 flat off = 498
        assertEquals(498.0, comboItem.product.price, 0.01)
        assertTrue(comboItem.product.description.contains("Fulfillment: Shop Installation"))

        val pouchItem = repo.cartItems.value.first { it.product.id.startsWith("pouch::") }
        assertEquals(599.0, pouchItem.product.price, 0.01)

        // Reprice preserving Shop Installation when combo discount changes
        val updatedComboConfig = reparsed.copy(
            comboOffer = ScreenGuardOffer(
                active = true,
                type = ScreenGuardOfferType.FLAT_OFF,
                value = 50.0,
                badgeText = "COMBO SAVE"
            )
        )
        repo.saveScreenGuardConfig(updatedComboConfig)
        val repricedCombo = repo.cartItems.value.first { it.product.id.startsWith("combo::") }
        assertEquals(548.0, repricedCombo.product.price, 0.01)
        assertTrue(repricedCombo.product.description.contains("Fulfillment: Shop Installation"))

        // Disabling combo removes combo item from live cart
        repo.saveScreenGuardConfig(updatedComboConfig.copy(comboEnabled = false))
        assertTrue(repo.cartItems.value.none { it.product.id.startsWith("combo::") })
    }

    @Test
    fun pouchImagesAndColors_roundTripResolveAndSeparateCartItemsByColor() = runBlocking {
        // Verify all 9 required preset colours exist
        val presetNames = ScreenGuardCatalog.presetPouchColors.map { it.name }
        assertEquals(
            listOf(
                "Black",
                "Midnight Blue",
                "Deep Green",
                "Lavender",
                "Red",
                "Clear/Transparent",
                "Pink",
                "Grey",
                "Brown"
            ),
            presetNames
        )

        val defaultDataImg = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD="
        val goldColorImg = "https://cdn.example.com/pouches/magsafe-gold.jpg"
        val pouchWithColors = PouchAdminItem(
            id = "pouch_magsafe_custom",
            name = "MagSafe Liquid Silicone",
            description = "Velvet interior lining",
            basePrice = 449.0,
            compatibleBrands = listOf("iPhone", "Samsung S Series"),
            imageUrl = defaultDataImg,
            colorOptions = listOf(
                PouchColorOption(name = "Black", hex = "#111827", imageUrl = ""),
                PouchColorOption(name = "Titanium Gold", hex = "#C5A059", imageUrl = goldColorImg)
            )
        )

        val cfg = ScreenGuardAdminConfig(pouchItems = listOf(pouchWithColors))
        val reparsed = ScreenGuardAdminConfig.fromJsonString(cfg.toJsonString())
        val parsedPouch = reparsed.pouchItems.first()
        assertEquals(pouchWithColors, parsedPouch)

        // Per-colour image overrides default pouch image; colour without own photo falls back to pouch image
        assertEquals(defaultDataImg, ScreenGuardCatalog.resolvePouchImageUrl(parsedPouch, "Black"))
        assertEquals(goldColorImg, ScreenGuardCatalog.resolvePouchImageUrl(parsedPouch, "Titanium Gold"))
        assertEquals(
            ScreenGuardCatalog.POUCH_IMAGE_URL,
            ScreenGuardCatalog.resolvePouchImageUrl(parsedPouch.copy(imageUrl = ""), "Black")
        )

        // Saving config & adding two colours of same pouch creates separate cart items with proper images & labels
        repo.saveScreenGuardConfig(reparsed)
        repo.addPouchToCart("iPhone", "iPhone 16 Pro", parsedPouch, "Black")
        repo.addPouchToCart("iPhone", "iPhone 16 Pro", parsedPouch, "Titanium Gold")

        assertEquals(2, repo.cartItems.value.size)
        val blackItem = repo.cartItems.value.first { it.product.id.endsWith("::Black") }
        val goldItem = repo.cartItems.value.first { it.product.id.endsWith("::Titanium Gold") }
        assertTrue(blackItem.product.name.contains("(Black)"))
        assertEquals(defaultDataImg, blackItem.product.imageUrl)
        assertTrue(goldItem.product.name.contains("(Titanium Gold)"))
        assertEquals(goldColorImg, goldItem.product.imageUrl)

        // Admin updates price + Titanium Gold image -> both live cart items reprice & refresh photo
        val updatedGoldImg = "https://cdn.example.com/pouches/magsafe-gold-v2.jpg"
        val updatedCfg = reparsed.copy(
            pouchItems = listOf(
                parsedPouch.copy(
                    basePrice = 399.0,
                    colorOptions = listOf(
                        PouchColorOption(name = "Black", hex = "#111827", imageUrl = ""),
                        PouchColorOption(name = "Titanium Gold", hex = "#C5A059", imageUrl = updatedGoldImg)
                    )
                )
            )
        )
        repo.saveScreenGuardConfig(updatedCfg)

        val repricedGold = repo.cartItems.value.first { it.product.id.endsWith("::Titanium Gold") }
        assertEquals(399.0, repricedGold.product.price, 0.01)
        assertEquals(updatedGoldImg, repricedGold.product.imageUrl)
    }

    @Test
    fun pouchShowOnHomePage_roundTripsAndFiltersActiveNonDeletedPouches() {
        val homeFeatured = PouchAdminItem(
            id = "pouch_home_featured",
            name = "MagSafe Clear Guard",
            basePrice = 399.0,
            enabled = true,
            deleted = false,
            showOnHomePage = true
        )
        val pausedHomePouch = PouchAdminItem(
            id = "pouch_home_paused",
            name = "Paused Silicone Case",
            basePrice = 299.0,
            enabled = false,
            deleted = false,
            showOnHomePage = true
        )
        val deletedHomePouch = PouchAdminItem(
            id = "pouch_home_deleted",
            name = "Deleted Leather Case",
            basePrice = 499.0,
            enabled = true,
            deleted = true,
            showOnHomePage = true
        )
        val nonHomePouch = PouchAdminItem(
            id = "pouch_catalog_only",
            name = "Catalog Only Armor",
            basePrice = 349.0,
            enabled = true,
            deleted = false,
            showOnHomePage = false
        )

        val cfg = ScreenGuardAdminConfig(
            pouchItems = listOf(homeFeatured, pausedHomePouch, deletedHomePouch, nonHomePouch)
        )
        val reparsed = ScreenGuardAdminConfig.fromJsonString(cfg.toJsonString())
        assertEquals(cfg, reparsed)
        assertTrue(reparsed.pouchItems.first { it.id == "pouch_home_featured" }.showOnHomePage)

        // Only enabled, non-deleted pouches with showOnHomePage = true should appear on the Home tab
        val featured = ScreenGuardCatalog.homeFeaturedPouches(reparsed)
        assertEquals(listOf("pouch_home_featured"), featured.map { it.id })

        // Toggling showOnHomePage off removes it immediately from Home featured pouches
        val toggledOff = reparsed.copy(
            pouchItems = reparsed.pouchItems.map {
                if (it.id == "pouch_home_featured") it.copy(showOnHomePage = false) else it
            }
        )
        assertTrue(ScreenGuardCatalog.homeFeaturedPouches(toggledOff).isEmpty())
    }

    @Test
    fun productReviewsAndModeration_computeRatingsEditApproveHideAndDeleteCleanly() = runBlocking {
        val product = InitialData.products.first()

        // 1. Submit two customer product reviews directly from Product Details (without Order ID -> unapproved initially)
        val rev1 = repo.submitProductReview(
            productId = product.id,
            productName = product.name,
            referenceId = "",
            customerName = "Fathima",
            rating = 5,
            comment = "Crystal clear fit and zero bubbles."
        ).getOrThrow()
        assertFalse(rev1.isApproved)
        assertEquals(product.id, rev1.productId)
        assertTrue(rev1.referenceId.startsWith("PROD-"))

        val rev2 = repo.submitProductReview(
            productId = product.id,
            productName = product.name,
            referenceId = "",
            customerName = "Arjun",
            rating = 3,
            comment = "Good overall."
        ).getOrThrow()

        // Pending reviews must not affect public rating summary until Admin approves
        val beforeApproval = ReviewRatingHelper.summarizeProductRating(
            repo.publicProductReviews.first(),
            product.id,
            product.name
        )
        assertEquals(0, beforeApproval.reviewCount)

        // 2. Admin approves both -> live rating becomes (5 + 3) / 2 = 4.0 across 2 reviews
        assertTrue(repo.verifyAdminPassword("2026"))
        repo.setProductReviewApproved(rev1.id, true)
        repo.setProductReviewApproved(rev2.id, true)

        val approvedSummary = ReviewRatingHelper.summarizeProductRating(
            repo.publicProductReviews.first(),
            product.id,
            product.name
        )
        assertEquals(2, approvedSummary.reviewCount)
        assertEquals(4.0, approvedSummary.averageRating, 0.01)

        // 3. Admin edits second review rating (3 -> 4) & comment -> live average updates to 4.5
        repo.updateReview(
            reviewId = rev2.id,
            targetTitle = product.name,
            customerName = "Arjun K",
            rating = 4,
            comment = "Great build quality after a week of use.",
            isApproved = true
        )
        val afterEdit = repo.publicProductReviews.first()
        val editedSummary = ReviewRatingHelper.summarizeProductRating(afterEdit, product.id, product.name)
        assertEquals(2, editedSummary.reviewCount)
        assertEquals(4.5, editedSummary.averageRating, 0.01)
        assertTrue(afterEdit.any { it.id == rev2.id && it.customerName == "Arjun K" })

        // 4. Service review cannot be published via updateReview(isApproved = true)
        val svcReview = repo.addManualVerifiedReview(
            referenceId = "REP-7777",
            reviewType = "SERVICE",
            targetTitle = "iPhone 15 Display Repair",
            customerName = "Zaid",
            rating = 5,
            comment = "Technician arrived fast.",
            isApproved = false
        )
        repo.updateReview(
            reviewId = svcReview.id,
            targetTitle = svcReview.targetTitle,
            customerName = svcReview.customerName,
            rating = 5,
            comment = svcReview.comment,
            isApproved = true
        )
        assertFalse(repo.adminAllReviews.first().first { it.id == svcReview.id }.isApproved)

        // 5. Hiding one review & deleting the other immediately updates public reviews & rating summary
        repo.setProductReviewApproved(rev1.id, false)
        val afterHide = ReviewRatingHelper.summarizeProductRating(
            repo.publicProductReviews.first(),
            product.id,
            product.name
        )
        assertEquals(1, afterHide.reviewCount)
        assertEquals(4.0, afterHide.averageRating, 0.01)

        repo.deleteReview(rev2.id)
        val afterDelete = ReviewRatingHelper.summarizeProductRating(
            repo.publicProductReviews.first(),
            product.id,
            product.name
        )
        assertEquals(0, afterDelete.reviewCount)
        assertTrue(repo.adminAllReviews.first().none { it.id == rev2.id })
    }

    @Test
    fun pouchesHomeCard_glassTypeQuickSelectAndMediaSupportWorkTogether() {
        val customPrivacyImg = "https://cdn.example.com/glass/privacy-custom.jpg"
        val customPrivacyVid = "https://cdn.example.com/glass/privacy-demo.mp4"
        val sapphireImg = "https://cdn.example.com/glass/sapphire.jpg"
        val sapphireVid = "https://youtu.be/sapphire9hDemo"
        val pouchCardImg = "https://cdn.example.com/pouches/hero-card.jpg"
        val camLensVid = "https://cdn.example.com/lens/demo.mp4"
        val pouchItemVid = "https://youtu.be/pouchArmor99"

        val cfg = ScreenGuardAdminConfig(
            glassTypeConfigs = ScreenGuardAdminConfig.defaultGlassTypeConfigs().toMutableMap().apply {
                this["Privacy"] = this["Privacy"]!!.copy(
                    imageUrl = customPrivacyImg,
                    videoUrl = customPrivacyVid
                )
                this["Matte"] = this["Matte"]!!.copy(enabled = false)
                this["Sapphire 9H"] = GlassTypeAdminConfig(
                    basePrice = 449.0,
                    enabled = true,
                    deleted = false,
                    helperNote = "Scratch-proof sapphire coating",
                    imageUrl = sapphireImg,
                    videoUrl = sapphireVid
                )
            },
            cameraLensDefault = GlassTypeAdminConfig(
                basePrice = 249.0,
                enabled = true,
                deleted = false,
                videoUrl = camLensVid
            ),
            pouchItems = listOf(
                PouchAdminItem(
                    id = "pouch_armor_media",
                    name = "MagSafe Armor Case",
                    basePrice = 399.0,
                    enabled = true,
                    deleted = false,
                    videoUrl = pouchItemVid
                )
            ),
            pouchCardImageUrl = pouchCardImg,
            pouchCardStartingPrice = 249.0
        )

        val reparsed = ScreenGuardAdminConfig.fromJsonString(cfg.toJsonString())
        assertEquals(cfg, reparsed)

        // 1. Pouches Home card image, starting price override & fallback, and Other Android WhatsApp message
        assertEquals(pouchCardImg, ScreenGuardCatalog.resolvePouchCardImageUrl(reparsed))
        assertEquals(
            ScreenGuardCatalog.POUCH_IMAGE_URL,
            ScreenGuardCatalog.resolvePouchCardImageUrl(reparsed.copy(pouchCardImageUrl = ""))
        )
        assertEquals(249.0, ScreenGuardCatalog.startingPouchPrice(reparsed), 0.01)
        assertEquals(
            399.0,
            ScreenGuardCatalog.startingPouchPrice(reparsed.copy(pouchCardStartingPrice = 0.0)),
            0.01
        )
        val otherAndroidMsg = ScreenGuardCatalog.buildOtherAndroidPouchWhatsAppMessage(
            model = "Google Pixel 9 Pro",
            pouchStyle = "MagSafe Clear Case"
        )
        assertTrue(otherAndroidMsg.contains("Google Pixel 9 Pro"))
        assertTrue(otherAndroidMsg.contains("MagSafe Clear Case"))

        // 2. Dynamic Glass Types sync: active default & new types appear; paused Matte stays hidden
        val activeTypes = ScreenGuardCatalog.activeGlassTypes(reparsed)
        assertTrue(activeTypes.contains("Privacy"))
        assertTrue(activeTypes.contains("Sapphire 9H"))
        assertFalse(activeTypes.contains("Matte"))

        // 3. Per-type image/video resolution & YouTube URL helper
        assertEquals(customPrivacyImg, ScreenGuardCatalog.resolveGlassTypeImageUrl("Privacy", reparsed))
        assertEquals(customPrivacyVid, ScreenGuardCatalog.resolveGlassTypeVideoUrl("Privacy", reparsed))
        assertEquals(sapphireImg, ScreenGuardCatalog.resolveGlassTypeImageUrl("Sapphire 9H", reparsed))
        assertEquals(sapphireVid, ScreenGuardCatalog.resolveGlassTypeVideoUrl("Sapphire 9H", reparsed))
        assertEquals(
            ScreenGuardCatalog.TEMPERED_GLASS_IMAGE_URL,
            ScreenGuardCatalog.resolveGlassTypeImageUrl("Normal 5D", reparsed)
        )
        assertTrue(ScreenGuardCatalog.isYouTubeUrl(sapphireVid))
        assertFalse(ScreenGuardCatalog.isYouTubeUrl(camLensVid))

        // 4. Default Code Export preserves all media and Pouches card settings
        val exported = ScreenGuardCatalog.generateDefaultCodeExport(InitialData.products, reparsed)
        val normalizedExport = exported.replace("\\/", "/")
        assertTrue(normalizedExport.contains(pouchCardImg))
        assertTrue(normalizedExport.contains(customPrivacyVid))
        assertTrue(normalizedExport.contains("Sapphire 9H"))
        assertTrue(normalizedExport.contains(camLensVid))
        assertTrue(normalizedExport.contains(pouchItemVid))
    }
}
