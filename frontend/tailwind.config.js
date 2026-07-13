/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        // Identidad de marca: verde esmeralda (marketplace moderno y de confianza)
        brand: {
          DEFAULT: "#059669",
          dark: "#047857",
          deep: "#064e3b",
        },
        accent: "#D97706", // ámbar, usado con moderación
      },
      fontFamily: {
        display: ["Rubik", "system-ui", "sans-serif"],
        sans: ['"Nunito Sans"', "system-ui", "sans-serif"],
      },
    },
  },
  plugins: [],
};
