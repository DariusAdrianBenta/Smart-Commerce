import { Routes, Route } from "react-router-dom";
import Login from "../pages/Login";
import Home from "../pages/Home";
import Favorites from "../pages/Favorites";
import MainLayout from "../layouts/MainLayout";
import ProtectedRoute from "./ProtectedRoute";

export default function AppRoutes() {
  return (
    <Routes>
      {/* Rutas públicas */}
      <Route path="/login" element={<Login />} />

      {/* Rutas privadas: envueltas en ProtectedRoute + layout con menú lateral */}
      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Home />} />
        <Route path="/favorites" element={<Favorites />} />
        {/* Productos, detalle, carrito y configuración se añaden en tareas siguientes */}
      </Route>
    </Routes>
  );
}
