package org.esprit.finovate.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.esprit.finovate.models.Investissement;
import org.esprit.finovate.models.Project;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/** Exports projects and investissements to PDF. */
public final class PdfExportUtil {

    private static final int FONT_SIZE = 10;
    private static final int MARGIN = 50;
    private static final int ROW_HEIGHT = 16;
    private static final float PAGE_HEIGHT = 750;

    private PdfExportUtil() {}

    public static void exportAdminReport(List<Project> projects, List<Investissement> investissements, File outputFile) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream content = new PDPageContentStream(doc, page);

        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        content.beginText();
        content.newLineAtOffset(MARGIN, PAGE_HEIGHT);
        content.showText("Finovate - Admin Report");
        content.endText();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        content.beginText();
        content.newLineAtOffset(MARGIN, PAGE_HEIGHT - 20);
        content.showText("Generated: " + sdf.format(new java.util.Date()));
        content.endText();

        float y = PAGE_HEIGHT - 50;

        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        content.beginText();
        content.newLineAtOffset(MARGIN, y);
        content.showText("Projects (" + projects.size() + ")");
        content.endText();
        y -= 25;

        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
        for (Project p : projects) {
            if (y < 80) {
                content.close();
                page = new PDPage();
                doc.addPage(page);
                content = new PDPageContentStream(doc, page);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
                y = PAGE_HEIGHT - 30;
            }
            String line = String.format("ID %d | %s | Owner %d | %s | %.0f / %.0f TND",
                    p.getProject_id(), truncate(p.getTitle(), 30), p.getOwner_id(), p.getStatus(),
                    p.getCurrent_amount(), p.getGoal_amount());
            content.beginText();
            content.newLineAtOffset(MARGIN, y);
            content.showText(line);
            content.endText();
            y -= ROW_HEIGHT;
        }
        y -= 15;

        if (y < 80) {
            content.close();
            page = new PDPage();
            doc.addPage(page);
            content = new PDPageContentStream(doc, page);
            y = PAGE_HEIGHT - 30;
        }

        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        content.beginText();
        content.newLineAtOffset(MARGIN, y);
        content.showText("Investissements (" + investissements.size() + ")");
        content.endText();
        y -= 25;

        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
        for (Investissement inv : investissements) {
            if (y < 80) {
                content.close();
                page = new PDPage();
                doc.addPage(page);
                content = new PDPageContentStream(doc, page);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
                y = PAGE_HEIGHT - 30;
            }
            String date = inv.getInvestment_date() != null ? sdf.format(inv.getInvestment_date()) : "—";
            String line = String.format("ID %d | Project %d | Investor %d | %.2f TND | %s | %s",
                    inv.getInvestissement_id(), inv.getProject_id(), inv.getInvestor_id(),
                    inv.getAmount(), inv.getStatus(), date);
            content.beginText();
            content.newLineAtOffset(MARGIN, y);
            content.showText(line);
            content.endText();
            y -= ROW_HEIGHT;
        }

        content.close();
        doc.save(outputFile);
        doc.close();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
