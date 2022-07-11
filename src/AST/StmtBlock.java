package AST;

import midCode.MidCodeType;

import java.util.ArrayList;

public class StmtBlock extends Stmt {
    private ArrayList<BlockItem> blockItems;

    public StmtBlock(ArrayList<BlockItem> blockItems) {
        this.blockItems = blockItems;
    }

    @Override
    public void addMidCode() {
        int l = newLable();
        midCodeList.addMidCodeItem(MidCodeType.LABEL, String.valueOf(l), "in", null);
        table.newBlock();
        for (BlockItem blockItem: blockItems) {
            blockItem.addMidCode();
        }
        table.deleteBlock();
        midCodeList.addMidCodeItem(MidCodeType.LABEL, String.valueOf(l), "out", null);
    }
}
