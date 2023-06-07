package com.icloud;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TextPair implements WritableComparable<TextPair> {

    private Text first;
    private Text second;

    public TextPair() {
        this.set(new Text(), new Text());
    }

    public TextPair(String first, String second) {
        this.set(new Text(first), new Text(second));
    }

    public TextPair(Text first, Text second) {
        this.set(first, second);
    }

    public void set(Text first, Text second) {
        this.first = first;
        this.second = second;
    }

    public Text getFirst() {
        return this.first;
    }

    public Text getSecond() {
        return this.second;
    }

    //TODO Writable 인스턴스는 가변적이고 (가끔) 재사용 됨.
    // 따라서 write() 메소드 혹은 readFields() 메소드로 객체를 할당하지 않게 주의 필요
    @Override
    public void write(DataOutput out) throws IOException {
        first.write(out);
        second.write(out);
    }

    //TODO Writable 인스턴스는 가변적이고 (가끔) 재사용 됨.
    // 따라서 write() 메소드 혹은 readFields() 메소드로 객체를 할당하지 않게 주의 필요
    @Override
    public void readFields(DataInput in) throws IOException {
        first.readFields(in);
        second.readFields(in);
    }

    @Override
    public int hashCode() {
        return first.hashCode() * 163 + second.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextPair) {
            TextPair tp = (TextPair) obj;
            return this.first.equals(tp.first) && this.second.equals(tp.second);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.first + "\t" + this.second;
    }

    @Override
    public int compareTo(TextPair tp) {
        int cmp = first.compareTo(tp.first);
        if (cmp != 0) {
            return cmp;
        }
        return second.compareTo(tp.second);
    }
}
