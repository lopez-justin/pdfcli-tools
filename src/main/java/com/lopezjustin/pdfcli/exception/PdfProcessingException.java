package com.lopezjustin.pdfcli.exception;

/**
 * Excepción personalizada para errores relacionados con el procesamiento de archivos PDF.
 */
public class PdfProcessingException extends Exception {

    public PdfProcessingException(String message) {
        super(message);
    }

    public PdfProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
