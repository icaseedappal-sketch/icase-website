import React, { useMemo, useState } from 'react';
import {
  Zap,
  ShoppingBag,
  Shield,
  Wrench,
  Search,
  User,
  MessageCircle,
  Phone,
  Mail,
  MapPin,
  Clock,
  ShieldCheck,
  AlertCircle,
  Heart,
  Award,
  X
} from 'lucide-react';
import {
  CATEGORIES,
  TOP_BRANDS,
  computeProductRatingSummary,
  formatINR,
  formatTimestamp,
  getHomeFeaturedPouches,
  resolvePouchImageUrl,
  resolvePouchPrice,
  resolvePouchSelectedColor
} from '../data/constants';

export default function HomeSection({
  products = [],
  settings = {},
  screenGuardConfig,
  publicReviews = [],
  wishlistIds = [],
  onToggleWishlist,
  onNavigate,
  onSelectCategory,
  onSelectBrand,
  onSelectProduct,
  onAddToCart,
  onOpenAccount,
  onOpenGuardsPouch,
  onOpenReviewModal
}) {
  const [homeSearch, setHomeSearch] = useState('');
  const [selectedHomePouchColors, setSelectedHomePouchColors] = useState({});

  const isStoreOnline = settings.store_online !== 'false';
  const oneHourEnabled = settings.one_hour_delivery_enabled !== 'false';
  const supportPhone = settings.store_phone || '9961454059';
  const supportWhatsapp = settings.store_whatsapp || supportPhone;
  const supportEmail = settings.store_email || 'icaseedappal@gmail.com';
  const storeAddress =
    settings.store_address || 'Main Road, Near Town Square, Edappal, Malappuram, Kerala';
  const storeHours = settings.store_hours || '9:00 AM - 10:00 PM (Daily)';

  const waSupportUrl = `https://api.whatsapp.com/send?phone=91${supportWhatsapp.replace(
    /\D/g,
    ''
  )}&text=${encodeURIComponent(
    'Hi iCASE Edappal! I would like to place an order or enquire about a repair.'
  )}`;

  const featuredProducts = useMemo(() => {
    const popular = [...products]
      .filter((p) => p.isPopular)
      .sort((a, b) => Number(a.popularityRank || 99) - Number(b.popularityRank || 99))
      .slice(0, 6);
    return popular.length > 0 ? popular : products.slice(0, 6);
  }, [products]);

  const homeFeaturedPouches = useMemo(
    () => getHomeFeaturedPouches(screenGuardConfig),
    [screenGuardConfig]
  );

  const allMatchingProducts = useMemo(() => {
    const hq = homeSearch.trim().toLowerCase();
    return [...products]
      .filter(
        (p) =>
          !hq ||
          (p.name || '').toLowerCase().includes(hq) ||
          (p.category || '').toLowerCase().includes(hq) ||
          (p.description || '').toLowerCase().includes(hq)
      )
      .sort((a, b) => Number(a.popularityRank || 99) - Number(b.popularityRank || 99));
  }, [products, homeSearch]);

  // Strict privacy filter: ONLY approved PRODUCT reviews ever display publicly
  const publicApprovedReviews = useMemo(
    () =>
      (Array.isArray(publicReviews) ? publicReviews : []).filter(
        (r) => r && r.reviewType === 'PRODUCT' && r.isApproved === true
      ),
    [publicReviews]
  );

  function renderProductCard(product) {
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
              {product.inStock ? 'Add to Cart' : 'Currently Out of Stock'}
            </button>
            {onToggleWishlist && (
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{
                  padding: '6px 10px',
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
  }

  return (
    <div>
      {!isStoreOnline && (
        <div className="store-alert-banner">
          <AlertCircle size={20} />
          <span>
            Online orders are temporarily paused by Admin. You can still browse products, track
            orders, or contact Customer Support at +91 {supportPhone}.
          </span>
        </div>
      )}

      {/* Hero Banner */}
      <section className="hero-banner">
        <div className="hero-grid">
          <div>
            <div className="hero-kicker">
              <Zap size={14} />
              <span>
                {oneHourEnabled
                  ? 'Express 1-Hour Doorstep Delivery in Edappal'
                  : 'Fast Local Home Delivery & Expert Mobile Care'}
              </span>
            </div>
            <h1 className="hero-title">
              Mobile Accessories &amp; <span>Express Repair Service</span>
            </h1>
            <p className="hero-subtitle">
              Smart Choice... Better Care. Shop shockproof mobile covers, tempered glass, camera lens
              protection, GaN fast chargers, and ANC audio—or book doorstep repair pickup in Edappal.
            </p>
            <div className="hero-ctas">
              <button className="btn btn-red" onClick={() => onNavigate('catalog')}>
                <ShoppingBag size={17} />
                Explore Shop Catalog
              </button>
              <button className="btn btn-outline" onClick={() => onNavigate('guards')}>
                <Shield size={17} />
                Tempered Glass &amp; Lens
              </button>
              <button className="btn btn-outline" onClick={() => onNavigate('repair')}>
                <Wrench size={17} />
                Book Repair Pickup
              </button>
              <button
                className="btn btn-whatsapp"
                onClick={() => window.open(waSupportUrl, '_blank')}
              >
                <MessageCircle size={17} />
                WhatsApp Support
              </button>
            </div>
          </div>

          <div className="hero-stats-card">
            <div className="hero-stat-row">
              <div className="hero-stat-icon">
                <ShieldCheck size={19} />
              </div>
              <div>
                <div style={{ fontWeight: 800, fontSize: 15 }}>Trusted Quality Accessories</div>
                <div style={{ fontSize: 12.5, color: '#CBD5E1' }}>
                  Original MagSafe cases, 9H glass &amp; certified GaN chargers
                </div>
              </div>
            </div>
            <div className="hero-stat-row">
              <div className="hero-stat-icon">
                <Zap size={19} />
              </div>
              <div>
                <div style={{ fontWeight: 800, fontSize: 15 }}>1-Hour Local Express</div>
                <div style={{ fontSize: 12.5, color: '#CBD5E1' }}>
                  Direct doorstep delivery across Edappal &amp; nearby areas
                </div>
              </div>
            </div>
            <div className="hero-stat-row">
              <div className="hero-stat-icon">
                <Wrench size={19} />
              </div>
              <div>
                <div style={{ fontWeight: 800, fontSize: 15 }}>Doorstep Repair &amp; Pickup</div>
                <div style={{ fontSize: 12.5, color: '#CBD5E1' }}>
                  Free repair diagnosis for display, battery &amp; charging issues
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Home Live Search Bar */}
      <div className="home-search-wrap">
        <Search size={18} color="#64748B" />
        <input
          type="text"
          placeholder="Search cases, chargers, tempered glass, earbuds..."
          value={homeSearch}
          onChange={(e) => setHomeSearch(e.target.value)}
        />
        {homeSearch && (
          <button
            type="button"
            className="btn btn-outline btn-sm"
            style={{ padding: '4px 10px' }}
            onClick={() => setHomeSearch('')}
          >
            <X size={14} /> Clear
          </button>
        )}
      </div>

      {/* 6 Quick Action Tiles matching Mobile App Home */}
      <section className="quick-actions-grid">
        <button className="quick-tile" onClick={() => onNavigate('catalog')}>
          <div className="quick-tile-icon" style={{ background: '#FFECEE', color: '#E50914' }}>
            <ShoppingBag size={22} />
          </div>
          <div>
            <div className="quick-tile-title">Shop Catalog</div>
            <div className="quick-tile-sub">{products.length} synced products</div>
          </div>
        </button>

        <button className="quick-tile" onClick={() => onNavigate('guards')}>
          <div className="quick-tile-icon" style={{ background: '#EAF2FF', color: '#0066CC' }}>
            <Shield size={22} />
          </div>
          <div>
            <div className="quick-tile-title">Tempered Glass</div>
            <div className="quick-tile-sub">Screen guards, lens &amp; pouches</div>
          </div>
        </button>

        <button className="quick-tile" onClick={() => onNavigate('repair')}>
          <div className="quick-tile-icon" style={{ background: '#EEF2FF', color: '#4F46E5' }}>
            <Wrench size={22} />
          </div>
          <div>
            <div className="quick-tile-title">Repair &amp; Pickup</div>
            <div className="quick-tile-sub">Book expert doorstep pickup</div>
          </div>
        </button>

        <button className="quick-tile" onClick={() => onNavigate('track')}>
          <div className="quick-tile-icon" style={{ background: '#E8FDF2', color: '#10B981' }}>
            <Search size={22} />
          </div>
          <div>
            <div className="quick-tile-title">Track Order</div>
            <div className="quick-tile-sub">Live status via Order / REP ID</div>
          </div>
        </button>

        <button className="quick-tile" onClick={onOpenAccount}>
          <div className="quick-tile-icon" style={{ background: '#F1F5F9', color: '#0B1728' }}>
            <User size={22} />
          </div>
          <div>
            <div className="quick-tile-title">Account</div>
            <div className="quick-tile-sub">Orders, wishlist &amp; address</div>
          </div>
        </button>

        <button className="quick-tile" onClick={() => window.open(waSupportUrl, '_blank')}>
          <div className="quick-tile-icon" style={{ background: '#DCFCE7', color: '#15803D' }}>
            <MessageCircle size={22} />
          </div>
          <div>
            <div className="quick-tile-title">WhatsApp</div>
            <div className="quick-tile-sub">+91 {supportPhone}</div>
          </div>
        </button>
      </section>

      {/* Category Shortcuts */}
      <section style={{ marginBottom: 28 }}>
        <div className="section-header">
          <h2 className="section-title">Browse by Category</h2>
          <button className="section-link" onClick={() => onNavigate('catalog')}>
            View Full Catalog →
          </button>
        </div>
        <div className="category-chips">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              className="chip"
              onClick={() => {
                onSelectCategory(cat);
                onNavigate('catalog');
              }}
            >
              {cat}
            </button>
          ))}
        </div>
      </section>

      {/* Featured Pouches (Admin "Show on Home Page") */}
      {homeFeaturedPouches.length > 0 && (
        <section style={{ marginBottom: 32 }}>
          <div className="section-header">
            <div>
              <h2 className="section-title">✨ Featured Pouches &amp; Cases</h2>
              <p style={{ fontSize: 13, color: 'var(--text-secondary)' }}>
                Handpicked pouches featured from iCASE Store
              </p>
            </div>
            <button
              className="section-link"
              onClick={() => {
                if (onOpenGuardsPouch) onOpenGuardsPouch();
                else onNavigate('guards');
              }}
            >
              Choose Phone Model →
            </button>
          </div>

          <div className="pouch-grid">
            {homeFeaturedPouches.map((pouch) => {
              const pricing = resolvePouchPrice(pouch);
              const activeColor = resolvePouchSelectedColor(
                pouch,
                selectedHomePouchColors[pouch.id] || ''
              );
              const thumbUrl = resolvePouchImageUrl(pouch, activeColor?.name || '');
              const colors = Array.isArray(pouch.colorOptions) ? pouch.colorOptions : [];

              return (
                <article key={pouch.id} className="product-card" style={{ padding: 0 }}>
                  <div className="product-media">
                    <img src={thumbUrl} alt={pouch.name} loading="lazy" />
                    {pricing.hasOffer && pricing.offerBadge && (
                      <span className="discount-badge">{pricing.offerBadge}</span>
                    )}
                  </div>
                  <div className="product-body">
                    <div className="product-category">
                      {(pouch.compatibleBrands || []).join(' • ')}
                    </div>
                    <h3 className="product-title">{pouch.name}</h3>
                    {pouch.description && <p className="product-desc">{pouch.description}</p>}
                    <div className="product-price-row">
                      <span className="product-price">{formatINR(pricing.finalPrice)}</span>
                      {pricing.hasOffer && pricing.basePrice > pricing.finalPrice && (
                        <span className="product-mrp">{formatINR(pricing.basePrice)}</span>
                      )}
                    </div>
                    {colors.length > 0 && (
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 10 }}>
                        {colors.slice(0, 7).map((c) => {
                          const selected =
                            activeColor?.name?.toLowerCase() === c.name.toLowerCase();
                          return (
                            <button
                              key={c.name}
                              type="button"
                              title={c.name}
                              onClick={() =>
                                setSelectedHomePouchColors((prev) => ({
                                  ...prev,
                                  [pouch.id]: c.name
                                }))
                              }
                              style={{
                                width: 24,
                                height: 24,
                                borderRadius: '50%',
                                padding: 2,
                                background: '#FFFFFF',
                                border: selected ? '2px solid #0B1728' : '1px solid #CBD5E1',
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
                                  background: c.hex || '#1E293B',
                                  display: 'block'
                                }}
                              />
                            </button>
                          );
                        })}
                      </div>
                    )}
                    <button
                      type="button"
                      className="btn btn-navy btn-sm"
                      style={{ width: '100%' }}
                      onClick={() => {
                        if (onOpenGuardsPouch) onOpenGuardsPouch();
                        else onNavigate('guards');
                      }}
                    >
                      Select Model &amp; Buy →
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        </section>
      )}

      {/* Popular Products Showcase */}
      <section style={{ marginBottom: 32 }}>
        <div className="section-header">
          <h2 className="section-title">Popular Products</h2>
          <button className="section-link" onClick={() => onNavigate('catalog')}>
            See All Accessories →
          </button>
        </div>
        <div className="products-grid">{featuredProducts.map(renderProductCard)}</div>
      </section>

      {/* Top Brands Strip */}
      <section style={{ marginBottom: 26 }}>
        <div className="section-header">
          <h2 className="section-title">Top Brands We Support</h2>
          <button className="section-link" onClick={() => onNavigate('guards')}>
            Glass &amp; Lens Finder →
          </button>
        </div>
        <div className="top-brands-grid">
          {TOP_BRANDS.map((brand) => (
            <button
              key={brand}
              type="button"
              className="brand-pill-card"
              onClick={() => {
                if (onSelectBrand) onSelectBrand(brand);
                onSelectCategory('All');
                onNavigate('catalog');
              }}
            >
              {brand}
            </button>
          ))}
        </div>
      </section>

      {/* Best Quality Best Price Trust Banner */}
      <section className="quality-banner">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Award size={26} color="#FFB800" />
          <div>
            <div style={{ fontWeight: 900, fontSize: 16 }}>
              Best Quality, Best Price — Genuine Accessories &amp; Certified Technicians
            </div>
            <div style={{ fontSize: 13, color: '#CBD5E1' }}>
              Quality-tested MagSafe cases, 9H Tempered Glass, Camera Rings &amp; original GaN fast
              chargers at Edappal&apos;s best prices.
            </div>
          </div>
        </div>
        <button className="btn btn-red btn-sm" onClick={() => onNavigate('guards')}>
          Shop Screen Guards
        </button>
      </section>

      {/* All Accessories Live-Filtered Grid */}
      <section style={{ marginBottom: 32 }}>
        <div className="section-header">
          <h2 className="section-title">
            All Accessories ({allMatchingProducts.length} items)
          </h2>
          <button className="section-link" onClick={() => onNavigate('catalog')}>
            Filter by Brand &amp; Category →
          </button>
        </div>
        {allMatchingProducts.length === 0 ? (
          <div className="card" style={{ textAlign: 'center', padding: 32 }}>
            <h3 style={{ color: 'var(--icase-navy)', marginBottom: 6 }}>
              No Accessories Matched &ldquo;{homeSearch}&rdquo;
            </h3>
            <button
              type="button"
              className="btn btn-outline btn-sm"
              onClick={() => setHomeSearch('')}
            >
              Reset Search
            </button>
          </div>
        ) : (
          <div className="products-grid">{allMatchingProducts.map(renderProductCard)}</div>
        )}
      </section>

      {/* Verified Customer Reviews (Strictly Approved PRODUCT Reviews Only) */}
      <section className="card" style={{ marginBottom: 30 }}>
        <div className="section-header">
          <div>
            <div
              style={{
                fontSize: 11.5,
                fontWeight: 900,
                color: 'var(--icase-green)',
                textTransform: 'uppercase',
                letterSpacing: '0.06em'
              }}
            >
              ✓ Verified Purchase Feedback
            </div>
            <h2 className="section-title">Verified Customer Reviews</h2>
            <p style={{ fontSize: 13, color: 'var(--text-secondary)' }}>
              Only customers with completed Order or Service IDs can submit reviews.
            </p>
          </div>
          {onOpenReviewModal && (
            <button
              type="button"
              className="btn btn-outline btn-sm"
              style={{ borderColor: 'var(--icase-red)', color: 'var(--icase-red)' }}
              onClick={() => onOpenReviewModal('')}
            >
              Write Verified Review
            </button>
          )}
        </div>

        {publicApprovedReviews.length === 0 ? (
          <div
            style={{
              padding: 18,
              borderRadius: 12,
              background: '#F8FAFC',
              border: '1px solid var(--border)',
              color: 'var(--text-secondary)',
              fontSize: 13.5
            }}
          >
            No approved product reviews published yet. Completed orders (ORD-...) can submit a
            verified review!
          </div>
        ) : (
          <div className="reviews-grid">
            {publicApprovedReviews.slice(0, 6).map((r) => (
              <article key={r.id} className="review-card">
                <div>
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      gap: 8,
                      marginBottom: 4
                    }}
                  >
                    <strong style={{ color: 'var(--icase-navy)', fontSize: 14 }}>
                      {r.customerName}
                    </strong>
                    <span style={{ color: '#FFB800', fontSize: 13.5, letterSpacing: 1 }}>
                      {'★'.repeat(Math.max(1, Math.min(5, Number(r.rating || 5))))}
                    </span>
                  </div>
                  <div
                    style={{
                      fontSize: 11.5,
                      fontWeight: 800,
                      color: 'var(--icase-green)',
                      marginBottom: 6
                    }}
                  >
                    ✓ Verified Purchase • {r.targetTitle}
                  </div>
                  <p style={{ fontSize: 13, color: '#334155', lineHeight: 1.5 }}>{r.comment}</p>
                  {r.adminReply && (
                    <div
                      style={{
                        marginTop: 8,
                        padding: 8,
                        borderRadius: 8,
                        background: '#EAF2FF',
                        color: 'var(--icase-navy)',
                        fontSize: 12,
                        fontWeight: 600
                      }}
                    >
                      iCASE Reply: {r.adminReply}
                    </div>
                  )}
                </div>
                <div style={{ fontSize: 11, color: '#94A3B8' }}>
                  {formatTimestamp(r.createdAt)}
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      {/* Repair Services Showcase */}
      <section className="repair-promo-banner">
        <div style={{ maxWidth: 640 }}>
          <div
            style={{
              fontSize: 12,
              fontWeight: 800,
              color: '#FDE047',
              letterSpacing: '0.06em',
              marginBottom: 6
            }}
          >
            EXPERT MOBILE CARE &amp; PICKUP
          </div>
          <h2 style={{ fontSize: 24, fontWeight: 900, marginBottom: 8 }}>
            Cracked Screen, Battery Drain or Charging Issue?
          </h2>
          <p style={{ color: '#CBD5E1', fontSize: 14.5 }}>
            Schedule free doorstep pickup anywhere in Edappal. Repair diagnosis is free, and your
            live repair ticket syncs directly between our website, mobile app, and technician hub.
          </p>
        </div>
        <button className="btn btn-red" onClick={() => onNavigate('repair')}>
          <Wrench size={17} />
          Book Repair &amp; Pickup Now
        </button>
      </section>

      {/* Customer Support & Store Contact Details */}
      <section className="support-card" id="contact-support">
        <div className="section-header" style={{ marginBottom: 4 }}>
          <h2 className="section-title">Customer Support &amp; Store Contact</h2>
        </div>
        <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
          Need help with an order, warranty, or urgent repair pickup? Reach our Edappal team
          directly.
        </p>

        <div className="support-grid">
          <div className="support-item">
            <Phone size={20} color="#E50914" />
            <div>
              <div style={{ fontSize: 12, color: 'var(--text-secondary)', fontWeight: 700 }}>
                SUPPORT PHONE
              </div>
              <a
                href={`tel:+91${supportPhone.replace(/\D/g, '')}`}
                style={{ fontWeight: 800, fontSize: 15, color: 'var(--icase-navy)' }}
              >
                +91 {supportPhone}
              </a>
            </div>
          </div>

          <div className="support-item">
            <Mail size={20} color="#0066CC" />
            <div>
              <div style={{ fontSize: 12, color: 'var(--text-secondary)', fontWeight: 700 }}>
                SUPPORT EMAIL
              </div>
              <a
                href={`mailto:${supportEmail}`}
                style={{ fontWeight: 800, fontSize: 15, color: 'var(--icase-navy)' }}
              >
                {supportEmail}
              </a>
            </div>
          </div>

          <div className="support-item">
            <MapPin size={20} color="#10B981" />
            <div>
              <div style={{ fontSize: 12, color: 'var(--text-secondary)', fontWeight: 700 }}>
                STORE ADDRESS
              </div>
              <div style={{ fontWeight: 700, fontSize: 13.5, color: 'var(--icase-navy)' }}>
                {storeAddress}
              </div>
            </div>
          </div>

          <div className="support-item">
            <Clock size={20} color="#D97706" />
            <div>
              <div style={{ fontSize: 12, color: 'var(--text-secondary)', fontWeight: 700 }}>
                WORKING HOURS
              </div>
              <div style={{ fontWeight: 700, fontSize: 13.5, color: 'var(--icase-navy)' }}>
                {storeHours}
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
