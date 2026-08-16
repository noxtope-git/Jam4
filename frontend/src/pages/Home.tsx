import { useAuth } from '../context/AuthContext'

export default function Home() {
  const { usuario, logout } = useAuth()

  return (
    <div className="min-h-screen">
      <header className="border-b border-slate-800 bg-slate-900">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-3">
          <h1 className="text-2xl font-bold text-brand-500">Jam!</h1>
          <button
            onClick={logout}
            className="rounded-lg border border-slate-700 px-3 py-1.5 text-sm text-slate-300 transition hover:bg-slate-800"
          >
            Cerrar sesión
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-3xl px-4 py-8">
        <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
          <h2 className="text-xl font-semibold">
            Hola, @{usuario?.username} 👋
          </h2>
          <p className="mt-2 text-sm text-slate-400">
            Bienvenido a la web de Jam!. El feed de eventos y el sistema de Jams
            llegarán en la próxima fase.
          </p>
          {usuario?.apoyo_beta && (
            <div className="mt-4 rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-400">
              ✚ Beta Supporter
            </div>
          )}
        </div>
      </main>
    </div>
  )
}
