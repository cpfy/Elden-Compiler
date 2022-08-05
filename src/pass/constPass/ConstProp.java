package pass.constPass;

import llvm.Block;
import llvm.Function;
import llvm.Instr.*;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/*** 常量传播、常量折叠 ***/
public class ConstProp {
    private Function function;
    private LinkedList<Instr> instrs = new LinkedList<>();

    private HashMap<String, InstrUses> varName2InstrUses = new HashMap<>();
    private HashMap<String, InstrUses> roots = new HashMap<>();

    public ConstProp(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        initList();     //获取函数中的指令
        constProp();    //常量折叠

        initList();
        deadCodeKill(); //死代码删除
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

    private void deadCodeKill() {
        for (Instr instr: instrs) {
            if (!(instr instanceof StoreInstr || instr instanceof BrTerm
                    || instr instanceof CondBrTerm || instr instanceof CallInst
                    || instr instanceof RetTerm)) {
                instr.setCanDelete(true);
            }

            String def = instr.getDef();
            ArrayList<String> uses = instr.getUses();
            ArrayList<InstrUses> instrUsesArrayList = new ArrayList<>();
            for (String s: uses) {
                instrUsesArrayList.add(varName2InstrUses.get(s));
            }

            if (def != null) {
                InstrUses instrUses = new InstrUses(instr);
                varName2InstrUses.put(def, instrUses);
                instrUses.addUses(instrUsesArrayList);
            }

            for (String s: instr.getRoots()) {
                roots.put(s, varName2InstrUses.get(s));
            }
        }

        for (InstrUses instrUses: roots.values()) {
            instrUses.dfs();
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
