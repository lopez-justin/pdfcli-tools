package com.lopezjustin.pdfcli;

import picocli.CommandLine;

public class Main {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PdfCliApp()).execute(args);
        System.exit(exitCode);
    }

}
