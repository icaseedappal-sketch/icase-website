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
      'https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1616440347437-b1c73416efc2?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1622445262464-84b1456045b6?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1609592424109-dd9892f1b177?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1586105251261-72a756497a11?w=500&auto=format&fit=crop&q=80',
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
      'https://images.unsplash.com/photo-1586775490184-b79f0621891f?w=500&auto=format&fit=crop&q=80',
    inStock: true,
    stockCount: 37,
    isOneHourEligible: true,
    isPopular: false,
    popularityRank: 12,
    colorOptionsJson:
      '[{"name":"Space Gray","hex":"#475569"},{"name":"Natural Silver","hex":"#CBD5E1"}]'
  }
];

export function normalizePhoneToE164(rawInput = '') {
  const cleaned = String(rawInput)
    .trim()
    .replace(/[\s\-()]/g, '');
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

export function normalizeHexColor(rawHex = '') {
  const cleaned = String(rawHex || '')
    .trim()
    .replace(/^#/, '')
    .toUpperCase();
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
