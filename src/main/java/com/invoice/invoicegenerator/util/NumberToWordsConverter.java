package com.invoice.invoicegenerator.util;


public class NumberToWordsConverter {

    private static final String[] units = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
            "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty",
            "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convert(double amount) {
        long rupees = (long) amount;
        int paise = (int) Math.round((amount - rupees) * 100);

        String result = convert(rupees) + " Rupees";
        if (paise > 0) {
            result += " and " + convert(paise) + " Paise";
        }
        return result + " Only";
    }

    public static String convert(long number) {
        if (number == 0) return "Zero";
        return convertToWords(number).trim();
    }

    private static String convertToWords(long number) {
        StringBuilder result = new StringBuilder();

        if (number >= 10000000) {
            result.append(convertToWords(number / 10000000)).append(" Crore ");
            number %= 10000000;
        }
        if (number >= 100000) {
            result.append(convertToWords(number / 100000)).append(" Lakh ");
            number %= 100000;
        }
        if (number >= 1000) {
            result.append(convertToWords(number / 1000)).append(" Thousand ");
            number %= 1000;
        }
        if (number >= 100) {
            result.append(convertToWords(number / 100)).append(" Hundred ");
            number %= 100;
        }
        if (number > 0) {
            if (number < 20) {
                result.append(units[(int) number]);
            } else {
                result.append(tens[(int) number / 10]);
                if ((number % 10) > 0) {
                    result.append(" ").append(units[(int) number % 10]);
                }
            }
        }
        return result.toString();
    }




}
