// Formatea un número como precio en euros (formato español): 59.99 -> "59,99 €"
export function formatPrice(value) {
  return new Intl.NumberFormat("es-ES", {
    style: "currency",
    currency: "EUR",
  }).format(value);
}
