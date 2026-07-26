import { useState } from "react";
import { categoryService } from "../../services/categoryService";
import Field, { fieldClass } from "./Field";
import FormFeedback from "./FormFeedback";
import Button from "../Button";

// Formulario de creación / edición de categoría.
// props:
//   categories -> lista para el desplegable de "categoría padre"
//   category   -> si viene, el formulario está en MODO EDICIÓN (PUT)
//   onSuccess  -> callback tras crear/editar con éxito
export default function CategoryForm({ categories, category, onSuccess }) {
  const isEdit = Boolean(category);

  const [name, setName] = useState(category?.name ?? "");
  const [parentId, setParentId] = useState(
    category?.parentId != null ? String(category.parentId) : ""
  );
  const [status, setStatus] = useState(null); // { type, message }
  const [submitting, setSubmitting] = useState(false);

  // En edición, una categoría no puede ser su propio padre: la excluimos.
  const parentOptions = categories.filter((c) => c.id !== category?.id);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus(null);

    if (!name.trim()) {
      setStatus({ type: "error", message: "El nombre es obligatorio." });
      return;
    }

    // parentId se envía solo si se ha elegido uno. Al editar, reenviarlo
    // conserva el padre (el PUT lo pone a raíz si se omite).
    const payload = { name: name.trim() };
    if (parentId) payload.parentId = Number(parentId);

    setSubmitting(true);
    try {
      const saved = isEdit
        ? await categoryService.update(category.id, payload)
        : await categoryService.create(payload);
      setStatus({
        type: "success",
        message: isEdit
          ? "Categoría actualizada correctamente."
          : "Categoría creada correctamente.",
      });
      if (!isEdit) {
        setName("");
        setParentId("");
      }
      onSuccess?.(saved);
    } catch (err) {
      const msg = err.response?.data?.message;
      setStatus({
        type: "error",
        message: `Ha habido un problema al ${
          isEdit ? "actualizar" : "crear"
        } la categoría: ${msg || "inténtalo de nuevo."}`,
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <FormFeedback status={status} />

      <Field label="Nombre" required htmlFor="cat-name">
        <input
          id="cat-name"
          className={fieldClass}
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Ej. Electrónica"
        />
      </Field>

      <Field label="Categoría padre" htmlFor="cat-parent">
        <select
          id="cat-parent"
          className={fieldClass}
          value={parentId}
          onChange={(e) => setParentId(e.target.value)}
        >
          <option value="">Ninguna (categoría raíz)</option>
          {parentOptions.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </Field>

      <Button type="submit" disabled={submitting}>
        {submitting
          ? "Guardando…"
          : isEdit
          ? "Guardar cambios"
          : "Crear categoría"}
      </Button>
    </form>
  );
}
