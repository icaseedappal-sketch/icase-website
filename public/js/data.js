export const CATEGORIES = [
  'All',
  'Cases',
  'Screen Protectors',
  'Fast Chargers',
  'Audio',
  'Cables',
  'Accessories'
];

export const COMPLAINT_OPTIONS = [
  'Display',
  'Battery',
  'Charging',
  'Speaker',
  'Microphone',
  'Camera',
  'Water Damage',
  'Software',
  'Network',
  'Other'
];

export const ORDER_STATUSES = [
  'Order Received',
  'Processing',
  'Out for Delivery',
  'Delivered'
];

export const REPAIR_STATUSES = [
  'Booking Received',
  'Pickup Scheduled',
  'Device Received',
  'Repairing',
  'Repair Completed',
  'Out for Delivery',
  'Delivered'
];

export const TIME_SLOTS = [
  '10:00 AM - 12:00 PM',
  '12:00 PM - 02:00 PM',
  '02:00 PM - 05:00 PM',
  '05:00 PM - 08:00 PM'
];

export const DEFAULT_SETTINGS = {
  one_hour_delivery_enabled: 'true',
  delivery_fee_standard: '50.00',
  delivery_fee_express: '80.00',
  free_delivery_threshold: '0.00',
  admin_pin: '2026',
  store_name: 'iCASE Mobile Accessories & Repair',
  store_phone: '9961454059',
  store_whatsapp: '9961454059',
  store_email: 'icaseedappal@gmail.com',
  store_address: 'Main Road, Near Town Square, Edappal, Malappuram, Kerala',
  store_hours: '9:00 AM - 10:00 PM (Daily)',
  store_online: 'true'
};

export const INITIAL_PRODUCTS = [
  {
    id: 'prod_1',
    name: 'iCASE Armor Shockproof MagSafe Case',
    category: 'Cases',
    description:
      'Military grade drop protection with built-in neodymium magnetic ring. Dual layer TPU + polycarbonate shell with reinforced shock air cushions on all corners.',
    price: 499,
    originalPrice: 799,
    imageUrl:
      'https://images.unsplash.com/photo-1580910051074-3eb694886505?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 42,
    isOneHourEligible: true,
    isPopular: true,
    popularityRank: 1,
    colorOptionsJson:
      '[{"name":"Space Black","hex":"#2C2C2E"},{"name":"Navy Blue","hex":"#1B2A4A"},{"name":"Deep Purple","hex":"#6B5B95"},{"name":"Natural Titanium","hex":"#C4B8A5"}]'
  },
  {
    id: 'prod_2',
    name: 'Galaxy S24 Ultra Tough Armor Kickstand Case',
    category: 'Cases',
    description:
      'Heavy duty rugged protective case with metal magnetic kickstand. Raised 1.5mm lips protect camera lenses and curved AMOLED display.',
    price: 449,
    originalPrice: 699,
    imageUrl:
      'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 28,
    isOneHourEligible: true,
    isPopular: true,
    popularityRank: 2,
    colorOptionsJson:
      '[{"name":"Graphite Black","hex":"#1E293B"},{"name":"Forest Green","hex":"#166534"},{"name":"Royal Blue","hex":"#1D4ED8"}]'
  },
  {
    id: 'prod_3',
    name: '9H Privacy Tempered Glass Shield',
    category: 'Screen Protectors',
    description:
      '28-degree anti-peeping privacy filter with 9H diamond hardness. Oleophobic nano-coating repels fingerprints, oils and dust.',
    price: 249,
    originalPrice: 399,
    imageUrl:
      'https://images.unsplash.com/photo-1616440347437-b1c73416efc2?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 65,
    isOneHourEligible: true,
    isPopular: true,
    popularityRank: 3,
    colorOptionsJson: '[{"name":"Matte Privacy Tint","hex":"#111827"}]'
  },
  {
    id: 'prod_4',
    name: 'UltraClear HD Tempered Glass (Pack of 2)',
    category: 'Screen Protectors',
    description:
      '99.9% transparency crystal glass with easy alignment tray. Edge-to-edge full adhesive curve fit with zero bubbles.',
    price: 299,
    originalPrice: 449,
    imageUrl:
      'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 34,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 7,
    colorOptionsJson: '[{"name":"Crystal Clear","hex":"#E2E8F0"}]'
  },
  {
    id: 'prod_5',
    name: '65W GaN Dual USB-C Ultra Fast Charger',
    category: 'Fast Chargers',
    description:
      'Next-gen Gallium Nitride tech. Charges smartphones from 0 to 65% in 25 minutes. Dual USB-C + USB-A multi-protocol power delivery.',
    price: 1299,
    originalPrice: 1799,
    imageUrl:
      'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 19,
    isOneHourEligible: true,
    isPopular: true,
    popularityRank: 4,
    colorOptionsJson:
      '[{"name":"Pearl White","hex":"#F1F5F9"},{"name":"Matte Black","hex":"#1E293B"}]'
  },
  {
    id: 'prod_6',
    name: '30W Super Mini PD Wall Adapter',
    category: 'Fast Chargers',
    description:
      'Ultra compact cube design for safe high-speed charging. Smart temperature control with over-current safety sensors.',
    price: 699,
    originalPrice: 999,
    imageUrl:
      'https://images.unsplash.com/photo-1622445262464-84b1456045b6?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 50,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 8,
    colorOptionsJson:
      '[{"name":"Frost White","hex":"#F8FAFC"},{"name":"Charcoal","hex":"#334155"}]'
  },
  {
    id: 'prod_7',
    name: 'iCASE Pro ANC Wireless Earbuds',
    category: 'Audio',
    description:
      'Active Noise Cancellation up to 35dB with Transparency Mode. 36 hours total battery with USB-C wireless charging case and deep bass drivers.',
    price: 1899,
    originalPrice: 2499,
    imageUrl:
      'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 15,
    isOneHourEligible: false,
    isPopular: true,
    popularityRank: 5,
    colorOptionsJson:
      '[{"name":"Pearl White","hex":"#F1F5F9"},{"name":"Obsidian Black","hex":"#111827"},{"name":"Crimson Red","hex":"#E50914"}]'
  },
  {
    id: 'prod_8',
    name: 'Type-C Gaming Earphones with Quad Mic',
    category: 'Audio',
    description:
      'Ultra-low latency gaming audio with detachable boom microphone and lossless Hi-Res DAC audio chip.',
    price: 399,
    originalPrice: 599,
    imageUrl:
      'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 30,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 9,
    colorOptionsJson:
      '[{"name":"Crimson Red","hex":"#E50914"},{"name":"Stealth Black","hex":"#18181B"}]'
  },
  {
    id: 'prod_9',
    name: '100W Braided Nylon Fast Charging Cable (2M)',
    category: 'Cables',
    description:
      'Heavy duty double-braided ballistic nylon cable tested for 30,000+ bends. Built-in E-marker chip supporting 5A 100W PD.',
    price: 299,
    originalPrice: 499,
    imageUrl:
      'https://images.unsplash.com/photo-1609592424109-dd9892f1b177?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 80,
    isOneHourEligible: true,
    isPopular: true,
    popularityRank: 6,
    colorOptionsJson:
      '[{"name":"Carbon Black","hex":"#1F2937"},{"name":"Racing Red","hex":"#DC2626"},{"name":"Navy Weave","hex":"#1E3A8A"}]'
  },
  {
    id: 'prod_10',
    name: '3-in-1 Universal Fast Cable (Lightning/Type-C/Micro)',
    category: 'Cables',
    description:
      'Universal charging solution for all phones and accessories with reinforced aluminum alloy connectors.',
    price: 349,
    originalPrice: 549,
    imageUrl:
      'https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 45,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 10,
    colorOptionsJson:
      '[{"name":"Space Gray","hex":"#475569"},{"name":"Crimson Red","hex":"#E50914"}]'
  },
  {
    id: 'prod_11',
    name: '360° Magnetic Air Vent Car Mount',
    category: 'Accessories',
    description:
      '6 powerful N52 rare-earth magnets ensure phone stays firmly anchored even over speed bumps and rough roads.',
    price: 499,
    originalPrice: 799,
    imageUrl:
      'https://images.unsplash.com/photo-1586105251261-72a756497a11?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 22,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 11,
    colorOptionsJson:
      '[{"name":"Anodized Black","hex":"#111827"},{"name":"Silver Alloy","hex":"#94A3B8"}]'
  },
  {
    id: 'prod_12',
    name: 'Foldable Heavy Aluminum Desk Stand',
    category: 'Accessories',
    description:
      'Ergonomic multi-angle phone and tablet holder with anti-slip silicone cushions and cable pass-through.',
    price: 399,
    originalPrice: 599,
    imageUrl:
      'https://images.unsplash.com/photo-1586775490184-b79f0621891f?w=600&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 37,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 12,
    colorOptionsJson:
      '[{"name":"Space Gray","hex":"#475569"},{"name":"Natural Silver","hex":"#CBD5E1"}]'
  }
];

