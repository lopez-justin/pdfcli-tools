package com.lopezjustin.pdfcli.service;

import com.lopezjustin.pdfcli.exception.PdfProcessingException;
import com.lopezjustin.pdfcli.service.PageRangeParser.PageRange;
import com.lopezjustin.pdfcli.validator.PdfFileValidator;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * División de PDFs: extracción de rangos a un archivo y todas las páginas a un directorio.
 */
public class SplitService {

    private static final Logger logger = LoggerFactory.getLogger(SplitService.class);

    private static final long MAX_MEMORY_BYTES = 1024L * 1024L * 10L; // 10 MB

    private final PdfFileValidator validator;

    public SplitService() {
        this.validator = new PdfFileValidator();
    }

    /**
     * Extrae los rangos indicados (1-based, inclusivos) a un único PDF de salida.
     *
     * @param inputFile     PDF de entrada
     * @param rangeExpression expresión de rangos, p. ej. "1-5,8,10-"
     * @param outputFile    PDF resultante
     * @throws PdfProcessingException
     */
    public void splitRanges(File inputFile, String rangeExpression, File outputFile) throws PdfProcessingException {
        validator.validateInputFile(inputFile);
        validator.validateOutputFile(outputFile);

        logger.debug("Extrayendo rangos '{}' de {} hacia {}",
                rangeExpression, inputFile.getName(), outputFile.getName());

        try (PDDocument source = Loader.loadPDF(inputFile, MemoryUsageSetting.setupMixed(MAX_MEMORY_BYTES).streamCache);
             PDDocument output = new PDDocument()) {

            List<PageRange> ranges = PageRangeParser.parse(rangeExpression, source.getNumberOfPages());

            for (PageRange range : ranges) {
                for (int page = range.start(); page <= range.end(); page++) {
                    logger.debug("Agregando página {}", page);
                    output.importPage(source.getPage(page - 1));
                }
            }

            output.save(outputFile);

        } catch (IOException e) {
            throw new PdfProcessingException(
                    "Error durante el split de los PDFs: " + e.getMessage(), e
            );
        }

        logger.debug("Split de rangos completado.");
    }

    /**
     * Divide todas las páginas del PDF en archivos individuales dentro de un directorio.
     * El directorio se crea si no existe.
     *
     * @param inputFile PDF de entrada
     * @param outputDir directorio de salida
     * @return archivos generados, en orden
     * @throws PdfProcessingException
     */
    public List<File> splitAll(File inputFile, File outputDir) throws PdfProcessingException {
        validator.validateInputFile(inputFile);
        validator.validateOutputDirectory(outputDir);

        List<File> created = new ArrayList<>();

        logger.debug("Dividiendo {} en {}", inputFile.getName(), outputDir.getAbsolutePath());

        try (PDDocument source = Loader.loadPDF(inputFile, MemoryUsageSetting.setupMixed(MAX_MEMORY_BYTES).streamCache)) {

            if (!outputDir.exists() && !outputDir.mkdirs())
                throw new PdfProcessingException("No se pudo crear el directorio de salida: " + outputDir.getAbsolutePath());

            String baseName = stripExtension(inputFile.getName());

            for (int page = 1; page <= source.getNumberOfPages(); page++) {
                File pageFile = new File(outputDir, String.format("%s_p%03d.pdf", baseName, page));

                try (PDDocument singlePage = new PDDocument()) {
                    PDPage imported = source.getPage(page - 1);
                    singlePage.importPage(imported);
                    singlePage.save(pageFile);
                }

                created.add(pageFile);
                logger.debug("Generado: {}", pageFile.getName());
            }

        } catch (IOException e) {
            // No dejar archivos a medias si el split falla a mitad de camino
            for (File file : created) {
                if (file.exists() && !file.delete())
                    logger.warn("No se pudo eliminar el archivo parcial: {}", file.getName());
            }

            throw new PdfProcessingException(
                    "Error durante el split de los PDFs: " + e.getMessage(), e
            );
        }

        logger.debug("Split completado: {} archivos generados.", created.size());

        return created;
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');

        if (dot > 0)
            return fileName.substring(0, dot);

        return fileName;
    }
}
