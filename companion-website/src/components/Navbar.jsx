import React, { useState } from 'react';
import {
  Home,
  ShoppingBag,
  Shield,
  Wrench,
  Search,
  User,
  ShieldCheck,
  ShoppingCart,
  Phone,
  Mail,
  Bike,
  Menu,
  X,
  MessageCircle
} from 'lucide-react';

export function ICaseArchLogo({ darkBg = false, showTagline = true, compact = false }) {
  const caseColor = darkBg ? '#FFFFFF' : '#0B1728';
  const subColor = darkBg ? '#94A3B8' : '#64748B';
  return (
    <div className="icase-arch-logo" style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'center' }}>
      <svg
        width={compact ? 56 : 64}
        height={compact ? 20 : 23}
        viewBox="0 0 80 30"
        fill="none"
        aria-hidden="true"
        style={{ marginBottom: compact ? -8 : -9 }}
      >
        <path
          d="M10 26 C16 4, 64 4, 70 26"
          stroke="#E50914"
          strokeWidth="3.2"
          strokeLinecap="round"
        />
      </svg>
      <div
        style={{
          fontSize: compact ? 20 : 23,
          fontWeight: 900,
          letterSpacing: '-0.02em',
          lineHeight: 1.05,
          color: caseColor
        }}
      >
        <span style={{ color: '#E50914' }}>i</span>CASE
      </div>
      {showTagline && (
        <div
          className="brand-text-sub"
          style={{
            fontSize: compact ? 7.5 : 8.5,
            fontWeight: 800,
            letterSpacing: '0.14em',
            color: subColor,
            textTransform: 'uppercase',
            marginTop: 1
          }}
        >
          YOUR PHONE, OUR CARE
        </div>
      )}
    </div>
  );
}