export const COLOR_PRESETS = [
  { name: 'Space Black', hex: '#2C2C2E' },
  { name: 'Navy Blue', hex: '#1B2A4A' },
  { name: 'Crimson Red', hex: '#E50914' },
  { name: 'Deep Purple', hex: '#6B5B95' },
  { name: 'Natural Titanium', hex: '#C4B8A5' },
  { name: 'Forest Green', hex: '#166534' },
  { name: 'Sky Blue', hex: '#0284C7' },
  { name: 'Rose Gold', hex: '#E11D48' },
  { name: 'Pearl White', hex: '#F1F5F9' },
  { name: 'Amber Gold', hex: '#D97706' }
];

export const MAJOR_DEVICE_BRANDS = [
  'Apple',
  'Samsung',
  'OnePlus',
  'Xiaomi',
  'Realme',
  'Vivo',
  'Oppo',
  'Motorola'
];

export const DEVICE_CATALOG = {
  Apple: [
    'iPhone 16 Pro Max', 'iPhone 16 Pro', 'iPhone 16 Plus', 'iPhone 16', 'iPhone 16e',
    'iPhone 15 Pro Max', 'iPhone 15 Pro', 'iPhone 15 Plus', 'iPhone 15',
    'iPhone 14 Pro Max', 'iPhone 14 Pro', 'iPhone 14 Plus', 'iPhone 14',
    'iPhone 13 Pro Max', 'iPhone 13 Pro', 'iPhone 13 Mini', 'iPhone 13',
    'iPhone 12 Pro Max', 'iPhone 12 Pro', 'iPhone 12 Mini', 'iPhone 12',
    'iPhone 11 Pro Max', 'iPhone 11 Pro', 'iPhone 11',
    'iPhone SE (3rd Gen)', 'iPhone SE (2nd Gen)', 'iPhone XS Max', 'iPhone XR', 'iPhone X'
  ],
  Samsung: [
    'Galaxy S25 Ultra', 'Galaxy S25+', 'Galaxy S25',
    'Galaxy S24 Ultra', 'Galaxy S24+', 'Galaxy S24', 'Galaxy S24 FE',
    'Galaxy S23 Ultra', 'Galaxy S23+', 'Galaxy S23', 'Galaxy S23 FE',
    'Galaxy S22 Ultra', 'Galaxy S22+', 'Galaxy S22',
    'Galaxy S21 Ultra', 'Galaxy S21 FE',
    'Galaxy Z Fold 6', 'Galaxy Z Flip 6', 'Galaxy Z Fold 5', 'Galaxy Z Flip 5',
    'Galaxy A55 5G', 'Galaxy A54 5G', 'Galaxy A35 5G', 'Galaxy A34 5G',
    'Galaxy A25 5G', 'Galaxy A16 5G', 'Galaxy A15 5G', 'Galaxy A14 5G',
    'Galaxy M55 5G', 'Galaxy M35 5G', 'Galaxy M34 5G', 'Galaxy M15 5G',
    'Galaxy F55 5G', 'Galaxy F54 5G', 'Galaxy F15 5G'
  ],
  OnePlus: [
    'OnePlus 13', 'OnePlus 13R',
    'OnePlus 12', 'OnePlus 12R',
    'OnePlus 11', 'OnePlus 11R',
    'OnePlus 10 Pro', 'OnePlus 10T', 'OnePlus 10R',
    'OnePlus 9 Pro', 'OnePlus 9', 'OnePlus 9R',
    'OnePlus Nord 4', 'OnePlus Nord 3 5G', 'OnePlus Nord CE 4',
    'OnePlus Nord CE 4 Lite', 'OnePlus Nord CE 3', 'OnePlus Nord CE 3 Lite',
    'OnePlus Open'
  ],
  Xiaomi: [
    'Xiaomi 15', 'Xiaomi 14 Ultra', 'Xiaomi 14', 'Xiaomi 14 CIVI',
    'Xiaomi 13 Pro', 'Xiaomi 12 Pro', 'Xiaomi 11i HyperCharge',
    'Redmi Note 14 Pro+ 5G', 'Redmi Note 14 Pro 5G', 'Redmi Note 14 5G',
    'Redmi Note 13 Pro+ 5G', 'Redmi Note 13 Pro 5G', 'Redmi Note 13 5G',
    'Redmi Note 12 Pro+ 5G', 'Redmi Note 12 Pro', 'Redmi Note 12',
    'Redmi Note 11 Pro+ 5G', 'Redmi Note 11', 'Redmi Note 10 Pro',
    'Redmi 13 5G', 'Redmi 13C 5G', 'Redmi 12 5G', 'Redmi A3',
    'POCO X7 Pro', 'POCO X6 Pro', 'POCO F6', 'POCO M6 Pro 5G'
  ],
  Realme: [
    'Realme GT 7 Pro', 'Realme GT 6', 'Realme GT 6T', 'Realme GT Neo 3',
    'Realme 14 Pro+', 'Realme 14 Pro',
    'Realme 13 Pro+', 'Realme 13 Pro', 'Realme 13+ 5G',
    'Realme 12 Pro+', 'Realme 12 Pro', 'Realme 12+ 5G', 'Realme 12x 5G',
    'Realme 11 Pro+', 'Realme 11 Pro', 'Realme 11x 5G',
    'Realme 10 Pro+', 'Realme 10 Pro',
    'Realme Narzo 70 Pro 5G', 'Realme Narzo 70x 5G', 'Realme Narzo 60 Pro',
    'Realme P2 Pro 5G', 'Realme P1 5G', 'Realme C67 5G', 'Realme C55'
  ],
  Vivo: [
    'Vivo X200 Pro', 'Vivo X200', 'Vivo X100 Pro', 'Vivo X100', 'Vivo X90 Pro',
    'Vivo X Fold 3 Pro',
    'Vivo V40 Pro', 'Vivo V40', 'Vivo V40e',
    'Vivo V30 Pro', 'Vivo V30', 'Vivo V30e',
    'Vivo V29 Pro', 'Vivo V29', 'Vivo V27 Pro',
    'Vivo T3 Ultra', 'Vivo T3 Pro 5G', 'Vivo T3 5G', 'Vivo T3x 5G',
    'Vivo T2 Pro 5G', 'Vivo Y300 5G', 'Vivo Y200e 5G', 'Vivo Y58 5G', 'Vivo Y28 5G'
  ],
  Oppo: [
    'Oppo Find X8 Pro', 'Oppo Find X8', 'Oppo Find N3 Flip',
    'Oppo Reno 13 Pro 5G', 'Oppo Reno 13 5G',
    'Oppo Reno 12 Pro 5G', 'Oppo Reno 12 5G',
    'Oppo Reno 11 Pro 5G', 'Oppo Reno 11 5G',
    'Oppo Reno 10 Pro+ 5G', 'Oppo Reno 10 5G',
    'Oppo F27 Pro+ 5G', 'Oppo F27 5G', 'Oppo F25 Pro 5G', 'Oppo F23 5G',
    'Oppo K12x 5G', 'Oppo A3 Pro 5G', 'Oppo A79 5G', 'Oppo A78 5G', 'Oppo A59 5G'
  ],
  Motorola: [
    'Motorola Razr 50 Ultra', 'Motorola Razr 50', 'Motorola Razr 40 Ultra',
    'Motorola Edge 50 Ultra', 'Motorola Edge 50 Pro', 'Motorola Edge 50 Fusion', 'Motorola Edge 50 Neo',
    'Motorola Edge 40 Pro', 'Motorola Edge 40', 'Motorola Edge 40 Neo',
    'Moto G85 5G', 'Moto G84 5G', 'Moto G64 5G', 'Moto G54 5G',
    'Moto G45 5G', 'Moto G35 5G', 'Moto G34 5G', 'Moto G24 Power',
    'Moto E14'
  ]
};

