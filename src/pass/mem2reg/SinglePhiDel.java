package pass.mem2reg;

import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.*;
import llvm.Value;
import pass.constPass.CSE;
import pass.constPass.InstrUses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class SinglePhiDel {
    private Function function;
    private LinkedList<Instr> instrs = new LinkedList<>();
    private LinkedList<Phi> phis = new LinkedList<>();

    public SinglePhiDel(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        initList();     //获取函数中的指令
        initPhiList();
        phiSimplify();
        phiDelete();
    }


    private void phiSimplify() {
        for (Phi phi : phis) {
            if (phi.getParams().size() == 1) {
                phi.setCanDelete(true);
                for (Value value : phi.getParams().values()) {
                    rename(value, phi.getValue());
                }
            }
        }
    }


    private void phiDelete() {
        for (Block block : function.getBlocklist()) {
            ArrayList<Phi> newPhis = new ArrayList<>();
            for (Phi phi : block.getPhis()) {
                if (!phi.isCanDelete()) {
                    newPhis.add(phi);
                }
            }
            block.setPhis(newPhis);
        }
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

    private void initPhiList() {
        for (Block block : function.getBlocklist()) {
            phis.addAll(block.getPhis());
        }
    }

}
