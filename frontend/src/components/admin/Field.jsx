// Envuelve un campo de formulario con su etiqueta. Marca visualmente si es
// obligatorio (asterisco rojo) u opcional (texto gris "(opcional)").
// `children` es el control real: <input>, <select> o <textarea>.
export default function Field({ label, required, htmlFor, children }) {
  return (
    <label htmlFor={htmlFor} className="mb-4 block">
      <span className="mb-1.5 block text-sm font-medium text-slate-700">
        {label}
        {required ? (
          <span className="text-red-500"> *</span>
        ) : (
          <span className="font-normal text-slate-400"> (opcional)</span>
        )}
      </span>
      {children}
    </label>
  );
}

// Clases compartidas para inputs/selects/textareas, iguales a las del
// componente Input para mantener la coherencia visual.
export const fieldClass =
  "w-full rounded-lg border border-slate-300 px-3.5 py-2.5 text-slate-900 placeholder-slate-400 transition focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/30";
