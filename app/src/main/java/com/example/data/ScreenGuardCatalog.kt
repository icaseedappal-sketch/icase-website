package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.roundToInt

enum class ScreenGuardOfferType {
    NONE,
    FLAT_OFF,
    PERCENT_OFF,
    SPECIAL_PRICE;

    companion object {
        fun fromWire(raw: String?): ScreenGuardOfferType = when (raw?.trim()?.uppercase()) {
            "FLAT_OFF" -> FLAT_OFF
            "PERCENT_OFF" -> PERCENT_OFF
            "SPECIAL_PRICE" -> SPECIAL_PRICE
            else -> NONE
        }
    }
}

data class ScreenGuardOffer(
    val active: Boolean = false,
    val type: ScreenGuardOfferType = ScreenGuardOfferType.NONE,
    val value: Double = 0.0,
    val badgeText: String = ""
) {
    fun resolvedBadgeText(): String {
        if (!active || type == ScreenGuardOfferType.NONE) return ""
        val custom = badgeText.trim()
        if (custom.isNotEmpty()) return custom
        return when (type) {
            ScreenGuardOfferType.FLAT_OFF -> "₹${value.roundToInt().coerceAtLeast(0)} OFF"
            ScreenGuardOfferType.PERCENT_OFF -> "${value.roundToInt().coerceIn(0, 99)}% OFF"
            ScreenGuardOfferType.SPECIAL_PRICE -> "SPECIAL OFFER"
            ScreenGuardOfferType.NONE -> ""
        }
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("active", active)
        put("type", type.name)
        put("value", value)
        put("badgeText", badgeText)
    }

    companion object {
        fun fromJson(obj: JSONObject?): ScreenGuardOffer {
            if (obj == null) return ScreenGuardOffer()
            return ScreenGuardOffer(
                active = obj.optBoolean("active", false),
                type = ScreenGuardOfferType.fromWire(obj.optString("type", "NONE")),
                value = obj.optDouble("value", 0.0).takeIf { !it.isNaN() && it >= 0.0 } ?: 0.0,
                badgeText = obj.optString("badgeText", "")
            )
        }
    }
}

data class GlassTypeAdminConfig(
    val basePrice: Double,
    val enabled: Boolean = true,
    val deleted: Boolean = false,
    val offer: ScreenGuardOffer = ScreenGuardOffer(),
    val helperNote: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("basePrice", basePrice)
        put("enabled", enabled)
        put("deleted", deleted)
        put("offer", offer.toJson())
        if (helperNote.isNotBlank()) put("helperNote", helperNote)
    }

    companion object {
        fun fromJson(obj: JSONObject?, fallbackPrice: Double): GlassTypeAdminConfig {
            if (obj == null) return GlassTypeAdminConfig(basePrice = fallbackPrice)
            return GlassTypeAdminConfig(
                basePrice = obj.optDouble("basePrice", fallbackPrice).takeIf { !it.isNaN() && it >= 0.0 } ?: fallbackPrice,
                enabled = obj.optBoolean("enabled", true),
                deleted = obj.optBoolean("deleted", false),
                offer = ScreenGuardOffer.fromJson(obj.optJSONObject("offer")),
                helperNote = obj.optString("helperNote", "")
            )
        }
    }
}

data class ModelAdminConfig(
    val enabled: Boolean = true,
    val deleted: Boolean = false,
    val customRates: Map<String, Double> = emptyMap(),
    val offer: ScreenGuardOffer = ScreenGuardOffer()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("enabled", enabled)
        put("deleted", deleted)
        val ratesObj = JSONObject()
        customRates.forEach { (k, v) -> ratesObj.put(k, v) }
        put("customRates", ratesObj)
        put("offer", offer.toJson())
    }

    companion object {
        fun fromJson(obj: JSONObject?): ModelAdminConfig {
            if (obj == null) return ModelAdminConfig()
            val ratesMap = linkedMapOf<String, Double>()
            val ratesObj = obj.optJSONObject("customRates")
            if (ratesObj != null) {
                val keys = ratesObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val v = ratesObj.optDouble(key, -1.0)
                    if (!v.isNaN() && v > 0.0) {
                        ratesMap[key] = v
                    }
                }
            }
            return ModelAdminConfig(
                enabled = obj.optBoolean("enabled", true),
                deleted = obj.optBoolean("deleted", false),
                customRates = ratesMap,
                offer = ScreenGuardOffer.fromJson(obj.optJSONObject("offer"))
            )
        }
    }
}

data class CameraLensModelConfig(
    val enabled: Boolean = true,
    val deleted: Boolean = false,
    val customPrice: Double? = null,
    val offer: ScreenGuardOffer = ScreenGuardOffer()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("enabled", enabled)
        put("deleted", deleted)
        if (customPrice != null && customPrice > 0.0) {
            put("customPrice", customPrice)
        }
        put("offer", offer.toJson())
    }

    companion object {
        fun fromJson(obj: JSONObject?): CameraLensModelConfig {
            if (obj == null) return CameraLensModelConfig()
            val rawPrice = if (obj.has("customPrice") && !obj.isNull("customPrice")) {
                obj.optDouble("customPrice", -1.0).takeIf { !it.isNaN() && it > 0.0 }
            } else {
                null
            }
            return CameraLensModelConfig(
                enabled = obj.optBoolean("enabled", true),
                deleted = obj.optBoolean("deleted", false),
                customPrice = rawPrice,
                offer = ScreenGuardOffer.fromJson(obj.optJSONObject("offer"))
            )
        }
    }
}

data class PouchColorOption(
    val name: String,
    val hex: String = "#1E293B",
    val imageUrl: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name.trim())
        put("hex", JsonHelper.normalizeHexColor(hex))
        if (imageUrl.isNotBlank()) {
            put("imageUrl", imageUrl.trim())
        }
    }

    companion object {
        fun fromJson(obj: JSONObject?): PouchColorOption? {
            if (obj == null) return null
            val cleanName = obj.optString("name", "").trim()
            if (cleanName.isEmpty()) return null
            return PouchColorOption(
                name = cleanName,
                hex = JsonHelper.normalizeHexColor(obj.optString("hex", "#1E293B")),
                imageUrl = obj.optString("imageUrl", "").trim()
            )
        }
    }
}

data class PouchAdminItem(
    val id: String,
    val name: String,
    val description: String = "",
    val basePrice: Double = 299.0,
    val compatibleBrands: List<String> = listOf("iPhone", "Samsung S Series"),
    val enabled: Boolean = true,
    val deleted: Boolean = false,
    val offer: ScreenGuardOffer = ScreenGuardOffer(),
    val imageUrl: String = "",
    val colorOptions: List<PouchColorOption> = emptyList(),
    val showOnHomePage: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("description", description)
        put("basePrice", basePrice)
        put("compatibleBrands", JSONArray(compatibleBrands))
        put("enabled", enabled)
        put("deleted", deleted)
        put("offer", offer.toJson())
        if (imageUrl.isNotBlank()) {
            put("imageUrl", imageUrl.trim())
        }
        put("showOnHomePage", showOnHomePage)
        val colorsArr = JSONArray()
        colorOptions.forEach { c ->
            if (c.name.isNotBlank()) {
                colorsArr.put(c.toJson())
            }
        }
        put("colorOptions", colorsArr)
    }

    companion object {
        fun fromJson(obj: JSONObject?): PouchAdminItem? {
            if (obj == null) return null
            val id = obj.optString("id", "").trim()
            val name = obj.optString("name", "").trim()
            if (id.isEmpty() || name.isEmpty()) return null
            val brandsArr = obj.optJSONArray("compatibleBrands")
            val parsedBrands = mutableListOf<String>()
            if (brandsArr != null) {
                for (i in 0 until brandsArr.length()) {
                    val b = brandsArr.optString(i, "").trim()
                    if (b.isNotEmpty()) parsedBrands.add(b)
                }
            }
            val colorsArr = obj.optJSONArray("colorOptions")
            val parsedColors = mutableListOf<PouchColorOption>()
            val seenColorNames = hashSetOf<String>()
            if (colorsArr != null) {
                for (i in 0 until colorsArr.length()) {
                    val opt = PouchColorOption.fromJson(colorsArr.optJSONObject(i)) ?: continue
                    val key = opt.name.lowercase()
                    if (seenColorNames.add(key)) {
                        parsedColors.add(opt)
                    }
                }
            }
            return PouchAdminItem(
                id = id,
                name = name,
                description = obj.optString("description", ""),
                basePrice = obj.optDouble("basePrice", 299.0).takeIf { !it.isNaN() && it >= 0.0 } ?: 299.0,
                compatibleBrands = parsedBrands.ifEmpty { listOf("iPhone", "Samsung S Series") },
                enabled = obj.optBoolean("enabled", true),
                deleted = obj.optBoolean("deleted", false),
                offer = ScreenGuardOffer.fromJson(obj.optJSONObject("offer")),
                imageUrl = obj.optString("imageUrl", "").trim(),
                colorOptions = parsedColors,
                showOnHomePage = obj.optBoolean("showOnHomePage", false)
            )
        }
    }
}

