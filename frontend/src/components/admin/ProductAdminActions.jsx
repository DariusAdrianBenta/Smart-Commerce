import { useState } from "react";
import { useAuth } from "../../hooks/useAuth";
import { categoryService } from "../../services/categoryService";
import { productService } from "../../services/productService";
import Modal from "../Modal";
import ProductForm from "./ProductForm";
import FormFeedback from "./FormFeedback";

// Controles de administración (Editar / Borrar) que se superponen sobre una
// tarjeta de producto. Solo se renderiza para usuarios ADMIN; para el resto
// devuelve null (no ocupa nada).
//
// props:
//   product   -> el producto de la tarjeta
//   onChanged -> se llama tras editar o borrar, para que el padre recargue
export default function ProductAdminActions({ product, onChanged }) {
  const { user } = useAuth();

  const [editing, setEditing] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [categories, setCategories] = useState([]);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  if (user?.role !== "ADMIN") return null;

  // Al abrir el editor cargamos las categorías (una vez) para el desplegable.
  const openEditor = async () => {
    if (categories.length === 0) {
      try {
        setCategories(await categoryService.getAllForAdmin());
      } catch {
        /* si falla, el desplegable saldrá vacío; el usuario puede reintentar */
      }
    }
    setEditing(true);
  };

  const handleEdited = () => {
    setEditing(false);
    onChanged?.();
  };

  const handleDelete = async () => {
    setDeleting(true);
    setDeleteError(null);
    try {
      await productService.remove(product.id);
      setConfirming(false);
      onChanged?.();
    } catch (err) {
      const msg = err.response?.data?.message;
      setDeleteError(
        `Ha habido un problema al borrar el producto: ${msg || "inténtalo de nuevo."}`
      );
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      {/* Barra de acciones, arriba a la izquierda de la tarjeta */}
      <div className="absolute left-2 top-2 z-10 flex gap-1.5">
        <button
          onClick={openEditor}
          aria-label="Editar producto"
          title="Editar"
          className="grid h-9 w-9 place-items-center rounded-full bg-white/90 text-slate-600 shadow-sm backdrop-blur transition-colors hover:text-brand"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 20h9" />
            <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z" />
          </svg>
        </button>
        <button
          onClick={() => {
            setDeleteError(null);
            setConfirming(true);
          }}
          aria-label="Borrar producto"
          title="Borrar"
          className="grid h-9 w-9 place-items-center rounded-full bg-white/90 text-slate-600 shadow-sm backdrop-blur transition-colors hover:text-red-600"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 6h18" />
            <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
          </svg>
        </button>
      </div>

      {/* Modal de edición */}
      <Modal open={editing} onClose={() => setEditing(false)} title="Editar producto">
        <ProductForm
          categories={categories}
          product={product}
          onSuccess={handleEdited}
        />
      </Modal>

      {/* Modal de confirmación de borrado */}
      <Modal
        open={confirming}
        onClose={() => setConfirming(false)}
        title="Borrar producto"
      >
        <FormFeedback status={deleteError ? { type: "error", message: deleteError } : null} />
        <p className="text-slate-600">
          ¿Seguro que quieres borrar{" "}
          <span className="font-semibold text-slate-900">{product.name}</span>? Se
          retirará del catálogo.
        </p>
        <div className="mt-5 flex justify-end gap-3">
          <button
            onClick={() => setConfirming(false)}
            className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-100"
          >
            Cancelar
          </button>
          <button
            onClick={handleDelete}
            disabled={deleting}
            className="rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-red-700 disabled:opacity-60"
          >
            {deleting ? "Borrando…" : "Borrar"}
          </button>
        </div>
      </Modal>
    </>
  );
}
