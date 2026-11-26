import React, { useEffect, useState } from 'react';
import { X, CheckCircle, Star } from 'lucide-react';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { db } from '../firebase';
import {
  isOrderCompleted,
  isRepairCompleted,
  parseOrderItems
} from '../data/constants';

export default function VerifiedReviewModal({
  isOpen,
  onClose,
  prefillId = '',
  profile,
  onLocalReviewCreated
}) {
  const [refInput, setRefInput] = useState(prefillId || '');
  const [verifying, setVerifying] = useState(false);
  const [verifyResult, setVerifyResult] = useState(null);
  const [customerName, setCustomerName] = useState(profile?.name || '');
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [submittedNote, setSubmittedNote] = useState('');

  useEffect(() => {
    if (isOpen) {
      const cleaned = String(prefillId || '').trim().replace(/^#/, '').toUpperCase();
      setRefInput(cleaned);
      setVerifyResult(null);
      setError('');
      setSubmittedNote('');
      setComment('');
      setRating(5);
      setCustomerName(profile?.name || '');
      if (cleaned) {
        verifyReference(cleaned);
      }
    }
  }, [isOpen, prefillId]);

  if (!isOpen) return null;

  async function verifyReference(rawRef) {
    const cleaned = String(rawRef || '').trim().replace(/^#/, '').toUpperCase();
    setRefInput(cleaned);
    setError('');
    setSubmittedNote('');
    if (!cleaned) {
      setError('Enter an Order ID (ORD-...) or Repair Service ID (REP-...).');
      return;
    }
    if (!db) {
      setError('Cloud verification is offline right now. Connect to Firestore to verify your ID.');
      return;
    }

    setVerifying(true);
    setVerifyResult(null);
    try {
      const ordSnap = await getDoc(doc(db, 'orders', cleaned));
      if (ordSnap.exists()) {
        const ord = { id: ordSnap.id, ...ordSnap.data() };
        if (isOrderCompleted(ord.status)) {
          const items = parseOrderItems(ord.itemsJson);
          setVerifyResult({
            status: 'ELIGIBLE',
            reviewType: 'PRODUCT',
            productId: items[0]?.productId || '',
            targetTitle: items[0]?.productName || `Order #${ord.id}`,
            customerName: ord.customerName || profile?.name || ''
          });
          setCustomerName(ord.customerName || profile?.name || '');
        } else {
          setVerifyResult({ status: 'NOT_COMPLETED', statusLabel: ord.status });
        }
        return;
      }

      const repSnap = await getDoc(doc(db, 'repairs', cleaned));
      if (repSnap.exists()) {
        const rep = { id: repSnap.id, ...repSnap.data() };
        if (isRepairCompleted(rep.status)) {
          setVerifyResult({
            status: 'ELIGIBLE',
            reviewType: 'SERVICE',
            productId: '',
            targetTitle:
              `${rep.brandName || ''} ${rep.modelName || ''}`.trim() || `Repair #${rep.id}`,
            customerName: rep.customerName || profile?.name || ''
          });
          setCustomerName(rep.customerName || profile?.name || '');
        } else {
          setVerifyResult({ status: 'NOT_COMPLETED', statusLabel: rep.status });
        }
        return;
      }

      setVerifyResult({ status: 'NOT_FOUND' });
    } catch (err) {
      setError(err.message || 'Unable to verify ID right now.');
    } finally {
      setVerifying(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!verifyResult || verifyResult.status !== 'ELIGIBLE') return;
    if (!comment.trim()) {
      setError('Please write your review comment.');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const now = Date.now();
      const reviewPayload = {
        id: `rev_${now}`,
        referenceId: refInput,
        reviewType: verifyResult.reviewType,
        productId: verifyResult.productId || '',
        targetTitle: verifyResult.targetTitle,
        customerName: customerName.trim() || 'Verified Customer',
        rating: Math.max(1, Math.min(5, Number(rating || 5))),
        comment: comment.trim(),
        isVerified: true,
        isApproved: false,
        adminReply: '',
        userId: profile?.userId || '',
        createdAt: now,
        updatedAt: now
      };
      if (db) {
        await setDoc(doc(db, 'reviews', reviewPayload.id), reviewPayload);
      }
      if (onLocalReviewCreated) {
        onLocalReviewCreated(reviewPayload);
      }
      setSubmittedNote(
        verifyResult.reviewType === 'SERVICE'
          ? 'Private service feedback sent to iCASE Admin!'
          : 'Thank you! Your verified review was submitted for Admin approval.'
      );
      setVerifyResult(null);
      setComment('');
    } catch (err) {
      setError(err.message || 'Could not submit review.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Star size={18} color="#FFB800" fill="#FFB800" />
            <strong>Write Verified Review</strong>
          </div>
          <button
            type="button"
            className="btn btn-sm"
            style={{ background: 'rgba(255,255,255,0.14)', color: '#FFF' }}
            onClick={onClose}
          >
            <X size={16} />
          </button>
        </div>

        <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <p style={{ fontSize: 13, color: 'var(--text-secondary)' }}>
            Enter your completed Order ID (<strong>ORD-XXXXX</strong>) or Service ID (
            <strong>REP-XXXXX</strong>) to verify eligibility.
          </p>

          <div style={{ display: 'flex', gap: 8 }}>
            <input
              className="input"
              style={{ flex: 1, fontWeight: 700, textTransform: 'uppercase' }}
              placeholder="e.g. ORD-10294 or REP-58192"
              value={refInput}
              onChange={(e) => setRefInput(e.target.value)}
            />
            <button
              type="button"
              className="btn btn-navy btn-sm"
              disabled={verifying}
              onClick={() => verifyReference(refInput)}
            >
              {verifying ? 'Checking...' : 'Verify'}
            </button>
          </div>

          {verifyResult?.status === 'NOT_FOUND' && (
            <div style={{ color: 'var(--icase-red)', fontSize: 13, fontWeight: 700 }}>
              No Order or Service found matching #{refInput}.
            </div>
          )}

          {verifyResult?.status === 'NOT_COMPLETED' && (
            <div style={{ color: 'var(--icase-red)', fontSize: 13, fontWeight: 700 }}>
              Current status is &ldquo;{verifyResult.statusLabel}&rdquo;. Reviews unlock after
              completion / delivery.
            </div>
          )}

          {submittedNote && (
            <div
              style={{
                padding: 14,
                borderRadius: 12,
                background: '#E8FDF2',
                color: '#065F46',
                fontWeight: 700,
                fontSize: 13.5,
                display: 'flex',
                alignItems: 'center',
                gap: 8
              }}
            >
              <CheckCircle size={18} />
              <span>{submittedNote}</span>
            </div>
          )}

          {verifyResult?.status === 'ELIGIBLE' && (
            <>
              <div
                style={{
                  padding: 12,
                  borderRadius: 12,
                  fontSize: 12.5,
                  fontWeight: 700,
                  background: verifyResult.reviewType === 'PRODUCT' ? '#E8FDF2' : '#EAF2FF',
                  color: verifyResult.reviewType === 'PRODUCT' ? '#065F46' : 'var(--icase-navy)'
                }}
              >
                {verifyResult.reviewType === 'PRODUCT'
                  ? `✓ Verified Purchase: ${verifyResult.targetTitle} — Goes live publicly after Admin approval.`
                  : `✓ Verified Repair: ${verifyResult.targetTitle} — Repair feedback stays strictly private in iCASE Admin Panel.`}
              </div>

              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label>Your Name *</label>
                  <input
                    className="input"
                    required
                    value={customerName}
                    onChange={(e) => setCustomerName(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label>Rating</label>
                  <div style={{ display: 'flex', gap: 6 }}>
                    {[1, 2, 3, 4, 5].map((s) => (
                      <button
                        key={s}
                        type="button"
                        onClick={() => setRating(s)}
                        style={{
                          border: 'none',
                          background: 'transparent',
                          fontSize: 26,
                          lineHeight: 1,
                          cursor: 'pointer',
                          color: s <= rating ? '#FFB800' : '#CBD5E1'
                        }}
                      >
                        ★
                      </button>
                    ))}
                  </div>
                </div>
                <div className="form-group">
                  <label>Review / Feedback *</label>
                  <textarea
                    className="input"
                    rows={3}
                    required
                    placeholder="Share your experience..."
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                  />
                </div>
                <button
                  type="submit"
                  className="btn btn-red"
                  style={{ width: '100%' }}
                  disabled={submitting}
                >
                  {submitting ? 'Submitting...' : 'Submit Verified Review'}
                </button>
              </form>
            </>
          )}

          {error && (
            <div style={{ color: 'var(--icase-red)', fontSize: 13, fontWeight: 700 }}>{error}</div>
          )}
        </div>
      </div>
    </div>
  );
}
