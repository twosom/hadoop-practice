package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

public class FileSystemDoubleCat {

    public static void main(String[] args) throws IOException {
        final String uri = args[0];
        final Configuration conf = new Configuration();
        final FileSystem fs = FileSystem.get(URI.create(uri), conf);
        FSDataInputStream in = null;
        final PrintStream out = System.out;
        try {
            in = fs.open(new Path(uri));
            IOUtils.copyBytes(in, out, 4096, false);
            in.seek(0); // 파일의 처음으로 되돌아간다.
            IOUtils.copyBytes(in, out, 4096, false);
        } finally {
            IOUtils.closeStream(in);
        }
    }
}
