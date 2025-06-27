package com.invoice.invoicegenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceData {
    private String invoiceNo;
    private String invoiceDate;
    private String partyName;
    private String partyAddress;
    private String gstin;
    private String placeOfSupply;
    private String HSNCode;
    private String expenseType;
    private String clientInfo;
    private String igstRate;
    private String taxRate;
    private double igstAmount;
    private double totalAmount;
    private String amountInWords;
    private double withoutTaxAmt;
    private double taxAmt;
}
