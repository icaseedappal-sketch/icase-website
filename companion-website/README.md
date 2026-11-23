# iCASE Companion Website

Responsive React + Vite companion web storefront, repair booking portal, live order tracker, and admin dashboard synced in real time with the **iCASE Android app** via Firebase Cloud Firestore & Firebase Authentication.

## Shared Firebase Backend
- **Project ID:** `gen-lang-client-0690121790`
- **Firestore Database ID:** `ai-studio-android-icase-44842779-78b7-4b24-894c-745423e44d91`
- **Real-time Collections Synced:**
  - `products` — Catalog items, pricing, stock, popularity & 1-hour eligibility
  - `orders` — Customer orders placed on Web or Android (`userId`-isolated history + full Admin view)
  - `repairs` — Doorstep repair bookings synced between Web, Android & Admin
  - `users` — Synchronized customer profiles across Web & Android
  - `settings` — Dynamic store contact (`+91 9961454059`, `icaseedappal@gmail.com`), delivery charges, 1-hour toggle, store online switch & admin PIN

## Run Locally
```bash
cd companion-website
npm install
npm run dev
```

## Production Build
```bash
cd companion-website
npm run build
```

## Firebase Authentication Notes
- Add your local/deployed domain (e.g., `localhost`) under **Firebase Console → Authentication → Settings → Authorized domains** for Google Sign-In & Phone Auth reCAPTCHA.
- Ensure **Google** and **Phone** providers are enabled under **Authentication → Sign-in method**.
