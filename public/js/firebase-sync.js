import { getApp, getApps, initializeApp } from 'https://www.gstatic.com/firebasejs/11.4.0/firebase-app.js';
import {
  getAuth,
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithPopup,
  signOut
} from 'https://www.gstatic.com/firebasejs/11.4.0/firebase-auth.js';
import {
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  getFirestore,
  limit,
  onSnapshot,
  query,
  setDoc,
  updateDoc,
  where
} from 'https://www.gstatic.com/firebasejs/11.4.0/firebase-firestore.js';
import {
  CATALOG_SEEDED_KEY,
  DEFAULT_SETTINGS,
  DELETED_PRODUCTS_KEY,
  INITIAL_PRODUCTS,
  SCREEN_GUARD_CONFIG_KEY,
  parseDeletedProductIds,
  serializeDeletedProductIds,
  serializeScreenGuardConfig
} from './data.js';

// Firebase Web Configuration synchronized with app/google-services.json
// and app/src/main/res/values/firebase_applet_config.xml
export const FIREBASE_CONFIG = {
  apiKey: 'AIzaSyCOaOclELFAYCotK9JnKHtcSO6yzrmNJHc',
  authDomain: 'gen-lang-client-0690121790.firebaseapp.com',
  projectId: 'gen-lang-client-0690121790',
  storageBucket: 'gen-lang-client-0690121790.firebasestorage.app',
  messagingSenderId: '848117223673',
  appId: '1:848117223673:web:icase-companion-web'
};

// Web OAuth Client ID (client_type: 3) & Android App ID from google-services.json
export const WEB_OAUTH_CLIENT_ID =
  '848117223673-h20tojhtrmok91k8mc4jlcnguludff58.apps.googleusercontent.com';
export const ANDROID_APP_ID = '1:848117223673:android:d2891c51e5fa1ea78ad228';

// Exact named Cloud Firestore database shared in real time with the iCASE Android app & Admin Panel
export const FIRESTORE_DATABASE_ID =
  'ai-studio-android-icase-44842779-78b7-4b24-894c-745423e44d91';

export const app = getApps().length ? getApp() : initializeApp(FIREBASE_CONFIG);
export const auth = getAuth(app);
export const db = getFirestore(app, FIRESTORE_DATABASE_ID);
export const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({ prompt: 'select_account' });

export async function seedFirestoreIfEmpty() {
  try {
    const [seededSnap, deletedSnap] = await Promise.all([
      getDoc(doc(db, 'settings', CATALOG_SEEDED_KEY)),
      getDoc(doc(db, 'settings', DELETED_PRODUCTS_KEY))
    ]);

    const alreadySeeded = seededSnap.exists() && seededSnap.data()?.value === 'true';
    const deletedIds = deletedSnap.exists()
      ? parseDeletedProductIds(deletedSnap.data()?.value)
      : [];

    if (!alreadySeeded) {
      const productSnap = await getDocs(query(collection(db, 'products'), limit(1)));
      if (productSnap.empty) {
        const deletedSet = new Set(deletedIds);
        const toSeed = INITIAL_PRODUCTS.filter((p) => !deletedSet.has(p.id));
        if (toSeed.length > 0) {
          await Promise.all(
            toSeed.map((product) =>
              setDoc(doc(db, 'products', product.id), {
                ...product,
                updatedAt: Date.now()
              })
            )
          );
        }
      }
      await setDoc(doc(db, 'settings', CATALOG_SEEDED_KEY), {
        key: CATALOG_SEEDED_KEY,
        value: 'true',
        updatedAt: Date.now()
      });
    }

    const settingSnap = await getDocs(query(collection(db, 'settings'), limit(1)));
    if (settingSnap.empty) {
      await Promise.all(
        Object.entries(DEFAULT_SETTINGS).map(([key, value]) =>
          setDoc(doc(db, 'settings', key), {
            key,
            value: String(value),
            updatedAt: Date.now()
          })
        )
      );
    }

    const guardCfgSnap = await getDoc(doc(db, 'settings', SCREEN_GUARD_CONFIG_KEY));
    if (!guardCfgSnap.exists()) {
      await setDoc(doc(db, 'settings', SCREEN_GUARD_CONFIG_KEY), {
        key: SCREEN_GUARD_CONFIG_KEY,
        value: serializeScreenGuardConfig(),
        updatedAt: Date.now()
      });
    }
    return true;
  } catch (err) {
    console.warn('Firestore initial seed skipped or offline:', err);
    return false;
  }
}

