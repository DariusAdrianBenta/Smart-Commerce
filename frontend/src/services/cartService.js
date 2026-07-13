import axiosClient from "../api/axiosClient";

// Carrito del usuario.
export const cartService = {
  getMyCart: () => axiosClient.get("/cart").then((res) => res.data),
  addItem: (productId, quantity = 1) =>
    axiosClient.post("/cart/items", { productId, quantity }).then((res) => res.data),
  updateItem: (productId, quantity) =>
    axiosClient.put(`/cart/items/${productId}`, { quantity }).then((res) => res.data),
  removeItem: (productId) =>
    axiosClient.delete(`/cart/items/${productId}`).then((res) => res.data),
  clear: () => axiosClient.delete("/cart"),
};
