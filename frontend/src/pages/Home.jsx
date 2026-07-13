import { useAuth } from "../hooks/useAuth";

export default function Home() {
  const { user } = useAuth();
  return (
    <div className="p-8">
      <h1 className="font-display text-3xl font-bold text-slate-900">
        Bienvenido a SmartCommerce
      </h1>
      <p className="mt-2 text-slate-600">
        Sesión iniciada como <strong>{user?.email}</strong>.
      </p>
    </div>
  );
}
