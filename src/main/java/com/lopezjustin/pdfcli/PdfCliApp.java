package com.lopezjustin.pdfcli;

import com.lopezjustin.pdfcli.command.MergeCommand;
import picocli.CommandLine.Model.CommandSpec;

import static picocli.CommandLine.*;

@Command(
        name = "pdfcli",
        description = "A command-line application for PDF manipulation.",
        version = "pdfcli 1.0",
        mixinStandardHelpOptions = true,

        subcommands = {
                MergeCommand.class
        },

        footer = "%nEjemplos:%n" +
                "  pdfcli merge a.pdf b.pdf -o resultado.pdf%n" +
                "  pdfcli merge *.pdf -o combinado.pdf%n",

        synopsisHeading = "%nUso: ",
        descriptionHeading = "%nDescripción:%n  ",
        optionListHeading = "%nOpciones:%n",
        commandListHeading = "%nComandos disponibles:%n"
)
public class PdfCliApp implements Runnable {

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

}
