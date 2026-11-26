import React, { useEffect, useMemo, useState } from 'react';
import {
  Plus,
  Trash2,
  Upload,
  Camera,
  X,
  Image as ImageIcon,
  Code2,
  RotateCcw,
  Play,
  Film
} from 'lucide-react';
import {
  DEFAULT_GLASS_TYPE_PRICES,
  POUCH_BRANDS,
  POUCH_PRESET_COLORS,
  SCREEN_GUARD_BRANDS,
  compressImageFileToDataUrl,
  createDefaultScreenGuardConfig,
  formatINR,
  getAllGlassTypes,
  getCameraLensModelsForBrand,
  getGlassTypeHelperNote,
  getGlassTypesForDevice,
  getModelsForBrand,
  getPouchModelsForBrand,
  getStartingPouchPrice,
  guardModelKey,
  normalizeHexColor,
  parseScreenGuardConfig,
  readShortVideoFileToDataUrl,
  resolveCameraLensImageUrl,
  resolveCameraLensPrice,
  resolveGlassTypeImageUrl,
  resolveGlassTypeVideoUrl,
  resolvePouchCardImageUrl,
  resolvePouchImageUrl,
  resolvePouchPrice,
  resolveTemperedGlassImageUrl,
  resolveTemperedGlassPrice
} from '../data/constants';
import MediaVideoModal from './MediaVideoModal';

