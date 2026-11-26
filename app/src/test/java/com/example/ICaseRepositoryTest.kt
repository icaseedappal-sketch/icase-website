package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.location.Address
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.DeviceCatalog
import com.example.data.InitialData
import com.example.data.JsonHelper
import com.example.data.LocationHelper
import com.example.data.ProductColorOption
import com.example.data.ProductEntity
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
class ICaseRepositoryTest {

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

        // Pass null for Firebase dependencies in local unit test
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
    fun ordersAndRepairs_areStrictlyIsolatedPerUser() = runBlocking {
        // Initially guest / blank profile sees zero orders and zero repairs
        assertTrue(repo.currentUserId.value.isBlank())
        assertTrue(repo.myOrders.first().isEmpty())
        assertTrue(repo.myRepairs.first().isEmpty())

        // User A logs in and places an order & repair
        repo.updateCustomerProfile("Alice", "9900112233", "9900112233", "Edappal North")
        val aliceUid = repo.currentUserId.value
        assertEquals("acct_9900112233", aliceUid)

        val prod = InitialData.products.first()
        repo.addToCart(prod, 1)
        val aliceOrder = repo.placeOrder(
            customerName = "Alice",
            customerPhone = "9900112233",
            whatsappNumber = "9900112233",
            deliveryAddress = "Edappal North",
            deliveryType = "STANDARD",
            deliveryFee = 50.0
        )
        val aliceRepair = repo.bookRepair(
            brandName = "Apple",
            modelName = "iPhone 15",
            complaints = listOf("Display"),
            description = "Cracked screen",
            photoUri = null,
            pickupAddress = "Edappal North",
            customerName = "Alice",
            phoneNumber = "9900112233",
            whatsappNumber = "9900112233",
            preferredDate = "Today",
            preferredTimeSlot = "10:00 AM - 12:00 PM"
        )

        assertEquals(aliceUid, aliceOrder.userId)
        assertEquals(aliceUid, aliceRepair.userId)
        assertEquals(listOf(aliceOrder.id), repo.myOrders.first().map { it.id })
        assertEquals(listOf(aliceRepair.id), repo.myRepairs.first().map { it.id })

        // User B signs in -> must NEVER see User A's orders or repairs
        repo.clearCustomerProfile()
        assertTrue(repo.myOrders.first().isEmpty())
        assertTrue(repo.myRepairs.first().isEmpty())

        repo.updateCustomerProfile("Bob", "8800112233", "8800112233", "Edappal South")
        val bobUid = repo.currentUserId.value
        assertEquals("acct_8800112233", bobUid)
        assertTrue("Bob must not see Alice's orders", repo.myOrders.first().isEmpty())
        assertTrue("Bob must not see Alice's repairs", repo.myRepairs.first().isEmpty())

        // Even if another user's order row exists in Room (e.g. cached before sign-out), myOrders filters strictly by userId
        db.appDao().insertOrder(aliceOrder)
        assertTrue("Room query must isolate Alice's order away from Bob", repo.myOrders.first().isEmpty())
    }

