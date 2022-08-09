### Grammar-lang

> BNF范式：https://lists.llvm.org/pipermail/llvm-dev/2018-June/123851.html

#### TopLevel

有用的应该就是下面列的几个

```
TopLevelEntity
	: SourceFilename
	| TargetDefinition
	| ModuleAsm
	| TypeDef
	| ComdatDef
	| GlobalDecl
	| GlobalDef
	| IndirectSymbolDef
	| FunctionDecl
	| FunctionDef
	| AttrGroupDef
	| NamedMetadataDef
	| MetadataDef
	| UseListOrder
	| UseListOrderBB
```

#### Global

##### GlobalDecl

```
GlobalDecl
	: GlobalIdent "=" ExternLinkage OptPreemptionSpecifier [Opt] Immutable Type GlobalAttrs FuncAttrs
```

##### GlobalDef

```
GlobalDef
	: GlobalIdent "=" OptLinkage OptPreemptionSpecifier OptVisibility OptDLLStorageClass OptThreadLocal OptUnnamedAddr OptAddrSpace OptExternallyInitialized Immutable Type Constant GlobalAttrs FuncAttrs
```

##### Immutable

```
Immutable
	: "constant"
	| "global"
;
```

##### 

##### GlobalAttrs系列

```
GlobalAttrs
	: empty
	| "," GlobalAttrList
;

GlobalAttrList
	: GlobalAttr
	| GlobalAttrList "," GlobalAttr
;

GlobalAttr
	: Section
	| Comdat
	| Alignment
	//   ::= !dbg !57
	| MetadataAttachment
;
```

##### Alignment

```
Alignment
	: "align" int_lit
;
```



##### GlobalIdent

```
GlobalIdent
	: global_ident
```

##### global_ident

```
global_ident
	: _global_name
	| _global_id
```

##### _global_name

```
_global_name
	: '@' ( _name | _quoted_name )
```

##### _global_id

```
_global_id
	: '@' _id
```

##### FuncAttrs系列

```
FuncAttrs
	: empty
	| FuncAttrList
;

FuncAttrList
	: FuncAttr
	| FuncAttrList FuncAttr
;

FuncAttr
	// not used in attribute groups.
	: AttrGroupID
	// used in attribute groups.
	| "align" "=" int_lit
	| "alignstack" "=" int_lit
	// used in functions.
	| Alignment
	| AllocSize
	| StackAlignment
	| StringLit
	| StringLit "=" StringLit
	| "alwaysinline"
	| "argmemonly"
	| "builtin"
	| "cold"
	| "convergent"
	| "inaccessiblemem_or_argmemonly"
	| "inaccessiblememonly"
	| "inlinehint"
	| "jumptable"
	| "minsize"
	| "naked"
	| "nobuiltin"
	| "noduplicate"
	| "noimplicitfloat"
	| "noinline"
	| "nonlazybind"
	| "norecurse"
	| "noredzone"
	| "noreturn"
	| "nounwind"
	| "optnone"
	| "optsize"
	| "readnone"
	| "readonly"
	| "returns_twice"
	| "safestack"
	| "sanitize_address"
	| "sanitize_hwaddress"
	| "sanitize_memory"
	| "sanitize_thread"
	| "speculatable"
	| "ssp"
	| "sspreq"
	| "sspstrong"
	| "strictfp"
	| "uwtable"
	| "writeonly"
;
```

##### One of Attr

```
attr_group_id
	: '#' _id
;
```



#### Func

##### FunctionDecl

最后用的

```
FunctionDecl
	: "declare" MetadataAttachments OptExternLinkage FunctionHeader
```

##### FunctionDef

```
FunctionDef
	: "define" OptLinkage FunctionHeader MetadataAttachments FunctionBody
```

##### FunctionHeader

```
FunctionHeader
	: OptPreemptionSpecifier [Opt] ReturnAttrs Type GlobalIdent "(" Params ")" OptUnnamedAddr FuncAttrs [Opt]
```

##### ReturnAttrs系列