export function searchDeviceCatalog(brandFilter = '', query = '') {
  const q = String(query || '').trim().toLowerCase();
  const brands = brandFilter ? [brandFilter] : MAJOR_DEVICE_BRANDS;
  const results = [];
  for (const brand of brands) {
    const models = DEVICE_CATALOG[brand] || [];
    for (const model of models) {
      if (!q || model.toLowerCase().includes(q) || brand.toLowerCase().includes(q) || `${brand} ${model}`.toLowerCase().includes(q)) {
        results.push({ brand, model });
      }
    }
  }
  return results;
}

export const OTHER_MODEL_OPTION = 'Other Model (Type Manually)';
export const OTHER_ANDROID_POUCHES_OPTION = 'Other Android Models';
export const SAMPLE_GUARD_VIDEO_URL =
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4';
export const TEMPERED_GLASS_IMAGE_URL =
  'https://images.unsplash.com/photo-1616440347437-b1c73416efc2?w=500&auto=format&fit=crop&q=80';
export const CAMERA_LENS_IMAGE_URL =
  'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=500&auto=format&fit=crop&q=80';
export const POUCH_IMAGE_URL =
  'https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500&auto=format&fit=crop&q=80';

export const SCREEN_GUARD_CONFIG_KEY = 'screen_guard_admin_config';
export const DELETED_PRODUCTS_KEY = 'deleted_product_ids';
export const CATALOG_SEEDED_KEY = 'catalog_seeded_v1';

export const IPHONE_GLASS_TYPES = [
  'Privacy',
  'Matte',
  'Borderless Tempered Glass',
  '360 Privacy',
  'Normal 5D'
];
export const SAMSUNG_FLAT_S_GLASS_TYPES = ['Privacy', 'Matte'];
export const STANDARD_GLASS_TYPES = ['Normal 5D'];
export const ALL_GLASS_TYPES = [
  'Privacy',
  'Matte',
  'Borderless Tempered Glass',
  '360 Privacy',
  'Normal 5D'
];

export const GLASS_TYPE_HELPER_NOTES = {
  Privacy: 'Blocks side viewing so only you see your screen clearly.',
  Matte: 'Reduces glare, reflections & fingerprint smudges for smooth touch.',
  'Borderless Tempered Glass': 'Edge-to-edge full clear glass with zero black borders.',
  '360 Privacy': 'All-round 4-way privacy shield from every viewing angle.',
  'Normal 5D': 'Standard 9H full-coverage curved-edge clear screen protection.'
};

export function defaultGlassTypeHelperNote(glassType = '') {
  return GLASS_TYPE_HELPER_NOTES[glassType] || 'Precision 9H tempered glass screen protection.';
}

export function getGlassTypeHelperNote(glassType = '', config = createDefaultScreenGuardConfig()) {
  const custom = String(config?.glassTypeConfigs?.[glassType]?.helperNote || '').trim();
  return custom || defaultGlassTypeHelperNote(glassType);
}

export function getAllGlassTypes(config = createDefaultScreenGuardConfig()) {
  const keys = Object.keys(config?.glassTypeConfigs || {});
  const extras = keys.filter((k) => !ALL_GLASS_TYPES.includes(k));
  return [...ALL_GLASS_TYPES, ...extras];
}

export const DEFAULT_CAMERA_LENS_PRICE = 249;
export const DEFAULT_GLASS_TYPE_PRICES = {
  'Normal 5D': 199,
  Matte: 299,
  Privacy: 349,
  'Borderless Tempered Glass': 399,
  '360 Privacy': 499
};

export const SCREEN_GUARD_BRANDS = [
  'iPhone',
  'Redmi/Xiaomi',
  'Samsung',
  'Oppo/Realme',
  'Vivo/iQOO',
  'OnePlus',
  'Nothing'
];

export const ALL_SAMSUNG_S_SERIES_MODELS = [
  'Galaxy S25 Ultra', 'Galaxy S25+', 'Galaxy S25', 'Galaxy S25 FE',
  'Galaxy S24 Ultra', 'Galaxy S24+', 'Galaxy S24', 'Galaxy S24 FE',
  'Galaxy S23 Ultra', 'Galaxy S23+', 'Galaxy S23', 'Galaxy S23 FE',
  'Galaxy S22 Ultra', 'Galaxy S22+', 'Galaxy S22',
  'Galaxy S21 Ultra', 'Galaxy S21+', 'Galaxy S21', 'Galaxy S21 FE',
  'Galaxy S20 FE / S20'
];

// Samsung Tempered Glass dropdown strictly EXCLUDES curved-display S series models
// (S20 series, S21 Ultra, S22 Ultra, S23 Ultra). Flat S series + non-S Samsung remain.
export const SCREEN_GUARD_MODELS = {
  iPhone: [
    'iPhone 16 Pro Max', 'iPhone 16 Pro', 'iPhone 16 Plus', 'iPhone 16', 'iPhone 16e',
    'iPhone 15 Pro Max', 'iPhone 15 Pro', 'iPhone 15 Plus', 'iPhone 15',
    'iPhone 14 Pro Max', 'iPhone 14 Pro', 'iPhone 14 Plus', 'iPhone 14',
    'iPhone 13 Pro Max', 'iPhone 13 Pro', 'iPhone 13 Mini', 'iPhone 13',
    'iPhone 12 Pro Max', 'iPhone 12 Pro', 'iPhone 12 Mini', 'iPhone 12',
    'iPhone 11 Pro Max', 'iPhone 11 Pro', 'iPhone 11',
    'iPhone SE (2020 / 2022)', 'iPhone XS Max', 'iPhone XR / X / XS'
  ],
  'Redmi/Xiaomi': [
    'Xiaomi 15', 'Xiaomi 14 Ultra', 'Xiaomi 14', 'Xiaomi 14 CIVI',
    'Xiaomi 13 Pro', 'Xiaomi 12 Pro', 'Xiaomi 11i / 11i HyperCharge',
    'Redmi Note 14 Pro+ 5G', 'Redmi Note 14 Pro 5G', 'Redmi Note 14 5G',
    'Redmi Note 13 Pro+ 5G', 'Redmi Note 13 Pro 5G', 'Redmi Note 13 5G',
    'Redmi Note 12 Pro+ 5G', 'Redmi Note 12 Pro', 'Redmi Note 12 4G / 5G',
    'Redmi Note 11 Pro+ / Note 11', 'Redmi Note 10 Pro / Note 10',
    'Redmi 14C 5G', 'Redmi 13 5G / 13C', 'Redmi 12 5G', 'Redmi A3 / A3x',
    'POCO X7 Pro 5G', 'POCO X6 Pro / X6 5G', 'POCO F6 5G', 'POCO M6 Pro 5G'
  ],
  Samsung: [
    'Galaxy S25 Ultra', 'Galaxy S25+', 'Galaxy S25', 'Galaxy S25 FE',
    'Galaxy S24 Ultra', 'Galaxy S24+', 'Galaxy S24', 'Galaxy S24 FE',
    'Galaxy S23+', 'Galaxy S23', 'Galaxy S23 FE',
    'Galaxy S22+', 'Galaxy S22',
    'Galaxy S21+', 'Galaxy S21', 'Galaxy S21 FE',
    'Galaxy A55 5G', 'Galaxy A54 5G', 'Galaxy A35 5G', 'Galaxy A34 5G',
    'Galaxy A25 5G', 'Galaxy A16 5G', 'Galaxy A15 5G', 'Galaxy A14 5G',
    'Galaxy M55 5G', 'Galaxy M35 5G', 'Galaxy M34 5G', 'Galaxy M15 5G',
    'Galaxy F55 5G', 'Galaxy F54 5G', 'Galaxy F15 5G',
    'Galaxy Z Fold 6 / Flip 6'
  ],
  'Oppo/Realme': [
    'Oppo Find X8 Pro', 'Oppo Find X8',
    'Oppo Reno 13 Pro 5G', 'Oppo Reno 13 5G',
    'Oppo Reno 12 Pro 5G', 'Oppo Reno 12 5G',
    'Oppo Reno 11 Pro 5G', 'Oppo Reno 11 5G', 'Oppo Reno 10 Pro+ / 10',
    'Oppo F27 Pro+ 5G', 'Oppo F27 5G', 'Oppo F25 Pro 5G', 'Oppo F23 5G',
    'Oppo K12x 5G', 'Oppo A3 Pro 5G', 'Oppo A79 5G / A78 5G', 'Oppo A59 5G',
    'Realme GT 7 Pro', 'Realme GT 6 / GT 6T',
    'Realme 14 Pro+ / 14 Pro', 'Realme 13 Pro+ / 13 Pro', 'Realme 13+ 5G / 13 5G',
    'Realme 12 Pro+ / 12 Pro', 'Realme 12+ / 12x 5G', 'Realme 11 Pro+ / 11 Pro',
    'Realme Narzo 70 Pro / 70x', 'Realme Narzo 60 Pro',
    'Realme P2 Pro / P1 5G', 'Realme C67 5G / C55'
  ],
  'Vivo/iQOO': [
    'Vivo X200 Pro', 'Vivo X200', 'Vivo X100 Pro', 'Vivo X100', 'Vivo X90 Pro',
    'Vivo V40 Pro', 'Vivo V40 / V40e', 'Vivo V30 Pro', 'Vivo V30 / V30e',
    'Vivo V29 Pro / V29', 'Vivo V27 Pro / V27',
    'Vivo T3 Ultra 5G', 'Vivo T3 Pro / T3 / T3x 5G', 'Vivo T2 Pro 5G',
    'Vivo Y300 5G', 'Vivo Y200e 5G', 'Vivo Y58 5G / Y28 5G',
    'iQOO 13 5G', 'iQOO 12 5G', 'iQOO Neo 9 Pro', 'iQOO Neo 7 Pro',
    'iQOO Z9s Pro / Z9s 5G', 'iQOO Z9 / Z9x 5G', 'iQOO Z7 Pro 5G'
  ],
  OnePlus: [
    'OnePlus 13', 'OnePlus 13R', 'OnePlus 12', 'OnePlus 12R',
    'OnePlus 11', 'OnePlus 11R', 'OnePlus 10 Pro', 'OnePlus 10T', 'OnePlus 10R',
    'OnePlus 9 Pro', 'OnePlus 9', 'OnePlus 9R',
    'OnePlus Nord 4', 'OnePlus Nord 3 5G', 'OnePlus Nord CE 4',
    'OnePlus Nord CE 4 Lite', 'OnePlus Nord CE 3', 'OnePlus Nord CE 3 Lite',
    'OnePlus Open'
  ],
  Nothing: [
    'Nothing Phone (3a) Pro', 'Nothing Phone (3a)',
    'Nothing Phone (2a) Plus', 'Nothing Phone (2a)',
    'Nothing Phone (2)', 'Nothing Phone (1)', 'CMF Phone 1'
  ]
};

