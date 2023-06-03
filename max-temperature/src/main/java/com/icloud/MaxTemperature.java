package com.icloud;

import com.icloud.mapper.MaxTemperatureMapper;
import com.icloud.reducer.MaxTemperatureReducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

public class MaxTemperature {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        if (args.length != 2) {
            System.err.println("Usage: MaxTemperature <input path> <output path>");
            System.exit(-1);
        }

        JobConf jobConfig = new JobConf();
        jobConfig.setJarByClass(MaxTemperature.class);
        jobConfig.setJobName("Max temperature");
        FileInputFormat.addInputPath(jobConfig, new Path(args[0]));
        FileOutputFormat.setOutputPath(jobConfig, new Path(args[1]));

        Job job = Job.getInstance(jobConfig);

        job.setMapperClass(MaxTemperatureMapper.class);
        job.setReducerClass(MaxTemperatureReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}