export function subscribePublicStoreData({ onProducts, onSettings, onSyncStatus }) {
  const unsubProducts = onSnapshot(
    collection(db, 'products'),
    (snap) => {
      onSyncStatus?.('connected');
      const list = snap.docs.map((d) => ({ id: d.id, ...d.data() }));
      onProducts(list);
    },
    () => onSyncStatus?.('offline')
  );

  const unsubSettings = onSnapshot(
    collection(db, 'settings'),
    (snap) => {
      onSyncStatus?.('connected');
      const next = { ...DEFAULT_SETTINGS };
      snap.docs.forEach((d) => {
        const data = d.data();
        const key = data.key || d.id;
        if (key) next[key] = String(data.value ?? '');
      });
      onSettings(next);
    },
    () => onSyncStatus?.('offline')
  );

  return () => {
    unsubProducts();
    unsubSettings();
  };
}

export function subscribePublicApprovedReviews(onReviews, onError) {
  const q = query(
    collection(db, 'reviews'),
    where('reviewType', '==', 'PRODUCT'),
    where('isApproved', '==', true)
  );
  return onSnapshot(
    q,
    (snap) => {
      const list = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .filter((r) => r.reviewType === 'PRODUCT' && r.isApproved === true)
        .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
      onReviews(list);
    },
    (err) => onError?.(err)
  );
}

export function subscribeAuthState(callback) {
  return onAuthStateChanged(auth, callback);
}

export function signInWithGooglePopup() {
  return signInWithPopup(auth, googleProvider);
}

export function signOutFirebase() {
  return signOut(auth);
}

export function subscribeUserScopedData(uid, { onProfile, onOrders, onRepairs, onError }) {
  if (!uid) return () => {};

  const unsubUser = onSnapshot(
    doc(db, 'users', uid),
    (snap) => {
      if (snap.exists()) {
        onProfile(snap.data());
      }
    },
    (err) => onError?.(err)
  );

  const unsubOrders = onSnapshot(
    query(collection(db, 'orders'), where('userId', '==', uid)),
    (snap) => {
      const list = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
      onOrders(list);
    },
    (err) => onError?.(err)
  );

  const unsubRepairs = onSnapshot(
    query(collection(db, 'repairs'), where('userId', '==', uid)),
    (snap) => {
      const list = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
      onRepairs(list);
    },
    (err) => onError?.(err)
  );

  return () => {
    unsubUser();
    unsubOrders();
    unsubRepairs();
  };
}

export function subscribeAdminData({ onOrders, onRepairs, onReviews, onError }) {
  const unsubOrders = onSnapshot(
    collection(db, 'orders'),
    (snap) => {
      const list = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
      onOrders(list);
    },
    (err) => onError?.(err)
  );

  const unsubRepairs = onSnapshot(
    collection(db, 'repairs'),
    (snap) => {
      const list = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
      onRepairs(list);
    },
    (err) => onError?.(err)
  );

  const unsubReviews = onSnapshot(
    collection(db, 'reviews'),
    (snap) => {
      const list = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) => Number(b.createdAt || 0) - Number(a.createdAt || 0));
      onReviews?.(list);
    },
    (err) => onError?.(err)
  );

  return () => {
    unsubOrders();
    unsubRepairs();
    unsubReviews();
  };
}

