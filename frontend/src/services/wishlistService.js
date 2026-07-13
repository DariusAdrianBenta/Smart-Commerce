import axiosClient from "../api/axiosClient";

// Favoritos del usuario (módulo wishlist del backend).
export const wishlistService = {
  getMyWishlist: () => axiosClient.get("/wishlist").then((res) => res.data),
  add: (productId) => axiosClient.post(`/wishlist/items/${productId}`),
  remove: (productId) => axiosClient.delete(`/wishlist/items/${productId}`),
};
