package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.junit.jupiter.api.Test;

import java.io.File;

import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxTemperatureDriverTest {

    @Test
    void test() throws Exception {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "file:///");
        conf.set("mapreduce.framework.name", "local");
        conf.setInt("mapreduce.task.io.sort.mb", 1);

        new File("output").deleteOnExit(); // output 폴더 존재 시 삭제
        Path input = new Path("input/ncdc/micro");
        Path output = new Path("output");

        FileSystem fs = FileSystem.getLocal(conf);
        fs.delete(output, true); // 이전 출력 삭제
        Tool driver = new MaxTemperatureDriver();
        driver.setConf(conf);

        int exitCode = driver.run(new String[]{
                input.toString(),
                output.toString()
        });

        assertEquals(exitCode, 0);
        checkOutput();
    }

    private void checkOutput() {
        File outputFile = new File("output");
        assertTrue(outputFile.exists());
        assertTrue(outputFile.isDirectory());
        File[] files = outputFile.listFiles();
        assert files != null;
        assertTrue(stream(files).anyMatch(e -> e.getName().equals("part-r-00000")));
        assertEquals(files.length, 4);
    }

}