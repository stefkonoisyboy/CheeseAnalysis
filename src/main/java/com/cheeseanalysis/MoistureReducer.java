package com.cheeseanalysis;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;

public class MoistureReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
    
    @Override
    public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
            throws IOException, InterruptedException {
        
        double sum = 0;
        int count = 0;
        
        for (DoubleWritable val : values) {
            sum += val.get();
            count++;
        }
        
        double average = count > 0 ? sum / count : 0.0;
        context.write(key, new DoubleWritable(average));
    }
}
