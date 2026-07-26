import { useState, useEffect } from "react";
import { productService } from "../../services/productService";
import { formatPrice } from "../../utils/formatPrice";
import Modal from "../Modal";
import ProductForm from "./ProductForm";
import FormFeedback from "./FormFeedback";

// Tabla de gestión de productos: muestra TODOS (incluidos los ocultos) con su
// estado y acciones Editar / Ocultar / Mostrar. Ocultar pide confirmación.
// props:
//   categories    -> para el desplegable del formulario de edición
//   refreshSignal -> al cambiar, recarga la lista
//   onChanged     -> se llama tras cualquier cambio (para sincronizar la página)
export default function ProductAdminTable({ categories, refreshSignal, onChanged }) {
  const [products, setProducts] = useState([]);
  const [editing, setEditing] = useState(null);      // producto | null
  const [confirming, setConfirming] = useState(null); // producto a ocultar | null
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState(null);

  const load = () =>
    productService
      .getAllForAdmin()
      .then(setProducts)
      .catch(() => setError("No se pudieron cargar los productos."));

  useEffect(() => {
    load();
  }, [refreshSignal]);

  const isHidden = (p) => p.status === "DISABLED";

  const changeVisibility = async (p, visible) => {
    setBusyId(p.id);
    setError(null);
    try {
      await productService.setVisibility(p.id, visible);
      setConfirming(null);
      onChanged?.();
    } catch (err) {
      setError(err.response?.data?.message || "No se pudo cambiar la visibilidad.");
    } finally {
      setBusyId(null);
    }
  };

  return (
    <section className="mt-8">
      <h2 className="font-display text-lg font-semibold text-slate-900">Productos</h2>
      <FormFeedback status={error ? { type: "error", message: error } : null} />

      <div className="mt-3 overflow-x-auto rounded-xl border border-slate-200">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-500">
            <tr>
              <th className="px-4 py-3 font-medium">Nombre</th>
              <th className="px-4 py-3 font-medium">Precio</th>
              <th className="px-4 py-3 font-medium">Estado</th>
              <th className="px-4 py-3 text-right font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {products.map((p) => (
              <tr key={p.id} className={isHidden(p) ? "bg-slate-50/60" : ""}>
                <td className="px-4 py-3 text-slate-900">{p.name}</td>
                <td className="px-4 py-3 text-slate-600">{formatPrice(p.price)}</td>
                <td className="px-4 py-3">
                  {isHidden(p) ? (
                    <span className="inline-flex items-center gap-1.5 text-slate-400">
                      <span className="h-2 w-2 rounded-full bg-slate-300" /> Oculto
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1.5 text-emerald-600">
                      <span className="h-2 w-2 rounded-full bg-emerald-500" /> Visible
                    </span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <div className="flex justify-end gap-2">
                    <button
                      onClick={() => setEditing(p)}
                      className="rounded-lg border border-slate-300 px-3 py-1.5 font-medium text-slate-700 transition-colors hover:bg-slate-100"
                    >
                      Editar
                    </button>
                    {isHidden(p) ? (
                      <button
                        onClick={() => changeVisibility(p, true)}
                        disabled={busyId === p.id}
                        className="rounded-lg bg-brand px-3 py-1.5 font-semibold text-white transition-colors hover:bg-brand-dark disabled:opacity-60"
                      >
                        {busyId === p.id ? "…" : "Mostrar"}
                      </button>
                    ) : (
                      <button
                        onClick={() => setConfirming(p)}
                        disabled={busyId === p.id}
                        className="rounded-lg border border-slate-300 px-3 py-1.5 font-medium text-slate-700 transition-colors hover:bg-slate-100 disabled:opacity-60"
                      >
                        Ocultar
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {products.length === 0 && (
              <tr>
                <td colSpan={4} className="px-4 py-6 text-center text-slate-400">
                  No hay productos.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal de edición */}
      <Modal open={Boolean(editing)} onClose={() => setEditing(null)} title="Editar producto">
        {editing && (
          <ProductForm
            categories={categories}
            product={editing}
            onSuccess={() => {
              setEditing(null);
              onChanged?.();
            }}
          />
        )}
      </Modal>

      {/* Confirmación de ocultar */}
      <Modal
        open={Boolean(confirming)}
        onClose={() => setConfirming(null)}
        title="Ocultar producto"
      >
        {confirming && (
          <>
            <p className="text-slate-600">
              ¿Ocultar{" "}
              <span className="font-semibold text-slate-900">{confirming.name}</span>?
              Dejará de aparecer en el catálogo, pero podrás volver a mostrarlo.
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
