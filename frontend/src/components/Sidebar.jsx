import { NavLink } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const links = [
  { to: "/", label: "Inicio", end: true },
  { to: "/products", label: "Productos" },
  { to: "/favorites", label: "Mis Favoritos" },
  { to: "/cart", label: "Carrito" },
  { to: "/settings", label: "Configuración" },
];

// Menú lateral persistente (a la izquierda). NavLink resalta la vista activa.
export default function Sidebar() {
  const { user, logout } = useAuth();
  return (
    <aside className="flex w-60 shrink-0 flex-col border-r border-slate-200 bg-white p-6">
      <div className="mb-8 flex items-center gap-2">
        <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand font-display font-bold text-white">
          S
        </span>
        <span className="font-display text-lg font-semibold text-slate-900">
          SmartCommerce
        </span>
      </div>

      <nav className="flex flex-col gap-1">
        {links.map((l) => (
          <NavLink
            key={l.to}
            to={l.to}
            end={l.end}
            className={({ isActive }) =>
              `rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-brand/10 text-brand-dark"
                  : "text-slate-600 hover:bg-slate-100"
              }`
            }
          >
            {l.label}
          </NavLink>
        ))}
      </nav>

      <div className="mt-auto border-t border-slate-200 pt-4">
        <p className="mb-2 truncate text-sm text-slate-500">{user?.email}</p>
        <button
          onClick={logout}
          className="text-sm font-medium text-red-600 hover:underline"
        >
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
