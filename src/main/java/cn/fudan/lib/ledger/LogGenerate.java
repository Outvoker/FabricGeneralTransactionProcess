package cn.fudan.lib.ledger;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;

public class LogGenerate {
    public static String namesrvAddr = "192.168.137.152:9876";
    public static String groupName = "group1";
    public static String topic = "Topic";
    public static String tags = "tag";

    public static void main(String[] args) throws IOException {
        generate(10000);
    }

    public static void generate( long number) throws IOException {
        //Simulation to generate transaction data
        SimpleDateFormat myDate = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat myTime = new SimpleDateFormat("HHmmss");
        String curDate = myDate.format(System.currentTimeMillis());
        String curTime = myTime.format(System.currentTimeMillis());

        int count = 1;
        long dealNum = Long.parseLong(curDate.substring(2) + "0000000000");//Transaction number: String
        String[] goodsCode = {"XM2000", "DD2300", "YM1700", "LD1400", "HD1100", "CY2600", "DM2900", "KF3200", "JD3500", "NR3800", "YR4100", "JR4400"};
        String[] goodsName = {"小麦2000", "大豆2300", "玉米1700", "绿豆1400", "红豆1100", "茶叶2600", "大麦2900", "咖啡3200", "鸡蛋3500", "牛肉3800", "羊肉4100", "鸡肉4400"};
        double[] goodsPrice = {2000, 2300, 1700, 1400, 1100, 2600, 2900, 3200, 3500, 3800, 4100, 4400};
        RocketMQProducer producer1 = new RocketMQProducer(groupName, namesrvAddr);
        try {
            producer1.init();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        while (number > 0) {
            int countNum = 10000;
            while (number > 0 && countNum-- > 0) {
                StringBuffer sb = new StringBuffer();
                curDate = myDate.format(System.currentTimeMillis());
                curTime = myTime.format(System.currentTimeMillis());
                int no = (int) (Math.random() * 8);
                int index = (int) (Math.random() * goodsCode.length);
                double price = goodsPrice[index] + ((int) (Math.random() * 200)) - 100;//Transaction price: Double
                int dealCount = 100 * (int) (Math.random() * 9 + 1);//Transaction amount(lots): Integer
                sb.append(dealNum + count).append("|@|")//Transaction number: String
                        .append("JW8880001000000").append(no).append("|@|")//Supplier account number: String
                        .append(goodsCode[index]).append("|@|")//Commodity code: String
                        .append(goodsName[index]).append("|@|")//Commodity name: String
                        .append(dealNum + count + 1).append("|@|")//Order number: String
                        .append(((int) (Math.random() * 10)) % 2 == 1 ? "B" : "S").append("|@|")//Trading direction: B buy, S sell
                        .append(((int) (Math.random() * 10)) % 2 + 1).append("|@|")//Opening and closing flag: 1open, 2close
                        .append(price).append("|@|")//Transaction price: Double
                        .append(dealCount).append("|@|")//Transaction amount(lots): Integer
                        .append(price * dealCount).append("|@|")//Transaction amount: Double
                        .append(price / 10).append("|@|")//Transaction fee: Double
                        .append(((int) (Math.random() * 10)) % 4 + 1).append("|@|")//Transaction type: 1 normal, 2 forced, 3 negotiated, 4 commission
                        .append(curDate + " " + curTime).append("|@|")//Transaction time: Date+Time yyyyMMdd+HHmmss
                        .append("JW8880001000000").append((no + 1) % 8).append("|@|").append("\r\n");//Account number of opponent: String
                Message msg = new Message(topic,tags,sb.toString().getBytes());
                SendResult sendResult = null;
                try {
                    sendResult = producer1.send(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemotingException e) {
                    e.printStackTrace();
                } catch (MQClientException e) {
                    e.printStackTrace();
                } catch (MQBrokerException e) {
                    e.printStackTrace();
                }
                System.out.printf("%s%n", sendResult); // Return whether the message was successfully delivered via sendResult
                count += 2;
                number--;
            }
        }
        producer1.shutdown(); // if stop sending message, shutdown the Producer instance
    }
}
