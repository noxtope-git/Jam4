import { Router } from 'express';
import rateLimit from 'express-rate-limit';
import { activarPremium } from '../controllers/premiumController.js';
import { firebaseAuth } from '../middleware/firebaseAuth.js';

const router = Router();

const premiumLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 10,
  standardHeaders: 'draft-7',
  legacyHeaders: false,
  message: { error: 'Demasiados intentos. Intenta más tarde.' },
});

router.post('/activar', premiumLimiter, firebaseAuth, activarPremium);

export default router;
