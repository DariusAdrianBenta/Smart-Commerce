// Campo de formulario reutilizable, con etiqueta y foco en color de marca.
// `endAdornment` añade un elemento a la derecha (p. ej. mostrar/ocultar contraseña).
// `hint` muestra un texto de ayuda debajo del campo.
export default function Input({ label, id, endAdornment, hint, className = "", ...props }) {
  return (
    <label htmlFor={id} className="mb-4 block">
      <span className="mb-1.5 block text-sm font-medium text-slate-700">{label}</span>
      <div className="relative">
        <input
          id={id}
          className={`w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-slate-900 placeholder-slate-400 transition focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/30 ${
            endAdornment ? "pr-11" : ""
          } ${className}`}
          {...props}
        />
        {endAdornment && (
          <div className="absolute inset-y-0 right-0 flex items-center pr-3">
            {endAdornment}
          </div>
        )}
      </div>
      {hint && <span className="mt-1 block text-xs text-slate-400">{hint}</span>}
    </label>
  );
}
