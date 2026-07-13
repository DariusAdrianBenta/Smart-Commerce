import { useState } from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "../components/Sidebar";

// Menú lateral plegable + área de contenido. El estado `open` vive aquí
// (ancestro común del botón y del menú): patrón "lifting state up".
export default function MainLayout() {
  const [open, setOpen] = useState(true);

  return (
    <div className="flex min-h-dvh bg-slate-50">
      {open && <Sidebar />}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex items-center gap-3 border-b border-slate-200 bg-white px-4 py-3">
          <button
            onClick={() => setOpen((v) => !v)}
            aria-label="Mostrar u ocultar el menú"
            className="grid h-9 w-9 place-items-center rounded-lg text-slate-600 transition-colors hover:bg-slate-100"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              strokeWidth="2" strokeLinecap="round">
              <path d="M3 6h18M3 12h18M3 18h18" />
            </svg>
          </button>
        </header>

        <main className="min-w-0 flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
