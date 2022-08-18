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
    public removePhi(Function function){
        this.function = function;
        execute();
    }
    public void execute (){
        Stack<Block> worklist = new Stack<>();
        worklist.push(function.getBlocklist().get(0));
        while(!worklist.isEmpty()){
            Block block = worklist.pop();
            haveworked.add(block);
            deletePhi(block);
            for(Block i:block.getSucBlocks()){
                if(!haveworked.contains(i)){
                    worklist.push(i);
                }
            }
        }
    }
    public void deletePhi(Block block){
        for(Phi phi:block.getPhis()){
            for(Block i:phi.getParams().keySet()){
                if(!phi.getValue().equals(phi.getParams().get(i))) {
                    addnewInstr(i, phi.getParams().get(i), phi.getValue(), phi.getType());
                }
            }
        }
        block.setPhis(new ArrayList<>());
    }
    public void addnewInstr(Block block, Value oldvalue, Value newvalue, Type type){
        BinaryInst binaryInst = new BinaryInst("binary","add",type,new Value(0),oldvalue);
        AssignInstr assignInstr = new AssignInstr("assign", newvalue.getIdent(), binaryInst);
        int size = block.getInblocklist().size();
        block.getInblocklist().add(size-1,assignInstr);
    }
}
