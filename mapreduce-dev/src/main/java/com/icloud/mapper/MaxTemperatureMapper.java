package com.icloud.mapper;

import com.icloud.parser.NcdcRecordParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import static com.icloud.parser.NcdcRecordParser.NcdcRecord;

public class MaxTemperatureMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    enum Temperature {
        MALFORMED,
        OVER_100
    }

    private final NcdcRecordParser parser;

    public MaxTemperatureMapper() {
        this.parser = new NcdcRecordParser();
    }

    @Override
    protected void map(LongWritable key,
                       Text value,
                       Mapper<LongWritable, Text, Text, IntWritable>.Context context)
            throws IOException, InterruptedException {
        final NcdcRecord record = this.parser.parse(value);
        if (record.isValidTemperature()) {
            int airTemperature = record.getAirTemperature();
            if (airTemperature > 1_000) {
                System.out.println("Temperature over 100 degrees for input: " + value);
                context.setStatus("Detected possibly corrupt record: see logs.");
                context.getCounter(Temperature.OVER_100).increment(1);
            }
            context.write(new Text(record.getYear()), new IntWritable(airTemperature));
        } else if (record.isMalformedTemperature()) {
            System.err.println("Ignoring possibly corrupt input: " + value);
            context.getCounter(Temperature.MALFORMED).increment(1);
        }
    }
}
