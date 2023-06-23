package com.icloud.join;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.junit.jupiter.api.Test;

import java.io.File;

class JoinRecordWithStationNameTest {

    @Test
    void test() throws Exception {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "file:///");
        conf.set("mapreduce.framework.name", "local");
        conf.setInt("mapreduce.task.io.sort.mb", 1);

        new File("output").deleteOnExit(); // output 폴더 존재 시 삭제
        Path input = new Path("input/ncdc/micro");
        Path station = new Path("input/ncdc/metadata/stations-fixed-width.txt");
        Path output = new Path("output");

        FileSystem fs = FileSystem.getLocal(conf);
        fs.delete(output, true); // 이전 출력 삭제
        Tool driver = new JoinRecordWithStationName();
        driver.setConf(conf);

        int exitCode = driver.run(new String[]{
                input.toString(),
                station.toString(),
                output.toString()
        });
        System.out.println("exitCode = " + exitCode);
    }
}