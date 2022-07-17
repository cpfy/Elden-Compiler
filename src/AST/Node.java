package AST;

import symbolTable.Table;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Node {
    static int labels = 0;

    static int reloadNum = 0;

    static HashMap<String, String> reloadMap = new HashMap<>();

    public String newReload() {
        String key = "&" + ++reloadNum;
        reloadMap.put(key, null);
        return key;
    }

    public void addReload(String key, String value) {
        reloadMap.put(key, value);
    }

    static boolean isGlobal = true;

    static String declType = null;

    public String newTemp() {
        return "%" + labels++;
    }

    public void setDeclType(String s) {
        declType = s;
    }

    public String getDeclType() {
        return declType;
    }

    public void clearLabels() {
        labels = 0;
    }
//    static MidCodeList midCodeList = new MidCodeList();
    static StringBuilder LLVMIR = new StringBuilder();

    static Table table = new Table();

    static ArrayList<String> strings = new ArrayList<>();

    public void addString(String s) {
        strings.add(s);
    }

    public ArrayList<String> getStrings() {
        return strings;
    }

    public String newLable() {
        return String.valueOf(labels++);
    }

    public abstract void addMidCode();

    public String getLLVMIR() {
        String IR = LLVMIR.toString();
        for (String key: reloadMap.keySet()) {
            if (reloadMap.get(key) == null) {
                continue;
            }
            IR = IR.replace(key, reloadMap.get(key));
        }
        return IR;
    }

    public void addCode(String s) {
        LLVMIR.append(s);
    }

}
