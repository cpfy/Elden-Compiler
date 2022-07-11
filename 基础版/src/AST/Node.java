package AST;

import midCode.MidCodeList;
import symbolTable.Table;

import java.util.ArrayList;

public abstract class Node {
    static int labels = 0;

    static MidCodeList midCodeList = new MidCodeList();

    static Table table = new Table();

    static ArrayList<String> strings = new ArrayList<>();

    public void addString(String s) {
        strings.add(s);
    }

    public ArrayList<String> getStrings() {
        return strings;
    }

    public int newLable() {
        return ++labels;
    }

    public abstract void addMidCode();

    public MidCodeList getMidCodeList() {
        return midCodeList;
    }

    static int jumpDst = 0;

    public int newJumpDst() {
        return ++jumpDst;
    }

}
