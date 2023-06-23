package com.icloud.io;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntPair implements WritableComparable<IntPair> {

    private int first;
    private int second;

    public IntPair() {
    }

    public IntPair(int first, int second) {
        set(first, second);
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public void set(int first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(first);
        out.writeInt(second);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.first = in.readInt();
        this.second = in.readInt();
    }

    @Override
    public int hashCode() {
        return this.first * 163 + this.second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntPair) {
            IntPair ip = (IntPair) obj;
            return first == ip.first && second == ip.second;
        }
        return false;
    }

    @Override
    public int compareTo(IntPair ip) {
        int cmp = compare(first, ip.first);
        if (cmp != 0) {
            return cmp;
        }
        return compare(second, ip.second);
    }

    @Override
    public String toString() {
        return this.first + "\t" + this.second;
    }

    public static int compare(int a, int b) {
        return Integer.compare(a, b);
    }

}
