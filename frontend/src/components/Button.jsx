// Botón primario reutilizable (presentacional). Usa el color de marca.
export default function Button({ children, className = "", ...props }) {
  return (
    <button
      className={`w-full rounded-lg bg-brand px-4 py-2.5 font-semibold text-white transition-colors duration-200 hover:bg-brand-dark focus:outline-none focus:ring-2 focus:ring-brand/40 disabled:cursor-not-allowed disabled:opacity-50 ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
