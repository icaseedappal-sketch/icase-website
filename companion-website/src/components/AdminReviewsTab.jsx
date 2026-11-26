import React, { useState } from 'react';
import { Trash2, Plus } from 'lucide-react';
import { deleteDoc, doc, setDoc, updateDoc } from 'firebase/firestore';
import { db } from '../firebase';
import { formatTimestamp } from '../data/constants';

export default function AdminReviewsTab({ reviews = [], onUpdateReviewsLocal }) {
  const [filter, setFilter] = useState('ALL'); // 'ALL' | 'PRODUCT' | 'SERVICE'
  const [showManualForm, setShowManualForm] = useState(false);

  // Manual form
  const [manualType, setManualType] = useState('PRODUCT');
  const [manualRef, setManualRef] = useState('ORD-');
  const [manualRating, setManualRating] = useState('5');
  const [manualTitle, setManualTitle] = useState('');
  const [manualName, setManualName] = useState('');
  const [manualComment, setManualComment] = useState('');
  const [manualApprove, setManualApprove] = useState(true);

  // Edit inline review
  const [editingReviewId, setEditingReviewId] = useState(null);
  const [editTitle, setEditTitle] = useState('');
  const [editName, setEditName] = useState('');
  const [editRating, setEditRating] = useState(5);
  const [editComment, setEditComment] = useState('');
  const [editApproved, setEditApproved] = useState(false);

  // Reply inputs map
  const [replyDrafts, setReplyDrafts] = useState({});
  const [toast, setToast] = useState('');

  function flash(msg) {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  }

  const filtered = reviews.filter((r) => {
    if (filter === 'PRODUCT') return r.reviewType === 'PRODUCT';
    if (filter === 'SERVICE') return r.reviewType === 'SERVICE';
    return true;
  });

  async function patchReview(id, updates) {
    const clean = { ...updates, updatedAt: Date.now() };
    if (db) {
      await updateDoc(doc(db, 'reviews', id), clean);
    }
    if (onUpdateReviewsLocal) {
      onUpdateReviewsLocal((prev) => prev.map((r) => (r.id === id ? { ...r, ...clean } : r)));
    }
  }

  async function handleDeleteReview(id) {
    if (db) {
      await deleteDoc(doc(db, 'reviews', id));
    }
    if (onUpdateReviewsLocal) {
      onUpdateReviewsLocal((prev) => prev.filter((r) => r.id !== id));
    }
    flash('Review permanently deleted.');
  }

  async function handleSaveManual(e) {
    e.preventDefault();
    const now = Date.now();
    const newRev = {
      id: `rev_${now}`,
      referenceId: manualRef.trim().toUpperCase() || 'ORD-MANUAL',
      reviewType: manualType,
      productId: '',
      targetTitle: manualTitle.trim() || 'iCASE Product',
      customerName: manualName.trim() || 'Verified Customer',
      rating: Math.max(1, Math.min(5, Number(manualRating || 5))),
      comment: manualComment.trim(),
      isVerified: true,
      // Strict privacy guard: SERVICE feedback is never publicly approved
      isApproved: manualType === 'PRODUCT' ? Boolean(manualApprove) : false,
      adminReply: '',
      userId: 'admin_manual',
      createdAt: now,
      updatedAt: now
    };
    if (db) {
      await setDoc(doc(db, 'reviews', newRev.id), newRev);
    }
    if (onUpdateReviewsLocal) {
      onUpdateReviewsLocal((prev) => [newRev, ...prev]);
    }
    setShowManualForm(false);
    setManualTitle('');
    setManualName('');
    setManualComment('');
    flash(manualType === 'SERVICE' ? 'Private service feedback added.' : 'Verified product review saved.');
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      {toast && (
        <div
          style={{
            padding: '10px 14px',
            borderRadius: 10,
            background: '#E8FDF2',
            color: '#065F46',
            fontWeight: 800,
            fontSize: 13
          }}
        >
          ✓ {toast}
        </div>
      )}

      <div
        className="card"
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: 10
        }}
      >
        <div className="category-chips">
          <button
            type="button"
            className={`chip ${filter === 'ALL' ? 'active' : ''}`}
            onClick={() => setFilter('ALL')}
          >
            All ({reviews.length})
          </button>
          <button
            type="button"
            className={`chip ${filter === 'PRODUCT' ? 'active' : ''}`}
            onClick={() => setFilter('PRODUCT')}
          >
            Product ({reviews.filter((r) => r.reviewType === 'PRODUCT').length})
          </button>
          <button
            type="button"
            className={`chip ${filter === 'SERVICE' ? 'active' : ''}`}
            onClick={() => setFilter('SERVICE')}
          >
            Private Service ({reviews.filter((r) => r.reviewType === 'SERVICE').length})
          </button>
        </div>

        <button
          type="button"
          className="btn btn-red btn-sm"
          onClick={() => setShowManualForm((v) => !v)}
        >
          <Plus size={15} /> Add Verified Review
        </button>
      </div>

      {showManualForm && (
        <form className="card" style={{ border: '2px solid var(--icase-blue)' }} onSubmit={handleSaveManual}>
          <h4 style={{ color: 'var(--icase-navy)', marginBottom: 10 }}>Add Manual Verified Review</h4>
          <div className="form-grid">
            <div className="form-group">
              <label>Review Type</label>
              <select
                className="input"
                value={manualType}
                onChange={(e) => setManualType(e.target.value)}
              >
                <option value="PRODUCT">Product Purchase (Can Publish)</option>
                <option value="SERVICE">Service Feedback (Always Private)</option>
              </select>
            </div>
            <div className="form-group">
              <label>Reference ID *</label>
              <input
                className="input"
                required
                value={manualRef}
                onChange={(e) => setManualRef(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Rating (1-5)</label>
              <input
                type="number"
                min={1}
                max={5}
                className="input"
                value={manualRating}
                onChange={(e) => setManualRating(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Item / Device Title *</label>
              <input
                className="input"
                required
                placeholder="e.g. MagSafe Case / iPhone 15 Display"
                value={manualTitle}
                onChange={(e) => setManualTitle(e.target.value)}
              />
            </div>
          </div>
          <div className="form-group">
            <label>Customer Name *</label>
            <input
              className="input"
              required
              value={manualName}
              onChange={(e) => setManualName(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Comment *</label>
            <textarea
              className="input"
              rows={2}
              required
              value={manualComment}
              onChange={(e) => setManualComment(e.target.value)}
            />
          </div>
          {manualType === 'PRODUCT' && (
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontWeight: 700, fontSize: 13, marginBottom: 12 }}>
              <input
                type="checkbox"
                checked={manualApprove}
                onChange={(e) => setManualApprove(e.target.checked)}
              />
              Approve for Public immediately
            </label>
          )}
          <div style={{ display: 'flex', gap: 8 }}>
            <button type="submit" className="btn btn-red btn-sm">
              Save Review
            </button>
            <button
              type="button"
              className="btn btn-outline btn-sm"
              onClick={() => setShowManualForm(false)}
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {filtered.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: 32, color: 'var(--text-secondary)' }}>
          No reviews in this filter yet.
        </div>
      ) : (
        <div className="admin-list">
          {filtered.map((r) => {
            const isService = r.reviewType === 'SERVICE';
            const isEditing = editingReviewId === r.id;
            const replyVal =
              replyDrafts[r.id] !== undefined ? replyDrafts[r.id] : r.adminReply || '';
            return (
              <div
                key={r.id}
                className="card"
                style={{
                  background: isService ? '#F8FAFC' : '#FFFFFF',
                  borderColor: isService ? 'rgba(0,102,204,0.35)' : 'var(--border)'
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    flexWrap: 'wrap',
                    gap: 8,
                    marginBottom: 8
                  }}
                >
                  <span
                    className={`status-pill ${isService ? '' : r.isApproved ? 'delivered' : 'active'}`}
                    style={
                      isService ? { background: '#EAF2FF', color: '#0066CC' } : undefined
                    }
                  >
                    {isService
                      ? '🔒 PRIVATE SERVICE FEEDBACK • HIDDEN FROM PUBLIC'
                      : r.isApproved
                        ? '✓ PUBLIC PRODUCT REVIEW'
                        : '⏳ PENDING APPROVAL'}
                  </span>
                  <span style={{ color: '#FFB800', fontWeight: 800, fontSize: 15 }}>
                    {'★'.repeat(Math.max(1, Math.min(5, Number(r.rating || 5))))}
                  </span>
                </div>

                <div style={{ fontWeight: 800, color: 'var(--icase-navy)', fontSize: 14.5 }}>
                  {r.customerName} • Ref: {r.referenceId}
                </div>
                <div style={{ fontSize: 12.5, fontWeight: 700, color: 'var(--icase-blue)' }}>
                  {r.targetTitle}
                </div>
                <div style={{ fontSize: 11.5, color: '#94A3B8', marginBottom: 6 }}>
                  {formatTimestamp(r.createdAt)}
                </div>
                <p style={{ fontSize: 13.5, color: '#334155', marginBottom: 8 }}>{r.comment}</p>

                {r.adminReply && (
                  <div
                    style={{
                      padding: 10,
                      borderRadius: 10,
                      background: '#EAF2FF',
                      fontSize: 12.5,
                      fontWeight: 600,
                      color: 'var(--icase-navy)',
                      marginBottom: 10
                    }}
                  >
                    Admin Reply: {r.adminReply}
                  </div>
                )}

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
                  {!isService && (
                    <button
                      type="button"
                      className={`btn btn-sm ${r.isApproved ? 'btn-outline' : 'btn-navy'}`}
                      onClick={async () => {
                        const nextVal = !r.isApproved;
                        await patchReview(r.id, { isApproved: nextVal });
                        flash(nextVal ? 'Review approved for public view.' : 'Review hidden from public.');
                      }}
                    >
                      {r.isApproved ? 'Hide / Unpublish' : 'Approve / Publish'}
                    </button>
                  )}
                  <button
                    type="button"
                    className="btn btn-outline btn-sm"
                    onClick={() => {
                      if (isEditing) {
                        setEditingReviewId(null);
                      } else {
                        setEditingReviewId(r.id);
                        setEditTitle(r.targetTitle || '');
                        setEditName(r.customerName || '');
                        setEditRating(Math.max(1, Math.min(5, Number(r.rating || 5))));
                        setEditComment(r.comment || '');
                        setEditApproved(Boolean(r.isApproved));
                      }
                    }}
                  >
                    {isEditing ? 'Close Edit' : 'Edit'}
                  </button>
                  <input
                    className="input"
                    style={{ flex: 1, minWidth: 160, padding: '6px 10px', fontSize: 12.5 }}
                    placeholder="Write admin reply..."
                    value={replyVal}
                    onChange={(e) =>
                      setReplyDrafts((prev) => ({ ...prev, [r.id]: e.target.value }))
                    }
                  />
                  <button
                    type="button"
                    className="btn btn-navy btn-sm"
                    onClick={async () => {
                      await patchReview(r.id, { adminReply: replyVal.trim() });
                      flash('Reply saved!');
                    }}
                  >
                    Reply
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline btn-sm"
                    style={{ color: 'var(--icase-red)' }}
                    onClick={() => handleDeleteReview(r.id)}
                  >
                    <Trash2 size={14} /> Delete
                  </button>
                </div>

                {isEditing && (
                  <form
                    onSubmit={async (e) => {
                      e.preventDefault();
                      await patchReview(r.id, {
                        targetTitle: editTitle.trim() || r.targetTitle,
                        customerName: editName.trim() || r.customerName,
                        rating: editRating,
                        comment: editComment.trim() || r.comment,
                        isApproved: isService ? false : Boolean(editApproved)
                      });
                      setEditingReviewId(null);
                      flash('Review updated & synced!');
                    }}
                    style={{
                      marginTop: 12,
                      padding: 12,
                      borderRadius: 12,
                      background: '#F8FAFC',
                      border: '1px solid rgba(0,102,204,0.35)'
                    }}
                  >
                    <div className="form-grid" style={{ gap: 8 }}>
                      <div className="form-group" style={{ marginBottom: 6 }}>
                        <label style={{ fontSize: 11 }}>Product / Device Name</label>
                        <input
                          className="input"
                          required
                          value={editTitle}
                          onChange={(e) => setEditTitle(e.target.value)}
                        />
                      </div>
                      <div className="form-group" style={{ marginBottom: 6 }}>
                        <label style={{ fontSize: 11 }}>Customer Name</label>
                        <input
                          className="input"
                          required
                          value={editName}
                          onChange={(e) => setEditName(e.target.value)}
                        />
                      </div>
                    </div>
                    <div className="form-group" style={{ marginBottom: 8 }}>
                      <label style={{ fontSize: 11 }}>Star Rating</label>
                      <div style={{ display: 'flex', gap: 4 }}>
                        {[1, 2, 3, 4, 5].map((s) => (
                          <button
                            key={s}
                            type="button"
                            onClick={() => setEditRating(s)}
                            style={{
                              border: 'none',
                              background: 'transparent',
                              fontSize: 22,
                              cursor: 'pointer',
                              color: s <= editRating ? '#FFB800' : '#CBD5E1'
                            }}
                          >
                            ★
                          </button>
                        ))}
                      </div>
                    </div>
                    <div className="form-group" style={{ marginBottom: 8 }}>
                      <label style={{ fontSize: 11 }}>Review Text</label>
                      <textarea
                        className="input"
                        rows={2}
                        required
                        value={editComment}
                        onChange={(e) => setEditComment(e.target.value)}
                      />
                    </div>
                    {!isService && (
                      <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12.5, fontWeight: 700, marginBottom: 10 }}>
                        <input
                          type="checkbox"
                          checked={editApproved}
                          onChange={(e) => setEditApproved(e.target.checked)}
                        />
                        Approved / Published on Product Page
                      </label>
                    )}
                    <button type="submit" className="btn btn-navy btn-sm">
                      Save Review Changes
                    </button>
                  </form>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
