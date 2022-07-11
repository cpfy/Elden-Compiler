package AST;

import midCode.MidCodeType;

import java.util.ArrayList;

public class StmtPrintf extends Stmt {
    private String formatString;
    private ArrayList<Exp> exps = new ArrayList<>();


    public StmtPrintf(String formatString, ArrayList<Exp> exps) {
        this.formatString = formatString;
        this.exps = exps;
    }

    @Override
    public void addMidCode() {
        String s = formatString.replace("\"", "");
        int i = 0;
        while (s.contains("%d")) {
            String[] list = s.split("%d", 2);
            s = list[1];
            if (list[0].length() > 0) {
                addString(list[0]);
                midCodeList.addMidCodeItem(MidCodeType.PRINT, null, null, "\"" + list[0] + "\"");
            }
            exps.get(i).addMidCode();
            midCodeList.addMidCodeItem(MidCodeType.PRINT, null, null, exps.get(i).getTemp());
            i++;
        }
        if (s.length() > 0) {
            addString(s);
            midCodeList.addMidCodeItem(MidCodeType.PRINT, null, null, "\"" + s + "\"");
        }
    }
}
