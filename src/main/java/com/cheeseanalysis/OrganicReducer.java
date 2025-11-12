package com.cheeseanalysis;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OrganicReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {
    
    private Map<String, Integer> totalCounts = new HashMap<>();
    private Map<String, Integer> organicCounts = new HashMap<>();
    
    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        
        String keyStr = key.toString();
        if (keyStr.endsWith("|TOTAL")) {
            String baseKey = keyStr.substring(0, keyStr.lastIndexOf("|TOTAL"));
            totalCounts.put(baseKey, sum);
        } else if (keyStr.endsWith("|ORGANIC")) {
            String baseKey = keyStr.substring(0, keyStr.lastIndexOf("|ORGANIC"));
            organicCounts.put(baseKey, sum);
        }
    }
    
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        // Calculate percentages after all reductions are complete
        for (String baseKey : totalCounts.keySet()) {
            int total = totalCounts.get(baseKey);
            int organic = organicCounts.getOrDefault(baseKey, 0);
            double percentage = total > 0 ? (double) organic / total * 100.0 : 0.0;
            context.write(new Text(baseKey), new DoubleWritable(percentage));
        }
    }
}