export function subscribeTrackingId(trackingId, { onUpdate, onError }) {
  const cleaned = String(trackingId || '').trim().replace(/^#/, '').toUpperCase();
  if (!cleaned) return () => {};

  let orderDoc = null;
  let repairDoc = null;
  let orderChecked = false;
  let repairChecked = false;

  function publish() {
    if (orderDoc) {
      onUpdate({ type: 'ORDER', data: orderDoc });
    } else if (repairDoc) {
      onUpdate({ type: 'REPAIR', data: repairDoc });
    } else if (orderChecked && repairChecked) {
      onUpdate({ type: 'NOT_FOUND', data: null });
    }
  }

  const unsubOrder = onSnapshot(
    doc(db, 'orders', cleaned),
    (snap) => {
      orderChecked = true;
      orderDoc = snap.exists() ? { id: snap.id, ...snap.data() } : null;
      publish();
    },
    (err) => {
      orderChecked = true;
      onError?.(err);
      publish();
    }
  );

  const unsubRepair = onSnapshot(
    doc(db, 'repairs', cleaned),
    (snap) => {
      repairChecked = true;
      repairDoc = snap.exists() ? { id: snap.id, ...snap.data() } : null;
      publish();
    },
    (err) => {
      repairChecked = true;
      onError?.(err);
      publish();
    }
  );

  return () => {
    unsubOrder();
    unsubRepair();
  };
}

export async function saveUserProfileToCloud(profile) {
  if (!profile?.userId) return;
  await setDoc(
    doc(db, 'users', profile.userId),
    {
      userId: profile.userId,
      name: profile.name || '',
      email: profile.email || '',
      phone: profile.phone || '',
      whatsapp: profile.whatsapp || '',
      address: profile.address || '',
      wishlistIds: Array.isArray(profile.wishlistIds) ? profile.wishlistIds : [],
      isGoogleSignedIn: Boolean(profile.isGoogleSignedIn),
      updatedAt: Date.now()
    },
    { merge: true }
  );
}

export async function createOrderInCloud(orderPayload) {
  await setDoc(doc(db, 'orders', orderPayload.id), orderPayload);
  return orderPayload;
}

export async function createRepairInCloud(repairPayload) {
  await setDoc(doc(db, 'repairs', repairPayload.id), repairPayload);
  return repairPayload;
}

export async function updateOrderStatusInCloud(orderId, status, statusNotes) {
  await updateDoc(doc(db, 'orders', orderId), {
    status,
    statusNotes: statusNotes || '',
    updatedAt: Date.now()
  });
}

export async function deleteOrderInCloud(orderId) {
  await deleteDoc(doc(db, 'orders', orderId));
}

export async function updateRepairStatusInCloud(repairId, status, adminNotes) {
  await updateDoc(doc(db, 'repairs', repairId), {
    status,
    adminNotes: adminNotes || '',
    updatedAt: Date.now()
  });
}

export async function rescheduleRepairInCloud(repairId, preferredDate, preferredTimeSlot, adminNotes) {
  await updateDoc(doc(db, 'repairs', repairId), {
    preferredDate,
    preferredTimeSlot,
    adminNotes: adminNotes || '',
    updatedAt: Date.now()
  });
}

export async function deleteRepairInCloud(repairId) {
  await deleteDoc(doc(db, 'repairs', repairId));
}

export async function saveProductInCloud(productPayload, currentDeletedIds = []) {
  const now = Date.now();
  const tasks = [
    setDoc(doc(db, 'products', productPayload.id), {
      ...productPayload,
      updatedAt: now
    }),
    setDoc(doc(db, 'settings', CATALOG_SEEDED_KEY), {
      key: CATALOG_SEEDED_KEY,
      value: 'true',
      updatedAt: now
    })
  ];

  const existingDeleted = parseDeletedProductIds(currentDeletedIds);
  if (existingDeleted.includes(productPayload.id)) {
    const nextDeleted = existingDeleted.filter((id) => id !== productPayload.id);
    tasks.push(
      setDoc(doc(db, 'settings', DELETED_PRODUCTS_KEY), {
        key: DELETED_PRODUCTS_KEY,
        value: serializeDeletedProductIds(nextDeleted),
        updatedAt: now
      })
    );
  }
  await Promise.all(tasks);
}

export async function deleteProductInCloud(productId, currentDeletedIds = []) {
  const now = Date.now();
  let remoteDeleted = parseDeletedProductIds(currentDeletedIds);
  try {
    const snap = await getDoc(doc(db, 'settings', DELETED_PRODUCTS_KEY));
    if (snap.exists()) {
      remoteDeleted = parseDeletedProductIds([
        ...remoteDeleted,
        ...parseDeletedProductIds(snap.data()?.value)
      ]);
    }
  } catch {}
  const nextDeleted = parseDeletedProductIds([...remoteDeleted, productId]);
  await Promise.all([
    setDoc(doc(db, 'settings', DELETED_PRODUCTS_KEY), {
      key: DELETED_PRODUCTS_KEY,
      value: serializeDeletedProductIds(nextDeleted),
      updatedAt: now
    }),
    setDoc(doc(db, 'settings', CATALOG_SEEDED_KEY), {
      key: CATALOG_SEEDED_KEY,
      value: 'true',
      updatedAt: now
    }),
    deleteDoc(doc(db, 'products', productId))
  ]);
}

export async function saveSettingInCloud(key, value) {
  await setDoc(doc(db, 'settings', key), {
    key,
    value: String(value),
    updatedAt: Date.now()
  });
}

export async function lookupReferenceInCloud(referenceId) {
  const cleaned = String(referenceId || '').trim().replace(/^#/, '').toUpperCase();
  if (!cleaned) return null;

  const orderSnap = await getDoc(doc(db, 'orders', cleaned));
  if (orderSnap.exists()) {
    return { type: 'ORDER', data: { id: orderSnap.id, ...orderSnap.data() } };
  }

  const repairSnap = await getDoc(doc(db, 'repairs', cleaned));
  if (repairSnap.exists()) {
    return { type: 'REPAIR', data: { id: repairSnap.id, ...repairSnap.data() } };
  }

  return null;
}

export async function createReviewInCloud(reviewPayload) {
  const enforced = {
    ...reviewPayload,
    isApproved: reviewPayload.reviewType === 'PRODUCT' ? Boolean(reviewPayload.isApproved) : false
  };
  await setDoc(doc(db, 'reviews', enforced.id), enforced);
  return enforced;
}

export async function updateReviewInCloud(reviewId, updates) {
  await updateDoc(doc(db, 'reviews', reviewId), {
    ...updates,
    updatedAt: Date.now()
  });
}

export async function deleteReviewInCloud(reviewId) {
  await deleteDoc(doc(db, 'reviews', reviewId));
}