export default function AdminGlassOffersTab({ screenGuardConfig, onSaveGuardConfig, onOpenExportCode }) {
  const cfg = screenGuardConfig || createDefaultScreenGuardConfig();
  const [toast, setToast] = useState('');
  const [glassImgInput, setGlassImgInput] = useState(cfg.temperedGlassImageUrl || '');
  const [camImgInput, setCamImgInput] = useState(cfg.cameraLensImageUrl || '');
  const [pouchCardImgInput, setPouchCardImgInput] = useState(cfg.pouchCardImageUrl || '');
  const [pouchStartPriceInput, setPouchStartPriceInput] = useState(
    String(cfg.pouchCardStartingPrice || '')
  );
  const [adminVideoPreview, setAdminVideoPreview] = useState({ open: false, title: '', url: '' });

  useEffect(() => {
    setGlassImgInput(cfg.temperedGlassImageUrl || '');
    setCamImgInput(cfg.cameraLensImageUrl || '');
    setPouchCardImgInput(cfg.pouchCardImageUrl || '');
    setPouchStartPriceInput(cfg.pouchCardStartingPrice ? String(cfg.pouchCardStartingPrice) : '');
  }, [
    cfg.temperedGlassImageUrl,
    cfg.cameraLensImageUrl,
    cfg.pouchCardImageUrl,
    cfg.pouchCardStartingPrice
  ]);

  // Glass types add
  const [addingGlassType, setAddingGlassType] = useState(false);
  const [newGtName, setNewGtName] = useState('');
  const [newGtPrice, setNewGtPrice] = useState('299');
  const [newGtNote, setNewGtNote] = useState('');
  const [newGtImageUrl, setNewGtImageUrl] = useState('');
  const [newGtVideoUrl, setNewGtVideoUrl] = useState('');

  // Glass models
  const [glassModelBrand, setGlassModelBrand] = useState('iPhone');
  const [glassModelSearch, setGlassModelSearch] = useState('');
  const [addingGlassModel, setAddingGlassModel] = useState(false);
  const [newGlassModelName, setNewGlassModelName] = useState('');
  const [editingGlassModelKey, setEditingGlassModelKey] = useState(null);

  // Camera lens models
  const [camBrand, setCamBrand] = useState('iPhone');
  const [camSearch, setCamSearch] = useState('');
  const [addingCamModel, setAddingCamModel] = useState(false);
  const [newCamModelName, setNewCamModelName] = useState('');
  const [editingCamModelKey, setEditingCamModelKey] = useState(null);

  // Pouches
  const [pouchDraft, setPouchDraft] = useState(null);
  const [customColorName, setCustomColorName] = useState('');
  const [customColorHex, setCustomColorHex] = useState('#1E293B');
  const [expandedColorName, setExpandedColorName] = useState('');
  const [pouchModelBrand, setPouchModelBrand] = useState('iPhone');
  const [pouchModelSearch, setPouchModelSearch] = useState('');
  const [addingPouchModel, setAddingPouchModel] = useState(false);
  const [newPouchModelName, setNewPouchModelName] = useState('');

  async function commitConfig(nextConfig, msg = 'Tempered Glass, Pouches & Offers synced live!') {
    await onSaveGuardConfig(parseScreenGuardConfig(nextConfig));
    setToast(msg);
    setTimeout(() => setToast(''), 3500);
  }

  const glassTypesList = useMemo(() => getAllGlassTypes(cfg), [cfg]);

  const filteredGlassModels = useMemo(() => {
    const q = glassModelSearch.trim().toLowerCase();
    return getModelsForBrand(glassModelBrand, cfg)
      .filter((m) => !q || m.toLowerCase().includes(q))
      .slice(0, 60);
  }, [cfg, glassModelBrand, glassModelSearch]);

  const filteredCamModels = useMemo(() => {
    const q = camSearch.trim().toLowerCase();
    return getCameraLensModelsForBrand(camBrand, cfg)
      .filter((m) => !q || m.toLowerCase().includes(q))
      .slice(0, 60);
  }, [cfg, camBrand, camSearch]);

  const filteredPouchModels = useMemo(() => {
    const q = pouchModelSearch.trim().toLowerCase();
    return getPouchModelsForBrand(pouchModelBrand, cfg)
      .filter((m) => !q || m.toLowerCase().includes(q))
      .slice(0, 60);
  }, [cfg, pouchModelBrand, pouchModelSearch]);

  const camDefaultPreview = resolveCameraLensPrice('iPhone', '__default__', {
    ...cfg,
    cameraLensModelConfigs: {}
  });

  // ---- Handlers for Glass Types ----
  async function handleAddGlassType() {
    const clean = newGtName.trim();
    if (!clean) return;
    const priceNum = Number(newGtPrice || 299);
    const next = parseScreenGuardConfig(cfg);
    next.glassTypeConfigs[clean] = {
      basePrice: Number.isFinite(priceNum) && priceNum >= 0 ? priceNum : 299,
      enabled: true,
      deleted: false,
      offer: { active: false, type: 'NONE', value: 0, badgeText: '' },
      helperNote: newGtNote.trim(),
      imageUrl: newGtImageUrl.trim(),
      videoUrl: newGtVideoUrl.trim()
    };
    setAddingGlassType(false);
    setNewGtName('');
    setNewGtNote('');
    setNewGtImageUrl('');
    setNewGtVideoUrl('');
    await commitConfig(next, `Added glass type "${clean}"`);
  }

  // ---- Handlers for Glass Models ----
  async function handleAddGlassModel() {
    const clean = newGlassModelName.trim();
    if (!clean) return;
    const next = parseScreenGuardConfig(cfg);
    const existing = next.customModelsByBrand[glassModelBrand] || [];
    if (!existing.includes(clean)) {
      next.customModelsByBrand[glassModelBrand] = [...existing, clean];
    }
    const key = guardModelKey(glassModelBrand, clean);
    if (next.modelConfigs[key]) {
      next.modelConfigs[key].deleted = false;
      next.modelConfigs[key].enabled = true;
    }
    setAddingGlassModel(false);
    setNewGlassModelName('');
    await commitConfig(next, `Added ${clean}`);
  }

  // ---- Handlers for Camera Lens Models ----
  async function handleAddCamModel() {
    const clean = newCamModelName.trim();
    if (!clean) return;
    const next = parseScreenGuardConfig(cfg);
    const existing = next.customCameraLensModelsByBrand[camBrand] || [];
    if (!existing.includes(clean)) {
      next.customCameraLensModelsByBrand[camBrand] = [...existing, clean];
    }
    const key = guardModelKey(camBrand, clean);
    if (next.cameraLensModelConfigs[key]) {
      next.cameraLensModelConfigs[key].deleted = false;
      next.cameraLensModelConfigs[key].enabled = true;
    }
    setAddingCamModel(false);
    setNewCamModelName('');
    await commitConfig(next, `Added ${clean} lens model`);
  }

  // ---- Handlers for Pouches ----
  function openNewPouchModal() {
    setExpandedColorName('');
    setCustomColorName('');
    setCustomColorHex('#1E293B');
    setPouchDraft({
      isNew: true,
      id: `pouch_${Date.now()}`,
      name: '',
      description: '',
      basePrice: '349',
      compatibleBrands: ['iPhone', 'Samsung S Series'],
      imageUrl: '',
      videoUrl: '',
      colorOptions: [],
      showOnHomePage: false,
      offerType: 'NONE',
      offerValue: '0',
      badgeText: ''
    });
  }

  function openEditPouchModal(p) {
    setExpandedColorName('');
    setCustomColorName('');
    setCustomColorHex('#1E293B');
    setPouchDraft({
      isNew: false,
      id: p.id,
      name: p.name || '',
      description: p.description || '',
      basePrice: String(p.basePrice || 299),
      compatibleBrands: Array.isArray(p.compatibleBrands) ? [...p.compatibleBrands] : ['iPhone', 'Samsung S Series'],
      imageUrl: p.imageUrl || '',
      videoUrl: p.videoUrl || '',
      colorOptions: Array.isArray(p.colorOptions) ? p.colorOptions.map((c) => ({ ...c })) : [],
      showOnHomePage: Boolean(p.showOnHomePage),
      offerType: p.offer?.type || 'NONE',
      offerValue: String(p.offer?.value || 0),
      badgeText: p.offer?.badgeText || ''
    });
  }

  async function handlePouchMainImageUpload(file) {
    if (!file) return;
    try {
      const dataUrl = await compressImageFileToDataUrl(file);
      setPouchDraft((prev) => (prev ? { ...prev, imageUrl: dataUrl } : prev));
    } catch (err) {
      setToast(err.message || 'Image upload failed');
    }
  }

  async function handlePouchColorImageUpload(colorIdx, file) {
    if (!file) return;
    try {
      const dataUrl = await compressImageFileToDataUrl(file);
      setPouchDraft((prev) => {
        if (!prev) return prev;
        const nextColors = prev.colorOptions.map((c, i) =>
          i === colorIdx ? { ...c, imageUrl: dataUrl } : c
        );
        return { ...prev, colorOptions: nextColors };
      });
    } catch (err) {
      setToast(err.message || 'Colour image upload failed');
    }
  }

  function togglePresetPouchColor(pc) {
    setPouchDraft((prev) => {
      if (!prev) return prev;
      const exists = prev.colorOptions.some(
        (c) => c.name.toLowerCase() === pc.name.toLowerCase()
      );
      const nextColors = exists
        ? prev.colorOptions.filter((c) => c.name.toLowerCase() !== pc.name.toLowerCase())
        : [...prev.colorOptions, { name: pc.name, hex: pc.hex, imageUrl: '' }];
      return { ...prev, colorOptions: nextColors };
    });
  }

  function addCustomPouchColor() {
    const name = customColorName.trim();
    if (!name) return;
    const hex = normalizeHexColor(customColorHex);
    setPouchDraft((prev) => {
      if (!prev) return prev;
      const exists = prev.colorOptions.some((c) => c.name.toLowerCase() === name.toLowerCase());
      const nextColors = exists
        ? prev.colorOptions.map((c) =>
            c.name.toLowerCase() === name.toLowerCase() ? { ...c, name, hex } : c
          )
        : [...prev.colorOptions, { name, hex, imageUrl: '' }];
      return { ...prev, colorOptions: nextColors };
    });
    setCustomColorName('');
  }

  async function handleSavePouch(e) {
    e.preventDefault();
    if (!pouchDraft || !pouchDraft.name.trim()) return;
    const next = parseScreenGuardConfig(cfg);
    const compat =
      pouchDraft.compatibleBrands.length > 0
        ? pouchDraft.compatibleBrands
        : ['iPhone', 'Samsung S Series'];
    const pouchObj = {
      id: pouchDraft.id,
      name: pouchDraft.name.trim(),
      description: pouchDraft.description.trim(),
      basePrice: Math.max(0, Number(pouchDraft.basePrice || 299)),
      compatibleBrands: compat,
      enabled: true,
      deleted: false,
      showOnHomePage: Boolean(pouchDraft.showOnHomePage),
      imageUrl: String(pouchDraft.imageUrl || '').trim(),
      videoUrl: String(pouchDraft.videoUrl || '').trim(),
      colorOptions: pouchDraft.colorOptions,
      offer: {
        active: pouchDraft.offerType !== 'NONE',
        type: pouchDraft.offerType,
        value: Number(pouchDraft.offerValue || 0),
        badgeText: pouchDraft.badgeText.trim()
      }
    };
    const existingIdx = next.pouchItems.findIndex((x) => x.id === pouchObj.id);
    if (existingIdx >= 0) {
      next.pouchItems[existingIdx] = pouchObj;
    } else {
      next.pouchItems.push(pouchObj);
    }
    setPouchDraft(null);
    await commitConfig(next, `Pouch "${pouchObj.name}" saved & synced!`);
  }

  async function handleAddPouchModel() {
    const clean = newPouchModelName.trim();
    if (!clean) return;
    const next = parseScreenGuardConfig(cfg);
    const existing = next.customPouchModelsByBrand[pouchModelBrand] || [];
    if (!existing.includes(clean)) {
      next.customPouchModelsByBrand[pouchModelBrand] = [...existing, clean];
    }
    const key = guardModelKey(pouchModelBrand, clean);
    if (next.pouchModelConfigs[key]) {
      next.pouchModelConfigs[key].deleted = false;
      next.pouchModelConfigs[key].enabled = true;
    }
    setAddingPouchModel(false);
    setNewPouchModelName('');
    await commitConfig(next, `Added ${clean} pouch model`);
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      {toast && (
        <div
          style={{
            padding: '10px 16px',
            borderRadius: 12,
            background: '#E8FDF2',
            color: '#065F46',
            fontWeight: 800,
            fontSize: 13.5
          }}
        >
          ✓ {toast}
        </div>
      )}

      {/* 0. SAMPLE CARD & ITEM IMAGES + GITHUB CODE EXPORT */}
      <section className="card">
        <div className="section-header" style={{ marginBottom: 12 }}>
          <div>
            <h3 style={{ color: 'var(--icase-navy)', fontSize: 17, fontWeight: 900 }}>
              Sample Card &amp; Item Images (Home &amp; Finder)
            </h3>
            <p style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
              Upload or paste custom photos for the Home Sample Cards, Finder headers, and Cart items.
            </p>
          </div>
          {onOpenExportCode && (
            <button type="button" className="btn btn-navy btn-sm" onClick={onOpenExportCode}>
              <Code2 size={15} /> Export Default Code for GitHub
            </button>
          )}
        </div>

        <div className="form-grid">
          <div
            style={{
              background: '#F8FAFC',
              border: '1px solid var(--border)',
              borderRadius: 14,
              padding: 14,
              display: 'flex',
              flexDirection: 'column',
              gap: 10
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <img
                src={resolveTemperedGlassImageUrl(cfg)}
                alt="Tempered Glass Sample"
                style={{
                  width: 68,
                  height: 68,
                  borderRadius: 12,
                  objectFit: 'cover',
                  border: '1px solid var(--border)',
                  background: '#FFF'
                }}
              />
              <div>
                <div style={{ fontWeight: 900, color: 'var(--icase-navy)', fontSize: 14.5 }}>
                  Tempered Glass Sample Image
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                  Shown on Home Card &amp; Tempered Glass orders
                </div>
              </div>
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
              <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer' }}>
                <Upload size={14} /> Upload Image
                <input
                  type="file"
                  accept="image/*"
                  hidden
                  onChange={async (e) => {
                    const f = e.target.files?.[0];
                    e.target.value = '';
                    if (!f) return;
                    try {
                      const dataUrl = await compressImageFileToDataUrl(f);
                      setGlassImgInput(dataUrl);
                      const next = parseScreenGuardConfig(cfg);
                      next.temperedGlassImageUrl = dataUrl;
                      await commitConfig(next, 'Tempered Glass sample image updated!');
                    } catch (err) {
                      setToast(err.message || 'Image upload failed');
                    }
                  }}
                />
              </label>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                onClick={async () => {
                  setGlassImgInput('');
                  const next = parseScreenGuardConfig(cfg);
                  next.temperedGlassImageUrl = '';
                  await commitConfig(next, 'Tempered Glass image reset to default');
                }}
              >
                <RotateCcw size={13} /> Reset
              </button>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <input
                className="input"
                placeholder="Or paste Tempered Glass image URL..."
                value={glassImgInput}
                onChange={(e) => setGlassImgInput(e.target.value)}
              />
              <button
                type="button"
                className="btn btn-navy btn-sm"
                onClick={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.temperedGlassImageUrl = glassImgInput.trim();
                  await commitConfig(next, 'Tempered Glass image URL saved!');
                }}
              >
                Save
              </button>
            </div>
          </div>

          <div
            style={{
              background: '#F8FAFC',
              border: '1px solid var(--border)',
              borderRadius: 14,
              padding: 14,
              display: 'flex',
              flexDirection: 'column',
              gap: 10
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <img
                src={resolveCameraLensImageUrl(cfg)}
                alt="Camera Lens Sample"
                style={{
                  width: 68,
                  height: 68,
                  borderRadius: 12,
                  objectFit: 'cover',
                  border: '1px solid var(--border)',
                  background: '#FFF'
                }}
              />
              <div>
                <div style={{ fontWeight: 900, color: 'var(--icase-navy)', fontSize: 14.5 }}>
                  Camera Lens Protector Image
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                  Shown on Home Card &amp; Camera Lens orders
                </div>
              </div>
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
              <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer' }}>
                <Upload size={14} /> Upload Image
                <input
                  type="file"
                  accept="image/*"
                  hidden
                  onChange={async (e) => {
                    const f = e.target.files?.[0];
                    e.target.value = '';
                    if (!f) return;
                    try {
                      const dataUrl = await compressImageFileToDataUrl(f);
                      setCamImgInput(dataUrl);
                      const next = parseScreenGuardConfig(cfg);
                      next.cameraLensImageUrl = dataUrl;
                      await commitConfig(next, 'Camera Lens sample image updated!');
                    } catch (err) {
                      setToast(err.message || 'Image upload failed');
                    }
                  }}
                />
              </label>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                onClick={async () => {
                  setCamImgInput('');
                  const next = parseScreenGuardConfig(cfg);
                  next.cameraLensImageUrl = '';
                  await commitConfig(next, 'Camera Lens image reset to default');
                }}
              >
                <RotateCcw size={13} /> Reset
              </button>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <input
                className="input"
                placeholder="Or paste Camera Lens image URL..."
                value={camImgInput}
                onChange={(e) => setCamImgInput(e.target.value)}
              />
              <button
                type="button"
                className="btn btn-navy btn-sm"
                onClick={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.cameraLensImageUrl = camImgInput.trim();
                  await commitConfig(next, 'Camera Lens image URL saved!');
                }}
              >
                Save
              </button>
            </div>
          </div>

          <div
            style={{
              background: '#F8FAFC',
              border: '1px solid var(--border)',
              borderRadius: 14,
              padding: 14,
              display: 'flex',
              flexDirection: 'column',
              gap: 10
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <img
                src={resolvePouchCardImageUrl(cfg)}
                alt="Pouches & Back Cases Card"
                style={{
                  width: 68,
                  height: 68,
                  borderRadius: 12,
                  objectFit: 'cover',
                  border: '1px solid var(--border)',
                  background: '#FFF'
                }}
              />
              <div>
                <div style={{ fontWeight: 900, color: 'var(--icase-navy)', fontSize: 14.5 }}>
                  Pouches &amp; Back Cases Card
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                  Home Card image &amp; starting price (Auto:{' '}
                  {formatINR(getStartingPouchPrice({ ...cfg, pouchCardStartingPrice: 0 }))})
                </div>
              </div>
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
              <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer' }}>
                <Upload size={14} /> Upload Image
                <input
                  type="file"
                  accept="image/*"
                  hidden
                  onChange={async (e) => {
                    const f = e.target.files?.[0];
                    e.target.value = '';
                    if (!f) return;
                    try {
                      const dataUrl = await compressImageFileToDataUrl(f);
                      setPouchCardImgInput(dataUrl);
                      const next = parseScreenGuardConfig(cfg);
                      next.pouchCardImageUrl = dataUrl;
                      await commitConfig(next, 'Pouches card image updated!');
                    } catch (err) {
                      setToast(err.message || 'Image upload failed');
                    }
                  }}
                />
              </label>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                onClick={async () => {
                  setPouchCardImgInput('');
                  const next = parseScreenGuardConfig(cfg);
                  next.pouchCardImageUrl = '';
                  await commitConfig(next, 'Pouches card image reset to default');
                }}
              >
                <RotateCcw size={13} /> Reset
              </button>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <input
                className="input"
                placeholder="Or paste Pouches card image URL..."
                value={pouchCardImgInput}
                onChange={(e) => setPouchCardImgInput(e.target.value)}
              />
              <button
                type="button"
                className="btn btn-navy btn-sm"
                onClick={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.pouchCardImageUrl = pouchCardImgInput.trim();
                  await commitConfig(next, 'Pouches card image URL saved!');
                }}
              >
                Save
              </button>
            </div>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <input
                type="number"
                className="input"
                placeholder="Starting price override (₹, 0 = auto)"
                value={pouchStartPriceInput}
                onChange={(e) => setPouchStartPriceInput(e.target.value)}
              />
              <button
                type="button"
                className="btn btn-navy btn-sm"
                onClick={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  const val = Math.max(0, Math.round(Number(pouchStartPriceInput || 0)));
                  next.pouchCardStartingPrice = val;
                  await commitConfig(
                    next,
                    val > 0
                      ? `Pouches card starting price set to ₹${val}`
                      : 'Pouches card starting price set to auto'
                  );
                }}
              >
                Save Price
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* 1. TEMPERED GLASS TYPES */}
      <section className="card">
        <div className="section-header" style={{ marginBottom: 12 }}>
          <div>
            <h3 style={{ color: 'var(--icase-navy)', fontSize: 17, fontWeight: 900 }}>
              Tempered Glass Types — Add, Rates, Offers, Media &amp; Delete
            </h3>
            <p style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
              Changes immediately update Home quick-select cards, Finder choices, and Cart totals.
            </p>
          </div>
          <button
            type="button"
            className="btn btn-red btn-sm"
            onClick={() => setAddingGlassType((v) => !v)}
          >
            <Plus size={15} /> Add Glass Type
          </button>
        </div>

        {addingGlassType && (
          <div
            style={{
              padding: 14,
              borderRadius: 12,
              background: '#EAF2FF',
              border: '1px solid rgba(0,102,204,0.3)',
              marginBottom: 14,
              display: 'flex',
              flexDirection: 'column',
              gap: 10
            }}
          >
            <div className="form-grid">
              <input
                className="input"
                placeholder="Type name (e.g. Anti-Blue 9H)"
                value={newGtName}
                onChange={(e) => setNewGtName(e.target.value)}
              />
              <input
                type="number"
                className="input"
                placeholder="Rate INR"
                value={newGtPrice}
                onChange={(e) => setNewGtPrice(e.target.value)}
              />
            </div>
            <input
              className="input"
              placeholder="1-line helper note (optional)"
              value={newGtNote}
              onChange={(e) => setNewGtNote(e.target.value)}
            />
            <div className="form-grid">
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  className="input"
                  placeholder="Optional Type Image URL"
                  value={newGtImageUrl.startsWith('data:image') ? '' : newGtImageUrl}
                  onChange={(e) => setNewGtImageUrl(e.target.value)}
                />
                <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer', flexShrink: 0 }}>
                  <Upload size={13} /> Img
                  <input
                    type="file"
                    accept="image/*"
                    hidden
                    onChange={async (e) => {
                      const f = e.target.files?.[0];
                      e.target.value = '';
                      if (!f) return;
                      try {
                        setNewGtImageUrl(await compressImageFileToDataUrl(f));
                      } catch (err) {
                        setToast(err.message || 'Image upload failed');
                      }
                    }}
                  />
                </label>
              </div>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  className="input"
                  placeholder="Optional Video URL / YouTube"
                  value={newGtVideoUrl.startsWith('data:video') ? '' : newGtVideoUrl}
                  onChange={(e) => setNewGtVideoUrl(e.target.value)}
                />
                <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer', flexShrink: 0 }}>
                  <Film size={13} /> Clip
                  <input
                    type="file"
                    accept="video/*"
                    hidden
                    onChange={async (e) => {
                      const f = e.target.files?.[0];
                      e.target.value = '';
                      if (!f) return;
                      try {
                        setNewGtVideoUrl(await readShortVideoFileToDataUrl(f));
                      } catch (err) {
                        setToast(err.message || 'Video upload failed');
                      }
                    }}
                  />
                </label>
              </div>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <button type="button" className="btn btn-navy btn-sm" onClick={handleAddGlassType}>
                Save Glass Type
              </button>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                onClick={() => setAddingGlassType(false)}
              >
                Cancel
              </button>
            </div>
          </div>
        )}

        <div className="pouch-grid">
          {glassTypesList.map((gt) => {
            const gCfg = cfg.glassTypeConfigs[gt] || {
              basePrice: DEFAULT_GLASS_TYPE_PRICES[gt] || 199,
              enabled: true,
              deleted: false,
              offer: { active: false, type: 'NONE', value: 0, badgeText: '' },
              helperNote: '',
              imageUrl: '',
              videoUrl: ''
            };
            return (
              <GlassTypeEditorCard
                key={gt}
                glassType={gt}
                gCfg={gCfg}
                resolvedThumb={resolveGlassTypeImageUrl(gt, cfg)}
                helperFallback={getGlassTypeHelperNote(gt, cfg)}
                preview={resolveTemperedGlassPrice('iPhone', '__default__', gt, {
                  ...cfg,
                  modelConfigs: {}
                })}
                onPreviewVideo={(title, url) =>
                  setAdminVideoPreview({ open: true, title, url })
                }
                onToast={(m) => setToast(m)}
                onToggleActive={async (enabled) => {
                  const next = parseScreenGuardConfig(cfg);
                  next.glassTypeConfigs[gt] = { ...gCfg, enabled };
                  await commitConfig(next);
                }}
                onDelete={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.glassTypeConfigs[gt] = { ...gCfg, deleted: true, enabled: false };
                  await commitConfig(next, `Deleted ${gt} permanently`);
                }}
                onRestore={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.glassTypeConfigs[gt] = { ...gCfg, deleted: false, enabled: true };
                  await commitConfig(next, `Restored ${gt}`);
                }}
                onSave={async (updated) => {
                  const next = parseScreenGuardConfig(cfg);
                  next.glassTypeConfigs[gt] = { ...gCfg, ...updated };
                  await commitConfig(next, `Updated ${gt}`);
                }}
              />
            );
          })}
        </div>
      </section>

      {/* 2. TEMPERED GLASS PHONE MODELS */}
      <section className="card">
        <div className="section-header" style={{ marginBottom: 12 }}>
          <div>
            <h3 style={{ color: 'var(--icase-navy)', fontSize: 17, fontWeight: 900 }}>
              Phone Models (Tempered Glass) — Add, Rename, Rates &amp; Delete
            </h3>
            <p style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
              Add custom models, override per-glass-type prices, pause, or delete permanently.
            </p>
          </div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <button
              type="button"
              className="btn btn-red btn-sm"
              onClick={() => setAddingGlassModel((v) => !v)}
            >
              <Plus size={15} /> Add Model
            </button>
            <input
              className="input"
              style={{ width: 200, padding: '6px 12px' }}
              placeholder="Filter models..."
              value={glassModelSearch}
              onChange={(e) => setGlassModelSearch(e.target.value)}
            />
          </div>
        </div>

        <div className="category-chips" style={{ marginBottom: 12 }}>
          {SCREEN_GUARD_BRANDS.map((b) => (
            <button
              key={b}
              type="button"
              className={`chip ${glassModelBrand === b ? 'active' : ''}`}
              onClick={() => {
                setGlassModelBrand(b);
                setEditingGlassModelKey(null);
              }}
            >
              {b}
            </button>
          ))}
        </div>

        {addingGlassModel && (
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              gap: 8,
              padding: 12,
              borderRadius: 12,
              background: '#EAF2FF',
              marginBottom: 12
            }}
          >
            <input
              className="input"
              style={{ flex: 1, minWidth: 200 }}
              placeholder={`New ${glassModelBrand} model name`}
              value={newGlassModelName}
              onChange={(e) => setNewGlassModelName(e.target.value)}
            />
            <button type="button" className="btn btn-navy btn-sm" onClick={handleAddGlassModel}>
              Add Model
            </button>
            <button
              type="button"
              className="btn btn-outline btn-sm"
              onClick={() => setAddingGlassModel(false)}
            >
              Cancel
            </button>
          </div>
        )}

        <div style={{ maxHeight: 360, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8 }}>
          {filteredGlassModels.map((model) => {
            const mKey = guardModelKey(glassModelBrand, model);
            const mCfg = cfg.modelConfigs[mKey] || {
              enabled: true,
              deleted: false,
              customRates: {},
              offer: { active: false, type: 'NONE', value: 0, badgeText: '' }
            };
            const isEditing = editingGlassModelKey === mKey;
            const applicableTypes = getGlassTypesForDevice(
              glassModelBrand,
              model,
              createDefaultScreenGuardConfig()
            );

            return (
              <GlassModelEditorRow
                key={mKey}
                brand={glassModelBrand}
                model={model}
                mCfg={mCfg}
                isEditing={isEditing}
                applicableTypes={applicableTypes}
                onToggleEdit={() => setEditingGlassModelKey(isEditing ? null : mKey)}
                onToggleActive={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.modelConfigs[mKey] = { ...mCfg, enabled: !mCfg.enabled };
                  await commitConfig(next);
                }}
                onDelete={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.modelConfigs[mKey] = { ...mCfg, deleted: true, enabled: false };
                  await commitConfig(next, `Deleted ${model}`);
                }}
                onRestore={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.modelConfigs[mKey] = { ...mCfg, deleted: false, enabled: true };
                  await commitConfig(next, `Restored ${model}`);
                }}
                onSave={async (newName, customRates, offer) => {
                  const next = parseScreenGuardConfig(cfg);
                  if (newName && newName !== model) {
                    const customArr = next.customModelsByBrand[glassModelBrand] || [];
                    if (!customArr.includes(newName)) {
                      next.customModelsByBrand[glassModelBrand] = [...customArr, newName];
                    }
                    next.modelConfigs[mKey] = { ...mCfg, deleted: true, enabled: false };
                    next.modelConfigs[guardModelKey(glassModelBrand, newName)] = {
                      enabled: true,
                      deleted: false,
                      customRates,
                      offer
                    };
                  } else {
                    next.modelConfigs[mKey] = {
                      ...mCfg,
                      enabled: mCfg.enabled !== false,
                      deleted: false,
                      customRates,
                      offer
                    };
                  }
                  setEditingGlassModelKey(null);
                  await commitConfig(next, `Saved settings for ${newName || model}`);
                }}
              />
            );
          })}
        </div>
      </section>

      {/* 3. CAMERA LENS PROTECTION + QUICK COMBO */}
      <section className="card">
        <div className="section-header" style={{ marginBottom: 12 }}>
          <div>
            <h3 style={{ color: 'var(--icase-navy)', fontSize: 17, fontWeight: 900 }}>
              Camera Lens Protection &amp; Quick Combo Offer
            </h3>
            <p style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
              Live Lens Price: <strong>{formatINR(camDefaultPreview.finalPrice)}</strong>
              {camDefaultPreview.offerBadge ? ` (${camDefaultPreview.offerBadge})` : ''}
            </p>
          </div>
          <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 800, fontSize: 13 }}>
            <input
              type="checkbox"
              checked={cfg.cameraLensDefault.enabled && !cfg.cameraLensDefault.deleted}
              onChange={async (e) => {
                const next = parseScreenGuardConfig(cfg);
                next.cameraLensDefault.enabled = e.target.checked;
                next.cameraLensDefault.deleted = !e.target.checked;
                await commitConfig(next);
              }}
            />
            Section Active
          </label>
        </div>

        <CameraLensDefaultsEditor
          camDefault={cfg.cameraLensDefault}
          comboEnabled={cfg.comboEnabled !== false}
          comboOffer={cfg.comboOffer}
          onPreviewVideo={(title, url) => setAdminVideoPreview({ open: true, title, url })}
          onToast={(m) => setToast(m)}
          onSaveDefaults={async (updated) => {
            const next = parseScreenGuardConfig(cfg);
            next.cameraLensDefault = { ...next.cameraLensDefault, ...updated };
            await commitConfig(next, 'Camera lens default rates & media saved!');
          }}
          onSaveCombo={async (enabled, comboOffer) => {
            const next = parseScreenGuardConfig(cfg);
            next.comboEnabled = enabled;
            next.comboOffer = comboOffer;
            await commitConfig(next, 'Combo bundle offer saved!');
          }}
        />

        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: 10,
            margin: '14px 0 10px'
          }}
        >
          <div className="category-chips">
            {POUCH_BRANDS.map((b) => (
              <button
                key={b}
                type="button"
                className={`chip ${camBrand === b ? 'active' : ''}`}
                onClick={() => {
                  setCamBrand(b);
                  setEditingCamModelKey(null);
                }}
              >
                {b}
              </button>
            ))}
            <button
              type="button"
              className="btn btn-red btn-sm"
              onClick={() => setAddingCamModel((v) => !v)}
            >
              <Plus size={14} /> Add Lens Model
            </button>
          </div>
          <input
            className="input"
            style={{ width: 220, padding: '6px 12px' }}
            placeholder="Filter lens models..."
            value={camSearch}
            onChange={(e) => setCamSearch(e.target.value)}
          />
        </div>

        {addingCamModel && (
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              gap: 8,
              padding: 12,
              borderRadius: 12,
              background: '#EAF2FF',
              marginBottom: 10
            }}
          >
            <input
              className="input"
              style={{ flex: 1, minWidth: 200 }}
              placeholder={`New ${camBrand} lens model`}
              value={newCamModelName}
              onChange={(e) => setNewCamModelName(e.target.value)}
            />
            <button type="button" className="btn btn-navy btn-sm" onClick={handleAddCamModel}>
              Add Model
            </button>
            <button
              type="button"
              className="btn btn-outline btn-sm"
              onClick={() => setAddingCamModel(false)}
            >
              Cancel
            </button>
          </div>
        )}

        <div style={{ maxHeight: 300, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8 }}>
          {filteredCamModels.map((model) => {
            const cKey = guardModelKey(camBrand, model);
            const cCfg = cfg.cameraLensModelConfigs[cKey] || {
              enabled: true,
              deleted: false,
              customPrice: null,
              offer: { active: false, type: 'NONE', value: 0, badgeText: '' }
            };
            const livePrice = resolveCameraLensPrice(camBrand, model, cfg);
            const isEditing = editingCamModelKey === cKey;
            return (
              <CameraModelEditorRow
                key={cKey}
                model={model}
                cCfg={cCfg}
                livePrice={livePrice}
                isEditing={isEditing}
                onToggleEdit={() => setEditingCamModelKey(isEditing ? null : cKey)}
                onToggleActive={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.cameraLensModelConfigs[cKey] = { ...cCfg, enabled: !cCfg.enabled };
                  await commitConfig(next);
                }}
                onDelete={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.cameraLensModelConfigs[cKey] = { ...cCfg, deleted: true, enabled: false };
                  await commitConfig(next, `Deleted ${model}`);
                }}
                onRestore={async () => {
                  const next = parseScreenGuardConfig(cfg);
                  next.cameraLensModelConfigs[cKey] = { ...cCfg, deleted: false, enabled: true };
                  await commitConfig(next, `Restored ${model}`);
                }}
                onSave={async (newName, customPrice, offer) => {
                  const next = parseScreenGuardConfig(cfg);
                  if (newName && newName !== model) {
                    const customArr = next.customCameraLensModelsByBrand[camBrand] || [];
                    if (!customArr.includes(newName)) {
                      next.customCameraLensModelsByBrand[camBrand] = [...customArr, newName];
                    }
                    next.cameraLensModelConfigs[cKey] = { ...cCfg, deleted: true, enabled: false };
                    next.cameraLensModelConfigs[guardModelKey(camBrand, newName)] = {
                      enabled: true,
                      deleted: false,
                      customPrice,
                      offer
                    };
                  } else {
                    next.cameraLensModelConfigs[cKey] = {
                      ...cCfg,
                      enabled: true,
                      deleted: false,
                      customPrice,
                      offer
                    };
                  }
                  setEditingCamModelKey(null);
                  await commitConfig(next, `Saved lens settings for ${newName || model}`);
                }}
              />
            );
          })}
        </div>
      </section>

      {/* 4. POUCHES MANAGER */}
      <section className="card">
        <div className="section-header" style={{ marginBottom: 12 }}>
          <div>
            <h3 style={{ color: 'var(--icase-navy)', fontSize: 17, fontWeight: 900 }}>
              Pouches (iPhone &amp; Samsung S Series) — Images, Colours, Show on Home &amp; Offers
            </h3>
            <p style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
              Upload pouch photos, select preset or custom colours, toggle Show on Home Page, and
              sync live to Cloud Firestore.
            </p>
          </div>
          <button type="button" className="btn btn-red btn-sm" onClick={openNewPouchModal}>
            <Plus size={15} /> Add New Pouch
          </button>
        </div>

        <div className="pouch-grid" style={{ marginBottom: 18 }}>
          {(cfg.pouchItems || []).map((p) => {
            const pr = resolvePouchPrice(p);
            const thumbUrl = resolvePouchImageUrl(p);
            const colors = Array.isArray(p.colorOptions) ? p.colorOptions : [];
            return (
              <div
                key={p.id}
                className="pouch-card"
                style={{
                  background: p.deleted ? '#FFF1F2' : '#F8FAFC',
                  borderColor: p.deleted ? '#FECDD3' : 'var(--border)'
                }}
              >
                <div style={{ display: 'flex', gap: 10, alignItems: 'flex-start' }}>
                  <img
                    src={thumbUrl}
                    alt={p.name}
                    style={{
                      width: 64,
                      height: 64,
                      borderRadius: 10,
                      objectFit: 'cover',
                      background: '#FFF',
                      border: '1px solid var(--border)'
                    }}
                  />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <strong style={{ fontSize: 14, color: 'var(--icase-navy)' }}>{p.name}</strong>
                      {p.showOnHomePage && !p.deleted && (
                        <span
                          style={{
                            padding: '1px 6px',
                            borderRadius: 6,
                            background: '#FEF3C7',
                            color: '#92400E',
                            fontSize: 10,
                            fontWeight: 900
                          }}
                        >
                          HOME
                        </span>
                      )}
                    </div>
                    <div
                      style={{
                        fontSize: 12,
                        fontWeight: 800,
                        color: p.deleted ? 'var(--icase-red)' : p.enabled ? 'var(--icase-green)' : '#B45309'
                      }}
                    >
                      {p.deleted ? 'DELETED' : `${formatINR(pr.finalPrice)}${p.enabled ? '' : ' • PAUSED'}`}
                      {!p.deleted && p.videoUrl ? ' • Video set' : ''}
                    </div>
                    <div style={{ fontSize: 11, color: 'var(--text-secondary)' }}>
                      {(p.compatibleBrands || []).join(' • ')}
                    </div>
                    {colors.length > 0 && (
                      <div style={{ display: 'flex', gap: 4, marginTop: 4, flexWrap: 'wrap' }}>
                        {colors.slice(0, 7).map((c) => (
                          <span
                            key={c.name}
                            title={c.name}
                            style={{
                              width: 13,
                              height: 13,
                              borderRadius: '50%',
                              background: c.hex || '#1E293B',
                              border: '1px solid #94A3B8',
                              display: 'inline-block'
                            }}
                          />
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                  {p.deleted ? (
                    <button
                      type="button"
                      className="btn btn-outline btn-sm"
                      onClick={async () => {
                        const next = parseScreenGuardConfig(cfg);
                        next.pouchItems = next.pouchItems.map((x) =>
                          x.id === p.id ? { ...x, deleted: false, enabled: true } : x
                        );
                        await commitConfig(next, `Restored "${p.name}"`);
                      }}
                    >
                      Restore
                    </button>
                  ) : (
                    <>
                      <button
                        type="button"
                        className="btn btn-navy btn-sm"
                        style={{ padding: '4px 10px', fontSize: 12 }}
                        onClick={() => openEditPouchModal(p)}
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        className="btn btn-outline btn-sm"
                        style={{
                          padding: '4px 10px',
                          fontSize: 12,
                          background: p.showOnHomePage ? '#FEF3C7' : '#FFF',
                          borderColor: p.showOnHomePage ? '#F59E0B' : 'var(--border)',
                          color: p.showOnHomePage ? '#92400E' : 'var(--icase-navy)'
                        }}
                        onClick={async () => {
                          const next = parseScreenGuardConfig(cfg);
                          const nextVal = !p.showOnHomePage;
                          next.pouchItems = next.pouchItems.map((x) =>
                            x.id === p.id ? { ...x, showOnHomePage: nextVal } : x
                          );
                          await commitConfig(
                            next,
                            nextVal ? `"${p.name}" featured on Home Page!` : `Removed "${p.name}" from Home Page`
                          );
                        }}
                      >
                        {p.showOnHomePage ? 'On Home ✓' : 'Show Home'}
                      </button>
                      <button
                        type="button"
                        className="btn btn-outline btn-sm"
                        style={{ padding: '4px 10px', fontSize: 12 }}
                        onClick={async () => {
                          const next = parseScreenGuardConfig(cfg);
                          next.pouchItems = next.pouchItems.map((x) =>
                            x.id === p.id ? { ...x, enabled: !x.enabled } : x
                          );
                          await commitConfig(next);
                        }}
                      >
                        {p.enabled ? 'Pause' : 'Enable'}
                      </button>
                      <button
                        type="button"
                        className="btn btn-outline btn-sm"
                        style={{ padding: '4px 10px', fontSize: 12, color: 'var(--icase-red)' }}
                        onClick={async () => {
                          const next = parseScreenGuardConfig(cfg);
                          next.pouchItems = next.pouchItems.map((x) =>
                            x.id === p.id ? { ...x, deleted: true, enabled: false } : x
                          );
                          await commitConfig(next, `Deleted "${p.name}"`);
                        }}
                      >
                        Delete
                      </button>
                    </>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {/* Pouch Compatible Models */}
        <div style={{ borderTop: '1px solid var(--border)', paddingTop: 14 }}>
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              justifyContent: 'space-between',
              alignItems: 'center',
              gap: 10,
              marginBottom: 10
            }}
          >
            <div className="category-chips">
              {POUCH_BRANDS.map((b) => (
                <button
                  key={b}
                  type="button"
                  className={`chip ${pouchModelBrand === b ? 'active' : ''}`}
                  onClick={() => setPouchModelBrand(b)}
                >
                  {b} Pouch Models
                </button>
              ))}
              <button
                type="button"
                className="btn btn-red btn-sm"
                onClick={() => setAddingPouchModel((v) => !v)}
              >
                <Plus size={14} /> Add Pouch Model
              </button>
            </div>
            <input
              className="input"
              style={{ width: 210, padding: '6px 12px' }}
              placeholder="Filter pouch models..."
              value={pouchModelSearch}
              onChange={(e) => setPouchModelSearch(e.target.value)}
            />
          </div>

          {addingPouchModel && (
            <div
              style={{
                display: 'flex',
                flexWrap: 'wrap',
                gap: 8,
                padding: 12,
                borderRadius: 12,
                background: '#EAF2FF',
                marginBottom: 10
              }}
            >
              <input
                className="input"
                style={{ flex: 1, minWidth: 200 }}
                placeholder={`New ${pouchModelBrand} pouch model`}
                value={newPouchModelName}
                onChange={(e) => setNewPouchModelName(e.target.value)}
              />
              <button type="button" className="btn btn-navy btn-sm" onClick={handleAddPouchModel}>
                Add Model
              </button>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                onClick={() => setAddingPouchModel(false)}
              >
                Cancel
              </button>
            </div>
          )}

          <div style={{ maxHeight: 220, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 6 }}>
            {filteredPouchModels.map((model) => {
              const pKey = guardModelKey(pouchModelBrand, model);
              const pmCfg = cfg.pouchModelConfigs[pKey] || { enabled: true, deleted: false };
              return (
                <div
                  key={pKey}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 12px',
                    borderRadius: 10,
                    background: pmCfg.deleted ? '#FFF1F2' : '#F8FAFC',
                    border: '1px solid var(--border)'
                  }}
                >
                  <span style={{ fontWeight: 700, fontSize: 13, color: 'var(--icase-navy)' }}>
                    {model}{' '}
                    <span style={{ fontSize: 11, color: pmCfg.deleted ? 'var(--icase-red)' : pmCfg.enabled ? 'var(--icase-green)' : '#B45309' }}>
                      ({pmCfg.deleted ? 'DELETED' : pmCfg.enabled ? 'Active' : 'Paused'})
                    </span>
                  </span>
                  <div style={{ display: 'flex', gap: 6 }}>
                    {pmCfg.deleted ? (
                      <button
                        type="button"
                        className="btn btn-outline btn-sm"
                        style={{ padding: '3px 8px', fontSize: 11.5 }}
                        onClick={async () => {
                          const next = parseScreenGuardConfig(cfg);
                          next.pouchModelConfigs[pKey] = { ...pmCfg, deleted: false, enabled: true };
                          await commitConfig(next);
                        }}
                      >
                        Restore
                      </button>
                    ) : (
                      <>
                        <button
                          type="button"
                          className="btn btn-outline btn-sm"
                          style={{ padding: '3px 8px', fontSize: 11.5 }}
                          onClick={async () => {
                            const next = parseScreenGuardConfig(cfg);
                            next.pouchModelConfigs[pKey] = { ...pmCfg, enabled: !pmCfg.enabled };
                            await commitConfig(next);
                          }}
                        >
                          {pmCfg.enabled ? 'Pause' : 'Enable'}
                        </button>
                        <button
                          type="button"
                          className="btn btn-outline btn-sm"
                          style={{ padding: '3px 8px', fontSize: 11.5, color: 'var(--icase-red)' }}
                          onClick={async () => {
                            const next = parseScreenGuardConfig(cfg);
                            next.pouchModelConfigs[pKey] = { ...pmCfg, deleted: true, enabled: false };
                            await commitConfig(next);
                          }}
                        >
                          Delete
                        </button>
                      </>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* ADD / EDIT POUCH MODAL */}
      {pouchDraft && (
        <div className="overlay" onClick={() => setPouchDraft(null)}>
          <div className="modal" style={{ maxWidth: 680 }} onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <strong>
                {pouchDraft.isNew ? 'Add New Pouch' : `Edit Pouch: ${pouchDraft.name}`}
              </strong>
              <button
                type="button"
                className="btn btn-sm"
                style={{ background: 'rgba(255,255,255,0.14)', color: '#FFF' }}
                onClick={() => setPouchDraft(null)}
              >
                <X size={16} />
              </button>
            </div>

            <form className="modal-body" onSubmit={handleSavePouch}>
              {/* 1. Image Upload */}
              <div
                style={{
                  padding: 14,
                  borderRadius: 14,
                  background: '#F8FAFC',
                  border: '1px solid var(--border)',
                  marginBottom: 14
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: 10
                  }}
                >
                  <strong style={{ fontSize: 12.5, color: 'var(--icase-navy)' }}>
                    1. Pouch Image Upload
                  </strong>
                  {pouchDraft.imageUrl && (
                    <button
                      type="button"
                      className="btn btn-sm"
                      style={{ background: '#FFECEE', color: '#E50914', padding: '4px 10px' }}
                      onClick={() => setPouchDraft((prev) => ({ ...prev, imageUrl: '' }))}
                    >
                      Remove Image
                    </button>
                  )}
                </div>
                <div style={{ display: 'flex', gap: 14, alignItems: 'center', flexWrap: 'wrap' }}>
                  <div
                    style={{
                      width: 88,
                      height: 88,
                      borderRadius: 12,
                      border: '2px dashed #CBD5E1',
                      background: '#FFF',
                      display: 'grid',
                      placeItems: 'center',
                      overflow: 'hidden'
                    }}
                  >
                    {pouchDraft.imageUrl ? (
                      <img
                        src={pouchDraft.imageUrl}
                        alt="Preview"
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                      />
                    ) : (
                      <ImageIcon size={26} color="#94A3B8" />
                    )}
                  </div>
                  <div style={{ flex: 1, minWidth: 220, display: 'flex', flexDirection: 'column', gap: 8 }}>
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      <label className="btn btn-navy btn-sm" style={{ cursor: 'pointer' }}>
                        <Upload size={14} />{' '}
                        {pouchDraft.imageUrl ? 'Change Gallery Image' : 'Upload from Gallery'}
                        <input
                          type="file"
                          accept="image/*"
                          style={{ display: 'none' }}
                          onChange={(e) => handlePouchMainImageUpload(e.target.files?.[0])}
                        />
                      </label>
                      <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer' }}>
                        <Camera size={14} /> Camera Photo
                        <input
                          type="file"
                          accept="image/*"
                          capture="environment"
                          style={{ display: 'none' }}
                          onChange={(e) => handlePouchMainImageUpload(e.target.files?.[0])}
                        />
                      </label>
                    </div>
                    <input
                      className="input"
                      placeholder="Or paste optional Image URL"
                      value={
                        pouchDraft.imageUrl && !String(pouchDraft.imageUrl).startsWith('data:image')
                          ? pouchDraft.imageUrl
                          : ''
                      }
                      onChange={(e) =>
                        setPouchDraft((prev) => ({ ...prev, imageUrl: e.target.value }))
                      }
                    />
                  </div>
                </div>
              </div>

              {/* 1b. Optional Video Upload / Link */}
              <div
                style={{
                  padding: 14,
                  borderRadius: 14,
                  background: '#F8FAFC',
                  border: '1px solid var(--border)',
                  marginBottom: 14
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: 8
                  }}
                >
                  <strong style={{ fontSize: 12.5, color: 'var(--icase-navy)', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Film size={14} /> Optional Pouch Video (MP4 URL, YouTube Link, or Clip ≤ 350 KB)
                  </strong>
                  <div style={{ display: 'flex', gap: 6 }}>
                    {pouchDraft.videoUrl && (
                      <>
                        <button
                          type="button"
                          className="btn btn-navy btn-sm"
                          style={{ padding: '4px 10px' }}
                          onClick={() =>
                            setAdminVideoPreview({
                              open: true,
                              title: pouchDraft.name || 'Pouch Video Preview',
                              url: pouchDraft.videoUrl
                            })
                          }
                        >
                          <Play size={12} fill="currentColor" /> Preview
                        </button>
                        <button
                          type="button"
                          className="btn btn-sm"
                          style={{ background: '#FFECEE', color: '#E50914', padding: '4px 10px' }}
                          onClick={() => setPouchDraft((prev) => ({ ...prev, videoUrl: '' }))}
                        >
                          Remove
                        </button>
                      </>
                    )}
                  </div>
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
                  <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer' }}>
                    <Upload size={13} /> Upload Video Clip (≤ 350 KB)
                    <input
                      type="file"
                      accept="video/*"
                      style={{ display: 'none' }}
                      onChange={async (e) => {
                        const f = e.target.files?.[0];
                        e.target.value = '';
                        if (!f) return;
                        try {
                          const dataUrl = await readShortVideoFileToDataUrl(f);
                          setPouchDraft((prev) => (prev ? { ...prev, videoUrl: dataUrl } : prev));
                        } catch (err) {
                          setToast(err.message || 'Video upload failed');
                        }
                      }}
                    />
                  </label>
                  <input
                    className="input"
                    style={{ flex: 1, minWidth: 200 }}
                    placeholder="Or paste MP4 / YouTube link"
                    value={
                      pouchDraft.videoUrl && !String(pouchDraft.videoUrl).startsWith('data:video')
                        ? pouchDraft.videoUrl
                        : ''
                    }
                    onChange={(e) =>
                      setPouchDraft((prev) => ({ ...prev, videoUrl: e.target.value }))
                    }
                  />
                </div>
                {String(pouchDraft.videoUrl || '').startsWith('data:video') && (
                  <div style={{ fontSize: 11.5, color: 'var(--icase-green)', fontWeight: 700, marginTop: 6 }}>
                    ✓ Short video clip uploaded ({Math.round((pouchDraft.videoUrl.length * 0.75) / 1024)} KB)
                  </div>
                )}
              </div>

              {/* 2. Details & Pricing */}
              <div className="form-grid">
                <div className="form-group">
                  <label>Pouch Name *</label>
                  <input
                    className="input"
                    required
                    value={pouchDraft.name}
                    onChange={(e) => setPouchDraft((prev) => ({ ...prev, name: e.target.value }))}
                  />
                </div>
                <div className="form-group">
                  <label>Base Price (INR) *</label>
                  <input
                    type="number"
                    className="input"
                    required
                    value={pouchDraft.basePrice}
                    onChange={(e) =>
                      setPouchDraft((prev) => ({ ...prev, basePrice: e.target.value }))
                    }
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Description</label>
                <input
                  className="input"
                  value={pouchDraft.description}
                  onChange={(e) =>
                    setPouchDraft((prev) => ({ ...prev, description: e.target.value }))
                  }
                />
              </div>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16, marginBottom: 12 }}>
                {POUCH_BRANDS.map((brand) => (
                  <label
                    key={brand}
                    style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 700, fontSize: 13 }}
                  >
                    <input
                      type="checkbox"
                      checked={pouchDraft.compatibleBrands.includes(brand)}
                      onChange={(e) => {
                        setPouchDraft((prev) => {
                          const nextBrands = e.target.checked
                            ? Array.from(new Set([...prev.compatibleBrands, brand]))
                            : prev.compatibleBrands.filter((x) => x !== brand);
                          return { ...prev, compatibleBrands: nextBrands };
                        });
                      }}
                    />
                    {brand}
                  </label>
                ))}
                <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 800, fontSize: 13, color: '#92400E' }}>
                  <input
                    type="checkbox"
                    checked={pouchDraft.showOnHomePage}
                    onChange={(e) =>
                      setPouchDraft((prev) => ({ ...prev, showOnHomePage: e.target.checked }))
                    }
                  />
                  Show on Home Page
                </label>
              </div>

              <div className="form-grid" style={{ marginBottom: 12 }}>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>Offer Type</label>
                  <select
                    className="input"
                    value={pouchDraft.offerType}
                    onChange={(e) =>
                      setPouchDraft((prev) => ({ ...prev, offerType: e.target.value }))
                    }
                  >
                    <option value="NONE">No Offer</option>
                    <option value="FLAT_OFF">Flat ₹ Off</option>
                    <option value="PERCENT_OFF">% Off</option>
                    <option value="SPECIAL_PRICE">Special ₹</option>
                  </select>
                </div>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>Offer Value</label>
                  <input
                    type="number"
                    className="input"
                    value={pouchDraft.offerValue}
                    onChange={(e) =>
                      setPouchDraft((prev) => ({ ...prev, offerValue: e.target.value }))
                    }
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Offer Badge</label>
                <input
                  className="input"
                  placeholder="HOT DEAL"
                  value={pouchDraft.badgeText}
                  onChange={(e) =>
                    setPouchDraft((prev) => ({ ...prev, badgeText: e.target.value }))
                  }
                />
              </div>

              {/* 3. Available Colours Manager */}
              <div
                style={{
                  padding: 14,
                  borderRadius: 14,
                  background: '#F8FAFC',
                  border: '1px solid var(--border)',
                  marginBottom: 16
                }}
              >
                <strong style={{ display: 'block', fontSize: 12.5, color: 'var(--icase-navy)', marginBottom: 8 }}>
                  3. Available Colours Manager
                </strong>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 10 }}>
                  {POUCH_PRESET_COLORS.map((pc) => {
                    const isSel = pouchDraft.colorOptions.some(
                      (c) => c.name.toLowerCase() === pc.name.toLowerCase()
                    );
                    return (
                      <button
                        key={pc.name}
                        type="button"
                        className={`color-swatch-chip ${isSel ? 'selected' : ''}`}
                        onClick={() => togglePresetPouchColor(pc)}
                      >
                        <span
                          style={{
                            width: 12,
                            height: 12,
                            borderRadius: '50%',
                            background: pc.hex,
                            border: '1px solid #94A3B8',
                            display: 'inline-block'
                          }}
                        />
                        <span>{pc.name}</span>
                        {isSel && <span>✓</span>}
                      </button>
                    );
                  })}
                </div>

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center', marginBottom: 10 }}>
                  <input
                    className="input"
                    style={{ flex: 1, minWidth: 160 }}
                    placeholder="Custom colour name (e.g. Titanium Gold)"
                    value={customColorName}
                    onChange={(e) => setCustomColorName(e.target.value)}
                  />
                  <input
                    type="color"
                    value={normalizeHexColor(customColorHex)}
                    onChange={(e) => setCustomColorHex(e.target.value)}
                    style={{ width: 42, height: 36, borderRadius: 8, border: '1px solid var(--border)' }}
                  />
                  <button
                    type="button"
                    className="btn btn-navy btn-sm"
                    onClick={addCustomPouchColor}
                  >
                    + Add Colour
                  </button>
                </div>

                {pouchDraft.colorOptions.length > 0 && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {pouchDraft.colorOptions.map((c, idx) => {
                      const isExpanded = expandedColorName === c.name;
                      return (
                        <div
                          key={`${c.name}-${idx}`}
                          style={{
                            background: '#FFF',
                            border: '1px solid var(--border)',
                            borderRadius: 10,
                            padding: 10
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                              <span
                                style={{
                                  width: 16,
                                  height: 16,
                                  borderRadius: '50%',
                                  background: c.hex,
                                  border: '1px solid #94A3B8',
                                  display: 'inline-block'
                                }}
                              />
                              <strong style={{ fontSize: 13 }}>{c.name}</strong>
                              {c.imageUrl && (
                                <span style={{ fontSize: 11, color: 'var(--icase-green)', fontWeight: 700 }}>
                                  Photo set
                                </span>
                              )}
                            </div>
                            <div style={{ display: 'flex', gap: 6 }}>
                              <button
                                type="button"
                                className="btn btn-outline btn-sm"
                                style={{ padding: '3px 8px', fontSize: 11 }}
                                onClick={() =>
                                  setExpandedColorName(isExpanded ? '' : c.name)
                                }
                              >
                                {isExpanded ? 'Hide Photo' : 'Colour Photo'}
                              </button>
                              <button
                                type="button"
                                className="btn btn-sm"
                                style={{ background: '#FFECEE', color: '#E50914', padding: '3px 8px' }}
                                onClick={() =>
                                  setPouchDraft((prev) => ({
                                    ...prev,
                                    colorOptions: prev.colorOptions.filter((_, i) => i !== idx)
                                  }))
                                }
                              >
                                <Trash2 size={12} />
                              </button>
                            </div>
                          </div>

                          {isExpanded && (
                            <div style={{ marginTop: 10, display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
                              <label className="btn btn-navy btn-sm" style={{ padding: '4px 10px', fontSize: 11, cursor: 'pointer' }}>
                                Upload Photo
                                <input
                                  type="file"
                                  accept="image/*"
                                  style={{ display: 'none' }}
                                  onChange={(e) => handlePouchColorImageUpload(idx, e.target.files?.[0])}
                                />
                              </label>
                              <input
                                className="input"
                                style={{ flex: 1, minWidth: 160, fontSize: 12, padding: '6px 10px' }}
                                placeholder={`Optional image URL for ${c.name}`}
                                value={
                                  c.imageUrl && !String(c.imageUrl).startsWith('data:image')
                                    ? c.imageUrl
                                    : ''
                                }
                                onChange={(e) =>
                                  setPouchDraft((prev) => ({
                                    ...prev,
                                    colorOptions: prev.colorOptions.map((col, i) =>
                                      i === idx ? { ...col, imageUrl: e.target.value } : col
                                    )
                                  }))
                                }
                              />
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              <div style={{ display: 'flex', gap: 10 }}>
                <button type="submit" className="btn btn-red">
                  Save Pouch
                </button>
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => setPouchDraft(null)}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <MediaVideoModal
        isOpen={adminVideoPreview.open}
        title={adminVideoPreview.title}
        url={adminVideoPreview.url}
        onClose={() => setAdminVideoPreview({ open: false, title: '', url: '' })}
      />
    </div>
  );
}

function GlassTypeEditorCard({
  glassType,
  gCfg,
  resolvedThumb,
  helperFallback,
  preview,
  onPreviewVideo,
  onToast,
  onToggleActive,
  onDelete,
  onRestore,
  onSave
}) {
  const [basePrice, setBasePrice] = useState(String(gCfg.basePrice));
  const [offerType, setOfferType] = useState(gCfg.offer?.type || 'NONE');
  const [offerVal, setOfferVal] = useState(String(gCfg.offer?.value || 0));
  const [badgeText, setBadgeText] = useState(gCfg.offer?.badgeText || '');
  const [helperNote, setHelperNote] = useState(gCfg.helperNote || '');
  const [imageUrl, setImageUrl] = useState(gCfg.imageUrl || '');
  const [videoUrl, setVideoUrl] = useState(gCfg.videoUrl || '');

  useEffect(() => {
    setImageUrl(gCfg.imageUrl || '');
    setVideoUrl(gCfg.videoUrl || '');
  }, [gCfg.imageUrl, gCfg.videoUrl]);

  return (
    <div
      className="pouch-card"
      style={{
        background: gCfg.deleted ? '#FFF1F2' : '#F8FAFC',
        borderColor: gCfg.deleted ? '#FECDD3' : 'var(--border)'
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <img
            src={imageUrl.trim() || resolvedThumb}
            alt={glassType}
            style={{
              width: 44,
              height: 44,
              borderRadius: 10,
              objectFit: 'cover',
              border: '1px solid var(--border)',
              background: '#FFF',
              flexShrink: 0
            }}
          />
          <div>
            <strong style={{ color: 'var(--icase-navy)', fontSize: 14 }}>{glassType}</strong>
            <div
              style={{
                fontSize: 12,
                fontWeight: 800,
                color: gCfg.deleted ? 'var(--icase-red)' : gCfg.enabled ? 'var(--icase-green)' : '#B45309'
              }}
            >
              {gCfg.deleted
                ? 'DELETED (Hidden)'
                : gCfg.enabled
                  ? `LIVE: ${formatINR(preview.finalPrice)}`
                  : 'PAUSED'}
              {!gCfg.deleted && preview.offerBadge ? ` • ${preview.offerBadge}` : ''}
              {!gCfg.deleted && videoUrl.trim() ? ' • Video set' : ''}
            </div>
          </div>
        </div>
        <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
          {gCfg.deleted ? (
            <button type="button" className="btn btn-outline btn-sm" onClick={onRestore}>
              Restore
            </button>
          ) : (
            <>
              <label style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 12, fontWeight: 700 }}>
                <input
                  type="checkbox"
                  checked={gCfg.enabled}
                  onChange={(e) => onToggleActive(e.target.checked)}
                />
                Active
              </label>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ padding: '4px 8px', fontSize: 11.5, color: 'var(--icase-red)' }}
                onClick={onDelete}
              >
                Delete
              </button>
            </>
          )}
        </div>
      </div>

      {!gCfg.deleted && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <div className="form-grid" style={{ gap: 8 }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label style={{ fontSize: 11 }}>Base Rate (₹)</label>
              <input
                type="number"
                className="input"
                value={basePrice}
                onChange={(e) => setBasePrice(e.target.value)}
              />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label style={{ fontSize: 11 }}>Offer Type</label>
              <select
                className="input"
                value={offerType}
                onChange={(e) => setOfferType(e.target.value)}
              >
                <option value="NONE">No Offer</option>
                <option value="FLAT_OFF">Flat ₹ Off</option>
                <option value="PERCENT_OFF">% Off</option>
                <option value="SPECIAL_PRICE">Special ₹</option>
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label style={{ fontSize: 11 }}>Offer Value</label>
              <input
                type="number"
                className="input"
                value={offerVal}
                onChange={(e) => setOfferVal(e.target.value)}
              />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label style={{ fontSize: 11 }}>Promo Badge</label>
              <input
                className="input"
                placeholder="FESTIVE"
                value={badgeText}
                onChange={(e) => setBadgeText(e.target.value)}
              />
            </div>
          </div>
          <input
            className="input"
            placeholder={`Note: ${helperFallback}`}
            value={helperNote}
            onChange={(e) => setHelperNote(e.target.value)}
          />
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', alignItems: 'center' }}>
            <input
              className="input"
              style={{ flex: 1, minWidth: 140 }}
              placeholder="Optional Type Image URL"
              value={imageUrl.startsWith('data:image') ? '' : imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
            />
            <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer', padding: '5px 8px' }}>
              <Upload size={12} /> Img
              <input
                type="file"
                accept="image/*"
                hidden
                onChange={async (e) => {
                  const f = e.target.files?.[0];
                  e.target.value = '';
                  if (!f) return;
                  try {
                    setImageUrl(await compressImageFileToDataUrl(f));
                  } catch (err) {
                    if (onToast) onToast(err.message || 'Image upload failed');
                  }
                }}
              />
            </label>
            {imageUrl && (
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ padding: '5px 8px' }}
                onClick={() => setImageUrl('')}
              >
                Reset Img
              </button>
            )}
          </div>
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', alignItems: 'center' }}>
            <input
              className="input"
              style={{ flex: 1, minWidth: 140 }}
              placeholder="Optional Video URL / YouTube"
              value={videoUrl.startsWith('data:video') ? '' : videoUrl}
              onChange={(e) => setVideoUrl(e.target.value)}
            />
            <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer', padding: '5px 8px' }}>
              <Film size={12} /> Clip
              <input
                type="file"
                accept="video/*"
                hidden
                onChange={async (e) => {
                  const f = e.target.files?.[0];
                  e.target.value = '';
                  if (!f) return;
                  try {
                    setVideoUrl(await readShortVideoFileToDataUrl(f));
                  } catch (err) {
                    if (onToast) onToast(err.message || 'Video upload failed');
                  }
                }}
              />
            </label>
            {videoUrl.trim() && (
              <>
                <button
                  type="button"
                  className="btn btn-navy btn-sm"
                  style={{ padding: '5px 8px' }}
                  onClick={() => onPreviewVideo && onPreviewVideo(`${glassType} Video`, videoUrl.trim())}
                >
                  <Play size={11} fill="currentColor" />
                </button>
                <button
                  type="button"
                  className="btn btn-outline btn-sm"
                  style={{ padding: '5px 8px', color: 'var(--icase-red)' }}
                  onClick={() => setVideoUrl('')}
                >
                  ×
                </button>
              </>
            )}
          </div>
          <button
            type="button"
            className="btn btn-navy btn-sm"
            onClick={() =>
              onSave({
                basePrice: Math.max(0, Number(basePrice || 0)),
                helperNote: helperNote.trim(),
                imageUrl: imageUrl.trim(),
                videoUrl: videoUrl.trim(),
                offer: {
                  active: offerType !== 'NONE',
                  type: offerType,
                  value: Number(offerVal || 0),
                  badgeText: badgeText.trim()
                }
              })
            }
          >
            Save Rate, Note &amp; Media
          </button>
        </div>
      )}
    </div>
  );
}

function GlassModelEditorRow({
  model,
  mCfg,
  isEditing,
  applicableTypes,
  onToggleEdit,
  onToggleActive,
  onDelete,
  onRestore,
  onSave
}) {
  const [newName, setNewName] = useState(model);
  const [rates, setRates] = useState(() => ({ ...(mCfg.customRates || {}) }));
  const [offerType, setOfferType] = useState(mCfg.offer?.type || 'NONE');
  const [offerVal, setOfferVal] = useState(String(mCfg.offer?.value || 0));
  const [badgeText, setBadgeText] = useState(mCfg.offer?.badgeText || '');

  return (
    <div
      style={{
        padding: 12,
        borderRadius: 12,
        border: '1px solid var(--border)',
        background: mCfg.deleted ? '#FFF1F2' : '#F8FAFC'
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
        <div>
          <strong style={{ fontSize: 13.5, color: 'var(--icase-navy)' }}>{model}</strong>
          <div style={{ fontSize: 11.5, fontWeight: 700, color: mCfg.deleted ? 'var(--icase-red)' : mCfg.enabled ? 'var(--icase-green)' : '#B45309' }}>
            {mCfg.deleted ? 'DELETED' : mCfg.enabled ? 'Active' : 'Paused'}
            {Object.keys(mCfg.customRates || {}).length > 0
              ? ` • ${Object.keys(mCfg.customRates).length} custom rate(s)`
              : ''}
          </div>
        </div>
        <div style={{ display: 'flex', gap: 6 }}>
          {mCfg.deleted ? (
            <button type="button" className="btn btn-outline btn-sm" onClick={onRestore}>
              Restore
            </button>
          ) : (
            <>
              <button type="button" className="btn btn-outline btn-sm" onClick={onToggleEdit}>
                {isEditing ? 'Close' : 'Edit & Rates'}
              </button>
              <button type="button" className="btn btn-outline btn-sm" onClick={onToggleActive}>
                {mCfg.enabled ? 'Pause' : 'Enable'}
              </button>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ color: 'var(--icase-red)' }}
                onClick={onDelete}
              >
                Delete
              </button>
            </>
          )}
        </div>
      </div>

      {isEditing && !mCfg.deleted && (
        <div style={{ marginTop: 10, paddingTop: 10, borderTop: '1px solid var(--border)', display: 'flex', flexDirection: 'column', gap: 8 }}>
          <input
            className="input"
            placeholder="Rename model"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
          />
          <div className="form-grid" style={{ gap: 8 }}>
            {applicableTypes.map((gt) => (
              <div key={gt} className="form-group" style={{ marginBottom: 0 }}>
                <label style={{ fontSize: 11 }}>{gt} Override (₹)</label>
                <input
                  type="number"
                  className="input"
                  placeholder={`Default (${DEFAULT_GLASS_TYPE_PRICES[gt] || 199})`}
                  value={rates[gt] || ''}
                  onChange={(e) => setRates((prev) => ({ ...prev, [gt]: e.target.value }))}
                />
              </div>
            ))}
          </div>
          <div className="form-grid" style={{ gap: 8 }}>
            <select
              className="input"
              value={offerType}
              onChange={(e) => setOfferType(e.target.value)}
            >
              <option value="NONE">Use Glass Type Default Offer</option>
              <option value="FLAT_OFF">Flat ₹ Off</option>
              <option value="PERCENT_OFF">% Off</option>
              <option value="SPECIAL_PRICE">Special ₹</option>
            </select>
            <input
              type="number"
              className="input"
              placeholder="Offer value"
              value={offerVal}
              onChange={(e) => setOfferVal(e.target.value)}
            />
            <input
              className="input"
              placeholder="Promo Badge"
              value={badgeText}
              onChange={(e) => setBadgeText(e.target.value)}
            />
          </div>
          <button
            type="button"
            className="btn btn-navy btn-sm"
            onClick={() => {
              const cleanedRates = {};
              for (const [k, v] of Object.entries(rates)) {
                const n = Number(v);
                if (Number.isFinite(n) && n > 0) cleanedRates[k] = n;
              }
              onSave(newName.trim(), cleanedRates, {
                active: offerType !== 'NONE',
                type: offerType,
                value: Number(offerVal || 0),
                badgeText: badgeText.trim()
              });
            }}
          >
            Save Model Settings
          </button>
        </div>
      )}
    </div>
  );
}

function CameraLensDefaultsEditor({
  camDefault,
  comboEnabled,
  comboOffer,
  onPreviewVideo,
  onToast,
  onSaveDefaults,
  onSaveCombo
}) {
  const [basePrice, setBasePrice] = useState(String(camDefault.basePrice));
  const [oType, setOType] = useState(camDefault.offer?.type || 'NONE');
  const [oVal, setOVal] = useState(String(camDefault.offer?.value || 0));
  const [oBadge, setOBadge] = useState(camDefault.offer?.badgeText || '');
  const [camVideoUrl, setCamVideoUrl] = useState(camDefault.videoUrl || '');

  useEffect(() => {
    setCamVideoUrl(camDefault.videoUrl || '');
  }, [camDefault.videoUrl]);

  const [isComboOn, setIsComboOn] = useState(comboEnabled);
  const [cType, setCType] = useState(comboOffer?.type || 'FLAT_OFF');
  const [cVal, setCVal] = useState(String(comboOffer?.value || 50));
  const [cBadge, setCBadge] = useState(comboOffer?.badgeText || 'COMBO SAVE');

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
      <div style={{ padding: 14, borderRadius: 12, background: '#F8FAFC', border: '1px solid var(--border)' }}>
        <div className="form-grid" style={{ gap: 8, marginBottom: 10 }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label style={{ fontSize: 11 }}>Default Lens Rate (₹)</label>
            <input
              type="number"
              className="input"
              value={basePrice}
              onChange={(e) => setBasePrice(e.target.value)}
            />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label style={{ fontSize: 11 }}>Offer Type</label>
            <select className="input" value={oType} onChange={(e) => setOType(e.target.value)}>
              <option value="NONE">No Offer</option>
              <option value="FLAT_OFF">Flat ₹ Off</option>
              <option value="PERCENT_OFF">% Off</option>
              <option value="SPECIAL_PRICE">Special ₹</option>
            </select>
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label style={{ fontSize: 11 }}>Offer Value</label>
            <input
              type="number"
              className="input"
              value={oVal}
              onChange={(e) => setOVal(e.target.value)}
            />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label style={{ fontSize: 11 }}>Promo Badge</label>
            <input
              className="input"
              placeholder="LENS DEAL"
              value={oBadge}
              onChange={(e) => setOBadge(e.target.value)}
            />
          </div>
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center', marginBottom: 10 }}>
          <input
            className="input"
            style={{ flex: 1, minWidth: 200 }}
            placeholder="Optional Camera Lens Video URL / YouTube"
            value={camVideoUrl.startsWith('data:video') ? '' : camVideoUrl}
            onChange={(e) => setCamVideoUrl(e.target.value)}
          />
          <label className="btn btn-outline btn-sm" style={{ cursor: 'pointer' }}>
            <Film size={13} /> Clip (≤ 350 KB)
            <input
              type="file"
              accept="video/*"
              hidden
              onChange={async (e) => {
                const f = e.target.files?.[0];
                e.target.value = '';
                if (!f) return;
                try {
                  setCamVideoUrl(await readShortVideoFileToDataUrl(f));
                } catch (err) {
                  if (onToast) onToast(err.message || 'Video upload failed');
                }
              }}
            />
          </label>
          {camVideoUrl.trim() && (
            <>
              <button
                type="button"
                className="btn btn-navy btn-sm"
                onClick={() => onPreviewVideo && onPreviewVideo('Camera Lens Video', camVideoUrl.trim())}
              >
                <Play size={12} fill="currentColor" /> Preview
              </button>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ color: 'var(--icase-red)' }}
                onClick={() => setCamVideoUrl('')}
              >
                Remove
              </button>
            </>
          )}
        </div>
        <button
          type="button"
          className="btn btn-navy btn-sm"
          onClick={() =>
            onSaveDefaults({
              basePrice: Math.max(0, Number(basePrice || 0)),
              videoUrl: camVideoUrl.trim(),
              offer: {
                active: oType !== 'NONE',
                type: oType,
                value: Number(oVal || 0),
                badgeText: oBadge.trim()
              }
            })
          }
        >
          Save Camera Lens Defaults
        </button>
      </div>

      <div style={{ padding: 14, borderRadius: 12, background: '#EAF2FF', border: '1px solid rgba(0,102,204,0.3)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
          <strong style={{ fontSize: 13.5, color: 'var(--icase-navy)' }}>
            Tempered Glass + Camera Lens Combo Deal
          </strong>
          <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12.5, fontWeight: 700 }}>
            <input
              type="checkbox"
              checked={isComboOn}
              onChange={(e) => setIsComboOn(e.target.checked)}
            />
            Combo Active
          </label>
        </div>
        <div className="form-grid" style={{ gap: 8, marginBottom: 10 }}>
          <select className="input" value={cType} onChange={(e) => setCType(e.target.value)}>
            <option value="NONE">No Discount</option>
            <option value="FLAT_OFF">Flat ₹ Off</option>
            <option value="PERCENT_OFF">% Off</option>
            <option value="SPECIAL_PRICE">Special ₹</option>
          </select>
          <input
            type="number"
            className="input"
            value={cVal}
            onChange={(e) => setCVal(e.target.value)}
          />
          <input
            className="input"
            placeholder="COMBO SAVE"
            value={cBadge}
            onChange={(e) => setCBadge(e.target.value)}
          />
        </div>
        <button
          type="button"
          className="btn btn-navy btn-sm"
          onClick={() =>
            onSaveCombo(isComboOn, {
              active: cType !== 'NONE',
              type: cType,
              value: Number(cVal || 0),
              badgeText: cBadge.trim()
            })
          }
        >
          Save Combo Offer
        </button>
      </div>
    </div>
  );
}

function CameraModelEditorRow({
  model,
  cCfg,
  livePrice,
  isEditing,
  onToggleEdit,
  onToggleActive,
  onDelete,
  onRestore,
  onSave
}) {
  const [newName, setNewName] = useState(model);
  const [customPrice, setCustomPrice] = useState(cCfg.customPrice ? String(cCfg.customPrice) : '');
  const [oType, setOType] = useState(cCfg.offer?.type || 'NONE');
  const [oVal, setOVal] = useState(String(cCfg.offer?.value || 0));
  const [oBadge, setOBadge] = useState(cCfg.offer?.badgeText || '');

  return (
    <div
      style={{
        padding: 10,
        borderRadius: 10,
        border: '1px solid var(--border)',
        background: cCfg.deleted ? '#FFF1F2' : '#F8FAFC'
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
        <div>
          <strong style={{ fontSize: 13.5, color: 'var(--icase-navy)' }}>{model}</strong>
          <div style={{ fontSize: 11.5, fontWeight: 700, color: cCfg.deleted ? 'var(--icase-red)' : cCfg.enabled ? 'var(--icase-green)' : '#B45309' }}>
            {cCfg.deleted
              ? 'DELETED'
              : `${formatINR(livePrice.finalPrice)}${cCfg.enabled ? '' : ' • PAUSED'}`}
          </div>
        </div>
        <div style={{ display: 'flex', gap: 6 }}>
          {cCfg.deleted ? (
            <button type="button" className="btn btn-outline btn-sm" onClick={onRestore}>
              Restore
            </button>
          ) : (
            <>
              <button type="button" className="btn btn-outline btn-sm" onClick={onToggleEdit}>
                {isEditing ? 'Close' : 'Edit & Rate'}
              </button>
              <button type="button" className="btn btn-outline btn-sm" onClick={onToggleActive}>
                {cCfg.enabled ? 'Pause' : 'Enable'}
              </button>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ color: 'var(--icase-red)' }}
                onClick={onDelete}
              >
                Delete
              </button>
            </>
          )}
        </div>
      </div>

      {isEditing && !cCfg.deleted && (
        <div style={{ marginTop: 8, paddingTop: 8, borderTop: '1px solid var(--border)', display: 'flex', flexDirection: 'column', gap: 8 }}>
          <input
            className="input"
            placeholder="Rename model"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
          />
          <div className="form-grid" style={{ gap: 8 }}>
            <input
              type="number"
              className="input"
              placeholder="Custom ₹"
              value={customPrice}
              onChange={(e) => setCustomPrice(e.target.value)}
            />
            <select className="input" value={oType} onChange={(e) => setOType(e.target.value)}>
              <option value="NONE">Use Default Offer</option>
              <option value="FLAT_OFF">Flat ₹ Off</option>
              <option value="PERCENT_OFF">% Off</option>
              <option value="SPECIAL_PRICE">Special ₹</option>
            </select>
            <input
              type="number"
              className="input"
              placeholder="Offer Value"
              value={oVal}
              onChange={(e) => setOVal(e.target.value)}
            />
            <input
              className="input"
              placeholder="Badge"
              value={oBadge}
              onChange={(e) => setOBadge(e.target.value)}
            />
          </div>
          <button
            type="button"
            className="btn btn-navy btn-sm"
            onClick={() => {
              const cp = Number(customPrice);
              onSave(newName.trim(), Number.isFinite(cp) && cp > 0 ? cp : null, {
                active: oType !== 'NONE',
                type: oType,
                value: Number(oVal || 0),
                badgeText: oBadge.trim()
              });
            }}
          >
            Save Lens Model
          </button>
        </div>
      )}
    </div>
  );
}
