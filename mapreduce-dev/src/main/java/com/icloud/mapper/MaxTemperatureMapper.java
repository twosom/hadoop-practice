package com.icloud.mapper;

import com.icloud.parser.NcdcRecordParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import static com.icloud.parser.NcdcRecordParser.NcdcRecord;

public class MaxTemperatureMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

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
            context.write(new Text(record.getYear()), new IntWritable(record.getAirTemperature()));
        }
    }
}
