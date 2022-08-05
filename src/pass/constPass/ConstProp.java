package pass.constPass;

import llvm.Block;
import llvm.Function;
import llvm.Instr.AssignInstr;
import llvm.Instr.Instr;
import llvm.Instr.Phi;
import llvm.Value;

import java.util.LinkedList;

/*** 常量传播、常量折叠 ***/
public class ConstProp {
    private Function function;
    private LinkedList<Instr> instrs = new LinkedList<>();


    public ConstProp(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        initList();
        constProp();
    }

    private void constProp() {
        boolean changed = true;
        while (changed) {
            changed = false;

            for (Instr instr: instrs) {
                if (instr.isCanDelete()) {
                    continue;
                }
                if (instr instanceof AssignInstr) {
                    AssignInstr assignInstr = (AssignInstr) instr;
                    Value newValue = assignInstr.mergeConst();
                    if (newValue != null) {
                        assignInstr.setCanDelete(true);
                        rename(newValue, new Value(assignInstr.getIdent()));
                        changed = true;
                    }
                }
            }
        }

        function.clear();
    }

    private void rename(Value newValue, Value oldValue) {
        for (Instr instr: instrs) {
            if (instr.isCanDelete()) {
                continue;
            }
            instr.renameUses(newValue, oldValue);
        }
    }

    private void initList() {
        for (Block block: function.getBlocklist()) {
            instrs.addAll(block.getPhis());
            instrs.addAll(block.getInblocklist());
        }
    }
}
