package com.icloud;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.util.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class IntWritableTest extends AbstractWritableTest {

    IntWritable writable = new IntWritable(163);

    @Test
    void whenCreateIntWritableAndSetValueThenShouldNotBeNull() {
        IntWritable writable = new IntWritable();
        writable.set(163);
        assertNotNull(writable);
    }

    @Test
    void whenCreateIntWritableWithValueThenShouldNotBeNull() {
        IntWritable intWritable = new IntWritable(163);
        assertNotNull(intWritable);
    }

    @Test
    void whenCreateIntWritableWithValueAndSerializeThenLengthShouldBeFour() throws IOException {
        byte[] bytes = serialize(writable);
        assertEquals(bytes.length, 4);
    }

    @Test
    void whenSerializedWritableHexStringShouldBeEqualTo() throws IOException {
        byte[] bytes = serialize(writable);
        assertEquals(StringUtils.byteToHexString(bytes), "000000a3");
    }

    @Test
    void whenDeserializeExistsValueThenShouldBeEqualTo() throws IOException {
        byte[] bytes = serialize(writable);
        IntWritable newWritable = new IntWritable();
        deserialize(newWritable, bytes);
        assertEquals(newWritable.get(), 163);
    }

    @Test
    void whenCompareWithWritableComparatorThenShouldBeWork() {
        WritableComparator comparator = WritableComparator.get(IntWritable.class);
        IntWritable w1 = new IntWritable(163);
        IntWritable w2 = new IntWritable(67);
        assertTrue(comparator.compare(w1, w2) > 0);
    }

    @Test
    void whenCompareSerializedValueWithWritableComparatorThenShouldBeWork() throws IOException {
        WritableComparator comparator = WritableComparator.get(IntWritable.class);
        IntWritable w1 = new IntWritable(163);
        IntWritable w2 = new IntWritable(67);
        byte[] b1 = serialize(w1);
        byte[] b2 = serialize(w2);
        assertTrue(comparator.compare(b1, 0, b1.length, b2, 0, b2.length) > 0);
    }

    @Test
    void whenSerializeVIntWritableAndToHexStringThenEqualTo() throws IOException {
        byte[] data = serialize(new VIntWritable(163));
        assertEquals(StringUtils.byteToHexString(data), "8fa3");
    }


}
