package AST;

import symbolTable.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public abstract class Node {

    static String getFloatString(float in) {
        String s = Integer.toHexString(Float.floatToIntBits(in));
        return "0x" + s;
    }

    static HashSet<String> globalNames = new HashSet<>();
    public static String newGlobalName(String name) {
        return name;
    }

    static int labels = 0;

    static int reloadNum = 0;

    static boolean isMain = false;

    static boolean isPoint;

    static void setIsPoint(boolean b) {
        isPoint = b;
    }

    static boolean isPoint() {
        return isPoint;
    }

    static HashMap<String, String> reloadMap = new HashMap<>();

    static boolean isGlobal = true;

    static String declType = null;

    public String newTemp() {
        return "%t" + labels++;
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
        return "l" + String.valueOf(labels++);
    }

    public abstract void addMidCode();

    public ArrayList<String> getLLVMIR() {

        
//        LLVMIR.add("declare dso_local void @starttime()\n");
//        LLVMIR.add("declare dso_local void @stoptime()\n");
        LLVMIR.add("declare dso_local i32 @getint()\n");
        LLVMIR.add("declare dso_local i32 @getch()\n");
        LLVMIR.add("declare dso_local float @getfloat()\n");
        LLVMIR.add("declare dso_local i32 @getarray(i32*)\n");
        LLVMIR.add("declare dso_local i32 @getfarray(float*)\n");
        LLVMIR.add("declare dso_local void @putint(i32)\n");
        LLVMIR.add("declare dso_local void @putch(i32)\n");
        LLVMIR.add("declare dso_local void @putfloat(float)\n");
        LLVMIR.add("declare dso_local void @putarray(i32, i32*)\n");
        LLVMIR.add("declare dso_local void @putfarray(i32, float*)\n");


        return LLVMIR;
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

    public String globalArrayInit(String type, ArrayList<Integer> dims, ArrayList<String> values, int n, ArrayList<Integer> p) {
        StringBuilder ans = new StringBuilder();
        String detailType = getArrayType(dims, type, n);
        if (n < dims.size()) {
            ans.append(detailType + " " + "[");
            for (int i = 0; i < dims.get(n); i++) {
                if (i != 0) {
                    ans.append(", ");
                }
                ans.append(globalArrayInit(type, dims, values, n + 1, p));
            }
            ans.append("]");
        }
        else {
            p.set(0, p.get(0) + 1);
            return type + " " + values.get(p.get(0) - 1);
        }
        return ans.toString();
    }

    public void localArrayInit(String type, ArrayList<Integer> dims, ArrayList<String> values, int n, ArrayList<Integer> p, String tempIn) {
        String detailType = getArrayType(dims, type, n);
        String nt = null;
        if (n < dims.size()) {
            nt = newTemp();
            addCode(nt + " = getelementptr inbounds " + detailType + ", " + detailType + "* " + tempIn + ", i32 0, i32 0\n");
            tempIn = nt;
            for (int i = 0; i < dims.get(n); i++) {
                detailType = getArrayType(dims, type, n + 1);
                if (i != 0) {
                    nt = newTemp();
                    addCode(nt + " = getelementptr inbounds " + detailType + ", " + detailType + "* " + tempIn + ", i32 1\n");
                    tempIn = nt;
                }
                localArrayInit(type, dims, values, n + 1, p, nt);
            }
        }
        else {
            p.set(0, p.get(0) + 1);
            addCode("store " + type + " " + values.get(p.get(0) - 1) + ", "
                    + type + "* " + tempIn + "\n");
        }
    }
}
