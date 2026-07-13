// Indicador de carga.
export default function Spinner() {
  return (
    <div className="flex justify-center py-20">
      <div className="h-10 w-10 animate-spin rounded-full border-4 border-slate-200 border-t-brand" />
    </div>
  );
}
