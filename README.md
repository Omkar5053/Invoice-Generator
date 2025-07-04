# 🧾 Invoice Generator using Spring Boot and JasperReports

This project is a robust **Invoice Generator** built with **Spring Boot** and **JasperReports**. It allows users to upload two Excel files:
- One containing **transaction data** (invoices).
- Another containing **expense metadata** (like client info and expense types).

It parses the data, generates professional **GST invoices**, and exports them as **PDF files** formatted using the **Indian number system** (e.g., `3,28,500.00`).

---

## 🚀 Features

- ✅ Upload two Excel files: Transaction + Expense Details
- ✅ Parses and maps data based on `invoiceNo`
- ✅ Generates PDF invoices using JasperReports
- ✅ Indian currency formatting (`#,##,##0.00`)
- ✅ Fonts with **bold** and **italic** styling supported
- ✅ Outputs consolidated ZIP with all invoice PDFs
- ✅ Fast and efficient – no database required

---

## 📥 API Endpoint

### `POST /api/invoice/upload`

Uploads two Excel files and generates invoices.

**Request Parameters (Multipart Form):**
- `file`: Transaction Excel File (mandatory)
- `expenseFile`: Expense Excel File (mandatory)

**Response:**
- Returns a ZIP file containing generated invoice PDFs.

---

## 📊 Excel Format Example

### 1️⃣ Transaction Excel (`file`)
| Date       | Invoice No | Account     | ... | Total Amount | Taxable Amount | IGST |
|------------|------------|-------------|-----|---------------|----------------|------|
| 2024-06-01 | INV-001    | ABC Travels | ... | 328500.00     | 278390.00      | 50110.00 |

### 2️⃣ Expense Excel (`expenseFile`)
| Invoice No | Client Info         | Expense Type |
|------------|---------------------|--------------|
| INV-001    | Omkar Singh x 4 pax | DOM HTL      |

---

## 🧮 Indian Number Formatter

The project includes a custom formatter:

```java
public static String formatIndianNumber(double value);
```
## 🇮🇳 Indian Number Format Support

It ensures numbers are printed in the Indian style:

✅ `1234567.00` → `12,34,567.00`  
✅ `328500.00` → `3,28,500.00`

---

## 🖨️ JasperReports Styling

- **Fonts**: `SansSerif`, `Arial`  
- **Font Styles**: **Bold**, *Italic*, and <u>Underline</u> supported  
- **Pattern for currency**: `#,##,##0.00`

---

## 🔧 Technologies Used

- 🧰 **Spring Boot**
- 🧾 **JasperReports 7.0.3**
- 📊 **Apache POI** (Excel parsing)
- 💡 **Java 17+**

---

## 🛠️ Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/invoice-generator.git
cd invoice-generator

./mvnw clean install
./mvnw spring-boot:run

```

---

## 👨‍💻 Author

**Developed by Omkar Singh**

