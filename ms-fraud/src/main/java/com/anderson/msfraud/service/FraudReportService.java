package com.anderson.msfraud.service;

import com.anderson.msfraud.model.FraudAnalysis;
import com.anderson.msfraud.repository.FraudAnalysisRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class FraudReportService {

    private final FraudAnalysisRepository repository;

    public FraudReportService(FraudAnalysisRepository repository) {
        this.repository = repository;
    }

    public byte[] generateFraudReportPdf(String requestId) {
        // Busca a análise específica pelo Request ID
        FraudAnalysis analysis = repository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Análise não encontrada para o ID: " + requestId));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // CORES
            Color primaryBlue = new Color(0, 51, 102);
            Color lightGray = new Color(245, 245, 245);

            // 1. CABEÇALHO
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell(new Phrase("REDE DE ANÁLISE DE CRÉDITO - SISTEMA DE AUDITORIA",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
            headerCell.setBackgroundColor(primaryBlue);
            headerCell.setBorder(Rectangle.NO_BORDER);
            headerCell.setPadding(5);
            headerCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            headerTable.addCell(headerCell);
            document.add(headerTable);

            // 2. TÍTULO
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, primaryBlue);
            Paragraph title = new Paragraph("Relatório de Risco e Fraude", titleFont);
            title.setSpacingBefore(20);
            title.setSpacingAfter(5);
            document.add(title);

            document.add(new Paragraph(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(1f, 100, primaryBlue, Element.ALIGN_CENTER, -2))));

            // 3. DADOS DO CLIENTE
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(15);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            infoTable.getDefaultCell().setPadding(5);

            infoTable.addCell(new Phrase("Cliente: " + analysis.getCustomerId(), FontFactory.getFont(FontFactory.HELVETICA, 11)));
            infoTable.addCell(new Phrase("Data da Análise: " + analysis.getAnalyzedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FontFactory.getFont(FontFactory.HELVETICA, 11)));
            infoTable.addCell(new Phrase("CPF: " + analysis.getCpf(), FontFactory.getFont(FontFactory.HELVETICA, 11)));
            infoTable.addCell(new Phrase("ID Requisição: " + analysis.getRequestId(), FontFactory.getFont(FontFactory.HELVETICA, 11)));
            document.add(infoTable);

            // 4. RISK SCORE (DESTAQUE)
            document.add(new Paragraph(" "));
            PdfPTable scoreTable = new PdfPTable(1);
            scoreTable.setWidthPercentage(30);
            scoreTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell scoreLabel = new PdfPCell(new Phrase("RISK SCORE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.GRAY)));
            scoreLabel.setBorder(Rectangle.NO_BORDER);
            scoreLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
            scoreTable.addCell(scoreLabel);

            PdfPCell scoreValue = new PdfPCell(new Phrase(String.valueOf(analysis.getRiskScore()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, primaryBlue)));
            scoreValue.setBorder(Rectangle.BOX);
            scoreValue.setBackgroundColor(lightGray);
            scoreValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            scoreValue.setPadding(10);
            scoreTable.addCell(scoreValue);
            document.add(scoreTable);

            // 5. DECISÃO FINAL
            PdfPTable resTable = new PdfPTable(1);
            resTable.setWidthPercentage(100);
            resTable.setSpacingBefore(20);

            String decision = analysis.getFinalDecision().name();
            Color statusColor = decision.equals("APPROVED") ? new Color(0, 153, 76) : new Color(204, 0, 0);

            PdfPCell resCell = new PdfPCell(new Phrase("PARECER FINAL: " + decision, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE)));
            resCell.setBackgroundColor(statusColor);
            resCell.setPadding(10);
            resCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            resTable.addCell(resCell);
            document.add(resTable);

            if (analysis.getReason() != null) {
                Paragraph reason = new Paragraph("Motivo: " + analysis.getReason(), FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11));
                reason.setSpacingBefore(10);
                document.add(reason);
            }

            // 6. RODAPÉ
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Documento gerado automaticamente - Crédito Analítico v1.0", FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }
}
