package com.example.inspection.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {
    public InputStream build(String reportNo, int score, String verdict, String summary,
                             Map<String, String> manual, List<String> warnings) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph("Inspection Report", new Font(Font.HELVETICA, 20, Font.BOLD)));
            doc.add(new Paragraph("Report No: " + reportNo));
            doc.add(new Paragraph("Score: " + score + " / 100"));
            doc.add(new Paragraph("Verdict: " + verdict));
            doc.add(new Paragraph("Summary: " + (summary == null ? "" : summary)));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Warnings", new Font(Font.HELVETICA, 14, Font.BOLD)));
            if (warnings == null || warnings.isEmpty()) doc.add(new Paragraph("None"));
            else for (String w : warnings) doc.add(new Paragraph("- " + w));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Manual Checks", new Font(Font.HELVETICA, 14, Font.BOLD)));
            if (manual == null || manual.isEmpty()) doc.add(new Paragraph("None"));
            else manual.forEach((k, v) -> { try { doc.add(new Paragraph(k + ": " + v)); } catch (Exception ignored) {} });

            doc.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("PDF generate failed", e);
        }
    }
}