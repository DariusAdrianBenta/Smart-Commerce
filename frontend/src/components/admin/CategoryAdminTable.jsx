import { useState } from "react";
import { categoryService } from "../../services/categoryService";
import Modal from "../Modal";
import CategoryForm from "./CategoryForm";
import FormFeedback from "./FormFeedback";

// Tabla de gestión de categorías: muestra TODAS (incluidas las ocultas) con su
// estado y acciones Editar / Ocultar / Mostrar. Ocultar es en cascada (también
// sus productos) y pide confirmación.
// props:
//   categories    -> lista completa (fuente de la tabla y de los desplegables)
//   refreshSignal -> no se usa para cargar (la fuente es `categories`) pero se
//                    mantiene por simetría; los datos vienen del padre
//   onChanged     -> se llama tras editar/ocultar/mostrar
export default function CategoryAdminTable({ categories, onChanged }) {
  const [editing, setEditing] = useState(null);      // categoría | null
  const [confirming, setConfirming] = useState(null); // categoría a ocultar | null
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState(null);

  const changeVisibility = async (c, visible) => {
    setBusyId(c.id);
    setError(null);
    try {
      await categoryService.setVisibility(c.id, visible);
      setConfirming(null);
      onChanged?.();
    } catch (err) {
      setError(err.response?.data?.message || "No se pudo cambiar la visibilidad.");
    } finally {
      setBusyId(null);
    }
  };

  return (
    <section className="mt-10">
      <h2 className="font-display text-lg font-semibold text-slate-900">Categorías</h2>
      <FormFeedback status={error ? { type: "error", message: error } : null} />

      <div className="mt-3 overflow-x-auto rounded-xl border border-slate-200">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-500">
            <tr>
              <th className="px-4 py-3 font-medium">Nombre</th>
              <th className="px-4 py-3 font-medium">Padre</th>
              <th className="px-4 py-3 font-medium">Estado</th>
              <th className="px-4 py-3 text-right font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {categories.map((c) => (
              <tr key={c.id} className={c.active ? "" : "bg-slate-50/60"}>
                <td className="px-4 py-3 text-slate-900">{c.name}</td>
                <td className="px-4 py-3 text-slate-500">{c.parentName || "—"}</td>
                <td className="px-4 py-3">
                  {c.active ? (
                    <span className="inline-flex items-center gap-1.5 text-emerald-600">
                      <span className="h-2 w-2 rounded-full bg-emerald-500" /> Visible
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1.5 text-slate-400">
                      <span className="h-2 w-2 rounded-full bg-slate-300" /> Oculto
                    </span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <div className="flex justify-end gap-2">
                    <button
                      onClick={() => setEditing(c)}
                      className="rounded-lg border border-slate-300 px-3 py-1.5 font-medium text-slate-700 transition-colors hover:bg-slate-100"
                    >
                      Editar
                    </button>
                    {c.active ? (
                      <button
                        onClick={() => setConfirming(c)}
                        disabled={busyId === c.id}
                        className="rounded-lg border border-slate-300 px-3 py-1.5 font-medium text-slate-700 transition-colors hover:bg-slate-100 disabled:opacity-60"
                      >
                        Ocultar
                      </button>
                    ) : (
                      <button
                        onClick={() => changeVisibility(c, true)}
                        disabled={busyId === c.id}
                        className="rounded-lg bg-brand px-3 py-1.5 font-semibold text-white transition-colors hover:bg-brand-dark disabled:opacity-60"
                      >
                        {busyId === c.id ? "…" : "Mostrar"}
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {categories.length === 0 && (
              <tr>
                <td colSpan={4} className="px-4 py-6 text-center text-slate-400">
                  No hay categorías.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal de edición */}
      <Modal open={Boolean(editing)} onClose={() => setEditing(null)} title="Editar categoría">
        {editing && (
          <CategoryForm
            categories={categories}
            category={editing}
            onSuccess={() => {
              setEditing(null);
              onChanged?.();
            }}
          />
        )}
      </Modal>

      {/* Confirmación de ocultar (avisa de la cascada) */}
      <Modal
        open={Boolean(confirming)}
        onClose={() => setConfirming(null)}
        title="Ocultar categoría"
      >
        {confirming && (
          <>
            <p className="text-slate-600">
              ¿Ocultar{" "}
              <span className="font-semibold text-slate-900">{confirming.name}</span>?
              También se ocultarán sus productos. Al volver a mostrarla se
              restaurarán solo esos.
            </p>
            <div className="mt-5 flex justify-end gap-3">
              <button
                onClick={() => setConfirming(null)}
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-100"
              >
                Cancelar
              </button>
              <button
                onClick={() => changeVisibility(confirming, false)}
                disabled={busyId === confirming.id}
                className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-slate-700 disabled:opacity-60"
              >
                {busyId === confirming.id ? "Ocultando…" : "Ocultar"}
              </button>
            </div>
          </>
        )}
      </Modal>
    </section>
  );
}
