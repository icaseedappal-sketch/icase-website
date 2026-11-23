package com.example.data

object InitialData {
    val categories = listOf(
        "All",
        "Cases",
        "Screen Protectors",
        "Fast Chargers",
        "Audio",
        "Cables",
        "Accessories"
    )

    val complaintOptions = listOf(
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

    val products = listOf(
        ProductEntity(
            id = "prod_1",
            name = "iCASE Armor Shockproof MagSafe Case",
            category = "Cases",
            description = "Military grade drop protection with built-in neodymium magnetic ring. Dual layer TPU + polycarbonate shell with reinforced shock air cushions on all corners.",
            price = 499.0,
            originalPrice = 799.0,
            imageUrl = "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 42,
            isOneHourEligible = true,
            isPopular = true,
            popularityRank = 1,
            colorOptionsJson = """[{"name":"Space Black","hex":"#2C2C2E"},{"name":"Navy Blue","hex":"#1B2A4A"},{"name":"Deep Purple","hex":"#6B5B95"},{"name":"Natural Titanium","hex":"#C4B8A5"}]"""
        ),
        ProductEntity(
            id = "prod_2",
            name = "Galaxy S24 Ultra Tough Armor Kickstand Case",
            category = "Cases",
            description = "Heavy duty rugged protective case with metal magnetic kickstand. Raised 1.5mm lips protect camera lenses and curved AMOLED display.",
            price = 449.0,
            originalPrice = 699.0,
            imageUrl = "https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 28,
            isOneHourEligible = true,
            isPopular = true,
            popularityRank = 2,
            colorOptionsJson = """[{"name":"Graphite Black","hex":"#1E293B"},{"name":"Forest Green","hex":"#166534"},{"name":"Royal Blue","hex":"#1D4ED8"}]"""
        ),
        ProductEntity(
            id = "prod_3",
            name = "9H Privacy Tempered Glass Shield",
            category = "Screen Protectors",
            description = "28-degree anti-peeping privacy filter with 9H diamond hardness. Oleophobic nano-coating repels fingerprints, oils and dust.",
            price = 249.0,
            originalPrice = 399.0,
            imageUrl = "https://images.unsplash.com/photo-1616440347437-b1c73416efc2?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 65,
            isOneHourEligible = true,
            isPopular = true,
            popularityRank = 3,
            colorOptionsJson = """[{"name":"Matte Privacy Tint","hex":"#111827"}]"""
        ),
        ProductEntity(
            id = "prod_4",
            name = "UltraClear HD Tempered Glass (Pack of 2)",
            category = "Screen Protectors",
            description = "99.9% transparency crystal glass with easy alignment tray. Edge-to-edge full adhesive curve fit with zero bubbles.",
            price = 299.0,
            originalPrice = 449.0,
            imageUrl = "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 34,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 7,
            colorOptionsJson = """[{"name":"Crystal Clear","hex":"#E2E8F0"}]"""
        ),
        ProductEntity(
            id = "prod_5",
            name = "65W GaN Dual USB-C Ultra Fast Charger",
            category = "Fast Chargers",
            description = "Next-gen Gallium Nitride tech. Charges smartphones from 0 to 65% in 25 minutes. Dual USB-C + USB-A multi-protocol power delivery.",
            price = 1299.0,
            originalPrice = 1799.0,
            imageUrl = "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 19,
            isOneHourEligible = true,
            isPopular = true,
            popularityRank = 4,
            colorOptionsJson = """[{"name":"Pearl White","hex":"#F1F5F9"},{"name":"Matte Black","hex":"#1E293B"}]"""
        ),
        ProductEntity(
            id = "prod_6",
            name = "30W Super Mini PD Wall Adapter",
            category = "Fast Chargers",
            description = "Ultra compact cube design for safe high-speed charging. Smart temperature control with over-current safety sensors.",
            price = 699.0,
            originalPrice = 999.0,
            imageUrl = "https://images.unsplash.com/photo-1622445262464-84b1456045b6?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 50,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 8,
            colorOptionsJson = """[{"name":"Frost White","hex":"#F8FAFC"},{"name":"Charcoal","hex":"#334155"}]"""
        ),
        ProductEntity(
            id = "prod_7",
            name = "iCASE Pro ANC Wireless Earbuds",
            category = "Audio",
            description = "Active Noise Cancellation up to 35dB with Transparency Mode. 36 hours total battery with USB-C wireless charging case and deep bass drivers.",
            price = 1899.0,
            originalPrice = 2499.0,
            imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 15,
            isOneHourEligible = false, // Not eligible for 1hr to demonstrate mixed eligibility
            isPopular = true,
            popularityRank = 5,
            colorOptionsJson = """[{"name":"Pearl White","hex":"#F1F5F9"},{"name":"Obsidian Black","hex":"#111827"},{"name":"Crimson Red","hex":"#E50914"}]"""
        ),
        ProductEntity(
            id = "prod_8",
            name = "Type-C Gaming Earphones with Quad Mic",
            category = "Audio",
            description = "Ultra-low latency gaming audio with detachable boom microphone and lossless Hi-Res DAC audio chip.",
            price = 399.0,
            originalPrice = 599.0,
            imageUrl = "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 30,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 9,
            colorOptionsJson = """[{"name":"Crimson Red","hex":"#E50914"},{"name":"Stealth Black","hex":"#18181B"}]"""
        ),
        ProductEntity(
            id = "prod_9",
            name = "100W Braided Nylon Fast Charging Cable (2M)",
            category = "Cables",
            description = "Heavy duty double-braided ballistic nylon cable tested for 30,000+ bends. Built-in E-marker chip supporting 5A 100W PD.",
            price = 299.0,
            originalPrice = 499.0,
            imageUrl = "https://images.unsplash.com/photo-1609592424109-dd9892f1b177?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 80,
            isOneHourEligible = true,
            isPopular = true,
            popularityRank = 6,
            colorOptionsJson = """[{"name":"Carbon Black","hex":"#1F2937"},{"name":"Racing Red","hex":"#DC2626"},{"name":"Navy Weave","hex":"#1E3A8A"}]"""
        ),
        ProductEntity(
            id = "prod_10",
            name = "3-in-1 Universal Fast Cable (Lightning/Type-C/Micro)",
            category = "Cables",
            description = "Universal charging solution for all phones and accessories with reinforced aluminum alloy connectors.",
            price = 349.0,
            originalPrice = 549.0,
            imageUrl = "https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 45,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 10,
            colorOptionsJson = """[{"name":"Space Gray","hex":"#475569"},{"name":"Crimson Red","hex":"#E50914"}]"""
        ),
        ProductEntity(
            id = "prod_11",
            name = "360° Magnetic Air Vent Car Mount",
            category = "Accessories",
            description = "6 powerful N52 rare-earth magnets ensure phone stays firmly anchored even over speed bumps and rough roads.",
            price = 499.0,
            originalPrice = 799.0,
            imageUrl = "https://images.unsplash.com/photo-1586105251261-72a756497a11?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 22,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 11,
            colorOptionsJson = """[{"name":"Anodized Black","hex":"#111827"},{"name":"Silver Alloy","hex":"#94A3B8"}]"""
        ),
        ProductEntity(
            id = "prod_12",
            name = "Foldable Heavy Aluminum Desk Stand",
            category = "Accessories",
            description = "Ergonomic multi-angle phone and tablet holder with anti-slip silicone cushions and cable pass-through.",
            price = 399.0,
            originalPrice = 599.0,
            imageUrl = "https://images.unsplash.com/photo-1586775490184-b79f0621891f?w=500&auto=format&fit=crop&q=80",
            inStock = true,
            stockCount = 37,
            isOneHourEligible = true,
            isPopular = false,
            popularityRank = 12,
            colorOptionsJson = """[{"name":"Space Gray","hex":"#475569"},{"name":"Natural Silver","hex":"#CBD5E1"}]"""
        )
    )

    val orders = listOf<OrderEntity>()

    val repairs = listOf<RepairBookingEntity>()

    val defaultSettings = listOf(
        AppSettingsEntity("one_hour_delivery_enabled", "true"),
        AppSettingsEntity("delivery_fee_standard", "50.00"),
        AppSettingsEntity("delivery_fee_express", "80.00"),
        AppSettingsEntity("free_delivery_threshold", "0.00"),
        AppSettingsEntity("admin_pin", "2026"),
        AppSettingsEntity("store_name", "iCASE Mobile Accessories & Repair"),
        AppSettingsEntity("store_phone", "9961454059"),
        AppSettingsEntity("store_whatsapp", "9961454059"),
        AppSettingsEntity("store_email", "icaseedappal@gmail.com"),
        AppSettingsEntity("store_address", "Main Road, Near Town Square, Edappal, Malappuram, Kerala"),
        AppSettingsEntity("store_hours", "9:00 AM - 10:00 PM (Daily)"),
        AppSettingsEntity("store_online", "true")
    )
}
