package com.icloud;

import com.icloud.mapper.MaxTemperatureMapper;
import com.icloud.reducer.MaxTemperatureReducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.Job;

public class MaxTemperatureWithCompression {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: MaxTemperatureWithCompression <input path> <output path>");
            System.exit(-1);
        }

        JobConf jobConfig = new JobConf();
        jobConfig.setJarByClass(MaxTemperature.class);

        FileInputFormat.addInputPath(jobConfig, new Path(args[0]));
        FileOutputFormat.setOutputPath(jobConfig, new Path(args[1]));
        FileOutputFormat.setCompressOutput(jobConfig, true);
        /**
         * 사실 이 부분만 해줘도 된다. 어차피 내부에서 {@link FileOutputFormat#setCompressOutput(JobConf, boolean)} 을 호출한다.
         */
        FileOutputFormat.setOutputCompressorClass(jobConfig, GzipCodec.class);
        SequenceFileOutputFormat.setOutputCompressionType(jobConfig, SequenceFile.CompressionType.BLOCK);

        Job job = Job.getInstance(jobConfig);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setMapperClass(MaxTemperatureMapper.class);
        job.setCombinerClass(MaxTemperatureReducer.class);
        job.setReducerClass(MaxTemperatureReducer.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
