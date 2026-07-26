import { useState, useEffect } from "react";
import { categoryService } from "../services/categoryService";
import Modal from "../components/Modal";
import CategoryForm from "../components/admin/CategoryForm";
import ProductForm from "../components/admin/ProductForm";

// Panel de administración. Dos botones abren sendos modales con los
// formularios de creación. Cargamos las categorías una vez (y las refrescamos
// tras crear una) para poblar los desplegables de ambos formularios.
export default function Admin() {
  const [categories, setCategories] = useState([]);
  const [openModal, setOpenModal] = useState(null); // "category" | "product" | null

  const loadCategories = () =>
    categoryService.getAllForAdmin().then(setCategories).catch(() => {});

  useEffect(() => {
    loadCategories();
  }, []);

  const closeModal = () => setOpenModal(null);

  return (
    <div className="p-8">
      <h1 className="font-display text-2xl font-bold text-slate-900">
        Panel de administración
      </h1>
      <p className="mt-1 text-slate-500">
        Crea nuevas categorías y productos para el catálogo.
      </p>

      <div className="mt-6 flex flex-wrap gap-4">
        <button
          onClick={() => setOpenModal("category")}
          className="rounded-xl border border-slate-200 bg-white px-5 py-4 text-left shadow-sm transition-colors hover:border-brand hover:bg-brand/5"
        >
          <span className="block font-display text-lg font-semibold text-slate-900">
            + Crear categoría
          </span>
          <span className="text-sm text-slate-500">Añade una categoría al catálogo</span>
        </button>

        <button
          onClick={() => setOpenModal("product")}
          className="rounded-xl border border-slate-200 bg-white px-5 py-4 text-left shadow-sm transition-colors hover:border-brand hover:bg-brand/5"
        >
          <span className="block font-display text-lg font-semibold text-slate-900">
            + Crear producto
          </span>
          <span className="text-sm text-slate-500">Añade un producto al catálogo</span>
        </button>
      </div>

      <Modal
        open={openModal === "category"}
        onClose={closeModal}
        title="Crear categoría"
      >
        <CategoryForm categories={categories} onCreated={loadCategories} />
      </Modal>

      <Modal
        open={openModal === "product"}
        onClose={closeModal}
        title="Crear producto"
      >
        <ProductForm categories={categories} onSuccess={loadCategories} />
      </Modal>
    </div>
  );
}
