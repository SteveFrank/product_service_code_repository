package com.eshop.storm.wc;

import lombok.extern.slf4j.Slf4j;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 单词计数拓扑
 * @author yangqian
 * @date 2019/10/4
 */
@Slf4j
public class WordCountTopology {

    /**
     * spout
     * spout 通过继承一个积累，实现对应的接口
     * 这里主要是负责从数据源获取到对应的数据
     * 此处做一个简化，不存外部数据源获取数据了
     * 只是通过内部不断发射一些句子进行统计的数据源头
     */
    public static class RandomSentenceSpout extends BaseRichSpout {

        private SpoutOutputCollector spoutOutputCollector;
        private Random random;

        /**
         * open方法是对于spout进行初始化私用的
         * 比如说 创建一个线程池 数据连接池 或者 httpclient
         * @param map
         * @param topologyContext
         * @param spoutOutputCollector
         */
        @Override
        public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
            // 在open初始化的时候会传入一个东西，叫做spoutOutputCollector
            // 用于数据的发射
            this.spoutOutputCollector = spoutOutputCollector;
            // 构造一个随机数据生产对象
            this.random = new Random();
        }

        /**
         * 真正在运行的时候，最终会运行在task中
         * 某个worker进程中的某个executor线程，内部的某一个task中
         * 那个task会负责不断无限循环调用nextTuple()方法 形成一个数据流
         */
        @Override
        public void nextTuple() {
            Utils.sleep(100);
            String[] sentences = new String[]{"the cow jumped over the moon", "an apple a day keeps the doctor away",
                    "four score and seven years ago", "snow white and the seven dwarfs", "i am at two with nature"};
            final String sentence = sentences[random.nextInt(sentences.length)];
            // 构建一个tuple
            // tuple是最小的数据单位 无限个tuple组成的流就是一个stream
            spoutOutputCollector.emit(new Values(sentence));
        }

        /**
         * declareOutputFields
         * 很重要 这个方法是定一个放射出去的每个tuple中的每个field的名称是什么
         * @param outputFieldsDeclarer
         */
        @Override
        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
            outputFieldsDeclarer.declare(new Fields("sentence"));
        }
    }

    /**
     * 业务处理流程1
     * 写一个bolt 直接继承一个BaseRichBolt基类
     * 实现里面的所有方法即可 每个bolt代码 同样是发送到worker某个executor的task里面进行运行
     */
    public static class SplitSentence extends BaseRichBolt {

        private OutputCollector outputCollector;

        /**
         * 对于Bolt来说 第一个方法就是prepare方法
         * 这个也是Bolt的这个tuple发射器
         * @param conf
         * @param topologyContext
         * @param outputCollector
         */
        @Override
        public void prepare(Map conf, TopologyContext topologyContext, OutputCollector outputCollector) {
            this.outputCollector = outputCollector;
        }

        /**
         * 每次接受到一条数据后，就会交个executor方法进行执行
         * @param tuple
         */
        @Override
        public void execute(Tuple tuple) {
            String sentence = tuple.getStringByField("sentence");
            String[] words  = sentence.split(" ");
            for (String word : words) {
                outputCollector.emit(new Values(word));
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
            outputFieldsDeclarer.declare(new Fields("word"));
        }
    }

    /**
     * 处理最终的业务逻辑汇总
     */
    public static class WordCount extends BaseRichBolt {

        private Map<String, Long> wordCounts = new HashMap<>();
        private OutputCollector outputCollector;

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
            this.outputCollector = outputCollector;
        }

        @Override
        public void execute(Tuple tuple) {
            String word = tuple.getStringByField("word");
            Long count  = wordCounts.get(word);
            if (count == null) {
                count = 0L;
            }
            count ++;
            wordCounts.put(word, count);
            log.info("统计单词计数:[单词]:{},[计数]:{}", word, count);
            outputCollector.emit(new Values(word, count));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
            outputFieldsDeclarer.declare(new Fields("word", "count"));
        }
    }

    public static void main(String[] args){
        // 在 main 方法中 会将相关的节点组合起来，构建成为一个拓扑
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        // param1:给spout设置名称
        // param2:创建spout的对象
        // param3:设置spout中的executor的个数
        topologyBuilder.setSpout("RandomSentence", new RandomSentenceSpout(), 2);
        // 某人task数量与executor是一样的在没有设置的情况下
        topologyBuilder.setBolt("SplitSentence", new SplitSentence(), 5)
                .setNumTasks(10).shuffleGrouping("RandomSentence");
        // 这里很重要 从SplitSentence 出来的数据一样会进入到下游的置顶的同一个task中
        // 只有这样才能欧准确的统计出每个单词的数量
        topologyBuilder.setBolt("WordCount", new WordCount(), 10)
                .setNumTasks(20).fieldsGrouping("SplitSentence", new Fields("word"));

        Config config = new Config();
        config.setDebug(true);

        // 说明在命令行执行打算提交到storm集群上去
        if (args != null && args.length > 0) {
            config.setNumWorkers(3);
            try {
                StormSubmitter.submitTopology(args[0], config, topologyBuilder.createTopology());
            } catch (AlreadyAliveException e) {
                e.printStackTrace();
            } catch (InvalidTopologyException e) {
                e.printStackTrace();
            } catch (AuthorizationException e) {
                e.printStackTrace();
            }
        } else {
            // 本地测试运行
            config.setMaxTaskParallelism(3);
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology("WordCountTopology", config, topologyBuilder.createTopology());

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            localCluster.shutdown();
        }

    }

}
