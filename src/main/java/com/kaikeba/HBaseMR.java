package com.kaikeba;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

public class HBaseMR {

    public static class HBaseMapper extends TableMapper<Text,Put>{
        @Override
        protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
            //获取rowkey的字节数组
            byte[] bytes = key.get();
            String rowkey = Bytes.toString(bytes);
            //构建一个put对象
            Put put = new Put(bytes);
            //获取一行中所有的cell对象
            Cell[] cells = value.rawCells();
            for (Cell cell : cells) {
                // f1列族
                if("f1".equals(Bytes.toString(CellUtil.cloneFamily(cell)))){
                    // name列名
                    if("name".equals(Bytes.toString(CellUtil.cloneQualifier(cell)))){
                        put.add(cell);
                    }
                    // age列名
                    if("age".equals(Bytes.toString(CellUtil.cloneQualifier(cell)))){
                        put.add(cell);
                    }
                }
            }
            if(!put.isEmpty()){
                context.write(new Text(rowkey),put);
            }

        }
    }

    public  static  class HbaseReducer extends TableReducer<Text,Put,ImmutableBytesWritable>{
        @Override
        protected void reduce(Text key, Iterable<Put> values, Context context) throws IOException, InterruptedException {
            for (Put put : values) {
                context.write(null,put);
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();

        Scan scan = new Scan();

        Job job = Job.getInstance(conf);
        job.setJarByClass(HBaseMR.class);
        //使用TableMapReduceUtil 工具类来初始化我们的mapper
        TableMapReduceUtil.initTableMapperJob(TableName.valueOf(args[0]),scan,HBaseMapper.class,Text.class,Put.class,job);
        TableMapReduceUtil.addDependencyJars(job);

        //使用TableMapReduceUtil 工具类来初始化我们的reducer
        TableMapReduceUtil.initTableReducerJob(args[1],HbaseReducer.class,job);

        //设置reduce task个数
        job.setNumReduceTasks(1);

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }

}
