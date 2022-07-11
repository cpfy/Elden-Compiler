package midCode;

public enum MidCodeType {
    PLUSOP, //+
    MINUOP, //-
    MULTOP, //*
    DIVOP,  // /
    MODOP,  //%
    LSSOP,  //<
    LEQOP,  //<=
    GREOP,  //>
    GEQOP,  //>=
    EQLOP,  //==
    NEQOP,  //!=
    ASSIGNOP,  //=
    GOTO,  //无条件跳转
    BZ,    //不满足条件跳转
    BNZ,   //满足条件跳转
    PUSH,  //函数调用时参数传递
    CALL,  //函数调用
    RET,   //函数返回语句
    RETVALUE, //有返回值函数返回的结果
    SCAN,  //读
    PRINT, //写
    LABEL, //标号
    CONST, //常量
    ARRAY, //数组
    VAR,   //变量
    FUNC,  //函数定义
    PARAM, //函数参数
    GETARRAY,  //取数组的值  t = a[]
    PUTARRAY,  //给数组赋值  a[] = t
    EXIT, //退出 main最后
    JUMP
}
