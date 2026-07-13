import { createContext, useState } from "react";
import { authService } from "../services/authService";

export const AuthContext = createContext(null);

// Estado de sesión global. Se inicializa desde localStorage para que la sesión
// sobreviva a recargas de página.
export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  });

  // Guarda la sesión (token + datos de usuario) a partir de un AuthResponse.
  // Lo usan tanto login como register (mismo formato de respuesta).
  const persistSession = (data) => {
    const userData = { id: data.userId, email: data.email, role: data.role };
    localStorage.setItem("token", data.token);
    localStorage.setItem("user", JSON.stringify(userData));
    setToken(data.token);
    setUser(userData);
  };

  const login = async (credentials) => {
    const data = await authService.login(credentials);
    persistSession(data);
    return data;
  };

  const register = async (payload) => {
    const data = await authService.register(payload);
    persistSession(data);
    return data;
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{ user, token, login, register, logout, isAuthenticated: !!token }}
    >
      {children}
    </AuthContext.Provider>
  );
}
