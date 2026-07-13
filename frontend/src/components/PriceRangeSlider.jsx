import { formatPrice } from "../utils/formatPrice";

// Estilos del "pulgar" (thumb) de cada input range. Van sueltos porque los dos
// inputs se solapan: el input entero es pointer-events-none y solo el thumb es
// clicable, así ambos controles se pueden arrastrar sin bloquearse entre sí.
const THUMB =
  "[&::-webkit-slider-thumb]:pointer-events-auto [&::-webkit-slider-thumb]:h-4 [&::-webkit-slider-thumb]:w-4 [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:cursor-pointer [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:border-2 [&::-webkit-slider-thumb]:border-brand [&::-webkit-slider-thumb]:bg-white [&::-webkit-slider-thumb]:shadow " +
  "[&::-moz-range-thumb]:pointer-events-auto [&::-moz-range-thumb]:h-4 [&::-moz-range-thumb]:w-4 [&::-moz-range-thumb]:cursor-pointer [&::-moz-range-thumb]:appearance-none [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:border-2 [&::-moz-range-thumb]:border-brand [&::-moz-range-thumb]:bg-white";

const INPUT =
  `pointer-events-none absolute top-0 h-5 w-full cursor-pointer appearance-none bg-transparent ${THUMB}`;

// Slider de rango con dos controles (mínimo y máximo).
// value = [lo, hi]; onChange recibe el nuevo par al mover cualquiera de los dos.
export default function PriceRangeSlider({ min, max, value, onChange, step = 1 }) {
  const [lo, hi] = value;

  const handleLo = (e) => {
    // el mínimo nunca puede superar al máximo (dejamos un hueco de un step)
    const next = Math.min(Number(e.target.value), hi - step);
    onChange([Math.max(min, next), hi]);
  };

  const handleHi = (e) => {
    const next = Math.max(Number(e.target.value), lo + step);
    onChange([lo, Math.min(max, next)]);
  };

  // Porcentajes para pintar el tramo seleccionado sobre la pista.
  const span = max - min || 1;
  const loPct = ((lo - min) / span) * 100;
  const hiPct = ((hi - min) / span) * 100;

  return (
    <div className="w-full max-w-xs">
      <div className="mb-2 flex items-center justify-between text-sm text-slate-600">
        <span>Precio</span>
        <span className="font-medium text-slate-900">
          {formatPrice(lo)} — {formatPrice(hi)}
        </span>
      </div>

      <div className="relative h-5">
        {/* pista de fondo */}
        <div className="absolute top-1/2 h-1 w-full -translate-y-1/2 rounded-full bg-slate-200" />
        {/* tramo activo entre los dos pulgares */}
        <div
          className="absolute top-1/2 h-1 -translate-y-1/2 rounded-full bg-brand"
          style={{ left: `${loPct}%`, right: `${100 - hiPct}%` }}
        />
        <input
          type="range"
          min={min}
          max={max}
          step={step}
          value={lo}
          onChange={handleLo}
          aria-label="Precio mínimo"
          className={INPUT}
        />
        <input
          type="range"
          min={min}
          max={max}
          step={step}
          value={hi}
          onChange={handleHi}
          aria-label="Precio máximo"
          className={INPUT}
        />
      </div>
    </div>
  );
}