```
ReturnAttrs
	: empty
	| ReturnAttrList
;

ReturnAttrList
	: ReturnAttr
	| ReturnAttrList ReturnAttr
;

ReturnAttr
	: Alignment
	| Dereferenceable
	| StringLit
	| "inreg"
	| "noalias"
	| "nonnull"
	| "signext"
	| "zeroext"
;
```



##### FunctionBody

```
FunctionBody
	: "{" BasicBlockList UseListOrders "}"
```

##### Params系列

```
Params
	: empty
	| "..."
	| ParamList
	| ParamList "," "..."
;

ParamList
	: Param
	| ParamList "," Param
;

Param
	: Type ParamAttrs
	| Type ParamAttrs LocalIdent
;
```

##### ParamAttrs系列

```
ParamAttrs
	: empty
	| ParamAttrList
;

ParamAttrList
	: ParamAttr
	| ParamAttrList ParamAttr
;

ParamAttr
	: Alignment
	| Dereferenceable
	| StringLit
	| "byval"
	| "inalloca"
	| "inreg"
	| "nest"
	| "noalias"
	| "nocapture"
	| "nonnull"
	| "readnone"
	| "readonly"
	| "returned"
	| "signext"
	| "sret"
	| "swifterror"
	| "swiftself"
	| "writeonly"
	| "zeroext"
;
```



#### Block

##### BasicBlockList

```
BasicBlockList
	: BasicBlock
	| BasicBlockList BasicBlock
```

##### BasicBlock

```
BasicBlock
	: OptLabelIdent Instructions Terminator
```

#### Label

##### OptLabelIdent

```
OptLabelIdent
	: empty
	| LabelIdent
;
```



##### LabelIdent

```
LabelIdent
	: label_ident
;
```



##### label_ident

```
label_ident
	: ( _letter | _decimal_digit ) { _letter | _decimal_digit } ':'
	| _quoted_string ':'
;
```



#### Instruction

##### Instructions

```
Instructions
	: empty
	| InstructionList
```

##### InstructionList

```
InstructionList
	: Instruction
	| InstructionList Instruction
```

##### Instruction

```
Instruction
	// Instructions not producing values.
	: StoreInst
	| FenceInst
	| CmpXchgInst
	| AtomicRMWInst
	// Instructions producing values.
	| LocalIdent "=" ValueInstruction
	| ValueInstruction
```

##### ValueInstruction

```
ValueInstruction
	// Binary instructions
	: AddInst
	| FAddInst
	| SubInst
	| FSubInst
	| MulInst
	| FMulInst
	| UDivInst
	| SDivInst
	| FDivInst
	| URemInst
	| SRemInst
	| FRemInst
	// Bitwise instructions
	| ShlInst
	| LShrInst
	| AShrInst
	| AndInst
	| OrInst
	| XorInst
	// Vector instructions
	| ExtractElementInst
	| InsertElementInst
	| ShuffleVectorInst
	// Aggregate instructions
	| ExtractValueInst
	| InsertValueInst
	// Memory instructions
	| AllocaInst
	| LoadInst
	| GetElementPtrInst
	// Conversion instructions
	| TruncInst
	| ZExtInst
	| SExtInst
	| FPTruncInst
	| FPExtInst
	| FPToUIInst
	| FPToSIInst
	| UIToFPInst
	| SIToFPInst
	| PtrToIntInst
	| IntToPtrInst
	| BitCastInst
	| AddrSpaceCastInst
	// Other instructions
	| ICmpInst
	| FCmpInst
	| PhiInst
	| SelectInst
	| CallInst
	| VAArgInst
	| LandingPadInst
	| CatchPadInst
	| CleanupPadInst
```

##### Binary类

