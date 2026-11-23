import React, { useEffect, useMemo, useState } from 'react';
import { Wrench, CheckCircle, Info, Search, Navigation, ChevronDown } from 'lucide-react';
import { doc, setDoc } from 'firebase/firestore';
import { db } from '../firebase';
import { COMPLAINT_OPTIONS, TIME_SLOTS, normalizePhoneToE164 } from '../data/constants';
import { MAJOR_DEVICE_BRANDS, searchDeviceCatalog } from '../data/deviceCatalog';
import { detectCurrentLocation } from '../data/locationHelper';

export default function RepairSection({ profile, resolvedUserId, onEnsureProfile, onTrackId }) {
  const [brandName, setBrandName] = useState('');
  const [modelName, setModelName] = useState('');
  const [deviceSearch, setDeviceSearch] = useState('');
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const [complaints, setComplaints] = useState([]);
  const [description, setDescription] = useState('');
  const [pickupAddress, setPickupAddress] = useState(profile.address || '');
  const [customerName, setCustomerName] = useState(profile.name || '');
  const [phoneNumber, setPhoneNumber] = useState(profile.phone || '');
  const [whatsappNumber, setWhatsappNumber] = useState(profile.whatsapp || '');
  const [preferredDate, setPreferredDate] = useState('Today');
  const [preferredTimeSlot, setPreferredTimeSlot] = useState(TIME_SLOTS[0]);

  const [detectingGps, setDetectingGps] = useState(false);
  const [gpsStatus, setGpsStatus] = useState('');
  const [gpsIsError, setGpsIsError] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [bookedRepair, setBookedRepair] = useState(null);

  useEffect(() => {
    if (profile.name && !customerName) setCustomerName(profile.name);
    if (profile.phone && !phoneNumber) setPhoneNumber(profile.phone);
    if (profile.whatsapp && !whatsappNumber) setWhatsappNumber(profile.whatsapp);
    if (profile.address && !pickupAddress) setPickupAddress(profile.address);
  }, [profile]);

  const filteredDevices = useMemo(
    () => searchDeviceCatalog(brandName, deviceSearch),
    [brandName, deviceSearch]
  );

  function toggleComplaint(item) {
    setComplaints((prev) =>
      prev.includes(item) ? prev.filter((x) => x !== item) : [...prev, item]
    );
  }

  async function handleDetectGps() {
    setDetectingGps(true);
    setGpsIsError(false);
    setGpsStatus('Fetching live GPS location...');
    try {
      const res = await detectCurrentLocation();
      setPickupAddress(res.formattedAddress);
      setGpsStatus(
        res.isGeocoded
          ? 'Current location detected via GPS.'
          : 'GPS coordinates captured. Add house/street details if needed.'
      );
    } catch (err) {
      setGpsIsError(true);
      setGpsStatus(err.message || 'Could not detect location.');
    } finally {
      setDetectingGps(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!brandName.trim() || !modelName.trim()) {
      setError('Please select your device brand and model.');
      return;
    }
    if (complaints.length === 0) {
      setError('Please select at least one device issue.');
      return;
    }
    if (!pickupAddress.trim() || !customerName.trim()) {
      setError('Please enter customer name and pickup address.');
      return;
    }
    const normalizedPhone = normalizePhoneToE164(phoneNumber);
    if (!normalizedPhone) {
      setError('Please enter a valid 10-digit mobile number.');
      return;
    }

    setIsSubmitting(true);
    try {
      const uid = resolvedUserId || `acct_${normalizedPhone.replace(/\D/g, '')}`;
      await onEnsureProfile({
        userId: uid,
        name: customerName.trim(),
        phone: normalizedPhone,
        whatsapp: whatsappNumber.trim() || normalizedPhone,
        address: pickupAddress.trim()
      });

      const repairId = `REP-${Math.floor(10000 + Math.random() * 90000)}`;
      const now = Date.now();
      const repairDoc = {
        id: repairId,
        userId: uid,
        brandName: brandName.trim(),
        modelName: modelName.trim(),
        complaintsJson: JSON.stringify(complaints),
        description: description.trim(),
        photoUri: '',
        pickupAddress: pickupAddress.trim(),
        customerName: customerName.trim(),
        phoneNumber: normalizedPhone,
        whatsappNumber: whatsappNumber.trim() || normalizedPhone,
        preferredDate,
        preferredTimeSlot,
        status: 'Booking Received',
        adminNotes: 'Technician assignment pending',
        createdAt: now,
        updatedAt: now
      };

      await setDoc(doc(db, 'repairs', repairId), repairDoc);
      setBookedRepair(repairDoc);
      setBrandName('');
      setModelName('');
      setDeviceSearch('');
      setComplaints([]);
      setDescription('');
    } catch (err) {
      setError(err.message || 'Could not submit repair booking. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div>
      <div className="section-header">
        <div>
          <h1 className="section-title">
            <Wrench size={22} color="#E50914" /> Doorstep Mobile Repair &amp; Pickup
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
            Book a doorstep repair pickup. Synced instantaneously with our technician &amp; mobile
            app dashboard.
          </p>
        </div>
      </div>

      {bookedRepair ? (
        <div className="card" style={{ textAlign: 'center', padding: 34 }}>
          <CheckCircle size={48} color="#10B981" style={{ marginBottom: 12 }} />
          <h2 style={{ color: 'var(--icase-navy)', marginBottom: 6 }}>Repair Pickup Scheduled!</h2>
          <p style={{ fontWeight: 800, color: 'var(--icase-red)', fontSize: 18, marginBottom: 8 }}>
            Booking ID: #{bookedRepair.id}
          </p>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginBottom: 20 }}>
            {bookedRepair.brandName} {bookedRepair.modelName} • {bookedRepair.preferredDate} (
            {bookedRepair.preferredTimeSlot})
          </p>
          <div style={{ display: 'flex', justifyContent: 'center', gap: 12, flexWrap: 'wrap' }}>
            <button className="btn btn-red" onClick={() => onTrackId(bookedRepair.id)}>
              Track Repair Live →
            </button>
            <button className="btn btn-outline" onClick={() => setBookedRepair(null)}>
              Book Another Device
            </button>
          </div>
        </div>
      ) : (
        <form className="card" onSubmit={handleSubmit}>
          {/* Searchable Brand & Model Selector */}
          <div className="form-group">
            <label>Select Device Brand</label>
            <div className="complaint-chips" style={{ marginBottom: 10 }}>
              <button
                type="button"
                className={`complaint-chip ${brandName === '' ? 'selected' : ''}`}
                onClick={() => {
                  setBrandName('');
                  setModelName('');
                  setDropdownOpen(true);
                }}
              >
                All Brands
              </button>
              {MAJOR_DEVICE_BRANDS.map((brand) => (
                <button
                  type="button"
                  key={brand}
                  className={`complaint-chip ${brandName === brand ? 'selected' : ''}`}
                  onClick={() => {
                    setBrandName(brand);
                    setModelName('');
                    setDeviceSearch('');
                    setDropdownOpen(true);
                  }}
                >
                  {brand}
                </button>
              ))}
            </div>
          </div>

          <div className="form-group" style={{ position: 'relative' }}>
            <label>Search &amp; Select Device Model</label>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 8,
                border: '1.5px solid var(--border)',
                borderRadius: 10,
                padding: '8px 12px',
                background: '#F8FAFC',
                cursor: 'pointer'
              }}
              onClick={() => setDropdownOpen((o) => !o)}
            >
              <Search size={16} color="#64748B" />
              <input
                type="text"
                style={{
                  border: 'none',
                  outline: 'none',
                  background: 'transparent',
                  flex: 1,
                  fontSize: 14,
                  color: 'var(--icase-navy)',
                  fontWeight: modelName ? 700 : 400
                }}
                placeholder={
                  brandName
                    ? `Search ${brandName} model (e.g. click to browse list)...`
                    : 'Search phone model (Apple, Samsung, OnePlus, Xiaomi, Realme, Vivo, Oppo, Motorola)...'
                }
                value={dropdownOpen ? deviceSearch : modelName ? `${brandName} — ${modelName}` : deviceSearch}
                onFocus={() => setDropdownOpen(true)}
                onChange={(e) => {
                  setDeviceSearch(e.target.value);
                  setDropdownOpen(true);
                }}
              />
              <ChevronDown size={18} color="#475569" />
            </div>

            {dropdownOpen && (
              <div
                style={{
                  marginTop: 6,
                  maxHeight: 240,
                  overflowY: 'auto',
                  border: '1px solid var(--border)',
                  borderRadius: 12,
                  background: '#FFF',
                  boxShadow: '0 10px 25px rgba(15, 23, 42, 0.12)',
                  zIndex: 30
                }}
              >
                {filteredDevices.length === 0 ? (
                  <div style={{ padding: 14, textAlign: 'center', fontSize: 13.5, color: '#64748B' }}>
                    No preset match.{' '}
                    {deviceSearch.trim() && (
                      <button
                        type="button"
                        className="section-link"
                        onClick={() => {
                          setBrandName(brandName || 'Other');
                          setModelName(deviceSearch.trim());
                          setDropdownOpen(false);
                        }}
                      >
                        Use &ldquo;{deviceSearch.trim()}&rdquo;
                      </button>
                    )}
                  </div>
                ) : (
                  filteredDevices.map((item) => (
                    <button
                      type="button"
                      key={`${item.brand}_${item.model}`}
                      style={{
                        display: 'flex',
                        width: '100%',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '10px 14px',
                        border: 'none',
                        borderBottom: '1px solid #F1F5F9',
                        background:
                          brandName === item.brand && modelName === item.model ? '#EAF2FF' : '#FFF',
                        cursor: 'pointer',
                        textAlign: 'left',
                        fontSize: 14,
                        color: 'var(--icase-navy)',
                        fontWeight: 600
                      }}
                      onClick={() => {
                        setBrandName(item.brand);
                        setModelName(item.model);
                        setDeviceSearch('');
                        setDropdownOpen(false);
                      }}
                    >
                      <span>{item.model}</span>
                      <span style={{ fontSize: 12, color: '#64748B', fontWeight: 500 }}>
                        {item.brand}
                      </span>
                    </button>
                  ))
                )}
              </div>
            )}
          </div>

          <div className="form-group">
            <label>Select Issue(s)</label>
            <div className="complaint-chips">
              {COMPLAINT_OPTIONS.map((item) => (
                <button
                  type="button"
                  key={item}
                  className={`complaint-chip ${complaints.includes(item) ? 'selected' : ''}`}
                  onClick={() => toggleComplaint(item)}
                >
                  {item}
                </button>
              ))}
            </div>
          </div>

          <div className="form-grid">
            <div className="form-group">
              <label>Customer Name</label>
              <input
                className="input"
                placeholder="Full Name"
                value={customerName}
                onChange={(e) => setCustomerName(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Mobile Number</label>
              <input
                className="input"
                placeholder="10-digit mobile or +91"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
              />
            </div>
          </div>

          <div className="form-grid">
            <div className="form-group">
              <label>Preferred Pickup Date</label>
              <select
                className="input"
                value={preferredDate}
                onChange={(e) => setPreferredDate(e.target.value)}
              >
                <option value="Today">Today</option>
                <option value="Tomorrow">Tomorrow</option>
              </select>
            </div>
            <div className="form-group">
              <label>Preferred Time Slot</label>
              <select
                className="input"
                value={preferredTimeSlot}
                onChange={(e) => setPreferredTimeSlot(e.target.value)}
              >
                {TIME_SLOTS.map((slot) => (
                  <option key={slot} value={slot}>
                    {slot}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                gap: 8,
                marginBottom: 6,
                flexWrap: 'wrap'
              }}
            >
              <label style={{ margin: 0 }}>Doorstep Pickup Address</label>
              <button
                type="button"
                className="btn btn-sm"
                style={{ background: '#EAF2FF', color: '#0066CC', fontWeight: 700 }}
                onClick={handleDetectGps}
                disabled={detectingGps}
              >
                <Navigation size={14} />
                {detectingGps ? 'Detecting GPS...' : 'Use GPS / Detect Current Location'}
              </button>
            </div>
            {gpsStatus && (
              <div
                style={{
                  fontSize: 12,
                  fontWeight: 600,
                  color: gpsIsError ? '#E50914' : '#10B981',
                  marginBottom: 6
                }}
              >
                {gpsStatus}
              </div>
            )}
            <textarea
              className="input"
              rows={2}
              placeholder="House / Flat No., Street, Landmark, City, PIN"
              value={pickupAddress}
              onChange={(e) => setPickupAddress(e.target.value)}
            />
          </div>

          {/* Manual Detailed Problem Description kept active at bottom */}
          <div className="form-group">
            <label>Detailed Problem Description (Optional)</label>
            <textarea
              className="input"
              rows={3}
              placeholder="Describe what happened (e.g. cracked glass, touch not working, phone gets hot while charging)..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              background: '#F8FAFC',
              padding: 12,
              borderRadius: 10,
              fontSize: 13,
              color: '#475569',
              marginBottom: 16
            }}
          >
            <Info size={18} color="#0066CC" />
            <span>
              Repair diagnosis is free. Exact quote is shared after technician inspection of your
              device.
            </span>
          </div>

          {error && (
            <div style={{ color: '#E50914', fontWeight: 700, fontSize: 13.5, marginBottom: 12 }}>
              {error}
            </div>
          )}

          <button type="submit" className="btn btn-navy" disabled={isSubmitting}>
            {isSubmitting ? 'Scheduling Pickup...' : 'Confirm Repair & Pickup Booking'}
          </button>
        </form>
      )}
    </div>
  );
}
