package pass.mem2reg;

import backend.Phi;
import backend.Variable;
import llvm.*;
import llvm.Instr.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Rename {
    private Function function;
    private HashMap<Value,HashMap<Block,Value>> valueList = new HashMap();
    private Stack<Block> dist = new Stack<>();
    HashSet<Block> haveworked = new HashSet();
    HashSet<Block> havePhi = new HashSet<>();
    Block start = new Block();
    private int version;
    private int label;
    public Rename(Function function){
        this.function = function;
        version = 1;
        label = 1;
        //start = function.getStart();
        dfs(start);
        renamePhi(start);
    }

    public void execute(){
        /*
        Stack<Block> workList = new Stack<>();
        workList.push(start);
        dist.push(start);
        while(!workList.isEmpty()){
            Boolean hasNext = false;
            Block working = workList.pop();
            haveworked.add(working);
            process(working);
            for(Block i :working.getSucBlocks()){
                if(!haveworked.contains(i)) {
                    hasNext = true;
                    workList.push(i);
                }
            }
            if(!hasNext){
                dist.pop();
            }
         */
    }

    public void dfs(Block block){
        Block pre = null;
        if(!dist.isEmpty()){
            pre = dist.peek();
        }
        //processPhi();
        if(pre!=null){
            for(Value i:valueList.keySet()){
                valueList.get(i).put(block,valueList.get(pre).get(i));
            }
        }
        dist.push(block);
        haveworked.add(block);
        for(Phi phi:block.getPhis()){
            phi.setValue(new Value(new Ident(version)));
            version++;
        }
        for(Instr i : block.getInblocklist()){
            switch(i.getInstrname()){
                case "add":
                case "fadd":
                case "sub":
                case "fsub":
                case "mul":
                case "fmul":
                case "sdiv":
                case "fdiv":
                    BinaryInst ins = (BinaryInst) i;
                    Value v1 = ins.getV1();
                    Value v2 = ins.getV2();
                    if(valueList.containsKey(v1)){
                        ins.setV1(valueList.get(v1).get(block));
                    }
                    if(valueList.containsKey(v2)){
                        ins.setV2(valueList.get(v2).get(block));
                    }
                    break;
                case "zext":
                    ZExtInst ins1 = (ZExtInst) i;
                    if(valueList.containsKey(ins1.getV())){
                        ins1.setV(valueList.get(ins1.getV()).get(block));
                    }
                    break;
                case "alloca":
                    break;
                case "assign":
                    AssignInstr ins2 = (AssignInstr) i;
                    ins2.getIdent().setId(version);
                    Value value = new Value(new Ident(version));
                    HashMap<Block,Value> newHash = new HashMap<>();
                    newHash.put(block,value);
                    valueList.put(value,newHash);
                    version++;
                    break;
                case "load":
                    LoadInst ins3 = (LoadInst) i;
                    if(valueList.containsKey(ins3.getV())){
                        Value dest = null;
                        Ident ident = getAssignDest(block,ins3);
                        if(ident!=null) dest = new Value(ident);
                        if(dest!=null) {
                            valueList.get(dest).put(block,ins3.getV());
                        }
                    }
                    break;
                case "store":
                    StoreInstr ins4 = (StoreInstr) i;
                    HashMap<Block,Value> hash = valueList.get(ins4.getV1());
                    hash.put(block,valueList.get(ins4.getV2()).get(block));
                    valueList.put(ins4.getV1(),hash);
                    break;
                case "call":
                    CallInst ins5 = (CallInst) i;
                    for(TypeValue typevalue:ins5.getArgs()){
                        if(valueList.containsKey(typevalue.getValue())){
                            typevalue.setValue(valueList.get(typevalue.getValue()).get(block));
                        }
                    }
                    break;
                //似乎不应当有
                case "getelementptr":
                    GetElementPtrInst ins6 = (GetElementPtrInst) i;
                    if(valueList.containsKey(ins6.getV())){
                        ins6.setValue(valueList.get(ins6.getV()).get(block));
                    }
                    break;
                case "ret":
                    RetTerm ins7 = (RetTerm) i;
                    if(valueList.containsKey(ins7.getV())){
                        ins7.setV(valueList.get(ins7.getV()).get(block));
                    }
                    break;
                case "icmp":
                    IcmpInst ins8 = (IcmpInst) i;
                    Value v3 = ins8.getV1();
                    Value v4 = ins8.getV2();
                    if(valueList.containsKey(v3)){
                        ins8.setV1(valueList.get(v3).get(block));
                    }
                    if(valueList.containsKey(v4)){
                        ins8.setV2(valueList.get(v4).get(block));
                    }
                    break;
                case "br":
                    BrTerm ins9 = (BrTerm) i;
                    renameLabel(ins9.getLabelName(),String.valueOf(version));
                    ins9.setLi(new Ident(version));
                    version++;
                    break;
                case "condbr":
                    CondBrTerm ins10 = (CondBrTerm) i;
                    renameLabel(ins10.getLabel1Name(),String.valueOf(version));
                    ins10.setL1(new Ident(version));
                    version++;
                    renameLabel(ins10.getLabel1Name(),String.valueOf(version));
                    ins10.setL2(new Ident(version));
                    version++;
                    if(valueList.containsKey(ins10.getV())){
                        ins10.setV(valueList.get(ins10.getV()).get(block));
                    }
                    break;
            }
        }
        for(Block i:block.getSucBlocks()){
            if(!haveworked.contains(i)){
                dfs(i);
            }
        }
        dist.pop();
    }

    public void process(Block block){
        int version = 1;
        Value nowvalue;
    }

    public Ident getAssignDest(Block i,Instr ins){
        for(Instr j:i.getInblocklist()){
            if(j instanceof AssignInstr){
                if(((AssignInstr) j).getValueinstr().equals(ins)){
                    return ((AssignInstr) j).getIdent();
                }
            }
        }
        return null;
    }

    public void renameLabel(String name,String newname){
        for(Block i:function.getBlocklist()){
            if(i.getLabel().equals(name)){
                i.setLabel(newname);
            }
        }
    }

    public void renamePhi (Block block){
        havePhi.add(block);
        for(Phi i:block.getPhis()){
            for(Block j:i.getParams().keySet()){
                i.reName(valueList.get(i.getValue()).get(block),j);
            }
        }
        for(Block j:block.getSucBlocks()){
            if(!havePhi.contains(j)){
                renamePhi(j);
            }
        }
    }
}