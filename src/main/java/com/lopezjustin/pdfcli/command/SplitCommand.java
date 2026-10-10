package com.lopezjustin.pdfcli.command;

import com.lopezjustin.pdfcli.exception.PdfProcessingException;
import com.lopezjustin.pdfcli.service.SplitService;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

import static picocli.CommandLine.*;

@Command(
        name = "split",
        description = "%nDivide un PDF extrayendo rangos de páginas o en un archivo por página%n",
        mixinStandardHelpOptions = true,
        footer = "%nEjemplos:%n" +
                "  pdfcli split entrada.pdf -r 1-5,8,10- -o salida.pdf%n" +
                "  pdfcli split entrada.pdf -d salida/%n"
)
public class SplitCommand implements Runnable {

    @Parameters(
            arity = "1",
            paramLabel = "input.pdf",
            description = "PDF de entrada a dividir"
    )
    private File inputFile;

    @ArgGroup(exclusive = true, multiplicity = "1")
    private ModeSplit mode;

    /**
     * Modos mutuamente exclusivos:
     * - Extraer rangos (-r + -o)
     * - Dividir a un directorio (-d).
     */
    static class ModeSplit {
        @ArgGroup(exclusive = false, multiplicity = "1")
        Ranges ranges;

        @ArgGroup(multiplicity = "1")
        Directory directory;
    }

    static class Ranges {
        @Option(
                names = {"-r", "--rango"},
                required = true,
                paramLabel = "rangos",
                description = "Páginas o rangos a extraer (1-based, p. ej. 1-5,8,10-)"
        )
        String expresion;

        @Option(
                names = {"-o", "--output"},
                required = true,
                paramLabel = "output.pdf",
                description = "PDF resultante con las páginas extraídas"
        )
        File outputFile;
    }

    static class Directory {
        @Option(
                names = {"-d", "--directorio"},
                paramLabel = "dir",
                description = "Directorio de salida: un PDF por página (se crea si no existe)"
        )
        File outputDir;
    }

    private final SplitService splitService = new SplitService();

    @Override
    public void run() {
        try {
            if (mode.ranges != null) {
                extraerRangos();
            } else {
                dividirEnPaginas();
            }
        } catch (PdfProcessingException e) {
            System.err.println("\n✗ Error: " + e.getMessage());

            throw new ExecutionException(
                    new CommandLine(this),
                    e.getMessage(), e
            );
        }
    }

    private void extraerRangos() throws PdfProcessingException {
        System.out.println("Extrayendo páginas de " + inputFile.getName() + " (" + mode.ranges.expresion + ")...");
        System.out.println("  → " + mode.ranges.outputFile.getName());

        this.splitService.splitRanges(inputFile, mode.ranges.expresion, mode.ranges.outputFile);

        System.out.println("\n✓ PDF generado exitosamente: " + mode.ranges.outputFile.getAbsolutePath());
    }

    private void dividirEnPaginas() throws PdfProcessingException {
        System.out.println("Dividiendo " + inputFile.getName() + " en archivos por página...");

        List<File> generados = splitService.splitAll(inputFile, mode.directory.outputDir);

        System.out.println("\n✓ " + generados.size() + " PDFs generados en: " + mode.directory.outputDir.getAbsolutePath());
    }
}
