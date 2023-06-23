package com.icloud.join.mapper;

import com.icloud.io.TextPair;
import com.icloud.parser.NcdcRecordParser;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import static com.icloud.parser.NcdcRecordParser.NcdcRecord;

public class JoinRecordMapper extends Mapper<LongWritable, Text, TextPair, Text> {

    private final NcdcRecordParser parser = new NcdcRecordParser();

    @Override
    protected void map(LongWritable key,
                       Text value,
                       Mapper<LongWritable, Text, TextPair, Text>.Context context)
            throws IOException, InterruptedException {
        NcdcRecord record = parser.parse(value);
        context.write(new TextPair(record.getStationId(), "1"), value);
    }
}
