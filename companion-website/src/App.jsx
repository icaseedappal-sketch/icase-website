import React, { useEffect, useState } from 'react';
import {
  collection,
  doc,
  onSnapshot,
  query,
  setDoc,
  where
} from 'firebase/firestore';
import { onAuthStateChanged } from 'firebase/auth';
import { ShieldCheck, Zap, Award } from 'lucide-react';
import { auth, db, seedFirestoreIfEmpty } from './firebase';
import {
  CATALOG_SEEDED_KEY,
  DEFAULT_SETTINGS,
  DELETED_PRODUCTS_KEY,
  INITIAL_PRODUCTS,
  SCREEN_GUARD_CONFIG_KEY,
  createDefaultScreenGuardConfig,
  parseDeletedProductIds,
  parseScreenGuardConfig,
  repriceOrNullCartProduct,
  serializeDeletedProductIds,
  serializeScreenGuardConfig
} from './data/constants';
import Navbar, { ICaseArchLogo } from './components/Navbar';
import HomeSection from './components/HomeSection';
import CatalogSection from './components/CatalogSection';
import ScreenGuardsSection from './components/ScreenGuardsSection';
import RepairSection from './components/RepairSection';
import TrackOrderSection from './components/TrackOrderSection';
import CartDrawer from './components/CartDrawer';
import AccountModal from './components/AccountModal';
import AdminSection from './components/AdminSection';
import VerifiedReviewModal from './components/VerifiedReviewModal';
import MediaVideoModal from './components/MediaVideoModal';

const STORAGE_CART_KEY = 'icase_web_cart';
const STORAGE_PROFILE_KEY = 'icase_web_profile';
const STORAGE_PRODUCTS_KEY = 'icase_web_products_v1';
const STORAGE_DELETED_PRODUCTS_KEY = 'icase_web_deleted_products_v1';
const STORAGE_CATALOG_SEEDED_KEY = 'icase_web_catalog_seeded_v1';
const STORAGE_SETTINGS_KEY = 'icase_web_settings_v1';
const STORAGE_GUARD_CONFIG_KEY = 'icase_web_guard_config_v1';

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
    return window.localStorage.getItem(key);
  } catch {
    return null;
  }
}

function safeStorageSetItem(key, value) {
  try {
    window.localStorage.setItem(key, value);
  } catch {}
}

function loadJsonStorage(key, fallback) {
  try {
    const raw = safeStorageGetItem(key);
    if (!raw) return fallback;
    const parsed = JSON.parse(raw);
    return parsed ?? fallback;
  } catch {
    return fallback;
  }
}

function saveJsonStorage(key, value) {
  try {
    safeStorageSetItem(key, JSON.stringify(value));
  } catch {}
}

