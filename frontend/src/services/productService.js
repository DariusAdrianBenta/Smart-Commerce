import axiosClient from "../api/axiosClient";

// Los productos vienen paginados (formato Page de Spring):
// { content: [...], totalElements, totalPages, number, size }
export const productService = {
  getAll: (params = {}) =>
    axiosClient.get("/products", { params }).then((res) => res.data),
  getById: (id) =>
    axiosClient.get(`/products/${id}`).then((res) => res.data),
};
