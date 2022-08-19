package pass.mem2reg;

import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.AssignInstr;
import llvm.Instr.BinaryInst;
import llvm.Instr.Phi;
import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

public class removePhi {
    Function function;
    HashSet<Block> haveworked = new HashSet<Block>();
    Boolean reservePhi = false;
    int version = 0;

    public removePhi(Function function, Boolean reservePhi) {
        this.function = function;
        this.reservePhi = reservePhi;
        execute();
    }

    public void execute() {
        Stack<Block> worklist = new Stack<>();
        worklist.push(function.getBlocklist().get(0));
        while (!worklist.isEmpty()) {
            Block block = worklist.pop();
            haveworked.add(block);
            deletePhi(block);
            for (Block i : block.getSucBlocks()) {
                if (!haveworked.contains(i)) {
                    worklist.push(i);
                }
            }
        }
    }

    public void deletePhi(Block block) {
        for (Phi phi : block.getPhis()) {
            Boolean isCopied = false;
            for (Block i : phi.getParams().keySet()) {
                if (!phi.getValue().equals(phi.getParams().get(i))) {
                    isCopied = true;
                    if (!reservePhi) addnewInstr(i, phi.getParams().get(i), phi.getValue(), phi.getType());
                    else {
                        addnewInstr(i, phi.getParams().get(i), new Value(new Ident("temp" + version)), phi.getType());
                    }
                }
            }
            if (reservePhi && isCopied) {
                copyPhi(block, new Value(new Ident("temp" + version)), phi.getValue(), phi.getType());
                version++;
            }
        }
        block.setPhis(new ArrayList<>());
    }

    public void addnewInstr(Block block, Value oldvalue, Value newvalue, Type type) {
        BinaryInst binaryInst = new BinaryInst("binary", "add", type, new Value(0), oldvalue);
        AssignInstr assignInstr = new AssignInstr("assign", newvalue.getIdent(), binaryInst);
        int size = block.getInblocklist().size();
        block.getInblocklist().add(size - 1, assignInstr);
    }

    public void copyPhi(Block block, Value oldvalue, Value newvalue, Type type) {
        BinaryInst binaryInst = new BinaryInst("binary", "add", type, new Value(0), oldvalue);
        AssignInstr assignInstr = new AssignInstr("assign", newvalue.getIdent(), binaryInst);
        int size = block.getInblocklist().size();
        block.getInblocklist().add(0, assignInstr);
    }
}
