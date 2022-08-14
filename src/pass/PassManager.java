package pass;

import llvm.Function;
import pass.constPass.ConstProp;
import pass.mem2reg.*;

import javax.xml.crypto.Data;
import java.util.ArrayList;

public class PassManager {

    public PassManager(ArrayList<Function> functions) {
        int i = 0;
        for (Function function: functions) {
            if (i == 0) {
                i++;
                continue;
            }
            new DataFlowGraph(function);
            new DominatorTree(function);
            new DominatorFrontier(function);
            new InsertPhi(function);
            new Rename(function);
//            new ConstProp(function);
        }
    }
}
