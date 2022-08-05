package pass.constPass;

import llvm.Function;
import llvm.Instr.Instr;

import java.util.LinkedList;

/*** 常量传播、常量折叠 ***/
public class ConstProp {
    private Function function;
    private LinkedList<Instr> instrs = new LinkedList<>();


    public ConstProp(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        boolean changed = true;
        initList();
        while (changed) {
            //todo
        }
    }

    private void initList() {
        //todo 将该函数的所有指令加入instrs中
    }
}
