const express    = require('express');
const bcrypt     = require('bcryptjs');
const jwt        = require('jsonwebtoken');
const mongoose   = require('mongoose');
const router     = express.Router();

const User       = require('../models/User');
const Otp        = require('../models/Otp');
const { generateOtp }    = require('../utils/otpUtils');
const { sendOtpEmail }   = require('../utils/mailer');

const OTP_EXPIRY_MS      = (parseInt(process.env.OTP_EXPIRY_MINUTES) || 5) * 60 * 1000;
const RESEND_COOLDOWN_MS = (parseInt(process.env.OTP_RESEND_COOLDOWN_SECONDS) || 45) * 1000;
const MAX_OTP_ATTEMPTS   = 5;

// In-memory fallback stores (active when MongoDB is connecting or credentials are being set)
const memUsers = new Map();
const memOtps  = new Map();

function isMongoConnected() {
  return mongoose.connection.readyState === 1;
}

// ─────────────────────────────────────────────────────────────────────────────
// POST /api/register
// Body: { name, email, password, profilePhotoUri, birthdate, age, gender }
// Creates user (unverified), sends real OTP to email inbox
// ─────────────────────────────────────────────────────────────────────────────
router.post('/register', async (req, res) => {
  try {
    const { name, email, password, profilePhotoUri, birthdate, age, gender } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json({ success: false, message: 'Name, email and password are required.' });
    }

    const normalizedEmail = email.trim().toLowerCase();

    // Validate email format
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
      return res.status(400).json({ success: false, message: 'Invalid email address.' });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 12);
    const code = generateOtp();

    if (isMongoConnected()) {
      const existingUser = await User.findOne({ email: normalizedEmail });
      if (existingUser && existingUser.isEmailVerified) {
        return res.status(409).json({ success: false, message: 'This email is already registered. Please login.' });
      }

      if (existingUser) {
        existingUser.name            = name.trim();
        existingUser.password        = hashedPassword;
        if (profilePhotoUri) existingUser.profilePhotoUri = profilePhotoUri;
        if (birthdate)       existingUser.birthdate       = birthdate;
        if (age)             existingUser.age             = age;
        if (gender)          existingUser.gender          = gender;
        await existingUser.save();
      } else {
        await User.create({
          name:            name.trim(),
          email:           normalizedEmail,
          password:        hashedPassword,
          isEmailVerified: false,
          profilePhotoUri: profilePhotoUri || null,
          birthdate:       birthdate || null,
          age:             age || null,
          gender:          gender || null
        });
      }

      await Otp.deleteMany({ email: normalizedEmail });
      await Otp.create({
        email:     normalizedEmail,
        code,
        expiresAt: new Date(Date.now() + OTP_EXPIRY_MS),
        attempts:  0
      });
    } else {
      const existing = memUsers.get(normalizedEmail);
      if (existing && existing.isEmailVerified) {
        return res.status(409).json({ success: false, message: 'This email is already registered. Please login.' });
      }
      memUsers.set(normalizedEmail, {
        _id:             'mem_' + Date.now(),
        name:            name.trim(),
        email:           normalizedEmail,
        password:        hashedPassword,
        isEmailVerified: false,
        profilePhotoUri: profilePhotoUri || null,
        birthdate:       birthdate || null,
        age:             age || null,
        gender:          gender || null,
        providerId:      'password'
      });
      memOtps.set(normalizedEmail, {
        code,
        expiresAt: new Date(Date.now() + OTP_EXPIRY_MS),
        attempts:  0,
        createdAt: new Date()
      });
    }

    // Send REAL OTP to user's inbox
    await sendOtpEmail(normalizedEmail, name.trim(), code);

    return res.json({
      success: true,
      message: 'OTP sent to your email inbox. Please check and verify.'
    });

  } catch (err) {
    console.error('[register]', err);
    return res.status(500).json({ success: false, message: 'Server error: ' + err.message });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// POST /api/verify-otp
// Body: { email, otp }
// Verifies OTP, marks user as verified, returns JWT token
// ─────────────────────────────────────────────────────────────────────────────
router.post('/verify-otp', async (req, res) => {
  try {
    const { email, otp } = req.body;
    if (!email || !otp) {
      return res.status(400).json({ success: false, message: 'Email and OTP are required.' });
    }

    const normalizedEmail = email.trim().toLowerCase();

    if (isMongoConnected()) {
      const otpRecord = await Otp.findOne({ email: normalizedEmail });

      if (!otpRecord) {
        return res.status(400).json({ success: false, message: 'No pending OTP found. Please request a new one.' });
      }

      if (new Date() > otpRecord.expiresAt) {
        await Otp.deleteOne({ _id: otpRecord._id });
        return res.status(400).json({ success: false, message: 'OTP expired (valid for 5 minutes). Please request a new one.' });
      }

      if (otpRecord.attempts >= MAX_OTP_ATTEMPTS) {
        await Otp.deleteOne({ _id: otpRecord._id });
        return res.status(400).json({ success: false, message: 'Too many incorrect attempts. Please request a new OTP.' });
      }

      if (otpRecord.code !== otp.trim()) {
        otpRecord.attempts += 1;
        await otpRecord.save();
        const left = MAX_OTP_ATTEMPTS - otpRecord.attempts;
        return res.status(400).json({ success: false, message: `Incorrect OTP. ${left} attempt(s) remaining.` });
      }

      await Otp.deleteOne({ _id: otpRecord._id });
      const user = await User.findOneAndUpdate(
        { email: normalizedEmail },
        { isEmailVerified: true },
        { new: true }
      );

      if (!user) {
        return res.status(404).json({ success: false, message: 'User not found.' });
      }

      const token = jwt.sign(
        { uid: user._id.toString(), email: user.email },
        process.env.JWT_SECRET,
        { expiresIn: '30d' }
      );

      return res.json({
        success: true,
        message: 'Email verified successfully!',
        token,
        user: {
          uid:              user._id.toString(),
          name:             user.name,
          email:            user.email,
          isEmailVerified:  user.isEmailVerified,
          profilePhotoUri:  user.profilePhotoUri,
          birthdate:        user.birthdate,
          age:              user.age,
          gender:           user.gender,
          providerId:       user.providerId
        }
      });
    } else {
      const otpRecord = memOtps.get(normalizedEmail);
      if (!otpRecord) {
        return res.status(400).json({ success: false, message: 'No pending OTP found. Please request a new one.' });
      }

      if (new Date() > otpRecord.expiresAt) {
        memOtps.delete(normalizedEmail);
        return res.status(400).json({ success: false, message: 'OTP expired (valid for 5 minutes). Please request a new one.' });
      }

      if (otpRecord.attempts >= MAX_OTP_ATTEMPTS) {
        memOtps.delete(normalizedEmail);
        return res.status(400).json({ success: false, message: 'Too many incorrect attempts. Please request a new OTP.' });
      }

      if (otpRecord.code !== otp.trim()) {
        otpRecord.attempts += 1;
        const left = MAX_OTP_ATTEMPTS - otpRecord.attempts;
        return res.status(400).json({ success: false, message: `Incorrect OTP. ${left} attempt(s) remaining.` });
      }

      memOtps.delete(normalizedEmail);
      const user = memUsers.get(normalizedEmail);
      if (!user) return res.status(404).json({ success: false, message: 'User not found.' });
      user.isEmailVerified = true;

      const token = jwt.sign(
        { uid: user._id, email: user.email },
        process.env.JWT_SECRET,
        { expiresIn: '30d' }
      );

      return res.json({
        success: true,
        message: 'Email verified successfully!',
        token,
        user: {
          uid:              user._id,
          name:             user.name,
          email:            user.email,
          isEmailVerified:  user.isEmailVerified,
          profilePhotoUri:  user.profilePhotoUri,
          birthdate:        user.birthdate,
          age:              user.age,
          gender:           user.gender,
          providerId:       user.providerId || 'password'
        }
      });
    }

  } catch (err) {
    console.error('[verify-otp]', err);
    return res.status(500).json({ success: false, message: 'Server error: ' + err.message });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// POST /api/resend-otp
// Body: { email }
// Resends OTP with 45-second cooldown
// ─────────────────────────────────────────────────────────────────────────────
router.post('/resend-otp', async (req, res) => {
  try {
    const { email } = req.body;
    if (!email) return res.status(400).json({ success: false, message: 'Email is required.' });

    const normalizedEmail = email.trim().toLowerCase();

    if (isMongoConnected()) {
      const existing = await Otp.findOne({ email: normalizedEmail });
      if (existing) {
        const elapsed = Date.now() - existing.createdAt.getTime();
        if (elapsed < RESEND_COOLDOWN_MS) {
          const waitSec = Math.ceil((RESEND_COOLDOWN_MS - elapsed) / 1000);
          return res.status(429).json({
            success: false,
            message: `Please wait ${waitSec} seconds before requesting a new OTP.`
          });
        }
      }

      const user = await User.findOne({ email: normalizedEmail });
      if (!user) return res.status(404).json({ success: false, message: 'No account found with this email.' });
      if (user.isEmailVerified) return res.status(400).json({ success: false, message: 'Email is already verified.' });

      await Otp.deleteMany({ email: normalizedEmail });
      const code = generateOtp();
      await Otp.create({
        email:    normalizedEmail,
        code,
        expiresAt: new Date(Date.now() + OTP_EXPIRY_MS)
      });

      await sendOtpEmail(normalizedEmail, user.name, code);
    } else {
      const existing = memOtps.get(normalizedEmail);
      if (existing) {
        const elapsed = Date.now() - (existing.createdAt?.getTime() || 0);
        if (elapsed < RESEND_COOLDOWN_MS) {
          const waitSec = Math.ceil((RESEND_COOLDOWN_MS - elapsed) / 1000);
          return res.status(429).json({
            success: false,
            message: `Please wait ${waitSec} seconds before requesting a new OTP.`
          });
        }
      }

      const user = memUsers.get(normalizedEmail);
      if (!user) return res.status(404).json({ success: false, message: 'No account found with this email.' });
      if (user.isEmailVerified) return res.status(400).json({ success: false, message: 'Email is already verified.' });

      const code = generateOtp();
      memOtps.set(normalizedEmail, {
        code,
        expiresAt: new Date(Date.now() + OTP_EXPIRY_MS),
        attempts: 0,
        createdAt: new Date()
      });

      await sendOtpEmail(normalizedEmail, user.name, code);
    }

    return res.json({ success: true, message: 'New OTP sent to your email inbox.' });

  } catch (err) {
    console.error('[resend-otp]', err);
    return res.status(500).json({ success: false, message: 'Server error.' });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// POST /api/login
// Body: { email, password }
// ─────────────────────────────────────────────────────────────────────────────
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ success: false, message: 'Email and password are required.' });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const user = isMongoConnected() 
      ? await User.findOne({ email: normalizedEmail })
      : memUsers.get(normalizedEmail);

    if (!user) {
      return res.status(401).json({ success: false, message: 'No account found with this email.' });
    }

    const passwordMatch = await bcrypt.compare(password, user.password);
    if (!passwordMatch) {
      return res.status(401).json({ success: false, message: 'Incorrect password. Please try again.' });
    }

    if (!user.isEmailVerified) {
      const code = generateOtp();
      if (isMongoConnected()) {
        await Otp.deleteMany({ email: normalizedEmail });
        await Otp.create({ email: normalizedEmail, code, expiresAt: new Date(Date.now() + OTP_EXPIRY_MS) });
      } else {
        memOtps.set(normalizedEmail, {
          code,
          expiresAt: new Date(Date.now() + OTP_EXPIRY_MS),
          attempts: 0,
          createdAt: new Date()
        });
      }
      await sendOtpEmail(normalizedEmail, user.name, code);

      return res.status(403).json({
        success: false,
        code: 'EMAIL_NOT_VERIFIED',
        message: 'Please verify your email. A new OTP has been sent to your inbox.'
      });
    }

    const uid = user._id?.toString() || user._id;
    const token = jwt.sign(
      { uid, email: user.email },
      process.env.JWT_SECRET,
      { expiresIn: '30d' }
    );

    return res.json({
      success: true,
      token,
      user: {
        uid,
        name:             user.name,
        email:            user.email,
        isEmailVerified:  user.isEmailVerified,
        profilePhotoUri:  user.profilePhotoUri,
        birthdate:        user.birthdate,
        age:              user.age,
        gender:           user.gender,
        providerId:       user.providerId || 'password'
      }
    });

  } catch (err) {
    console.error('[login]', err);
    return res.status(500).json({ success: false, message: 'Server error.' });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// POST /api/update-profile
// Body: { email, profilePhotoUri, birthdate, age, gender }
// Auth: Bearer JWT token
// ─────────────────────────────────────────────────────────────────────────────
router.post('/update-profile', async (req, res) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ success: false, message: 'Unauthorized.' });
    }
    const token   = authHeader.split(' ')[1];
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    const { profilePhotoUri, birthdate, age, gender } = req.body;
    const user = await User.findByIdAndUpdate(
      decoded.uid,
      { profilePhotoUri, birthdate, age, gender },
      { new: true }
    );

    return res.json({
      success: true,
      user: {
        uid:              user._id.toString(),
        name:             user.name,
        email:            user.email,
        isEmailVerified:  user.isEmailVerified,
        profilePhotoUri:  user.profilePhotoUri,
        birthdate:        user.birthdate,
        age:              user.age,
        gender:           user.gender,
        providerId:       user.providerId
      }
    });
  } catch (err) {
    console.error('[update-profile]', err);
    return res.status(500).json({ success: false, message: 'Server error.' });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// GET /api/health
// Simple health check endpoint
// ─────────────────────────────────────────────────────────────────────────────
router.get('/health', (req, res) => {
  res.json({ success: true, message: 'Viora Music API is running 🎵', time: new Date().toISOString() });
});

module.exports = router;
