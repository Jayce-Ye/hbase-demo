package com.kaikeba;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class HBaseLoad {

    public static class LoadMapper  extends Mapper<LongWritable,Text,ImmutableBytesWritable,Put> {
        @Override
        protected void map(LongWritable key, Text value, Mapper.Context context) throws IOException, InterruptedException {
            String[] split = value.toString().split(" ");
            Put put = new Put(Bytes.toBytes(split[0]));
            put.addColumn("f1".getBytes(),"name".getBytes(),split[1].getBytes());
            put.addColumn("f1".getBytes(),"age".getBytes(), split[2].getBytes());
            context.write(new ImmutableBytesWritable(Bytes.toBytes(split[0])),put);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        final String INPUT_PATH=  "hdfs://node1:9000/input";
        final String OUTPUT_PATH= "hdfs://node1:9000/output_HFile";
        Configuration conf = HBaseConfiguration.create();

        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection.getTable(TableName.valueOf("t4"));
        Job job= Job.getInstance(conf);

        job.setJarByClass(HBaseLoad.class);
        job.setMapperClass(LoadMapper.class);
        job.setMapOutputKeyClass(ImmutableBytesWritable.class);
        job.setMapOutputValueClass(Put.class);
        //指定输出的类型HFileOutputFormat2
        job.setOutputFormatClass(HFileOutputFormat2.class);

        HFileOutputFormat2.configureIncrementalLoad(job,table,connection.getRegionLocator(TableName.valueOf("t4")));
        FileInputFormat.addInputPath(job,new Path(INPUT_PATH));
        FileOutputFormat.setOutputPath(job,new Path(OUTPUT_PATH));
        System.exit(job.waitForCompletion(true)?0:1);


    }
}
