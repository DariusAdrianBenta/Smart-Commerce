// Banner de resultado tras enviar un formulario.
// status = { type: "success" | "error", message: string } | null
export default function FormFeedback({ status }) {
  if (!status) return null;

  const success = status.type === "success";

  return (
    <div
      role="status"
      className={`mb-4 flex items-start gap-2 rounded-lg border px-3.5 py-3 text-sm ${
        success
          ? "border-green-200 bg-green-50 text-green-800"
          : "border-red-200 bg-red-50 text-red-700"
      }`}
    >
      <span
        aria-hidden
        className={`mt-0.5 grid h-5 w-5 shrink-0 place-items-center rounded-full text-xs font-bold text-white ${
          success ? "bg-green-500" : "bg-red-500"
        }`}
      >
        {success ? "✓" : "!"}
      </span>
      <span>{status.message}</span>
    </div>
  );
}
