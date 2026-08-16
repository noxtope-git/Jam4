-- Jam! schema PostgreSQL
-- Requiere PostgreSQL 13+ (gen_random_uuid nativo)

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(30) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    -- Datos personales
    numero_identidad VARCHAR(30) UNIQUE,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    telefono VARCHAR(30),
    pais VARCHAR(60),
    ciudad VARCHAR(100),
    bio TEXT DEFAULT '',
    fecha_nacimiento DATE,

    -- Perfil / personalización
    foto_perfil_url TEXT DEFAULT '',
    banner_url TEXT DEFAULT '',
    etiquetas TEXT[] DEFAULT '{}',
    color_primario BIGINT DEFAULT 4294967295,
    color_secundario BIGINT DEFAULT 4289240982,
    modo_oscuro BOOLEAN DEFAULT true,
    mostrar_nombre_real BOOLEAN DEFAULT false,
    mostrar_email BOOLEAN DEFAULT false,
    luces_activas BOOLEAN DEFAULT false,

    -- Ubicación
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,

    -- Estado / verificación
    es_verificado BOOLEAN DEFAULT false,
    datos_personales_completos BOOLEAN DEFAULT false,

    -- Comunidad / premium
    es_premium BOOLEAN DEFAULT false,
    premium_hasta BIGINT DEFAULT 0,
    premium_vitalicio BOOLEAN DEFAULT false,
    apoyo_beta BOOLEAN DEFAULT false,
    puntos_apoyo INTEGER DEFAULT 0,
    jams_esta_semana INTEGER DEFAULT 0,
    semana_actual INTEGER DEFAULT 0,

    -- Social
    seguidores UUID[] DEFAULT '{}',
    siguiendo UUID[] DEFAULT '{}',
    bloqueados UUID[] DEFAULT '{}',

    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios (username);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios (email);
CREATE INDEX IF NOT EXISTS idx_usuarios_numero_identidad ON usuarios (numero_identidad);
CREATE INDEX IF NOT EXISTS idx_usuarios_puntos_apoyo ON usuarios (puntos_apoyo DESC);
