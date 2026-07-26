import axiosClient from "../api/axiosClient";

// Los productos vienen paginados (formato Page de Spring):
// { content: [...], totalElements, totalPages, number, size }
export const productService = {
  getAll: (params = {}) =>
    axiosClient.get("/products", { params }).then((res) => res.data),
  getById: (id) =>
    axiosClient.get(`/products/${id}`).then((res) => res.data),

  // Crea un producto (solo ADMIN).
  // payload: { name, price, stock, categoryId, description?, brand?, imageUrls? }.
  create: (payload) =>
    axiosClient.post("/admin/products", payload).then((res) => res.data),

  // Actualiza un producto existente (solo ADMIN). Campos opcionales.
  update: (id, payload) =>
    axiosClient.put(`/admin/products/${id}`, payload).then((res) => res.data),

  // Borrado lógico de un producto (solo ADMIN). Responde 204 sin cuerpo.
  remove: (id) => axiosClient.delete(`/admin/products/${id}`),
};
