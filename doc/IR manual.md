## 入门

一个简单的IR实例程序

```c#
// main.c
int foo(int first, int second) {
    return first + second;
}

int a = 5;

int main() {
    int b = 4;
    return foo(a, b);
}
```
将上面的c语言程序翻译成LLVM IR语言后可得到如下代码，还加上了相关的注释：
```elixir
; 所有的全局变量都以 @ 为前缀，后面的 global 关键字表明了它是一个全局变量
@a = global i32 5 ; 注意，@a 的类型是 i32* ，后面会详细说明

; 函数定义以 `define` 开头，i32 标明了函数的返回类型，其中 `foo`是函数的名字，`@` 是其前缀
; 函数参数 (i32 %0, i32 %1) 分别标明了其第一、第二个参数的类型以及他们的名字
define i32 @foo(i32 %0, i32 %1)  { ; 第一个参数的名字是 %0，类型是 i32；第二个参数的名字是 %1，类型是 i32。
  ; 以 % 开头的符号表示虚拟寄存器，你可以把它当作一个临时变量（与全局变量相区分），或称之为临时寄存器
  %3 = alloca i32 ; 为 %3 分配空间，其大小与一个 i32 类型的大小相同。%3 类型即为 i32*
  %4 = alloca i32 ; 同理，%4 类型为 i32*

  store i32 %0, i32* %3 ; 将 %0（i32）存入 %3（i32*）
  store i32 %1, i32* %4 ; 将 %1（i32）存入 %4（i32*）

  %5 = load i32, i32* %3 ; 从 %3（i32*）中 load 出一个值（类型为 i32），这个值的名字为 %5
  %6 = load i32, i32* %4 ; 同理，从 %4（i32*） 中 load 出一个值给 %6（i32）

  %7 = add nsw i32 %5, %6 ; 将 %5（i32） 与 %6（i32）相加，其和的名字为 %7。nsw 是 "No Signed Wrap" 的缩写，表示无符号值运算

  ret i32 %7 ; 返回 %7（i32）
}

define i32 @main() {
  ; 注意，下面出现的 %1，%2……与上面的无关，即每个函数的临时寄存器是独立的
  %1 = alloca i32
  %2 = alloca i32

  store i32 0, i32* %1
  store i32 4, i32* %2

  %3 = load i32, i32* @a
  %4 = load i32, i32* %2

  ; 调用函数 @foo ，i32 表示函数的返回值类型
  ; 第一个参数是 %3（i32），第二个参数是 %4（i32），给函数的返回值命名为 %5
  %5 = call i32 @foo(i32 %3, i32 %4)

  ret i32 %5
}
```
虽然上面这个文件并没有包含本实验中可能使用到的所有特性与指令，但是已经展现出了很多值得注意的地方，比如：
* 注释以 `;` 开头
* LLVM IR 是静态类型的（即在编写时每个值都有明确的类型）
* 局部变量的作用域是单个函数（比如 `@main` 中的 `%1` 是一个 `i32*` 类型的地址，而 `@foo` 中的 `%1` 是一个 `i32` 类型的值）
* 临时寄存器（或者说临时变量）拥有升序的名字（比如 `@main` 函数中的 `%1`，`%2`，`%3`）
* 全局变量与局部变量由前缀区分，全局变量和函数名以 `@` 为前缀，局部变量以 `%` 为前缀
* 大多数指令与字面含义相同
    * `alloca` 分配内存并返回地址；
    * `load` 从内存读出值；
    * `store` 向内存存值；
    * `add` 用于加法；
    * 等等...


## 标识符与变量

LLVM标识符有2个基本类型：全局标识符和局部标识符。全局标识符（包括函数，全局变量，和全局常量）以“@”字符开头，而局部标识符（分配临时寄存器的变量，包括函数形参、局部变量和临时变量）以'％'字符开头。

如下：

```c#
@a = global i32 5  ;全局变量a用@a表示

define i32 @foo(i32 %0, i32 %1) {  ;函数名foo用@foo表示，第一个函数形参用%0表示，第二个函数形参用%1表示
  %2 = alloca i32  ;局部变量用%1表示
  ret i32 0
}
```
## 函数

* 函数定义
一个函数定义的最简单的语法结构为`define+ 返回值类型 (i32) + 函数名 (@foo) + 参数列表 ((i32 %0,i32 %1)) + 函数体 ({ret 返回值(i32 0)})`

