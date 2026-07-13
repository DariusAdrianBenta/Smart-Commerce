import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// El backend habilita CORS, por lo que el front llama directamente a VITE_API_URL
// (no se usa el proxy de desarrollo de Vite).
// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
});
