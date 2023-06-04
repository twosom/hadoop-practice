package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;

import static java.util.Arrays.stream;

public class ListStatus {

    public static void main(String[] args) throws IOException {
        final String uri = args[0];
        final Configuration conf = new Configuration();
        final FileSystem fs = FileSystem.get(URI.create(uri), conf);

        final Path[] paths = stream(args)
                .map(Path::new)
                .toArray(Path[]::new);

        final FileStatus[] status = fs.listStatus(paths);
        stream(status) // FileStatus [] -> Path [] -> For-Each
                .map(FileStatus::getPath)
                .forEach(System.out::println);
    }
}