export const CAMERA_LENS_BRANDS = ['iPhone', 'Samsung S Series'];

export const CAMERA_LENS_MODELS = {
  iPhone: [...SCREEN_GUARD_MODELS.iPhone],
  'Samsung S Series': [...ALL_SAMSUNG_S_SERIES_MODELS]
};

export const POUCH_BRANDS = ['iPhone', 'Samsung S Series'];

export const POUCH_MODELS = {
  iPhone: [...SCREEN_GUARD_MODELS.iPhone],
  'Samsung S Series': [...ALL_SAMSUNG_S_SERIES_MODELS]
};

export const POUCH_PRESET_COLORS = [
  { name: 'Black', hex: '#111827', imageUrl: '' },
  { name: 'Midnight Blue', hex: '#1E3A8A', imageUrl: '' },
  { name: 'Deep Green', hex: '#166534', imageUrl: '' },
  { name: 'Lavender', hex: '#A78BFA', imageUrl: '' },
  { name: 'Red', hex: '#DC2626', imageUrl: '' },
  { name: 'Clear/Transparent', hex: '#E2E8F0', imageUrl: '' },
  { name: 'Pink', hex: '#EC4899', imageUrl: '' },
  { name: 'Grey', hex: '#64748B', imageUrl: '' },
  { name: 'Brown', hex: '#78350F', imageUrl: '' }
];

export function guardModelKey(brand = '', model = '') {
  return `${String(brand || '').trim()}::${String(model || '').trim()}`;
}

export function normalizeGuardOffer(raw) {
  const obj = raw && typeof raw === 'object' ? raw : {};
  const rawType = String(obj.type || 'NONE').trim().toUpperCase();
  const type = ['FLAT_OFF', 'PERCENT_OFF', 'SPECIAL_PRICE'].includes(rawType) ? rawType : 'NONE';
  const numVal = Number(obj.value);
  return {
    active: Boolean(obj.active) && type !== 'NONE',
    type,
    value: Number.isFinite(numVal) && numVal >= 0 ? numVal : 0,
    badgeText: String(obj.badgeText || '')
  };
}

export function resolveOfferBadge(offer) {
  const clean = normalizeGuardOffer(offer);
  if (!clean.active || clean.type === 'NONE') return '';
  const custom = clean.badgeText.trim();
  if (custom) return custom;
  if (clean.type === 'FLAT_OFF') return `₹${Math.max(0, Math.round(clean.value))} OFF`;
  if (clean.type === 'PERCENT_OFF') return `${Math.min(99, Math.max(0, Math.round(clean.value)))}% OFF`;
  if (clean.type === 'SPECIAL_PRICE') return 'SPECIAL OFFER';
  return '';
}

export function createDefaultComboOffer() {
  return { active: true, type: 'FLAT_OFF', value: 50, badgeText: 'COMBO SAVE' };
}

export function createDefaultPouchItems() {
  return [
    {
      id: 'pouch_magsafe_clear',
      name: 'MagSafe Crystal Clear Pouch',
      description: 'Anti-yellowing clear shell with built-in magnetic ring',
      basePrice: 399,
      compatibleBrands: ['iPhone', 'Samsung S Series'],
      enabled: true,
      deleted: false,
      offer: { active: false, type: 'NONE', value: 0, badgeText: '' }
    },
    {
      id: 'pouch_matte_armor',
      name: 'Matte Translucent Shockproof Pouch',
      description: 'Anti-fingerprint frosted back with raised camera bump guard',
      basePrice: 349,
      compatibleBrands: ['iPhone', 'Samsung S Series'],
      enabled: true,
      deleted: false,
      offer: { active: false, type: 'NONE', value: 0, badgeText: '' }
    },
    {
      id: 'pouch_silicone_grip',
      name: 'Soft Liquid Silicone Velvet Pouch',
      description: 'Microfiber interior lining with silky soft-touch grip',
      basePrice: 299,
      compatibleBrands: ['iPhone', 'Samsung S Series'],
      enabled: true,
      deleted: false,
      offer: { active: false, type: 'NONE', value: 0, badgeText: '' }
    }
  ];
}

function normalizeStringListMap(raw) {
  const out = {};
  if (!raw || typeof raw !== 'object') return out;
  for (const [brand, list] of Object.entries(raw)) {
    const cleanBrand = String(brand || '').trim();
    if (!cleanBrand || !Array.isArray(list)) continue;
    const seen = [];
    for (const item of list) {
      const m = String(item || '').trim();
      if (m && !seen.includes(m)) seen.push(m);
    }
    if (seen.length > 0) out[cleanBrand] = seen;
  }
  return out;
}

function normalizePouchColorOption(raw) {
  if (!raw || typeof raw !== 'object') return null;
  const name = String(raw.name || '').trim();
  if (!name) return null;
  return {
    name,
    hex: normalizeHexColor(raw.hex || '#1E293B'),
    imageUrl: String(raw.imageUrl || '').trim()
  };
}

function normalizePouchItem(raw) {
  if (!raw || typeof raw !== 'object') return null;
  const id = String(raw.id || '').trim();
  const name = String(raw.name || '').trim();
  if (!id || !name) return null;
  const bp = Number(raw.basePrice);
  const compat = Array.isArray(raw.compatibleBrands)
    ? raw.compatibleBrands.map((b) => String(b || '').trim()).filter(Boolean)
    : [];
  const rawColors = Array.isArray(raw.colorOptions) ? raw.colorOptions : [];
  const colorOptions = [];
  const seenColorKeys = new Set();
  for (const c of rawColors) {
    const clean = normalizePouchColorOption(c);
    if (!clean) continue;
    const key = clean.name.toLowerCase();
    if (!seenColorKeys.has(key)) {
      seenColorKeys.add(key);
      colorOptions.push(clean);
    }
  }
  return {
    id,
    name,
    description: String(raw.description || ''),
    basePrice: Number.isFinite(bp) && bp >= 0 ? bp : 299,
    compatibleBrands: compat.length > 0 ? compat : ['iPhone', 'Samsung S Series'],
    enabled: raw.enabled !== false,
    deleted: Boolean(raw.deleted),
    offer: normalizeGuardOffer(raw.offer),
    imageUrl: String(raw.imageUrl || '').trim(),
    colorOptions,
    showOnHomePage: Boolean(raw.showOnHomePage)
  };
}