data class ResolvedGuardPricing(
    val basePrice: Double,
    val finalPrice: Double,
    val offerBadge: String = "",
    val hasOffer: Boolean = false
)

data class ScreenGuardAdminConfig(
    val glassTypeConfigs: Map<String, GlassTypeAdminConfig> = defaultGlassTypeConfigs(),
    val modelConfigs: Map<String, ModelAdminConfig> = emptyMap(),
    val customModelsByBrand: Map<String, List<String>> = emptyMap(),
    val cameraLensDefault: GlassTypeAdminConfig = GlassTypeAdminConfig(basePrice = DEFAULT_CAMERA_LENS_PRICE),
    val cameraLensModelConfigs: Map<String, CameraLensModelConfig> = emptyMap(),
    val customCameraLensModelsByBrand: Map<String, List<String>> = emptyMap(),
    val comboEnabled: Boolean = true,
    val comboOffer: ScreenGuardOffer = defaultComboOffer(),
    val pouchItems: List<PouchAdminItem> = defaultPouchItems(),
    val pouchModelConfigs: Map<String, ModelAdminConfig> = emptyMap(),
    val customPouchModelsByBrand: Map<String, List<String>> = emptyMap(),
    val temperedGlassImageUrl: String = ScreenGuardCatalog.TEMPERED_GLASS_IMAGE_URL,
    val cameraLensImageUrl: String = ScreenGuardCatalog.CAMERA_LENS_IMAGE_URL
) {
    fun toJsonString(): String {
        val root = JSONObject()
        val glassObj = JSONObject()
        glassTypeConfigs.forEach { (type, cfg) ->
            glassObj.put(type, cfg.toJson())
        }
        root.put("glassTypeConfigs", glassObj)

        val modelsObj = JSONObject()
        modelConfigs.forEach { (key, cfg) ->
            modelsObj.put(key, cfg.toJson())
        }
        root.put("modelConfigs", modelsObj)

        root.put("customModelsByBrand", stringListMapToJson(customModelsByBrand))
        root.put("cameraLensDefault", cameraLensDefault.toJson())

        val camObj = JSONObject()
        cameraLensModelConfigs.forEach { (key, cfg) ->
            camObj.put(key, cfg.toJson())
        }
        root.put("cameraLensModelConfigs", camObj)
        root.put("customCameraLensModelsByBrand", stringListMapToJson(customCameraLensModelsByBrand))

        root.put("comboEnabled", comboEnabled)
        root.put("comboOffer", comboOffer.toJson())

        val pouchArr = JSONArray()
        pouchItems.forEach { pouchArr.put(it.toJson()) }
        root.put("pouchItems", pouchArr)

        val pouchModelObj = JSONObject()
        pouchModelConfigs.forEach { (key, cfg) ->
            pouchModelObj.put(key, cfg.toJson())
        }
        root.put("pouchModelConfigs", pouchModelObj)
        root.put("customPouchModelsByBrand", stringListMapToJson(customPouchModelsByBrand))
        root.put("temperedGlassImageUrl", temperedGlassImageUrl)
        root.put("cameraLensImageUrl", cameraLensImageUrl)

        return root.toString()
    }

    companion object {
        const val DEFAULT_CAMERA_LENS_PRICE = 249.0

        val defaultPricesByGlassType: Map<String, Double> = mapOf(
            "Normal 5D" to 199.0,
            "Matte" to 299.0,
            "Privacy" to 349.0,
            "Borderless Tempered Glass" to 399.0,
            "360 Privacy" to 499.0
        )

        fun defaultGlassTypeConfigs(): Map<String, GlassTypeAdminConfig> =
            defaultPricesByGlassType.mapValues { (_, price) -> GlassTypeAdminConfig(basePrice = price) }

        fun defaultComboOffer(): ScreenGuardOffer = ScreenGuardOffer(
            active = true,
            type = ScreenGuardOfferType.FLAT_OFF,
            value = 50.0,
            badgeText = "COMBO SAVE"
        )

        fun defaultPouchItems(): List<PouchAdminItem> = listOf(
            PouchAdminItem(
                id = "pouch_magsafe_clear",
                name = "MagSafe Crystal Clear Pouch",
                description = "Anti-yellowing clear shell with built-in magnetic ring",
                basePrice = 399.0,
                compatibleBrands = listOf("iPhone", "Samsung S Series")
            ),
            PouchAdminItem(
                id = "pouch_matte_armor",
                name = "Matte Translucent Shockproof Pouch",
                description = "Anti-fingerprint frosted back with raised camera bump guard",
                basePrice = 349.0,
                compatibleBrands = listOf("iPhone", "Samsung S Series")
            ),
            PouchAdminItem(
                id = "pouch_silicone_grip",
                name = "Soft Liquid Silicone Velvet Pouch",
                description = "Microfiber interior lining with silky soft-touch grip",
                basePrice = 299.0,
                compatibleBrands = listOf("iPhone", "Samsung S Series")
            )
        )

        fun modelKey(brand: String, model: String): String = "${brand.trim()}::${model.trim()}"

        private fun stringListMapToJson(map: Map<String, List<String>>): JSONObject {
            val obj = JSONObject()
            map.forEach { (brand, models) ->
                val clean = models.map { it.trim() }.filter { it.isNotBlank() }.distinct()
                if (clean.isNotEmpty()) {
                    obj.put(brand.trim(), JSONArray(clean))
                }
            }
            return obj
        }

        private fun jsonToStringListMap(obj: JSONObject?): Map<String, List<String>> {
            if (obj == null) return emptyMap()
            val out = linkedMapOf<String, List<String>>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val brand = keys.next()
                val arr = obj.optJSONArray(brand) ?: continue
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val m = arr.optString(i, "").trim()
                    if (m.isNotEmpty() && !list.contains(m)) list.add(m)
                }
                if (list.isNotEmpty()) out[brand] = list
            }
            return out
        }

        fun fromJsonString(raw: String?): ScreenGuardAdminConfig {
            if (raw.isNullOrBlank()) return ScreenGuardAdminConfig()
            return try {
                val root = JSONObject(raw)
                val defaults = defaultGlassTypeConfigs().toMutableMap()
                val glassObj = root.optJSONObject("glassTypeConfigs")
                if (glassObj != null) {
                    val keys = glassObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val fallback = defaultPricesByGlassType[k] ?: 199.0
                        defaults[k] = GlassTypeAdminConfig.fromJson(glassObj.optJSONObject(k), fallback)
                    }
                }

                val modelMap = linkedMapOf<String, ModelAdminConfig>()
                val modelsObj = root.optJSONObject("modelConfigs")
                if (modelsObj != null) {
                    val keys = modelsObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        modelMap[k] = ModelAdminConfig.fromJson(modelsObj.optJSONObject(k))
                    }
                }

                val customModels = jsonToStringListMap(root.optJSONObject("customModelsByBrand"))

                val camDefault = GlassTypeAdminConfig.fromJson(
                    root.optJSONObject("cameraLensDefault"),
                    DEFAULT_CAMERA_LENS_PRICE
                )

                val camMap = linkedMapOf<String, CameraLensModelConfig>()
                val camObj = root.optJSONObject("cameraLensModelConfigs")
                if (camObj != null) {
                    val keys = camObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        camMap[k] = CameraLensModelConfig.fromJson(camObj.optJSONObject(k))
                    }
                }

                val customCamModels = jsonToStringListMap(root.optJSONObject("customCameraLensModelsByBrand"))
                val comboEnabled = root.optBoolean("comboEnabled", true)
                val comboOffer = if (root.has("comboOffer")) {
                    ScreenGuardOffer.fromJson(root.optJSONObject("comboOffer"))
                } else {
                    defaultComboOffer()
                }

                val pouches = if (root.has("pouchItems")) {
                    val arr = root.optJSONArray("pouchItems")
                    val parsed = mutableListOf<PouchAdminItem>()
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            PouchAdminItem.fromJson(arr.optJSONObject(i))?.let { parsed.add(it) }
                        }
                    }
                    parsed
                } else {
                    defaultPouchItems()
                }

                val pouchModelMap = linkedMapOf<String, ModelAdminConfig>()
                val pouchModelObj = root.optJSONObject("pouchModelConfigs")
                if (pouchModelObj != null) {
                    val keys = pouchModelObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        pouchModelMap[k] = ModelAdminConfig.fromJson(pouchModelObj.optJSONObject(k))
                    }
                }
                val customPouchModels = jsonToStringListMap(root.optJSONObject("customPouchModelsByBrand"))
                val temperedGlassImg = root.optString("temperedGlassImageUrl", "").trim()
                    .ifBlank { ScreenGuardCatalog.TEMPERED_GLASS_IMAGE_URL }
                val cameraLensImg = root.optString("cameraLensImageUrl", "").trim()
                    .ifBlank { ScreenGuardCatalog.CAMERA_LENS_IMAGE_URL }

                ScreenGuardAdminConfig(
                    glassTypeConfigs = defaults,
                    modelConfigs = modelMap,
                    customModelsByBrand = customModels,
                    cameraLensDefault = camDefault,
                    cameraLensModelConfigs = camMap,
                    customCameraLensModelsByBrand = customCamModels,
                    comboEnabled = comboEnabled,
                    comboOffer = comboOffer,
                    pouchItems = pouches,
                    pouchModelConfigs = pouchModelMap,
                    customPouchModelsByBrand = customPouchModels,
                    temperedGlassImageUrl = temperedGlassImg,
                    cameraLensImageUrl = cameraLensImg
                )
            } catch (_: Exception) {
                ScreenGuardAdminConfig()
            }
        }
    }
}

