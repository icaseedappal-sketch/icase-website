import React, { useEffect, useState } from 'react';
import {
  X,
  Plus,
  Minus,
  Trash2,
  ShoppingBag,
  CheckCircle,
  MessageCircle,
  Search,
  Navigation
} from 'lucide-react';
import { doc, setDoc } from 'firebase/firestore';
import { db } from '../firebase';
import { formatINR, normalizePhoneToE164 } from '../data/constants';
import { detectCurrentLocation } from '../data/locationHelper';

export default function CartDrawer({
  isOpen,
  onClose,
  cartItems,
  onUpdateQty,
  onRemoveItem,
  onClearCart,
  settings,
  profile,
  resolvedUserId,
  onEnsureProfile,
  onOpenAccount,
  onTrackOrder
}) {
  const [customerName, setCustomerName] = useState(profile.name || '');
  const [customerPhone, setCustomerPhone] = useState(profile.phone || '');
  const [deliveryAddress, setDeliveryAddress] = useState(profile.address || '');
  const [deliveryType, setDeliveryType] = useState('STANDARD');
  const [paymentMethod, setPaymentMethod] = useState('UPI');
  const [error, setError] = useState('');
  const [isPlacing, setIsPlacing] = useState(false);
  const [placedOrder, setPlacedOrder] = useState(null);
  const [detectingGps, setDetectingGps] = useState(false);
  const [gpsMessage, setGpsMessage] = useState('');

  useEffect(() => {
    if (profile.name && !customerName) setCustomerName(profile.name);
    if (profile.phone && !customerPhone) setCustomerPhone(profile.phone);
    if (profile.address && !deliveryAddress) setDeliveryAddress(profile.address);
  }, [profile]);

  if (!isOpen) return null;

  const isStoreOnline = settings.store_online !== 'false';
  const oneHourEnabled = settings.one_hour_delivery_enabled !== 'false';
  const standardFee = Number(settings.delivery_fee_standard || 50);
  const expressFee = Number(settings.delivery_fee_express || 80);
  const freeThreshold = Number(settings.free_delivery_threshold || 0);

  const allOneHourEligible =
    cartItems.length > 0 && cartItems.every((item) => item.product.isOneHourEligible);
  const canChooseOneHour = oneHourEnabled && allOneHourEligible;
  const activeDeliveryType = canChooseOneHour ? deliveryType : 'STANDARD';

  const subtotal = cartItems.reduce(
    (sum, item) => sum + Number(item.product.price) * item.quantity,
    0
  );
  const freeStandardUnlocked = freeThreshold > 0 && subtotal >= freeThreshold;
  const deliveryFee =
    cartItems.length === 0
      ? 0
      : activeDeliveryType === 'ONE_HOUR'
        ? expressFee
        : freeStandardUnlocked
          ? 0
          : standardFee;
  const totalAmount = cartItems.length === 0 ? 0 : subtotal + deliveryFee;

  async function handlePlaceOrder(e) {
    e.preventDefault();
    setError('');

    if (!isStoreOnline) {
      setError('Store checkout is currently paused by Admin.');
      return;
    }
    if (!customerName.trim() || !deliveryAddress.trim()) {
      setError('Please enter your Full Name and Delivery Address.');
      return;
    }
    const normalizedPhone = normalizePhoneToE164(customerPhone);
    if (!normalizedPhone) {
      setError('Please enter a valid 10-digit mobile number.');
      return;
    }

    setIsPlacing(true);
    try {
      const uid = resolvedUserId || `acct_${normalizedPhone.replace(/\D/g, '')}`;
      await onEnsureProfile({
        userId: uid,
        name: customerName.trim(),
        phone: normalizedPhone,
        whatsapp: profile.whatsapp || normalizedPhone,
        address: deliveryAddress.trim()
      });

      const orderId = `ORD-${Math.floor(10000 + Math.random() * 90000)}`;
      const itemsJson = JSON.stringify(
        cartItems.map((item) => ({
          productId: item.product.id,
          productName: item.selectedColor
            ? `${item.product.name} (${item.selectedColor})`
            : item.product.name,
          selectedFinish: item.selectedColor || '',
          price: Number(item.product.price),
          quantity: item.quantity,
          imageUrl: item.product.imageUrl
        }))
      );

      const now = Date.now();
      const orderPayload = {
        id: orderId,
        userId: uid,
        customerName: customerName.trim(),
        customerPhone: normalizedPhone,
        whatsappNumber: profile.whatsapp || normalizedPhone,
        deliveryAddress: deliveryAddress.trim(),
        deliveryType: activeDeliveryType,
        subtotal,
        deliveryFee,
        totalAmount,
        status: 'Order Received',
        statusNotes:
          activeDeliveryType === 'ONE_HOUR'
            ? 'Express 1-Hour Delivery Order Received (Website)'
            : 'Standard Home Delivery Order Received (Website)',
        itemsJson,
        createdAt: now,
        updatedAt: now
      };

      if (db) {
        await setDoc(doc(db, 'orders', orderId), orderPayload);
      }
      onClearCart();
      setPlacedOrder(orderPayload);
    } catch (err) {
      setError(err.message || 'Failed to place order. Please retry.');
    } finally {
      setIsPlacing(false);
    }
  }

  function buildWhatsAppUrl(order) {
    const storeWa = (settings.store_whatsapp || settings.store_phone || '9961454059').replace(
      /\D/g,
      ''
    );
    const text = `*iCASE Order Confirmation* 🛍️\nOrder ID: *${order.id}*\nCustomer: ${order.customerName}\nPhone: ${order.customerPhone}\nAddress: ${order.deliveryAddress}\nTotal Amount: *${formatINR(order.totalAmount)}*\nStatus: ${order.status}`;
    return `https://api.whatsapp.com/send?phone=91${storeWa}&text=${encodeURIComponent(text)}`;
  }

  return (
    <div className="overlay drawer-overlay" onClick={onClose}>
      <aside className="drawer" onClick={(e) => e.stopPropagation()}>
        <div className="drawer-header">
          <strong>My Cart &amp; Checkout ({cartItems.reduce((a, b) => a + b.quantity, 0)})</strong>
          <button
            className="btn btn-sm"
            style={{ background: 'rgba(255,255,255,0.14)', color: '#FFF' }}
            onClick={onClose}
          >
            <X size={17} />
          </button>
        </div>

        <div className="drawer-body">
          {placedOrder ? (
            <div style={{ textAlign: 'center', padding: '24px 8px' }}>
              <CheckCircle size={52} color="#10B981" style={{ marginBottom: 12 }} />
              <h2 style={{ color: 'var(--icase-navy)', marginBottom: 6 }}>Order Placed!</h2>
              <div style={{ color: 'var(--icase-red)', fontWeight: 900, fontSize: 18, marginBottom: 8 }}>
                #{placedOrder.id}
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginBottom: 18 }}>
                Your order is live in the iCASE Cloud Firestore database and immediately visible on
                the mobile app &amp; admin dashboard.
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                <button
                  className="btn btn-red"
                  onClick={() => {
                    const id = placedOrder.id;
                    setPlacedOrder(null);
                    onClose();
                    onTrackOrder(id);
                  }}
                >
                  <Search size={16} /> Track Order Live Now
                </button>
                <a
                  className="btn btn-whatsapp"
                  href={buildWhatsAppUrl(placedOrder)}
                  target="_blank"
                  rel="noreferrer"
                >
                  <MessageCircle size={16} /> Send WhatsApp Confirmation
                </a>
                <button
                  className="btn btn-outline"
                  onClick={() => {
                    setPlacedOrder(null);
                    onClose();
                  }}
                >
                  Done
                </button>
              </div>
            </div>
          ) : cartItems.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '48px 16px' }}>
              <ShoppingBag size={44} color="#E50914" style={{ marginBottom: 12 }} />
              <h3 style={{ color: 'var(--icase-navy)', marginBottom: 6 }}>Your Cart is Empty</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
                Add mobile covers, tempered glass, or fast chargers from the catalog to check out.
              </p>
            </div>
          ) : (
            <form id="checkout-form" onSubmit={handlePlaceOrder}>
              {/* Cart Items */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 18 }}>
                {cartItems.map(({ product, quantity, selectedColor }) => (
                  <div
                    key={`${product.id}::${selectedColor || ''}`}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 12,
                      padding: 10,
                      border: '1px solid var(--border)',
                      borderRadius: 12
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
                          fontSize: 13.5,
                          color: 'var(--icase-navy)',
                          whiteSpace: 'nowrap',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis'
                        }}
                      >
                        {product.name}
                      </div>
                      {selectedColor && (
                        <div style={{ fontSize: 11.5, color: 'var(--icase-blue)', fontWeight: 700 }}>
                          Finish: {selectedColor}
                        </div>
                      )}
                      <div style={{ fontWeight: 800, color: 'var(--icase-red)', fontSize: 14 }}>
                        {formatINR(product.price)}
                      </div>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <button
                        type="button"
                        className="btn btn-outline btn-sm"
                        style={{ padding: '4px 8px' }}
                        onClick={() => onUpdateQty(product.id, quantity - 1, selectedColor || '')}
                      >
                        <Minus size={13} />
                      </button>
                      <strong style={{ minWidth: 20, textAlign: 'center', fontSize: 13.5 }}>
                        {quantity}
                      </strong>
                      <button
                        type="button"
                        className="btn btn-outline btn-sm"
                        style={{ padding: '4px 8px' }}
                        onClick={() => onUpdateQty(product.id, quantity + 1, selectedColor || '')}
                      >
                        <Plus size={13} />
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm"
                        style={{ background: '#FFECEE', color: '#E50914', padding: '5px 8px' }}
                        onClick={() => onRemoveItem(product.id, selectedColor || '')}
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              {!profile.isLoggedIn && (
                <div
                  style={{
                    background: '#EAF2FF',
                    padding: 12,
                    borderRadius: 12,
                    marginBottom: 14,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    gap: 10
                  }}
                >
                  <span style={{ fontSize: 12.5, color: 'var(--icase-navy)', fontWeight: 600 }}>
                    Sign in to sync orders directly with your Mobile App My Orders.
                  </span>
                  <button
                    type="button"
                    className="btn btn-navy btn-sm"
                    onClick={() => {
                      onClose();
                      onOpenAccount();
                    }}
                  >
                    Login
                  </button>
                </div>
              )}

              {/* Delivery Details */}
              <div className="form-group">
                <label>Full Name</label>
                <input
                  className="input"
                  placeholder="Customer Name"
                  value={customerName}
                  onChange={(e) => setCustomerName(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>Mobile Number</label>
                <input
                  className="input"
                  placeholder="10-digit mobile or +91"
                  value={customerPhone}
                  onChange={(e) => setCustomerPhone(e.target.value)}
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
                      setGpsMessage('Detecting live GPS location...');
                      try {
                        const res = await detectCurrentLocation();
                        setDeliveryAddress(res.formattedAddress);
                        setGpsMessage('Location detected via GPS.');
                      } catch (err) {
                        setGpsMessage(err.message || 'Could not detect GPS location.');
                      } finally {
                        setDetectingGps(false);
                      }
                    }}
                  >
                    <Navigation size={13} />
                    {detectingGps ? 'Detecting...' : 'Use GPS / Detect Current Location'}
                  </button>
                </div>
                {gpsMessage && (
                  <div style={{ fontSize: 12, color: '#0066CC', marginBottom: 6, fontWeight: 600 }}>
                    {gpsMessage}
                  </div>
                )}
                <textarea
                  className="input"
                  rows={2}
                  placeholder="House / Flat No., Street, Landmark, City, PIN"
                  value={deliveryAddress}
                  onChange={(e) => setDeliveryAddress(e.target.value)}
                />
              </div>

              {/* Delivery Speed */}
              <div className="form-group">
                <label>Delivery Option</label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <label
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: 10,
                      borderRadius: 10,
                      border: '1.5px solid var(--border)',
                      background: activeDeliveryType === 'STANDARD' ? '#FFF1F2' : '#FFF',
                      cursor: 'pointer'
                    }}
                  >
                    <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <input
                        type="radio"
                        checked={activeDeliveryType === 'STANDARD'}
                        onChange={() => setDeliveryType('STANDARD')}
                      />
                      Standard Home Delivery (1-2 Days)
                    </span>
                    <strong>{freeStandardUnlocked ? 'FREE' : formatINR(standardFee)}</strong>
                  </label>

                  {canChooseOneHour && (
                    <label
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: 10,
                        borderRadius: 10,
                        border: '1.5px solid var(--border)',
                        background: activeDeliveryType === 'ONE_HOUR' ? '#FFF1F2' : '#FFF',
                        cursor: 'pointer'
                      }}
                    >
                      <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <input
                          type="radio"
                          checked={activeDeliveryType === 'ONE_HOUR'}
                          onChange={() => setDeliveryType('ONE_HOUR')}
                        />
                        ⚡ Express 1-Hour Delivery
                      </span>
                      <strong>{formatINR(expressFee)}</strong>
                    </label>
                  )}
                </div>
              </div>

              {/* Payment Method */}
              <div className="form-group">
                <label>Payment Method</label>
                <select
                  className="input"
                  value={paymentMethod}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                >
                  <option value="UPI">UPI (Google Pay / PhonePe)</option>
                  <option value="COD">Cash on Delivery</option>
                  <option value="CARD">Card Payment</option>
                </select>
              </div>

              {error && (
                <div style={{ color: '#E50914', fontWeight: 700, fontSize: 13, marginTop: 8 }}>
                  {error}
                </div>
              )}
            </form>
          )}
        </div>

        {!placedOrder && cartItems.length > 0 && (
          <div className="drawer-footer">
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13.5, marginBottom: 4 }}>
              <span style={{ color: 'var(--text-secondary)' }}>Subtotal</span>
              <strong>{formatINR(subtotal)}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13.5, marginBottom: 8 }}>
              <span style={{ color: 'var(--text-secondary)' }}>Delivery Charge</span>
              <strong>{deliveryFee === 0 ? 'FREE' : formatINR(deliveryFee)}</strong>
            </div>
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                fontSize: 17,
                fontWeight: 900,
                color: 'var(--icase-navy)',
                marginBottom: 12
              }}
            >
              <span>Total Amount</span>
              <span style={{ color: 'var(--icase-red)' }}>{formatINR(totalAmount)}</span>
            </div>
            <button
              type="submit"
              form="checkout-form"
              className="btn btn-red"
              style={{ width: '100%' }}
              disabled={isPlacing || !isStoreOnline}
            >
              {!isStoreOnline
                ? 'Store Currently Paused'
                : isPlacing
                  ? 'Placing Order...'
                  : `Place Order • ${formatINR(totalAmount)} →`}
            </button>
          </div>
        )}
      </aside>
    </div>
  );
}
