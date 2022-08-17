package pass.uselessBlockDelete;

import llvm.Block;
import llvm.Function;
import llvm.Instr.*;

import java.util.ArrayList;
import java.util.HashMap;

public class CondSimplify {
    private Function function;

    public CondSimplify(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        for (Block block : function.getBlocklist()) {
            simplify(block);
        }
    }

    private void simplify(Block block) {
        ArrayList<Instr> instrs = block.getInblocklist();
        if (!(instrs.get(instrs.size() - 1) instanceof CondBrTerm)) {
            return;
        }
        if (instrs.size() <= 3) {
            return;
        }
        if (!(instrs.get(instrs.size() - 3) instanceof AssignInstr)) {
            return;
        }
        AssignInstr assignInstr = (AssignInstr) instrs.get(instrs.size() - 3);
        if (!(assignInstr.getValueinstr() instanceof ZExtInst)) {
            return;
        }
        CondBrTerm condBrTerm = (CondBrTerm) instrs.get(instrs.size() - 1);
        ZExtInst zExtInst = (ZExtInst) assignInstr.getValueinstr();
        condBrTerm.setV(zExtInst.getV());
        instrs.remove(instrs.size() - 2);
        instrs.remove(instrs.size() - 2);
    }
}
