import React from 'react';
import {
  Zap,
  ShoppingBag,
  Wrench,
  Search,
  MessageCircle,
  Phone,
  Mail,
  MapPin,
  Clock,
  ShieldCheck,
  AlertCircle,
  Heart
} from 'lucide-react';
import { CATEGORIES, formatINR } from '../data/constants';

export default function HomeSection({
  products,
  settings,
  wishlistIds = [],
  onToggleWishlist,
  onNavigate,
  onSelectCategory,
  onSelectProduct,
  onAddToCart
}) {
  const isStoreOnline = settings.store_online !== 'false';
  const oneHourEnabled = settings.one_hour_delivery_enabled !== 'false';
  const supportPhone = settings.store_phone || '9961454059';
  const supportWhatsapp = settings.store_whatsapp || supportPhone;
  const supportEmail = settings.store_email || 'icaseedappal@gmail.com';
  const storeAddress =
    settings.store_address || 'Main Road, Near Town Square, Edappal, Malappuram, Kerala';
  const storeHours = settings.store_hours || '9:00 AM - 10:00 PM (Daily)';

  const popularProducts = products
    .filter((p) => p.isPopular)
    .sort((a, b) => (a.popularityRank || 99) - (b.popularityRank || 99))
    .slice(0, 6);

  const featuredProducts = popularProducts.length > 0 ? popularProducts : products.slice(0, 6);

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
              Smart Choice... Better Care. Shop shockproof mobile covers, tempered glass, GaN fast
              chargers, and ANC audio—or book doorstep repair pickup in Edappal, synced live with
              your iCASE mobile app.
            </p>
            <div className="hero-ctas">
              <button className="btn btn-red" onClick={() => onNavigate('catalog')}>
                <ShoppingBag size={17} />
                Explore Shop Catalog
              </button>
              <button className="btn btn-outline" onClick={() => onNavigate('repair')}>
                <Wrench size={17} />
                Book Repair Pickup
              </button>
              <button
                className="btn btn-whatsapp"
                onClick={() =>
                  window.open(
                    `https://api.whatsapp.com/send?phone=91${supportWhatsapp.replace(/\D/g, '')}&text=${encodeURIComponent('Hi iCASE Edappal! I would like to place an order or enquire about a repair.')}`,
                    '_blank'
                  )
                }
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

      {/* Quick Action Tiles */}
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

        <button className="quick-tile" onClick={() => onNavigate('repair')}>
          <div className="quick-tile-icon" style={{ background: '#EAF2FF', color: '#0066CC' }}>
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

        <button
          className="quick-tile"
          onClick={() =>
            window.open(`tel:+91${supportPhone.replace(/\D/g, '')}`, '_self')
          }
        >
          <div className="quick-tile-icon" style={{ background: '#FEF3C7', color: '#D97706' }}>
            <Phone size={22} />
          </div>
          <div>
            <div className="quick-tile-title">Call Support</div>
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

      {/* Popular Products Showcase */}
      <section style={{ marginBottom: 32 }}>
        <div className="section-header">
          <h2 className="section-title">Popular Products</h2>
          <button className="section-link" onClick={() => onNavigate('catalog')}>
            See All Accessories →
          </button>
        </div>
        <div className="products-grid">
          {featuredProducts.map((product) => {
            const discountPct =
              product.originalPrice > product.price
                ? Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)
                : 0;
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
                  <div className="product-category">{product.category}</div>
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
          })}
        </div>
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