object ScreenGuardCatalog {
    const val OTHER_MODEL_OPTION = "Other Model (Type Manually)"
    const val OTHER_ANDROID_POUCHES_OPTION = "Other Android Models"
    const val SETTINGS_KEY = "screen_guard_admin_config"
    const val DELETED_PRODUCTS_KEY = "deleted_product_ids"
    const val CATALOG_SEEDED_KEY = "catalog_seeded_v1"

    const val TEMPERED_GLASS_IMAGE_URL =
        "https://images.unsplash.com/photo-1616440347437-b1c73416efc2?w=500&auto=format&fit=crop&q=80"
    const val CAMERA_LENS_IMAGE_URL =
        "https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=500&auto=format&fit=crop&q=80"
    const val POUCH_IMAGE_URL =
        "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500&auto=format&fit=crop&q=80"

    val presetPouchColors: List<PouchColorOption> = listOf(
        PouchColorOption("Black", "#111827"),
        PouchColorOption("Midnight Blue", "#1E3A8A"),
        PouchColorOption("Deep Green", "#166534"),
        PouchColorOption("Lavender", "#A78BFA"),
        PouchColorOption("Red", "#DC2626"),
        PouchColorOption("Clear/Transparent", "#E2E8F0"),
        PouchColorOption("Pink", "#EC4899"),
        PouchColorOption("Grey", "#64748B"),
        PouchColorOption("Brown", "#78350F")
    )

    /**
     * Clean, lazy-loaded placeholder demo video URL for Tempered Glass & Camera Lens Protection.
     * Video playback initializes only when the user opens the preview modal.
     */
    const val SAMPLE_GUARD_VIDEO_URL =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

    val iphoneGlassTypes: List<String> = listOf(
        "Privacy",
        "Matte",
        "Borderless Tempered Glass",
        "360 Privacy",
        "Normal 5D"
    )

    val samsungFlatSGlassTypes: List<String> = listOf(
        "Privacy",
        "Matte"
    )

    val standardGlassTypes: List<String> = listOf(
        "Normal 5D"
    )

    val allGlassTypes: List<String> = listOf(
        "Privacy",
        "Matte",
        "Borderless Tempered Glass",
        "360 Privacy",
        "Normal 5D"
    )

    fun defaultGlassTypeHelperNote(glassType: String): String = when (glassType) {
        "Privacy" -> "Blocks side viewing so only you see your screen clearly."
        "Matte" -> "Reduces glare, reflections & fingerprint smudges for smooth touch."
        "Borderless Tempered Glass" -> "Edge-to-edge full clear glass with zero black borders."
        "360 Privacy" -> "All-round 4-way privacy shield from every viewing angle."
        "Normal 5D" -> "Standard 9H full-coverage curved-edge clear screen protection."
        else -> "Precision 9H tempered glass screen protection."
    }

    fun glassTypeHelperNote(glassType: String, config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): String {
        val customNote = config.glassTypeConfigs[glassType]?.helperNote?.trim().orEmpty()
        return customNote.ifEmpty { defaultGlassTypeHelperNote(glassType) }
    }

    fun allGlassTypes(config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): List<String> {
        val extras = config.glassTypeConfigs.keys.filter { !allGlassTypes.contains(it) }
        return allGlassTypes + extras
    }

    val brands: List<String> = listOf(
        "iPhone",
        "Redmi/Xiaomi",
        "Samsung",
        "Oppo/Realme",
        "Vivo/iQOO",
        "OnePlus",
        "Nothing"
    )

    val allSamsungSSeriesModels: List<String> = listOf(
        "Galaxy S25 Ultra",
        "Galaxy S25+",
        "Galaxy S25",
        "Galaxy S25 FE",
        "Galaxy S24 Ultra",
        "Galaxy S24+",
        "Galaxy S24",
        "Galaxy S24 FE",
        "Galaxy S23 Ultra",
        "Galaxy S23+",
        "Galaxy S23",
        "Galaxy S23 FE",
        "Galaxy S22 Ultra",
        "Galaxy S22+",
        "Galaxy S22",
        "Galaxy S21 Ultra",
        "Galaxy S21+",
        "Galaxy S21",
        "Galaxy S21 FE",
        "Galaxy S20 FE / S20"
    )

