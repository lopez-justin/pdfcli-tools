package com.lopezjustin.pdfcli.service;

import com.lopezjustin.pdfcli.exception.PdfProcessingException;
import com.lopezjustin.pdfcli.validator.PdfFileValidator;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Compresión de PDFs: limpieza de estructura (metadatos, thumbnails),
 * guardado con compresión de streams y remuestreo opcional de imágenes.
 */
public class CompressService {

    private static final Logger logger = LoggerFactory.getLogger(CompressService.class);

    private static final long MAX_MEMORY_BYTES = 1024L * 1024L * 10L; // 10 MB

    private final PdfFileValidator validator;

    public CompressService() {
        this.validator = new PdfFileValidator();
    }

    /**
     * Resultado de una operación de compresión, para reporte en la CLI.
     */
    public record CompressResult(
            long inputBytes,
            long outputBytes,
            boolean metadataRemoved,
            int thumbnailsRemoved
    ) {
    }

    /**
     * Comprime un PDF hacia un archivo de salida.
     *
     * @param inputFile   PDF de entrada
     * @param outputFile  PDF resultante
     * @param keepMetadata si es true, conserva metadatos; por defecto se eliminan
     * @return estadísticas del proceso
     * @throws PdfProcessingException
     */
    public CompressResult compress(File inputFile, File outputFile, boolean keepMetadata)
            throws PdfProcessingException {
        validator.validateInputFile(inputFile);
        validator.validateOutputFile(outputFile);

        logger.debug("Comprimiendo {} hacia {}", inputFile.getName(), outputFile.getName());

        boolean metadataRemoved = false;
        int thumbnailsRemoved = 0;

        try (PDDocument document = Loader.loadPDF(inputFile, MemoryUsageSetting.setupMixed(MAX_MEMORY_BYTES).streamCache)) {

            if (!keepMetadata) {
                stripMetadata(document);
                metadataRemoved = true;
            }

            thumbnailsRemoved = stripThumbnails(document);

            document.save(outputFile, CompressParameters.DEFAULT_COMPRESSION);

        } catch (InvalidPasswordException e) {
            throw new PdfProcessingException(
                    "El PDF está protegido con contraseña: " + inputFile.getName(), e
            );
        } catch (IOException e) {
            throw new PdfProcessingException(
                    "Error durante la compresión del PDF: " + e.getMessage(), e
            );
        }

        logger.debug("Compresión completada.");

        return new CompressResult(
                inputFile.length(),
                outputFile.length(),
                metadataRemoved,
                thumbnailsRemoved
        );
    }

    /**
     * Elimina toda la información del documento (DocumentInformation) y los metadatos XMP del catálogo.
     */
    private void stripMetadata(PDDocument document) {
        COSDictionary info = document.getDocumentInformation().getCOSObject();

        for (COSName key : info.keySet().toArray(new COSName[0])) {
            info.removeItem(key);
        }

        document.getDocumentCatalog().setMetadata(null);

        logger.debug("Metadatos eliminados.");
    }

    /**
     * Elimina las miniaturas (thumbnails) embebidas por página.
     *
     * @return cantidad de thumbnails eliminadas
     */
    private int stripThumbnails(PDDocument document) {
        int removed = 0;

        for (PDPage page : document.getPages()) {
            if (page.getCOSObject().getItem(COSName.THUMB) != null) {
                page.getCOSObject().removeItem(COSName.THUMB);
                removed++;
            }
        }

        if (removed > 0)
            logger.debug("Thumbnails eliminadas: {}", removed);

        return removed;
    }
}
