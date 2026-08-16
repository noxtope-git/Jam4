import { ZodError } from 'zod';

export function errorHandler(err, req, res, next) {
  if (err instanceof ZodError) {
    return res.status(400).json({
      error: 'Datos inválidos',
      details: err.errors.map((e) => ({ campo: e.path.join('.'), mensaje: e.message })),
    });
  }

  if (err.code === '23505') {
    return res.status(409).json({ error: 'El valor ya está en uso' });
  }

  console.error('Error no controlado:', err);
  return res.status(500).json({ error: 'Error interno del servidor' });
}

export function notFound(req, res) {
  res.status(404).json({ error: 'Ruta no encontrada' });
}
