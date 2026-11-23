import React, { useEffect, useMemo, useState } from 'react';
import { Search, Zap, X, Heart } from 'lucide-react';
import { CATEGORIES, formatINR, parseColorOptions } from '../data/constants';

export default function CatalogSection({
  products,
  selectedCategory,
  onSelectCategory,
  settings,
  wishlistIds = [],
  onToggleWishlist,
  onAddToCart,
  onBuyNow,
  selectedProduct,
  onSelectProduct
}) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedColorIdx, setSelectedColorIdx] = useState(0);
  const oneHourEnabled = settings.one_hour_delivery_enabled !== 'false';

  const activeProduct = useMemo(() => {
    if (!selectedProduct) return null;
    return products.find((p) => p.id === selectedProduct.id) || selectedProduct;
  }, [products, selectedProduct]);

  useEffect(() => {
    setSelectedColorIdx(0);
  }, [selectedProduct?.id]);

  const activeColorOptions = useMemo(
    () => (activeProduct ? parseColorOptions(activeProduct.colorOptionsJson) : []),
    [activeProduct]
  );

  const filteredProducts = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return products
      .filter((p) => selectedCategory === 'All' || p.category === selectedCategory)
      .filter(
        (p) =>
          !q ||
          p.name.toLowerCase().includes(q) ||
          p.category.toLowerCase().includes(q) ||
          (p.description || '').toLowerCase().includes(q)
      )
      .sort((a, b) => a.name.localeCompare(b.name));
  }, [products, selectedCategory, searchQuery]);

  return (
    <div>
      <div className="section-header">
        <div>
          <h1 className="section-title">iCASE Product Catalog</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
            Real-time inventory synchronized with the iCASE mobile app &amp; admin hub.
          </p>
        </div>
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

      {filteredProducts.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: 40 }}>
          <h3 style={{ color: 'var(--icase-navy)', marginBottom: 6 }}>No Matching Products</h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
            Try another category or clear your search filter.
          </p>
        </div>
      ) : (
        <div className="products-grid">
          {filteredProducts.map((product) => {
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
              <h2 style={{ color: 'var(--icase-navy)', fontSize: 22, fontWeight: 900, marginBottom: 8 }}>
                {activeProduct.name}
              </h2>
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
                    Color:{' '}
                    <span style={{ fontWeight: 600, color: '#475569' }}>
                      {(activeColorOptions[selectedColorIdx] || activeColorOptions[0]).name}
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
                    {activeColorOptions.map((opt, idx) => {
                      const activeIdx = selectedColorIdx < activeColorOptions.length ? selectedColorIdx : 0;
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
              <div style={{ display: 'flex', gap: 10 }}>
                <button
                  className="btn btn-red"
                  style={{ flex: 1 }}
                  disabled={!activeProduct.inStock}
                  onClick={() => {
                    onAddToCart(activeProduct);
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
                    const prod = activeProduct;
                    onSelectProduct(null);
                    onBuyNow(prod);
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
                      borderColor: wishlistIds.includes(activeProduct.id) ? '#E50914' : 'var(--border)'
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
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
