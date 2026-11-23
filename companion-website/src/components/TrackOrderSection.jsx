import React, { useEffect, useState } from 'react';
import { Check, Search, AlertCircle } from 'lucide-react';
import { doc, onSnapshot } from 'firebase/firestore';
import { db } from '../firebase';
import {
  REPAIR_STATUSES,
  formatINR,
  formatTimestamp,
  parseComplaints,
  parseOrderItems
} from '../data/constants';

export default function TrackOrderSection({ activeTrackingId, onChangeTrackingId }) {
  const [inputId, setInputId] = useState(activeTrackingId || '');
  const [trackingState, setTrackingState] = useState({
    loading: false,
    type: null, // 'ORDER' | 'REPAIR' | 'NOT_FOUND' | null
    data: null
  });

  useEffect(() => {
    setInputId(activeTrackingId || '');
  }, [activeTrackingId]);

  useEffect(() => {
    const cleaned = (activeTrackingId || '').trim().replace(/^#/, '').toUpperCase();
    if (!cleaned) {
      setTrackingState({ loading: false, type: null, data: null });
      return;
    }

    setTrackingState((prev) => ({ ...prev, loading: true }));

    let orderDoc = null;
    let repairDoc = null;
    let orderChecked = false;
    let repairChecked = false;

    function publish() {
      if (orderDoc) {
        setTrackingState({ loading: false, type: 'ORDER', data: orderDoc });
      } else if (repairDoc) {
        setTrackingState({ loading: false, type: 'REPAIR', data: repairDoc });
      } else if (orderChecked && repairChecked) {
        setTrackingState({ loading: false, type: 'NOT_FOUND', data: null });
      }
    }

    const unsubOrder = onSnapshot(
      doc(db, 'orders', cleaned),
      (snap) => {
        orderChecked = true;
        orderDoc = snap.exists() ? { id: snap.id, ...snap.data() } : null;
        publish();
      },
      () => {
        orderChecked = true;
        publish();
      }
    );

    const unsubRepair = onSnapshot(
      doc(db, 'repairs', cleaned),
      (snap) => {
        repairChecked = true;
        repairDoc = snap.exists() ? { id: snap.id, ...snap.data() } : null;
        publish();
      },
      () => {
        repairChecked = true;
        publish();
      }
    );

    return () => {
      unsubOrder();
      unsubRepair();
    };
  }, [activeTrackingId]);

  function handleSearch(e) {
    e.preventDefault();
    const cleaned = inputId.trim().replace(/^#/, '').toUpperCase();
    onChangeTrackingId(cleaned);
  }

  return (
    <div>
      <div className="section-header">
        <div>
          <h1 className="section-title">Live Order &amp; Repair Tracking</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
            Real-time Firestore updates synced between the iCASE website, mobile app &amp; admin
            hub.
          </p>
        </div>
      </div>

      <form className="card" style={{ marginBottom: 20 }} onSubmit={handleSearch}>
        <label
          htmlFor="track-input"
          style={{ display: 'block', fontWeight: 800, color: 'var(--icase-navy)', marginBottom: 8 }}
        >
          Enter Reference Number
        </label>
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          <input
            id="track-input"
            className="input"
            style={{ flex: 1, minWidth: 220 }}
            placeholder="e.g. ORD-10294 or REP-58192"
            value={inputId}
            onChange={(e) => {
              const val = e.target.value;
              setInputId(val);
              if (!val.trim()) onChangeTrackingId('');
            }}
          />
          <button type="submit" className="btn btn-red">
            <Search size={17} />
            Track Status
          </button>
        </div>
      </form>

      {!activeTrackingId && (
        <div className="card" style={{ textAlign: 'center', padding: 40 }}>
          <Search size={36} color="#0B1728" style={{ marginBottom: 10 }} />
          <h3 style={{ color: 'var(--icase-navy)', marginBottom: 6 }}>
            Track Your Order or Repair
          </h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14, maxWidth: 460, margin: '0 auto' }}>
            Enter your Order ID (ORD-XXXXX) or Repair Booking ID (REP-XXXXX) above to view live
            real-time status updates.
          </p>
        </div>
      )}

      {trackingState.loading && (
        <div className="card" style={{ textAlign: 'center', padding: 36 }}>
          Looking up live status for <strong>#{activeTrackingId}</strong>...
        </div>
      )}

      {!trackingState.loading && trackingState.type === 'NOT_FOUND' && (
        <div className="card" style={{ textAlign: 'center', padding: 36 }}>
          <AlertCircle size={40} color="#E50914" style={{ marginBottom: 10 }} />
          <h3 style={{ color: 'var(--icase-navy)', marginBottom: 6 }}>Order Not Found</h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
            We could not find an active order or repair matching &ldquo;#{activeTrackingId}&rdquo;.
            Please double-check your ID and try again.
          </p>
        </div>
      )}

      {!trackingState.loading && trackingState.type === 'ORDER' && (
        <OrderTimelineCard order={trackingState.data} />
      )}

      {!trackingState.loading && trackingState.type === 'REPAIR' && (
        <RepairTimelineCard repair={trackingState.data} />
      )}
    </div>
  );
}

