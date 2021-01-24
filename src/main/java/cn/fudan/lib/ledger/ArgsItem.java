package cn.fudan.lib.ledger;

public class ArgsItem {
    private String time;
    private String DemanderID;
    private String SupplierID;
    private String TaskID;
    private int Count;
    private String[] ExidList;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDemanderID() {
        return DemanderID;
    }

    public void setDemanderID(String demanderID) {
        DemanderID = demanderID;
    }

    public String getSupplierID() {
        return SupplierID;
    }

    public void setSupplierID(String supplierID) {
        SupplierID = supplierID;
    }

    public String getTaskID() {
        return TaskID;
    }

    public void setTaskID(String taskID) {
        TaskID = taskID;
    }

    public int getCount() {
        return Count;
    }

    public void setCount(int count) {
        Count = count;
    }

    public String[] getExidList() {
        return ExidList;
    }

    public void setExidList(String[] exidList) {
        ExidList = exidList;
    }
}
