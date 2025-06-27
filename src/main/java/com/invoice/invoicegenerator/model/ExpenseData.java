package com.invoice.invoicegenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseData {

    private String expenseType;
    private String clientInfo;

}
