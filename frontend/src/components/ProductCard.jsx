import { useState } from "react";
import { Link } from "react-router-dom";
import { formatPrice } from "../utils/formatPrice";
import { useWishlist } from "../hooks/useWishlist";
import { cartService } from "../services/cartService";
import { HeartIcon, CartIcon } from "./icons";

// Tarjeta de producto con corazón (favoritos) y botón de añadir al carrito.
// Los botones van FUERA del Link para no anidar elementos interactivos.
export default function ProductCard({ product }) {
  const { isFavorite, toggleFavorite } = useWishlist();
  const favorite = isFavorite(product.id);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  const handleToggleFav = async () => {
    try {
      await toggleFavorite(product.id);
    } catch {
      /* si falla, no rompemos la UI */
    }
  };

  const handleAddToCart = async () => {
    setAdding(true);
    try {
      await cartService.addItem(product.id, 1);
      setAdded(true);
      setTimeout(() => setAdded(false), 1500);
    } catch {
      /* noop */
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="group relative flex flex-col overflow-hidden rounded-xl border border-slate-200 bg-white transition hover:-translate-y-0.5 hover:shadow-md">
      <button
        onClick={handleToggleFav}
        aria-label={favorite ? "Quitar de favoritos" : "Añadir a favoritos"}
        className={`absolute right-2 top-2 z-10 grid h-9 w-9 place-items-center rounded-full bg-white/90 shadow-sm backdrop-blur transition-colors ${
          favorite ? "text-red-500" : "text-slate-400 hover:text-red-500"
        }`}
      >
        <HeartIcon filled={favorite} />
      </button>

      <Link to={`/products/${product.id}`} className="flex flex-1 flex-col">
        <div className="aspect-square overflow-hidden bg-slate-100">
          {product.imageUrls?.[0] ? (
            <img
              src={product.imageUrls[0]}
              alt={product.name}
              loading="lazy"
              className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-sm text-slate-300">
              Sin imagen
            </div>
          )}
        </div>
        <div className="flex flex-1 flex-col p-4 pb-2">
          <h3 className="line-clamp-1 font-medium text-slate-900">{product.name}</h3>
          {product.brand && <p className="text-sm text-slate-500">{product.brand}</p>}
          <p className="mt-2 font-display text-lg font-bold text-brand">
            {formatPrice(product.price)}
          </p>
        </div>
      </Link>

      <div className="px-4 pb-4">
        <button
          onClick={handleAddToCart}
          disabled={adding}
          className={`flex w-full items-center justify-center gap-2 rounded-lg py-2 text-sm font-semibold transition-colors disabled:opacity-60 ${
            added ? "bg-emerald-100 text-emerald-700" : "bg-brand text-white hover:bg-brand-dark"
          }`}
        >
          {added ? (
            "Añadido ✓"
          ) : (
            <>
              <CartIcon /> Añadir
            </>
          )}
        </button>
      </div>
    </div>
  );
}