    /**
     * Samsung Tempered Glass dropdown list strictly EXCLUDES curved-display S series models
     * (S20 series, S21 Ultra, S22 Ultra, S23 Ultra). Flat S series + non-S Samsung remain.
     */
    val modelsByBrand: Map<String, List<String>> = mapOf(
        "iPhone" to listOf(
            "iPhone 16 Pro Max",
            "iPhone 16 Pro",
            "iPhone 16 Plus",
            "iPhone 16",
            "iPhone 16e",
            "iPhone 15 Pro Max",
            "iPhone 15 Pro",
            "iPhone 15 Plus",
            "iPhone 15",
            "iPhone 14 Pro Max",
            "iPhone 14 Pro",
            "iPhone 14 Plus",
            "iPhone 14",
            "iPhone 13 Pro Max",
            "iPhone 13 Pro",
            "iPhone 13 Mini",
            "iPhone 13",
            "iPhone 12 Pro Max",
            "iPhone 12 Pro",
            "iPhone 12 Mini",
            "iPhone 12",
            "iPhone 11 Pro Max",
            "iPhone 11 Pro",
            "iPhone 11",
            "iPhone SE (2020 / 2022)",
            "iPhone XS Max",
            "iPhone XR / X / XS"
        ),
        "Redmi/Xiaomi" to listOf(
            "Xiaomi 15",
            "Xiaomi 14 Ultra",
            "Xiaomi 14",
            "Xiaomi 14 CIVI",
            "Xiaomi 13 Pro",
            "Xiaomi 12 Pro",
            "Xiaomi 11i / 11i HyperCharge",
            "Redmi Note 14 Pro+ 5G",
            "Redmi Note 14 Pro 5G",
            "Redmi Note 14 5G",
            "Redmi Note 13 Pro+ 5G",
            "Redmi Note 13 Pro 5G",
            "Redmi Note 13 5G",
            "Redmi Note 12 Pro+ 5G",
            "Redmi Note 12 Pro",
            "Redmi Note 12 4G / 5G",
            "Redmi Note 11 Pro+ / Note 11",
            "Redmi Note 10 Pro / Note 10",
            "Redmi 14C 5G",
            "Redmi 13 5G / 13C",
            "Redmi 12 5G",
            "Redmi A3 / A3x",
            "POCO X7 Pro 5G",
            "POCO X6 Pro / X6 5G",
            "POCO F6 5G",
            "POCO M6 Pro 5G"
        ),
        "Samsung" to listOf(
            "Galaxy S25 Ultra",
            "Galaxy S25+",
            "Galaxy S25",
            "Galaxy S25 FE",
            "Galaxy S24 Ultra",
            "Galaxy S24+",
            "Galaxy S24",
            "Galaxy S24 FE",
            "Galaxy S23+",
            "Galaxy S23",
            "Galaxy S23 FE",
            "Galaxy S22+",
            "Galaxy S22",
            "Galaxy S21+",
            "Galaxy S21",
            "Galaxy S21 FE",
            "Galaxy A55 5G",
            "Galaxy A54 5G",
            "Galaxy A35 5G",
            "Galaxy A34 5G",
            "Galaxy A25 5G",
            "Galaxy A16 5G",
            "Galaxy A15 5G",
            "Galaxy A14 5G",
            "Galaxy M55 5G",
            "Galaxy M35 5G",
            "Galaxy M34 5G",
            "Galaxy M15 5G",
            "Galaxy F55 5G",
            "Galaxy F54 5G",
            "Galaxy F15 5G",
            "Galaxy Z Fold 6 / Flip 6"
        ),
        "Oppo/Realme" to listOf(
            "Oppo Find X8 Pro",
            "Oppo Find X8",
            "Oppo Reno 13 Pro 5G",
            "Oppo Reno 13 5G",
            "Oppo Reno 12 Pro 5G",
            "Oppo Reno 12 5G",
            "Oppo Reno 11 Pro 5G",
            "Oppo Reno 11 5G",
            "Oppo Reno 10 Pro+ / 10",
            "Oppo F27 Pro+ 5G",
            "Oppo F27 5G",
            "Oppo F25 Pro 5G",
            "Oppo F23 5G",
            "Oppo K12x 5G",
            "Oppo A3 Pro 5G",
            "Oppo A79 5G / A78 5G",
            "Oppo A59 5G",
            "Realme GT 7 Pro",
            "Realme GT 6 / GT 6T",
            "Realme 14 Pro+ / 14 Pro",
            "Realme 13 Pro+ / 13 Pro",
            "Realme 13+ 5G / 13 5G",
            "Realme 12 Pro+ / 12 Pro",
            "Realme 12+ / 12x 5G",
            "Realme 11 Pro+ / 11 Pro",
            "Realme Narzo 70 Pro / 70x",
            "Realme Narzo 60 Pro",
            "Realme P2 Pro / P1 5G",
            "Realme C67 5G / C55"
        ),
        "Vivo/iQOO" to listOf(
            "Vivo X200 Pro",
            "Vivo X200",
            "Vivo X100 Pro",
            "Vivo X100",
            "Vivo X90 Pro",
            "Vivo V40 Pro",
            "Vivo V40 / V40e",
            "Vivo V30 Pro",
            "Vivo V30 / V30e",
            "Vivo V29 Pro / V29",
            "Vivo V27 Pro / V27",
            "Vivo T3 Ultra 5G",
            "Vivo T3 Pro / T3 / T3x 5G",
            "Vivo T2 Pro 5G",
            "Vivo Y300 5G",
            "Vivo Y200e 5G",
            "Vivo Y58 5G / Y28 5G",
            "iQOO 13 5G",
            "iQOO 12 5G",
            "iQOO Neo 9 Pro",
            "iQOO Neo 7 Pro",
            "iQOO Z9s Pro / Z9s 5G",
            "iQOO Z9 / Z9x 5G",
            "iQOO Z7 Pro 5G"
        ),
        "OnePlus" to listOf(
            "OnePlus 13",
            "OnePlus 13R",
            "OnePlus 12",
            "OnePlus 12R",
            "OnePlus 11",
            "OnePlus 11R",
            "OnePlus 10 Pro",
            "OnePlus 10T",
            "OnePlus 10R",
            "OnePlus 9 Pro",
            "OnePlus 9",
            "OnePlus 9R",
            "OnePlus Nord 4",
            "OnePlus Nord 3 5G",
            "OnePlus Nord CE 4",
            "OnePlus Nord CE 4 Lite",
            "OnePlus Nord CE 3",
            "OnePlus Nord CE 3 Lite",
            "OnePlus Open"
        ),
        "Nothing" to listOf(
            "Nothing Phone (3a) Pro",
            "Nothing Phone (3a)",
            "Nothing Phone (2a) Plus",
            "Nothing Phone (2a)",
            "Nothing Phone (2)",
            "Nothing Phone (1)",
            "CMF Phone 1"
        )
    )

    val cameraLensBrands: List<String> = listOf(
        "iPhone",
        "Samsung S Series"
    )

    val cameraLensModelsByBrand: Map<String, List<String>> = mapOf(
        "iPhone" to (modelsByBrand["iPhone"] ?: emptyList()),
        "Samsung S Series" to allSamsungSSeriesModels
    )

    val pouchBrands: List<String> = listOf(
        "iPhone",
        "Samsung S Series"
    )

    val pouchModelsByBrand: Map<String, List<String>> = mapOf(
        "iPhone" to (modelsByBrand["iPhone"] ?: emptyList()),
        "Samsung S Series" to allSamsungSSeriesModels
    )

    fun modelsForBrand(
        brand: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): List<String> {
        val base = modelsByBrand[brand].orEmpty()
        val custom = config.customModelsByBrand[brand].orEmpty()
        return (base + custom).distinct()
    }

    fun cameraLensModelsForBrand(
        brand: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): List<String> {
        val base = cameraLensModelsByBrand[brand].orEmpty()
        val custom = config.customCameraLensModelsByBrand[brand].orEmpty()
        return (base + custom).distinct()
    }

    fun pouchModelsForBrand(
        brand: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): List<String> {
        val base = pouchModelsByBrand[brand].orEmpty()
        val custom = config.customPouchModelsByBrand[brand].orEmpty()
        return (base + custom).distinct()
    }

    fun isGlassTypeAvailable(glassType: String, config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): Boolean {
        val cfg = config.glassTypeConfigs[glassType] ?: return true
        return !cfg.deleted && cfg.enabled
    }

    fun isTemperedGlassModelAvailable(
        brand: String,
        model: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): Boolean {
        if (brand.equals("CUSTOM", ignoreCase = true) || model == OTHER_MODEL_OPTION) return true
        val validCatalogModel = modelsForBrand(brand, config).contains(model)
        if (!validCatalogModel) return false
        val cfg = config.modelConfigs[ScreenGuardAdminConfig.modelKey(brand, model)] ?: return true
        return !cfg.deleted && cfg.enabled
    }

    fun isCameraLensSectionAvailable(config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): Boolean {
        return !config.cameraLensDefault.deleted && config.cameraLensDefault.enabled
    }

