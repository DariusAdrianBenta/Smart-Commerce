import axiosClient from "../api/axiosClient";

// Categorías raíz (público). Se usan para el filtro del catálogo.
export const categoryService = {
  getRoot: () => axiosClient.get("/categories").then((res) => res.data),

  // Todas las categorías (endpoint de admin). Se usa para poblar los
  // desplegables de "categoría padre" y "categoría del producto".
  getAllForAdmin: () =>
    axiosClient.get("/categories/admin").then((res) => res.data),

  // Crea una categoría (solo ADMIN). payload: { name, parentId? }.
  create: (payload) =>
    axiosClient.post("/categories/admin", payload).then((res) => res.data),

  // Cambia la visibilidad de una categoría (solo ADMIN). visible=false la oculta
  // en cascada (también sus productos).
  setVisibility: (id, visible) =>
    axiosClient
      .patch(`/categories/admin/${id}/visibility`, { visible })
      .then((res) => res.data),

  // Actualiza una categoría (solo ADMIN). payload: { name, parentId? }.
  update: (id, payload) =>
    axiosClient.put(`/categories/admin/${id}`, payload).then((res) => res.data),
};
