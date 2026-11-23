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
import { auth, db, googleProvider, seedFirestoreIfEmpty, signInWithPopup } from './firebase';
import { DEFAULT_SETTINGS, INITIAL_PRODUCTS } from './data/constants';
import Navbar from './components/Navbar';
import HomeSection from './components/HomeSection';
import CatalogSection from './components/CatalogSection';
import RepairSection from './components/RepairSection';
import TrackOrderSection from './components/TrackOrderSection';
import CartDrawer from './components/CartDrawer';
import AccountModal from './components/AccountModal';
import AdminSection from './components/AdminSection';

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

export default function App() {
  const [activeTab, setActiveTab] = useState('home'); // 'home' | 'catalog' | 'repair' | 'track' | 'admin'
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [activeTrackingId, setActiveTrackingId] = useState('');

  const [products, setProducts] = useState(INITIAL_PRODUCTS);
  const [settings, setSettings] = useState(DEFAULT_SETTINGS);

  const [profile, setProfile] = useState(() => {
    try {
      const saved = localStorage.getItem('icase_web_profile');
      return saved ? { ...EMPTY_PROFILE, ...JSON.parse(saved) } : EMPTY_PROFILE;
    } catch {
      return EMPTY_PROFILE;
    }
  });

  const [myOrders, setMyOrders] = useState([]);
  const [myRepairs, setMyRepairs] = useState([]);

  const [isAdminLoggedIn, setIsAdminLoggedIn] = useState(false);
  const [adminOrders, setAdminOrders] = useState([]);
  const [adminRepairs, setAdminRepairs] = useState([]);

  const [cartItems, setCartItems] = useState(() => {
    try {
      const saved = localStorage.getItem('icase_web_cart');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isAccountOpen, setIsAccountOpen] = useState(false);
  const [authResolved, setAuthResolved] = useState(false);
  const [authBusy, setAuthBusy] = useState(false);
  const [authError, setAuthError] = useState('');

  // Persist cart & profile locally
  useEffect(() => {
    try {
      localStorage.setItem('icase_web_cart', JSON.stringify(cartItems));
    } catch {}
  }, [cartItems]);

  useEffect(() => {
    try {
      localStorage.setItem('icase_web_profile', JSON.stringify(profile));
    } catch {}
  }, [profile]);

  // Real-time public Firestore sync: Products + Global Settings
  useEffect(() => {
    seedFirestoreIfEmpty();

    const unsubProducts = onSnapshot(collection(db, 'products'), (snap) => {
      if (!snap.empty) {
        const list = snap.docs.map((d) => ({ id: d.id, ...d.data() }));
        setProducts(list);
      }
    });

    const unsubSettings = onSnapshot(collection(db, 'settings'), (snap) => {
      if (!snap.empty) {
        const next = { ...DEFAULT_SETTINGS };
        snap.docs.forEach((d) => {
          const data = d.data();
          const key = data.key || d.id;
          if (key) next[key] = String(data.value ?? '');
        });
        setSettings(next);
      }
    });

    return () => {
      unsubProducts();
      unsubSettings();
    };
  }, []);

  // Restore Firebase Auth session (Google Sign-In only)
  useEffect(() => {
    const unsubAuth = onAuthStateChanged(auth, (fbUser) => {
      const hasGoogle = Boolean(
        fbUser && fbUser.providerData.some((p) => p.providerId === 'google.com')
      );
      if (!fbUser || !hasGoogle) {
        setProfile(EMPTY_PROFILE);
        setMyOrders([]);
        setMyRepairs([]);
        setAuthResolved(true);
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
      setAuthResolved(true);
    });
    return () => unsubAuth();
  }, []);

  // Scoped customer sync: /users/{userId}, orders, and repairs isolated strictly to current userId
  const resolvedUserId =
    profile.isLoggedIn && profile.isGoogleSignedIn ? profile.userId : '';

  useEffect(() => {
    if (!resolvedUserId) {
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
    if (!isAdminLoggedIn) {
      setAdminOrders([]);
      setAdminRepairs([]);
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

    return () => {
      unsubOrders();
      unsubRepairs();
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

    if (uid) {
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

    if (profile.isLoggedIn && profile.userId) {
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

  function addToCart(product, quantity = 1) {
    setCartItems((prev) => {
      const idx = prev.findIndex((item) => item.product.id === product.id);
      if (idx >= 0) {
        return prev.map((item, i) =>
          i === idx ? { ...item, quantity: item.quantity + quantity } : item
        );
      }
      return [...prev, { product, quantity }];
    });
  }

  function updateCartQty(productId, qty) {
    if (qty <= 0) {
      setCartItems((prev) => prev.filter((item) => item.product.id !== productId));
    } else {
      setCartItems((prev) =>
        prev.map((item) =>
          item.product.id === productId ? { ...item, quantity: qty } : item
        )
      );
    }
  }

  function removeCartItem(productId) {
    setCartItems((prev) => prev.filter((item) => item.product.id !== productId));
  }

  function handleBuyNow(product) {
    addToCart(product, 1);
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
    setActiveTab(tab);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function handleLandingGoogleSignIn() {
    setAuthError('');
    setAuthBusy(true);
    try {
      const result = await signInWithPopup(auth, googleProvider);
      const fbUser = result.user;
      await handleSaveProfile({
        userId: fbUser.uid,
        name: fbUser.displayName || 'Customer',
        email: fbUser.email || '',
        phone: fbUser.phoneNumber || '',
        whatsapp: fbUser.phoneNumber || '',
        address: '',
        isGoogleSignedIn: true
      });
    } catch (err) {
      setAuthError(err.message || 'Google Sign-In could not be completed. Please try again.');
    } finally {
      setAuthBusy(false);
    }
  }

  const cartCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);
  const isAuthenticated = profile.isLoggedIn && profile.isGoogleSignedIn;

  if (!authResolved || !isAuthenticated) {
    return (
      <div
        style={{
          minHeight: '100vh',
          background: 'linear-gradient(135deg, #071220 0%, #0B1728 60%, #162944 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: 20
        }}
      >
        <div
          className="card"
          style={{
            maxWidth: 440,
            width: '100%',
            padding: 32,
            borderRadius: 22,
            textAlign: 'center',
            boxShadow: '0 22px 50px rgba(0, 0, 0, 0.35)'
          }}
        >
          <div
            style={{
              width: 64,
              height: 64,
              borderRadius: 16,
              background: '#0B1728',
              color: '#FFFFFF',
              display: 'grid',
              placeItems: 'center',
              fontSize: 28,
              fontWeight: 900,
              margin: '0 auto 14px',
              border: '2px solid #E50914'
            }}
          >
            <span>
              <span style={{ color: '#E50914' }}>i</span>C
            </span>
          </div>
          <h1 style={{ fontSize: 24, fontWeight: 900, color: 'var(--icase-navy)', marginBottom: 4 }}>
            <span style={{ color: '#E50914' }}>i</span>CASE
          </h1>
          <div style={{ fontSize: 12, fontWeight: 700, color: '#64748B', letterSpacing: '0.08em', marginBottom: 16 }}>
            MOBILE ACCESSORIES &amp; EXPRESS REPAIR
          </div>
          <p style={{ fontSize: 14, color: '#475569', marginBottom: 22, lineHeight: 1.55 }}>
            {!authResolved
              ? 'Checking your Google session...'
              : 'Sign in with your Google account to browse products, place orders, book doorstep repairs, and sync live across Android and Web.'}
          </p>

          {authError && (
            <div style={{ color: '#E50914', fontWeight: 700, fontSize: 13, marginBottom: 14 }}>
              {authError}
            </div>
          )}

          <button
            type="button"
            className="btn btn-red"
            style={{ width: '100%', padding: '13px 18px', fontSize: 15 }}
            disabled={!authResolved || authBusy}
            onClick={handleLandingGoogleSignIn}
          >
            {!authResolved
              ? 'Checking Session...'
              : authBusy
                ? 'Signing in with Google...'
                : 'Sign in with Google'}
          </button>

          <div
            style={{
              marginTop: 18,
              paddingTop: 16,
              borderTop: '1px solid var(--border)',
              display: 'flex',
              justifyContent: 'center',
              gap: 16,
              fontSize: 12,
              color: '#64748B',
              fontWeight: 600
            }}
          >
            <span>1-Hour Delivery</span>
            <span>•</span>
            <span>Live Tracking</span>
            <span>•</span>
            <span>Secure Google Sync</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar
        activeTab={activeTab}
        onSelectTab={navigateTab}
        cartCount={cartCount}
        onOpenCart={() => setIsCartOpen(true)}
        onOpenAccount={() => setIsAccountOpen(true)}
        profile={profile}
        settings={settings}
        isAdminLoggedIn={isAdminLoggedIn}
      />

      <main className="container main-content" style={{ flex: 1 }}>
        {activeTab === 'home' && (
          <HomeSection
            products={products}
            settings={settings}
            wishlistIds={profile.wishlistIds || []}
            onToggleWishlist={handleToggleWishlist}
            onNavigate={navigateTab}
            onSelectCategory={(cat) => setSelectedCategory(cat)}
            onSelectProduct={(prod) => {
              setSelectedProduct(prod);
              setActiveTab('catalog');
            }}
            onAddToCart={(prod) => addToCart(prod, 1)}
          />
        )}

        {activeTab === 'catalog' && (
          <CatalogSection
            products={products}
            selectedCategory={selectedCategory}
            onSelectCategory={setSelectedCategory}
            settings={settings}
            wishlistIds={profile.wishlistIds || []}
            onToggleWishlist={handleToggleWishlist}
            onAddToCart={(prod) => addToCart(prod, 1)}
            onBuyNow={handleBuyNow}
            selectedProduct={selectedProduct}
            onSelectProduct={setSelectedProduct}
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
          />
        )}
      </main>

      <footer className="brand-footer">
        <div className="container footer-grid">
          <div>
            <div style={{ fontSize: 22, fontWeight: 900 }}>
              <span style={{ color: '#E50914' }}>i</span>CASE
            </div>
            <div style={{ fontSize: 12, color: '#94A3B8' }}>
              YOUR PHONE, OUR CARE • Edappal, Malappuram, Kerala
            </div>
          </div>

          <div className="footer-badges">
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
              <ShieldCheck size={16} color="#10B981" /> Trusted Service
            </span>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
              <Zap size={16} color="#FDE047" /> Fast Delivery
            </span>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
              <Award size={16} color="#FF4D57" /> Best Quality Products
            </span>
          </div>

          <div style={{ fontSize: 13, color: '#CBD5E1' }}>
            Support: <strong>+91 {settings.store_phone || '9961454059'}</strong> •{' '}
            <strong>{settings.store_email || 'icaseedappal@gmail.com'}</strong>
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
      />
    </div>
  );
}
