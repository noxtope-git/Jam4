import { Router } from 'express';
import authRoutes from './authRoutes.js';
import premiumRoutes from './premiumRoutes.js';
import notificacionRoutes from './notificacionRoutes.js';

const router = Router();

router.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

router.use('/auth', authRoutes);
router.use('/premium', premiumRoutes);
router.use('/notificaciones', notificacionRoutes);

export default router;
