import React, { useEffect, useState } from 'react';
import {
  X,
  CheckCircle,
  LogOut,
  ShoppingBag,
  Wrench,
  User,
  Navigation,
  Trash2
} from 'lucide-react';
import {
  auth,
  googleProvider,
  signInWithPopup,
  signOut
} from '../firebase';
import {
  formatINR,
  isOrderCompleted,
  isRepairCompleted,
  parseComplaints,
  parseOrderItems
} from '../data/constants';
import { detectCurrentLocation } from '../data/locationHelper';

export default function AccountModal({
  isOpen,
  onClose,
  profile,
  products = [],
  myOrders,
  myRepairs,
  onSaveProfile,
  onToggleWishlist,
  onAddToCart,
  onLogout,
  onTrackId,
  onOpenReview
}) {
  const [tab, setTab] = useState('PROFILE'); // 'PROFILE' | 'ORDERS' | 'REPAIRS' | 'WISHLIST'
  const [name, setName] = useState(profile.name || '');
  const [phone, setPhone] = useState(profile.phone || '');
  const [whatsapp, setWhatsapp] = useState(profile.whatsapp || '');
  const [address, setAddress] = useState(profile.address || '');
  const [detectingGps, setDetectingGps] = useState(false);

  const [busy, setBusy] = useState(false);
  const [statusMsg, setStatusMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    setName(profile.name || '');
    setPhone(profile.phone || '');
    setWhatsapp(profile.whatsapp || '');
    setAddress(profile.address || '');
  }, [profile]);

  if (!isOpen) return null;

  async function handleGoogleLogin() {
    setErrorMsg('');
    setStatusMsg('');
    if (!auth || !googleProvider) {
      setErrorMsg('Google Sign-In is unavailable offline.');
      return;
    }
    setBusy(true);
    try {
      const result = await signInWithPopup(auth, googleProvider);
      const fbUser = result.user;
      await onSaveProfile({
        userId: fbUser.uid,
        name: name.trim() || fbUser.displayName || 'Customer',
        email: fbUser.email || profile.email || '',
        phone: phone.trim() || fbUser.phoneNumber || profile.phone || '',
        whatsapp: whatsapp.trim() || fbUser.phoneNumber || profile.whatsapp || '',
        address: address.trim() || profile.address || '',
        isGoogleSignedIn: true
      });
      setStatusMsg('Google account synced with iCASE cloud.');
    } catch (err) {
      setErrorMsg(err.message || 'Google Sign-In failed.');
    } finally {
      setBusy(false);
    }
  }

  async function handleSaveDetails(e) {
    e.preventDefault();
    setErrorMsg('');
    setStatusMsg('');

    if (!name.trim()) {
      setErrorMsg('Please enter your full name.');
      return;
    }
    const cleanPhone = phone.trim();
    const digits = cleanPhone.replace(/\D/g, '');
    if (digits.length > 0 && digits.length < 10) {
      setErrorMsg('Please enter a valid 10-digit mobile number.');
      return;
    }

    setBusy(true);
    try {
      await onSaveProfile({
        userId: profile.userId,
        name: name.trim(),
        email: profile.email || '',
        phone: cleanPhone,
        whatsapp: whatsapp.trim() || cleanPhone,
        address: address.trim(),
        isGoogleSignedIn: profile.isGoogleSignedIn
      });
      setStatusMsg('Profile updated & synced with Mobile App!');
    } catch (err) {
      setErrorMsg(err.message || 'Failed to save profile.');
    } finally {
      setBusy(false);
    }
  }

  async function handleLogout() {
    try {
      if (auth) await signOut(auth);
    } catch {}
    onLogout();
    onClose();
    setStatusMsg('');
    setErrorMsg('');
  }

  return (
    <div className="overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <User size={19} />
            <strong>
              {profile.isLoggedIn ? profile.name || 'iCASE Account' : 'Sign in with Google'}
            </strong>
          </div>
          <button
            className="btn btn-sm"
            style={{ background: 'rgba(255,255,255,0.14)', color: '#FFF' }}
            onClick={onClose}
          >
            <X size={16} />
          </button>
        </div>

        <div className="modal-body">
          <div className="admin-tabs" style={{ marginBottom: 16 }}>
            <button
              className={`chip ${tab === 'PROFILE' ? 'active' : ''}`}
              onClick={() => setTab('PROFILE')}
            >
              Profile &amp; Account
            </button>
            <button
              className={`chip ${tab === 'ORDERS' ? 'active' : ''}`}
              onClick={() => setTab('ORDERS')}
            >
              My Orders ({myOrders.length})
            </button>
            <button
              className={`chip ${tab === 'REPAIRS' ? 'active' : ''}`}
              onClick={() => setTab('REPAIRS')}
            >
              My Repairs ({myRepairs.length})
            </button>
            <button
              className={`chip ${tab === 'WISHLIST' ? 'active' : ''}`}
              onClick={() => setTab('WISHLIST')}
            >
              Wishlist ({(profile.wishlistIds || []).length})
            </button>
          </div>

          {tab === 'PROFILE' && (
            <div>
              {profile.isGoogleSignedIn && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 14 }}>
                  <span className="status-pill delivered">
                    <CheckCircle size={13} style={{ marginRight: 4 }} /> Google Signed In
                  </span>
                  {profile.email && (
                    <span style={{ fontSize: 13, color: 'var(--text-secondary)', fontWeight: 600 }}>
                      {profile.email}
                    </span>
                  )}
                </div>
              )}

              <form onSubmit={handleSaveDetails}>
                <div className="form-group">
                  <label>Full Name</label>
                  <input
                    className="input"
                    placeholder="Your Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label>Mobile Number (for Orders &amp; Pickups)</label>
                  <input
                    className="input"
                    placeholder="10-digit mobile number"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label>WhatsApp Number (Optional)</label>
                  <input
                    className="input"
                    placeholder="WhatsApp number"
                    value={whatsapp}
                    onChange={(e) => setWhatsapp(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      gap: 8,
                      marginBottom: 6,
                      flexWrap: 'wrap'
                    }}
                  >
                    <label style={{ margin: 0 }}>Delivery Address</label>
                    <button
                      type="button"
                      className="btn btn-sm"
                      style={{ background: '#EAF2FF', color: '#0066CC', fontWeight: 700 }}
                      disabled={detectingGps}
                      onClick={async () => {
                        setDetectingGps(true);
                        setErrorMsg('');
                        setStatusMsg('Detecting live GPS location...');
                        try {
                          const res = await detectCurrentLocation();
                          setAddress(res.formattedAddress);
                          setStatusMsg('Live location detected via GPS.');
                        } catch (err) {
                          setStatusMsg('');
                          setErrorMsg(err.message || 'Could not detect GPS location.');
                        } finally {
                          setDetectingGps(false);
                        }
                      }}
                    >
                      <Navigation size={13} />
                      {detectingGps ? 'Detecting...' : 'Use GPS / Detect Current Location'}
                    </button>
                  </div>
                  <textarea
                    className="input"
                    rows={2}
                    placeholder="House / Flat No., Street, Landmark, City, PIN"
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                  />
                </div>

                {statusMsg && (
                  <div style={{ color: '#10B981', fontWeight: 700, fontSize: 13, marginBottom: 10 }}>
                    {statusMsg}
                  </div>
                )}
                {errorMsg && (
                  <div style={{ color: '#E50914', fontWeight: 700, fontSize: 13, marginBottom: 10 }}>
                    {errorMsg}
                  </div>
                )}

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
                  {profile.isGoogleSignedIn ? (
                    <button type="submit" className="btn btn-red" disabled={busy}>
                      {busy ? 'Saving...' : 'Save Profile'}
                    </button>
                  ) : (
                    <button
                      type="button"
                      className="btn btn-red"
                      disabled={busy}
                      onClick={handleGoogleLogin}
                    >
                      Sign in with Google
                    </button>
                  )}
                  {profile.isLoggedIn && (
                    <button
                      type="button"
                      className="btn btn-outline"
                      onClick={handleLogout}
                    >
                      <LogOut size={15} /> Logout
                    </button>
                  )}
                </div>
              </form>
            </div>
          )}

          {tab === 'ORDERS' && (
            <div>
              {!profile.isLoggedIn ? (
                <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
                  Sign in with Google first to view your personal order history synced
                  with the Mobile App.
                </p>
              ) : myOrders.length === 0 ? (
                <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
                  No orders found on your account yet. Orders placed from Website or Mobile App
                  appear here live.
                </p>
              ) : (
                <div className="admin-list">
                  {myOrders.map((order) => (
                    <div key={order.id} className="card" style={{ padding: 14 }}>
                      <div
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          marginBottom: 6
                        }}
                      >
                        <strong style={{ color: 'var(--icase-navy)' }}>
                          <ShoppingBag size={14} style={{ marginRight: 5 }} />#{order.id}
                        </strong>
                        <span
                          className={`status-pill ${order.status === 'Delivered' ? 'delivered' : 'active'}`}
                        >
                          {order.status}
                        </span>
                      </div>
                      <div style={{ fontSize: 13, color: '#475569', marginBottom: 6 }}>
                        {parseOrderItems(order.itemsJson)
                          .map((i) => `${i.quantity}x ${i.productName}`)
                          .join(', ')}
                      </div>
                      <div
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center'
                        }}
                      >
                        <strong style={{ color: 'var(--icase-red)' }}>
                          {formatINR(order.totalAmount)}
                        </strong>
                        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                          {onOpenReview && isOrderCompleted(order.status) && (
                            <button
                              type="button"
                              className="section-link"
                              style={{ color: 'var(--icase-green)' }}
                              onClick={() => {
                                onClose();
                                onOpenReview(order.id);
                              }}
                            >
                              Write Review ★
                            </button>
                          )}
                          <button
                            className="section-link"
                            onClick={() => {
                              onClose();
                              onTrackId(order.id);
                            }}
                          >
                            Track Live →
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {tab === 'REPAIRS' && (
            <div>
              {!profile.isLoggedIn ? (
                <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
                  Sign in with Google first to view your repair bookings.
                </p>
              ) : myRepairs.length === 0 ? (
                <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
                  No repair bookings on your account yet.
                </p>
              ) : (
                <div className="admin-list">
                  {myRepairs.map((repair) => (
                    <div key={repair.id} className="card" style={{ padding: 14 }}>
                      <div
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          marginBottom: 6
                        }}
                      >
                        <strong style={{ color: 'var(--icase-navy)' }}>
                          <Wrench size={14} style={{ marginRight: 5 }} />#{repair.id}
                        </strong>
                        <span
                          className={`status-pill ${repair.status === 'Delivered' ? 'delivered' : 'active'}`}
                        >
                          {repair.status}
                        </span>
                      </div>
                      <div style={{ fontSize: 13, color: '#475569', marginBottom: 6 }}>
                        {repair.brandName} {repair.modelName} (
                        {parseComplaints(repair.complaintsJson).join(', ')})
                      </div>
                      <div
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center'
                        }}
                      >
                        <span style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
                          {repair.preferredDate} • {repair.preferredTimeSlot}
                        </span>
                        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                          {onOpenReview && isRepairCompleted(repair.status) && (
                            <button
                              type="button"
                              className="section-link"
                              style={{ color: 'var(--icase-blue)' }}
                              onClick={() => {
                                onClose();
                                onOpenReview(repair.id);
                              }}
                            >
                              Service Feedback ★
                            </button>
                          )}
                          <button
                            className="section-link"
                            onClick={() => {
                              onClose();
                              onTrackId(repair.id);
                            }}
                          >
                            Track Live →
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {tab === 'WISHLIST' && (
            <div>
              {products.filter((p) => (profile.wishlistIds || []).includes(p.id)).length === 0 ? (
                <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
                  Your Wishlist is empty. Tap the heart icon on any product to save it across Web
                  and Mobile App.
                </p>
              ) : (
                <div className="admin-list">
                  {products
                    .filter((p) => (profile.wishlistIds || []).includes(p.id))
                    .map((product) => (
                      <div
                        key={product.id}
                        className="card"
                        style={{
                          padding: 12,
                          display: 'flex',
                          alignItems: 'center',
                          gap: 12
                        }}
                      >
                        <img
                          src={product.imageUrl}
                          alt={product.name}
                          style={{ width: 56, height: 56, borderRadius: 8, objectFit: 'cover' }}
                        />
                        <div style={{ flex: 1, minWidth: 0 }}>
                          <div
                            style={{
                              fontWeight: 700,
                              color: 'var(--icase-navy)',
                              fontSize: 14,
                              whiteSpace: 'nowrap',
                              overflow: 'hidden',
                              textOverflow: 'ellipsis'
                            }}
                          >
                            {product.name}
                          </div>
                          <div style={{ fontWeight: 800, color: 'var(--icase-red)', fontSize: 14 }}>
                            {formatINR(product.price)}
                          </div>
                        </div>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <button
                            type="button"
                            className="btn btn-navy btn-sm"
                            disabled={!product.inStock}
                            onClick={() => onAddToCart && onAddToCart(product)}
                          >
                            Add
                          </button>
                          <button
                            type="button"
                            className="btn btn-sm"
                            style={{ background: '#FFECEE', color: '#E50914' }}
                            onClick={() => onToggleWishlist && onToggleWishlist(product.id)}
                            title="Remove from Wishlist"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </div>
                    ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
