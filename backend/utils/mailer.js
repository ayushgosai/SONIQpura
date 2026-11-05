const nodemailer = require('nodemailer');

/**
 * Gmail SMTP transporter using an App Password from .env.
 */
const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.GMAIL_USER,
    pass: process.env.GMAIL_APP_PASSWORD
  }
});

/**
 * Send a beautiful OTP email to user's inbox.
 * @param {string} toEmail  - recipient email
 * @param {string} name     - recipient name
 * @param {string} otp      - 6-digit OTP code
 */
async function sendOtpEmail(toEmail, name, otp) {
  const expiryMinutes = process.env.OTP_EXPIRY_MINUTES || 5;

  const htmlBody = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <style>
    body { margin: 0; padding: 0; background: #0B0B10; font-family: 'Helvetica Neue', Arial, sans-serif; }
    .wrapper { max-width: 480px; margin: 32px auto; background: #1B1B26; border-radius: 20px; overflow: hidden; border: 1px solid #383545; }
    .header { background: linear-gradient(135deg, #9B7BFF 0%, #6366F1 100%); padding: 32px 32px 24px; text-align: center; }
    .header h1 { margin: 0; color: #fff; font-size: 26px; font-weight: 800; letter-spacing: -0.5px; }
    .header p { margin: 6px 0 0; color: rgba(255,255,255,0.8); font-size: 13px; }
    .body { padding: 32px; }
    .greeting { color: #F7F5FF; font-size: 17px; font-weight: 600; margin: 0 0 8px; }
    .desc { color: #A7A4B5; font-size: 14px; line-height: 1.6; margin: 0 0 28px; }
    .otp-box { background: #131220; border: 2px solid #9B7BFF; border-radius: 16px; text-align: center; padding: 24px 16px; margin: 0 0 24px; }
    .otp-label { color: #A7A4B5; font-size: 12px; text-transform: uppercase; letter-spacing: 1.5px; margin: 0 0 12px; }
    .otp-code { color: #9B7BFF; font-size: 44px; font-weight: 900; letter-spacing: 12px; margin: 0; font-variant-numeric: tabular-nums; }
    .expiry { color: #6B687B; font-size: 13px; margin: 0 0 28px; }
    .expiry span { color: #F87171; font-weight: 600; }
    .warning { background: #2A1B1B; border: 1px solid #F87171; border-radius: 12px; padding: 14px 18px; color: #F87171; font-size: 13px; line-height: 1.5; }
    .footer { border-top: 1px solid #383545; padding: 20px 32px; text-align: center; color: #6B687B; font-size: 12px; }
    .footer a { color: #9B7BFF; text-decoration: none; }
  </style>
</head>
<body>
  <div class="wrapper">
    <div class="header">
      <h1>🎵 Viora Music</h1>
      <p>Premium Music Experience</p>
    </div>
    <div class="body">
      <p class="greeting">Hello, ${name}! 👋</p>
      <p class="desc">
        Your One-Time Password (OTP) for creating your Viora Music account.
        Enter this code in the app to verify your email address.
      </p>
      <div class="otp-box">
        <p class="otp-label">Your verification code</p>
        <p class="otp-code">${otp}</p>
      </div>
      <p class="expiry">This code expires in <span>${expiryMinutes} minutes</span>. Do not share it with anyone.</p>
      <div class="warning">
        🔒 <strong>Security Notice:</strong> Viora Music will NEVER ask for this code via phone call or chat.
        If you didn't request this, please ignore this email.
      </div>
    </div>
    <div class="footer">
      <p>Sent from <a href="#">Viora Music</a> — Your Premium Music Companion</p>
      <p>© ${new Date().getFullYear()} Viora Music. All rights reserved.</p>
    </div>
  </div>
</body>
</html>
  `;

  const mailOptions = {
    from: `"Viora Music" <${process.env.GMAIL_USER}>`,
    to: toEmail,
    subject: `${otp} is your Viora Music verification code`,
    html: htmlBody,
    // Plain-text fallback (helps inbox delivery, avoids spam)
    text: `Hello ${name},\n\nYour Viora Music OTP is: ${otp}\n\nThis code expires in ${expiryMinutes} minutes.\n\nDo NOT share this with anyone.\n\n— Viora Music Team`
  };

  await transporter.sendMail(mailOptions);
  console.log(`[OTP] Email sent to ${toEmail}`);
}

module.exports = { sendOtpEmail };
