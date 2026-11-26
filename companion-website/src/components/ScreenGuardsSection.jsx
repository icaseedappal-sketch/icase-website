import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  Search,
  Shield,
  Camera,
  ShoppingBag,
  Play,
  X,
  MessageCircle,
  Check,
  AlertTriangle
} from 'lucide-react';
import {
  OTHER_ANDROID_POPULAR_MODELS,
  OTHER_ANDROID_POUCHES_OPTION,
  OTHER_MODEL_OPTION,
  POUCH_BRANDS,
  SAMPLE_GUARD_VIDEO_URL,
  buildCameraLensCartProduct,
  buildCameraLensWhatsAppMessage,
  buildComboCartProduct,
  buildOtherAndroidPouchWhatsAppMessage,
  buildPouchCartProduct,
  buildTemperedGlassCartProduct,
  buildTemperedGlassWhatsAppMessage,
  createDefaultScreenGuardConfig,
  formatINR,
  getActiveCameraLensBrands,
  getActivePouchesForBrand,
  getActiveScreenGuardBrands,
  getGlassTypeHelperNote,
  getGlassTypesForDevice,
  getPopularCameraLensModels,
  getPopularGlassModels,
  isDeviceEligibleForCameraLens,
  resolveCameraLensImageUrl,
  resolveCameraLensPrice,
  resolveComboPrice,
  resolveGlassTypeImageUrl,
  resolveGlassTypeVideoUrl,
  resolvePouchCardImageUrl,
  resolvePouchImageUrl,
  resolvePouchPrice,
  resolvePouchSelectedColor,
  resolveTemperedGlassImageUrl,
  resolveTemperedGlassPrice,
  searchCameraLensCatalog,
  searchPouchCatalog,
  searchScreenGuardCatalog
} from '../data/constants';

