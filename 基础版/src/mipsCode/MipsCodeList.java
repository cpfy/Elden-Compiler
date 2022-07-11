package mipsCode;

import java.io.PrintStream;
import java.util.ArrayList;

public class MipsCodeList {
    private ArrayList<MipsCodeItem> mipsCodeList = new ArrayList<>();

    public MipsCodeList() {
    }

    public void addMipsCode(MipsCodeType type, String x, String y, String z, int i) {
        mipsCodeList.add(new MipsCodeItem(type, x, y, z, i));
    }

    public void addMipsCode(MipsCodeType type, String x, String y, String z) {
        mipsCodeList.add(new MipsCodeItem(type, x, y, z));
    }

    public void output(PrintStream printStream) {
        mipsCodeList.remove(mipsCodeList.size() - 1);
        for (MipsCodeItem mipsCodeItem: mipsCodeList) {
            printStream.println(mipsCodeItem.toString());
        }
    }
}
