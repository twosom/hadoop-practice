package com.icloud;

import org.apache.hadoop.io.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapWritableTest extends AbstractWritableTest {

    @Test
    void testCloneMapWritable() throws IOException {
        MapWritable src = new MapWritable();
        src.put(new IntWritable(1), new Text("cat"));
        src.put(new VIntWritable(2), new LongWritable(163));

        MapWritable dest = new MapWritable();
        WritableUtils.cloneInto(dest, src);
        assertEquals((Text) dest.get(new IntWritable(1)), new Text("cat"));
        assertEquals((LongWritable) dest.get(new VIntWritable(2)), new LongWritable(163));
    }
}
