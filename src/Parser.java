import AST.*;
import word.RawWord;
import word.WordType;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Parser {
    private ArrayList<RawWord> rawWords;
    private int point = 0;
    private ArrayList<String> outputs = new ArrayList<>();
    private CompUnit compUnit;


    public Parser(ArrayList<RawWord> rawWords) throws FileNotFoundException {
        this.rawWords = rawWords;
        compUnit = getCompUnit();
        compUnit.addMidCode();
        PrintStream printStream = new PrintStream("llvmir.ll");
        for (String s : compUnit.getLLVMIR()) {
            printStream.print(s);
        }

    }

    public ArrayList<String> getStrings() {
        return compUnit.getStrings();
    }


    private void error() {
        System.out.println("error at line: " + rawWords.get(point - 1).getLine());
    }

//    private void funcNewName(RawWord rawWord) {
//        String ans = rawWord.getName();
//        String name = rawWord.getName();
//        for (int i = 0; i < name.length(); i++) {
//            if (name.charAt(i) >= '0' && name.charAt(i) <= '9') {
//                ans = name.substring(0, i);
//                break;
//            }
//        }
//        while (funcNameMap.containsValue(ans)) {
//            ans += 'a';
//        }
//        funcNameMap.put(name, ans);
//        rawWord.setName(ans);
//    }
//
//    private void funcReName(RawWord rawWord) {
//        if (funcNameMap.containsKey(rawWord.getName())) {
//            rawWord.setName(funcNameMap.get(rawWord.getName()));
//        }
//    }

    private RawWord getNextWord() {
        RawWord rawWord = rawWords.get(point);
        //System.out.println(rawWord.getName());
        outputs.add(rawWord.getType().name() + " " + rawWord.getName());
        point++;
        return rawWord;
    }

    private RawWord seeNextWord() {
        return rawWords.get(point);
    }

    private RawWord seeSecondWord() {
        return rawWords.get(point + 1);
    }

    private RawWord seeThirdWord() {
        return rawWords.get(point + 2);
    }

    //CompUnit → [ CompUnit ] ( Decl | FuncDef )
    private CompUnit getCompUnit() {
        ArrayList<Decl> decls = new ArrayList<>();
        ArrayList<FuncDef> funcDefs = new ArrayList<>();
        MainFuncDef mainFuncDef = null;
        while (true) {
            if (point == rawWords.size()) {
                break;
            }
            if (seeNextWord().getType() == WordType.CONSTTK
                    || (seeNextWord().getType() == WordType.INTTK && seeThirdWord().getType() != WordType.LPARENT)
                    || (seeNextWord().getType() == WordType.FLOATTK && seeThirdWord().getType() != WordType.LPARENT)) {
                decls.add(getDecl());
            } else if (seeNextWord().getType() == WordType.VOIDTK
                    || (seeNextWord().getType() == WordType.INTTK && seeThirdWord().getType() == WordType.LPARENT)
                    || (seeNextWord().getType() == WordType.FLOATTK && seeThirdWord().getType() == WordType.LPARENT)) {
                funcDefs.add(getFuncDef());
            } else {
                break;
            }
        }
        outputs.add("<CompUnit>");
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    //Decl → ConstDecl | VarDecl
    private Decl getDecl() {
        Decl decl = null;
        if (seeNextWord().getType() == WordType.CONSTTK) {
            decl = getConstDecl();
        } else if (seeNextWord().getType() == WordType.INTTK
                || seeNextWord().getType() == WordType.FLOATTK) {
            decl = getVarDecl();
        } else {
            error();
        }
        //outputs.add("<Decl>");
        return decl;
    }

    //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private ConstDecl getConstDecl() {
        String type = null;
        ArrayList<ConstDef> constDefs = new ArrayList<>();
        if (getNextWord().getType() == WordType.CONSTTK) {
            type = getBType();
            constDefs.add(getConstDef());
            while (seeNextWord().getType() == WordType.COMMA) {
                getNextWord();
                constDefs.add(getConstDef());
            }
            if (getNextWord().getType() == WordType.SEMICN) {
            } else {
                error();
            }
        } else {
            error();
        }
        outputs.add("<ConstDecl>");
        return new ConstDecl(type, constDefs);
    }

    //BType → 'int' | 'float'
    private String getBType() {
        String type = null;
        if (seeNextWord().getType() == WordType.INTTK) {
            getNextWord();
            type = "i32";
        } else if (seeNextWord().getType() == WordType.FLOATTK) {
            getNextWord();
            type = "float";
        } else {
            error();
        }
        //outputs.add("<BType>");
        return type;
    }
    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private ConstDef getConstDef() {
        ID id = null;
        ArrayList<Exp> dims = new ArrayList<>();
        ConstInitVal constInitVal = null;
        if (seeNextWord().getType() == WordType.IDENFR) {
            id = new ID(getNextWord());
            while (seeNextWord().getType() == WordType.LBRACK) {
                getNextWord();
                dims.add(getConstExp());
                if (getNextWord().getType() == WordType.RBRACK) {

                } else {
                    error();
                }
            }
            if (getNextWord().getType() == WordType.ASSIGN) {

                constInitVal = getConstInitVal();

            } else {
                error();
            }
        } else {
            error();
        }
        outputs.add("<ConstDef>");
        return new ConstDef(id, dims, constInitVal);
    }




    //VarDecl → BType VarDef { ',' VarDef } ';'
    private VarDecl getVarDecl() {
        String type = null;
        ArrayList<VarDef> varDefs = new ArrayList<>();

        type = getBType();
        varDefs.add(getVarDef());
        while (seeNextWord().getType() == WordType.COMMA) {
            getNextWord();
            varDefs.add(getVarDef());
        }
        if (getNextWord().getType() == WordType.SEMICN) {

        } else {
            error();
        }
        outputs.add("<VarDecl>");
        return new VarDecl(type, varDefs);
    }

    //VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    //    | Ident { '[' ConstExp ']' } '=' InitVal
    private VarDef getVarDef() {
        ID id = null;
        ArrayList<Exp> dims = new ArrayList<>();
        InitVal initVal = null;

        if (seeNextWord().getType() == WordType.IDENFR) {
            id = new ID(getNextWord());
            while (seeNextWord().getType() == WordType.LBRACK) {
                getNextWord();
                dims.add(getConstExp());
                if (getNextWord().getType() == WordType.RBRACK) {

                } else {
                    error();
                }
            }
        }
        if (seeNextWord().getType() == WordType.ASSIGN) {
            getNextWord();
            initVal = getInitVal();
        }
        outputs.add("<VarDef>");
        return new VarDef(id, dims, initVal);
    }



    //ConstInitVal → ConstExp
    //    | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    private ConstInitVal getConstInitVal() {
        ArrayList<Object> initValues = new ArrayList<>();
        if (seeNextWord().getType() == WordType.LBRACE) {
            getNextWord();
            if (seeNextWord().getType() != WordType.RBRACE) {
                if (seeNextWord().getType() == WordType.LBRACE) {
                    initValues.add(getConstInitVal());
                } else {
                    initValues.add(getConstExp());
                }
                while (seeNextWord().getType() == WordType.COMMA) {
                    getNextWord();
                    if (seeNextWord().getType() == WordType.LBRACE) {
                        initValues.add(getConstInitVal());
                    } else {
                        initValues.add(getConstExp());
                    }
                }
            }
            if (getNextWord().getType() == WordType.RBRACE) {

            } else {
                error();
            }
        } else {
            initValues.add(getConstExp());
        }
        outputs.add("<ConstInitVal>");
        return new ConstInitVal(initValues);
    }

    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    private InitVal getInitVal() {
        ArrayList<Object> initValues = new ArrayList<>();
        if (seeNextWord().getType() == WordType.LBRACE) {
            getNextWord();
            if (seeNextWord().getType() != WordType.RBRACE) {
                if (seeNextWord().getType() == WordType.LBRACE) {
                    initValues.add(getInitVal());
                } else {
                    initValues.add(getExp());
                }
                while (seeNextWord().getType() == WordType.COMMA) {
                    getNextWord();
                    if (seeNextWord().getType() == WordType.LBRACE) {
                        initValues.add(getInitVal());
                    } else {
                        initValues.add(getExp());
                    }
                }
            }
            if (getNextWord().getType() == WordType.RBRACE) {

            } else {
                error();
            }
        } else {
            initValues.add(getExp());
        }
        outputs.add("<InitVal>");
        return new InitVal(initValues);
    }

    //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncDef getFuncDef() {
        String type = null;
        ID id = null;
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        StmtBlock block = null;

        type = getFuncType();
        if (seeNextWord().getType() != WordType.IDENFR) {
            error();
        }
        id = new ID(getNextWord());
        if (getNextWord().getType() != WordType.LPARENT) {
            error();
        }
        if (seeNextWord().getType() != WordType.RPARENT) {
            funcFParams = getFuncFParams();
        }
        getNextWord();
        block = getBlock();
        outputs.add("<FuncDef>");
        return new FuncDef(type, id, funcFParams, block);
    }

    //MainFuncDef → 'int' 'main' '(' ')' Block
    private MainFuncDef getMainFuncDef() {
        StmtBlock block = null;
        if (!getNextWord().getName().equals("int")) {
            error();
        }
        if (!getNextWord().getName().equals("main")) {
            error();
        }
        if (getNextWord().getType() != WordType.LPARENT) {
            error();
        }
        if (getNextWord().getType() != WordType.RPARENT) {
            error();
        }
        block = getBlock();
        outputs.add("<MainFuncDef>");
        return new MainFuncDef(block);
    }

    //FuncType → 'void' | 'int'
    private String getFuncType() {
        String type = null;
        if (seeNextWord().getType() == WordType.VOIDTK) {
            getNextWord();
            type = "void";
        } else if (seeNextWord().getType() == WordType.INTTK) {
            getNextWord();
            type = "i32";
        } else if (seeNextWord().getType() == WordType.FLOATTK) {
            getNextWord();
            type = "float";
        } else {
            error();
        }
        outputs.add("<FuncType>");
        return type;
    }

    //FuncFParams → FuncFParam { ',' FuncFParam }
    private ArrayList<FuncFParam> getFuncFParams() {
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        funcFParams.add(getFuncFParam());
        while (seeNextWord().getType() == WordType.COMMA) {
            getNextWord();
            funcFParams.add(getFuncFParam());
        }
        outputs.add("<FuncFParams>");
        return funcFParams;
    }

    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private FuncFParam getFuncFParam() {
        String type = null;
        ID id = null;
        ArrayList<Exp> dims = new ArrayList<>();

        type = getBType();
        if (seeNextWord().getType() != WordType.IDENFR) {
            error();
        }
        id = new ID(getNextWord());
        if (seeNextWord().getType() == WordType.LBRACK) {
            dims.add(new ConstInt(-1));
            getNextWord();
            if (getNextWord().getType() != WordType.RBRACK) {
                error();
            }
            while (seeNextWord().getType() == WordType.LBRACK) {
                getNextWord();
                dims.add(getConstExp());
                if (getNextWord().getType() != WordType.RBRACK) {
                    error();
                }
            }
        }
        outputs.add("<FuncFParam>");
        return new FuncFParam(type, id, dims);
    }

    //Block → '{' { BlockItem } '}'
    private StmtBlock getBlock() {
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        if (getNextWord().getType() != WordType.LBRACE) {
            error();
        }
        while (seeNextWord().getType() != WordType.RBRACE) {
            blockItems.add(getBlockItem());
        }
        getNextWord();
        outputs.add("<Block>");
        return new StmtBlock(blockItems);
    }

    //BlockItem → Decl | Stmt
    private BlockItem getBlockItem() {
        BlockItem blockItem = null;
        if (seeNextWord().getType() == WordType.CONSTTK
                || seeNextWord().getType() == WordType.INTTK
                || seeNextWord().getType() == WordType.FLOATTK) {
            blockItem = getDecl();
        } else {
            blockItem = getStmt();
        }
        //outputs.add("<BlockItem>");
        return blockItem;
    }

    //Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //    | [Exp] ';' //有无Exp两种情况
    //    | Block
    //    | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //    | 'while' '(' Cond ')' Stmt
    //    | 'break' ';'
    //    | 'continue' ';'
    //    | 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //    | LVal = 'getint''('')'';'
    //    | 'printf' '('FormatString {',' Exp} ')'';' // 1.有Exp 2.无Exp
    private Stmt getStmt() {
        Stmt stmt = null;
        //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        if (seeNextWord().getType() == WordType.IFTK) {
            Cond cond = null;
            Stmt stmt1 = null;
            Stmt stmt2 = null;
            getNextWord();
            if (getNextWord().getType() != WordType.LPARENT) {
                error();
            }
            cond = getCond();
            if (getNextWord().getType() != WordType.RPARENT) {
                error();
            }
            stmt1 = getStmt();
            if (seeNextWord().getType() == WordType.ELSETK) {
                getNextWord();
                stmt2 = getStmt();
            }
            stmt = new StmtIf(cond, stmt1, stmt2);
        }
        //'while' '(' Cond ')' Stmt
        else if (seeNextWord().getType() == WordType.WHILETK) {
            Cond cond = null;
            Stmt stmt1 = null;
            getNextWord();
            if (getNextWord().getType() != WordType.LPARENT) {
                error();
            }
            cond = getCond();
            if (getNextWord().getType() != WordType.RPARENT) {
                error();
            }
            stmt1 = getStmt();
            stmt = new StmtWhile(cond, stmt1);
        }
        //'break' ';'
        else if (seeNextWord().getType() == WordType.BREAKTK) {
            getNextWord();
            if (getNextWord().getType() != WordType.SEMICN) {
                error();
            }
            stmt = new StmtBreak();
        }
        //'continue' ';'
        else if (seeNextWord().getType() == WordType.CONTINUETK) {
            getNextWord();
            if (getNextWord().getType() != WordType.SEMICN) {
                error();
            }
            stmt = new StmtContinue();
        }
        //'return' [Exp] ';'
        else if (seeNextWord().getType() == WordType.RETURNTK) {
            Exp returnExp = null;
            getNextWord();
            if (seeNextWord().getType() != WordType.SEMICN) {
                returnExp = getExp();
            }
            if (getNextWord().getType() != WordType.SEMICN) {
                error();
            }
            stmt = new StmtReturn(returnExp);
        }
        //'printf' '('FormatString {',' Exp} ')'';'
        else if (seeNextWord().getType() == WordType.PRINTFTK) {
            String formatString = null;
            ArrayList<Exp> exps = new ArrayList<>();

            getNextWord();
            if (getNextWord().getType() != WordType.LPARENT) {
                error();
            }
            if (seeNextWord().getType() != WordType.STRCON) {
                error();
            }
            formatString = getNextWord().getName();
            while (seeNextWord().getType() == WordType.COMMA) {
                getNextWord();
                exps.add(getExp());
            }
            if (getNextWord().getType() != WordType.RPARENT) {
                error();
            }
            if (getNextWord().getType() != WordType.SEMICN) {
                error();
            }
            stmt = new StmtPrintf(formatString, exps);
        }
        //Block
        else if (seeNextWord().getType() == WordType.LBRACE) {
            stmt = getBlock();
        } else if (seeNextWord().getType() == WordType.SEMICN) {
            getNextWord();
            stmt = new StmtExp(null);
        } else if (seeNextWord().getType() == WordType.INTCON
                || seeNextWord().getType() == WordType.FLOATCON
                || seeNextWord().getType() == WordType.PLUS
                || seeNextWord().getType() == WordType.MINU
                || seeNextWord().getType() == WordType.NOT
                || seeNextWord().getType() == WordType.LPARENT
                || (seeNextWord().getType() == WordType.IDENFR && seeSecondWord().getType() == WordType.LPARENT)
                || !isLVal()) {
            Exp exp = getExp();
            if (getNextWord().getType() != WordType.SEMICN) {
                error();
            }
            stmt = new StmtExp(exp);
        } else {
            LVal lVal = null;
            lVal = getLVal();
            if (getNextWord().getType() != WordType.ASSIGN) {
                error();
            }

            Exp exp = getExp();
            if (getNextWord().getType() != WordType.SEMICN) {
                error();
            }
            stmt = new StmtAssign(lVal, exp);

        }
        outputs.add("<Stmt>");
        return stmt;
    }

    //Exp → AddExp
    private Exp getExp() {
        Exp exp = null;
        exp = getAddExp();
        outputs.add("<Exp>");
        return exp;
    }

    //Cond → LOrExp
    private Cond getCond() {
        LOrExp lOrExp = getLOrExp();
        outputs.add("<Cond>");
        return new Cond(lOrExp);
    }

    private boolean isLVal() {
        int a = 0;
        for (int i = point; ; i++) {
            if (rawWords.get(i).getType() == WordType.ASSIGN) {
                a = 1;
            }
            if (rawWords.get(i).getType() == WordType.SEMICN) {
                return a == 1;
            }
        }
    }

    //LVal → Ident {'[' Exp ']'}
    private LVal getLVal() {
        ID id = null;
        ArrayList<Exp> dims = new ArrayList<>();
        if (seeNextWord().getType() != WordType.IDENFR) {
            error();
        }
        id = new ID(getNextWord());
        while (seeNextWord().getType() == WordType.LBRACK) {
            getNextWord();
            dims.add(getExp());
            if (getNextWord().getType() != WordType.RBRACK) {
                error();
            }
        }
        outputs.add("<LVal>");
        return new LVal(id, dims);
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number
    private Exp getPrimaryExp() {
        Exp exp = null;
        if (seeNextWord().getType() == WordType.LPARENT) {
            getNextWord();
            exp = getExp();
            if (getNextWord().getType() != WordType.RPARENT) {
                error();
            }
        } else if (seeNextWord().getType() == WordType.INTCON
                || seeNextWord().getType() == WordType.FLOATCON) {
            exp = getNumber();
        } else {
            exp = getLVal();
        }
        outputs.add("<PrimaryExp>");
        return exp;
    }

    //Number → IntConst
    private MyNumber getNumber() {
        MyNumber number = null;
        if (seeNextWord().getType() == WordType.INTCON) {
            number = new ConstInt(Integer.parseInt(getNextWord().getName()));
        } else if (seeNextWord().getType() == WordType.FLOATCON) {
            number = new ConstFloat(Float.parseFloat(getNextWord().getName()));
        } else {
            error();
        }
        outputs.add("<Number>");
        return number;
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'
    //    | UnaryOp UnaryExp
    private Exp getUnaryExp() {
        Exp exp = null;
        if (seeNextWord().getType() == WordType.PLUS
                || seeNextWord().getType() == WordType.MINU
                || seeNextWord().getType() == WordType.NOT) {
            WordType op = getUnaryOp();
            Exp exp1 = getUnaryExp();
            exp = new ExpOp(op, new ConstInt(0), exp1);
        } else if (seeNextWord().getType() == WordType.IDENFR
                && seeSecondWord().getType() == WordType.LPARENT) {
            ArrayList<Exp> params = new ArrayList<>();
            ID id = new ID(getNextWord());
            getNextWord();
            if (seeNextWord().getType() != WordType.RPARENT) {
                params = getFuncRParams();
            }
            if (getNextWord().getType() != WordType.RPARENT) {
                error();
            }
            exp = new FuncCall(id, params);
        } else {
            exp = getPrimaryExp();
        }
        outputs.add("<UnaryExp>");
        return exp;
    }

    //UnaryOp → '+' | '−' | '!'
    private WordType getUnaryOp() {
        WordType op = null;
        if (seeNextWord().getType() == WordType.PLUS
                || seeNextWord().getType() == WordType.MINU
                || seeNextWord().getType() == WordType.NOT) {
            op = getNextWord().getType();
        } else {
            error();
        }
        outputs.add("<UnaryOp>");
        return op;
    }

    //FuncRParams → Exp { ',' Exp }
    private ArrayList<Exp> getFuncRParams() {
        ArrayList<Exp> params = new ArrayList<>();
        params.add(getExp());
        while (seeNextWord().getType() == WordType.COMMA) {
            getNextWord();
            params.add(getExp());
        }
        outputs.add("<FuncRParams>");
        return params;
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    private Exp getMulExp() {
        Exp exp = getUnaryExp();
        if (seeNextWord().getType() == WordType.MULT
                || seeNextWord().getType() == WordType.DIV
                || seeNextWord().getType() == WordType.MOD) {
            while (seeNextWord().getType() == WordType.MULT
                    || seeNextWord().getType() == WordType.DIV
                    || seeNextWord().getType() == WordType.MOD) {
                outputs.add("<MulExp>");
                WordType op = getNextWord().getType();
                Exp exp2 = getUnaryExp();
                exp = new ExpOp(op, exp, exp2);
            }
        } else {
            exp = exp;
        }
        outputs.add("<MulExp>");
        return exp;
    }

    //AddExp → MulExp | AddExp ('+' | '−') MulExp
    private Exp getAddExp() {
        Exp exp = getMulExp();
        if (seeNextWord().getType() == WordType.PLUS
                || seeNextWord().getType() == WordType.MINU) {
            while (seeNextWord().getType() == WordType.PLUS
                    || seeNextWord().getType() == WordType.MINU) {
                outputs.add("<AddExp>");
                WordType op = getNextWord().getType();
                Exp exp2 = getMulExp();
                exp = new ExpOp(op, exp, exp2);
            }
        } else {
            exp = exp;
        }
        outputs.add("<AddExp>");
        return exp;
    }

    //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    private Exp getRelExp() {
        Exp exp = getAddExp();
        if (seeNextWord().getType() == WordType.LSS
                || seeNextWord().getType() == WordType.LEQ
                || seeNextWord().getType() == WordType.GRE
                || seeNextWord().getType() == WordType.GEQ) {
            while (seeNextWord().getType() == WordType.LSS
                    || seeNextWord().getType() == WordType.LEQ
                    || seeNextWord().getType() == WordType.GRE
                    || seeNextWord().getType() == WordType.GEQ) {
                outputs.add("<RelExp>");
                WordType op = getNextWord().getType();
                Exp exp2 = getAddExp();
                exp = new ExpOp(op, exp, exp2);
            }
        } else {
            exp = exp;
        }
        outputs.add("<RelExp>");
        return exp;
    }

    //EqExp → RelExp | EqExp ('==' | '!=') RelExp
    private Exp getEqExp() {
        Exp exp = getRelExp();
        if (seeNextWord().getType() == WordType.EQL
                || seeNextWord().getType() == WordType.NEQ) {
            while (seeNextWord().getType() == WordType.EQL
                    || seeNextWord().getType() == WordType.NEQ) {
                outputs.add("<EqExp>");
                WordType op = getNextWord().getType();
                Exp exp2 = getRelExp();
                exp = new ExpOp(op, exp, exp2);
            }
        } else {
            exp = exp;
        }
        outputs.add("<EqExp>");
        return exp;
    }

    //LAndExp → EqExp | LAndExp '&&' EqExp
    private LAndExp getLAndExp() {
        ArrayList<Exp> exps = new ArrayList<>();
        exps.add(getEqExp());
        while (seeNextWord().getType() == WordType.AND) {
            outputs.add("<LAndExp>");
            getNextWord();
            exps.add(getEqExp());
        }
        outputs.add("<LAndExp>");
        return new LAndExp(exps);
    }

    //LOrExp → LAndExp | LOrExp '||' LAndExp
    private LOrExp getLOrExp() {
        ArrayList<LAndExp> lAndExps = new ArrayList<>();
        lAndExps.add(getLAndExp());
        while (seeNextWord().getType() == WordType.OR) {
            outputs.add("<LOrExp>");
            getNextWord();
            lAndExps.add(getLAndExp());
        }
        outputs.add("<LOrExp>");
        return new LOrExp(lAndExps);
    }

    private Exp getConstExp() {
        Exp exp = null;
        exp = getAddExp();
        outputs.add("<ConstExp>");
        return exp;
    }

    public void output2(PrintStream printStream) {
        for (String s : outputs) {
            printStream.println(s);
        }
    }
}