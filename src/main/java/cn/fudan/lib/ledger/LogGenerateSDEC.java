package cn.fudan.lib.ledger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Random;

public class LogGenerateSDEC {
    public static final String FILE_PATH = "D:\\university\\blockchain\\logTest\\";  //filePath
    public static final int memberNum = 8;  //number of file generated
    public static final int number = 1000;  //the number of logs contained by each file

    public static final String charStr = "0123456789abcdefghijklmnopqrstuvwxyz";
    public static final Integer EXID_LENGTH = 32;

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= memberNum; i++) {
            generate(i, number);
        }
    }

    public static void generate(Integer demanderID, long number) throws IOException {
        SimpleDateFormat myDate = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat myTime = new SimpleDateFormat("HHmmss");
        String curDate = myDate.format(System.currentTimeMillis());
        String curTime = myTime.format(System.currentTimeMillis());

        String fileName = curDate + "_" + demanderID;   //文件名称

        File file=new File(FILE_PATH + fileName);
        if(file.exists()){
            file.delete();
        }
        file.createNewFile();
        Writer out =new FileWriter(file);

        int[] suppliers = new int[7];
        int k = 0;
        for(int i=1;i<=8;i++){
            if(demanderID.equals(i))
                continue;
            suppliers[k++] = i;
        }
        int count = 1;
        while(number > 0){
            int countNum = 10000;
            StringBuffer sb = new StringBuffer();
            while(number > 0 && countNum-- > 0){
                int index = (int) (Math.random() * suppliers.length);
                int supplierID = suppliers[index];
                String exid = getCharAndNum(EXID_LENGTH);                    //随机生成exid
                String taskID = "CTN201712150000"+ demanderID + supplierID;  //生成taskID
                sb.append(count++).append("|@|")    //自增序号
                        .append("21").append("|@|")  //流程状态
                        .append(curDate).append("|@|")  //日期
                        .append(curTime).append("|@|")   //时间
                        .append(demanderID).append("|@|")  //需方会员
                        .append(supplierID).append("|@|")  //供方会员
                        .append(taskID).append("|@|")     //taskID
                        .append(curDate + "_" + curTime).append("|@|")  //业务流水号
                        .append(exid).append("|@|")                   //exid
                        .append("0002|@|2|@|1|@|0|@|031010|@|3|@|1|@|0000149|@|1|@||@|2|@||@|1|@||@|3|@|0000149|@|1|@|")
                        .append("\r\n");

                count++;
                number--;
            }
            out.write(sb.toString());
        }
        out.close();

    }

    public static String getCharAndNum(int length){
        Random random = new Random();
        StringBuffer valSb = new StringBuffer();
        int charLength = charStr.length();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charLength);
            valSb.append(charStr.charAt(index));
        }
        return valSb.toString();
    }
}
