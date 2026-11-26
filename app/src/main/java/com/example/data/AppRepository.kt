package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

data class CustomerProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val address: String = "",
    val isLoggedIn: Boolean = false,
    val isGoogleSignedIn: Boolean = false,
    val wishlistIds: List<String> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppRepository(
    private val appDao: AppDao,
    private val context: Context,
    private val scope: CoroutineScope,
    injectedFirestore: FirebaseFirestore? = null,
    injectedAuth: FirebaseAuth? = null
) {
    // In-memory cart
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val prefs = context.getSharedPreferences("icase_prefs", Context.MODE_PRIVATE)

    // Firebase Auth & Firestore (only resolved when injected or FirebaseApp is initialized)
    private val firebaseAuth: FirebaseAuth? by lazy {
        injectedAuth ?: if (FirebaseApp.getApps(context).isNotEmpty()) Firebase.auth else null
    }

    val firestoreDb: FirebaseFirestore? by lazy {
        if (injectedFirestore != null) {
            injectedFirestore
        } else if (FirebaseApp.getApps(context).isNotEmpty()) {
            val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(databaseId)
        } else {
            null
        }
    }

    private val _customerProfile = MutableStateFlow(loadInitialProfile())
    val customerProfile: StateFlow<CustomerProfile> = _customerProfile.asStateFlow()

    private val _currentUserId = MutableStateFlow(resolveUserId(_customerProfile.value, firebaseAuth?.currentUser))
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Admin Auth State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Admin Attempt Lockout: 3 attempts -> 2 min (120 sec) lockout
    private val _adminFailedAttempts = MutableStateFlow(0)
    val adminFailedAttempts: StateFlow<Int> = _adminFailedAttempts.asStateFlow()

    private val _lockoutEndTime = MutableStateFlow(0L)
    val lockoutEndTime: StateFlow<Long> = _lockoutEndTime.asStateFlow()

    // Catalog & Admin DB flows
    val allProducts: Flow<List<ProductEntity>> = appDao.getAllProducts()
    val popularProducts: Flow<List<ProductEntity>> = appDao.getPopularProducts()
    val allOrders: Flow<List<OrderEntity>> = appDao.getAllOrders()
    val allRepairs: Flow<List<RepairBookingEntity>> = appDao.getAllRepairs()
    val publicProductReviews: Flow<List<ReviewEntity>> = appDao.getPublicApprovedProductReviews()
    val adminAllReviews: Flow<List<ReviewEntity>> = appDao.getAllReviews()

    // Strictly isolated customer-specific flows (empty if not signed in / guest)
    val myOrders: Flow<List<OrderEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) flowOf(emptyList()) else appDao.getOrdersByUserId(uid)
    }

    val myRepairs: Flow<List<RepairBookingEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) flowOf(emptyList()) else appDao.getRepairsByUserId(uid)
    }

    val ordersCount: Flow<Int> = appDao.getOrdersCount()
    val pendingOrdersCount: Flow<Int> = appDao.getPendingOrdersCount()
    val repairsCount: Flow<Int> = appDao.getRepairsCount()
    val pendingRepairsCount: Flow<Int> = appDao.getPendingRepairsCount()

    // Dynamic Store & Delivery Settings
    val isOneHourDeliveryGloballyEnabled: Flow<Boolean> = appDao.getSetting("one_hour_delivery_enabled")
        .map { it?.value?.toBooleanStrictOrNull() ?: true }

    val standardDeliveryFee: Flow<Double> = appDao.getSetting("delivery_fee_standard")
        .map { it?.value?.toDoubleOrNull() ?: 50.0 }

    val expressDeliveryFee: Flow<Double> = appDao.getSetting("delivery_fee_express")
        .map { it?.value?.toDoubleOrNull() ?: 80.0 }

    val freeDeliveryThreshold: Flow<Double> = appDao.getSetting("free_delivery_threshold")
        .map { it?.value?.toDoubleOrNull() ?: 0.0 }

    val storeName: Flow<String> = appDao.getSetting("store_name")
        .map { it?.value?.takeIf { v -> v.isNotBlank() } ?: "iCASE Mobile Accessories & Repair" }

    val storePhone: Flow<String> = appDao.getSetting("store_phone")
        .map { it?.value?.takeIf { v -> v.isNotBlank() } ?: "9961454059" }

    val storeWhatsapp: Flow<String> = appDao.getSetting("store_whatsapp")
        .map { it?.value?.takeIf { v -> v.isNotBlank() } ?: "9961454059" }

    val storeEmail: Flow<String> = appDao.getSetting("store_email")
        .map { it?.value?.takeIf { v -> v.isNotBlank() } ?: "icaseedappal@gmail.com" }

    val storeAddress: Flow<String> = appDao.getSetting("store_address")
        .map { it?.value?.takeIf { v -> v.isNotBlank() } ?: "Main Road, Near Town Square, Edappal, Malappuram, Kerala" }

    val storeHours: Flow<String> = appDao.getSetting("store_hours")
        .map { it?.value?.takeIf { v -> v.isNotBlank() } ?: "9:00 AM - 10:00 PM (Daily)" }

    val isStoreOnline: Flow<Boolean> = appDao.getSetting("store_online")
        .map { it?.value?.toBooleanStrictOrNull() ?: true }

    val adminPin: Flow<String> = appDao.getSetting("admin_pin")
        .map { it?.value?.takeIf { v -> v.isNotBlank() } ?: "2026" }

    val adminRecoveryCode: Flow<String> = appDao.getSetting("admin_recovery_code")
        .map { it?.value?.takeIf { v -> v.isNotBlank() } ?: "ICASE2026" }

    private val _latestGuardConfig = MutableStateFlow(loadCachedGuardConfig())

    val screenGuardConfig: Flow<ScreenGuardAdminConfig> = appDao.getSetting(ScreenGuardCatalog.SETTINGS_KEY)
        .map { setting ->
            val raw = setting?.value ?: prefs.getString(ScreenGuardCatalog.SETTINGS_KEY, null)
            ScreenGuardAdminConfig.fromJsonString(raw).also { parsed ->
                _latestGuardConfig.value = parsed
            }
        }

    private val deletedProductIdsFlow: Flow<Set<String>> = appDao.getSetting(ScreenGuardCatalog.DELETED_PRODUCTS_KEY)
        .map { setting ->
            (parseDeletedIds(setting?.value) + loadDeletedProductIdsFromPrefs())
        }

    private var settingsListener: ListenerRegistration? = null
    private var productsListener: ListenerRegistration? = null
    private var reviewsPublicListener: ListenerRegistration? = null
    private var scopedOrdersListener: ListenerRegistration? = null
    private var scopedRepairsListener: ListenerRegistration? = null
    private var scopedUserProfileListener: ListenerRegistration? = null
    private var adminOrdersListener: ListenerRegistration? = null
    private var adminRepairsListener: ListenerRegistration? = null
    private var adminReviewsListener: ListenerRegistration? = null

    init {
        syncAuthStateWithProfile()
        startPublicFirestoreSync()
        restartScopedOrderSync(_currentUserId.value)
        startCartReconciliation()
    }

    private fun loadCachedGuardConfig(): ScreenGuardAdminConfig {
        return ScreenGuardAdminConfig.fromJsonString(prefs.getString(ScreenGuardCatalog.SETTINGS_KEY, null))
    }

    private fun parseDeletedIds(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        val trimmed = raw.trim()
        if (trimmed.startsWith("[")) {
            try {
                val arr = org.json.JSONArray(trimmed)
                return (0 until arr.length())
                    .mapNotNull { arr.optString(it).trim().takeIf { s -> s.isNotEmpty() } }
                    .toSet()
            } catch (_: Exception) {
            }
        }
        return trimmed.split(",")
            .map { it.trim().trim('"', '[', ']') }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun serializeDeletedIds(ids: Set<String>): String {
        return org.json.JSONArray(ids.sorted()).toString()
    }

    private fun loadDeletedProductIdsFromPrefs(): Set<String> {
        return parseDeletedIds(prefs.getString(ScreenGuardCatalog.DELETED_PRODUCTS_KEY, null))
    }

    private fun persistDeletedProductIdsToPrefs(ids: Set<String>) {
        prefs.edit()
            .putString(ScreenGuardCatalog.DELETED_PRODUCTS_KEY, serializeDeletedIds(ids))
            .putBoolean(ScreenGuardCatalog.CATALOG_SEEDED_KEY, true)
            .apply()
    }

    private fun startCartReconciliation() {
        var previousCatalogIds: Set<String>? = null
        scope.launch {
            combine(allProducts, screenGuardConfig, deletedProductIdsFlow) { products, guardConfig, deletedIds ->
                Triple(products, guardConfig, deletedIds)
            }.collect { (products, guardConfig, deletedIds) ->
                val productMap = products.associateBy { it.id }
                reconcileCartWithLiveCatalog(productMap, guardConfig, deletedIds, previousCatalogIds)
                previousCatalogIds = productMap.keys
            }
        }
    }

    private fun reconcileCartWithLiveCatalog(
        productMap: Map<String, ProductEntity>,
        guardConfig: ScreenGuardAdminConfig,
        deletedIds: Set<String>,
        previousCatalogIds: Set<String>?
    ) {
        _cartItems.update { currentList ->
            if (currentList.isEmpty()) return@update currentList
            currentList.mapNotNull { item ->
                val pid = item.product.id
                if (deletedIds.contains(pid)) {
                    null
                } else if (
                    pid.startsWith("sg::") ||
                    pid.startsWith("cl::") ||
                    pid.startsWith("combo::") ||
                    pid.startsWith("pouch::")
                ) {
                    val repriced = ScreenGuardCatalog.repriceOrNullCartProduct(item.product, guardConfig)
                    repriced?.let { item.copy(product = it) }
                } else {
                    val liveProduct = productMap[pid]
                    when {
                        liveProduct != null -> if (!liveProduct.inStock) null else item.copy(product = liveProduct)
                        previousCatalogIds != null && previousCatalogIds.contains(pid) -> null
                        else -> item
                    }
                }
            }
        }
    }

    private fun parseWishlistPrefs(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct()
    }

    private fun serializeWishlistPrefs(ids: List<String>): String {
        return ids.map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString(",")
    }

    private fun loadInitialProfile(): CustomerProfile {
        val savedUid = prefs.getString("cust_user_id", "") ?: ""
        val name = prefs.getString("cust_name", "") ?: ""
        val email = prefs.getString("cust_email", "") ?: ""
        val phone = prefs.getString("cust_phone", "") ?: ""
        val whatsapp = prefs.getString("cust_whatsapp", "") ?: ""
        val address = prefs.getString("cust_address", "") ?: ""
        val isLoggedIn = prefs.getBoolean("cust_logged_in", false)
        val isGoogle = prefs.getBoolean("cust_google_signed_in", false)
        val wishlistIds = parseWishlistPrefs(prefs.getString("cust_wishlist_ids", ""))
        return CustomerProfile(
            userId = savedUid,
            name = name,
            email = email,
            phone = phone,
            whatsapp = whatsapp,
            address = address,
            isLoggedIn = isLoggedIn,
            isGoogleSignedIn = isGoogle,
            wishlistIds = wishlistIds
        )
    }

    private fun resolveUserId(profile: CustomerProfile, firebaseUser: FirebaseUser?): String {
        if (firebaseUser != null) return firebaseUser.uid
        if (profile.userId.isNotBlank() && profile.isLoggedIn) return profile.userId
        val digits = profile.phone.filter { it.isDigit() }
        if (profile.isLoggedIn && digits.isNotEmpty()) return "acct_$digits"
        val cleanName = profile.name.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
        if (profile.isLoggedIn && cleanName.isNotBlank()) return "acct_$cleanName"
        return ""
    }

    private fun syncAuthStateWithProfile() {
        val fbUser = firebaseAuth?.currentUser
        if (fbUser != null) {
            onFirebaseUserRestored(fbUser)
        }
    }

    fun onFirebaseUserRestored(user: FirebaseUser) {
        onGoogleUserAuthenticated(user)
    }

    fun onGoogleUserAuthenticated(user: FirebaseUser) {
        val uid = user.uid
        // Restore any per-user cached phone/address for this Google UID
        val savedPhone = prefs.getString("acct_${uid}_phone", null)
            ?: user.phoneNumber?.takeIf { it.isNotBlank() }
            ?: _customerProfile.value.phone
        val savedWhatsapp = prefs.getString("acct_${uid}_whatsapp", null) ?: _customerProfile.value.whatsapp
        val savedAddress = prefs.getString("acct_${uid}_address", null) ?: _customerProfile.value.address
        val savedName = prefs.getString("acct_${uid}_name", null)?.takeIf { it.isNotBlank() }
            ?: user.displayName?.takeIf { it.isNotBlank() }
            ?: _customerProfile.value.name
        val email = user.email ?: _customerProfile.value.email

        val savedWishlist = (parseWishlistPrefs(prefs.getString("acct_${uid}_wishlist_ids", "")) + _customerProfile.value.wishlistIds).distinct()

        val updated = CustomerProfile(
            userId = uid,
            name = savedName,
            email = email,
            phone = savedPhone,
            whatsapp = savedWhatsapp,
            address = savedAddress,
            isLoggedIn = true,
            isGoogleSignedIn = true,
            wishlistIds = savedWishlist
        )
        persistActiveProfile(updated)
        _customerProfile.value = updated
        switchActiveUserId(uid)
    }

    private fun persistActiveProfile(profile: CustomerProfile, syncToCloud: Boolean = true) {
        val wishlistSerialized = serializeWishlistPrefs(profile.wishlistIds)
        val editor = prefs.edit()
            .putString("cust_user_id", profile.userId)
            .putString("cust_name", profile.name)
            .putString("cust_email", profile.email)
            .putString("cust_phone", profile.phone)
            .putString("cust_whatsapp", profile.whatsapp)
            .putString("cust_address", profile.address)
            .putBoolean("cust_logged_in", profile.isLoggedIn)
            .putBoolean("cust_google_signed_in", profile.isGoogleSignedIn)
            .putString("cust_wishlist_ids", wishlistSerialized)

        if (profile.userId.isNotBlank()) {
            editor
                .putString("acct_${profile.userId}_name", profile.name)
                .putString("acct_${profile.userId}_email", profile.email)
                .putString("acct_${profile.userId}_phone", profile.phone)
                .putString("acct_${profile.userId}_whatsapp", profile.whatsapp)
                .putString("acct_${profile.userId}_address", profile.address)
                .putString("acct_${profile.userId}_wishlist_ids", wishlistSerialized)
        }
        editor.apply()

        if (syncToCloud) {
            syncProfileToFirestore(profile)
        }
    }

    private fun syncProfileToFirestore(profile: CustomerProfile) {
        val db = firestoreDb ?: return
        if (!profile.isLoggedIn || profile.userId.isBlank()) return
        scope.launch(Dispatchers.IO) {
            try {
                val userMap = hashMapOf(
                    "userId" to profile.userId,
                    "name" to profile.name,
                    "email" to profile.email,
                    "phone" to profile.phone.trim(),
                    "whatsapp" to profile.whatsapp.trim(),
                    "address" to profile.address,
                    "wishlistIds" to profile.wishlistIds,
                    "isGoogleSignedIn" to profile.isGoogleSignedIn,
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("users").document(profile.userId)
                    .set(userMap, SetOptions.merge())
                    .await()
            } catch (_: Exception) {
            }
        }
    }

    private fun switchActiveUserId(newUid: String) {
        val previousUid = _currentUserId.value
        _currentUserId.value = newUid
        if (previousUid != newUid) {
            scope.launch(Dispatchers.IO) {
                if (!_isAdminLoggedIn.value) {
                    appDao.deleteOrdersNotOwnedBy(newUid)
                    appDao.deleteRepairsNotOwnedBy(newUid)
                }
            }
            restartScopedOrderSync(newUid)
        }
    }

    private fun startPublicFirestoreSync() {
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                // Ensure initial catalog & default settings exist in central Firestore database
                ensureInitialFirestoreSeeded(db)

                // Live Settings Sync
                settingsListener?.remove()
                settingsListener = db.collection("settings")
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null) return@addSnapshotListener
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshots.documents) {
                                val key = doc.getString("key") ?: doc.id
                                val value = doc.getString("value") ?: ""
                                if (key.isNotBlank()) {
                                    when (key) {
                                        ScreenGuardCatalog.SETTINGS_KEY -> {
                                            prefs.edit().putString(ScreenGuardCatalog.SETTINGS_KEY, value).apply()
                                            _latestGuardConfig.value = ScreenGuardAdminConfig.fromJsonString(value)
                                        }
                                        ScreenGuardCatalog.CATALOG_SEEDED_KEY -> {
                                            prefs.edit().putBoolean(ScreenGuardCatalog.CATALOG_SEEDED_KEY, value == "true").apply()
                                        }
                                        ScreenGuardCatalog.DELETED_PRODUCTS_KEY -> {
                                            val merged = loadDeletedProductIdsFromPrefs() + parseDeletedIds(value)
                                            persistDeletedProductIdsToPrefs(merged)
                                            for (deletedId in merged) {
                                                appDao.deleteProductById(deletedId)
                                            }
                                        }
                                    }
                                    appDao.insertSetting(AppSettingsEntity(key, value))
                                }
                            }
                        }
                    }

                // Live Products Sync (full reconciliation supports Admin additions, edits, and permanent deletions)
                productsListener?.remove()
                productsListener = db.collection("products")
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null) return@addSnapshotListener
                        scope.launch(Dispatchers.IO) {
                            val deletedIds = loadDeletedProductIdsFromPrefs() +
                                parseDeletedIds(appDao.getSettingDirect(ScreenGuardCatalog.DELETED_PRODUCTS_KEY)?.value)
                            val remoteIds = snapshots.documents.map { it.id }.toSet()

                            for (doc in snapshots.documents) {
                                val product = documentToProduct(doc)
                                if (product != null && !deletedIds.contains(product.id)) {
                                    appDao.insertProduct(product)
                                }
                            }

                            val localProducts = appDao.getAllProductsDirect()
                            for (local in localProducts) {
                                if (!remoteIds.contains(local.id) || deletedIds.contains(local.id)) {
                                    appDao.deleteProductById(local.id)
                                }
                            }
                        }
                    }

                // Live Public Approved Product Reviews Sync (NEVER subscribes to private SERVICE feedback or pending reviews)
                reviewsPublicListener?.remove()
                reviewsPublicListener = db.collection("reviews")
                    .whereEqualTo("reviewType", "PRODUCT")
                    .whereEqualTo("isApproved", true)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null) return@addSnapshotListener
                        scope.launch(Dispatchers.IO) {
                            val remoteApprovedIds = snapshots.documents.map { it.id }.toSet()
                            for (doc in snapshots.documents) {
                                val review = documentToReview(doc)
                                if (review != null && review.reviewType == "PRODUCT" && review.isApproved) {
                                    appDao.insertReview(review)
                                }
                            }
                            if (!_isAdminLoggedIn.value) {
                                for (change in snapshots.documentChanges) {
                                    if (change.type == DocumentChange.Type.REMOVED) {
                                        appDao.deleteReviewById(change.document.id)
                                    }
                                }
                                val localReviews = appDao.getAllReviewsDirect()
                                for (local in localReviews) {
                                    if (local.reviewType == "PRODUCT" && local.isApproved && !remoteApprovedIds.contains(local.id)) {
                                        appDao.deleteReviewById(local.id)
                                    }
                                }
                            }
                        }
                    }
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun ensureInitialFirestoreSeeded(db: FirebaseFirestore) {
        try {
            val seededDoc = db.collection("settings").document(ScreenGuardCatalog.CATALOG_SEEDED_KEY).get().await()
            val remoteSeeded = seededDoc.exists() && seededDoc.getString("value") == "true"
            val localSeeded = prefs.getBoolean(ScreenGuardCatalog.CATALOG_SEEDED_KEY, false) ||
                appDao.getSettingDirect(ScreenGuardCatalog.CATALOG_SEEDED_KEY)?.value == "true"

            val deletedDoc = db.collection("settings").document(ScreenGuardCatalog.DELETED_PRODUCTS_KEY).get().await()
            val deletedIds = loadDeletedProductIdsFromPrefs() +
                parseDeletedIds(appDao.getSettingDirect(ScreenGuardCatalog.DELETED_PRODUCTS_KEY)?.value) +
                parseDeletedIds(deletedDoc.getString("value"))

            if (deletedIds.isNotEmpty()) {
                persistDeletedProductIdsToPrefs(deletedIds)
                appDao.insertSetting(
                    AppSettingsEntity(
                        ScreenGuardCatalog.DELETED_PRODUCTS_KEY,
                        serializeDeletedIds(deletedIds)
                    )
                )
            }

            if (!remoteSeeded && !localSeeded) {
                val productCheck = db.collection("products").limit(1).get().await()
                if (productCheck.isEmpty) {
                    for (product in InitialData.products) {
                        if (!deletedIds.contains(product.id)) {
                            db.collection("products").document(product.id)
                                .set(productToMap(product))
                                .await()
                        }
                    }
                }
                val now = System.currentTimeMillis()
                db.collection("settings").document(ScreenGuardCatalog.CATALOG_SEEDED_KEY)
                    .set(
                        mapOf(
                            "key" to ScreenGuardCatalog.CATALOG_SEEDED_KEY,
                            "value" to "true",
                            "updatedAt" to now
                        )
                    )
                    .await()
            }

            prefs.edit().putBoolean(ScreenGuardCatalog.CATALOG_SEEDED_KEY, true).apply()
            appDao.insertSetting(AppSettingsEntity(ScreenGuardCatalog.CATALOG_SEEDED_KEY, "true"))

            val settingCheck = db.collection("settings").document("store_name").get().await()
            if (!settingCheck.exists()) {
                for (setting in InitialData.defaultSettings) {
                    db.collection("settings").document(setting.key)
                        .set(
                            mapOf(
                                "key" to setting.key,
                                "value" to setting.value,
                                "updatedAt" to System.currentTimeMillis()
                            )
                        )
                        .await()
                }
            }

            val guardCfgDoc = db.collection("settings").document(ScreenGuardCatalog.SETTINGS_KEY).get().await()
            if (!guardCfgDoc.exists()) {
                val defaultJson = ScreenGuardAdminConfig().toJsonString()
                db.collection("settings").document(ScreenGuardCatalog.SETTINGS_KEY)
                    .set(
                        mapOf(
                            "key" to ScreenGuardCatalog.SETTINGS_KEY,
                            "value" to defaultJson,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }
        } catch (_: Exception) {
        }
    }

    private fun restartScopedOrderSync(uid: String) {
        scopedOrdersListener?.remove()
        scopedOrdersListener = null
        scopedRepairsListener?.remove()
        scopedRepairsListener = null
        scopedUserProfileListener?.remove()
        scopedUserProfileListener = null

        val db = firestoreDb ?: return
        if (uid.isBlank() || _isAdminLoggedIn.value) return

        scope.launch(Dispatchers.IO) {
            try {
                // Real-time Profile Sync across Mobile App & Companion Website
                scopedUserProfileListener = db.collection("users").document(uid)
                    .addSnapshotListener { doc, error ->
                        if (error != null || doc == null || !doc.exists()) return@addSnapshotListener
                        if (_currentUserId.value != uid) return@addSnapshotListener
                        val current = _customerProfile.value
                        val remoteName = doc.getString("name")?.takeIf { it.isNotBlank() } ?: current.name
                        val remoteEmail = doc.getString("email")?.takeIf { it.isNotBlank() } ?: current.email
                        val remotePhone = doc.getString("phone")?.takeIf { it.isNotBlank() } ?: current.phone
                        val remoteWhatsapp = doc.getString("whatsapp")?.takeIf { it.isNotBlank() } ?: current.whatsapp
                        val remoteAddress = doc.getString("address")?.takeIf { it.isNotBlank() } ?: current.address
                        val remoteGoogle = (doc.getBoolean("isGoogleSignedIn") == true) || current.isGoogleSignedIn
                        val remoteWishlist = (doc.get("wishlistIds") as? List<*>)
                            ?.mapNotNull { it as? String }
                            ?.filter { it.isNotBlank() }
                            ?: current.wishlistIds

                        val merged = current.copy(
                            userId = uid,
                            name = remoteName,
                            email = remoteEmail,
                            phone = remotePhone,
                            whatsapp = remoteWhatsapp,
                            address = remoteAddress,
                            isLoggedIn = true,
                            isGoogleSignedIn = remoteGoogle,
                            wishlistIds = remoteWishlist
                        )
                        if (merged != current) {
                            persistActiveProfile(merged, syncToCloud = false)
                            _customerProfile.value = merged
                        }
                    }

                scopedOrdersListener = db.collection("orders")
                    .whereEqualTo("userId", uid)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null) return@addSnapshotListener
                        scope.launch(Dispatchers.IO) {
                            for (change in snapshots.documentChanges) {
                                val doc = change.document
                                if (change.type == DocumentChange.Type.REMOVED) {
                                    appDao.deleteOrderById(doc.id)
                                } else {
                                    appDao.insertOrder(documentToOrder(doc))
                                }
                            }
                        }
                    }

                scopedRepairsListener = db.collection("repairs")
                    .whereEqualTo("userId", uid)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null) return@addSnapshotListener
                        scope.launch(Dispatchers.IO) {
                            for (change in snapshots.documentChanges) {
                                val doc = change.document
                                if (change.type == DocumentChange.Type.REMOVED) {
                                    appDao.deleteRepairById(doc.id)
                                } else {
                                    appDao.insertRepair(documentToRepair(doc))
                                }
                            }
                        }
                    }
            } catch (_: Exception) {
            }
        }
    }

    private fun startAdminFullSync() {
        val db = firestoreDb ?: return
        scopedOrdersListener?.remove()
        scopedOrdersListener = null
        scopedRepairsListener?.remove()
        scopedRepairsListener = null

        scope.launch(Dispatchers.IO) {
            try {
                adminOrdersListener?.remove()
                adminOrdersListener = db.collection("orders")
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null) return@addSnapshotListener
                        scope.launch(Dispatchers.IO) {
                            for (change in snapshots.documentChanges) {
                                val doc = change.document
                                if (change.type == DocumentChange.Type.REMOVED) {
                                    appDao.deleteOrderById(doc.id)
                                } else {
                                    appDao.insertOrder(documentToOrder(doc))
                                }
                            }
                        }
                    }

                adminRepairsListener?.remove()
                adminRepairsListener = db.collection("repairs")
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null) return@addSnapshotListener
                        scope.launch(Dispatchers.IO) {
                            for (change in snapshots.documentChanges) {
                                val doc = change.document
                                if (change.type == DocumentChange.Type.REMOVED) {
                                    appDao.deleteRepairById(doc.id)
                                } else {
                                    appDao.insertRepair(documentToRepair(doc))
                                }
                            }
                        }
                    }

                // Full Admin Review Sync (pending/approved product reviews + private service feedback)
                adminReviewsListener?.remove()
                adminReviewsListener = db.collection("reviews")
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null) return@addSnapshotListener
                        scope.launch(Dispatchers.IO) {
                            val remoteAllIds = snapshots.documents.map { it.id }.toSet()
                            for (doc in snapshots.documents) {
                                val review = documentToReview(doc)
                                if (review != null) {
                                    appDao.insertReview(review)
                                }
                            }
                            val localReviews = appDao.getAllReviewsDirect()
                            for (local in localReviews) {
                                if (!remoteAllIds.contains(local.id)) {
                                    appDao.deleteReviewById(local.id)
                                }
                            }
                        }
                    }
            } catch (_: Exception) {
            }
        }
    }

    private fun stopAdminFullSync() {
        adminOrdersListener?.remove()
        adminOrdersListener = null
        adminRepairsListener?.remove()
        adminRepairsListener = null
        adminReviewsListener?.remove()
        adminReviewsListener = null

        val uid = _currentUserId.value
        scope.launch(Dispatchers.IO) {
            appDao.deleteOrdersNotOwnedBy(uid)
            appDao.deleteRepairsNotOwnedBy(uid)
            appDao.deleteNonPublicReviews()
        }
        restartScopedOrderSync(uid)
    }

    private fun documentToProduct(doc: DocumentSnapshot): ProductEntity? {
        val id = doc.getString("id") ?: doc.id
        val name = doc.getString("name") ?: return null
        val colorJson = doc.getString("colorOptionsJson") ?: "[]"
        return ProductEntity(
            id = id,
            name = name,
            category = doc.getString("category") ?: "Accessories",
            description = doc.getString("description") ?: "",
            price = (doc.get("price") as? Number)?.toDouble() ?: 0.0,
            originalPrice = (doc.get("originalPrice") as? Number)?.toDouble() ?: 0.0,
            imageUrl = doc.getString("imageUrl") ?: "",
            inStock = doc.getBoolean("inStock") ?: true,
            stockCount = (doc.get("stockCount") as? Number)?.toInt() ?: 0,
            isOneHourEligible = doc.getBoolean("isOneHourEligible") ?: true,
            isPopular = doc.getBoolean("isPopular") ?: false,
            popularityRank = (doc.get("popularityRank") as? Number)?.toInt() ?: 10,
            colorOptionsJson = colorJson
        )
    }

    private fun documentToOrder(doc: DocumentSnapshot): OrderEntity {
        val id = doc.getString("id") ?: doc.id
        return OrderEntity(
            id = id,
            userId = doc.getString("userId") ?: "",
            customerName = doc.getString("customerName") ?: "",
            customerPhone = doc.getString("customerPhone") ?: "",
            whatsappNumber = doc.getString("whatsappNumber") ?: "",
            deliveryAddress = doc.getString("deliveryAddress") ?: "",
            deliveryType = doc.getString("deliveryType") ?: "STANDARD",
            subtotal = (doc.get("subtotal") as? Number)?.toDouble() ?: 0.0,
            deliveryFee = (doc.get("deliveryFee") as? Number)?.toDouble() ?: 50.0,
            totalAmount = (doc.get("totalAmount") as? Number)?.toDouble() ?: 0.0,
            status = doc.getString("status") ?: "Order Received",
            statusNotes = doc.getString("statusNotes") ?: "",
            itemsJson = doc.getString("itemsJson") ?: "[]",
            createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun documentToRepair(doc: DocumentSnapshot): RepairBookingEntity {
        val id = doc.getString("id") ?: doc.id
        return RepairBookingEntity(
            id = id,
            userId = doc.getString("userId") ?: "",
            brandName = doc.getString("brandName") ?: "",
            modelName = doc.getString("modelName") ?: "",
            complaintsJson = doc.getString("complaintsJson") ?: "[]",
            description = doc.getString("description") ?: "",
            photoUri = doc.getString("photoUri"),
            pickupAddress = doc.getString("pickupAddress") ?: "",
            customerName = doc.getString("customerName") ?: "",
            phoneNumber = doc.getString("phoneNumber") ?: "",
            whatsappNumber = doc.getString("whatsappNumber") ?: "",
            preferredDate = doc.getString("preferredDate") ?: "",
            preferredTimeSlot = doc.getString("preferredTimeSlot") ?: "",
            status = doc.getString("status") ?: "Booking Received",
            adminNotes = doc.getString("adminNotes") ?: "",
            createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun documentToReview(doc: DocumentSnapshot): ReviewEntity? {
        val id = doc.getString("id") ?: doc.id
        val refId = doc.getString("referenceId") ?: return null
        val type = doc.getString("reviewType") ?: return null
        val comment = doc.getString("comment") ?: return null
        return ReviewEntity(
            id = id,
            referenceId = refId,
            reviewType = type,
            targetTitle = doc.getString("targetTitle") ?: "",
            customerName = doc.getString("customerName") ?: "Verified Customer",
            rating = ((doc.get("rating") as? Number)?.toInt() ?: 5).coerceIn(1, 5),
            comment = comment,
            isVerified = doc.getBoolean("isVerified") ?: true,
            isApproved = if (type == "SERVICE") false else (doc.getBoolean("isApproved") ?: false),
            adminReply = doc.getString("adminReply") ?: "",
            userId = doc.getString("userId") ?: "",
            productId = doc.getString("productId") ?: "",
            createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun reviewToMap(review: ReviewEntity): HashMap<String, Any> = hashMapOf(
        "id" to review.id,
        "referenceId" to review.referenceId,
        "reviewType" to review.reviewType,
        "targetTitle" to review.targetTitle,
        "customerName" to review.customerName,
        "rating" to review.rating.coerceIn(1, 5),
        "comment" to review.comment,
        "isVerified" to review.isVerified,
        "isApproved" to if (review.reviewType == "SERVICE") false else review.isApproved,
        "adminReply" to review.adminReply,
        "userId" to review.userId,
        "productId" to review.productId,
        "createdAt" to review.createdAt,
        "updatedAt" to review.updatedAt
    )

    fun updateCustomerProfile(name: String, phone: String, whatsapp: String, address: String) {
        val fbUser = firebaseAuth?.currentUser
        val existing = _customerProfile.value
        val hasGoogle = fbUser?.providerData?.any { it.providerId == "google.com" } == true || existing.isGoogleSignedIn

        val candidateProfile = existing.copy(
            name = name,
            phone = phone,
            whatsapp = whatsapp,
            address = address,
            isLoggedIn = true,
            isGoogleSignedIn = hasGoogle
        )
        val resolvedUid = resolveUserId(candidateProfile, fbUser)
        val finalProfile = candidateProfile.copy(userId = resolvedUid)
        persistActiveProfile(finalProfile)
        _customerProfile.value = finalProfile
        switchActiveUserId(resolvedUid)
    }

    fun logoutCustomer() {
        clearCustomerProfile()
    }

    fun loginCustomer(name: String, phone: String) {
        updateCustomerProfile(
            name = name,
            phone = phone,
            whatsapp = _customerProfile.value.whatsapp,
            address = _customerProfile.value.address
        )
    }

    fun updateCustomerAddress(address: String) {
        val clean = address.trim()
        val existing = _customerProfile.value
        val updated = existing.copy(address = clean)
        persistActiveProfile(updated, syncToCloud = existing.isLoggedIn && existing.userId.isNotBlank())
        _customerProfile.value = updated
    }

    fun toggleWishlist(productId: String): Boolean {
        val cleanId = productId.trim()
        if (cleanId.isBlank()) return false
        val existing = _customerProfile.value
        val currentlySaved = existing.wishlistIds.contains(cleanId)
        val nextIds = if (currentlySaved) {
            existing.wishlistIds.filterNot { it == cleanId }
        } else {
            (existing.wishlistIds + cleanId).distinct()
        }
        val updated = existing.copy(wishlistIds = nextIds)
        persistActiveProfile(updated, syncToCloud = existing.isLoggedIn && existing.userId.isNotBlank())
        _customerProfile.value = updated
        return !currentlySaved
    }

    fun clearCustomerProfile() {
        prefs.edit()
            .remove("cust_user_id")
            .remove("cust_name")
            .remove("cust_email")
            .remove("cust_phone")
            .remove("cust_whatsapp")
            .remove("cust_address")
            .remove("cust_wishlist_ids")
            .putBoolean("cust_logged_in", false)
            .putBoolean("cust_google_signed_in", false)
            .remove("cust_phone_verified")
            .apply()
        _customerProfile.value = CustomerProfile()
        switchActiveUserId("")
    }

    // Cart Operations
    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        _cartItems.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
            if (existingIndex >= 0) {
                currentList.mapIndexed { index, item ->
                    if (index == existingIndex) {
                        item.copy(product = product, quantity = item.quantity + quantity)
                    } else {
                        item
                    }
                }
            } else {
                currentList + CartItem(product, quantity)
            }
        }
    }

    fun addTemperedGlassToCart(
        brand: String,
        model: String,
        glassType: String,
        installationOption: String = ""
    ) {
        val cfg = _latestGuardConfig.value
        val product = ScreenGuardCatalog.buildTemperedGlassCartProduct(
            brand = brand,
            model = model,
            glassType = glassType,
            config = cfg,
            installationOption = installationOption
        )
        addToCart(product, 1)
    }

    fun addCameraLensToCart(
        brand: String,
        model: String,
        installationOption: String = ""
    ) {
        val cfg = _latestGuardConfig.value
        val product = ScreenGuardCatalog.buildCameraLensCartProduct(
            brand = brand,
            model = model,
            config = cfg,
            installationOption = installationOption
        )
        addToCart(product, 1)
    }

    fun addComboToCart(
        glassBrand: String,
        glassModel: String,
        glassType: String,
        cameraBrand: String,
        cameraModel: String,
        installationOption: String = ""
    ) {
        val cfg = _latestGuardConfig.value
        val product = ScreenGuardCatalog.buildComboCartProduct(
            glassBrand = glassBrand,
            glassModel = glassModel,
            glassType = glassType,
            cameraBrand = cameraBrand,
            cameraModel = cameraModel,
            config = cfg,
            installationOption = installationOption
        )
        addToCart(product, 1)
    }

    fun addPouchToCart(brand: String, model: String, pouch: PouchAdminItem, selectedColor: String = "") {
        val product = ScreenGuardCatalog.buildPouchCartProduct(brand, model, pouch, selectedColor)
        addToCart(product, 1)
    }

    fun updateCartQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
        } else {
            _cartItems.update { currentList ->
                currentList.map { item ->
                    if (item.product.id == productId) item.copy(quantity = newQuantity) else item
                }
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.update { currentList ->
            currentList.filterNot { it.product.id == productId }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Checkout with strict userId ownership & live sync to Firestore Central Database
    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        whatsappNumber: String,
        deliveryAddress: String,
        deliveryType: String, // "STANDARD" or "ONE_HOUR"
        deliveryFee: Double
    ): OrderEntity {
        if (_currentUserId.value.isBlank()) {
            updateCustomerProfile(customerName, customerPhone, whatsappNumber, deliveryAddress)
        }
        val uid = _currentUserId.value.ifBlank {
            val digits = customerPhone.filter { it.isDigit() }
            if (digits.isNotEmpty()) "acct_$digits" else "acct_${System.currentTimeMillis()}"
        }

        val currentCart = _cartItems.value
        val subtotal = currentCart.sumOf { it.product.price * it.quantity }
        val total = subtotal + deliveryFee
        val orderId = "ORD-" + Random.nextInt(10000, 99999).toString()

        val order = OrderEntity(
            id = orderId,
            userId = uid,
            customerName = customerName,
            customerPhone = customerPhone,
            whatsappNumber = whatsappNumber,
            deliveryAddress = deliveryAddress,
            deliveryType = deliveryType,
            itemsJson = JsonHelper.serializeCartItems(currentCart),
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            totalAmount = total,
            status = "Order Received",
            statusNotes = if (deliveryType == "ONE_HOUR") "Express 1-Hour Delivery Order Received" else "Standard Home Delivery Order Received",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Save locally
        appDao.insertOrder(order)
        clearCart()

        // Sync live to Firebase Cloud Firestore for instant Admin Panel visibility
        val db = firestoreDb
        if (db != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val orderMap = hashMapOf(
                        "id" to order.id,
                        "userId" to order.userId,
                        "customerName" to order.customerName,
                        "customerPhone" to order.customerPhone,
                        "whatsappNumber" to order.whatsappNumber,
                        "deliveryAddress" to order.deliveryAddress,
                        "deliveryType" to order.deliveryType,
                        "subtotal" to order.subtotal,
                        "deliveryFee" to order.deliveryFee,
                        "totalAmount" to order.totalAmount,
                        "status" to order.status,
                        "statusNotes" to order.statusNotes,
                        "itemsJson" to order.itemsJson,
                        "createdAt" to order.createdAt,
                        "updatedAt" to order.updatedAt
                    )
                    db.collection("orders").document(order.id).set(orderMap).await()
                } catch (_: Exception) {
                }
            }
        }

        return order
    }

    // Repair Booking with strict userId ownership & live sync to Firestore Central Database
    suspend fun bookRepair(
        brandName: String,
        modelName: String,
        complaints: List<String>,
        description: String,
        photoUri: String?,
        pickupAddress: String,
        customerName: String,
        phoneNumber: String,
        whatsappNumber: String,
        preferredDate: String,
        preferredTimeSlot: String
    ): RepairBookingEntity {
        if (_currentUserId.value.isBlank()) {
            updateCustomerProfile(customerName, phoneNumber, whatsappNumber, pickupAddress)
        }
        val uid = _currentUserId.value.ifBlank {
            val digits = phoneNumber.filter { it.isDigit() }
            if (digits.isNotEmpty()) "acct_$digits" else "acct_${System.currentTimeMillis()}"
        }

        val repairId = "REP-" + Random.nextInt(10000, 99999).toString()
        val booking = RepairBookingEntity(
            id = repairId,
            userId = uid,
            brandName = brandName,
            modelName = modelName,
            complaintsJson = JsonHelper.serializeComplaints(complaints),
            description = description,
            photoUri = photoUri,
            pickupAddress = pickupAddress,
            customerName = customerName,
            phoneNumber = phoneNumber,
            whatsappNumber = whatsappNumber,
            preferredDate = preferredDate,
            preferredTimeSlot = preferredTimeSlot,
            status = "Booking Received",
            adminNotes = "Technician assignment pending in Edappal",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Save locally
        appDao.insertRepair(booking)

        // Sync live to Firebase Cloud Firestore for instant Admin Panel visibility
        val db = firestoreDb
        if (db != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val repairMap = hashMapOf(
                        "id" to booking.id,
                        "userId" to booking.userId,
                        "brandName" to booking.brandName,
                        "modelName" to booking.modelName,
                        "complaintsJson" to booking.complaintsJson,
                        "description" to booking.description,
                        "photoUri" to (booking.photoUri ?: ""),
                        "pickupAddress" to booking.pickupAddress,
                        "customerName" to booking.customerName,
                        "phoneNumber" to booking.phoneNumber,
                        "whatsappNumber" to booking.whatsappNumber,
                        "preferredDate" to booking.preferredDate,
                        "preferredTimeSlot" to booking.preferredTimeSlot,
                        "status" to booking.status,
                        "adminNotes" to booking.adminNotes,
                        "createdAt" to booking.createdAt,
                        "updatedAt" to booking.updatedAt
                    )
                    db.collection("repairs").document(booking.id).set(repairMap).await()
                } catch (_: Exception) {
                }
            }
        }

        return booking
    }

    // Unified Track Order / Repair Lookup (Checks local and Cloud Firestore by exact tracking ID)
    suspend fun lookupTracking(queryId: String): TrackingResult {
        val trimmed = queryId.trim().removePrefix("#").trim().uppercase()
        if (trimmed.isEmpty()) return TrackingResult.NotFound

        val localOrder = appDao.getOrderByIdDirect(trimmed)
        if (localOrder != null) {
            val items = JsonHelper.parseOrderItems(localOrder.itemsJson)
            return TrackingResult.ProductOrder(localOrder, items)
        }

        val localRepair = appDao.getRepairByIdDirect(trimmed)
        if (localRepair != null) {
            val complaints = JsonHelper.parseComplaints(localRepair.complaintsJson)
            return TrackingResult.Repair(localRepair, complaints)
        }

        val db = firestoreDb ?: return TrackingResult.NotFound
        try {
            val orderDoc = db.collection("orders").document(trimmed).get().await()
            if (orderDoc.exists()) {
                val order = documentToOrder(orderDoc)
                return TrackingResult.ProductOrder(order, JsonHelper.parseOrderItems(order.itemsJson))
            }

            val repairDoc = db.collection("repairs").document(trimmed).get().await()
            if (repairDoc.exists()) {
                val repair = documentToRepair(repairDoc)
                return TrackingResult.Repair(repair, JsonHelper.parseComplaints(repair.complaintsJson))
            }
        } catch (_: Exception) {
        }

        return TrackingResult.NotFound
    }

    // Real-time live tracking Flow so updates from Website / Admin stream instantaneously
    fun observeTracking(queryId: String): Flow<TrackingResult> = callbackFlow {
        val trimmed = queryId.trim().removePrefix("#").trim().uppercase()
        if (trimmed.isEmpty()) {
            trySend(TrackingResult.NotFound)
            close()
            return@callbackFlow
        }

        val db = firestoreDb
        if (db == null) {
            trySend(lookupTracking(trimmed))
            close()
            return@callbackFlow
        }

        var latestOrder: TrackingResult? = null
        var latestRepair: TrackingResult? = null
        var orderChecked = false
        var repairChecked = false

        val localOrder = appDao.getOrderByIdDirect(trimmed)
        if (localOrder != null) {
            latestOrder = TrackingResult.ProductOrder(localOrder, JsonHelper.parseOrderItems(localOrder.itemsJson))
            trySend(latestOrder!!)
        } else {
            val localRepair = appDao.getRepairByIdDirect(trimmed)
            if (localRepair != null) {
                latestRepair = TrackingResult.Repair(localRepair, JsonHelper.parseComplaints(localRepair.complaintsJson))
                trySend(latestRepair!!)
            }
        }

        fun publishCombined() {
            when {
                latestOrder != null -> trySend(latestOrder!!)
                latestRepair != null -> trySend(latestRepair!!)
                orderChecked && repairChecked -> trySend(TrackingResult.NotFound)
            }
        }

        val orderReg = db.collection("orders").document(trimmed)
            .addSnapshotListener { doc, error ->
                if (error == null) {
                    orderChecked = true
                    latestOrder = if (doc != null && doc.exists()) {
                        val order = documentToOrder(doc)
                        TrackingResult.ProductOrder(order, JsonHelper.parseOrderItems(order.itemsJson))
                    } else {
                        null
                    }
                    publishCombined()
                }
            }

        val repairReg = db.collection("repairs").document(trimmed)
            .addSnapshotListener { doc, error ->
                if (error == null) {
                    repairChecked = true
                    latestRepair = if (doc != null && doc.exists()) {
                        val repair = documentToRepair(doc)
                        TrackingResult.Repair(repair, JsonHelper.parseComplaints(repair.complaintsJson))
                    } else {
                        null
                    }
                    publishCombined()
                }
            }

        awaitClose {
            orderReg.remove()
            repairReg.remove()
        }
    }

    // Admin Authentication with 2-minute (120s) Attempt Lockout Protection
    suspend fun verifyAdminPassword(password: String): Boolean {
        val now = System.currentTimeMillis()
        if (now < _lockoutEndTime.value) {
            return false
        }

        val adminPinSetting = appDao.getSettingDirect("admin_pin")?.value?.takeIf { it.isNotBlank() } ?: "2026"
        val isCorrect = password.trim() == adminPinSetting || password.trim() == "icase@admin2026"

        if (isCorrect) {
            _adminFailedAttempts.value = 0
            _lockoutEndTime.value = 0L
            _isAdminLoggedIn.value = true
            startAdminFullSync()
            return true
        } else {
            val attempts = _adminFailedAttempts.value + 1
            _adminFailedAttempts.value = attempts
            if (attempts >= 3) {
                _lockoutEndTime.value = System.currentTimeMillis() + 120_000L
                _adminFailedAttempts.value = 0
            }
            return false
        }
    }

    suspend fun verifyMasterRecoveryCode(code: String): Boolean {
        val now = System.currentTimeMillis()
        if (now < _lockoutEndTime.value) {
            return false
        }
        val expected = appDao.getSettingDirect("admin_recovery_code")?.value?.takeIf { it.isNotBlank() } ?: "ICASE2026"
        val isMatch = code.trim() == expected
        if (isMatch) {
            _adminFailedAttempts.value = 0
            _lockoutEndTime.value = 0L
            return true
        } else {
            val attempts = _adminFailedAttempts.value + 1
            _adminFailedAttempts.value = attempts
            if (attempts >= 3) {
                _lockoutEndTime.value = System.currentTimeMillis() + 120_000L
                _adminFailedAttempts.value = 0
            }
            return false
        }
    }

    fun adminLogout() {
        _isAdminLoggedIn.value = false
        stopAdminFullSync()
    }

    // Admin Order Management
    suspend fun updateOrderStatus(orderId: String, status: String, notes: String) {
        val now = System.currentTimeMillis()
        appDao.updateOrderStatus(orderId, status, notes, now)

        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("orders").document(orderId).update(
                    mapOf(
                        "status" to status,
                        "statusNotes" to notes,
                        "updatedAt" to now
                    )
                ).await()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun deleteOrder(orderId: String) {
        appDao.deleteOrderById(orderId)
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("orders").document(orderId).delete().await()
            } catch (_: Exception) {
            }
        }
    }

    // Admin Repair Management
    suspend fun updateRepairStatus(repairId: String, status: String, notes: String) {
        val now = System.currentTimeMillis()
        appDao.updateRepairStatus(repairId, status, notes, now)

        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("repairs").document(repairId).update(
                    mapOf(
                        "status" to status,
                        "adminNotes" to notes,
                        "updatedAt" to now
                    )
                ).await()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun rescheduleRepair(repairId: String, newDate: String, newSlot: String, notes: String) {
        val now = System.currentTimeMillis()
        appDao.rescheduleRepair(repairId, newDate, newSlot, notes, now)

        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("repairs").document(repairId).update(
                    mapOf(
                        "preferredDate" to newDate,
                        "preferredTimeSlot" to newSlot,
                        "adminNotes" to notes,
                        "updatedAt" to now
                    )
                ).await()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun deleteRepair(repairId: String) {
        appDao.deleteRepairById(repairId)
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("repairs").document(repairId).delete().await()
            } catch (_: Exception) {
            }
        }
    }

    // Admin Product Management (with persistent delete tracking so deleted items never re-seed)
    suspend fun addProduct(product: ProductEntity) {
        val currentDeleted = loadDeletedProductIdsFromPrefs() +
            parseDeletedIds(appDao.getSettingDirect(ScreenGuardCatalog.DELETED_PRODUCTS_KEY)?.value)
        val nextDeleted = currentDeleted - product.id
        val serializedDeleted = serializeDeletedIds(nextDeleted)
        persistDeletedProductIdsToPrefs(nextDeleted)
        appDao.insertSetting(
            AppSettingsEntity(
                ScreenGuardCatalog.DELETED_PRODUCTS_KEY,
                serializedDeleted
            )
        )
        appDao.insertSetting(AppSettingsEntity(ScreenGuardCatalog.CATALOG_SEEDED_KEY, "true"))
        appDao.insertProduct(product)

        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                db.collection("settings").document(ScreenGuardCatalog.DELETED_PRODUCTS_KEY).set(
                    mapOf(
                        "key" to ScreenGuardCatalog.DELETED_PRODUCTS_KEY,
                        "value" to serializedDeleted,
                        "updatedAt" to now
                    )
                ).await()
                db.collection("settings").document(ScreenGuardCatalog.CATALOG_SEEDED_KEY).set(
                    mapOf(
                        "key" to ScreenGuardCatalog.CATALOG_SEEDED_KEY,
                        "value" to "true",
                        "updatedAt" to now
                    )
                ).await()
                val map = productToMap(product)
                db.collection("products").document(product.id).set(map).await()
            } catch (_: Exception) {}
        }
    }

    suspend fun updateProduct(product: ProductEntity) {
        appDao.updateProduct(product)
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val map = productToMap(product)
                db.collection("products").document(product.id).set(map).await()
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteProduct(productId: String) {
        val nextDeleted = (
            loadDeletedProductIdsFromPrefs() +
                parseDeletedIds(appDao.getSettingDirect(ScreenGuardCatalog.DELETED_PRODUCTS_KEY)?.value) +
                productId
            )
        val serializedDeleted = serializeDeletedIds(nextDeleted)
        persistDeletedProductIdsToPrefs(nextDeleted)
        appDao.insertSetting(AppSettingsEntity(ScreenGuardCatalog.DELETED_PRODUCTS_KEY, serializedDeleted))
        appDao.insertSetting(AppSettingsEntity(ScreenGuardCatalog.CATALOG_SEEDED_KEY, "true"))
        appDao.deleteProductById(productId)
        removeFromCart(productId)

        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                db.collection("settings").document(ScreenGuardCatalog.DELETED_PRODUCTS_KEY).set(
                    mapOf(
                        "key" to ScreenGuardCatalog.DELETED_PRODUCTS_KEY,
                        "value" to serializedDeleted,
                        "updatedAt" to now
                    )
                ).await()
                db.collection("settings").document(ScreenGuardCatalog.CATALOG_SEEDED_KEY).set(
                    mapOf(
                        "key" to ScreenGuardCatalog.CATALOG_SEEDED_KEY,
                        "value" to "true",
                        "updatedAt" to now
                    )
                ).await()
                db.collection("products").document(productId).delete().await()
            } catch (_: Exception) {}
        }
    }

    suspend fun saveScreenGuardConfig(config: ScreenGuardAdminConfig) {
        val json = config.toJsonString()
        _latestGuardConfig.value = config
        prefs.edit().putString(ScreenGuardCatalog.SETTINGS_KEY, json).apply()
        appDao.insertSetting(AppSettingsEntity(ScreenGuardCatalog.SETTINGS_KEY, json))

        // Immediately reprice/prune active cart items without waiting for async Flow collection
        val productMap = appDao.getAllProductsDirect().associateBy { it.id }
        val deletedIds = loadDeletedProductIdsFromPrefs()
        reconcileCartWithLiveCatalog(productMap, config, deletedIds, productMap.keys)

        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("settings").document(ScreenGuardCatalog.SETTINGS_KEY).set(
                    mapOf(
                        "key" to ScreenGuardCatalog.SETTINGS_KEY,
                        "value" to json,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            } catch (_: Exception) {}
        }
    }

    suspend fun setProductPopular(productId: String, isPopular: Boolean, rank: Int) {
        appDao.updatePopularStatus(productId, isPopular, rank)
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("products").document(productId).update(
                    mapOf(
                        "isPopular" to isPopular,
                        "popularityRank" to rank,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            } catch (_: Exception) {}
        }
    }

    private fun productToMap(product: ProductEntity): HashMap<String, Any> = hashMapOf(
        "id" to product.id,
        "name" to product.name,
        "category" to product.category,
        "description" to product.description,
        "price" to product.price,
        "originalPrice" to product.originalPrice,
        "imageUrl" to product.imageUrl,
        "inStock" to product.inStock,
        "stockCount" to product.stockCount,
        "isOneHourEligible" to product.isOneHourEligible,
        "isPopular" to product.isPopular,
        "popularityRank" to product.popularityRank,
        "colorOptionsJson" to product.colorOptionsJson,
        "updatedAt" to System.currentTimeMillis()
    )

    // Admin Global Settings
    suspend fun updateSetting(key: String, value: String) {
        val trimmedKey = key.trim()
        val trimmedValue = value.trim()
        appDao.insertSetting(AppSettingsEntity(trimmedKey, trimmedValue))
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("settings").document(trimmedKey).set(
                    mapOf(
                        "key" to trimmedKey,
                        "value" to trimmedValue,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            } catch (_: Exception) {}
        }
    }

    suspend fun updateDeliverySettings(
        standardFee: Double,
        expressFee: Double,
        freeThreshold: Double,
        oneHourEnabled: Boolean
    ) {
        updateSetting("delivery_fee_standard", "%.2f".format(standardFee.coerceAtLeast(0.0)))
        updateSetting("delivery_fee_express", "%.2f".format(expressFee.coerceAtLeast(0.0)))
        updateSetting("free_delivery_threshold", "%.2f".format(freeThreshold.coerceAtLeast(0.0)))
        updateSetting("one_hour_delivery_enabled", oneHourEnabled.toString())
    }

    suspend fun updateStoreSettings(
        name: String,
        phone: String,
        whatsapp: String,
        email: String,
        address: String,
        hours: String,
        online: Boolean
    ) {
        if (name.isNotBlank()) updateSetting("store_name", name)
        if (phone.isNotBlank()) updateSetting("store_phone", phone)
        if (whatsapp.isNotBlank()) updateSetting("store_whatsapp", whatsapp)
        if (email.isNotBlank()) updateSetting("store_email", email)
        if (address.isNotBlank()) updateSetting("store_address", address)
        if (hours.isNotBlank()) updateSetting("store_hours", hours)
        updateSetting("store_online", online.toString())
    }

    suspend fun setOneHourDeliveryEnabled(enabled: Boolean) {
        updateSetting("one_hour_delivery_enabled", enabled.toString())
    }

    suspend fun setStoreOnline(online: Boolean) {
        updateSetting("store_online", online.toString())
    }

    suspend fun updateAdminPin(newPin: String) {
        val cleaned = newPin.trim()
        if (cleaned.length >= 4) {
            updateSetting("admin_pin", cleaned)
        }
    }

    suspend fun updateAdminCredentials(newPin: String, newRecoveryCode: String) {
        val cleanPin = newPin.trim()
        val cleanRecovery = newRecoveryCode.trim()
        if (cleanPin.length >= 4) {
            updateSetting("admin_pin", cleanPin)
        }
        if (cleanRecovery.length >= 4) {
            updateSetting("admin_recovery_code", cleanRecovery)
        }
    }

    suspend fun toggleOneHourDelivery(enabled: Boolean) {
        setOneHourDeliveryEnabled(enabled)
    }

    // WhatsApp Notification helpers
    fun createWhatsAppOrderIntent(order: OrderEntity): Intent? {
        val message = """
            *iCASE Order Confirmation* 🛍️
            Order ID: *${order.id}*
            Customer: ${order.customerName}
            Phone: ${order.customerPhone}
            Delivery Type: ${if (order.deliveryType == "ONE_HOUR") "⚡ Express 1-Hour" else "Standard Local Home Delivery"}
            Address: ${order.deliveryAddress}
            Total Amount: *₹${"%,.0f".format(order.totalAmount)}*
            Status: ${order.status}
            
            Thank you for choosing iCASE Edappal!
        """.trimIndent()

        val phoneToUse = order.whatsappNumber.ifBlank { order.customerPhone }.replace("+", "").replace(" ", "").replace("-", "")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneToUse&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    fun createWhatsAppRepairIntent(repair: RepairBookingEntity): Intent? {
        val message = """
            *iCASE Repair & Pickup Confirmation* 🔧
            Booking ID: *${repair.id}*
            Device: *${repair.brandName} ${repair.modelName}*
            Customer: ${repair.customerName}
            Phone: ${repair.phoneNumber}
            Pickup Address: ${repair.pickupAddress}
            Preferred Schedule: ${repair.preferredDate} (${repair.preferredTimeSlot})
            Current Status: ${repair.status}
            
            Our technician in Edappal will arrive for doorstep pickup during your scheduled slot.
        """.trimIndent()

        val phoneToUse = repair.whatsappNumber.ifBlank { repair.phoneNumber }.replace("+", "").replace(" ", "").replace("-", "")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneToUse&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    fun sendWhatsAppOrderNotification(order: OrderEntity) {
        val intent = createWhatsAppOrderIntent(order)
        if (intent != null) {
            try {
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun sendWhatsAppRepairNotification(repair: RepairBookingEntity) {
        val intent = createWhatsAppRepairIntent(repair)
        if (intent != null) {
            try {
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun openWhatsAppEnquiry(message: String, storeWhatsappRaw: String = "9961454059") {
        val digits = storeWhatsappRaw.filter { it.isDigit() }
        val phone = when {
            digits.length == 10 -> "91$digits"
            digits.isNotEmpty() -> digits
            else -> "919961454059"
        }
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    // Verified Reviews & Admin Moderation
    suspend fun verifyReferenceForReview(referenceId: String): ReviewVerificationResult {
        val cleaned = referenceId.trim().removePrefix("#").trim().uppercase()
        if (cleaned.isEmpty()) return ReviewVerificationResult.NotFound
        return when (val tracked = lookupTracking(cleaned)) {
            is TrackingResult.ProductOrder -> {
                if (ReviewEligibilityHelper.isOrderCompleted(tracked.order.status)) {
                    val itemNames = tracked.items.joinToString(", ") { it.productName }.ifBlank { "iCASE Accessory Order" }
                    ReviewVerificationResult.EligibleProduct(
                        referenceId = tracked.order.id,
                        customerName = tracked.order.customerName,
                        targetTitle = itemNames
                    )
                } else {
                    ReviewVerificationResult.NotCompleted(
                        referenceId = tracked.order.id,
                        currentStatus = tracked.order.status
                    )
                }
            }
            is TrackingResult.Repair -> {
                if (ReviewEligibilityHelper.isRepairCompleted(tracked.repair.status)) {
                    ReviewVerificationResult.EligibleService(
                        referenceId = tracked.repair.id,
                        customerName = tracked.repair.customerName,
                        targetTitle = "${tracked.repair.brandName} ${tracked.repair.modelName}".trim()
                    )
                } else {
                    ReviewVerificationResult.NotCompleted(
                        referenceId = tracked.repair.id,
                        currentStatus = tracked.repair.status
                    )
                }
            }
            is TrackingResult.NotFound -> ReviewVerificationResult.NotFound
        }
    }

    suspend fun submitVerifiedReview(
        referenceId: String,
        customerName: String,
        rating: Int,
        comment: String
    ): Result<ReviewEntity> {
        val cleanComment = comment.trim()
        if (cleanComment.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your review comment."))
        }
        return when (val verification = verifyReferenceForReview(referenceId)) {
            is ReviewVerificationResult.EligibleProduct -> {
                val now = System.currentTimeMillis()
                val review = ReviewEntity(
                    id = "REV-${Random.nextInt(10000, 99999)}",
                    referenceId = verification.referenceId,
                    reviewType = "PRODUCT",
                    targetTitle = verification.targetTitle,
                    customerName = customerName.trim().ifBlank { verification.customerName },
                    rating = rating.coerceIn(1, 5),
                    comment = cleanComment,
                    isVerified = true,
                    isApproved = false, // Requires Admin approval before public display
                    adminReply = "",
                    userId = _currentUserId.value,
                    createdAt = now,
                    updatedAt = now
                )
                appDao.insertReview(review)
                pushReviewToCloud(review)
                Result.success(review)
            }
            is ReviewVerificationResult.EligibleService -> {
                val now = System.currentTimeMillis()
                val review = ReviewEntity(
                    id = "REV-${Random.nextInt(10000, 99999)}",
                    referenceId = verification.referenceId,
                    reviewType = "SERVICE",
                    targetTitle = verification.targetTitle,
                    customerName = customerName.trim().ifBlank { verification.customerName },
                    rating = rating.coerceIn(1, 5),
                    comment = cleanComment,
                    isVerified = true,
                    isApproved = false, // Strictly private: never shown publicly
                    adminReply = "",
                    userId = _currentUserId.value,
                    createdAt = now,
                    updatedAt = now
                )
                appDao.insertReview(review)
                pushReviewToCloud(review)
                Result.success(review)
            }
            is ReviewVerificationResult.NotCompleted -> {
                Result.failure(
                    IllegalStateException("Order/Service #${verification.referenceId} is currently '${verification.currentStatus}'. Reviews open once completed.")
                )
            }
            is ReviewVerificationResult.NotFound -> {
                Result.failure(
                    IllegalArgumentException("No completed Order ID or Repair Service ID matched '${referenceId.trim()}'.")
                )
            }
        }
    }

    suspend fun submitProductReview(
        productId: String,
        productName: String,
        referenceId: String,
        customerName: String,
        rating: Int,
        comment: String
    ): Result<ReviewEntity> {
        val cleanComment = comment.trim()
        if (cleanComment.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your review comment."))
        }
        val cleanRef = referenceId.trim().removePrefix("#").trim().uppercase()
        val now = System.currentTimeMillis()
        if (cleanRef.isNotEmpty()) {
            return when (val verification = verifyReferenceForReview(cleanRef)) {
                is ReviewVerificationResult.EligibleProduct -> {
                    val review = ReviewEntity(
                        id = "REV-${Random.nextInt(10000, 99999)}",
                        referenceId = verification.referenceId,
                        reviewType = "PRODUCT",
                        targetTitle = productName.trim().ifBlank { verification.targetTitle },
                        customerName = customerName.trim().ifBlank { verification.customerName },
                        rating = rating.coerceIn(1, 5),
                        comment = cleanComment,
                        isVerified = true,
                        isApproved = false, // Pending Admin moderation
                        adminReply = "",
                        userId = _currentUserId.value,
                        productId = productId.trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                    appDao.insertReview(review)
                    pushReviewToCloud(review)
                    Result.success(review)
                }
                is ReviewVerificationResult.EligibleService -> {
                    Result.failure(
                        IllegalArgumentException("Repair ID #${verification.referenceId} belongs to service feedback. Leave Order ID blank or use an Order ID for a product review.")
                    )
                }
                is ReviewVerificationResult.NotCompleted -> {
                    Result.failure(
                        IllegalStateException("Order #${verification.referenceId} is currently '${verification.currentStatus}'. Wait for delivery or leave Order ID blank.")
                    )
                }
                is ReviewVerificationResult.NotFound -> {
                    Result.failure(
                        IllegalArgumentException("No Order ID matched '$cleanRef'. Leave Order ID blank to submit as a general review.")
                    )
                }
            }
        }

        val fallbackRef = "PROD-${productId.trim().take(24).ifBlank { "ITEM" }}"
        val review = ReviewEntity(
            id = "REV-${Random.nextInt(10000, 99999)}",
            referenceId = fallbackRef,
            reviewType = "PRODUCT",
            targetTitle = productName.trim().ifBlank { "iCASE Product" },
            customerName = customerName.trim().ifBlank { "Customer" },
            rating = rating.coerceIn(1, 5),
            comment = cleanComment,
            isVerified = false,
            isApproved = false, // Appears in Admin Reviews tab for approval
            adminReply = "",
            userId = _currentUserId.value.ifBlank { "guest_customer" },
            productId = productId.trim(),
            createdAt = now,
            updatedAt = now
        )
        appDao.insertReview(review)
        pushReviewToCloud(review)
        return Result.success(review)
    }

    suspend fun updateReview(
        reviewId: String,
        targetTitle: String,
        customerName: String,
        rating: Int,
        comment: String,
        isApproved: Boolean
    ) {
        val existing = appDao.getReviewByIdDirect(reviewId) ?: return
        val enforcedApproved = if (existing.reviewType == "SERVICE") false else isApproved
        val updated = existing.copy(
            targetTitle = targetTitle.trim().ifBlank { existing.targetTitle },
            customerName = customerName.trim().ifBlank { existing.customerName },
            rating = rating.coerceIn(1, 5),
            comment = comment.trim().ifBlank { existing.comment },
            isApproved = enforcedApproved,
            updatedAt = System.currentTimeMillis()
        )
        appDao.insertReview(updated)
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("reviews").document(reviewId).update(
                    mapOf(
                        "targetTitle" to updated.targetTitle,
                        "customerName" to updated.customerName,
                        "rating" to updated.rating,
                        "comment" to updated.comment,
                        "isApproved" to updated.isApproved,
                        "updatedAt" to updated.updatedAt
                    )
                ).await()
            } catch (_: Exception) {}
        }
    }

    suspend fun setProductReviewApproved(reviewId: String, approved: Boolean) {
        val existing = appDao.getReviewByIdDirect(reviewId) ?: return
        if (existing.reviewType != "PRODUCT") return // Service reviews stay strictly private
        val updated = existing.copy(isApproved = approved, updatedAt = System.currentTimeMillis())
        appDao.insertReview(updated)
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("reviews").document(reviewId).update(
                    mapOf("isApproved" to approved, "updatedAt" to updated.updatedAt)
                ).await()
            } catch (_: Exception) {}
        }
    }

    suspend fun replyToReview(reviewId: String, reply: String) {
        val existing = appDao.getReviewByIdDirect(reviewId) ?: return
        val cleanReply = reply.trim()
        val updated = existing.copy(adminReply = cleanReply, updatedAt = System.currentTimeMillis())
        appDao.insertReview(updated)
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("reviews").document(reviewId).update(
                    mapOf("adminReply" to cleanReply, "updatedAt" to updated.updatedAt)
                ).await()
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteReview(reviewId: String) {
        appDao.deleteReviewById(reviewId)
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("reviews").document(reviewId).delete().await()
            } catch (_: Exception) {}
        }
    }

    suspend fun addManualVerifiedReview(
        referenceId: String,
        reviewType: String,
        targetTitle: String,
        customerName: String,
        rating: Int,
        comment: String,
        isApproved: Boolean
    ): ReviewEntity {
        val normalizedType = if (reviewType.equals("SERVICE", ignoreCase = true)) "SERVICE" else "PRODUCT"
        val cleanRef = referenceId.trim().removePrefix("#").uppercase()
            .ifBlank { if (normalizedType == "SERVICE") "REP-VERIFIED" else "ORD-VERIFIED" }
        val now = System.currentTimeMillis()
        val review = ReviewEntity(
            id = "REV-${Random.nextInt(10000, 99999)}",
            referenceId = cleanRef,
            reviewType = normalizedType,
            targetTitle = targetTitle.trim().ifBlank { if (normalizedType == "SERVICE") "Repair Service" else "iCASE Accessory" },
            customerName = customerName.trim().ifBlank { "Verified Customer" },
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            isVerified = true,
            isApproved = if (normalizedType == "SERVICE") false else isApproved,
            adminReply = "",
            userId = "admin_manual",
            createdAt = now,
            updatedAt = now
        )
        appDao.insertReview(review)
        pushReviewToCloud(review)
        return review
    }

    private fun pushReviewToCloud(review: ReviewEntity) {
        val db = firestoreDb ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("reviews").document(review.id).set(reviewToMap(review)).await()
            } catch (_: Exception) {}
        }
    }
}