```c#
int foo(int a, int b) {
  return 0;
}

int main() {
  return 0;
}
```
将上述c语言代码翻译成LLVM IR代码后得：
```elixir
define i32 @foo(i32 %a,i32 %b) {
    ret i32 0 ; 返回 i32 类型的值 0
}

define i32 @main() {
    ret i32 0 ; 返回 i32 类型的值 0
}
```
* 函数声明
除了函数定义以外，函数声明也是非常常见的，我们在一个`module`里，如果想要调用别的模块的函数，就需要在本模块先声明这个函数。在本实验中，要使用库函数，你可能需要用函数声明的形式在你生成的`.ll`文件里声明库函数的名字（我们将在下面做出示例）函数声明的结构也比较简单，就是使用`declare`关键词替换`define`，并且没有函数体。比如，下面是一些你在后续实验中可能会用到的库函数的函数声明：

```elixir
declare i32 @getint()
declare i32 @getarray(i32*)
declare i32 @getch()
declare void @putint(i32)
declare void @putch(i32)
declare void @putarray(i32,i32*)
```
* 函数调用
函数调用的语法结构为`call + 返回值类型 (i32) + 函数名 (@foo) + 实参列表 ((i32 %0,i32 %1)))` 

```plain
i = foo(x, y);
```
翻译后如下：
```plain
%i = call i32 @foo(%x, %y)
```
* 函数返回
函数返回的语法结构为`ret + 返回值类型 (i32) + 返回值(%0或0）` 

```plain
return 0;
return x;  //某个临时变量x
```
对应翻译为：
```c#
ret i32 0  ;返回值0
ret i32 %i  ;返回临时变量%i
```

## 分支和跳转

* label标签
用label来表示分支跳转的目标地址

```plain
10:  ;表示标签10
  store i32 5, i32* %4 
  br label %10  ;表示跳转到标签10
```
值得注意的是，label是一种系统类型，在引用时需要写成`类型 (label) + 寄存器值(%10)` 的形式
* icmp比较指令
icmp指令的操作数类型是整型或整型向量（指针或指针向量），其语法规则如下：

```plain
<result> = icmp <cond> <type> <op1>, <op2>
```
*  cond表示比较类型，共如下3条规则：
    * eq和ne分别表示相等和不相等；
    * 无符号比较ugt、uge、ult、ule分别表示大于、大于等于、小于、小于等于
    * 有符号比较sgt、sge、slt、sle分别与无符号比较相对应
* op1表示第一个比较数，op2表示第二个比较数，icmp结果返回一个i1类型的数，用来表示结果是否为真
举例来说：

```plain
<result>= icmp eq i32 4, 5        ; result=false
<result> = icmp ne float* %X, %X  ; result=false
<result> = icmp ult i16 4, 5      ; result=true
<result> = icmp sgt i16 4, 5      ; result=false
```
* br跳转指令
br指令的语法规则如下：

```plain
br i1 <cond>, label <iftrue>, label <iffalse> //有条件跳转
br label <dest>   //无条件跳转
```

可以看到`br`跳转包括`无条件跳转`和`有条件跳转`

 `cond`表示跳转条件：

 第一个lable表示如果条件为`true`要跳转到哪一个基本块的标签（用来标记该基本块的入口）

 第二个label表示如果比较条件`false`要跳转的基本块。

以下面的示例为例：

```plain
 br i1 %7, label %8, label %9 
```
如果局部变量%7的值为真，则跳转到标签为label %8的基本块执行，否则跳转到标签为label %9的基本块执行。


至于无条件跳转br指令就很容易理解了，直接跳转至标签为dest的基本块执行。

```c#
  br label %10 
```
* 条件分支
使用以上的label标签、icmp比较指令和br跳转指令就可以写出条件分支与循环跳转了

```c#
// ...
if (a == b) {
  c = 5;
}
else {
  c = 10;
}
// ...
```
 翻译成IR后的代码如下：
