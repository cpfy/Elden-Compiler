package AST;

import symbolTable.Table;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Node {
    static int labels = 0;

    static int reloadNum = 0;

    static boolean isPoint;

    static void setIsPoint(boolean b) {
        isPoint = b;
    }

    static boolean isPoint() {
        return isPoint;
    }

    static HashMap<String, String> reloadMap = new HashMap<>();

    public String newReload() {
        String key = "&" + ++reloadNum + "&";
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
    static ArrayList<String> LLVMIR = new ArrayList<>();

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

    public ArrayList<String> getLLVMIR() {
        ArrayList<String> ans = new ArrayList<>();
        for (String s: LLVMIR) {
            for (String key: reloadMap.keySet()) {
                if (reloadMap.get(key) == null) {
                    continue;
                }
                if (!s.contains("&")) {
                    break;
                }
                s = s.replaceAll(key, reloadMap.get(key));
            }
            ans.add(s);
        }
        return ans;
    }

    public String getArrayType(ArrayList<Exp> exps, String type) {
        StringBuilder ans = new StringBuilder();
        for (Exp exp: exps) {
            ans.append("[").append(exp.getValue()).append(" x ");
        }
        ans.append(type);
        for (Exp exp: exps) {
            ans.append("]");
        }
        return ans.toString();
    }

    public String getArrayType(ArrayList<Integer> exps, String type, int n) {
        StringBuilder ans = new StringBuilder();
        int i = 0;
        for (Integer exp: exps) {
            if (i++ >= n) {
                ans.append("[").append(exp).append(" x ");
            }
        }
        ans.append(type);
        i = 0;
        for (Integer exp: exps) {
            if (i++ >= n) {
                ans.append("]");
            }
        }
        return ans.toString();
    }

    public void addCode(String s) {
        LLVMIR.add(s);
    }

    public void addCode(ArrayList<String> s) {
        LLVMIR.addAll(s);
    }

}
