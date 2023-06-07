package com.icloud;

import org.apache.hadoop.io.Text;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextWritableTest {

    @Test
    void testTextIndexMethod() {
        Text t = new Text("hadoop");
        assertEquals(t.getLength(), 6);
        assertEquals(t.getBytes().length, 6);
        assertEquals(t.charAt(2), (int) 'd');
        assertEquals(t.charAt(100), -1, "Out of bounds");
    }

    @Test
    void testTextFindMethod() {
        Text t = new Text("hadoop");
        assertEquals(t.find("do"), 2, "Find a substring");
        assertEquals(t.find("o"), 3, "Finds first 'o'");
        assertEquals(t.find("o", 4), 4, "Finds 'o' from position 4 or later");
        assertEquals(t.find("pig"), -1, "No match");
    }

    @Test
    void textObjectShouldBeVariable() {
        Text t = new Text("hadoop");
        assertEquals(t.getLength(), 6);
        assertEquals(t.getBytes().length, 6);
        t.set("pig");
        assertEquals(t.getLength(), 3);
        assertEquals(t.getBytes().length, 3);
    }

    @Test
    void test() {
        Text t = new Text("hadoop");
        t.set(new Text("pig"));
        assertEquals(t.getLength(), 3);
        assertEquals(t.getBytes().length, 6);
    }
    @Test
    void textObjectCanConvertToString() {
        assertEquals(new Text("hadoop").toString(), "hadoop");
    }

}
