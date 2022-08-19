package backend.backendTable;

import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.AllocaInst;
import llvm.Instr.AssignInstr;
import llvm.Instr.Instr;

import java.util.ArrayList;
import java.util.HashSet;


//用于给指令编号
public class NumberInstr {
    private Function function;
    private HashSet<Block> walked = new HashSet<>();
    private int num = 0;


    public NumberInstr(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        postOrderWalk(function.getBlocklist().get(0));
    }

    private void postOrderWalk(Block block) {
        walked.add(block);
        for (Instr instr: block.getInblocklist()) {
            instr.setInstrNo(num);
            num += 4;
        }
        for (Block suc : block.getSucBlocks()) {
            if (!walked.contains(suc)) {
                postOrderWalk(suc);
            }
        }
    }

}