export default function App() {
  const [activeTab, setActiveTab] = useState('home'); // 'home' | 'catalog' | 'guards' | 'repair' | 'track' | 'admin'
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [selectedBrand, setSelectedBrand] = useState('');
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [activeTrackingId, setActiveTrackingId] = useState('');
  const [guardsFocusTarget, setGuardsFocusTarget] = useState('');
  const [preselectedGlassType, setPreselectedGlassType] = useState('');
  const [videoPreview, setVideoPreview] = useState({ open: false, title: '', url: '' });

  const [deletedProductIds, setDeletedProductIds] = useState(() => {
    const localDel = parseDeletedProductIds(loadJsonStorage(STORAGE_DELETED_PRODUCTS_KEY, []));
    return localDel;
  });

  const [products, setProducts] = useState(() => {
    const localDel = new Set(parseDeletedProductIds(loadJsonStorage(STORAGE_DELETED_PRODUCTS_KEY, [])));
    const localProducts = loadJsonStorage(STORAGE_PRODUCTS_KEY, null);
    if (Array.isArray(localProducts) && localProducts.length > 0) {
      return localProducts.filter((p) => p && !localDel.has(p.id));
    }
    if (safeStorageGetItem(STORAGE_CATALOG_SEEDED_KEY) === 'true' && Array.isArray(localProducts)) {
      return localProducts.filter((p) => p && !localDel.has(p.id));
    }
    return INITIAL_PRODUCTS.filter((p) => !localDel.has(p.id));
  });

  const [settings, setSettings] = useState(() => {
    const localSettings = loadJsonStorage(STORAGE_SETTINGS_KEY, null);
    return localSettings && typeof localSettings === 'object'
      ? { ...DEFAULT_SETTINGS, ...localSettings }
      : DEFAULT_SETTINGS;
  });

  const [screenGuardConfig, setScreenGuardConfig] = useState(() => {
    const localGuard = loadJsonStorage(STORAGE_GUARD_CONFIG_KEY, null);
    if (localGuard) return parseScreenGuardConfig(localGuard);
    const localSettings = loadJsonStorage(STORAGE_SETTINGS_KEY, null);
    if (localSettings && localSettings[SCREEN_GUARD_CONFIG_KEY]) {
      return parseScreenGuardConfig(localSettings[SCREEN_GUARD_CONFIG_KEY]);
    }
    return createDefaultScreenGuardConfig();
  });

  const [profile, setProfile] = useState(() => {
    const saved = loadJsonStorage(STORAGE_PROFILE_KEY, null);
    return saved && typeof saved === 'object' ? { ...EMPTY_PROFILE, ...saved } : EMPTY_PROFILE;
  });

  const [myOrders, setMyOrders] = useState([]);
  const [myRepairs, setMyRepairs] = useState([]);
  const [publicReviews, setPublicReviews] = useState([]);

  const [isAdminLoggedIn, setIsAdminLoggedIn] = useState(false);
  const [adminOrders, setAdminOrders] = useState([]);
  const [adminRepairs, setAdminRepairs] = useState([]);
  const [adminReviews, setAdminReviews] = useState([]);

  const [cartItems, setCartItems] = useState(() => {
    const saved = loadJsonStorage(STORAGE_CART_KEY, []);
    return Array.isArray(saved) ? saved : [];
  });
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isAccountOpen, setIsAccountOpen] = useState(false);
  const [reviewModalState, setReviewModalState] = useState({ open: false, prefillId: '' });

  // Persist local fallbacks cleanly
  useEffect(() => {
    saveJsonStorage(STORAGE_CART_KEY, cartItems);
  }, [cartItems]);

  useEffect(() => {
    saveJsonStorage(STORAGE_PROFILE_KEY, profile);
  }, [profile]);

  useEffect(() => {
    saveJsonStorage(STORAGE_PRODUCTS_KEY, products);
  }, [products]);

  useEffect(() => {
    saveJsonStorage(STORAGE_SETTINGS_KEY, settings);
  }, [settings]);

  useEffect(() => {
    saveJsonStorage(STORAGE_GUARD_CONFIG_KEY, screenGuardConfig);
  }, [screenGuardConfig]);

  // Real-time public Firestore sync: Products, Settings, and Approved Product Reviews
  useEffect(() => {
    if (!db) return;

    seedFirestoreIfEmpty();

    const unsubProducts = onSnapshot(collection(db, 'products'), (snap) => {
      const localDeleted = parseDeletedProductIds(loadJsonStorage(STORAGE_DELETED_PRODUCTS_KEY, []));
      const settingsDeleted = parseDeletedProductIds(settings[DELETED_PRODUCTS_KEY]);
      const delSet = new Set([...localDeleted, ...settingsDeleted, ...deletedProductIds]);

      if (!snap.empty) {
        const list = snap.docs
          .map((d) => ({ id: d.id, ...d.data() }))
          .filter((p) => p && !delSet.has(p.id));
        setProducts(list);
      } else {
        const isSeeded =
          safeStorageGetItem(STORAGE_CATALOG_SEEDED_KEY) === 'true' ||
          settings[CATALOG_SEEDED_KEY] === 'true';
        if (!isSeeded) {
          setProducts(INITIAL_PRODUCTS.filter((p) => !delSet.has(p.id)));
        } else {
          setProducts([]);
        }
      }
    });

    const unsubSettings = onSnapshot(collection(db, 'settings'), (snap) => {
      if (snap.empty) return;
      const next = { ...DEFAULT_SETTINGS };
      snap.docs.forEach((d) => {
        const data = d.data();
        const key = data.key || d.id;
        if (key) next[key] = String(data.value ?? '');
      });
      setSettings(next);

      if (next[SCREEN_GUARD_CONFIG_KEY]) {
        setScreenGuardConfig(parseScreenGuardConfig(next[SCREEN_GUARD_CONFIG_KEY]));
      }
      if (next[DELETED_PRODUCTS_KEY]) {
        const mergedDel = parseDeletedProductIds(next[DELETED_PRODUCTS_KEY]);
        setDeletedProductIds(mergedDel);
        saveJsonStorage(STORAGE_DELETED_PRODUCTS_KEY, mergedDel);
        const delSet = new Set(mergedDel);
        setProducts((prev) => prev.filter((p) => !delSet.has(p.id)));
      }
      if (next[CATALOG_SEEDED_KEY] === 'true') {
        safeStorageSetItem(STORAGE_CATALOG_SEEDED_KEY, 'true');
      }
    });

    const unsubPublicReviews = onSnapshot(
      query(
        collection(db, 'reviews'),
        where('reviewType', '==', 'PRODUCT'),
        where('isApproved', '==', true)
      ),
      (snap) => {
        const list = snap.docs
          .map((d) => ({ id: d.id, ...d.data() }))
          .filter((r) => r.reviewType === 'PRODUCT' && r.isApproved === true)
          .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
        setPublicReviews(list);
      }
    );

    return () => {
      unsubProducts();
      unsubSettings();
      unsubPublicReviews();
    };
  }, []);

  // Reconcile cart live with ScreenGuardConfig and deleted catalog items
  useEffect(() => {
    setCartItems((prev) => {
      let changed = false;
      const next = [];
      for (const item of prev) {
        const p = item?.product;
        if (!p?.id) {
          changed = true;
          continue;
        }
        const isDynamic =
          p.id.startsWith('sg::') ||
          p.id.startsWith('cl::') ||
          p.id.startsWith('combo::') ||
          p.id.startsWith('pouch::');
        if (isDynamic) {
          const repriced = repriceOrNullCartProduct(p, screenGuardConfig, item.selectedColor || '');
          if (!repriced) {
            changed = true;
            continue;
          }
          if (
            repriced.price !== p.price ||
            repriced.description !== p.description ||
            repriced.imageUrl !== p.imageUrl
          ) {
            changed = true;
          }
          next.push({ ...item, product: repriced });
        } else {
          const liveProd = products.find((x) => x.id === p.id);
          if (deletedProductIds.includes(p.id)) {
            changed = true;
            continue;
          }
          if (liveProd && (liveProd.price !== p.price || liveProd.name !== p.name)) {
            changed = true;
            next.push({ ...item, product: liveProd });
          } else {
            next.push(item);
          }
        }
      }
      return changed ? next : prev;
    });
  }, [screenGuardConfig, products, deletedProductIds]);

  // Restore Firebase Auth session (Google Sign-In) cleanly when available
  useEffect(() => {
    if (!auth) return;
    const unsubAuth = onAuthStateChanged(auth, (fbUser) => {
      const hasGoogle = Boolean(
        fbUser && fbUser.providerData.some((p) => p.providerId === 'google.com')
      );
      if (!fbUser || !hasGoogle) {
        return;
      }

      setProfile((prev) => ({
        ...prev,
        userId: fbUser.uid,
        name: prev.name || fbUser.displayName || '',
        email: fbUser.email || prev.email || '',
        phone: fbUser.phoneNumber || prev.phone || '',
        isLoggedIn: true,
        isGoogleSignedIn: true
      }));
    });
    return () => unsubAuth();
  }, []);

  // Scoped customer sync: /users/{userId}, orders, and repairs isolated strictly to current userId
  const resolvedUserId =
    profile.isLoggedIn && profile.isGoogleSignedIn ? profile.userId : '';

  useEffect(() => {
    if (!db || !resolvedUserId) {
      setMyOrders([]);
      setMyRepairs([]);
      return;
    }

    const unsubUser = onSnapshot(doc(db, 'users', resolvedUserId), (snap) => {
      if (!snap.exists()) return;
      const remote = snap.data();
      const remoteWishlist = Array.isArray(remote.wishlistIds)
        ? remote.wishlistIds.filter((x) => typeof x === 'string' && x.trim())
        : null;
      setProfile((prev) => ({
        ...prev,
        userId: resolvedUserId,
        name: remote.name || prev.name,
        email: remote.email || prev.email,
        phone: remote.phone || prev.phone,
        whatsapp: remote.whatsapp || prev.whatsapp,
        address: remote.address || prev.address,
        isLoggedIn: true,
        isGoogleSignedIn: true,
        wishlistIds: remoteWishlist ?? prev.wishlistIds ?? []
      }));
    });

    const unsubMyOrders = onSnapshot(
      query(collection(db, 'orders'), where('userId', '==', resolvedUserId)),
      (snap) => {
        const list = snap.docs
          .map((d) => ({ id: d.id, ...d.data() }))
          .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
        setMyOrders(list);
      }
    );

    const unsubMyRepairs = onSnapshot(
      query(collection(db, 'repairs'), where('userId', '==', resolvedUserId)),
      (snap) => {
        const list = snap.docs
          .map((d) => ({ id: d.id, ...d.data() }))
          .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
        setMyRepairs(list);
      }
    );

    return () => {
      unsubUser();
      unsubMyOrders();
      unsubMyRepairs();
    };
  }, [resolvedUserId]);

  // Admin full collection sync when unlocked
  useEffect(() => {
    if (!db || !isAdminLoggedIn) {
      setAdminOrders([]);
      setAdminRepairs([]);
      setAdminReviews([]);
      return;
    }

    const unsubOrders = onSnapshot(collection(db, 'orders'), (snap) => {
      const list = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
      setAdminOrders(list);
    });

    const unsubRepairs = onSnapshot(collection(db, 'repairs'), (snap) => {
      const list = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
      setAdminRepairs(list);
    });

    const unsubReviews = onSnapshot(collection(db, 'reviews'), (snap) => {
      const list = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
      setAdminReviews(list);
    });

    return () => {
      unsubOrders();
      unsubRepairs();
      unsubReviews();
    };
  }, [isAdminLoggedIn]);

  async function handleSaveProfile(updates) {
    const uid = updates.userId || profile.userId;
    const nextProfile = {
      ...profile,
      ...updates,
      userId: uid,
      isLoggedIn: true,
      wishlistIds: Array.isArray(updates.wishlistIds)
        ? updates.wishlistIds
        : profile.wishlistIds || []
    };
    setProfile(nextProfile);

    if (db && uid) {
      await setDoc(
        doc(db, 'users', uid),
        {
          userId: uid,
          name: nextProfile.name || '',
          email: nextProfile.email || '',
          phone: nextProfile.phone || '',
          whatsapp: nextProfile.whatsapp || '',
          address: nextProfile.address || '',
          wishlistIds: nextProfile.wishlistIds || [],
          isGoogleSignedIn: Boolean(nextProfile.isGoogleSignedIn),
          updatedAt: Date.now()
        },
        { merge: true }
      );
    }
  }

  async function handleToggleWishlist(productId) {
    if (!productId) return;
    const current = Array.isArray(profile.wishlistIds) ? profile.wishlistIds : [];
    const exists = current.includes(productId);
    const nextWishlist = exists
      ? current.filter((id) => id !== productId)
      : [...current, productId];

    setProfile((prev) => ({ ...prev, wishlistIds: nextWishlist }));

    if (db && profile.isLoggedIn && profile.userId) {
      try {
        await setDoc(
          doc(db, 'users', profile.userId),
          {
            userId: profile.userId,
            name: profile.name || '',
            phone: profile.phone || '',
            wishlistIds: nextWishlist,
            updatedAt: Date.now()
          },
          { merge: true }
        );
      } catch {}
    }
  }

  function handleLogout() {
    setProfile(EMPTY_PROFILE);
    setMyOrders([]);
    setMyRepairs([]);
  }

  function addToCart(product, quantity = 1, selectedColor = '') {
    const cleanColor = typeof selectedColor === 'string' ? selectedColor : '';
    setCartItems((prev) => {
      const idx = prev.findIndex(
        (item) => item.product.id === product.id && (item.selectedColor || '') === cleanColor
      );
      if (idx >= 0) {
        return prev.map((item, i) =>
          i === idx ? { ...item, quantity: item.quantity + quantity } : item
        );
      }
      return [...prev, { product, quantity, selectedColor: cleanColor }];
    });
  }

  function updateCartQty(productId, qty, selectedColor = '') {
    if (qty <= 0) {
      removeCartItem(productId, selectedColor);
    } else {
      setCartItems((prev) =>
        prev.map((item) =>
          item.product.id === productId && (item.selectedColor || '') === selectedColor
            ? { ...item, quantity: qty }
            : item
        )
      );
    }
  }

  function removeCartItem(productId, selectedColor = '') {
    setCartItems((prev) =>
      prev.filter(
        (item) => !(item.product.id === productId && (item.selectedColor || '') === selectedColor)
      )
    );
  }

  function handleBuyNow(product, selectedColor = '') {
    addToCart(product, 1, selectedColor);
    setIsCartOpen(true);
  }

  function openTrackId(id) {
    setActiveTrackingId(String(id || '').replace(/^#/, '').toUpperCase());
    setActiveTab('track');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function navigateTab(tab) {
    if (tab === 'track') {
      setActiveTrackingId('');
    }
    if (tab !== 'guards') {
      setGuardsFocusTarget('');
      setPreselectedGlassType('');
    }
    setActiveTab(tab);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function handleSaveGuardConfig(nextConfig) {
    const normalized = parseScreenGuardConfig(nextConfig);
    setScreenGuardConfig(normalized);
    const serialized = serializeScreenGuardConfig(normalized);
    setSettings((prev) => ({ ...prev, [SCREEN_GUARD_CONFIG_KEY]: serialized }));
    if (db) {
      await setDoc(doc(db, 'settings', SCREEN_GUARD_CONFIG_KEY), {
        key: SCREEN_GUARD_CONFIG_KEY,
        value: serialized,
        updatedAt: Date.now()
      });
    }
  }

  async function handleDeleteProductPersistent(productId) {
    const nextDeleted = Array.from(new Set([...deletedProductIds, productId]));
    setDeletedProductIds(nextDeleted);
    saveJsonStorage(STORAGE_DELETED_PRODUCTS_KEY, nextDeleted);
    safeStorageSetItem(STORAGE_CATALOG_SEEDED_KEY, 'true');
    setProducts((prev) => prev.filter((p) => p.id !== productId));

    const serializedDel = serializeDeletedProductIds(nextDeleted);
    setSettings((prev) => ({
      ...prev,
      [DELETED_PRODUCTS_KEY]: serializedDel,
      [CATALOG_SEEDED_KEY]: 'true'
    }));

    if (db) {
      await Promise.all([
        setDoc(doc(db, 'settings', DELETED_PRODUCTS_KEY), {
          key: DELETED_PRODUCTS_KEY,
          value: serializedDel,
          updatedAt: Date.now()
        }),
        setDoc(doc(db, 'settings', CATALOG_SEEDED_KEY), {
          key: CATALOG_SEEDED_KEY,
          value: 'true',
          updatedAt: Date.now()
        })
      ]);
    }
  }

  const cartCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar
        activeTab={activeTab}
        onSelectTab={navigateTab}
        cartCount={cartCount}
        onOpenCart={() => setIsCartOpen(true)}
        onOpenAccount={() => setIsAccountOpen(true)}
        isAccountOpen={isAccountOpen}
        profile={profile}
        settings={settings}
        isAdminLoggedIn={isAdminLoggedIn}
      />

      <main className="container main-content" style={{ flex: 1 }}>
        {activeTab === 'home' && (
          <HomeSection
            products={products}
            settings={settings}
            screenGuardConfig={screenGuardConfig}
            publicReviews={publicReviews}
            wishlistIds={profile.wishlistIds || []}
            onToggleWishlist={handleToggleWishlist}
            onNavigate={navigateTab}
            onSelectCategory={(cat) => setSelectedCategory(cat)}
            onSelectBrand={(brand) => setSelectedBrand(brand)}
            onSelectProduct={(prod) => {
              setSelectedProduct(prod);
              setActiveTab('catalog');
            }}
            onAddToCart={(prod) => addToCart(prod, 1)}
            onOpenAccount={() => setIsAccountOpen(true)}
            onOpenGuardsPouch={() => {
              setGuardsFocusTarget('pouch');
              setPreselectedGlassType('');
              setActiveTab('guards');
            }}
            onOpenGuardsSection={(target) => {
              setGuardsFocusTarget(target || 'glass');
              setPreselectedGlassType('');
              setActiveTab('guards');
            }}
            onOpenGuardsGlassType={(gt) => {
              setGuardsFocusTarget('glass');
              setPreselectedGlassType(gt || '');
              setActiveTab('guards');
            }}
            onOpenVideo={(title, url) =>
              setVideoPreview({ open: true, title: title || 'Product Video', url: url || '' })
            }
            onOpenReviewModal={(prefillId) =>
              setReviewModalState({ open: true, prefillId: prefillId || '' })
            }
          />
        )}

        {activeTab === 'catalog' && (
          <CatalogSection
            products={products}
            selectedCategory={selectedCategory}
            onSelectCategory={setSelectedCategory}
            selectedBrand={selectedBrand}
            onSelectBrand={setSelectedBrand}
            settings={settings}
            publicReviews={publicReviews}
            profile={profile}
            wishlistIds={profile.wishlistIds || []}
            onToggleWishlist={handleToggleWishlist}
            onAddToCart={(prod, selectedColor) => addToCart(prod, 1, selectedColor || '')}
            onBuyNow={(prod, selectedColor) => handleBuyNow(prod, selectedColor || '')}
            selectedProduct={selectedProduct}
            onSelectProduct={setSelectedProduct}
            onOpenGuards={() => navigateTab('guards')}
            onOpenVideo={(title, url) =>
              setVideoPreview({ open: true, title: title || 'Product Video', url: url || '' })
            }
            onLocalReviewCreated={(rev) => {
              setAdminReviews((prev) => [rev, ...prev]);
            }}
          />
        )}

        {activeTab === 'guards' && (
          <ScreenGuardsSection
            screenGuardConfig={screenGuardConfig}
            settings={settings}
            initialFocus={guardsFocusTarget}
            initialGlassType={preselectedGlassType}
            onOpenVideo={(title, url) =>
              setVideoPreview({ open: true, title: title || 'Product Video', url: url || '' })
            }
            onAddToCart={(prod, qty = 1, selectedColor = '') => {
              addToCart(prod, qty, selectedColor);
              setIsCartOpen(true);
            }}
          />
        )}

        {activeTab === 'repair' && (
          <RepairSection
            profile={profile}
            resolvedUserId={resolvedUserId}
            onEnsureProfile={handleSaveProfile}
            onTrackId={openTrackId}
          />
        )}

        {activeTab === 'track' && (
          <TrackOrderSection
            activeTrackingId={activeTrackingId}
            onChangeTrackingId={setActiveTrackingId}
          />
        )}

        {activeTab === 'admin' && (
          <AdminSection
            isAdminLoggedIn={isAdminLoggedIn}
            onAdminLoginSuccess={() => setIsAdminLoggedIn(true)}
            onAdminLogout={() => setIsAdminLoggedIn(false)}
            orders={adminOrders}
            repairs={adminRepairs}
            products={products}
            settings={settings}
            screenGuardConfig={screenGuardConfig}
            reviews={adminReviews}
            onSaveGuardConfig={handleSaveGuardConfig}
            onUpdateProductsLocal={setProducts}
            onDeleteProductPersistent={handleDeleteProductPersistent}
            onUpdateSettingsLocal={(updates) =>
              setSettings((prev) => ({ ...prev, ...updates }))
            }
            onUpdateReviewsLocal={(updater) => {
              setAdminReviews((prev) => {
                const next = typeof updater === 'function' ? updater(prev) : updater;
                setPublicReviews(
                  next.filter((r) => r.reviewType === 'PRODUCT' && r.isApproved === true)
                );
                return next;
              });
            }}
          />
        )}
      </main>

      <footer className="brand-footer">
        <div className="container brand-footer-inner">
          <div style={{ display: 'flex', justifyContent: 'center' }}>
            <ICaseArchLogo darkBg showTagline />
          </div>

          <div className="footer-trust-row">
            <div className="footer-trust-badge">
              <div className="footer-trust-circle">
                <ShieldCheck size={20} color="#FFFFFF" />
              </div>
              <span>Trusted Service</span>
            </div>
            <div className="footer-trust-badge">
              <div className="footer-trust-circle">
                <Zap size={20} color="#FFFFFF" />
              </div>
              <span>Fast Delivery</span>
            </div>
            <div className="footer-trust-badge">
              <div className="footer-trust-circle">
                <Award size={20} color="#FFB800" />
              </div>
              <span>Best Quality Products</span>
            </div>
          </div>

          <div className="footer-slogan-row">
            <span className="footer-slogan-bag">🛍️</span>
            <span className="footer-slogan-text">Shop Smart... Live Better...</span>
          </div>

          <div className="footer-bottom-meta">
            <span>
              Edappal, Malappuram, Kerala • Support:{' '}
              <strong>+91 {settings.store_phone || '9961454059'}</strong> •{' '}
              <strong>{settings.store_email || 'icaseedappal@gmail.com'}</strong>
            </span>
            <button
              type="button"
              onClick={() => navigateTab('admin')}
              className="footer-admin-btn"
            >
              Admin Panel
            </button>
          </div>
        </div>
      </footer>

      <CartDrawer
        isOpen={isCartOpen}
        onClose={() => setIsCartOpen(false)}
        cartItems={cartItems}
        onUpdateQty={updateCartQty}
        onRemoveItem={removeCartItem}
        onClearCart={() => setCartItems([])}
        settings={settings}
        profile={profile}
        resolvedUserId={resolvedUserId}
        onEnsureProfile={handleSaveProfile}
        onOpenAccount={() => setIsAccountOpen(true)}
        onTrackOrder={openTrackId}
      />

      <AccountModal
        isOpen={isAccountOpen}
        onClose={() => setIsAccountOpen(false)}
        profile={profile}
        products={products}
        myOrders={myOrders}
        myRepairs={myRepairs}
        onSaveProfile={handleSaveProfile}
        onToggleWishlist={handleToggleWishlist}
        onAddToCart={(prod) => addToCart(prod, 1)}
        onLogout={handleLogout}
        onTrackId={openTrackId}
        onOpenReview={(refId) =>
          setReviewModalState({ open: true, prefillId: refId || '' })
        }
        onOpenAdmin={() => navigateTab('admin')}
      />

      <VerifiedReviewModal
        isOpen={reviewModalState.open}
        onClose={() => setReviewModalState({ open: false, prefillId: '' })}
        prefillId={reviewModalState.prefillId}
        profile={profile}
        onLocalReviewCreated={(rev) => setAdminReviews((prev) => [rev, ...prev])}
      />

      <MediaVideoModal
        isOpen={videoPreview.open}
        title={videoPreview.title}
        url={videoPreview.url}
        onClose={() => setVideoPreview({ open: false, title: '', url: '' })}
      />
    </div>
  );
}
