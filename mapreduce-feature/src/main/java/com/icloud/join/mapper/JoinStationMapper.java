package com.icloud.join.mapper;

import com.icloud.io.TextPair;
import com.icloud.parser.NcdcStationMetadataParser;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import static com.icloud.parser.NcdcStationMetadataParser.StationMetadata;

/**
 * Reduce-Side Join 을 위해 기상관측소 레코드를 태깅하는 매퍼
 */
public class JoinStationMapper extends Mapper<LongWritable, Text, TextPair, Text> {

    private final NcdcStationMetadataParser parser = new NcdcStationMetadataParser();

    @Override
    protected void map(LongWritable key,
                       Text value,
                       Mapper<LongWritable, Text, TextPair, Text>.Context context)
            throws IOException, InterruptedException {
        StationMetadata metadata = parser.parse(value);
        if (metadata != null) {
            context.write(
                    new TextPair(metadata.getStationId(),/*for TAG*/ "0"),
                    new Text(metadata.getStationName())
            );
        }

    }
}
