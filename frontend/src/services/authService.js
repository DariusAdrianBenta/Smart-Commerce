import axiosClient from "../api/axiosClient";

// Capa de servicio: los componentes llaman aquí, nunca a axios directamente.
export const authService = {
  login: (credentials) =>
    axiosClient.post("/auth/login", credentials).then((res) => res.data),
  register: (data) =>
    axiosClient.post("/auth/register", data).then((res) => res.data),
};
