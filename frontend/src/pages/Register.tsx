import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const { registrar } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    numeroIdentidad: '',
    nombre: '',
    apellido: '',
    telefono: '',
    pais: '',
    ciudad: '',
  })
  const [error, setError] = useState('')
  const [cargando, setCargando] = useState(false)

  const setCampo = (campo: string, valor: string) =>
    setForm((f) => ({ ...f, [campo]: valor }))

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setCargando(true)
    try {
      await registrar(form)
      navigate('/')
    } catch (err: any) {
      setError(err.response?.data?.error ?? 'Error al registrarse')
    } finally {
      setCargando(false)
    }
  }

  const inputClase =
    'w-full rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm outline-none focus:border-brand-500'

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-10">
      <div className="w-full max-w-md rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl">
        <h1 className="text-center text-3xl font-bold text-brand-500">Jam!</h1>
        <p className="mt-2 text-center text-sm text-slate-400">Crea tu cuenta</p>

        <form onSubmit={onSubmit} className="mt-8 space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-1 block text-sm text-slate-300">Username</label>
              <input required value={form.username} onChange={(e) => setCampo('username', e.target.value)} className={inputClase} />
            </div>
            <div>
              <label className="mb-1 block text-sm text-slate-300">Email</label>
              <input type="email" required value={form.email} onChange={(e) => setCampo('email', e.target.value)} className={inputClase} />
            </div>
          </div>

          <div>
            <label className="mb-1 block text-sm text-slate-300">Contraseña</label>
            <input type="password" required value={form.password} onChange={(e) => setCampo('password', e.target.value)} className={inputClase} placeholder="Mínimo 6 caracteres" />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-1 block text-sm text-slate-300">Nombre</label>
              <input required value={form.nombre} onChange={(e) => setCampo('nombre', e.target.value)} className={inputClase} />
            </div>
            <div>
              <label className="mb-1 block text-sm text-slate-300">Apellido</label>
              <input required value={form.apellido} onChange={(e) => setCampo('apellido', e.target.value)} className={inputClase} />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-1 block text-sm text-slate-300">Nº de identidad</label>
              <input required value={form.numeroIdentidad} onChange={(e) => setCampo('numeroIdentidad', e.target.value)} className={inputClase} />
            </div>
            <div>
              <label className="mb-1 block text-sm text-slate-300">Teléfono</label>
              <input value={form.telefono} onChange={(e) => setCampo('telefono', e.target.value)} className={inputClase} />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-1 block text-sm text-slate-300">País</label>
              <input value={form.pais} onChange={(e) => setCampo('pais', e.target.value)} className={inputClase} />
            </div>
            <div>
              <label className="mb-1 block text-sm text-slate-300">Ciudad</label>
              <input value={form.ciudad} onChange={(e) => setCampo('ciudad', e.target.value)} className={inputClase} />
            </div>
          </div>

          {error && <p className="text-sm text-red-400">{error}</p>}

          <button type="submit" disabled={cargando} className="w-full rounded-lg bg-brand-600 py-2 font-semibold text-white transition hover:bg-brand-500 disabled:opacity-50">
            {cargando ? 'Creando cuenta...' : 'Crear cuenta'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-400">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="font-semibold text-brand-400 hover:underline">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  )
}