    fun isCameraLensModelAvailable(
        brand: String,
        model: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): Boolean {
        if (!isCameraLensSectionAvailable(config)) return false
        val validCatalogModel = cameraLensModelsForBrand(brand, config).contains(model)
        if (!validCatalogModel) return false
        val cfg = config.cameraLensModelConfigs[ScreenGuardAdminConfig.modelKey(brand, model)] ?: return true
        return !cfg.deleted && cfg.enabled
    }

    fun isPouchModelAvailable(
        brand: String,
        model: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): Boolean {
        val validCatalogModel = pouchModelsForBrand(brand, config).contains(model)
        if (!validCatalogModel) return false
        val cfg = config.pouchModelConfigs[ScreenGuardAdminConfig.modelKey(brand, model)] ?: return true
        return !cfg.deleted && cfg.enabled
    }

    fun activePouchesForBrand(
        brand: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): List<PouchAdminItem> {
        return config.pouchItems.filter { item ->
            !item.deleted && item.enabled &&
                (item.compatibleBrands.isEmpty() || item.compatibleBrands.any { it.equals(brand, ignoreCase = true) })
        }
    }

    fun homeFeaturedPouches(
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): List<PouchAdminItem> {
        return config.pouchItems.filter { item ->
            item.showOnHomePage && item.enabled && !item.deleted
        }
    }

    fun activeBrands(config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): List<String> {
        return brands.filter { brand ->
            modelsForBrand(brand, config).any { model -> isTemperedGlassModelAvailable(brand, model, config) }
        }
    }

    fun activeCameraLensBrands(config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): List<String> {
        if (!isCameraLensSectionAvailable(config)) return emptyList()
        return cameraLensBrands.filter { brand ->
            cameraLensModelsForBrand(brand, config).any { model ->
                isCameraLensModelAvailable(brand, model, config)
            }
        }
    }

    fun searchGroupedModels(
        brandFilter: String?,
        query: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): Map<String, List<String>> {
        val q = query.trim().lowercase()
        val targetBrands = if (!brandFilter.isNullOrBlank()) {
            if (brands.contains(brandFilter)) listOf(brandFilter) else emptyList()
        } else {
            brands
        }
        val grouped = linkedMapOf<String, List<String>>()
        for (brand in targetBrands) {
            val matches = modelsForBrand(brand, config).filter { model ->
                isTemperedGlassModelAvailable(brand, model, config) && (
                    q.isEmpty() ||
                        model.lowercase().contains(q) ||
                        brand.lowercase().contains(q) ||
                        "$brand $model".lowercase().contains(q)
                    )
            }
            if (matches.isNotEmpty()) {
                grouped[brand] = matches
            }
        }
        return grouped
    }

    fun searchCameraLensModels(
        brandFilter: String?,
        query: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): Map<String, List<String>> {
        if (!isCameraLensSectionAvailable(config)) return emptyMap()
        val q = query.trim().lowercase()
        val targetBrands = if (!brandFilter.isNullOrBlank()) {
            if (cameraLensBrands.contains(brandFilter)) listOf(brandFilter) else emptyList()
        } else {
            cameraLensBrands
        }
        val grouped = linkedMapOf<String, List<String>>()
        for (brand in targetBrands) {
            val matches = cameraLensModelsForBrand(brand, config).filter { model ->
                isCameraLensModelAvailable(brand, model, config) && (
                    q.isEmpty() ||
                        model.lowercase().contains(q) ||
                        brand.lowercase().contains(q) ||
                        "$brand $model".lowercase().contains(q)
                    )
            }
            if (matches.isNotEmpty()) {
                grouped[brand] = matches
            }
        }
        return grouped
    }

    fun searchPouchModels(
        brandFilter: String,
        query: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): List<String> {
        val q = query.trim().lowercase()
        return pouchModelsForBrand(brandFilter, config).filter { model ->
            isPouchModelAvailable(brandFilter, model, config) && (
                q.isEmpty() || model.lowercase().contains(q)
                )
        }
    }

    private val popularGlassPresets: List<Pair<String, String>> = listOf(
        "iPhone" to "iPhone 16 Pro Max",
        "iPhone" to "iPhone 16 Pro",
        "iPhone" to "iPhone 15 Pro",
        "iPhone" to "iPhone 15",
        "iPhone" to "iPhone 14",
        "Samsung" to "Galaxy S25 Ultra",
        "Samsung" to "Galaxy S24 Ultra",
        "Samsung" to "Galaxy S24",
        "Samsung" to "Galaxy A55 5G",
        "Redmi/Xiaomi" to "Redmi Note 14 Pro 5G",
        "Redmi/Xiaomi" to "Redmi Note 13 Pro 5G",
        "Redmi/Xiaomi" to "Xiaomi 14",
        "Oppo/Realme" to "Oppo Reno 12 Pro 5G",
        "Oppo/Realme" to "Realme GT 6 / GT 6T",
        "Vivo/iQOO" to "Vivo V40 Pro",
        "Vivo/iQOO" to "iQOO 12 5G",
        "OnePlus" to "OnePlus 13",
        "OnePlus" to "OnePlus 12R",
        "OnePlus" to "OnePlus Nord 4",
        "Nothing" to "Nothing Phone (3a)",
        "Nothing" to "Nothing Phone (2a)"
    )

    private val popularAllBrandGlassHighlight: List<Pair<String, String>> = listOf(
        "iPhone" to "iPhone 16 Pro Max",
        "iPhone" to "iPhone 15",
        "Samsung" to "Galaxy S24 Ultra",
        "Redmi/Xiaomi" to "Redmi Note 13 Pro 5G",
        "OnePlus" to "OnePlus 12R",
        "Nothing" to "Nothing Phone (2a)"
    )

    fun popularGlassModels(
        brandFilter: String = "",
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): List<Pair<String, String>> {
        val cleanBrand = brandFilter.trim()
        val candidates = if (cleanBrand.isBlank()) {
            popularAllBrandGlassHighlight
        } else {
            popularGlassPresets.filter { it.first.equals(cleanBrand, ignoreCase = true) }
        }
        return candidates.filter { (brand, model) ->
            isTemperedGlassModelAvailable(brand, model, config) &&
                modelsForBrand(brand, config).contains(model)
        }.take(6)
    }

    private val popularCameraLensPresets: List<Pair<String, String>> = listOf(
        "iPhone" to "iPhone 16 Pro Max",
        "iPhone" to "iPhone 16 Pro",
        "iPhone" to "iPhone 15 Pro",
        "iPhone" to "iPhone 15",
        "Samsung S Series" to "Galaxy S25 Ultra",
        "Samsung S Series" to "Galaxy S24 Ultra",
        "Samsung S Series" to "Galaxy S23 Ultra",
        "Samsung S Series" to "Galaxy S24"
    )

    fun popularCameraLensModels(
        brandFilter: String = "",
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): List<Pair<String, String>> {
        val cleanBrand = brandFilter.trim()
        val candidates = if (cleanBrand.isBlank()) {
            listOf(
                "iPhone" to "iPhone 16 Pro Max",
                "iPhone" to "iPhone 15 Pro",
                "iPhone" to "iPhone 15",
                "Samsung S Series" to "Galaxy S25 Ultra",
                "Samsung S Series" to "Galaxy S24 Ultra",
                "Samsung S Series" to "Galaxy S23 Ultra"
            )
        } else {
            popularCameraLensPresets.filter { it.first.equals(cleanBrand, ignoreCase = true) }
        }
        return candidates.filter { (brand, model) ->
            isCameraLensModelAvailable(brand, model, config) &&
                cameraLensModelsForBrand(brand, config).contains(model)
        }.take(6)
    }

    fun buildTemperedGlassWhatsAppMessage(
        brand: String,
        model: String,
        glassType: String,
        finalPrice: Double,
        installationOption: String = "Shop Installation"
    ): String {
        val brandLine = if (brand.isBlank() || brand.equals("CUSTOM", ignoreCase = true)) {
            "Custom / Other Brand"
        } else {
            brand
        }
        return buildString {
            append("Hi iCASE Edappal! I want to ask / order Tempered Glass:\n")
            append("• Brand: $brandLine\n")
            append("• Model: $model\n")
            append("• Glass Type: $glassType\n")
            append("• Price: ₹${finalPrice.roundToInt()}\n")
            append("• Preference: $installationOption")
        }
    }

