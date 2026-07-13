import { useState, useEffect } from "react";
import { productService } from "../services/productService";
import { categoryService } from "../services/categoryService";
import { useDebounce } from "../hooks/useDebounce";
import ProductCard from "../components/ProductCard";
import Spinner from "../components/Spinner";
import EmptyState from "../components/EmptyState";

const PAGE_SIZE = 8;
const SORT_OPTIONS = [
  { value: "", label: "Destacados" },
  { value: "price,asc", label: "Precio: menor a mayor" },
  { value: "price,desc", label: "Precio: mayor a menor" },
];

function chipClass(active) {
  return `rounded-full border px-3 py-1.5 text-sm font-medium transition-colors ${
    active
      ? "border-brand bg-brand text-white"
      : "border-slate-300 text-slate-600 hover:border-brand hover:text-brand-dark"
  }`;
}

export default function Home() {
  const [search, setSearch] = useState("");
  const debouncedSearch = useDebounce(search, 350);
  const [categoryId, setCategoryId] = useState(null);
  const [sort, setSort] = useState("");
  const [page, setPage] = useState(0);

  const [categories, setCategories] = useState([]);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Categorías (una sola vez) para el filtro.
  useEffect(() => {
    categoryService.getRoot().then(setCategories).catch(() => {});
  }, []);

  // Al cambiar cualquier filtro, volvemos a la primera página.
  useEffect(() => {
    setPage(0);
  }, [debouncedSearch, categoryId, sort]);

  // Carga de productos según filtros + página (useEffect = efecto secundario).
  useEffect(() => {
    setLoading(true);
    setError(null);
    const params = { page, size: PAGE_SIZE };
    if (debouncedSearch) params.name = debouncedSearch;
    if (categoryId) params.categoryId = categoryId;
    if (sort) params.sort = sort;

    productService
      .getAll(params)
      .then(setData)
      .catch(() => setError("No se pudieron cargar los productos."))
      .finally(() => setLoading(false));
  }, [debouncedSearch, categoryId, sort, page]);

  return (
    <div className="p-8">
      <h1 className="font-display text-2xl font-bold text-slate-900">Productos</h1>

      {/* Búsqueda */}
      <div className="mt-4">
        <input
          type="search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Buscar productos…"
          className="w-full max-w-md rounded-lg border border-slate-300 px-4 py-2.5 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/30"
        />
      </div>

      {/* Filtros: categorías + orden */}
      <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap gap-2">
          <button onClick={() => setCategoryId(null)} className={chipClass(categoryId === null)}>
            Todas
          </button>
          {categories.map((c) => (
            <button key={c.id} onClick={() => setCategoryId(c.id)} className={chipClass(categoryId === c.id)}>
              {c.name}
            </button>
          ))}
        </div>
        <select
          value={sort}
          onChange={(e) => setSort(e.target.value)}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand focus:outline-none"
        >
          {SORT_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>

      {/* Resultado */}
      <div className="mt-6">
        {loading ? (
          <Spinner />
        ) : error ? (
          <p className="py-20 text-center text-red-600">{error}</p>
        ) : !data || data.content.length === 0 ? (
          <EmptyState message="No se han encontrado productos." />
        ) : (
          <>
            <div className="grid grid-cols-2 gap-5 md:grid-cols-3 lg:grid-cols-4">
              {data.content.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>

            {data.totalPages > 1 && (
              <div className="mt-8 flex items-center justify-center gap-4">
                <button
                  onClick={() => setPage((p) => p - 1)}
                  disabled={data.number === 0}
                  className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100 disabled:opacity-40"
                >
                  Anterior
                </button>
                <span className="text-sm text-slate-500">
                  Página {data.number + 1} de {data.totalPages}
                </span>
                <button
                  onClick={() => setPage((p) => p + 1)}
                  disabled={data.number + 1 >= data.totalPages}
                  className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100 disabled:opacity-40"
                >
                  Siguiente
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
