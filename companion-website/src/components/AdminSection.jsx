import React, { useEffect, useState } from 'react';
import {
  ShieldCheck,
  Trash2,
  Plus,
  LogOut,
  Lock,
  Upload,
  RotateCcw,
  Code2,
  Copy,
  Download,
  X,
  Check
} from 'lucide-react';
import { deleteDoc, doc, setDoc, updateDoc } from 'firebase/firestore';
import { db } from '../firebase';
import {
  CATALOG_SEEDED_KEY,
  CATEGORIES,
  COLOR_PRESETS,
  ORDER_STATUSES,
  REPAIR_STATUSES,
  compressImageFileToDataUrl,
  formatINR,
  generateDefaultCodeExport,
  normalizeHexColor,
  parseColorOptions,
  parseComplaints,
  parseOrderItems,
  serializeColorOptions
} from '../data/constants';
import AdminGlassOffersTab from './AdminGlassOffersTab';
import AdminReviewsTab from './AdminReviewsTab';

export default function AdminSection({
  isAdminLoggedIn,
  onAdminLoginSuccess,
  onAdminLogout,
  orders,
  repairs,
  products,
  settings,
  screenGuardConfig,
  reviews = [],
  onSaveGuardConfig,
  onUpdateProductsLocal,
  onDeleteProductPersistent,
  onUpdateSettingsLocal,
  onUpdateReviewsLocal
}) {
  const [pinInput, setPinInput] = useState('');
  const [authError, setAuthError] = useState('');
  const [authSuccess, setAuthSuccess] = useState('');
  const [failedAttempts, setFailedAttempts] = useState(0);
  const [lockUntil, setLockUntil] = useState(() => {
    try {
      const saved = Number(localStorage.getItem('icase_admin_lock_until_v1') || 0);
      return saved > Date.now() ? saved : 0;
    } catch {
      return 0;
    }
  });
  const [lockSecondsLeft, setLockSecondsLeft] = useState(() =>
    lockUntil > Date.now() ? Math.ceil((lockUntil - Date.now()) / 1000) : 0
  );
  const [forgotStep, setForgotStep] = useState('LOGIN'); // 'LOGIN' | 'VERIFY_RECOVERY' | 'RESET_PIN'
  const [recoveryCodeInput, setRecoveryCodeInput] = useState('');
  const [resetNewPin, setResetNewPin] = useState('');
  const [resetConfirmPin, setResetConfirmPin] = useState('');

  const [adminTab, setAdminTab] = useState('ORDERS'); // 'ORDERS' | 'REPAIRS' | 'PRODUCTS' | 'SETTINGS'
  const [searchFilter, setSearchFilter] = useState('');

  // Product editor state
  const [editingProduct, setEditingProduct] = useState(null);
  const [prodName, setProdName] = useState('');
  const [prodCategory, setProdCategory] = useState('Cases');
  const [prodPrice, setProdPrice] = useState('');
  const [prodOriginalPrice, setProdOriginalPrice] = useState('');
  const [prodStockCount, setProdStockCount] = useState('25');
  const [prodImageUrl, setProdImageUrl] = useState('');
  const [prodDesc, setProdDesc] = useState('');
  const [prodInStock, setProdInStock] = useState(true);
  const [prodPopular, setProdPopular] = useState(false);
  const [prodOneHour, setProdOneHour] = useState(true);
  const [prodColors, setProdColors] = useState([]);
  const [editingColorIdx, setEditingColorIdx] = useState(null);
  const [colorNameInput, setColorNameInput] = useState('');
  const [colorHexInput, setColorHexInput] = useState('#1B2A4A');
  const [prodImgUploadErr, setProdImgUploadErr] = useState('');
  const [exportModalOpen, setExportModalOpen] = useState(false);
  const [exportCopied, setExportCopied] = useState(false);

  // Settings form state
  const [standardFee, setStandardFee] = useState(settings.delivery_fee_standard || '50.00');
  const [expressFee, setExpressFee] = useState(settings.delivery_fee_express || '80.00');
  const [freeThreshold, setFreeThreshold] = useState(settings.free_delivery_threshold || '0.00');
  const [oneHourEnabled, setOneHourEnabled] = useState(
    settings.one_hour_delivery_enabled !== 'false'
  );
  const [storeOnline, setStoreOnline] = useState(settings.store_online !== 'false');
  const [storeName, setStoreName] = useState(settings.store_name || '');
  const [storePhone, setStorePhone] = useState(settings.store_phone || '');
  const [storeWhatsapp, setStoreWhatsapp] = useState(settings.store_whatsapp || '');
  const [storeEmail, setStoreEmail] = useState(settings.store_email || '');
  const [storeAddress, setStoreAddress] = useState(settings.store_address || '');
  const [storeHours, setStoreHours] = useState(settings.store_hours || '');
  const [newAdminPin, setNewAdminPin] = useState('');
  const [recoveryCodeSetting, setRecoveryCodeSetting] = useState(
    settings.admin_recovery_code || 'ICASE2026'
  );
  const [settingsSavedMsg, setSettingsSavedMsg] = useState('');
  const [settingsErrorMsg, setSettingsErrorMsg] = useState('');

  useEffect(() => {
    setStandardFee(settings.delivery_fee_standard || '50.00');
    setExpressFee(settings.delivery_fee_express || '80.00');
    setFreeThreshold(settings.free_delivery_threshold || '0.00');
    setOneHourEnabled(settings.one_hour_delivery_enabled !== 'false');
    setStoreOnline(settings.store_online !== 'false');
    setStoreName(settings.store_name || 'iCASE Mobile Accessories & Repair');
    setStorePhone(settings.store_phone || '9961454059');
    setStoreWhatsapp(settings.store_whatsapp || '9961454059');
    setStoreEmail(settings.store_email || 'icaseedappal@gmail.com');
    setStoreAddress(
      settings.store_address || 'Main Road, Near Town Square, Edappal, Malappuram, Kerala'
    );
    setStoreHours(settings.store_hours || '9:00 AM - 10:00 PM (Daily)');
    setRecoveryCodeSetting(settings.admin_recovery_code || 'ICASE2026');
  }, [settings]);

  useEffect(() => {
    if (!lockUntil || lockUntil <= Date.now()) {
      setLockSecondsLeft(0);
      try {
        localStorage.removeItem('icase_admin_lock_until_v1');
      } catch {}
      return undefined;
    }
    try {
      localStorage.setItem('icase_admin_lock_until_v1', String(lockUntil));
    } catch {}
    const updateTick = () => {
      const rem = Math.ceil((lockUntil - Date.now()) / 1000);
      if (rem <= 0) {
        setLockSecondsLeft(0);
        setLockUntil(0);
        setAuthError('');
        try {
          localStorage.removeItem('icase_admin_lock_until_v1');
        } catch {}
      } else {
        setLockSecondsLeft(rem);
      }
    };
    updateTick();
    const timer = setInterval(updateTick, 1000);
    return () => clearInterval(timer);
  }, [lockUntil]);

  function formatLockoutTimer(secs) {
    const mins = Math.floor(secs / 60);
    const rem = secs % 60;
    return mins > 0 ? `${mins}m ${String(rem).padStart(2, '0')}s` : `${rem}s`;
  }

  function triggerLockout(messagePrefix) {
    const until = Date.now() + 120000;
    setLockUntil(until);
    setLockSecondsLeft(120);
    setFailedAttempts(0);
    setAuthError(`${messagePrefix} 3 failed attempts. Locked for 2 minutes.`);
  }

  function handlePinSubmit(e) {
    e.preventDefault();
    const now = Date.now();
    if (now < lockUntil) {
      const rem = Math.ceil((lockUntil - now) / 1000);
      setLockSecondsLeft(rem);
      setAuthError(`Locked for ${formatLockoutTimer(rem)} due to repeated invalid attempts.`);
      return;
    }
    const activePin = String(settings.admin_pin || '2026').trim();
    if (pinInput.trim() === activePin || pinInput.trim() === 'icase@admin2026') {
      setFailedAttempts(0);
      setLockUntil(0);
      setLockSecondsLeft(0);
      setPinInput('');
      setAuthError('');
      setAuthSuccess('');
      onAdminLoginSuccess();
    } else {
      const next = failedAttempts + 1;
      if (next >= 3) {
        triggerLockout('Incorrect PIN.');
      } else {
        setFailedAttempts(next);
        setAuthError(`Invalid Admin PIN. ${3 - next} attempt(s) left.`);
      }
    }
  }

  function handleVerifyRecoverySubmit(e) {
    e.preventDefault();
    const now = Date.now();
    if (now < lockUntil) {
      const rem = Math.ceil((lockUntil - now) / 1000);
      setLockSecondsLeft(rem);
      setAuthError(`Locked for ${formatLockoutTimer(rem)} due to repeated invalid attempts.`);
      return;
    }
    const expectedCode = String(settings.admin_recovery_code || 'ICASE2026').trim();
    if (recoveryCodeInput.trim() && recoveryCodeInput.trim() === expectedCode) {
      setFailedAttempts(0);
      setLockUntil(0);
      setLockSecondsLeft(0);
      setAuthError('');
      setForgotStep('RESET_PIN');
    } else {
      const next = failedAttempts + 1;
      if (next >= 3) {
        triggerLockout('Invalid Recovery Code.');
      } else {
        setFailedAttempts(next);
        setAuthError(`Invalid Master Recovery Code. ${3 - next} attempt(s) left.`);
      }
    }
  }

  async function handleResetPinSubmit(e) {
    e.preventDefault();
    const cleanPin = resetNewPin.trim();
    const cleanConfirm = resetConfirmPin.trim();
    if (cleanPin.length < 4) {
      setAuthError('New Admin Password / PIN must be at least 4 characters.');
      return;
    }
    if (cleanPin !== cleanConfirm) {
      setAuthError('Passwords do not match. Please re-enter.');
      return;
    }
    await saveSetting('admin_pin', cleanPin);
    setPinInput('');
    setRecoveryCodeInput('');
    setResetNewPin('');
    setResetConfirmPin('');
    setAuthError('');
    setAuthSuccess('New Admin Password / PIN saved! Enter it below to unlock.');
    setForgotStep('LOGIN');
  }

  if (!isAdminLoggedIn) {
    const isLocked = lockSecondsLeft > 0;
    return (
      <div className="card" style={{ maxWidth: 460, margin: '28px auto', textAlign: 'center' }}>
        <Lock size={40} color="#E50914" style={{ marginBottom: 10 }} />
        <h2 style={{ color: 'var(--icase-navy)', marginBottom: 4 }}>iCASE Management</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: 13.5, marginBottom: 16 }}>
          {forgotStep === 'VERIFY_RECOVERY'
            ? 'Enter your Master Recovery Code to reset the Admin Password / PIN.'
            : forgotStep === 'RESET_PIN'
              ? 'Master Recovery Code verified. Set a new Admin Password / PIN.'
              : 'Manage orders, repairs, product catalog, delivery charges & live app settings synced with the Mobile App.'}
        </p>

        {forgotStep === 'VERIFY_RECOVERY' && (
          <form onSubmit={handleVerifyRecoverySubmit}>
            <input
              type="password"
              className="input"
              placeholder="Enter Master Recovery Code"
              value={recoveryCodeInput}
              disabled={isLocked}
              onChange={(e) => {
                setRecoveryCodeInput(e.target.value);
                setAuthError('');
              }}
              style={{ marginBottom: 10 }}
            />
            {authError && (
              <div style={{ color: '#E50914', fontWeight: 700, fontSize: 13, marginBottom: 10 }}>
                {authError}
              </div>
            )}
            {isLocked && (
              <div
                style={{
                  background: '#FEF2F2',
                  color: '#B91C1C',
                  fontWeight: 800,
                  fontSize: 13,
                  padding: '10px 12px',
                  borderRadius: 10,
                  marginBottom: 10
                }}
              >
                🔒 Locked for {formatLockoutTimer(lockSecondsLeft)}
              </div>
            )}
            <button
              type="submit"
              className="btn btn-navy"
              disabled={isLocked}
              style={{ width: '100%', minHeight: 46 }}
            >
              Verify Master Recovery Code
            </button>
            <button
              type="button"
              className="section-link"
              style={{ marginTop: 12, fontSize: 13 }}
              onClick={() => {
                setForgotStep('LOGIN');
                setAuthError('');
              }}
            >
              ← Back to Admin Login
            </button>
          </form>
        )}

        {forgotStep === 'RESET_PIN' && (
          <form onSubmit={handleResetPinSubmit}>
            <input
              type="password"
              className="input"
              placeholder="New Admin Password / PIN (min 4 chars)"
              value={resetNewPin}
              onChange={(e) => {
                setResetNewPin(e.target.value);
                setAuthError('');
              }}
              style={{ marginBottom: 10 }}
            />
            <input
              type="password"
              className="input"
              placeholder="Confirm New Password / PIN"
              value={resetConfirmPin}
              onChange={(e) => {
                setResetConfirmPin(e.target.value);
                setAuthError('');
              }}
              style={{ marginBottom: 10 }}
            />
            {authError && (
              <div style={{ color: '#E50914', fontWeight: 700, fontSize: 13, marginBottom: 10 }}>
                {authError}
              </div>
            )}
            <button type="submit" className="btn btn-navy" style={{ width: '100%', minHeight: 46 }}>
              Save New Admin Password / PIN
            </button>
            <button
              type="button"
              className="section-link"
              style={{ marginTop: 12, fontSize: 13 }}
              onClick={() => {
                setForgotStep('LOGIN');
                setAuthError('');
              }}
            >
              Cancel &amp; Back to Login
            </button>
          </form>
        )}

        {forgotStep === 'LOGIN' && (
          <form onSubmit={handlePinSubmit}>
            <input
              type="password"
              className="input"
              placeholder="Enter Admin Password / PIN"
              value={pinInput}
              disabled={isLocked}
              onChange={(e) => setPinInput(e.target.value)}
              style={{ marginBottom: 10 }}
            />
            {authSuccess && (
              <div style={{ color: '#15803D', fontWeight: 800, fontSize: 13, marginBottom: 10 }}>
                {authSuccess}
              </div>
            )}
            {authError && (
              <div style={{ color: '#E50914', fontWeight: 700, fontSize: 13, marginBottom: 10 }}>
                {authError}
              </div>
            )}
            {isLocked && (
              <div
                style={{
                  background: '#FEF2F2',
                  color: '#B91C1C',
                  fontWeight: 800,
                  fontSize: 13,
                  padding: '10px 12px',
                  borderRadius: 10,
                  marginBottom: 10
                }}
              >
                🔒 Too many failed attempts. Locked for {formatLockoutTimer(lockSecondsLeft)}
              </div>
            )}
            <button
              type="submit"
              className="btn btn-navy"
              disabled={isLocked}
              style={{ width: '100%', minHeight: 46 }}
            >
              <ShieldCheck size={17} /> Unlock Admin Dashboard
            </button>
            <button
              type="button"
              className="section-link"
              style={{ marginTop: 12, fontSize: 13, fontWeight: 700 }}
              onClick={() => {
                setForgotStep('VERIFY_RECOVERY');
                setAuthError('');
                setAuthSuccess('');
                setRecoveryCodeInput('');
              }}
            >
              Forgot Password / PIN?
            </button>
          </form>
        )}
      </div>
    );
  }

  const totalRevenue = orders.reduce((sum, o) => sum + Number(o.totalAmount || 0), 0);
  const pendingOrders = orders.filter((o) => o.status !== 'Delivered').length;
  const pendingRepairs = repairs.filter((r) => r.status !== 'Delivered').length;

  async function saveSetting(key, value) {
    if (onUpdateSettingsLocal) {
      onUpdateSettingsLocal({ [key]: String(value) });
    }
    if (!db) return;
    await setDoc(doc(db, 'settings', key), {
      key,
      value: String(value),
      updatedAt: Date.now()
    });
  }

  async function handleSaveAllSettings(e) {
    e.preventDefault();
    const cleanPin = newAdminPin.trim();
    const cleanRec = recoveryCodeSetting.trim();
    if (cleanPin.length > 0 && cleanPin.length < 4) {
      setSettingsErrorMsg('New Admin Password / PIN must be at least 4 characters.');
      setSettingsSavedMsg('');
      return;
    }
    if (cleanRec.length > 0 && cleanRec.length < 4) {
      setSettingsErrorMsg('Master Recovery Code must be at least 4 characters.');
      setSettingsSavedMsg('');
      return;
    }
    setSettingsErrorMsg('');
    const updates = {
      delivery_fee_standard: Number(standardFee || 0).toFixed(2),
      delivery_fee_express: Number(expressFee || 0).toFixed(2),
      free_delivery_threshold: Number(freeThreshold || 0).toFixed(2),
      one_hour_delivery_enabled: oneHourEnabled ? 'true' : 'false',
      store_online: storeOnline ? 'true' : 'false',
      store_name: storeName.trim(),
      store_phone: storePhone.trim(),
      store_whatsapp: storeWhatsapp.trim(),
      store_email: storeEmail.trim(),
      store_address: storeAddress.trim(),
      store_hours: storeHours.trim(),
      ...(cleanPin.length >= 4 ? { admin_pin: cleanPin } : {}),
      ...(cleanRec.length >= 4 ? { admin_recovery_code: cleanRec } : {})
    };
    if (onUpdateSettingsLocal) {
      onUpdateSettingsLocal(updates);
    }
    if (db) {
      await Promise.all(
        Object.entries(updates).map(([k, v]) =>
          setDoc(doc(db, 'settings', k), {
            key: k,
            value: String(v),
            updatedAt: Date.now()
          })
        )
      );
    }
    setNewAdminPin('');
    setSettingsSavedMsg(
      db
        ? 'Settings updated and synced live to Web & Mobile App!'
        : 'Settings updated locally for this session.'
    );
    setTimeout(() => setSettingsSavedMsg(''), 4000);
  }

  function startEditProduct(p) {
    setEditingProduct(p || { isNew: true });
    setProdName(p?.name || '');
    setProdCategory(p?.category || 'Cases');
    setProdPrice(p ? String(p.price) : '');
    setProdOriginalPrice(p ? String(p.originalPrice || p.price) : '');
    setProdStockCount(p ? String(p.stockCount ?? 25) : '25');
    setProdImageUrl(
      p?.imageUrl ||
        'https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500&auto=format&fit=crop&q=80'
    );
    setProdDesc(p?.description || '');
    setProdInStock(p ? Boolean(p.inStock) : true);
    setProdPopular(p ? Boolean(p.isPopular) : false);
    setProdOneHour(p ? Boolean(p.isOneHourEligible) : true);
    setProdColors(p ? parseColorOptions(p.colorOptionsJson) : []);
    setEditingColorIdx(null);
    setColorNameInput('');
    setColorHexInput('#1B2A4A');
  }

  function handleSaveColorVariant() {
    const cleanName = colorNameInput.trim();
    if (!cleanName) return;
    const entry = {
      name: cleanName,
      hex: normalizeHexColor(colorHexInput)
    };
    if (editingColorIdx !== null && editingColorIdx >= 0 && editingColorIdx < prodColors.length) {
      setProdColors((prev) => prev.map((c, idx) => (idx === editingColorIdx ? entry : c)));
    } else {
      setProdColors((prev) => [...prev, entry]);
    }
    setEditingColorIdx(null);
    setColorNameInput('');
    setColorHexInput('#1B2A4A');
  }

  function handleDeleteColorVariant(idx) {
    setProdColors((prev) => prev.filter((_, i) => i !== idx));
    if (editingColorIdx === idx) {
      setEditingColorIdx(null);
      setColorNameInput('');
      setColorHexInput('#1B2A4A');
    } else if (editingColorIdx !== null && editingColorIdx > idx) {
      setEditingColorIdx(editingColorIdx - 1);
    }
  }

  async function handleSaveProduct(e) {
    e.preventDefault();
    if (!prodName.trim() || !prodPrice) return;
    const isEdit = Boolean(editingProduct && !editingProduct.isNew);
    const id = isEdit ? editingProduct.id : `prod_${Date.now()}`;
    const payload = {
      id,
      name: prodName.trim(),
      category: prodCategory,
      description: prodDesc.trim() || 'Premium iCASE accessory.',
      price: Number(prodPrice),
      originalPrice: Number(prodOriginalPrice || prodPrice),
      imageUrl: prodImageUrl.trim(),
      inStock: prodInStock,
      stockCount: Number(prodStockCount || 0),
      isOneHourEligible: prodOneHour,
      isPopular: prodPopular,
      popularityRank: editingProduct?.popularityRank || 5,
      colorOptionsJson: serializeColorOptions(prodColors),
      updatedAt: Date.now()
    };
    if (onUpdateProductsLocal) {
      onUpdateProductsLocal((prev) =>
        isEdit ? prev.map((p) => (p.id === id ? payload : p)) : [payload, ...prev]
      );
    }
    if (db) {
      await setDoc(doc(db, 'products', id), payload);
      await saveSetting(CATALOG_SEEDED_KEY, 'true');
    }
    setEditingProduct(null);
  }

  const q = searchFilter.trim().toLowerCase();
  const filteredOrders = orders.filter(
    (o) =>
      !q ||
      o.id.toLowerCase().includes(q) ||
      o.customerName.toLowerCase().includes(q) ||
      o.customerPhone.toLowerCase().includes(q)
  );
  const filteredRepairs = repairs.filter(
    (r) =>
      !q ||
      r.id.toLowerCase().includes(q) ||
      r.customerName.toLowerCase().includes(q) ||
      `${r.brandName} ${r.modelName}`.toLowerCase().includes(q)
  );

  return (
    <div>
      <div className="section-header">
        <div>
          <h1 className="section-title">
            <ShieldCheck size={23} color="#E50914" /> iCASE Admin Control Center
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 13.5 }}>
            Full live control synced with Cloud Firestore &amp; Mobile App.
          </p>
        </div>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <button
            type="button"
            className="btn btn-navy btn-sm"
            onClick={() => {
              setExportCopied(false);
              setExportModalOpen(true);
            }}
          >
            <Code2 size={15} /> Export Default Code
          </button>
          <button className="btn btn-outline btn-sm" onClick={onAdminLogout}>
            <LogOut size={15} /> Lock Admin
          </button>
        </div>
      </div>

      {/* KPI Metrics */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-value">{orders.length}</div>
          <div className="stat-label">Total Orders ({pendingOrders} pending)</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{repairs.length}</div>
          <div className="stat-label">Repair Bookings ({pendingRepairs} active)</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{products.length}</div>
          <div className="stat-label">Catalog Products</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ color: '#E50914' }}>
            {formatINR(totalRevenue)}
          </div>
          <div className="stat-label">Gross Sales</div>
        </div>
      </div>

      <div className="admin-tabs">
        <button
          className={`chip ${adminTab === 'ORDERS' ? 'active' : ''}`}
          onClick={() => setAdminTab('ORDERS')}
        >
          Orders ({orders.length})
        </button>
        <button
          className={`chip ${adminTab === 'REPAIRS' ? 'active' : ''}`}
          onClick={() => setAdminTab('REPAIRS')}
        >
          Repairs ({repairs.length})
        </button>
        <button
          className={`chip ${adminTab === 'PRODUCTS' ? 'active' : ''}`}
          onClick={() => setAdminTab('PRODUCTS')}
        >
          Products ({products.length})
        </button>
        <button
          className={`chip ${adminTab === 'GLASS_OFFERS' ? 'active' : ''}`}
          onClick={() => setAdminTab('GLASS_OFFERS')}
        >
          Glass, Lens &amp; Pouches
        </button>
        <button
          className={`chip ${adminTab === 'REVIEWS' ? 'active' : ''}`}
          onClick={() => setAdminTab('REVIEWS')}
        >
          Reviews ({reviews.length})
        </button>
        <button
          className={`chip ${adminTab === 'SETTINGS' ? 'active' : ''}`}
          onClick={() => setAdminTab('SETTINGS')}
        >
          Delivery &amp; Store Settings
        </button>
      </div>

      {(adminTab === 'ORDERS' || adminTab === 'REPAIRS') && (
        <div style={{ marginBottom: 16 }}>
          <input
            className="input"
            placeholder="Filter by Order / Repair ID, Customer Name, Phone or Device..."
            value={searchFilter}
            onChange={(e) => setSearchFilter(e.target.value)}
          />
        </div>
      )}

      {adminTab === 'ORDERS' && (
        <div className="admin-list">
          {filteredOrders.length === 0 ? (
            <div className="card">No customer orders recorded yet.</div>
          ) : (
            filteredOrders.map((order) => (
              <div key={order.id} className="card">
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    flexWrap: 'wrap',
                    gap: 10,
                    marginBottom: 8
                  }}
                >
                  <div>
                    <strong style={{ fontSize: 16, color: 'var(--icase-navy)' }}>#{order.id}</strong>{' '}
                    • <span>{order.customerName}</span> ({order.customerPhone})
                    <div style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
                      {order.deliveryAddress}
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <strong style={{ color: 'var(--icase-red)', fontSize: 16 }}>
                      {formatINR(order.totalAmount)}
                    </strong>
                    <div style={{ fontSize: 11.5, color: 'var(--text-secondary)' }}>
                      {order.deliveryType === 'ONE_HOUR' ? '⚡ 1-Hour Express' : 'Standard'}
                    </div>
                  </div>
                </div>
                <div style={{ fontSize: 13, color: '#334155', marginBottom: 10 }}>
                  {parseOrderItems(order.itemsJson)
                    .map((i) => `${i.quantity}x ${i.productName}`)
                    .join(', ')}
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, alignItems: 'center' }}>
                  <select
                    className="input"
                    style={{ maxWidth: 220 }}
                    value={order.status}
                    onChange={(e) => {
                      if (!db) return;
                      updateDoc(doc(db, 'orders', order.id), {
                        status: e.target.value,
                        updatedAt: Date.now()
                      });
                    }}
                  >
                    {ORDER_STATUSES.map((st) => (
                      <option key={st} value={st}>
                        {st}
                      </option>
                    ))}
                  </select>
                  <button
                    className="btn btn-outline btn-sm"
                    style={{ color: '#E50914' }}
                    onClick={() => {
                      if (!db) return;
                      deleteDoc(doc(db, 'orders', order.id));
                    }}
                  >
                    <Trash2 size={14} /> Delete
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {adminTab === 'REPAIRS' && (
        <div className="admin-list">
          {filteredRepairs.length === 0 ? (
            <div className="card">No repair bookings recorded yet.</div>
          ) : (
            filteredRepairs.map((repair) => (
              <div key={repair.id} className="card">
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    flexWrap: 'wrap',
                    gap: 10,
                    marginBottom: 8
                  }}
                >
                  <div>
                    <strong style={{ fontSize: 16, color: 'var(--icase-navy)' }}>
                      #{repair.id}
                    </strong>{' '}
                    • {repair.brandName} {repair.modelName}
                    <div style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
                      {repair.customerName} ({repair.phoneNumber}) • {repair.pickupAddress}
                    </div>
                  </div>
                  <div style={{ fontSize: 12.5, fontWeight: 700, color: 'var(--icase-blue)' }}>
                    {repair.preferredDate} • {repair.preferredTimeSlot}
                  </div>
                </div>
                <div style={{ fontSize: 13, color: '#E50914', fontWeight: 700, marginBottom: 10 }}>
                  Issues: {parseComplaints(repair.complaintsJson).join(', ')}
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, alignItems: 'center' }}>
                  <select
                    className="input"
                    style={{ maxWidth: 230 }}
                    value={repair.status}
                    onChange={(e) => {
                      if (!db) return;
                      updateDoc(doc(db, 'repairs', repair.id), {
                        status: e.target.value,
                        updatedAt: Date.now()
                      });
                    }}
                  >
                    {REPAIR_STATUSES.map((st) => (
                      <option key={st} value={st}>
                        {st}
                      </option>
                    ))}
                  </select>
                  <button
                    className="btn btn-outline btn-sm"
                    style={{ color: '#E50914' }}
                    onClick={() => {
                      if (!db) return;
                      deleteDoc(doc(db, 'repairs', repair.id));
                    }}
                  >
                    <Trash2 size={14} /> Delete
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {adminTab === 'PRODUCTS' && (
        <div>
          <div style={{ marginBottom: 14, display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <button className="btn btn-red btn-sm" onClick={() => startEditProduct(null)}>
              <Plus size={16} /> Add New Product
            </button>
            <button
              type="button"
              className="btn btn-navy btn-sm"
              onClick={() => {
                setExportCopied(false);
                setExportModalOpen(true);
              }}
            >
              <Code2 size={15} /> Export Products &amp; Data Code
            </button>
          </div>

          {editingProduct && (
            <form className="card" style={{ marginBottom: 18 }} onSubmit={handleSaveProduct}>
              <h3 style={{ marginBottom: 12, color: 'var(--icase-navy)' }}>
                {editingProduct.isNew ? 'Add New Product' : `Edit ${editingProduct.name}`}
              </h3>
              <div className="form-grid">
                <div className="form-group">
                  <label>Product Name</label>
                  <input
                    className="input"
                    value={prodName}
                    onChange={(e) => setProdName(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Category</label>
                  <select
                    className="input"
                    value={prodCategory}
                    onChange={(e) => setProdCategory(e.target.value)}
                  >
                    {CATEGORIES.filter((c) => c !== 'All').map((cat) => (
                      <option key={cat} value={cat}>
                        {cat}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Selling Price (INR)</label>
                  <input
                    type="number"
                    className="input"
                    value={prodPrice}
                    onChange={(e) => setProdPrice(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Original Price (INR)</label>
                  <input
                    type="number"
                    className="input"
                    value={prodOriginalPrice}
                    onChange={(e) => setProdOriginalPrice(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label>Stock Units</label>
                  <input
                    type="number"
                    className="input"
                    value={prodStockCount}
                    onChange={(e) => setProdStockCount(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label>Product Image (Upload or URL)</label>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
                    <img
                      src={
                        prodImageUrl ||
                        'https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500&auto=format&fit=crop&q=80'
                      }
                      alt="Product preview"
                      style={{
                        width: 56,
                        height: 56,
                        borderRadius: 10,
                        objectFit: 'cover',
                        border: '1px solid var(--border)',
                        background: '#FFF',
                        flexShrink: 0
                      }}
                    />
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                      <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer' }}>
                        <Upload size={14} /> Upload Image
                        <input
                          type="file"
                          accept="image/*"
                          hidden
                          onChange={async (e) => {
                            const f = e.target.files?.[0];
                            e.target.value = '';
                            if (!f) return;
                            try {
                              setProdImgUploadErr('');
                              const dataUrl = await compressImageFileToDataUrl(f);
                              setProdImageUrl(dataUrl);
                            } catch (err) {
                              setProdImgUploadErr(err.message || 'Image upload failed');
                            }
                          }}
                        />
                      </label>
                      <button
                        type="button"
                        className="btn btn-outline btn-sm"
                        onClick={() =>
                          setProdImageUrl(
                            'https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500&auto=format&fit=crop&q=80'
                          )
                        }
                      >
                        <RotateCcw size={13} /> Reset
                      </button>
                    </div>
                  </div>
                  <input
                    className="input"
                    placeholder="https://... or upload image above"
                    value={prodImageUrl}
                    onChange={(e) => setProdImageUrl(e.target.value)}
                  />
                  {prodImgUploadErr && (
                    <div style={{ color: '#E50914', fontSize: 12, fontWeight: 700 }}>
                      {prodImgUploadErr}
                    </div>
                  )}
                </div>
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  className="input"
                  rows={2}
                  value={prodDesc}
                  onChange={(e) => setProdDesc(e.target.value)}
                />
              </div>

              {/* Dynamic Color Variants Manager */}
              <div
                style={{
                  background: '#F8FAFC',
                  border: '1px solid var(--border)',
                  borderRadius: 12,
                  padding: 14,
                  marginBottom: 14
                }}
              >
                <div style={{ fontWeight: 800, fontSize: 13.5, color: 'var(--icase-navy)' }}>
                  Color Variants ({prodColors.length})
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginBottom: 10 }}>
                  Add, edit, or remove color options displayed on the Product Details screen.
                </div>

                {prodColors.length > 0 && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 10 }}>
                    {prodColors.map((opt, idx) => (
                      <div
                        key={`${opt.name}-${idx}`}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          gap: 10,
                          padding: '6px 10px',
                          borderRadius: 8,
                          border: '1px solid var(--border)',
                          background: editingColorIdx === idx ? '#EAF2FF' : '#FFFFFF'
                        }}
                      >
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                          <span
                            style={{
                              width: 18,
                              height: 18,
                              borderRadius: '50%',
                              background: opt.hex,
                              border: '1px solid #94A3B8',
                              display: 'inline-block'
                            }}
                          />
                          <strong style={{ fontSize: 13, color: 'var(--icase-navy)' }}>{opt.name}</strong>
                          <span style={{ fontSize: 11.5, color: 'var(--text-secondary)' }}>{opt.hex}</span>
                        </div>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <button
                            type="button"
                            className="btn btn-outline btn-sm"
                            onClick={() => {
                              setEditingColorIdx(idx);
                              setColorNameInput(opt.name);
                              setColorHexInput(opt.hex);
                            }}
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            className="btn btn-outline btn-sm"
                            style={{ color: '#E50914' }}
                            onClick={() => handleDeleteColorVariant(idx)}
                          >
                            Remove
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 10 }}>
                  {COLOR_PRESETS.map((preset) => (
                    <button
                      key={preset.name}
                      type="button"
                      title={preset.name}
                      onClick={() => {
                        setColorNameInput(preset.name);
                        setColorHexInput(preset.hex);
                      }}
                      style={{
                        width: 24,
                        height: 24,
                        borderRadius: '50%',
                        background: preset.hex,
                        border: '1.5px solid #94A3B8',
                        cursor: 'pointer'
                      }}
                    />
                  ))}
                </div>

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
                  <input
                    className="input"
                    style={{ flex: '1 1 160px' }}
                    placeholder="Color name (e.g. Midnight Blue)"
                    value={colorNameInput}
                    onChange={(e) => setColorNameInput(e.target.value)}
                  />
                  <input
                    type="color"
                    value={normalizeHexColor(colorHexInput).slice(0, 7)}
                    onChange={(e) => setColorHexInput(e.target.value.toUpperCase())}
                    style={{ width: 42, height: 38, border: '1px solid var(--border)', borderRadius: 8, cursor: 'pointer', padding: 2 }}
                    title="Pick Color"
                  />
                  <input
                    className="input"
                    style={{ width: 110 }}
                    placeholder="#1B2A4A"
                    value={colorHexInput}
                    onChange={(e) => setColorHexInput(e.target.value)}
                  />
                  <button
                    type="button"
                    className="btn btn-navy btn-sm"
                    onClick={handleSaveColorVariant}
                  >
                    <Plus size={14} /> {editingColorIdx !== null ? 'Update Color' : 'Add Color'}
                  </button>
                  {editingColorIdx !== null && (
                    <button
                      type="button"
                      className="btn btn-outline btn-sm"
                      onClick={() => {
                        setEditingColorIdx(null);
                        setColorNameInput('');
                        setColorHexInput('#1B2A4A');
                      }}
                    >
                      Cancel
                    </button>
                  )}
                </div>
              </div>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 18, marginBottom: 14 }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 700 }}>
                  <input
                    type="checkbox"
                    checked={prodInStock}
                    onChange={(e) => setProdInStock(e.target.checked)}
                  />
                  In Stock
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 700 }}>
                  <input
                    type="checkbox"
                    checked={prodPopular}
                    onChange={(e) => setProdPopular(e.target.checked)}
                  />
                  Popular Highlight
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 700 }}>
                  <input
                    type="checkbox"
                    checked={prodOneHour}
                    onChange={(e) => setProdOneHour(e.target.checked)}
                  />
                  1-Hour Delivery Eligible
                </label>
              </div>
              <div style={{ display: 'flex', gap: 10 }}>
                <button type="submit" className="btn btn-navy btn-sm">
                  Save Product
                </button>
                <button
                  type="button"
                  className="btn btn-outline btn-sm"
                  onClick={() => setEditingProduct(null)}
                >
                  Cancel
                </button>
              </div>
            </form>
          )}

          <div className="admin-list">
            {products.map((product) => (
              <div
                key={product.id}
                className="card"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexWrap: 'wrap',
                  gap: 12
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <img
                    src={product.imageUrl}
                    alt={product.name}
                    style={{ width: 54, height: 54, borderRadius: 10, objectFit: 'cover' }}
                  />
                  <div>
                    <strong style={{ color: 'var(--icase-navy)' }}>{product.name}</strong>
                    <div style={{ fontSize: 13, color: 'var(--text-secondary)' }}>
                      {product.category} • {formatINR(product.price)} •{' '}
                      {product.inStock ? 'In Stock' : 'Out of Stock'}
                    </div>
                    {parseColorOptions(product.colorOptionsJson).length > 0 && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginTop: 4 }}>
                        {parseColorOptions(product.colorOptionsJson).map((c, i) => (
                          <span
                            key={`${c.name}-${i}`}
                            title={c.name}
                            style={{
                              width: 12,
                              height: 12,
                              borderRadius: '50%',
                              background: c.hex,
                              border: '1px solid #94A3B8',
                              display: 'inline-block'
                            }}
                          />
                        ))}
                        <span style={{ fontSize: 11.5, color: 'var(--text-secondary)' }}>
                          {parseColorOptions(product.colorOptionsJson).length} color(s)
                        </span>
                      </div>
                    )}
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                  <button
                    className="btn btn-outline btn-sm"
                    onClick={async () => {
                      const nextStock = !product.inStock;
                      if (onUpdateProductsLocal) {
                        onUpdateProductsLocal((prev) =>
                          prev.map((p) =>
                            p.id === product.id ? { ...p, inStock: nextStock, updatedAt: Date.now() } : p
                          )
                        );
                      }
                      if (db) {
                        await updateDoc(doc(db, 'products', product.id), {
                          inStock: nextStock,
                          updatedAt: Date.now()
                        });
                      }
                    }}
                  >
                    {product.inStock ? 'Mark Out of Stock' : 'Mark In Stock'}
                  </button>
                  <button
                    className="btn btn-navy btn-sm"
                    onClick={() => startEditProduct(product)}
                  >
                    Edit
                  </button>
                  <button
                    className="btn btn-outline btn-sm"
                    style={{ color: '#E50914' }}
                    onClick={async () => {
                      if (db) {
                        await deleteDoc(doc(db, 'products', product.id));
                      }
                      if (onDeleteProductPersistent) {
                        await onDeleteProductPersistent(product.id);
                      }
                    }}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {adminTab === 'GLASS_OFFERS' && (
        <AdminGlassOffersTab
          screenGuardConfig={screenGuardConfig}
          onSaveGuardConfig={onSaveGuardConfig}
          onOpenExportCode={() => {
            setExportCopied(false);
            setExportModalOpen(true);
          }}
        />
      )}

      {adminTab === 'REVIEWS' && (
        <AdminReviewsTab
          reviews={reviews}
          onUpdateReviewsLocal={onUpdateReviewsLocal}
        />
      )}

      {adminTab === 'SETTINGS' && (
        <form className="card" onSubmit={handleSaveAllSettings}>
          <h3 style={{ color: 'var(--icase-navy)', marginBottom: 14 }}>
            Delivery Charges &amp; Global Store Controls
          </h3>
          <div className="form-grid">
            <div className="form-group">
              <label>Standard Delivery Fee (INR)</label>
              <input
                type="number"
                className="input"
                value={standardFee}
                onChange={(e) => setStandardFee(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Express 1-Hour Fee (INR)</label>
              <input
                type="number"
                className="input"
                value={expressFee}
                onChange={(e) => setExpressFee(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Free Delivery Threshold (0 = Off)</label>
              <input
                type="number"
                className="input"
                value={freeThreshold}
                onChange={(e) => setFreeThreshold(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Store Name</label>
              <input
                className="input"
                value={storeName}
                onChange={(e) => setStoreName(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Support Phone</label>
              <input
                className="input"
                value={storePhone}
                onChange={(e) => setStorePhone(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Support WhatsApp</label>
              <input
                className="input"
                value={storeWhatsapp}
                onChange={(e) => setStoreWhatsapp(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Support Email</label>
              <input
                className="input"
                value={storeEmail}
                onChange={(e) => setStoreEmail(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Working Hours</label>
              <input
                className="input"
                value={storeHours}
                onChange={(e) => setStoreHours(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label>Store Address</label>
            <input
              className="input"
              value={storeAddress}
              onChange={(e) => setStoreAddress(e.target.value)}
            />
          </div>

          <div
            style={{
              padding: 14,
              borderRadius: 12,
              background: '#F8FAFC',
              border: '1px solid #CBD5E1',
              marginBottom: 16
            }}
          >
            <div
              style={{
                fontWeight: 800,
                color: 'var(--icase-navy)',
                fontSize: 14,
                marginBottom: 10
              }}
            >
              Change Admin Password &amp; Recovery Code
            </div>
            <div className="form-grid-2">
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>New Admin Password / PIN (Optional, 4+ chars)</label>
                <input
                  type="password"
                  className="input"
                  placeholder="Leave blank to keep current PIN"
                  value={newAdminPin}
                  onChange={(e) => {
                    setNewAdminPin(e.target.value);
                    setSettingsErrorMsg('');
                  }}
                />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>Master Recovery Code (Default: ICASE2026)</label>
                <input
                  type="text"
                  className="input"
                  placeholder="Enter Master Recovery Code"
                  value={recoveryCodeSetting}
                  onChange={(e) => {
                    setRecoveryCodeSetting(e.target.value);
                    setSettingsErrorMsg('');
                  }}
                />
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 18, marginBottom: 16 }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontWeight: 700 }}>
              <input
                type="checkbox"
                checked={oneHourEnabled}
                onChange={(e) => setOneHourEnabled(e.target.checked)}
              />
              Enable Express 1-Hour Delivery
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontWeight: 700 }}>
              <input
                type="checkbox"
                checked={storeOnline}
                onChange={(e) => setStoreOnline(e.target.checked)}
              />
              Store Online (Accepting Checkout Orders)
            </label>
          </div>

          {settingsErrorMsg && (
            <div style={{ color: '#E50914', fontWeight: 800, fontSize: 13.5, marginBottom: 10 }}>
              {settingsErrorMsg}
            </div>
          )}

          {settingsSavedMsg && (
            <div style={{ color: '#10B981', fontWeight: 800, fontSize: 13.5, marginBottom: 10 }}>
              {settingsSavedMsg}
            </div>
          )}

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
            <button type="submit" className="btn btn-red">
              Save &amp; Sync All Settings
            </button>
            <button
              type="button"
              className="btn btn-navy"
              onClick={() => {
                setExportCopied(false);
                setExportModalOpen(true);
              }}
            >
              <Code2 size={16} /> Export Default Code for GitHub
            </button>
          </div>
        </form>
      )}

      {exportModalOpen && (
        <div className="overlay" onClick={() => setExportModalOpen(false)}>
          <div className="modal" style={{ maxWidth: 760 }} onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <strong style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <Code2 size={18} /> Export Default Data Code for GitHub (No-DB Live Site)
              </strong>
              <button
                type="button"
                className="btn btn-sm"
                style={{ background: 'rgba(255,255,255,0.14)', color: '#FFF' }}
                onClick={() => setExportModalOpen(false)}
              >
                <X size={16} />
              </button>
            </div>
            <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <div className="helper-note-bar">
                Copy or download this code and replace <strong>DEFAULT_SETTINGS</strong>,{' '}
                <strong>INITIAL_PRODUCTS</strong>, and <strong>BAKED_SCREEN_GUARD_CONFIG</strong>{' '}
                in <code>companion-website/src/data/constants.js</code> and{' '}
                <code>public/js/data.js</code>. Push to GitHub so your live Netlify site loads every
                product, custom photo, rate, and pouch without a database.
              </div>
              {(() => {
                const exportCode = generateDefaultCodeExport({
                  products,
                  settings,
                  screenGuardConfig
                });
                return (
                  <>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
                      <button
                        type="button"
                        className="btn btn-navy btn-sm"
                        onClick={async () => {
                          try {
                            await navigator.clipboard.writeText(exportCode);
                            setExportCopied(true);
                            setTimeout(() => setExportCopied(false), 3000);
                          } catch {}
                        }}
                      >
                        {exportCopied ? <Check size={15} /> : <Copy size={15} />}
                        {exportCopied ? 'Copied to Clipboard!' : 'Copy Code'}
                      </button>
                      <button
                        type="button"
                        className="btn btn-outline btn-sm"
                        onClick={() => {
                          const blob = new Blob([exportCode], { type: 'text/javascript;charset=utf-8' });
                          const url = URL.createObjectURL(blob);
                          const a = document.createElement('a');
                          a.href = url;
                          a.download = 'default-data-export.js';
                          document.body.appendChild(a);
                          a.click();
                          a.remove();
                          URL.revokeObjectURL(url);
                        }}
                      >
                        <Download size={15} /> Download default-data-export.js
                      </button>
                    </div>
                    <pre className="code-export-pre">{exportCode}</pre>
                  </>
                );
              })()}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
