const express = require('express');
const router = express.Router();

let cachedToken = null;
let tokenExpiresAt = 0;

async function getAccessToken() {
  if (cachedToken && Date.now() < tokenExpiresAt - 60_000) {
    return cachedToken;
  }

  const clientId = process.env.SPOTIFY_CLIENT_ID;
  const clientSecret = process.env.SPOTIFY_CLIENT_SECRET;
  if (!clientId || !clientSecret) {
    throw new Error('Spotify credentials are not configured in .env');
  }

  const basic = Buffer.from(`${clientId}:${clientSecret}`).toString('base64');
  const response = await fetch('https://accounts.spotify.com/api/token', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      Authorization: `Basic ${basic}`,
    },
    body: 'grant_type=client_credentials',
  });

  if (!response.ok) {
    throw new Error(`Spotify token request failed (${response.status})`);
  }

  const data = await response.json();
  cachedToken = data.access_token;
  tokenExpiresAt = Date.now() + (data.expires_in || 3600) * 1000;
  return cachedToken;
}

const SPOTIFY_PAGE_MAX = 10;

// GET /api/spotify/search?q=kesariya&limit=30
router.get('/search', async (req, res) => {
  const query = String(req.query.q || '').trim();
  const limit = Math.min(Math.max(parseInt(req.query.limit, 10) || 30, 1), 50);

  if (!query) {
    return res.status(400).json({ success: false, message: 'Query parameter q is required.' });
  }

  try {
    const token = await getAccessToken();
    const tracks = [];

    // Spotify rejects limit > 10 for search, so page through in chunks of 10.
    for (let offset = 0; offset < limit; offset += SPOTIFY_PAGE_MAX) {
      const pageLimit = Math.min(SPOTIFY_PAGE_MAX, limit - offset);
      const url =
        'https://api.spotify.com/v1/search' +
        `?q=${encodeURIComponent(query)}` +
        '&type=track' +
        `&limit=${pageLimit}` +
        `&offset=${offset}` +
        '&market=IN';

      const response = await fetch(url, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!response.ok) {
        throw new Error(`Spotify search failed (${response.status})`);
      }

      const data = await response.json();
      const items = (data.tracks && data.tracks.items) || [];

      tracks.push(
        ...items
          .filter((track) => track && track.id)
          .map((track) => ({
            id: track.id,
            name: track.name,
            artists: (track.artists || []).map((artist) => artist.name).join(', '),
            album: (track.album && track.album.name) || '',
            image: (track.album && track.album.images && track.album.images[0] && track.album.images[0].url) || '',
            durationMs: track.duration_ms || 0,
            uri: track.uri,
            spotifyUrl: (track.external_urls && track.external_urls.spotify) || `https://open.spotify.com/track/${track.id}`,
          }))
      );

      if (items.length < pageLimit) break;
    }

    return res.json({ success: true, tracks });
  } catch (error) {
    console.error('Spotify search error:', error.message);
    return res.status(502).json({ success: false, message: 'Spotify search is unavailable right now.' });
  }
});

module.exports = router;
