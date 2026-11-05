const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  name:             { type: String, required: true, trim: true },
  email:            { type: String, required: true, unique: true, lowercase: true, trim: true },
  password:         { type: String, required: true },
  isEmailVerified:  { type: Boolean, default: false },
  profilePhotoUri:  { type: String, default: null },
  birthdate:        { type: String, default: null },
  age:              { type: Number, default: null },
  gender:           { type: String, default: null },
  providerId:       { type: String, default: 'password' },
  createdAt:        { type: Date,   default: Date.now }
});

module.exports = mongoose.model('User', userSchema);
