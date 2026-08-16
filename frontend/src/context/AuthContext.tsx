import { createContext, useContext, useEffect, useState } from 'react'
import api from '../api/client'

export interface Usuario {
  id: string
  username: string
  email: string
  bio?: string
  foto_perfil_url?: string
  apoyo_beta?: boolean
  puntos_apoyo?: number
  [key: string]: unknown
}

interface AuthContextValue {
  usuario: Usuario | null
  cargando: boolean
  login: (email: string, password: string) => Promise<void>
  registrar: (data: Record<string, string>) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('jam_token')
    if (!token) {
      setCargando(false)
      return
    }
    api
      .get('/auth/me')
      .then((res) => setUsuario(res.data.usuario))
      .catch(() => localStorage.removeItem('jam_token'))
      .finally(() => setCargando(false))
  }, [])

  const login = async (email: string, password: string) => {
    const res = await api.post('/auth/login', { email, password })
    localStorage.setItem('jam_token', res.data.token)
    setUsuario(res.data.usuario)
  }

  const registrar = async (data: Record<string, string>) => {
    const res = await api.post('/auth/registro', data)
    localStorage.setItem('jam_token', res.data.token)
    setUsuario(res.data.usuario)
  }

  const logout = () => {
    localStorage.removeItem('jam_token')
    setUsuario(null)
  }

  return (
    <AuthContext.Provider value={{ usuario, cargando, login, registrar, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return ctx
}
