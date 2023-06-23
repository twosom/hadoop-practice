package com.icloud.join.reducer;

import com.icloud.io.TextPair;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Iterator;

public class JoinReducer extends Reducer<TextPair, Text, Text, Text> {

    @Override
    protected void reduce(TextPair key,
                          Iterable<Text> values,
                          Reducer<TextPair, Text, Text, Text>.Context context)
            throws IOException, InterruptedException {
        Iterator<Text> iter = values.iterator();
        Text stationName = new Text(iter.next());
        while (iter.hasNext()) {
            Text record = iter.next();
            Text outValue = new Text(stationName + "\t" + record.toString());
            context.write(key.getFirst(), outValue);
        }
    }
}