export function createDefaultScreenGuardConfig() {
  const glassTypeConfigs = {};
  for (const [glassType, basePrice] of Object.entries(DEFAULT_GLASS_TYPE_PRICES)) {
    glassTypeConfigs[glassType] = {
      basePrice,
      enabled: true,
      deleted: false,
      offer: { active: false, type: 'NONE', value: 0, badgeText: '' },
      helperNote: ''
    };
  }
  return {
    glassTypeConfigs,
    modelConfigs: {},
    customModelsByBrand: {},
    cameraLensDefault: {
      basePrice: DEFAULT_CAMERA_LENS_PRICE,
      enabled: true,
      deleted: false,
      offer: { active: false, type: 'NONE', value: 0, badgeText: '' }
    },
    cameraLensModelConfigs: {},
    customCameraLensModelsByBrand: {},
    comboEnabled: true,
    comboOffer: createDefaultComboOffer(),
    pouchItems: createDefaultPouchItems(),
    pouchModelConfigs: {},
    customPouchModelsByBrand: {}
  };
}

export function parseScreenGuardConfig(raw) {
  const base = createDefaultScreenGuardConfig();
  if (!raw) return base;
  try {
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw;
    if (!parsed || typeof parsed !== 'object') return base;

    if (parsed.glassTypeConfigs && typeof parsed.glassTypeConfigs === 'object') {
      for (const [k, v] of Object.entries(parsed.glassTypeConfigs)) {
        const fallback = DEFAULT_GLASS_TYPE_PRICES[k] || 199;
        const bp = Number(v?.basePrice);
        base.glassTypeConfigs[k] = {
          basePrice: Number.isFinite(bp) && bp >= 0 ? bp : fallback,
          enabled: v?.enabled !== false,
          deleted: Boolean(v?.deleted),
          offer: normalizeGuardOffer(v?.offer),
          helperNote: String(v?.helperNote || '')
        };
      }
    }

    if (parsed.modelConfigs && typeof parsed.modelConfigs === 'object') {
      for (const [k, v] of Object.entries(parsed.modelConfigs)) {
        const customRates = {};
        if (v?.customRates && typeof v.customRates === 'object') {
          for (const [gt, rate] of Object.entries(v.customRates)) {
            const rNum = Number(rate);
            if (Number.isFinite(rNum) && rNum > 0) customRates[gt] = rNum;
          }
        }
        base.modelConfigs[k] = {
          enabled: v?.enabled !== false,
          deleted: Boolean(v?.deleted),
          customRates,
          offer: normalizeGuardOffer(v?.offer)
        };
      }
    }

    base.customModelsByBrand = normalizeStringListMap(parsed.customModelsByBrand);

    if (parsed.cameraLensDefault && typeof parsed.cameraLensDefault === 'object') {
      const bp = Number(parsed.cameraLensDefault.basePrice);
      base.cameraLensDefault = {
        basePrice: Number.isFinite(bp) && bp >= 0 ? bp : DEFAULT_CAMERA_LENS_PRICE,
        enabled: parsed.cameraLensDefault.enabled !== false,
        deleted: Boolean(parsed.cameraLensDefault.deleted),
        offer: normalizeGuardOffer(parsed.cameraLensDefault.offer)
      };
    }

    if (parsed.cameraLensModelConfigs && typeof parsed.cameraLensModelConfigs === 'object') {
      for (const [k, v] of Object.entries(parsed.cameraLensModelConfigs)) {
        const cp = Number(v?.customPrice);
        base.cameraLensModelConfigs[k] = {
          enabled: v?.enabled !== false,
          deleted: Boolean(v?.deleted),
          customPrice: Number.isFinite(cp) && cp > 0 ? cp : null,
          offer: normalizeGuardOffer(v?.offer)
        };
      }
    }

    base.customCameraLensModelsByBrand = normalizeStringListMap(parsed.customCameraLensModelsByBrand);
    base.comboEnabled = parsed.comboEnabled !== false;
    base.comboOffer =
      parsed.comboOffer !== undefined ? normalizeGuardOffer(parsed.comboOffer) : createDefaultComboOffer();

    if (Array.isArray(parsed.pouchItems)) {
      base.pouchItems = parsed.pouchItems.map(normalizePouchItem).filter(Boolean);
    }

    if (parsed.pouchModelConfigs && typeof parsed.pouchModelConfigs === 'object') {
      for (const [k, v] of Object.entries(parsed.pouchModelConfigs)) {
        base.pouchModelConfigs[k] = {
          enabled: v?.enabled !== false,
          deleted: Boolean(v?.deleted),
          customRates: {},
          offer: normalizeGuardOffer(v?.offer)
        };
      }
    }

    base.customPouchModelsByBrand = normalizeStringListMap(parsed.customPouchModelsByBrand);
    return base;
  } catch {
    return base;
  }
}

export function serializeScreenGuardConfig(config) {
  return JSON.stringify(parseScreenGuardConfig(config));
}

export function parseDeletedProductIds(raw) {
  if (!raw) return [];
  if (Array.isArray(raw)) {
    return Array.from(new Set(raw.map((x) => String(x || '').trim()).filter(Boolean)));
  }
  const str = String(raw).trim();
  if (!str) return [];
  if (str.startsWith('[')) {
    try {
      const parsed = JSON.parse(str);
      if (Array.isArray(parsed)) {
        return Array.from(new Set(parsed.map((x) => String(x || '').trim()).filter(Boolean)));
      }
    } catch {}
  }
  return Array.from(
    new Set(
      str
        .split(',')
        .map((x) => x.trim())
        .filter(Boolean)
    )
  );
}

export function serializeDeletedProductIds(ids = []) {
  return JSON.stringify(parseDeletedProductIds(ids));
}

export function getModelsForBrand(brand = '', config = createDefaultScreenGuardConfig()) {
  const base = SCREEN_GUARD_MODELS[brand] || [];
  const custom = config?.customModelsByBrand?.[brand] || [];
  return Array.from(new Set([...base, ...custom]));
}

export function getCameraLensModelsForBrand(brand = '', config = createDefaultScreenGuardConfig()) {
  const base = CAMERA_LENS_MODELS[brand] || [];
  const custom = config?.customCameraLensModelsByBrand?.[brand] || [];
  return Array.from(new Set([...base, ...custom]));
}

export function getPouchModelsForBrand(brand = '', config = createDefaultScreenGuardConfig()) {
  const base = POUCH_MODELS[brand] || [];
  const custom = config?.customPouchModelsByBrand?.[brand] || [];
  return Array.from(new Set([...base, ...custom]));
}

export function isGlassTypeAvailable(glassType, config = createDefaultScreenGuardConfig()) {
  const cfg = config?.glassTypeConfigs?.[glassType];
  if (!cfg) return true;
  return !cfg.deleted && cfg.enabled !== false;
}

export function isTemperedGlassModelAvailable(brand = '', model = '', config = createDefaultScreenGuardConfig()) {
  const cleanBrand = String(brand || '').trim();
  const cleanModel = String(model || '').trim();
  if (cleanBrand.toUpperCase() === 'CUSTOM' || cleanModel === OTHER_MODEL_OPTION) return true;
  const catalogList = getModelsForBrand(cleanBrand, config);
  if (!catalogList.includes(cleanModel)) return false;
  const cfg = config?.modelConfigs?.[guardModelKey(cleanBrand, cleanModel)];
  if (!cfg) return true;
  return !cfg.deleted && cfg.enabled !== false;
}

export function isCameraLensSectionAvailable(config = createDefaultScreenGuardConfig()) {
  const def = config?.cameraLensDefault;
  if (!def) return true;
  return !def.deleted && def.enabled !== false;
}

export function isCameraLensModelAvailable(brand = '', model = '', config = createDefaultScreenGuardConfig()) {
  if (!isCameraLensSectionAvailable(config)) return false;
  const cleanBrand = String(brand || '').trim();
  const cleanModel = String(model || '').trim();
  const catalogList = getCameraLensModelsForBrand(cleanBrand, config);
  if (!catalogList.includes(cleanModel)) return false;
  const cfg = config?.cameraLensModelConfigs?.[guardModelKey(cleanBrand, cleanModel)];
  if (!cfg) return true;
  return !cfg.deleted && cfg.enabled !== false;
}

