package com.icloud;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;


public class ShowFileStatusTest {

    /**
     * 현재 운영되고 있는 HDFS 클러스터 테스트
     */
    private MiniDFSCluster cluster;
    private FileSystem fs;

    @BeforeEach
    public void setUp() throws IOException {
        Configuration conf = new Configuration();
        if (System.getProperty("test.build.data") == null)
            System.setProperty("test.build.data", "/tmp");
        this.cluster = new MiniDFSCluster.Builder(conf).build();
        fs = cluster.getFileSystem();
        OutputStream out = fs.create(new Path("/dir/file"));
        out.write("content".getBytes(StandardCharsets.UTF_8));
        out.close();
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (fs != null) fs.close();
        if (cluster != null) cluster.shutdown();
    }

    @Test
    void throwFileNotFoundForNonExistentFile() {
        assertThrows(FileNotFoundException.class,
                () -> fs.getFileStatus(new Path("no-such-file"))
        );
    }

    @Test
    void fileStatusForFile() throws IOException {
        Path file = new Path("/dir/file");
        FileStatus stat = fs.getFileStatus(file);
        assertAll(
                () -> assertEquals(stat.getPath().toUri().getPath(), "/dir/file"),
                () -> assertFalse(stat.isDirectory()),
                () -> assertEquals(stat.getLen(), 7L),
                () -> assertTrue(stat.getModificationTime() <= System.currentTimeMillis()),
                () -> assertEquals(stat.getReplication(), (short) 1),
                () -> assertEquals(stat.getBlockSize(), 128 * 1024 * 1024L),
                () -> assertEquals(stat.getOwner(), System.getProperty("user.name")),
                () -> assertEquals(stat.getGroup(), "supergroup"),
                () -> assertEquals(stat.getPermission().toString(), "rw-r--r--")
        );
    }

    @Test
    void fileStatusForDirectory() throws IOException {
        Path dir = new Path("/dir");
        FileStatus stat = fs.getFileStatus(dir);
        assertAll(
                () -> assertEquals(stat.getPath().toUri().getPath(), "/dir"),
                () -> assertTrue(stat.isDirectory()),
                () -> assertEquals(stat.getLen(), 0L),
                () -> assertTrue(stat.getModificationTime() <= System.currentTimeMillis()),
                () -> assertEquals(stat.getReplication(), (short) 0),
                () -> assertEquals(stat.getBlockSize(), 0L),
                () -> assertEquals(stat.getOwner(), System.getProperty("user.name")),
                () -> assertEquals(stat.getGroup(), "supergroup"),
                () -> assertEquals(stat.getPermission().toString(), "rwxr-xr-x")
        );
    }

    @Test
    void whenFileCreateThenFileShouldBeExists() throws IOException {
        Path p = new Path("p");
        fs.create(p);
        assertTrue(fs.exists(p));
    }

    @Test
    void whenFileFlushedThenFileSizeIsNotGreaterThen0() throws IOException {
        Path p = new Path("p");
        OutputStream out = fs.create(p);
        out.write("content".getBytes(StandardCharsets.UTF_8));
        out.flush();
        assertEquals(fs.getFileStatus(p).getLen(), 0L);
    }

    @Test
    void whenFileHFlushedThenFileSizeIsGreaterThen0() throws IOException {
        Path p = new Path("p");
        FSDataOutputStream output = fs.create(p);
        output.write("content".getBytes(StandardCharsets.UTF_8));
        output.hflush(); // 데이터노드가 디스크를 데이터에 쓰는 것이 아닌, 메모리에 데이터를 쓰기 때문에 데이터 유실에 각별히 주의 필요
        assertEquals(fs.getFileStatus(p).getLen(), "content".length());
    }
}

