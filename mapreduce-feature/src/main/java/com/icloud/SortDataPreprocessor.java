package com.icloud;

import com.icloud.parser.NcdcRecordParser;
import com.icloud.util.JobBuilder;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

import static com.icloud.parser.NcdcRecordParser.NcdcRecord;

public class SortDataPreprocessor extends ConfiguredTool {

    /**
     * 키는 기온이고, 값은 Text 를 가지도록 하는 Mapper 클래스
     */
    static class CleanerMapper extends Mapper<LongWritable, Text, IntWritable, Text> {
        private final NcdcRecordParser parser = new NcdcRecordParser();

        @Override
        protected void map(LongWritable key,
                           Text value,
                           Mapper<LongWritable, Text, IntWritable, Text>.Context context)
                throws IOException, InterruptedException {
            NcdcRecord record = parser.parse(value);
            if (record.isValidTemperature()) {
                context.write(new IntWritable(record.getAirTemperature()), value);
            }
        }
    }


    @Override
    public int run(String[] args) throws Exception {
        Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
        if (job == null) return -1;

        job.setMapperClass(CleanerMapper.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(0);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        SequenceFileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
        SequenceFileOutputFormat.setOutputCompressionType(job, SequenceFile.CompressionType.BLOCK);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new SortDataPreprocessor(), args);
        System.exit(exitCode);
    }
}
