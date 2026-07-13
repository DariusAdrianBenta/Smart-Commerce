// Chrome compartido de las pantallas de autenticación (Login y Registro):
// panel de marca a la izquierda + área de formulario a la derecha.
// Recibe el título/subtítulo del panel y el formulario como children.

function Wordmark({ dark = false }) {
  return (
    <div className="flex items-center gap-2">
      <span className="grid h-9 w-9 place-items-center rounded-lg bg-brand font-display text-lg font-bold text-white">
        S
      </span>
      <span className={`font-display text-xl font-semibold ${dark ? "text-slate-900" : "text-white"}`}>
        SmartCommerce
      </span>
    </div>
  );
}

export default function AuthLayout({ panelTitle, panelSubtitle, children }) {
  return (
    <div className="grid min-h-dvh lg:grid-cols-2">
      {/* Panel de marca (oculto en móvil) */}
      <aside className="relative hidden overflow-hidden bg-brand-deep p-12 text-white lg:flex lg:flex-col lg:justify-between">
        {/* Signature: rejilla de tiles (evoca la parrilla de un marketplace) */}
        <div
          className="pointer-events-none absolute inset-0 opacity-[0.15]"
          style={{
            backgroundImage:
              "linear-gradient(to right, rgba(255,255,255,.4) 1px, transparent 1px), linear-gradient(to bottom, rgba(255,255,255,.4) 1px, transparent 1px)",
            backgroundSize: "64px 64px",
          }}
          aria-hidden="true"
        />
        <div className="relative">
          <Wordmark />
        </div>
        <div className="relative">
          <h1 className="font-display text-4xl font-bold leading-tight">{panelTitle}</h1>
          <p className="mt-4 max-w-sm text-white/70">{panelSubtitle}</p>
        </div>
        <p className="relative text-sm text-white/50">© 2026 SmartCommerce</p>
      </aside>

      {/* Área de formulario */}
      <main className="flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-sm">
          <div className="mb-8 lg:hidden">
            <Wordmark dark />
          </div>
          {children}
        </div>
      </main>
    </div>
  );
}
