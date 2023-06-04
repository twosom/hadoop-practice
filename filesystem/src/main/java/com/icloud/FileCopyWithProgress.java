package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileCopyWithProgress {

    public static void main(String[] args) throws IOException {
        final String localSrc = args[0];
        final String dst = args[1];

        final InputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(localSrc)));

        final Configuration conf = new Configuration();
        final FileSystem fs = FileSystem.get(URI.create(dst), conf);
        OutputStream out = fs.create(new Path(dst), () -> {
            System.out.print(".");
        });

        IOUtils.copyBytes(in, out, 4096, true);
    }
}
