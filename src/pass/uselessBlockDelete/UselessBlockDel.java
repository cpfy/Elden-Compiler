package pass.uselessBlockDelete;

import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.BrTerm;
import llvm.Instr.CondBrTerm;
import llvm.Instr.Instr;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class UselessBlockDel {
    private Function function;


    private boolean changed;
    private ArrayList<Instr> brList = new ArrayList<>();
    private HashMap<Instr, Block> instrBlockHashMap = new HashMap<>();

    public UselessBlockDel(Function function) {
        this.function = function;
        changed = true;
        while (changed) {
            changed = false;
            execute();
        }
    }

    private void execute() {
        brList = new ArrayList<>();
        for (Block block: function.getBlocklist()) {
            block.setDead(false);
            Instr instr = block.getInblocklist().get(block.getInblocklist().size() - 1);
            if (instr instanceof CondBrTerm || instr instanceof BrTerm) {
                brList.add(instr);
                instrBlockHashMap.put(instr, block);
            }
        }

        for (Instr instr: brList) {
            Block block = instrBlockHashMap.get(instr);
            if (block.getInblocklist().size() == 1 && block.getPhis().size() == 0 && instr instanceof BrTerm) {
                changed = true;
                BrTerm brTerm = (BrTerm) instr;
                block.setDead(true);
                rename(new Value(new Ident(String.valueOf(brTerm.getLi().getId()))), new Value(new Ident(block.getLabel())));
            }
        }

        for (Block block: function.getBlocklist()) {
            Instr instr = block.getInblocklist().get(block.getInblocklist().size() - 1);
            if (instr instanceof CondBrTerm) {
                CondBrTerm condBrTerm = (CondBrTerm) instr;
                if (condBrTerm.getI1().equals(condBrTerm.getI2())) {
                    block.getInblocklist().set(block.getInblocklist().size() - 1, new BrTerm("br", condBrTerm.getI1()));
                }
            }
        }

        deleteDeadBlock();
    }

    private void rename(Value newValue, Value oldValue) {
        for (Instr instr: brList) {
            instr.renameUses(newValue, oldValue);
        }
    }

    private void deleteDeadBlock() {
        ArrayList<Block> newBlocks = new ArrayList<>();
        for (Block block: function.getBlocklist()) {
            if (!block.isDead()) {
                newBlocks.add(block);
            }
        }
        function.setBlocklist(newBlocks);
    }
}
