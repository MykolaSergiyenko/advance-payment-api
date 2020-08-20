package online.oboz.trip.trip_carrier_advance_payment_api.service.util;

import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;

public interface PdfUtils {
    Logger log = LoggerFactory.getLogger(PdfUtils.class);

    static PDDocument loadPdf(Resource resource) throws IOException {
        log.info("Загрузка PDF-документа: {}", resource.getDescription());
        RandomAccessBufferedFileInputStream strm =
            new RandomAccessBufferedFileInputStream(resource.getInputStream());
        try {
            PDFParser parser = new PDFParser(strm);
            try {
                parser.parse();
            } catch (NoClassDefFoundError e) {
                throw new SecurityException("PDF-документ защищен от вскрытия.", e);
            }
            return parser.getPDDocument();
        } finally {
            strm.close();
        }
    }


    static void closePdf(PDDocument pdf) {
        //log.info("close pdf: {}", pdf.getDocumentInformation());
        try {
            if (null != pdf) pdf.close();
        } catch (Exception ignored) {
        }
    }
}
