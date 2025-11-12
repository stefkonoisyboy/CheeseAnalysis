package com.cheeseanalysis;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

public class MoistureMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
    
    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        
        String line = value.toString();
        String[] fields = line.split(",");
        
        // Skip header or malformed lines
        if (fields.length < 13 || line.contains("CheeseId")) {
            return;
        }
        
        try {
            String categoryType = cleanString(fields[7]); // CategoryTypeEn
            String milkType = cleanString(fields[8]); // MilkTypeEn
            double moisturePercent = parseDoubleSafe(fields[3]); // MoisturePercent
            
            // Create composite key: categoryType|milkType
            String compositeKey = categoryType + "|" + milkType;
            
            context.write(new Text(compositeKey), new DoubleWritable(moisturePercent));
        } catch (Exception e) {
            // Skip malformed lines
        }
    }
    
    private String cleanString(String input) {
        if (input == null) return "";
        input = input.trim();
        if (input.startsWith("\"") && input.endsWith("\"") && input.length() >= 2) {
            input = input.substring(1, input.length() - 1);
        }
        return input;
    }
    
    private double parseDoubleSafe(String input) {
        String cleaned = cleanString(input);
        if (cleaned.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
