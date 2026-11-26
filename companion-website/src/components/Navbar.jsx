import React from 'react';
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
  Mail
} from 'lucide-react';

export default function Navbar({
  activeTab,
  onSelectTab,
  cartCount,
  onOpenCart,
  onOpenAccount,
  profile,
  settings,
  isAdminLoggedIn
}) {
  const supportPhone = settings.store_phone || '9961454059';
  const supportEmail = settings.store_email || 'icaseedappal@gmail.com';

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
          <div
            className="brand-lockup"
            onClick={() => onSelectTab('home')}
            role="button"
            tabIndex={0}
          >
            <div className="brand-badge">
              <span className="brand-badge-letter">
                <span>i</span>C
              </span>
            </div>
            <div>
              <div className="brand-text-title">
                <span>i</span>CASE
              </div>
              <div className="brand-text-sub">YOUR PHONE, OUR CARE</div>
            </div>
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
            <button className="btn btn-outline btn-sm" onClick={onOpenAccount}>
              <User size={15} />
              <span>
                {profile?.isLoggedIn
                  ? (profile.name || profile.phone || 'Account').split(' ')[0]
                  : 'Account / Login'}
              </span>
            </button>
            <button className="btn btn-red btn-sm cart-btn" onClick={onOpenCart}>
              <ShoppingCart size={16} />
              <span>Cart</span>
              {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
            </button>
          </div>
        </div>
      </header>

      <nav className="mobile-bottom-nav" aria-label="Mobile Navigation">
        <div className="mobile-bottom-nav-inner">
          <button
            className={`mobile-nav-item ${activeTab === 'home' ? 'active' : ''}`}
            onClick={() => onSelectTab('home')}
          >
            <Home size={18} />
            <span>Home</span>
          </button>
          <button
            className={`mobile-nav-item ${activeTab === 'catalog' ? 'active' : ''}`}
            onClick={() => onSelectTab('catalog')}
          >
            <ShoppingBag size={18} />
            <span>Shop</span>
          </button>
          <button
            className={`mobile-nav-item ${activeTab === 'guards' ? 'active' : ''}`}
            onClick={() => onSelectTab('guards')}
          >
            <Shield size={18} />
            <span>Glass</span>
          </button>
          <button
            className={`mobile-nav-item ${activeTab === 'repair' ? 'active' : ''}`}
            onClick={() => onSelectTab('repair')}
          >
            <Wrench size={18} />
            <span>Repair</span>
          </button>
          <button
            className={`mobile-nav-item ${activeTab === 'track' ? 'active' : ''}`}
            onClick={() => onSelectTab('track')}
          >
            <Search size={18} />
            <span>Track</span>
          </button>
          <button
            className={`mobile-nav-item ${activeTab === 'admin' ? 'active' : ''}`}
            onClick={() => onSelectTab('admin')}
          >
            <ShieldCheck size={18} />
            <span>Admin</span>
          </button>
        </div>
      </nav>
    </>
  );
}