export function isPouchModelAvailable(brand = '', model = '', config = createDefaultScreenGuardConfig()) {
  const cleanBrand = String(brand || '').trim();
  const cleanModel = String(model || '').trim();
  const catalogList = getPouchModelsForBrand(cleanBrand, config);
  if (!catalogList.includes(cleanModel)) return false;
  const cfg = config?.pouchModelConfigs?.[guardModelKey(cleanBrand, cleanModel)];
  if (!cfg) return true;
  return !cfg.deleted && cfg.enabled !== false;
}

export function getActivePouchesForBrand(brand = '', config = createDefaultScreenGuardConfig()) {
  const cleanBrand = String(brand || '').trim().toLowerCase();
  const items = Array.isArray(config?.pouchItems) ? config.pouchItems : [];
  return items.filter(
    (item) =>
      item &&
      !item.deleted &&
      item.enabled !== false &&
      (!Array.isArray(item.compatibleBrands) ||
        item.compatibleBrands.length === 0 ||
        item.compatibleBrands.some((b) => String(b || '').trim().toLowerCase() === cleanBrand))
  );
}

export function getHomeFeaturedPouches(config = createDefaultScreenGuardConfig()) {
  const items = Array.isArray(config?.pouchItems) ? config.pouchItems : [];
  return items.filter((item) => item && item.showOnHomePage === true && !item.deleted && item.enabled !== false);
}

export function getMatchingProductReviews(reviews = [], product = null) {
  if (!Array.isArray(reviews) || !product) return [];
  const pid = String(product.id || '').trim();
  const pname = String(product.name || '').trim().toLowerCase();
  return reviews
    .filter((r) => {
      if (!r || r.reviewType !== 'PRODUCT' || r.isApproved !== true) return false;
      const rPid = String(r.productId || '').trim();
      if (rPid && pid && rPid === pid) return true;
      return pname && String(r.targetTitle || '').toLowerCase().includes(pname);
    })
    .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
}

export function computeProductRatingSummary(reviews = [], product = null) {
  const matched = getMatchingProductReviews(reviews, product);
  if (matched.length === 0) return { averageRating: 0, reviewCount: 0 };
  const total = matched.reduce((acc, r) => acc + Math.max(1, Math.min(5, Number(r.rating || 5))), 0);
  return {
    averageRating: total / matched.length,
    reviewCount: matched.length
  };
}

export function getActiveScreenGuardBrands(config = createDefaultScreenGuardConfig()) {
  return SCREEN_GUARD_BRANDS.filter((brand) =>
    getModelsForBrand(brand, config).some((model) => isTemperedGlassModelAvailable(brand, model, config))
  );
}

export function getActiveCameraLensBrands(config = createDefaultScreenGuardConfig()) {
  if (!isCameraLensSectionAvailable(config)) return [];
  return CAMERA_LENS_BRANDS.filter((brand) =>
    getCameraLensModelsForBrand(brand, config).some((model) => isCameraLensModelAvailable(brand, model, config))
  );
}

export function searchScreenGuardCatalog(brandFilter = '', query = '', config = createDefaultScreenGuardConfig()) {
  const q = String(query || '').trim().toLowerCase();
  const hasBrand = String(brandFilter || '').trim().length > 0;
  const targetBrands = hasBrand
    ? (SCREEN_GUARD_MODELS[brandFilter] ? [brandFilter] : [])
    : SCREEN_GUARD_BRANDS;
  const grouped = [];
  for (const brand of targetBrands) {
    const models = getModelsForBrand(brand, config).filter((model) => {
      if (!isTemperedGlassModelAvailable(brand, model, config)) return false;
      if (!q) return true;
      return (
        model.toLowerCase().includes(q) ||
        brand.toLowerCase().includes(q) ||
        `${brand} ${model}`.toLowerCase().includes(q)
      );
    });
    if (models.length > 0) {
      grouped.push({ brand, models });
    }
  }
  return grouped;
}

export function searchCameraLensCatalog(brandFilter = '', query = '', config = createDefaultScreenGuardConfig()) {
  if (!isCameraLensSectionAvailable(config)) return [];
  const q = String(query || '').trim().toLowerCase();
  const hasBrand = String(brandFilter || '').trim().length > 0;
  const targetBrands = hasBrand
    ? (CAMERA_LENS_MODELS[brandFilter] ? [brandFilter] : [])
    : CAMERA_LENS_BRANDS;
  const grouped = [];
  for (const brand of targetBrands) {
    const models = getCameraLensModelsForBrand(brand, config).filter((model) => {
      if (!isCameraLensModelAvailable(brand, model, config)) return false;
      if (!q) return true;
      return (
        model.toLowerCase().includes(q) ||
        brand.toLowerCase().includes(q) ||
        `${brand} ${model}`.toLowerCase().includes(q)
      );
    });
    if (models.length > 0) {
      grouped.push({ brand, models });
    }
  }
  return grouped;
}

export function searchPouchCatalog(brand = 'iPhone', query = '', config = createDefaultScreenGuardConfig()) {
  const q = String(query || '').trim().toLowerCase();
  return getPouchModelsForBrand(brand, config).filter(
    (model) => isPouchModelAvailable(brand, model, config) && (!q || model.toLowerCase().includes(q))
  );
}

export function isSamsungFlatSSeries(brand = '', model = '') {
  const cleanBrand = String(brand || '').trim();
  const cleanModel = String(model || '').trim();
  if (cleanBrand.toLowerCase() !== 'samsung') return false;
  if (!cleanModel || cleanModel === OTHER_MODEL_OPTION) return false;
  const lower = cleanModel.toLowerCase();
  if (/\bs20(\+|\b)/.test(lower)) return false;
  if (/\bs2[123]\s+ultra\b/.test(lower)) return false;
  return /\b(?:galaxy\s+)?s2[1-5](\+|\b)/.test(lower);
}

export function getGlassTypesForDevice(brand = '', model = '', config = createDefaultScreenGuardConfig()) {
  const cleanBrand = String(brand || '').trim();
  const cleanModel = String(model || '').trim();
  if (!cleanModel) return [];
  if (
    cleanModel !== OTHER_MODEL_OPTION &&
    cleanBrand.toUpperCase() !== 'CUSTOM' &&
    cleanBrand &&
    !isTemperedGlassModelAvailable(cleanBrand, cleanModel, config)
  ) {
    return [];
  }
  const customTypes = Object.keys(config?.glassTypeConfigs || {}).filter((k) => !ALL_GLASS_TYPES.includes(k));
  let baseTypes;
  if (cleanModel === OTHER_MODEL_OPTION || cleanBrand.toUpperCase() === 'CUSTOM') {
    baseTypes = [...STANDARD_GLASS_TYPES];
  } else if (cleanBrand.toLowerCase() === 'iphone') {
    baseTypes = [...IPHONE_GLASS_TYPES, ...customTypes];
  } else if (isSamsungFlatSSeries(cleanBrand, cleanModel)) {
    baseTypes = [...SAMSUNG_FLAT_S_GLASS_TYPES];
  } else {
    baseTypes = [...STANDARD_GLASS_TYPES];
  }
  return baseTypes.filter((gt) => isGlassTypeAvailable(gt, config));
}

function applyGuardOffer(basePrice, offer) {
  const cleanBase = Math.max(0, Number(basePrice || 0));
  const cleanOffer = normalizeGuardOffer(offer);
  if (!cleanOffer.active || cleanOffer.type === 'NONE') {
    return { basePrice: cleanBase, finalPrice: cleanBase, offerBadge: '', hasOffer: false };
  }
  let finalPrice = cleanBase;
  if (cleanOffer.type === 'FLAT_OFF') {
    finalPrice = Math.round(Math.max(0, cleanBase - cleanOffer.value));
  } else if (cleanOffer.type === 'PERCENT_OFF') {
    const pct = Math.min(99, Math.max(0, cleanOffer.value));
    finalPrice = Math.round(Math.max(0, cleanBase * (1 - pct / 100)));
  } else if (cleanOffer.type === 'SPECIAL_PRICE') {
    finalPrice = Math.round(Math.max(0, cleanOffer.value));
  }
  const offerBadge = resolveOfferBadge(cleanOffer);
  return {
    basePrice: cleanBase,
    finalPrice,
    offerBadge,
    hasOffer: Boolean(offerBadge) || finalPrice < cleanBase
  };
}

