package com.lopezjustin.pdfcli.service;

import com.lopezjustin.pdfcli.exception.PdfProcessingException;
import com.lopezjustin.pdfcli.validator.PdfFileValidator;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MergeService {

    private static final Logger logger = LoggerFactory.getLogger(MergeService.class);

    private final PdfFileValidator validator;

    public MergeService() {
        this.validator = new PdfFileValidator();
    }

    /**
     * Une una lista de archivos pdf en un único archivo de salida.
     * @param inputFiles
     * @param outputFile
     * @throws PdfProcessingException
     */
    public void merge(List<File> inputFiles, File outputFile) throws PdfProcessingException {

        // 1. Validar archivos de entrada y salida
        logger.debug("Validando {} archivos de entrada...", inputFiles.size());
        validator.validateInputFiles(inputFiles);
        validator.validateOutputFile(outputFile);

        // 2. Ejecutar el merge con PdfBox
        logger.debug("Iniciando merge hacia: {}", outputFile.getAbsoluteFile());

        PDFMergerUtility merger = new PDFMergerUtility();

        try {
            for (File inputFile : inputFiles) {
                // Agregar cada archivo al merger
                logger.debug("Agregando: {}", inputFile.getName());
                merger.addSource(inputFile);
            }

            // Definir el archivo de destino
            merger.setDestinationFileName(outputFile.getAbsolutePath());

            // Ejecutar el merge
            merger.mergeDocuments(
                    MemoryUsageSetting
                            .setupMixed(1024 * 1024 * 10) // Usar hasta 10 MB de memoria antes de usar disco
                            .streamCache
            );

            logger.debug("Merge completado exitosamente.");

        } catch (IOException e) {
            throw new PdfProcessingException(
                    "Error durante el merge de los PDFs: " + e.getMessage(), e
            );
        }

    }

}
