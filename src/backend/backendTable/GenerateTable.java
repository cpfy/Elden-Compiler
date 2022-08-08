package backend.backendTable;

import llvm.Block;
import llvm.Function;
import llvm.Instr.AllocaInst;
import llvm.Instr.AssignInstr;
import llvm.Instr.Instr;

import java.util.ArrayList;

public class GenerateTable {

    private Function function;
    private ArrayList<Instr> instrs = new ArrayList<>();

    public GenerateTable(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        initList();

        for (Instr instr: instrs) {
            int n = 0;
            if (instr instanceof AssignInstr) {
                n += 4;
                AssignInstr assignInstr = (AssignInstr) instr;
                if (assignInstr.getValueinstr() instanceof AllocaInst) {
                    AllocaInst allocaInst = (AllocaInst) assignInstr.getValueinstr();
                    n += allocaInst.getType().getSpace();
                }
                function.addVar(assignInstr.getIdent().toString(), n);
            }
        }
    }

    private void initList() {
        for (Block block: function.getBlocklist()) {
            instrs.addAll(block.getPhis());
            instrs.addAll(block.getInblocklist());
        }
    }
}
