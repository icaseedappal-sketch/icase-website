package com.example.data

data class DeviceBrandEntry(
    val brand: String,
    val models: List<String>
)

data class DeviceModelOption(
    val brand: String,
    val model: String
) {
    val displayName: String get() = "$brand $model"
}

object DeviceCatalog {
    val majorBrands: List<String> = listOf(
        "Apple",
        "Samsung",
        "OnePlus",
        "Xiaomi",
        "Realme",
        "Vivo",
        "Oppo",
        "Motorola"
    )

    val catalog: List<DeviceBrandEntry> = listOf(
        DeviceBrandEntry(
            brand = "Apple",
            models = listOf(
                "iPhone 16 Pro Max", "iPhone 16 Pro", "iPhone 16 Plus", "iPhone 16", "iPhone 16e",
                "iPhone 15 Pro Max", "iPhone 15 Pro", "iPhone 15 Plus", "iPhone 15",
                "iPhone 14 Pro Max", "iPhone 14 Pro", "iPhone 14 Plus", "iPhone 14",
                "iPhone 13 Pro Max", "iPhone 13 Pro", "iPhone 13 Mini", "iPhone 13",
                "iPhone 12 Pro Max", "iPhone 12 Pro", "iPhone 12 Mini", "iPhone 12",
                "iPhone 11 Pro Max", "iPhone 11 Pro", "iPhone 11",
                "iPhone SE (3rd Gen)", "iPhone SE (2nd Gen)", "iPhone XS Max", "iPhone XR", "iPhone X"
            )
        ),
        DeviceBrandEntry(
            brand = "Samsung",
            models = listOf(
                "Galaxy S25 Ultra", "Galaxy S25+", "Galaxy S25",
                "Galaxy S24 Ultra", "Galaxy S24+", "Galaxy S24", "Galaxy S24 FE",
                "Galaxy S23 Ultra", "Galaxy S23+", "Galaxy S23", "Galaxy S23 FE",
                "Galaxy S22 Ultra", "Galaxy S22+", "Galaxy S22",
                "Galaxy S21 Ultra", "Galaxy S21 FE",
                "Galaxy Z Fold 6", "Galaxy Z Flip 6", "Galaxy Z Fold 5", "Galaxy Z Flip 5",
                "Galaxy A55 5G", "Galaxy A54 5G", "Galaxy A35 5G", "Galaxy A34 5G",
                "Galaxy A25 5G", "Galaxy A16 5G", "Galaxy A15 5G", "Galaxy A14 5G",
                "Galaxy M55 5G", "Galaxy M35 5G", "Galaxy M34 5G", "Galaxy M15 5G",
                "Galaxy F55 5G", "Galaxy F54 5G", "Galaxy F15 5G"
            )
        ),
        DeviceBrandEntry(
            brand = "OnePlus",
            models = listOf(
                "OnePlus 13", "OnePlus 13R",
                "OnePlus 12", "OnePlus 12R",
                "OnePlus 11", "OnePlus 11R",
                "OnePlus 10 Pro", "OnePlus 10T", "OnePlus 10R",
                "OnePlus 9 Pro", "OnePlus 9", "OnePlus 9R",
                "OnePlus Nord 4", "OnePlus Nord 3 5G", "OnePlus Nord CE 4",
                "OnePlus Nord CE 4 Lite", "OnePlus Nord CE 3", "OnePlus Nord CE 3 Lite",
                "OnePlus Open"
            )
        ),
        DeviceBrandEntry(
            brand = "Xiaomi",
            models = listOf(
                "Xiaomi 15", "Xiaomi 14 Ultra", "Xiaomi 14", "Xiaomi 14 CIVI",
                "Xiaomi 13 Pro", "Xiaomi 12 Pro", "Xiaomi 11i HyperCharge",
                "Redmi Note 14 Pro+ 5G", "Redmi Note 14 Pro 5G", "Redmi Note 14 5G",
                "Redmi Note 13 Pro+ 5G", "Redmi Note 13 Pro 5G", "Redmi Note 13 5G",
                "Redmi Note 12 Pro+ 5G", "Redmi Note 12 Pro", "Redmi Note 12",
                "Redmi Note 11 Pro+ 5G", "Redmi Note 11", "Redmi Note 10 Pro",
                "Redmi 13 5G", "Redmi 13C 5G", "Redmi 12 5G", "Redmi A3",
                "POCO X7 Pro", "POCO X6 Pro", "POCO F6", "POCO M6 Pro 5G"
            )
        ),
        DeviceBrandEntry(
            brand = "Realme",
            models = listOf(
                "Realme GT 7 Pro", "Realme GT 6", "Realme GT 6T", "Realme GT Neo 3",
                "Realme 14 Pro+", "Realme 14 Pro",
                "Realme 13 Pro+", "Realme 13 Pro", "Realme 13+ 5G",
                "Realme 12 Pro+", "Realme 12 Pro", "Realme 12+ 5G", "Realme 12x 5G",
                "Realme 11 Pro+", "Realme 11 Pro", "Realme 11x 5G",
                "Realme 10 Pro+", "Realme 10 Pro",
                "Realme Narzo 70 Pro 5G", "Realme Narzo 70x 5G", "Realme Narzo 60 Pro",
                "Realme P2 Pro 5G", "Realme P1 5G", "Realme C67 5G", "Realme C55"
            )
        ),
        DeviceBrandEntry(
            brand = "Vivo",
            models = listOf(
                "Vivo X200 Pro", "Vivo X200", "Vivo X100 Pro", "Vivo X100", "Vivo X90 Pro",
                "Vivo X Fold 3 Pro",
                "Vivo V40 Pro", "Vivo V40", "Vivo V40e",
                "Vivo V30 Pro", "Vivo V30", "Vivo V30e",
                "Vivo V29 Pro", "Vivo V29", "Vivo V27 Pro",
                "Vivo T3 Ultra", "Vivo T3 Pro 5G", "Vivo T3 5G", "Vivo T3x 5G",
                "Vivo T2 Pro 5G", "Vivo Y300 5G", "Vivo Y200e 5G", "Vivo Y58 5G", "Vivo Y28 5G"
            )
        ),
        DeviceBrandEntry(
            brand = "Oppo",
            models = listOf(
                "Oppo Find X8 Pro", "Oppo Find X8", "Oppo Find N3 Flip",
                "Oppo Reno 13 Pro 5G", "Oppo Reno 13 5G",
                "Oppo Reno 12 Pro 5G", "Oppo Reno 12 5G",
                "Oppo Reno 11 Pro 5G", "Oppo Reno 11 5G",
                "Oppo Reno 10 Pro+ 5G", "Oppo Reno 10 5G",
                "Oppo F27 Pro+ 5G", "Oppo F27 5G", "Oppo F25 Pro 5G", "Oppo F23 5G",
                "Oppo K12x 5G", "Oppo A3 Pro 5G", "Oppo A79 5G", "Oppo A78 5G", "Oppo A59 5G"
            )
        ),
        DeviceBrandEntry(
            brand = "Motorola",
            models = listOf(
                "Motorola Razr 50 Ultra", "Motorola Razr 50", "Motorola Razr 40 Ultra",
                "Motorola Edge 50 Ultra", "Motorola Edge 50 Pro", "Motorola Edge 50 Fusion", "Motorola Edge 50 Neo",
                "Motorola Edge 40 Pro", "Motorola Edge 40", "Motorola Edge 40 Neo",
                "Moto G85 5G", "Moto G84 5G", "Moto G64 5G", "Moto G54 5G",
                "Moto G45 5G", "Moto G35 5G", "Moto G34 5G", "Moto G24 Power",
                "Moto E14"
            )
        )
    )

    val allModels: List<DeviceModelOption> = catalog.flatMap { entry ->
        entry.models.map { model -> DeviceModelOption(brand = entry.brand, model = model) }
    }

    fun modelsForBrand(brand: String): List<String> {
        if (brand.isBlank()) return emptyList()
        return catalog.firstOrNull { it.brand.equals(brand.trim(), ignoreCase = true) }?.models ?: emptyList()
    }

    fun searchModels(brandFilter: String, query: String): List<DeviceModelOption> {
        val q = query.trim().lowercase()
        val source = if (brandFilter.isBlank()) {
            allModels
        } else {
            modelsForBrand(brandFilter).map { DeviceModelOption(brand = brandFilter, model = it) }
        }
        if (q.isBlank()) return source
        return source.filter { option ->
            option.model.lowercase().contains(q) ||
                option.brand.lowercase().contains(q) ||
                option.displayName.lowercase().contains(q)
        }
    }
}
