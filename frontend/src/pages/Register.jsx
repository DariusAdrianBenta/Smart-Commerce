import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import AuthLayout from "../layouts/AuthLayout";
import Input from "../components/Input";
import PasswordInput from "../components/PasswordInput";
import Button from "../components/Button";

const initialForm = {
  firstName: "",
  lastName: "",
  phone: "",
  birthDate: "",
  email: "",
  password: "",
};

export default function Register() {
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  // Un solo manejador para todos los campos: actualiza la clave correspondiente.
  const onChange = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await register(form);
      navigate("/"); // register auto-inicia sesión (devuelve token)
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "No se pudo crear la cuenta. Revisa los datos e inténtalo de nuevo."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      panelTitle={
        <>
          Únete a
          <br />
          SmartCommerce.
        </>
      }
      panelSubtitle="Crea tu cuenta y empieza a comprar en segundos."
    >
      <h2 className="font-display text-2xl font-bold text-slate-900">Crea tu cuenta</h2>
      <p className="mb-8 mt-1 text-slate-500">Es rápido y gratis.</p>

      {error && (
        <div
          className="mb-5 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
          role="alert"
        >
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div className="grid grid-cols-2 gap-x-3">
          <Input
            id="firstName"
            label="Nombre"
            autoComplete="given-name"
            value={form.firstName}
            onChange={onChange("firstName")}
            required
          />
          <Input
            id="lastName"
            label="Apellidos"
            autoComplete="family-name"
            value={form.lastName}
            onChange={onChange("lastName")}
            required
          />
        </div>
        <Input
          id="phone"
          label="Teléfono"
          type="tel"
          autoComplete="tel"
          value={form.phone}
          onChange={onChange("phone")}
          required
        />
        <Input
          id="birthDate"
          label="Fecha de nacimiento"
          type="date"
          value={form.birthDate}
          onChange={onChange("birthDate")}
          required
        />
        <Input
          id="email"
          label="Email"
          type="email"
          autoComplete="email"
          placeholder="tucorreo@ejemplo.com"
          value={form.email}
          onChange={onChange("email")}
          required
        />
        <PasswordInput
          id="password"
          autoComplete="new-password"
          placeholder="••••••••"
          hint="Mínimo 8 caracteres"
          value={form.password}
          onChange={onChange("password")}
          required
        />
        <Button type="submit" disabled={loading} className="mt-2">
          {loading ? "Creando cuenta…" : "Crear cuenta"}
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        ¿Ya tienes cuenta?{" "}
        <Link to="/login" className="font-semibold text-brand hover:text-brand-dark">
          Inicia sesión
        </Link>
      </p>
    </AuthLayout>
  );
}
