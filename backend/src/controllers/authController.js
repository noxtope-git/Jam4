import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { z } from 'zod';
import pool from '../config/db.js';

const registroSchema = z.object({
  username: z.string().min(3, 'Mínimo 3 caracteres').max(30, 'Máximo 30 caracteres')
    .regex(/^[a-zA-Z0-9_.]+$/, 'Solo letras, números, puntos y guiones bajos'),
  email: z.string().email('Email inválido').max(255),
  password: z.string().min(6, 'Mínimo 6 caracteres').max(100),
  numeroIdentidad: z.string().min(3, 'Número de identidad requerido').max(30).optional(),
  nombre: z.string().min(2, 'Nombre requerido').max(100).optional(),
  apellido: z.string().min(2, 'Apellido requerido').max(100).optional(),
  telefono: z.string().max(30).optional(),
  pais: z.string().max(60).optional(),
  ciudad: z.string().max(100).optional(),
});

const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(1, 'Contraseña requerida'),
});

function firmarToken(userId) {
  return jwt.sign(
    { sub: userId },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRES_IN || '7d' },
  );
}

function toUsuarioPublico(row) {
  const { password_hash, ...publico } = row;
  return publico;
}

export async function registrar(req, res, next) {
  try {
    const data = registroSchema.parse(req.body);

    const passwordHash = await bcrypt.hash(data.password, 10);

    const result = await pool.query(
      `INSERT INTO usuarios
        (username, email, password_hash, numero_identidad, nombre, apellido, telefono, pais, ciudad)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
       RETURNING *`,
      [
        data.username,
        data.email.toLowerCase(),
        passwordHash,
        data.numeroIdentidad ?? null,
        data.nombre ?? null,
        data.apellido ?? null,
        data.telefono ?? null,
        data.pais ?? null,
        data.ciudad ?? null,
      ],
    );

    const usuario = result.rows[0];
    const token = firmarToken(usuario.id);

    res.status(201).json({ token, usuario: toUsuarioPublico(usuario) });
  } catch (err) {
    next(err);
  }
}

export async function login(req, res, next) {
  try {
    const data = loginSchema.parse(req.body);

    const result = await pool.query(
      'SELECT * FROM usuarios WHERE email = $1',
      [data.email.toLowerCase()],
    );
    const usuario = result.rows[0];

    if (!usuario) {
      return res.status(401).json({ error: 'Credenciales inválidas' });
    }

    const passwordValido = await bcrypt.compare(data.password, usuario.password_hash);
    if (!passwordValido) {
      return res.status(401).json({ error: 'Credenciales inválidas' });
    }

    const token = firmarToken(usuario.id);
    res.json({ token, usuario: toUsuarioPublico(usuario) });
  } catch (err) {
    next(err);
  }
}

export async function me(req, res, next) {
  try {
    const result = await pool.query(
      'SELECT * FROM usuarios WHERE id = $1',
      [req.userId],
    );
    const usuario = result.rows[0];
    if (!usuario) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }
    res.json({ usuario: toUsuarioPublico(usuario) });
  } catch (err) {
    next(err);
  }
}