```
AddInst
	: "add" OverflowFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

FAddInst
	: "fadd" FastMathFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

SubInst
	: "sub" OverflowFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

FSubInst
	: "fsub" FastMathFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

MulInst
	: "mul" OverflowFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

FMulInst
	: "fmul" FastMathFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

UDivInst（未用到）
	: "udiv" OptExact Type Value "," Value OptCommaSepMetadataAttachmentList
;

SDivInst
	: "sdiv" OptExact Type Value "," Value OptCommaSepMetadataAttachmentList
;

FDivInst
	: "fdiv" FastMathFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

```



##### StoreInst

```
StoreInst
	: "store" OptVolatile Type Value "," Type Value OptCommaSepMetadataAttachmentList
	| "store" OptVolatile Type Value "," Type Value "," Alignment OptCommaSepMetadataAttachmentList
	| "store" "atomic" OptVolatile Type Value "," Type Value OptSyncScope AtomicOrdering OptCommaSepMetadataAttachmentList
	| "store" "atomic" OptVolatile Type Value "," Type Value OptSyncScope AtomicOrdering "," Alignment OptCommaSepMetadataAttachmentList
;
```

##### LoadInst

```
LoadInst
	// Load.
	: "load" OptVolatile Type "," Type Value OptCommaSepMetadataAttachmentList
	| "load" OptVolatile Type "," Type Value "," Alignment OptCommaSepMetadataAttachmentList
	// Atomic load.
	| "load" "atomic" OptVolatile Type "," Type Value OptSyncScope AtomicOrdering OptCommaSepMetadataAttachmentList
	| "load" "atomic" OptVolatile Type "," Type Value OptSyncScope AtomicOrdering "," Alignment OptCommaSepMetadataAttachmentList
```

##### CallInst

```
CallInst
	: OptTail "call" FastMathFlags OptCallingConv ReturnAttrs Type Value "(" Args ")" FuncAttrs OperandBundles OptCommaSepMetadataAttachmentList
;
```

##### 函数Arg系列

```
Args
	: empty
	| "..."
	| ArgList
	| ArgList "," "..."
;

ArgList
	: Arg
	| ArgList "," Arg
;

Arg
	: ConcreteType ParamAttrs Value
	| MetadataType Metadata
;
```

##### ICmpInst系列

```
ICmpInst
	: "icmp" IPred Type Value "," Value OptCommaSepMetadataAttachmentList
;

IPred
	: "eq"
	| "ne"
	| "sge"
	| "sgt"
	| "sle"
	| "slt"
	| "uge"
	| "ugt"
	| "ule"
	| "ult"
;
```

##### AllocaInst

```
AllocaInst
	: "alloca" OptInAlloca OptSwiftError Type OptCommaSepMetadataAttachmentList
	| "alloca" OptInAlloca OptSwiftError Type "," Alignment OptCommaSepMetadataAttachmentList
	| "alloca" OptInAlloca OptSwiftError Type "," Type Value OptCommaSepMetadataAttachmentList
	| "alloca" OptInAlloca OptSwiftError Type "," Type Value "," Alignment OptCommaSepMetadataAttachmentList
	| "alloca" OptInAlloca OptSwiftError Type "," AddrSpace OptCommaSepMetadataAttachmentList
	| "alloca" OptInAlloca OptSwiftError Type "," Alignment "," AddrSpace OptCommaSepMetadataAttachmentList
	| "alloca" OptInAlloca OptSwiftError Type "," Type Value "," AddrSpace OptCommaSepMetadataAttachmentList
	| "alloca" OptInAlloca OptSwiftError Type "," Type Value "," Alignment "," AddrSpace OptCommaSepMetadataAttachmentList
;

OptInAlloca
	: empty
	| "inalloca"
;

OptSwiftError
	: empty
	| "swifterror"
;
```

##### ZExtInst

```
ZExtInst
	: "zext" Type Value "to" Type OptCommaSepMetadataAttachmentList
;
```

##### SI与FP

```
SIToFPInst
	: "sitofp" Type Value "to" Type OptCommaSepMetadataAttachmentList
;

FPToSIInst
	: "fptosi" Type Value "to" Type OptCommaSepMetadataAttachmentList
;
```



#### Terminator

