package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.junit.jupiter.api.Test;

class XMLFileConverterTest {

    @Test
    void test() throws Exception {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "file:///");
        conf.set("mapreduce.framework.name", "local");
        conf.setInt("mapreduce.task.io.sort.mb", 1);

        Path input = new Path("input/kowiki");
        Path output = new Path("output/kowiki");

        FileSystem fs = FileSystem.getLocal(conf);
        fs.delete(output, true);
        Tool driver = new XMLFileConverter();
        driver.setConf(conf);

        int exitCode = driver.run(new String[]{
                input.toString(),
                output.toString()
        });
        System.out.println("exitCode = " + exitCode);
    }

}