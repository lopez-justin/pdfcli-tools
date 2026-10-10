package com.lopezjustin.pdfcli.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompressServiceTest {

    @TempDir
    Path tempDir;

    private final CompressService service = new CompressService();

    // ── Metadatos ─────────────────────────────────────────────────────────

    @Test
    void metadatosEliminadosPorDefecto() throws Exception {
        File input = createPdf("entrada.pdf", 1, "Mi título", "Mi autor");
        File output = tempDir.resolve("salida.pdf").toFile();

        CompressService.CompressResult result = service.compress(input, output, false);

        assertTrue(result.metadataRemoved());

        try (PDDocument reloaded = Loader.loadPDF(output)) {
            assertNull(reloaded.getDocumentInformation().getTitle());
            assertNull(reloaded.getDocumentInformation().getAuthor());
            assertNull(reloaded.getDocumentCatalog().getMetadata());
        }
    }

    @Test
    void keepMetadataConservaMetadatos() throws Exception {
        File input = createPdf("entrada.pdf", 1, "Mi título", "Mi autor");
        File output = tempDir.resolve("salida.pdf").toFile();

        CompressService.CompressResult result = service.compress(input, output, true);

        assertFalse(result.metadataRemoved());

        try (PDDocument reloaded = Loader.loadPDF(output)) {
            assertEquals("Mi título", reloaded.getDocumentInformation().getTitle());
            assertEquals("Mi autor", reloaded.getDocumentInformation().getAuthor());
        }
    }

    // ── Estructura ────────────────────────────────────────────────────────

    @Test
    void paginasConservadas() throws Exception {
        File input = createPdf("entrada.pdf", 3, null, null);
        File output = tempDir.resolve("salida.pdf").toFile();

        service.compress(input, output, false);

        try (PDDocument reloaded = Loader.loadPDF(output)) {
            assertEquals(3, reloaded.getNumberOfPages());
        }
    }


    // ── Helpers ───────────────────────────────────────────────────────────

    private File createPdf(String fileName, int pages, String title, String author) throws Exception {
        File file = tempDir.resolve(fileName).toFile();

        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(100, 700);
                    content.showText("Página " + (i + 1));
                    content.endText();
                }
            }

            if (title != null)
                document.getDocumentInformation().setTitle(title);

            if (author != null)
                document.getDocumentInformation().setAuthor(author);

            document.save(file);
        }

        return file;
    }

}
