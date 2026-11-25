package com.enlasate.backend.controller;

import com.enlasate.backend.entity.PagoTotal;
import com.enlasate.backend.repository.PagoTotalRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final PagoTotalRepository pagoRepository;

    public ReporteController(PagoTotalRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    // -----------------------------------------------------------------
    // Reporte de ventas en PDF (solo administrador)
    // -----------------------------------------------------------------
    @GetMapping("/ventas.pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<byte[]> ventas(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes) {

        int anioActual = LocalDate.now().getYear();
        int anioPedido = (anio == null || anio < 2000 || anio > anioActual)
                ? anioActual
                : anio;

        String prefijo = mes != null
                ? String.format("%d-%02d", anioPedido, mes)
                : String.valueOf(anioPedido);

        List<PagoTotal> pagos = pagoRepository.findAll().stream()
                .filter(p -> p.getMesCorrespondiente() != null
                        && p.getMesCorrespondiente().startsWith(prefijo))
                .sorted((a, b) -> a.getMesCorrespondiente()
                        .compareTo(b.getMesCorrespondiente()))
                .toList();

        byte[] pdf = generarPdf(pagos, prefijo, anioPedido, mes);

        String nombre = "reporte-ventas-" + prefijo + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private byte[] generarPdf(
            List<PagoTotal> pagos,
            String periodo,
            int anio,
            Integer mes) {

        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        Document documento = new Document();
        PdfWriter.getInstance(documento, salida);
        documento.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font sub = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        Paragraph p1 = new Paragraph("ENLÁSATE · Reporte de ventas", titulo);
        p1.setAlignment(Element.ALIGN_CENTER);
        documento.add(p1);

        String etiquetaPeriodo = mes != null
                ? "Periodo: " + periodo
                : "Periodo: año " + anio;

        Paragraph p2 = new Paragraph(etiquetaPeriodo, sub);
        p2.setAlignment(Element.ALIGN_CENTER);
        p2.setSpacingAfter(16);
        documento.add(p2);

        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);

        String[] encabezados = {"Cliente", "Mes", "Monto", "Método", "Fecha"};

        for (String e : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(e, encabezado));
            celda.setPadding(6);
            tabla.addCell(celda);
        }

        BigDecimal total = BigDecimal.ZERO;

        for (PagoTotal p : pagos) {

            String cliente = "—";

            if (p.getCliente() != null) {
                if (p.getCliente().getUsuario() != null) {
                    cliente = p.getCliente().getUsuario().getNombre();
                } else if (p.getCliente().getNombreContacto() != null) {
                    cliente = p.getCliente().getNombreContacto();
                }
            }
            BigDecimal monto = p.getMonto() != null ? p.getMonto() : BigDecimal.ZERO;
            total = total.add(monto);

            tabla.addCell(celda(cliente));
            tabla.addCell(celda(p.getMesCorrespondiente()));
            tabla.addCell(celda("$" + monto.setScale(2, java.math.RoundingMode.HALF_UP)));
            tabla.addCell(celda(p.getMetodoPago() != null ? p.getMetodoPago() : "—"));
            tabla.addCell(celda(p.getFechaPago() != null ? p.getFechaPago().toString() : "—"));
        }

        documento.add(tabla);

        Paragraph p3 = new Paragraph(
                "\nPagos registrados: " + pagos.size()
                        + "\nTotal cobrado: $" + total.setScale(2, java.math.RoundingMode.HALF_UP),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)
        );
        p3.setSpacingBefore(18);
        documento.add(p3);

        Paragraph p4 = new Paragraph(
                "\nGenerado el " + LocalDate.now() + " · Sistema ENLÁSATE",
                sub
        );
        documento.add(p4);

        documento.close();

        return salida.toByteArray();
    }

    private PdfPCell celda(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(
                texto,
                FontFactory.getFont(FontFactory.HELVETICA, 9)
        ));
        c.setPadding(5);
        return c;
    }
}