    @Test
    fun admin_hasFullControlOverProductsOrdersDeliveryAndSettings() = runBlocking {
        // Admin authenticates
        assertTrue(repo.verifyAdminPassword("2026"))
        assertTrue(repo.isAdminLoggedIn.value)

        // Add product
        val newProduct = ProductEntity(
            id = "prod_test_1",
            name = "MagSafe Test Case",
            category = "Cases",
            description = "Protective test case",
            price = 699.0,
            originalPrice = 999.0,
            imageUrl = "https://example.com/case.jpg",
            inStock = true,
            stockCount = 15,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 9
        )
        repo.addProduct(newProduct)
        assertTrue(repo.allProducts.first().any { it.id == "prod_test_1" })

        // Edit product stock & popularity
        repo.updateProduct(newProduct.copy(price = 599.0, inStock = false))
        val updatedProd = repo.allProducts.first().first { it.id == "prod_test_1" }
        assertEquals(599.0, updatedProd.price, 0.01)
        assertFalse(updatedProd.inStock)

        repo.setProductPopular("prod_test_1", true, 1)
        val popList = repo.popularProducts.first()
        assertTrue(popList.any { it.id == "prod_test_1" && it.popularityRank == 1 })

        // Delete product
        repo.deleteProduct("prod_test_1")
        assertFalse(repo.allProducts.first().any { it.id == "prod_test_1" })

        // Delivery charges & express toggle
        repo.updateDeliverySettings(
            standardFee = 75.0,
            expressFee = 120.0,
            freeThreshold = 999.0,
            oneHourEnabled = false
        )
        assertEquals(75.0, repo.standardDeliveryFee.first(), 0.01)
        assertEquals(120.0, repo.expressDeliveryFee.first(), 0.01)
        assertEquals(999.0, repo.freeDeliveryThreshold.first(), 0.01)
        assertFalse(repo.isOneHourDeliveryGloballyEnabled.first())

        // Store & customer support settings
        repo.updateStoreSettings(
            name = "iCASE Premium Hub",
            phone = "9961454059",
            whatsapp = "9961454059",
            email = "icaseedappal@gmail.com",
            address = "Edappal Town Square",
            hours = "10:00 AM - 9:00 PM",
            online = false
        )
        assertEquals("iCASE Premium Hub", repo.storeName.first())
        assertEquals("Edappal Town Square", repo.storeAddress.first())
        assertEquals("10:00 AM - 9:00 PM", repo.storeHours.first())
        assertFalse(repo.isStoreOnline.first())

        // Order status update & delete
        repo.updateCustomerProfile("Test Customer", "9123456789", "9123456789", "Edappal")
        repo.addToCart(InitialData.products.first(), 1)
        val order = repo.placeOrder("Test Customer", "9123456789", "9123456789", "Edappal", "STANDARD", 75.0)
        repo.updateOrderStatus(order.id, "Processing", "Packed at hub")
        assertEquals("Processing", repo.allOrders.first().first { it.id == order.id }.status)
        repo.deleteOrder(order.id)
        assertFalse(repo.allOrders.first().any { it.id == order.id })
    }

    @Test
    fun productColors_serializeParseAndAdminCrudReflectLive() = runBlocking {
        // Color hex normalization & JSON serialization/parsing
        assertEquals("#E50914", JsonHelper.normalizeHexColor("e50914"))
        assertEquals("#1E293B", JsonHelper.normalizeHexColor("invalid"))

        val initialColors = listOf(
            ProductColorOption("Midnight Black", "#1C1C1E"),
            ProductColorOption("Ocean Blue", "#0066CC")
        )
        val encoded = JsonHelper.serializeColorOptions(initialColors)
        val decoded = JsonHelper.parseColorOptions(encoded)
        assertEquals(initialColors, decoded)

        // Admin adds product with color variants, then edits & removes a variant
        val prod = ProductEntity(
            id = "prod_color_test",
            name = "Color Case",
            category = "Cases",
            description = "Dynamic color test case",
            price = 499.0,
            originalPrice = 699.0,
            imageUrl = "https://example.com/color.jpg",
            inStock = true,
            stockCount = 20,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 15,
            colorOptionsJson = encoded
        )
        repo.addProduct(prod)
        val stored = repo.allProducts.first().first { it.id == "prod_color_test" }
        assertEquals(2, JsonHelper.parseColorOptions(stored.colorOptionsJson).size)

        // Admin edits variant list (edits Ocean Blue -> Sky Blue, deletes Midnight Black, adds Red)
        val updatedColors = listOf(
            ProductColorOption("Sky Blue", "#0284C7"),
            ProductColorOption("Crimson Red", "#E50914")
        )
        repo.updateProduct(stored.copy(colorOptionsJson = JsonHelper.serializeColorOptions(updatedColors)))
        val updated = repo.allProducts.first().first { it.id == "prod_color_test" }
        assertEquals(updatedColors, JsonHelper.parseColorOptions(updated.colorOptionsJson))
    }

