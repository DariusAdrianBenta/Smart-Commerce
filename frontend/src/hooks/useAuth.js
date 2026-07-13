import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

// Atajo para consumir el AuthContext desde cualquier componente.
export function useAuth() {
  return useContext(AuthContext);
}
