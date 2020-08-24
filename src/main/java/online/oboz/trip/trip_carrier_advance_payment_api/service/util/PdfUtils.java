package online.oboz.trip.trip_carrier_advance_payment_api.service.util;

import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface PdfUtils {
    Logger log = LoggerFactory.getLogger(PdfUtils.class);

    static PDDocument loadPdf(Resource resource) throws IOException {
        log.info("[PDF]: Загрузка документа: {}", resource.getFilename());
        RandomAccessBufferedFileInputStream strm =
            new RandomAccessBufferedFileInputStream(resource.getInputStream());
        try {
            PDFParser parser = new PDFParser(strm);
            try {
                parser.parse();
            } catch (NoClassDefFoundError e) {
                throw new SecurityException("[PDF]: PDF-документ защищен от вскрытия.", e);
            }
            return parser.getPDDocument();
        } finally {
            strm.close();
        }
    }

    static PDDocument loadPdf(Resource resource, Integer pageNum) throws IOException {
        RandomAccessBufferedFileInputStream strm =
            new RandomAccessBufferedFileInputStream(resource.getInputStream());
        try {
            PDFParser parser = new PDFParser(strm);
            try {
                parser.parse();
            } catch (NoClassDefFoundError e) {
                throw new SecurityException("[PDF]: PDF-документ защищен от вскрытия.", e);
            }
            PDDocument doc = parser.getPDDocument();

            int numberOfPages = getPageCount(doc);

            if (pageNum < 1 || pageNum > numberOfPages) {
                throw new SecurityException("[PDF]: Номер страницы для превью должен быть меньше общего числа страниц ("
                    + pageNum + " из " + numberOfPages + ").");
            }
            log.info("[PDF]: Загрузка документа: {} (страница {} из {}).", resource.getFilename(), pageNum, numberOfPages);

            return doc;
        } finally {
            strm.close();
        }
    }

    static int getPageCount(PDDocument doc) {
        return doc.getNumberOfPages();
    }


    static void closePdf(PDDocument pdf) {
        //log.info(" <-- [PDF]: Закрытие документа: {}.", pdf.getNumberOfPages());
        try {
            if (null != pdf) pdf.close();
        } catch (Exception ignored) {
        }
    }

    static byte[] toPngByteArray(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        }
    }

    static void saveImage(String fileName, BufferedImage bImage) {
        try {
            File outPutFile = new File(fileName);
            ImageIO.write(bImage, "png", outPutFile);
        } catch (IOException e) {
            log.error("Failed to save pdf-preview file: {}.", fileName);
        }
    }

    static ResponseEntity<Resource> imageToPng(BufferedImage bImage) {
        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.setContentType(MediaType.IMAGE_PNG);
        try {
            return new ResponseEntity<>(new ByteArrayResource(toPngByteArray(bImage)), responseHeader, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Ошибка конвертации: {}.", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    static BufferedImage renderToImage(PDDocument pdDoc, Integer pageNum, Integer previewDPI) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(pdDoc);
        return pdfRenderer.renderImageWithDPI(pageNum - 1, previewDPI, ImageType.RGB);
    }

    static String getPreviewFileName(String fileName, UUID uuid, Integer pageNum) {
        return (fileName).replace(".pdf", (("_" + uuid) + ("_" + pageNum) + ".png"));
    }
}