    @Test
    fun freshInstall_hasBlankProfile_andWishlistPersistsAndClears() {
        val initial = repo.customerProfile.value
        assertFalse(initial.isLoggedIn)
        assertEquals("", initial.name)
        assertEquals("", initial.phone)
        assertEquals("", initial.address)
        assertTrue(initial.wishlistIds.isEmpty())

        // Address update without login stores address without fabricating Guest Customer
        repo.updateCustomerAddress("Beach Road, Kozhikode, Kerala")
        assertEquals("Beach Road, Kozhikode, Kerala", repo.customerProfile.value.address)
        assertEquals("", repo.customerProfile.value.name)
        assertFalse(repo.customerProfile.value.isLoggedIn)

        // Toggle wishlist items on and off
        assertTrue(repo.toggleWishlist("prod_1"))
        assertTrue(repo.toggleWishlist("prod_3"))
        assertEquals(listOf("prod_1", "prod_3"), repo.customerProfile.value.wishlistIds)

        assertFalse(repo.toggleWishlist("prod_1"))
        assertEquals(listOf("prod_3"), repo.customerProfile.value.wishlistIds)

        // Logout resets everything cleanly
        repo.clearCustomerProfile()
        assertTrue(repo.customerProfile.value.wishlistIds.isEmpty())
        assertEquals("", repo.customerProfile.value.address)
    }

    @Test
    fun deviceCatalog_containsAllEightMajorBrandsAndFilters() {
        val expectedBrands = listOf("Apple", "Samsung", "OnePlus", "Xiaomi", "Realme", "Vivo", "Oppo", "Motorola")
        assertEquals(expectedBrands, DeviceCatalog.majorBrands)
        expectedBrands.forEach { brand ->
            assertTrue("Brand $brand should have models", DeviceCatalog.modelsForBrand(brand).isNotEmpty())
        }
        val searchHits = DeviceCatalog.searchModels("Samsung", "S24")
        assertTrue(searchHits.any { it.brand == "Samsung" && it.model.contains("S24") })
        val crossBrandHits = DeviceCatalog.searchModels("", "Nord")
        assertTrue(crossBrandHits.any { it.brand == "OnePlus" })
    }

    @Test
    fun locationHelper_usesActualGeocodedCityWithoutDefaultingToEdappal() {
        val addr = Address(Locale.getDefault()).apply {
            setAddressLine(0, "Mavoor Road, Kozhikode, Kerala 673004, India")
            locality = "Kozhikode"
            adminArea = "Kerala"
            postalCode = "673004"
        }
        val detected = LocationHelper.formatAddressResult(11.25875, 75.78041, addr)
        assertEquals("Kozhikode", detected.locality)
        assertTrue(detected.formattedAddress.contains("Kozhikode"))
        assertFalse(detected.formattedAddress.contains("Edappal"))
    }

    @Test
    fun productVideoUrl_persistsInRoomAndExportsCleanly() = runBlocking {
        val prodWithVideo = ProductEntity(
            id = "prod_vid_test",
            name = "Clear MagSafe Demo Case",
            category = "Cases",
            description = "Case with optional demo video",
            price = 599.0,
            originalPrice = 799.0,
            imageUrl = "https://example.com/pouch.jpg",
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            inStock = true,
            stockCount = 12,
            isOneHourEligible = true,
            isPopular = true,
            popularityRank = 1
        )
        repo.addProduct(prodWithVideo)
        val saved = repo.allProducts.first().first { it.id == "prod_vid_test" }
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", saved.videoUrl)

        val exportWithVideo = com.example.data.ScreenGuardCatalog.generateDefaultCodeExport(
            products = listOf(saved),
            config = com.example.data.ScreenGuardAdminConfig()
        )
        assertTrue(exportWithVideo.contains("dQw4w9WgXcQ"))

        repo.updateProduct(saved.copy(videoUrl = ""))
        val cleared = repo.allProducts.first().first { it.id == "prod_vid_test" }
        assertEquals("", cleared.videoUrl)
    }
}
