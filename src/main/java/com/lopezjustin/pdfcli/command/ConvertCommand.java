package com.lopezjustin.pdfcli.command;

import com.lopezjustin.pdfcli.exception.PdfProcessingException;
import com.lopezjustin.pdfcli.service.ConvertService;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

import static picocli.CommandLine.*;

@Command(
        name = "convert",
        description = "%nConvierte imágenes (PNG, JPEG, BMP, GIF) a un PDF con páginas A4%n",
        mixinStandardHelpOptions = true,
        footer = "%nEjemplos:%n" +
                "  pdfcli convert foto.png -o foto.pdf%n" +
                "  pdfcli convert portada.jpg contenido.png -o documento.pdf%n" +
                "  pdfcli convert escaneo01.png escaneo02.png -o escaneo.pdf%n"
)
public class ConvertCommand implements Runnable {

    @Parameters(
            arity = "1..*",
            paramLabel = "imagen",
            description = "Imágenes de entrada en el orden deseado (PNG, JPEG, BMP o GIF)"
    )
    private List<File> imageFiles;

    @Option(
            names = {"-o", "--output"},
            required = true,
            paramLabel = "output.pdf",
            description = "PDF resultante con una página A4 por imagen"
    )
    private File outputFile;

    private final ConvertService convertService = new ConvertService();

    @Override
    public void run() {
        System.out.println("Convirtiendo " + imageFiles.size() + " imagen(es) a PDF...");
        imageFiles.forEach(f -> System.out.println("  + " + f.getName()));
        System.out.println("  → " + outputFile.getName());

        try {
            convertService.convertImagesToPdf(imageFiles, outputFile);
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
