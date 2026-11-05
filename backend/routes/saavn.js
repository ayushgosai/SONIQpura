const express = require('express');
const crypto = require('crypto');
const router = express.Router();

const BROWSER_HEADERS = {
  'User-Agent': 'Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36',
  Accept: 'application/json',
};

// JioSaavn ships media URLs with the long-standing 3DES/ECB obfuscation.
function decryptMediaUrl(encrypted) {
  const cipher = crypto.createDecipheriv(
    'des-ede3',
    Buffer.from('383465913834659138346591'),
    null
  );
  return Buffer.concat([
    cipher.update(Buffer.from(encrypted, 'base64')),
    cipher.final(),
  ]).toString('utf8');
}

function decodeEntities(value = '') {
  return String(value)
    .replace(/&quot;/g, '"')
    .replace(/&#039;/g, "'")
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>');
}

function largeImage(url = '') {
  return url.replace(/([_-])\d+x\d+\./, '$1' + '500x500.');
}

// GET /api/saavn/search?q=kesariya&limit=30
router.get('/search', async (req, res) => {
  const query = String(req.query.q || '').trim();
  const limit = Math.min(Math.max(parseInt(req.query.limit, 10) || 30, 1), 50);

  if (!query) {
    return res.status(400).json({ success: false, message: 'Query parameter q is required.' });
  }

  try {
    const url =
      'https://www.jiosaavn.com/api.php?__call=search.getResults' +
      `&q=${encodeURIComponent(query)}` +
      '&_format=json&_marker=0&api_version=4&ctx=web6dot0' +
      `&n=${limit}`;

    const response = await fetch(url, { headers: BROWSER_HEADERS });
    if (!response.ok) {
      throw new Error(`JioSaavn search failed (${response.status})`);
    }

    const data = await response.json();
    const items = data.results || [];

    const songs = items
      .map((item) => {
        const info = item.more_info || {};
        let streamUrl = '';

        if (info.encrypted_media_url) {
          try {
            streamUrl = decryptMediaUrl(info.encrypted_media_url);
            if (String(info['320kbps']).toLowerCase() === 'true') {
              streamUrl = streamUrl.replace('_96.mp4', '_320.mp4');
            }
          } catch (error) {
            streamUrl = '';
          }
        }

        const primaryArtists = ((info.artistMap && info.artistMap.primary_artists) || [])
          .map((artist) => artist && artist.name)
          .filter(Boolean);

        const subtitleParts = String(item.subtitle || '').split(' - ');

        return {
          id: item.id,
          title: decodeEntities(item.title || ''),
          artist: primaryArtists.length
            ? primaryArtists.map(decodeEntities).join(', ')
            : decodeEntities(subtitleParts[0] || ''),
          album: decodeEntities(info.album || subtitleParts.slice(1).join(' - ') || ''),
          image: largeImage(item.image || ''),
          durationMs: (parseInt(info.duration, 10) || 0) * 1000,
          streamUrl,
          permaUrl: item.perma_url || '',
          language: item.language || '',
        };
      })
      .filter((song) => song.id && song.streamUrl);

    return res.json({ success: true, songs });
  } catch (error) {
    console.error('JioSaavn search error:', error.message);
    return res.status(502).json({ success: false, message: 'Full song search is unavailable right now.' });
  }
});

module.exports = router;