```c#
// ...
%cond = icmp eq i32 %a, %b
br i1 %cond, label %1, label %2

1:
  store i32 5, i32* %c
  br %3
2:
  store i32 10, i32* %c
  br %3
  
3:
// ...
```
* 循环跳转
```c#
int main() {
    int n = getint();
    int i = 0, sum = 0;
    while (i < n) {
        i = i + 1;
        sum = sum + i;
        printf("%d", sum);
        printf("%d", 10);
    }
    return 0;
}
```
翻译后的代码为：
```elixir
declare i32 @getint()
declare void @putint(i32)
declare void @putch(i32)
define dso_local i32 @main() {
    %1 = alloca i32
    %2 = alloca i32
    %3 = alloca i32
    %4 = call i32 @getint()
    store i32 %4, i32* %3
    store i32 0, i32* %2
    store i32 0, i32* %1
    br label %5

5:
    %6 = load i32, i32* %2
    %7 = load i32, i32* %3
    %8 = icmp slt i32 %6, %7
    br i1 %8, label %9, label %16

9:
    %10 = load i32, i32* %2
    %11 = add i32 %10, 1
    store i32 %11, i32* %2
    %12 = load i32, i32* %1
    %13 = load i32, i32* %2
    %14 = add i32 %12, %13
    store i32 %14, i32* %1
    %15 = load i32, i32* %1
    call void @putint(i32 %15)
    call void @putch(i32 10)
    br label %5

16:
    ret i32 0
}
```

## 数组

* getelementptr指令
GetElementPtr指令其实是一条指针计算语句，本身并不进行任何数据的访问或修改，进行是计算指针，修改计算后指针的类型。

GetElementPtr至少有两个参数，第一个参数为要进行计算的原始指针，往往是一个结构体指针，或数组首地址指针。第二个参数及以后的参数，都称为`indices`，表示要进行计算的参数，相当于offset，作用在第二个参数给出的初始指针，如结构体的第几个元素，数组的第几个元素。

我们先来看一段LLVM官网上的示例：

```plain
struct munger_struct {
  int f1;
  int f2;
};
void munge(struct munger_struct *P) {
  P[0].f1 = P[1].f1 + P[2].f2;
}
...
munger_struct Array[3];
...
munge(Array);
```
`munge`函数会编译成如下IR：
```plain
void %munge(%struct.munger_struct* %P) {
entry:
  %tmp = getelementptr %struct.munger_struct* %P, i32 1, i32 0
  %tmp = load i32* %tmp
  %tmp6 = getelementptr %struct.munger_struct* %P, i32 2, i32 1
  %tmp7 = load i32* %tmp6
  %tmp8 = add i32 %tmp7, %tmp
  %tmp9 = getelementptr %struct.munger_struct* %P, i32 0, i32 0
  store i32 %tmp8, i32* %tmp9
  ret void
}
```
我们结合示例，来对应看一下是如何工作的：
```plain
P[0].f1 
```
这是示例代码中的被赋值指针，我们C语言的经验告诉我们，首先P[0]的地址就是数组的首地址，而f1又是结构体的第一个参数，那么P的地址就是我们最终要放置数据的结构地址。
这条地址计算对应如下语句：

```plain
%tmp9 = getelementptr %struct.munger_struct* %P, i32 0, i32 0
```
我们发现参数是两个0，这两个0含义不大一样，第一个0是数组计算符，并不会改变返回的类型，因为，我们任何一个指针都可以作为一个数组来使用，进行对应的指针计算，所以这个0并不会省略。
第二个0是结构体的计算地址，表示的是结构体的第0个元素的地址，这时，会根据结构体指针的类型，选取其中的元素长度，进行计算，最后返回的则是结构体成员的指针。

同理，我们可以对照参考这两条语句：

```plain
P[1].f1
P[2].f2
```
对应的计算翻译后为：
```plain
%tmp = getelementptr %struct.munger_struct* %P, i32 1, i32 0
%tmp6 = getelementptr %struct.munger_struct* %P, i32 2, i32 1
```
注意事项：
* 首先，不是全部的indices都必须是i32，也可以是i64，但结构体的计算地址，也就是上面例子中的第二个数字，必须是i32；
* GEP x,1,0,0 和 GEP x,1 计算后的地址是一样的，但类型不一样，所以千万注意不要在语句后添加多余的0。
* 数组定义
示例1：

