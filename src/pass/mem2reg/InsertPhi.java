package pass.mem2reg;


import llvm.Instr.Phi;
import llvm.Block;
import llvm.Function;
import llvm.Instr.AllocaInst;
import llvm.Instr.AssignInstr;
import llvm.Instr.Instr;
import llvm.Instr.StoreInstr;
import llvm.Type.IntType;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class InsertPhi {
    private Function function;
    private HashMap<Block, HashSet<Value>> def = new HashMap<>();
    public InsertPhi(Function function){
        this.function = function;
        execute();
    }
    public void execute(){
        ArrayList<Value> vars = new ArrayList<>();
        vars = getVariables(function); //遍历这个函数内的所有变量
        caldef(); //计算哪些alloca变量曾被store赋过值。
        for(Value i : vars){
            process(i);
        }
    }
    public void process(Value var){
        HashMap<Block,Boolean> hasPhi = new HashMap<>();
        HashMap<Block,Boolean> processed = new HashMap<>();
        Stack<Block> workList = new Stack<>();
        for(Block i:function.getBlocklist()){
            hasPhi.put(i,false);
            processed.put(i,false);
        }
        for(Block i:function.getBlocklist()){
            Boolean cond1 = true;
            cond1 = def.get(i).contains(var); //判断这个基本块是否给这个变量赋过值。
            if(cond1){
                processed.put(i,true);
                workList.push(i);
            }
        }
        while(!workList.isEmpty()){
            Block i = workList.pop();
            for(Block j:i.getDominatorFrontiers()){
                if(!hasPhi.get(j)){
                    Phi phi = new Phi("phi",var,j.getPreBlocks());
                    j.addPhi(phi);
                    hasPhi.put(j,true);
                    if(!processed.get(j)){
                        processed.put(j,true);
                        workList.push(j);
                    }
                }
            }
        }
    }

    public boolean hasAssigned(Block i,Value var){
        return def.get(i).contains(var);
    }

    public void caldef(){
        for(Block i:function.getBlocklist()){
            for(Instr j:i.getInblocklist()){
                if(j instanceof StoreInstr){
                    StoreInstr store = (StoreInstr) j;
                    def.get(i).add(store.getV2());
                }
            }
        }
    }
    public ArrayList<Value> getVariables(Function f){
        ArrayList<Value> ans = new ArrayList<>();
        for(Block i:f.getBlocklist()){
            for(Instr j:i.getInblocklist()){
                if(j instanceof AssignInstr){
                    AssignInstr assign = (AssignInstr) j;
                    if(assign.getValueinstr() instanceof AllocaInst){
                        AllocaInst alloca = (AllocaInst) assign.getValueinstr();
                        if(alloca.getType() instanceof IntType){
                            ans.add(new Value(assign.getIdent()));
                        }
                    }
                }
            }
        }
        return ans;
    }
}
