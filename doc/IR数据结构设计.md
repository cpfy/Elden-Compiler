

#### llvm示例

```

```





### IRCode

最基础的 “指令” 类，包括

```
private String type;    //N种中间代码种类
private String rawstr;  //输出的ircode字符串格式

private String IRstring;
private String kind;    //const 等情况
private String name;
private int num;

public boolean global;   //是否全局
public boolean init = false;    //int,array是否有初始化值
private ArrayList<Integer> initList = new ArrayList<>(); //数组的初始化值List
public boolean voidreturn;

private Variable variable;  //含有表达式等情况时，对应的Variable类型

private int array1;     //数组形式时第1维的大小
private int array2;     //数组形式时第2维的大小

private String operator;
private Variable dest;      //二元运算或一元运算中的目标变量
private Variable oper1;     //二元运算中的第1个操作数，或一元运算的右操作数
private Variable oper2;     //二元运算第2个操作数

private Symbol symbol;  //含有表达式等情况时，对应的symbol类型的符号
private SymbolTable.Scope scope;    //todo inblockoffset用到
```



### Block

基本块

```
private ArrayList<IRCode> inblocklist;  // 基本块内所有指令
private String label;   // 标签名
private int num;    // 按顺序基本块编号

private Phi phi;    // 基本块的Phi函数
private String instr;   //branch跳转 的bne等类型
private String jumploc; //branch的跳转位置

public boolean global;   //是否全局
private String innerfunc;   // 所属函数名
```



### Function

N个基本块构成

```
private FuncHeader funcheader;  // 函数具体属性
private ArrayList<Block> blocklist;  // 函数内所有基本块
private int blocknum;    // 函数内基本块个数
public boolean voidreturn;  //返回值是否为空
```



##### FuncHeader

函数头

```
private Symbol.TYPE type;
private String fname;
private ArrayList<Symbol> paras;
```

