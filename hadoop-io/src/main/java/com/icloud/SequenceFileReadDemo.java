package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;

public class SequenceFileReadDemo {
    public static void main(String[] args) throws IOException {
        final String uri = args[0];
        final Configuration conf = new Configuration();
        final Path path = new Path(uri);

        SequenceFile.Reader reader = null;
        try {
            reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(path));
            Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
            Writable value = (Writable) ReflectionUtils.newInstance(reader.getValueClass(), conf);
            long position = reader.getPosition();
            while (reader.next(key, value)) {
                String syncSeen = reader.syncSeen() ? "*" : "";
                System.out.printf("[%s%s]\t%s\t%s\n", position, syncSeen, key, value);
                position = reader.getPosition();
            }
        } finally {
            IOUtils.closeStream(reader);
        }
    }
}
