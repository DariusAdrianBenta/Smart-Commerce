import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { productService } from "../services/productService";
import { cartService } from "../services/cartService";
import { useWishlist } from "../hooks/useWishlist";
import { formatPrice } from "../utils/formatPrice";
import { HeartIcon } from "../components/icons";
import Spinner from "../components/Spinner";

export default function ProductDetail() {
  const { id } = useParams();
  const { isFavorite, toggleFavorite } = useWishlist();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    setLoading(true);
    setError(null);
    setQuantity(1);
    productService
      .getById(id)
      .then(setProduct)
      .catch(() => setError("Producto no encontrado."))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAddToCart = async () => {
    try {
      await cartService.addItem(product.id, quantity);
      setAdded(true);
      setTimeout(() => setAdded(false), 1500);
    } catch {
      /* noop */
    }
  };

  if (loading) return <Spinner />;

  if (error) {
    return (
      <div className="p-8">
        <p className="text-red-600">{error}</p>
        <Link to="/" className="mt-4 inline-block text-sm text-brand hover:text-brand-dark">
          ← Volver al catálogo
        </Link>
      </div>
    );
  }

  const favorite = isFavorite(product.id);

  return (
    <div className="mx-auto max-w-5xl p-8">
      <Link
        to="/"
        className="mb-6 inline-flex items-center gap-1 text-sm text-slate-500 hover:text-brand"
      >
        ← Volver al catálogo
      </Link>

      <div className="grid gap-8 md:grid-cols-2">
        <div className="aspect-square overflow-hidden rounded-xl border border-slate-200 bg-slate-100">
          {product.imageUrls?.[0] && (
            <img src={product.imageUrls[0]} alt={product.name} className="h-full w-full object-cover" />
          )}
        </div>

        <div>
          {product.categoryName && (
            <span className="inline-block rounded-full bg-brand/10 px-3 py-1 text-xs font-medium text-brand-dark">
              {product.categoryName}
            </span>
          )}
          <h1 className="mt-3 font-display text-3xl font-bold text-slate-900">{product.name}</h1>
          {product.brand && <p className="mt-1 text-slate-500">{product.brand}</p>}
          <p className="mt-4 font-display text-3xl font-bold text-brand">{formatPrice(product.price)}</p>
          {product.description && <p className="mt-4 text-slate-600">{product.description}</p>}

          <div className="mt-6">
            {product.stock > 0 ? (
              <span className="rounded-full bg-emerald-50 px-3 py-1 text-sm font-medium text-emerald-700">
                En stock ({product.stock} disponibles)
              </span>
            ) : (
              <span className="rounded-full bg-red-50 px-3 py-1 text-sm font-medium text-red-700">
                Sin stock
              </span>
            )}
          </div>

          {/* Cantidad + añadir al carrito + favorito */}
          <div className="mt-6 flex flex-wrap items-center gap-3">
            <div className="flex items-center rounded-lg border border-slate-300">
              <button
                onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                className="px-3 py-2 text-lg text-slate-600 hover:bg-slate-100"
                aria-label="Disminuir cantidad"
              >
                −
              </button>
              <span className="w-10 text-center">{quantity}</span>
              <button
                onClick={() => setQuantity((q) => Math.min(product.stock, q + 1))}
                disabled={quantity >= product.stock}
                className="px-3 py-2 text-lg text-slate-600 hover:bg-slate-100 disabled:opacity-40"
                aria-label="Aumentar cantidad"
              >
                +
              </button>
            </div>

            <button
              onClick={handleAddToCart}
              disabled={product.stock === 0}
              className={`rounded-lg px-8 py-2.5 font-semibold text-white transition-colors disabled:opacity-50 ${
                added ? "bg-emerald-600" : "bg-brand hover:bg-brand-dark"
              }`}
            >
              {added ? "Añadido ✓" : "Añadir al carrito"}
            </button>

            <button
              onClick={() => toggleFavorite(product.id)}
              aria-label={favorite ? "Quitar de favoritos" : "Añadir a favoritos"}
              className={`grid h-11 w-11 place-items-center rounded-lg border transition-colors ${
                favorite
                  ? "border-red-200 bg-red-50 text-red-500"
                  : "border-slate-300 text-slate-400 hover:text-red-500"
              }`}
            >
              <HeartIcon filled={favorite} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
