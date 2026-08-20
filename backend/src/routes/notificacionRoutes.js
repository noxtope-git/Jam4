import { Router } from 'express';
import { enviarNotificacion } from '../controllers/notificacionController.js';

const router = Router();

router.post('/enviar', enviarNotificacion);

export default router;
