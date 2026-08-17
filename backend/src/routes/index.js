import { Router } from 'express';
import authRoutes from './authRoutes.js';
import premiumRoutes from './premiumRoutes.js';

const router = Router();

router.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

router.use('/auth', authRoutes);
router.use('/premium', premiumRoutes);

export default router;
