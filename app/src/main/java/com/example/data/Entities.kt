package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val price: Double,
    val originalPrice: Double,
    val imageUrl: String,
    val inStock: Boolean = true,
    val stockCount: Int = 25,
    val isOneHourEligible: Boolean = true,
    val isPopular: Boolean = false,
    val popularityRank: Int = 0,
    val colorOptionsJson: String = "[]"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val userId: String = "",
    val customerName: String,
    val customerPhone: String,
    val whatsappNumber: String,
    val deliveryAddress: String,
    val deliveryType: String, // "STANDARD" or "ONE_HOUR"
    val itemsJson: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val status: String = "Order Received", // Order Received -> Processing -> Out for Delivery -> Delivered
    val statusNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "repair_bookings")
data class RepairBookingEntity(
    @PrimaryKey
    val id: String,
    val userId: String = "",
    val brandName: String,
    val modelName: String,
    val complaintsJson: String, // JSON array of string complaints
    val description: String,
    val photoUri: String? = null,
    val pickupAddress: String,
    val customerName: String,
    val phoneNumber: String,
    val whatsappNumber: String,
    val preferredDate: String,
    val preferredTimeSlot: String,
    val status: String = "Booking Received", // Booking Received -> Pickup Scheduled -> Device Received -> Repairing -> Repair Completed -> Out for Delivery -> Delivered
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey
    val id: String,
    val referenceId: String,
    val reviewType: String, // "PRODUCT" or "SERVICE"
    val targetTitle: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val isVerified: Boolean = true,
    val isApproved: Boolean = false,
    val adminReply: String = "",
    val userId: String = "",
    val productId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

