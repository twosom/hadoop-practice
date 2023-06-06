package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class FileDecompressor {
    public static void main(String[] args) throws IOException {
        final String uri = args[0];
        final Configuration conf = new Configuration();
        final FileSystem fs = FileSystem.get(URI.create(uri), conf);

        final Path inputPath = new Path(uri);
        final CompressionCodecFactory factory = new CompressionCodecFactory(conf);
        final CompressionCodec codec = factory.getCodec(inputPath);
        if (codec == null) {
            System.err.println("No codec found for " + uri);
            System.exit(-1);
        }

        final String outputUri = CompressionCodecFactory.removeSuffix(uri, codec.getDefaultExtension());
        InputStream in = null;
        OutputStream out = null;
        try {
            in = codec.createInputStream(fs.open(inputPath));
            out = fs.create(new Path(outputUri));
            IOUtils.copyBytes(in, out, conf);
        } finally {
            IOUtils.closeStream(in);
            IOUtils.closeStream(out);
        }

    }
}
