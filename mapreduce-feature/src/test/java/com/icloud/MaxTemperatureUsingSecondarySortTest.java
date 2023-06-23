package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.junit.jupiter.api.Test;

import java.io.File;

class MaxTemperatureUsingSecondarySortTest {

    @Test
    void test() throws Exception {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "file:///");
        conf.set("mapreduce.framework.name", "local");
        conf.setInt("mapreduce.task.io.sort.mb", 1);

        new File("output").deleteOnExit(); // output 폴더 존재 시 삭제
        Path input = new Path("input/ncdc/micro");
        Path output = new Path("output");

        FileSystem fs = FileSystem.get(conf);
        fs.delete(output, true);
        Tool driver = new MaxTemperatureUsingSecondarySort();
        driver.setConf(conf);
        int exitCode = driver.run(new String[]{
                input.toString(),
                output.toString()
        });

        System.out.println("exitCode = " + exitCode);
    }
}