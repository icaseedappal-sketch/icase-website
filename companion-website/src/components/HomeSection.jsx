import React, { useMemo, useState } from 'react';
import {
  Zap,
  ShoppingBag,
  ShoppingCart,
  Shield,
  Wrench,
  Search,
  User,
  MessageCircle,
  Phone,
  Mail,
  ShieldCheck,
  AlertCircle,
  Heart,
  Headphones,
  Star,
  ArrowRight,
  X,
  Play
} from 'lucide-react';
import {
  CATEGORIES,
  TOP_BRANDS,
  computeProductRatingSummary,
  formatINR,
  formatTimestamp,
  getActiveGlassTypes,
  getGlassTypeHelperNote,
  getHomeFeaturedPouches,
  getStartingCameraLensPrice,
  getStartingPouchPrice,
  getStartingTemperedGlassPrice,
  isCameraLensSectionAvailable,
  resolveCameraLensImageUrl,
  resolveGlassTypeImageUrl,
  resolveGlassTypeVideoUrl,
  resolvePouchCardImageUrl,
  resolvePouchImageUrl,
  resolvePouchPrice,
  resolvePouchSelectedColor,
  resolveTemperedGlassImageUrl,
  resolveTemperedGlassPrice
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
  onOpenGuardsSection,
  onOpenGuardsGlassType,
  onOpenVideo,
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

  const glassStartingPrice = useMemo(
    () => getStartingTemperedGlassPrice(screenGuardConfig),
    [screenGuardConfig]
  );
  const cameraStartingPrice = useMemo(
    () => getStartingCameraLensPrice(screenGuardConfig),
    [screenGuardConfig]
  );
  const pouchStartingPrice = useMemo(
    () => getStartingPouchPrice(screenGuardConfig),
    [screenGuardConfig]
  );
  const cameraSectionActive = useMemo(
    () => isCameraLensSectionAvailable(screenGuardConfig),
    [screenGuardConfig]
  );
  const temperedGlassSampleImg = useMemo(
    () => resolveTemperedGlassImageUrl(screenGuardConfig),
    [screenGuardConfig]
  );
  const cameraLensSampleImg = useMemo(
    () => resolveCameraLensImageUrl(screenGuardConfig),
    [screenGuardConfig]
  );
  const pouchSampleImg = useMemo(
    () => resolvePouchCardImageUrl(screenGuardConfig),
    [screenGuardConfig]
  );
  const activeGlassTypes = useMemo(
    () => getActiveGlassTypes(screenGuardConfig),
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

  const publicApprovedReviews = useMemo(
    () =>
      (Array.isArray(publicReviews) ? publicReviews : []).filter(
        (r) => r && r.reviewType === 'PRODUCT' && r.isApproved === true
      ),
    [publicReviews]
  );

  function renderProductCard(product, keyPrefix = 'all') {
    const ratingSummary = computeProductRatingSummary(publicApprovedReviews, product);
    const isWishlisted = wishlistIds.includes(product.id);

    return (
      <article key={`${keyPrefix}-${product.id}`} className="product-card icase-app-product-card">
        <div className="product-media" onClick={() => onSelectProduct(product)}>
          <img src={product.imageUrl} alt={product.name} loading="lazy" />

          {oneHourEnabled && product.isOneHourEligible && (
            <span className="express-badge-top">
              <Zap size={10} fill="currentColor" /> 1-Hour
            </span>
          )}

          {product.isPopular && <span className="popular-badge-top">POPULAR</span>}

          {!product.inStock && <span className="stock-pill-bottom">OUT OF STOCK</span>}

          {product.videoUrl && onOpenVideo && (
            <button
              type="button"
              className="media-video-pill"
              style={{ position: 'absolute', bottom: 8, right: 8 }}
              onClick={(e) => {
                e.stopPropagation();
                onOpenVideo(product.name, product.videoUrl);
              }}
            >
              <Play size={10} fill="currentColor" /> Video
            </button>
          )}
        </div>

        <div className="product-body">
          <div className="product-meta-row">
            <div className="product-category">{product.category}</div>
            {ratingSummary.reviewCount > 0 && (
              <span className="rating-badge">
                ★ {ratingSummary.averageRating.toFixed(1)} ({ratingSummary.reviewCount})
              </span>
            )}
          </div>

          <h3 className="product-title" onClick={() => onSelectProduct(product)}>
            {product.name}
          </h3>

          <div className="product-bottom-row">
            <div className="product-price-col">
              <span className="product-price">{formatINR(product.price)}</span>
              {product.originalPrice > product.price && (
                <span className="product-mrp">{formatINR(product.originalPrice)}</span>
              )}
            </div>

            <div className="product-action-cluster">
              {onToggleWishlist && (
                <button
                  type="button"
                  className={`wishlist-mini-btn ${isWishlisted ? 'active' : ''}`}
                  onClick={() => onToggleWishlist(product.id)}
                  title="Toggle Wishlist"
                  aria-label="Toggle Wishlist"
                >
                  <Heart size={14} fill={isWishlisted ? '#E50914' : 'none'} />
                </button>
              )}
              <button
                type="button"
                className="add-compact-btn"
                disabled={!product.inStock}
                onClick={() => onAddToCart(product)}
              >
                <ShoppingCart size={13} />
                <span>{product.inStock ? 'Add' : 'Out'}</span>
              </button>
            </div>
          </div>
        </div>
      </article>
    );
  }

  return (
    <div className="home-screen-root">
      {!isStoreOnline && (
        <div className="store-alert-banner">
          <AlertCircle size={20} />
          <span>
            Online orders are temporarily paused by Admin. You can still browse products, track
            orders, or contact Customer Support at +91 {supportPhone}.
          </span>
        </div>
      )}

      <div className="home-top-stack">
        {/* Hero Banner matching Android HomeScreen Hero */}
        <section className="hero-banner">
          <div className="hero-grid">
            <div>
              <h1 className="hero-title">
                Mobile Accessories &amp;
                <br />
                <span>Repair Service</span>
              </h1>
              <p className="hero-subtitle">
                Quality Products. Reliable Service.
                <span className="hero-desktop-extra">
                  {' '}
                  — Express 1-hour doorstep delivery &amp; repair pickup in Edappal, Malappuram.
                </span>
              </p>
              <div className="hero-ctas">
                <button className="btn btn-gold" onClick={() => onNavigate('catalog')}>
                  <span>Explore Shop</span>
                  <ArrowRight size={16} />
                </button>
                <button
                  className="btn btn-hero-ghost hero-secondary-desktop"
                  onClick={() => onNavigate('guards')}
                >
                  <Shield size={16} />
                  <span>Tempered Glass &amp; Lens</span>
                </button>
                <button
                  className="btn btn-hero-ghost hero-secondary-desktop"
                  onClick={() => onNavigate('repair')}
                >
                  <Wrench size={16} />
                  <span>Book Repair Pickup</span>
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

        {/* Home Live Search Bar (Sticky below header on mobile) */}
        <div className="home-search-wrap">
          <div className="home-search-pill">
            <Search size={18} color="#64748B" style={{ flexShrink: 0 }} />
            <input
              type="text"
              placeholder="Search cases, chargers, tempered glass..."
              value={homeSearch}
              onChange={(e) => setHomeSearch(e.target.value)}
            />
            {homeSearch && (
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ padding: '4px 10px', flexShrink: 0 }}
                onClick={() => setHomeSearch('')}
              >
                <X size={14} /> Clear
              </button>
            )}
          </div>
        </div>

        {/* 6 Quick Action Tiles matching exact Android HomeScreen colors & labels */}
        <section className="quick-actions-grid" aria-label="Quick Actions">
          <button className="quick-tile" onClick={() => onNavigate('catalog')}>
            <div className="quick-tile-icon" style={{ background: '#FEF3C7', color: '#D97706' }}>
              <ShoppingBag size={20} />
            </div>
            <div className="quick-tile-copy">
              <div className="quick-tile-title">Shop</div>
              <div className="quick-tile-sub">Browse &amp; Buy</div>
            </div>
          </button>

          <button className="quick-tile" onClick={() => onNavigate('repair')}>
            <div className="quick-tile-icon" style={{ background: '#E0E7FF', color: '#4338CA' }}>
              <Wrench size={20} />
            </div>
            <div className="quick-tile-copy">
              <div className="quick-tile-title">Repair</div>
              <div className="quick-tile-sub">Get Expert Service</div>
            </div>
          </button>

          <button className="quick-tile" onClick={() => onNavigate('guards')}>
            <div className="quick-tile-icon" style={{ background: '#FFECEE', color: '#E50914' }}>
              <Shield size={20} />
            </div>
            <div className="quick-tile-copy">
              <div className="quick-tile-title">Tempered Glass</div>
              <div className="quick-tile-sub">Screen Guards &amp; Lens</div>
            </div>
          </button>

          <button className="quick-tile" onClick={() => onNavigate('track')}>
            <div className="quick-tile-icon" style={{ background: '#DCFCE7', color: '#15803D' }}>
              <Search size={20} />
            </div>
            <div className="quick-tile-copy">
              <div className="quick-tile-title">Track Order</div>
              <div className="quick-tile-sub">Know Your Order</div>
            </div>
          </button>

          <button className="quick-tile" onClick={onOpenAccount}>
            <div className="quick-tile-icon" style={{ background: '#F1F5F9', color: '#0F172A' }}>
              <User size={20} />
            </div>
            <div className="quick-tile-copy">
              <div className="quick-tile-title">Account</div>
              <div className="quick-tile-sub">Profile &amp; Sync</div>
            </div>
          </button>

          <button className="quick-tile" onClick={() => window.open(waSupportUrl, '_blank')}>
            <div className="quick-tile-icon" style={{ background: '#E8F9EE', color: '#25D366' }}>
              <MessageCircle size={20} />
            </div>
            <div className="quick-tile-copy">
              <div className="quick-tile-title">WhatsApp</div>
              <div className="quick-tile-sub">Order Now</div>
            </div>
          </button>
        </section>
      </div>

      {/* Screen, Lens & Pouch Protection Cards + Tempered Glass Styles */}
      <section style={{ marginBottom: 26 }}>
        <div className="section-header">
          <div>
            <h2 className="section-title">🛡️ Screen, Lens &amp; Pouch Protection</h2>
            <p className="section-sub">Tap a card or glass style to choose your phone model</p>
          </div>
          <button
            className="section-link"
            onClick={() => {
              if (onOpenGuardsSection) onOpenGuardsSection('glass');
              else onNavigate('guards');
            }}
          >
            Explore All →
          </button>
        </div>

        <div className="guard-sample-grid">
          <div
            role="button"
            tabIndex={0}
            className="guard-sample-card"
            onClick={() => {
              if (onOpenGuardsSection) onOpenGuardsSection('glass');
              else onNavigate('guards');
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                if (onOpenGuardsSection) onOpenGuardsSection('glass');
                else onNavigate('guards');
              }
            }}
          >
            <div className="guard-sample-thumb">
              <img src={temperedGlassSampleImg} alt="Tempered Glass Protector" loading="lazy" />
              <span className="guard-starting-badge">Starting {formatINR(glassStartingPrice)}</span>
            </div>
            <div className="guard-sample-body">
              <div className="guard-sample-top">
                <div className="guard-card-title">Tempered Glass</div>
                <div className="compat-pill compat-blue">
                  Available for iPhone &amp; All Android Models
                </div>
                <p className="guard-card-desc">
                  Privacy, Matte, Borderless, 360 &amp; Normal 5D
                </p>
              </div>
              <div className="guard-card-cta">Select Glass →</div>
            </div>
          </div>

          {cameraSectionActive && (
            <div
              role="button"
              tabIndex={0}
              className="guard-sample-card"
              onClick={() => {
                if (onOpenGuardsSection) onOpenGuardsSection('camera');
                else onNavigate('guards');
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  if (onOpenGuardsSection) onOpenGuardsSection('camera');
                  else onNavigate('guards');
                }
              }}
            >
              <div className="guard-sample-thumb">
                <img src={cameraLensSampleImg} alt="Camera Lens Protector" loading="lazy" />
                <span className="guard-starting-badge">
                  Starting {formatINR(cameraStartingPrice)}
                </span>
              </div>
              <div className="guard-sample-body">
                <div className="guard-sample-top">
                  <div className="guard-card-title">Camera Lens Protector</div>
                  <div className="compat-pill compat-amber">
                    iPhone &amp; Samsung S Series Only
                  </div>
                  <p className="guard-card-desc">
                    9H Sapphire Rings • Galaxy S20–S25 &amp; iPhone
                  </p>
                </div>
                <div className="guard-card-cta">Select Lens →</div>
              </div>
            </div>
          )}

          <div
            role="button"
            tabIndex={0}
            className={`guard-sample-card ${cameraSectionActive ? 'guard-sample-card-full-mobile' : ''}`}
            onClick={() => {
              if (onOpenGuardsPouch) onOpenGuardsPouch();
              else if (onOpenGuardsSection) onOpenGuardsSection('pouch');
              else onNavigate('guards');
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                if (onOpenGuardsPouch) onOpenGuardsPouch();
                else onNavigate('guards');
              }
            }}
          >
            <div className="guard-sample-thumb">
              <img src={pouchSampleImg} alt="Pouches & Back Cases" loading="lazy" />
              <span className="guard-starting-badge">
                Starting {formatINR(pouchStartingPrice)}
              </span>
            </div>
            <div className="guard-sample-body">
              <div className="guard-sample-top">
                <div className="guard-card-title">Pouches &amp; Back Cases</div>
                <div className="compat-pill compat-green">
                  iPhone, Samsung S Series &amp; Other Android Models
                </div>
                <p className="guard-card-desc">
                  MagSafe Clear, Matte Armor &amp; Velvet Silicone Cases
                </p>
              </div>
              <div className="guard-card-cta">Select Pouch →</div>
            </div>
          </div>
        </div>

        {/* Dynamic Tempered Glass Styles (Quick Select) */}
        {activeGlassTypes.length > 0 && (
          <div style={{ marginTop: 16 }}>
            <div className="section-header" style={{ marginBottom: 8 }}>
              <h3 style={{ fontSize: 15, fontWeight: 900, color: 'var(--icase-navy)' }}>
                Tempered Glass Styles (Quick Select)
              </h3>
            </div>
            <div className="glass-type-examples-grid">
              {activeGlassTypes.map((gt) => {
                const pricing = resolveTemperedGlassPrice('iPhone', '__default__', gt, {
                  ...(screenGuardConfig || {}),
                  modelConfigs: {}
                });
                const gtImg = resolveGlassTypeImageUrl(gt, screenGuardConfig);
                const gtVideo = resolveGlassTypeVideoUrl(gt, screenGuardConfig);
                const note = getGlassTypeHelperNote(gt, screenGuardConfig);

                return (
                  <div
                    key={gt}
                    role="button"
                    tabIndex={0}
                    className="glass-type-example-card"
                    onClick={() => {
                      if (onOpenGuardsGlassType) onOpenGuardsGlassType(gt);
                      else if (onOpenGuardsSection) onOpenGuardsSection('glass');
                      else onNavigate('guards');
                    }}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        if (onOpenGuardsGlassType) onOpenGuardsGlassType(gt);
                        else onNavigate('guards');
                      }
                    }}
                  >
                    <div className="glass-style-top-row">
                      <img src={gtImg} alt={gt} className="glass-type-example-thumb" loading="lazy" />
                      <div style={{ minWidth: 0, flex: 1 }}>
                        <strong className="glass-style-name">{gt}</strong>
                        <div className="glass-style-price-row">
                          <span className="glass-style-price">{formatINR(pricing.finalPrice)}</span>
                          {pricing.basePrice > pricing.finalPrice && (
                            <span className="glass-style-mrp">{formatINR(pricing.basePrice)}</span>
                          )}
                        </div>
                      </div>
                    </div>
                    <div className="glass-style-note">{note}</div>
                    <div className="glass-style-badges">
                      {pricing.offerBadge && (
                        <span className="glass-style-offer">{pricing.offerBadge}</span>
                      )}
                      {gtVideo && onOpenVideo && (
                        <button
                          type="button"
                          className="media-video-pill"
                          onClick={(e) => {
                            e.stopPropagation();
                            onOpenVideo(`${gt} Tempered Glass`, gtVideo);
                          }}
                        >
                          <Play size={9} fill="currentColor" /> Video
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </section>

      {/* Popular Products Showcase (Horizontal LazyRow on mobile, grid on desktop) */}
      <section style={{ marginBottom: 26 }}>
        <div className="section-header">
          <h2 className="section-title">Popular Products</h2>
          <button className="section-link" onClick={() => onNavigate('catalog')}>
            See All →
          </button>
        </div>
        <div className="products-carousel-row">
          {featuredProducts.map((p) => renderProductCard(p, 'pop'))}
        </div>
      </section>

      {/* Featured Pouches & Cases */}
      {homeFeaturedPouches.length > 0 && (
        <section style={{ marginBottom: 26 }}>
          <div className="section-header">
            <div>
              <h2 className="section-title">Featured Pouches &amp; Cases</h2>
              <p className="section-sub">Pick your phone brand &amp; case color</p>
            </div>
            <button
              className="section-link"
              onClick={() => {
                if (onOpenGuardsPouch) onOpenGuardsPouch();
                else onNavigate('guards');
              }}
            >
              All Pouches →
            </button>
          </div>

          <div className="pouches-carousel-row">
            {homeFeaturedPouches.map((pouch) => {
              const pricing = resolvePouchPrice(pouch);
              const activeColor = resolvePouchSelectedColor(
                pouch,
                selectedHomePouchColors[pouch.id] || ''
              );
              const thumbUrl = resolvePouchImageUrl(pouch, activeColor?.name || '');
              const colors = Array.isArray(pouch.colorOptions) ? pouch.colorOptions : [];

              return (
                <article key={pouch.id} className="product-card icase-app-product-card">
                  <div className="product-media">
                    <img src={thumbUrl} alt={pouch.name} loading="lazy" />
                    {pricing.hasOffer && pricing.offerBadge && (
                      <span className="express-badge-top">{pricing.offerBadge}</span>
                    )}
                    {pouch.videoUrl && onOpenVideo && (
                      <button
                        type="button"
                        className="media-video-pill"
                        style={{ position: 'absolute', bottom: 8, right: 8 }}
                        onClick={(e) => {
                          e.stopPropagation();
                          onOpenVideo(pouch.name, pouch.videoUrl);
                        }}
                      >
                        <Play size={10} fill="currentColor" /> Video
                      </button>
                    )}
                  </div>
                  <div className="product-body">
                    <div className="home-pouch-brands">
                      {(pouch.compatibleBrands || []).join(' • ')}
                    </div>
                    <h3 className="product-title">{pouch.name}</h3>
                    <div className="product-price-row" style={{ marginBottom: 8 }}>
                      <span className="product-price">{formatINR(pricing.finalPrice)}</span>
                      {pricing.hasOffer && pricing.basePrice > pricing.finalPrice && (
                        <span className="product-mrp">{formatINR(pricing.basePrice)}</span>
                      )}
                    </div>
                    <div className="home-pouch-swatches">
                      {colors.slice(0, 6).map((c) => {
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
                              width: 22,
                              height: 22,
                              borderRadius: '50%',
                              padding: 2,
                              background: '#FFFFFF',
                              border: selected ? '2px solid #0A1128' : '1px solid #CBD5E1',
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
                    <button
                      type="button"
                      className="guard-card-cta"
                      onClick={() => {
                        if (onOpenGuardsPouch) onOpenGuardsPouch();
                        else onNavigate('guards');
                      }}
                    >
                      Choose Model &amp; Buy →
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        </section>
      )}

      {/* Top Brands Strip */}
      <section style={{ marginBottom: 24 }}>
        <div className="section-header">
          <h2 className="section-title">Top Brands</h2>
          <button className="section-link" onClick={() => onNavigate('catalog')}>
            View All →
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

      {/* Best Quality / Best Price Navy Crown Banner matching Android HomeScreen */}
      <section
        className="quality-banner-card"
        role="button"
        tabIndex={0}
        onClick={() => onNavigate('guards')}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onNavigate('guards');
          }
        }}
      >
        <div>
          <div className="quality-gold-title">Best Quality</div>
          <div className="quality-white-title">Best Price</div>
          <div className="quality-sub">
            Genuine mobile accessories &amp; 6-month repair warranty in Edappal
          </div>
        </div>
        <div className="quality-crown-circle" aria-hidden="true">
          <Star size={24} color="#FFB800" fill="#FFB800" />
        </div>
      </section>

      {/* Category Shortcuts + All Accessories 2-Column Mobile Grid */}
      <section style={{ marginBottom: 28 }}>
        <div className="section-header" style={{ marginBottom: 10 }}>
          <h2 className="section-title">All Accessories ({allMatchingProducts.length})</h2>
          <button className="section-link" onClick={() => onNavigate('catalog')}>
            Filter All →
          </button>
        </div>

        <div className="category-chips" style={{ marginBottom: 14 }}>
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
          <div className="products-grid">
            {allMatchingProducts.map((p) => renderProductCard(p, 'all'))}
          </div>
        )}
      </section>

      {/* Verified Customer Reviews (Strictly Approved PRODUCT Reviews Only) */}
      <section className="card" style={{ marginBottom: 26 }}>
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
            <p className="section-sub">
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
              padding: 16,
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
              color: '#FFB800',
              letterSpacing: '0.06em',
              marginBottom: 6
            }}
          >
            EXPERT MOBILE CARE &amp; PICKUP
          </div>
          <h2 style={{ fontSize: 22, fontWeight: 900, marginBottom: 8 }}>
            Cracked Screen, Battery Drain or Charging Issue?
          </h2>
          <p style={{ color: '#CBD5E1', fontSize: 14 }}>
            Schedule free doorstep pickup anywhere in Edappal. Repair diagnosis is free, and your
            live repair ticket syncs directly between our website, mobile app, and technician hub.
          </p>
        </div>
        <button className="btn btn-gold" onClick={() => onNavigate('repair')}>
          <Wrench size={16} />
          <span>Book Repair &amp; Pickup</span>
        </button>
      </section>

      {/* Customer Support & Store Contact Card matching Android CustomerSupportCard */}
      <section className="support-card" id="contact-support">
        <div className="support-header-row">
          <div className="support-header-icon">
            <Headphones size={22} color="#0066CC" />
          </div>
          <div>
            <h2 className="support-card-title">Customer Support &amp; Store</h2>
            <div className="support-card-meta">
              {storeAddress} • {storeHours}
            </div>
          </div>
        </div>
        <p className="support-card-desc">
          Need help with your order, warranty or product details? Reach out to our team:
        </p>

        <div className="support-rows-stack">
          <a
            className="support-action-row phone"
            href={`tel:+91${supportPhone.replace(/\D/g, '')}`}
          >
            <div className="support-row-circle" style={{ background: '#0066CC' }}>
              <Phone size={18} color="#FFFFFF" />
            </div>
            <div className="support-row-copy">
              <div className="support-row-title">Phone Support</div>
              <div className="support-row-val">+91 {supportPhone}</div>
            </div>
            <span className="support-row-cta" style={{ color: '#0066CC' }}>
              CALL NOW
            </span>
          </a>

          <a
            className="support-action-row whatsapp"
            href={waSupportUrl}
            target="_blank"
            rel="noreferrer"
          >
            <div className="support-row-circle" style={{ background: '#25D366' }}>
              <MessageCircle size={18} color="#FFFFFF" />
            </div>
            <div className="support-row-copy">
              <div className="support-row-title">WhatsApp Support</div>
              <div className="support-row-val">+91 {supportWhatsapp}</div>
            </div>
            <span className="support-row-cta" style={{ color: '#16A34A' }}>
              CHAT
            </span>
          </a>

          <a className="support-action-row email" href={`mailto:${supportEmail}`}>
            <div className="support-row-circle" style={{ background: '#E50914' }}>
              <Mail size={18} color="#FFFFFF" />
            </div>
            <div className="support-row-copy">
              <div className="support-row-title">Official Gmail</div>
              <div className="support-row-val">{supportEmail}</div>
            </div>
            <span className="support-row-cta" style={{ color: '#E50914' }}>
              EMAIL
            </span>
          </a>
        </div>
      </section>
    </div>
  );
}
