package com.lopezjustin.pdfcli.service;

import com.lopezjustin.pdfcli.exception.PdfProcessingException;
import com.lopezjustin.pdfcli.validator.PdfFileValidator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Conversión de imágenes (PNG, JPEG, BMP, GIF) a un PDF de páginas A4.
 */
public class ConvertService {

    private static final Logger logger = LoggerFactory.getLogger(ConvertService.class);

    /** Margen fijo alrededor de la imagen dentro de la página A4, en puntos (~7 mm). */
    private static final float MARGIN_POINTS = 20f;

    private final PdfFileValidator validator;

    public ConvertService() {
        this.validator = new PdfFileValidator();
    }

    /**
     * Convierte una lista de imágenes en un PDF: una página A4 por imagen,
     * en el orden dado. Las imágenes grandes se reducen para caber (sin ampliar);
     * las apaisadas usan página A4 horizontal.
     *
     * @param imageFiles imágenes de entrada (PNG, JPEG, BMP o GIF)
     * @param outputFile PDF resultante
     * @throws PdfProcessingException
     */
    public void convertImagesToPdf(List<File> imageFiles, File outputFile) throws PdfProcessingException {
        for (File imageFile : imageFiles) {
            validator.validateImageFile(imageFile);
        }
        validator.validateOutputFile(outputFile);

        logger.debug("Convirtiendo {} imágenes hacia {}", imageFiles.size(), outputFile.getName());

        try (PDDocument document = new PDDocument()) {

            for (File imageFile : imageFiles) {
                PDImageXObject image = loadImage(imageFile, document);

                PDRectangle pageBounds = a4For(image);
                PDPage page = new PDPage(pageBounds);
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    drawFitted(content, image, pageBounds);
                }

                logger.debug("Agregada página A4 desde: {}", imageFile.getName());
            }

            document.save(outputFile, CompressParameters.DEFAULT_COMPRESSION);

        } catch (IOException e) {
            throw new PdfProcessingException(
                    "Error durante la conversión de imágenes a PDF: " + e.getMessage(), e
            );
        }

        logger.debug("Conversión completada.");
    }

    private PDImageXObject loadImage(File imageFile, PDDocument document) throws PdfProcessingException {
        try {
            return PDImageXObject.createFromFileByContent(imageFile, document);
        } catch (IOException e) {
            throw new PdfProcessingException(
                    "No se pudo leer la imagen (formato no soportado o archivo corrupto): " + imageFile.getName(), e
            );
        }
    }

    private static PDRectangle a4For(PDImageXObject image) {
        if (image.getWidth() > image.getHeight())
            return new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());

        return PDRectangle.A4;
    }

    /**
     * Dibuja la imagen centrada en la página: escala para caber dentro de los márgenes,
     * sin ampliar más allá de su tamaño nativo.
     */
    private static void drawFitted(PDPageContentStream content, PDImageXObject image, PDRectangle page) throws IOException {
        float usableWidth = page.getWidth() - 2 * MARGIN_POINTS;
        float usableHeight = page.getHeight() - 2 * MARGIN_POINTS;

        float scale = Math.min(1f, Math.min(
                usableWidth / image.getWidth(),
                usableHeight / image.getHeight()));

        float width = image.getWidth() * scale;
        float height = image.getHeight() * scale;
        float x = (page.getWidth() - width) / 2;
        float y = (page.getHeight() - height) / 2;

        content.drawImage(image, x, y, width, height);
    }
}
