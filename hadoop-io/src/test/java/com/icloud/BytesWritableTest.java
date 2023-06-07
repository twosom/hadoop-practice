package com.icloud;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.util.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BytesWritableTest extends AbstractWritableTest {

    BytesWritable b = new BytesWritable(new byte[]{3, 5});

    @Test
    void testByteWritableHexString() throws IOException {
        byte[] bytes = serialize(b);
        assertEquals(StringUtils.byteToHexString(bytes), "00000002" + "0305");
    }

    @Test
    void test() {
        b.setCapacity(11);
        assertEquals(b.getLength(), 2);
        assertEquals(b.getBytes().length, 11);
    }


}
