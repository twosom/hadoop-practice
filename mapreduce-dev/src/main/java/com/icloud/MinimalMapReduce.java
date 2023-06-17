package com.icloud;

import com.icloud.util.JobBuilder;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MinimalMapReduce extends Configured implements Tool {


    @Override
    public int run(String[] args) throws Exception {
        final Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
        if (job == null) return -1;
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new MinimalMapReduce(), args);
        System.exit(exitCode);
    }
}
