require('dotenv').config();
const express  = require('express');
const mongoose = require('mongoose');
const cors     = require('cors');
const authRoutes = require('./routes/auth');
const spotifyRoutes = require('./routes/spotify');
const saavnRoutes = require('./routes/saavn');

const app  = express();
const PORT = process.env.PORT || 3000;

// ── Middleware ───────────────────────────────────────────────────────────────
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// ── Routes ───────────────────────────────────────────────────────────────────
app.use('/api', authRoutes);
app.use('/api/spotify', spotifyRoutes);
app.use('/api/saavn', saavnRoutes);

// ── Root endpoint ─────────────────────────────────────────────────────────────
app.get('/', (req, res) => {
  res.json({ message: '🎵 Viora Music Backend — Running!', version: '1.0.0' });
});

// ── Connect MongoDB Atlas ──────────────────────────────────────────────────
if (process.env.MONGO_URI && !process.env.MONGO_URI.includes('CHANGE_ME')) {
  mongoose.connect(process.env.MONGO_URI)
    .then(() => {
      console.log('✅ MongoDB Atlas connected successfully');
    })
    .catch(err => {
      console.warn('⚠️ MongoDB Atlas connection error:', err.message);
    });
} else {
  console.warn('⚠️ MongoDB: Please replace CHANGE_ME with your actual MongoDB username and password in backend/.env');
}

// ── Start Express Server ────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`🚀 Viora Music API running on port ${PORT}`);
});