    fun buildCameraLensWhatsAppMessage(
        brand: String,
        model: String,
        finalPrice: Double,
        installationOption: String = "Shop Installation"
    ): String = buildString {
        append("Hi iCASE Edappal! I want to ask / order Camera Lens Protection:\n")
        append("• Brand: $brand\n")
        append("• Model: $model\n")
        append("• Product: 9H Sapphire Camera Lens Protection\n")
        append("• Price: ₹${finalPrice.roundToInt()}\n")
        append("• Preference: $installationOption")
    }

    /**
     * Samsung (Flat S Series only):
     * Exclude curved-display models (S20 series, S21 Ultra, S22 Ultra, S23 Ultra).
     * Flat S series models: S21, S21+, S21 FE, S22, S22+, S23, S23+, S23 FE,
     * S24, S24+, S24 Ultra, S24 FE, S25, S25+, S25 Ultra, S25 FE.
     */
    fun isSamsungFlatSSeries(brand: String?, model: String?): Boolean {
        val cleanBrand = brand?.trim().orEmpty()
        val cleanModel = model?.trim().orEmpty()
        if (!cleanBrand.equals("Samsung", ignoreCase = true)) return false
        if (cleanModel.isBlank() || cleanModel == OTHER_MODEL_OPTION) return false

        val lower = cleanModel.lowercase()
        if (Regex("""\bs20(\+|\b)""").containsMatchIn(lower)) return false
        if (Regex("""\bs2[123]\s+ultra\b""").containsMatchIn(lower)) return false

        return Regex("""\b(?:galaxy\s+)?s2[1-5](\+|\b)""").containsMatchIn(lower)
    }

    /**
     * Dynamic glass-type filter chips filtered against Admin active/deleted configuration:
     * - iPhone: Privacy, Matte, Borderless Tempered Glass, 360 Privacy, Normal 5D (+ Admin custom types if added)
     * - Samsung Flat S Series: Privacy, Matte
     * - All other brands & models: Normal 5D
     */
    fun glassTypesForDevice(
        brand: String?,
        model: String?,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): List<String> {
        val cleanBrand = brand?.trim().orEmpty()
        val cleanModel = model?.trim().orEmpty()
        if (cleanModel.isBlank()) return emptyList()
        if (cleanModel != OTHER_MODEL_OPTION && !cleanBrand.equals("CUSTOM", ignoreCase = true) && cleanBrand.isNotBlank()) {
            if (!isTemperedGlassModelAvailable(cleanBrand, cleanModel, config)) {
                return emptyList()
            }
        }
        val customTypes = config.glassTypeConfigs.keys.filter { !allGlassTypes.contains(it) }
        val baseTypes = when {
            cleanModel == OTHER_MODEL_OPTION || cleanBrand.equals("CUSTOM", ignoreCase = true) -> standardGlassTypes
            cleanBrand.equals("iPhone", ignoreCase = true) -> iphoneGlassTypes + customTypes
            isSamsungFlatSSeries(cleanBrand, cleanModel) -> samsungFlatSGlassTypes
            else -> standardGlassTypes
        }
        return baseTypes.filter { isGlassTypeAvailable(it, config) }
    }

    private fun applyOffer(basePrice: Double, offer: ScreenGuardOffer?): ResolvedGuardPricing {
        val cleanBase = max(0.0, basePrice)
        if (offer == null || !offer.active || offer.type == ScreenGuardOfferType.NONE) {
            return ResolvedGuardPricing(basePrice = cleanBase, finalPrice = cleanBase)
        }
        val finalPrice = when (offer.type) {
            ScreenGuardOfferType.FLAT_OFF -> max(0.0, cleanBase - offer.value).roundToInt().toDouble()
            ScreenGuardOfferType.PERCENT_OFF -> {
                val pct = offer.value.coerceIn(0.0, 99.0)
                max(0.0, (cleanBase * (1.0 - pct / 100.0)).roundToInt().toDouble())
            }
            ScreenGuardOfferType.SPECIAL_PRICE -> max(0.0, offer.value).roundToInt().toDouble()
            ScreenGuardOfferType.NONE -> cleanBase
        }
        val badge = offer.resolvedBadgeText()
        return ResolvedGuardPricing(
            basePrice = cleanBase,
            finalPrice = finalPrice,
            offerBadge = badge,
            hasOffer = badge.isNotBlank() || finalPrice < cleanBase
        )
    }

    fun resolveTemperedGlassPrice(
        brand: String,
        model: String,
        glassType: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): ResolvedGuardPricing {
        val fallback = ScreenGuardAdminConfig.defaultPricesByGlassType[glassType] ?: 199.0
        val glassCfg = config.glassTypeConfigs[glassType] ?: GlassTypeAdminConfig(basePrice = fallback)
        val modelCfg = if (brand.isNotBlank() && !brand.equals("CUSTOM", ignoreCase = true) && model != OTHER_MODEL_OPTION) {
            config.modelConfigs[ScreenGuardAdminConfig.modelKey(brand, model)]
        } else {
            null
        }
        val basePrice = modelCfg?.customRates?.get(glassType)?.takeIf { it > 0.0 } ?: glassCfg.basePrice
        val activeOffer = when {
            modelCfg?.offer?.active == true && modelCfg.offer.type != ScreenGuardOfferType.NONE -> modelCfg.offer
            glassCfg.offer.active && glassCfg.offer.type != ScreenGuardOfferType.NONE -> glassCfg.offer
            else -> null
        }
        return applyOffer(basePrice, activeOffer)
    }

    fun resolveCameraLensPrice(
        brand: String,
        model: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): ResolvedGuardPricing {
        val modelCfg = config.cameraLensModelConfigs[ScreenGuardAdminConfig.modelKey(brand, model)]
        val basePrice = modelCfg?.customPrice?.takeIf { it > 0.0 } ?: config.cameraLensDefault.basePrice
        val activeOffer = when {
            modelCfg?.offer?.active == true && modelCfg.offer.type != ScreenGuardOfferType.NONE -> modelCfg.offer
            config.cameraLensDefault.offer.active && config.cameraLensDefault.offer.type != ScreenGuardOfferType.NONE -> config.cameraLensDefault.offer
            else -> null
        }
        return applyOffer(basePrice, activeOffer)
    }

    fun resolveComboPrice(
        glassBrand: String,
        glassModel: String,
        glassType: String,
        cameraBrand: String,
        cameraModel: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): ResolvedGuardPricing {
        val glassPricing = resolveTemperedGlassPrice(glassBrand, glassModel, glassType, config)
        val camPricing = resolveCameraLensPrice(cameraBrand, cameraModel, config)
        val combinedBase = glassPricing.finalPrice + camPricing.finalPrice
        return if (config.comboEnabled && config.comboOffer.active && config.comboOffer.type != ScreenGuardOfferType.NONE) {
            applyOffer(combinedBase, config.comboOffer)
        } else {
            ResolvedGuardPricing(basePrice = combinedBase, finalPrice = combinedBase)
        }
    }

    fun resolvePouchPrice(pouch: PouchAdminItem): ResolvedGuardPricing {
        return applyOffer(pouch.basePrice, pouch.offer)
    }

    fun resolvePouchSelectedColor(
        pouch: PouchAdminItem,
        selectedColor: String = ""
    ): PouchColorOption? {
        if (pouch.colorOptions.isEmpty()) return null
        val clean = selectedColor.trim()
        if (clean.isNotEmpty()) {
            pouch.colorOptions.firstOrNull { it.name.equals(clean, ignoreCase = true) }?.let { return it }
        }
        return pouch.colorOptions.firstOrNull()
    }

    fun resolveTemperedGlassImageUrl(config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): String {
        return config.temperedGlassImageUrl.trim().ifBlank { TEMPERED_GLASS_IMAGE_URL }
    }

    fun resolveCameraLensImageUrl(config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): String {
        return config.cameraLensImageUrl.trim().ifBlank { CAMERA_LENS_IMAGE_URL }
    }

