package com.stockpilot.io;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.exception.InvalidInputException;
import com.stockpilot.model.Product;
import com.stockpilot.service.StockPilotService;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CSVImporter {
    private final StockPilotService service = new StockPilotService();


    public List<Product> importProducts(String filePath) throws IOException, DataAccessException {
        List<Product> importedProducts = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip CSV headers
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Product p = parseCSVLine(line);
                    service.addProduct(p);
                    importedProducts.add(p);
                } catch (InvalidInputException | IllegalArgumentException e) {
                    System.err.println("Skipping malformed CSV line: " + line + ". Reason: " + e.getMessage());
                }
            }
        }
        return importedProducts;
    }

    public List<Product> importProductsFromStream(InputStream in) throws IOException, DataAccessException {
        List<Product> importedProducts = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Product p = parseCSVLine(line);
                    service.addProduct(p);
                    importedProducts.add(p);
                } catch (InvalidInputException | IllegalArgumentException e) {
                    System.err.println("Skipping malformed stream line: " + line + ". Reason: " + e.getMessage());
                }
            }
        }
        return importedProducts;
    }

    private Product parseCSVLine(String line) {
        // Splits by comma, ignoring commas nested inside quotes (simplistic CSV parsing)
        String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        if (tokens.length < 5) {
            throw new InvalidInputException("Insufficient data tokens in line. Expected: SKU, Name, Category, Price, StockQuantity");
        }

        String sku = tokens[0].trim().replace("\"", "");
        String name = tokens[1].trim().replace("\"", "");
        String category = tokens[2].trim().replace("\"", "");
        BigDecimal price = new BigDecimal(tokens[3].trim());
        int stock = Integer.parseInt(tokens[4].trim());

        String id = UUID.randomUUID().toString();
        return new Product(id, sku, name, category, price, stock);
    }
}
