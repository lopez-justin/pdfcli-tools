package com.lopezjustin.pdfcli.command;


import com.lopezjustin.pdfcli.exception.PdfProcessingException;
import com.lopezjustin.pdfcli.service.MergeService;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

import static picocli.CommandLine.*;

@Command(
        name = "merge",
        description = "Une múltiples archivos PDF en uno solo",
        mixinStandardHelpOptions = true,
        footer = "%nEjemplos:%n" +
                "  pdfcli merge archivo1.pdf archivo2.pdf -o resultado.pdf%n" +
                "  pdfcli merge doc1.pdf doc2.pdf doc3.pdf -o combinado.pdf%n"
)
public class MergeCommand implements Runnable {

    @Parameters(
            arity = "2..*", // Requiere al menos 2 archivos de entrada
            paramLabel = "input.pdf",
            description = "Archivos PDF a unir (mínimo 2, en el orden deseado)"
    )
    private List<File> inputFiles;

    @Option(
            names = {"-o", "--output"},
            required = true,
            paramLabel = "output.pdf",
            description = "Archivo PDF resultante de la unión"
    )
    private File outputFile;

    private final MergeService mergeService = new MergeService();

    @Override
    public void run() {
        System.out.println("Uniendo " + inputFiles.size() + " archivos PDF...");
        inputFiles.forEach(f -> System.out.println("  + " + f.getName()));
        System.out.println("  → " + outputFile.getName());

        try {
            mergeService.merge(inputFiles, outputFile);
            System.out.println("\n✓ PDF generado exitosamente: " + outputFile.getAbsolutePath());
        } catch (PdfProcessingException e) {
            System.err.println("\n✗ Error: " + e.getMessage());

            throw new ExecutionException(
                    new CommandLine(this),
                    e.getMessage(), e
            );
        }
    }
}
