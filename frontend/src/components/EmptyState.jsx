// Mensaje para cuando no hay contenido que mostrar.
export default function EmptyState({ message = "No hay resultados." }) {
  return <p className="py-20 text-center text-slate-500">{message}</p>;
}
