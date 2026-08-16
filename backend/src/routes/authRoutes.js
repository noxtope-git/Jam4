import { Router } from 'express';
import rateLimit from 'express-rate-limit';
import { registrar, login, me } from '../controllers/authController.js';
import { authRequired } from '../middleware/auth.js';

const router = Router();

const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 20,
  standardHeaders: 'draft-7',
  legacyHeaders: false,
  message: { error: 'Demasiados intentos. Intenta de nuevo en 15 minutos.' },
});

router.post('/registro', authLimiter, registrar);
router.post('/login', authLimiter, login);
router.get('/me', authRequired, me);

export default router;
