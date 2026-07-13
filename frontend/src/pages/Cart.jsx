import { useState, useEffect } from "react";
import { cartService } from "../services/cartService";
import { formatPrice } from "../utils/formatPrice";
import Spinner from "../components/Spinner";
import EmptyState from "../components/EmptyState";
import { TrashIcon, PlusIcon, MinusIcon } from "../components/icons";

export default function Cart() {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // Selección: vive solo en el frontend (Set de productId). El total se calcula
  // a partir de los productos marcados aquí.
  const [selected, setSelected] = useState(() => new Set());
  const [mutating, setMutating] = useState(false);

  // Carga inicial del carrito. Por defecto, todo seleccionado.
  useEffect(() => {
    cartService
      .getMyCart()
      .then((c) => {
        setCart(c);
        setSelected(new Set(c.items.map((i) => i.productId)));
      })
      .catch(() => setError("No se pudo cargar el carrito."))
      .finally(() => setLoading(false));
  }, []);

  // Guarda el carrito que devuelve el backend (fuente de verdad de cantidades y
  // subtotales) sin perder la selección de productos que aún siguen existiendo.
  const applyCart = (c) => {
    setCart(c);
    const ids = new Set(c.items.map((i) => i.productId));
    setSelected((prev) => new Set([...prev].filter((id) => ids.has(id))));
  };

  const changeQty = async (productId, quantity) => {
    if (quantity < 1 || mutating) return;
    setMutating(true);
    try {
      applyCart(await cartService.updateItem(productId, quantity));
    } catch {
      /* si falla, dejamos el carrito como estaba */
    } finally {
      setMutating(false);
    }
  };

  const removeItem = async (productId) => {
    if (mutating) return;
    setMutating(true);
    try {
      applyCart(await cartService.removeItem(productId));
    } catch {
      /* noop */
    } finally {
      setMutating(false);
    }
  };

  const toggle = (productId) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(productId)) next.delete(productId);
      else next.add(productId);
      return next;
    });
  };

  // Orden estable por productId: así las filas nunca se recolocan al actualizar,
  // aunque el backend devuelva los items en otro orden. Evita que un clic rápido
  // caiga sobre el producto equivocado.
  const items = [...(cart?.items ?? [])].sort((a, b) => a.productId - b.productId);
  const allSelected = items.length > 0 && selected.size === items.length;

  const toggleAll = () => {
    setSelected(allSelected ? new Set() : new Set(items.map((i) => i.productId)));
  };

  // --- Estado derivado: se recalcula en cada render a partir de cart + selected ---
  const selectedItems = items.filter((i) => selected.has(i.productId));
  const selectedUnits = selectedItems.reduce((sum, i) => sum + i.quantity, 0);
  const selectedTotal = selectedItems.reduce((sum, i) => sum + Number(i.subtotal), 0);

  return (
    <div className="p-8">
      <h1 className="font-display text-2xl font-bold text-slate-900">Carrito</h1>

      <div className="mt-6">
        {loading ? (
          <Spinner />
        ) : error ? (
          <p className="py-20 text-center text-red-600">{error}</p>
        ) : items.length === 0 ? (
          <EmptyState message="Tu carrito está vacío. Añade productos desde Inicio." />
        ) : (
          <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
            {/* Lista de productos */}
            <div>
              <label className="mb-3 flex items-center gap-3 text-sm text-slate-600">
                <input
                  type="checkbox"
                  checked={allSelected}
                  onChange={toggleAll}
                  className="h-4 w-4 accent-brand"
                />
                Seleccionar todo
              </label>

              <ul className="divide-y divide-slate-200 rounded-xl border border-slate-200 bg-white">
                {items.map((item) => (
                  <li key={item.productId} className="flex items-center gap-4 p-4">
                    <input
                      type="checkbox"
                      checked={selected.has(item.productId)}
                      onChange={() => toggle(item.productId)}
                      aria-label={`Seleccionar ${item.productName}`}
                      className="h-4 w-4 shrink-0 accent-brand"
                    />

                    <div className="h-16 w-16 shrink-0 overflow-hidden rounded-lg bg-slate-100">
                      {item.productImage ? (
                        <img
                          src={item.productImage}
                          alt={item.productName}
                          className="h-full w-full object-cover"
                        />
                      ) : (
                        <div className="grid h-full w-full place-items-center text-[10px] text-slate-300">
                          Sin imagen
                        </div>
                      )}
                    </div>

                    <div className="min-w-0 flex-1">
                      <h3 className="line-clamp-1 font-medium text-slate-900">
                        {item.productName}
                      </h3>
                      <p className="text-sm text-slate-500">
                        {formatPrice(item.productPrice)} / ud.
                      </p>
                    </div>

                    {/* Stepper de cantidad */}
                    <div className="flex items-center rounded-lg border border-slate-300">
                      <button
                        onClick={() => changeQty(item.productId, item.quantity - 1)}
                        disabled={mutating || item.quantity <= 1}
                        aria-label="Reducir cantidad"
                        className="grid h-8 w-8 place-items-center text-slate-600 transition hover:bg-slate-100 disabled:opacity-30"
                      >
                        <MinusIcon />
                      </button>
                      <span className="w-8 text-center text-sm font-medium tabular-nums">
                        {item.quantity}
                      </span>
                      <button
                        onClick={() => changeQty(item.productId, item.quantity + 1)}
                        disabled={mutating}
                        aria-label="Aumentar cantidad"
                        className="grid h-8 w-8 place-items-center text-slate-600 transition hover:bg-slate-100 disabled:opacity-30"
                      >
                        <PlusIcon />
                      </button>
                    </div>

                    <div className="w-24 text-right font-display font-semibold text-slate-900">
                      {formatPrice(item.subtotal)}
                    </div>

                    <button
                      onClick={() => removeItem(item.productId)}
                      disabled={mutating}
                      aria-label={`Eliminar ${item.productName}`}
                      className="grid h-8 w-8 shrink-0 place-items-center rounded-lg text-slate-400 transition hover:bg-red-50 hover:text-red-500 disabled:opacity-30"
                    >
                      <TrashIcon />
                    </button>
                  </li>
                ))}
              </ul>
            </div>

            {/* Resumen */}
            <aside className="h-fit rounded-xl border border-slate-200 bg-white p-6 lg:sticky lg:top-8">
              <h2 className="font-display text-lg font-semibold text-slate-900">Resumen</h2>
              <div className="mt-4 flex justify-between text-sm text-slate-600">
                <span>Artículos seleccionados</span>
                <span className="font-medium text-slate-900">{selectedUnits}</span>
              </div>
              <div className="mt-4 flex items-baseline justify-between border-t border-slate-200 pt-4">
                <span className="font-medium text-slate-900">Total</span>
                <span className="font-display text-2xl font-bold text-brand">
                  {formatPrice(selectedTotal)}
                </span>
              </div>
              <button
                disabled={selectedItems.length === 0}
                className="mt-6 w-full rounded-lg bg-brand py-2.5 text-sm font-semibold text-white transition hover:bg-brand-dark disabled:opacity-40"
              >
                Tramitar pedido
              </button>
            </aside>
          </div>
        )}
      </div>
    </div>
  );
}