export default function ScreenGuardsSection({
  screenGuardConfig,
  settings,
  onAddToCart,
  initialFocus = '',
  initialGlassType = '',
  onOpenVideo
}) {
  const config = screenGuardConfig || createDefaultScreenGuardConfig();

  // Tempered Glass state
  const [glassBrand, setGlassBrand] = useState('');
  const [glassModel, setGlassModel] = useState('');
  const [glassSearch, setGlassSearch] = useState('');
  const [glassDropdownOpen, setGlassDropdownOpen] = useState(false);
  const [glassManualModel, setGlassManualModel] = useState('');
  const [glassType, setGlassType] = useState(initialGlassType || 'Normal 5D');
  const [glassError, setGlassError] = useState('');

  // Camera Lens Protection state
  const [cameraBrand, setCameraBrand] = useState('');
  const [cameraModel, setCameraModel] = useState('');
  const [cameraSearch, setCameraSearch] = useState('');
  const [cameraDropdownOpen, setCameraDropdownOpen] = useState(false);

  // Pouches state
  const [pouchBrand, setPouchBrand] = useState('iPhone');
  const [pouchModel, setPouchModel] = useState('');
  const [pouchSearch, setPouchSearch] = useState('');
  const [pouchDropdownOpen, setPouchDropdownOpen] = useState(false);
  const [pouchError, setPouchError] = useState('');
  const [selectedPouchColors, setSelectedPouchColors] = useState({});
  const [otherAndroidModel, setOtherAndroidModel] = useState('');
  const [otherAndroidPouchPref, setOtherAndroidPouchPref] = useState('Any Matching Design');

  // Installation & Video modal state
  const [installationOption, setInstallationOption] = useState('Shop Installation');
  const [videoModalOpen, setVideoModalOpen] = useState(false);
  const [videoError, setVideoError] = useState(false);

  const glassDropRef = useRef(null);
  const cameraDropRef = useRef(null);
  const pouchDropRef = useRef(null);
  const glassSectionRef = useRef(null);
  const cameraSectionRef = useRef(null);
  const pouchSectionRef = useRef(null);

  useEffect(() => {
    if (initialGlassType) {
      setGlassType(initialGlassType);
    }
  }, [initialGlassType]);

  useEffect(() => {
    const targetRef =
      initialFocus === 'camera'
        ? cameraSectionRef
        : initialFocus === 'pouch'
          ? pouchSectionRef
          : initialFocus === 'glass'
            ? glassSectionRef
            : null;
    if (targetRef?.current) {
      setTimeout(() => {
        targetRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 60);
    }
  }, [initialFocus]);

  useEffect(() => {
    function handleDocClick(e) {
      if (glassDropRef.current && !glassDropRef.current.contains(e.target)) {
        setGlassDropdownOpen(false);
      }
      if (cameraDropRef.current && !cameraDropRef.current.contains(e.target)) {
        setCameraDropdownOpen(false);
      }
      if (pouchDropRef.current && !pouchDropRef.current.contains(e.target)) {
        setPouchDropdownOpen(false);
      }
    }
    document.addEventListener('mousedown', handleDocClick);
    return () => document.removeEventListener('mousedown', handleDocClick);
  }, []);

  const storeWaDigits =
    (settings.store_whatsapp || settings.store_phone || '9961454059').replace(/\D/g, '') ||
    '9961454059';

  function buildWaUrl(message) {
    return `https://api.whatsapp.com/send?phone=91${storeWaDigits}&text=${encodeURIComponent(
      message
    )}`;
  }

  // Active brands & glass types validation
  const activeGlassBrands = useMemo(() => getActiveScreenGuardBrands(config), [config]);

  useEffect(() => {
    if (glassBrand && !activeGlassBrands.includes(glassBrand)) {
      setGlassBrand('');
      if (glassModel !== OTHER_MODEL_OPTION) setGlassModel('');
    }
  }, [activeGlassBrands, glassBrand, glassModel]);

  const isManualGlass = glassModel === OTHER_MODEL_OPTION;
  const availableGlassTypes = useMemo(
    () => getGlassTypesForDevice(glassBrand, glassModel, config),
    [glassBrand, glassModel, config]
  );

  useEffect(() => {
    if (availableGlassTypes.length > 0 && !availableGlassTypes.includes(glassType)) {
      setGlassType(availableGlassTypes[0]);
    }
  }, [availableGlassTypes, glassType]);

  const groupedGlass = useMemo(
    () => searchScreenGuardCatalog(glassBrand, glassSearch, config),
    [glassBrand, glassSearch, config]
  );

  const popularGlassModels = useMemo(
    () => getPopularGlassModels(glassBrand, config),
    [glassBrand, config]
  );

  const glassPricing = useMemo(() => {
    if (!glassModel || availableGlassTypes.length === 0) return null;
    return resolveTemperedGlassPrice(
      isManualGlass ? 'CUSTOM' : glassBrand,
      isManualGlass ? glassManualModel.trim() || OTHER_MODEL_OPTION : glassModel,
      glassType,
      config
    );
  }, [glassModel, availableGlassTypes, isManualGlass, glassBrand, glassManualModel, glassType, config]);

  const glassHelperNote =
    availableGlassTypes.length > 0 ? getGlassTypeHelperNote(glassType, config) : '';

  // Camera lens catalog
  const activeCameraBrands = useMemo(() => getActiveCameraLensBrands(config), [config]);

  useEffect(() => {
    if (cameraBrand && !activeCameraBrands.includes(cameraBrand)) {
      setCameraBrand('');
      setCameraModel('');
    }
  }, [activeCameraBrands, cameraBrand]);

  const groupedCamera = useMemo(
    () => searchCameraLensCatalog(cameraBrand, cameraSearch, config),
    [cameraBrand, cameraSearch, config]
  );

  const popularCameraModels = useMemo(
    () => getPopularCameraLensModels(cameraBrand, config),
    [cameraBrand, config]
  );

  const cameraPricing = useMemo(() => {
    if (!cameraModel) return null;
    return resolveCameraLensPrice(cameraBrand, cameraModel, config);
  }, [cameraBrand, cameraModel, config]);

  // Combo pricing when both glass & camera lens selected
  const comboGlassSelection = useMemo(() => {
    if (!glassModel) return null;
    const effectiveModel = isManualGlass ? glassManualModel.trim() : glassModel;
    if (!effectiveModel) return null;
    const brandPart = isManualGlass ? 'CUSTOM' : glassBrand;
    if (!availableGlassTypes.includes(glassType)) return null;
    return { brand: brandPart, model: effectiveModel, glassType };
  }, [glassModel, isManualGlass, glassManualModel, glassBrand, availableGlassTypes, glassType]);

  const comboPricing = useMemo(() => {
    if (!cameraModel || !comboGlassSelection || config.comboEnabled === false) return null;
    return resolveComboPrice(
      comboGlassSelection.brand,
      comboGlassSelection.model,
      comboGlassSelection.glassType,
      cameraBrand,
      cameraModel,
      config
    );
  }, [cameraModel, comboGlassSelection, config, cameraBrand]);

  // Pouches catalog
  const isOtherAndroidPouches = pouchBrand === OTHER_ANDROID_POUCHES_OPTION;
  const pouchModels = useMemo(
    () => (isOtherAndroidPouches ? [] : searchPouchCatalog(pouchBrand, pouchSearch, config)),
    [isOtherAndroidPouches, pouchBrand, pouchSearch, config]
  );
  const activePouches = useMemo(
    () => (isOtherAndroidPouches ? [] : getActivePouchesForBrand(pouchBrand, config)),
    [isOtherAndroidPouches, pouchBrand, config]
  );

  function handleAddGlassToCart() {
    const effectiveModel = isManualGlass ? glassManualModel.trim() : glassModel;
    if (!effectiveModel) {
      setGlassError('Please enter your exact device model name first.');
      return;
    }
    const brandPart = isManualGlass ? 'CUSTOM' : glassBrand;
    const prod = buildTemperedGlassCartProduct(
      brandPart,
      effectiveModel,
      glassType,
      config,
      installationOption
    );
    onAddToCart(prod, 1);
  }

  function handleAddCameraToCart() {
    if (!cameraModel) return;
    const prod = buildCameraLensCartProduct(cameraBrand, cameraModel, config, installationOption);
    onAddToCart(prod, 1);
  }

  function handleAddComboToCart() {
    if (!cameraModel || !comboGlassSelection) return;
    const prod = buildComboCartProduct(
      comboGlassSelection.brand,
      comboGlassSelection.model,
      comboGlassSelection.glassType,
      cameraBrand,
      cameraModel,
      config,
      installationOption
    );
    onAddToCart(prod, 1);
  }

  function handleAddPouchToCart(pouch) {
    if (!pouchModel) {
      setPouchError(`Please select your ${pouchBrand} model first.`);
      setPouchDropdownOpen(true);
      return;
    }
    const chosenColor =
      resolvePouchSelectedColor(pouch, selectedPouchColors[pouch.id] || '')?.name || '';
    setPouchError('');
    const prod = buildPouchCartProduct(pouchBrand, pouchModel, pouch, chosenColor);
    onAddToCart(prod, 1, chosenColor);
  }

  const showCameraCard = activeCameraBrands.length > 0;
  const temperedGlassSampleImg = resolveGlassTypeImageUrl(glassType, config);
  const activeGlassTypeVideoUrl = resolveGlassTypeVideoUrl(glassType, config);
  const cameraLensSampleImg = resolveCameraLensImageUrl(config);
  const cameraLensVideoUrl = String(config?.cameraLensDefault?.videoUrl || '').trim();
  const pouchHeaderImg = resolvePouchCardImageUrl(config);
  const allActivePouchDesigns = useMemo(
    () => (Array.isArray(config?.pouchItems) ? config.pouchItems.filter((p) => p && !p.deleted && p.enabled !== false) : []),
    [config]
  );

  const selectedGlassDeviceName = isManualGlass ? glassManualModel.trim() : glassModel;
  const selectedGlassDeviceUnsupportedForCamera = useMemo(() => {
    if (!selectedGlassDeviceName) return false;
    return !isDeviceEligibleForCameraLens(
      isManualGlass ? 'CUSTOM' : glassBrand,
      selectedGlassDeviceName,
      config
    );
  }, [selectedGlassDeviceName, isManualGlass, glassBrand, config]);

  return (
    <div className="guards-stack">
      <div className="section-header" style={{ marginBottom: 0 }}>
        <div>
          <h1 className="section-title">
            <Shield size={23} color="#E50914" /> Tempered Glass, Camera Lens &amp; Pouches
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
            Precision device-filtered tempered glass, camera lens protection rings, and pouches with
            live Admin prices &amp; offers.
          </p>
        </div>
      </div>

      {/* 1. TEMPERED GLASS CARD */}
      <section
        id="tempered-glass-wrapper"
        ref={glassSectionRef}
        className={`card ${initialFocus === 'glass' ? 'section-focus-ring' : ''}`}
        style={{ display: 'flex', flexDirection: 'column', gap: 16 }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 14, flexWrap: 'wrap' }}>
          <img
            src={temperedGlassSampleImg}
            alt="Tempered Glass Sample"
            style={{
              width: 60,
              height: 60,
              borderRadius: 12,
              objectFit: 'cover',
              border: '1px solid var(--border)',
              flexShrink: 0
            }}
          />
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', marginBottom: 4 }}>
              <h2 style={{ color: 'var(--icase-navy)', fontSize: 20, fontWeight: 900 }}>
                Tempered Glass
              </h2>
              <span className="compat-pill all">Available for iPhone &amp; All Android Models</span>
              {activeGlassTypeVideoUrl && onOpenVideo && (
                <button
                  type="button"
                  className="media-video-pill"
                  onClick={() => onOpenVideo(`${glassType} Tempered Glass`, activeGlassTypeVideoUrl)}
                >
                  <Play size={10} fill="currentColor" /> Watch {glassType} Video
                </button>
              )}
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: 13.5 }}>
              Select your brand &amp; model for exact tempered glass
            </p>
          </div>
        </div>

        {!glassModel && glassType && (
          <div
            className="helper-note-bar"
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              flexWrap: 'wrap',
              gap: 8
            }}
          >
            <span>
              Pre-selected style: <strong>{glassType}</strong> — pick your phone model below to see
              exact availability &amp; price.
            </span>
          </div>
        )}

        <div className="category-chips">
          <button
            type="button"
            className={`chip ${!glassBrand ? 'active' : ''}`}
            onClick={() => {
              setGlassBrand('');
              setGlassModel('');
              setGlassManualModel('');
              setGlassSearch('');
              setGlassError('');
              setGlassDropdownOpen(true);
            }}
          >
            All Brands
          </button>
          {activeGlassBrands.map((brand) => (
            <button
              key={brand}
              type="button"
              className={`chip ${glassBrand === brand ? 'active' : ''}`}
              onClick={() => {
                setGlassBrand(brand);
                setGlassModel('');
                setGlassManualModel('');
                setGlassSearch('');
                setGlassError('');
                setGlassDropdownOpen(true);
              }}
            >
              {brand}
            </button>
          ))}
        </div>

        <div className="searchable-dropdown" ref={glassDropRef}>
          <label
            style={{
              display: 'block',
              fontSize: 11.5,
              fontWeight: 800,
              color: 'var(--text-secondary)',
              textTransform: 'uppercase',
              marginBottom: 6
            }}
          >
            Phone Model (Searchable Dropdown)
          </label>
          <div className="searchable-dropdown-box">
            <Search size={16} color="#64748B" />
            <input
              type="text"
              placeholder="Search iPhone 15, Redmi Note 13, Galaxy S24, Nord 4..."
              value={
                glassDropdownOpen
                  ? glassSearch
                  : glassModel
                    ? isManualGlass
                      ? OTHER_MODEL_OPTION
                      : `${glassBrand} — ${glassModel}`
                    : ''
              }
              onFocus={() => setGlassDropdownOpen(true)}
              onChange={(e) => {
                setGlassSearch(e.target.value);
                setGlassDropdownOpen(true);
              }}
            />
            {glassModel && (
              <button
                type="button"
                className="section-link"
                style={{ fontSize: 12 }}
                onClick={() => {
                  setGlassModel('');
                  setGlassManualModel('');
                  setGlassSearch('');
                  setGlassError('');
                  setGlassDropdownOpen(true);
                }}
              >
                Clear
              </button>
            )}
          </div>

          {glassDropdownOpen && (
            <div className="searchable-dropdown-menu">
              {popularGlassModels.length > 0 && !glassSearch.trim() && (
                <div
                  style={{
                    padding: '10px 12px',
                    borderBottom: '1px solid #E2E8F0',
                    background: '#F8FAFC'
                  }}
                >
                  <div
                    style={{
                      fontSize: 10.5,
                      fontWeight: 900,
                      textTransform: 'uppercase',
                      letterSpacing: '0.05em',
                      color: '#475569',
                      marginBottom: 6
                    }}
                  >
                    Popular Models (Quick Select)
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                    {popularGlassModels.map((item) => {
                      const active = glassBrand === item.brand && glassModel === item.model;
                      return (
                        <button
                          key={`pop-glass-${item.brand}-${item.model}`}
                          type="button"
                          onClick={() => {
                            setGlassBrand(item.brand);
                            setGlassModel(item.model);
                            setGlassManualModel('');
                            setGlassSearch('');
                            setGlassError('');
                            setGlassDropdownOpen(false);
                          }}
                          style={{
                            padding: '6px 11px',
                            borderRadius: 999,
                            fontSize: 12,
                            fontWeight: 700,
                            border: active ? '1px solid #0B1728' : '1px solid #CBD5E1',
                            background: active ? '#0B1728' : '#FFFFFF',
                            color: active ? '#FFFFFF' : '#0B1728',
                            cursor: 'pointer'
                          }}
                        >
                          {item.model}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              {groupedGlass.length === 0 ? (
                <div style={{ padding: 14, fontSize: 13, color: 'var(--text-secondary)' }}>
                  No preset model matched &ldquo;{glassSearch}&rdquo;. Choose{' '}
                  <strong>{OTHER_MODEL_OPTION}</strong> below.
                </div>
              ) : (
                groupedGlass.map((grp) => (
                  <div key={grp.brand}>
                    <div className="dropdown-group-label">{grp.brand}</div>
                    {grp.models.map((m) => {
                      const active = glassBrand === grp.brand && glassModel === m;
                      return (
                        <button
                          key={`${grp.brand}-${m}`}
                          type="button"
                          className={`dropdown-option-btn ${active ? 'selected' : ''}`}
                          onClick={() => {
                            setGlassBrand(grp.brand);
                            setGlassModel(m);
                            setGlassManualModel('');
                            setGlassSearch('');
                            setGlassError('');
                            setGlassDropdownOpen(false);
                          }}
                        >
                          <span>{m}</span>
                          {active && <Check size={15} />}
                        </button>
                      );
                    })}
                  </div>
                ))
              )}

              <button
                type="button"
                className="dropdown-option-btn"
                style={{ background: '#FFF1F2', color: 'var(--icase-red)', fontWeight: 800 }}
                onClick={() => {
                  setGlassModel(OTHER_MODEL_OPTION);
                  setGlassSearch('');
                  setGlassError('');
                  setGlassDropdownOpen(false);
                }}
              >
                <span>{OTHER_MODEL_OPTION}</span>
                <span style={{ fontSize: 11.5 }}>Type Manually →</span>
              </button>
            </div>
          )}
        </div>

        {glassBrand === 'Samsung' && (
          <a
            href={buildWaUrl(
              'Hi iCASE Edappal! I have a curved-display Samsung S series phone (S20 / S21 Ultra / S22 Ultra / S23 Ultra). Please share UV tempered glass options & pricing.'
            )}
            target="_blank"
            rel="noreferrer"
            className="helper-note-bar"
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: 10,
              border: '1px solid rgba(0,102,204,0.25)'
            }}
          >
            <span>Curved display S series? Contact on WhatsApp for UV glass options.</span>
            <span style={{ textDecoration: 'underline', flexShrink: 0 }}>WhatsApp →</span>
          </a>
        )}

        {isManualGlass && (
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>Type Your Exact Phone Model *</label>
            <input
              className="input"
              placeholder="e.g. Motorola Edge 50 Pro, Google Pixel 9"
              value={glassManualModel}
              onChange={(e) => {
                setGlassManualModel(e.target.value);
                setGlassError('');
              }}
            />
          </div>
        )}

        {glassModel && availableGlassTypes.length === 0 && (
          <div
            style={{
              padding: 12,
              borderRadius: 10,
              background: '#FEF3C7',
              color: '#92400E',
              fontSize: 13,
              fontWeight: 700
            }}
          >
            This tempered glass option is currently unavailable in store catalog.
          </div>
        )}

        {availableGlassTypes.length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <div style={{ fontSize: 13, fontWeight: 800, color: 'var(--icase-navy)' }}>
              Select Tempered Glass Type
            </div>
            <div className="category-chips">
              {availableGlassTypes.map((gt) => {
                const active = glassType === gt;
                const gtPricing = resolveTemperedGlassPrice(
                  isManualGlass ? 'CUSTOM' : glassBrand,
                  isManualGlass ? glassManualModel.trim() || OTHER_MODEL_OPTION : glassModel,
                  gt,
                  config
                );
                return (
                  <button
                    key={gt}
                    type="button"
                    className={`chip ${active ? 'active' : ''}`}
                    onClick={() => setGlassType(gt)}
                    style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}
                  >
                    <span>
                      {active ? '✓ ' : ''}
                      {gt}
                    </span>
                    <span style={{ opacity: 0.88 }}>• {formatINR(gtPricing.finalPrice)}</span>
                    {gtPricing.offerBadge && (
                      <span
                        style={{
                          padding: '1px 6px',
                          borderRadius: 6,
                          background: '#FDE047',
                          color: '#0B1728',
                          fontSize: 10,
                          fontWeight: 900
                        }}
                      >
                        {gtPricing.offerBadge}
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
            {glassHelperNote && <div className="helper-note-bar">{glassHelperNote}</div>}
          </div>
        )}

        {glassPricing && (
          <div className="pricing-summary-box">
            <div>
              <div style={{ fontWeight: 800, color: 'var(--icase-navy)', fontSize: 14 }}>
                {glassType} Tempered Glass
              </div>
              <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                {isManualGlass
                  ? glassManualModel.trim() || 'Custom Model'
                  : `${glassBrand} ${glassModel}`}{' '}
                • {installationOption}
              </div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              {glassPricing.basePrice > glassPricing.finalPrice && (
                <span className="product-mrp">{formatINR(glassPricing.basePrice)}</span>
              )}
              <span style={{ fontSize: 20, fontWeight: 900, color: 'var(--icase-navy)' }}>
                {formatINR(glassPricing.finalPrice)}
              </span>
              {glassPricing.offerBadge && (
                <span className="discount-badge" style={{ position: 'static' }}>
                  {glassPricing.offerBadge}
                </span>
              )}
            </div>
          </div>
        )}

        {glassError && (
          <div style={{ color: 'var(--icase-red)', fontSize: 13, fontWeight: 700 }}>{glassError}</div>
        )}

        {glassModel && glassPricing && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
            <button
              type="button"
              className="btn btn-navy"
              style={{ flex: '1 1 220px', minHeight: 46 }}
              onClick={handleAddGlassToCart}
            >
              Add to Cart • {formatINR(glassPricing.finalPrice)}
            </button>
            <a
              href={buildWaUrl(
                buildTemperedGlassWhatsAppMessage({
                  brand: isManualGlass ? 'CUSTOM' : glassBrand,
                  model: isManualGlass ? glassManualModel.trim() || OTHER_MODEL_OPTION : glassModel,
                  glassType,
                  finalPrice: glassPricing.finalPrice,
                  installationOption
                })
              )}
              target="_blank"
              rel="noreferrer"
              className="btn"
              style={{
                flex: '1 1 220px',
                minHeight: 46,
                background: '#F0FDF4',
                color: '#15803D',
                border: '1.5px solid #16A34A',
                fontWeight: 800,
                textDecoration: 'none'
              }}
              onClick={(e) => {
                if (isManualGlass && !glassManualModel.trim()) {
                  e.preventDefault();
                  setGlassError('Please enter your exact device model name first.');
                }
              }}
            >
              <MessageCircle size={17} /> Ask / Order on WhatsApp
            </a>
          </div>
        )}
      </section>

      {/* 2. CAMERA LENS PROTECTION CARD */}
      {showCameraCard && (
        <section
          id="camera-lens-wrapper"
          ref={cameraSectionRef}
          className={`card ${initialFocus === 'camera' ? 'section-focus-ring' : ''}`}
          style={{ display: 'flex', flexDirection: 'column', gap: 16 }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 14, flexWrap: 'wrap' }}>
            <img
              src={cameraLensSampleImg}
              alt="Camera Lens Protector Sample"
              style={{
                width: 60,
                height: 60,
                borderRadius: 12,
                objectFit: 'cover',
                border: '1px solid var(--border)',
                flexShrink: 0
              }}
            />
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', marginBottom: 4 }}>
                <h2
                  style={{
                    color: 'var(--icase-navy)',
                    fontSize: 20,
                    fontWeight: 900,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 8
                  }}
                >
                  <Camera size={20} color="#E50914" /> Camera Lens Protection
                </h2>
                <span className="compat-pill limited">iPhone &amp; Samsung S Series Only</span>
                {cameraLensVideoUrl && onOpenVideo && (
                  <button
                    type="button"
                    className="media-video-pill"
                    onClick={() => onOpenVideo('Camera Lens Protection', cameraLensVideoUrl)}
                  >
                    <Play size={10} fill="currentColor" /> Watch Video
                  </button>
                )}
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: 13.5 }}>
                Available exclusively for iPhone &amp; Samsung S Series models (including S20–S25
                &amp; Ultra models). Not available for Redmi, Oppo, Realme, Vivo, OnePlus, Nothing,
                or Galaxy A/M/F models.
              </p>
            </div>
          </div>

          {selectedGlassDeviceUnsupportedForCamera && (
            <div className="compat-warning-box" role="alert">
              <AlertTriangle size={18} style={{ flexShrink: 0, marginTop: 2 }} />
              <div>
                <div>
                  Camera Lens Protector is not available for{' '}
                  <strong>
                    {isManualGlass ? selectedGlassDeviceName : `${glassBrand} ${selectedGlassDeviceName}`}
                  </strong>
                  .
                </div>
                <div style={{ fontSize: 12, fontWeight: 600, marginTop: 2 }}>
                  Camera Lens rings are currently supported only on iPhone and Samsung S Series
                  (S20–S25 &amp; Ultra) models.
                </div>
              </div>
            </div>
          )}

          <div className="category-chips">
            <button
              type="button"
              className={`chip ${!cameraBrand ? 'active' : ''}`}
              onClick={() => {
                setCameraBrand('');
                setCameraModel('');
                setCameraSearch('');
                setCameraDropdownOpen(true);
              }}
            >
              All Eligible
            </button>
            {activeCameraBrands.map((brand) => (
              <button
                key={brand}
                type="button"
                className={`chip ${cameraBrand === brand ? 'active' : ''}`}
                onClick={() => {
                  setCameraBrand(brand);
                  setCameraModel('');
                  setCameraSearch('');
                  setCameraDropdownOpen(true);
                }}
              >
                {brand}
              </button>
            ))}
          </div>

          <div className="searchable-dropdown" ref={cameraDropRef}>
            <label
              style={{
                display: 'block',
                fontSize: 11.5,
                fontWeight: 800,
                color: 'var(--text-secondary)',
                textTransform: 'uppercase',
                marginBottom: 6
              }}
            >
              Select iPhone or Samsung S Series Model
            </label>
            <div className="searchable-dropdown-box">
              <Search size={16} color="#64748B" />
              <input
                type="text"
                placeholder="Search iPhone 16 Pro, Galaxy S24 Ultra, S25..."
                value={
                  cameraDropdownOpen
                    ? cameraSearch
                    : cameraModel
                      ? `${cameraBrand} — ${cameraModel}`
                      : ''
                }
                onFocus={() => setCameraDropdownOpen(true)}
                onChange={(e) => {
                  setCameraSearch(e.target.value);
                  setCameraDropdownOpen(true);
                }}
              />
              {cameraModel && (
                <button
                  type="button"
                  className="section-link"
                  style={{ fontSize: 12 }}
                  onClick={() => {
                    setCameraModel('');
                    setCameraSearch('');
                    setCameraDropdownOpen(true);
                  }}
                >
                  Clear
                </button>
              )}
            </div>

            {cameraDropdownOpen && (
              <div className="searchable-dropdown-menu">
                {popularCameraModels.length > 0 && !cameraSearch.trim() && (
                  <div
                    style={{
                      padding: '10px 12px',
                      borderBottom: '1px solid #E2E8F0',
                      background: '#F8FAFC'
                    }}
                  >
                    <div
                      style={{
                        fontSize: 10.5,
                        fontWeight: 900,
                        textTransform: 'uppercase',
                        letterSpacing: '0.05em',
                        color: '#475569',
                        marginBottom: 6
                      }}
                    >
                      Popular Models (Quick Select)
                    </div>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                      {popularCameraModels.map((item) => {
                        const active = cameraBrand === item.brand && cameraModel === item.model;
                        return (
                          <button
                            key={`pop-cam-${item.brand}-${item.model}`}
                            type="button"
                            onClick={() => {
                              setCameraBrand(item.brand);
                              setCameraModel(item.model);
                              setCameraSearch('');
                              setCameraDropdownOpen(false);
                            }}
                            style={{
                              padding: '6px 11px',
                              borderRadius: 999,
                              fontSize: 12,
                              fontWeight: 700,
                              border: active ? '1px solid #0B1728' : '1px solid #CBD5E1',
                              background: active ? '#0B1728' : '#FFFFFF',
                              color: active ? '#FFFFFF' : '#0B1728',
                              cursor: 'pointer'
                            }}
                          >
                            {item.model}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                )}

                {groupedCamera.length === 0 ? (
                  <div style={{ padding: 14, fontSize: 13, color: 'var(--text-secondary)' }}>
                    Camera Lens Protection is available only for active iPhone and Samsung S Series
                    models.
                  </div>
                ) : (
                  groupedCamera.map((grp) => (
                    <div key={grp.brand}>
                      <div className="dropdown-group-label">{grp.brand}</div>
                      {grp.models.map((m) => {
                        const active = cameraBrand === grp.brand && cameraModel === m;
                        return (
                          <button
                            key={`${grp.brand}-${m}`}
                            type="button"
                            className={`dropdown-option-btn ${active ? 'selected' : ''}`}
                            onClick={() => {
                              setCameraBrand(grp.brand);
                              setCameraModel(m);
                              setCameraSearch('');
                              setCameraDropdownOpen(false);
                            }}
                          >
                            <span>{m}</span>
                            {active && <Check size={15} />}
                          </button>
                        );
                      })}
                    </div>
                  ))
                )}
              </div>
            )}
          </div>

          {cameraSearch.trim().length > 0 && groupedCamera.length === 0 && (
            <div className="compat-warning-box" role="alert">
              <AlertTriangle size={18} style={{ flexShrink: 0, marginTop: 2 }} />
              <div>
                Camera Lens Protector is not available for &ldquo;{cameraSearch.trim()}&rdquo;. Only
                iPhone and Samsung Galaxy S Series (S20–S25 / Ultra) models are supported.
              </div>
            </div>
          )}

          {cameraModel && cameraPricing && (
            <>
              <div className="pricing-summary-box">
                <div style={{ fontSize: 13.5, color: '#334155' }}>
                  9H Sapphire Tempered Camera Lens Ring Protection for{' '}
                  <strong>{cameraModel}</strong> • {installationOption}.
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  {cameraPricing.basePrice > cameraPricing.finalPrice && (
                    <span className="product-mrp">{formatINR(cameraPricing.basePrice)}</span>
                  )}
                  <span style={{ fontSize: 20, fontWeight: 900, color: 'var(--icase-navy)' }}>
                    {formatINR(cameraPricing.finalPrice)}
                  </span>
                  {cameraPricing.offerBadge && (
                    <span className="discount-badge" style={{ position: 'static' }}>
                      {cameraPricing.offerBadge}
                    </span>
                  )}
                </div>
              </div>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
                <button
                  type="button"
                  className="btn btn-navy"
                  style={{ flex: '1 1 220px', minHeight: 46 }}
                  onClick={handleAddCameraToCart}
                >
                  Add to Cart • {formatINR(cameraPricing.finalPrice)}
                </button>
                <a
                  href={buildWaUrl(
                    buildCameraLensWhatsAppMessage({
                      brand: cameraBrand,
                      model: cameraModel,
                      finalPrice: cameraPricing.finalPrice,
                      installationOption
                    })
                  )}
                  target="_blank"
                  rel="noreferrer"
                  className="btn"
                  style={{
                    flex: '1 1 220px',
                    minHeight: 46,
                    background: '#F0FDF4',
                    color: '#15803D',
                    border: '1.5px solid #16A34A',
                    fontWeight: 800,
                    textDecoration: 'none'
                  }}
                >
                  <MessageCircle size={17} /> Ask / Order on WhatsApp
                </a>
              </div>
            </>
          )}

          {comboPricing && comboGlassSelection && (
            <div className="combo-bundle-card">
              <div
                style={{
                  display: 'flex',
                  flexWrap: 'wrap',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  gap: 10
                }}
              >
                <div>
                  <div
                    style={{
                      fontSize: 11,
                      fontWeight: 900,
                      letterSpacing: '0.06em',
                      textTransform: 'uppercase',
                      color: 'var(--icase-blue)'
                    }}
                  >
                    Quick Bundle • Save with Combo
                  </div>
                  <div style={{ fontSize: 14, fontWeight: 800, color: 'var(--icase-navy)' }}>
                    {comboGlassSelection.model} ({comboGlassSelection.glassType}) + {cameraModel}{' '}
                    Lens
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  {comboPricing.basePrice > comboPricing.finalPrice && (
                    <span className="product-mrp">{formatINR(comboPricing.basePrice)}</span>
                  )}
                  <span style={{ fontSize: 20, fontWeight: 900, color: 'var(--icase-navy)' }}>
                    {formatINR(comboPricing.finalPrice)}
                  </span>
                  {comboPricing.offerBadge && (
                    <span className="discount-badge" style={{ position: 'static' }}>
                      {comboPricing.offerBadge}
                    </span>
                  )}
                </div>
              </div>
              <button
                type="button"
                className="btn btn-navy"
                style={{ width: '100%' }}
                onClick={handleAddComboToCart}
              >
                Add Tempered Glass + Camera Lens Combo • {formatINR(comboPricing.finalPrice)}
              </button>
            </div>
          )}
        </section>
      )}

      {/* 3. POUCHES CARD */}
      <section
        className={`card ${initialFocus === 'pouch' ? 'section-focus-ring' : ''}`}
        ref={pouchSectionRef}
        id="pouch-wrapper"
        style={{ display: 'flex', flexDirection: 'column', gap: 16 }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 14, flexWrap: 'wrap' }}>
          <img
            src={pouchHeaderImg}
            alt="Pouches & Back Cases"
            style={{
              width: 60,
              height: 60,
              borderRadius: 12,
              objectFit: 'cover',
              border: '1px solid var(--border)',
              flexShrink: 0
            }}
          />
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', marginBottom: 4 }}>
              <h2
                style={{
                  color: 'var(--icase-navy)',
                  fontSize: 20,
                  fontWeight: 900,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8
                }}
              >
                <ShoppingBag size={20} color="#0066CC" /> Pouches &amp; Back Cases
              </h2>
              <span className="compat-pill pouch">
                iPhone, Samsung S Series &amp; Other Android Models
              </span>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: 13.5 }}>
              Custom-fit protective pouches for iPhone &amp; Samsung S Series, plus instant WhatsApp
              enquiry for all Other Android models.
            </p>
          </div>
        </div>

        <div className="category-chips">
          {[...POUCH_BRANDS, OTHER_ANDROID_POUCHES_OPTION].map((brand) => (
            <button
              key={brand}
              type="button"
              className={`chip ${pouchBrand === brand ? 'active' : ''}`}
              onClick={() => {
                setPouchBrand(brand);
                setPouchModel('');
                setPouchSearch('');
                setPouchError('');
                setPouchDropdownOpen(false);
              }}
            >
              {brand}
            </button>
          ))}
        </div>

        {isOtherAndroidPouches ? (
          <div
            style={{
              background: '#F8FAFC',
              border: '1px solid var(--border)',
              borderRadius: 14,
              padding: 18,
              display: 'flex',
              flexDirection: 'column',
              gap: 12
            }}
          >
            <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--icase-navy)' }}>
              Enter or pick your Android phone model for instant WhatsApp pouch photos &amp; price:
            </div>

            <div>
              <div
                style={{
                  fontSize: 11,
                  fontWeight: 800,
                  color: 'var(--text-secondary)',
                  textTransform: 'uppercase',
                  marginBottom: 6
                }}
              >
                Popular Android Models (Quick Pick)
              </div>
              <div className="category-chips">
                {OTHER_ANDROID_POPULAR_MODELS.map((m) => (
                  <button
                    key={m}
                    type="button"
                    className={`chip ${otherAndroidModel === m ? 'active' : ''}`}
                    onClick={() => setOtherAndroidModel(m)}
                  >
                    {m}
                  </button>
                ))}
              </div>
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label>Your Android Phone Model *</label>
              <input
                className="input"
                placeholder="e.g. Redmi Note 13 Pro, OnePlus 12R, Vivo V40, Motorola Edge 50"
                value={otherAndroidModel}
                onChange={(e) => setOtherAndroidModel(e.target.value)}
              />
            </div>

            <div>
              <div
                style={{
                  fontSize: 11,
                  fontWeight: 800,
                  color: 'var(--text-secondary)',
                  textTransform: 'uppercase',
                  marginBottom: 6
                }}
              >
                Preferred Pouch Style
              </div>
              <div className="category-chips">
                {['Any Matching Design', ...allActivePouchDesigns.map((p) => p.name)].map((styleName) => (
                  <button
                    key={styleName}
                    type="button"
                    className={`chip ${otherAndroidPouchPref === styleName ? 'active' : ''}`}
                    onClick={() => setOtherAndroidPouchPref(styleName)}
                  >
                    {styleName}
                  </button>
                ))}
              </div>
            </div>

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
              <a
                href={buildWaUrl(
                  buildOtherAndroidPouchWhatsAppMessage({
                    modelName: otherAndroidModel,
                    pouchName: otherAndroidPouchPref
                  })
                )}
                target="_blank"
                rel="noreferrer"
                className="btn"
                style={{
                  background: '#16A34A',
                  color: '#FFFFFF',
                  fontWeight: 800,
                  textDecoration: 'none'
                }}
              >
                <MessageCircle size={16} /> Ask / Order on WhatsApp →
              </a>
            </div>
          </div>
        ) : (
          <>
            <div className="searchable-dropdown" ref={pouchDropRef}>
              <label
                style={{
                  display: 'block',
                  fontSize: 11.5,
                  fontWeight: 800,
                  color: 'var(--text-secondary)',
                  textTransform: 'uppercase',
                  marginBottom: 6
                }}
              >
                Select {pouchBrand} Model
              </label>
              <div className="searchable-dropdown-box">
                <Search size={16} color="#64748B" />
                <input
                  type="text"
                  placeholder={`Search ${pouchBrand} model...`}
                  value={pouchDropdownOpen ? pouchSearch : pouchModel || ''}
                  onFocus={() => setPouchDropdownOpen(true)}
                  onChange={(e) => {
                    setPouchSearch(e.target.value);
                    setPouchDropdownOpen(true);
                  }}
                />
                {pouchModel && (
                  <button
                    type="button"
                    className="section-link"
                    style={{ fontSize: 12 }}
                    onClick={() => {
                      setPouchModel('');
                      setPouchSearch('');
                      setPouchError('');
                      setPouchDropdownOpen(true);
                    }}
                  >
                    Clear
                  </button>
                )}
              </div>

              {pouchDropdownOpen && (
                <div className="searchable-dropdown-menu">
                  {pouchModels.length === 0 ? (
                    <div style={{ padding: 14, fontSize: 13, color: 'var(--text-secondary)' }}>
                      No active {pouchBrand} pouch models matched.
                    </div>
                  ) : (
                    pouchModels.map((m) => (
                      <button
                        key={m}
                        type="button"
                        className={`dropdown-option-btn ${pouchModel === m ? 'selected' : ''}`}
                        onClick={() => {
                          setPouchModel(m);
                          setPouchSearch('');
                          setPouchError('');
                          setPouchDropdownOpen(false);
                        }}
                      >
                        <span>{m}</span>
                        {pouchModel === m && <Check size={15} />}
                      </button>
                    ))
                  )}
                </div>
              )}
            </div>

            {pouchError && (
              <div style={{ color: 'var(--icase-red)', fontSize: 13, fontWeight: 700 }}>
                {pouchError}
              </div>
            )}

            {activePouches.length === 0 ? (
              <div
                style={{
                  padding: 14,
                  borderRadius: 12,
                  background: '#FEF3C7',
                  color: '#92400E',
                  fontSize: 13,
                  fontWeight: 700
                }}
              >
                No pouches are currently enabled for {pouchBrand} in store inventory.
              </div>
            ) : (
              <div className="pouch-grid">
                {activePouches.map((p) => {
                  const pr = resolvePouchPrice(p);
                  const colors = Array.isArray(p.colorOptions) ? p.colorOptions : [];
                  const activeColorOpt = resolvePouchSelectedColor(
                    p,
                    selectedPouchColors[p.id] || ''
                  );
                  const activeColorName = activeColorOpt?.name || '';
                  const activeImageUrl = resolvePouchImageUrl(p, activeColorName);
                  return (
                    <div key={p.id} className="pouch-card">
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        <div className="pouch-thumb-wrap">
                          <img src={activeImageUrl} alt={p.name} loading="lazy" />
                          {pr.offerBadge && (
                            <span
                              className="discount-badge"
                              style={{ left: 'auto', right: 10, top: 10 }}
                            >
                              {pr.offerBadge}
                            </span>
                          )}
                          {p.videoUrl && onOpenVideo && (
                            <button
                              type="button"
                              className="media-video-pill"
                              style={{ position: 'absolute', bottom: 8, left: 8 }}
                              onClick={(e) => {
                                e.stopPropagation();
                                onOpenVideo(p.name, p.videoUrl);
                              }}
                            >
                              <Play size={10} fill="currentColor" /> Video
                            </button>
                          )}
                        </div>
                        <div>
                          <div style={{ fontWeight: 800, color: 'var(--icase-navy)', fontSize: 15 }}>
                            {p.name}
                          </div>
                          {p.description && (
                            <p style={{ fontSize: 12.5, color: 'var(--text-secondary)', marginTop: 4 }}>
                              {p.description}
                            </p>
                          )}
                        </div>
                        {colors.length > 0 && (
                          <div>
                            <div
                              style={{
                                fontSize: 11.5,
                                fontWeight: 800,
                                color: 'var(--icase-navy)',
                                marginBottom: 6
                              }}
                            >
                              Colour:{' '}
                              <span style={{ fontWeight: 600, color: '#475569' }}>
                                {activeColorName}
                              </span>
                            </div>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                              {colors.map((c) => {
                                const isSel =
                                  activeColorName.toLowerCase() === c.name.toLowerCase();
                                return (
                                  <button
                                    key={c.name}
                                    type="button"
                                    className={`color-swatch-chip ${isSel ? 'selected' : ''}`}
                                    onClick={() =>
                                      setSelectedPouchColors((prev) => ({
                                        ...prev,
                                        [p.id]: c.name
                                      }))
                                    }
                                  >
                                    <span
                                      style={{
                                        width: 12,
                                        height: 12,
                                        borderRadius: '50%',
                                        background: c.hex || '#1E293B',
                                        border: '1px solid #94A3B8',
                                        display: 'inline-block'
                                      }}
                                    />
                                    <span>{c.name}</span>
                                  </button>
                                );
                              })}
                            </div>
                          </div>
                        )}
                      </div>

                      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        <div className="product-price-row" style={{ marginBottom: 0 }}>
                          <span className="product-price">{formatINR(pr.finalPrice)}</span>
                          {pr.basePrice > pr.finalPrice && (
                            <span className="product-mrp">{formatINR(pr.basePrice)}</span>
                          )}
                        </div>
                        <button
                          type="button"
                          className="btn btn-navy btn-sm"
                          style={{ width: '100%' }}
                          onClick={() => handleAddPouchToCart(p)}
                        >
                          {!pouchModel
                            ? 'Select Model & Add'
                            : activeColorName
                              ? `Add ${activeColorName} to Cart`
                              : 'Add to Cart'}
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </>
        )}
      </section>

      {/* 4. INSTALLATION & FULFILLMENT TOGGLE */}
      <section
        className="card"
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 14
        }}
      >
        <div>
          <div
            style={{
              fontSize: 11,
              fontWeight: 900,
              textTransform: 'uppercase',
              letterSpacing: '0.06em',
              color: 'var(--icase-blue)'
            }}
          >
            Installation &amp; Fulfillment
          </div>
          <div style={{ fontWeight: 800, color: 'var(--icase-navy)', fontSize: 14.5 }}>
            Choose Shop Installation vs Delivery / Pickup
          </div>
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
          {[
            { id: 'Shop Installation', sub: 'Free dust-free fitting at iCASE Edappal' },
            { id: 'Delivery / Pickup', sub: 'Doorstep delivery or counter pickup' }
          ].map((opt) => {
            const active = installationOption === opt.id;
            return (
              <button
                key={opt.id}
                type="button"
                onClick={() => setInstallationOption(opt.id)}
                style={{
                  padding: '10px 14px',
                  borderRadius: 12,
                  border: active ? '2px solid var(--icase-navy)' : '1.5px solid var(--border)',
                  background: active ? 'var(--icase-navy)' : '#F8FAFC',
                  color: active ? '#FFFFFF' : 'var(--icase-navy)',
                  textAlign: 'left',
                  cursor: 'pointer'
                }}
              >
                <div style={{ fontSize: 13, fontWeight: 800 }}>
                  {active ? '✓ ' : ''}
                  {opt.id}
                </div>
                <div style={{ fontSize: 11, color: active ? '#CBD5E1' : 'var(--text-secondary)' }}>
                  {opt.sub}
                </div>
              </button>
            );
          })}
        </div>
      </section>

      {/* 5. BOTTOM DEMO VIDEO BANNER */}
      <section className="video-banner-card">
        <div>
          <div
            style={{
              fontSize: 11.5,
              fontWeight: 800,
              color: '#FDE047',
              letterSpacing: '0.06em',
              textTransform: 'uppercase',
              marginBottom: 4
            }}
          >
            Product Quality Demo
          </div>
          <h3 style={{ fontSize: 20, fontWeight: 900, marginBottom: 4 }}>
            See Tempered Glass &amp; Camera Lens in Use
          </h3>
          <p style={{ fontSize: 13.5, color: '#CBD5E1' }}>
            Watch edge-to-edge clarity, oleophobic coating, and scratch resistance in action.
          </p>
        </div>
        <button
          type="button"
          className="btn btn-red"
          onClick={() => {
            setVideoError(false);
            setVideoModalOpen(true);
          }}
        >
          <Play size={16} /> Watch Demo Video
        </button>
      </section>

      {/* Lazy Video Modal */}
      {videoModalOpen && (
        <div className="overlay" onClick={() => setVideoModalOpen(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <strong>See Tempered Glass &amp; Camera Lens in Use</strong>
              <button
                type="button"
                className="btn btn-sm"
                style={{ background: 'rgba(255,255,255,0.14)', color: '#FFF' }}
                onClick={() => setVideoModalOpen(false)}
              >
                <X size={16} />
              </button>
            </div>
            <div className="modal-body">
              <video
                src={SAMPLE_GUARD_VIDEO_URL}
                controls
                autoPlay
                loop
                playsInline
                style={{ width: '100%', borderRadius: 12, background: '#0B1728' }}
                onError={() => setVideoError(true)}
              />
              {videoError && (
                <p style={{ color: 'var(--icase-red)', fontSize: 13, fontWeight: 700, marginTop: 10 }}>
                  Unable to play video preview right now. Please check your internet connection.
                </p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
