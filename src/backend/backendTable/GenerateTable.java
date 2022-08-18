package backend.backendTable;

import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.AllocaInst;
import llvm.Instr.AssignInstr;
import llvm.Instr.Instr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GenerateTable {

    private Function function;
    private ArrayList<Instr> instrs = new ArrayList<>();
    private HashSet<String> varNames = new HashSet<>();

    public GenerateTable(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        initList();
        int n = 0;
        for (Ident ident: function.getFuncheader().getParas()) {
            if (varNames.contains(ident.toString())) {
                continue;
            }
            n = 4;
            varNames.add(ident.toString());
            function.addVar(ident.toString(), n);
        }

        for (Instr instr: instrs) {
            n = 0;
            if (instr instanceof AssignInstr) {
                n += 4;
                AssignInstr assignInstr = (AssignInstr) instr;
                if (varNames.contains(assignInstr.getIdent().toString())) {
                    continue;
                }
                varNames.add(assignInstr.getIdent().toString());
                if (assignInstr.getValueinstr() instanceof AllocaInst) {
                    AllocaInst allocaInst = (AllocaInst) assignInstr.getValueinstr();
                    n += allocaInst.getType().getSpace();
                }
//                System.out.println("name: " + assignInstr.getIdent().toString() + " " + n);
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
