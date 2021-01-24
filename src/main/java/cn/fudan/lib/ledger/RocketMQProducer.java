package cn.fudan.lib.ledger;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

public class RocketMQProducer {
    private String groupName;  //The name of the consumer group.

    private String namesrvAddr;    //The address of nameserver.

    // Specify consumer group name and nameserver addresses.
    public RocketMQProducer(String groupName, String namesrvAddr) {
        this.groupName = groupName;
        this.namesrvAddr = namesrvAddr;
    }

    private DefaultMQProducer producer;

    //Instantiate producer.
    public void init() throws MQClientException {
        producer = new DefaultMQProducer(this.groupName);
        this.producer.setNamesrvAddr(this.namesrvAddr);
        this.producer.start();
        System.out.println("Start the Log Producer...");
    }

    //Send mesage.
    public SendResult send(Message msg) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        return this.producer.send(msg);
    }

    ////Shutdown the producer instance.
    public void shutdown() {
        producer.shutdown();
    }
/*    public static void main(String[] args) throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        RocketMQProducer producer1 = new RocketMQProducer("group1","192.168.1.100:9876");
        producer1.init();
        Message msg = new Message("test","tag",("hello").getBytes());
        SendResult sendResult = producer1.send(msg);
        System.out.printf("%s%n", sendResult);
        producer1.producer.shutdown();
    }*/
}