```plain
int a[3];
```
对应LLVM IR：
```plain
%a = alloca [3 x i32]
```
示例2：
```plain
int a[2][2] = {{1}, {2, 3}};
```
对应LLVM IR：
```plain
%1 = alloca [2 x [2 x i32]]
%2 = alloca [2 x [2 x i32]]
%3 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %2, i32 0, i32 0
%4 = getelementptr [2 x i32], [2 x i32]* %3, i32 0, i32 0
call void @memset(i32* %4, i32 0, i32 16)
store i32 1, i32* %4
%5 = getelementptr i32, i32* %4, i32 2
store i32 2, i32* %5
%6 = getelementptr i32, i32* %4, i32 3
store i32 3, i32* %6
```

* 数组读取
```plain
z = a[x][y]; // 假设总大小为a[4][2]
```
对应IR：
```plain
%2 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %1, i32 0, i32 0
%3 = add i32 0, x
%4 = mul i32 %3, 2
%5 = getelementptr [2 x i32], [2 x i32]* %2, i32 0, i32 0
%6 = add i32 %4, y
%7 = getelementptr i32, i32* %5, i32 %6
%8 = load i32, i32* %31
```

#### 

* 数组赋值
```plain
a[x][y] = z; // a[4][2]
```
对应IR：（与读取的区别仅在于最后一步load变为store）
```plain
%2 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %1, i32 0, i32 0
%3 = add i32 0, x
%4 = mul i32 %3, 2
%5 = getelementptr [2 x i32], [2 x i32]* %2, i32 0, i32 0
%6 = add i32 %4, y
%7 = getelementptr i32, i32* %5, i32 %6
%8 = store i32 z, i32* %31
```

## IR基本结构

### 模块

是一份LLVM IR的顶层容器，对应于编译前端的每个翻译单元（TranslationUnit）。每个模块由目标机器信息、全局符号（全局变量和函数）及元信息组成。

### 函数

编程语言中的函数，包括函数签名和若干个基本块，函数内的第一个基本块叫做入口基本块。

### 基本块

一组顺序执行的指令集合，只有一个入口和一个出口，非头尾指令执行时不会违背顺序跳转到其他指令上去。每个基本块最后一条指令一般是跳转指令（跳转到其它基本块上去），函数内最后一个基本块的最后条指令是函数返回指令。

### 指令

LLVM IR中的最小可执行单位，每一条指令都单占一行

### IR指令集表

|llvm ir|usage|intro|
|:----|:----|:----|
|add|<result> = add <ty> <op1>, <op2>|/|
|sub|<result> = sub <ty> <op1>, <op2>|/|
|mul|<result> = mul <ty> <op1>, <op2>|/|
|sdiv|<result> = sdiv <ty> <op1>, <op2>|有符号除法|
|icmp|<result> = icmp <cond> <ty> <op1>, <op2>|比较指令|
|and|<result> = and <ty> <op1>, <op2>|与|
|or|<result> = or <ty> <op1>, <op2>|或|
|call|<result> = call [ret attrs] <ty> <fnptrval>(<function args>)|函数调用|
|alloca|<result> = alloca <type>|分配内存|
|load|<result> = load <ty>, <ty>* <pointer>|读取内存|
|store|store <ty> <value>, <ty>* <pointer>|写内存|
|br|br i1 <cond>, label <iftrue>, label <iffalse> br label <dest>|改变控制流|
|ret|ret <type> <value> ,ret void|退出当前函数，并返回值（可选）|

### 其余关键词

| llvm ir       | usage | intro |
| :------------ | :---- | :---- |
| getelementptr |       | /     |
| memset        |       | /     |
| define        |       | /     |
| global        |       | /     |
| i32           |       | /     |



## 参考资料

1. [https://www.daimajiaoliu.com/daima/48711490a100405](https://www.daimajiaoliu.com/daima/48711490a100405) LLVM IR 语法
2. [https://www.zhihu.com/column/llvm-tutorial](https://www.zhihu.com/column/llvm-tutorial) LLVM入门笔记
3. [LLVM IR 三部曲之一 --- IR语法 - 简书 (jianshu.com)](https://www.jianshu.com/p/2e8863737cdd) LLVM IR语法简书
4. [https://blog.csdn.net/qq_37206105/article/details/115274241](https://blog.csdn.net/qq_37206105/article/details/115274241) LLVM IR语法概述
5. [http://static.kancloud.cn/digest/xf-llvm/162268](http://static.kancloud.cn/digest/xf-llvm/162268) 深入理解GetElementPtr
6. [https://llvm.org/docs/tutorial/index.html](https://llvm.org/docs/tutorial/index.html) LLVM IR 官网





