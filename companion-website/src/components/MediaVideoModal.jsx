import React, { useState } from 'react';
import { X, ExternalLink, Film } from 'lucide-react';
import { isYouTubeUrl, parseYouTubeEmbedUrl } from '../data/constants';

export default function MediaVideoModal({ isOpen, title, url, onClose }) {
  const [videoErr, setVideoErr] = useState(false);

  if (!isOpen || !url) return null;

  const cleanUrl = String(url || '').trim();
  const ytEmbed = parseYouTubeEmbedUrl(cleanUrl);
  const ytRaw = isYouTubeUrl(cleanUrl);
  const isDataOrHttp = /^(https?:\/\/|data:video|blob:|\/)/i.test(cleanUrl);

  return (
    <div className="overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 680 }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <strong style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Film size={17} /> {title || 'Product Video Preview'}
          </strong>
          <button
            type="button"
            className="btn btn-sm"
            style={{ background: 'rgba(255,255,255,0.14)', color: '#FFF' }}
            onClick={onClose}
          >
            <X size={16} />
          </button>
        </div>
        <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {ytEmbed ? (
            <div
              style={{
                position: 'relative',
                width: '100%',
                aspectRatio: '16 / 9',
                borderRadius: 12,
                overflow: 'hidden',
                background: '#0B1728'
              }}
            >
              <iframe
                src={ytEmbed}
                title={title || 'YouTube Preview'}
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
                style={{ width: '100%', height: '100%', border: 'none' }}
              />
            </div>
          ) : ytRaw ? (
            <div
              style={{
                padding: 20,
                borderRadius: 12,
                background: '#F8FAFC',
                border: '1px solid var(--border)',
                textAlign: 'center'
              }}
            >
              <p style={{ fontSize: 13.5, color: 'var(--icase-navy)', fontWeight: 700, marginBottom: 10 }}>
                Open this YouTube video link directly:
              </p>
              <a
                href={cleanUrl}
                target="_blank"
                rel="noreferrer"
                className="btn btn-navy btn-sm"
                style={{ textDecoration: 'none' }}
              >
                <ExternalLink size={14} /> Open on YouTube
              </a>
            </div>
          ) : (
            <>
              <video
                key={cleanUrl}
                src={cleanUrl}
                controls
                autoPlay
                playsInline
                style={{ width: '100%', maxHeight: 380, borderRadius: 12, background: '#0B1728' }}
                onError={() => setVideoErr(true)}
              />
              {videoErr && (
                <div style={{ color: 'var(--icase-red)', fontSize: 13, fontWeight: 700 }}>
                  Unable to play this video inside the browser player. Try opening the link directly.
                </div>
              )}
            </>
          )}

          {isDataOrHttp && !cleanUrl.startsWith('data:video') && (
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <a
                href={cleanUrl}
                target="_blank"
                rel="noreferrer"
                className="btn btn-outline btn-sm"
                style={{ textDecoration: 'none' }}
              >
                <ExternalLink size={14} /> Open Video Link
              </a>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
