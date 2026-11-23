package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Products
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsDirect(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isPopular = 1 ORDER BY popularityRank ASC")
    fun getPopularProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("UPDATE products SET isPopular = :isPopular, popularityRank = :rank WHERE id = :id")
    suspend fun updatePopularStatus(id: String, isPopular: Boolean, rank: Int)

    // Orders
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId AND :userId != '' ORDER BY createdAt DESC")
    fun getOrdersByUserId(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderByIdDirect(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status, statusNotes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: String, notes: String, updatedAt: Long)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: String)

    @Query("DELETE FROM orders WHERE userId != :keepUserId")
    suspend fun deleteOrdersNotOwnedBy(keepUserId: String)

    @Query("SELECT COUNT(*) FROM orders")
    fun getOrdersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status != 'Delivered'")
    fun getPendingOrdersCount(): Flow<Int>

    // Repair Bookings
    @Query("SELECT * FROM repair_bookings ORDER BY createdAt DESC")
    fun getAllRepairs(): Flow<List<RepairBookingEntity>>

    @Query("SELECT * FROM repair_bookings WHERE userId = :userId AND :userId != '' ORDER BY createdAt DESC")
    fun getRepairsByUserId(userId: String): Flow<List<RepairBookingEntity>>

    @Query("SELECT * FROM repair_bookings WHERE id = :id")
    fun getRepairById(id: String): Flow<RepairBookingEntity?>

    @Query("SELECT * FROM repair_bookings WHERE id = :id")
    suspend fun getRepairByIdDirect(id: String): RepairBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepair(repair: RepairBookingEntity)

    @Update
    suspend fun updateRepair(repair: RepairBookingEntity)

    @Query("UPDATE repair_bookings SET status = :status, adminNotes = :adminNotes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRepairStatus(id: String, status: String, adminNotes: String, updatedAt: Long)

    @Query("UPDATE repair_bookings SET preferredDate = :newDate, preferredTimeSlot = :newSlot, adminNotes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rescheduleRepair(id: String, newDate: String, newSlot: String, notes: String, updatedAt: Long)

    @Query("DELETE FROM repair_bookings WHERE id = :id")
    suspend fun deleteRepairById(id: String)

    @Query("DELETE FROM repair_bookings WHERE userId != :keepUserId")
    suspend fun deleteRepairsNotOwnedBy(keepUserId: String)

    @Query("SELECT COUNT(*) FROM repair_bookings")
    fun getRepairsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM repair_bookings WHERE status != 'Delivered'")
    fun getPendingRepairsCount(): Flow<Int>

    // Settings
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingsEntity>>

    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    suspend fun getSettingDirect(key: String): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSetting(setting: AppSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSettings(settings: List<AppSettingsEntity>)

    // Verified Reviews (Public shows ONLY approved PRODUCT reviews; Admin sees all)
    @Query("SELECT * FROM reviews WHERE reviewType = 'PRODUCT' AND isApproved = 1 ORDER BY createdAt DESC")
    fun getPublicApprovedProductReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    suspend fun getAllReviewsDirect(): List<ReviewEntity>

    @Query("SELECT * FROM reviews WHERE id = :id")
    suspend fun getReviewByIdDirect(id: String): ReviewEntity?

    @Query("SELECT * FROM reviews WHERE referenceId = :referenceId LIMIT 1")
    suspend fun getReviewByReferenceIdDirect(referenceId: String): ReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReviewById(id: String)

    @Query("DELETE FROM reviews WHERE reviewType != 'PRODUCT' OR isApproved != 1")
    suspend fun deleteNonPublicReviews()
}

