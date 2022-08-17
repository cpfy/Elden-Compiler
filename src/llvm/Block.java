package llvm;

import llvm.Instr.Phi;
import llvm.Instr.BrTerm;
import llvm.Instr.CondBrTerm;
import llvm.Instr.Instr;

import java.util.ArrayList;

public class Block {
    // 基本块
    private ArrayList<Instr> inblocklist;  // 基本块内所有指令
    private String label;   // 标签名
    private int num;    // 按顺序基本块编号1-n

    private boolean dirty = false;  // 是否arm生成时丢弃

//    private int instrnum;   // b内指令条数

    /*** add start by sujunzhe ***/
    private boolean isDead;

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    private ArrayList<Block> preBlocks = new ArrayList<>();     //前驱基本块
    private ArrayList<Block> sucBlocks = new ArrayList<>();     //后继基本块

    private Block IDOM = null; //IDOM

    private ArrayList<Block> dominatorFrontiers = new ArrayList<>();     //该节点的支配边界节点

    public ArrayList<Block> getPreBlocks() {
        if (preBlocks == null) {
            System.err.println("Error! 请先计算前驱基本块");
        }
        return preBlocks;
    }

    public void addPreBlock(Block block) {
        if (preBlocks == null) {
            preBlocks = new ArrayList<>();
        }
        preBlocks.add(block);
    }

    public ArrayList<Block> getSucBlocks() {
        if (sucBlocks == null) {
            System.err.println("Error! 请先计算后继基本块");
        }
        return sucBlocks;
    }

    public void addSucBlock(Block block) {
        if (sucBlocks == null) {
            sucBlocks = new ArrayList<>();
        }
        sucBlocks.add(block);
    }

    public Block getIDom() {
        if (IDOM == null) {
            System.err.println(this.getLabel());
        }
        return IDOM;
    }

    public void addIDom(Block block) {
        IDOM = block;
    }

    public ArrayList<Block> getDominatorFrontiers() {
        if (dominatorFrontiers == null) {
            System.err.println("Error! 请先计算支配边界");
        }
        return dominatorFrontiers;
    }

    public void addDominatorFrontier(Block block) {
        if (dominatorFrontiers == null) {
            dominatorFrontiers = new ArrayList<>();
        }
        dominatorFrontiers.add(block);
    }

    // 得全读取完再查，不然会出问题
    // 返回列表，其中列表中为该基本块可能进入的后继基本块（应该通过该基本块的最后一条指令就可以计算出）
    // 无suc的情况返回空列表
    public ArrayList<Block> getBrInfo() {
        ArrayList<Block> sucBlist = new ArrayList<>();
        Instr last = inblocklist.get(inblocklist.size() - 1);
        switch (last.getInstrname()) {
            case "ret":
                return sucBlist;
            case "br":
                if (last instanceof BrTerm) {
                    BrTerm brt = (BrTerm) last;
                    String label1 = brt.getLabelName();
                    Block b = IRParser.searchBlockmapByLabel(label1);
                    if (b != null) {
                        sucBlist.add(b);
                    }
                }
                break;
            case "condbr":
                if (last instanceof CondBrTerm) {
                    CondBrTerm cbrt = (CondBrTerm) last;
                    String label1 = cbrt.getLabel1Name();
                    String label2 = cbrt.getLabel2Name();
                    Block b = IRParser.searchBlockmapByLabel(label1);
                    if (b != null) {
                        sucBlist.add(b);
                    }
                    b = IRParser.searchBlockmapByLabel(label2);
                    if (b != null) {
                        sucBlist.add(b);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException();
        }

        return sucBlist;
    }

    /*** add end by sujunzhe ***/

    // 基本属性2
    private ArrayList<Phi> phis = new ArrayList<>();    // 基本块的Phi函数
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

    public ArrayList<Phi> getPhis() {
        return phis;
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

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void addInstr(Instr instr) {
        this.inblocklist.add(instr);
    }

    public void clear() {
        ArrayList<Instr> newInblocklist = new ArrayList<>();
        for (Instr instr : inblocklist) {
            if (!instr.isCanDelete()) {
                newInblocklist.add(instr);
            }
        }
        inblocklist = newInblocklist;
    }


    public void addPhi(Phi phi) {
        phis.add(phi);
    }
}
