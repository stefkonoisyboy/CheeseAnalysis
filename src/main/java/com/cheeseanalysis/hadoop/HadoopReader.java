package com.cheeseanalysis.hadoop;

import com.cheeseanalysis.model.CheeseRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class HadoopReader {

    public static List<CheeseRecord> readCSV(String hdfsPath) throws Exception {
        List<CheeseRecord> records = new ArrayList<>();
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:9000"); // change if needed
        FileSystem fs = FileSystem.get(new URI("hdfs://localhost:9000"), conf);

        Path path = new Path(hdfsPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(path)));
        String line;

        br.readLine(); // skip header

        while ((line = br.readLine()) != null) {
            String[] cols = parseCSVLine(line);
            
            if (cols.length >= 13) { // Ensure we have enough columns
                try {
                    String cheeseId = cleanString(cols[0]);
                    String manufacturerProvCode = cleanString(cols[1]);
                    String manufacturingTypeEn = cleanString(cols[2]);
                    double moisturePercent = parseDoubleSafe(cols[3]);
                    String flavourEn = cleanString(cols[4]);
                    String characteristicsEn = cleanString(cols[5]);
                    int organic = parseIntSafe(cols[6]);
                    String categoryTypeEn = cleanString(cols[7]);
                    String milkTypeEn = cleanString(cols[8]);
                    String milkTreatmentTypeEn = cleanString(cols[9]);
                    String rindTypeEn = cleanString(cols[10]);
                    String cheeseName = cleanString(cols[11]);
                    String fatLevel = cleanString(cols[12]);

                    records.add(new CheeseRecord(cheeseId, manufacturerProvCode, manufacturingTypeEn,
                            moisturePercent, flavourEn, characteristicsEn, organic, categoryTypeEn,
                            milkTypeEn, milkTreatmentTypeEn, rindTypeEn, cheeseName, fatLevel));
                } catch (Exception e) {
                    // Skip malformed lines
                    System.err.println("Skipping malformed line: " + line);
                    e.printStackTrace();
                }
            } else {
                System.err.println("Line has insufficient columns (" + cols.length + "): " + line);
            }
        }

        br.close();
        fs.close();
        return records;
    }

    // Helper to remove quotes and trim
    private static String cleanString(String input) {
        if (input == null) return "";
        input = input.trim();
        if (input.startsWith("\"") && input.endsWith("\"") && input.length() >= 2) {
            input = input.substring(1, input.length() - 1);
        }
        return input;
    }

    // Helper to safely parse doubles
    private static double parseDoubleSafe(String input) {
        String cleaned = cleanString(input);
        if (cleaned.isEmpty()) return 0.0; // default value if blank
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Helper to safely parse integers
    private static int parseIntSafe(String input) {
        String cleaned = cleanString(input);
        if (cleaned.isEmpty()) return 0; // default value if blank
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Helper to parse CSV line with proper handling of quoted fields
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add the last field
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
}
