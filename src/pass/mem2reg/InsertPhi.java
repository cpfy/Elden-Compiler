package pass.mem2reg;


import llvm.Ident;
import llvm.Instr.Phi;
import llvm.Block;
import llvm.Function;
import llvm.Instr.AllocaInst;
import llvm.Instr.AssignInstr;
import llvm.Instr.Instr;
import llvm.Instr.StoreInstr;
import llvm.Type.FloatType;
import llvm.Type.IntType;
import llvm.Type.Type;
import llvm.Type.TypeC;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class InsertPhi {
    private Function function;
    private HashMap<Block, HashSet<Value>> def = new HashMap<>();
    private HashMap<Block, HashSet<Value>> defined = new HashMap<>(); //每个基本块存在的所有定义的value
    private HashSet<Block> haveworked = new HashSet<>();
    private HashSet<Block> worked = new HashSet<>();
    private HashSet<Value> isfloat = new HashSet<>();
    private Stack<Block> dist = new Stack<>();
    public InsertPhi(Function function){
        this.function = function;
        System.out.println("insertphi start");
        execute();
    }
    public void execute(){
        ArrayList<Value> vars = new ArrayList<>();
        vars = getVariables(function); //遍历这个函数内的所有变量
        caldef(); //计算哪些alloca变量曾被store赋过值。
        System.out.println("caldefined start");
        calDefined(function.getBlocklist().get(0)); //计算到达-定义链;
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
                    int source = 0;
                    for(Block block:j.getPreBlocks()){
                        worked = new HashSet<>();
                        if(hasDefined(var,block)) source++;
                    }
                    if(source>1) {

                        Type type;
                        if(isfloat.contains(var)){
                            type = new FloatType(TypeC.F);
                        }
                        else{
                            type = new IntType(TypeC.I,32);
                        }
                        Phi phi = new Phi("phi", var, j.getPreBlocks(),type);
                        System.out.println("insert new phi in block " + j.getLabel() + ":" + phi.toString());
                        j.addPhi(phi);
                    }
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
            def.put(i,new HashSet<>());
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
                        if(alloca.getType() instanceof IntType ){
                            ans.add(new Value(new Ident(assign.getIdent().getName())));
                        }
                        else if(alloca.getType() instanceof FloatType){
                            ans.add(new Value(new Ident(assign.getIdent().getName())));
                            isfloat.add(new Value(new Ident(assign.getIdent().getName())));
                        }
                    }
                }
            }
        }
        return ans;
    }

    public void calDefined(Block block){
        Block pre = null;
        if(!dist.isEmpty()){
            pre = dist.peek();
        }

        //processPhi();
        //前导基本块流入数据
        System.out.println("label:" + block.getLabel());
        if(!defined.containsKey(block)){
            HashSet<Value> hashmap = new HashSet<>();
            defined.put(block,hashmap);
        }
        if(pre!=null){
            System.out.println("prelabel:"+pre.getLabel());
            for(Value i: defined.get(pre)){
                defined.get(block).add(i);
            }
        }
        dist.push(block);
        haveworked.add(block);
        for(Instr ins:block.getInblocklist()){
            System.out.println(ins);
            /*
            if(ins instanceof StoreInstr){
                //System.out.println("working:" + ins);
                StoreInstr store = (StoreInstr) ins;
                //System.out.println("add " + store.getV2() + " in " + block.getLabel());
                def.get(block).add(store.getV2());
            }
            */
            if(ins instanceof AssignInstr){
                AssignInstr assignInstr = (AssignInstr) ins;
                if(assignInstr.getValueinstr() instanceof AllocaInst) {
                    AllocaInst alloca = (AllocaInst) assignInstr.getValueinstr();
                    if (alloca.getType() instanceof IntType || alloca.getType() instanceof FloatType) {
                        //System.out.println("add " + assignInstr.getIdent() + " in " + block.getLabel());
                        defined.get(block).add(new Value(new Ident(assignInstr.getIdent().getName())));
                    }
                }
            }

        }
        for(Block i:block.getSucBlocks()){
            if(!haveworked.contains(i)){
                calDefined(i);
            }
        }
        dist.pop();
    }

    public Boolean hasDefined(Value value,Block block){
        if(defined.get(block).contains(value)){
            //System.out.println("find " + value + " in " + block.getLabel());
        }
        else{
            //System.out.println("cannot find " + value + " in " + block.getLabel());

        }
        return defined.get(block).contains(value);
        /*
        worked.add(block);
        if(hasAssigned(block,value)){
            System.out.println(block.getLabel() +"has" + value);
            return true;
        }
        else {
            Boolean hasDefined = false;
            for (Block i : block.getPreBlocks()) {
                if (!worked.contains(i)) {
                    System.out.println("calling from " + block.getLabel() + " to " + i.getLabel());
                    hasDefined |= hasDefined(value, i);
                }
            }
            return hasDefined;
        }
        */
    }
}