function OrderTimelineCard({ order }) {
  const steps = ['Order Placed', 'Packed', 'Out for Delivery', 'Delivered'];
  const currentStep =
    order.status === 'Delivered'
      ? 3
      : order.status === 'Out for Delivery'
        ? 2
        : order.status === 'Processing'
          ? 1
          : 0;
  const items = parseOrderItems(order.itemsJson);

  return (
    <div className="card">
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 10,
          marginBottom: 16
        }}
      >
        <div>
          <h2 style={{ color: 'var(--icase-navy)', fontSize: 22, fontWeight: 900 }}>
            #{order.id}
          </h2>
          <div style={{ color: 'var(--text-secondary)', fontSize: 13 }}>
            {formatTimestamp(order.createdAt)} • {order.customerName}
          </div>
        </div>
        <span className={`status-pill ${order.status === 'Delivered' ? 'delivered' : 'active'}`}>
          {order.status}
        </span>
      </div>

      <div className="timeline">
        {steps.map((step, idx) => {
          const completed = idx <= currentStep;
          return (
            <div key={step} className="timeline-step">
              <div className="timeline-marker-col">
                <div className={`timeline-dot ${completed ? 'completed' : ''}`}>
                  {completed && <Check size={15} />}
                </div>
                {idx < steps.length - 1 && (
                  <div className={`timeline-line ${idx < currentStep ? 'completed' : ''}`} />
                )}
              </div>
              <div>
                <div
                  style={{
                    fontWeight: 800,
                    fontSize: 14.5,
                    color: completed ? 'var(--icase-navy)' : 'var(--text-secondary)'
                  }}
                >
                  {step}
                </div>
                <div style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
                  {completed ? (idx === currentStep && order.statusNotes ? order.statusNotes : formatTimestamp(order.updatedAt || order.createdAt)) : 'Pending'}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '16px 0' }} />

      <div style={{ fontSize: 13.5, color: '#334155', marginBottom: 6 }}>
        <strong>Items:</strong>{' '}
        {items.map((it) => `${it.quantity}x ${it.productName}`).join(', ') || 'Accessory Order'}
      </div>
      <div style={{ fontSize: 13.5, color: '#334155', marginBottom: 6 }}>
        <strong>Delivery Address:</strong> {order.deliveryAddress}
      </div>
      <div style={{ fontSize: 15, fontWeight: 800, color: 'var(--icase-red)' }}>
        Total Paid: {formatINR(order.totalAmount)} (
        {order.deliveryType === 'ONE_HOUR' ? '⚡ 1-Hour Express' : 'Standard Delivery'})
      </div>
    </div>
  );
}

function RepairTimelineCard({ repair }) {
  const currentIdx = Math.max(0, REPAIR_STATUSES.indexOf(repair.status));
  const complaints = parseComplaints(repair.complaintsJson);

  return (
    <div className="card">
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 10,
          marginBottom: 16
        }}
      >
        <div>
          <h2 style={{ color: 'var(--icase-navy)', fontSize: 22, fontWeight: 900 }}>
            #{repair.id}
          </h2>
          <div style={{ color: 'var(--text-secondary)', fontSize: 13 }}>
            {repair.brandName} {repair.modelName} • {repair.preferredDate} ({repair.preferredTimeSlot})
          </div>
        </div>
        <span className={`status-pill ${repair.status === 'Delivered' ? 'delivered' : 'active'}`}>
          {repair.status}
        </span>
      </div>

      <div className="timeline">
        {REPAIR_STATUSES.map((step, idx) => {
          const completed = idx <= currentIdx;
          return (
            <div key={step} className="timeline-step">
              <div className="timeline-marker-col">
                <div className={`timeline-dot ${completed ? 'completed' : ''}`}>
                  {completed && <Check size={15} />}
                </div>
                {idx < REPAIR_STATUSES.length - 1 && (
                  <div className={`timeline-line ${idx < currentIdx ? 'completed' : ''}`} />
                )}
              </div>
              <div>
                <div
                  style={{
                    fontWeight: 800,
                    fontSize: 14,
                    color: completed ? 'var(--icase-navy)' : 'var(--text-secondary)'
                  }}
                >
                  {step}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '16px 0' }} />

      <div style={{ fontSize: 13.5, color: 'var(--icase-red)', fontWeight: 700, marginBottom: 4 }}>
        Reported Issues: {complaints.join(', ')}
      </div>
      <div style={{ fontSize: 13.5, color: '#334155', marginBottom: 4 }}>
        <strong>Pickup Address:</strong> {repair.pickupAddress}
      </div>
      {repair.adminNotes && (
        <div style={{ fontSize: 13, color: 'var(--text-secondary)' }}>
          <strong>Technician Note:</strong> {repair.adminNotes}
        </div>
      )}
    </div>
  );
}
