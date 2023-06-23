package com.icloud.io;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

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

    public static class Comparator extends WritableComparator {
        private static final Text.Comparator TEXT_COMPARATOR = new Text.Comparator();

        public Comparator() {
            super(TextPair.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                           byte[] b2, int s2, int l2) {
            try {
                // 가변길이 정수의 길이, 인코딩 값
                int firstL1 = WritableUtils.decodeVIntSize(b1[s1]) + readVInt(b1, s1);
                int firstL2 = WritableUtils.decodeVIntSize(b2[s2]) + readVInt(b2, s2);
                int cmp = TEXT_COMPARATOR.compare(b1, s1, firstL1, b2, s2, firstL2);
                if (cmp != 0) {
                    return cmp;
                }
                return TEXT_COMPARATOR.compare(
                        b1, s1 + firstL1, l1 - firstL1,
                        b2, s2 + firstL2, l2 - firstL1
                );
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    // 맵리듀스가 TextPair 클래스를 만날 때마다 원시 비교자를 맵리듀스의 기본 비교자로 사용할 수 있도록 지정하기 위해 정적 블록에 해당 원시 비교자 등록
    static {
        WritableComparator.define(TextPair.class, new Comparator());
    }

    public static class FirstComparator extends WritableComparator {
        private static final Text.Comparator TEXT_COMPARATOR = new Text.Comparator();

        public FirstComparator() {
            super(TextPair.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                           byte[] b2, int s2, int l2) {
            try {
                int firstL1 = WritableUtils.decodeVIntSize(b1[s1]) + readVInt(b1, s1);
                int firstL2 = WritableUtils.decodeVIntSize(b2[s2]) + readVInt(b2, s2);
                return TEXT_COMPARATOR.compare(b1, s1, firstL1, b2, s2, firstL2);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            if (a instanceof TextPair && b instanceof TextPair) {
                return ((TextPair) a).first.compareTo(((TextPair) b).first);
            }
            return super.compare(a, b);
        }

    }


}