    fun startingTemperedGlassPrice(config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): Double {
        val activePrices = allGlassTypes(config)
            .filter { isGlassTypeAvailable(it, config) }
            .map { resolveTemperedGlassPrice("", "", it, config).finalPrice }
        return activePrices.minOrNull() ?: 199.0
    }

    fun startingCameraLensPrice(config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()): Double {
        val defaultPrice = resolveCameraLensPrice("", "", config).finalPrice
        val activeCustomPrices = cameraLensBrands.flatMap { brand ->
            cameraLensModelsForBrand(brand, config)
                .filter { isCameraLensModelAvailable(brand, it, config) }
                .map { resolveCameraLensPrice(brand, it, config).finalPrice }
        }
        return (activeCustomPrices + defaultPrice).minOrNull() ?: defaultPrice
    }

    fun isDeviceEligibleForCameraLens(
        brand: String?,
        model: String?,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig()
    ): Boolean {
        if (!isCameraLensSectionAvailable(config)) return false
        val cleanBrand = brand?.trim().orEmpty()
        val cleanModel = model?.trim().orEmpty()
        if (cleanModel.isBlank() || cleanModel == OTHER_MODEL_OPTION) return false
        if (cleanBrand.equals("iPhone", ignoreCase = true)) {
            return isCameraLensModelAvailable("iPhone", cleanModel, config) ||
                cleanModel.contains("iphone", ignoreCase = true)
        }
        if (cleanBrand.equals("Samsung", ignoreCase = true)) {
            val isSSeries = allSamsungSSeriesModels.any { it.equals(cleanModel, ignoreCase = true) } ||
                Regex("""\b(?:galaxy\s+)?s2[0-5]\b""", RegexOption.IGNORE_CASE).containsMatchIn(cleanModel) ||
                config.customCameraLensModelsByBrand["Samsung S Series"].orEmpty().any { it.equals(cleanModel, ignoreCase = true) }
            if (!isSSeries) return false
            val cfg = config.cameraLensModelConfigs[ScreenGuardAdminConfig.modelKey("Samsung S Series", cleanModel)]
            return cfg == null || (!cfg.deleted && cfg.enabled)
        }
        if (cleanBrand.equals("Samsung S Series", ignoreCase = true)) {
            return isCameraLensModelAvailable("Samsung S Series", cleanModel, config)
        }
        // Custom manual entry
        val lower = cleanModel.lowercase()
        return lower.contains("iphone") || Regex("""\b(?:galaxy\s+)?s2[0-5]\b""").containsMatchIn(lower)
    }

    fun resolvePouchImageUrl(
        pouch: PouchAdminItem,
        selectedColor: String = ""
    ): String {
        val colorObj = resolvePouchSelectedColor(pouch, selectedColor)
        val perColorImg = colorObj?.imageUrl?.trim().orEmpty()
        if (perColorImg.isNotEmpty()) return perColorImg
        val baseImg = pouch.imageUrl.trim()
        if (baseImg.isNotEmpty()) return baseImg
        return POUCH_IMAGE_URL
    }

    private fun safeToken(raw: String): String =
        raw.trim().replace("::", ":")

    private fun installationNoteSuffix(installationOption: String): String {
        val clean = installationOption.trim()
        return if (clean.isNotEmpty()) " • Fulfillment: $clean" else ""
    }

    private fun extractInstallationOption(description: String): String {
        val marker = " • Fulfillment: "
        val idx = description.indexOf(marker)
        if (idx < 0) return ""
        val after = description.substring(idx + marker.length)
        val nextBullet = after.indexOf(" • ")
        return (if (nextBullet >= 0) after.substring(0, nextBullet) else after).trim()
    }

    fun buildTemperedGlassCartProduct(
        brand: String,
        model: String,
        glassType: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig(),
        installationOption: String = ""
    ): ProductEntity {
        val cleanBrand = brand.trim().ifBlank { "CUSTOM" }
        val cleanModel = model.trim()
        val pricing = resolveTemperedGlassPrice(cleanBrand, cleanModel, glassType, config)
        val offerSuffix = if (pricing.offerBadge.isNotBlank()) " • Offer: ${pricing.offerBadge}" else ""
        val installSuffix = installationNoteSuffix(installationOption)
        return ProductEntity(
            id = "sg::${safeToken(cleanBrand)}::${safeToken(cleanModel)}::${safeToken(glassType)}",
            name = "$cleanModel — $glassType Tempered Glass",
            category = "Tempered Glass",
            description = "Precision $glassType Tempered Glass for ${if (cleanBrand == "CUSTOM") cleanModel else "$cleanBrand $cleanModel"}$installSuffix$offerSuffix",
            price = pricing.finalPrice,
            originalPrice = if (pricing.basePrice > pricing.finalPrice) pricing.basePrice else pricing.finalPrice,
            imageUrl = resolveTemperedGlassImageUrl(config),
            inStock = true,
            stockCount = 99,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 99,
            colorOptionsJson = "[]"
        )
    }

    fun buildCameraLensCartProduct(
        brand: String,
        model: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig(),
        installationOption: String = ""
    ): ProductEntity {
        val cleanBrand = brand.trim()
        val cleanModel = model.trim()
        val pricing = resolveCameraLensPrice(cleanBrand, cleanModel, config)
        val offerSuffix = if (pricing.offerBadge.isNotBlank()) " • Offer: ${pricing.offerBadge}" else ""
        val installSuffix = installationNoteSuffix(installationOption)
        return ProductEntity(
            id = "cl::${safeToken(cleanBrand)}::${safeToken(cleanModel)}",
            name = "$cleanModel — Camera Lens Protection",
            category = "Camera Lens Protection",
            description = "9H Sapphire Tempered Camera Lens Ring Protection for $cleanModel$installSuffix$offerSuffix",
            price = pricing.finalPrice,
            originalPrice = if (pricing.basePrice > pricing.finalPrice) pricing.basePrice else pricing.finalPrice,
            imageUrl = resolveCameraLensImageUrl(config),
            inStock = true,
            stockCount = 99,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 99,
            colorOptionsJson = "[]"
        )
    }

    fun buildComboCartProduct(
        glassBrand: String,
        glassModel: String,
        glassType: String,
        cameraBrand: String,
        cameraModel: String,
        config: ScreenGuardAdminConfig = ScreenGuardAdminConfig(),
        installationOption: String = ""
    ): ProductEntity {
        val cleanGBrand = glassBrand.trim().ifBlank { "CUSTOM" }
        val cleanGModel = glassModel.trim()
        val cleanCBrand = cameraBrand.trim()
        val cleanCModel = cameraModel.trim()
        val pricing = resolveComboPrice(cleanGBrand, cleanGModel, glassType, cleanCBrand, cleanCModel, config)
        val offerSuffix = if (pricing.offerBadge.isNotBlank()) " • Offer: ${pricing.offerBadge}" else ""
        val installSuffix = installationNoteSuffix(installationOption)
        return ProductEntity(
            id = "combo::${safeToken(cleanGBrand)}::${safeToken(cleanGModel)}::${safeToken(glassType)}::${safeToken(cleanCBrand)}::${safeToken(cleanCModel)}",
            name = "$cleanGModel ($glassType) + $cleanCModel Lens Combo",
            category = "Tempered Glass + Lens Combo",
            description = "Tempered Glass ($cleanGModel • $glassType) + Camera Lens Protection ($cleanCModel)$installSuffix$offerSuffix",
            price = pricing.finalPrice,
            originalPrice = if (pricing.basePrice > pricing.finalPrice) pricing.basePrice else pricing.finalPrice,
            imageUrl = resolveTemperedGlassImageUrl(config),
            inStock = true,
            stockCount = 99,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 99,
            colorOptionsJson = "[]"
        )
    }

