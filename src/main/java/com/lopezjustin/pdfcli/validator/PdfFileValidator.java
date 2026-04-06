package com.lopezjustin.pdfcli.validator;


import com.lopezjustin.pdfcli.exception.PdfProcessingException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Validaciones reutilizables para archivos PDF
 */
public class PdfFileValidator {

    // Firma de un archivo pdf válido.
    // PDF bien formado empieza con estos bytes: "%PDF"
    private static final byte[] PDF_MAGIC_BYTES = {0x25, 0x50, 0x44, 0x46}; //%PDF

    /**
     * Valida una lista completa de archivos de entrada.
     * @param files
     * @throws PdfProcessingException
     */
    public void validateInputFiles(List<File> files) throws PdfProcessingException {
        if (files == null || files.isEmpty())
            throw new PdfProcessingException("Debes proporcionar al menos 2 archivos PDF para unir");


        if (files.size() < 2)
            throw new PdfProcessingException("Se necesitan al menos 2 archivos PDF para realizar el merge.");

        for (File file : files) {
            validateSingleFile(file);
        }
    }

    /**
     * Valida un único archivo PDF, verificando su existencia, tipo, permisos y firma.
     * @param file
     * @throws PdfProcessingException
     */
    private void validateSingleFile(File file) throws PdfProcessingException {
        if (!file.exists())
            throw new PdfProcessingException("Archivo no encontrado: " + file.getAbsolutePath());

        if (!file.isFile())
            throw new PdfProcessingException("La ruta no corresponde a un archivo: " + file.getAbsolutePath());

        if (!file.canRead())
            throw new PdfProcessingException("Sin permiso de lectura sobre: " + file.getAbsolutePath());

        if (file.length() == 0)
            throw new PdfProcessingException("El archivo está vacío:" + file.getName());

        validatePdfSignature(file);
    }

    /**
     * Valida el archivo de salida (el PDF resultante).
     * @param outputFile
     * @throws PdfProcessingException
     */
    public void validateOutputFile(File outputFile) throws PdfProcessingException {
        File parentDir = outputFile.getParentFile();

        // Si el output tiene un directorio padre, verificar que exista
        if (parentDir != null && !parentDir.exists())
            throw new PdfProcessingException("El directorio de salida no existe: " + parentDir.getAbsolutePath());

        // Si ya existe un archivo con ese nombre, advertimos
        if (outputFile.exists() && !outputFile.canWrite())
            throw new PdfProcessingException("Sin permiso de escritura sobre el archivo: " + outputFile.getAbsolutePath());
    }

    /**
     * Lee los 4 primeros 4 bytes del archivo y comprueba que coincidan con la firma mágica de PDF.
     * @param file
     * @throws PdfProcessingException
     */
    private void validatePdfSignature(File file) throws PdfProcessingException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] header = new byte[4];
            int bytesRead = raf.read(header);

            if (bytesRead < 4)
                throw new PdfProcessingException("El archivo es demasiado pequeño para ser un PDF válido: " + file.getName());

            for (int i = 0; i < PDF_MAGIC_BYTES.length; i++) {
                if (header[i] != PDF_MAGIC_BYTES[i]) {
                    throw new PdfProcessingException("El archivo no es un PDF válido: " + file.getName());
                }
            }
        } catch (IOException e) {
            throw new PdfProcessingException("Error al leer el archivo: " + file.getName(), e);
        }

    }

}
