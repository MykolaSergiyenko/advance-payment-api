package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PdfHelper {
    private static final Logger log = LoggerFactory.getLogger(PdfHelper.class);


    public static PDDocument loadPdf(File pdfFile) throws IOException {
        log.info("--- loadPdf file: {}", pdfFile.getName());
        try {
            return PDDocument.load(pdfFile);
        } catch (Exception e) {
            FileInputStream fileIn = new FileInputStream(pdfFile);
            log.info("--- loadPdf fileIn: {}", fileIn);

            try {
                return PDDocument.load(fileIn);
            } finally {
                try {
                    fileIn.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
    public static void closePdf(PDDocument pdf) {
        log.info("--- closePdf: {}", pdf.getDocumentInformation());

        try {
            if (null != pdf) pdf.close();
        } catch (Exception ignored) {
        }
    }

    public static void printPdf(File file) {
        PDDocument pdf = null;
        try {
            pdf = loadPdf(file);
            // print
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            closePdf(pdf);
        }
    }
}
