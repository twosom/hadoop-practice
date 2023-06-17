package com.icloud;

import com.icloud.util.JobBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XMLFileConverter extends Configured implements Tool {


    static class XmlFileRecordReader extends RecordReader<NullWritable, BytesWritable> {

        private FileSplit fileSplit;
        private Configuration conf;
        private BytesWritable value = new BytesWritable();
        private boolean processed = false;

        @Override
        public void initialize(InputSplit split, TaskAttemptContext context)
                throws IOException, InterruptedException {
            if (!(split instanceof FileSplit)) return;
            this.fileSplit = (FileSplit) split;
            this.conf = context.getConfiguration();
        }

        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            if (this.processed) return false;
            final int contentsLength = (int) this.fileSplit.getLength();
            final byte[] contents = new byte[contentsLength];
            final Path file = this.fileSplit.getPath();
            final FileSystem fs = file.getFileSystem(this.conf);
            FSDataInputStream in = null;
            try {
                in = fs.open(file);
                IOUtils.readFully(in, contents, 0, contentsLength);
                this.value.set(contents, 0, contentsLength);
            } finally {
                IOUtils.closeStream(in);
            }
            this.processed = true;
            return true;
        }

        @Override
        public NullWritable getCurrentKey() throws IOException, InterruptedException {
            return NullWritable.get();
        }

        @Override
        public BytesWritable getCurrentValue() throws IOException, InterruptedException {
            return this.value;
        }

        @Override
        public float getProgress() throws IOException, InterruptedException {
            return this.processed ? 1.0f : 0.0f;
        }

        @Override
        public void close() throws IOException {

        }
    }


    static class XMLFileInputFormat extends FileInputFormat<NullWritable, BytesWritable> {
        @Override
        protected boolean isSplitable(JobContext context, Path file) {
            return false;
        }


        @Override
        public RecordReader<NullWritable, BytesWritable> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
            RecordReader<NullWritable, BytesWritable> reader = new XmlFileRecordReader();
            reader.initialize(split, context);
            return reader;
        }
    }


    static class XMLMapper extends Mapper<NullWritable, BytesWritable, NullWritable, Text> {

        private final XMLInputFactory factory = XMLInputFactory.newInstance();
        private Text value = new Text();

        @Override
        protected void map(NullWritable key,
                           BytesWritable value,
                           Mapper<NullWritable, BytesWritable, NullWritable, Text>.Context context)
                throws IOException, InterruptedException {
            InputStream in = new ByteArrayInputStream(value.getBytes());

            try {
                XMLStreamReader xmlReader = factory.createXMLStreamReader(in);
                while (xmlReader.hasNext()) {
                    switch (xmlReader.getEventType()) {
                        case XMLStreamReader.START_ELEMENT: {
                            if (xmlReader.hasName()) {
                                String name = xmlReader.getLocalName();
                                switch (name) {
                                    case "title":
                                        String title = xmlReader.getElementText();
                                        this.value.set("title = " + title + "\n");
                                        break;
                                    case "ns":
                                        String ns = xmlReader.getElementText();
                                        this.value.set(this.value + "ns = " + ns + "\n");
                                        break;
                                    case "text":
                                        String text = xmlReader.getElementText();
                                        this.value.set(this.value + "text = " + text + "\n");
                                        context.write(NullWritable.get(), this.value);
                                        break;
                                }
                            }
                            break;
                        }

                        case XMLStreamReader.END_ELEMENT: {

                        }
                    }
                    xmlReader.next();
                }
                xmlReader.close();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeStream(in);
            }
        }
    }


    @Override
    public int run(String[] args) throws Exception {
        Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
        if (job == null) return -1;

        job.setInputFormatClass(XMLFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(XMLMapper.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new XMLFileConverter(), args);
        System.exit(exitCode);
    }
}
