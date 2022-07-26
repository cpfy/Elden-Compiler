

#### llvm示例

```

```





### Instr

最基础的 “指令” 类，包括

```
private String instrname;   //N种中间代码种类

//todo
```

#### AssignInstr

以下均为Instr子类

```
private Ident localident;
private Instr valueinstr;
```

#### CallInst

```
private Type returntype;
private String funcname;
ArrayList<TypeValue> args;
```

#### GetElementPtrInst

```
private Type type1;
private Type type2;
private Value v;
```

#### IcmpInst

```
public class IcmpInst extends Instr {
    private String ipred;
    private Type t;
    private Value v1;
    private Value v2;
```

#### StoreInstr

```
public class StoreInstr extends Instr{
    private Type t1;
    private Type t2;
    private Value v1;
    private Value v2;
```

#### BinaryInst

```
public class BinaryInst extends Instr {
    private Type t;
    private Value v1;
    private Value v2;
```



#### RetTerm

```
public class RetTerm extends Instr{
    private Type retype;
    private Value v;
```

#### BrTerm

```
public class BrTerm extends Instr {
    private Ident li;
```

#### CondBrTerm

```
public class CondBrTerm extends Instr {
    private Value v;
    private Ident l1;
    private Ident l2;
```



### Block

基本块

```
// 基本块
private ArrayList<Instr> inblocklist;  // 基本块内所有指令
private String label;   // 标签名
private int num;    // 按顺序基本块编号1-n

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