export function resolveTemperedGlassPrice(brand = '', model = '', glassType = '', config = createDefaultScreenGuardConfig()) {
  const fallback = DEFAULT_GLASS_TYPE_PRICES[glassType] || 199;
  const glassCfg = config?.glassTypeConfigs?.[glassType] || { basePrice: fallback, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
  const cleanBrand = String(brand || '').trim();
  const modelCfg =
    cleanBrand && cleanBrand.toUpperCase() !== 'CUSTOM' && model !== OTHER_MODEL_OPTION
      ? config?.modelConfigs?.[guardModelKey(cleanBrand, model)]
      : null;
  const customRate = Number(modelCfg?.customRates?.[glassType]);
  const basePrice = Number.isFinite(customRate) && customRate > 0 ? customRate : Number(glassCfg.basePrice || fallback);
  const modelOffer = modelCfg?.offer;
  const activeOffer =
    modelOffer?.active && modelOffer?.type !== 'NONE'
      ? modelOffer
      : glassCfg.offer?.active && glassCfg.offer?.type !== 'NONE'
        ? glassCfg.offer
        : null;
  return applyGuardOffer(basePrice, activeOffer);
}

export function resolveCameraLensPrice(brand = '', model = '', config = createDefaultScreenGuardConfig()) {
  const modelCfg = config?.cameraLensModelConfigs?.[guardModelKey(brand, model)];
  const customPrice = Number(modelCfg?.customPrice);
  const camDefault = config?.cameraLensDefault || { basePrice: DEFAULT_CAMERA_LENS_PRICE };
  const basePrice = Number.isFinite(customPrice) && customPrice > 0 ? customPrice : Number(camDefault.basePrice || DEFAULT_CAMERA_LENS_PRICE);
  const modelOffer = modelCfg?.offer;
  const activeOffer =
    modelOffer?.active && modelOffer?.type !== 'NONE'
      ? modelOffer
      : camDefault.offer?.active && camDefault.offer?.type !== 'NONE'
        ? camDefault.offer
        : null;
  return applyGuardOffer(basePrice, activeOffer);
}

export function resolveComboPrice(
  glassBrand = '',
  glassModel = '',
  glassType = '',
  cameraBrand = '',
  cameraModel = '',
  config = createDefaultScreenGuardConfig()
) {
  const glassPricing = resolveTemperedGlassPrice(glassBrand, glassModel, glassType, config);
  const camPricing = resolveCameraLensPrice(cameraBrand, cameraModel, config);
  const combinedBase = glassPricing.finalPrice + camPricing.finalPrice;
  if (config?.comboEnabled !== false && config?.comboOffer?.active && config?.comboOffer?.type !== 'NONE') {
    return applyGuardOffer(combinedBase, config.comboOffer);
  }
  return { basePrice: combinedBase, finalPrice: combinedBase, offerBadge: '', hasOffer: false };
}

export function resolvePouchPrice(pouch) {
  return applyGuardOffer(Number(pouch?.basePrice || 0), pouch?.offer);
}

export function resolvePouchSelectedColor(pouch, selectedColor = '') {
  const colors = Array.isArray(pouch?.colorOptions) ? pouch.colorOptions : [];
  if (colors.length === 0) return null;
  const clean = String(selectedColor || '').trim().toLowerCase();
  if (clean) {
    const match = colors.find((c) => String(c?.name || '').trim().toLowerCase() === clean);
    if (match) return match;
  }
  return colors[0] || null;
}

export function resolvePouchImageUrl(pouch, selectedColor = '') {
  const colorObj = resolvePouchSelectedColor(pouch, selectedColor);
  const perColorImg = String(colorObj?.imageUrl || '').trim();
  if (perColorImg) return perColorImg;
  const baseImg = String(pouch?.imageUrl || '').trim();
  if (baseImg) return baseImg;
  return POUCH_IMAGE_URL;
}

function safeGuardToken(raw = '') {
  return String(raw || '').trim().replace(/::/g, ':');
}

function installationNoteSuffix(installationOption = '') {
  const clean = String(installationOption || '').trim();
  return clean ? ` • Fulfillment: ${clean}` : '';
}

function extractInstallationOption(description = '') {
  const marker = ' • Fulfillment: ';
  const idx = String(description || '').indexOf(marker);
  if (idx < 0) return '';
  const after = String(description).slice(idx + marker.length);
  const nextBullet = after.indexOf(' • ');
  return (nextBullet >= 0 ? after.slice(0, nextBullet) : after).trim();
}

export function buildTemperedGlassCartProduct(
  brand = '',
  model = '',
  glassType = '',
  config = createDefaultScreenGuardConfig(),
  installationOption = ''
) {
  const cleanBrand = String(brand || '').trim() || 'CUSTOM';
  const cleanModel = String(model || '').trim();
  const pricing = resolveTemperedGlassPrice(cleanBrand, cleanModel, glassType, config);
  const offerSuffix = pricing.offerBadge ? ` • Offer: ${pricing.offerBadge}` : '';
  const installSuffix = installationNoteSuffix(installationOption);
  return {
    id: `sg::${safeGuardToken(cleanBrand)}::${safeGuardToken(cleanModel)}::${safeGuardToken(glassType)}`,
    name: `${cleanModel} — ${glassType} Tempered Glass`,
    category: 'Tempered Glass',
    description: `Precision ${glassType} Tempered Glass for ${cleanBrand === 'CUSTOM' ? cleanModel : `${cleanBrand} ${cleanModel}`}${installSuffix}${offerSuffix}`,
    price: pricing.finalPrice,
    originalPrice: pricing.basePrice > pricing.finalPrice ? pricing.basePrice : pricing.finalPrice,
    imageUrl: TEMPERED_GLASS_IMAGE_URL,
    inStock: true,
    stockCount: 99,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 99,
    colorOptionsJson: '[]'
  };
}

export function buildCameraLensCartProduct(
  brand = '',
  model = '',
  config = createDefaultScreenGuardConfig(),
  installationOption = ''
) {
  const cleanBrand = String(brand || '').trim();
  const cleanModel = String(model || '').trim();
  const pricing = resolveCameraLensPrice(cleanBrand, cleanModel, config);
  const offerSuffix = pricing.offerBadge ? ` • Offer: ${pricing.offerBadge}` : '';
  const installSuffix = installationNoteSuffix(installationOption);
  return {
    id: `cl::${safeGuardToken(cleanBrand)}::${safeGuardToken(cleanModel)}`,
    name: `${cleanModel} — Camera Lens Protection`,
    category: 'Camera Lens Protection',
    description: `9H Sapphire Tempered Camera Lens Ring Protection for ${cleanModel}${installSuffix}${offerSuffix}`,
    price: pricing.finalPrice,
    originalPrice: pricing.basePrice > pricing.finalPrice ? pricing.basePrice : pricing.finalPrice,
    imageUrl: CAMERA_LENS_IMAGE_URL,
    inStock: true,
    stockCount: 99,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 99,
    colorOptionsJson: '[]'
  };
}

export function buildComboCartProduct(
  glassBrand = '',
  glassModel = '',
  glassType = '',
  cameraBrand = '',
  cameraModel = '',
  config = createDefaultScreenGuardConfig(),
  installationOption = ''
) {
  const cleanGBrand = String(glassBrand || '').trim() || 'CUSTOM';
  const cleanGModel = String(glassModel || '').trim();
  const cleanCBrand = String(cameraBrand || '').trim();
  const cleanCModel = String(cameraModel || '').trim();
  const pricing = resolveComboPrice(cleanGBrand, cleanGModel, glassType, cleanCBrand, cleanCModel, config);
  const offerSuffix = pricing.offerBadge ? ` • Offer: ${pricing.offerBadge}` : '';
  const installSuffix = installationNoteSuffix(installationOption);
  return {
    id: `combo::${safeGuardToken(cleanGBrand)}::${safeGuardToken(cleanGModel)}::${safeGuardToken(glassType)}::${safeGuardToken(cleanCBrand)}::${safeGuardToken(cleanCModel)}`,
    name: `${cleanGModel} (${glassType}) + ${cleanCModel} Lens Combo`,
    category: 'Tempered Glass + Lens Combo',
    description: `Tempered Glass (${cleanGModel} • ${glassType}) + Camera Lens Protection (${cleanCModel})${installSuffix}${offerSuffix}`,
    price: pricing.finalPrice,
    originalPrice: pricing.basePrice > pricing.finalPrice ? pricing.basePrice : pricing.finalPrice,
    imageUrl: TEMPERED_GLASS_IMAGE_URL,
    inStock: true,
    stockCount: 99,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 99,
    colorOptionsJson: '[]'
  };
}

export function buildPouchCartProduct(brand = '', model = '', pouch = {}, selectedColor = '') {
  const cleanBrand = String(brand || '').trim();
  const cleanModel = String(model || '').trim();
  const pricing = resolvePouchPrice(pouch);
  const matchedColor = resolvePouchSelectedColor(pouch, selectedColor)?.name || String(selectedColor || '').trim();
  const resolvedImage = resolvePouchImageUrl(pouch, matchedColor);
  const offerSuffix = pricing.offerBadge ? ` • Offer: ${pricing.offerBadge}` : '';
  const colorSuffix = matchedColor ? ` • Colour: ${matchedColor}` : '';
  const descBody = String(pouch?.description || '').trim() || 'Precision protective fit pouch';
  return {
    id: `pouch::${safeGuardToken(cleanBrand)}::${safeGuardToken(cleanModel)}::${safeGuardToken(pouch.id)}`,
    name: `${cleanModel} — ${pouch.name}`,
    category: 'Pouches',
    description: `${descBody} for ${cleanModel}${colorSuffix}${offerSuffix}`,
    price: pricing.finalPrice,
    originalPrice: pricing.basePrice > pricing.finalPrice ? pricing.basePrice : pricing.finalPrice,
    imageUrl: resolvedImage,
    inStock: true,
    stockCount: 99,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 99,
    colorOptionsJson: serializeColorOptions(pouch?.colorOptions || [])
  };
}

export function repriceOrNullCartProduct(product, config = createDefaultScreenGuardConfig(), selectedColor = '') {
  if (!product?.id) return product;
  const installOpt = extractInstallationOption(product.description);
  if (product.id.startsWith('sg::')) {
    const parts = product.id.split('::');
    if (parts.length < 4) return product;
    const [, brand, model, glassType] = parts;
    if (!isGlassTypeAvailable(glassType, config)) return null;
    if (brand.toUpperCase() !== 'CUSTOM') {
      if (!isTemperedGlassModelAvailable(brand, model, config)) return null;
      const allowedTypes = getGlassTypesForDevice(brand, model, config);
      if (!allowedTypes.includes(glassType)) return null;
    }
    return buildTemperedGlassCartProduct(brand, model, glassType, config, installOpt);
  }
  if (product.id.startsWith('cl::')) {
    const parts = product.id.split('::');
    if (parts.length < 3) return product;
    const [, brand, model] = parts;
    if (!isCameraLensModelAvailable(brand, model, config)) return null;
    return buildCameraLensCartProduct(brand, model, config, installOpt);
  }
  if (product.id.startsWith('combo::')) {
    const parts = product.id.split('::');
    if (parts.length < 6) return product;
    const [, gBrand, gModel, glassType, cBrand, cModel] = parts;
    if (config?.comboEnabled === false) return null;
    if (!isGlassTypeAvailable(glassType, config)) return null;
    if (gBrand.toUpperCase() !== 'CUSTOM') {
      if (!isTemperedGlassModelAvailable(gBrand, gModel, config)) return null;
      if (!getGlassTypesForDevice(gBrand, gModel, config).includes(glassType)) return null;
    }
    if (!isCameraLensModelAvailable(cBrand, cModel, config)) return null;
    return buildComboCartProduct(gBrand, gModel, glassType, cBrand, cModel, config, installOpt);
  }
  if (product.id.startsWith('pouch::')) {
    const parts = product.id.split('::');
    if (parts.length < 4) return product;
    const [, brand, model, pouchId] = parts;
    if (!isPouchModelAvailable(brand, model, config)) return null;
    const pouch = (config?.pouchItems || []).find(
      (item) =>
        item &&
        item.id === pouchId &&
        !item.deleted &&
        item.enabled !== false &&
        (!Array.isArray(item.compatibleBrands) ||
          item.compatibleBrands.length === 0 ||
          item.compatibleBrands.some((b) => String(b || '').trim().toLowerCase() === brand.toLowerCase()))
    );
    if (!pouch) return null;
    return buildPouchCartProduct(brand, model, pouch, selectedColor);
  }
  return product;
}


export function isOrderCompleted(status = '') {
  const normalized = String(status || '').trim().toLowerCase();
  return normalized === 'delivered' || normalized === 'completed';
}

export function isRepairCompleted(status = '') {
  const normalized = String(status || '').trim().toLowerCase();
  return (
    normalized === 'repair completed' ||
    normalized === 'out for delivery' ||
    normalized === 'delivered' ||
    normalized === 'completed'
  );
}

export function normalizePhoneToE164(rawInput = '') {
  const cleaned = String(rawInput).trim().replace(/[\s\-()]/g, '');
  if (!cleaned) return null;
  if (cleaned.startsWith('+')) {
    const digits = cleaned.slice(1).replace(/\D/g, '');
    return digits.length >= 10 && digits.length <= 15 ? `+${digits}` : null;
  }
  const digits = cleaned.replace(/\D/g, '');
  if (digits.length === 10) return `+91${digits}`;
  if (digits.length === 11 && digits.startsWith('0')) return `+91${digits.slice(1)}`;
  if (digits.length === 12 && digits.startsWith('91')) return `+${digits}`;
  if (digits.length >= 10 && digits.length <= 15) return `+${digits}`;
  return null;
}

export function formatINR(amount) {
  return `₹${Math.round(Number(amount || 0)).toLocaleString('en-IN')}`;
}

export function formatTimestamp(ts) {
  if (!ts) return '';
  try {
    return new Date(Number(ts)).toLocaleString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return '';
  }
}

export function parseOrderItems(itemsJson) {
  try {
    const parsed = JSON.parse(itemsJson || '[]');
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function parseComplaints(complaintsJson) {
  try {
    const parsed = JSON.parse(complaintsJson || '[]');
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function normalizeHexColor(rawHex = '') {
  const cleaned = String(rawHex || '').trim().replace(/^#/, '').toUpperCase();
  if (/^[0-9A-F]{6}$/.test(cleaned) || /^[0-9A-F]{8}$/.test(cleaned)) {
    return `#${cleaned}`;
  }
  return '#1E293B';
}

export function parseColorOptions(colorOptionsJson) {
  if (!colorOptionsJson) return [];
  try {
    const parsed = Array.isArray(colorOptionsJson)
      ? colorOptionsJson
      : JSON.parse(colorOptionsJson || '[]');
    if (!Array.isArray(parsed)) return [];
    return parsed
      .map((item) => ({
        name: String(item?.name || '').trim(),
        hex: normalizeHexColor(item?.hex || '#1E293B')
      }))
      .filter((item) => item.name.length > 0);
  } catch {
    return [];
  }
}

export function serializeColorOptions(options = []) {
  const clean = (Array.isArray(options) ? options : [])
    .map((item) => ({
      name: String(item?.name || '').trim(),
      hex: normalizeHexColor(item?.hex || '#1E293B')
    }))
    .filter((item) => item.name.length > 0);
  return JSON.stringify(clean);
}

export function escapeHtml(str = '') {
  return String(str ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

export function detectCurrentLocation() {
  return new Promise((resolve, reject) => {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      reject(new Error('GPS / Geolocation is not supported in this browser.'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        const coordsText = `${lat.toFixed(5)}, ${lon.toFixed(5)}`;

        try {
          const resp = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}`,
            { headers: { Accept: 'application/json' } }
          );
          if (resp.ok) {
            const data = await resp.json();
            if (data && data.display_name) {
              resolve({
                latitude: lat,
                longitude: lon,
                formattedAddress: `${data.display_name} (GPS: ${coordsText})`,
                isGeocoded: true
              });
              return;
            }
          }
        } catch {
          // Fall back to exact live coordinates without ever fabricating a default city
        }

        resolve({
          latitude: lat,
          longitude: lon,
          formattedAddress: `GPS Coordinates: ${coordsText}`,
          isGeocoded: false
        });
      },
      (err) => {
        if (err.code === 1) {
          reject(new Error('Location permission denied. Please allow location access or enter your address manually.'));
        } else if (err.code === 2) {
          reject(new Error('Live GPS location unavailable right now. Please enter your address manually.'));
        } else {
          reject(new Error('Location detection timed out. Please retry or enter your address manually.'));
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  });
}
