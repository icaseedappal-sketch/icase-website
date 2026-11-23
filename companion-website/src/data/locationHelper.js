export function detectCurrentLocation() {
  return new Promise((resolve, reject) => {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      reject(new Error('GPS / Geolocation is not supported in this browser.'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        const coordsText = `${lat.toFixed(5)}, ${lon.toFixed(5)}`;

        try {
          const resp = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}`,
            { headers: { Accept: 'application/json' } }
          );
          if (resp.ok) {
            const data = await resp.json();
            if (data && data.display_name) {
              resolve({
                latitude: lat,
                longitude: lon,
                formattedAddress: `${data.display_name} (GPS: ${coordsText})`,
                isGeocoded: true
              });
              return;
            }
          }
        } catch {
          // Fall back to exact coordinates without fabricating any city
        }

        resolve({
          latitude: lat,
          longitude: lon,
          formattedAddress: `GPS Coordinates: ${coordsText}`,
          isGeocoded: false
        });
      },
      (err) => {
        if (err.code === 1) {
          reject(new Error('Location permission denied. Please allow location access or enter your address manually.'));
        } else if (err.code === 2) {
          reject(new Error('Live GPS location unavailable right now. Please enter your address manually.'));
        } else {
          reject(new Error('Location detection timed out. Please retry or enter your address manually.'));
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  });
}
