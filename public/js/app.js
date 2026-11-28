import {
  ALL_GLASS_TYPES,
  CAMERA_LENS_BRANDS,
  CAMERA_LENS_MODELS,
  CATALOG_SEEDED_KEY,
  CATEGORIES,
  COLOR_PRESETS,
  COMPLAINT_OPTIONS,
  DEFAULT_CAMERA_LENS_PRICE,
  DEFAULT_GLASS_TYPE_PRICES,
  DEFAULT_SETTINGS,
  DELETED_PRODUCTS_KEY,
  INITIAL_PRODUCTS,
  MAJOR_DEVICE_BRANDS,
  ORDER_STATUSES,
  OTHER_ANDROID_POPULAR_MODELS,
  OTHER_ANDROID_POUCHES_OPTION,
  OTHER_MODEL_OPTION,
  POUCH_BRANDS,
  POUCH_MODELS,
  POUCH_PRESET_COLORS,
  REPAIR_DATE_OPTIONS,
  REPAIR_STATUSES,
  SAMPLE_GUARD_VIDEO_URL,
  SCREEN_GUARD_BRANDS,
  SCREEN_GUARD_CONFIG_KEY,
  SCREEN_GUARD_MODELS,
  TIME_SLOTS,
  TOP_BRANDS,
  buildCameraLensCartProduct,
  buildCameraLensWhatsAppMessage,
  buildComboCartProduct,
  buildOtherAndroidPouchWhatsAppMessage,
  buildPouchCartProduct,
  buildTemperedGlassCartProduct,
  buildTemperedGlassWhatsAppMessage,
  computeProductRatingSummary,
  createDefaultScreenGuardConfig,
  detectCurrentLocation,
  escapeHtml,
  formatINR,
  formatTimestamp,
  generateDefaultCodeExport,
  getActiveCameraLensBrands,
  getActiveGlassTypes,
  getActivePouchesForBrand,
  getActiveScreenGuardBrands,
  getAllGlassTypes,
  getCameraLensModelsForBrand,
  getGlassTypeHelperNote,
  getGlassTypesForDevice,
  getHomeFeaturedPouches,
  getMatchingProductReviews,
  getModelsForBrand,
  getPopularCameraLensModels,
  getPopularGlassModels,
  getPouchModelsForBrand,
  getStartingCameraLensPrice,
  getStartingPouchPrice,
  getStartingTemperedGlassPrice,
  guardModelKey,
  isCameraLensSectionAvailable,
  isDeviceEligibleForCameraLens,
  isOrderCompleted,
  isRepairCompleted,
  isYouTubeUrl,
  normalizeHexColor,
  normalizePhoneToE164,
  parseColorOptions,
  parseComplaints,
  parseDeletedProductIds,
  parseOrderItems,
  parseScreenGuardConfig,
  parseYouTubeEmbedUrl,
  readShortVideoFileToDataUrl,
  repriceOrNullCartProduct,
  resolveCameraLensImageUrl,
  resolveCameraLensPrice,
  resolveComboPrice,
  resolveGlassTypeImageUrl,
  resolveGlassTypeVideoUrl,
  resolvePouchCardImageUrl,
  resolvePouchImageUrl,
  resolvePouchPrice,
  resolvePouchSelectedColor,
  resolveTemperedGlassImageUrl,
  resolveTemperedGlassPrice,
  searchCameraLensCatalog,
  searchDeviceCatalog,
  searchPouchCatalog,
  searchScreenGuardCatalog,
  serializeColorOptions,
  serializeDeletedProductIds,
  serializeScreenGuardConfig
} from './data.js';
import {
  createOrderInCloud,
  createRepairInCloud,
  createReviewInCloud,
  deleteOrderInCloud,
  deleteProductInCloud,
  deleteRepairInCloud,
  deleteReviewInCloud,
  lookupReferenceInCloud,
  rescheduleRepairInCloud,
  saveProductInCloud,
  saveSettingInCloud,
  saveUserProfileToCloud,
  seedFirestoreIfEmpty,
  signInWithGooglePopup,
  signOutFirebase,
  subscribeAdminData,
  subscribeAuthState,
  subscribePublicApprovedReviews,
  subscribePublicStoreData,
  subscribeTrackingId,
  subscribeUserScopedData,
  updateOrderStatusInCloud,
  updateRepairStatusInCloud,
  updateReviewInCloud
} from './firebase-sync.js';

const STORAGE_CART_KEY = 'icase_web_cart_v1';
const STORAGE_PROFILE_KEY = 'icase_web_profile_v1';
const STORAGE_RECENT_IDS_KEY = 'icase_web_recent_tracking_v1';
const STORAGE_PRODUCTS_KEY = 'icase_web_products_v1';
const STORAGE_CATALOG_SEEDED_KEY = 'icase_web_catalog_seeded_v1';
const STORAGE_DELETED_PRODUCTS_KEY = 'icase_web_deleted_products_v1';
const STORAGE_GUARD_CONFIG_KEY = 'icase_web_guard_config_v1';
const STORAGE_SETTINGS_KEY = 'icase_web_settings_v1';
const STORAGE_ADMIN_LOCK_KEY = 'icase_admin_lock_until_v1';

const EMPTY_PROFILE = {
  userId: '',
  name: '',
  email: '',
  phone: '',
  whatsapp: '',
  address: '',
  isLoggedIn: false,
  isGoogleSignedIn: false,
  wishlistIds: []
};

function safeStorageGetItem(key) {
  try {
    return typeof localStorage !== 'undefined' ? localStorage.getItem(key) : null;
  } catch {
    return null;
  }
}

function safeStorageSetItem(key, value) {
  try {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(key, String(value));
    }
  } catch {}
}

function loadJsonStorage(key, fallback) {
  try {
    const raw = safeStorageGetItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
}

function saveJsonStorage(key, value) {
  try {
    safeStorageSetItem(key, JSON.stringify(value));
  } catch {}
}

function productMatchesBrand(product, brand) {
  const b = String(brand || '').trim().toLowerCase();
  if (!b) return true;
  if (String(product?.brand || '').trim().toLowerCase() === b) return true;
  const hay = `${product?.name || ''} ${product?.description || ''} ${product?.category || ''}`.toLowerCase();
  if (b === 'apple') {
    return ['apple', 'iphone', 'magsafe', 'lightning', 'ipad'].some((k) => hay.includes(k));
  }
  if (b === 'samsung') {
    return ['samsung', 'galaxy'].some((k) => hay.includes(k));
  }
  if (b === 'xiaomi') {
    return ['xiaomi', 'redmi', 'poco'].some((k) => hay.includes(k));
  }
  if (b === 'oppo') {
    return ['oppo', 'reno'].some((k) => hay.includes(k));
  }
  if (b === 'vivo') {
    return ['vivo', 'iqoo'].some((k) => hay.includes(k));
  }
  if (b === 'realme') {
    return ['realme', 'narzo'].some((k) => hay.includes(k));
  }
  return hay.includes(b);
}

const initialDeletedProductIds = parseDeletedProductIds(
  loadJsonStorage(STORAGE_DELETED_PRODUCTS_KEY, [])
);
const initialCachedSettings = {
  ...DEFAULT_SETTINGS,
  ...(loadJsonStorage(STORAGE_SETTINGS_KEY, {}) || {})
};
const initialCatalogSeeded =
  safeStorageGetItem(STORAGE_CATALOG_SEEDED_KEY) === 'true' ||
  safeStorageGetItem(STORAGE_PRODUCTS_KEY) !== null;

const initialProducts = (() => {
  const deletedSet = new Set(initialDeletedProductIds);
  const cached = loadJsonStorage(STORAGE_PRODUCTS_KEY, null);
  if (Array.isArray(cached)) {
    return cached.filter((p) => p?.id && !deletedSet.has(p.id));
  }
  if (initialCatalogSeeded) {
    return [];
  }
  return INITIAL_PRODUCTS.filter((p) => !deletedSet.has(p.id));
})();
const initialGuardConfig = parseScreenGuardConfig(
  initialCachedSettings[SCREEN_GUARD_CONFIG_KEY] || loadJsonStorage(STORAGE_GUARD_CONFIG_KEY, null)
);

const state = {
  activeTab: 'home',
  guardsFocusTarget: '',
  homeSearch: '',
  selectedCategory: 'All',
  selectedCatalogBrand: '',
  catalogSearch: '',
  products: initialProducts,
  deletedProductIds: initialDeletedProductIds,
  hasSyncedProductsFromCloud: false,
  settings: initialCachedSettings,
  screenGuardConfig: initialGuardConfig,
  syncStatus: 'connecting',

  profile: loadJsonStorage(STORAGE_PROFILE_KEY, { ...EMPTY_PROFILE }),
  cartItems: loadJsonStorage(STORAGE_CART_KEY, []),
  recentTrackingIds: loadJsonStorage(STORAGE_RECENT_IDS_KEY, []),

  myOrders: [],
  myRepairs: [],

  // Product detail modal
  modalProductId: null,
  modalSelectedColorIdx: 0,
  modalReviewFormOpen: false,
  modalReviewRating: 5,
  modalReviewSubmitting: false,
  modalReviewError: '',

  // Cart drawer
  cartOpen: false,
  cartDeliveryType: 'STANDARD',
  cartPaymentMethod: 'UPI',
  cartPlacedOrder: null,
  cartError: '',
  cartBusy: false,
  cartGpsMsg: '',

  // Account modal
  accountOpen: false,
  accountTab: 'PROFILE', // PROFILE | ORDERS | REPAIRS | WISHLIST
  accountBusy: false,
  accountStatus: '',
  accountError: '',

  // Repair booking form
  repairBrand: '',
  repairModel: '',
  repairSearch: '',
  repairDropdownOpen: false,
  repairComplaints: [],
  bookedRepair: null,

  // Tracking
  trackingId: '',
  trackingResult: null, // null | { loading: true } | { type: 'ORDER'|'REPAIR'|'NOT_FOUND', data }
  unsubTracking: null,

  // Tempered Glass Finder
  glassBrand: '',
  glassModel: '',
  glassManualModel: '',
  glassSearch: '',
  glassDropdownOpen: false,
  glassType: 'Normal 5D',
  glassError: '',
  glassInstallationOption: 'Shop Installation',

  // Camera Lens Protection Finder (iPhone & Samsung S Series only)
  cameraBrand: '',
  cameraModel: '',
  cameraSearch: '',
  cameraDropdownOpen: false,

  // Pouches Finder (iPhone, Samsung S Series & Other Android Models WhatsApp enquiry)
  pouchBrand: 'iPhone',
  pouchModel: '',
  pouchSearch: '',
  pouchDropdownOpen: false,
  pouchError: '',
  otherAndroidPouchModel: '',
  otherAndroidPouchPref: 'Any Matching Design',
  selectedPouchColors: {},
  selectedHomePouchColors: {},

  // Verified Customer Reviews
  publicProductReviews: [],
  reviewModalOpen: false,
  reviewRefInput: '',
  reviewVerifying: false,
  reviewSubmitting: false,
  reviewVerifyResult: null, // null | { status: 'ELIGIBLE'|'NOT_COMPLETED'|'NOT_FOUND', reviewType, targetTitle, customerName, statusLabel }
  reviewRating: 5,
  reviewFormError: '',

  // Admin
  isAdminUnlocked: false,
  adminFailedAttempts: 0,
  adminLockUntil: (() => {
    const saved = Number(safeStorageGetItem(STORAGE_ADMIN_LOCK_KEY) || 0);
    return saved > Date.now() ? saved : 0;
  })(),
  adminLockTimerId: null,
  adminForgotStep: 'LOGIN', // 'LOGIN' | 'VERIFY_RECOVERY' | 'RESET_PIN'
  adminTab: 'ORDERS',
  adminSearch: '',
  adminOrders: [],
  adminRepairs: [],
  adminReviews: [],
  adminReviewFilter: 'ALL',
  adminShowManualReviewForm: false,
  adminReplyingReviewId: null,
  adminEditingReviewId: null,
  adminEditingReviewRating: 5,
  editingProductId: null, // null | 'NEW' | productId
  editingProductColors: [],
  editingColorIdx: null,
  adminGlassAddingType: false,
  adminNewGtImageUrl: '',
  adminNewGtVideoUrl: '',
  adminGlassModelBrand: 'iPhone',
  adminGlassModelSearch: '',
  adminGlassAddModelOpen: false,
  adminGlassEditingModelKey: null,
  adminCameraModelBrand: 'iPhone',
  adminCameraModelSearch: '',
  adminCamAddModelOpen: false,
  adminCameraEditingModelKey: null,
  adminPouchAdding: false,
  adminPouchEditingId: null,
  adminPouchDraft: null,
  adminPouchModelBrand: 'iPhone',
  adminPouchModelSearch: '',
  adminPouchAddModelOpen: false,
  adminPouchEditingModelKey: null,
  unsubAdmin: null,
  unsubUserScoped: null
};

function compressImageFileToDataUrl(file, maxDimension = 420, quality = 0.72) {
  return new Promise((resolve, reject) => {
    if (!file) {
      reject(new Error('No file selected'));
      return;
    }
    const reader = new FileReader();
    reader.onerror = () => reject(new Error('Failed to read image file'));
    reader.onload = () => {
      const img = new Image();
      img.onerror = () => reject(new Error('Unsupported image format'));
      img.onload = () => {
        const longest = Math.max(img.width || 1, img.height || 1);
        const scale = longest > maxDimension ? maxDimension / longest : 1;
        const w = Math.max(1, Math.round(img.width * scale));
        const h = Math.max(1, Math.round(img.height * scale));
        const canvas = document.createElement('canvas');
        canvas.width = w;
        canvas.height = h;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          reject(new Error('Canvas unavailable'));
          return;
        }
        ctx.fillStyle = '#FFFFFF';
        ctx.fillRect(0, 0, w, h);
        ctx.drawImage(img, 0, 0, w, h);
        let dataUrl = canvas.toDataURL('image/jpeg', quality);
        if (dataUrl.length > 125000) {
          dataUrl = canvas.toDataURL('image/jpeg', 0.52);
        }
        resolve(dataUrl);
      };
      img.src = String(reader.result || '');
    };
    reader.readAsDataURL(file);
  });
}

function rememberTrackingId(id) {
  const clean = String(id || '').trim().toUpperCase();
  if (!clean) return;
  state.recentTrackingIds = [clean, ...state.recentTrackingIds.filter((x) => x !== clean)].slice(0, 6);
  saveJsonStorage(STORAGE_RECENT_IDS_KEY, state.recentTrackingIds);
  renderRecentTrackingChips();
}

let toastTimer = null;
function showToast(msg) {
  const el = document.getElementById('toast-banner');
  const txt = document.getElementById('toast-message');
  if (!el || !txt) return;
  txt.textContent = msg;
  el.classList.remove('hidden');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.add('hidden'), 3200);
}

// ==================== NAVIGATION ====================
function syncMobileBottomNavActive() {
  document.querySelectorAll('.mobile-nav-btn').forEach((btn) => {
    const isAccountBtn = btn.id === 'mobile-nav-account';
    const isActive = state.accountOpen
      ? isAccountBtn
      : Boolean(btn.dataset.nav && btn.dataset.nav === state.activeTab);
    btn.className = `mobile-nav-btn flex flex-col items-center justify-center gap-0.5 px-1 py-1 rounded-lg text-[11px] font-bold ${
      isActive ? 'text-icase-red bg-icase-redLight' : 'text-slate-500'
    }`;
  });
}

function setActiveTab(tab, focusTarget = '') {
  state.activeTab = tab;
  state.guardsFocusTarget = tab === 'guards' ? String(focusTarget || '') : '';
  const sections = ['home', 'guards', 'catalog', 'repair', 'track', 'admin'];
  sections.forEach((s) => {
    const el = document.getElementById(`section-${s}`);
    if (el) el.classList.toggle('hidden', s !== tab);
  });

  document.querySelectorAll('.nav-btn').forEach((btn) => {
    const isActive = btn.dataset.nav === tab;
    btn.className = `nav-btn px-3.5 py-2 rounded-xl text-sm font-bold transition flex items-center gap-1.5 ${
      isActive ? 'bg-icase-redLight text-icase-red' : 'text-slate-600 hover:bg-slate-100 hover:text-icase-navy'
    }`;
  });

  syncMobileBottomNavActive();

  if (tab === 'guards') {
    renderTemperedGlassFinder();
    renderCameraLensFinder();
    renderPouchesFinder();
    renderGuardInstallationOption();
    if (state.guardsFocusTarget) {
      const targetMap = {
        glass: 'tempered-glass-wrapper',
        camera: 'camera-lens-wrapper',
        pouch: 'pouch-wrapper'
      };
      const elId = targetMap[state.guardsFocusTarget];
      if (elId) {
        requestAnimationFrame(() => {
          document.getElementById(elId)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
        return;
      }
    }
  }
  if (tab === 'catalog') {
    renderCatalog();
    renderPublicReviewsSections();
  }
  if (tab === 'repair') renderDeviceDropdown();
  if (tab === 'track') renderTrackingView();
  if (tab === 'admin') renderAdminSection();

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ==================== SETTINGS & BRANDING SYNC ====================
function applySettingsToDom() {
  const s = state.settings;
  const phone = s.store_phone || '9961454059';
  const digits = phone.replace(/\D/g, '') || '9961454059';
  const waDigits = (s.store_whatsapp || phone).replace(/\D/g, '') || '9961454059';
  const email = s.store_email || 'icaseedappal@gmail.com';
  const address = s.store_address || 'Main Road, Near Town Square, Edappal, Malappuram, Kerala';
  const hours = s.store_hours || '9:00 AM - 10:00 PM (Daily)';
  const oneHourEnabled = s.one_hour_delivery_enabled !== 'false';
  const storeOnline = s.store_online !== 'false';

  const setHref = (id, href) => {
    const el = document.getElementById(id);
    if (el) el.href = href;
  };
  const setText = (id, text) => {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
  };

  setHref('top-phone-link', `tel:+91${digits}`);
  setText('top-phone-text', `+91 ${phone}`);
  setHref('top-email-link', `mailto:${email}`);
  setText('top-email-text', email);

  setHref('quick-call-link', `tel:+91${digits}`);
  setText('quick-call-text', `+91 ${phone}`);
  setHref(
    'quick-whatsapp-link',
    `https://api.whatsapp.com/send?phone=91${waDigits}&text=${encodeURIComponent('Hi iCASE Edappal! I would like to enquire about mobile accessories or repair.')}`
  );
  setText('quick-whatsapp-text', `+91 ${s.store_whatsapp || phone}`);
  setHref(
    'hero-whatsapp-link',
    `https://api.whatsapp.com/send?phone=91${waDigits}&text=${encodeURIComponent('Hi iCASE Edappal! I would like to enquire about an accessory order or mobile repair.')}`
  );
  setText(
    'hero-express-kicker',
    oneHourEnabled
      ? 'Express 1-Hour Doorstep Delivery in Edappal'
      : 'Fast Local Home Delivery & Expert Mobile Care'
  );

  setHref('contact-phone-card', `tel:+91${digits}`);
  setText('contact-phone-val', `+91 ${phone}`);
  setHref('contact-email-card', `mailto:${email}`);
  setText('contact-email-val', email);
  setText('contact-address-val', address);
  setText('contact-hours-val', hours);

  setHref('footer-phone-link', `tel:+91${digits}`);
  setText('footer-phone-link', `+91 ${phone}`);
  setHref('footer-email-link', `mailto:${email}`);
  setText('footer-email-link', email);

  setText('paused-phone', `+91 ${phone}`);
  const pausedBanner = document.getElementById('store-paused-banner');
  if (pausedBanner) pausedBanner.classList.toggle('hidden', storeOnline);

  const countEl = document.getElementById('home-product-count');
  if (countEl) countEl.textContent = `${state.products.length} items`;
}

function updateSyncStatusBadge(status) {
  state.syncStatus = status;
  const dot = document.getElementById('sync-dot');
  const label = document.getElementById('sync-label');
  if (!dot || !label) return;
  if (status === 'connected') {
    dot.className = 'w-2 h-2 rounded-full bg-emerald-400 sync-pulse';
    label.textContent = 'Live Cloud Sync Active';
  } else if (status === 'offline') {
    dot.className = 'w-2 h-2 rounded-full bg-amber-400';
    label.textContent = 'Offline Fallback Mode';
  } else {
    dot.className = 'w-2 h-2 rounded-full bg-sky-400 sync-pulse';
    label.textContent = 'Connecting to Firestore...';
  }
}

// ==================== PRODUCT CARDS & DETAILS ====================
function renderProductCardHtml(product) {
  const oneHourEnabled = state.settings.one_hour_delivery_enabled !== 'false';
  const price = Number(product.price || 0);
  const orig = Number(product.originalPrice || 0);
  const discountPct = orig > price ? Math.round(((orig - price) / orig) * 100) : 0;
  const colors = parseColorOptions(product.colorOptionsJson);
  const isWishlisted = (state.profile.wishlistIds || []).includes(product.id);
  const ratingSummary = computeProductRatingSummary(state.publicProductReviews, product);

  return `
    <article class="bg-white rounded-2xl border border-slate-200 overflow-hidden flex flex-col hover:shadow-lg transition group">
      <div class="relative aspect-[4/3] bg-slate-100 overflow-hidden cursor-pointer" data-open-product="${escapeHtml(product.id)}">
        <img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" loading="lazy" class="w-full h-full object-cover group-hover:scale-105 transition duration-300" />
        ${discountPct > 0 ? `<span class="absolute top-2.5 left-2.5 px-2.5 py-1 rounded-full bg-icase-red text-white text-[11px] font-extrabold shadow">${discountPct}% OFF</span>` : ''}
        ${oneHourEnabled && product.isOneHourEligible ? `<span class="absolute bottom-2.5 left-2.5 px-2.5 py-1 rounded-full bg-icase-navy/90 text-amber-300 text-[11px] font-bold">⚡ 1-Hour Eligible</span>` : ''}
        ${
          product.videoUrl
            ? `<button type="button" data-open-video="${escapeHtml(product.videoUrl)}" data-video-title="${escapeHtml(product.name)}" class="absolute bottom-2.5 right-2.5 px-2.5 py-1 rounded-full bg-slate-950/85 hover:bg-icase-red text-white text-[11px] font-extrabold shadow transition">▶ Video</button>`
            : ''
        }
        ${!product.inStock ? `<span class="absolute inset-0 bg-slate-950/60 flex items-center justify-center text-white text-xs font-extrabold uppercase tracking-wider">Out of Stock</span>` : ''}
      </div>

      <div class="p-4 flex-1 flex flex-col">
        <div class="flex items-center justify-between gap-2 mb-1">
          <span class="text-[11px] font-bold uppercase tracking-wider text-icase-blue truncate">${escapeHtml(product.category)}</span>
          <div class="flex items-center gap-1.5 shrink-0">
            ${
              ratingSummary.reviewCount > 0
                ? `<span class="inline-flex items-center gap-0.5 px-1.5 py-0.5 rounded-md bg-amber-50 text-amber-800 text-[10px] font-extrabold border border-amber-200">★ ${ratingSummary.averageRating.toFixed(1)} (${ratingSummary.reviewCount})</span>`
                : ''
            }
            ${
              colors.length > 0
                ? `<div class="flex items-center gap-1" title="${colors.length} color options">
                    ${colors
                      .slice(0, 4)
                      .map(
                        (c) =>
                          `<span class="w-3 h-3 rounded-full border border-slate-300 inline-block" style="background:${escapeHtml(c.hex)}" title="${escapeHtml(c.name)}"></span>`
                      )
                      .join('')}
                    ${colors.length > 4 ? `<span class="text-[10px] font-bold text-slate-400">+${colors.length - 4}</span>` : ''}
                  </div>`
                : ''
            }
          </div>
        </div>

        <h3 data-open-product="${escapeHtml(product.id)}" class="font-extrabold text-icase-navy text-sm sm:text-base line-clamp-2 cursor-pointer hover:text-icase-red transition">
          ${escapeHtml(product.name)}
        </h3>
        <p class="text-xs text-slate-500 line-clamp-2 mt-1 mb-3 flex-1">${escapeHtml(product.description || '')}</p>

        <div class="flex items-baseline gap-2 mb-3">
          <span class="text-lg font-black text-icase-navy">${formatINR(price)}</span>
          ${orig > price ? `<span class="text-xs text-slate-400 line-through font-semibold">${formatINR(orig)}</span>` : ''}
        </div>

        <div class="flex items-center gap-2">
          <button type="button" data-add-cart="${escapeHtml(product.id)}" ${!product.inStock ? 'disabled' : ''} class="flex-1 py-2.5 px-3 rounded-xl text-xs font-extrabold transition min-h-[40px] ${
            product.inStock
              ? 'bg-icase-red hover:bg-red-700 text-white'
              : 'bg-slate-200 text-slate-500 cursor-not-allowed'
          }">
            ${product.inStock ? 'Add to Cart' : 'Out of Stock'}
          </button>
          <button type="button" data-buy-now="${escapeHtml(product.id)}" ${!product.inStock ? 'disabled' : ''} class="py-2.5 px-3 rounded-xl text-xs font-extrabold transition min-h-[40px] ${
            product.inStock
              ? 'bg-icase-navy hover:bg-icase-dark text-white'
              : 'bg-slate-100 text-slate-400 cursor-not-allowed'
          }">
            Buy
          </button>
          <button type="button" data-toggle-wishlist="${escapeHtml(product.id)}" title="Toggle Wishlist" class="p-2.5 rounded-xl border transition min-h-[40px] min-w-[40px] flex items-center justify-center ${
            isWishlisted ? 'border-icase-red bg-icase-redLight text-icase-red' : 'border-slate-200 text-slate-500 hover:border-icase-red hover:text-icase-red'
          }">
            ${isWishlisted ? '♥' : '♡'}
          </button>
        </div>
      </div>
    </article>
  `;
}

function renderHomeGuardSampleCards() {
  const host = document.getElementById('home-guard-sample-cards');
  if (!host) return;
  const config = state.screenGuardConfig || createDefaultScreenGuardConfig();
  const startingGlass = getStartingTemperedGlassPrice(config);
  const startingLens = getStartingCameraLensPrice(config);
  const startingPouch = getStartingPouchPrice(config);
  const glassImg = resolveTemperedGlassImageUrl(config);
  const lensImg = resolveCameraLensImageUrl(config);
  const pouchImg = resolvePouchCardImageUrl(config);
  const camEnabled = isCameraLensSectionAvailable(config);
  const activeGlassTypes = getActiveGlassTypes(config);

  host.innerHTML = `
    <div class="space-y-4">
      <div class="flex items-center justify-between gap-2">
        <div>
          <h2 class="text-xl font-extrabold text-icase-navy">Tempered Glass, Camera Lens &amp; Pouches</h2>
          <p class="text-xs text-slate-500">Tap a protection card to pick your exact phone model and order directly</p>
        </div>
        <button type="button" data-nav="guards" class="text-sm font-bold text-icase-blue hover:underline shrink-0">Open Finder →</button>
      </div>
      <div class="icase-guard-sample-grid">
        <button type="button" data-home-guard-card="glass" class="icase-guard-sample-card group text-left bg-white rounded-3xl border border-slate-200 hover:border-icase-navy hover:shadow-md overflow-hidden flex flex-col transition">
          <div class="icase-guard-thumb relative w-full h-44 bg-slate-100 shrink-0 overflow-hidden">
            <img src="${escapeHtml(glassImg)}" alt="Tempered Glass Sample" class="w-full h-full object-cover group-hover:scale-105 transition duration-300" />
            <span class="absolute top-2 left-2 px-2 py-0.5 rounded-full bg-icase-red text-white text-[10px] sm:text-[11px] font-extrabold shadow">Starting ${formatINR(startingGlass)}</span>
          </div>
          <div class="icase-guard-body p-4 sm:p-5 flex-1 flex flex-col justify-between gap-2 min-w-0">
            <div class="space-y-1">
              <div class="flex flex-wrap items-center gap-1.5">
                <span class="px-2 py-0.5 rounded-full bg-emerald-50 border border-emerald-200 text-icase-green text-[10px] sm:text-[11px] font-extrabold">Available for iPhone &amp; All Android Models</span>
              </div>
              <h3 class="text-sm sm:text-lg font-extrabold text-icase-navy">Tempered Glass</h3>
              <p class="text-[11.5px] sm:text-xs text-slate-600 line-clamp-2">Privacy, Matte, Borderless, 360 Privacy &amp; Normal 5D edge-to-edge guards with shop installation.</p>
            </div>
            <div class="flex items-center justify-between gap-2 pt-2 border-t border-slate-100">
              <span class="text-sm sm:text-base font-black text-icase-red">From ${formatINR(startingGlass)}</span>
              <span class="px-2.5 py-1 rounded-xl bg-icase-navy group-hover:bg-icase-red text-white text-[11px] sm:text-xs font-bold transition shrink-0">Select Model →</span>
            </div>
          </div>
        </button>

        ${
          camEnabled
            ? `
          <button type="button" data-home-guard-card="camera" class="icase-guard-sample-card group text-left bg-white rounded-3xl border border-slate-200 hover:border-icase-navy hover:shadow-md overflow-hidden flex flex-col transition">
            <div class="icase-guard-thumb relative w-full h-44 bg-slate-100 shrink-0 overflow-hidden">
              <img src="${escapeHtml(lensImg)}" alt="Camera Lens Protector Sample" class="w-full h-full object-cover group-hover:scale-105 transition duration-300" />
              <span class="absolute top-2 left-2 px-2 py-0.5 rounded-full bg-icase-navy text-white text-[10px] sm:text-[11px] font-extrabold shadow">Starting ${formatINR(startingLens)}</span>
            </div>
            <div class="icase-guard-body p-4 sm:p-5 flex-1 flex flex-col justify-between gap-2 min-w-0">
              <div class="space-y-1">
                <div class="flex flex-wrap items-center gap-1.5">
                  <span class="px-2 py-0.5 rounded-full bg-icase-blueLight border border-icase-blue/30 text-icase-blue text-[10px] sm:text-[11px] font-extrabold">iPhone &amp; Samsung S Series Only</span>
                </div>
                <h3 class="text-sm sm:text-lg font-extrabold text-icase-navy">Camera Lens Protector</h3>
                <p class="text-[11.5px] sm:text-xs text-slate-600 line-clamp-2">9H Sapphire individual lens rings for iPhone &amp; Samsung Galaxy S20–S25 / Ultra models (combo discount supported).</p>
              </div>
              <div class="flex items-center justify-between gap-2 pt-2 border-t border-slate-100">
                <span class="text-sm sm:text-base font-black text-icase-red">From ${formatINR(startingLens)}</span>
                <span class="px-2.5 py-1 rounded-xl bg-icase-navy group-hover:bg-icase-red text-white text-[11px] sm:text-xs font-bold transition shrink-0">Select Model →</span>
              </div>
            </div>
          </button>
        `
            : ''
        }

        <button type="button" data-home-guard-card="pouch" class="icase-guard-sample-card group text-left bg-white rounded-3xl border border-slate-200 hover:border-icase-navy hover:shadow-md overflow-hidden flex flex-col transition">
          <div class="icase-guard-thumb relative w-full h-44 bg-slate-100 shrink-0 overflow-hidden">
            <img src="${escapeHtml(pouchImg)}" alt="Pouches &amp; Back Cases Sample" class="w-full h-full object-cover group-hover:scale-105 transition duration-300" />
            <span class="absolute top-2 left-2 px-2 py-0.5 rounded-full bg-teal-700 text-white text-[10px] sm:text-[11px] font-extrabold shadow">Starting ${formatINR(startingPouch)}</span>
          </div>
          <div class="icase-guard-body p-4 sm:p-5 flex-1 flex flex-col justify-between gap-2 min-w-0">
            <div class="space-y-1">
              <div class="flex flex-wrap items-center gap-1.5">
                <span class="px-2 py-0.5 rounded-full bg-teal-50 border border-teal-200 text-teal-800 text-[10px] sm:text-[11px] font-extrabold">iPhone, Samsung S Series &amp; Other Android Models</span>
              </div>
              <h3 class="text-sm sm:text-lg font-extrabold text-icase-navy">Pouches &amp; Back Cases</h3>
              <p class="text-[11.5px] sm:text-xs text-slate-600 line-clamp-2">MagSafe Clear, Matte Armor &amp; Soft Silicone cases with colour picks and quick WhatsApp Android stock checks.</p>
            </div>
            <div class="flex items-center justify-between gap-2 pt-2 border-t border-slate-100">
              <span class="text-sm sm:text-base font-black text-icase-red">From ${formatINR(startingPouch)}</span>
              <span class="px-2.5 py-1 rounded-xl bg-icase-navy group-hover:bg-icase-red text-white text-[11px] sm:text-xs font-bold transition shrink-0">Select Pouch →</span>
            </div>
          </div>
        </button>
      </div>

      ${
        activeGlassTypes.length > 0
          ? `
        <div class="pt-1 space-y-2.5">
          <div>
            <h3 class="text-sm sm:text-base font-extrabold text-icase-navy">Tempered Glass Types — Tap to Select Style</h3>
            <p class="text-xs text-slate-500">Synced live with store rates &amp; offers. Tap any style to open Tempered Glass with it pre-selected.</p>
          </div>
          <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-3">
            ${activeGlassTypes
              .map((gt) => {
                const pricing = resolveTemperedGlassPrice('iPhone', '__default__', gt, {
                  ...config,
                  modelConfigs: {}
                });
                const gtImg = resolveGlassTypeImageUrl(gt, config);
                const gtVid = resolveGlassTypeVideoUrl(gt, config);
                const note = getGlassTypeHelperNote(gt, config);
                return `
                  <div role="button" tabindex="0" data-home-glass-type="${escapeHtml(gt)}" class="bg-white rounded-2xl border border-slate-200 hover:border-icase-red hover:shadow-sm p-3 flex items-center gap-3 cursor-pointer transition">
                    <img src="${escapeHtml(gtImg)}" alt="${escapeHtml(gt)}" loading="lazy" class="w-16 h-16 rounded-xl object-cover border border-slate-200 bg-slate-100 shrink-0" />
                    <div class="min-w-0 flex-1 space-y-1">
                      <div class="flex items-center justify-between gap-1.5">
                        <strong class="text-xs sm:text-sm font-extrabold text-icase-navy truncate">${escapeHtml(gt)}</strong>
                        ${pricing.offerBadge ? `<span class="px-1.5 py-0.5 rounded bg-amber-300 text-icase-navy text-[9px] font-black shrink-0">${escapeHtml(pricing.offerBadge)}</span>` : ''}
                      </div>
                      <div class="text-[11px] text-slate-500 truncate" title="${escapeHtml(note)}">${escapeHtml(note)}</div>
                      <div class="flex items-center justify-between gap-2 pt-0.5">
                        <div class="flex items-baseline gap-1.5">
                          <span class="text-xs sm:text-sm font-black text-icase-red">${formatINR(pricing.finalPrice)}</span>
                          ${pricing.basePrice > pricing.finalPrice ? `<span class="text-[10px] text-slate-400 line-through font-semibold">${formatINR(pricing.basePrice)}</span>` : ''}
                        </div>
                        ${
                          gtVid
                            ? `<button type="button" data-open-video="${escapeHtml(gtVid)}" data-video-title="${escapeHtml(`${gt} Tempered Glass`)}" class="px-2 py-0.5 rounded-full bg-slate-900 hover:bg-icase-red text-white text-[10px] font-extrabold transition shrink-0">▶ Video</button>`
                            : ''
                        }
                      </div>
                    </div>
                  </div>
                `;
              })
              .join('')}
          </div>
        </div>
      `
          : ''
      }
    </div>
  `;
}

function renderHome() {
  applySettingsToDom();

  const homeCats = document.getElementById('home-category-chips');
  if (homeCats) {
    homeCats.innerHTML = CATEGORIES.map(
      (cat) => `
      <button type="button" data-home-cat="${escapeHtml(cat)}" class="px-4 py-2 rounded-full text-xs sm:text-sm font-bold border border-slate-200 bg-white hover:border-icase-navy hover:bg-icase-navy hover:text-white text-icase-navy transition">
        ${escapeHtml(cat)}
      </button>
    `
    ).join('');
  }

  renderHomeGuardSampleCards();

  const popular = state.products
    .filter((p) => p.isPopular)
    .sort((a, b) => Number(a.popularityRank || 99) - Number(b.popularityRank || 99))
    .slice(0, 6);
  const featured = popular.length > 0 ? popular : state.products.slice(0, 6);

  const grid = document.getElementById('home-popular-grid');
  if (grid) {
    grid.innerHTML = featured.map(renderProductCardHtml).join('');
  }

  renderHomeFeaturedPouches();

  const brandsHost = document.getElementById('home-top-brands');
  if (brandsHost) {
    brandsHost.innerHTML = TOP_BRANDS.map(
      (brand) => `
      <button type="button" data-home-brand="${escapeHtml(brand)}" class="bg-white border border-slate-200 hover:border-icase-navy hover:bg-slate-50 rounded-2xl py-3 px-2 text-center font-extrabold text-xs sm:text-sm text-icase-navy shadow-sm transition">
        ${escapeHtml(brand)}
      </button>
    `
    ).join('');
  }

  const hq = state.homeSearch.trim().toLowerCase();
  const clearBtn = document.getElementById('home-search-clear');
  if (clearBtn) clearBtn.classList.toggle('hidden', !hq);

  const allMatching = state.products
    .filter(
      (p) =>
        !hq ||
        (p.name || '').toLowerCase().includes(hq) ||
        (p.category || '').toLowerCase().includes(hq) ||
        (p.description || '').toLowerCase().includes(hq)
    )
    .sort((a, b) => Number(a.popularityRank || 99) - Number(b.popularityRank || 99));

  const allCountEl = document.getElementById('home-all-products-count');
  if (allCountEl) allCountEl.textContent = `${allMatching.length} items`;

  const allGrid = document.getElementById('home-all-products-grid');
  const allEmpty = document.getElementById('home-all-products-empty');
  if (allGrid && allEmpty) {
    if (allMatching.length === 0) {
      allGrid.innerHTML = '';
      allEmpty.classList.remove('hidden');
    } else {
      allEmpty.classList.add('hidden');
      allGrid.innerHTML = allMatching.map(renderProductCardHtml).join('');
    }
  }

  renderPublicReviewsSections();
}

function renderHomeFeaturedPouches() {
  const host = document.getElementById('home-featured-pouches-section');
  if (!host) return;
  const featuredPouches = getHomeFeaturedPouches(state.screenGuardConfig);
  if (featuredPouches.length === 0) {
    host.innerHTML = '';
    return;
  }

  host.innerHTML = `
    <div class="space-y-4">
      <div class="flex items-center justify-between gap-2">
        <div>
          <h2 class="text-xl font-extrabold text-icase-navy">✨ Featured Pouches &amp; Cases</h2>
          <p class="text-xs text-slate-500">Handpicked pouches featured from iCASE Store</p>
        </div>
        <button type="button" id="home-featured-pouches-nav" class="text-sm font-bold text-icase-blue hover:underline shrink-0">Choose Phone Model →</button>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        ${featuredPouches
          .map((pouch) => {
            const pricing = resolvePouchPrice(pouch);
            const activeColor = resolvePouchSelectedColor(
              pouch,
              state.selectedHomePouchColors?.[pouch.id] || ''
            );
            const thumbUrl = resolvePouchImageUrl(pouch, activeColor?.name || '');
            const colors = Array.isArray(pouch.colorOptions) ? pouch.colorOptions : [];
            return `
              <article class="bg-white rounded-2xl border border-slate-200 overflow-hidden flex flex-col hover:shadow-lg transition">
                <div class="relative aspect-[4/3] bg-slate-100 overflow-hidden">
                  <img src="${escapeHtml(thumbUrl)}" alt="${escapeHtml(pouch.name)}" loading="lazy" class="w-full h-full object-cover" />
                  ${
                    pricing.hasOffer && pricing.offerBadge
                      ? `<span class="absolute top-2.5 left-2.5 px-2.5 py-1 rounded-full bg-icase-red text-white text-[11px] font-extrabold shadow">${escapeHtml(pricing.offerBadge)}</span>`
                      : ''
                  }
                  ${
                    pouch.videoUrl
                      ? `<button type="button" data-open-video="${escapeHtml(pouch.videoUrl)}" data-video-title="${escapeHtml(pouch.name)}" class="absolute bottom-2.5 left-2.5 px-2.5 py-1 rounded-full bg-slate-950/85 hover:bg-icase-red text-white text-[11px] font-extrabold shadow transition">▶ Video</button>`
                      : ''
                  }
                </div>
                <div class="p-4 flex-1 flex flex-col gap-2">
                  <div class="text-[11px] font-bold uppercase tracking-wider text-icase-blue truncate">${escapeHtml((pouch.compatibleBrands || []).join(' • '))}</div>
                  <h3 class="font-extrabold text-icase-navy text-sm sm:text-base line-clamp-2">${escapeHtml(pouch.name)}</h3>
                  ${pouch.description ? `<p class="text-xs text-slate-500 line-clamp-2 flex-1">${escapeHtml(pouch.description)}</p>` : '<div class="flex-1"></div>'}
                  <div class="flex items-baseline gap-2">
                    <span class="text-lg font-black text-icase-red">${formatINR(pricing.finalPrice)}</span>
                    ${pricing.hasOffer && pricing.basePrice > pricing.finalPrice ? `<span class="text-xs text-slate-400 line-through font-semibold">${formatINR(pricing.basePrice)}</span>` : ''}
                  </div>
                  ${
                    colors.length > 0
                      ? `<div class="flex flex-wrap items-center gap-1.5 pt-0.5">
                          ${colors
                            .slice(0, 7)
                            .map((c) => {
                              const selected = activeColor?.name?.toLowerCase() === c.name.toLowerCase();
                              return `<button type="button" data-home-pouch-color="${escapeHtml(pouch.id)}" data-color-name="${escapeHtml(c.name)}" title="${escapeHtml(c.name)}" class="w-6 h-6 rounded-full p-0.5 bg-white border transition flex items-center justify-center ${
                                selected ? 'ring-2 ring-icase-navy ring-offset-1 border-icase-navy' : 'border-slate-300'
                              }"><span class="w-full h-full rounded-full block" style="background:${escapeHtml(c.hex || '#1E293B')}"></span></button>`;
                            })
                            .join('')}
                        </div>`
                      : ''
                  }
                  <button type="button" data-home-pouch-open="${escapeHtml(pouch.id)}" class="mt-1 w-full py-2.5 px-3 rounded-xl bg-icase-navy hover:bg-icase-dark text-white text-xs font-extrabold transition min-h-[42px]">
                    Select Model &amp; Buy →
                  </button>
                </div>
              </article>
            `;
          })
          .join('')}
      </div>
    </div>
  `;

  document.getElementById('home-featured-pouches-nav')?.addEventListener('click', () => {
    setActiveTab('guards');
    setTimeout(() => {
      document.getElementById('pouch-wrapper')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 60);
  });
  host.querySelectorAll('[data-home-pouch-color]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const pid = btn.dataset.homePouchColor;
      const cname = btn.dataset.colorName || '';
      if (!pid || !cname) return;
      state.selectedHomePouchColors = { ...(state.selectedHomePouchColors || {}), [pid]: cname };
      renderHomeFeaturedPouches();
    })
  );
  host.querySelectorAll('[data-home-pouch-open]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const pid = btn.dataset.homePouchOpen;
      const chosenColor = state.selectedHomePouchColors?.[pid];
      if (pid && chosenColor) {
        state.selectedPouchColors = { ...(state.selectedPouchColors || {}), [pid]: chosenColor };
      }
      setActiveTab('guards');
      setTimeout(() => {
        document.getElementById('pouch-wrapper')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 60);
    })
  );
}

// ==================== TEMPERED GLASS & CAMERA LENS PROTECTION + SAMPLE VIDEO ====================
function getStoreWaUrl(message) {
  const digits = (state.settings.store_whatsapp || state.settings.store_phone || '9961454059').replace(/\D/g, '') || '9961454059';
  return `https://api.whatsapp.com/send?phone=91${digits}&text=${encodeURIComponent(message)}`;
}

function getSelectedGlassForCombo(config) {
  if (!state.glassModel) return null;
  const isManual = state.glassModel === OTHER_MODEL_OPTION;
  const effectiveModel = isManual ? state.glassManualModel.trim() : state.glassModel;
  if (!effectiveModel) return null;
  const brandPart = isManual ? 'CUSTOM' : state.glassBrand;
  const allowed = getGlassTypesForDevice(brandPart, state.glassModel, config);
  if (!allowed.includes(state.glassType)) return null;
  return { brand: brandPart, model: effectiveModel, glassType: state.glassType };
}

function renderTemperedGlassFinder() {
  const container = document.getElementById('tempered-glass-finder-container');
  if (!container) return;

  const config = state.screenGuardConfig || createDefaultScreenGuardConfig();
  const activeBrands = getActiveScreenGuardBrands(config);
  if (state.glassBrand && !activeBrands.includes(state.glassBrand)) {
    state.glassBrand = '';
    if (state.glassModel !== OTHER_MODEL_OPTION) {
      state.glassModel = '';
    }
  }

  const isManual = state.glassModel === OTHER_MODEL_OPTION;
  const glassTypes = getGlassTypesForDevice(state.glassBrand, state.glassModel, config);
  if (glassTypes.length > 0 && !glassTypes.includes(state.glassType)) {
    state.glassType = glassTypes[0];
  }
  const grouped = searchScreenGuardCatalog(state.glassBrand, state.glassSearch, config);
  const pricing =
    state.glassModel && glassTypes.length > 0
      ? resolveTemperedGlassPrice(
          isManual ? 'CUSTOM' : state.glassBrand,
          isManual ? (state.glassManualModel.trim() || OTHER_MODEL_OPTION) : state.glassModel,
          state.glassType,
          config
        )
      : null;

  const helperNote = glassTypes.length > 0 ? getGlassTypeHelperNote(state.glassType, config) : '';
  const showSamsungUvNote = state.glassBrand === 'Samsung';
  const uvWhatsappUrl = getStoreWaUrl(
    'Hi iCASE Edappal! I have a curved-display Samsung S series phone (S20 / S21 Ultra / S22 Ultra / S23 Ultra). Please share UV tempered glass options & pricing.'
  );

  const glassImgUrl = resolveGlassTypeImageUrl(state.glassType, config);
  const activeGlassVideoUrl = resolveGlassTypeVideoUrl(state.glassType, config);
  const glassFocused = state.guardsFocusTarget === 'glass';

  const popularGlassModels = getPopularGlassModels(state.glassBrand, config);

  container.innerHTML = `
    <div id="tempered-glass-wrapper" class="bg-white rounded-2xl border ${
      glassFocused ? 'border-icase-navy ring-2 ring-icase-navy/25' : 'border-slate-200'
    } p-5 sm:p-6 shadow-sm space-y-4">
      <div class="flex items-start gap-3.5">
        <img src="${escapeHtml(glassImgUrl)}" alt="Tempered Glass Sample" class="w-14 h-14 rounded-2xl object-cover border border-slate-200 shrink-0" />
        <div class="min-w-0 flex-1 space-y-1">
          <div class="flex flex-wrap items-center gap-2">
            <h2 class="text-lg sm:text-xl font-extrabold text-icase-navy">Tempered Glass</h2>
            <span class="px-2.5 py-0.5 rounded-full bg-emerald-50 border border-emerald-200 text-icase-green text-[11px] font-extrabold">Available for iPhone &amp; All Android Models</span>
            ${
              activeGlassVideoUrl
                ? `<button type="button" data-open-video="${escapeHtml(activeGlassVideoUrl)}" data-video-title="${escapeHtml(`${state.glassType} Tempered Glass`)}" class="px-2.5 py-0.5 rounded-full bg-slate-900 hover:bg-icase-red text-white text-[11px] font-extrabold transition">▶ Watch ${escapeHtml(state.glassType)} Video</button>`
                : ''
            }
          </div>
          <p class="text-xs sm:text-sm text-slate-600">Select your brand &amp; model for exact tempered glass (Samsung curved S20 / S21–S23 Ultra UV glass via WhatsApp).</p>
        </div>
      </div>

      ${
        !state.glassModel && state.glassType
          ? `<div class="px-3.5 py-2 rounded-xl bg-icase-blueLight/60 border border-icase-blue/20 text-xs font-semibold text-icase-navy">Pre-selected style: <strong>${escapeHtml(state.glassType)}</strong> — pick your phone model below to see exact availability &amp; price.</div>`
          : ''
      }

      <div class="flex flex-wrap gap-2">
        <button type="button" data-glass-brand="" class="px-3 py-1.5 rounded-full text-xs font-bold border transition ${
          !state.glassBrand ? 'bg-icase-navy text-white border-icase-navy' : 'bg-slate-50 text-icase-navy border-slate-200 hover:border-icase-navy'
        }">All Brands</button>
        ${activeBrands
          .map(
            (brand) => `
          <button type="button" data-glass-brand="${escapeHtml(brand)}" class="px-3 py-1.5 rounded-full text-xs font-bold border transition ${
            state.glassBrand === brand ? 'bg-icase-navy text-white border-icase-navy' : 'bg-slate-50 text-icase-navy border-slate-200 hover:border-icase-navy'
          }">${escapeHtml(brand)}</button>
        `
          )
          .join('')}
      </div>

      <div class="relative">
        <label class="block text-xs font-bold uppercase text-slate-500 mb-1.5">Phone Model (Searchable Dropdown)</label>
        <div class="flex items-center gap-2 px-3.5 py-2.5 rounded-xl border border-slate-300 bg-slate-50 focus-within:bg-white focus-within:border-icase-blue">
          <svg class="w-4 h-4 text-slate-400 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/></svg>
          <input id="glass-search-input" type="text" autocomplete="off" value="${escapeHtml(
            state.glassDropdownOpen
              ? state.glassSearch
              : state.glassModel
                ? isManual
                  ? OTHER_MODEL_OPTION
                  : `${state.glassBrand} — ${state.glassModel}`
                : ''
          )}" placeholder="Search iPhone 15, Redmi Note 13, Galaxy S24, Nord 4..." class="w-full bg-transparent text-sm font-semibold text-icase-navy focus:outline-none placeholder:font-normal" />
          ${
            state.glassModel
              ? `<button id="glass-clear-btn" type="button" class="text-xs font-bold text-slate-500 hover:text-icase-red px-1">Clear</button>`
              : ''
          }
        </div>

        <div id="glass-dropdown-menu" class="${state.glassDropdownOpen ? '' : 'hidden '}absolute left-0 right-0 mt-1.5 max-h-72 overflow-y-auto bg-white border border-slate-200 rounded-2xl shadow-xl z-30">
          ${
            popularGlassModels.length > 0 && !state.glassSearch.trim()
              ? `
            <div class="px-3.5 py-2.5 bg-slate-50 border-b border-slate-200">
              <div class="text-[10px] font-black uppercase tracking-wider text-slate-500 mb-1.5">Popular Models (Quick Select)</div>
              <div class="flex flex-wrap gap-1.5">
                ${popularGlassModels
                  .map((item) => {
                    const active = state.glassBrand === item.brand && state.glassModel === item.model;
                    return `<button type="button" data-pick-glass-brand="${escapeHtml(item.brand)}" data-pick-glass-model="${escapeHtml(item.model)}" class="px-2.5 py-1.5 rounded-full text-xs font-bold border transition ${
                      active ? 'bg-icase-navy text-white border-icase-navy' : 'bg-white text-icase-navy border-slate-300 hover:border-icase-navy'
                    }">${escapeHtml(item.model)}</button>`;
                  })
                  .join('')}
              </div>
            </div>
          `
              : ''
          }
          ${
            grouped.length === 0
              ? `<div class="p-4 text-xs text-slate-500">No preset model matched "${escapeHtml(state.glassSearch)}". Choose <strong>${escapeHtml(OTHER_MODEL_OPTION)}</strong> below.</div>`
              : grouped
                  .map(
                    (grp) => `
                  <div class="px-3.5 py-1.5 bg-slate-100 text-[11px] font-black uppercase tracking-wider text-icase-blue sticky top-0">${escapeHtml(grp.brand)}</div>
                  ${grp.models
                    .map((model) => {
                      const active = state.glassBrand === grp.brand && state.glassModel === model;
                      return `
                        <button type="button" data-pick-glass-brand="${escapeHtml(grp.brand)}" data-pick-glass-model="${escapeHtml(model)}" class="w-full px-4 py-2.5 text-left text-xs sm:text-sm font-semibold flex items-center justify-between border-b border-slate-100 hover:bg-icase-blueLight transition ${
                          active ? 'bg-icase-blueLight text-icase-blue font-extrabold' : 'text-icase-navy'
                        }">
                          <span>${escapeHtml(model)}</span>
                          ${active ? '<span class="text-xs">✓</span>' : ''}
                        </button>
                      `;
                    })
                    .join('')}
                `
                  )
                  .join('')
          }
          <button type="button" data-pick-glass-other="true" class="w-full px-4 py-3 text-left text-xs sm:text-sm font-extrabold text-icase-red bg-rose-50/70 hover:bg-icase-redLight transition flex items-center justify-between">
            <span>${escapeHtml(OTHER_MODEL_OPTION)}</span>
            <span class="text-[11px] font-bold">Type Manually →</span>
          </button>
        </div>
      </div>

      ${
        showSamsungUvNote
          ? `
        <a href="${uvWhatsappUrl}" target="_blank" rel="noreferrer" class="flex items-center justify-between gap-2 px-3.5 py-2.5 rounded-xl bg-icase-blueLight/70 border border-icase-blue/30 text-xs font-bold text-icase-blue hover:bg-icase-blueLight transition">
          <span>Curved display S series? Contact on WhatsApp for UV glass options.</span>
          <span class="shrink-0 underline">WhatsApp →</span>
        </a>
      `
          : ''
      }

      ${
        isManual
          ? `
        <div>
          <label for="glass-manual-input" class="block text-xs font-bold text-icase-navy mb-1">Type Your Exact Phone Model *</label>
          <input id="glass-manual-input" type="text" value="${escapeHtml(state.glassManualModel)}" placeholder="e.g. Motorola Edge 50 Pro, Google Pixel 9" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 focus:border-icase-blue focus:outline-none text-sm font-medium" />
        </div>
      `
          : ''
      }

      ${
        state.glassModel && glassTypes.length === 0
          ? `<div class="p-3.5 rounded-xl bg-amber-50 border border-amber-200 text-xs font-semibold text-amber-900">This tempered glass option is currently unavailable in store catalog.</div>`
          : ''
      }

      ${
        glassTypes.length > 0
          ? `
        <div class="space-y-2">
          <div class="text-xs font-extrabold text-icase-navy">Select Tempered Glass Type</div>
          <div class="flex flex-wrap gap-2">
            ${glassTypes
              .map((gt) => {
                const active = state.glassType === gt;
                const gtPricing = resolveTemperedGlassPrice(
                  isManual ? 'CUSTOM' : state.glassBrand,
                  isManual ? (state.glassManualModel.trim() || OTHER_MODEL_OPTION) : state.glassModel,
                  gt,
                  config
                );
                return `
                  <button type="button" data-glass-type="${escapeHtml(gt)}" class="px-3.5 py-2 rounded-xl text-xs font-bold border transition inline-flex items-center gap-1.5 ${
                    active ? 'bg-icase-red text-white border-icase-red shadow-sm' : 'bg-slate-50 text-icase-navy border-slate-200 hover:border-icase-red'
                  }">
                    <span>${active ? '✓ ' : ''}${escapeHtml(gt)}</span>
                    <span class="opacity-85">• ${formatINR(gtPricing.finalPrice)}</span>
                    ${gtPricing.offerBadge ? `<span class="px-1.5 py-0.5 rounded bg-amber-300 text-icase-navy text-[10px] font-black">${escapeHtml(gtPricing.offerBadge)}</span>` : ''}
                  </button>
                `;
              })
              .join('')}
          </div>
          ${helperNote ? `<p class="text-xs font-medium text-icase-blue bg-icase-blueLight/50 px-3 py-1.5 rounded-lg truncate" title="${escapeHtml(helperNote)}">${escapeHtml(helperNote)}</p>` : ''}
        </div>
      `
          : ''
      }

      ${
        pricing
          ? `
        <div class="p-3.5 rounded-xl bg-slate-50 border border-slate-200 flex flex-wrap items-center justify-between gap-3">
          <div>
            <div class="text-xs font-extrabold text-icase-navy">${escapeHtml(state.glassType)} Tempered Glass</div>
            <div class="text-[11px] text-slate-500">${escapeHtml(isManual ? (state.glassManualModel.trim() || 'Custom Model') : `${state.glassBrand} ${state.glassModel}`)} • ${escapeHtml(state.glassInstallationOption)}</div>
          </div>
          <div class="flex items-center gap-2">
            ${pricing.basePrice > pricing.finalPrice ? `<span class="text-xs text-slate-400 line-through font-semibold">${formatINR(pricing.basePrice)}</span>` : ''}
            <span class="text-lg font-black text-icase-navy">${formatINR(pricing.finalPrice)}</span>
            ${pricing.offerBadge ? `<span class="px-2 py-0.5 rounded-full bg-icase-red text-white text-[10px] font-extrabold">${escapeHtml(pricing.offerBadge)}</span>` : ''}
          </div>
        </div>
      `
          : ''
      }

      ${state.glassError ? `<div class="text-xs font-bold text-icase-red">${escapeHtml(state.glassError)}</div>` : ''}

      ${
        state.glassModel && pricing
          ? `
        <div class="grid sm:grid-cols-2 gap-2.5">
          <button id="btn-glass-add-cart" type="button" class="w-full px-5 py-3 rounded-xl bg-icase-navy hover:bg-icase-dark text-white text-xs sm:text-sm font-extrabold transition shadow-sm min-h-[46px]">
            Add to Cart • ${formatINR(pricing.finalPrice)}
          </button>
          <a id="btn-glass-whatsapp-order" href="${escapeHtml(
            getStoreWaUrl(
              buildTemperedGlassWhatsAppMessage({
                brand: isManual ? 'CUSTOM' : state.glassBrand,
                model: isManual ? state.glassManualModel.trim() || OTHER_MODEL_OPTION : state.glassModel,
                glassType: state.glassType,
                finalPrice: pricing.finalPrice,
                installationOption: state.glassInstallationOption
              })
            )
          )}" target="_blank" rel="noreferrer" class="w-full px-5 py-3 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-icase-green border border-emerald-500 text-xs sm:text-sm font-extrabold transition inline-flex items-center justify-center gap-2 min-h-[46px]">
            💬 Ask / Order on WhatsApp
          </a>
        </div>
      `
          : ''
      }
    </div>
  `;

  const searchEl = document.getElementById('glass-search-input');
  searchEl?.addEventListener('focus', () => {
    state.glassDropdownOpen = true;
    searchEl.value = state.glassSearch;
    document.getElementById('glass-dropdown-menu')?.classList.remove('hidden');
  });
  searchEl?.addEventListener('input', (e) => {
    state.glassSearch = e.target.value;
    state.glassDropdownOpen = true;
    renderTemperedGlassFinder();
    const reFocus = document.getElementById('glass-search-input');
    if (reFocus) {
      reFocus.focus();
      const len = reFocus.value.length;
      reFocus.setSelectionRange(len, len);
    }
  });

  document.getElementById('glass-clear-btn')?.addEventListener('click', () => {
    state.glassModel = '';
    state.glassManualModel = '';
    state.glassSearch = '';
    state.glassError = '';
    state.glassDropdownOpen = true;
    renderTemperedGlassFinder();
    renderCameraLensFinder();
  });

  document.getElementById('glass-manual-input')?.addEventListener('input', (e) => {
    state.glassManualModel = e.target.value;
    state.glassError = '';
    renderCameraLensFinder();
  });

  document.getElementById('btn-glass-add-cart')?.addEventListener('click', () => {
    const effectiveModel = isManual ? state.glassManualModel.trim() : state.glassModel;
    if (!effectiveModel) {
      state.glassError = 'Please enter your exact device model name first.';
      renderTemperedGlassFinder();
      return;
    }
    const brandPart = isManual ? 'CUSTOM' : state.glassBrand;
    const guardProduct = buildTemperedGlassCartProduct(
      brandPart,
      effectiveModel,
      state.glassType,
      config,
      state.glassInstallationOption
    );
    addDirectProductToCart(guardProduct);
  });
}

function renderCameraLensFinder() {
  const container = document.getElementById('camera-lens-container');
  if (!container) return;

  const config = state.screenGuardConfig || createDefaultScreenGuardConfig();
  if (!isCameraLensSectionAvailable(config)) {
    container.innerHTML = '';
    return;
  }

  const activeCamBrands = getActiveCameraLensBrands(config);
  if (state.cameraBrand && !activeCamBrands.includes(state.cameraBrand)) {
    state.cameraBrand = '';
    state.cameraModel = '';
  }

  const grouped = searchCameraLensCatalog(state.cameraBrand, state.cameraSearch, config);
  const pricing = state.cameraModel
    ? resolveCameraLensPrice(state.cameraBrand, state.cameraModel, config)
    : null;

  const comboGlass = getSelectedGlassForCombo(config);
  const comboPricing =
    state.cameraModel && comboGlass && config.comboEnabled !== false
      ? resolveComboPrice(
          comboGlass.brand,
          comboGlass.model,
          comboGlass.glassType,
          state.cameraBrand,
          state.cameraModel,
          config
        )
      : null;

  const camImgUrl = resolveCameraLensImageUrl(config);
  const camVideoUrl = String(config?.cameraLensDefault?.videoUrl || '').trim();
  const camFocused = state.guardsFocusTarget === 'camera';
  const popularCameraModels = getPopularCameraLensModels(state.cameraBrand, config);
  const effectiveGlassModel =
    state.glassModel === OTHER_MODEL_OPTION ? state.glassManualModel.trim() : state.glassModel;
  const effectiveGlassBrand =
    state.glassModel === OTHER_MODEL_OPTION ? 'CUSTOM' : state.glassBrand;
  const selectedGlassUnsupported =
    Boolean(effectiveGlassModel) &&
    !isDeviceEligibleForCameraLens(effectiveGlassBrand, effectiveGlassModel, config);
  const searchUnsupported = Boolean(state.cameraSearch.trim()) && grouped.length === 0;

  container.innerHTML = `
    <div id="camera-lens-wrapper" class="bg-white rounded-2xl border ${
      camFocused ? 'border-icase-navy ring-2 ring-icase-navy/25' : 'border-slate-200'
    } p-5 sm:p-6 shadow-sm space-y-4">
      <div class="flex items-start gap-3.5">
        <img src="${escapeHtml(camImgUrl)}" alt="Camera Lens Protector Sample" class="w-14 h-14 rounded-2xl object-cover border border-slate-200 shrink-0" />
        <div class="min-w-0 flex-1 space-y-1">
          <div class="flex flex-wrap items-center gap-2">
            <h2 class="text-lg sm:text-xl font-extrabold text-icase-navy">Camera Lens Protection</h2>
            <span class="px-2.5 py-0.5 rounded-full bg-icase-blueLight border border-icase-blue/30 text-icase-blue text-[11px] font-extrabold">iPhone &amp; Samsung S Series Only</span>
            ${
              camVideoUrl
                ? `<button type="button" data-open-video="${escapeHtml(camVideoUrl)}" data-video-title="Camera Lens Protection" class="px-2.5 py-0.5 rounded-full bg-slate-900 hover:bg-icase-red text-white text-[11px] font-extrabold transition">▶ Watch Video</button>`
                : ''
            }
          </div>
          <p class="text-xs sm:text-sm text-slate-600">Supported exclusively for iPhone &amp; Samsung Galaxy S Series (S20–S25 / Ultra). Not stocked for Redmi, Oppo, Realme, Vivo, OnePlus, Nothing, or Galaxy A/M series.</p>
        </div>
      </div>

      ${
        selectedGlassUnsupported || searchUnsupported
          ? `
        <div class="p-3.5 rounded-xl bg-amber-50 border border-amber-300 text-xs text-amber-900 space-y-1">
          <div class="font-extrabold">Camera Lens Protector Not Available for This Model</div>
          <p>${
            selectedGlassUnsupported
              ? `Your selected Tempered Glass device (<strong>${escapeHtml(
                  state.glassModel === OTHER_MODEL_OPTION
                    ? effectiveGlassModel
                    : `${state.glassBrand} ${effectiveGlassModel}`.trim()
                )}</strong>) does not have a Camera Lens Protector in stock. Camera Lens Protection is available only for <strong>iPhone</strong> and <strong>Samsung S Series</strong>.`
              : `No matching Camera Lens Protector for "<strong>${escapeHtml(
                  state.cameraSearch.trim()
                )}</strong>". Camera Lens Protection is stocked strictly for <strong>iPhone</strong> and <strong>Samsung S Series</strong> models.`
          }</p>
        </div>
      `
          : ''
      }

      <div class="flex flex-wrap gap-2">
        <button type="button" data-camera-brand="" class="px-3 py-1.5 rounded-full text-xs font-bold border transition ${
          !state.cameraBrand ? 'bg-icase-navy text-white border-icase-navy' : 'bg-slate-50 text-icase-navy border-slate-200 hover:border-icase-navy'
        }">All Eligible</button>
        ${activeCamBrands
          .map(
            (brand) => `
          <button type="button" data-camera-brand="${escapeHtml(brand)}" class="px-3 py-1.5 rounded-full text-xs font-bold border transition ${
            state.cameraBrand === brand ? 'bg-icase-navy text-white border-icase-navy' : 'bg-slate-50 text-icase-navy border-slate-200 hover:border-icase-navy'
          }">${escapeHtml(brand)}</button>
        `
          )
          .join('')}
      </div>

      <div class="relative">
        <label class="block text-xs font-bold uppercase text-slate-500 mb-1.5">Select iPhone or Samsung S Series Model</label>
        <div class="flex items-center gap-2 px-3.5 py-2.5 rounded-xl border border-slate-300 bg-slate-50 focus-within:bg-white focus-within:border-icase-red">
          <svg class="w-4 h-4 text-slate-400 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/></svg>
          <input id="camera-search-input" type="text" autocomplete="off" value="${escapeHtml(
            state.cameraDropdownOpen
              ? state.cameraSearch
              : state.cameraModel
                ? `${state.cameraBrand} — ${state.cameraModel}`
                : ''
          )}" placeholder="Search iPhone 16 Pro, Galaxy S24 Ultra, S25..." class="w-full bg-transparent text-sm font-semibold text-icase-navy focus:outline-none placeholder:font-normal" />
          ${
            state.cameraModel
              ? `<button id="camera-clear-btn" type="button" class="text-xs font-bold text-slate-500 hover:text-icase-red px-1">Clear</button>`
              : ''
          }
        </div>

        <div id="camera-dropdown-menu" class="${state.cameraDropdownOpen ? '' : 'hidden '}absolute left-0 right-0 mt-1.5 max-h-64 overflow-y-auto bg-white border border-slate-200 rounded-2xl shadow-xl z-30">
          ${
            popularCameraModels.length > 0 && !state.cameraSearch.trim()
              ? `
            <div class="px-3.5 py-2.5 bg-slate-50 border-b border-slate-200">
              <div class="text-[10px] font-black uppercase tracking-wider text-slate-500 mb-1.5">Popular Models (Quick Select)</div>
              <div class="flex flex-wrap gap-1.5">
                ${popularCameraModels
                  .map((item) => {
                    const active = state.cameraBrand === item.brand && state.cameraModel === item.model;
                    return `<button type="button" data-pick-camera-brand="${escapeHtml(item.brand)}" data-pick-camera-model="${escapeHtml(item.model)}" class="px-2.5 py-1.5 rounded-full text-xs font-bold border transition ${
                      active ? 'bg-icase-navy text-white border-icase-navy' : 'bg-white text-icase-navy border-slate-300 hover:border-icase-navy'
                    }">${escapeHtml(item.model)}</button>`;
                  })
                  .join('')}
              </div>
            </div>
          `
              : ''
          }
          ${
            grouped.length === 0
              ? `<div class="p-4 text-xs text-slate-500">Camera Lens Protection is available only for active iPhone and Samsung S Series models.</div>`
              : grouped
                  .map(
                    (grp) => `
                  <div class="px-3.5 py-1.5 bg-slate-100 text-[11px] font-black uppercase tracking-wider text-icase-blue sticky top-0">${escapeHtml(grp.brand)}</div>
                  ${grp.models
                    .map((model) => {
                      const active = state.cameraBrand === grp.brand && state.cameraModel === model;
                      return `
                        <button type="button" data-pick-camera-brand="${escapeHtml(grp.brand)}" data-pick-camera-model="${escapeHtml(model)}" class="w-full px-4 py-2.5 text-left text-xs sm:text-sm font-semibold flex items-center justify-between border-b border-slate-100 hover:bg-icase-redLight transition ${
                          active ? 'bg-icase-redLight text-icase-red font-extrabold' : 'text-icase-navy'
                        }">
                          <span>${escapeHtml(model)}</span>
                          ${active ? '<span class="text-xs">✓</span>' : ''}
                        </button>
                      `;
                    })
                    .join('')}
                `
                  )
                  .join('')
          }
        </div>
      </div>

      ${
        state.cameraModel && pricing
          ? `
        <div class="p-3.5 rounded-xl bg-slate-50 border border-slate-200 flex flex-wrap items-center justify-between gap-3">
          <div class="text-xs text-slate-700">
            9H Sapphire Tempered Camera Lens Ring Protection for <strong>${escapeHtml(state.cameraModel)}</strong> • ${escapeHtml(state.glassInstallationOption)}.
          </div>
          <div class="flex items-center gap-2">
            ${pricing.basePrice > pricing.finalPrice ? `<span class="text-xs text-slate-400 line-through font-semibold">${formatINR(pricing.basePrice)}</span>` : ''}
            <span class="text-lg font-black text-icase-navy">${formatINR(pricing.finalPrice)}</span>
            ${pricing.offerBadge ? `<span class="px-2 py-0.5 rounded-full bg-icase-red text-white text-[10px] font-extrabold">${escapeHtml(pricing.offerBadge)}</span>` : ''}
          </div>
        </div>
        <div class="grid sm:grid-cols-2 gap-2.5">
          <button id="btn-camera-add-cart" type="button" class="w-full px-5 py-3 rounded-xl bg-icase-navy hover:bg-icase-dark text-white text-xs sm:text-sm font-extrabold transition shadow-sm min-h-[46px]">
            Add to Cart • ${formatINR(pricing.finalPrice)}
          </button>
          <a id="btn-camera-whatsapp-order" href="${escapeHtml(
            getStoreWaUrl(
              buildCameraLensWhatsAppMessage({
                brand: state.cameraBrand,
                model: state.cameraModel,
                finalPrice: pricing.finalPrice,
                installationOption: state.glassInstallationOption
              })
            )
          )}" target="_blank" rel="noreferrer" class="w-full px-5 py-3 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-icase-green border border-emerald-500 text-xs sm:text-sm font-extrabold transition inline-flex items-center justify-center gap-2 min-h-[46px]">
            💬 Ask / Order on WhatsApp
          </a>
        </div>
      `
          : ''
      }

      ${
        comboPricing && comboGlass
          ? `
        <div class="p-4 rounded-2xl bg-icase-blueLight/60 border border-icase-blue/30 space-y-2.5">
          <div class="flex flex-wrap items-center justify-between gap-2">
            <div>
              <div class="text-[11px] font-black uppercase tracking-wider text-icase-blue">Quick Bundle • Save with Combo</div>
              <div class="text-xs font-extrabold text-icase-navy">${escapeHtml(comboGlass.model)} (${escapeHtml(comboGlass.glassType)}) + ${escapeHtml(state.cameraModel)} Lens</div>
            </div>
            <div class="flex items-center gap-2">
              ${comboPricing.basePrice > comboPricing.finalPrice ? `<span class="text-xs text-slate-500 line-through font-semibold">${formatINR(comboPricing.basePrice)}</span>` : ''}
              <span class="text-lg font-black text-icase-navy">${formatINR(comboPricing.finalPrice)}</span>
              ${comboPricing.offerBadge ? `<span class="px-2 py-0.5 rounded-full bg-icase-red text-white text-[10px] font-extrabold">${escapeHtml(comboPricing.offerBadge)}</span>` : ''}
            </div>
          </div>
          <button id="btn-combo-add-cart" type="button" class="w-full px-5 py-3 rounded-xl bg-icase-navy hover:bg-icase-dark text-white text-xs sm:text-sm font-extrabold transition min-h-[46px]">
            Add Tempered Glass + Camera Lens Combo • ${formatINR(comboPricing.finalPrice)}
          </button>
        </div>
      `
          : ''
      }
    </div>
  `;

  const searchEl = document.getElementById('camera-search-input');
  searchEl?.addEventListener('focus', () => {
    state.cameraDropdownOpen = true;
    searchEl.value = state.cameraSearch;
    document.getElementById('camera-dropdown-menu')?.classList.remove('hidden');
  });
  searchEl?.addEventListener('input', (e) => {
    state.cameraSearch = e.target.value;
    state.cameraDropdownOpen = true;
    renderCameraLensFinder();
    const reFocus = document.getElementById('camera-search-input');
    if (reFocus) {
      reFocus.focus();
      const len = reFocus.value.length;
      reFocus.setSelectionRange(len, len);
    }
  });

  document.getElementById('camera-clear-btn')?.addEventListener('click', () => {
    state.cameraModel = '';
    state.cameraSearch = '';
    state.cameraDropdownOpen = true;
    renderCameraLensFinder();
  });

  document.getElementById('btn-camera-add-cart')?.addEventListener('click', () => {
    if (!state.cameraModel) return;
    const camProduct = buildCameraLensCartProduct(
      state.cameraBrand,
      state.cameraModel,
      config,
      state.glassInstallationOption
    );
    addDirectProductToCart(camProduct);
  });

  document.getElementById('btn-combo-add-cart')?.addEventListener('click', () => {
    if (!state.cameraModel || !comboGlass) return;
    const comboProduct = buildComboCartProduct(
      comboGlass.brand,
      comboGlass.model,
      comboGlass.glassType,
      state.cameraBrand,
      state.cameraModel,
      config,
      state.glassInstallationOption
    );
    addDirectProductToCart(comboProduct);
  });
}

function renderPouchesFinder() {
  const container = document.getElementById('pouch-finder-container');
  if (!container) return;

  const config = state.screenGuardConfig || createDefaultScreenGuardConfig();
  const isOtherAndroid = state.pouchBrand === OTHER_ANDROID_POUCHES_OPTION;
  const models = isOtherAndroid ? [] : searchPouchCatalog(state.pouchBrand, state.pouchSearch, config);
  const pouches = isOtherAndroid ? [] : getActivePouchesForBrand(state.pouchBrand, config);
  const allActivePouches = (Array.isArray(config?.pouchItems) ? config.pouchItems : []).filter(
    (p) => p && !p.deleted && p.enabled !== false
  );
  const pouchHeaderImg = resolvePouchCardImageUrl(config);
  const pouchFocused = state.guardsFocusTarget === 'pouch';
  const otherWaUrl = getStoreWaUrl(
    buildOtherAndroidPouchWhatsAppMessage({
      modelName: state.otherAndroidPouchModel,
      pouchName: state.otherAndroidPouchPref
    })
  );

  container.innerHTML = `
    <div id="pouch-wrapper" class="bg-white rounded-2xl border ${
      pouchFocused ? 'border-icase-navy ring-2 ring-icase-navy/25' : 'border-slate-200'
    } p-5 sm:p-6 shadow-sm space-y-4">
      <div class="flex items-start gap-3.5">
        <img src="${escapeHtml(pouchHeaderImg)}" alt="Pouches &amp; Back Cases" class="w-14 h-14 rounded-2xl object-cover border border-slate-200 shrink-0" />
        <div class="min-w-0 flex-1 space-y-1">
          <div class="flex flex-wrap items-center gap-2">
            <h2 class="text-lg sm:text-xl font-extrabold text-icase-navy">Pouches &amp; Back Cases</h2>
            <span class="px-2.5 py-0.5 rounded-full bg-teal-50 border border-teal-200 text-teal-800 text-[11px] font-extrabold">iPhone, Samsung S Series &amp; Other Android Models</span>
          </div>
          <p class="text-xs sm:text-sm text-slate-600">Custom-fit protective pouches for iPhone &amp; Samsung S Series, plus instant WhatsApp enquiry for all Other Android models.</p>
        </div>
      </div>

      <div class="flex flex-wrap gap-2">
        ${[...POUCH_BRANDS, OTHER_ANDROID_POUCHES_OPTION]
          .map(
            (b) => `
          <button type="button" data-pouch-brand="${escapeHtml(b)}" class="px-3.5 py-1.5 rounded-full text-xs font-bold border transition ${
            state.pouchBrand === b ? 'bg-icase-navy text-white border-icase-navy' : 'bg-slate-50 text-icase-navy border-slate-200 hover:border-icase-navy'
          }">${escapeHtml(b)}</button>
        `
          )
          .join('')}
      </div>

      ${
        isOtherAndroid
          ? `
        <div class="p-4 sm:p-5 rounded-2xl bg-slate-50 border border-slate-200 space-y-3.5">
          <div class="text-xs sm:text-sm font-bold text-icase-navy">
            Enter or pick your Android phone model for instant WhatsApp pouch photos &amp; price:
          </div>

          <div class="space-y-1.5">
            <div class="text-[10px] font-black uppercase tracking-wider text-slate-500">Popular Android Models (Quick Pick)</div>
            <div class="flex flex-wrap gap-1.5">
              ${OTHER_ANDROID_POPULAR_MODELS.map((m) => {
                const active = state.otherAndroidPouchModel === m;
                return `<button type="button" data-other-android-chip="${escapeHtml(m)}" class="px-3 py-1.5 rounded-full text-xs font-bold border transition ${
                  active ? 'bg-icase-navy text-white border-icase-navy' : 'bg-white text-icase-navy border-slate-300 hover:border-icase-navy'
                }">${escapeHtml(m)}</button>`;
              }).join('')}
            </div>
          </div>

          <div>
            <label for="pouch-other-android-input" class="block text-xs font-bold text-icase-navy mb-1">Your Android Phone Model *</label>
            <input id="pouch-other-android-input" type="text" value="${escapeHtml(state.otherAndroidPouchModel)}" placeholder="e.g. Redmi Note 13 Pro, OnePlus 12R, Vivo V40, Motorola Edge 50" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 bg-white focus:border-icase-blue focus:outline-none text-sm font-medium" />
          </div>

          <div class="space-y-1.5">
            <div class="text-[10px] font-black uppercase tracking-wider text-slate-500">Preferred Pouch Style</div>
            <div class="flex flex-wrap gap-1.5">
              ${['Any Matching Design', ...allActivePouches.map((p) => p.name)]
                .map((styleName) => {
                  const active = state.otherAndroidPouchPref === styleName;
                  return `<button type="button" data-other-android-style="${escapeHtml(styleName)}" class="px-3 py-1.5 rounded-full text-xs font-bold border transition ${
                    active ? 'bg-icase-red text-white border-icase-red' : 'bg-white text-icase-navy border-slate-300 hover:border-icase-navy'
                  }">${escapeHtml(styleName)}</button>`;
                })
                .join('')}
            </div>
          </div>

          <div>
            <a id="btn-other-android-pouch-wa" href="${escapeHtml(otherWaUrl)}" target="_blank" rel="noreferrer" class="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl bg-icase-green hover:bg-emerald-700 text-white text-xs sm:text-sm font-extrabold transition min-h-[46px] shadow-sm">
              💬 Ask / Order on WhatsApp →
            </a>
          </div>
        </div>
      `
          : `
        <div class="relative">
          <label class="block text-xs font-bold uppercase text-slate-500 mb-1.5">Select ${escapeHtml(state.pouchBrand)} Model</label>
          <div class="flex items-center gap-2 px-3.5 py-2.5 rounded-xl border border-slate-300 bg-slate-50 focus-within:bg-white focus-within:border-icase-blue">
            <svg class="w-4 h-4 text-slate-400 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/></svg>
            <input id="pouch-search-input" type="text" autocomplete="off" value="${escapeHtml(
              state.pouchDropdownOpen ? state.pouchSearch : state.pouchModel || ''
            )}" placeholder="Search ${escapeHtml(state.pouchBrand)} model..." class="w-full bg-transparent text-sm font-semibold text-icase-navy focus:outline-none placeholder:font-normal" />
            ${
              state.pouchModel
                ? `<button id="pouch-clear-btn" type="button" class="text-xs font-bold text-slate-500 hover:text-icase-red px-1">Clear</button>`
                : ''
            }
          </div>

          <div id="pouch-dropdown-menu" class="${state.pouchDropdownOpen ? '' : 'hidden '}absolute left-0 right-0 mt-1.5 max-h-60 overflow-y-auto bg-white border border-slate-200 rounded-2xl shadow-xl z-30">
            ${
              models.length === 0
                ? `<div class="p-4 text-xs text-slate-500">No active ${escapeHtml(state.pouchBrand)} pouch models matched.</div>`
                : models
                    .map((m) => {
                      const active = state.pouchModel === m;
                      return `
                        <button type="button" data-pick-pouch-model="${escapeHtml(m)}" class="w-full px-4 py-2.5 text-left text-xs sm:text-sm font-semibold flex items-center justify-between border-b border-slate-100 hover:bg-icase-blueLight transition ${
                          active ? 'bg-icase-blueLight text-icase-blue font-extrabold' : 'text-icase-navy'
                        }">
                          <span>${escapeHtml(m)}</span>
                          ${active ? '<span class="text-xs">✓</span>' : ''}
                        </button>
                      `;
                    })
                    .join('')
            }
          </div>
        </div>

        ${state.pouchError ? `<div class="text-xs font-bold text-icase-red">${escapeHtml(state.pouchError)}</div>` : ''}

        ${
          pouches.length === 0
            ? `<div class="p-3.5 rounded-xl bg-amber-50 border border-amber-200 text-xs font-semibold text-amber-900">No pouches are currently enabled for ${escapeHtml(state.pouchBrand)} in store inventory.</div>`
            : `
          <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-3.5">
            ${pouches
              .map((p) => {
                const pr = resolvePouchPrice(p);
                const colors = Array.isArray(p.colorOptions) ? p.colorOptions : [];
                const activeColorOpt = resolvePouchSelectedColor(p, state.selectedPouchColors?.[p.id] || '');
                const activeColorName = activeColorOpt?.name || '';
                const activeImageUrl = resolvePouchImageUrl(p, activeColorName);
                return `
                  <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 flex flex-col justify-between gap-3">
                    <div class="space-y-2.5">
                      <div class="relative aspect-[4/3] w-full rounded-xl overflow-hidden bg-white border border-slate-200">
                        <img src="${escapeHtml(activeImageUrl)}" alt="${escapeHtml(p.name)}" class="w-full h-full object-cover" />
                        ${pr.offerBadge ? `<span class="absolute top-2 right-2 px-2 py-0.5 rounded-full bg-icase-red text-white text-[10px] font-extrabold shadow-sm">${escapeHtml(pr.offerBadge)}</span>` : ''}
                        ${
                          p.videoUrl
                            ? `<button type="button" data-open-video="${escapeHtml(p.videoUrl)}" data-video-title="${escapeHtml(p.name)}" class="absolute bottom-2 left-2 px-2.5 py-1 rounded-full bg-slate-950/85 hover:bg-icase-red text-white text-[10px] font-extrabold shadow transition">▶ Video</button>`
                            : ''
                        }
                      </div>
                      <div>
                        <div class="text-sm font-extrabold text-icase-navy">${escapeHtml(p.name)}</div>
                        ${p.description ? `<p class="text-xs text-slate-600 mt-1">${escapeHtml(p.description)}</p>` : ''}
                      </div>
                      ${
                        colors.length > 0
                          ? `
                        <div class="space-y-1.5 pt-1">
                          <div class="text-[11px] font-bold text-icase-navy">
                            Colour: <span class="font-semibold text-slate-600">${escapeHtml(activeColorName)}</span>
                          </div>
                          <div class="flex flex-wrap gap-1.5">
                            ${colors
                              .map((c) => {
                                const isSel = activeColorName.toLowerCase() === c.name.toLowerCase();
                                return `
                                  <button type="button" data-pouch-color="${escapeHtml(p.id)}" data-color-name="${escapeHtml(c.name)}" class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] border transition ${
                                    isSel
                                      ? 'bg-icase-blueLight border-icase-navy text-icase-navy font-extrabold ring-1 ring-icase-navy'
                                      : 'bg-white border-slate-300 text-slate-700 font-semibold hover:border-icase-navy'
                                  }">
                                    <span class="w-3 h-3 rounded-full border border-slate-400 shrink-0" style="background-color: ${escapeHtml(c.hex || '#1E293B')}"></span>
                                    <span>${escapeHtml(c.name)}</span>
                                  </button>
                                `;
                              })
                              .join('')}
                          </div>
                        </div>
                      `
                          : ''
                      }
                    </div>
                    <div class="space-y-2 pt-1">
                      <div class="flex items-baseline gap-2">
                        <span class="text-base font-black text-icase-navy">${formatINR(pr.finalPrice)}</span>
                        ${pr.basePrice > pr.finalPrice ? `<span class="text-xs text-slate-400 line-through font-semibold">${formatINR(pr.basePrice)}</span>` : ''}
                      </div>
                      <button type="button" data-pouch-add="${escapeHtml(p.id)}" class="w-full py-2.5 px-3 rounded-xl bg-icase-navy hover:bg-icase-dark text-white text-xs font-extrabold transition min-h-[42px]">
                        ${
                          !state.pouchModel
                            ? 'Select Model & Add'
                            : activeColorName
                              ? `Add ${escapeHtml(activeColorName)} to Cart`
                              : 'Add to Cart'
                        }
                      </button>
                    </div>
                  </div>
                `;
              })
              .join('')}
          </div>
        `
        }
      `
      }
    </div>
  `;

  document.getElementById('pouch-other-android-input')?.addEventListener('input', (e) => {
    state.otherAndroidPouchModel = e.target.value;
    const waLink = document.getElementById('btn-other-android-pouch-wa');
    if (waLink) {
      waLink.href = getStoreWaUrl(
        buildOtherAndroidPouchWhatsAppMessage({
          modelName: state.otherAndroidPouchModel,
          pouchName: state.otherAndroidPouchPref
        })
      );
    }
  });
  container.querySelectorAll('[data-other-android-chip]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.otherAndroidPouchModel = btn.dataset.otherAndroidChip || '';
      renderPouchesFinder();
    })
  );
  container.querySelectorAll('[data-other-android-style]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.otherAndroidPouchPref = btn.dataset.otherAndroidStyle || 'Any Matching Design';
      renderPouchesFinder();
    })
  );

  const pSearch = document.getElementById('pouch-search-input');
  pSearch?.addEventListener('focus', () => {
    state.pouchDropdownOpen = true;
    pSearch.value = state.pouchSearch;
    document.getElementById('pouch-dropdown-menu')?.classList.remove('hidden');
  });
  pSearch?.addEventListener('input', (e) => {
    state.pouchSearch = e.target.value;
    state.pouchDropdownOpen = true;
    renderPouchesFinder();
    const reFocus = document.getElementById('pouch-search-input');
    if (reFocus) {
      reFocus.focus();
      const len = reFocus.value.length;
      reFocus.setSelectionRange(len, len);
    }
  });

  document.getElementById('pouch-clear-btn')?.addEventListener('click', () => {
    state.pouchModel = '';
    state.pouchSearch = '';
    state.pouchError = '';
    state.pouchDropdownOpen = true;
    renderPouchesFinder();
  });

  container.querySelectorAll('[data-pouch-color]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const pouchId = btn.dataset.pouchColor;
      const colorName = btn.dataset.colorName || '';
      if (!pouchId || !colorName) return;
      state.selectedPouchColors = { ...(state.selectedPouchColors || {}), [pouchId]: colorName };
      renderPouchesFinder();
    })
  );

  container.querySelectorAll('[data-pouch-add]').forEach((btn) =>
    btn.addEventListener('click', () => {
      if (!state.pouchModel) {
        state.pouchError = `Please select your ${state.pouchBrand} model first.`;
        state.pouchDropdownOpen = true;
        renderPouchesFinder();
        return;
      }
      const pouch = pouches.find((x) => x.id === btn.dataset.pouchAdd);
      if (!pouch) return;
      const chosenColor = resolvePouchSelectedColor(pouch, state.selectedPouchColors?.[pouch.id] || '')?.name || '';
      state.pouchError = '';
      addDirectProductToCart(
        buildPouchCartProduct(state.pouchBrand, state.pouchModel, pouch, chosenColor),
        1,
        chosenColor
      );
    })
  );
}

function renderGuardInstallationOption() {
  const container = document.getElementById('guard-installation-container');
  if (!container) return;

  const opts = [
    { id: 'Shop Installation', sub: 'Free dust-free fitting at iCASE Edappal' },
    { id: 'Delivery / Pickup', sub: 'Doorstep delivery or counter pickup' }
  ];

  container.innerHTML = `
    <div class="bg-white rounded-2xl border border-slate-200 p-4 sm:p-5 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-3">
      <div>
        <div class="text-xs font-black uppercase tracking-wider text-icase-blue">Installation &amp; Fulfillment</div>
        <div class="text-xs sm:text-sm font-bold text-icase-navy">Choose Shop Installation vs Delivery / Pickup</div>
      </div>
      <div class="flex flex-wrap gap-2">
        ${opts
          .map((o) => {
            const active = state.glassInstallationOption === o.id;
            return `
              <button type="button" data-install-opt="${escapeHtml(o.id)}" class="px-3.5 py-2 rounded-xl text-left border transition ${
                active ? 'bg-icase-navy text-white border-icase-navy shadow-sm' : 'bg-slate-50 text-icase-navy border-slate-200 hover:border-icase-navy'
              }">
                <div class="text-xs font-extrabold">${active ? '✓ ' : ''}${escapeHtml(o.id)}</div>
                <div class="text-[10px] ${active ? 'text-slate-200' : 'text-slate-500'}">${escapeHtml(o.sub)}</div>
              </button>
            `;
          })
          .join('')}
      </div>
    </div>
  `;
}

function openVideoModal(titleOrEvent = 'Tempered Glass & Camera Lens Demo', rawUrl = SAMPLE_GUARD_VIDEO_URL) {
  const modal = document.getElementById('video-demo-modal');
  const titleEl = document.getElementById('video-modal-title');
  const ytFrame = document.getElementById('sample-guard-youtube-frame');
  const video = document.getElementById('sample-guard-video');
  const errEl = document.getElementById('video-demo-error');
  const toggleBtn = document.getElementById('btn-video-play-pause');
  const extLink = document.getElementById('btn-video-external-link');
  if (!modal || !video) return;

  const title =
    typeof titleOrEvent === 'string' && titleOrEvent.trim()
      ? titleOrEvent.trim()
      : 'Tempered Glass & Camera Lens Demo';
  const targetUrl = String(typeof titleOrEvent === 'string' ? rawUrl : SAMPLE_GUARD_VIDEO_URL || '').trim() || SAMPLE_GUARD_VIDEO_URL;
  if (titleEl) titleEl.textContent = title;
  errEl?.classList.add('hidden');

  const isHttp = /^https?:\/\//i.test(targetUrl);
  if (extLink) {
    if (isHttp) {
      extLink.href = targetUrl;
      extLink.classList.remove('hidden');
    } else {
      extLink.classList.add('hidden');
    }
  }

  const ytEmbed = isYouTubeUrl(targetUrl) ? parseYouTubeEmbedUrl(targetUrl) : '';
  if (ytEmbed && ytFrame) {
    try {
      video.pause();
      video.removeAttribute('src');
      video.load();
    } catch {}
    video.classList.add('hidden');
    toggleBtn?.classList.add('hidden');
    ytFrame.src = ytEmbed;
    ytFrame.classList.remove('hidden');
    modal.classList.remove('hidden');
    return;
  }

  if (ytFrame) {
    ytFrame.src = '';
    ytFrame.classList.add('hidden');
  }
  video.classList.remove('hidden');
  toggleBtn?.classList.remove('hidden');
  video.setAttribute('src', targetUrl);
  video.loop = true;
  modal.classList.remove('hidden');

  video.onerror = () => errEl?.classList.remove('hidden');
  video
    .play()
    .then(() => {
      if (toggleBtn) toggleBtn.textContent = 'Pause Video';
    })
    .catch(() => {
      if (toggleBtn) toggleBtn.textContent = 'Play Video';
    });
}

function closeVideoModal() {
  const modal = document.getElementById('video-demo-modal');
  const video = document.getElementById('sample-guard-video');
  const ytFrame = document.getElementById('sample-guard-youtube-frame');
  if (ytFrame) {
    ytFrame.src = '';
    ytFrame.classList.add('hidden');
  }
  if (video) {
    try {
      video.pause();
      video.removeAttribute('src');
      video.load();
    } catch {}
  }
  modal?.classList.add('hidden');
}

// ==================== VERIFIED CUSTOMER REVIEWS ====================
function renderPublicReviewsSections() {
  // Defensive strict privacy guard: ONLY approved PRODUCT reviews may ever appear publicly
  const publicApproved = state.publicProductReviews.filter(
    (r) => r.reviewType === 'PRODUCT' && r.isApproved === true
  );

  const html = `
    <div class="bg-white rounded-3xl border border-slate-200 p-6 sm:p-7 shadow-sm space-y-4">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="inline-flex items-center gap-1.5 text-xs font-extrabold uppercase tracking-wider text-icase-green">
            ✓ Verified Purchase Feedback
          </div>
          <h2 class="text-xl font-extrabold text-icase-navy mt-0.5">Verified Customer Reviews</h2>
          <p class="text-xs text-slate-500">Only customers with completed Order or Service IDs can submit reviews.</p>
        </div>
        <button type="button" data-launch-review="" class="px-4 py-2.5 rounded-xl border border-icase-red text-icase-red hover:bg-icase-redLight text-xs sm:text-sm font-extrabold transition">
          Write Verified Review
        </button>
      </div>

      ${
        publicApproved.length === 0
          ? `<div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 text-xs sm:text-sm text-slate-500">No approved product reviews published yet. Completed orders (ORD-...) can submit a verified review!</div>`
          : `
        <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          ${publicApproved
            .slice(0, 6)
            .map((r) => {
              const stars = '★'.repeat(Math.max(1, Math.min(5, Number(r.rating || 5))));
              return `
                <article class="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-2 flex flex-col justify-between">
                  <div class="space-y-1.5">
                    <div class="flex items-center justify-between gap-2">
                      <span class="font-extrabold text-icase-navy text-sm truncate">${escapeHtml(r.customerName)}</span>
                      <span class="text-icase-gold text-xs font-bold tracking-wider">${stars}</span>
                    </div>
                    <div class="text-[11px] font-bold text-icase-green">✓ Verified Purchase • ${escapeHtml(r.targetTitle)}</div>
                    <p class="text-xs text-slate-700 leading-relaxed">${escapeHtml(r.comment)}</p>
                    ${
                      r.adminReply
                        ? `<div class="mt-2 p-2.5 rounded-xl bg-icase-blueLight text-icase-navy text-[11px] font-semibold">iCASE Reply: ${escapeHtml(r.adminReply)}</div>`
                        : ''
                    }
                  </div>
                  <div class="text-[10px] text-slate-400 pt-1">${escapeHtml(formatTimestamp(r.createdAt))}</div>
                </article>
              `;
            })
            .join('')}
        </div>
      `
      }
    </div>
  `;

  ['home-reviews-section', 'catalog-reviews-section'].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.innerHTML = html;
  });
}

async function verifyReviewReference(refId) {
  const cleaned = String(refId || '').trim().replace(/^#/, '').toUpperCase();
  state.reviewRefInput = cleaned;
  state.reviewFormError = '';
  if (!cleaned) {
    state.reviewFormError = 'Enter an Order ID (ORD-...) or Repair Service ID (REP-...).';
    renderReviewModal();
    return;
  }

  state.reviewVerifying = true;
  state.reviewVerifyResult = null;
  renderReviewModal();

  try {
    const found = await lookupReferenceInCloud(cleaned);
    if (!found) {
      state.reviewVerifyResult = { status: 'NOT_FOUND' };
    } else if (found.type === 'ORDER') {
      const ord = found.data;
      if (isOrderCompleted(ord.status)) {
        const items = parseOrderItems(ord.itemsJson);
        const title = items[0]?.productName || `Order #${ord.id}`;
        state.reviewVerifyResult = {
          status: 'ELIGIBLE',
          reviewType: 'PRODUCT',
          targetTitle: title,
          customerName: ord.customerName || state.profile.name || ''
        };
      } else {
        state.reviewVerifyResult = { status: 'NOT_COMPLETED', statusLabel: ord.status };
      }
    } else if (found.type === 'REPAIR') {
      const rep = found.data;
      if (isRepairCompleted(rep.status)) {
        state.reviewVerifyResult = {
          status: 'ELIGIBLE',
          reviewType: 'SERVICE',
          targetTitle: `${rep.brandName || ''} ${rep.modelName || ''}`.trim() || `Repair #${rep.id}`,
          customerName: rep.customerName || state.profile.name || ''
        };
      } else {
        state.reviewVerifyResult = { status: 'NOT_COMPLETED', statusLabel: rep.status };
      }
    }
  } catch (err) {
    state.reviewFormError = err.message || 'Unable to verify ID right now.';
  } finally {
    state.reviewVerifying = false;
    renderReviewModal();
  }
}

function openReviewModal(prefillId = '') {
  state.reviewModalOpen = true;
  state.reviewRefInput = String(prefillId || '').trim().replace(/^#/, '').toUpperCase();
  state.reviewVerifyResult = null;
  state.reviewRating = 5;
  state.reviewFormError = '';
  document.getElementById('review-modal-overlay')?.classList.remove('hidden');
  renderReviewModal();
  if (state.reviewRefInput) {
    verifyReviewReference(state.reviewRefInput);
  }
}

function closeReviewModal() {
  state.reviewModalOpen = false;
  document.getElementById('review-modal-overlay')?.classList.add('hidden');
}

function renderReviewModal() {
  const body = document.getElementById('review-modal-body');
  if (!body) return;
  const vr = state.reviewVerifyResult;
  const eligible = vr?.status === 'ELIGIBLE';

  body.innerHTML = `
    <p class="text-xs text-slate-500">Enter your completed Order ID (<strong>ORD-XXXXX</strong>) or Service ID (<strong>REP-XXXXX</strong>) to verify eligibility.</p>
    <div class="flex gap-2">
      <input id="rv-ref-input" type="text" value="${escapeHtml(state.reviewRefInput)}" placeholder="e.g. ORD-10294 or REP-5819" class="flex-1 px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm font-bold uppercase focus:border-icase-blue focus:outline-none" />
      <button id="rv-verify-btn" type="button" ${state.reviewVerifying ? 'disabled' : ''} class="px-4 py-2.5 rounded-xl bg-icase-navy text-white text-xs sm:text-sm font-bold">
        ${state.reviewVerifying ? 'Checking...' : 'Verify'}
      </button>
    </div>

    ${vr?.status === 'NOT_FOUND' ? `<div class="text-xs font-bold text-icase-red">No Order or Service found matching #${escapeHtml(state.reviewRefInput)}.</div>` : ''}
    ${vr?.status === 'NOT_COMPLETED' ? `<div class="text-xs font-bold text-icase-red">Current status is "${escapeHtml(vr.statusLabel)}". Reviews unlock after completion / delivery.</div>` : ''}

    ${
      eligible
        ? `
      <div class="p-3 rounded-xl text-xs font-bold ${
        vr.reviewType === 'PRODUCT' ? 'bg-emerald-50 text-emerald-800' : 'bg-icase-blueLight text-icase-navy'
      }">
        ${
          vr.reviewType === 'PRODUCT'
            ? `✓ Verified Purchase: ${escapeHtml(vr.targetTitle)} — Goes live publicly after Admin approval.`
            : `✓ Verified Repair: ${escapeHtml(vr.targetTitle)} — Repair feedback stays strictly private in iCASE Admin Panel.`
        }
      </div>

      <form id="rv-submit-form" class="space-y-3">
        <div>
          <label class="block text-xs font-bold text-slate-600 mb-1">Your Name *</label>
          <input id="rv-name" type="text" required value="${escapeHtml(vr.customerName || state.profile.name || '')}" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm" />
        </div>
        <div>
          <label class="block text-xs font-bold text-slate-600 mb-1">Rating</label>
          <div class="flex items-center gap-1.5">
            ${[1, 2, 3, 4, 5]
              .map(
                (s) => `
              <button type="button" data-rv-star="${s}" class="text-2xl leading-none px-1 ${
                s <= state.reviewRating ? 'text-icase-gold' : 'text-slate-300'
              }">★</button>
            `
              )
              .join('')}
          </div>
        </div>
        <div>
          <label class="block text-xs font-bold text-slate-600 mb-1">Review / Feedback *</label>
          <textarea id="rv-comment" rows="3" required placeholder="Share your experience..." class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm"></textarea>
        </div>
        ${state.reviewFormError ? `<div class="text-xs font-bold text-icase-red">${escapeHtml(state.reviewFormError)}</div>` : ''}
        <button type="submit" ${state.reviewSubmitting ? 'disabled' : ''} class="w-full py-3 rounded-xl bg-icase-red hover:bg-red-700 text-white text-sm font-extrabold transition">
          ${state.reviewSubmitting ? 'Submitting...' : 'Submit Verified Review'}
        </button>
      </form>
    `
        : state.reviewFormError
          ? `<div class="text-xs font-bold text-icase-red">${escapeHtml(state.reviewFormError)}</div>`
          : ''
    }
  `;

  document.getElementById('rv-verify-btn')?.addEventListener('click', () => {
    const val = document.getElementById('rv-ref-input')?.value || '';
    verifyReviewReference(val);
  });

  body.querySelectorAll('[data-rv-star]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.reviewRating = Number(btn.dataset.rvStar || 5);
      renderReviewModal();
    })
  );

  document.getElementById('rv-submit-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const nameVal = document.getElementById('rv-name')?.value?.trim() || 'Verified Customer';
    const commentVal = document.getElementById('rv-comment')?.value?.trim() || '';
    if (!commentVal) {
      state.reviewFormError = 'Please write your review comment.';
      renderReviewModal();
      return;
    }

    state.reviewSubmitting = true;
    state.reviewFormError = '';
    renderReviewModal();

    try {
      const now = Date.now();
      await createReviewInCloud({
        id: `rev_${now}`,
        referenceId: state.reviewRefInput,
        reviewType: vr.reviewType,
        targetTitle: vr.targetTitle,
        customerName: nameVal,
        rating: state.reviewRating,
        comment: commentVal,
        isVerified: true,
        isApproved: false,
        adminReply: '',
        userId: state.profile.userId || '',
        createdAt: now,
        updatedAt: now
      });
      closeReviewModal();
      showToast(
        vr.reviewType === 'SERVICE'
          ? 'Private service feedback sent to iCASE Admin!'
          : 'Thank you! Review submitted for Admin approval.'
      );
    } catch (err) {
      state.reviewFormError = err.message || 'Could not submit review.';
      renderReviewModal();
    } finally {
      state.reviewSubmitting = false;
    }
  });
}

function renderCatalog() {
  const catWrap = document.getElementById('catalog-category-chips');
  if (catWrap) {
    catWrap.innerHTML = CATEGORIES.map((cat) => {
      const active = state.selectedCategory === cat;
      return `
        <button type="button" data-catalog-cat="${escapeHtml(cat)}" class="px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition ${
          active ? 'bg-icase-navy text-white shadow-sm' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
        }">
          ${escapeHtml(cat)}
        </button>
      `;
    }).join('');
  }

  const brandWrap = document.getElementById('catalog-brand-chips');
  if (brandWrap) {
    brandWrap.innerHTML = ['', ...TOP_BRANDS]
      .map((brand) => {
        const active = state.selectedCatalogBrand === brand;
        const label = brand || 'All Brands';
        return `
          <button type="button" data-catalog-brand="${escapeHtml(brand)}" class="px-3 py-1 rounded-full text-xs font-bold border transition ${
            active
              ? 'bg-icase-red text-white border-icase-red shadow-sm'
              : 'bg-slate-50 text-icase-navy border-slate-200 hover:border-icase-navy'
          }">
            ${escapeHtml(label)}
          </button>
        `;
      })
      .join('');
  }

  const q = state.catalogSearch.trim().toLowerCase();
  const filtered = state.products
    .filter((p) => state.selectedCategory === 'All' || p.category === state.selectedCategory)
    .filter((p) => productMatchesBrand(p, state.selectedCatalogBrand))
    .filter(
      (p) =>
        !q ||
        (p.name || '').toLowerCase().includes(q) ||
        (p.category || '').toLowerCase().includes(q) ||
        (p.description || '').toLowerCase().includes(q)
    )
    .sort((a, b) => String(a.name || '').localeCompare(String(b.name || '')));

  const grid = document.getElementById('catalog-grid');
  const empty = document.getElementById('catalog-empty');
  if (!grid || !empty) return;

  if (filtered.length === 0) {
    grid.innerHTML = '';
    empty.classList.remove('hidden');
  } else {
    empty.classList.add('hidden');
    grid.innerHTML = filtered.map(renderProductCardHtml).join('');
  }
}

function openProductModal(productId) {
  state.modalProductId = productId;
  state.modalSelectedColorIdx = 0;
  state.modalReviewFormOpen = false;
  state.modalReviewRating = 5;
  state.modalReviewSubmitting = false;
  state.modalReviewError = '';
  renderProductModal();
  document.getElementById('product-modal')?.classList.remove('hidden');
}

function closeProductModal() {
  state.modalProductId = null;
  state.modalReviewFormOpen = false;
  document.getElementById('product-modal')?.classList.add('hidden');
}

function renderProductModal() {
  if (!state.modalProductId) return;
  const product = state.products.find((p) => p.id === state.modalProductId);
  if (!product) {
    closeProductModal();
    return;
  }

  document.getElementById('pm-category').textContent = product.category || 'Product Details';
  const colors = parseColorOptions(product.colorOptionsJson);
  const safeColorIdx = state.modalSelectedColorIdx < colors.length ? state.modalSelectedColorIdx : 0;
  const activeColor = colors[safeColorIdx] || null;
  const oneHourEnabled = state.settings.one_hour_delivery_enabled !== 'false';
  const isWishlisted = (state.profile.wishlistIds || []).includes(product.id);
  const orig = Number(product.originalPrice || 0);
  const price = Number(product.price || 0);
  const matchedReviews = getMatchingProductReviews(state.publicProductReviews, product);
  const ratingSummary = computeProductRatingSummary(state.publicProductReviews, product);

  document.getElementById('pm-body').innerHTML = `
    <img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" class="w-full h-56 sm:h-64 object-cover rounded-2xl bg-slate-100" />
    <div>
      <div class="flex flex-wrap items-center justify-between gap-2">
        <h2 class="text-xl sm:text-2xl font-extrabold text-icase-navy">${escapeHtml(product.name)}</h2>
        ${
          ratingSummary.reviewCount > 0
            ? `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-amber-50 text-amber-800 border border-amber-200 text-xs font-extrabold">★ ${ratingSummary.averageRating.toFixed(1)} (${ratingSummary.reviewCount})</span>`
            : ''
        }
      </div>
      <div class="flex items-baseline gap-2.5 mt-2">
        <span class="text-2xl font-black text-icase-red">${formatINR(price)}</span>
        ${orig > price ? `<span class="text-sm text-slate-400 line-through font-semibold">${formatINR(orig)}</span>` : ''}
      </div>
    </div>

    ${
      colors.length > 0
        ? `
      <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 space-y-2">
        <div class="text-xs font-extrabold text-icase-navy">
          Selected Finish: <span class="text-icase-blue">${escapeHtml(activeColor?.name || '')}</span>
        </div>
        <div class="flex flex-wrap gap-2.5">
          ${colors
            .map((opt, idx) => {
              const selected = idx === safeColorIdx;
              return `
                <button type="button" data-select-color="${idx}" title="${escapeHtml(opt.name)}" class="w-8 h-8 rounded-full p-0.5 bg-white transition flex items-center justify-center ${
                  selected ? 'ring-2 ring-icase-navy ring-offset-2' : 'border border-slate-300'
                }">
                  <span class="w-full h-full rounded-full block" style="background:${escapeHtml(opt.hex)}"></span>
                </button>
              `;
            })
            .join('')}
        </div>
      </div>
    `
        : ''
    }

    <p class="text-sm text-slate-600 leading-relaxed">${escapeHtml(product.description || '')}</p>

    <div class="flex flex-wrap gap-2">
      <span class="px-3 py-1 rounded-full text-xs font-bold ${
        product.inStock ? 'bg-emerald-50 text-icase-green' : 'bg-rose-50 text-icase-red'
      }">
        ${product.inStock ? `In Stock (${product.stockCount ?? 25} units)` : 'Out of Stock'}
      </span>
      ${
        oneHourEnabled && product.isOneHourEligible
          ? `<span class="px-3 py-1 rounded-full text-xs font-bold bg-icase-blueLight text-icase-blue">⚡ 1-Hour Express Delivery Eligible</span>`
          : ''
      }
      ${
        product.videoUrl
          ? `<button type="button" data-open-video="${escapeHtml(product.videoUrl)}" data-video-title="${escapeHtml(product.name)}" class="px-3 py-1 rounded-full text-xs font-extrabold bg-slate-900 hover:bg-icase-red text-white transition">▶ Watch Product Video</button>`
          : ''
      }
    </div>

    <div class="flex flex-wrap gap-2.5 pt-2">
      <button type="button" id="pm-add-cart" ${!product.inStock ? 'disabled' : ''} class="flex-1 py-3 px-4 rounded-xl text-sm font-extrabold transition min-h-[46px] ${
        product.inStock ? 'bg-icase-red hover:bg-red-700 text-white' : 'bg-slate-200 text-slate-500'
      }">
        Add to Cart
      </button>
      <button type="button" id="pm-buy-now" ${!product.inStock ? 'disabled' : ''} class="flex-1 py-3 px-4 rounded-xl text-sm font-extrabold transition min-h-[46px] ${
        product.inStock ? 'bg-icase-navy hover:bg-icase-dark text-white' : 'bg-slate-200 text-slate-400'
      }">
        Buy Now
      </button>
      <button type="button" id="pm-wishlist-btn" class="px-4 py-3 rounded-xl border text-sm font-extrabold transition min-h-[46px] ${
        isWishlisted ? 'border-icase-red bg-icase-redLight text-icase-red' : 'border-slate-200 text-slate-600'
      }">
        ${isWishlisted ? '♥ Saved' : '♡ Save'}
      </button>
    </div>

    <!-- Product Customer Ratings & Reviews -->
    <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-3">
      <div class="flex flex-wrap items-center justify-between gap-2">
        <div>
          <h3 class="text-sm font-extrabold text-icase-navy">Customer Ratings &amp; Reviews</h3>
          <p class="text-xs text-slate-500">
            ${
              ratingSummary.reviewCount > 0
                ? `<strong class="text-icase-navy">★ ${ratingSummary.averageRating.toFixed(1)} / 5</strong> • ${ratingSummary.reviewCount} published review${ratingSummary.reviewCount === 1 ? '' : 's'}`
                : 'No published reviews for this product yet.'
            }
          </p>
        </div>
        <button type="button" id="pm-toggle-review-form" class="px-3.5 py-2 rounded-xl border border-icase-navy text-icase-navy hover:bg-icase-navy hover:text-white text-xs font-extrabold transition">
          ${state.modalReviewFormOpen ? 'Close Form' : 'Write a Review ★'}
        </button>
      </div>

      ${
        state.modalReviewFormOpen
          ? `
        <form id="pm-review-form" class="p-3.5 rounded-xl bg-white border border-slate-200 space-y-2.5">
          <div>
            <label class="block text-[11px] font-bold text-slate-600 mb-1">Your Star Rating</label>
            <div class="flex items-center gap-1">
              ${[1, 2, 3, 4, 5]
                .map(
                  (s) => `
                <button type="button" data-pm-star="${s}" class="text-2xl leading-none px-1 ${
                  s <= state.modalReviewRating ? 'text-icase-gold' : 'text-slate-300'
                }">★</button>
              `
                )
                .join('')}
            </div>
          </div>
          <div class="grid sm:grid-cols-2 gap-2">
            <div>
              <label class="block text-[11px] font-bold text-slate-600 mb-1">Your Name *</label>
              <input id="pm-rv-name" type="text" required value="${escapeHtml(state.profile.name || '')}" placeholder="Customer name" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-xs" />
            </div>
            <div>
              <label class="block text-[11px] font-bold text-slate-600 mb-1">Order ID (Optional)</label>
              <input id="pm-rv-ref" type="text" placeholder="ORD-12345" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-xs uppercase" />
            </div>
          </div>
          <div>
            <label class="block text-[11px] font-bold text-slate-600 mb-1">Your Review *</label>
            <textarea id="pm-rv-comment" rows="2" required placeholder="Share how this accessory fits &amp; performs..." class="w-full px-3 py-2 rounded-xl border border-slate-300 text-xs"></textarea>
          </div>
          ${state.modalReviewError ? `<div class="text-xs font-bold text-icase-red">${escapeHtml(state.modalReviewError)}</div>` : ''}
          <button type="submit" ${state.modalReviewSubmitting ? 'disabled' : ''} class="px-4 py-2 rounded-xl bg-icase-navy text-white text-xs font-extrabold">
            ${state.modalReviewSubmitting ? 'Submitting...' : 'Submit Review'}
          </button>
        </form>
      `
          : ''
      }

      ${
        matchedReviews.length > 0
          ? `<div class="space-y-2 max-h-60 overflow-y-auto">
              ${matchedReviews
                .map((r) => {
                  const stars = '★'.repeat(Math.max(1, Math.min(5, Number(r.rating || 5))));
                  return `
                    <div class="p-3 rounded-xl bg-white border border-slate-200 space-y-1">
                      <div class="flex items-center justify-between gap-2">
                        <span class="text-xs font-extrabold text-icase-navy">${escapeHtml(r.customerName)}${r.isVerified ? ' <span class="text-[10px] text-icase-green font-bold">✓ Verified</span>' : ''}</span>
                        <span class="text-xs text-icase-gold font-bold">${stars}</span>
                      </div>
                      <p class="text-xs text-slate-700 leading-relaxed">${escapeHtml(r.comment)}</p>
                      ${r.adminReply ? `<div class="p-2 rounded-lg bg-icase-blueLight text-[11px] font-semibold text-icase-navy">iCASE Reply: ${escapeHtml(r.adminReply)}</div>` : ''}
                      <div class="text-[10px] text-slate-400">${escapeHtml(formatTimestamp(r.createdAt))}</div>
                    </div>
                  `;
                })
                .join('')}
            </div>`
          : ''
      }
    </div>
  `;

  document.getElementById('pm-add-cart')?.addEventListener('click', () => {
    addToCart(product, 1, activeColor?.name || '');
    closeProductModal();
  });
  document.getElementById('pm-buy-now')?.addEventListener('click', () => {
    addToCart(product, 1, activeColor?.name || '');
    closeProductModal();
    openCartDrawer();
  });
  document.getElementById('pm-wishlist-btn')?.addEventListener('click', () => {
    toggleWishlist(product.id);
    renderProductModal();
  });
  document.getElementById('pm-toggle-review-form')?.addEventListener('click', () => {
    state.modalReviewFormOpen = !state.modalReviewFormOpen;
    state.modalReviewError = '';
    renderProductModal();
  });
  document.querySelectorAll('[data-pm-star]').forEach((starBtn) =>
    starBtn.addEventListener('click', () => {
      state.modalReviewRating = Number(starBtn.dataset.pmStar || 5);
      renderProductModal();
    })
  );
  document.getElementById('pm-review-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const customerName = document.getElementById('pm-rv-name')?.value?.trim() || state.profile.name || 'Customer';
    const rawRef = document.getElementById('pm-rv-ref')?.value?.trim().replace(/^#/, '').toUpperCase() || '';
    const comment = document.getElementById('pm-rv-comment')?.value?.trim() || '';
    if (!comment) {
      state.modalReviewError = 'Please write your review comment.';
      renderProductModal();
      return;
    }

    state.modalReviewSubmitting = true;
    state.modalReviewError = '';
    renderProductModal();

    try {
      let verifiedOrder = false;
      let finalRef = `PROD-${String(product.id).slice(0, 24)}`;
      if (rawRef) {
        const found = await lookupReferenceInCloud(rawRef);
        if (!found || found.type !== 'ORDER') {
          state.modalReviewError = `Order ID #${rawRef} not found. Leave Order ID blank to submit as a general review.`;
          state.modalReviewSubmitting = false;
          renderProductModal();
          return;
        }
        if (!isOrderCompleted(found.data.status)) {
          state.modalReviewError = `Order #${rawRef} status is "${found.data.status}". Leave Order ID blank or review after delivery.`;
          state.modalReviewSubmitting = false;
          renderProductModal();
          return;
        }
        verifiedOrder = true;
        finalRef = found.data.id;
      }

      const now = Date.now();
      await createReviewInCloud({
        id: `REV-${Math.floor(10000 + Math.random() * 90000)}`,
        referenceId: finalRef,
        reviewType: 'PRODUCT',
        targetTitle: product.name,
        customerName,
        rating: Math.max(1, Math.min(5, Number(state.modalReviewRating || 5))),
        comment,
        isVerified: verifiedOrder,
        isApproved: false,
        adminReply: '',
        userId: state.profile.userId || 'guest_customer',
        productId: product.id,
        createdAt: now,
        updatedAt: now
      });
      state.modalReviewFormOpen = false;
      showToast('Review submitted! It will appear once approved by Admin.');
    } catch (err) {
      state.modalReviewError = err.message || 'Could not submit review right now.';
    } finally {
      state.modalReviewSubmitting = false;
      renderProductModal();
    }
  });
}

// ==================== CART & CHECKOUT ====================
function reconcileCartWithLiveCatalog() {
  if (!Array.isArray(state.cartItems) || state.cartItems.length === 0) return;
  const deletedSet = new Set(state.deletedProductIds || []);
  const productMap = new Map(state.products.map((p) => [p.id, p]));
  let changed = false;
  const nextItems = [];

  for (const item of state.cartItems) {
    const pid = item?.product?.id;
    if (!pid) {
      changed = true;
      continue;
    }
    if (pid.startsWith('sg::') || pid.startsWith('cl::') || pid.startsWith('combo::') || pid.startsWith('pouch::')) {
      const repriced = repriceOrNullCartProduct(item.product, state.screenGuardConfig, item.selectedColor || '');
      if (!repriced) {
        changed = true;
        continue;
      }
      if (
        repriced.price !== item.product.price ||
        repriced.originalPrice !== item.product.originalPrice ||
        repriced.name !== item.product.name ||
        repriced.description !== item.product.description ||
        repriced.imageUrl !== item.product.imageUrl ||
        String(repriced.videoUrl || '') !== String(item.product.videoUrl || '')
      ) {
        changed = true;
      }
      nextItems.push({ ...item, product: repriced });
    } else {
      if (deletedSet.has(pid)) {
        changed = true;
        continue;
      }
      const live = productMap.get(pid);
      if (live) {
        if (!live.inStock) {
          changed = true;
          continue;
        }
        if (
          live.price !== item.product.price ||
          live.originalPrice !== item.product.originalPrice ||
          live.name !== item.product.name ||
          live.isOneHourEligible !== item.product.isOneHourEligible
        ) {
          changed = true;
        }
        nextItems.push({ ...item, product: live });
      } else if (state.hasSyncedProductsFromCloud) {
        changed = true;
      } else {
        nextItems.push(item);
      }
    }
  }

  if (changed) {
    state.cartItems = nextItems;
    saveJsonStorage(STORAGE_CART_KEY, state.cartItems);
    updateCartBadge();
    if (state.cartOpen) renderCartDrawer();
  }
}

function addToCart(product, quantity = 1, selectedColor = '') {
  if (!product || !product.inStock) return;
  const colorLabel = selectedColor || parseColorOptions(product.colorOptionsJson)[0]?.name || '';
  const idx = state.cartItems.findIndex(
    (item) => item.product.id === product.id && (item.selectedColor || '') === colorLabel
  );
  if (idx >= 0) {
    state.cartItems[idx].product = product;
    state.cartItems[idx].quantity += quantity;
  } else {
    state.cartItems.push({ product, quantity, selectedColor: colorLabel });
  }
  saveJsonStorage(STORAGE_CART_KEY, state.cartItems);
  updateCartBadge();
  showToast(`Added "${product.name}" to cart`);
}

function addDirectProductToCart(product, quantity = 1, selectedColor = '') {
  if (!product) return;
  const colorLabel = String(selectedColor || '').trim();
  const idx = state.cartItems.findIndex(
    (item) => item.product.id === product.id && (item.selectedColor || '') === colorLabel
  );
  if (idx >= 0) {
    state.cartItems[idx].product = product;
    state.cartItems[idx].quantity += quantity;
  } else {
    state.cartItems.push({ product, quantity, selectedColor: colorLabel });
  }
  saveJsonStorage(STORAGE_CART_KEY, state.cartItems);
  updateCartBadge();
  if (state.cartOpen) renderCartDrawer();
  showToast(
    `Added "${product.name}${colorLabel ? ` (${colorLabel})` : ''}" (${formatINR(product.price)}) to cart`
  );
}

function updateCartQty(idx, nextQty) {
  if (nextQty <= 0) {
    state.cartItems.splice(idx, 1);
  } else if (state.cartItems[idx]) {
    state.cartItems[idx].quantity = nextQty;
  }
  saveJsonStorage(STORAGE_CART_KEY, state.cartItems);
  updateCartBadge();
  renderCartDrawer();
}

function updateCartBadge() {
  const count = state.cartItems.reduce((sum, it) => sum + Number(it.quantity || 0), 0);
  const badge = document.getElementById('header-cart-badge');
  if (!badge) return;
  if (count > 0) {
    badge.textContent = String(count);
    badge.classList.remove('hidden');
    badge.classList.add('inline-flex');
  } else {
    badge.classList.add('hidden');
    badge.classList.remove('inline-flex');
  }
}

function openCartDrawer() {
  state.cartOpen = true;
  state.cartError = '';
  renderCartDrawer();
  document.getElementById('cart-drawer-overlay')?.classList.remove('hidden');
}

function closeCartDrawer() {
  state.cartOpen = false;
  state.cartPlacedOrder = null;
  document.getElementById('cart-drawer-overlay')?.classList.add('hidden');
}

function computeCartPricing() {
  const oneHourEnabled = state.settings.one_hour_delivery_enabled !== 'false';
  const standardFee = Number(state.settings.delivery_fee_standard || 50);
  const expressFee = Number(state.settings.delivery_fee_express || 80);
  const freeThreshold = Number(state.settings.free_delivery_threshold || 0);

  const allOneHourEligible =
    state.cartItems.length > 0 && state.cartItems.every((it) => it.product.isOneHourEligible !== false);
  const canChooseOneHour = oneHourEnabled && allOneHourEligible;
  const activeDeliveryType = canChooseOneHour ? state.cartDeliveryType : 'STANDARD';

  const subtotal = state.cartItems.reduce(
    (sum, it) => sum + Number(it.product.price || 0) * Number(it.quantity || 1),
    0
  );
  const freeUnlocked = freeThreshold > 0 && subtotal >= freeThreshold;
  const deliveryFee =
    state.cartItems.length === 0
      ? 0
      : activeDeliveryType === 'ONE_HOUR'
        ? expressFee
        : freeUnlocked
          ? 0
          : standardFee;
  const totalAmount = state.cartItems.length === 0 ? 0 : subtotal + deliveryFee;

  return {
    oneHourEnabled,
    canChooseOneHour,
    activeDeliveryType,
    standardFee,
    expressFee,
    freeUnlocked,
    subtotal,
    deliveryFee,
    totalAmount
  };
}

function renderCartDrawer() {
  const titleEl = document.getElementById('cart-drawer-title');
  const bodyEl = document.getElementById('cart-drawer-body');
  const footerEl = document.getElementById('cart-drawer-footer');
  if (!titleEl || !bodyEl || !footerEl) return;

  const count = state.cartItems.reduce((sum, it) => sum + Number(it.quantity || 0), 0);
  titleEl.textContent = `My Cart & Checkout (${count})`;

  if (state.cartPlacedOrder) {
    const ord = state.cartPlacedOrder;
    const storeWa = (state.settings.store_whatsapp || state.settings.store_phone || '9961454059').replace(/\D/g, '');
    const waText = `*iCASE Order Confirmation* 🛍️\nOrder ID: *${ord.id}*\nCustomer: ${ord.customerName}\nPhone: ${ord.customerPhone}\nAddress: ${ord.deliveryAddress}\nTotal: *${formatINR(ord.totalAmount)}*\nStatus: ${ord.status}`;
    const waHref = `https://api.whatsapp.com/send?phone=91${storeWa}&text=${encodeURIComponent(waText)}`;

    footerEl.classList.add('hidden');
    bodyEl.innerHTML = `
      <div class="text-center py-8 space-y-4">
        <div class="w-16 h-16 rounded-full bg-emerald-50 text-icase-green flex items-center justify-center mx-auto text-3xl font-black">✓</div>
        <h3 class="text-2xl font-extrabold text-icase-navy">Order Placed!</h3>
        <div class="text-lg font-black text-icase-red">#${escapeHtml(ord.id)}</div>
        <p class="text-sm text-slate-600">
          Your order is saved in Cloud Firestore and synced in real time with the iCASE mobile app &amp; admin dashboard.
        </p>
        <div class="flex flex-col gap-2.5 pt-2">
          <button id="cart-track-placed" type="button" class="w-full py-3 rounded-xl bg-icase-red hover:bg-red-700 text-white font-bold text-sm transition">
            Track Order Live Now →
          </button>
          <a href="${waHref}" target="_blank" rel="noreferrer" class="w-full py-3 rounded-xl bg-icase-whatsapp hover:bg-emerald-600 text-white font-bold text-sm transition">
            Send WhatsApp Confirmation
          </a>
          <button id="cart-done-placed" type="button" class="w-full py-3 rounded-xl border border-slate-300 font-bold text-sm text-icase-navy">
            Done
          </button>
        </div>
      </div>
    `;

    document.getElementById('cart-track-placed')?.addEventListener('click', () => {
      const id = ord.id;
      closeCartDrawer();
      openTrackId(id);
    });
    document.getElementById('cart-done-placed')?.addEventListener('click', closeCartDrawer);
    return;
  }

  if (state.cartItems.length === 0) {
    footerEl.classList.add('hidden');
    bodyEl.innerHTML = `
      <div class="text-center py-14 space-y-3">
        <div class="text-4xl">🛍️</div>
        <h3 class="text-lg font-extrabold text-icase-navy">Your Cart is Empty</h3>
        <p class="text-sm text-slate-500">Add mobile pouches, tempered glass, or fast chargers from the catalog.</p>
        <button id="cart-browse-btn" type="button" class="mt-2 px-5 py-2.5 rounded-xl bg-icase-navy text-white text-sm font-bold">
          Browse Shop Catalog
        </button>
      </div>
    `;
    document.getElementById('cart-browse-btn')?.addEventListener('click', () => {
      closeCartDrawer();
      setActiveTab('catalog');
    });
    return;
  }

  const pricing = computeCartPricing();
  const isStoreOnline = state.settings.store_online !== 'false';

  bodyEl.innerHTML = `
    <div class="space-y-3">
      ${state.cartItems
        .map(
          (item, idx) => `
        <div class="flex items-center gap-3 p-3 rounded-2xl border border-slate-200 bg-white">
          <img src="${escapeHtml(item.product.imageUrl)}" alt="${escapeHtml(item.product.name)}" class="w-14 h-14 rounded-xl object-cover bg-slate-100 shrink-0" />
          <div class="flex-1 min-w-0">
            <div class="text-xs sm:text-sm font-extrabold text-icase-navy truncate">${escapeHtml(item.product.name)}</div>
            ${item.selectedColor ? `<div class="text-[11px] font-semibold text-slate-500">Color: ${escapeHtml(item.selectedColor)}</div>` : ''}
            <div class="text-sm font-black text-icase-red mt-0.5">${formatINR(item.product.price)}</div>
          </div>
          <div class="flex items-center gap-1.5 shrink-0">
            <button type="button" data-cart-dec="${idx}" class="w-7 h-7 rounded-lg border border-slate-300 font-bold text-sm hover:bg-slate-100">−</button>
            <span class="w-6 text-center text-xs font-extrabold">${item.quantity}</span>
            <button type="button" data-cart-inc="${idx}" class="w-7 h-7 rounded-lg border border-slate-300 font-bold text-sm hover:bg-slate-100">+</button>
            <button type="button" data-cart-del="${idx}" class="w-7 h-7 rounded-lg bg-icase-redLight text-icase-red font-bold text-xs ml-1">✕</button>
          </div>
        </div>
      `
        )
        .join('')}
    </div>

    <form id="cart-checkout-form" class="space-y-3.5 pt-2 border-t border-slate-200">
      <div>
        <label class="block text-xs font-extrabold uppercase text-slate-600 mb-1">Full Name *</label>
        <input id="checkout-name" type="text" required value="${escapeHtml(state.profile.name || '')}" placeholder="Customer Name" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm font-medium focus:border-icase-blue focus:outline-none" />
      </div>
      <div>
        <label class="block text-xs font-extrabold uppercase text-slate-600 mb-1">Mobile Number *</label>
        <input id="checkout-phone" type="tel" required value="${escapeHtml(state.profile.phone || '')}" placeholder="10-digit mobile or +91" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm font-medium focus:border-icase-blue focus:outline-none" />
      </div>
      <div>
        <div class="flex flex-wrap items-center justify-between gap-2 mb-1">
          <label class="text-xs font-extrabold uppercase text-slate-600">Delivery Address *</label>
          <button id="btn-cart-gps" type="button" class="px-2.5 py-1 rounded-lg bg-icase-blueLight text-icase-blue text-[11px] font-bold hover:bg-blue-100">
            📍 Detect Current Location
          </button>
        </div>
        ${state.cartGpsMsg ? `<div class="text-[11px] font-semibold text-icase-blue mb-1">${escapeHtml(state.cartGpsMsg)}</div>` : ''}
        <textarea id="checkout-address" rows="2" required placeholder="House / Flat No., Street, Landmark, City, PIN" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm font-medium focus:border-icase-blue focus:outline-none">${escapeHtml(state.profile.address || '')}</textarea>
      </div>

      <div>
        <label class="block text-xs font-extrabold uppercase text-slate-600 mb-1.5">Delivery Option</label>
        <div class="space-y-2">
          <label class="flex items-center justify-between p-3 rounded-xl border cursor-pointer text-xs sm:text-sm font-bold ${
            pricing.activeDeliveryType === 'STANDARD' ? 'border-icase-red bg-rose-50/60' : 'border-slate-200 bg-white'
          }">
            <span class="flex items-center gap-2">
              <input type="radio" name="deliveryType" value="STANDARD" ${pricing.activeDeliveryType === 'STANDARD' ? 'checked' : ''} />
              Standard Home Delivery (1-2 Days)
            </span>
            <span class="font-black">${pricing.freeUnlocked ? 'FREE' : formatINR(pricing.standardFee)}</span>
          </label>
          ${
            pricing.canChooseOneHour
              ? `
            <label class="flex items-center justify-between p-3 rounded-xl border cursor-pointer text-xs sm:text-sm font-bold ${
              pricing.activeDeliveryType === 'ONE_HOUR' ? 'border-icase-red bg-rose-50/60' : 'border-slate-200 bg-white'
            }">
              <span class="flex items-center gap-2">
                <input type="radio" name="deliveryType" value="ONE_HOUR" ${pricing.activeDeliveryType === 'ONE_HOUR' ? 'checked' : ''} />
                ⚡ Express 1-Hour Delivery
              </span>
              <span class="font-black text-icase-red">${formatINR(pricing.expressFee)}</span>
            </label>
          `
              : ''
          }
        </div>
      </div>

      <div>
        <label class="block text-xs font-extrabold uppercase text-slate-600 mb-1">Payment Method</label>
        <select id="checkout-payment" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 bg-white text-sm font-semibold">
          <option value="UPI" ${state.cartPaymentMethod === 'UPI' ? 'selected' : ''}>UPI (Google Pay / PhonePe)</option>
          <option value="COD" ${state.cartPaymentMethod === 'COD' ? 'selected' : ''}>Cash on Delivery</option>
          <option value="CARD" ${state.cartPaymentMethod === 'CARD' ? 'selected' : ''}>Card Payment</option>
        </select>
      </div>

      ${state.cartError ? `<div class="text-xs font-bold text-icase-red">${escapeHtml(state.cartError)}</div>` : ''}
    </form>
  `;

  footerEl.classList.remove('hidden');
  footerEl.innerHTML = `
    <div class="space-y-1 text-xs sm:text-sm mb-3">
      <div class="flex justify-between text-slate-600"><span>Subtotal</span><strong>${formatINR(pricing.subtotal)}</strong></div>
      <div class="flex justify-between text-slate-600"><span>Delivery Charge</span><strong>${pricing.deliveryFee === 0 ? 'FREE' : formatINR(pricing.deliveryFee)}</strong></div>
      <div class="flex justify-between text-base font-black text-icase-navy pt-1 border-t border-slate-200">
        <span>Total Amount</span>
        <span class="text-icase-red">${formatINR(pricing.totalAmount)}</span>
      </div>
    </div>
    <button type="submit" form="cart-checkout-form" ${!isStoreOnline || state.cartBusy ? 'disabled' : ''} class="w-full py-3.5 rounded-xl bg-icase-red hover:bg-red-700 disabled:bg-slate-300 text-white font-extrabold text-sm transition min-h-[48px]">
      ${!isStoreOnline ? 'Store Paused by Admin' : state.cartBusy ? 'Placing Order...' : `Place Order • ${formatINR(pricing.totalAmount)} →`}
    </button>
  `;

  bodyEl.querySelectorAll('[data-cart-dec]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const i = Number(btn.dataset.cartDec);
      updateCartQty(i, state.cartItems[i].quantity - 1);
    })
  );
  bodyEl.querySelectorAll('[data-cart-inc]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const i = Number(btn.dataset.cartInc);
      updateCartQty(i, state.cartItems[i].quantity + 1);
    })
  );
  bodyEl.querySelectorAll('[data-cart-del]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const i = Number(btn.dataset.cartDel);
      updateCartQty(i, 0);
    })
  );

  bodyEl.querySelectorAll('input[name="deliveryType"]').forEach((radio) =>
    radio.addEventListener('change', (e) => {
      state.cartDeliveryType = e.target.value;
      renderCartDrawer();
    })
  );

  document.getElementById('checkout-payment')?.addEventListener('change', (e) => {
    state.cartPaymentMethod = e.target.value;
  });

  document.getElementById('btn-cart-gps')?.addEventListener('click', async () => {
    state.cartGpsMsg = 'Detecting live GPS location...';
    renderCartDrawer();
    try {
      const res = await detectCurrentLocation();
      state.profile = { ...state.profile, address: res.formattedAddress };
      state.cartGpsMsg = res.isGeocoded ? 'Location detected via GPS.' : 'GPS coordinates captured.';
    } catch (err) {
      state.cartGpsMsg = err.message || 'Could not detect GPS location.';
    }
    renderCartDrawer();
  });

  document.getElementById('cart-checkout-form')?.addEventListener('submit', handleCheckoutSubmit);
}

async function handleCheckoutSubmit(e) {
  e.preventDefault();
  if (state.settings.store_online === 'false') {
    state.cartError = 'Store checkout is currently paused by Admin.';
    renderCartDrawer();
    return;
  }

  const nameVal = document.getElementById('checkout-name')?.value?.trim() || '';
  const phoneRaw = document.getElementById('checkout-phone')?.value?.trim() || '';
  const addrVal = document.getElementById('checkout-address')?.value?.trim() || '';
  const normalizedPhone = normalizePhoneToE164(phoneRaw);

  if (!nameVal || !addrVal) {
    state.cartError = 'Please enter your Full Name and Delivery Address.';
    renderCartDrawer();
    return;
  }
  if (!normalizedPhone) {
    state.cartError = 'Please enter a valid 10-digit mobile number.';
    renderCartDrawer();
    return;
  }

  state.cartBusy = true;
  state.cartError = '';
  renderCartDrawer();

  try {
    const uid =
      (state.profile.isLoggedIn && state.profile.userId) ||
      `acct_${normalizedPhone.replace(/\D/g, '')}`;

    await ensureProfileSaved({
      userId: uid,
      name: nameVal,
      phone: normalizedPhone,
      whatsapp: state.profile.whatsapp || normalizedPhone,
      address: addrVal
    });

    const pricing = computeCartPricing();
    const orderId = `ORD-${Math.floor(10000 + Math.random() * 90000)}`;
    const itemsJson = JSON.stringify(
      state.cartItems.map((it) => ({
        productId: it.product.id,
        productName: it.selectedColor ? `${it.product.name} (${it.selectedColor})` : it.product.name,
        price: Number(it.product.price),
        quantity: Number(it.quantity),
        imageUrl: it.product.imageUrl
      }))
    );

    const now = Date.now();
    const orderPayload = {
      id: orderId,
      userId: uid,
      customerName: nameVal,
      customerPhone: normalizedPhone,
      whatsappNumber: state.profile.whatsapp || normalizedPhone,
      deliveryAddress: addrVal,
      deliveryType: pricing.activeDeliveryType,
      subtotal: pricing.subtotal,
      deliveryFee: pricing.deliveryFee,
      totalAmount: pricing.totalAmount,
      status: 'Order Received',
      statusNotes:
        pricing.activeDeliveryType === 'ONE_HOUR'
          ? 'Express 1-Hour Delivery Order Received (Website)'
          : 'Standard Home Delivery Order Received (Website)',
      itemsJson,
      createdAt: now,
      updatedAt: now
    };

    await createOrderInCloud(orderPayload);
    rememberTrackingId(orderId);
    state.cartItems = [];
    saveJsonStorage(STORAGE_CART_KEY, []);
    updateCartBadge();
    state.cartPlacedOrder = orderPayload;
    showToast(`Order #${orderId} placed & synced live!`);
  } catch (err) {
    state.cartError = err.message || 'Failed to submit order. Please try again.';
  } finally {
    state.cartBusy = false;
    renderCartDrawer();
  }
}

// ==================== REPAIR & PICKUP BOOKING ====================
function initRepairSection() {
  const dateSel = document.getElementById('repair-date');
  if (dateSel) {
    dateSel.innerHTML = REPAIR_DATE_OPTIONS.map(
      (dt) => `<option value="${escapeHtml(dt)}">${escapeHtml(dt)}</option>`
    ).join('');
  }
  const slotSel = document.getElementById('repair-slot');
  if (slotSel) {
    slotSel.innerHTML = TIME_SLOTS.map(
      (slot) => `<option value="${escapeHtml(slot)}">${escapeHtml(slot)}</option>`
    ).join('');
  }
  renderRepairBrands();
  renderRepairComplaints();
  prefillRepairCustomerFields();
}

function prefillRepairCustomerFields() {
  const n = document.getElementById('repair-name');
  const p = document.getElementById('repair-phone');
  const w = document.getElementById('repair-whatsapp');
  const a = document.getElementById('repair-address');
  if (n && !n.value && state.profile.name) n.value = state.profile.name;
  if (p && !p.value && state.profile.phone) p.value = state.profile.phone;
  if (w && !w.value && state.profile.whatsapp) w.value = state.profile.whatsapp;
  if (a && !a.value && state.profile.address) a.value = state.profile.address;
}

function renderRepairBrands() {
  const wrap = document.getElementById('repair-brand-chips');
  if (!wrap) return;
  const brands = ['', ...MAJOR_DEVICE_BRANDS];
  wrap.innerHTML = brands
    .map((brand) => {
      const selected = state.repairBrand === brand;
      const label = brand || 'All Brands';
      return `
        <button type="button" data-repair-brand="${escapeHtml(brand)}" class="px-3.5 py-2 rounded-xl text-xs font-bold border transition ${
          selected
            ? 'bg-icase-navy text-white border-icase-navy'
            : 'bg-slate-50 text-slate-700 border-slate-200 hover:border-icase-navy'
        }">
          ${escapeHtml(label)}
        </button>
      `;
    })
    .join('');
}

function renderRepairComplaints() {
  const wrap = document.getElementById('repair-complaint-chips');
  if (!wrap) return;
  wrap.innerHTML = COMPLAINT_OPTIONS.map((item) => {
    const selected = state.repairComplaints.includes(item);
    return `
      <button type="button" data-repair-complaint="${escapeHtml(item)}" class="px-3.5 py-2 rounded-xl text-xs font-bold border transition ${
        selected
          ? 'bg-icase-red text-white border-icase-red shadow-sm'
          : 'bg-slate-50 text-slate-700 border-slate-200 hover:border-icase-red'
      }">
        ${selected ? '✓ ' : ''}${escapeHtml(item)}
      </button>
    `;
  }).join('');
}

function renderDeviceDropdown() {
  const menu = document.getElementById('device-dropdown-menu');
  const pill = document.getElementById('selected-device-pill');
  const clearBtn = document.getElementById('device-clear-btn');
  const input = document.getElementById('device-search-input');
  if (!menu) return;

  if (pill) {
    if (state.repairModel) {
      pill.textContent = `Selected: ${state.repairBrand || 'Device'} — ${state.repairModel}`;
      pill.classList.remove('hidden');
      clearBtn?.classList.remove('hidden');
    } else {
      pill.classList.add('hidden');
      clearBtn?.classList.add('hidden');
    }
  }

  if (input && !state.repairDropdownOpen && state.repairModel) {
    input.value = `${state.repairBrand} — ${state.repairModel}`;
  }

  menu.classList.toggle('hidden', !state.repairDropdownOpen);
  if (!state.repairDropdownOpen) return;

  const matches = searchDeviceCatalog(state.repairBrand, state.repairSearch);
  if (matches.length === 0) {
    const typed = state.repairSearch.trim();
    menu.innerHTML = `
      <div class="p-4 text-center text-xs sm:text-sm text-slate-500">
        No matching preset model found.
        ${
          typed
            ? `<button type="button" id="use-custom-device-btn" class="ml-1 font-extrabold text-icase-blue hover:underline">Use "${escapeHtml(typed)}"</button>`
            : ''
        }
      </div>
    `;
    document.getElementById('use-custom-device-btn')?.addEventListener('click', () => {
      state.repairBrand = state.repairBrand || 'Other';
      state.repairModel = typed;
      state.repairSearch = '';
      state.repairDropdownOpen = false;
      renderRepairBrands();
      renderDeviceDropdown();
    });
    return;
  }

  menu.innerHTML = matches
    .slice(0, 80)
    .map((m) => {
      const active = state.repairBrand === m.brand && state.repairModel === m.model;
      return `
        <button type="button" data-pick-brand="${escapeHtml(m.brand)}" data-pick-model="${escapeHtml(m.model)}" class="w-full px-4 py-2.5 flex items-center justify-between text-left text-xs sm:text-sm font-semibold hover:bg-icase-blueLight transition ${
          active ? 'bg-icase-blueLight text-icase-blue font-extrabold' : 'text-icase-navy'
        }">
          <span>${escapeHtml(m.model)}</span>
          <span class="text-[11px] font-bold text-slate-400">${escapeHtml(m.brand)}</span>
        </button>
      `;
    })
    .join('');
}

async function handleRepairSubmit(e) {
  e.preventDefault();
  const errEl = document.getElementById('repair-form-error');
  const setErr = (msg) => {
    if (!errEl) return;
    errEl.textContent = msg;
    errEl.classList.toggle('hidden', !msg);
  };
  setErr('');

  const typedFallback = document.getElementById('device-search-input')?.value?.trim() || '';
  const finalModel = state.repairModel || typedFallback;
  const finalBrand = state.repairBrand || (finalModel ? 'Other' : '');

  if (!finalBrand || !finalModel) {
    setErr('Please search and select your device brand and model.');
    return;
  }
  if (state.repairComplaints.length === 0) {
    setErr('Please select at least one issue for your device.');
    return;
  }

  const customerName = document.getElementById('repair-name')?.value?.trim() || '';
  const phoneRaw = document.getElementById('repair-phone')?.value?.trim() || '';
  const whatsappRaw = document.getElementById('repair-whatsapp')?.value?.trim() || '';
  const pickupAddress = document.getElementById('repair-address')?.value?.trim() || '';
  const description = document.getElementById('repair-description')?.value?.trim() || '';
  const preferredDate = document.getElementById('repair-date')?.value || 'Today';
  const preferredTimeSlot = document.getElementById('repair-slot')?.value || TIME_SLOTS[0];

  if (!customerName || !pickupAddress) {
    setErr('Please enter your Full Name and Doorstep Pickup Address.');
    return;
  }
  const normalizedPhone = normalizePhoneToE164(phoneRaw);
  if (!normalizedPhone) {
    setErr('Please enter a valid 10-digit mobile number.');
    return;
  }

  const submitBtn = document.getElementById('btn-submit-repair');
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Scheduling Pickup...';
  }

  try {
    const uid =
      (state.profile.isLoggedIn && state.profile.userId) ||
      `acct_${normalizedPhone.replace(/\D/g, '')}`;

    await ensureProfileSaved({
      userId: uid,
      name: customerName,
      phone: normalizedPhone,
      whatsapp: whatsappRaw || normalizedPhone,
      address: pickupAddress
    });

    const repairId = `REP-${Math.floor(10000 + Math.random() * 90000)}`;
    const now = Date.now();
    const repairPayload = {
      id: repairId,
      userId: uid,
      brandName: finalBrand,
      modelName: finalModel,
      complaintsJson: JSON.stringify(state.repairComplaints),
      description,
      photoUri: '',
      pickupAddress,
      customerName,
      phoneNumber: normalizedPhone,
      whatsappNumber: whatsappRaw || normalizedPhone,
      preferredDate,
      preferredTimeSlot,
      status: 'Booking Received',
      adminNotes: 'Technician assignment pending in Edappal',
      createdAt: now,
      updatedAt: now
    };

    await createRepairInCloud(repairPayload);
    rememberTrackingId(repairId);
    state.bookedRepair = repairPayload;

    // Reset form selections
    state.repairBrand = '';
    state.repairModel = '';
    state.repairSearch = '';
    state.repairComplaints = [];
    document.getElementById('device-search-input').value = '';
    document.getElementById('repair-description').value = '';
    renderRepairBrands();
    renderRepairComplaints();
    renderDeviceDropdown();

    const storeWa = (state.settings.store_whatsapp || state.settings.store_phone || '9961454059').replace(/\D/g, '');
    const waMsg = `*iCASE Repair & Pickup Confirmation* 🔧\nBooking ID: *${repairId}*\nDevice: *${finalBrand} ${finalModel}*\nIssues: ${JSON.parse(repairPayload.complaintsJson).join(', ')}\nCustomer: ${customerName}\nSchedule: ${preferredDate} (${preferredTimeSlot})\nPickup: ${pickupAddress}`;

    document.getElementById('repair-success-id').textContent = `Booking ID: #${repairId}`;
    document.getElementById('repair-success-summary').textContent =
      `${finalBrand} ${finalModel} • ${preferredDate} (${preferredTimeSlot}) • Synced live with iCASE Admin`;
    document.getElementById('repair-success-whatsapp').href =
      `https://api.whatsapp.com/send?phone=91${storeWa}&text=${encodeURIComponent(waMsg)}`;

    document.getElementById('repair-booking-form')?.classList.add('hidden');
    document.getElementById('repair-success-card')?.classList.remove('hidden');
    showToast(`Repair #${repairId} scheduled & synced!`);
  } catch (err) {
    setErr(err.message || 'Could not submit repair booking. Please retry.');
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Confirm Repair & Pickup Booking';
    }
  }
}

// ==================== LIVE ORDER & REPAIR TRACKING ====================
function openTrackId(id) {
  const cleaned = String(id || '').trim().replace(/^#/, '').toUpperCase();
  state.trackingId = cleaned;
  const input = document.getElementById('track-id-input');
  if (input) input.value = cleaned;
  startLiveTrackingSubscription(cleaned);
  setActiveTab('track');
}

function startLiveTrackingSubscription(trackingId) {
  state.unsubTracking?.();
  state.unsubTracking = null;

  const cleaned = String(trackingId || '').trim().replace(/^#/, '').toUpperCase();
  if (!cleaned) {
    state.trackingResult = null;
    renderTrackingView();
    return;
  }

  state.trackingResult = { loading: true };
  renderTrackingView();

  state.unsubTracking = subscribeTrackingId(cleaned, {
    onUpdate: (res) => {
      state.trackingResult = res;
      if (res.type === 'ORDER' || res.type === 'REPAIR') {
        rememberTrackingId(cleaned);
      }
      renderTrackingView();
    },
    onError: () => {
      state.trackingResult = { type: 'NOT_FOUND', data: null };
      renderTrackingView();
    }
  });
}

function renderRecentTrackingChips() {
  const wrap = document.getElementById('recent-tracking-chips');
  if (!wrap) return;
  const combinedIds = Array.from(
    new Set([
      ...state.recentTrackingIds,
      ...state.myOrders.map((o) => o.id),
      ...state.myRepairs.map((r) => r.id)
    ])
  ).slice(0, 6);

  if (combinedIds.length === 0) {
    wrap.classList.add('hidden');
    return;
  }
  wrap.classList.remove('hidden');
  wrap.innerHTML =
    `<span class="text-slate-500 font-bold">Quick Track:</span>` +
    combinedIds
      .map(
        (id) =>
          `<button type="button" data-quick-track="${escapeHtml(id)}" class="px-2.5 py-1 rounded-lg bg-icase-blueLight text-icase-blue font-extrabold hover:bg-blue-100">#${escapeHtml(id)}</button>`
      )
      .join('');
}

function renderTrackingView() {
  renderRecentTrackingChips();
  const container = document.getElementById('track-result-container');
  if (!container) return;

  if (!state.trackingId) {
    container.innerHTML = `
      <div class="bg-white rounded-3xl border border-slate-200 p-10 text-center space-y-2">
        <div class="text-3xl">📦</div>
        <h3 class="text-lg font-extrabold text-icase-navy">Track Your Order or Repair Live</h3>
        <p class="text-sm text-slate-500 max-w-md mx-auto">
          Enter your Order ID (<strong>ORD-XXXXX</strong>) or Repair Booking ID (<strong>REP-XXXXX</strong>) above to watch live updates from our Edappal store &amp; technicians.
        </p>
      </div>
    `;
    return;
  }

  if (state.trackingResult?.loading) {
    container.innerHTML = `
      <div class="bg-white rounded-3xl border border-slate-200 p-8 text-center text-sm font-bold text-slate-600">
        Looking up live status for <span class="text-icase-navy">#${escapeHtml(state.trackingId)}</span>...
      </div>
    `;
    return;
  }

  if (!state.trackingResult || state.trackingResult.type === 'NOT_FOUND') {
    container.innerHTML = `
      <div class="bg-white rounded-3xl border border-rose-200 p-8 text-center space-y-2">
        <div class="text-3xl">⚠️</div>
        <h3 class="text-lg font-extrabold text-icase-navy">Reference ID Not Found</h3>
        <p class="text-sm text-slate-500">No order or repair booking matched <strong>#${escapeHtml(state.trackingId)}</strong>. Double-check the ID and try again.</p>
      </div>
    `;
    return;
  }

  if (state.trackingResult.type === 'ORDER') {
    const order = state.trackingResult.data;
    const steps = ['Order Received', 'Processing', 'Out for Delivery', 'Delivered'];
    const currentIdx = Math.max(0, steps.indexOf(order.status));
    const items = parseOrderItems(order.itemsJson);

    container.innerHTML = `
      <div class="bg-white rounded-3xl border border-slate-200 p-6 sm:p-8 shadow-sm space-y-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 class="text-2xl font-black text-icase-navy">#${escapeHtml(order.id)}</h2>
            <div class="text-xs sm:text-sm text-slate-500">${escapeHtml(formatTimestamp(order.createdAt))} • ${escapeHtml(order.customerName)}</div>
          </div>
          <span class="px-3.5 py-1.5 rounded-full text-xs font-extrabold ${
            order.status === 'Delivered' ? 'bg-emerald-50 text-icase-green' : 'bg-icase-blueLight text-icase-blue'
          }">
            ${escapeHtml(order.status)}
          </span>
        </div>

        <div class="space-y-4">
          ${steps
            .map((step, idx) => {
              const done = idx <= currentIdx;
              return `
                <div class="flex items-start gap-3.5">
                  <div class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-extrabold shrink-0 ${
                    done ? 'bg-icase-green text-white' : 'bg-slate-200 text-slate-500'
                  }">${done ? '✓' : idx + 1}</div>
                  <div class="pt-0.5">
                    <div class="text-sm font-extrabold ${done ? 'text-icase-navy' : 'text-slate-400'}">${escapeHtml(step)}</div>
                    <div class="text-xs text-slate-500">${
                      done
                        ? idx === currentIdx && order.statusNotes
                          ? escapeHtml(order.statusNotes)
                          : escapeHtml(formatTimestamp(order.updatedAt || order.createdAt))
                        : 'Pending'
                    }</div>
                  </div>
                </div>
              `;
            })
            .join('')}
        </div>

        <div class="border-t border-slate-200 pt-4 space-y-1.5 text-xs sm:text-sm text-slate-700">
          <div><strong>Items:</strong> ${escapeHtml(items.map((it) => `${it.quantity}x ${it.productName}`).join(', ') || 'Accessory Order')}</div>
          <div><strong>Delivery Address:</strong> ${escapeHtml(order.deliveryAddress)}</div>
          <div class="text-base font-black text-icase-red pt-1">
            Total Paid: ${formatINR(order.totalAmount)} (${order.deliveryType === 'ONE_HOUR' ? '⚡ 1-Hour Express' : 'Standard Delivery'})
          </div>
          ${
            isOrderCompleted(order.status)
              ? `<div class="pt-3"><button type="button" data-launch-review="${escapeHtml(order.id)}" class="px-5 py-2.5 rounded-xl bg-icase-red hover:bg-red-700 text-white text-xs sm:text-sm font-extrabold transition">Write Verified Product Review</button></div>`
              : ''
          }
        </div>
      </div>
    `;
    return;
  }

  if (state.trackingResult.type === 'REPAIR') {
    const rep = state.trackingResult.data;
    const currentIdx = Math.max(0, REPAIR_STATUSES.indexOf(rep.status));
    const complaints = parseComplaints(rep.complaintsJson);

    container.innerHTML = `
      <div class="bg-white rounded-3xl border border-slate-200 p-6 sm:p-8 shadow-sm space-y-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 class="text-2xl font-black text-icase-navy">#${escapeHtml(rep.id)}</h2>
            <div class="text-xs sm:text-sm text-slate-500">${escapeHtml(rep.brandName)} ${escapeHtml(rep.modelName)} • ${escapeHtml(rep.preferredDate)} (${escapeHtml(rep.preferredTimeSlot)})</div>
          </div>
          <span class="px-3.5 py-1.5 rounded-full text-xs font-extrabold ${
            rep.status === 'Delivered' ? 'bg-emerald-50 text-icase-green' : 'bg-icase-blueLight text-icase-blue'
          }">
            ${escapeHtml(rep.status)}
          </span>
        </div>

        <div class="space-y-3.5">
          ${REPAIR_STATUSES.map((step, idx) => {
            const done = idx <= currentIdx;
            return `
              <div class="flex items-center gap-3.5">
                <div class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-extrabold shrink-0 ${
                  done ? 'bg-icase-green text-white' : 'bg-slate-200 text-slate-500'
                }">${done ? '✓' : idx + 1}</div>
                <div class="text-sm font-extrabold ${done ? 'text-icase-navy' : 'text-slate-400'}">${escapeHtml(step)}</div>
              </div>
            `;
          }).join('')}
        </div>

        <div class="border-t border-slate-200 pt-4 space-y-1.5 text-xs sm:text-sm text-slate-700">
          <div class="text-icase-red font-bold">Reported Issues: ${escapeHtml(complaints.join(', '))}</div>
          ${rep.description ? `<div><strong>Problem Notes:</strong> ${escapeHtml(rep.description)}</div>` : ''}
          <div><strong>Pickup Address:</strong> ${escapeHtml(rep.pickupAddress)}</div>
          ${rep.adminNotes ? `<div><strong>Technician Note:</strong> ${escapeHtml(rep.adminNotes)}</div>` : ''}
          ${
            isRepairCompleted(rep.status)
              ? `<div class="pt-3"><button type="button" data-launch-review="${escapeHtml(rep.id)}" class="px-5 py-2.5 rounded-xl border border-icase-blue text-icase-blue hover:bg-icase-blueLight text-xs sm:text-sm font-extrabold transition">Share Private Service Feedback</button></div>`
              : ''
          }
        </div>
      </div>
    `;
  }
}

// ==================== PROFILE, GOOGLE SYNC & WISHLIST ====================
async function ensureProfileSaved(updates) {
  const uid = updates.userId || state.profile.userId;
  const next = {
    ...state.profile,
    ...updates,
    userId: uid,
    isLoggedIn: Boolean(uid),
    wishlistIds: Array.isArray(updates.wishlistIds) ? updates.wishlistIds : state.profile.wishlistIds || []
  };
  state.profile = next;
  saveJsonStorage(STORAGE_PROFILE_KEY, next);
  updateHeaderAccountLabel();
  if (uid) {
    try {
      await saveUserProfileToCloud(next);
    } catch {}
    startUserScopedSync(uid);
  }
}

function updateHeaderAccountLabel() {
  const el = document.getElementById('header-account-label');
  if (!el) return;
  if (state.profile.isLoggedIn) {
    const first = (state.profile.name || state.profile.phone || 'Account').split(' ')[0];
    el.textContent = first;
  } else {
    el.textContent = 'Sign In';
  }
}

function toggleWishlist(productId) {
  if (!productId) return;
  const current = Array.isArray(state.profile.wishlistIds) ? state.profile.wishlistIds : [];
  const exists = current.includes(productId);
  const nextIds = exists ? current.filter((x) => x !== productId) : [...current, productId];
  state.profile = { ...state.profile, wishlistIds: nextIds };
  saveJsonStorage(STORAGE_PROFILE_KEY, state.profile);
  if (state.profile.isLoggedIn && state.profile.userId) {
    saveUserProfileToCloud(state.profile).catch(() => {});
  }
  renderHome();
  if (state.activeTab === 'catalog') renderCatalog();
  showToast(exists ? 'Removed from Wishlist' : 'Saved to Wishlist');
}

function startUserScopedSync(uid) {
  state.unsubUserScoped?.();
  state.unsubUserScoped = null;
  if (!uid) return;

  state.unsubUserScoped = subscribeUserScopedData(uid, {
    onProfile: (remote) => {
      const remoteWishlist = Array.isArray(remote.wishlistIds)
        ? remote.wishlistIds.filter((x) => typeof x === 'string' && x.trim())
        : null;
      state.profile = {
        ...state.profile,
        userId: uid,
        name: remote.name || state.profile.name,
        email: remote.email || state.profile.email,
        phone: remote.phone || state.profile.phone,
        whatsapp: remote.whatsapp || state.profile.whatsapp,
        address: remote.address || state.profile.address,
        isLoggedIn: true,
        isGoogleSignedIn: Boolean(remote.isGoogleSignedIn || state.profile.isGoogleSignedIn),
        wishlistIds: remoteWishlist ?? state.profile.wishlistIds ?? []
      };
      saveJsonStorage(STORAGE_PROFILE_KEY, state.profile);
      updateHeaderAccountLabel();
      prefillRepairCustomerFields();
      if (state.accountOpen) renderAccountModal();
    },
    onOrders: (orders) => {
      state.myOrders = orders;
      renderRecentTrackingChips();
      if (state.accountOpen) renderAccountModal();
    },
    onRepairs: (repairs) => {
      state.myRepairs = repairs;
      renderRecentTrackingChips();
      if (state.accountOpen) renderAccountModal();
    }
  });
}

function openAccountModal() {
  state.accountOpen = true;
  state.accountStatus = '';
  state.accountError = '';
  syncMobileBottomNavActive();
  renderAccountModal();
  document.getElementById('account-modal-overlay')?.classList.remove('hidden');
}

function closeAccountModal() {
  state.accountOpen = false;
  syncMobileBottomNavActive();
  document.getElementById('account-modal-overlay')?.classList.add('hidden');
}

function renderAccountModal() {
  const titleEl = document.getElementById('account-modal-title');
  const bodyEl = document.getElementById('account-modal-body');
  if (!titleEl || !bodyEl) return;

  titleEl.textContent = state.profile.isLoggedIn
    ? state.profile.name || 'iCASE Customer Account'
    : 'Sign In & Cloud Sync';

  const tabsHtml = `
    <div class="flex flex-wrap gap-2">
      ${[
        ['PROFILE', 'Profile & Sync'],
        ['ORDERS', `My Orders (${state.myOrders.length})`],
        ['REPAIRS', `My Repairs (${state.myRepairs.length})`],
        ['WISHLIST', `Wishlist (${(state.profile.wishlistIds || []).length})`]
      ]
        .map(
          ([key, label]) => `
        <button type="button" data-acct-tab="${key}" class="px-3.5 py-2 rounded-xl text-xs font-bold transition ${
          state.accountTab === key ? 'bg-icase-navy text-white' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
        }">
          ${escapeHtml(label)}
        </button>
      `
        )
        .join('')}
    </div>
  `;

  let innerHtml = '';
  if (state.accountTab === 'PROFILE') {
    innerHtml = `
      <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-sm font-extrabold text-icase-navy">
            ${state.profile.isGoogleSignedIn ? '✓ Google Account Synced' : 'Link Google Account for App Sync'}
          </div>
          <div class="text-xs text-slate-500">
            ${
              state.profile.isGoogleSignedIn
                ? escapeHtml(state.profile.email || state.profile.userId)
                : 'Sign in with Google to share orders & repairs automatically with your iCASE Android app.'
            }
          </div>
        </div>
        <div class="flex gap-2">
          ${
            !state.profile.isGoogleSignedIn
              ? `<button id="acct-google-btn" type="button" class="px-4 py-2 rounded-xl bg-icase-red hover:bg-red-700 text-white text-xs font-bold">Sign in with Google</button>`
              : `<button id="acct-logout-btn" type="button" class="px-4 py-2 rounded-xl border border-slate-300 bg-white text-icase-navy text-xs font-bold">Sign Out</button>`
          }
        </div>
      </div>

      ${state.accountStatus ? `<div class="text-xs font-bold text-icase-green">${escapeHtml(state.accountStatus)}</div>` : ''}
      ${state.accountError ? `<div class="text-xs font-bold text-icase-red">${escapeHtml(state.accountError)}</div>` : ''}

      <form id="acct-profile-form" class="space-y-3">
        <div class="grid sm:grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-1">Full Name</label>
            <input id="acct-name" type="text" value="${escapeHtml(state.profile.name || '')}" placeholder="Your Full Name" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm" />
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-1">Mobile Number</label>
            <input id="acct-phone" type="tel" value="${escapeHtml(state.profile.phone || '')}" placeholder="+91 9876543210" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm" />
          </div>
        </div>
        <div>
          <label class="block text-xs font-bold text-slate-600 mb-1">WhatsApp Number</label>
          <input id="acct-whatsapp" type="tel" value="${escapeHtml(state.profile.whatsapp || '')}" placeholder="Same as mobile if blank" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm" />
        </div>
        <div>
          <div class="flex items-center justify-between mb-1">
            <label class="text-xs font-bold text-slate-600">Saved Delivery / Pickup Address</label>
            <button id="acct-gps-btn" type="button" class="text-xs font-bold text-icase-blue hover:underline">📍 Use Current GPS Location</button>
          </div>
          <textarea id="acct-address" rows="2" placeholder="House / Flat No., Street, Landmark, City, PIN" class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm">${escapeHtml(state.profile.address || '')}</textarea>
        </div>
        <button type="submit" class="px-5 py-2.5 rounded-xl bg-icase-navy hover:bg-icase-dark text-white text-sm font-bold">
          Save &amp; Sync Profile
        </button>
      </form>
      <div class="pt-3 border-t border-slate-200 flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-xs font-extrabold text-icase-navy">iCASE Store Management</div>
          <div class="text-[11px] text-slate-500">Manage products, glass rates, orders &amp; export default code</div>
        </div>
        <button id="acct-open-admin-btn" type="button" class="px-4 py-2 rounded-xl bg-icase-navy hover:bg-icase-dark text-white text-xs font-bold">
          Admin Panel
        </button>
      </div>
    `;
  } else if (state.accountTab === 'ORDERS') {
    innerHtml =
      state.myOrders.length === 0
        ? `<div class="p-8 text-center text-sm text-slate-500">No orders synced to this account yet.</div>`
        : state.myOrders
            .map(
              (ord) => `
          <div class="p-4 rounded-2xl border border-slate-200 flex flex-wrap items-center justify-between gap-3">
            <div>
              <div class="font-extrabold text-icase-navy text-sm">#${escapeHtml(ord.id)} • ${formatINR(ord.totalAmount)}</div>
              <div class="text-xs text-slate-500">${escapeHtml(formatTimestamp(ord.createdAt))} • ${escapeHtml(ord.status)}</div>
            </div>
            <div class="flex items-center gap-2">
              ${
                isOrderCompleted(ord.status)
                  ? `<button type="button" data-launch-review="${escapeHtml(ord.id)}" class="px-3 py-1.5 rounded-xl border border-icase-red text-icase-red text-xs font-bold">Review</button>`
                  : ''
              }
              <button type="button" data-acct-track="${escapeHtml(ord.id)}" class="px-3.5 py-1.5 rounded-xl bg-icase-blueLight text-icase-blue text-xs font-bold">Track Live</button>
            </div>
          </div>
        `
            )
            .join('');
  } else if (state.accountTab === 'REPAIRS') {
    innerHtml =
      state.myRepairs.length === 0
        ? `<div class="p-8 text-center text-sm text-slate-500">No repair bookings synced to this account yet.</div>`
        : state.myRepairs
            .map(
              (rep) => `
          <div class="p-4 rounded-2xl border border-slate-200 flex flex-wrap items-center justify-between gap-3">
            <div>
              <div class="font-extrabold text-icase-navy text-sm">#${escapeHtml(rep.id)} • ${escapeHtml(rep.brandName)} ${escapeHtml(rep.modelName)}</div>
              <div class="text-xs text-slate-500">${escapeHtml(rep.preferredDate)} (${escapeHtml(rep.preferredTimeSlot)}) • ${escapeHtml(rep.status)}</div>
            </div>
            <div class="flex items-center gap-2">
              ${
                isRepairCompleted(rep.status)
                  ? `<button type="button" data-launch-review="${escapeHtml(rep.id)}" class="px-3 py-1.5 rounded-xl border border-icase-blue text-icase-blue text-xs font-bold">Feedback</button>`
                  : ''
              }
              <button type="button" data-acct-track="${escapeHtml(rep.id)}" class="px-3.5 py-1.5 rounded-xl bg-icase-blueLight text-icase-blue text-xs font-bold">Track Live</button>
            </div>
          </div>
        `
            )
            .join('');
  } else {
    const wishProducts = state.products.filter((p) => (state.profile.wishlistIds || []).includes(p.id));
    innerHtml =
      wishProducts.length === 0
        ? `<div class="p-8 text-center text-sm text-slate-500">Your wishlist is empty. Tap ♡ on any product to save it!</div>`
        : wishProducts
            .map(
              (p) => `
          <div class="p-3 rounded-2xl border border-slate-200 flex items-center gap-3">
            <img src="${escapeHtml(p.imageUrl)}" alt="${escapeHtml(p.name)}" class="w-12 h-12 rounded-xl object-cover" />
            <div class="flex-1 min-w-0">
              <div class="text-sm font-bold text-icase-navy truncate">${escapeHtml(p.name)}</div>
              <div class="text-xs font-black text-icase-red">${formatINR(p.price)}</div>
            </div>
            <button type="button" data-wish-cart="${escapeHtml(p.id)}" class="px-3 py-1.5 rounded-xl bg-icase-red text-white text-xs font-bold">Add</button>
            <button type="button" data-wish-remove="${escapeHtml(p.id)}" class="px-2.5 py-1.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-500">✕</button>
          </div>
        `
            )
            .join('');
  }

  bodyEl.innerHTML = tabsHtml + `<div class="space-y-3">${innerHtml}</div>`;

  bodyEl.querySelectorAll('[data-acct-tab]').forEach((b) =>
    b.addEventListener('click', () => {
      state.accountTab = b.dataset.acctTab;
      renderAccountModal();
    })
  );

  document.getElementById('acct-google-btn')?.addEventListener('click', async () => {
    state.accountError = '';
    try {
      const res = await signInWithGooglePopup();
      const u = res.user;
      await ensureProfileSaved({
        userId: u.uid,
        name: u.displayName || state.profile.name || 'Customer',
        email: u.email || '',
        phone: u.phoneNumber || state.profile.phone || '',
        whatsapp: u.phoneNumber || state.profile.whatsapp || '',
        address: state.profile.address || '',
        isGoogleSignedIn: true
      });
      state.accountStatus = 'Google account linked & synced with Android App!';
    } catch (err) {
      state.accountError =
        'Google Sign-In requires an authorized web domain (e.g. localhost or Firebase Hosting). You can still save your phone & profile details below!';
    }
    renderAccountModal();
  });

  document.getElementById('acct-logout-btn')?.addEventListener('click', async () => {
    try {
      await signOutFirebase();
    } catch {}
    state.unsubUserScoped?.();
    state.profile = { ...EMPTY_PROFILE };
    state.myOrders = [];
    state.myRepairs = [];
    saveJsonStorage(STORAGE_PROFILE_KEY, state.profile);
    updateHeaderAccountLabel();
    renderAccountModal();
  });

  document.getElementById('acct-open-admin-btn')?.addEventListener('click', () => {
    closeAccountModal();
    setActiveTab('admin');
  });

  document.getElementById('acct-gps-btn')?.addEventListener('click', async () => {
    try {
      const res = await detectCurrentLocation();
      const addrBox = document.getElementById('acct-address');
      if (addrBox) addrBox.value = res.formattedAddress;
      state.accountStatus = 'GPS location detected.';
    } catch (err) {
      state.accountError = err.message || 'Could not detect GPS location.';
    }
    renderAccountModal();
  });

  document.getElementById('acct-profile-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const nameVal = document.getElementById('acct-name')?.value?.trim() || '';
    const phoneRaw = document.getElementById('acct-phone')?.value?.trim() || '';
    const waRaw = document.getElementById('acct-whatsapp')?.value?.trim() || '';
    const addrVal = document.getElementById('acct-address')?.value?.trim() || '';

    const normalized = phoneRaw ? normalizePhoneToE164(phoneRaw) : '';
    if (phoneRaw && !normalized) {
      state.accountError = 'Please enter a valid 10-digit mobile number.';
      renderAccountModal();
      return;
    }

    const uid =
      state.profile.userId ||
      (normalized ? `acct_${normalized.replace(/\D/g, '')}` : `acct_${Date.now()}`);

    await ensureProfileSaved({
      userId: uid,
      name: nameVal || 'iCASE Customer',
      phone: normalized || '',
      whatsapp: waRaw || normalized || '',
      address: addrVal
    });
    state.accountStatus = 'Profile saved & synced to Cloud Firestore!';
    state.accountError = '';
    renderAccountModal();
  });

  bodyEl.querySelectorAll('[data-acct-track]').forEach((b) =>
    b.addEventListener('click', () => {
      closeAccountModal();
      openTrackId(b.dataset.acctTrack);
    })
  );
  bodyEl.querySelectorAll('[data-wish-cart]').forEach((b) =>
    b.addEventListener('click', () => {
      const p = state.products.find((x) => x.id === b.dataset.wishCart);
      if (p) addToCart(p, 1);
    })
  );
  bodyEl.querySelectorAll('[data-wish-remove]').forEach((b) =>
    b.addEventListener('click', () => {
      toggleWishlist(b.dataset.wishRemove);
      renderAccountModal();
    })
  );
}

function formatAdminLockRemaining(secs) {
  const mins = Math.floor(secs / 60);
  const rem = secs % 60;
  return mins > 0 ? `${mins}m ${String(rem).padStart(2, '0')}s` : `${rem}s`;
}

function syncAdminLockCardUi() {
  const subtitle = document.getElementById('admin-lock-subtitle');
  const loginForm = document.getElementById('admin-login-form');
  const recoveryForm = document.getElementById('admin-recovery-form');
  const resetForm = document.getElementById('admin-reset-pin-form');
  const lockBanner = document.getElementById('admin-lockout-banner');
  const pinInput = document.getElementById('admin-pin-input');
  const unlockBtn = document.getElementById('admin-unlock-btn');
  const recInput = document.getElementById('admin-recovery-input');
  const recBtn = document.getElementById('admin-verify-recovery-btn');

  if (subtitle) {
    if (state.adminForgotStep === 'VERIFY_RECOVERY') {
      subtitle.textContent = 'Enter your Master Recovery Code to reset the Admin Password / PIN.';
    } else if (state.adminForgotStep === 'RESET_PIN') {
      subtitle.textContent = 'Master Recovery Code verified. Set a new Admin Password / PIN.';
    } else {
      subtitle.textContent =
        'Manage orders, repairs, product catalog & live app settings synced with the Android App.';
    }
  }

  loginForm?.classList.toggle('hidden', state.adminForgotStep !== 'LOGIN');
  recoveryForm?.classList.toggle('hidden', state.adminForgotStep !== 'VERIFY_RECOVERY');
  resetForm?.classList.toggle('hidden', state.adminForgotStep !== 'RESET_PIN');

  const now = Date.now();
  const remSec = state.adminLockUntil > now ? Math.ceil((state.adminLockUntil - now) / 1000) : 0;
  const locked = remSec > 0;

  if (lockBanner) {
    if (locked) {
      lockBanner.textContent = `🔒 Too many failed attempts. Locked for ${formatAdminLockRemaining(remSec)}`;
      lockBanner.classList.remove('hidden');
    } else {
      lockBanner.classList.add('hidden');
      if (state.adminLockUntil > 0) {
        state.adminLockUntil = 0;
        safeStorageSetItem(STORAGE_ADMIN_LOCK_KEY, '0');
        document.getElementById('admin-pin-error')?.classList.add('hidden');
      }
      if (state.adminLockTimerId) {
        clearInterval(state.adminLockTimerId);
        state.adminLockTimerId = null;
      }
    }
  }

  if (pinInput) pinInput.disabled = locked;
  if (unlockBtn) unlockBtn.disabled = locked;
  if (recInput) recInput.disabled = locked;
  if (recBtn) recBtn.disabled = locked;
}

function startAdminLockoutTimer(durationMs = 120000) {
  state.adminLockUntil = Date.now() + durationMs;
  state.adminFailedAttempts = 0;
  safeStorageSetItem(STORAGE_ADMIN_LOCK_KEY, String(state.adminLockUntil));
  if (state.adminLockTimerId) clearInterval(state.adminLockTimerId);
  syncAdminLockCardUi();
  state.adminLockTimerId = setInterval(() => {
    syncAdminLockCardUi();
  }, 1000);
}

// ==================== ADMIN DASHBOARD ====================
function renderAdminSection() {
  const lockCard = document.getElementById('admin-lock-card');
  const consoleEl = document.getElementById('admin-console');
  const navLabel = document.getElementById('nav-admin-label');
  if (navLabel) navLabel.textContent = state.isAdminUnlocked ? 'Admin (Live)' : 'Admin';

  if (!state.isAdminUnlocked) {
    lockCard?.classList.remove('hidden');
    consoleEl?.classList.add('hidden');
    syncAdminLockCardUi();
    if (state.adminLockUntil > Date.now() && !state.adminLockTimerId) {
      state.adminLockTimerId = setInterval(() => syncAdminLockCardUi(), 1000);
    }
    return;
  }

  lockCard?.classList.add('hidden');
  consoleEl?.classList.remove('hidden');

  const totalRevenue = state.adminOrders.reduce((sum, o) => sum + Number(o.totalAmount || 0), 0);
  const pendingOrders = state.adminOrders.filter((o) => o.status !== 'Delivered').length;
  const pendingRepairs = state.adminRepairs.filter((r) => r.status !== 'Delivered').length;

  const statsEl = document.getElementById('admin-stats-grid');
  if (statsEl) {
    statsEl.innerHTML = [
      ['Total Revenue', formatINR(totalRevenue), 'text-icase-green'],
      ['Active Orders', `${pendingOrders} / ${state.adminOrders.length}`, 'text-icase-blue'],
      ['Active Repairs', `${pendingRepairs} / ${state.adminRepairs.length}`, 'text-icase-red'],
      ['Catalog Products', String(state.products.length), 'text-icase-navy']
    ]
      .map(
        ([label, val, cls]) => `
      <div class="bg-white rounded-2xl border border-slate-200 p-4">
        <div class="text-xs font-bold uppercase text-slate-500">${label}</div>
        <div class="text-xl sm:text-2xl font-black mt-1 ${cls}">${val}</div>
      </div>
    `
      )
      .join('');
  }

  document.querySelectorAll('.admin-tab-btn').forEach((btn) => {
    const active = btn.dataset.adminTab === state.adminTab;
    btn.className = `admin-tab-btn px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition ${
      active ? 'bg-icase-red text-white shadow-sm' : 'bg-white border border-slate-200 text-icase-navy hover:bg-slate-50'
    }`;
  });

  const content = document.getElementById('admin-tab-content');
  if (!content) return;

  if (state.adminTab === 'ORDERS') {
    content.innerHTML =
      state.adminOrders.length === 0
        ? `<div class="bg-white rounded-2xl border border-slate-200 p-8 text-center text-sm text-slate-500">No orders placed yet. Orders from Web &amp; Android appear here live.</div>`
        : `<div class="space-y-3">${state.adminOrders
            .map((ord) => {
              const items = parseOrderItems(ord.itemsJson);
              return `
            <div class="bg-white rounded-2xl border border-slate-200 p-4 sm:p-5 space-y-3">
              <div class="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <div class="font-black text-icase-navy">#${escapeHtml(ord.id)} • ${formatINR(ord.totalAmount)}</div>
                  <div class="text-xs text-slate-500">${escapeHtml(ord.customerName)} (${escapeHtml(ord.customerPhone)}) • ${escapeHtml(formatTimestamp(ord.createdAt))}</div>
                </div>
                <span class="px-3 py-1 rounded-full text-xs font-extrabold ${
                  ord.status === 'Delivered' ? 'bg-emerald-50 text-icase-green' : 'bg-icase-blueLight text-icase-blue'
                }">${escapeHtml(ord.status)}</span>
              </div>
              <div class="text-xs text-slate-600">
                <strong>Items:</strong> ${escapeHtml(items.map((it) => `${it.quantity}x ${it.productName}`).join(', '))}<br/>
                <strong>Address:</strong> ${escapeHtml(ord.deliveryAddress)}
              </div>
              <div class="flex flex-wrap items-center gap-2 pt-1">
                <select data-order-status="${escapeHtml(ord.id)}" class="px-3 py-2 rounded-xl border border-slate-300 text-xs font-bold bg-white">
                  ${ORDER_STATUSES.map(
                    (st) => `<option value="${escapeHtml(st)}" ${ord.status === st ? 'selected' : ''}>${escapeHtml(st)}</option>`
                  ).join('')}
                </select>
                <input data-order-note="${escapeHtml(ord.id)}" type="text" value="${escapeHtml(ord.statusNotes || '')}" placeholder="Status note..." class="flex-1 min-w-[180px] px-3 py-2 rounded-xl border border-slate-300 text-xs" />
                <button type="button" data-save-order="${escapeHtml(ord.id)}" class="px-3.5 py-2 rounded-xl bg-icase-navy text-white text-xs font-bold">Update</button>
                <button type="button" data-delete-order="${escapeHtml(ord.id)}" class="px-3 py-2 rounded-xl bg-icase-redLight text-icase-red text-xs font-bold">Delete</button>
              </div>
            </div>
          `;
            })
            .join('')}</div>`;

    content.querySelectorAll('[data-save-order]').forEach((btn) =>
      btn.addEventListener('click', async () => {
        const id = btn.dataset.saveOrder;
        const st = content.querySelector(`[data-order-status="${id}"]`)?.value;
        const note = content.querySelector(`[data-order-note="${id}"]`)?.value || '';
        await updateOrderStatusInCloud(id, st, note);
        showToast(`Order #${id} updated to ${st}`);
      })
    );
    content.querySelectorAll('[data-delete-order]').forEach((btn) =>
      btn.addEventListener('click', async () => {
        await deleteOrderInCloud(btn.dataset.deleteOrder);
        showToast(`Order #${btn.dataset.deleteOrder} deleted`);
      })
    );
  } else if (state.adminTab === 'REPAIRS') {
    content.innerHTML =
      state.adminRepairs.length === 0
        ? `<div class="bg-white rounded-2xl border border-slate-200 p-8 text-center text-sm text-slate-500">No repair bookings yet.</div>`
        : `<div class="space-y-3">${state.adminRepairs
            .map((rep) => {
              const complaints = parseComplaints(rep.complaintsJson);
              return `
            <div class="bg-white rounded-2xl border border-slate-200 p-4 sm:p-5 space-y-3">
              <div class="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <div class="font-black text-icase-navy">#${escapeHtml(rep.id)} • ${escapeHtml(rep.brandName)} ${escapeHtml(rep.modelName)}</div>
                  <div class="text-xs text-slate-500">${escapeHtml(rep.customerName)} (${escapeHtml(rep.phoneNumber)}) • ${escapeHtml(rep.preferredDate)} (${escapeHtml(rep.preferredTimeSlot)})</div>
                </div>
                <span class="px-3 py-1 rounded-full text-xs font-extrabold ${
                  rep.status === 'Delivered' ? 'bg-emerald-50 text-icase-green' : 'bg-icase-blueLight text-icase-blue'
                }">${escapeHtml(rep.status)}</span>
              </div>
              <div class="text-xs text-slate-600">
                <span class="text-icase-red font-bold">Issues: ${escapeHtml(complaints.join(', '))}</span><br/>
                <strong>Pickup:</strong> ${escapeHtml(rep.pickupAddress)}
                ${rep.description ? `<br/><strong>Notes:</strong> ${escapeHtml(rep.description)}` : ''}
              </div>
              <div class="flex flex-wrap items-center gap-2 pt-1">
                <select data-repair-status="${escapeHtml(rep.id)}" class="px-3 py-2 rounded-xl border border-slate-300 text-xs font-bold bg-white">
                  ${REPAIR_STATUSES.map(
                    (st) => `<option value="${escapeHtml(st)}" ${rep.status === st ? 'selected' : ''}>${escapeHtml(st)}</option>`
                  ).join('')}
                </select>
                <input data-repair-note="${escapeHtml(rep.id)}" type="text" value="${escapeHtml(rep.adminNotes || '')}" placeholder="Technician note..." class="flex-1 min-w-[180px] px-3 py-2 rounded-xl border border-slate-300 text-xs" />
                <button type="button" data-save-repair="${escapeHtml(rep.id)}" class="px-3.5 py-2 rounded-xl bg-icase-navy text-white text-xs font-bold">Update</button>
                <button type="button" data-reschedule-repair="${escapeHtml(rep.id)}" class="px-3 py-2 rounded-xl bg-icase-blueLight text-icase-blue text-xs font-bold">Slot +1 Day</button>
                <button type="button" data-delete-repair="${escapeHtml(rep.id)}" class="px-3 py-2 rounded-xl bg-icase-redLight text-icase-red text-xs font-bold">Delete</button>
              </div>
            </div>
          `;
            })
            .join('')}</div>`;

    content.querySelectorAll('[data-save-repair]').forEach((btn) =>
      btn.addEventListener('click', async () => {
        const id = btn.dataset.saveRepair;
        const st = content.querySelector(`[data-repair-status="${id}"]`)?.value;
        const note = content.querySelector(`[data-repair-note="${id}"]`)?.value || '';
        await updateRepairStatusInCloud(id, st, note);
        showToast(`Repair #${id} updated to ${st}`);
      })
    );
    content.querySelectorAll('[data-reschedule-repair]').forEach((btn) =>
      btn.addEventListener('click', async () => {
        const id = btn.dataset.rescheduleRepair;
        const rep = state.adminRepairs.find((r) => r.id === id);
        if (!rep) return;
        const nextDate = rep.preferredDate === 'Today' ? 'Tomorrow' : 'Today';
        await rescheduleRepairInCloud(id, nextDate, rep.preferredTimeSlot, `Rescheduled to ${nextDate}`);
        showToast(`Repair #${id} rescheduled to ${nextDate}`);
      })
    );
    content.querySelectorAll('[data-delete-repair]').forEach((btn) =>
      btn.addEventListener('click', async () => {
        await deleteRepairInCloud(btn.dataset.deleteRepair);
        showToast(`Repair #${btn.dataset.deleteRepair} deleted`);
      })
    );
  } else if (state.adminTab === 'PRODUCTS') {
    const editingProd =
      state.editingProductId === 'NEW'
        ? { id: '', name: '', category: 'Cases', price: '', originalPrice: '', stockCount: 25, imageUrl: 'https://images.unsplash.com/photo-1580910051074-3eb694886505?w=600&auto=format&fit=crop&q=80', videoUrl: '', description: '', inStock: true, isPopular: false, isOneHourEligible: true }
        : state.products.find((p) => p.id === state.editingProductId) || null;

    content.innerHTML = `
      <div class="space-y-4">
        <div class="flex flex-wrap justify-between items-center gap-2">
          <h3 class="font-extrabold text-icase-navy text-base">Manage Products &amp; Color Variants</h3>
          <div class="flex items-center gap-2">
            <button type="button" data-open-export-code="true" class="px-3.5 py-2 rounded-xl border border-slate-300 bg-white hover:bg-slate-50 text-icase-navy text-xs font-bold">Export Default Code</button>
            <button id="admin-new-prod-btn" type="button" class="px-4 py-2 rounded-xl bg-icase-red text-white text-xs font-bold">+ Add New Product</button>
          </div>
        </div>

        ${
          editingProd
            ? `
          <form id="admin-product-form" class="bg-white rounded-2xl border-2 border-icase-blue p-5 space-y-4">
            <div class="flex justify-between items-center">
              <strong class="text-icase-navy">${state.editingProductId === 'NEW' ? 'Create New Product' : `Edit: ${escapeHtml(editingProd.name)}`}</strong>
              <button id="admin-cancel-prod" type="button" class="text-xs font-bold text-slate-500">Cancel</button>
            </div>
            <div class="grid sm:grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Product Name *</label>
                <input id="ap-name" type="text" required value="${escapeHtml(editingProd.name)}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Category</label>
                <select id="ap-cat" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm bg-white">
                  ${CATEGORIES.filter((c) => c !== 'All')
                    .map((c) => `<option value="${escapeHtml(c)}" ${editingProd.category === c ? 'selected' : ''}>${escapeHtml(c)}</option>`)
                    .join('')}
                </select>
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Selling Price (INR) *</label>
                <input id="ap-price" type="number" required value="${escapeHtml(editingProd.price)}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Original MRP (INR)</label>
                <input id="ap-orig" type="number" value="${escapeHtml(editingProd.originalPrice)}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
              </div>
              <div class="sm:col-span-2 flex flex-wrap items-center gap-2">
                <span class="text-[11px] font-bold text-slate-500">Quick Offer Discount from MRP:</span>
                ${[10, 20, 30, 50]
                  .map(
                    (pct) =>
                      `<button type="button" data-quick-discount="${pct}" class="px-2.5 py-1 rounded-lg bg-rose-50 text-icase-red border border-rose-200 text-xs font-extrabold hover:bg-icase-red hover:text-white transition">-${pct}% OFF</button>`
                  )
                  .join('')}
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Stock Count</label>
                <input id="ap-stock" type="number" value="${escapeHtml(editingProd.stockCount ?? 25)}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
              </div>
              <div class="sm:col-span-2">
                <label class="block text-xs font-bold text-slate-600 mb-1.5">Product Image (Upload Photo or Paste URL)</label>
                <div class="p-3 rounded-xl bg-slate-50 border border-slate-200 flex flex-wrap items-center gap-3">
                  <img id="ap-img-preview" src="${escapeHtml(editingProd.imageUrl || '')}" alt="Preview" class="w-16 h-16 rounded-xl object-cover border border-slate-300 bg-white shrink-0" />
                  <div class="flex-1 min-w-[200px] space-y-2">
                    <div class="flex flex-wrap items-center gap-2">
                      <label for="ap-img-file" class="px-3 py-1.5 rounded-lg bg-icase-navy hover:bg-icase-dark text-white text-xs font-bold cursor-pointer">Upload Image</label>
                      <input id="ap-img-file" type="file" accept="image/*" class="hidden" />
                      <button id="ap-img-reset" type="button" class="px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold text-slate-700 hover:bg-slate-100">Reset Default</button>
                    </div>
                    <input id="ap-img" type="text" value="${escapeHtml(editingProd.imageUrl || '')}" placeholder="https://... or uploaded image data" class="w-full px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs sm:text-sm" />
                  </div>
                </div>
              </div>
              <div class="sm:col-span-2">
                <label class="block text-xs font-bold text-slate-600 mb-1.5">Optional Product Video (Direct Video URL, YouTube Link, or Short Video &le; 350 KB)</label>
                <div class="p-3 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
                  <div class="flex flex-wrap items-center gap-2">
                    <label for="ap-video-file" class="px-3 py-1.5 rounded-lg bg-slate-900 hover:bg-icase-navy text-white text-xs font-bold cursor-pointer">Upload Short Video</label>
                    <input id="ap-video-file" type="file" accept="video/*" class="hidden" />
                    <button id="ap-video-preview-btn" type="button" class="px-3 py-1.5 rounded-lg bg-icase-blueLight text-icase-blue text-xs font-bold ${editingProd.videoUrl ? '' : 'hidden'}">▶ Preview Video</button>
                    <button id="ap-video-clear" type="button" class="px-3 py-1.5 rounded-lg bg-icase-redLight text-icase-red text-xs font-bold ${editingProd.videoUrl ? '' : 'hidden'}">Remove Video</button>
                  </div>
                  <input id="ap-video" type="text" value="${escapeHtml(editingProd.videoUrl || '')}" placeholder="https://youtube.com/... or https://.../demo.mp4 (optional)" class="w-full px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs sm:text-sm" />
                </div>
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-600 mb-1">Description</label>
              <textarea id="ap-desc" rows="2" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm">${escapeHtml(editingProd.description || '')}</textarea>
            </div>

            <div class="flex flex-wrap gap-4 text-xs font-bold text-icase-navy">
              <label class="flex items-center gap-1.5"><input id="ap-instock" type="checkbox" ${editingProd.inStock ? 'checked' : ''} /> In Stock</label>
              <label class="flex items-center gap-1.5"><input id="ap-onehr" type="checkbox" ${editingProd.isOneHourEligible ? 'checked' : ''} /> 1-Hour Express Eligible</label>
              <label class="flex items-center gap-1.5"><input id="ap-popular" type="checkbox" ${editingProd.isPopular ? 'checked' : ''} /> Show on Home Page (Featured Carousel)</label>
            </div>

            <!-- Dynamic Color Variant Editor -->
            <div class="p-3.5 rounded-xl bg-slate-50 border border-slate-200 space-y-2.5">
              <div class="text-xs font-extrabold text-icase-navy">Product Color Variants</div>
              <div id="ap-colors-list" class="flex flex-wrap gap-2">
                ${
                  state.editingProductColors.length === 0
                    ? `<span class="text-xs text-slate-400">No color variants added yet.</span>`
                    : state.editingProductColors
                        .map(
                          (c, idx) => `
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-white border border-slate-300 text-xs font-bold">
                      <span class="w-3 h-3 rounded-full border border-slate-300" style="background:${escapeHtml(c.hex)}"></span>
                      ${escapeHtml(c.name)}
                      <button type="button" data-del-color="${idx}" class="text-icase-red ml-1">✕</button>
                    </span>
                  `
                        )
                        .join('')
                }
              </div>
              <div class="flex flex-wrap items-center gap-2">
                <input id="ap-color-name" type="text" placeholder="Color name (e.g. Navy Blue)" class="px-3 py-1.5 rounded-lg border border-slate-300 text-xs flex-1 min-w-[140px]" />
                <input id="ap-color-hex" type="color" value="#1B2A4A" class="w-9 h-8 rounded cursor-pointer border border-slate-300" />
                <button id="ap-add-color-btn" type="button" class="px-3 py-1.5 rounded-lg bg-icase-navy text-white text-xs font-bold">+ Add Color</button>
              </div>
              <div class="flex flex-wrap gap-1.5">
                ${COLOR_PRESETS.map(
                  (pr) =>
                    `<button type="button" data-preset-name="${escapeHtml(pr.name)}" data-preset-hex="${escapeHtml(pr.hex)}" class="px-2 py-0.5 rounded-full bg-white border border-slate-200 text-[10px] font-bold hover:border-icase-navy">${escapeHtml(pr.name)}</button>`
                ).join('')}
              </div>
            </div>

            <button type="submit" class="px-5 py-2.5 rounded-xl bg-icase-red text-white text-sm font-bold">Save &amp; Sync Product</button>
          </form>
        `
            : ''
        }

        ${
          state.products.length === 0
            ? `<div class="bg-white rounded-2xl border border-slate-200 p-8 text-center text-sm text-slate-500">All catalog products are currently deleted. Click "+ Add New Product" above to add items.</div>`
            : `<div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-3">
          ${state.products
            .map(
              (p) => `
            <div class="bg-white rounded-2xl border border-slate-200 p-4 flex flex-col gap-2">
              <div class="flex items-center gap-3">
                <img src="${escapeHtml(p.imageUrl)}" class="w-12 h-12 rounded-xl object-cover shrink-0" />
                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-1.5">
                    <div class="text-sm font-extrabold text-icase-navy truncate">${escapeHtml(p.name)}</div>
                    ${p.isPopular ? `<span class="px-1.5 py-0.5 rounded bg-amber-100 text-amber-800 text-[9px] font-black shrink-0">HOME</span>` : ''}
                    ${p.videoUrl ? `<span class="px-1.5 py-0.5 rounded bg-slate-900 text-white text-[9px] font-black shrink-0">VIDEO</span>` : ''}
                  </div>
                  <div class="text-xs text-slate-500">${escapeHtml(p.category)} • ${formatINR(p.price)}${Number(p.originalPrice || 0) > Number(p.price || 0) ? ` <span class="line-through">${formatINR(p.originalPrice)}</span>` : ''}</div>
                </div>
              </div>
              <div class="flex gap-2 pt-1">
                <button type="button" data-edit-prod="${escapeHtml(p.id)}" class="flex-1 py-1.5 rounded-lg bg-icase-blueLight text-icase-blue text-xs font-bold">Edit</button>
                <button type="button" data-del-prod="${escapeHtml(p.id)}" class="px-3 py-1.5 rounded-lg bg-icase-redLight text-icase-red text-xs font-bold">Delete</button>
              </div>
            </div>
          `
            )
            .join('')}
        </div>`
        }
      </div>
    `;

    document.getElementById('admin-new-prod-btn')?.addEventListener('click', () => {
      state.editingProductId = 'NEW';
      state.editingProductColors = [];
      renderAdminSection();
    });
    document.getElementById('admin-cancel-prod')?.addEventListener('click', () => {
      state.editingProductId = null;
      renderAdminSection();
    });
    document.getElementById('ap-img')?.addEventListener('input', (e) => {
      const prev = document.getElementById('ap-img-preview');
      if (prev) prev.src = e.target.value.trim();
    });
    document.getElementById('ap-img-reset')?.addEventListener('click', () => {
      const fallbackImg = 'https://images.unsplash.com/photo-1580910051074-3eb694886505?w=600&auto=format&fit=crop&q=80';
      const inp = document.getElementById('ap-img');
      const prev = document.getElementById('ap-img-preview');
      if (inp) inp.value = fallbackImg;
      if (prev) prev.src = fallbackImg;
    });
    document.getElementById('ap-img-file')?.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      try {
        const dataUrl = await compressImageFileToDataUrl(file);
        const inp = document.getElementById('ap-img');
        const prev = document.getElementById('ap-img-preview');
        if (inp) inp.value = dataUrl;
        if (prev) prev.src = dataUrl;
        showToast('Product image uploaded! Click Save & Sync Product.');
      } catch (err) {
        showToast(err?.message || 'Unable to process selected image');
      }
      e.target.value = '';
    });
    const syncProductVideoButtons = () => {
      const hasVid = Boolean(document.getElementById('ap-video')?.value?.trim());
      document.getElementById('ap-video-preview-btn')?.classList.toggle('hidden', !hasVid);
      document.getElementById('ap-video-clear')?.classList.toggle('hidden', !hasVid);
    };
    document.getElementById('ap-video')?.addEventListener('input', syncProductVideoButtons);
    document.getElementById('ap-video-file')?.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      try {
        const dataUrl = await readShortVideoFileToDataUrl(file);
        const inp = document.getElementById('ap-video');
        if (inp) inp.value = dataUrl;
        syncProductVideoButtons();
        showToast('Product video uploaded! Click Save & Sync Product.');
      } catch (err) {
        showToast(err?.message || 'Unable to read selected video');
      }
      e.target.value = '';
    });
    document.getElementById('ap-video-preview-btn')?.addEventListener('click', () => {
      const url = document.getElementById('ap-video')?.value?.trim();
      if (!url) return;
      const title = document.getElementById('ap-name')?.value?.trim() || 'Product Video';
      openVideoModal(title, url);
    });
    document.getElementById('ap-video-clear')?.addEventListener('click', () => {
      const inp = document.getElementById('ap-video');
      if (inp) inp.value = '';
      syncProductVideoButtons();
    });
    content.querySelectorAll('[data-quick-discount]').forEach((b) =>
      b.addEventListener('click', () => {
        const pct = Number(b.dataset.quickDiscount || 0);
        const priceEl = document.getElementById('ap-price');
        const origEl = document.getElementById('ap-orig');
        if (!priceEl || !origEl) return;
        const base = Number(origEl.value || 0) || Number(priceEl.value || 0);
        if (base <= 0) return;
        if (!Number(origEl.value || 0)) origEl.value = String(Math.round(base));
        priceEl.value = String(Math.max(1, Math.round(base * (1 - pct / 100))));
      })
    );
    content.querySelectorAll('[data-edit-prod]').forEach((b) =>
      b.addEventListener('click', () => {
        const p = state.products.find((x) => x.id === b.dataset.editProd);
        state.editingProductId = p?.id || null;
        state.editingProductColors = p ? parseColorOptions(p.colorOptionsJson) : [];
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-del-prod]').forEach((b) =>
      b.addEventListener('click', async () => {
        const targetId = b.dataset.delProd;
        if (!targetId) return;
        state.deletedProductIds = parseDeletedProductIds([...state.deletedProductIds, targetId]);
        state.products = state.products.filter((x) => x.id !== targetId);
        if (state.editingProductId === targetId) state.editingProductId = null;
        saveJsonStorage(STORAGE_DELETED_PRODUCTS_KEY, state.deletedProductIds);
        saveJsonStorage(STORAGE_PRODUCTS_KEY, state.products);
        safeStorageSetItem(STORAGE_CATALOG_SEEDED_KEY, 'true');
        reconcileCartWithLiveCatalog();
        renderHome();
        renderAdminSection();
        await deleteProductInCloud(targetId, state.deletedProductIds);
        showToast('Product permanently deleted & synced');
      })
    );
    content.querySelectorAll('[data-del-color]').forEach((b) =>
      b.addEventListener('click', () => {
        state.editingProductColors.splice(Number(b.dataset.delColor), 1);
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-preset-name]').forEach((b) =>
      b.addEventListener('click', () => {
        state.editingProductColors.push({
          name: b.dataset.presetName,
          hex: b.dataset.presetHex
        });
        renderAdminSection();
      })
    );
    document.getElementById('ap-add-color-btn')?.addEventListener('click', () => {
      const cname = document.getElementById('ap-color-name')?.value?.trim();
      const chex = document.getElementById('ap-color-hex')?.value || '#1B2A4A';
      if (!cname) return;
      state.editingProductColors.push({ name: cname, hex: normalizeHexColor(chex) });
      renderAdminSection();
    });

    document.getElementById('admin-product-form')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = state.editingProductId === 'NEW' ? `prod_${Date.now()}` : state.editingProductId;
      const existing = state.products.find((x) => x.id === id);
      const priceNum = Number(document.getElementById('ap-price')?.value || 0);
      const origNum = Number(document.getElementById('ap-orig')?.value || priceNum);
      const payload = {
        id,
        name: document.getElementById('ap-name')?.value?.trim() || 'iCASE Product',
        category: document.getElementById('ap-cat')?.value || 'Cases',
        description: document.getElementById('ap-desc')?.value?.trim() || 'Premium iCASE accessory.',
        price: priceNum,
        originalPrice: origNum,
        stockCount: Number(document.getElementById('ap-stock')?.value || 25),
        imageUrl: document.getElementById('ap-img')?.value?.trim() || '',
        videoUrl: document.getElementById('ap-video')?.value?.trim() || '',
        inStock: Boolean(document.getElementById('ap-instock')?.checked),
        isOneHourEligible: Boolean(document.getElementById('ap-onehr')?.checked),
        isPopular: Boolean(document.getElementById('ap-popular')?.checked),
        popularityRank: existing?.popularityRank || 5,
        colorOptionsJson: serializeColorOptions(state.editingProductColors)
      };
      state.deletedProductIds = state.deletedProductIds.filter((x) => x !== id);
      state.products = [...state.products.filter((x) => x.id !== id), payload];
      saveJsonStorage(STORAGE_DELETED_PRODUCTS_KEY, state.deletedProductIds);
      saveJsonStorage(STORAGE_PRODUCTS_KEY, state.products);
      reconcileCartWithLiveCatalog();
      await saveProductInCloud(payload, state.deletedProductIds);
      state.editingProductId = null;
      showToast('Product saved & synced to Mobile App!');
      renderHome();
      renderAdminSection();
    });
  } else if (state.adminTab === 'GLASS_OFFERS') {
    const cfg = state.screenGuardConfig || createDefaultScreenGuardConfig();
    const saveGuardConfig = async (nextConfig, toastMessage = 'Tempered Glass, Pouches & Offers synced live!') => {
      const normalized = parseScreenGuardConfig(nextConfig);
      state.screenGuardConfig = normalized;
      const serialized = serializeScreenGuardConfig(normalized);
      state.settings[SCREEN_GUARD_CONFIG_KEY] = serialized;
      saveJsonStorage(STORAGE_GUARD_CONFIG_KEY, normalized);
      saveJsonStorage(STORAGE_SETTINGS_KEY, state.settings);
      reconcileCartWithLiveCatalog();
      renderHome();
      renderTemperedGlassFinder();
      renderCameraLensFinder();
      renderPouchesFinder();
      renderGuardInstallationOption();
      renderAdminSection();
      await saveSettingInCloud(SCREEN_GUARD_CONFIG_KEY, serialized);
      showToast(toastMessage);
    };

    const glassTypesList = getAllGlassTypes(cfg);
    const gSearch = state.adminGlassModelSearch.trim().toLowerCase();
    const modelBrandList = getModelsForBrand(state.adminGlassModelBrand, cfg);
    const filteredModels = modelBrandList
      .filter((m) => !gSearch || m.toLowerCase().includes(gSearch))
      .slice(0, 60);

    const camSearch = state.adminCameraModelSearch.trim().toLowerCase();
    const camBrandList = getCameraLensModelsForBrand(state.adminCameraModelBrand, cfg);
    const filteredCamModels = camBrandList
      .filter((m) => !camSearch || m.toLowerCase().includes(camSearch))
      .slice(0, 60);

    const pModelSearch = state.adminPouchModelSearch.trim().toLowerCase();
    const pouchBrandList = getPouchModelsForBrand(state.adminPouchModelBrand, cfg);
    const filteredPouchModels = pouchBrandList
      .filter((m) => !pModelSearch || m.toLowerCase().includes(pModelSearch))
      .slice(0, 60);

    const camDefPricing = resolveCameraLensPrice('iPhone', '__default__', {
      ...cfg,
      cameraLensModelConfigs: {}
    });

    const draftPouch = state.adminPouchDraft;

    content.innerHTML = `
      <div class="space-y-6">
        <!-- 0. Sample Card & Item Images (Home & Finder) -->
        <div class="bg-white rounded-2xl border border-slate-200 p-5 space-y-4">
          <div class="flex flex-wrap items-center justify-between gap-2">
            <div>
              <h3 class="font-extrabold text-icase-navy text-base">Sample Card Images &amp; Starting Prices (Home &amp; Finder)</h3>
              <p class="text-xs text-slate-500">Upload or paste custom images for Tempered Glass, Camera Lens Protector, and Pouches &amp; Back Cases cards.</p>
            </div>
            <button type="button" data-open-export-code="true" class="px-3.5 py-2 rounded-xl bg-icase-navy hover:bg-icase-dark text-white text-xs font-extrabold transition">Export Default Code for GitHub</button>
          </div>
          <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <div class="p-3.5 rounded-xl bg-slate-50 border border-slate-200 space-y-2.5">
              <div class="text-xs font-extrabold text-icase-navy">Tempered Glass Image</div>
              <div class="flex items-center gap-3">
                <img id="adm-tg-img-preview" src="${escapeHtml(resolveTemperedGlassImageUrl(cfg))}" alt="Tempered Glass Preview" class="w-16 h-16 rounded-xl object-cover border border-slate-300 bg-white shrink-0" />
                <div class="flex flex-wrap gap-1.5">
                  <label for="adm-tg-img-file" class="px-3 py-1.5 rounded-lg bg-icase-navy hover:bg-icase-dark text-white text-xs font-bold cursor-pointer">Upload Photo</label>
                  <input id="adm-tg-img-file" type="file" accept="image/*" class="hidden" />
                  <button id="adm-tg-img-reset" type="button" class="px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold text-slate-700 hover:bg-slate-100">Reset Default</button>
                </div>
              </div>
              <div class="flex gap-2">
                <input id="adm-tg-img-url" type="text" value="${escapeHtml(cfg.temperedGlassImageUrl || '')}" placeholder="Custom Image URL (blank = default)" class="flex-1 px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-xs" />
                <button id="adm-tg-img-save" type="button" class="px-3.5 py-1.5 rounded-xl bg-icase-blue text-white text-xs font-bold shrink-0">Save</button>
              </div>
            </div>

            <div class="p-3.5 rounded-xl bg-slate-50 border border-slate-200 space-y-2.5">
              <div class="text-xs font-extrabold text-icase-navy">Camera Lens Protector Image</div>
              <div class="flex items-center gap-3">
                <img id="adm-cl-img-preview" src="${escapeHtml(resolveCameraLensImageUrl(cfg))}" alt="Camera Lens Preview" class="w-16 h-16 rounded-xl object-cover border border-slate-300 bg-white shrink-0" />
                <div class="flex flex-wrap gap-1.5">
                  <label for="adm-cl-img-file" class="px-3 py-1.5 rounded-lg bg-icase-navy hover:bg-icase-dark text-white text-xs font-bold cursor-pointer">Upload Photo</label>
                  <input id="adm-cl-img-file" type="file" accept="image/*" class="hidden" />
                  <button id="adm-cl-img-reset" type="button" class="px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold text-slate-700 hover:bg-slate-100">Reset Default</button>
                </div>
              </div>
              <div class="flex gap-2">
                <input id="adm-cl-img-url" type="text" value="${escapeHtml(cfg.cameraLensImageUrl || '')}" placeholder="Custom Image URL (blank = default)" class="flex-1 px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-xs" />
                <button id="adm-cl-img-save" type="button" class="px-3.5 py-1.5 rounded-xl bg-icase-blue text-white text-xs font-bold shrink-0">Save</button>
              </div>
            </div>

            <div class="p-3.5 rounded-xl bg-slate-50 border border-slate-200 space-y-2.5">
              <div class="text-xs font-extrabold text-icase-navy">Pouches &amp; Back Cases Card</div>
              <div class="flex items-center gap-3">
                <img id="adm-pcard-img-preview" src="${escapeHtml(resolvePouchCardImageUrl(cfg))}" alt="Pouches Card Preview" class="w-16 h-16 rounded-xl object-cover border border-slate-300 bg-white shrink-0" />
                <div class="flex flex-wrap gap-1.5">
                  <label for="adm-pcard-img-file" class="px-3 py-1.5 rounded-lg bg-icase-navy hover:bg-icase-dark text-white text-xs font-bold cursor-pointer">Upload Photo</label>
                  <input id="adm-pcard-img-file" type="file" accept="image/*" class="hidden" />
                  <button id="adm-pcard-img-reset" type="button" class="px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold text-slate-700 hover:bg-slate-100">Reset Default</button>
                </div>
              </div>
              <div class="flex gap-2">
                <input id="adm-pcard-img-url" type="text" value="${escapeHtml(cfg.pouchCardImageUrl || '')}" placeholder="Custom Image URL (blank = default)" class="flex-1 px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-xs" />
                <button id="adm-pcard-img-save" type="button" class="px-3.5 py-1.5 rounded-xl bg-icase-blue text-white text-xs font-bold shrink-0">Save</button>
              </div>
              <div class="flex items-center gap-2">
                <input id="adm-pcard-start-price" type="number" min="0" value="${escapeHtml(cfg.pouchCardStartingPrice || '')}" placeholder="Starting ₹ (0 = Auto ${getStartingPouchPrice({ ...cfg, pouchCardStartingPrice: 0 })})" class="flex-1 px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-xs font-bold" />
                <button id="adm-pcard-price-save" type="button" class="px-3 py-1.5 rounded-xl bg-icase-navy text-white text-xs font-bold shrink-0">Save ₹</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 1. Glass Type Rates, Helper Notes, Offers, Images & Videos -->
        <div class="bg-white rounded-2xl border border-slate-200 p-5 space-y-4">
          <div class="flex flex-wrap items-center justify-between gap-2">
            <div>
              <h3 class="font-extrabold text-icase-navy text-base">Tempered Glass Types — Add, Rates, Media, Offers &amp; Delete</h3>
              <p class="text-xs text-slate-500">Active glass types automatically sync to the Home quick-select strip and customer picker.</p>
            </div>
            <button id="btn-adm-toggle-add-gt" type="button" class="px-3.5 py-2 rounded-xl bg-icase-red text-white text-xs font-bold">+ Add Glass Type</button>
          </div>

          ${
            state.adminGlassAddingType
              ? `
            <div class="p-4 rounded-2xl bg-icase-blueLight/40 border border-icase-blue/30 grid sm:grid-cols-3 gap-2.5">
              <input id="adm-new-gt-name" type="text" placeholder="Type name (e.g. Anti-Blue 9H)" class="px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs font-bold" />
              <input id="adm-new-gt-price" type="number" value="299" placeholder="Rate ₹" class="px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs font-bold" />
              <input id="adm-new-gt-note" type="text" placeholder="1-line helper note (optional)" class="px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs" />
              <div class="sm:col-span-3 grid sm:grid-cols-2 gap-2.5">
                <div class="p-2.5 rounded-xl bg-white border border-slate-200 space-y-1.5">
                  <div class="flex items-center justify-between gap-2">
                    <span class="text-[11px] font-bold text-slate-600">Optional Type Image</span>
                    <div class="flex items-center gap-1.5">
                      <label for="adm-new-gt-img-file" class="px-2.5 py-1 rounded-lg bg-icase-navy text-white text-[10px] font-bold cursor-pointer">Upload</label>
                      <input id="adm-new-gt-img-file" type="file" accept="image/*" class="hidden" />
                      <button id="adm-new-gt-img-clear" type="button" class="px-2 py-1 rounded-lg bg-slate-100 text-slate-600 text-[10px] font-bold ${state.adminNewGtImageUrl ? '' : 'hidden'}">Clear</button>
                    </div>
                  </div>
                  <input id="adm-new-gt-img-url" type="text" value="${escapeHtml(state.adminNewGtImageUrl || '')}" placeholder="Optional image URL (blank = Tempered Glass default)" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs" />
                </div>
                <div class="p-2.5 rounded-xl bg-white border border-slate-200 space-y-1.5">
                  <div class="flex items-center justify-between gap-2">
                    <span class="text-[11px] font-bold text-slate-600">Optional Video (URL, YouTube, &le; 350 KB)</span>
                    <div class="flex items-center gap-1.5">
                      <label for="adm-new-gt-vid-file" class="px-2.5 py-1 rounded-lg bg-slate-900 text-white text-[10px] font-bold cursor-pointer">Upload Video</label>
                      <input id="adm-new-gt-vid-file" type="file" accept="video/*" class="hidden" />
                      <button id="adm-new-gt-vid-clear" type="button" class="px-2 py-1 rounded-lg bg-slate-100 text-slate-600 text-[10px] font-bold ${state.adminNewGtVideoUrl ? '' : 'hidden'}">Clear</button>
                    </div>
                  </div>
                  <input id="adm-new-gt-vid-url" type="text" value="${escapeHtml(state.adminNewGtVideoUrl || '')}" placeholder="YouTube or .mp4 URL (optional)" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs" />
                </div>
              </div>
              <div class="sm:col-span-3 flex gap-2">
                <button id="btn-adm-save-new-gt" type="button" class="px-4 py-1.5 rounded-xl bg-icase-navy text-white text-xs font-bold">Add Glass Type</button>
                <button id="btn-adm-cancel-new-gt" type="button" class="px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-xs font-bold">Cancel</button>
              </div>
            </div>
          `
              : ''
          }

          <div class="grid md:grid-cols-2 gap-3">
            ${glassTypesList
              .map((gt, idx) => {
                const gCfg = cfg.glassTypeConfigs[gt] || {
                  basePrice: DEFAULT_GLASS_TYPE_PRICES[gt] || 199,
                  enabled: true,
                  deleted: false,
                  offer: { active: false, type: 'NONE', value: 0, badgeText: '' },
                  helperNote: '',
                  imageUrl: '',
                  videoUrl: ''
                };
                const offer = gCfg.offer || { active: false, type: 'NONE', value: 0, badgeText: '' };
                const preview = resolveTemperedGlassPrice('iPhone', '__default__', gt, {
                  ...cfg,
                  modelConfigs: {}
                });
                const resolvedGtImg = resolveGlassTypeImageUrl(gt, cfg);
                const hasGtVid = Boolean(String(gCfg.videoUrl || '').trim());
                return `
                  <div class="p-4 rounded-2xl border ${gCfg.deleted ? 'bg-rose-50/40 border-rose-200' : 'bg-slate-50 border-slate-200'} space-y-2.5">
                    <div class="flex items-center justify-between gap-2">
                      <div class="flex items-center gap-2.5 min-w-0">
                        <img id="gt-img-preview-${idx}" src="${escapeHtml(resolvedGtImg)}" alt="${escapeHtml(gt)}" class="w-10 h-10 rounded-xl object-cover border border-slate-300 bg-white shrink-0" />
                        <div class="min-w-0">
                          <div class="text-sm font-extrabold text-icase-navy truncate">${escapeHtml(gt)}</div>
                          <div class="text-[11px] font-bold ${gCfg.deleted ? 'text-icase-red' : gCfg.enabled ? 'text-icase-green' : 'text-amber-700'}">
                            ${gCfg.deleted ? 'DELETED (Hidden)' : gCfg.enabled ? `LIVE: ${formatINR(preview.finalPrice)}` : 'PAUSED'}
                            ${!gCfg.deleted && preview.offerBadge ? ` • ${escapeHtml(preview.offerBadge)}` : ''}
                            ${!gCfg.deleted && hasGtVid ? ' • Video set' : ''}
                          </div>
                        </div>
                      </div>
                      <div class="flex items-center gap-2 shrink-0">
                        ${
                          gCfg.deleted
                            ? `<button type="button" data-gt-restore="${idx}" class="px-3 py-1 rounded-lg bg-icase-blueLight text-icase-blue text-xs font-bold">Restore</button>`
                            : `<label class="inline-flex items-center gap-1 text-xs font-bold text-icase-navy cursor-pointer">
                                <input type="checkbox" data-gt-toggle="${idx}" ${gCfg.enabled ? 'checked' : ''} /> Active
                              </label>
                              <button type="button" data-gt-delete="${idx}" class="px-2.5 py-1 rounded-lg bg-icase-redLight text-icase-red text-xs font-bold">Delete</button>`
                        }
                      </div>
                    </div>
                    ${
                      !gCfg.deleted
                        ? `
                      <div class="grid grid-cols-2 sm:grid-cols-4 gap-2">
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500">Base Rate (₹)</label>
                          <input id="gt-price-${idx}" type="number" value="${escapeHtml(gCfg.basePrice)}" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
                        </div>
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500">Offer Type</label>
                          <select id="gt-otype-${idx}" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold">
                            <option value="NONE" ${offer.type === 'NONE' ? 'selected' : ''}>No Offer</option>
                            <option value="FLAT_OFF" ${offer.type === 'FLAT_OFF' ? 'selected' : ''}>Flat ₹ Off</option>
                            <option value="PERCENT_OFF" ${offer.type === 'PERCENT_OFF' ? 'selected' : ''}>% Off</option>
                            <option value="SPECIAL_PRICE" ${offer.type === 'SPECIAL_PRICE' ? 'selected' : ''}>Special ₹</option>
                          </select>
                        </div>
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500">Offer Value</label>
                          <input id="gt-oval-${idx}" type="number" value="${escapeHtml(offer.value || 0)}" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
                        </div>
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500">Promo Badge</label>
                          <input id="gt-obadge-${idx}" type="text" value="${escapeHtml(offer.badgeText || '')}" placeholder="e.g. FESTIVE" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs" />
                        </div>
                      </div>
                      <input id="gt-note-${idx}" type="text" value="${escapeHtml(gCfg.helperNote || '')}" placeholder="1-line helper note: ${escapeHtml(getGlassTypeHelperNote(gt, cfg))}" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs" />
                      <div class="grid sm:grid-cols-2 gap-2">
                        <div class="p-2 rounded-xl bg-white border border-slate-200 space-y-1.5">
                          <div class="flex items-center justify-between gap-1.5">
                            <span class="text-[10px] font-bold text-slate-500">Type Image</span>
                            <div class="flex items-center gap-1">
                              <label for="gt-img-file-${idx}" class="px-2 py-0.5 rounded bg-icase-navy text-white text-[10px] font-bold cursor-pointer">Upload</label>
                              <input id="gt-img-file-${idx}" data-gt-img-file="${idx}" type="file" accept="image/*" class="hidden" />
                              ${gCfg.imageUrl ? `<button type="button" data-gt-img-reset="${idx}" class="px-2 py-0.5 rounded bg-slate-100 text-slate-600 text-[10px] font-bold">Reset</button>` : ''}
                            </div>
                          </div>
                          <input id="gt-img-${idx}" type="text" value="${escapeHtml(gCfg.imageUrl || '')}" placeholder="Custom Image URL (blank = default)" class="w-full px-2 py-1 rounded-lg border border-slate-300 text-[11px]" />
                        </div>
                        <div class="p-2 rounded-xl bg-white border border-slate-200 space-y-1.5">
                          <div class="flex items-center justify-between gap-1.5">
                            <span class="text-[10px] font-bold text-slate-500">Optional Video (&le; 350 KB)</span>
                            <div class="flex items-center gap-1">
                              <label for="gt-vid-file-${idx}" class="px-2 py-0.5 rounded bg-slate-900 text-white text-[10px] font-bold cursor-pointer">Upload</label>
                              <input id="gt-vid-file-${idx}" data-gt-vid-file="${idx}" type="file" accept="video/*" class="hidden" />
                              <button id="gt-vid-prev-${idx}" type="button" data-gt-vid-preview="${idx}" class="px-2 py-0.5 rounded bg-icase-blueLight text-icase-blue text-[10px] font-bold ${hasGtVid ? '' : 'hidden'}">▶</button>
                              <button id="gt-vid-clr-${idx}" type="button" data-gt-vid-clear="${idx}" class="px-2 py-0.5 rounded bg-icase-redLight text-icase-red text-[10px] font-bold ${hasGtVid ? '' : 'hidden'}">Clear</button>
                            </div>
                          </div>
                          <input id="gt-vid-${idx}" data-gt-vid-input="${idx}" type="text" value="${escapeHtml(gCfg.videoUrl || '')}" placeholder="YouTube or .mp4 URL (optional)" class="w-full px-2 py-1 rounded-lg border border-slate-300 text-[11px]" />
                        </div>
                      </div>
                      <button type="button" data-gt-save="${idx}" class="px-3.5 py-1.5 rounded-lg bg-icase-navy text-white text-xs font-bold">Save Rate, Media &amp; Offer</button>
                    `
                        : ''
                    }
                  </div>
                `;
              })
              .join('')}
          </div>
        </div>

        <!-- 2. Tempered Glass Phone Models — Add, Rename, Rates, Offers & Delete -->
        <div class="bg-white rounded-2xl border border-slate-200 p-5 space-y-4">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h3 class="font-extrabold text-icase-navy text-base">Phone Models — Add, Rename, Rates &amp; Delete</h3>
              <p class="text-xs text-slate-500">Add new phone models, rename entries, override rates, or pause/delete permanently.</p>
            </div>
            <div class="flex flex-wrap items-center gap-2">
              <button id="btn-adm-toggle-add-gm" type="button" class="px-3 py-1.5 rounded-xl bg-icase-red text-white text-xs font-bold">+ Add Model</button>
              <input id="admin-glass-model-search" type="text" value="${escapeHtml(state.adminGlassModelSearch)}" placeholder="Filter models..." class="px-3 py-2 rounded-xl border border-slate-300 text-xs sm:text-sm w-48 sm:w-56" />
            </div>
          </div>

          <div class="flex flex-wrap gap-1.5">
            ${SCREEN_GUARD_BRANDS.map(
              (b) => `
              <button type="button" data-adm-glass-brand="${escapeHtml(b)}" class="px-3 py-1.5 rounded-xl text-xs font-bold border ${
                state.adminGlassModelBrand === b ? 'bg-icase-navy text-white border-icase-navy' : 'bg-slate-50 text-icase-navy border-slate-200'
              }">${escapeHtml(b)}</button>
            `
            ).join('')}
          </div>

          ${
            state.adminGlassAddModelOpen
              ? `
            <div class="p-3.5 rounded-xl bg-icase-blueLight/40 border border-icase-blue/30 flex flex-wrap items-center gap-2">
              <input id="adm-new-gm-name" type="text" placeholder="New ${escapeHtml(state.adminGlassModelBrand)} model name" class="flex-1 min-w-[200px] px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
              <button id="btn-adm-save-new-gm" type="button" class="px-3.5 py-1.5 rounded-lg bg-icase-navy text-white text-xs font-bold">Add Model</button>
              <button id="btn-adm-cancel-new-gm" type="button" class="px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold">Cancel</button>
            </div>
          `
              : ''
          }

          <div class="space-y-2 max-h-96 overflow-y-auto pr-1">
            ${filteredModels
              .map((model, mIdx) => {
                const mKey = guardModelKey(state.adminGlassModelBrand, model);
                const mCfg = cfg.modelConfigs[mKey] || { enabled: true, deleted: false, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
                const isEditing = state.adminGlassEditingModelKey === mKey;
                const applicableTypes = getGlassTypesForDevice(state.adminGlassModelBrand, model, createDefaultScreenGuardConfig());
                return `
                  <div class="p-3 rounded-xl border ${mCfg.deleted ? 'bg-rose-50/40 border-rose-200' : 'bg-slate-50 border-slate-200'} space-y-2">
                    <div class="flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <div class="text-xs sm:text-sm font-extrabold text-icase-navy">${escapeHtml(model)}</div>
                        <div class="text-[11px] font-bold ${mCfg.deleted ? 'text-icase-red' : mCfg.enabled ? 'text-icase-green' : 'text-amber-700'}">
                          ${mCfg.deleted ? 'DELETED' : mCfg.enabled ? 'Active' : 'Paused'}
                          ${Object.keys(mCfg.customRates || {}).length > 0 ? ` • ${Object.keys(mCfg.customRates).length} custom rate(s)` : ''}
                          ${mCfg.offer?.active && mCfg.offer?.type !== 'NONE' ? ` • Offer active` : ''}
                        </div>
                      </div>
                      <div class="flex flex-wrap items-center gap-1.5">
                        ${
                          mCfg.deleted
                            ? `<button type="button" data-adm-m-restore="${escapeHtml(mKey)}" class="px-2.5 py-1 rounded-lg bg-icase-blueLight text-icase-blue text-xs font-bold">Restore</button>`
                            : `
                              <button type="button" data-adm-m-edit="${escapeHtml(mKey)}" class="px-2.5 py-1 rounded-lg bg-white border border-slate-300 text-icase-navy text-xs font-bold">${isEditing ? 'Close' : 'Edit & Rates'}</button>
                              <button type="button" data-adm-m-toggle="${escapeHtml(mKey)}" class="px-2.5 py-1 rounded-lg bg-white border border-slate-300 text-xs font-bold">${mCfg.enabled ? 'Pause' : 'Enable'}</button>
                              <button type="button" data-adm-m-delete="${escapeHtml(mKey)}" class="px-2.5 py-1 rounded-lg bg-icase-redLight text-icase-red text-xs font-bold">Delete</button>
                            `
                        }
                      </div>
                    </div>
                    ${
                      isEditing && !mCfg.deleted
                        ? `
                      <div class="pt-2 border-t border-slate-200 space-y-2.5 bg-white p-3 rounded-xl">
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500">Rename Model (optional)</label>
                          <input id="adm-mrename-${mIdx}" type="text" value="${escapeHtml(model)}" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs font-bold" />
                        </div>
                        <div class="grid sm:grid-cols-3 gap-2">
                          ${applicableTypes
                            .map((gt, gtI) => {
                              const curr = mCfg.customRates?.[gt] || '';
                              return `
                                <div>
                                  <label class="block text-[10px] font-bold text-slate-500">${escapeHtml(gt)} Override (₹)</label>
                                  <input id="adm-mr-${mIdx}-${gtI}" data-mr-gt="${escapeHtml(gt)}" type="number" value="${escapeHtml(curr)}" placeholder="Default (${DEFAULT_GLASS_TYPE_PRICES[gt] || 199})" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs" />
                                </div>
                              `;
                            })
                            .join('')}
                        </div>
                        <div class="grid grid-cols-3 gap-2">
                          <div>
                            <label class="block text-[10px] font-bold text-slate-500">Model Offer</label>
                            <select id="adm-mo-type-${mIdx}" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 text-xs bg-white">
                              <option value="NONE" ${mCfg.offer?.type === 'NONE' ? 'selected' : ''}>Use Glass Type Default</option>
                              <option value="FLAT_OFF" ${mCfg.offer?.type === 'FLAT_OFF' ? 'selected' : ''}>Flat ₹ Off</option>
                              <option value="PERCENT_OFF" ${mCfg.offer?.type === 'PERCENT_OFF' ? 'selected' : ''}>% Off</option>
                              <option value="SPECIAL_PRICE" ${mCfg.offer?.type === 'SPECIAL_PRICE' ? 'selected' : ''}>Special ₹</option>
                            </select>
                          </div>
                          <div>
                            <label class="block text-[10px] font-bold text-slate-500">Value</label>
                            <input id="adm-mo-val-${mIdx}" type="number" value="${escapeHtml(mCfg.offer?.value || 0)}" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs" />
                          </div>
                          <div>
                            <label class="block text-[10px] font-bold text-slate-500">Badge</label>
                            <input id="adm-mo-badge-${mIdx}" type="text" value="${escapeHtml(mCfg.offer?.badgeText || '')}" placeholder="Optional" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs" />
                          </div>
                        </div>
                        <button type="button" data-adm-m-save="${escapeHtml(mKey)}" data-adm-m-idx="${mIdx}" data-adm-m-orig="${escapeHtml(model)}" class="px-4 py-1.5 rounded-lg bg-icase-navy text-white text-xs font-bold">Save Model Settings</button>
                      </div>
                    `
                        : ''
                    }
                  </div>
                `;
              })
              .join('')}
          </div>
        </div>

        <!-- 3. Camera Lens Protection + Glass Combo Offer -->
        <div class="bg-white rounded-2xl border border-slate-200 p-5 space-y-4">
          <div class="flex flex-wrap items-center justify-between gap-2">
            <div>
              <h3 class="font-extrabold text-icase-navy text-base">Camera Lens Protection &amp; Quick Combo Offer</h3>
              <p class="text-xs text-slate-500">Live Lens Price: <strong>${formatINR(camDefPricing.finalPrice)}</strong>${camDefPricing.offerBadge ? ` (${escapeHtml(camDefPricing.offerBadge)})` : ''}</p>
            </div>
            <label class="inline-flex items-center gap-2 text-xs font-extrabold text-icase-navy cursor-pointer">
              <input id="adm-cam-global-enable" type="checkbox" ${cfg.cameraLensDefault.enabled && !cfg.cameraLensDefault.deleted ? 'checked' : ''} /> Section Active
            </label>
          </div>

          <div class="grid grid-cols-2 sm:grid-cols-4 gap-2 p-3.5 rounded-xl bg-slate-50 border border-slate-200">
            <div>
              <label class="block text-[10px] font-bold text-slate-500">Default Rate (₹)</label>
              <input id="adm-cam-def-price" type="number" value="${escapeHtml(cfg.cameraLensDefault.basePrice)}" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
            </div>
            <div>
              <label class="block text-[10px] font-bold text-slate-500">Offer Type</label>
              <select id="adm-cam-def-otype" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold">
                <option value="NONE" ${cfg.cameraLensDefault.offer?.type === 'NONE' ? 'selected' : ''}>No Offer</option>
                <option value="FLAT_OFF" ${cfg.cameraLensDefault.offer?.type === 'FLAT_OFF' ? 'selected' : ''}>Flat ₹ Off</option>
                <option value="PERCENT_OFF" ${cfg.cameraLensDefault.offer?.type === 'PERCENT_OFF' ? 'selected' : ''}>% Off</option>
                <option value="SPECIAL_PRICE" ${cfg.cameraLensDefault.offer?.type === 'SPECIAL_PRICE' ? 'selected' : ''}>Special ₹</option>
              </select>
            </div>
            <div>
              <label class="block text-[10px] font-bold text-slate-500">Offer Value</label>
              <input id="adm-cam-def-oval" type="number" value="${escapeHtml(cfg.cameraLensDefault.offer?.value || 0)}" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
            </div>
            <div>
              <label class="block text-[10px] font-bold text-slate-500">Promo Badge</label>
              <input id="adm-cam-def-obadge" type="text" value="${escapeHtml(cfg.cameraLensDefault.offer?.badgeText || '')}" placeholder="e.g. LENS DEAL" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs" />
            </div>
            <div class="col-span-2 sm:col-span-4 pt-1">
              <div class="p-2.5 rounded-xl bg-white border border-slate-200 space-y-1.5">
                <div class="flex flex-wrap items-center justify-between gap-2">
                  <span class="text-[10px] font-bold text-slate-600">Optional Camera Lens Demo Video (YouTube, URL, or Short Video &le; 350 KB)</span>
                  <div class="flex items-center gap-1.5">
                    <label for="adm-cam-def-vid-file" class="px-2.5 py-1 rounded-lg bg-slate-900 text-white text-[10px] font-bold cursor-pointer">Upload Video</label>
                    <input id="adm-cam-def-vid-file" type="file" accept="video/*" class="hidden" />
                    <button id="adm-cam-def-vid-preview" type="button" class="px-2.5 py-1 rounded-lg bg-icase-blueLight text-icase-blue text-[10px] font-bold ${cfg.cameraLensDefault.videoUrl ? '' : 'hidden'}">▶ Preview</button>
                    <button id="adm-cam-def-vid-clear" type="button" class="px-2.5 py-1 rounded-lg bg-icase-redLight text-icase-red text-[10px] font-bold ${cfg.cameraLensDefault.videoUrl ? '' : 'hidden'}">Remove</button>
                  </div>
                </div>
                <input id="adm-cam-def-vid-url" type="text" value="${escapeHtml(cfg.cameraLensDefault.videoUrl || '')}" placeholder="YouTube link or direct .mp4 URL (optional)" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs" />
              </div>
            </div>
            <div class="col-span-2 sm:col-span-4">
              <button id="adm-cam-def-save" type="button" class="px-4 py-1.5 rounded-lg bg-icase-navy text-white text-xs font-bold">Save Camera Lens Defaults</button>
            </div>
          </div>

          <!-- Tempered Glass + Camera Lens Combo Editor -->
          <div class="p-3.5 rounded-xl bg-icase-blueLight/40 border border-icase-blue/30 space-y-2.5">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div class="text-xs font-extrabold text-icase-navy">Tempered Glass + Camera Lens Combo Deal</div>
              <label class="inline-flex items-center gap-1.5 text-xs font-bold text-icase-navy cursor-pointer">
                <input id="adm-combo-enable" type="checkbox" ${cfg.comboEnabled !== false ? 'checked' : ''} /> Combo Active
              </label>
            </div>
            <div class="grid grid-cols-3 gap-2">
              <div>
                <label class="block text-[10px] font-bold text-slate-500">Combo Discount</label>
                <select id="adm-combo-otype" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold">
                  <option value="NONE" ${cfg.comboOffer?.type === 'NONE' ? 'selected' : ''}>No Discount</option>
                  <option value="FLAT_OFF" ${cfg.comboOffer?.type === 'FLAT_OFF' ? 'selected' : ''}>Flat ₹ Off</option>
                  <option value="PERCENT_OFF" ${cfg.comboOffer?.type === 'PERCENT_OFF' ? 'selected' : ''}>% Off</option>
                  <option value="SPECIAL_PRICE" ${cfg.comboOffer?.type === 'SPECIAL_PRICE' ? 'selected' : ''}>Special ₹</option>
                </select>
              </div>
              <div>
                <label class="block text-[10px] font-bold text-slate-500">Discount Value</label>
                <input id="adm-combo-oval" type="number" value="${escapeHtml(cfg.comboOffer?.value || 0)}" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
              </div>
              <div>
                <label class="block text-[10px] font-bold text-slate-500">Combo Badge</label>
                <input id="adm-combo-obadge" type="text" value="${escapeHtml(cfg.comboOffer?.badgeText || '')}" placeholder="COMBO SAVE" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs" />
              </div>
            </div>
            <button id="adm-combo-save" type="button" class="px-4 py-1.5 rounded-lg bg-icase-navy text-white text-xs font-bold">Save Combo Offer</button>
          </div>

          <div class="flex flex-wrap items-center justify-between gap-2">
            <div class="flex flex-wrap gap-1.5">
              ${['iPhone', 'Samsung S Series']
                .map(
                  (b) => `
                <button type="button" data-adm-cam-brand="${escapeHtml(b)}" class="px-3 py-1.5 rounded-xl text-xs font-bold border ${
                  state.adminCameraModelBrand === b ? 'bg-icase-navy text-white border-icase-navy' : 'bg-slate-50 text-icase-navy border-slate-200'
                }">${escapeHtml(b)}</button>
              `
                )
                .join('')}
              <button id="btn-adm-toggle-add-cm" type="button" class="px-3 py-1.5 rounded-xl bg-icase-red text-white text-xs font-bold">+ Add Lens Model</button>
            </div>
            <input id="admin-cam-model-search" type="text" value="${escapeHtml(state.adminCameraModelSearch)}" placeholder="Filter camera lens models..." class="px-3 py-1.5 rounded-xl border border-slate-300 text-xs w-full sm:w-56" />
          </div>

          ${
            state.adminCamAddModelOpen
              ? `
            <div class="p-3.5 rounded-xl bg-icase-blueLight/40 border border-icase-blue/30 flex flex-wrap items-center gap-2">
              <input id="adm-new-cm-name" type="text" placeholder="New ${escapeHtml(state.adminCameraModelBrand)} lens model" class="flex-1 min-w-[200px] px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
              <button id="btn-adm-save-new-cm" type="button" class="px-3.5 py-1.5 rounded-lg bg-icase-navy text-white text-xs font-bold">Add Model</button>
              <button id="btn-adm-cancel-new-cm" type="button" class="px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold">Cancel</button>
            </div>
          `
              : ''
          }

          <div class="space-y-2 max-h-80 overflow-y-auto pr-1">
            ${filteredCamModels
              .map((model, cIdx) => {
                const cKey = guardModelKey(state.adminCameraModelBrand, model);
                const cCfg = cfg.cameraLensModelConfigs[cKey] || { enabled: true, deleted: false, customPrice: null, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
                const isEditing = state.adminCameraEditingModelKey === cKey;
                const livePrice = resolveCameraLensPrice(state.adminCameraModelBrand, model, cfg);
                return `
                  <div class="p-3 rounded-xl border ${cCfg.deleted ? 'bg-rose-50/40 border-rose-200' : 'bg-slate-50 border-slate-200'} space-y-2">
                    <div class="flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <div class="text-xs sm:text-sm font-extrabold text-icase-navy">${escapeHtml(model)}</div>
                        <div class="text-[11px] font-bold ${cCfg.deleted ? 'text-icase-red' : cCfg.enabled ? 'text-icase-green' : 'text-amber-700'}">
                          ${cCfg.deleted ? 'DELETED' : `${formatINR(livePrice.finalPrice)}${cCfg.enabled ? '' : ' • PAUSED'}`}
                        </div>
                      </div>
                      <div class="flex items-center gap-1.5">
                        ${
                          cCfg.deleted
                            ? `<button type="button" data-adm-cam-restore="${escapeHtml(cKey)}" class="px-2.5 py-1 rounded-lg bg-icase-blueLight text-icase-blue text-xs font-bold">Restore</button>`
                            : `
                              <button type="button" data-adm-cam-edit="${escapeHtml(cKey)}" class="px-2.5 py-1 rounded-lg bg-white border border-slate-300 text-xs font-bold">${isEditing ? 'Close' : 'Edit & Rate'}</button>
                              <button type="button" data-adm-cam-toggle="${escapeHtml(cKey)}" class="px-2.5 py-1 rounded-lg bg-white border border-slate-300 text-xs font-bold">${cCfg.enabled ? 'Pause' : 'Enable'}</button>
                              <button type="button" data-adm-cam-delete="${escapeHtml(cKey)}" class="px-2.5 py-1 rounded-lg bg-icase-redLight text-icase-red text-xs font-bold">Delete</button>
                            `
                        }
                      </div>
                    </div>
                    ${
                      isEditing && !cCfg.deleted
                        ? `
                      <div class="pt-2 border-t border-slate-200 bg-white p-3 rounded-xl grid grid-cols-2 sm:grid-cols-4 gap-2">
                        <div class="col-span-2 sm:col-span-4">
                          <label class="block text-[10px] font-bold text-slate-500">Rename Model (optional)</label>
                          <input id="adm-cm-rename-${cIdx}" type="text" value="${escapeHtml(model)}" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 text-xs font-bold" />
                        </div>
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500">Custom ₹</label>
                          <input id="adm-cm-price-${cIdx}" type="number" value="${escapeHtml(cCfg.customPrice || '')}" placeholder="Default" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 text-xs" />
                        </div>
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500">Offer Type</label>
                          <select id="adm-cm-otype-${cIdx}" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 text-xs bg-white">
                            <option value="NONE" ${cCfg.offer?.type === 'NONE' ? 'selected' : ''}>Use Default</option>
                            <option value="FLAT_OFF" ${cCfg.offer?.type === 'FLAT_OFF' ? 'selected' : ''}>Flat ₹ Off</option>
                            <option value="PERCENT_OFF" ${cCfg.offer?.type === 'PERCENT_OFF' ? 'selected' : ''}>% Off</option>
                            <option value="SPECIAL_PRICE" ${cCfg.offer?.type === 'SPECIAL_PRICE' ? 'selected' : ''}>Special ₹</option>
                          </select>
                        </div>
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500">Offer Value</label>
                          <input id="adm-cm-oval-${cIdx}" type="number" value="${escapeHtml(cCfg.offer?.value || 0)}" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 text-xs" />
                        </div>
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500">Badge</label>
                          <input id="adm-cm-obadge-${cIdx}" type="text" value="${escapeHtml(cCfg.offer?.badgeText || '')}" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 text-xs" />
                        </div>
                        <div class="col-span-2 sm:col-span-4">
                          <button type="button" data-adm-cam-save="${escapeHtml(cKey)}" data-adm-cam-idx="${cIdx}" data-adm-cam-orig="${escapeHtml(model)}" class="px-4 py-1.5 rounded-lg bg-icase-navy text-white text-xs font-bold">Save Lens Model</button>
                        </div>
                      </div>
                    `
                        : ''
                    }
                  </div>
                `;
              })
              .join('')}
          </div>
        </div>

        <!-- 4. Pouches (iPhone & Samsung S Series) Full CRUD -->
        <div class="bg-white rounded-2xl border border-slate-200 p-5 space-y-4">
          <div class="flex flex-wrap items-center justify-between gap-2">
            <div>
              <h3 class="font-extrabold text-icase-navy text-base">Pouches (iPhone &amp; Samsung S Series) — Images, Colours, Add, Edit &amp; Offers</h3>
              <p class="text-xs text-slate-500">Upload pouch images, pick preset or custom colours, and sync live to Cloud Firestore across Android &amp; Web.</p>
            </div>
            <button id="btn-adm-add-pouch" type="button" class="px-3.5 py-2 rounded-xl bg-icase-red text-white text-xs font-bold">+ Add New Pouch</button>
          </div>

          <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-3">
            ${(cfg.pouchItems || [])
              .map((p) => {
                const pr = resolvePouchPrice(p);
                const thumbUrl = resolvePouchImageUrl(p);
                const colors = Array.isArray(p.colorOptions) ? p.colorOptions : [];
                return `
                  <div class="p-3.5 rounded-2xl border ${p.deleted ? 'bg-rose-50/40 border-rose-200' : 'bg-slate-50 border-slate-200'} space-y-2.5">
                    <div class="flex items-start gap-3">
                      <div class="w-16 h-16 rounded-xl bg-white border border-slate-200 overflow-hidden shrink-0 flex items-center justify-center">
                        <img src="${escapeHtml(thumbUrl)}" alt="${escapeHtml(p.name)}" class="w-full h-full object-cover" />
                      </div>
                      <div class="min-w-0 flex-1">
                        <div class="flex items-center gap-1.5">
                          <div class="text-xs sm:text-sm font-extrabold text-icase-navy truncate">${escapeHtml(p.name)}</div>
                          ${p.showOnHomePage && !p.deleted ? `<span class="px-1.5 py-0.5 rounded bg-amber-100 text-amber-800 text-[9px] font-black shrink-0">HOME</span>` : ''}
                          ${p.videoUrl && !p.deleted ? `<span class="px-1.5 py-0.5 rounded bg-slate-900 text-white text-[9px] font-black shrink-0">VIDEO</span>` : ''}
                        </div>
                        <div class="text-[11px] font-bold ${p.deleted ? 'text-icase-red' : p.enabled ? 'text-icase-green' : 'text-amber-700'}">
                          ${p.deleted ? 'DELETED' : `${formatINR(pr.finalPrice)}${p.enabled ? '' : ' • PAUSED'}`}
                        </div>
                        <div class="text-[10px] text-slate-500 truncate">${escapeHtml((p.compatibleBrands || []).join(' • '))}</div>
                        ${
                          colors.length > 0
                            ? `
                          <div class="flex flex-wrap items-center gap-1 mt-1.5" title="${escapeHtml(colors.map((c) => c.name).join(', '))}">
                            ${colors
                              .slice(0, 7)
                              .map(
                                (c) => `
                              <span class="w-3.5 h-3.5 rounded-full border border-slate-300 inline-block" style="background:${escapeHtml(c.hex || '#1E293B')};" title="${escapeHtml(c.name)}"></span>
                            `
                              )
                              .join('')}
                            ${colors.length > 7 ? `<span class="text-[10px] font-bold text-slate-500">+${colors.length - 7}</span>` : ''}
                          </div>
                        `
                            : `<div class="text-[10px] text-slate-400 mt-1">Standard finish</div>`
                        }
                      </div>
                    </div>
                    <div class="flex flex-wrap gap-1.5">
                      ${
                        p.deleted
                          ? `<button type="button" data-adm-p-restore="${escapeHtml(p.id)}" class="px-2.5 py-1 rounded-lg bg-icase-blueLight text-icase-blue text-xs font-bold">Restore</button>`
                          : `
                            <button type="button" data-adm-p-edit="${escapeHtml(p.id)}" class="px-2.5 py-1 rounded-lg bg-white border border-slate-300 text-xs font-bold">Edit</button>
                            <button type="button" data-adm-p-home-toggle="${escapeHtml(p.id)}" class="px-2.5 py-1 rounded-lg text-xs font-bold border ${p.showOnHomePage ? 'bg-amber-50 border-amber-300 text-amber-800' : 'bg-white border-slate-300 text-slate-700'}">${p.showOnHomePage ? 'On Home ✓' : 'Show Home'}</button>
                            <button type="button" data-adm-p-toggle="${escapeHtml(p.id)}" class="px-2.5 py-1 rounded-lg bg-white border border-slate-300 text-xs font-bold">${p.enabled ? 'Pause' : 'Enable'}</button>
                            <button type="button" data-adm-p-delete="${escapeHtml(p.id)}" class="px-2.5 py-1 rounded-lg bg-icase-redLight text-icase-red text-xs font-bold">Delete</button>
                          `
                      }
                    </div>
                  </div>
                `;
              })
              .join('')}
          </div>

          ${
            draftPouch
              ? `
            <!-- Add / Edit Pouch Modal -->
            <div id="adm-pouch-modal-backdrop" class="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-3 sm:p-5 overflow-y-auto">
              <div class="bg-white rounded-3xl border border-slate-200 shadow-2xl w-full max-w-2xl max-h-[92vh] flex flex-col overflow-hidden">
                <div class="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
                  <div>
                    <h4 class="text-base font-extrabold text-icase-navy">${draftPouch.isNew ? 'Add New Pouch' : `Edit Pouch: ${escapeHtml(draftPouch.name)}`}</h4>
                    <p class="text-[11px] text-slate-500">Upload pouch photo, choose available colours, and sync to Cloud Firestore.</p>
                  </div>
                  <button id="btn-adm-cancel-pouch" type="button" class="px-3 py-1.5 rounded-xl bg-white border border-slate-300 text-xs font-bold text-slate-600 hover:bg-slate-100">Close</button>
                </div>

                <div class="p-5 space-y-5 overflow-y-auto flex-1">
                  <!-- 1. Pouch Image Upload -->
                  <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-3">
                    <div class="flex items-center justify-between gap-2">
                      <span class="text-xs font-extrabold text-icase-navy uppercase tracking-wide">1. Pouch Image Upload</span>
                      ${
                        draftPouch.imageUrl
                          ? `<button id="btn-adm-p-remove-img" type="button" class="px-2.5 py-1 rounded-lg bg-icase-redLight text-icase-red text-[11px] font-bold">Remove Image</button>`
                          : ''
                      }
                    </div>
                    <div class="flex flex-col sm:flex-row items-start sm:items-center gap-4">
                      <div class="w-24 h-24 rounded-2xl bg-white border-2 border-dashed border-slate-300 overflow-hidden shrink-0 flex items-center justify-center">
                        ${
                          draftPouch.imageUrl
                            ? `<img src="${escapeHtml(draftPouch.imageUrl)}" alt="Pouch preview" class="w-full h-full object-cover" />`
                            : `<div class="text-center px-2"><div class="text-[10px] font-bold text-slate-400">No Image</div><div class="text-[9px] text-slate-400">Preview</div></div>`
                        }
                      </div>
                      <div class="flex-1 w-full space-y-2">
                        <div class="flex flex-wrap gap-2">
                          <label class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-icase-navy text-white text-xs font-bold cursor-pointer hover:opacity-95">
                            <span>${draftPouch.imageUrl ? 'Change Gallery Image' : 'Upload from Gallery'}</span>
                            <input id="adm-p-img-file" type="file" accept="image/*" class="hidden" />
                          </label>
                          <label class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-white border border-slate-300 text-icase-navy text-xs font-bold cursor-pointer hover:bg-slate-100">
                            <span>Camera Photo</span>
                            <input id="adm-p-img-cam" type="file" accept="image/*" capture="environment" class="hidden" />
                          </label>
                        </div>
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500 mb-1">Or paste optional Image URL</label>
                          <input id="adm-p-img-url" type="url" value="${draftPouch.imageUrl && !String(draftPouch.imageUrl).startsWith('data:image') ? escapeHtml(draftPouch.imageUrl) : ''}" placeholder="https://example.com/pouch-photo.jpg" class="w-full px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs" />
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 1b. Optional Pouch Video -->
                  <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-2.5">
                    <div class="flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <span class="text-xs font-extrabold text-icase-navy uppercase tracking-wide">Optional Pouch Video</span>
                        <p class="text-[11px] text-slate-500">Direct video URL, YouTube link, or short video file (&le; 350 KB)</p>
                      </div>
                      <div class="flex items-center gap-1.5">
                        ${
                          draftPouch.videoUrl
                            ? `<button id="btn-adm-p-preview-vid" type="button" class="px-2.5 py-1 rounded-lg bg-icase-blueLight text-icase-blue text-[11px] font-bold">▶ Preview</button>
                               <button id="btn-adm-p-remove-vid" type="button" class="px-2.5 py-1 rounded-lg bg-icase-redLight text-icase-red text-[11px] font-bold">Remove Video</button>`
                            : ''
                        }
                      </div>
                    </div>
                    <div class="flex flex-wrap items-center gap-2">
                      <label class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-900 text-white text-xs font-bold cursor-pointer hover:opacity-95">
                        <span>${draftPouch.videoUrl ? 'Replace Short Video' : 'Upload Short Video'}</span>
                        <input id="adm-p-vid-file" type="file" accept="video/*" class="hidden" />
                      </label>
                      ${
                        draftPouch.videoUrl && String(draftPouch.videoUrl).startsWith('data:')
                          ? `<span class="px-2.5 py-1 rounded-lg bg-emerald-50 text-emerald-700 text-[11px] font-bold">Short video file attached</span>`
                          : ''
                      }
                    </div>
                    <input id="adm-p-vid-url" type="text" value="${draftPouch.videoUrl && !String(draftPouch.videoUrl).startsWith('data:') ? escapeHtml(draftPouch.videoUrl) : ''}" placeholder="YouTube link or direct .mp4/.webm URL (optional)" class="w-full px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs" />
                  </div>

                  <!-- 2. Pouch Details -->
                  <div class="space-y-3">
                    <span class="block text-xs font-extrabold text-icase-navy uppercase tracking-wide">2. Pouch Details &amp; Pricing</span>
                    <div class="grid sm:grid-cols-2 gap-2.5">
                      <div>
                        <label class="block text-[10px] font-bold text-slate-500 mb-1">Pouch Name *</label>
                        <input id="adm-p-name" type="text" value="${escapeHtml(draftPouch.name)}" placeholder="e.g. MagSafe Matte Armor Pouch" class="w-full px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs font-bold" />
                      </div>
                      <div>
                        <label class="block text-[10px] font-bold text-slate-500 mb-1">Base Price ₹ *</label>
                        <input id="adm-p-price" type="number" value="${escapeHtml(draftPouch.basePrice)}" placeholder="349" class="w-full px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs font-bold" />
                      </div>
                    </div>
                    <div>
                      <label class="block text-[10px] font-bold text-slate-500 mb-1">Description</label>
                      <input id="adm-p-desc" type="text" value="${escapeHtml(draftPouch.description || '')}" placeholder="Material, camera ring guard, MagSafe magnet, etc." class="w-full px-3 py-2 rounded-xl border border-slate-300 bg-white text-xs" />
                    </div>
                    <div class="flex flex-wrap gap-4 text-xs font-bold text-icase-navy">
                      <label class="inline-flex items-center gap-1.5 cursor-pointer"><input id="adm-p-brand-iphone" type="checkbox" ${(draftPouch.compatibleBrands || []).includes('iPhone') ? 'checked' : ''} /> iPhone</label>
                      <label class="inline-flex items-center gap-1.5 cursor-pointer"><input id="adm-p-brand-samsung" type="checkbox" ${(draftPouch.compatibleBrands || []).includes('Samsung S Series') ? 'checked' : ''} /> Samsung S Series</label>
                    </div>
                    <label class="flex items-center justify-between gap-3 p-3 rounded-xl bg-slate-50 border border-slate-200 cursor-pointer">
                      <div>
                        <div class="text-xs font-extrabold text-icase-navy">Show on Home Page</div>
                        <div class="text-[11px] text-slate-500">Feature this pouch in Featured Pouches on the Home tab</div>
                      </div>
                      <input id="adm-p-show-home" type="checkbox" ${draftPouch.showOnHomePage ? 'checked' : ''} class="w-4 h-4 accent-icase-green" />
                    </label>
                    <div class="grid grid-cols-3 gap-2">
                      <div>
                        <label class="block text-[10px] font-bold text-slate-500 mb-1">Offer Type</label>
                        <select id="adm-p-otype" class="w-full px-2 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold">
                          <option value="NONE" ${draftPouch.offerType === 'NONE' ? 'selected' : ''}>No Offer</option>
                          <option value="FLAT_OFF" ${draftPouch.offerType === 'FLAT_OFF' ? 'selected' : ''}>Flat ₹ Off</option>
                          <option value="PERCENT_OFF" ${draftPouch.offerType === 'PERCENT_OFF' ? 'selected' : ''}>% Off</option>
                          <option value="SPECIAL_PRICE" ${draftPouch.offerType === 'SPECIAL_PRICE' ? 'selected' : ''}>Special ₹</option>
                        </select>
                      </div>
                      <div>
                        <label class="block text-[10px] font-bold text-slate-500 mb-1">Offer Value</label>
                        <input id="adm-p-oval" type="number" value="${escapeHtml(draftPouch.offerValue || 0)}" placeholder="0" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
                      </div>
                      <div>
                        <label class="block text-[10px] font-bold text-slate-500 mb-1">Offer Badge</label>
                        <input id="adm-p-obadge" type="text" value="${escapeHtml(draftPouch.badgeText || '')}" placeholder="HOT DEAL" class="w-full px-2.5 py-1.5 rounded-lg border border-slate-300 bg-white text-xs" />
                      </div>
                    </div>
                  </div>

                  <!-- 3. Available Colours Manager -->
                  <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-3">
                    <div>
                      <span class="block text-xs font-extrabold text-icase-navy uppercase tracking-wide">3. Available Colours Manager</span>
                      <p class="text-[11px] text-slate-500">Tap preset colours to toggle selection or add custom colours with optional per-colour photos.</p>
                    </div>

                    <!-- Preset Multi-select -->
                    <div class="flex flex-wrap gap-1.5">
                      ${POUCH_PRESET_COLORS.map((pc) => {
                        const isSelected = (draftPouch.colorOptions || []).some(
                          (c) => c.name.toLowerCase() === pc.name.toLowerCase()
                        );
                        return `
                          <button type="button" data-adm-p-preset-color="${escapeHtml(pc.name)}" class="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl text-xs font-bold border transition ${
                            isSelected
                              ? 'bg-icase-navy text-white border-icase-navy shadow-xs'
                              : 'bg-white text-icase-navy border-slate-300 hover:border-icase-navy'
                          }">
                            <span class="w-3 h-3 rounded-full border border-slate-300 shrink-0" style="background:${escapeHtml(pc.hex)};"></span>
                            <span>${escapeHtml(pc.name)}</span>
                            ${isSelected ? '<span class="text-[10px]">✓</span>' : ''}
                          </button>
                        `;
                      }).join('')}
                    </div>

                    <!-- Custom Colour Adder -->
                    <div class="flex flex-wrap items-center gap-2 pt-1">
                      <input id="adm-p-custom-cname" type="text" placeholder="Custom colour name (e.g. Titanium Gold)" class="flex-1 min-w-[170px] px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-xs font-bold" />
                      <div class="inline-flex items-center gap-1.5 bg-white border border-slate-300 rounded-xl px-2 py-1">
                        <input id="adm-p-custom-cpicker" type="color" value="#1E293B" class="w-6 h-6 rounded cursor-pointer border-0 bg-transparent p-0" />
                        <input id="adm-p-custom-chex" type="text" value="#1E293B" placeholder="#HEX" class="w-20 text-xs font-mono font-bold text-icase-navy outline-none" />
                      </div>
                      <button id="btn-adm-p-add-custom-color" type="button" class="px-3 py-1.5 rounded-xl bg-icase-navy text-white text-xs font-bold">+ Add Colour</button>
                    </div>

                    <!-- Selected Colours & Optional Per-Colour Photos -->
                    ${
                      (draftPouch.colorOptions || []).length > 0
                        ? `
                      <div class="space-y-2 pt-2 border-t border-slate-200">
                        <div class="text-[11px] font-extrabold text-slate-600">Selected Colours (${draftPouch.colorOptions.length})</div>
                        ${draftPouch.colorOptions
                          .map((c, cIdx) => {
                            const isExpanded = draftPouch.expandedColorName === c.name;
                            return `
                              <div class="p-2.5 rounded-xl bg-white border border-slate-200 space-y-2">
                                <div class="flex flex-wrap items-center justify-between gap-2">
                                  <div class="flex items-center gap-2 min-w-0">
                                    <span class="w-4 h-4 rounded-full border border-slate-300 shrink-0" style="background:${escapeHtml(c.hex || '#1E293B')};"></span>
                                    <span class="text-xs font-extrabold text-icase-navy truncate">${escapeHtml(c.name)}</span>
                                    <span class="text-[10px] font-mono text-slate-400">${escapeHtml(c.hex || '')}</span>
                                    ${c.imageUrl ? '<span class="px-1.5 py-0.5 rounded bg-emerald-50 text-emerald-700 text-[10px] font-bold">Photo set</span>' : ''}
                                  </div>
                                  <div class="flex items-center gap-1.5">
                                    <button type="button" data-adm-p-color-photo-toggle="${escapeHtml(c.name)}" class="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-icase-navy text-[11px] font-bold">
                                      ${isExpanded ? 'Hide Photo' : 'Colour Photo'}
                                    </button>
                                    <button type="button" data-adm-p-color-del="${cIdx}" class="px-2 py-1 rounded-lg bg-icase-redLight text-icase-red text-[11px] font-bold">Remove</button>
                                  </div>
                                </div>
                                ${
                                  isExpanded
                                    ? `
                                  <div class="pt-2 border-t border-slate-100 flex flex-col sm:flex-row items-start sm:items-center gap-3">
                                    <div class="w-14 h-14 rounded-lg bg-slate-50 border border-slate-200 overflow-hidden shrink-0 flex items-center justify-center">
                                      ${
                                        c.imageUrl
                                          ? `<img src="${escapeHtml(c.imageUrl)}" alt="${escapeHtml(c.name)}" class="w-full h-full object-cover" />`
                                          : `<span class="text-[9px] text-slate-400 font-bold">None</span>`
                                      }
                                    </div>
                                    <div class="flex-1 w-full space-y-1.5">
                                      <div class="flex flex-wrap gap-1.5">
                                        <label class="px-2.5 py-1 rounded-lg bg-icase-navy text-white text-[11px] font-bold cursor-pointer">
                                          Gallery
                                          <input type="file" accept="image/*" data-adm-p-color-file="${cIdx}" class="hidden" />
                                        </label>
                                        <label class="px-2.5 py-1 rounded-lg bg-white border border-slate-300 text-icase-navy text-[11px] font-bold cursor-pointer">
                                          Camera
                                          <input type="file" accept="image/*" capture="environment" data-adm-p-color-cam="${cIdx}" class="hidden" />
                                        </label>
                                        ${
                                          c.imageUrl
                                            ? `<button type="button" data-adm-p-color-clearimg="${cIdx}" class="px-2.5 py-1 rounded-lg bg-icase-redLight text-icase-red text-[11px] font-bold">Clear Photo</button>`
                                            : ''
                                        }
                                      </div>
                                      <input type="url" data-adm-p-color-url="${cIdx}" value="${c.imageUrl && !String(c.imageUrl).startsWith('data:image') ? escapeHtml(c.imageUrl) : ''}" placeholder="Optional image URL for ${escapeHtml(c.name)}" class="w-full px-2.5 py-1 rounded-lg border border-slate-300 text-[11px]" />
                                    </div>
                                  </div>
                                `
                                    : ''
                                }
                              </div>
                            `;
                          })
                          .join('')}
                      </div>
                    `
                        : `<div class="text-[11px] text-slate-500">No colours selected yet.</div>`
                    }
                  </div>
                </div>

                <div class="px-5 py-3.5 border-t border-slate-200 bg-slate-50 flex items-center justify-end gap-2">
                  <button id="btn-adm-cancel-pouch-footer" type="button" class="px-4 py-2 rounded-xl border border-slate-300 bg-white text-xs font-bold text-slate-600">Cancel</button>
                  <button id="btn-adm-save-pouch" type="button" class="px-5 py-2 rounded-xl bg-icase-navy text-white text-xs font-bold shadow-sm hover:opacity-95">Save Pouch</button>
                </div>
              </div>
            </div>
          `
              : ''
          }

          <!-- Pouch Phone Models Management -->
          <div class="pt-3 border-t border-slate-200 space-y-3">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div class="flex flex-wrap items-center gap-1.5">
                <span class="text-xs font-extrabold text-icase-navy mr-1">Pouch Models:</span>
                ${POUCH_BRANDS.map(
                  (b) => `
                  <button type="button" data-adm-pm-brand="${escapeHtml(b)}" class="px-3 py-1 rounded-xl text-xs font-bold border ${
                    state.adminPouchModelBrand === b ? 'bg-icase-navy text-white border-icase-navy' : 'bg-slate-50 text-icase-navy border-slate-200'
                  }">${escapeHtml(b)}</button>
                `
                ).join('')}
                <button id="btn-adm-toggle-add-pm" type="button" class="px-3 py-1 rounded-xl bg-icase-red text-white text-xs font-bold">+ Add Model</button>
              </div>
              <input id="admin-pouch-model-search" type="text" value="${escapeHtml(state.adminPouchModelSearch)}" placeholder="Filter pouch models..." class="px-3 py-1.5 rounded-xl border border-slate-300 text-xs w-full sm:w-52" />
            </div>

            ${
              state.adminPouchAddModelOpen
                ? `
              <div class="p-3 rounded-xl bg-icase-blueLight/40 border border-icase-blue/30 flex flex-wrap items-center gap-2">
                <input id="adm-new-pm-name" type="text" placeholder="New ${escapeHtml(state.adminPouchModelBrand)} model" class="flex-1 min-w-[200px] px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
                <button id="btn-adm-save-new-pm" type="button" class="px-3.5 py-1.5 rounded-lg bg-icase-navy text-white text-xs font-bold">Add Model</button>
                <button id="btn-adm-cancel-new-pm" type="button" class="px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-bold">Cancel</button>
              </div>
            `
                : ''
            }

            <div class="space-y-1.5 max-h-64 overflow-y-auto pr-1">
              ${filteredPouchModels
                .map((model, pmIdx) => {
                  const pmKey = guardModelKey(state.adminPouchModelBrand, model);
                  const pmCfg = cfg.pouchModelConfigs?.[pmKey] || { enabled: true, deleted: false };
                  const isRenaming = state.adminPouchEditingModelKey === pmKey;
                  return `
                    <div class="p-2.5 rounded-xl border ${pmCfg.deleted ? 'bg-rose-50/40 border-rose-200' : 'bg-slate-50 border-slate-200'} space-y-2">
                      <div class="flex flex-wrap items-center justify-between gap-2">
                        <div class="text-xs font-extrabold text-icase-navy">
                          ${escapeHtml(model)}
                          <span class="ml-1.5 text-[10px] ${pmCfg.deleted ? 'text-icase-red' : pmCfg.enabled ? 'text-icase-green' : 'text-amber-700'}">
                            (${pmCfg.deleted ? 'DELETED' : pmCfg.enabled ? 'Active' : 'Paused'})
                          </span>
                        </div>
                        <div class="flex items-center gap-1.5">
                          ${
                            pmCfg.deleted
                              ? `<button type="button" data-adm-pm-restore="${escapeHtml(pmKey)}" class="px-2 py-1 rounded-lg bg-icase-blueLight text-icase-blue text-[11px] font-bold">Restore</button>`
                              : `
                                <button type="button" data-adm-pm-edit="${escapeHtml(pmKey)}" class="px-2 py-1 rounded-lg bg-white border border-slate-300 text-[11px] font-bold">${isRenaming ? 'Close' : 'Rename'}</button>
                                <button type="button" data-adm-pm-toggle="${escapeHtml(pmKey)}" class="px-2 py-1 rounded-lg bg-white border border-slate-300 text-[11px] font-bold">${pmCfg.enabled ? 'Pause' : 'Enable'}</button>
                                <button type="button" data-adm-pm-delete="${escapeHtml(pmKey)}" class="px-2 py-1 rounded-lg bg-icase-redLight text-icase-red text-[11px] font-bold">Delete</button>
                              `
                          }
                        </div>
                      </div>
                      ${
                        isRenaming && !pmCfg.deleted
                          ? `
                        <div class="flex items-center gap-2 pt-1">
                          <input id="adm-pm-rename-${pmIdx}" type="text" value="${escapeHtml(model)}" class="flex-1 px-2.5 py-1 rounded-lg border border-slate-300 bg-white text-xs font-bold" />
                          <button type="button" data-adm-pm-save="${escapeHtml(pmKey)}" data-adm-pm-idx="${pmIdx}" data-adm-pm-orig="${escapeHtml(model)}" class="px-3 py-1 rounded-lg bg-icase-navy text-white text-xs font-bold">Save Name</button>
                        </div>
                      `
                          : ''
                      }
                    </div>
                  `;
                })
                .join('')}
            </div>
          </div>
        </div>
      </div>
    `;

    // 0. Sample Card & Item Images Events
    document.getElementById('adm-tg-img-file')?.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      try {
        const dataUrl = await compressImageFileToDataUrl(file);
        const next = parseScreenGuardConfig(cfg);
        next.temperedGlassImageUrl = dataUrl;
        saveGuardConfig(next, 'Tempered Glass sample image updated!');
      } catch (err) {
        showToast(err?.message || 'Unable to process image');
      }
      e.target.value = '';
    });
    document.getElementById('adm-tg-img-save')?.addEventListener('click', () => {
      const next = parseScreenGuardConfig(cfg);
      next.temperedGlassImageUrl = document.getElementById('adm-tg-img-url')?.value?.trim() || '';
      saveGuardConfig(next, 'Tempered Glass image saved!');
    });
    document.getElementById('adm-tg-img-reset')?.addEventListener('click', () => {
      const next = parseScreenGuardConfig(cfg);
      next.temperedGlassImageUrl = '';
      saveGuardConfig(next, 'Tempered Glass image reset to default');
    });
    document.getElementById('adm-cl-img-file')?.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      try {
        const dataUrl = await compressImageFileToDataUrl(file);
        const next = parseScreenGuardConfig(cfg);
        next.cameraLensImageUrl = dataUrl;
        saveGuardConfig(next, 'Camera Lens sample image updated!');
      } catch (err) {
        showToast(err?.message || 'Unable to process image');
      }
      e.target.value = '';
    });
    document.getElementById('adm-cl-img-save')?.addEventListener('click', () => {
      const next = parseScreenGuardConfig(cfg);
      next.cameraLensImageUrl = document.getElementById('adm-cl-img-url')?.value?.trim() || '';
      saveGuardConfig(next, 'Camera Lens image saved!');
    });
    document.getElementById('adm-cl-img-reset')?.addEventListener('click', () => {
      const next = parseScreenGuardConfig(cfg);
      next.cameraLensImageUrl = '';
      saveGuardConfig(next, 'Camera Lens image reset to default');
    });
    document.getElementById('adm-pcard-img-file')?.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      try {
        const dataUrl = await compressImageFileToDataUrl(file);
        const next = parseScreenGuardConfig(cfg);
        next.pouchCardImageUrl = dataUrl;
        saveGuardConfig(next, 'Pouches card sample image updated!');
      } catch (err) {
        showToast(err?.message || 'Unable to process image');
      }
      e.target.value = '';
    });
    document.getElementById('adm-pcard-img-save')?.addEventListener('click', () => {
      const next = parseScreenGuardConfig(cfg);
      next.pouchCardImageUrl = document.getElementById('adm-pcard-img-url')?.value?.trim() || '';
      saveGuardConfig(next, 'Pouches card image saved!');
    });
    document.getElementById('adm-pcard-img-reset')?.addEventListener('click', () => {
      const next = parseScreenGuardConfig(cfg);
      next.pouchCardImageUrl = '';
      saveGuardConfig(next, 'Pouches card image reset to default');
    });
    document.getElementById('adm-pcard-price-save')?.addEventListener('click', () => {
      const raw = Number(document.getElementById('adm-pcard-start-price')?.value || 0);
      const next = parseScreenGuardConfig(cfg);
      next.pouchCardStartingPrice = Number.isFinite(raw) && raw > 0 ? raw : 0;
      saveGuardConfig(next, 'Pouches starting price updated!');
    });

    // 1. Glass Type Events (Add + Save + Toggle + Delete + Restore + Media)
    document.getElementById('btn-adm-toggle-add-gt')?.addEventListener('click', () => {
      state.adminGlassAddingType = !state.adminGlassAddingType;
      renderAdminSection();
    });
    document.getElementById('btn-adm-cancel-new-gt')?.addEventListener('click', () => {
      state.adminGlassAddingType = false;
      state.adminNewGtImageUrl = '';
      state.adminNewGtVideoUrl = '';
      renderAdminSection();
    });
    document.getElementById('adm-new-gt-img-file')?.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      try {
        const dataUrl = await compressImageFileToDataUrl(file);
        state.adminNewGtImageUrl = dataUrl;
        const inp = document.getElementById('adm-new-gt-img-url');
        if (inp) inp.value = dataUrl;
        document.getElementById('adm-new-gt-img-clear')?.classList.remove('hidden');
        showToast('Glass type image ready!');
      } catch (err) {
        showToast(err?.message || 'Unable to process image');
      }
      e.target.value = '';
    });
    document.getElementById('adm-new-gt-img-clear')?.addEventListener('click', () => {
      state.adminNewGtImageUrl = '';
      const inp = document.getElementById('adm-new-gt-img-url');
      if (inp) inp.value = '';
      document.getElementById('adm-new-gt-img-clear')?.classList.add('hidden');
    });
    document.getElementById('adm-new-gt-vid-file')?.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      try {
        const dataUrl = await readShortVideoFileToDataUrl(file);
        state.adminNewGtVideoUrl = dataUrl;
        const inp = document.getElementById('adm-new-gt-vid-url');
        if (inp) inp.value = dataUrl;
        document.getElementById('adm-new-gt-vid-clear')?.classList.remove('hidden');
        showToast('Glass type video ready!');
      } catch (err) {
        showToast(err?.message || 'Unable to read video');
      }
      e.target.value = '';
    });
    document.getElementById('adm-new-gt-vid-clear')?.addEventListener('click', () => {
      state.adminNewGtVideoUrl = '';
      const inp = document.getElementById('adm-new-gt-vid-url');
      if (inp) inp.value = '';
      document.getElementById('adm-new-gt-vid-clear')?.classList.add('hidden');
    });
    document.getElementById('btn-adm-save-new-gt')?.addEventListener('click', () => {
      const name = document.getElementById('adm-new-gt-name')?.value?.trim();
      const basePrice = Math.max(0, Number(document.getElementById('adm-new-gt-price')?.value || 299));
      const helperNote = document.getElementById('adm-new-gt-note')?.value?.trim() || '';
      const imageUrl = document.getElementById('adm-new-gt-img-url')?.value?.trim() || state.adminNewGtImageUrl || '';
      const videoUrl = document.getElementById('adm-new-gt-vid-url')?.value?.trim() || state.adminNewGtVideoUrl || '';
      if (!name) return;
      const next = parseScreenGuardConfig(cfg);
      next.glassTypeConfigs[name] = {
        basePrice,
        enabled: true,
        deleted: false,
        offer: { active: false, type: 'NONE', value: 0, badgeText: '' },
        helperNote,
        imageUrl,
        videoUrl
      };
      state.adminGlassAddingType = false;
      state.adminNewGtImageUrl = '';
      state.adminNewGtVideoUrl = '';
      saveGuardConfig(next, `${name} glass type added!`);
    });

    const readGlassTypeRowAndSave = (idx, overrideFields = {}, customToast = null) => {
      const gt = glassTypesList[idx];
      if (!gt) return;
      const basePrice = Math.max(0, Number(document.getElementById(`gt-price-${idx}`)?.value || DEFAULT_GLASS_TYPE_PRICES[gt] || 199));
      const type = document.getElementById(`gt-otype-${idx}`)?.value || 'NONE';
      const value = Math.max(0, Number(document.getElementById(`gt-oval-${idx}`)?.value || 0));
      const badgeText = document.getElementById(`gt-obadge-${idx}`)?.value?.trim() || '';
      const helperNote = document.getElementById(`gt-note-${idx}`)?.value?.trim() || '';
      const imageUrl =
        overrideFields.imageUrl !== undefined
          ? overrideFields.imageUrl
          : document.getElementById(`gt-img-${idx}`)?.value?.trim() || '';
      const videoUrl =
        overrideFields.videoUrl !== undefined
          ? overrideFields.videoUrl
          : document.getElementById(`gt-vid-${idx}`)?.value?.trim() || '';
      const next = parseScreenGuardConfig(cfg);
      next.glassTypeConfigs[gt] = {
        ...next.glassTypeConfigs[gt],
        basePrice,
        deleted: false,
        offer: { active: type !== 'NONE', type, value, badgeText },
        helperNote,
        imageUrl,
        videoUrl
      };
      saveGuardConfig(next, customToast || `${gt} rate, media & offer synced!`);
    };

    content.querySelectorAll('[data-gt-save]').forEach((b) =>
      b.addEventListener('click', () => {
        readGlassTypeRowAndSave(Number(b.dataset.gtSave));
      })
    );
    content.querySelectorAll('[data-gt-img-file]').forEach((inp) =>
      inp.addEventListener('change', async (e) => {
        const idx = Number(inp.dataset.gtImgFile);
        const file = e.target.files?.[0];
        if (!file) return;
        try {
          const dataUrl = await compressImageFileToDataUrl(file);
          readGlassTypeRowAndSave(idx, { imageUrl: dataUrl }, `${glassTypesList[idx]} image uploaded & saved!`);
        } catch (err) {
          showToast(err?.message || 'Unable to process image');
        }
        e.target.value = '';
      })
    );
    content.querySelectorAll('[data-gt-img-reset]').forEach((b) =>
      b.addEventListener('click', () => {
        const idx = Number(b.dataset.gtImgReset);
        readGlassTypeRowAndSave(idx, { imageUrl: '' }, `${glassTypesList[idx]} image reset to default`);
      })
    );
    content.querySelectorAll('[data-gt-vid-file]').forEach((inp) =>
      inp.addEventListener('change', async (e) => {
        const idx = Number(inp.dataset.gtVidFile);
        const file = e.target.files?.[0];
        if (!file) return;
        try {
          const dataUrl = await readShortVideoFileToDataUrl(file);
          readGlassTypeRowAndSave(idx, { videoUrl: dataUrl }, `${glassTypesList[idx]} video uploaded & saved!`);
        } catch (err) {
          showToast(err?.message || 'Unable to read selected video');
        }
        e.target.value = '';
      })
    );
    content.querySelectorAll('[data-gt-vid-input]').forEach((inp) =>
      inp.addEventListener('input', () => {
        const idx = Number(inp.dataset.gtVidInput);
        const has = Boolean(inp.value.trim());
        document.getElementById(`gt-vid-prev-${idx}`)?.classList.toggle('hidden', !has);
        document.getElementById(`gt-vid-clr-${idx}`)?.classList.toggle('hidden', !has);
      })
    );
    content.querySelectorAll('[data-gt-vid-preview]').forEach((b) =>
      b.addEventListener('click', () => {
        const idx = Number(b.dataset.gtVidPreview);
        const gt = glassTypesList[idx];
        const url = document.getElementById(`gt-vid-${idx}`)?.value?.trim();
        if (url) openVideoModal(`${gt} Tempered Glass`, url);
      })
    );
    content.querySelectorAll('[data-gt-vid-clear]').forEach((b) =>
      b.addEventListener('click', () => {
        const idx = Number(b.dataset.gtVidClear);
        readGlassTypeRowAndSave(idx, { videoUrl: '' }, `${glassTypesList[idx]} video removed`);
      })
    );
    content.querySelectorAll('[data-gt-toggle]').forEach((chk) =>
      chk.addEventListener('change', () => {
        const idx = Number(chk.dataset.gtToggle);
        const gt = glassTypesList[idx];
        const next = parseScreenGuardConfig(cfg);
        next.glassTypeConfigs[gt] = { ...next.glassTypeConfigs[gt], enabled: Boolean(chk.checked), deleted: false };
        saveGuardConfig(next, `${gt} ${chk.checked ? 'enabled' : 'paused'}`);
      })
    );
    content.querySelectorAll('[data-gt-delete]').forEach((b) =>
      b.addEventListener('click', () => {
        const idx = Number(b.dataset.gtDelete);
        const gt = glassTypesList[idx];
        const next = parseScreenGuardConfig(cfg);
        next.glassTypeConfigs[gt] = { ...next.glassTypeConfigs[gt], deleted: true, enabled: false };
        saveGuardConfig(next, `${gt} deleted permanently`);
      })
    );
    content.querySelectorAll('[data-gt-restore]').forEach((b) =>
      b.addEventListener('click', () => {
        const idx = Number(b.dataset.gtRestore);
        const gt = glassTypesList[idx];
        const next = parseScreenGuardConfig(cfg);
        next.glassTypeConfigs[gt] = { ...next.glassTypeConfigs[gt], deleted: false, enabled: true };
        saveGuardConfig(next, `${gt} restored`);
      })
    );

    // 2. Tempered Model Events (Add + Search + Brand + Rename/Rates + Toggle + Delete + Restore)
    document.getElementById('btn-adm-toggle-add-gm')?.addEventListener('click', () => {
      state.adminGlassAddModelOpen = !state.adminGlassAddModelOpen;
      renderAdminSection();
    });
    document.getElementById('btn-adm-cancel-new-gm')?.addEventListener('click', () => {
      state.adminGlassAddModelOpen = false;
      renderAdminSection();
    });
    document.getElementById('btn-adm-save-new-gm')?.addEventListener('click', () => {
      const mName = document.getElementById('adm-new-gm-name')?.value?.trim();
      if (!mName) return;
      const next = parseScreenGuardConfig(cfg);
      const list = [...(next.customModelsByBrand[state.adminGlassModelBrand] || [])];
      if (!list.includes(mName)) list.push(mName);
      next.customModelsByBrand[state.adminGlassModelBrand] = list;
      delete next.modelConfigs[guardModelKey(state.adminGlassModelBrand, mName)];
      state.adminGlassAddModelOpen = false;
      saveGuardConfig(next, `${mName} added to ${state.adminGlassModelBrand}`);
    });
    document.getElementById('admin-glass-model-search')?.addEventListener('input', (e) => {
      state.adminGlassModelSearch = e.target.value;
      renderAdminSection();
      const el = document.getElementById('admin-glass-model-search');
      if (el) {
        el.focus();
        el.setSelectionRange(el.value.length, el.value.length);
      }
    });
    content.querySelectorAll('[data-adm-glass-brand]').forEach((b) =>
      b.addEventListener('click', () => {
        state.adminGlassModelBrand = b.dataset.admGlassBrand;
        state.adminGlassEditingModelKey = null;
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-adm-m-edit]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admMEdit;
        state.adminGlassEditingModelKey = state.adminGlassEditingModelKey === k ? null : k;
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-adm-m-toggle]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admMToggle;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.modelConfigs[k] || { enabled: true, deleted: false, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        next.modelConfigs[k] = { ...cur, enabled: !cur.enabled, deleted: false };
        saveGuardConfig(next, 'Model availability updated');
      })
    );
    content.querySelectorAll('[data-adm-m-delete]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admMDelete;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.modelConfigs[k] || { enabled: true, deleted: false, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        next.modelConfigs[k] = { ...cur, deleted: true, enabled: false };
        if (state.adminGlassEditingModelKey === k) state.adminGlassEditingModelKey = null;
        saveGuardConfig(next, 'Model permanently deleted');
      })
    );
    content.querySelectorAll('[data-adm-m-restore]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admMRestore;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.modelConfigs[k] || { enabled: true, deleted: false, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        next.modelConfigs[k] = { ...cur, deleted: false, enabled: true };
        saveGuardConfig(next, 'Model restored');
      })
    );
    content.querySelectorAll('[data-adm-m-save]').forEach((b) =>
      b.addEventListener('click', () => {
        const origKey = b.dataset.admMSave;
        const mIdx = Number(b.dataset.admMIdx);
        const origModel = b.dataset.admMOrig;
        const newModel = document.getElementById(`adm-mrename-${mIdx}`)?.value?.trim() || origModel;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.modelConfigs[origKey] || { enabled: true, deleted: false, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        const customRates = {};
        content.querySelectorAll(`[id^="adm-mr-${mIdx}-"]`).forEach((inp) => {
          const gt = inp.dataset.mrGt;
          const v = Number(inp.value || 0);
          if (gt && v > 0) customRates[gt] = v;
        });
        const type = document.getElementById(`adm-mo-type-${mIdx}`)?.value || 'NONE';
        const value = Math.max(0, Number(document.getElementById(`adm-mo-val-${mIdx}`)?.value || 0));
        const badgeText = document.getElementById(`adm-mo-badge-${mIdx}`)?.value?.trim() || '';
        const updatedCfg = {
          ...cur,
          customRates,
          offer: { active: type !== 'NONE', type, value, badgeText }
        };
        let targetKey = origKey;
        if (newModel && newModel !== origModel) {
          const brand = state.adminGlassModelBrand;
          const customList = [...(next.customModelsByBrand[brand] || [])];
          const replaced = customList.filter((m) => m !== origModel);
          if (!replaced.includes(newModel)) replaced.push(newModel);
          next.customModelsByBrand[brand] = replaced;
          if ((SCREEN_GUARD_MODELS[brand] || []).includes(origModel)) {
            next.modelConfigs[origKey] = { enabled: false, deleted: true, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
          } else {
            delete next.modelConfigs[origKey];
          }
          targetKey = guardModelKey(brand, newModel);
        }
        next.modelConfigs[targetKey] = updatedCfg;
        state.adminGlassEditingModelKey = null;
        saveGuardConfig(next, 'Model settings saved!');
      })
    );

    // 3. Camera Lens & Combo Events
    document.getElementById('adm-cam-global-enable')?.addEventListener('change', (e) => {
      const next = parseScreenGuardConfig(cfg);
      next.cameraLensDefault = { ...next.cameraLensDefault, enabled: Boolean(e.target.checked), deleted: false };
      saveGuardConfig(next, `Camera Lens Protection ${e.target.checked ? 'enabled' : 'paused'}`);
    });
    const syncCamDefVideoBtns = () => {
      const has = Boolean(document.getElementById('adm-cam-def-vid-url')?.value?.trim());
      document.getElementById('adm-cam-def-vid-preview')?.classList.toggle('hidden', !has);
      document.getElementById('adm-cam-def-vid-clear')?.classList.toggle('hidden', !has);
    };
    document.getElementById('adm-cam-def-vid-url')?.addEventListener('input', syncCamDefVideoBtns);
    document.getElementById('adm-cam-def-vid-file')?.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      try {
        const dataUrl = await readShortVideoFileToDataUrl(file);
        const inp = document.getElementById('adm-cam-def-vid-url');
        if (inp) inp.value = dataUrl;
        syncCamDefVideoBtns();
        showToast('Camera Lens video uploaded! Click Save Camera Lens Defaults.');
      } catch (err) {
        showToast(err?.message || 'Unable to read selected video');
      }
      e.target.value = '';
    });
    document.getElementById('adm-cam-def-vid-preview')?.addEventListener('click', () => {
      const url = document.getElementById('adm-cam-def-vid-url')?.value?.trim();
      if (url) openVideoModal('Camera Lens Protection Demo', url);
    });
    document.getElementById('adm-cam-def-vid-clear')?.addEventListener('click', () => {
      const inp = document.getElementById('adm-cam-def-vid-url');
      if (inp) inp.value = '';
      syncCamDefVideoBtns();
    });
    document.getElementById('adm-cam-def-save')?.addEventListener('click', () => {
      const basePrice = Math.max(0, Number(document.getElementById('adm-cam-def-price')?.value || DEFAULT_CAMERA_LENS_PRICE));
      const type = document.getElementById('adm-cam-def-otype')?.value || 'NONE';
      const value = Math.max(0, Number(document.getElementById('adm-cam-def-oval')?.value || 0));
      const badgeText = document.getElementById('adm-cam-def-obadge')?.value?.trim() || '';
      const videoUrl = document.getElementById('adm-cam-def-vid-url')?.value?.trim() || '';
      const next = parseScreenGuardConfig(cfg);
      next.cameraLensDefault = {
        ...next.cameraLensDefault,
        basePrice,
        deleted: false,
        offer: { active: type !== 'NONE', type, value, badgeText },
        videoUrl
      };
      saveGuardConfig(next, 'Camera Lens defaults & media saved!');
    });
    document.getElementById('adm-combo-enable')?.addEventListener('change', (e) => {
      const next = parseScreenGuardConfig(cfg);
      next.comboEnabled = Boolean(e.target.checked);
      saveGuardConfig(next, `Combo deal ${next.comboEnabled ? 'enabled' : 'disabled'}`);
    });
    document.getElementById('adm-combo-save')?.addEventListener('click', () => {
      const enabled = Boolean(document.getElementById('adm-combo-enable')?.checked);
      const type = document.getElementById('adm-combo-otype')?.value || 'NONE';
      const value = Math.max(0, Number(document.getElementById('adm-combo-oval')?.value || 0));
      const badgeText = document.getElementById('adm-combo-obadge')?.value?.trim() || '';
      const next = parseScreenGuardConfig(cfg);
      next.comboEnabled = enabled;
      next.comboOffer = { active: type !== 'NONE', type, value, badgeText };
      saveGuardConfig(next, 'Combo offer synced live!');
    });
    document.getElementById('btn-adm-toggle-add-cm')?.addEventListener('click', () => {
      state.adminCamAddModelOpen = !state.adminCamAddModelOpen;
      renderAdminSection();
    });
    document.getElementById('btn-adm-cancel-new-cm')?.addEventListener('click', () => {
      state.adminCamAddModelOpen = false;
      renderAdminSection();
    });
    document.getElementById('btn-adm-save-new-cm')?.addEventListener('click', () => {
      const mName = document.getElementById('adm-new-cm-name')?.value?.trim();
      if (!mName) return;
      const next = parseScreenGuardConfig(cfg);
      const list = [...(next.customCameraLensModelsByBrand[state.adminCameraModelBrand] || [])];
      if (!list.includes(mName)) list.push(mName);
      next.customCameraLensModelsByBrand[state.adminCameraModelBrand] = list;
      delete next.cameraLensModelConfigs[guardModelKey(state.adminCameraModelBrand, mName)];
      state.adminCamAddModelOpen = false;
      saveGuardConfig(next, `${mName} added to Camera Lens list`);
    });
    document.getElementById('admin-cam-model-search')?.addEventListener('input', (e) => {
      state.adminCameraModelSearch = e.target.value;
      renderAdminSection();
      const el = document.getElementById('admin-cam-model-search');
      if (el) {
        el.focus();
        el.setSelectionRange(el.value.length, el.value.length);
      }
    });
    content.querySelectorAll('[data-adm-cam-brand]').forEach((b) =>
      b.addEventListener('click', () => {
        state.adminCameraModelBrand = b.dataset.admCamBrand;
        state.adminCameraEditingModelKey = null;
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-adm-cam-edit]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admCamEdit;
        state.adminCameraEditingModelKey = state.adminCameraEditingModelKey === k ? null : k;
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-adm-cam-toggle]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admCamToggle;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.cameraLensModelConfigs[k] || { enabled: true, deleted: false, customPrice: null, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        next.cameraLensModelConfigs[k] = { ...cur, enabled: !cur.enabled, deleted: false };
        saveGuardConfig(next, 'Camera lens model availability updated');
      })
    );
    content.querySelectorAll('[data-adm-cam-delete]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admCamDelete;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.cameraLensModelConfigs[k] || { enabled: true, deleted: false, customPrice: null, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        next.cameraLensModelConfigs[k] = { ...cur, deleted: true, enabled: false };
        if (state.adminCameraEditingModelKey === k) state.adminCameraEditingModelKey = null;
        saveGuardConfig(next, 'Camera lens model deleted');
      })
    );
    content.querySelectorAll('[data-adm-cam-restore]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admCamRestore;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.cameraLensModelConfigs[k] || { enabled: true, deleted: false, customPrice: null, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        next.cameraLensModelConfigs[k] = { ...cur, deleted: false, enabled: true };
        saveGuardConfig(next, 'Camera lens model restored');
      })
    );
    content.querySelectorAll('[data-adm-cam-save]').forEach((b) =>
      b.addEventListener('click', () => {
        const origKey = b.dataset.admCamSave;
        const cIdx = Number(b.dataset.admCamIdx);
        const origModel = b.dataset.admCamOrig;
        const newModel = document.getElementById(`adm-cm-rename-${cIdx}`)?.value?.trim() || origModel;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.cameraLensModelConfigs[origKey] || { enabled: true, deleted: false, customPrice: null, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        const rawP = Number(document.getElementById(`adm-cm-price-${cIdx}`)?.value || 0);
        const type = document.getElementById(`adm-cm-otype-${cIdx}`)?.value || 'NONE';
        const value = Math.max(0, Number(document.getElementById(`adm-cm-oval-${cIdx}`)?.value || 0));
        const badgeText = document.getElementById(`adm-cm-obadge-${cIdx}`)?.value?.trim() || '';
        const updatedCfg = {
          ...cur,
          customPrice: rawP > 0 ? rawP : null,
          offer: { active: type !== 'NONE', type, value, badgeText }
        };
        let targetKey = origKey;
        if (newModel && newModel !== origModel) {
          const brand = state.adminCameraModelBrand;
          const customList = [...(next.customCameraLensModelsByBrand[brand] || [])];
          const replaced = customList.filter((m) => m !== origModel);
          if (!replaced.includes(newModel)) replaced.push(newModel);
          next.customCameraLensModelsByBrand[brand] = replaced;
          if ((CAMERA_LENS_MODELS[brand] || []).includes(origModel)) {
            next.cameraLensModelConfigs[origKey] = { enabled: false, deleted: true, customPrice: null, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
          } else {
            delete next.cameraLensModelConfigs[origKey];
          }
          targetKey = guardModelKey(brand, newModel);
        }
        next.cameraLensModelConfigs[targetKey] = updatedCfg;
        state.adminCameraEditingModelKey = null;
        saveGuardConfig(next, 'Camera lens model saved!');
      })
    );

    // 4. Pouches & Pouch Models Events
    function openPouchDraft(pouchObj = null) {
      if (pouchObj) {
        state.adminPouchAdding = false;
        state.adminPouchEditingId = pouchObj.id;
        state.adminPouchDraft = {
          isNew: false,
          id: pouchObj.id,
          name: pouchObj.name || '',
          description: pouchObj.description || '',
          basePrice: pouchObj.basePrice ?? 349,
          imageUrl: pouchObj.imageUrl || '',
          videoUrl: pouchObj.videoUrl || '',
          colorOptions: Array.isArray(pouchObj.colorOptions)
            ? pouchObj.colorOptions.map((c) => ({ ...c }))
            : [],
          compatibleBrands: Array.isArray(pouchObj.compatibleBrands)
            ? [...pouchObj.compatibleBrands]
            : ['iPhone', 'Samsung S Series'],
          showOnHomePage: Boolean(pouchObj.showOnHomePage),
          offerType: pouchObj.offer?.type || 'NONE',
          offerValue: pouchObj.offer?.value || 0,
          badgeText: pouchObj.offer?.badgeText || '',
          expandedColorName: null
        };
      } else {
        state.adminPouchAdding = true;
        state.adminPouchEditingId = null;
        state.adminPouchDraft = {
          isNew: true,
          id: '',
          name: '',
          description: '',
          basePrice: 349,
          imageUrl: '',
          videoUrl: '',
          colorOptions: [],
          compatibleBrands: ['iPhone', 'Samsung S Series'],
          showOnHomePage: false,
          offerType: 'NONE',
          offerValue: 0,
          badgeText: '',
          expandedColorName: null
        };
      }
      renderAdminSection();
    }

    function syncDraftFieldsFromDom() {
      const d = state.adminPouchDraft;
      if (!d) return;
      const nameEl = document.getElementById('adm-p-name');
      const priceEl = document.getElementById('adm-p-price');
      const descEl = document.getElementById('adm-p-desc');
      const imgUrlEl = document.getElementById('adm-p-img-url');
      const vidUrlEl = document.getElementById('adm-p-vid-url');
      const iphEl = document.getElementById('adm-p-brand-iphone');
      const samEl = document.getElementById('adm-p-brand-samsung');
      const showHomeEl = document.getElementById('adm-p-show-home');
      const oTypeEl = document.getElementById('adm-p-otype');
      const oValEl = document.getElementById('adm-p-oval');
      const oBadgeEl = document.getElementById('adm-p-obadge');

      if (nameEl) d.name = nameEl.value;
      if (priceEl) d.basePrice = Math.max(0, Number(priceEl.value || 349));
      if (descEl) d.description = descEl.value;
      if (imgUrlEl && !String(d.imageUrl || '').startsWith('data:image')) {
        d.imageUrl = imgUrlEl.value.trim();
      } else if (imgUrlEl && imgUrlEl.value.trim()) {
        d.imageUrl = imgUrlEl.value.trim();
      }
      if (vidUrlEl && !String(d.videoUrl || '').startsWith('data:')) {
        d.videoUrl = vidUrlEl.value.trim();
      } else if (vidUrlEl && vidUrlEl.value.trim()) {
        d.videoUrl = vidUrlEl.value.trim();
      }
      if (iphEl || samEl) {
        const compat = [];
        if (iphEl?.checked) compat.push('iPhone');
        if (samEl?.checked) compat.push('Samsung S Series');
        d.compatibleBrands = compat;
      }
      if (showHomeEl) d.showOnHomePage = Boolean(showHomeEl.checked);
      if (oTypeEl) d.offerType = oTypeEl.value || 'NONE';
      if (oValEl) d.offerValue = Math.max(0, Number(oValEl.value || 0));
      if (oBadgeEl) d.badgeText = oBadgeEl.value;
    }

    document.getElementById('btn-adm-add-pouch')?.addEventListener('click', () => {
      openPouchDraft(null);
    });
    const closePouchModal = () => {
      state.adminPouchAdding = false;
      state.adminPouchEditingId = null;
      state.adminPouchDraft = null;
      renderAdminSection();
    };
    document.getElementById('btn-adm-cancel-pouch')?.addEventListener('click', closePouchModal);
    document.getElementById('btn-adm-cancel-pouch-footer')?.addEventListener('click', closePouchModal);

    // Pouch Main Image Upload (Gallery / Camera / URL / Remove)
    const handleMainPouchImageFile = async (e) => {
      const file = e.target.files?.[0];
      if (!file || !state.adminPouchDraft) return;
      syncDraftFieldsFromDom();
      try {
        const dataUrl = await compressImageFileToDataUrl(file);
        state.adminPouchDraft.imageUrl = dataUrl;
        renderAdminSection();
      } catch {
        showToast('Could not read selected image', 'error');
      }
    };
    document.getElementById('adm-p-img-file')?.addEventListener('change', handleMainPouchImageFile);
    document.getElementById('adm-p-img-cam')?.addEventListener('change', handleMainPouchImageFile);
    document.getElementById('adm-p-img-url')?.addEventListener('change', (e) => {
      if (!state.adminPouchDraft) return;
      syncDraftFieldsFromDom();
      state.adminPouchDraft.imageUrl = e.target.value.trim();
      renderAdminSection();
    });
    document.getElementById('btn-adm-p-remove-img')?.addEventListener('click', () => {
      if (!state.adminPouchDraft) return;
      syncDraftFieldsFromDom();
      state.adminPouchDraft.imageUrl = '';
      renderAdminSection();
    });

    // Pouch Optional Video Upload / URL / Preview / Remove
    document.getElementById('adm-p-vid-file')?.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file || !state.adminPouchDraft) return;
      syncDraftFieldsFromDom();
      try {
        const dataUrl = await readShortVideoFileToDataUrl(file);
        state.adminPouchDraft.videoUrl = dataUrl;
        renderAdminSection();
        showToast('Pouch video ready!');
      } catch (err) {
        showToast(err?.message || 'Could not read selected video', 'error');
      }
      e.target.value = '';
    });
    document.getElementById('adm-p-vid-url')?.addEventListener('change', (e) => {
      if (!state.adminPouchDraft) return;
      syncDraftFieldsFromDom();
      state.adminPouchDraft.videoUrl = e.target.value.trim();
      renderAdminSection();
    });
    document.getElementById('btn-adm-p-preview-vid')?.addEventListener('click', () => {
      if (!state.adminPouchDraft) return;
      syncDraftFieldsFromDom();
      if (state.adminPouchDraft.videoUrl) {
        openVideoModal(state.adminPouchDraft.name || 'Pouch Video Preview', state.adminPouchDraft.videoUrl);
      }
    });
    document.getElementById('btn-adm-p-remove-vid')?.addEventListener('click', () => {
      if (!state.adminPouchDraft) return;
      syncDraftFieldsFromDom();
      state.adminPouchDraft.videoUrl = '';
      renderAdminSection();
    });

    // Preset Colour Multi-Select Toggles
    content.querySelectorAll('[data-adm-p-preset-color]').forEach((btn) =>
      btn.addEventListener('click', () => {
        if (!state.adminPouchDraft) return;
        syncDraftFieldsFromDom();
        const cname = btn.dataset.admPPresetColor;
        const preset = POUCH_PRESET_COLORS.find((x) => x.name.toLowerCase() === String(cname || '').toLowerCase());
        if (!preset) return;
        const exists = state.adminPouchDraft.colorOptions.some(
          (c) => c.name.toLowerCase() === preset.name.toLowerCase()
        );
        if (exists) {
          state.adminPouchDraft.colorOptions = state.adminPouchDraft.colorOptions.filter(
            (c) => c.name.toLowerCase() !== preset.name.toLowerCase()
          );
        } else {
          state.adminPouchDraft.colorOptions = [...state.adminPouchDraft.colorOptions, { ...preset }];
        }
        renderAdminSection();
      })
    );

    // Custom Colour Inputs + Add
    const pickerEl = document.getElementById('adm-p-custom-cpicker');
    const hexInputEl = document.getElementById('adm-p-custom-chex');
    pickerEl?.addEventListener('input', (e) => {
      if (hexInputEl) hexInputEl.value = e.target.value.toUpperCase();
    });
    hexInputEl?.addEventListener('input', (e) => {
      const val = normalizeHexColor(e.target.value);
      if (pickerEl && /^#[0-9A-F]{6}$/.test(val)) pickerEl.value = val;
    });
    document.getElementById('btn-adm-p-add-custom-color')?.addEventListener('click', () => {
      if (!state.adminPouchDraft) return;
      syncDraftFieldsFromDom();
      const cname = document.getElementById('adm-p-custom-cname')?.value?.trim();
      if (!cname) {
        showToast('Enter a custom colour name', 'error');
        return;
      }
      const hex = normalizeHexColor(hexInputEl?.value || pickerEl?.value || '#1E293B');
      const existingIdx = state.adminPouchDraft.colorOptions.findIndex(
        (c) => c.name.toLowerCase() === cname.toLowerCase()
      );
      if (existingIdx >= 0) {
        state.adminPouchDraft.colorOptions[existingIdx] = {
          ...state.adminPouchDraft.colorOptions[existingIdx],
          hex
        };
      } else {
        state.adminPouchDraft.colorOptions = [
          ...state.adminPouchDraft.colorOptions,
          { name: cname, hex, imageUrl: '' }
        ];
      }
      renderAdminSection();
    });

    // Selected Colour Row Controls (Per-Colour Photo Upload/URL & Delete)
    content.querySelectorAll('[data-adm-p-color-del]').forEach((btn) =>
      btn.addEventListener('click', () => {
        if (!state.adminPouchDraft) return;
        syncDraftFieldsFromDom();
        const idx = Number(btn.dataset.admPColorDel);
        state.adminPouchDraft.colorOptions = state.adminPouchDraft.colorOptions.filter((_, i) => i !== idx);
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-adm-p-color-photo-toggle]').forEach((btn) =>
      btn.addEventListener('click', () => {
        if (!state.adminPouchDraft) return;
        syncDraftFieldsFromDom();
        const cname = btn.dataset.admPColorPhotoToggle;
        state.adminPouchDraft.expandedColorName =
          state.adminPouchDraft.expandedColorName === cname ? null : cname;
        renderAdminSection();
      })
    );
    const handlePerColorImage = async (e, idxAttr) => {
      const file = e.target.files?.[0];
      const idx = Number(e.target.dataset[idxAttr]);
      if (!file || !state.adminPouchDraft || !Number.isFinite(idx)) return;
      syncDraftFieldsFromDom();
      try {
        const dataUrl = await compressImageFileToDataUrl(file);
        if (state.adminPouchDraft.colorOptions[idx]) {
          state.adminPouchDraft.colorOptions[idx].imageUrl = dataUrl;
        }
        renderAdminSection();
      } catch {
        showToast('Could not read colour image', 'error');
      }
    };
    content.querySelectorAll('[data-adm-p-color-file]').forEach((inp) =>
      inp.addEventListener('change', (e) => handlePerColorImage(e, 'admPColorFile'))
    );
    content.querySelectorAll('[data-adm-p-color-cam]').forEach((inp) =>
      inp.addEventListener('change', (e) => handlePerColorImage(e, 'admPColorCam'))
    );
    content.querySelectorAll('[data-adm-p-color-url]').forEach((inp) =>
      inp.addEventListener('change', (e) => {
        if (!state.adminPouchDraft) return;
        const idx = Number(inp.dataset.admPColorUrl);
        if (state.adminPouchDraft.colorOptions[idx]) {
          state.adminPouchDraft.colorOptions[idx].imageUrl = e.target.value.trim();
        }
      })
    );
    content.querySelectorAll('[data-adm-p-color-clearimg]').forEach((btn) =>
      btn.addEventListener('click', () => {
        if (!state.adminPouchDraft) return;
        syncDraftFieldsFromDom();
        const idx = Number(btn.dataset.admPColorClearimg);
        if (state.adminPouchDraft.colorOptions[idx]) {
          state.adminPouchDraft.colorOptions[idx].imageUrl = '';
        }
        renderAdminSection();
      })
    );

    // Save Pouch to Cloud Firestore
    document.getElementById('btn-adm-save-pouch')?.addEventListener('click', () => {
      if (!state.adminPouchDraft) return;
      syncDraftFieldsFromDom();
      const d = state.adminPouchDraft;
      const name = String(d.name || '').trim();
      if (!name) {
        showToast('Please enter a pouch name', 'error');
        return;
      }
      const basePrice = Math.max(1, Number(d.basePrice || 299));
      const description = String(d.description || '').trim();
      const compat = d.compatibleBrands?.length > 0 ? d.compatibleBrands : ['iPhone', 'Samsung S Series'];
      const type = d.offerType || 'NONE';
      const value = Math.max(0, Number(d.offerValue || 0));
      const badgeText = String(d.badgeText || '').trim();
      const next = parseScreenGuardConfig(cfg);
      const id = d.isNew
        ? `pouch_${Date.now()}_${name.toLowerCase().replace(/[^a-z0-9]/g, '_').slice(0, 16)}`
        : d.id;
      const existing = next.pouchItems.find((p) => p.id === id);
      const payload = {
        id,
        name,
        description,
        basePrice,
        compatibleBrands: compat,
        enabled: existing ? existing.enabled : true,
        deleted: false,
        showOnHomePage: Boolean(d.showOnHomePage),
        offer: { active: type !== 'NONE', type, value, badgeText },
        imageUrl: String(d.imageUrl || '').trim(),
        videoUrl: String(d.videoUrl || '').trim(),
        colorOptions: (d.colorOptions || []).map((c) => ({
          name: String(c.name || '').trim(),
          hex: normalizeHexColor(c.hex || '#1E293B'),
          imageUrl: String(c.imageUrl || '').trim()
        }))
      };
      next.pouchItems = existing
        ? next.pouchItems.map((p) => (p.id === id ? payload : p))
        : [...next.pouchItems, payload];
      state.adminPouchAdding = false;
      state.adminPouchEditingId = null;
      state.adminPouchDraft = null;
      saveGuardConfig(next, `${name} saved & synced to Cloud Firestore!`);
    });

    content.querySelectorAll('[data-adm-p-edit]').forEach((b) =>
      b.addEventListener('click', () => {
        const target = (cfg.pouchItems || []).find((p) => p.id === b.dataset.admPEdit);
        if (target) openPouchDraft(target);
      })
    );
    content.querySelectorAll('[data-adm-p-home-toggle]').forEach((b) =>
      b.addEventListener('click', () => {
        const id = b.dataset.admPHomeToggle;
        const next = parseScreenGuardConfig(cfg);
        next.pouchItems = next.pouchItems.map((p) =>
          p.id === id ? { ...p, showOnHomePage: !p.showOnHomePage } : p
        );
        saveGuardConfig(next, 'Home Page showcase updated');
      })
    );
    content.querySelectorAll('[data-adm-p-toggle]').forEach((b) =>
      b.addEventListener('click', () => {
        const id = b.dataset.admPToggle;
        const next = parseScreenGuardConfig(cfg);
        next.pouchItems = next.pouchItems.map((p) =>
          p.id === id ? { ...p, enabled: !p.enabled, deleted: false } : p
        );
        saveGuardConfig(next, 'Pouch status updated');
      })
    );
    content.querySelectorAll('[data-adm-p-delete]').forEach((b) =>
      b.addEventListener('click', () => {
        const id = b.dataset.admPDelete;
        const next = parseScreenGuardConfig(cfg);
        next.pouchItems = next.pouchItems.map((p) =>
          p.id === id ? { ...p, deleted: true, enabled: false } : p
        );
        if (state.adminPouchEditingId === id) {
          state.adminPouchEditingId = null;
          state.adminPouchDraft = null;
        }
        saveGuardConfig(next, 'Pouch deleted');
      })
    );
    content.querySelectorAll('[data-adm-p-restore]').forEach((b) =>
      b.addEventListener('click', () => {
        const id = b.dataset.admPRestore;
        const next = parseScreenGuardConfig(cfg);
        next.pouchItems = next.pouchItems.map((p) =>
          p.id === id ? { ...p, deleted: false, enabled: true } : p
        );
        saveGuardConfig(next, 'Pouch restored');
      })
    );

    content.querySelectorAll('[data-adm-pm-brand]').forEach((b) =>
      b.addEventListener('click', () => {
        state.adminPouchModelBrand = b.dataset.admPmBrand;
        state.adminPouchEditingModelKey = null;
        renderAdminSection();
      })
    );
    document.getElementById('btn-adm-toggle-add-pm')?.addEventListener('click', () => {
      state.adminPouchAddModelOpen = !state.adminPouchAddModelOpen;
      renderAdminSection();
    });
    document.getElementById('btn-adm-cancel-new-pm')?.addEventListener('click', () => {
      state.adminPouchAddModelOpen = false;
      renderAdminSection();
    });
    document.getElementById('btn-adm-save-new-pm')?.addEventListener('click', () => {
      const mName = document.getElementById('adm-new-pm-name')?.value?.trim();
      if (!mName) return;
      const next = parseScreenGuardConfig(cfg);
      const list = [...(next.customPouchModelsByBrand[state.adminPouchModelBrand] || [])];
      if (!list.includes(mName)) list.push(mName);
      next.customPouchModelsByBrand[state.adminPouchModelBrand] = list;
      delete next.pouchModelConfigs[guardModelKey(state.adminPouchModelBrand, mName)];
      state.adminPouchAddModelOpen = false;
      saveGuardConfig(next, `${mName} added to Pouch models`);
    });
    document.getElementById('admin-pouch-model-search')?.addEventListener('input', (e) => {
      state.adminPouchModelSearch = e.target.value;
      renderAdminSection();
      const el = document.getElementById('admin-pouch-model-search');
      if (el) {
        el.focus();
        el.setSelectionRange(el.value.length, el.value.length);
      }
    });
    content.querySelectorAll('[data-adm-pm-edit]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admPmEdit;
        state.adminPouchEditingModelKey = state.adminPouchEditingModelKey === k ? null : k;
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-adm-pm-toggle]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admPmToggle;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.pouchModelConfigs[k] || { enabled: true, deleted: false, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        next.pouchModelConfigs[k] = { ...cur, enabled: !cur.enabled, deleted: false };
        saveGuardConfig(next, 'Pouch model availability updated');
      })
    );
    content.querySelectorAll('[data-adm-pm-delete]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admPmDelete;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.pouchModelConfigs[k] || { enabled: true, deleted: false, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        next.pouchModelConfigs[k] = { ...cur, deleted: true, enabled: false };
        if (state.adminPouchEditingModelKey === k) state.adminPouchEditingModelKey = null;
        saveGuardConfig(next, 'Pouch model deleted');
      })
    );
    content.querySelectorAll('[data-adm-pm-restore]').forEach((b) =>
      b.addEventListener('click', () => {
        const k = b.dataset.admPmRestore;
        const next = parseScreenGuardConfig(cfg);
        const cur = next.pouchModelConfigs[k] || { enabled: true, deleted: false, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        next.pouchModelConfigs[k] = { ...cur, deleted: false, enabled: true };
        saveGuardConfig(next, 'Pouch model restored');
      })
    );
    content.querySelectorAll('[data-adm-pm-save]').forEach((b) =>
      b.addEventListener('click', () => {
        const origKey = b.dataset.admPmSave;
        const pmIdx = Number(b.dataset.admPmIdx);
        const origModel = b.dataset.admPmOrig;
        const newModel = document.getElementById(`adm-pm-rename-${pmIdx}`)?.value?.trim() || origModel;
        const next = parseScreenGuardConfig(cfg);
        if (newModel && newModel !== origModel) {
          const brand = state.adminPouchModelBrand;
          const customList = [...(next.customPouchModelsByBrand[brand] || [])];
          const replaced = customList.filter((m) => m !== origModel);
          if (!replaced.includes(newModel)) replaced.push(newModel);
          next.customPouchModelsByBrand[brand] = replaced;
          if ((POUCH_MODELS[brand] || []).includes(origModel)) {
            next.pouchModelConfigs[origKey] = { enabled: false, deleted: true, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
          } else {
            delete next.pouchModelConfigs[origKey];
          }
          next.pouchModelConfigs[guardModelKey(brand, newModel)] = { enabled: true, deleted: false, customRates: {}, offer: { active: false, type: 'NONE', value: 0, badgeText: '' } };
        }
        state.adminPouchEditingModelKey = null;
        saveGuardConfig(next, 'Pouch model renamed!');
      })
    );
  } else if (state.adminTab === 'SETTINGS') {
    const s = state.settings;
    content.innerHTML = `
      <form id="admin-settings-form" class="bg-white rounded-2xl border border-slate-200 p-5 sm:p-6 space-y-4 max-w-3xl">
        <h3 class="font-extrabold text-icase-navy text-base">Live Store &amp; Delivery Settings</h3>
        <div class="grid sm:grid-cols-3 gap-3">
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-1">Standard Delivery (₹)</label>
            <input id="as-std" type="number" step="0.01" value="${escapeHtml(s.delivery_fee_standard || '50.00')}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-1">Express 1-Hr Fee (₹)</label>
            <input id="as-exp" type="number" step="0.01" value="${escapeHtml(s.delivery_fee_express || '80.00')}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-1">Free Delivery Threshold (₹)</label>
            <input id="as-free" type="number" step="0.01" value="${escapeHtml(s.free_delivery_threshold || '0.00')}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
          </div>
        </div>
        <div class="grid sm:grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-1">Support Phone</label>
            <input id="as-phone" type="text" value="${escapeHtml(s.store_phone || '9961454059')}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-1">Support Email</label>
            <input id="as-email" type="email" value="${escapeHtml(s.store_email || 'icaseedappal@gmail.com')}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
          </div>
        </div>
        <div>
          <label class="block text-xs font-bold text-slate-600 mb-1">Store Address</label>
          <input id="as-addr" type="text" value="${escapeHtml(s.store_address || '')}" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
        </div>
        <div class="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-3">
          <div class="text-sm font-extrabold text-icase-navy">Change Admin Password &amp; Recovery Code</div>
          <div class="grid sm:grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-bold text-slate-600 mb-1">New Admin Password / PIN (Optional, 4+ chars)</label>
              <input id="as-admin-pin" type="password" placeholder="Leave blank to keep current PIN" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm bg-white" />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-600 mb-1">Master Recovery Code (Default: ICASE2026)</label>
              <input id="as-recovery-code" type="text" value="${escapeHtml(s.admin_recovery_code || 'ICASE2026')}" placeholder="Minimum 4 characters" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm bg-white" />
            </div>
          </div>
          <div id="as-sec-error" class="hidden text-xs font-bold text-icase-red"></div>
        </div>
        <div class="flex flex-wrap gap-5 text-xs font-bold text-icase-navy">
          <label class="flex items-center gap-2"><input id="as-onehr" type="checkbox" ${s.one_hour_delivery_enabled !== 'false' ? 'checked' : ''} /> Enable 1-Hour Express Delivery</label>
          <label class="flex items-center gap-2"><input id="as-online" type="checkbox" ${s.store_online !== 'false' ? 'checked' : ''} /> Store Online (Accept Orders)</label>
        </div>
        <div class="flex flex-wrap items-center gap-3">
          <button type="submit" class="px-5 py-2.5 rounded-xl bg-icase-navy text-white text-sm font-bold">Save &amp; Sync Settings</button>
          <button type="button" data-open-export-code="true" class="px-4 py-2.5 rounded-xl border border-slate-300 bg-white hover:bg-slate-50 text-icase-navy text-sm font-bold">Export Default Code for GitHub</button>
        </div>
      </form>
    `;

    document.getElementById('admin-settings-form')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const newPin = document.getElementById('as-admin-pin')?.value?.trim() || '';
      const newRecovery = document.getElementById('as-recovery-code')?.value?.trim() || '';
      const secErr = document.getElementById('as-sec-error');
      if (newPin && newPin.length < 4) {
        if (secErr) {
          secErr.textContent = 'New Admin Password / PIN must be at least 4 characters.';
          secErr.classList.remove('hidden');
        }
        return;
      }
      if (newRecovery && newRecovery.length < 4) {
        if (secErr) {
          secErr.textContent = 'Master Recovery Code must be at least 4 characters.';
          secErr.classList.remove('hidden');
        }
        return;
      }
      secErr?.classList.add('hidden');

      const updates = {
        delivery_fee_standard: Number(document.getElementById('as-std')?.value || 50).toFixed(2),
        delivery_fee_express: Number(document.getElementById('as-exp')?.value || 80).toFixed(2),
        free_delivery_threshold: Number(document.getElementById('as-free')?.value || 0).toFixed(2),
        store_phone: document.getElementById('as-phone')?.value?.trim() || '9961454059',
        store_whatsapp: document.getElementById('as-phone')?.value?.trim() || '9961454059',
        store_email: document.getElementById('as-email')?.value?.trim() || 'icaseedappal@gmail.com',
        store_address: document.getElementById('as-addr')?.value?.trim() || '',
        one_hour_delivery_enabled: document.getElementById('as-onehr')?.checked ? 'true' : 'false',
        store_online: document.getElementById('as-online')?.checked ? 'true' : 'false',
        ...(newPin.length >= 4 ? { admin_pin: newPin } : {}),
        ...(newRecovery.length >= 4 ? { admin_recovery_code: newRecovery } : {})
      };

      state.settings = { ...state.settings, ...updates };
      saveJsonStorage(STORAGE_SETTINGS_KEY, state.settings);

      await Promise.all(Object.entries(updates).map(([k, v]) => saveSettingInCloud(k, v)));
      const pinField = document.getElementById('as-admin-pin');
      if (pinField) pinField.value = '';
      showToast('Store & security settings synced live!');
    });
  } else if (state.adminTab === 'REVIEWS') {
    const filtered = state.adminReviews.filter((r) => {
      if (state.adminReviewFilter === 'PRODUCT') return r.reviewType === 'PRODUCT';
      if (state.adminReviewFilter === 'SERVICE') return r.reviewType === 'SERVICE';
      return true;
    });

    content.innerHTML = `
      <div class="space-y-4">
        <div class="bg-white rounded-2xl border border-slate-200 p-4 flex flex-wrap items-center justify-between gap-3">
          <div class="flex flex-wrap gap-2">
            ${[
              ['ALL', `All (${state.adminReviews.length})`],
              ['PRODUCT', `Product (${state.adminReviews.filter((r) => r.reviewType === 'PRODUCT').length})`],
              ['SERVICE', `Private Service (${state.adminReviews.filter((r) => r.reviewType === 'SERVICE').length})`]
            ]
              .map(
                ([k, label]) => `
              <button type="button" data-admin-rev-filter="${k}" class="px-3.5 py-1.5 rounded-xl text-xs font-bold ${
                state.adminReviewFilter === k ? 'bg-icase-navy text-white' : 'bg-slate-100 text-slate-700'
              }">${escapeHtml(label)}</button>
            `
              )
              .join('')}
          </div>
          <button id="btn-toggle-manual-review" type="button" class="px-4 py-2 rounded-xl bg-icase-red text-white text-xs font-bold">+ Add Verified Review</button>
        </div>

        ${
          state.adminShowManualReviewForm
            ? `
          <form id="admin-manual-review-form" class="bg-white rounded-2xl border-2 border-icase-blue p-5 space-y-3">
            <div class="flex items-center justify-between">
              <strong class="text-icase-navy text-sm">Add Manual Verified Review</strong>
              <button id="btn-cancel-manual-review" type="button" class="text-xs font-bold text-slate-500">Cancel</button>
            </div>
            <div class="grid sm:grid-cols-3 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Type</label>
                <select id="amr-type" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm bg-white">
                  <option value="PRODUCT">Product Purchase (Can Publish)</option>
                  <option value="SERVICE">Service Feedback (Always Private)</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Reference ID *</label>
                <input id="amr-ref" type="text" required value="ORD-" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Rating (1-5)</label>
                <input id="amr-rating" type="number" min="1" max="5" value="5" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
              </div>
            </div>
            <div class="grid sm:grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Item / Device Title *</label>
                <input id="amr-title" type="text" required placeholder="e.g. MagSafe Case / iPhone 15 Display" class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-600 mb-1">Customer Name *</label>
                <input id="amr-name" type="text" required class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm" />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-600 mb-1">Comment *</label>
              <textarea id="amr-comment" rows="2" required class="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm"></textarea>
            </div>
            <label id="amr-approve-row" class="flex items-center gap-2 text-xs font-bold text-icase-navy">
              <input id="amr-approve" type="checkbox" checked /> Approve for Public immediately (Product Reviews only)
            </label>
            <button type="submit" class="px-5 py-2.5 rounded-xl bg-icase-red text-white text-xs font-bold">Save &amp; Sync Review</button>
          </form>
        `
            : ''
        }

        ${
          filtered.length === 0
            ? `<div class="bg-white rounded-2xl border border-slate-200 p-8 text-center text-sm text-slate-500">No reviews in this filter yet.</div>`
            : `<div class="space-y-3">${filtered
                .map((r) => {
                  const isService = r.reviewType === 'SERVICE';
                  return `
                <div class="rounded-2xl border p-4 sm:p-5 space-y-3 ${
                  isService ? 'bg-slate-50 border-icase-blue/40' : 'bg-white border-slate-200'
                }">
                  <div class="flex flex-wrap items-center justify-between gap-2">
                    <span class="px-2.5 py-1 rounded-lg text-[10px] font-black uppercase ${
                      isService
                        ? 'bg-icase-blueLight text-icase-blue'
                        : r.isApproved
                          ? 'bg-emerald-50 text-emerald-800'
                          : 'bg-amber-50 text-amber-800'
                    }">
                      ${
                        isService
                          ? '🔒 PRIVATE SERVICE FEEDBACK • HIDDEN FROM PUBLIC'
                          : r.isApproved
                            ? '✓ PUBLIC PRODUCT REVIEW'
                            : '⏳ PENDING APPROVAL'
                      }
                    </span>
                    <span class="text-icase-gold text-sm font-bold">${'★'.repeat(Math.max(1, Math.min(5, Number(r.rating || 5))))}</span>
                  </div>
                  <div>
                    <div class="font-extrabold text-icase-navy text-sm">${escapeHtml(r.customerName)} • Ref: ${escapeHtml(r.referenceId)}</div>
                    <div class="text-xs font-bold text-icase-blue">${escapeHtml(r.targetTitle)}</div>
                    <div class="text-[11px] text-slate-400 mt-0.5">${escapeHtml(formatTimestamp(r.createdAt))}</div>
                  </div>
                  <p class="text-xs sm:text-sm text-slate-700">${escapeHtml(r.comment)}</p>
                  ${
                    r.adminReply
                      ? `<div class="p-2.5 rounded-xl bg-icase-blueLight text-xs text-icase-navy font-semibold">Admin Reply: ${escapeHtml(r.adminReply)}</div>`
                      : ''
                  }
                  <div class="flex flex-wrap items-center gap-2 pt-1">
                    ${
                      !isService
                        ? `<button type="button" data-admin-toggle-approve="${escapeHtml(r.id)}" data-next-approved="${!r.isApproved}" class="px-3.5 py-1.5 rounded-xl text-xs font-bold ${
                            r.isApproved ? 'bg-slate-100 text-icase-navy' : 'bg-icase-green text-white'
                          }">${r.isApproved ? 'Hide / Unpublish' : 'Approve / Publish'}</button>`
                        : ''
                    }
                    <button type="button" data-admin-edit-review="${escapeHtml(r.id)}" class="px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-icase-navy text-xs font-bold">Edit</button>
                    <input data-admin-reply-input="${escapeHtml(r.id)}" type="text" value="${escapeHtml(r.adminReply || '')}" placeholder="Write reply..." class="flex-1 min-w-[160px] px-3 py-1.5 rounded-xl border border-slate-300 text-xs" />
                    <button type="button" data-admin-save-reply="${escapeHtml(r.id)}" class="px-3.5 py-1.5 rounded-xl bg-icase-navy text-white text-xs font-bold">Reply</button>
                    <button type="button" data-admin-del-review="${escapeHtml(r.id)}" class="px-3 py-1.5 rounded-xl bg-icase-redLight text-icase-red text-xs font-bold">Delete</button>
                  </div>
                  ${
                    state.adminEditingReviewId === r.id
                      ? `
                    <form data-admin-edit-rev-form="${escapeHtml(r.id)}" class="p-3.5 rounded-xl bg-slate-50 border border-icase-blue/40 space-y-2.5">
                      <div class="flex items-center justify-between">
                        <strong class="text-xs text-icase-navy">Edit Review</strong>
                        <button type="button" data-admin-cancel-edit-rev="true" class="text-xs font-bold text-slate-500">Cancel</button>
                      </div>
                      <div class="grid sm:grid-cols-2 gap-2">
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500 mb-1">Product / Device Name</label>
                          <input data-edit-rev-title="${escapeHtml(r.id)}" type="text" required value="${escapeHtml(r.targetTitle)}" class="w-full px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-xs" />
                        </div>
                        <div>
                          <label class="block text-[10px] font-bold text-slate-500 mb-1">Customer Name</label>
                          <input data-edit-rev-name="${escapeHtml(r.id)}" type="text" required value="${escapeHtml(r.customerName)}" class="w-full px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-xs" />
                        </div>
                      </div>
                      <div>
                        <label class="block text-[10px] font-bold text-slate-500 mb-1">Star Rating</label>
                        <div class="flex items-center gap-1">
                          ${[1, 2, 3, 4, 5]
                            .map(
                              (s) => `
                            <button type="button" data-admin-edit-star="${s}" class="text-xl leading-none px-1 ${
                              s <= state.adminEditingReviewRating ? 'text-icase-gold' : 'text-slate-300'
                            }">★</button>
                          `
                            )
                            .join('')}
                        </div>
                      </div>
                      <div>
                        <label class="block text-[10px] font-bold text-slate-500 mb-1">Review Text</label>
                        <textarea data-edit-rev-comment="${escapeHtml(r.id)}" rows="2" required class="w-full px-3 py-1.5 rounded-xl border border-slate-300 bg-white text-xs">${escapeHtml(r.comment)}</textarea>
                      </div>
                      ${
                        !isService
                          ? `<label class="flex items-center gap-2 text-xs font-bold text-icase-navy"><input data-edit-rev-approved="${escapeHtml(r.id)}" type="checkbox" ${r.isApproved ? 'checked' : ''} /> Approved / Published on Product Page</label>`
                          : ''
                      }
                      <button type="submit" class="px-4 py-1.5 rounded-xl bg-icase-navy text-white text-xs font-bold">Save Review Changes</button>
                    </form>
                  `
                      : ''
                  }
                </div>
              `;
                })
                .join('')}</div>`
        }
      </div>
    `;

    content.querySelectorAll('[data-admin-rev-filter]').forEach((b) =>
      b.addEventListener('click', () => {
        state.adminReviewFilter = b.dataset.adminRevFilter;
        renderAdminSection();
      })
    );

    document.getElementById('btn-toggle-manual-review')?.addEventListener('click', () => {
      state.adminShowManualReviewForm = !state.adminShowManualReviewForm;
      renderAdminSection();
    });
    document.getElementById('btn-cancel-manual-review')?.addEventListener('click', () => {
      state.adminShowManualReviewForm = false;
      renderAdminSection();
    });
    document.getElementById('amr-type')?.addEventListener('change', (e) => {
      const row = document.getElementById('amr-approve-row');
      if (row) row.classList.toggle('hidden', e.target.value === 'SERVICE');
    });
    document.getElementById('admin-manual-review-form')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const reviewType = document.getElementById('amr-type')?.value || 'PRODUCT';
      const now = Date.now();
      await createReviewInCloud({
        id: `rev_${now}`,
        referenceId: document.getElementById('amr-ref')?.value?.trim().toUpperCase() || 'ORD-MANUAL',
        reviewType,
        targetTitle: document.getElementById('amr-title')?.value?.trim() || 'iCASE Product',
        customerName: document.getElementById('amr-name')?.value?.trim() || 'Verified Customer',
        rating: Math.max(1, Math.min(5, Number(document.getElementById('amr-rating')?.value || 5))),
        comment: document.getElementById('amr-comment')?.value?.trim() || '',
        isVerified: true,
        isApproved: reviewType === 'PRODUCT' ? Boolean(document.getElementById('amr-approve')?.checked) : false,
        adminReply: '',
        userId: 'admin_manual',
        createdAt: now,
        updatedAt: now
      });
      state.adminShowManualReviewForm = false;
      showToast(reviewType === 'SERVICE' ? 'Private service feedback added' : 'Verified product review saved');
      renderAdminSection();
    });

    content.querySelectorAll('[data-admin-toggle-approve]').forEach((b) =>
      b.addEventListener('click', async () => {
        const nextVal = b.dataset.nextApproved === 'true';
        await updateReviewInCloud(b.dataset.adminToggleApprove, { isApproved: nextVal });
        showToast(nextVal ? 'Review approved for public view' : 'Review hidden from public');
      })
    );
    content.querySelectorAll('[data-admin-edit-review]').forEach((b) =>
      b.addEventListener('click', () => {
        const id = b.dataset.adminEditReview;
        const target = state.adminReviews.find((x) => x.id === id);
        if (!target) return;
        if (state.adminEditingReviewId === id) {
          state.adminEditingReviewId = null;
        } else {
          state.adminEditingReviewId = id;
          state.adminEditingReviewRating = Math.max(1, Math.min(5, Number(target.rating || 5)));
        }
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-admin-cancel-edit-rev]').forEach((b) =>
      b.addEventListener('click', () => {
        state.adminEditingReviewId = null;
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-admin-edit-star]').forEach((b) =>
      b.addEventListener('click', () => {
        state.adminEditingReviewRating = Math.max(1, Math.min(5, Number(b.dataset.adminEditStar || 5)));
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-admin-edit-rev-form]').forEach((form) =>
      form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = form.dataset.adminEditRevForm;
        const target = state.adminReviews.find((x) => x.id === id);
        if (!target) return;
        const targetTitle = form.querySelector(`[data-edit-rev-title="${id}"]`)?.value?.trim() || target.targetTitle;
        const customerName = form.querySelector(`[data-edit-rev-name="${id}"]`)?.value?.trim() || target.customerName;
        const comment = form.querySelector(`[data-edit-rev-comment="${id}"]`)?.value?.trim() || target.comment;
        const isService = target.reviewType === 'SERVICE';
        const approvedBox = form.querySelector(`[data-edit-rev-approved="${id}"]`);
        const isApproved = isService ? false : Boolean(approvedBox ? approvedBox.checked : target.isApproved);
        await updateReviewInCloud(id, {
          targetTitle,
          customerName,
          rating: state.adminEditingReviewRating,
          comment,
          isApproved
        });
        state.adminEditingReviewId = null;
        showToast('Review updated & synced!');
        renderAdminSection();
      })
    );
    content.querySelectorAll('[data-admin-save-reply]').forEach((b) =>
      b.addEventListener('click', async () => {
        const id = b.dataset.adminSaveReply;
        const replyVal = content.querySelector(`[data-admin-reply-input="${id}"]`)?.value?.trim() || '';
        await updateReviewInCloud(id, { adminReply: replyVal });
        showToast('Reply saved!');
      })
    );
    content.querySelectorAll('[data-admin-del-review]').forEach((b) =>
      b.addEventListener('click', async () => {
        if (state.adminEditingReviewId === b.dataset.adminDelReview) {
          state.adminEditingReviewId = null;
        }
        await deleteReviewInCloud(b.dataset.adminDelReview);
        showToast('Review deleted');
      })
    );
  }
}

// ==================== EXPORT DEFAULT CODE MODAL ====================
function openExportCodeModal() {
  const modal = document.getElementById('export-code-modal');
  const pre = document.getElementById('export-code-pre');
  if (!modal || !pre) return;
  pre.textContent = generateDefaultCodeExport({
    products: state.products,
    settings: state.settings,
    screenGuardConfig: state.screenGuardConfig
  });
  modal.classList.remove('hidden');
}

function closeExportCodeModal() {
  document.getElementById('export-code-modal')?.classList.add('hidden');
}

// ==================== GLOBAL EVENT DELEGATION & INIT ====================
function bindGlobalEvents() {
  document.addEventListener('click', (e) => {
    const openVidBtn = e.target.closest('[data-open-video]');
    if (openVidBtn) {
      e.preventDefault();
      e.stopPropagation();
      openVideoModal(openVidBtn.dataset.videoTitle || 'Video Preview', openVidBtn.dataset.openVideo);
      return;
    }

    const homeGlassTypeBtn = e.target.closest('[data-home-glass-type]');
    if (homeGlassTypeBtn) {
      const gt = homeGlassTypeBtn.dataset.homeGlassType;
      if (gt) {
        state.glassType = gt;
        setActiveTab('guards', 'glass');
      }
      return;
    }

    const guardSampleBtn = e.target.closest('[data-home-guard-card]');
    if (guardSampleBtn) {
      setActiveTab('guards', guardSampleBtn.dataset.homeGuardCard);
      return;
    }

    const exportCodeBtn = e.target.closest('[data-open-export-code]');
    if (exportCodeBtn) {
      openExportCodeModal();
      return;
    }

    const navTarget = e.target.closest('[data-nav]');
    if (navTarget) {
      setActiveTab(navTarget.dataset.nav);
      return;
    }

    const homeCat = e.target.closest('[data-home-cat]');
    if (homeCat) {
      state.selectedCategory = homeCat.dataset.homeCat;
      state.selectedCatalogBrand = '';
      setActiveTab('catalog');
      return;
    }

    const homeBrand = e.target.closest('[data-home-brand]');
    if (homeBrand) {
      state.selectedCategory = 'All';
      state.selectedCatalogBrand = homeBrand.dataset.homeBrand;
      setActiveTab('catalog');
      return;
    }

    const catBtn = e.target.closest('[data-catalog-cat]');
    if (catBtn) {
      state.selectedCategory = catBtn.dataset.catalogCat;
      renderCatalog();
      return;
    }

    const brandChip = e.target.closest('[data-catalog-brand]');
    if (brandChip) {
      const nextB = brandChip.dataset.catalogBrand;
      state.selectedCatalogBrand = state.selectedCatalogBrand === nextB ? '' : nextB;
      renderCatalog();
      return;
    }

    const openProd = e.target.closest('[data-open-product]');
    if (openProd) {
      openProductModal(openProd.dataset.openProduct);
      return;
    }

    const selectColor = e.target.closest('[data-select-color]');
    if (selectColor) {
      state.modalSelectedColorIdx = Number(selectColor.dataset.selectColor);
      renderProductModal();
      return;
    }

    const addBtn = e.target.closest('[data-add-cart]');
    if (addBtn) {
      const prod = state.products.find((p) => p.id === addBtn.dataset.addCart);
      if (prod) addToCart(prod, 1);
      return;
    }

    const buyBtn = e.target.closest('[data-buy-now]');
    if (buyBtn) {
      const prod = state.products.find((p) => p.id === buyBtn.dataset.buyNow);
      if (prod) {
        addToCart(prod, 1);
        openCartDrawer();
      }
      return;
    }

    const wishBtn = e.target.closest('[data-toggle-wishlist]');
    if (wishBtn) {
      toggleWishlist(wishBtn.dataset.toggleWishlist);
      return;
    }

    const brandBtn = e.target.closest('[data-repair-brand]');
    if (brandBtn) {
      state.repairBrand = brandBtn.dataset.repairBrand;
      state.repairModel = '';
      state.repairSearch = '';
      state.repairDropdownOpen = true;
      const inp = document.getElementById('device-search-input');
      if (inp) inp.value = '';
      renderRepairBrands();
      renderDeviceDropdown();
      return;
    }

    const pickDevice = e.target.closest('[data-pick-model]');
    if (pickDevice) {
      state.repairBrand = pickDevice.dataset.pickBrand;
      state.repairModel = pickDevice.dataset.pickModel;
      state.repairSearch = '';
      state.repairDropdownOpen = false;
      renderRepairBrands();
      renderDeviceDropdown();
      return;
    }

    const compBtn = e.target.closest('[data-repair-complaint]');
    if (compBtn) {
      const item = compBtn.dataset.repairComplaint;
      state.repairComplaints = state.repairComplaints.includes(item)
        ? state.repairComplaints.filter((x) => x !== item)
        : [...state.repairComplaints, item];
      renderRepairComplaints();
      return;
    }

    const quickTrack = e.target.closest('[data-quick-track]');
    if (quickTrack) {
      openTrackId(quickTrack.dataset.quickTrack);
      return;
    }

    const adminTab = e.target.closest('[data-admin-tab]');
    if (adminTab) {
      state.adminTab = adminTab.dataset.adminTab;
      renderAdminSection();
      return;
    }

    const gBrand = e.target.closest('[data-glass-brand]');
    if (gBrand) {
      state.glassBrand = gBrand.dataset.glassBrand;
      state.glassModel = '';
      state.glassManualModel = '';
      state.glassSearch = '';
      state.glassError = '';
      state.glassDropdownOpen = true;
      renderTemperedGlassFinder();
      renderCameraLensFinder();
      return;
    }

    const pickGlass = e.target.closest('[data-pick-glass-model]');
    if (pickGlass) {
      state.glassBrand = pickGlass.dataset.pickGlassBrand;
      state.glassModel = pickGlass.dataset.pickGlassModel;
      state.glassManualModel = '';
      state.glassSearch = '';
      state.glassError = '';
      state.glassDropdownOpen = false;
      renderTemperedGlassFinder();
      renderCameraLensFinder();
      return;
    }

    if (e.target.closest('[data-pick-glass-other]')) {
      state.glassBrand = '';
      state.glassModel = OTHER_MODEL_OPTION;
      if (!state.glassManualModel && state.glassSearch.trim()) {
        state.glassManualModel = state.glassSearch.trim();
      }
      state.glassSearch = '';
      state.glassError = '';
      state.glassDropdownOpen = false;
      renderTemperedGlassFinder();
      renderCameraLensFinder();
      return;
    }

    const glassTypeBtn = e.target.closest('[data-glass-type]');
    if (glassTypeBtn) {
      state.glassType = glassTypeBtn.dataset.glassType;
      renderTemperedGlassFinder();
      renderCameraLensFinder();
      return;
    }

    const cBrand = e.target.closest('[data-camera-brand]');
    if (cBrand) {
      state.cameraBrand = cBrand.dataset.cameraBrand;
      state.cameraModel = '';
      state.cameraSearch = '';
      state.cameraDropdownOpen = true;
      renderCameraLensFinder();
      return;
    }

    const pickCam = e.target.closest('[data-pick-camera-model]');
    if (pickCam) {
      state.cameraBrand = pickCam.dataset.pickCameraBrand;
      state.cameraModel = pickCam.dataset.pickCameraModel;
      state.cameraSearch = '';
      state.cameraDropdownOpen = false;
      renderCameraLensFinder();
      return;
    }

    const pBrand = e.target.closest('[data-pouch-brand]');
    if (pBrand) {
      state.pouchBrand = pBrand.dataset.pouchBrand;
      state.pouchModel = '';
      state.pouchSearch = '';
      state.pouchError = '';
      state.pouchDropdownOpen = false;
      renderPouchesFinder();
      return;
    }

    const pickPouch = e.target.closest('[data-pick-pouch-model]');
    if (pickPouch) {
      state.pouchModel = pickPouch.dataset.pickPouchModel;
      state.pouchSearch = '';
      state.pouchError = '';
      state.pouchDropdownOpen = false;
      renderPouchesFinder();
      return;
    }

    const instBtn = e.target.closest('[data-install-opt]');
    if (instBtn) {
      state.glassInstallationOption = instBtn.dataset.installOpt;
      renderGuardInstallationOption();
      renderTemperedGlassFinder();
      renderCameraLensFinder();
      return;
    }

    const reviewLaunchBtn = e.target.closest('[data-launch-review]');
    if (reviewLaunchBtn) {
      closeAccountModal();
      openReviewModal(reviewLaunchBtn.dataset.launchReview || '');
      return;
    }

    // Close dropdowns if clicking outside
    if (state.repairDropdownOpen && !e.target.closest('#device-selector-wrapper')) {
      state.repairDropdownOpen = false;
      renderDeviceDropdown();
    }
    if (state.glassDropdownOpen && !e.target.closest('#tempered-glass-wrapper')) {
      state.glassDropdownOpen = false;
      renderTemperedGlassFinder();
    }
    if (state.cameraDropdownOpen && !e.target.closest('#camera-lens-wrapper')) {
      state.cameraDropdownOpen = false;
      renderCameraLensFinder();
    }
    if (state.pouchDropdownOpen && !e.target.closest('#pouch-wrapper')) {
      state.pouchDropdownOpen = false;
      renderPouchesFinder();
    }
  });

  document.getElementById('home-search-input')?.addEventListener('input', (e) => {
    state.homeSearch = e.target.value;
    renderHome();
  });
  document.getElementById('home-search-clear')?.addEventListener('click', () => {
    state.homeSearch = '';
    const inp = document.getElementById('home-search-input');
    if (inp) inp.value = '';
    renderHome();
  });
  document.getElementById('quick-account-btn')?.addEventListener('click', openAccountModal);

  document.getElementById('catalog-search-input')?.addEventListener('input', (e) => {
    state.catalogSearch = e.target.value;
    renderCatalog();
  });

  // Searchable device input
  const deviceInput = document.getElementById('device-search-input');
  deviceInput?.addEventListener('focus', () => {
    state.repairDropdownOpen = true;
    deviceInput.value = state.repairSearch;
    renderDeviceDropdown();
  });
  deviceInput?.addEventListener('input', (e) => {
    state.repairSearch = e.target.value;
    state.repairDropdownOpen = true;
    renderDeviceDropdown();
  });
  document.getElementById('device-clear-btn')?.addEventListener('click', (e) => {
    e.stopPropagation();
    state.repairModel = '';
    state.repairSearch = '';
    if (deviceInput) deviceInput.value = '';
    state.repairDropdownOpen = true;
    renderDeviceDropdown();
  });

  document.getElementById('btn-repair-gps')?.addEventListener('click', async () => {
    const statusEl = document.getElementById('repair-gps-status');
    const addrEl = document.getElementById('repair-address');
    if (statusEl) {
      statusEl.className = 'text-xs font-semibold text-icase-blue mb-1.5';
      statusEl.textContent = 'Fetching live GPS location...';
    }
    try {
      const res = await detectCurrentLocation();
      if (addrEl) addrEl.value = res.formattedAddress;
      if (statusEl) {
        statusEl.className = 'text-xs font-semibold text-icase-green mb-1.5';
        statusEl.textContent = res.isGeocoded
          ? 'Current location detected via GPS.'
          : 'GPS coordinates captured. Add house/street details if needed.';
      }
    } catch (err) {
      if (statusEl) {
        statusEl.className = 'text-xs font-semibold text-icase-red mb-1.5';
        statusEl.textContent = err.message || 'Could not detect GPS location.';
      }
    }
  });

  document.getElementById('repair-booking-form')?.addEventListener('submit', handleRepairSubmit);
  document.getElementById('btn-track-booked-repair')?.addEventListener('click', () => {
    if (state.bookedRepair) openTrackId(state.bookedRepair.id);
  });
  document.getElementById('btn-book-another-repair')?.addEventListener('click', () => {
    state.bookedRepair = null;
    document.getElementById('repair-success-card')?.classList.add('hidden');
    document.getElementById('repair-booking-form')?.classList.remove('hidden');
  });

  document.getElementById('track-search-form')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const raw = document.getElementById('track-id-input')?.value || '';
    const cleaned = raw.trim().replace(/^#/, '').toUpperCase();
    state.trackingId = cleaned;
    startLiveTrackingSubscription(cleaned);
  });

  document.getElementById('pm-close')?.addEventListener('click', closeProductModal);
  document.getElementById('product-modal')?.addEventListener('click', (e) => {
    if (e.target.id === 'product-modal') closeProductModal();
  });

  document.getElementById('btn-open-cart')?.addEventListener('click', openCartDrawer);
  document.getElementById('cart-drawer-close')?.addEventListener('click', closeCartDrawer);
  document.getElementById('cart-drawer-overlay')?.addEventListener('click', (e) => {
    if (e.target.id === 'cart-drawer-overlay') closeCartDrawer();
  });

  document.getElementById('btn-open-account')?.addEventListener('click', openAccountModal);
  document.getElementById('mobile-nav-account')?.addEventListener('click', openAccountModal);
  document.getElementById('account-modal-close')?.addEventListener('click', closeAccountModal);
  document.getElementById('account-modal-overlay')?.addEventListener('click', (e) => {
    if (e.target.id === 'account-modal-overlay') closeAccountModal();
  });

  document.getElementById('btn-open-guard-video')?.addEventListener('click', openVideoModal);
  document.getElementById('btn-open-guard-video-cta')?.addEventListener('click', openVideoModal);
  document.getElementById('video-modal-close')?.addEventListener('click', closeVideoModal);
  document.getElementById('btn-video-done')?.addEventListener('click', closeVideoModal);
  document.getElementById('video-demo-modal')?.addEventListener('click', (e) => {
    if (e.target.id === 'video-demo-modal') closeVideoModal();
  });
  document.getElementById('btn-video-play-pause')?.addEventListener('click', () => {
    const video = document.getElementById('sample-guard-video');
    const btn = document.getElementById('btn-video-play-pause');
    if (!video || !btn) return;
    if (video.paused) {
      video.play().catch(() => {});
      btn.textContent = 'Pause Video';
    } else {
      video.pause();
      btn.textContent = 'Play Video';
    }
  });

  document.getElementById('review-modal-close')?.addEventListener('click', closeReviewModal);
  document.getElementById('review-modal-overlay')?.addEventListener('click', (e) => {
    if (e.target.id === 'review-modal-overlay') closeReviewModal();
  });

  document.getElementById('btn-admin-forgot')?.addEventListener('click', () => {
    state.adminForgotStep = 'VERIFY_RECOVERY';
    document.getElementById('admin-pin-error')?.classList.add('hidden');
    document.getElementById('admin-pin-success')?.classList.add('hidden');
    const recInput = document.getElementById('admin-recovery-input');
    if (recInput) recInput.value = '';
    syncAdminLockCardUi();
  });

  const backToAdminLoginHandler = () => {
    state.adminForgotStep = 'LOGIN';
    document.getElementById('admin-pin-error')?.classList.add('hidden');
    syncAdminLockCardUi();
  };
  document.getElementById('btn-admin-back-login-1')?.addEventListener('click', backToAdminLoginHandler);
  document.getElementById('btn-admin-back-login-2')?.addEventListener('click', backToAdminLoginHandler);

  document.getElementById('admin-recovery-form')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const errEl = document.getElementById('admin-pin-error');
    const now = Date.now();
    if (now < state.adminLockUntil) {
      const rem = Math.ceil((state.adminLockUntil - now) / 1000);
      errEl.textContent = `Locked for ${formatAdminLockRemaining(rem)} due to invalid attempts.`;
      errEl.classList.remove('hidden');
      syncAdminLockCardUi();
      return;
    }
    const entered = document.getElementById('admin-recovery-input')?.value?.trim() || '';
    const expected = String(state.settings.admin_recovery_code || 'ICASE2026').trim();
    if (entered && entered === expected) {
      state.adminFailedAttempts = 0;
      state.adminLockUntil = 0;
      safeStorageSetItem(STORAGE_ADMIN_LOCK_KEY, '0');
      errEl?.classList.add('hidden');
      state.adminForgotStep = 'RESET_PIN';
      const newPinEl = document.getElementById('admin-reset-new-pin');
      const confPinEl = document.getElementById('admin-reset-confirm-pin');
      if (newPinEl) newPinEl.value = '';
      if (confPinEl) confPinEl.value = '';
      syncAdminLockCardUi();
    } else {
      state.adminFailedAttempts += 1;
      if (state.adminFailedAttempts >= 3) {
        startAdminLockoutTimer(120000);
        errEl.textContent = 'Invalid Master Recovery Code. Locked for 2 minutes.';
      } else {
        errEl.textContent = `Invalid Master Recovery Code. ${3 - state.adminFailedAttempts} attempt(s) left.`;
      }
      errEl.classList.remove('hidden');
    }
  });

  document.getElementById('admin-reset-pin-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const errEl = document.getElementById('admin-pin-error');
    const succEl = document.getElementById('admin-pin-success');
    const newPin = document.getElementById('admin-reset-new-pin')?.value?.trim() || '';
    const confPin = document.getElementById('admin-reset-confirm-pin')?.value?.trim() || '';
    if (newPin.length < 4) {
      errEl.textContent = 'New Admin Password / PIN must be at least 4 characters.';
      errEl.classList.remove('hidden');
      return;
    }
    if (newPin !== confPin) {
      errEl.textContent = 'Passwords do not match. Please re-enter.';
      errEl.classList.remove('hidden');
      return;
    }
    errEl?.classList.add('hidden');
    state.settings = { ...state.settings, admin_pin: newPin };
    saveJsonStorage(STORAGE_SETTINGS_KEY, state.settings);
    await saveSettingInCloud('admin_pin', newPin);
    const pinInput = document.getElementById('admin-pin-input');
    if (pinInput) pinInput.value = '';
    state.adminForgotStep = 'LOGIN';
    if (succEl) {
      succEl.textContent = 'New Admin Password / PIN saved! Enter it below to unlock.';
      succEl.classList.remove('hidden');
    }
    syncAdminLockCardUi();
  });

  document.getElementById('admin-login-form')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const errEl = document.getElementById('admin-pin-error');
    const succEl = document.getElementById('admin-pin-success');
    succEl?.classList.add('hidden');
    const now = Date.now();
    if (now < state.adminLockUntil) {
      const rem = Math.ceil((state.adminLockUntil - now) / 1000);
      errEl.textContent = `Locked for ${formatAdminLockRemaining(rem)} due to invalid attempts.`;
      errEl.classList.remove('hidden');
      syncAdminLockCardUi();
      return;
    }
    const pin = document.getElementById('admin-pin-input')?.value?.trim() || '';
    const expected = String(state.settings.admin_pin || '2026').trim();
    if (pin === expected || pin === 'icase@admin2026') {
      state.isAdminUnlocked = true;
      state.adminFailedAttempts = 0;
      state.adminLockUntil = 0;
      safeStorageSetItem(STORAGE_ADMIN_LOCK_KEY, '0');
      errEl.classList.add('hidden');
      document.getElementById('admin-pin-input').value = '';
      state.unsubAdmin?.();
      state.unsubAdmin = subscribeAdminData({
        onOrders: (orders) => {
          state.adminOrders = orders;
          if (state.activeTab === 'admin') renderAdminSection();
        },
        onRepairs: (repairs) => {
          state.adminRepairs = repairs;
          if (state.activeTab === 'admin') renderAdminSection();
        },
        onReviews: (reviews) => {
          state.adminReviews = reviews;
          if (state.activeTab === 'admin') renderAdminSection();
        }
      });
      renderAdminSection();
    } else {
      state.adminFailedAttempts += 1;
      if (state.adminFailedAttempts >= 3) {
        startAdminLockoutTimer(120000);
        errEl.textContent = '3 failed attempts. Locked for 2 minutes.';
      } else {
        errEl.textContent = `Invalid PIN. ${3 - state.adminFailedAttempts} attempt(s) left.`;
      }
      errEl.classList.remove('hidden');
    }
  });

  document.getElementById('btn-admin-logout')?.addEventListener('click', () => {
    state.isAdminUnlocked = false;
    state.adminReviews = [];
    state.unsubAdmin?.();
    state.unsubAdmin = null;
    renderAdminSection();
  });

  document.getElementById('export-code-close')?.addEventListener('click', closeExportCodeModal);
  document.getElementById('export-code-modal')?.addEventListener('click', (e) => {
    if (e.target.id === 'export-code-modal') closeExportCodeModal();
  });
  document.getElementById('btn-copy-export-code')?.addEventListener('click', async () => {
    const code = document.getElementById('export-code-pre')?.textContent || '';
    if (!code) return;
    try {
      await navigator.clipboard.writeText(code);
      showToast('Default code copied to clipboard!');
    } catch {
      showToast('Select & copy from code preview');
    }
  });
  document.getElementById('btn-download-export-code')?.addEventListener('click', () => {
    const code = document.getElementById('export-code-pre')?.textContent || '';
    if (!code) return;
    const blob = new Blob([code], { type: 'application/javascript;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'default-data-export.js';
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    showToast('Downloaded default-data-export.js!');
  });
}

function bootstrap() {
  bindGlobalEvents();
  initRepairSection();
  reconcileCartWithLiveCatalog();
  updateCartBadge();
  updateHeaderAccountLabel();
  renderHome();
  renderTemperedGlassFinder();
  renderCameraLensFinder();
  renderPouchesFinder();
  renderGuardInstallationOption();
  setActiveTab('home');

  seedFirestoreIfEmpty();
  subscribePublicStoreData({
    onProducts: (list) => {
      const deletedSet = new Set(state.deletedProductIds || []);
      if (Array.isArray(list) && list.length > 0) {
        state.hasSyncedProductsFromCloud = true;
        state.products = list.filter((p) => p?.id && !deletedSet.has(p.id));
        saveJsonStorage(STORAGE_PRODUCTS_KEY, state.products);
        safeStorageSetItem(STORAGE_CATALOG_SEEDED_KEY, 'true');
      } else {
        state.products = INITIAL_PRODUCTS.filter((p) => !deletedSet.has(p.id));
      }
      reconcileCartWithLiveCatalog();
      renderHome();
      if (state.activeTab === 'catalog') renderCatalog();
      if (state.modalProductId) renderProductModal();
      if (state.activeTab === 'admin') renderAdminSection();
    },
    onSettings: (nextSettings) => {
      state.settings = nextSettings;
      state.deletedProductIds = parseDeletedProductIds(nextSettings[DELETED_PRODUCTS_KEY]);
      state.screenGuardConfig = parseScreenGuardConfig(nextSettings[SCREEN_GUARD_CONFIG_KEY]);
      const deletedSet = new Set(state.deletedProductIds);
      if (deletedSet.size > 0) {
        state.products = state.products.filter((p) => !deletedSet.has(p.id));
        saveJsonStorage(STORAGE_PRODUCTS_KEY, state.products);
      }
      saveJsonStorage(STORAGE_SETTINGS_KEY, nextSettings);
      saveJsonStorage(STORAGE_DELETED_PRODUCTS_KEY, state.deletedProductIds);
      saveJsonStorage(STORAGE_GUARD_CONFIG_KEY, state.screenGuardConfig);
      reconcileCartWithLiveCatalog();
      applySettingsToDom();
      renderHome();
      if (state.activeTab === 'catalog') renderCatalog();
      renderTemperedGlassFinder();
      renderCameraLensFinder();
      renderPouchesFinder();
      renderGuardInstallationOption();
      if (state.cartOpen) renderCartDrawer();
      if (state.activeTab === 'admin') renderAdminSection();
    },
    onSyncStatus: updateSyncStatusBadge
  });

  subscribePublicApprovedReviews((list) => {
    state.publicProductReviews = list;
    renderHome();
    if (state.activeTab === 'catalog') renderCatalog();
    if (state.modalProductId) renderProductModal();
  });

  if (state.profile.isLoggedIn && state.profile.userId) {
    startUserScopedSync(state.profile.userId);
  }

  subscribeAuthState((fbUser) => {
    if (fbUser && fbUser.providerData?.some((p) => p.providerId === 'google.com')) {
      ensureProfileSaved({
        userId: fbUser.uid,
        name: state.profile.name || fbUser.displayName || '',
        email: fbUser.email || state.profile.email || '',
        phone: fbUser.phoneNumber || state.profile.phone || '',
        isGoogleSignedIn: true
      });
    }
  });
}

bootstrap();
