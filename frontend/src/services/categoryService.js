import axiosClient from "../api/axiosClient";

// Categorías raíz (público). Se usan para el filtro del catálogo.
export const categoryService = {
  getRoot: () => axiosClient.get("/categories").then((res) => res.data),
};
