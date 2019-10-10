package com.product.eshop.kafka;

import com.product.eshop.kafka.processor.KafkaMessageProcessor;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author yangqian
 * @date 2019/10/1
 */
public class KafkaConsumer implements Runnable {

    private ConsumerConnector consumerConnector;
    private String topic;

    public KafkaConsumer(String topic) {
        this.consumerConnector = Consumer.createJavaConsumerConnector(createConsumerConfig());
        this.topic = topic;
    }

    @Override
    public void run() {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>(1);
        topicCountMap.put(topic, 1);
        /**
         * Kafka对应多个KafkaStream
         */
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap
                = consumerConnector.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        for (final KafkaStream stream : streams) {
            // 创建执行线程的相关逻辑
            new Thread(new KafkaMessageProcessor(stream)).start();
        }
    }

    /**
     * 创建Kafka Consumer config
     *
     * @return
     */
    private static ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", "172.16.7.140:2181,172.16.7.141:2181,172.16.7.142:2181");
        props.put("group.id", "eshop-cache-group");
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }

}
