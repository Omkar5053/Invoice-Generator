package com.invoice.invoicegenerator.service;

import com.invoice.invoicegenerator.model.ExpenseData;
import com.invoice.invoicegenerator.model.InvoiceData;



import com.invoice.invoicegenerator.util.NumberToWordsConverter;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class InvoiceService {

    public List<InvoiceData> parseExcel(MultipartFile file, MultipartFile expenseFile) throws Exception {
        Map<String, InvoiceData> invoiceMap = new HashMap<>();
        Map<String, ExpenseData> expenseMap = new HashMap<>();

        // ✅ Step 1: Parse expenseFile and build a map of invoiceNo → ExpenseData
        try (Workbook workbook = new XSSFWorkbook(expenseFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String invoiceNo = getStringValue(row.getCell(0)).trim();  // Assuming invoiceNo is in col 0
                String expenseType = getStringValue(row.getCell(1)).trim(); // Assuming expenseType is in col 1
                String clientInfo = getStringValue(row.getCell(2)).trim();  // Assuming clientInfo is in col 2

                ExpenseData expenseData = new ExpenseData(expenseType, clientInfo);
                expenseMap.put(invoiceNo, expenseData);
            }
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                // Skip the header
                if (row.getRowNum() == 0) {
                    continue;
                }

                // ✅ Extract columns
                String dateStr = getStringValue(row.getCell(0)).trim();
                String invoiceNo = getStringValue(row.getCell(1)).trim();
                String account = getStringValue(row.getCell(2)).trim();
                String totalAmountStr = getStringValue(row.getCell(4)).trim();
                String saleAmountStr = getStringValue(row.getCell(5)).trim();
                String taxableAmountStr = getStringValue(row.getCell(6)).trim();
                String igstStr = getStringValue(row.getCell(7)).trim();
                String otherAmtStr = getStringValue(row.getCell(10)).trim();

                // ✅ Parse Date
                LocalDate invoiceDate;
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                    invoiceDate = LocalDate.parse(dateStr, formatter);
                } catch (DateTimeParseException e) {
                    System.out.println("⚠️ Skipping row " + (row.getRowNum() + 1) + ", invalid date: " + dateStr);
                    continue;
                }

                // ✅ Parse Amounts
                double totalAmount = parseDoubleSafe(totalAmountStr);
//                double saleAmount = parseDoubleSafe(saleAmountStr);
                double igstAmount = parseDoubleSafe(igstStr);
                double otherAmount = parseDoubleSafe(otherAmtStr);
                double taxableAmount = parseDoubleSafe(taxableAmountStr);

                // here will extract the expense type and client info from the another excel
                ExpenseData expenseData = expenseMap.getOrDefault(invoiceNo, new ExpenseData("UNKNOWN", "UNKNOWN"));
                String expenseType = expenseData.getExpenseType();
                String clientInfo = expenseData.getClientInfo();



                InvoiceData invoice = new InvoiceData();

                    invoice.setInvoiceNo(invoiceNo);
                    invoice.setInvoiceDate(invoiceDate.toString());
                    invoice.setExpenseType(expenseType);
                    invoice.setClientInfo(clientInfo);

//                    invoice.setPartyName(account);
//                    invoice.setPartyAddress("No.B-40A, 2nd Floor, Okhla, New Delhi");
//                    invoice.setGstin("07AAJCM6528M2ZW");
                    invoice.setPlaceOfSupply("Delhi (07)");
                    invoiceMap.put(invoiceNo, invoice);

                    double withoutTaxAmt = taxableAmount + otherAmount;
                    double taxAmount = (igstAmount * 100) / 18;
                    invoice.setIgstRate("18.00%");
                    invoice.setTaxRate("18%");
                    invoice.setIgstAmount(igstAmount);
                    invoice.setTotalAmount(totalAmount);
                    invoice.setTaxAmt(taxAmount);
                    invoice.setWithoutTaxAmt(withoutTaxAmt);
                    invoice.setHSNCode("9985");
                    invoice.setAmountInWords(NumberToWordsConverter.convert(totalAmount));

            }
        }

        return new ArrayList<>(invoiceMap.values());
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    private double parseDoubleSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Failed to parse double value: '" + value + "'. Defaulting to 0.0");
            return 0.0;
        }
    }


    public List<InvoiceData> parsePdf(MultipartFile file) throws Exception {
        List<InvoiceData> invoices = new ArrayList<>();



        return invoices;
    }

    private double parseAmount(String raw) {
        return Double.parseDouble(raw.replace(",", ""));
    }


    public byte[] generateInvoicesZip(List<InvoiceData> invoices) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);

        for (InvoiceData invoice : invoices) {
            byte[] pdf = generateInvoicePdf(invoice);
            ZipEntry entry = new ZipEntry(invoice.getInvoiceNo() + ".pdf");
            zipOut.putNextEntry(entry);
            zipOut.write(pdf);
            zipOut.closeEntry();
        }

        zipOut.close();
        return baos.toByteArray();
    }



    public byte[] generateInvoicePdf(InvoiceData data) throws Exception {
        InputStream jrxml = getClass().getResourceAsStream("/reports/invoice_template.jrxml");
        JasperReport report = JasperCompileManager.compileReport(jrxml);


        Map<String, Object> params = new HashMap<>();
        params.put("invoiceNo", data.getInvoiceNo());
        params.put("invoiceDate", data.getInvoiceDate());
        params.put("placeOfSupply", data.getPlaceOfSupply());
        params.put("GSTPercentage", data.getIgstRate());
        params.put("IGSTAmt", formatIndianNumber(data.getIgstAmount()));
        params.put("totalAmount", formatIndianNumber(data.getTotalAmount()));
        params.put("amountInWords", data.getAmountInWords());
        params.put("taxableAmount", formatIndianNumber(data.getTaxAmt()));
        params.put("HSN", data.getHSNCode());
        params.put("taxRate", data.getTaxRate());
        params.put("withoutTaxAmt", formatIndianNumber(data.getWithoutTaxAmt()));
        params.put("expenseType", data.getExpenseType());
        params.put("clientInfo", data.getClientInfo());


//        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(data.getItems());

        JasperPrint print = JasperFillManager.fillReport(report, params, new JREmptyDataSource());
        return JasperExportManager.exportReportToPdf(print);
    }


    public static String formatIndianNumber(double value) {
        String[] parts = String.format("%.2f", value).split("\\.");
        String integerPart = parts[0];
        String decimalPart = parts[1];

        StringBuilder result = new StringBuilder();

        int len = integerPart.length();

        // If length is less than or equal to 3, just return with decimal
        if (len <= 3) {
            result.append(integerPart);
        } else {
            // Get the last 3 digits
            result.append(integerPart.substring(len - 3));
            integerPart = integerPart.substring(0, len - 3);

            // Process remaining digits in pairs
            while (integerPart.length() > 0) {
                int pairLen = Math.min(2, integerPart.length());
                result.insert(0, ",");
                result.insert(0, integerPart.substring(integerPart.length() - pairLen));
                integerPart = integerPart.substring(0, integerPart.length() - pairLen);
            }
        }

        result.append(".").append(decimalPart);
        return result.toString();
    }







}
