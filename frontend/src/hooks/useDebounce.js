import { useState, useEffect } from "react";

// Devuelve el valor "retrasado": solo se actualiza cuando el usuario deja de
// escribir durante `delay` ms. Evita lanzar una petición por cada tecla.
export function useDebounce(value, delay = 350) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer); // limpia el temporizador anterior
  }, [value, delay]);

  return debounced;
}