export default function Navbar({
  activeTab,
  onSelectTab,
  cartCount,
  onOpenCart,
  onOpenAccount,
  isAccountOpen = false,
  profile,
  settings,
  isAdminLoggedIn
}) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const supportPhone = settings.store_phone || '9961454059';
  const supportWhatsapp = settings.store_whatsapp || supportPhone;
  const supportEmail = settings.store_email || 'icaseedappal@gmail.com';
  const waUrl = `https://api.whatsapp.com/send?phone=91${supportWhatsapp.replace(
    /\D/g,
    ''
  )}&text=${encodeURIComponent(
    'Hi iCASE Edappal! I would like to place an order or enquire about a repair.'
  )}`;

  function handleMobileNav(tab) {
    setMobileMenuOpen(false);
    onSelectTab(tab);
  }

  return (
    <>
      <div className="top-utility">
        <div className="container top-utility-inner">
          <div className="top-utility-links">
            <a className="top-utility-item" href={`tel:+91${supportPhone}`}>
              <Phone size={13} />
              <span>Support: +91 {supportPhone}</span>
            </a>
            <a className="top-utility-item" href={`mailto:${supportEmail}`}>
              <Mail size={13} />
              <span>{supportEmail}</span>
            </a>
          </div>
          <div className="sync-pill">
            <span className="sync-dot" />
            <span>Live Cloud Sync with iCASE Mobile App</span>
          </div>
        </div>
      </div>

      <header className="navbar">
        <div className="container navbar-inner">
          <button
            type="button"
            className="mobile-menu-trigger"
            aria-label="Open Navigation Menu"
            onClick={() => setMobileMenuOpen(true)}
          >
            <Menu size={24} />
          </button>

          <div
            className="brand-lockup"
            onClick={() => onSelectTab('home')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                onSelectTab('home');
              }
            }}
          >
            <ICaseArchLogo />
          </div>

          <nav className="nav-links" aria-label="Primary Navigation">
            <button
              className={`nav-link-btn ${activeTab === 'home' ? 'active' : ''}`}
              onClick={() => onSelectTab('home')}
            >
              <Home size={16} /> Home
            </button>
            <button
              className={`nav-link-btn ${activeTab === 'catalog' ? 'active' : ''}`}
              onClick={() => onSelectTab('catalog')}
            >
              <ShoppingBag size={16} /> Catalog
            </button>
            <button
              className={`nav-link-btn ${activeTab === 'guards' ? 'active' : ''}`}
              onClick={() => onSelectTab('guards')}
            >
              <Shield size={16} /> Glass &amp; Lens
            </button>
            <button
              className={`nav-link-btn ${activeTab === 'repair' ? 'active' : ''}`}
              onClick={() => onSelectTab('repair')}
            >
              <Wrench size={16} /> Repair Services
            </button>
            <button
              className={`nav-link-btn ${activeTab === 'track' ? 'active' : ''}`}
              onClick={() => onSelectTab('track')}
            >
              <Search size={16} /> Track Order
            </button>
            <button
              className={`nav-link-btn ${activeTab === 'admin' ? 'active' : ''}`}
              onClick={() => onSelectTab('admin')}
            >
              <ShieldCheck size={16} />
              {isAdminLoggedIn ? 'Admin (Live)' : 'Admin'}
            </button>
          </nav>

          <div className="nav-actions">
            <button
              className="btn btn-outline btn-sm desktop-account-btn"
              onClick={onOpenAccount}
            >
              <User size={15} />
              <span>
                {profile?.isLoggedIn
                  ? (profile.name || profile.phone || 'Account').split(' ')[0]
                  : 'Account / Login'}
              </span>
            </button>
            <button
              className="btn btn-red btn-sm cart-btn header-cart-btn"
              onClick={onOpenCart}
              aria-label="Open Cart"
            >
              <ShoppingCart size={20} />
              <span className="cart-btn-label">Cart</span>
              {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
            </button>
          </div>
        </div>
      </header>

      {/* Mobile Slide-Over Menu Drawer */}
      {mobileMenuOpen && (
        <div
          className="overlay mobile-drawer-overlay"
          onClick={() => setMobileMenuOpen(false)}
          role="dialog"
          aria-modal="true"
          aria-label="Mobile Menu"
        >
          <aside className="mobile-drawer" onClick={(e) => e.stopPropagation()}>
            <div className="mobile-drawer-header">
              <ICaseArchLogo darkBg showTagline />
              <button
                type="button"
                className="mobile-drawer-close"
                onClick={() => setMobileMenuOpen(false)}
                aria-label="Close Menu"
              >
                <X size={20} />
              </button>
            </div>
            <div className="mobile-drawer-body">
              <button
                type="button"
                className={`mobile-drawer-link ${activeTab === 'home' ? 'active' : ''}`}
                onClick={() => handleMobileNav('home')}
              >
                <Home size={18} /> Home
              </button>
              <button
                type="button"
                className={`mobile-drawer-link ${activeTab === 'catalog' ? 'active' : ''}`}
                onClick={() => handleMobileNav('catalog')}
              >
                <ShoppingBag size={18} /> Shop Catalog
              </button>
              <button
                type="button"
                className={`mobile-drawer-link ${activeTab === 'guards' ? 'active' : ''}`}
                onClick={() => handleMobileNav('guards')}
              >
                <Shield size={18} /> Tempered Glass, Lens &amp; Pouches
              </button>
              <button
                type="button"
                className={`mobile-drawer-link ${activeTab === 'repair' ? 'active' : ''}`}
                onClick={() => handleMobileNav('repair')}
              >
                <Wrench size={18} /> Repair Services &amp; Pickup
              </button>
              <button
                type="button"
                className={`mobile-drawer-link ${activeTab === 'track' ? 'active' : ''}`}
                onClick={() => handleMobileNav('track')}
              >
                <Search size={18} /> Track Order / Repair
              </button>
              <button
                type="button"
                className="mobile-drawer-link"
                onClick={() => {
                  setMobileMenuOpen(false);
                  onOpenAccount();
                }}
              >
                <User size={18} /> Account, Orders &amp; Wishlist
              </button>
              <button
                type="button"
                className={`mobile-drawer-link ${activeTab === 'admin' ? 'active' : ''}`}
                onClick={() => handleMobileNav('admin')}
              >
                <ShieldCheck size={18} />
                {isAdminLoggedIn ? 'Admin Panel (Unlocked)' : 'Admin Panel'}
              </button>
              <a
                href={waUrl}
                target="_blank"
                rel="noreferrer"
                className="mobile-drawer-link whatsapp"
                onClick={() => setMobileMenuOpen(false)}
              >
                <MessageCircle size={18} /> WhatsApp Support (+91 {supportPhone})
              </a>
            </div>
          </aside>
        </div>
      )}

      <nav className="mobile-bottom-nav" aria-label="Mobile Navigation">
        <div className="mobile-bottom-nav-inner">
          <button
            type="button"
            className={`mobile-nav-item ${!isAccountOpen && activeTab === 'home' ? 'active' : ''}`}
            onClick={() => onSelectTab('home')}
          >
            <Home size={20} />
            <span>Home</span>
          </button>
          <button
            type="button"
            className={`mobile-nav-item ${!isAccountOpen && activeTab === 'catalog' ? 'active' : ''}`}
            onClick={() => onSelectTab('catalog')}
          >
            <ShoppingBag size={20} />
            <span>Shop</span>
          </button>
          <button
            type="button"
            className={`mobile-nav-item ${!isAccountOpen && activeTab === 'repair' ? 'active' : ''}`}
            onClick={() => onSelectTab('repair')}
          >
            <Wrench size={20} />
            <span>Repair</span>
          </button>
          <button
            type="button"
            className={`mobile-nav-item ${!isAccountOpen && activeTab === 'track' ? 'active' : ''}`}
            onClick={() => onSelectTab('track')}
          >
            <Bike size={20} />
            <span>Pickup</span>
          </button>
          <button
            type="button"
            className={`mobile-nav-item ${isAccountOpen ? 'active' : ''}`}
            onClick={onOpenAccount}
          >
            <User size={20} />
            <span>Account</span>
          </button>
        </div>
      </nav>
    </>
  );
}
