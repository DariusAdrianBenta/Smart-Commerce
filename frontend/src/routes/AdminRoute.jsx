import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

// Restringe una ruta a usuarios con rol ADMIN. Si no lo son, redirige al
// inicio. Se usa DENTRO de ProtectedRoute, que ya garantiza que hay sesión.
export default function AdminRoute({ children }) {
  const { user } = useAuth();
  return user?.role === "ADMIN" ? children : <Navigate to="/" replace />;
}