    fun buildPouchCartProduct(
        brand: String,
        model: String,
        pouch: PouchAdminItem,
        selectedColor: String = ""
    ): ProductEntity {
        val cleanBrand = brand.trim()
        val cleanModel = model.trim()
        val pricing = resolvePouchPrice(pouch)
        val matchedColor = resolvePouchSelectedColor(pouch, selectedColor)?.name
            ?: selectedColor.trim()
        val resolvedImage = resolvePouchImageUrl(pouch, matchedColor)
        val offerSuffix = if (pricing.offerBadge.isNotBlank()) " • Offer: ${pricing.offerBadge}" else ""
        val colorSuffix = if (matchedColor.isNotBlank()) " • Colour: $matchedColor" else ""
        val descBody = pouch.description.trim().ifBlank { "Precision protective fit pouch" }
        val baseId = "pouch::${safeToken(cleanBrand)}::${safeToken(cleanModel)}::${safeToken(pouch.id)}"
        val fullId = if (matchedColor.isNotBlank()) "$baseId::${safeToken(matchedColor)}" else baseId
        val fullName = if (matchedColor.isNotBlank()) {
            "$cleanModel — ${pouch.name} ($matchedColor)"
        } else {
            "$cleanModel — ${pouch.name}"
        }
        val colorJson = JsonHelper.serializeColorOptions(
            pouch.colorOptions.map { ProductColorOption(name = it.name, hex = it.hex) }
        )
        return ProductEntity(
            id = fullId,
            name = fullName,
            category = "Pouches",
            description = "$descBody for $cleanModel$colorSuffix$offerSuffix",
            price = pricing.finalPrice,
            originalPrice = if (pricing.basePrice > pricing.finalPrice) pricing.basePrice else pricing.finalPrice,
            imageUrl = resolvedImage,
            inStock = true,
            stockCount = 99,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 99,
            colorOptionsJson = colorJson
        )
    }

    fun repriceOrNullCartProduct(
        product: ProductEntity,
        config: ScreenGuardAdminConfig
    ): ProductEntity? {
        val installOpt = extractInstallationOption(product.description)
        if (product.id.startsWith("sg::")) {
            val parts = product.id.split("::")
            if (parts.size < 4) return product
            val brand = parts[1]
            val model = parts[2]
            val glassType = parts[3]
            if (!isGlassTypeAvailable(glassType, config)) return null
            if (!brand.equals("CUSTOM", ignoreCase = true) && !isTemperedGlassModelAvailable(brand, model, config)) {
                return null
            }
            if (!brand.equals("CUSTOM", ignoreCase = true)) {
                val allowedTypes = glassTypesForDevice(brand, model, config)
                if (!allowedTypes.contains(glassType)) return null
            }
            return buildTemperedGlassCartProduct(brand, model, glassType, config, installOpt)
        }
        if (product.id.startsWith("cl::")) {
            val parts = product.id.split("::")
            if (parts.size < 3) return product
            val brand = parts[1]
            val model = parts[2]
            if (!isCameraLensModelAvailable(brand, model, config)) return null
            return buildCameraLensCartProduct(brand, model, config, installOpt)
        }
        if (product.id.startsWith("combo::")) {
            val parts = product.id.split("::")
            if (parts.size < 6) return product
            val gBrand = parts[1]
            val gModel = parts[2]
            val glassType = parts[3]
            val cBrand = parts[4]
            val cModel = parts[5]
            if (!config.comboEnabled) return null
            if (!isGlassTypeAvailable(glassType, config)) return null
            if (!gBrand.equals("CUSTOM", ignoreCase = true)) {
                if (!isTemperedGlassModelAvailable(gBrand, gModel, config)) return null
                if (!glassTypesForDevice(gBrand, gModel, config).contains(glassType)) return null
            }
            if (!isCameraLensModelAvailable(cBrand, cModel, config)) return null
            return buildComboCartProduct(gBrand, gModel, glassType, cBrand, cModel, config, installOpt)
        }
        if (product.id.startsWith("pouch::")) {
            val parts = product.id.split("::")
            if (parts.size < 4) return product
            val brand = parts[1]
            val model = parts[2]
            val pouchId = parts[3]
            val selectedColor = parts.getOrNull(4).orEmpty().trim()
            if (!isPouchModelAvailable(brand, model, config)) return null
            val pouch = config.pouchItems.firstOrNull {
                it.id == pouchId && !it.deleted && it.enabled &&
                    (it.compatibleBrands.isEmpty() || it.compatibleBrands.any { b -> b.equals(brand, ignoreCase = true) })
            } ?: return null
            return buildPouchCartProduct(brand, model, pouch, selectedColor)
        }
        return product
    }

    fun generateDefaultCodeExport(
        products: List<ProductEntity>,
        config: ScreenGuardAdminConfig,
        storeName: String = "iCASE Mobile Accessories & Repair",
        storePhone: String = "9961454059",
        storeWhatsapp: String = "9961454059",
        storeEmail: String = "icaseedappal@gmail.com",
        storeAddress: String = "Main Road, Near Town Square, Edappal, Malappuram, Kerala",
        storeHours: String = "9:00 AM - 10:00 PM (Daily)",
        isStoreOnline: Boolean = true,
        isOneHourEnabled: Boolean = true,
        standardDeliveryFee: Double = 50.0,
        expressDeliveryFee: Double = 80.0,
        freeDeliveryThreshold: Double = 0.0,
        adminPin: String = "2026",
        adminRecoveryCode: String = "ICASE2026"
    ): String {
        val productsArr = JSONArray()
        products.forEach { prod ->
            val pObj = JSONObject()
            pObj.put("id", prod.id)
            pObj.put("name", prod.name)
            pObj.put("category", prod.category)
            pObj.put("description", prod.description)
            pObj.put("price", prod.price)
            pObj.put("originalPrice", prod.originalPrice)
            pObj.put("imageUrl", prod.imageUrl)
            pObj.put("inStock", prod.inStock)
            pObj.put("stockCount", prod.stockCount)
            pObj.put("isOneHourEligible", prod.isOneHourEligible)
            pObj.put("isPopular", prod.isPopular)
            pObj.put("popularityRank", prod.popularityRank)
            val colorArr = JSONArray()
            JsonHelper.parseColorOptions(prod.colorOptionsJson).forEach { opt ->
                val cObj = JSONObject()
                cObj.put("name", opt.name)
                cObj.put("hex", opt.hex)
                colorArr.put(cObj)
            }
            pObj.put("colorOptions", colorArr)
            productsArr.put(pObj)
        }

        val settingsObj = JSONObject()
        settingsObj.put("storeName", storeName)
        settingsObj.put("storePhone", storePhone)
        settingsObj.put("storeWhatsapp", storeWhatsapp)
        settingsObj.put("storeEmail", storeEmail)
        settingsObj.put("storeAddress", storeAddress)
        settingsObj.put("storeHours", storeHours)
        settingsObj.put("isStoreOnline", isStoreOnline)
        settingsObj.put("isOneHourEnabled", isOneHourEnabled)
        settingsObj.put("standardDeliveryFee", standardDeliveryFee)
        settingsObj.put("expressDeliveryFee", expressDeliveryFee)
        settingsObj.put("freeDeliveryThreshold", freeDeliveryThreshold)
        settingsObj.put("adminPin", adminPin.ifBlank { "2026" })
        settingsObj.put("adminRecoveryCode", adminRecoveryCode.ifBlank { "ICASE2026" })

        val guardObj = JSONObject(config.toJsonString())

        return buildString {
            append("// ============================================================\n")
            append("// iCASE DEFAULT CATALOG & CONFIG EXPORT (NO-DB LIVE SITE SYNC)\n")
            append("// Paste INITIAL_PRODUCTS, DEFAULT_SETTINGS & BAKED_SCREEN_GUARD_CONFIG\n")
            append("// into companion-website/src/data/constants.js and public/js/data.js,\n")
            append("// then commit & push to GitHub.\n")
            append("// ============================================================\n\n")
            append("export const INITIAL_PRODUCTS = ")
            append(productsArr.toString(2))
            append(";\n\n")
            append("export const DEFAULT_SETTINGS = ")
            append(settingsObj.toString(2))
            append(";\n\n")
            append("export const BAKED_SCREEN_GUARD_CONFIG = ")
            append(guardObj.toString(2))
            append(";\n")
        }
    }
}
