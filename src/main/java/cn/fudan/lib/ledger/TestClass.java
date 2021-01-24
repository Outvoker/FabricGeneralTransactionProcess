package cn.fudan.lib.ledger;

import java.util.HashMap;
import java.util.HashSet;

public class TestClass {
    private HashSet<String> set;
    private HashMap<HashSet<String>, TestClass> map;

    public TestClass(HashSet<String> set, HashMap<HashSet<String>, TestClass> map) {
        this.set = set;
        this.map = map;
    }

    public void upload() {
        this.map.remove(this.set);
    }
}
