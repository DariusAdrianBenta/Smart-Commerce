import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import AuthLayout from "../layouts/AuthLayout";
import Input from "../components/Input";
import PasswordInput from "../components/PasswordInput";
import Button from "../components/Button";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login({ email, password });
      navigate("/");
    } catch {
      setError("Email o contraseña incorrectos.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      panelTitle={
        <>
          Tu marketplace,
          <br />
          más rápido y simple.
        </>
      }
      panelSubtitle="Explora miles de productos, gestiona tu carrito y compra en segundos."
    >
      <h2 className="font-display text-2xl font-bold text-slate-900">Inicia sesión</h2>
      <p className="mb-8 mt-1 text-slate-500">Bienvenido de nuevo. Nos alegra verte.</p>

      {error && (
        <div
          className="mb-5 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
          role="alert"
        >
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <Input
          id="email"
          label="Email"
          type="email"
          autoComplete="email"
          placeholder="tucorreo@ejemplo.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <PasswordInput
          id="password"
          autoComplete="current-password"
          placeholder="••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Button type="submit" disabled={loading} className="mt-2">
          {loading ? "Entrando…" : "Entrar"}
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        ¿No tienes cuenta?{" "}
        <Link to="/register" className="font-semibold text-brand hover:text-brand-dark">
          Regístrate
        </Link>
      </p>
    </AuthLayout>
  );
}
