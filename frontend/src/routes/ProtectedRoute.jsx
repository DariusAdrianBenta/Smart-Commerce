import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

// Protege las rutas privadas: sin sesión, redirige al login.
export default function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}
