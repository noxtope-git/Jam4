import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import pool from '../config/db.js';

const __dirname = dirname(fileURLToPath(import.meta.url));

async function migrate() {
  const client = await pool.connect();
  try {
    const sql = await readFile(join(__dirname, 'schema.sql'), 'utf-8');
    await client.query(sql);
    console.log('✅ Migración aplicada correctamente');
  } catch (err) {
    console.error('❌ Error aplicando migración:', err.message);
    process.exitCode = 1;
  } finally {
    client.release();
    await pool.end();
  }
}

migrate();
