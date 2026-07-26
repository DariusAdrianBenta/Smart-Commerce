import { useState, useEffect } from "react";
import { wishlistService } from "../services/wishlistService";
import { useWishlist } from "../hooks/useWishlist";
import ProductCard from "../components/ProductCard";
import Spinner from "../components/Spinner";
import EmptyState from "../components/EmptyState";

export default function Favorites() {
  const { favoriteIds } = useWishlist();
  const [products, setProducts] = useState(null);
  const [loading, setLoading] = useState(true);
  // Se incrementa tras borrar/editar un producto (admin) para forzar recarga.
  const [refreshKey, setRefreshKey] = useState(0);

  // Recarga cuando cambian los favoritos (p. ej. al quitar uno desde aquí).
  useEffect(() => {
    setLoading(true);
    wishlistService
      .getMyWishlist()
      .then(setProducts)
      .catch(() => setProducts([]))
      .finally(() => setLoading(false));
  }, [favoriteIds, refreshKey]);

  return (
    <div className="p-8">
      <h1 className="font-display text-2xl font-bold text-slate-900">Mis Favoritos</h1>

      <div className="mt-6">
        {loading ? (
          <Spinner />
        ) : !products || products.length === 0 ? (
          <EmptyState message="Aún no tienes productos favoritos. Pulsa el corazón en un producto para guardarlo aquí." />
        ) : (
          <div className="grid grid-cols-2 gap-5 md:grid-cols-3 lg:grid-cols-4">
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onChanged={() => setRefreshKey((k) => k + 1)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
