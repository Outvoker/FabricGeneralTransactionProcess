package cn.fudan.lib.ledger;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.io.IOException;
import java.util.List;

public class RocketMQCounsumer {

    static String policyPath = "./src/main/java/cn/fudan/lib/ledger/testCommodity.yml";
    static String namesrvAddr = "192.168.137.152:9876";
    static String consumerGroup = "group";
    static String topic = "Topic";

    public static void main(String[] args) throws IOException, MQClientException {
        // Instantiate with specified consumer group name.
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);

        // Specify name server addresses.
        consumer.setNamesrvAddr(namesrvAddr);

        // Subscribe one more more topics to consume.
        consumer.subscribe(topic, "*");
        // Register callback to execute on arrival of messages fetched from brokers.
        consumer.registerMessageListener(new MessageListenerConcurrently() {


            TestHandler handler = new TestHandler(policyPath);
            Object lock = new Object();

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                for (MessageExt msg: msgs) {
                    try {
                        synchronized (lock) {
                            System.out.println("Receive from MQ:" + new String(msg.getBody()));
                            handler.logProcess(new String(msg.getBody()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                System.out.println(new String(msgs.get(msgs.size()-1).getBody()));
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

//        consumer.setConsumeThreadMin(1);
//        consumer.setConsumeThreadMax(1);

        //Launch the consumer instance.
        consumer.start();

        System.out.printf("Consumer Started.%n");
        //Shutdown the consumer instance.
        //consumer.shutdown();
    }
}
