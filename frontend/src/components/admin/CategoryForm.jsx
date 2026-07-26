import { useState } from "react";
import { categoryService } from "../../services/categoryService";
import Field, { fieldClass } from "./Field";
import FormFeedback from "./FormFeedback";
import Button from "../Button";

// Formulario de creación de categoría.
// props:
//   categories -> lista para el desplegable de "categoría padre"
//   onCreated  -> callback tras crear (para refrescar la lista en la página)
export default function CategoryForm({ categories, onCreated }) {
  const [name, setName] = useState("");
  const [parentId, setParentId] = useState("");
  const [status, setStatus] = useState(null); // { type, message }
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus(null);

    // Validación en cliente: el nombre es obligatorio.
    if (!name.trim()) {
      setStatus({ type: "error", message: "El nombre es obligatorio." });
      return;
    }

    // Construimos el payload; parentId solo se envía si se ha elegido uno.
    const payload = { name: name.trim() };
    if (parentId) payload.parentId = Number(parentId);

    setSubmitting(true);
    try {
      const created = await categoryService.create(payload);
      setStatus({ type: "success", message: "Categoría creada correctamente." });
      setName("");
      setParentId("");
      onCreated?.(created);
    } catch (err) {
      // El backend responde con { timestamp, status, message }.
      const msg = err.response?.data?.message;
      setStatus({
        type: "error",
        message: `Ha habido un problema al crear la categoría: ${
          msg || "inténtalo de nuevo."
        }`,
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
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </Field>

      <Button type="submit" disabled={submitting}>
        {submitting ? "Creando…" : "Crear categoría"}
      </Button>
    </form>
  );
}
