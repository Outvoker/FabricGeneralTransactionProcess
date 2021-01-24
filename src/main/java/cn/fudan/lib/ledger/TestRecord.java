package cn.fudan.lib.ledger;

import org.joda.time.DateTime;

import java.util.Date;

public class TestRecord {
    private int  size;
    private long start;
    private long end;
    private Date startdate;

    public int getSize () {
        return size;
    }

    public void setSize (int size) {
        this.size = size;
    }

    public long getStart () {
        return start;
    }

    public void setStart (long start) {
        this.start = start;
    }

    public long getEnd () {
        return end;
    }

    public void setEnd (long end) {
        this.end = end;
    }

    public void showResult () {
//        System.out.println("开始时间：" + new DateTime(this.startdate).toString("yyyy-MM-dd HH:mm:ss.SSS") + " 批大小：" + size + "\t耗时：" + (end - start) + "ms");
        System.out.println(new DateTime(this.startdate).toString("HH:mm:ss.SSS"));
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }
}
