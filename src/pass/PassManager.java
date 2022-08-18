package pass;

import llvm.Function;
import pass.constPass.ConstProp;
import pass.mem2reg.*;
import pass.uselessBlockDelete.CondSimplify;
import pass.uselessBlockDelete.UselessBlockDel;

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
//            i++;
//            if (i == 2) {
//                continue;
//            }
            new CondSimplify(function);           //条件表达式简化
            new UselessBlockDel(function);        //空基本块删除

            new DataFlowGraph(function);          //构建基本块前驱后继
            new DominatorTree(function);          //计算支配节点树
            new DominatorFrontier(function);      //计算支配前驱
            new InsertPhi(function);              //插入Phi函数
            new Rename(function);                 //变量重命名
            new SinglePhiDel(function);           //删除参数列表只有一个元素的phi函数，并将变量传播
            new ConstProp(function);              //常数折叠、局部公共子表达式删除、死代码删除
//            new UselessBlockDel(function);        //空基本块删除


        }
    }
}
