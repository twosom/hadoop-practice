package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CodecPool;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.io.compress.Compressor;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;

public class PooledStreamCompressor {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        final String codecClassname = args[0];
        final Class<? extends CompressionCodec> codecClass =
                (Class<? extends CompressionCodec>) Class.forName(codecClassname);
        Configuration conf = new Configuration();
        CompressionCodec codec = ReflectionUtils.newInstance(codecClass, conf);
        Compressor compressor = null;
        try {
            compressor = CodecPool.getCompressor(codec);
            CompressionOutputStream out = codec.createOutputStream(System.out, compressor);
            IOUtils.copyBytes(System.in, out, 4096, false);
            out.finish();
        } finally {
            CodecPool.returnCompressor(compressor);
        }
    }
}
