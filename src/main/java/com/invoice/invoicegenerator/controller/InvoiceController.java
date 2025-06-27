package com.invoice.invoicegenerator.controller;

import com.invoice.invoicegenerator.model.InvoiceData;
import com.invoice.invoicegenerator.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/upload")
    public ResponseEntity<byte[]> uploadFile(@RequestParam("file") MultipartFile file,
                                                @RequestParam("expenseFile") MultipartFile expenseFile) throws Exception {
        String filename = file.getOriginalFilename();
        String expenseFileName = expenseFile.getOriginalFilename();
        List<InvoiceData> invoices;

        if (filename == null || filename.isEmpty() || expenseFileName == null || expenseFileName.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid file".getBytes());
        }

        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            invoices = invoiceService.parseExcel(file, expenseFile);
        } else if (filename.endsWith(".pdf")) {
            invoices = invoiceService.parsePdf(file);
        } else {
            return ResponseEntity.badRequest().body("Unsupported file type. Only Excel or PDF allowed.".getBytes());
        }

        byte[] zipBytes = invoiceService.generateInvoicesZip(invoices);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoices.zip")
                .body(zipBytes);
    }

}

