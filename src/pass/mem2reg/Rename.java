package pass.mem2reg;

import llvm.Instr.Phi;
import llvm.*;
import llvm.Instr.*;
import llvm.Type.*;

import java.io.PipedOutputStream;
import java.util.*;

public class Rename {
    private Function function;
    private HashMap<Value,HashMap<Block,Value>> valueList = new HashMap();
    private Stack<Block> dist = new Stack<>();
    private HashMap<String,String> renamedLabel = new HashMap<>();
    private HashSet<Ident> isPointer = new HashSet<>();
    HashSet<Block> haveworked = new HashSet();
    HashSet<Block> havePhi = new HashSet<>();
    Block start = new Block();
    private int version;
    private int label;
    public Rename(Function function){
        this.function = function;
        version = function.getFuncheader().getParas().size();
        label = 1;
        start = function.getBlocklist().get(0);
        System.out.println("rename start");
        dfs(start);
        renamePhi(start);
        deleteInstr();
    }

    public void execute(){ //废弃
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
        //前导基本块流入数据
        System.out.println("label:" + block.getLabel());
        if(pre!=null){
            System.out.println("prelabel:"+pre.getLabel());
            for(Value i:valueList.keySet()){
                if(valueList.get(i).containsKey(pre)) {
                    valueList.get(i).put(block, valueList.get(i).get(pre));
                }
            }
        }
        dist.push(block);
        haveworked.add(block);
        for(Phi phi:block.getPhis()){
            System.out.println(phi.toString());
            //System.out.println("________"+phi.getValue());
            System.out.println(phi.getValue());
            if(valueList.containsKey(phi.getValue())) {
                valueList.get(phi.getValue()).put(block, new Value(new Ident("p" + version)));
            }
            else{
                HashMap<Block,Value> hashMap = new HashMap<>();
                hashMap.put(block,new Value(new Ident("p" + version)));
                valueList.put(phi.getValue(),hashMap);
            }
            phi.setValue(new Value(new Ident("p" + version)));
            version++;
        }
        for(Instr i : block.getInblocklist()){
            System.out.println("working:" + i.toString() + ",its name:" + i.getInstrname());
            switch(i.getInstrname()) {
                case "assign":
                    AssignInstr ins2 = (AssignInstr) i;
                    Instr j = ins2.getValueinstr();
                    Boolean renameNewIdent = true;
                    //System.out.println("an assign instr:"+j.getInstrname());
                    switch (j.getInstrname()) {
                        case "binary":
                            BinaryInst ins = (BinaryInst) j;
                            Value v1 = ins.getV1();
                            Value v2 = ins.getV2();
                            //System.out.println("a binary instr");
                            if (valueList.containsKey(v1)) {
                                System.out.println("change in binary1:" + valueList.get(v1).get(block).toString());
                                ins.setV1(valueList.get(v1).get(block));
                            }
                            if (valueList.containsKey(v2)) {
                                ins.setV2(valueList.get(v2).get(block));
                            }
                            break;
                        case "zext":
                            ZExtInst ins1 = (ZExtInst) j;
                            if (valueList.containsKey(ins1.getV())) {
                                ins1.setV(valueList.get(ins1.getV()).get(block));
                            }
                            break;
                        case "sitofp":
                            SIToFPInst si_ins = (SIToFPInst) j;
                            if(valueList.containsKey(si_ins.getV())){
                                si_ins.setV(valueList.get(si_ins.getV()).get(block));
                            }
                            break;
                        case "fptosi":
                            FPToSIInst fp_ins = (FPToSIInst) j;
                            if(valueList.containsKey(fp_ins.getV())){
                                fp_ins.setV(valueList.get(fp_ins.getV()).get(block));
                            }
                            break;
                        case "alloca":
                            AllocaInst alloc_ins = (AllocaInst) j;
                            if(alloc_ins.getType() instanceof FloatType || alloc_ins.getT() instanceof IntType) {
                                if(!ins2.getIdent().isGlobal()) i.setCanDelete(true);
                            }
                            else if(alloc_ins.getType() instanceof PointerType){
                                isPointer.add(new Ident(ins2.getIdent().getName()));
                            }
                            break;
                        case "load":
                            LoadInst ins3 = (LoadInst) j;
                            System.out.println("in load:" + ins3.getV().getIdent());
                            if(ins3.getT1() instanceof FloatType || ins3.getT1() instanceof IntType) {
                                if(!ins3.getV().getIdent().isGlobal() && !isPointer.contains(ins3.getV().getIdent())) i.setCanDelete(true);
                                else{System.out.println("cannot delete");}
                            }
                            Value dest = new Value(new Ident(ins2.getIdent().getName()));
                            //不为全局变量
                            if (valueList.containsKey(ins3.getV())) {
                                    if (!valueList.containsKey(dest)) {
                                        HashMap<Block, Value> hashmap = new HashMap<>();
                                        //判断value是否为pointer，如果是则不作操作。
                                        if(!isPointer.contains(ins3.getV().getIdent())) {
                                                //System.out.println("yes,it's:"+valueList.get(ins3.getV()).get(block));
                                                hashmap.put(block, valueList.get(ins3.getV()).get(block));
                                        }
                                        else{
                                            ins3.setV(valueList.get(ins3.getV()).get(block));
                                            hashmap.put(block,new Value(new Ident("t" + version)));
                                        }
                                        valueList.put(dest, hashmap);
                                    }
                                    //实际上按照程序逻辑不可能出现else的情况
                                    else {
                                        if(valueList.containsKey(ins3.getV())){
                                            valueList.get(dest).put(block,valueList.get(ins3.getV()).get(block));
                                        }
                                        else {
                                            valueList.get(dest).put(block, ins3.getV());
                                        }
                                    }
                            }
                            //全局变量
                            else{
                                HashMap<Block,Value> hashmap = new HashMap<>();
                                hashmap.put(block,new Value(new Ident("t"+version)));
                                valueList.put(dest,hashmap);
                            }
                            break;
                        case "call":
                            CallInst ins5 = (CallInst) j;
                            for (TypeValue typevalue : ins5.getArgs()) {
                                if (valueList.containsKey(typevalue.getValue())) {
                                    typevalue.setValue(valueList.get(typevalue.getValue()).get(block));
                                }
                            }
                            break;
                        case "icmp":
                            IcmpInst ins8 = (IcmpInst) j;
                            Value v3 = ins8.getV1();
                            Value v4 = ins8.getV2();
                            if (valueList.containsKey(v3)) {
                                ins8.setV1(valueList.get(v3).get(block));
                            }
                            if (valueList.containsKey(v4)) {
                                ins8.setV2(valueList.get(v4).get(block));
                            }
                            break;
                        case "fcmp":
                            FCmpInst ins9 = (FCmpInst) j;
                            Value v5 = ins9.getV1();
                            Value v6 = ins9.getV2();
                            if (valueList.containsKey(v5)) {
                                ins9.setV1(valueList.get(v5).get(block));
                            }
                            if (valueList.containsKey(v6)) {
                                ins9.setV2(valueList.get(v6).get(block));
                            }
                            break;
                        case "getelementptr":
                            GetElementPtrInst ins6 = (GetElementPtrInst) j;
                            //System.out.println("in getele:" + ins2.getIdent());
                            isPointer.add(new Ident(ins2.getIdent().getName()));
                            if (valueList.containsKey(ins6.getV())) {
                                ins6.setValue(valueList.get(ins6.getV()).get(block));
                            }
                            ArrayList<TypeValue> typeValues = ins6.getCommas();
                            for(TypeValue k:typeValues){
                                if(valueList.containsKey(k.getValue())){
                                    k.setValue(valueList.get(k.getValue()).get(block));
                                }
                            }
                            break;
                    }
                    //System.out.println(ins2.getIdent());
                    //System.out.println(ins2.getIdent());
                    String name = ins2.getIdent().getName();
                    Value oldvalue = new Value(new Ident(name));
                    ins2.getIdent().setName("t"+version);
                    version++;
                    //System.out.println("........................................."+name);
                    if (!valueList.containsKey(oldvalue) && !j.getInstrname().equals("load")) {
                        System.out.println("assign don't have "+oldvalue);
                        HashMap<Block, Value> newHash = new HashMap<>();
                        newHash.put(block, new Value(ins2.getIdent()));
                        valueList.put(oldvalue, newHash);
                    }
                    break;

                case "store":
                    StoreInstr ins4 = (StoreInstr) i;
                    PointerType pointer = (PointerType) ins4.getT2();
                    if(pointer.getT() instanceof IntType
                            || pointer.getT() instanceof FloatType) {
                        if(!ins4.getV2().getIdent().isGlobal() && !isPointer.contains(ins4.getV2().getIdent())) {
                            i.setCanDelete(true);
                            if (valueList.containsKey(ins4.getV2())) {
                                if (valueList.containsKey(ins4.getV1())) {
                                    valueList.get(ins4.getV2()).put(block, valueList.get(ins4.getV1()).get(block));
                                } else {
                                    valueList.get(ins4.getV2()).put(block, ins4.getV1());
                                }
                            }
                        }
                    }
                    if(!i.isCanDelete()){
                        if(valueList.containsKey(ins4.getV1())){
                            ins4.setV1(valueList.get(ins4.getV1()).get(block));
                        }
                        if(valueList.containsKey(ins4.getV2())){
                            ins4.setV2(valueList.get(ins4.getV2()).get(block));
                        }
                    }
                    /*
                    HashMap<Block,Value> hash = valueList.get(ins4.getV1());
                    hash.put(block,valueList.get(ins4.getV2()).get(block));
                    valueList.put(ins4.getV1(),hash);
                     */
                    break;
                case "call":
                    CallInst ins5 = (CallInst) i;
                    for (TypeValue typevalue : ins5.getArgs()) {
                        if (valueList.containsKey(typevalue.getValue())) {
                            typevalue.setValue(valueList.get(typevalue.getValue()).get(block));
                        }
                    }
                    break;
                //似乎不应当有
                case "ret":
                    RetTerm ins7 = (RetTerm) i;
                    if (valueList.containsKey(ins7.getV())) {
                        ins7.setV(valueList.get(ins7.getV()).get(block));
                    }
                    break;
                case "br":
                    BrTerm ins9 = (BrTerm) i;
                    /*
                    if(renamedLabel.containsKey(ins9.getLabelName())){
                        ins9.setLi(new Ident(renamedLabel.get(ins9.getLabelName())));
                    }
                    else{
                        renamedLabel.put(ins9.getLabelName(),String.valueOf(version));
                        renameLabel(ins9.getLabelName(), String.valueOf(version));
                        ins9.setLi(new Ident(version));
                        version++;
                    }
                    */
                    break;
                case "condbr":
                    CondBrTerm ins10 = (CondBrTerm) i;
                    /*
                    if(renamedLabel.containsKey(ins10.getLabel1Name())) {
                        ins10.setI1(new Ident(renamedLabel.get(ins10.getLabel1Name())));
                    }
                    else{
                        renamedLabel.put(ins10.getLabel1Name(), String.valueOf(version));
                        renameLabel(ins10.getLabel1Name(), String.valueOf(version));
                        ins10.setI1(new Ident(version));
                        version++;
                    }
                    if(renamedLabel.containsKey(ins10.getLabel2Name())) {
                        ins10.setI2(new Ident(renamedLabel.get(ins10.getLabel2Name())));
                    }
                    else{
                        renamedLabel.put(ins10.getLabel2Name(), String.valueOf(version));
                        renameLabel(ins10.getLabel2Name(), String.valueOf(version));
                        ins10.setI2(new Ident(version));
                        version++;
                    }
                    */
                    if (valueList.containsKey(ins10.getV())) {
                        ins10.setV(valueList.get(ins10.getV()).get(block));
                    }
                    break;
            }
            System.out.println("worked:" + i.toString());
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
                //System.out.println("succeed in renamelabel");
            }
        }
    }

    public void renamePhi (Block block){
        havePhi.add(block);
        for(Phi i:block.getPhis()){
            HashSet<Block> needDeleted = new HashSet<>();
            System.out.println("in renamePhi:" + block.getLabel()+":"+i.toString());
            for(Block j:i.getParams().keySet()){
                if(valueList.get(i.getOriginValue()).containsKey(j)) {
                    i.reName(valueList.get(i.getOriginValue()).get(j), j);
                }
                else{
                    needDeleted.add(j);
                }
            }
            for(Block j:needDeleted){
                i.deleteBlock(j);
            }
            System.out.println("renamed:"+i.toString());
        }
        for(Block j:block.getSucBlocks()){
            if(!havePhi.contains(j)){
                renamePhi(j);
            }
        }
    }

    public void deleteInstr(){
        for(Block i:function.getBlocklist()){
            ArrayList<Instr> instrlist = i.getInblocklist();
            instrlist.removeIf(Instr::isCanDelete);
        }
    }
}
