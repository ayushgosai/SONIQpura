const crypto = require('crypto');

/**
 * Generate a secure 6-digit OTP code.
 * Uses crypto.randomInt for cryptographic security (not Math.random).
 */
function generateOtp() {
  return String(crypto.randomInt(100000, 999999));
}

module.exports = { generateOtp };
