package com.lopezjustin.pdfcli.service;

import com.lopezjustin.pdfcli.exception.PdfProcessingException;
import com.lopezjustin.pdfcli.service.PageRangeParser.PageRange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageRangeParserTest {

    // ── Casos válidos ─────────────────────────────────────────────────────

    @Test
    void paginaUnica() throws Exception {
        assertEquals(List.of(new PageRange(5, 5)), PageRangeParser.parse("5", 12));
    }

    @Test
    void rangoCerrado() throws Exception {
        assertEquals(List.of(new PageRange(1, 5)), PageRangeParser.parse("1-5", 12));
    }

    @Test
    void rangoDeUnaPagina() throws Exception {
        assertEquals(List.of(new PageRange(3, 3)), PageRangeParser.parse("3-3", 12));
    }

    @Test
    void rangoAbiertoResuelveHastaElFinal() throws Exception {
        assertEquals(List.of(new PageRange(10, 12)), PageRangeParser.parse("10-", 12));
    }

    @Test
    void mezclaDeSintaxis() throws Exception {
        List<PageRange> result = PageRangeParser.parse("1-5,8,10-", 12);
        assertEquals(List.of(
                new PageRange(1, 5),
                new PageRange(8, 8),
                new PageRange(10, 12)
        ), result);
    }

    @Test
    void preservaOrdenYPermiteRepetidos() throws Exception {
        List<PageRange> result = PageRangeParser.parse("3,1,3", 12);
        assertEquals(List.of(
                new PageRange(3, 3),
                new PageRange(1, 1),
                new PageRange(3, 3)
        ), result);
    }

    @Test
    void toleraEspacios() throws Exception {
        List<PageRange> result = PageRangeParser.parse(" 1 - 5 , 8 ", 12);
        assertEquals(List.of(
                new PageRange(1, 5),
                new PageRange(8, 8)
        ), result);
    }

    // ── Casos inválidos ───────────────────────────────────────────────────

    @Test
    void exprecionNulaOVacia() {
        assertThrows(PdfProcessingException.class, () -> PageRangeParser.parse(null, 12));
        assertThrows(PdfProcessingException.class, () -> PageRangeParser.parse("", 12));
        assertThrows(PdfProcessingException.class, () -> PageRangeParser.parse("   ", 12));
    }

    @Test
    void documentoSinPaginas() {
        assertThrows(PdfProcessingException.class, () -> PageRangeParser.parse("1", 0));
    }

    @Test
    void paginaCero() {
        PdfProcessingException e = assertThrows(PdfProcessingException.class,
                () -> PageRangeParser.parse("0", 12));
        assertTrue(e.getMessage().contains("1..12"));
    }

    @Test
    void paginaFueraDeRango() {
        PdfProcessingException e = assertThrows(PdfProcessingException.class,
                () -> PageRangeParser.parse("13", 12));
        assertTrue(e.getMessage().contains("1..12"));
    }

    @Test
    void inicioMayorQueFin() {
        PdfProcessingException e = assertThrows(PdfProcessingException.class,
                () -> PageRangeParser.parse("5-3", 12));
        assertTrue(e.getMessage().contains("5-3"));
    }

    @Test
    void rangoCerradoExcedeElDocumento() {
        PdfProcessingException e = assertThrows(PdfProcessingException.class,
                () -> PageRangeParser.parse("1-13", 12));
        assertTrue(e.getMessage().contains("excede"));
    }

    @Test
    void rangoAbiertoFueraDeRango() {
        PdfProcessingException e = assertThrows(PdfProcessingException.class,
                () -> PageRangeParser.parse("13-", 12));
        assertTrue(e.getMessage().contains("excede"));
    }

    @Test
    void textoNoNumerico() {
        PdfProcessingException e = assertThrows(PdfProcessingException.class,
                () -> PageRangeParser.parse("abc", 12));
        assertTrue(e.getMessage().contains("inválido"));
    }

    @Test
    void formatoDesconocido() {
        assertThrows(PdfProcessingException.class, () -> PageRangeParser.parse("-5", 12));
        assertThrows(PdfProcessingException.class, () -> PageRangeParser.parse("--", 12));
        assertThrows(PdfProcessingException.class, () -> PageRangeParser.parse("1-5-9", 12));
        assertThrows(PdfProcessingException.class, () -> PageRangeParser.parse("1,", 12));
        assertThrows(PdfProcessingException.class, () -> PageRangeParser.parse(",", 12));
    }
}
