package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.OrderEntity
import com.example.data.PouchAdminItem
import com.example.data.ProductEntity
import com.example.data.RepairBookingEntity
import com.example.data.ReviewEntity
import com.example.data.ReviewVerificationResult
import com.example.data.ScreenGuardAdminConfig
import com.example.data.TrackingResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Shop : Screen()
    data class ScreenGuards(val initialSection: String = "") : Screen()
    data class ProductDetail(val product: ProductEntity) : Screen()
    object Cart : Screen()
    object RepairPickup : Screen()
    object TrackOrder : Screen()
    object Profile : Screen()
    object MyOrders : Screen()
    object MyRepairs : Screen()
    object MyAddresses : Screen()
    object Wishlist : Screen()
    object AdminDashboard : Screen()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AppRepository(database.appDao(), application, viewModelScope)
    }

    // Screen navigation stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val currentScreen: StateFlow<Screen> = MutableStateFlow<Screen>(Screen.Home).apply {
        viewModelScope.launch {
            _screenStack.collect { stack ->
                value = stack.lastOrNull() ?: Screen.Home
            }
        }
    }

    fun navigateTo(screen: Screen) {
        if (screen is Screen.TrackOrder) {
            clearTracking()
        }
        _screenStack.value = _screenStack.value + screen
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        return if (current.size > 1) {
            _screenStack.value = current.dropLast(1)
            true
        } else {
            false
        }
    }

    fun navigateToHome() {
        _screenStack.value = listOf(Screen.Home)
    }

    // Products & Categories
    val allProducts = repository.allProducts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val popularProducts = repository.popularProducts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Strictly public approved PRODUCT reviews only
    val publicProductReviews = repository.publicProductReviews.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Full reviews for protected Admin Dashboard only (includes pending & private SERVICE feedback)
    val adminAllReviews = repository.adminAllReviews.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val selectedCategory = MutableStateFlow("All")

    // Dynamic Store & Delivery Settings
    val cartItems = repository.cartItems
    val isOneHourDeliveryGloballyEnabled = repository.isOneHourDeliveryGloballyEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val standardDeliveryFee = repository.standardDeliveryFee.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        50.0
    )

    val expressDeliveryFee = repository.expressDeliveryFee.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        80.0
    )

    val freeDeliveryThreshold = repository.freeDeliveryThreshold.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.0
    )

    val storeName = repository.storeName.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "iCASE Mobile Accessories & Repair"
    )

    val storePhone = repository.storePhone.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "9961454059"
    )

    val storeWhatsapp = repository.storeWhatsapp.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "9961454059"
    )

    val storeEmail = repository.storeEmail.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "icaseedappal@gmail.com"
    )

    val storeAddress = repository.storeAddress.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "Main Road, Near Town Square, Edappal, Malappuram, Kerala"
    )

    val storeHours = repository.storeHours.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "9:00 AM - 10:00 PM (Daily)"
    )

    val isStoreOnline = repository.isStoreOnline.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val adminPin = repository.adminPin.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "2026"
    )

    val adminRecoveryCode = repository.adminRecoveryCode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "ICASE2026"
    )

    val screenGuardConfig = repository.screenGuardConfig.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ScreenGuardAdminConfig()
    )

    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        repository.addToCart(product, quantity)
    }

    fun addTemperedGlassToCart(
        brand: String,
        model: String,
        glassType: String,
        installationOption: String = ""
    ) {
        repository.addTemperedGlassToCart(brand, model, glassType, installationOption)
    }

    fun addCameraLensToCart(
        brand: String,
        model: String,
        installationOption: String = ""
    ) {
        repository.addCameraLensToCart(brand, model, installationOption)
    }

    fun addComboToCart(
        glassBrand: String,
        glassModel: String,
        glassType: String,
        cameraBrand: String,
        cameraModel: String,
        installationOption: String = ""
    ) {
        repository.addComboToCart(
            glassBrand = glassBrand,
            glassModel = glassModel,
            glassType = glassType,
            cameraBrand = cameraBrand,
            cameraModel = cameraModel,
            installationOption = installationOption
        )
    }

    fun addPouchToCart(brand: String, model: String, pouch: PouchAdminItem, selectedColor: String = "") {
        repository.addPouchToCart(brand, model, pouch, selectedColor)
    }

    fun openWhatsAppEnquiry(message: String) {
        repository.openWhatsAppEnquiry(message, storeWhatsapp.value)
    }

    fun saveScreenGuardConfig(config: ScreenGuardAdminConfig) {
        viewModelScope.launch {
            repository.saveScreenGuardConfig(config)
        }
    }

    fun buildDefaultCodeExport(): String {
        return com.example.data.ScreenGuardCatalog.generateDefaultCodeExport(
            products = allProducts.value,
            config = screenGuardConfig.value,
            storeName = storeName.value,
            storePhone = storePhone.value,
            storeWhatsapp = storeWhatsapp.value,
            storeEmail = storeEmail.value,
            storeAddress = storeAddress.value,
            storeHours = storeHours.value,
            isStoreOnline = isStoreOnline.value,
            isOneHourEnabled = isOneHourDeliveryGloballyEnabled.value,
            standardDeliveryFee = standardDeliveryFee.value,
            expressDeliveryFee = expressDeliveryFee.value,
            freeDeliveryThreshold = freeDeliveryThreshold.value,
            adminPin = adminPin.value,
            adminRecoveryCode = adminRecoveryCode.value
        )
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        repository.updateCartQuantity(productId, quantity)
    }

    fun removeFromCart(productId: String) {
        repository.removeFromCart(productId)
    }

    fun clearCart() {
        repository.clearCart()
    }

    // Checkout
    var isPlacingOrder = MutableStateFlow(false)
    var lastPlacedOrder = MutableStateFlow<OrderEntity?>(null)

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        whatsappNumber: String,
        deliveryAddress: String,
        deliveryType: String,
        deliveryFee: Double,
        onSuccess: (OrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            isPlacingOrder.value = true
            val order = repository.placeOrder(
                customerName,
                customerPhone,
                whatsappNumber,
                deliveryAddress,
                deliveryType,
                deliveryFee
            )
            lastPlacedOrder.value = order
            isPlacingOrder.value = false
            onSuccess(order)
        }
    }

    // Repair Booking
    var isBookingRepair = MutableStateFlow(false)
    var lastBookedRepair = MutableStateFlow<RepairBookingEntity?>(null)

    fun bookRepair(
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
        preferredTimeSlot: String,
        onSuccess: (RepairBookingEntity) -> Unit
    ) {
        viewModelScope.launch {
            isBookingRepair.value = true
            val booking = repository.bookRepair(
                brandName,
                modelName,
                complaints,
                description,
                photoUri,
                pickupAddress,
                customerName,
                phoneNumber,
                whatsappNumber,
                preferredDate,
                preferredTimeSlot
            )
            lastBookedRepair.value = booking
            isBookingRepair.value = false
            onSuccess(booking)
        }
    }

    // Track Order / Repair Lookup (live real-time stream)
    val trackingQuery = MutableStateFlow("")
    val trackingResult = MutableStateFlow<TrackingResult?>(null)
    val isSearchingTracking = MutableStateFlow(false)
    private var trackingJob: Job? = null

    fun searchTracking(id: String) {
        val trimmed = id.trim()
        trackingQuery.value = trimmed
        trackingJob?.cancel()
        if (trimmed.isEmpty()) {
            trackingResult.value = null
            isSearchingTracking.value = false
            return
        }
        trackingJob = viewModelScope.launch {
            isSearchingTracking.value = true
            repository.observeTracking(trimmed).collect { result ->
                trackingResult.value = result
                isSearchingTracking.value = false
            }
        }
    }

    fun clearTracking() {
        trackingJob?.cancel()
        trackingJob = null
        trackingQuery.value = ""
        trackingResult.value = null
        isSearchingTracking.value = false
    }

    // Hidden Admin Access logic: 5 consecutive taps on iCASE logo within 3 seconds
    private var tapTimestamps = mutableListOf<Long>()
    val showAdminPasswordDialog = MutableStateFlow(false)
    val adminPasswordInput = MutableStateFlow("")
    val adminAuthError = MutableStateFlow<String?>(null)
    val adminAuthSuccess = MutableStateFlow<String?>(null)
    val adminForgotStep = MutableStateFlow("LOGIN") // LOGIN | VERIFY_RECOVERY | RESET_PIN
    val recoveryCodeInput = MutableStateFlow("")
    val newResetPinInput = MutableStateFlow("")
    val confirmResetPinInput = MutableStateFlow("")
    val lockoutRemainingSeconds = MutableStateFlow(0)
    private var lockoutCountdownJob: Job? = null

    val isAdminLoggedIn = repository.isAdminLoggedIn

    fun onLogoClicked() {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)
        tapTimestamps = tapTimestamps.filter { now - it <= 3000L }.toMutableList()

        if (tapTimestamps.size >= 5) {
            tapTimestamps.clear()
            adminPasswordInput.value = ""
            adminAuthError.value = null
            adminAuthSuccess.value = null
            adminForgotStep.value = "LOGIN"
            recoveryCodeInput.value = ""
            newResetPinInput.value = ""
            confirmResetPinInput.value = ""
            checkLockoutStatus()
            showAdminPasswordDialog.value = true
        }
    }

    private fun checkLockoutStatus() {
        val now = System.currentTimeMillis()
        val lockEnd = repository.lockoutEndTime.value
        if (lockEnd > now) {
            startLockoutCountdown(((lockEnd - now) + 999L) / 1000L)
        } else {
            lockoutRemainingSeconds.value = 0
        }
    }

    private fun startLockoutCountdown(seconds: Long) {
        lockoutCountdownJob?.cancel()
        lockoutCountdownJob = viewModelScope.launch {
            var s = seconds.toInt().coerceAtLeast(1)
            while (s > 0) {
                lockoutRemainingSeconds.value = s
                delay(1000)
                s--
            }
            lockoutRemainingSeconds.value = 0
            adminAuthError.value = null
        }
    }

    fun openForgotPasswordFlow() {
        adminAuthError.value = null
        adminAuthSuccess.value = null
        recoveryCodeInput.value = ""
        newResetPinInput.value = ""
        confirmResetPinInput.value = ""
        adminForgotStep.value = "VERIFY_RECOVERY"
        checkLockoutStatus()
    }

    fun backToAdminLogin() {
        adminAuthError.value = null
        adminAuthSuccess.value = null
        recoveryCodeInput.value = ""
        newResetPinInput.value = ""
        confirmResetPinInput.value = ""
        adminForgotStep.value = "LOGIN"
    }

    fun submitRecoveryCode() {
        checkLockoutStatus()
        if (lockoutRemainingSeconds.value > 0) {
            adminAuthError.value = "Too many failed attempts. Locked for ${lockoutRemainingSeconds.value}s."
            return
        }
        val code = recoveryCodeInput.value.trim()
        if (code.isBlank()) {
            adminAuthError.value = "Please enter your Master Recovery Code."
            return
        }
        viewModelScope.launch {
            val verified = repository.verifyMasterRecoveryCode(code)
            if (verified) {
                adminAuthError.value = null
                adminForgotStep.value = "RESET_PIN"
            } else {
                checkLockoutStatus()
                if (lockoutRemainingSeconds.value > 0) {
                    adminAuthError.value = "Incorrect code! 3 failed attempts. Account locked for 2 minutes."
                } else {
                    val remaining = 3 - repository.adminFailedAttempts.value
                    adminAuthError.value = "Invalid Master Recovery Code. $remaining attempt(s) left."
                }
            }
        }
    }

    fun submitNewPinReset() {
        val pin = newResetPinInput.value.trim()
        val confirm = confirmResetPinInput.value.trim()
        if (pin.length < 4) {
            adminAuthError.value = "New Password / PIN must be at least 4 characters."
            return
        }
        if (pin != confirm) {
            adminAuthError.value = "Passwords do not match. Please re-enter."
            return
        }
        viewModelScope.launch {
            repository.updateAdminPin(pin)
            adminPasswordInput.value = ""
            recoveryCodeInput.value = ""
            newResetPinInput.value = ""
            confirmResetPinInput.value = ""
            adminAuthError.value = null
            adminAuthSuccess.value = "New Admin Password / PIN saved! Enter it below to unlock."
            adminForgotStep.value = "LOGIN"
        }
    }

    fun submitAdminPassword(onSuccess: () -> Unit) {
        checkLockoutStatus()
        if (lockoutRemainingSeconds.value > 0) {
            adminAuthError.value = "Too many failed attempts. Locked for ${lockoutRemainingSeconds.value}s."
            return
        }

        viewModelScope.launch {
            val success = repository.verifyAdminPassword(adminPasswordInput.value)
            if (success) {
                showAdminPasswordDialog.value = false
                adminPasswordInput.value = ""
                adminAuthError.value = null
                adminAuthSuccess.value = null
                adminForgotStep.value = "LOGIN"
                navigateTo(Screen.AdminDashboard)
                onSuccess()
            } else {
                checkLockoutStatus()
                if (lockoutRemainingSeconds.value > 0) {
                    adminAuthError.value = "Incorrect password! 3 failed attempts. Account locked for 2 minutes."
                } else {
                    val remaining = 3 - repository.adminFailedAttempts.value
                    adminAuthError.value = "Invalid Admin credentials. $remaining attempt(s) left."
                }
            }
        }
    }

    fun dismissAdminPasswordDialog() {
        showAdminPasswordDialog.value = false
        adminPasswordInput.value = ""
        adminAuthError.value = null
        adminAuthSuccess.value = null
        adminForgotStep.value = "LOGIN"
        recoveryCodeInput.value = ""
        newResetPinInput.value = ""
        confirmResetPinInput.value = ""
    }

    fun adminLogout() {
        repository.adminLogout()
        navigateToHome()
    }

    // Customer-Isolated Orders & Repairs (ONLY current logged-in user)
    val currentUserId = repository.currentUserId

    val myOrders = repository.myOrders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val myRepairs = repository.myRepairs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Admin Dashboard Data (ALL orders & repairs across all users)
    val allOrders = repository.allOrders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allRepairs = repository.allRepairs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val ordersCount = repository.ordersCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val pendingOrdersCount = repository.pendingOrdersCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val repairsCount = repository.repairsCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val pendingRepairsCount = repository.pendingRepairsCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    fun updateOrderStatus(orderId: String, status: String, notes: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, notes)
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
        }
    }

    fun updateRepairStatus(repairId: String, status: String, notes: String) {
        viewModelScope.launch {
            repository.updateRepairStatus(repairId, status, notes)
        }
    }

    fun rescheduleRepair(repairId: String, newDate: String, newSlot: String, notes: String) {
        viewModelScope.launch {
            repository.rescheduleRepair(repairId, newDate, newSlot, notes)
        }
    }

    fun deleteRepair(repairId: String) {
        viewModelScope.launch {
            repository.deleteRepair(repairId)
        }
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.addProduct(product)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun setProductPopular(productId: String, isPopular: Boolean, rank: Int) {
        viewModelScope.launch {
            repository.setProductPopular(productId, isPopular, rank)
        }
    }

    fun toggleOneHourDelivery(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleOneHourDelivery(enabled)
        }
    }

    fun updateDeliverySettings(
        standardFee: Double,
        expressFee: Double,
        freeThreshold: Double,
        oneHourEnabled: Boolean
    ) {
        viewModelScope.launch {
            repository.updateDeliverySettings(standardFee, expressFee, freeThreshold, oneHourEnabled)
        }
    }

    fun updateStoreSettings(
        name: String,
        phone: String,
        whatsapp: String,
        email: String,
        address: String,
        hours: String,
        online: Boolean
    ) {
        viewModelScope.launch {
            repository.updateStoreSettings(name, phone, whatsapp, email, address, hours, online)
        }
    }

    fun toggleStoreOnline(online: Boolean) {
        viewModelScope.launch {
            repository.setStoreOnline(online)
        }
    }

    fun updateAdminPin(newPin: String) {
        viewModelScope.launch {
            repository.updateAdminPin(newPin)
        }
    }

    fun updateAdminCredentials(newPin: String, newRecoveryCode: String) {
        viewModelScope.launch {
            repository.updateAdminCredentials(newPin, newRecoveryCode)
        }
    }

    fun sendWhatsAppOrderNotification(order: OrderEntity) {
        repository.sendWhatsAppOrderNotification(order)
    }

    fun sendWhatsAppRepairNotification(repair: RepairBookingEntity) {
        repository.sendWhatsAppRepairNotification(repair)
    }

    val customerProfile = repository.customerProfile

    fun onFirebaseSessionRestored(user: FirebaseUser) {
        repository.onFirebaseUserRestored(user)
    }

    fun onGoogleUserAuthenticated(user: FirebaseUser) {
        repository.onGoogleUserAuthenticated(user)
    }

    val wishlistIds: StateFlow<List<String>> = customerProfile
        .map { it.wishlistIds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), customerProfile.value.wishlistIds)

    fun toggleWishlist(productId: String): Boolean {
        return repository.toggleWishlist(productId)
    }

    fun updateCustomerAddress(address: String) {
        repository.updateCustomerAddress(address)
    }

    fun updateCustomerProfile(name: String, phone: String, whatsapp: String, address: String) {
        repository.updateCustomerProfile(name, phone, whatsapp, address)
    }

    fun clearCustomerProfile() {
        repository.clearCustomerProfile()
    }

    // Verified Reviews & Admin Review Management
    suspend fun verifyReferenceForReview(referenceId: String): ReviewVerificationResult {
        return repository.verifyReferenceForReview(referenceId)
    }

    fun submitVerifiedReview(
        referenceId: String,
        customerName: String,
        rating: Int,
        comment: String,
        onResult: (Result<ReviewEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.submitVerifiedReview(referenceId, customerName, rating, comment))
        }
    }

    fun submitProductReview(
        productId: String,
        productName: String,
        referenceId: String,
        customerName: String,
        rating: Int,
        comment: String,
        onResult: (Result<ReviewEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(
                repository.submitProductReview(
                    productId = productId,
                    productName = productName,
                    referenceId = referenceId,
                    customerName = customerName,
                    rating = rating,
                    comment = comment
                )
            )
        }
    }

    fun updateReview(
        reviewId: String,
        targetTitle: String,
        customerName: String,
        rating: Int,
        comment: String,
        isApproved: Boolean
    ) {
        viewModelScope.launch {
            repository.updateReview(
                reviewId = reviewId,
                targetTitle = targetTitle,
                customerName = customerName,
                rating = rating,
                comment = comment,
                isApproved = isApproved
            )
        }
    }

    fun setProductReviewApproved(reviewId: String, approved: Boolean) {
        viewModelScope.launch {
            repository.setProductReviewApproved(reviewId, approved)
        }
    }

    fun replyToReview(reviewId: String, reply: String) {
        viewModelScope.launch {
            repository.replyToReview(reviewId, reply)
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            repository.deleteReview(reviewId)
        }
    }

    fun addManualVerifiedReview(
        referenceId: String,
        reviewType: String,
        targetTitle: String,
        customerName: String,
        rating: Int,
        comment: String,
        isApproved: Boolean,
        onAdded: (ReviewEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            val added = repository.addManualVerifiedReview(
                referenceId = referenceId,
                reviewType = reviewType,
                targetTitle = targetTitle,
                customerName = customerName,
                rating = rating,
                comment = comment,
                isApproved = isApproved
            )
            onAdded(added)
        }
    }
}
