import React, { useEffect, useMemo, useState } from 'react';
import { Search, Zap, X, Heart, Shield, Award } from 'lucide-react';
import { doc, setDoc } from 'firebase/firestore';
import { db } from '../firebase';
import {
  CATEGORIES,
  TOP_BRANDS,
  computeProductRatingSummary,
  formatINR,
  formatTimestamp,
  getMatchingProductReviews,
  parseColorOptions,
  productMatchesBrand
} from '../data/constants';

export default function CatalogSection({
  products = [],
  selectedCategory,
  onSelectCategory,
  selectedBrand = '',
  onSelectBrand,
  settings = {},
  publicReviews = [],
  profile,
  wishlistIds = [],
  onToggleWishlist,
  onAddToCart,
  onBuyNow,
  selectedProduct,
  onSelectProduct,
  onOpenGuards,
  onLocalReviewCreated
}) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedColorIdx, setSelectedColorIdx] = useState(0);

  // Inline product review form inside product modal
  const [reviewFormOpen, setReviewFormOpen] = useState(false);
  const [reviewName, setReviewName] = useState(profile?.name || '');
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewSubmitting, setReviewSubmitting] = useState(false);
  const [reviewMsg, setReviewMsg] = useState('');
  const [reviewErr, setReviewErr] = useState('');

  const oneHourEnabled = settings.one_hour_delivery_enabled !== 'false';

  const activeProduct = useMemo(() => {
    if (!selectedProduct) return null;
    return products.find((p) => p.id === selectedProduct.id) || selectedProduct;
  }, [products, selectedProduct]);

  useEffect(() => {
    setSelectedColorIdx(0);
    setReviewFormOpen(false);
    setReviewMsg('');
    setReviewErr('');
    setReviewComment('');
    setReviewRating(5);
    setReviewName(profile?.name || '');
  }, [selectedProduct?.id]);

  const activeColorOptions = useMemo(
    () => (activeProduct ? parseColorOptions(activeProduct.colorOptionsJson) : []),
    [activeProduct]
  );

  const publicApprovedReviews = useMemo(
    () =>
      (Array.isArray(publicReviews) ? publicReviews : []).filter(
        (r) => r && r.reviewType === 'PRODUCT' && r.isApproved === true
      ),
    [publicReviews]
  );

  const filteredProducts = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return products
      .filter((p) => selectedCategory === 'All' || p.category === selectedCategory)
      .filter((p) => !selectedBrand || productMatchesBrand(p, selectedBrand))
      .filter(
        (p) =>
          !q ||
          p.name.toLowerCase().includes(q) ||
          p.category.toLowerCase().includes(q) ||
          (p.description || '').toLowerCase().includes(q)
      )
      .sort((a, b) => a.name.localeCompare(b.name));
  }, [products, selectedCategory, selectedBrand, searchQuery]);

  const modalMatchedReviews = useMemo(
    () => (activeProduct ? getMatchingProductReviews(publicApprovedReviews, activeProduct) : []),
    [publicApprovedReviews, activeProduct]
  );

  const modalRatingSummary = useMemo(
    () => (activeProduct ? computeProductRatingSummary(publicApprovedReviews, activeProduct) : null),
    [publicApprovedReviews, activeProduct]
  );

  async function handleSubmitProductReview(e) {
    e.preventDefault();
    if (!activeProduct) return;
    if (!reviewComment.trim()) {
      setReviewErr('Please enter your review comment.');
      return;
    }

    setReviewSubmitting(true);
    setReviewErr('');
    setReviewMsg('');
    try {
      const now = Date.now();
      const reviewDoc = {
        id: `rev_${now}`,
        referenceId: 'DIRECT',
        reviewType: 'PRODUCT',
        productId: activeProduct.id,
        targetTitle: activeProduct.name,
        customerName: reviewName.trim() || 'Customer',
        rating: Math.max(1, Math.min(5, Number(reviewRating || 5))),
        comment: reviewComment.trim(),
        isVerified: Boolean(profile?.isLoggedIn),
        isApproved: false,
        adminReply: '',
        userId: profile?.userId || '',
        createdAt: now,
        updatedAt: now
      };
      if (db) {
        await setDoc(doc(db, 'reviews', reviewDoc.id), reviewDoc);
      }
      if (onLocalReviewCreated) {
        onLocalReviewCreated(reviewDoc);
      }
      setReviewFormOpen(false);
      setReviewComment('');
      setReviewMsg('Thank you! Review submitted for Admin approval.');
    } catch (err) {
      setReviewErr(err.message || 'Could not submit review right now.');
    } finally {
      setReviewSubmitting(false);
    }
  }

  return (
    <div>
      <div className="section-header">
        <div>
          <h1 className="section-title">iCASE Product Catalog</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
            Real-time inventory synchronized with the iCASE mobile app &amp; admin hub.
          </p>
        </div>
        {onOpenGuards && (
          <button type="button" className="btn btn-navy btn-sm" onClick={onOpenGuards}>
            <Shield size={15} /> Find Tempered Glass &amp; Camera Lens →
          </button>
        )}
      </div>

      <div className="catalog-toolbar">
        <div className="category-chips">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              className={`chip ${selectedCategory === cat ? 'active' : ''}`}
              onClick={() => onSelectCategory(cat)}
            >
              {cat}
            </button>
          ))}
        </div>

        <div className="search-box">
          <Search size={17} className="search-box-icon" />
          <input
            type="text"
            placeholder="Search cases, chargers, earbuds..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
      </div>

      {/* Brand Chips Strip */}
      {onSelectBrand && (
        <div className="category-chips" style={{ marginBottom: 18 }}>
          <button
            type="button"
            className={`chip ${!selectedBrand ? 'active' : ''}`}
            onClick={() => onSelectBrand('')}
          >
            All Brands
          </button>
          {TOP_BRANDS.map((brand) => (
            <button
              key={brand}
              type="button"
              className={`chip ${selectedBrand === brand ? 'active' : ''}`}
              onClick={() => onSelectBrand(selectedBrand === brand ? '' : brand)}
            >
              {brand}
            </button>
          ))}
        </div>
      )}

      {/* Best Quality Best Price Strip */}
      <div className="quality-banner" style={{ padding: '12px 18px', marginBottom: 20 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <Award size={20} color="#FFB800" />
          <span style={{ fontSize: 13.5, fontWeight: 700 }}>
            Best Quality, Best Price • Checked before dispatch in Edappal
          </span>
        </div>
        <span style={{ fontSize: 12, color: '#CBD5E1', fontWeight: 600 }}>
          {filteredProducts.length} matching item(s)
        </span>
      </div>

      {filteredProducts.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: 40 }}>
          <h3 style={{ color: 'var(--icase-navy)', marginBottom: 6 }}>No Matching Products</h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginBottom: 14 }}>
            Try another category, brand, or clear your search filter.
          </p>
          <button
            type="button"
            className="btn btn-outline btn-sm"
            onClick={() => {
              onSelectCategory('All');
              if (onSelectBrand) onSelectBrand('');
              setSearchQuery('');
            }}
          >
            Show All Products
          </button>
        </div>
      ) : (
        <div className="products-grid">
          {filteredProducts.map((product) => {
            const discountPct =
              product.originalPrice > product.price
                ? Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)
                : 0;
            const ratingSummary = computeProductRatingSummary(publicApprovedReviews, product);
            return (
              <article key={product.id} className="product-card">
                <div className="product-media" onClick={() => onSelectProduct(product)}>
                  <img src={product.imageUrl} alt={product.name} loading="lazy" />
                  {discountPct > 0 && <span className="discount-badge">{discountPct}% OFF</span>}
                  {oneHourEnabled && product.isOneHourEligible && (
                    <span className="express-badge">
                      <Zap size={11} /> 1-Hour Eligible
                    </span>
                  )}
                  {!product.inStock && <span className="stock-pill-out">Out of Stock</span>}
                </div>
                <div className="product-body">
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      gap: 8
                    }}
                  >
                    <div className="product-category" style={{ marginBottom: 0 }}>
                      {product.category}
                    </div>
                    {ratingSummary.reviewCount > 0 && (
                      <span className="rating-badge">
                        ★ {ratingSummary.averageRating.toFixed(1)} ({ratingSummary.reviewCount})
                      </span>
                    )}
                  </div>
                  <h3 className="product-title" onClick={() => onSelectProduct(product)}>
                    {product.name}
                  </h3>
                  <p className="product-desc">{product.description}</p>
                  <div className="product-price-row">
                    <span className="product-price">{formatINR(product.price)}</span>
                    {product.originalPrice > product.price && (
                      <span className="product-mrp">{formatINR(product.originalPrice)}</span>
                    )}
                  </div>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button
                      className="btn btn-red btn-sm"
                      style={{ flex: 1 }}
                      disabled={!product.inStock}
                      onClick={() => onAddToCart(product)}
                    >
                      {product.inStock ? 'Add to Cart' : 'Out of Stock'}
                    </button>
                    <button
                      className="btn btn-navy btn-sm"
                      disabled={!product.inStock}
                      onClick={() => onBuyNow(product)}
                    >
                      Buy
                    </button>
                    {onToggleWishlist && (
                      <button
                        type="button"
                        className="btn btn-outline btn-sm"
                        style={{
                          padding: '6px 9px',
                          color: wishlistIds.includes(product.id) ? '#E50914' : '#64748B',
                          borderColor: wishlistIds.includes(product.id) ? '#E50914' : 'var(--border)'
                        }}
                        onClick={() => onToggleWishlist(product.id)}
                        title="Toggle Wishlist"
                      >
                        <Heart
                          size={15}
                          fill={wishlistIds.includes(product.id) ? '#E50914' : 'none'}
                        />
                      </button>
                    )}
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}

      {/* Product Detail Modal */}
      {activeProduct && (
        <div className="overlay" onClick={() => onSelectProduct(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <strong>{activeProduct.category}</strong>
              <button
                className="btn btn-sm"
                style={{ background: 'rgba(255,255,255,0.14)', color: '#FFF' }}
                onClick={() => onSelectProduct(null)}
              >
                <X size={16} />
              </button>
            </div>
            <div className="modal-body">
              <img
                src={activeProduct.imageUrl}
                alt={activeProduct.name}
                style={{
                  width: '100%',
                  maxHeight: 280,
                  objectFit: 'cover',
                  borderRadius: 12,
                  marginBottom: 16
                }}
              />
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
                <h2 style={{ color: 'var(--icase-navy)', fontSize: 22, fontWeight: 900 }}>
                  {activeProduct.name}
                </h2>
                {modalRatingSummary && modalRatingSummary.reviewCount > 0 && (
                  <span className="rating-badge">
                    ★ {modalRatingSummary.averageRating.toFixed(1)} ({modalRatingSummary.reviewCount})
                  </span>
                )}
              </div>

              <div className="product-price-row">
                <span className="product-price" style={{ fontSize: 24 }}>
                  {formatINR(activeProduct.price)}
                </span>
                {activeProduct.originalPrice > activeProduct.price && (
                  <span className="product-mrp" style={{ fontSize: 15 }}>
                    {formatINR(activeProduct.originalPrice)}
                  </span>
                )}
              </div>

              {activeColorOptions.length > 0 && (
                <div style={{ marginBottom: 16 }}>
                  <div style={{ fontSize: 13.5, fontWeight: 700, color: 'var(--icase-navy)', marginBottom: 8 }}>
                    Selected Finish:{' '}
                    <span style={{ fontWeight: 600, color: '#475569' }}>
                      {(activeColorOptions[selectedColorIdx] || activeColorOptions[0]).name}
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
                    {activeColorOptions.map((opt, idx) => {
                      const activeIdx =
                        selectedColorIdx < activeColorOptions.length ? selectedColorIdx : 0;
                      const isSelected = activeIdx === idx;
                      return (
                        <button
                          key={`${opt.name}-${idx}`}
                          type="button"
                          title={opt.name}
                          onClick={() => setSelectedColorIdx(idx)}
                          style={{
                            width: 32,
                            height: 32,
                            borderRadius: '50%',
                            padding: 3,
                            background: '#FFFFFF',
                            border: isSelected ? '2.5px solid #0B1728' : '1px solid #CBD5E1',
                            cursor: 'pointer',
                            display: 'grid',
                            placeItems: 'center'
                          }}
                        >
                          <span
                            style={{
                              width: '100%',
                              height: '100%',
                              borderRadius: '50%',
                              background: opt.hex,
                              display: 'block'
                            }}
                          />
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              <p style={{ color: '#334155', fontSize: 14.5, marginBottom: 16 }}>
                {activeProduct.description}
              </p>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginBottom: 18 }}>
                <span className={`status-pill ${activeProduct.inStock ? 'delivered' : 'active'}`}>
                  {activeProduct.inStock
                    ? `In Stock (${activeProduct.stockCount ?? 25} units)`
                    : 'Out of Stock'}
                </span>
                {oneHourEnabled && activeProduct.isOneHourEligible && (
                  <span className="status-pill" style={{ background: '#EAF2FF', color: '#0066CC' }}>
                    ⚡ 1-Hour Express Delivery Eligible
                  </span>
                )}
              </div>

              <div style={{ display: 'flex', gap: 10, marginBottom: 20 }}>
                <button
                  className="btn btn-red"
                  style={{ flex: 1 }}
                  disabled={!activeProduct.inStock}
                  onClick={() => {
                    const selectedFinish =
                      activeColorOptions[
                        selectedColorIdx < activeColorOptions.length ? selectedColorIdx : 0
                      ]?.name || '';
                    onAddToCart(activeProduct, selectedFinish);
                    onSelectProduct(null);
                  }}
                >
                  Add to Cart
                </button>
                <button
                  className="btn btn-navy"
                  style={{ flex: 1 }}
                  disabled={!activeProduct.inStock}
                  onClick={() => {
                    const selectedFinish =
                      activeColorOptions[
                        selectedColorIdx < activeColorOptions.length ? selectedColorIdx : 0
                      ]?.name || '';
                    const prod = activeProduct;
                    onSelectProduct(null);
                    onBuyNow(prod, selectedFinish);
                  }}
                >
                  Buy Now
                </button>
                {onToggleWishlist && (
                  <button
                    type="button"
                    className="btn btn-outline"
                    style={{
                      color: wishlistIds.includes(activeProduct.id) ? '#E50914' : '#64748B',
                      borderColor: wishlistIds.includes(activeProduct.id)
                        ? '#E50914'
                        : 'var(--border)'
                    }}
                    onClick={() => onToggleWishlist(activeProduct.id)}
                    title="Toggle Wishlist"
                  >
                    <Heart
                      size={18}
                      fill={wishlistIds.includes(activeProduct.id) ? '#E50914' : 'none'}
                    />
                  </button>
                )}
              </div>

              {/* Customer Ratings & Reviews inside Modal */}
              <div
                style={{
                  background: '#F8FAFC',
                  border: '1px solid var(--border)',
                  borderRadius: 14,
                  padding: 16
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    flexWrap: 'wrap',
                    gap: 8,
                    marginBottom: 10
                  }}
                >
                  <div>
                    <h3 style={{ fontSize: 15, fontWeight: 800, color: 'var(--icase-navy)' }}>
                      Customer Ratings &amp; Reviews
                    </h3>
                    <div style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
                      {modalRatingSummary && modalRatingSummary.reviewCount > 0
                        ? `★ ${modalRatingSummary.averageRating.toFixed(1)} / 5 • ${modalRatingSummary.reviewCount} published review(s)`
                        : 'No published reviews for this product yet.'}
                    </div>
                  </div>
                  <button
                    type="button"
                    className="btn btn-outline btn-sm"
                    onClick={() => setReviewFormOpen((o) => !o)}
                  >
                    {reviewFormOpen ? 'Close Form' : 'Write a Review ★'}
                  </button>
                </div>

                {reviewMsg && (
                  <div style={{ color: '#065F46', fontWeight: 700, fontSize: 13, marginBottom: 8 }}>
                    {reviewMsg}
                  </div>
                )}

                {reviewFormOpen && (
                  <form
                    onSubmit={handleSubmitProductReview}
                    style={{
                      background: '#FFFFFF',
                      border: '1px solid var(--border)',
                      borderRadius: 12,
                      padding: 14,
                      marginBottom: 12
                    }}
                  >
                    <div className="form-group" style={{ marginBottom: 8 }}>
                      <label style={{ fontSize: 12 }}>Your Star Rating</label>
                      <div style={{ display: 'flex', gap: 4 }}>
                        {[1, 2, 3, 4, 5].map((s) => (
                          <button
                            key={s}
                            type="button"
                            onClick={() => setReviewRating(s)}
                            style={{
                              border: 'none',
                              background: 'transparent',
                              fontSize: 22,
                              cursor: 'pointer',
                              color: s <= reviewRating ? '#FFB800' : '#CBD5E1'
                            }}
                          >
                            ★
                          </button>
                        ))}
                      </div>
                    </div>
                    <div className="form-group" style={{ marginBottom: 8 }}>
                      <label style={{ fontSize: 12 }}>Your Name</label>
                      <input
                        className="input"
                        placeholder="Full Name"
                        value={reviewName}
                        onChange={(e) => setReviewName(e.target.value)}
                      />
                    </div>
                    <div className="form-group" style={{ marginBottom: 10 }}>
                      <label style={{ fontSize: 12 }}>Review *</label>
                      <textarea
                        className="input"
                        rows={2}
                        required
                        placeholder="Share your feedback about this item..."
                        value={reviewComment}
                        onChange={(e) => setReviewComment(e.target.value)}
                      />
                    </div>
                    {reviewErr && (
                      <div style={{ color: 'var(--icase-red)', fontSize: 12.5, marginBottom: 8 }}>
                        {reviewErr}
                      </div>
                    )}
                    <button
                      type="submit"
                      className="btn btn-navy btn-sm"
                      disabled={reviewSubmitting}
                    >
                      {reviewSubmitting ? 'Submitting...' : 'Submit Review'}
                    </button>
                  </form>
                )}

                {modalMatchedReviews.length > 0 && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {modalMatchedReviews.slice(0, 5).map((r) => (
                      <div
                        key={r.id}
                        style={{
                          background: '#FFFFFF',
                          border: '1px solid var(--border)',
                          borderRadius: 10,
                          padding: 10
                        }}
                      >
                        <div
                          style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                          }}
                        >
                          <strong style={{ fontSize: 13, color: 'var(--icase-navy)' }}>
                            {r.customerName}
                          </strong>
                          <span style={{ color: '#FFB800', fontSize: 12.5 }}>
                            {'★'.repeat(Math.max(1, Math.min(5, Number(r.rating || 5))))}
                          </span>
                        </div>
                        <p style={{ fontSize: 12.5, color: '#334155', marginTop: 4 }}>{r.comment}</p>
                        {r.adminReply && (
                          <div style={{ fontSize: 11.5, color: '#0066CC', marginTop: 4, fontWeight: 600 }}>
                            iCASE Reply: {r.adminReply}
                          </div>
                        )}
                        <div style={{ fontSize: 10.5, color: '#94A3B8', marginTop: 4 }}>
                          {formatTimestamp(r.createdAt)}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
