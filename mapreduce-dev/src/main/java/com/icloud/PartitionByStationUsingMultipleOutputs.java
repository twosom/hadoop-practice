package com.icloud;

import com.icloud.parser.NcdcRecordParser;
import com.icloud.util.JobBuilder;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

import static com.icloud.parser.NcdcRecordParser.NcdcRecord;

/**
 * {@link MultipleOutputs}를 사용하여 전체 데이터셋을 기상 관측소 아이디로 명명된 파일로 분할하는 Job
 */
public class PartitionByStationUsingMultipleOutputs extends Configured implements Tool {

    /**
     * MAPPER
     */
    static class StationMapper extends Mapper<LongWritable, Text, Text, Text> {
        NcdcRecordParser parser = new NcdcRecordParser();

        @Override
        protected void map(LongWritable key,
                           Text value,
                           Mapper<LongWritable, Text, Text, Text>.Context context)
                throws IOException, InterruptedException {
            NcdcRecord record = parser.parse(value);
            context.write(
                    new Text(record.getStationId()),
                    value
            );
        }
    }

    /**
     * REDUCER
     * key: StationId
     * value: VALUE
     * =====>
     * key: Null
     * value: VALUE
     */
    static class MultipleOutputsReducer extends Reducer<Text, Text, NullWritable, Text> {

        private MultipleOutputs<NullWritable, Text> multipleOutputs;

        @Override
        protected void setup(Reducer<Text, Text, NullWritable, Text>.Context context)
                throws IOException, InterruptedException {
            this.multipleOutputs = new MultipleOutputs<>(context);
        }

        @Override
        protected void reduce(Text key,
                              Iterable<Text> values,
                              Reducer<Text, Text, NullWritable, Text>.Context context)
                throws IOException, InterruptedException {
            for (final Text value : values) {
                multipleOutputs.write(NullWritable.get(), value, key.toString());
            }
        }

        @Override
        protected void cleanup(Reducer<Text, Text, NullWritable, Text>.Context context)
                throws IOException, InterruptedException {
            multipleOutputs.close();
        }
    }


    @Override
    public int run(String[] args) throws Exception {
        Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
        if (job == null) return -1;
        job.setMapperClass(StationMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setReducerClass(MultipleOutputsReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new PartitionByStationUsingMultipleOutputs(), args);
        System.exit(exitCode);
    }
}
