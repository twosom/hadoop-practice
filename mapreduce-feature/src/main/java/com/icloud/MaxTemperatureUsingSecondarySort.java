package com.icloud;

import com.icloud.io.IntPair;
import com.icloud.parser.NcdcRecordParser;
import com.icloud.util.JobBuilder;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

import static com.icloud.parser.NcdcRecordParser.NcdcRecord;


public class MaxTemperatureUsingSecondarySort extends ConfiguredTool {


    static class MaxTemperatureMapper extends Mapper<LongWritable, Text, IntPair, NullWritable> {
        private final NcdcRecordParser parser = new NcdcRecordParser();
        @Override
        protected void map(LongWritable key,
                           Text value,
                           Mapper<LongWritable, Text, IntPair, NullWritable>.Context context)
                throws IOException, InterruptedException {
            NcdcRecord record = parser.parse(value);
            if (record.isValidTemperature()) {
                context.write(
                        new IntPair(record.getYearInt(), record.getAirTemperature()),
                        NullWritable.get()
                );
            }
        }
    }

    static class MaxTemperatureReducer extends Reducer<IntPair, NullWritable, IntPair, NullWritable> {

        @Override
        protected void reduce(IntPair key,
                              Iterable<NullWritable> values,
                              Reducer<IntPair, NullWritable, IntPair, NullWritable>.Context context)
                throws IOException, InterruptedException {
            context.write(key, NullWritable.get());
        }
    }

    public static class FirstPartitioner extends Partitioner<IntPair, NullWritable> {

        @Override
        public int getPartition(IntPair key, NullWritable value, int numPartitions) {
            return Math.abs(key.getFirst() * 127) % numPartitions;
        }
    }

    /**
     * {@link Reducer}로 전달되기 전에 정렬방식을 정의하는 클래스
     */
    public static class KeyComparator extends WritableComparator {
        protected KeyComparator() {
            super(IntPair.class, true);
        }

        /**
         * 각 {@link IntPair} 를 비교하는 로직.
         * <p>
         * <b>[연도,온도]</b> 한 쌍을 하나의 값(여기서는 키)로 취급.
         * 만약, 각 연도가 같지 않다면(즉, 0이 아니라면)
         * 더 이상의 비교는 무의미해지므로 결괏값을 바로 반환.
         * <p>
         * 그렇지 않고 값이 같다면(즉, 같은 연도라면) 온도 비교를 한 후 반전시킨 값을 반환.(내림차순을 위한)
         *
         * @param a the first object to be compared.
         * @param b the second object to be compared.
         * @return
         */
        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            IntPair ip1 = (IntPair) a;
            IntPair ip2 = (IntPair) b;
            int cmp = IntPair.compare(ip1.getFirst(), ip2.getFirst());
            if (cmp != 0) {
                return cmp;
            }
            // Reverse for  Sort Desc
            return -IntPair.compare(ip1.getSecond(), ip2.getSecond());
        }
    }

    public static class GroupComparator extends WritableComparator {
        protected GroupComparator() {
            super(IntPair.class, true);
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            IntPair ip1 = (IntPair) a;
            IntPair ip2 = (IntPair) b;
            return IntPair.compare(ip1.getFirst(), ip2.getFirst());
        }
    }


    @Override
    public int run(String[] args) throws Exception {
        Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
        if (job == null) return -1;

        job.setInputFormatClass(TextInputFormat.class);
        job.setMapOutputKeyClass(IntPair.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setMapperClass(MaxTemperatureMapper.class);
        job.setPartitionerClass(FirstPartitioner.class);
        job.setSortComparatorClass(KeyComparator.class);
        job.setGroupingComparatorClass(GroupComparator.class);
        job.setReducerClass(MaxTemperatureReducer.class);
        job.setOutputKeyClass(IntPair.class);
        job.setOutputValueClass(NullWritable.class);

        job.setOutputFormatClass(TextOutputFormat.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new MaxTemperatureUsingSecondarySort(), args);
        System.exit(exitCode);
    }
}


