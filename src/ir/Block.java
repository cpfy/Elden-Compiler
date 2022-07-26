package ir;

import backend.Phi;
import ir.instr.Instr;

import java.util.ArrayList;

public class Block {
    // 基本块
    private ArrayList<Instr> inblocklist;  // 基本块内所有指令
    private String label;   // 标签名
    private int num;    // 按顺序基本块编号1-n


    /***** 此处几个还todo ****/

//    private int instrnum;   // b内指令条数


    private Phi phi;    // 基本块的Phi函数
    private String instr;   //branch跳转 的bne等类型
    private String jumploc; //branch的跳转位置

    public boolean global;   //是否全局
    private String innerfunc;   // 所属函数名

    public Block() {
        this.inblocklist = new ArrayList<>();
    }

    public ArrayList<Instr> getInblocklist() {
        return inblocklist;
    }

    public boolean isGlobal() {
        return global;
    }

    public String getJumploc() {
        return jumploc;
    }

    public String getInstr() {
        return instr;
    }

    public int getNum() {
        return num;
    }

    public Phi getPhi() {
        return phi;
    }

    public String getInnerfunc() {
        return innerfunc;
    }

    public String getLabel() {
        return label;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void addInstr(Instr instr){
        this.inblocklist.add(instr);
    }
}
