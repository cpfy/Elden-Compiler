package midCode;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;

public class MidCodeList {
    private LinkedList<MidCodeItem> midCodeItems;

    public MidCodeList() {
        this.midCodeItems = new LinkedList<>();
    }

    public void addMidCodeItem(MidCodeType type, String xx, String yy, String zz) {
        //System.out.println(type + " " + xx + " " + yy + " " + zz);
        midCodeItems.add(new MidCodeItem(type, xx, yy, zz));
    }

    public LinkedList<MidCodeItem> getMidCodeItems() {
        return midCodeItems;
    }

    public void output() throws FileNotFoundException {
        PrintStream printStream2 = new PrintStream("midcode.txt");
        for (MidCodeItem item: midCodeItems) {
            printStream2.println(item.toString());
        }
    }
}
