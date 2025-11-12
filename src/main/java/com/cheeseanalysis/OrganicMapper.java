package com.cheeseanalysis;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

public class OrganicMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    
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
            String manufacturerProvCode = cleanString(fields[1]); // ManufacturerProvCode
            int organic = parseIntSafe(fields[6]); // Organic (1 or 0)
            
            // Create composite key: categoryType|manufacturerProvCode
            String compositeKey = categoryType + "|" + manufacturerProvCode;
            
            // Emit total count (always 1) and organic count (1 if organic, 0 if not)
            context.write(new Text(compositeKey + "|TOTAL"), new IntWritable(1));
            context.write(new Text(compositeKey + "|ORGANIC"), new IntWritable(organic));
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
    
    private int parseIntSafe(String input) {
        String cleaned = cleanString(input);
        if (cleaned.isEmpty()) return 0;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
