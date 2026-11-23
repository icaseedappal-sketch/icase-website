package com.example

import com.example.data.CartItem
import com.example.data.InitialData
import com.example.data.JsonHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {
    @Test
    fun initialProducts_areValidAndPopulated() {
        assertTrue("Products list must not be empty", InitialData.products.isNotEmpty())
        assertTrue("Must have at least 10 products", InitialData.products.size >= 10)

        // Verify categories
        val categories = InitialData.products.map { it.category }.distinct()
        assertTrue("Must include Cases", categories.contains("Cases"))
        assertTrue("Must include Screen Protectors", categories.contains("Screen Protectors"))
        assertTrue("Must include Fast Chargers", categories.contains("Fast Chargers"))

        // Verify products have valid pricing and stock
        for (prod in InitialData.products) {
            assertTrue("Price must be positive: ${prod.name}", prod.price > 0.0)
            assertTrue("Stock count must be positive: ${prod.name}", prod.stockCount > 0)
        }
    }

    @Test
    fun jsonHelper_serializesAndParsesCartItemsCorrectly() {
        val sampleProd = InitialData.products.first()
        val cart = listOf(CartItem(sampleProd, 2))

        val serialized = JsonHelper.serializeCartItems(cart)
        val parsed = JsonHelper.parseOrderItems(serialized)

        assertEquals(1, parsed.size)
        assertEquals(sampleProd.id, parsed[0].productId)
        assertEquals(sampleProd.name, parsed[0].productName)
        assertEquals(sampleProd.price, parsed[0].price, 0.001)
        assertEquals(2, parsed[0].quantity)
    }

    @Test
    fun jsonHelper_serializesAndParsesComplaintsCorrectly() {
        val complaints = listOf("Display", "Battery", "Charging")
        val serialized = JsonHelper.serializeComplaints(complaints)
        val parsed = JsonHelper.parseComplaints(serialized)

        assertEquals(3, parsed.size)
        assertEquals("Display", parsed[0])
        assertEquals("Battery", parsed[1])
        assertEquals("Charging", parsed[2])
    }

    @Test
    fun repairComplaints_matchRequirements() {
        val requiredComplaints = listOf(
            "Display",
            "Battery",
            "Charging",
            "Speaker",
            "Microphone",
            "Camera",
            "Water Damage",
            "Software",
            "Network",
            "Other"
        )
        for (c in requiredComplaints) {
            assertTrue("Complaint option must exist: $c", InitialData.complaintOptions.contains(c))
        }
    }

    @Test
    fun initialOrdersAndRepairs_startEmpty() {
        assertTrue("Orders must start empty", InitialData.orders.isEmpty())
        assertTrue("Repairs must start empty", InitialData.repairs.isEmpty())
        val settingsMap = InitialData.defaultSettings.associate { it.key to it.value }
        assertEquals("9961454059", settingsMap["store_phone"])
        assertEquals("icaseedappal@gmail.com", settingsMap["store_email"])
    }
}
