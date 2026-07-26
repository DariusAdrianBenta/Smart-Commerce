import { useState } from "react";
import { productService } from "../../services/productService";
import Field, { fieldClass } from "./Field";
import FormFeedback from "./FormFeedback";
import Button from "../Button";

// Estado inicial del formulario. En modo edición se precarga con el producto.
// Los números se guardan como string (así los maneja <input>) y se convierten
// a Number al construir el payload.
function initialForm(product) {
  return {
    name: product?.name ?? "",
    price: product?.price != null ? String(product.price) : "",
    stock: product?.stock != null ? String(product.stock) : "",
    categoryId: product?.categoryId != null ? String(product.categoryId) : "",
    description: product?.description ?? "",
    brand: product?.brand ?? "",
  };
}

// Formulario de creación / edición de producto.
// props:
//   categories -> lista para el desplegable de "categoría" (obligatorio)
//   product    -> si viene, el formulario está en MODO EDICIÓN (PUT)
//   onSuccess  -> callback tras crear/editar con éxito
export default function ProductForm({ categories, product, onSuccess }) {
  const isEdit = Boolean(product);

  const [form, setForm] = useState(() => initialForm(product));
  const [imageUrls, setImageUrls] = useState([""]); // solo se usa al crear
  const [status, setStatus] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const set = (field) => (e) =>
    setForm((f) => ({ ...f, [field]: e.target.value }));

  // --- Gestión de las URLs de imagen (lista dinámica, solo al crear) ---
  const setImage = (i, value) =>
    setImageUrls((urls) => urls.map((u, idx) => (idx === i ? value : u)));
  const addImage = () => setImageUrls((urls) => [...urls, ""]);
  const removeImage = (i) =>
    setImageUrls((urls) => urls.filter((_, idx) => idx !== i));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus(null);

    // Validación en cliente (refleja las reglas del backend).
    if (!form.name.trim())
      return setStatus({ type: "error", message: "El nombre es obligatorio." });
    if (form.price === "" || Number(form.price) <= 0)
      return setStatus({ type: "error", message: "El precio debe ser mayor que 0." });
    if (form.stock === "" || Number(form.stock) < 0)
      return setStatus({ type: "error", message: "El stock no puede ser negativo." });
    if (!form.categoryId)
      return setStatus({ type: "error", message: "La categoría es obligatoria." });

    // Payload común a crear y editar (los opcionales solo si tienen valor).
    const payload = {
      name: form.name.trim(),
      price: Number(form.price),
      stock: Number(form.stock),
      categoryId: Number(form.categoryId),
    };
    if (form.description.trim()) payload.description = form.description.trim();
    if (form.brand.trim()) payload.brand = form.brand.trim();

    // Las imágenes solo se envían al crear (el endpoint de edición no las toca).
    if (!isEdit) {
      const urls = imageUrls.map((u) => u.trim()).filter(Boolean);
      if (urls.length) payload.imageUrls = urls;
    }

    setSubmitting(true);
    try {
      const saved = isEdit
        ? await productService.update(product.id, payload)
        : await productService.create(payload);

      setStatus({
        type: "success",
        message: isEdit
          ? "Producto actualizado correctamente."
          : "Producto creado correctamente.",
      });

      // Al crear reseteamos para poder encadenar otro; al editar mantenemos.
      if (!isEdit) {
        setForm(initialForm(null));
        setImageUrls([""]);
      }
      onSuccess?.(saved);
    } catch (err) {
      const msg = err.response?.data?.message;
      setStatus({
        type: "error",
        message: `Ha habido un problema al ${
          isEdit ? "actualizar" : "crear"
        } el producto: ${msg || "inténtalo de nuevo."}`,
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <FormFeedback status={status} />

      <Field label="Nombre" required htmlFor="prod-name">
        <input
          id="prod-name"
          className={fieldClass}
          value={form.name}
          onChange={set("name")}
          placeholder="Ej. Auriculares inalámbricos"
        />
      </Field>

      <div className="flex gap-4">
        <div className="flex-1">
          <Field label="Precio (€)" required htmlFor="prod-price">
            <input
              id="prod-price"
              type="number"
              min="0"
              step="0.01"
              className={fieldClass}
              value={form.price}
              onChange={set("price")}
              placeholder="0.00"
            />
          </Field>
        </div>
        <div className="flex-1">
          <Field label="Stock" required htmlFor="prod-stock">
            <input
              id="prod-stock"
              type="number"
              min="0"
              step="1"
              className={fieldClass}
              value={form.stock}
              onChange={set("stock")}
              placeholder="0"
            />
          </Field>
        </div>
      </div>

      <Field label="Categoría" required htmlFor="prod-category">
        <select
          id="prod-category"
          className={fieldClass}
          value={form.categoryId}
          onChange={set("categoryId")}
        >
          <option value="">Selecciona una categoría…</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        {categories.length === 0 && (
          <span className="mt-1 block text-xs text-amber-600">
            No hay categorías todavía. Crea una categoría primero.
          </span>
        )}
      </Field>

      <Field label="Marca" htmlFor="prod-brand">
        <input
          id="prod-brand"
          className={fieldClass}
          value={form.brand}
          onChange={set("brand")}
          placeholder="Ej. Sony"
        />
      </Field>

      <Field label="Descripción" htmlFor="prod-description">
        <textarea
          id="prod-description"
          rows={3}
          className={fieldClass}
          value={form.description}
          onChange={set("description")}
          placeholder="Detalles del producto…"
        />
      </Field>

      {/* URLs de imagen: solo al crear. La edición de imágenes va aparte. */}
      {!isEdit && (
        <div className="mb-4">
          <span className="mb-1.5 block text-sm font-medium text-slate-700">
            Imágenes
            <span className="font-normal text-slate-400"> (opcional)</span>
          </span>
          <div className="flex flex-col gap-2">
            {imageUrls.map((url, i) => (
              <div key={i} className="flex gap-2">
                <input
                  className={fieldClass}
                  value={url}
                  onChange={(e) => setImage(i, e.target.value)}
                  placeholder="https://…"
                />
                {imageUrls.length > 1 && (
                  <button
                    type="button"
                    onClick={() => removeImage(i)}
                    aria-label="Quitar imagen"
                    className="shrink-0 rounded-lg border border-slate-300 px-3 text-slate-500 transition-colors hover:bg-slate-100"
                  >
                    ✕
                  </button>
                )}
              </div>
            ))}
          </div>
          <button
            type="button"
            onClick={addImage}
            className="mt-2 text-sm font-medium text-brand hover:text-brand-dark"
          >
            + Añadir imagen
          </button>
        </div>
      )}

      <Button type="submit" disabled={submitting}>
        {submitting
          ? "Guardando…"
          : isEdit
          ? "Guardar cambios"
          : "Crear producto"}
      </Button>
    </form>
  );
}
