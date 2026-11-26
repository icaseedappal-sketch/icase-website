import { getApp, getApps, initializeApp } from 'firebase/app';
import {
  getAuth,
  GoogleAuthProvider,
  signInWithPopup,
  signOut
} from 'firebase/auth';
import {
  collection,
  doc,
  getDoc,
  getDocs,
  getFirestore,
  limit,
  query,
  setDoc
} from 'firebase/firestore';
import {
  CATALOG_SEEDED_KEY,
  DEFAULT_SETTINGS,
  DELETED_PRODUCTS_KEY,
  INITIAL_PRODUCTS,
  SCREEN_GUARD_CONFIG_KEY,
  parseDeletedProductIds,
  serializeScreenGuardConfig
} from './data/constants';

// Mirrors app/google-services.json & app/src/main/res/values/firebase_applet_config.xml
export const FIREBASE_CONFIG = {
  apiKey: 'AIzaSyCOaOclELFAYCotK9JnKHtcSO6yzrmNJHc',
  authDomain: 'gen-lang-client-0690121790.firebaseapp.com',
  projectId: 'gen-lang-client-0690121790',
  storageBucket: 'gen-lang-client-0690121790.firebasestorage.app',
  messagingSenderId: '848117223673',
  appId: '1:848117223673:web:icase-companion-web'
};

// Exact named Firestore database shared with the iCASE Android app
export const FIRESTORE_DATABASE_ID =
  'ai-studio-android-icase-44842779-78b7-4b24-894c-745423e44d91';

let app = null;
let auth = null;
let db = null;
let googleProvider = null;

try {
  app = getApps().length ? getApp() : initializeApp(FIREBASE_CONFIG);
  auth = getAuth(app);
  db = getFirestore(app, FIRESTORE_DATABASE_ID);
  googleProvider = new GoogleAuthProvider();
  googleProvider.setCustomParameters({ prompt: 'select_account' });
} catch (err) {
  console.warn('Firebase initialized in offline fallback mode:', err);
}

export {
  app,
  auth,
  db,
  googleProvider,
  signInWithPopup,
  signOut
};

export async function seedFirestoreIfEmpty() {
  if (!db) return false;
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
    console.warn('Initial Firestore seed skipped or offline:', err);
    return false;
  }
}
