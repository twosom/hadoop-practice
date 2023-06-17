package com.icloud.input;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import java.io.IOException;

/**
 * 파일의 전체 내용을 입력으로 받아오기 위한 {@link FileInputFormat} 의 서브클래스.
 * <p>
 * 입력 파일이 Split 으로 나뉘는 것을 방지하기 위해 {@link WholeFileInputFormat#isSplitable} 메소드에 항상 false 를 반환하도록 함.
 */
public class WholeFileInputFormat extends FileInputFormat<NullWritable, BytesWritable> {

    @Override
    public RecordReader<NullWritable, BytesWritable> createRecordReader(InputSplit split,
                                                                        TaskAttemptContext context)
            throws IOException, InterruptedException {
        RecordReader<NullWritable, BytesWritable> reader = new WholeFileRecordReader();
        reader.initialize(split, context);
        return reader;
    }
}
