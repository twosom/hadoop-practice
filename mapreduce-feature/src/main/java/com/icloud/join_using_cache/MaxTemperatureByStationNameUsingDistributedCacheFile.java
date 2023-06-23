package com.icloud.join_using_cache;

import com.icloud.ConfiguredTool;
import com.icloud.parser.NcdcRecordParser;
import com.icloud.util.JobBuilder;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static com.icloud.parser.NcdcRecordParser.NcdcRecord;

public class MaxTemperatureByStationNameUsingDistributedCacheFile extends ConfiguredTool {

    static class StationTemperatureMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

        private final NcdcRecordParser parser = new NcdcRecordParser();

        @Override
        protected void map(LongWritable key,
                           Text value,
                           Mapper<LongWritable, Text, Text, IntWritable>.Context context)
                throws IOException, InterruptedException {
            NcdcRecord record = parser.parse(value);
            if (record.isValidTemperature()) {
                context.write(
                        new Text(record.getStationId()),
                        new IntWritable(record.getAirTemperature())
                );
            }
        }
    }

    static class MaxTemperatureReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        @Override
        protected void reduce(Text key,
                              Iterable<IntWritable> values,
                              Reducer<Text, IntWritable, Text, IntWritable>.Context context)
                throws IOException, InterruptedException {
            int maxValue = Integer.MIN_VALUE;
            for (IntWritable value : values) {
                maxValue = Math.max(maxValue, value.get());
            }
            context.write(key, new IntWritable(maxValue));
        }
    }


    static class MaxTemperatureReducerWithStationLookUp extends Reducer<Text, IntWritable, Text, IntWritable> {

        private NcdcStationMetadata metadata;

        @Override
        protected void setup(Reducer<Text, IntWritable, Text, IntWritable>.Context context)
                throws IOException, InterruptedException {
//            URI cacheFile = context.getCacheFiles()[0]; // 단일 주소만 준다고 가정...
            this.metadata = new NcdcStationMetadata();
//            FileSystem fs = FileSystem.get(cacheFile, context.getConfiguration());
//            InputStream cacheFileInputStream = fs.open(new Path(cacheFile));
//            this.metadata.initialize(cacheFileInputStream);
            metadata.initialize(new File("stations-fixed-width.txt"));
        }

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values,
                              Reducer<Text, IntWritable, Text, IntWritable>.Context context)
                throws IOException, InterruptedException {
            String stationName = this.metadata.getStationName(key.toString());
            int maxValue = Integer.MIN_VALUE;
            for (IntWritable value : values) {
                maxValue = Math.max(maxValue, value.get());
            }
            context.write(
                    new Text(stationName),
                    new IntWritable(maxValue)
            );
        }
    }


    @Override
    public int run(String[] args) throws Exception {
        Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
        if (job == null) return -1;

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setMapperClass(StationTemperatureMapper.class);
        job.setCombinerClass(MaxTemperatureReducer.class);
        job.setReducerClass(MaxTemperatureReducerWithStationLookUp.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(
                new MaxTemperatureByStationNameUsingDistributedCacheFile(), args
        );
        System.exit(exitCode);
    }
}
