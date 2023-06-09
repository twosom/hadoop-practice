package com.icloud;

import com.icloud.input.WholeFileInputFormat;
import com.icloud.util.JobBuilder;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

public class SmallFilesToSequenceFileConverter extends Configured implements Tool {


    static class SequenceFileMapper extends Mapper<NullWritable, BytesWritable, Text, BytesWritable> {

        private Text filenameKey;

        @Override
        protected void setup(Mapper<NullWritable, BytesWritable, Text, BytesWritable>.Context context)
                throws IOException, InterruptedException {
            InputSplit split = context.getInputSplit();
            if (!(split instanceof FileSplit)) return;
            Path path = ((FileSplit) split).getPath();
            this.filenameKey = new Text(path.toString());
        }

        @Override
        protected void map(NullWritable key,
                           BytesWritable value,
                           Mapper<NullWritable, BytesWritable, Text, BytesWritable>.Context context)
                throws IOException, InterruptedException {
            context.write(this.filenameKey, value);
        }
    }


    @Override
    public int run(String[] args) throws Exception {
        Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
        if (job == null) return -1;

        job.setInputFormatClass(WholeFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(BytesWritable.class);

        job.setMapperClass(SequenceFileMapper.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new SmallFilesToSequenceFileConverter(), args);
        System.exit(exitCode);
    }
}
