package com.smartcommerce.common;

import java.text.Normalizer;

/**
 * Utilidad para generar slugs URL-friendly a partir de un nombre.
 * "Electrónica" -> "electronica", "Ropa de Hombre" -> "ropa-de-hombre".
 */
public final class SlugUtils {

    private SlugUtils() {
    }

    public static String toSlug(String name) {
        String normalized = Normalizer
                .normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");        // quita acentos
        return normalized.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")  // quita símbolos
                .trim()
                .replaceAll("\\s+", "-");         // espacios -> guiones
    }
}