```
Terminator
	: RetTerm
	| BrTerm
	| CondBrTerm
	| SwitchTerm
	| IndirectBrTerm
	| InvokeTerm
	| ResumeTerm
	| CatchSwitchTerm
	| CatchRetTerm
	| CleanupRetTerm
	| UnreachableTerm
;
```

##### 各种Term

```
RetTerm
	// Void return.
	: "ret" VoidType OptCommaSepMetadataAttachmentList
	// Value return.
	| "ret" ConcreteType Value OptCommaSepMetadataAttachmentList
;

BrTerm
	: "br" LabelType LocalIdent OptCommaSepMetadataAttachmentList
;

CondBrTerm
	: "br" IntType Value "," LabelType LocalIdent "," LabelType LocalIdent OptCommaSepMetadataAttachmentList
;
```



##### Label

```
Label 	[-a-zA-Z$._0-9]+
```

##### LocalIdent

```
LocalIdent
	: local_ident
;
```

##### local系列

```
local_ident
	: _local_name
	| _local_id
;

_local_name
	: '%' ( _name | _quoted_name )
;

_local_id
	: '%' _id
;
```

#### Float

##### float_lit

```
float_lit
	: _frac_lit
	| _sci_lit
	| _float_hex_lit
```

##### _frac_lit

```
_frac_lit
	: [ _sign ] _decimals '.' { _decimal_digit }
```

##### _sci_lit

```
_sci_lit
	: _frac_lit ( 'e' | 'E' ) [ _sign ] _decimals
```

##### _float_hex_lit

```
_float_hex_lit
	:  '0' 'x'      _hex_digit { _hex_digit }
	|  '0' 'x' 'K'  _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit 
```

#### Array

##### GetElementPtrInst

```
GetElementPtrInst
	: "getelementptr" OptInBounds Type "," Type Value OptCommaSepMetadataAttachmentList
	| "getelementptr" OptInBounds Type "," Type Value "," CommaSepTypeValueList OptCommaSepMetadataAttachmentList
```



##### GetElementPtrExpr

```
GetElementPtrExpr
	: "getelementptr" OptInBounds "(" Type "," Type Constant "," GEPConstIndices ")"
```

##### OptInBounds

可选

```
OptInBounds
	: empty
	| "inbounds"
```



##### GEP系列

```
GEPConstIndices
	: empty
	| GEPConstIndexList
;

GEPConstIndexList
	: GEPConstIndex
	| GEPConstIndexList "," GEPConstIndex
;

GEPConstIndex
	: OptInrange Type Constant
;

OptInrange
	: empty
	| "inrange"
```

##### CommaSepTypeValue系列

```
CommaSepTypeValueList
	: TypeValue
	| CommaSepTypeValueList "," TypeValue
;

TypeValue
	: Type Value
;

OptCommaSepMetadataAttachmentList
	: empty
	| "," CommaSepMetadataAttachmentList
;

CommaSepMetadataAttachmentList
	: MetadataAttachment
	| CommaSepMetadataAttachmentList "," MetadataAttachment
;
```

##### Meta系列

应该无用

```
MetadataAttachment
	: MetadataName MDNode
;

MetadataName
	: metadata_name
;

MDNode
	// !{ ... }
	: MDTuple
	// !42
	| MetadataID
	| SpecializedMDNode
;

```



### Basic

#### Type系列

```
Type
	: VoidType
	| FuncType
	| FirstClassType
;

FirstClassType
	: ConcreteType
	| MetadataType
;
```

##### FuncType

```
FuncType
	: Type "(" Params ")"
;

```



##### ConcreteType

```
ConcreteType
	: IntType
	// Type ::= 'float' | 'void' (etc)
	| FloatType
	// Type ::= Type '*'
	// Type ::= Type 'addrspace' '(' uint32 ')' '*'
	| PointerType
	// Type ::= '<' ... '>'
	| VectorType
	| LabelType
	// Type ::= '[' ... ']'
	| ArrayType
	// Type ::= StructType
	| StructType
	// Type ::= %foo
	// Type ::= %4
	| NamedType
	| MMXType
	| TokenType
```

