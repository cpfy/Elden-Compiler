package llvm.Instr;

import java.util.ArrayList;
import java.util.HashMap;
import llvm.Block;
import llvm.Type.FloatType;
import llvm.Type.Type;
import llvm.Value;

public class Phi extends Instr{
    // 基本块的Phi函数
    private Value value;
    private Value originValue; //初始化后不可变。
    //<label,value>键值对,由于block决定value名，因此block的label为key。
    private HashMap<Block,Value> params = new HashMap();
    private Type type;
    public Phi (String instrname, Value value, ArrayList<Block> blocks, Type type){
        super(instrname);
        this.value = value;
        this.originValue = value;
        for (Block i : blocks){
            params.put(i,value);
        }
        this.type = type;
    }
    public String toString(){
        ArrayList<String> choices = new ArrayList<>();
        String type = (this.type instanceof FloatType)? "float" : "i32";
        for(Block i: params.keySet()){
            if (params.get(i) == null) {
                continue;
            }
            String choice = "["+ params.get(i).toString() + ",%l" + i.getLabel() +"]";
            choices.add(choice);
        }
        StringBuffer ans = new StringBuffer(value.toString() + " = phi " + type + " ");
        int size = choices.size()-1;
        for(int i = 0;i <= size;i++){
            ans.append(choices.get(i));
            if(i != size) ans.append(",");
        }
        return ans.toString();
    }

    public HashMap<Block, Value> getParams() {
        return params;
    }
    public void SetParams(HashMap<Block, Value> params){
        this.params = params;
    }
    public Value getValue(){return this.value;}
    public void setValue(Value value){this.value = value;}
    public void reName(Value value,Block block){params.put(block,value);}
    public void reName(Value value){this.value = value;}
    public Value getOriginValue(){return originValue;}
    public void deleteBlock(Block block){params.remove(block);}
    public Type getType(){return this.type;}
    public void setBlock(Block newBlock, Block oldBlock) {
        if (!params.containsKey(oldBlock)) {
            return;
        }
        Value value = params.get(oldBlock);
        params.remove(oldBlock);
        params.put(newBlock, value);
    }
    @Override
    public void renameUses(Value newValue, Value oldValue) {
        for (Block block: params.keySet()) {
            Value v = params.get(block);
            if (v.isIdent() && v.getIdent().equals(oldValue.getIdent())) {
                params.put(block, newValue);
            }
        }
    }

    @Override
    public Value mergeConst() {
        return null;
    }

    @Override
    public ArrayList<String> getUses() {
        ArrayList<String> ans = new ArrayList<>();
        for (Value v: params.values()) {
            if (v.isIdent()) {
                ans.add(v.getIdent().toString());
            }
        }
        return ans;
    }

    @Override
    public String getDef() {
        return value.getIdent().toString();
    }

    @Override
    public HashMap<String, Boolean> getUsesAndTypes() {
        HashMap<String, Boolean> ans = new HashMap<>();
        System.out.println("ERROR!!!!!");
        return ans;
    }

    @Override
    public HashMap<String, Boolean> getDefAndType() {
        System.out.println("ERROR!!!!!");
        return new HashMap<>();
    }

    @Override
    public ArrayList<String> getRoots() {
        ArrayList<String> ans = new ArrayList<>();
        return ans;
    }

    @Override
    public boolean setAssignType() {
        System.err.println("ERROR!!");
        return false;
    }

}
