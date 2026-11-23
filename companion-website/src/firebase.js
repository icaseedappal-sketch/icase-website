import { initializeApp } from 'firebase/app';
import {
  getAuth,
  GoogleAuthProvider,
  signInWithPopup,
  signOut
} from 'firebase/auth';
import {
  collection,
  doc,
  getDocs,
  getFirestore,
  limit,
  query,
  setDoc
} from 'firebase/firestore';
import { DEFAULT_SETTINGS, INITIAL_PRODUCTS } from './data/constants';

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

const app = initializeApp(FIREBASE_CONFIG);
export const auth = getAuth(app);
export const db = getFirestore(app, FIRESTORE_DATABASE_ID);
export const googleProvider = new GoogleAuthProvider();

export {
  signInWithPopup,
  signOut
};

export async function seedFirestoreIfEmpty() {
  try {
    const productSnap = await getDocs(query(collection(db, 'products'), limit(1)));
    if (productSnap.empty) {
      await Promise.all(
        INITIAL_PRODUCTS.map((product) =>
          setDoc(doc(db, 'products', product.id), {
            ...product,
            updatedAt: Date.now()
          })
        )
      );
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
  } catch (err) {
    console.warn('Initial Firestore seed skipped or failed:', err);
  }
}
