import { createContext, useState, useEffect, useCallback } from "react";
import { wishlistService } from "../services/wishlistService";
import { useAuth } from "../hooks/useAuth";

export const WishlistContext = createContext(null);

// Mantiene el conjunto de ids de productos favoritos del usuario, para que el
// corazón se muestre igual en todas las vistas (catálogo y detalle).
export function WishlistProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [favoriteIds, setFavoriteIds] = useState(() => new Set());

  // Carga los favoritos al iniciar sesión; los limpia al cerrar sesión.
  useEffect(() => {
    if (!isAuthenticated) {
      setFavoriteIds(new Set());
      return;
    }
    wishlistService
      .getMyWishlist()
      .then((products) => setFavoriteIds(new Set(products.map((p) => p.id))))
      .catch(() => setFavoriteIds(new Set()));
  }, [isAuthenticated]);

  const isFavorite = useCallback((id) => favoriteIds.has(id), [favoriteIds]);

  const toggleFavorite = useCallback(
    async (id) => {
      if (favoriteIds.has(id)) {
        await wishlistService.remove(id);
        setFavoriteIds((prev) => {
          const next = new Set(prev);
          next.delete(id);
          return next;
        });
      } else {
        await wishlistService.add(id);
        setFavoriteIds((prev) => new Set(prev).add(id));
      }
    },
    [favoriteIds]
  );

  return (
    <WishlistContext.Provider value={{ favoriteIds, isFavorite, toggleFavorite }}>
      {children}
    </WishlistContext.Provider>
  );
}
