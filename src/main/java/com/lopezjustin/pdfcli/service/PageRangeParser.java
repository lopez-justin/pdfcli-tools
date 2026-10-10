package com.lopezjustin.pdfcli.service;

import com.lopezjustin.pdfcli.exception.PdfProcessingException;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser de expresiones de rangos de páginas con sintaxis "N", "A-B" o "N-".
 * Preserva el orden dado y permite rangos repetidos.
 */
public final class PageRangeParser {

    /**
     * Rango de páginas inclusive, basado en 1 (como se ve el usuario).
     */
    public record PageRange(int start, int end) {
    }

    private PageRangeParser() {
    }

    /**
     * Parsea una expresión de rangos y la valida contra el total de páginas.
     *
     * @param expression expresión, p. ej. "1-5,8,10-"
     * @param totalPages total de páginas del PDF (>= 1)
     * @return rangos normalizados en el orden dado
     * @throws PdfProcessingException si la expresión es inválida o excede el documento
     */
    public static List<PageRange> parse(String expression, int totalPages) throws PdfProcessingException {
        if (totalPages < 1)
            throw new PdfProcessingException("El PDF no tiene páginas que dividir.");

        if (expression == null || expression.isBlank())
            throw new PdfProcessingException("Debes indicar una expresión de rangos, p. ej. 1-5,8,10-");

        List<PageRange> ranges = new ArrayList<>();

        for (String token : expression.split(",", -1)) {
            String t = token.trim();
            if (t.isEmpty())
                throw new PdfProcessingException("Formato de rango inválido: '" + expression + "' (usa N, A-B o N-)");

            ranges.add(parseToken(t, totalPages, expression));
        }

        if (ranges.isEmpty())
            throw new PdfProcessingException("Debes indicar al menos una página o rango, p. ej. 1-5,8,10-");

        return ranges;
    }

    private static PageRange parseToken(String token, int totalPages, String expression) throws PdfProcessingException {
        int dash = token.indexOf('-');

        if (dash < 0)
            return singlePage(token, totalPages);

        String startPart = token.substring(0, dash).trim();
        String endPart = token.substring(dash + 1).trim();

        if (startPart.isEmpty() && endPart.isEmpty())
            throw new PdfProcessingException("Formato de rango inválido: '" + expression + "' (usa N, A-B o N-)");

        if (startPart.isEmpty())
            throw new PdfProcessingException("Formato de rango inválido: '" + token + "' (un rango abierto al inicio no está soportado, usa N, A-B o N-)");

        int start = parseNumber(startPart);
        int end = endPart.isEmpty() ? totalPages : parseNumber(endPart);

        if (start < 1 || start > totalPages || end > totalPages)
            throw new PdfProcessingException("El rango '" + token + "' excede el total de páginas (" + totalPages + ")");

        if (start > end)
            throw new PdfProcessingException("El rango '" + token + "' es inválido: el inicio es mayor que el fin");

        return new PageRange(start, end);
    }

    private static PageRange singlePage(String token, int totalPages) throws PdfProcessingException {
        int page = parseNumber(token);

        if (page < 1 || page > totalPages)
            throw new PdfProcessingException("La página " + page + " está fuera del rango válido (1.." + totalPages + ")");

        return new PageRange(page, page);
    }

    private static int parseNumber(String value) throws PdfProcessingException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new PdfProcessingException("Número de página inválido: '" + value + "' (usa N, A-B o N-)");
        }
    }
}
