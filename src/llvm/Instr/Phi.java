package llvm.Instr;

import java.util.ArrayList;
import java.util.HashMap;
import llvm.Block;
import llvm.Value;

public class Phi extends Instr{
    // 基本块的Phi函数
    private Value value;
    //<label,value>键值对,由于block决定value名，因此block的label为key。
    private HashMap<Block,Value> params = new HashMap();
    public Phi (String instrname, Value value, ArrayList<Block> blocks){
        super(instrname);
        this.value = value;
        for (Block i : blocks){
            params.put(i,value);
        }
    }
    public String toString(){
        ArrayList<String> choices = new ArrayList<>();
        for(Block i: params.keySet()){
            String choice = "["+ params.get(i).toString() + ",%l" + i.getLabel() +"]";
            choices.add(choice);
        }
        StringBuffer ans = new StringBuffer(value.toString() + " = phi i32 ");
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
    public ArrayList<String> getRoots() {
        ArrayList<String> ans = new ArrayList<>();
        return ans;
    }

}
