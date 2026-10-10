package com.lopezjustin.pdfcli.command;

import com.lopezjustin.pdfcli.exception.PdfProcessingException;
import com.lopezjustin.pdfcli.service.CompressService;
import com.lopezjustin.pdfcli.service.CompressService.CompressResult;
import picocli.CommandLine;

import java.io.File;

import static picocli.CommandLine.*;

@Command(
        name = "compress",
        description = "%nReduce el tamaño de un PDF limpiando metadatos%n",
        mixinStandardHelpOptions = true,
        footer = "%nEjemplos:%n" +
                "  pdfcli compress entrada.pdf -o salida.pdf%n" +
                "  pdfcli compress entrada.pdf -o salida.pdf --keep-metadata%n"
)
public class CompressCommand implements Runnable {

    @Parameters(
            arity = "1",
            paramLabel = "input.pdf",
            description = "PDF de entrada a comprimir"
    )
    private File inputFile;

    @Option(
            names = {"-o", "--output"},
            required = true,
            paramLabel = "output.pdf",
            description = "PDF resultante comprimido"
    )
    private File outputFile;

    @Option(
            names = {"--keep-metadata"},
            description = "Conservar los metadatos del documento (por defecto se eliminan)"
    )
    private boolean keepMetadata;


    private final CompressService compressService = new CompressService();

    @Override
    public void run() {
        System.out.println("Comprimiendo " + inputFile.getName() + " (" + formatSize(inputFile.length()) + ")...");

        try {
            CompressResult result = compressService.compress(inputFile, outputFile, keepMetadata);

            if (result.metadataRemoved())
                System.out.println("  · Metadatos eliminados");
            else
                System.out.println("  · Metadatos conservados");

            if (result.thumbnailsRemoved() > 0)
                System.out.println("  · Thumbnails eliminadas: " + result.thumbnailsRemoved());

            long inputBytes = result.inputBytes();
            long outputBytes = result.outputBytes();
            long diff = outputBytes - inputBytes;
            double percent = inputBytes > 0 ? (diff * 100.0) / inputBytes : 0; // avoid division by zero

            System.out.println("\n✓ PDF generado: " + outputFile.getAbsolutePath()
                    + " (" + formatSize(outputBytes) + ", "
                    + String.format("%+.1f%%", percent) + ")");

            if (diff > 0)
                System.out.println("  ⚠ El resultado es más grande que el original, el PDF ya estaba bien comprimido");

        } catch (PdfProcessingException e) {
            System.err.println("\n✗ Error: " + e.getMessage());

            throw new ExecutionException(
                    new CommandLine(this),
                    e.getMessage(), e
            );
        }
    }

    private static String formatSize(long bytes) {
        if (bytes >= 1024L * 1024L) // 1 MB
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0)); // format the size in MB with 2 decimal places

        return String.format("%.1f KB", bytes / 1024.0); // format the size in KB with 1 decimal place
    }
}
