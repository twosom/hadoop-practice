package com.icloud;

import com.icloud.parser.NcdcRecordParser;
import com.icloud.util.JobBuilder;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

import static com.icloud.parser.NcdcRecordParser.NcdcRecord;

public class MaxTemperatureWithCounters extends Configured implements Tool {

    enum Temperature {
        MISSING,
        MALFORMED
    }

    static class MaxTemperatureMapperWithCounters
            extends Mapper<LongWritable, Text, Text, IntWritable> {

        private final NcdcRecordParser parser = new NcdcRecordParser();

        @Override
        protected void map(LongWritable key,
                           Text value,
                           Mapper<LongWritable, Text, Text, IntWritable>.Context context)
                throws IOException, InterruptedException {
            final NcdcRecord record = parser.parse(value);
            if (record.isValidTemperature()) {
                int airTemperature = record.getAirTemperature();
                context.write(new Text(record.getYear()), new IntWritable(airTemperature));
            } else if (record.isMalformedTemperature()) {
                System.err.println("Ignoring possibly corrupt input: " + value);
                context.getCounter(Temperature.MALFORMED).increment(1);
            } else if (record.isMissingTemperature()) {
                context.getCounter(Temperature.MISSING).increment(1);
            }
            context.getCounter("TemperatureQuality", record.getQuality()).increment(1);
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


    @Override
    public int run(String[] args) throws Exception {
        final Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
        if (job == null) return -1;

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setMapperClass(MaxTemperatureMapperWithCounters.class);
        job.setCombinerClass(MaxTemperatureReducer.class);
        job.setReducerClass(MaxTemperatureReducer.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new MaxTemperatureWithCounters(), args);
        System.exit(exitCode);
    }
}
