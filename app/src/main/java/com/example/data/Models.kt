package com.example.data

import org.json.JSONArray
import org.json.JSONObject

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
)

data class OrderItemJson(
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

data class ProductColorOption(
    val name: String = "",
    val hex: String = "#1E293B"
)

object JsonHelper {
    val presetColors: List<ProductColorOption> = listOf(
        ProductColorOption("Space Black", "#2C2C2E"),
        ProductColorOption("Navy Blue", "#1B2A4A"),
        ProductColorOption("Crimson Red", "#E50914"),
        ProductColorOption("Deep Purple", "#6B5B95"),
        ProductColorOption("Natural Titanium", "#C4B8A5"),
        ProductColorOption("Forest Green", "#166534"),
        ProductColorOption("Sky Blue", "#0284C7"),
        ProductColorOption("Rose Gold", "#E11D48"),
        ProductColorOption("Pearl White", "#F1F5F9"),
        ProductColorOption("Amber Gold", "#D97706")
    )

    fun normalizeHexColor(rawHex: String): String {
        val cleaned = rawHex.trim().removePrefix("#").uppercase()
        return when {
            cleaned.length == 6 && cleaned.all { it in '0'..'9' || it in 'A'..'F' } -> "#$cleaned"
            cleaned.length == 8 && cleaned.all { it in '0'..'9' || it in 'A'..'F' } -> "#$cleaned"
            else -> "#1E293B"
        }
    }

    fun serializeColorOptions(options: List<ProductColorOption>): String {
        val array = JSONArray()
        for (opt in options) {
            val cleanName = opt.name.trim()
            if (cleanName.isBlank()) continue
            val obj = JSONObject()
            obj.put("name", cleanName)
            obj.put("hex", normalizeHexColor(opt.hex))
            array.put(obj)
        }
        return array.toString()
    }

    fun parseColorOptions(jsonStr: String): List<ProductColorOption> {
        if (jsonStr.isBlank()) return emptyList()
        val result = mutableListOf<ProductColorOption>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val name = obj.optString("name").trim()
                val hex = normalizeHexColor(obj.optString("hex", "#1E293B"))
                if (name.isNotBlank()) {
                    result.add(ProductColorOption(name = name, hex = hex))
                }
            }
        } catch (_: Exception) {
        }
        return result
    }

    fun serializeCartItems(cartItems: List<CartItem>): String {
        val array = JSONArray()
        for (item in cartItems) {
            val obj = JSONObject()
            obj.put("productId", item.product.id)
            obj.put("productName", item.product.name)
            obj.put("price", item.product.price)
            obj.put("quantity", item.quantity)
            obj.put("imageUrl", item.product.imageUrl)
            array.put(obj)
        }
        return array.toString()
    }

    fun parseOrderItems(jsonStr: String): List<OrderItemJson> {
        val result = mutableListOf<OrderItemJson>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    OrderItemJson(
                        productId = obj.optString("productId"),
                        productName = obj.optString("productName"),
                        price = obj.optDouble("price", 0.0),
                        quantity = obj.optInt("quantity", 1),
                        imageUrl = obj.optString("imageUrl")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return result
    }

    fun serializeComplaints(complaints: List<String>): String {
        val array = JSONArray()
        for (c in complaints) {
            array.put(c)
        }
        return array.toString()
    }

    fun parseComplaints(jsonStr: String): List<String> {
        val result = mutableListOf<String>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                result.add(array.getString(i))
            }
        } catch (_: Exception) {
        }
        return result
    }
}

sealed class TrackingResult {
    data class ProductOrder(val order: OrderEntity, val items: List<OrderItemJson>) : TrackingResult()
    data class Repair(val repair: RepairBookingEntity, val complaints: List<String>) : TrackingResult()
    object NotFound : TrackingResult()
}

sealed class ReviewVerificationResult {
    data class EligibleProduct(
        val referenceId: String,
        val customerName: String,
        val targetTitle: String
    ) : ReviewVerificationResult()

    data class EligibleService(
        val referenceId: String,
        val customerName: String,
        val targetTitle: String
    ) : ReviewVerificationResult()

    data class NotCompleted(
        val referenceId: String,
        val currentStatus: String
    ) : ReviewVerificationResult()

    object NotFound : ReviewVerificationResult()
}

object ReviewEligibilityHelper {
    val completedOrderStatuses = setOf("Delivered", "Completed")
    val completedRepairStatuses = setOf("Repair Completed", "Out for Delivery", "Delivered", "Completed")

    fun isOrderCompleted(status: String): Boolean =
        completedOrderStatuses.any { it.equals(status.trim(), ignoreCase = true) }

    fun isRepairCompleted(status: String): Boolean =
        completedRepairStatuses.any { it.equals(status.trim(), ignoreCase = true) }
}

data class ProductRatingSummary(
    val averageRating: Double,
    val reviewCount: Int
)

object ReviewRatingHelper {
    fun matchesProduct(review: ReviewEntity, productId: String, productName: String): Boolean {
        if (review.reviewType != "PRODUCT" || !review.isApproved) return false
        val pid = productId.trim()
        val pname = productName.trim()
        if (review.productId.isNotBlank() && pid.isNotBlank() && review.productId.trim() == pid) {
            return true
        }
        return pname.isNotBlank() && review.targetTitle.contains(pname, ignoreCase = true)
    }

    fun matchingReviews(
        reviews: List<ReviewEntity>,
        productId: String,
        productName: String
    ): List<ReviewEntity> {
        return reviews
            .filter { matchesProduct(it, productId, productName) }
            .sortedByDescending { it.createdAt }
    }

    fun summarizeProductRating(
        reviews: List<ReviewEntity>,
        productId: String,
        productName: String
    ): ProductRatingSummary {
        val matched = matchingReviews(reviews, productId, productName)
        if (matched.isEmpty()) return ProductRatingSummary(averageRating = 0.0, reviewCount = 0)
        val avg = matched.sumOf { it.rating.coerceIn(1, 5) }.toDouble() / matched.size.toDouble()
        return ProductRatingSummary(averageRating = avg, reviewCount = matched.size)
    }
}