##### voidType

```
VoidType
	: "void"
```

##### ArrayType

```
ArrayType
	: "[" int_lit "x" Type "]"
;
```

##### LabelType

```
LabelType
	: "label"
```

##### IntType

```
IntType
	: int_type
;

int_type
	: 'i' _decimals
;
```

##### PointerType

```
PointerType
	: Type OptAddrSpace "*"
;
```



#### Value

```
Value
	: Constant
	// %42
	// %foo
	| LocalIdent
	| InlineAsm
;
```

#### Constants

##### Constant

```
Constant
	: BoolConst
	| IntConst
	| FloatConst
	| NullConst
	| NoneConst
	| StructConst
	| ArrayConst
	| CharArrayConst
	| VectorConst
	| ZeroInitializerConst
	// @42
	// @foo
	| GlobalIdent
	| UndefConst
	| BlockAddressConst
	| ConstantExpr
```

##### int/float

```
IntConst
	: int_lit
;

IntLit
	: int_lit
;

FloatConst
	: float_lit
```

##### lit系列

```
int_lit
	: _decimal_lit
;

_decimal_lit
	: [ '-' ] _decimals
;

_decimals
	: _decimal_digit { _decimal_digit }

float_lit
	: _frac_lit
	| _sci_lit
	| _float_hex_lit
;

_frac_lit
	: [ _sign ] _decimals '.' { _decimal_digit }
;
```

##### ArrayConst

```
ArrayConst
	: "[" TypeConsts "]"
;
```

#### ConstantExpr

```
ConstantExpr
	// Binary expressions
	: AddExpr
	| FAddExpr
	| SubExpr
	| FSubExpr
	| MulExpr
	| FMulExpr
	| UDivExpr
	| SDivExpr
	| FDivExpr
	| URemExpr
	| SRemExpr
	| FRemExpr
	// Bitwise expressions
	| ShlExpr
	| LShrExpr
	| AShrExpr
	| AndExpr
	| OrExpr
	| XorExpr
	// Vector expressions
	| ExtractElementExpr
	| InsertElementExpr
	| ShuffleVectorExpr
	// Aggregate expressions
	| ExtractValueExpr
	| InsertValueExpr
	// Memory expressions
	| GetElementPtrExpr
	// Conversion expressions
	| TruncExpr
	| ZExtExpr
	| SExtExpr
	| FPTruncExpr
	| FPExtExpr
	| FPToUIExpr
	| FPToSIExpr
	| UIToFPExpr
	| SIToFPExpr
	| PtrToIntExpr
	| IntToPtrExpr
	| BitCastExpr
	| AddrSpaceCastExpr
	// Other expressions
	| ICmpExpr
	| FCmpExpr
	| SelectExpr
;
```



#### id & name

```
_name
	: _letter { _letter | _decimal_digit }
;

_escape_name
	: _escape_letter { _escape_letter | _decimal_digit }
;

_quoted_name
	: _quoted_string
;

_id
	: _decimals
;
```

#### digits & decimal

```
_letter
	: _ascii_letter
	| '$'
	| '-'
	| '.'
	| '_'
;

_escape_letter
	: _letter
	| '\\'
;

_decimal_digit
	: '0' - '9'
;

_hex_digit
	: _decimal_digit
	| 'A' - 'F'
	| 'a' - 'f'
;

_decimals
	: _decimal_digit { _decimal_digit }
```



### Optimal

#### OptLinkage

```
OptLinkage
	: empty
	| Linkage
;

Linkage
	: "appending"
	| "available_externally"
	| "common"
	| "internal"
	| "linkonce"
	| "linkonce_odr"
	| "private"
	| "weak"
	| "weak_odr"
;
```

#### OptUnnamedAddr

```
OptUnnamedAddr
	: empty
	| UnnamedAddr
;

UnnamedAddr
	: "local_unnamed_addr"
	| "unnamed_addr"
;
```

