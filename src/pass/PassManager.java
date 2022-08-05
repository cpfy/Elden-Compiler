package pass;

import llvm.Function;
import pass.constPass.ConstProp;
import pass.mem2reg.*;

import javax.xml.crypto.Data;

public class PassManager {

    public PassManager(Function function) {
//        new DataFlowGraph(function);
//        new DominatorTree(function);
//        new DominatorFrontier(function);
//        new InsertPhi(function);
//        new Rename(function);
        new ConstProp(function);
    }
}
