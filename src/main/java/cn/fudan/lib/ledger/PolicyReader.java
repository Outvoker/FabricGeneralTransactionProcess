package cn.fudan.lib.ledger;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by Elegenthus on 2020/7/14.
 */
public class PolicyReader {

    private String separator;
    private String filePath;
    private int time;
    private int size;
    private int num;
    private String[] mergedItem;
    private String[] originalItem;
    private String[] filteredItem;
    private String[] mergedContent;
    private int[] filteredItemIndex;
    private int[] mergedItemIndex;
    private int[] mergedContentIndex;

    public int[] getFilteredItemIndex() {
        return filteredItemIndex;
    }

    public int[] getMergedItemIndex() {
        return mergedItemIndex;
    }

    public int[] getMergedContentIndex() {
        return mergedContentIndex;
    }

    public String getSeparator() {
        return separator;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getTime() {
        return time;
    }

    public int getSize() {
        return size;
    }

    public int getNum() {
        return num;
    }

    public String[] getMergedItem() {
        return mergedItem;
    }

    public String[] getOriginalItem() {
        return originalItem;
    }

    public String[] getFilteredItem() {
        return filteredItem;
    }

    public String[] getMergedContent() {
        return mergedContent;
    }

    public static Map getMap() {
        return map;
    }

    private static Map map;

    public PolicyReader(String path) throws FileNotFoundException {
            Yaml yaml = new Yaml();
            File f = new File(path);

            map = (Map)yaml.load(new FileInputStream(f));

    }

    public static void main(String[] args) {

        try {
            Yaml yaml = new Yaml();
            File f = new File("./src/main/java/cn/fudan/lib/ledger/test.yml");

            Map result = (Map)yaml.load(new FileInputStream(f));


            for(Object item : result.keySet()){
                System.out.println(item + ":" + result.get(item));

            }



        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
