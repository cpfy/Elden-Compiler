package pass.constPass;

import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.*;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/*** 常量传播、常量折叠、死代码删除 ***/
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

        condBrMerge();

        initList();
        deadCodeKill(); //死代码删除

//        for (Block block: function.getBlocklist()) {
//            new CSE(block); //公共子表达式删除.2963
//
//        }

        initList();
        deadCodeKill(); //死代码删除
    }

    private void condBrMerge() {
        for (Block block : function.getBlocklist()) {
            ArrayList<Instr> instrs = block.getInblocklist();
            Instr instr = instrs.get(instrs.size() - 1);
            if (instr instanceof CondBrTerm) {
                CondBrTerm condBrTerm = (CondBrTerm) instr;
                if (!condBrTerm.getV().isIdent()) {
                    if (condBrTerm.getV().getVal() == 1) {
                        instrs.set(instrs.size() - 1, new BrTerm("br", condBrTerm.getI1()));
                    } else if (condBrTerm.getV().getVal() == 0) {
                        instrs.set(instrs.size() - 1, new BrTerm("br", condBrTerm.getI2()));
                    }
                }
            }
        }
    }

    private void constProp() {
        boolean changed = true;
        while (changed) {
            changed = false;

            for (Instr instr : instrs) {
                if (instr.isCanDelete()) {
                    continue;
                }
                if (instr instanceof AssignInstr) {
                    AssignInstr assignInstr = (AssignInstr) instr;
                    Value newValue = assignInstr.mergeConst();
                    if (newValue != null) {
                        assignInstr.setCanDelete(true);
                        rename(newValue, new Value(new Ident(assignInstr.getIdent().getName())));
                        changed = true;
                    }
                }
            }
        }

        function.clear();
    }

    private void deadCodeKill() {
        for (Ident ident : function.getFuncheader().getParas()) {
            InstrUses instrUses = new InstrUses(null);
            varName2InstrUses.put(ident.toString(), instrUses);
        }
        for (Instr instr : instrs) {
            if (!(instr instanceof StoreInstr || instr instanceof BrTerm
                    || instr instanceof CondBrTerm || instr instanceof CallInst
                    || instr instanceof RetTerm)) {
                instr.setCanDelete(true);
            }

            String def = instr.getDef();
            if (def != null) {
                InstrUses instrUses = new InstrUses(instr);
                varName2InstrUses.put(def, instrUses);
            }
        }


        for (Instr instr : instrs) {
            if (!(instr instanceof StoreInstr || instr instanceof BrTerm
                    || instr instanceof CondBrTerm || instr instanceof CallInst
                    || instr instanceof RetTerm)) {
                instr.setCanDelete(true);
            }

            String def = instr.getDef();
            ArrayList<String> uses = instr.getUses();
            ArrayList<InstrUses> instrUsesArrayList = new ArrayList<>();
            for (String s : uses) {
                if (s.charAt(0) != '%') {
                    continue;
                }
                if (varName2InstrUses.get(s) == null) {
                    System.err.println("\t" + s);
                }
                if (def != null) {
//                    System.out.println(def + "\tuses\t" + s);
                }
                instrUsesArrayList.add(varName2InstrUses.get(s));
            }

            if (def != null) {
                InstrUses instrUses = varName2InstrUses.get(def);
                instrUses.addUses(instrUsesArrayList);
            }

            for (String s : instr.getRoots()) {
                if (s.charAt(0) != '%') {
                    continue;
                }
//                System.out.println("root " + s);
                roots.put(s, varName2InstrUses.get(s));
            }
        }
        for (InstrUses instrUses : roots.values()) {
            instrUses.dfs();
        }

        function.clear();
    }

    private void rename(Value newValue, Value oldValue) {
        for (Instr instr : instrs) {
            if (instr.isCanDelete()) {
                continue;
            }
            instr.renameUses(newValue, oldValue);
        }
    }

    private void initList() {
        for (Block block : function.getBlocklist()) {
            instrs.addAll(block.getPhis());
            instrs.addAll(block.getInblocklist());
        }
    }
}
