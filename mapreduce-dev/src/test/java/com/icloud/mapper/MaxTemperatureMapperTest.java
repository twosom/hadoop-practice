package com.icloud.mapper;

import com.icloud.reducer.MaxTemperatureReducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaxTemperatureMapperTest {

    @Test
    void processValidRecord() throws IOException {
        Text value = new Text("0043011990999991950051518004+68750+023550FM-12+0382" +
                              // 연도 ^^^^
                              "99999V0203201N00261220001CN9999999N9-00111+99999999999");
        // 기온 ^^^^

        new MapDriver<LongWritable, Text, Text, IntWritable>()
                .withMapper(new MaxTemperatureMapper())
                .withInput(new LongWritable(0), value)
                .withOutput(new Text("1950"), new IntWritable(-11))
                .runTest();
    }

    @Test
    void ignoresMissingTemperatureRecord() throws IOException {
        Text value = new Text("0043011990999991950051518004+68750+023550FM-12+0382" +// 연도 ^^^^
                              "99999V0203201N00261220001CN9999999N9+99991+99999999999");// 기온 ^^^^

        new MapDriver<LongWritable, Text, Text, IntWritable>()
                .withMapper(new MaxTemperatureMapper())
                .withInput(new LongWritable(0), value)
                .runTest();
    }

    @Test
    void returnsMaximumIntegerInValues() throws IOException {
        new ReduceDriver<Text, IntWritable, Text, IntWritable>()
                .withReducer(new MaxTemperatureReducer())
                .withInput(new Text("1950"),
                        Arrays.asList(new IntWritable(10), new IntWritable(5)))
                .withOutput(new Text("1950"), new IntWritable(10))
                .runTest();
    }

    @Test
    void parseMalformedTemperature() throws IOException {
        Text value = new Text(
                "0335999999433181957042302005+37950+139117SAO +0004" +
                        /*연도*/
                "RJSN V02011359003150070356999999433201957010100005+353"
                /* 기온*/);
        Counters counters = new Counters();
        new MapDriver<LongWritable, Text, Text, IntWritable>()
                .withMapper(new MaxTemperatureMapper())
                .withInput(new LongWritable(0), value)
                .withCounters(counters)
                .runTest();

        Counter c = counters.findCounter(MaxTemperatureMapper.Temperature.MALFORMED);
        assertEquals(c.getValue(), 1);
    }


}
