## IR & Arm指令对应关系

### ops

| TAG  | llvm ir  | usage                                                        | intro                                                        |
| ---- | -------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Add  | add      | `<result> = add <ty> <op1>, <op2> ; yields ty:result`        |                                                              |
| Sub  | sub      | `<result> = sub `<ty`> `<op1`>, `<op2`> ; yields ty:result`  |                                                              |
| Rsb  | /        |                                                              | 配合arm                                                      |
| Mul  | mul      | `<result> = mul <ty> <op1>, <op2>`                           |                                                              |
| Div  | sdiv     | `<result> = sdiv <ty> <op1>, <op2>`                          |                                                              |
| Mod  | srem     | `<result> = srem <ty> <op1>, <op2> ; yields ty:result`       | The ‘`srem`’ instruction returns the remainder from the signed division of its two operands. This instruction can also take [vector](https://llvm.org/docs/LangRef.html#t-vector) versions of the values in which case the elements must be integers. 但是我们要做运算强度削弱，所以在asm级别要转换 |
| Lt   | icmp slt | `<result> = icmp <cond> <ty> <op1>, <op2> ; yields i1 or <N x i1>:result` |                                                              |
| Le   | icmp sle | 同上                                                         |                                                              |
| Ge   | icmp sge | 同上                                                         |                                                              |
| Gt   | icmp sgt | 同上                                                         |                                                              |
| Eq   | icmp sq  | 同上                                                         |                                                              |
| Ne   | icmp ne  | 同上                                                         |                                                              |
| And  | and      | `<result> = and <ty> <op1>, <op2> ; yields ty:result`        |                                                              |
| Or   | or       | `<result> = or <ty> <op1>, <op2> ; yields ty:result`         |                                                              |

### terminator insts

| TAG                   | llvm ir | usage                                                        | intro                                                        |
| --------------------- | ------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Br                    | br      | `br i1 <cond>, label <iftrue>, label <iffalse>` `br label <dest>` | cause control flow to transfer to a different basic block ** |
| in current function** |         |                                                              |                                                              |
| Ret                   | ret     | `ret <type> <value> ``ret void`                              | return control flow(optionally a value)                      |
| Call                  | call    | ` = [tail                                                    | musttail                                                     |

### memoryops

| TAG    | llvm ir       | usage                                                        | intro                                                      |
| ------ | ------------- | ------------------------------------------------------------ | ---------------------------------------------------------- |
| Alloca | alloca        | `<result> = alloca [inalloca] <type> [, <ty> <NumElements>] [, align <alignment>] [, addrspace(<num>)] ; yields type addrspace(num)*:result` | allocate memory in current stack frame                     |
| Load   | load          | `<result> = load [volatile] <ty>, <ty>* <pointer>[, align <alignment>][, !nontemporal !][, !invariant.load !<empty_node>][, !invariant.group !][, !nonnull !<empty_node>][, !dereferenceable !][, !dereferenceable_or_null !<deref_bytes_node>][, !align !][, !noundef !<empty_node>]` | read memory                                                |
| Store  | store         | `store [volatile] <ty> <value>, <ty>* <pointer>[, align <alignment>][, !nontemporal !<nontemp_node>][, !invariant.group !<empty_node>] ; yields void` | write memory                                               |
| GEP    | getelementptr | `<result> = getelementptr <ty>, * {, [inrange] <ty> <idx>}*` `<result> = getelementptr inbounds <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*` `<result> = getelementptr <ty>, <ptr vector> <ptrval>, [inrange] <vector index type> <idx>` | this inst only calculate memory,do not read or load memory |
| Phi    | phi           | `<result> = phi [fast-math-flags] <ty> [ <val0>, <label0>], ...` |                                                            |
| zext   | zext..to      | = zext to ; yields ty2                                       | zext                                                       |









## Basic

```assembly
AREA Example,CODE,READONLY    ;声明代码段Example
ENTRY ;程序入口

Start
	mov r0, #0xffffffff
	B OVER

OVER
	END
```

